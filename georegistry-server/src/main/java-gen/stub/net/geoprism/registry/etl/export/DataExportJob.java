package net.geoprism.registry.etl.export;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.dhis2.dhis2adapter.DHIS2Facade;
import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.HTTPResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.RemoteConnectionException;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.postgres.GeoObjectQuery;

public class DataExportJob extends DataExportJobBase
{
  private static final long serialVersionUID = -1821569567;
  
  private static final Logger logger = LoggerFactory.getLogger(DataExportJob.class);
  
  //Set to VALIDATE and the remote DHIS2 server will not commit, only "dry-run".
  private static final String IMPORT_MODE      = "COMMIT";
  
  private SynchronizationConfig syncConfig;
  
  private DHIS2SyncConfig dhis2Config;
  
  private DHIS2Facade dhis2;
  
  public DataExportJob()
  {
    super();
  }
  
  private class ExportError extends Exception
  {
    private static final long serialVersionUID = 8463740942015611693L;

    protected HTTPResponse response;
    
    protected Throwable error;
    
    private ExportError(HTTPResponse response, Throwable t)
    {
      this.response = response;
      this.error = t;
    }
  }

  @Override
  public synchronized JobHistory start()
  {
    throw new UnsupportedOperationException();
  }

  public synchronized ExportHistory start(SynchronizationConfig configuration)
  {
    return executableJobStart(configuration);
  }

  private ExportHistory executableJobStart(SynchronizationConfig configuration)
  {
    JobHistoryRecord record = startInTrans(configuration);

    this.getQuartzJob().start(record);

    return (ExportHistory) record.getChild();
  }

  @Transaction
  private JobHistoryRecord startInTrans(SynchronizationConfig configuration)
  {
    ExportHistory history = (ExportHistory) this.createNewHistory();

    // TODO
    // configuration.setHistoryId(history.getOid());
    // configuration.setJobId(this.getOid());

    // history.appLock();
    // history.setConfig(configuration);
    // history.apply();

    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();

    return record;
  }
  
  /**
   * We want the actual API version, which is different than the DHIS2 core version.
   * This function will consume the DHIS2 version and return an API version.
   * 
   * @return
   */
  private String getAPIVersion()
  {
    String in = this.dhis2Config.getSystem().getVersion();
    
    if (in.startsWith("2.3.1"))
    {
      return "31";
    }
    
    return "31"; // We currently only support API version 31 right now anyway
  }
  
  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    ExportHistory history = (ExportHistory) executionContext.getJobHistoryRecord().getChild();
    
    this.syncConfig = this.getConfig();
    this.dhis2Config = (DHIS2SyncConfig) this.getConfig().buildConfiguration();
    
    DHIS2ExternalSystem system = this.dhis2Config.getSystem();

    HTTPConnector connector = new HTTPConnector();
    connector.setServerUrl(system.getUrl());
    connector.setCredentials(system.getUsername(), system.getPassword());
    
    dhis2 = new DHIS2Facade(connector, this.getAPIVersion());

    this.setStage(history, ExportStage.EXPORT);
    
    this.doExport();
    
    this.setStage(history, ExportStage.COMPLETE);
  }
  
  private void setStage(ExportHistory history, ExportStage stage)
  {
    history.appLock();
    history.clearStage();
    history.addStage(stage);
    history.apply();
  }
  
  public OIterator<GeoObject> postgresQuery(ServerGeoObjectType got)
  {
    GeoObjectQuery goq = new GeoObjectQuery(got);
    
    goq.orderBy(DefaultAttribute.LAST_UPDATE_DATE.getName(), SortOrder.ASC);
    
    return goq.getIterator();
  }
  
  private void doExport()
  {
    List<SyncLevel> levels = this.dhis2Config.getLevels();
    
    for (SyncLevel level : levels)
    {
      OIterator<GeoObject> it = this.postgresQuery(level.getGeoObjectType());
      
      it.forEach(go -> {
        try
        {
          exportGeoObject(level, go);
        }
        catch (ExportError ee)
        {
          recordExportError(ee);
        }
      });
      
//            List<SyncLevel> levels = this.dhis2Config.getLevels();
//            
//            for (SyncLevel level : levels)
//            {
//              ServerGeoObjectType got = level.getGeoObjectType();
//              
//              GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, this.syncConfig.getServerHierarchyType(), null, true, GeoObjectExportFormat.JSON_DHIS2, this.syncConfig.getExternalSystem(), -1, -1);
//              exporter.setDHIS2Facade(this.dhis2);
//              exporter.setSyncLevel(level);
//              exporter.writeObjects(jw);
//            }
    }
  }
  
  private void recordExportError(ExportError ee)
  {
    HTTPResponse resp = ee.response;
    Throwable ex = ee.error;
    
    if (resp != null)
    {
      // TODO : do stuff
    }
    
    
    // TODO : Record the error or something
    if (ex instanceof RuntimeException)
    {
      throw (RuntimeException) ex;
    }
    else
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * It's important that we try our best to maintain an accurate state between our database and the DHIS2 database.
   * The DHIS2Serailizer will create new ids and save them as externalIds for GeoObjects that do not have externalIds.
   * If our push to DHIS2 for this new GeoObject fails for whatever reason, then we want to rollback our database
   * so that we do not store the id which does not exist in the DHIS2 database.
   * 
   * TODO : In the future perhaps we should directly ask DHIS2 if an object exists and then we'll know
   * 
   * @param level
   * @param go
   * @return
   * @throws ExportError 
   */
  @Transaction
  private void exportGeoObject(SyncLevel level, GeoObject go) throws ExportError
  {
    MetadataImportResponse resp = null;
    
    try
    {
      VertexServerGeoObject serverGo = new VertexGeoObjectStrategy(level.getGeoObjectType()).getGeoObjectByCode(go.getCode());
      
      boolean isNew = serverGo.getExternalId(this.syncConfig.getExternalSystem()) == null;
      
      if (isNew && level.getSyncType() != SyncLevel.Type.ALL)
      {
        // TODO : Maybe we want to throw an error here instead?
        // Also, logging is OK for now but ultimately it might be too noisy
        
        logger.warn("Skipping GeoObject [" + go.getCode() + "] because it is new and sync type does not equal all.");
        return;
      }
      
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(GeoObject.class, new DHIS2GeoObjectJsonAdapters.DHIS2Serializer(this.dhis2, level, level.getGeoObjectType(), this.syncConfig.getServerHierarchyType(), this.syncConfig.getExternalSystem()));
      String sJson = builder.create().toJson(go, go.getClass());
      
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("mergeMode", "MERGE"));
      
      try
      {
        if (!isNew)
        {
          resp = dhis2.entityIdPatch("organisationUnits", serverGo.getExternalId(this.syncConfig.getExternalSystem()), params, new StringEntity(sJson));
        }
        else
        {
          resp = dhis2.entityPost("organisationUnits", params, new StringEntity(sJson));
        }
      }
      catch (UnsupportedEncodingException | InvalidLoginException | HTTPException e)
      {
        RemoteConnectionException rce = new RemoteConnectionException(e);
        throw rce;
      }
  
      if (!resp.isSuccess())
      {
        ExportRemoteException re = new ExportRemoteException();
        
        if (resp.hasErrorMessage())
        {
          re.setRemoteError(resp.getErrorMessage());
        }
        
        throw re;
      }
    }
    catch (Throwable t)
    {
      ExportError er = new ExportError(resp, t);
      throw er;
    }
  }
  
  /*
   * This method was used when we were posting to the metadata endpoint and doing bulk operations.
   * We had to switch over to object-by-object posting because it's the only endpoint that supports
   * "partial updates". I'm keeping it here for a little bit in case we need it for some reason.
   */
//  private void write(OutputStream out) throws IOException
//  {
//    try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(out, Charset.forName("UTF-8"))))
//    {
//      jw.beginObject();
//      {
//        jw.name("organisationUnits").beginArray();
//        {
//          List<SyncLevel> levels = this.dhis2Config.getLevels();
//          
//          for (SyncLevel level : levels)
//          {
//            ServerGeoObjectType got = level.getGeoObjectType();
//            
//            GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, this.syncConfig.getServerHierarchyType(), null, true, GeoObjectExportFormat.JSON_DHIS2, this.syncConfig.getExternalSystem(), -1, -1);
//            exporter.setDHIS2Facade(this.dhis2);
//            exporter.setSyncLevel(level);
//            exporter.writeObjects(jw);
//          }
//        } jw.endArray();
//      } jw.endObject();
//    }
//  }
  
//  public InputStream export() throws IOException
//  {
//    PipedOutputStream pos = new PipedOutputStream();
//    PipedInputStream pis = new PipedInputStream(pos);
//
//    Thread exportThread = new Thread(new Runnable()
//    {
//      @Override
//      public void run()
//      {
//        try
//        {
//          try
//          {
//            runInReq();
//          }
//          finally
//          {
//            pos.close();
//          }
//        }
//        catch (IOException e)
//        {
//          logger.error("Error while writing", e);
//        }
//      }
//      
//      @Request
//      public void runInReq()
//      {
//        try
//        {
//          DataExportJob.this.write(pos);
//        }
//        catch (Throwable t)
//        {
//          logger.error("Data Export Job encountered error while writing to stream.", t);
//          
//          if (t instanceof RuntimeException)
//          {
//            throw (RuntimeException) t;
//          }
//          else
//          {
//            throw new RuntimeException(t);
//          }
//        }
//      }
//    });
//    exportThread.setDaemon(true);
//    exportThread.start();
//
//    return pis;
//  }

  @Override
  protected JobHistory createNewHistory()
  {
    ExportHistory history = new ExportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ExportStage.CONNECTING);
    history.apply();

    return history;
  }

  public boolean canResume()
  {
    return false;
  }

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }

  public static List<? extends DataExportJob> getAll(SynchronizationConfig config)
  {
    DataExportJobQuery query = new DataExportJobQuery(new QueryFactory());
    query.WHERE(query.getConfig().EQ(config));

    try (OIterator<? extends DataExportJob> it = query.getIterator())
    {
      return it.getAll();
    }
  }
}
