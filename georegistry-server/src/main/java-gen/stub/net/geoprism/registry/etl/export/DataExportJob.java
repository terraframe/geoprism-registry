package net.geoprism.registry.etl.export;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.RunwayException;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
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
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ExportJobHasErrors;
import net.geoprism.registry.etl.NewGeoObjectInvalidSyncTypeError;
import net.geoprism.registry.etl.RemoteConnectionException;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

/**
 * This class is currently hardcoded to DHIS2 export, however the metadata is attempting to be generic enough
 * to scale to more generic usecases.
 * 
 * @author rrowlands
 * @author jsmethie
 *
 */
public class DataExportJob extends DataExportJobBase
{
  private static final long     serialVersionUID = -1821569567;

  private static final Logger   logger           = LoggerFactory.getLogger(DataExportJob.class);

  // Set to VALIDATE and the remote DHIS2 server will not commit, only
  // "dry-run".
  private static final String   IMPORT_MODE      = "COMMIT";

  private SynchronizationConfig syncConfig;

  private DHIS2SyncConfig       dhis2Config;

  private DHIS2Facade           dhis2;
  
  private ExportHistory         history;

  private JobExportError exportWarning;

  public DataExportJob()
  {
    super();
  }

  private class JobExportError extends RunwayException
  {
    private static final long serialVersionUID = 8463740942015611693L;

    protected DHIS2Response    response;
    
    protected String          submittedJson;

    protected Throwable       error;
    
    protected String          geoObjectCode;
    
    protected Long            rowIndex;

    private JobExportError(Long rowIndex, DHIS2Response response, String submittedJson, Throwable t, String geoObjectCode)
    {
      super("");
      this.response = response;
      this.submittedJson = submittedJson;
      this.error = t;
      this.geoObjectCode = geoObjectCode;
      this.rowIndex = rowIndex;
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
   * We want the actual API version, which is different than the DHIS2 core
   * version. This function will consume the DHIS2 version and return an API
   * version.
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
    this.history = (ExportHistory) executionContext.getJobHistoryRecord().getChild();
    
    this.syncConfig = this.getConfig();
    this.dhis2Config = (DHIS2SyncConfig) this.getConfig().buildConfiguration();

    DHIS2ExternalSystem system = this.dhis2Config.getSystem();

    HTTPConnector connector = new HTTPConnector();
    connector.setServerUrl(system.getUrl());
    connector.setCredentials(system.getUsername(), system.getPassword());

    dhis2 = new DHIS2Facade(connector, this.getAPIVersion());

    this.setStage(history, ExportStage.EXPORT);

    this.doExport();
  }

  private void setStage(ExportHistory history, ExportStage stage)
  {
    history.appLock();
    history.clearStage();
    history.addStage(stage);
    history.apply();
  }

  private long getCount(ServerGeoObjectType got)
  {
    MdVertexDAOIF mdVertex = got.getMdVertex();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

    return new GraphQuery<Long>(statement.toString()).getSingleResult();
  }

  public List<VertexServerGeoObject> query(ServerGeoObjectType got, long skip, long pageSize)
  {
    MdVertexDAOIF mdVertex = got.getMdVertex();
    MdAttributeDAOIF mdAttribute = MdAttributeDAO.getByKey(GeoVertex.CLASS + "." + GeoVertex.LASTUPDATEDATE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" ORDER BY " + mdAttribute.getColumnName() + ", oid ASC");
    statement.append(" SKIP " + skip + " LIMIT " + pageSize);

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    List<VertexServerGeoObject> response = new LinkedList<VertexServerGeoObject>();

    for (VertexObject vObject : vObjects)
    {
      VertexServerGeoObject vSGO = new VertexServerGeoObject(got, vObject);
      vSGO.setDate(ValueOverTime.INFINITY_END_DATE);

      response.add(vSGO);
    }

    return response;
  }

  private void doExport()
  {
    long rowIndex = 0;
    long total = 0;
    long exportCount = 0;
    
    List<SyncLevel> levels = this.dhis2Config.getLevels();
    
    Boolean includeTranslations = LocalizationFacade.getInstalledLocales().size() > 0;

    for (SyncLevel level : levels)
    {
      long skip = 0;
      long pageSize = 1000;

      long count = this.getCount(level.getGeoObjectType());
      total += count;

      while (skip < count)
      {
        List<VertexServerGeoObject> objects = this.query(level.getGeoObjectType(), skip, pageSize);

        for (VertexServerGeoObject go : objects) {
          try
          {
            this.exportWarning = null;
            
            exportGeoObject(level, rowIndex, go, includeTranslations);
            
            exportCount++;
            
            this.history.appLock();
            this.history.setWorkProgress(rowIndex);
            this.history.setExportedRecords(exportCount);
            this.history.apply();
          }
          catch (JobExportError ee)
          {
            recordExportError(ee);
          }
          
          if (this.exportWarning != null)
          {
            recordExportError(this.exportWarning);
          }
          
          rowIndex++;
        };

        skip += pageSize;
      }
    }
    
    this.history.appLock();
    this.history.setWorkTotal(total);
    this.history.setWorkProgress(rowIndex);
    this.history.setExportedRecords(exportCount);
    history.clearStage();
    history.addStage(ExportStage.COMPLETE);
    this.history.apply();
    
    ExportErrorQuery query = new ExportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this.history));
    Boolean hasErrors = query.getCount() > 0;
    
    if (hasErrors)
    {
      ExportJobHasErrors ex = new ExportJobHasErrors();
      
      throw ex;
    }
  }

  private void recordExportError(JobExportError ee)
  {
    DHIS2Response resp = ee.response;
    Throwable ex = ee.error;
    String geoObjectCode = ee.geoObjectCode;
    
    ExportError exportError = new ExportError();

    if (ee.submittedJson != null)
    {
      exportError.setSubmittedJson(ee.submittedJson);
    }
    
    if (resp != null && resp.hasErrorReports())
    {
      List<ErrorReport> reports = resp.getErrorReports();
      
      ErrorReport report = reports.get(0);
      
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();
      
      exportError.setResponseJson(gson.toJson(report));
    }
    
    exportError.setCode(geoObjectCode);
    
    if (ex != null)
    {
      exportError.setErrorJson(JobHistory.exceptionToJson(ex).toString());
    }
    
    exportError.setRowIndex(ee.rowIndex);
    
    exportError.setHistory(this.history);
    
    exportError.apply();
  }

  /**
   * It's important that we try our best to maintain an accurate state between
   * our database and the DHIS2 database. The DHIS2Serailizer will create new
   * ids and save them as externalIds for GeoObjects that do not have
   * externalIds. If our push to DHIS2 for this new GeoObject fails for whatever
   * reason, then we want to rollback our database so that we do not store the
   * id which does not exist in the DHIS2 database.
   * 
   * TODO : In the future perhaps we should directly ask DHIS2 if an object
   * exists and then we'll know
   * 
   * @param level
   * @param go
   * @return
   * @throws ExportError
   */
  @Transaction
  private void exportGeoObject(SyncLevel level, Long rowIndex, VertexServerGeoObject serverGo, Boolean includeTranslations) throws JobExportError
  {
    DHIS2Response resp = null;
    
    JsonObject orgUnitJson = null;
    
    String externalId = null;
    
    try
    {
      externalId = serverGo.getExternalId(this.syncConfig.getExternalSystem());
      boolean isNew = (externalId == null);

      if (isNew && level.getSyncType() != SyncLevel.Type.ALL)
      {
        NewGeoObjectInvalidSyncTypeError err = new NewGeoObjectInvalidSyncTypeError();
        err.setGeoObject(serverGo.getDisplayLabel().getValue());
        throw err;
      }

      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(VertexServerGeoObject.class, new DHIS2GeoObjectJsonAdapters.DHIS2Serializer(this.dhis2, level, level.getGeoObjectType(), this.syncConfig.getServerHierarchyType(), this.syncConfig.getExternalSystem()));
      orgUnitJson = builder.create().toJsonTree(serverGo, serverGo.getClass()).getAsJsonObject();
      
      externalId = serverGo.getExternalId(this.syncConfig.getExternalSystem());

      List<NameValuePair> params = new ArrayList<NameValuePair>();
//      params.add(new BasicNameValuePair("mergeMode", "MERGE"));

      try
      {
        if (!isNew)
        {
          resp = dhis2.entityIdPatch("organisationUnits", externalId, params, new StringEntity(orgUnitJson.toString(), Charset.forName("UTF-8")));
        }
        else
        {
          resp = dhis2.entityPost("organisationUnits", params, new StringEntity(orgUnitJson.toString(), Charset.forName("UTF-8")));
        }
      }
      catch (InvalidLoginException | HTTPException e)
      {
        RemoteConnectionException rce = new RemoteConnectionException(e);
        throw rce;
      }

      if (!resp.isSuccess())
      {
        ExportRemoteException re = new ExportRemoteException();

        if (resp.hasMessage())
        {
          re.setRemoteError(resp.getMessage());
        }

        throw re;
      }
    }
    catch (Throwable t)
    {
      JobExportError er = new JobExportError(rowIndex, resp, orgUnitJson.toString(), t, serverGo.getCode());
      throw er;
    }
    
    String translationJson = null;
    DHIS2Response translationResp = null;
    try
    {
      if (includeTranslations)
      {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        
        JsonObject payload = new JsonObject();
        payload.add("translations", orgUnitJson.get("translations").getAsJsonArray().deepCopy());
        
        translationResp = dhis2.entityTranslations("organisationUnits", externalId, params, new StringEntity(payload.toString(), Charset.forName("UTF-8")));
        
        if (!translationResp.isSuccess())
        {
          ExportRemoteException re = new ExportRemoteException();

          if (translationResp.hasMessage())
          {
            re.setRemoteError(translationResp.getMessage());
          }

          throw re;
        }
      }
    }
    catch (Throwable t)
    {
      this.exportWarning = new JobExportError(rowIndex, translationResp, translationJson, t, serverGo.getCode());
    }
  }

  /*
   * This method was used when we were posting to the metadata endpoint and
   * doing bulk operations. We had to switch over to object-by-object posting
   * because it's the only endpoint that supports "partial updates". I'm keeping
   * it here for a little bit in case we need it for some reason.
   */
  // private void write(OutputStream out) throws IOException
  // {
  // try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(out,
  // Charset.forName("UTF-8"))))
  // {
  // jw.beginObject();
  // {
  // jw.name("organisationUnits").beginArray();
  // {
  // List<SyncLevel> levels = this.dhis2Config.getLevels();
  //
  // for (SyncLevel level : levels)
  // {
  // ServerGeoObjectType got = level.getGeoObjectType();
  //
  // GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got,
  // this.syncConfig.getServerHierarchyType(), null, true,
  // GeoObjectExportFormat.JSON_DHIS2, this.syncConfig.getExternalSystem(), -1,
  // -1);
  // exporter.setDHIS2Facade(this.dhis2);
  // exporter.setSyncLevel(level);
  // exporter.writeObjects(jw);
  // }
  // } jw.endArray();
  // } jw.endObject();
  // }
  // }

  // public InputStream export() throws IOException
  // {
  // PipedOutputStream pos = new PipedOutputStream();
  // PipedInputStream pis = new PipedInputStream(pos);
  //
  // Thread exportThread = new Thread(new Runnable()
  // {
  // @Override
  // public void run()
  // {
  // try
  // {
  // try
  // {
  // runInReq();
  // }
  // finally
  // {
  // pos.close();
  // }
  // }
  // catch (IOException e)
  // {
  // logger.error("Error while writing", e);
  // }
  // }
  //
  // @Request
  // public void runInReq()
  // {
  // try
  // {
  // DataExportJob.this.write(pos);
  // }
  // catch (Throwable t)
  // {
  // logger.error("Data Export Job encountered error while writing to stream.",
  // t);
  //
  // if (t instanceof RuntimeException)
  // {
  // throw (RuntimeException) t;
  // }
  // else
  // {
  // throw new RuntimeException(t);
  // }
  // }
  // }
  // });
  // exportThread.setDaemon(true);
  // exportThread.start();
  //
  // return pis;
  // }

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
