package net.geoprism.registry.etl.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
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
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.registry.SSLTrustConfiguration;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.RemoteConnectionException;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;

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
  
  private String getDhis2Version()
  {
//    String in = this.dhis2Config.getSystem().getVersion();
//    
//    if (in.equals(""))
    
    return "31"; // TODO : Actually use the version
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
    
    dhis2 = new DHIS2Facade(connector, this.getDhis2Version());

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("importMode", IMPORT_MODE));

    try
    {
      this.setStage(history, ExportStage.EXPORT);
      
      InputStream isPayload = this.export();
      
      MetadataImportResponse resp = dhis2.metadataPost(params, new InputStreamEntity(isPayload));

      if (resp.getStatusCode() != 200)
      {
        ExportRemoteException re = new ExportRemoteException();
        re.setRemoteError(resp.getError()); // TODO : pull the error from dhis2
        throw re;
      }
      
      this.setStage(history, ExportStage.COMPLETE);
    }
    catch (InvalidLoginException | HTTPException e)
    {
      RemoteConnectionException rce = new RemoteConnectionException(e);
      throw rce;
    }
  }
  
  private void setStage(ExportHistory history, ExportStage stage)
  {
    history.appLock();
    history.clearStage();
    history.addStage(stage);
    history.apply();
  }
  
  private void write(OutputStream out) throws IOException
  {
    try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(out, Charset.forName("UTF-8"))))
    {
      jw.beginObject();
      {
        jw.name("organisationUnits").beginArray();
        {
          List<SyncLevel> levels = this.dhis2Config.getLevels();
          
          for (SyncLevel level : levels)
          {
            ServerGeoObjectType got = level.getGeoObjectType();
            
            GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, this.syncConfig.getServerHierarchyType(), null, true, GeoObjectExportFormat.JSON_DHIS2, this.syncConfig.getExternalSystem(), -1, -1);
            exporter.setDHIS2Facade(this.dhis2);
            exporter.writeObjects(jw);
          }
        } jw.endArray();
      } jw.endObject();
    }
  }
  
  public InputStream export() throws IOException
  {
    PipedOutputStream pos = new PipedOutputStream();
    PipedInputStream pis = new PipedInputStream(pos);

    Thread exportThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          try
          {
            runInReq();
          }
          finally
          {
            pos.close();
          }
        }
        catch (IOException e)
        {
          logger.error("Error while writing", e);
        }
      }
      
      @Request
      public void runInReq()
      {
        try
        {
          DataExportJob.this.write(pos);
        }
        catch (Throwable t)
        {
          logger.error("Data Export Job encountered error while writing to stream.", t);
          
          if (t instanceof RuntimeException)
          {
            throw (RuntimeException) t;
          }
          else
          {
            throw new RuntimeException(t);
          }
        }
      }
    });
    exportThread.setDaemon(true);
    exportThread.start();

    return pis;
  }

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
