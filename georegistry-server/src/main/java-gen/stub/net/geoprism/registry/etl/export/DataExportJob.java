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
  
  private SynchronizationConfig config;
  
  private Thread exportThread;
  
  private DHIS2Facade dhis2;
  
  private static final Logger logger = LoggerFactory.getLogger(DataExportJob.class);
  
  // Set to VALIDATE and the remote DHIS2 server will not commit, only "dry-run".
  private static final String IMPORT_MODE = "VALIDATE";
  
  public DataExportJob()
  {
    super();
  }
  
  @Override
  public synchronized JobHistory start()
  {
    throw new UnsupportedOperationException();
  }

  public synchronized ImportHistory start(DHIS2SyncConfig configuration)
  {
    return executableJobStart(configuration);
  }

  private ImportHistory executableJobStart(DHIS2SyncConfig configuration)
  {
    JobHistoryRecord record = startInTrans(configuration);

    this.getQuartzJob().start(record);

    return (ImportHistory) record.getChild();
  }
  
  @Transaction
  private JobHistoryRecord startInTrans(DHIS2SyncConfig configuration)
  {
    ExportHistory history = (ExportHistory) this.createNewHistory();

    // TODO
//    configuration.setHistoryId(history.getOid());
//    configuration.setJobId(this.getOid());
    
//    history.appLock();
//    history.setConfigJson(configuration.toJSON().toString());
//    history.apply();

    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();
    
    return record;
  }
  
  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    DHIS2ExternalSystem system = (DHIS2ExternalSystem) config.getExternalSystem();
    
    HTTPConnector connector = new HTTPConnector();
    connector.setServerUrl(system.getUrl());
    connector.setCredentials(system.getUsername(), system.getPassword());
    
    dhis2 = new DHIS2Facade(connector, "33");
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("importMode", IMPORT_MODE));
    
    try
    {
      InputStream isPayload = this.export();
      
      MetadataImportResponse resp = dhis2.metadataPost(params, new InputStreamEntity(isPayload));
      
      if (resp.getStatusCode() != 200)
      {
        ExportRemoteException re = new ExportRemoteException();
        re.setRemoteError(resp.getError()); // TODO : resp.getError throws UnsupportedOp
      }
    }
    catch (InvalidLoginException | HTTPException e)
    {
      RemoteConnectionException rce = new RemoteConnectionException(e);
      throw rce;
    }
  }
  
  private void write(OutputStream out) throws IOException
  {
    final DHIS2SyncConfig syncConfig = (DHIS2SyncConfig) config.buildConfiguration();
    
    try (JsonWriter jw = new JsonWriter(new OutputStreamWriter(out, Charset.forName("UTF-8"))))
    {
      jw.beginObject();
      {
        jw.name("organisationUnits").beginArray();
        {
          List<SyncLevel> levels = syncConfig.getLevels();
          
          for (SyncLevel level : levels)
          {
            ServerGeoObjectType got = level.getGeoObjectType();
            
            GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(got, this.config.getServerHierarchyType(), null, true, GeoObjectExportFormat.JSON_DHIS2, this.config.getExternalSystem(), -1, -1);
            exporter.setDHIS2Facade(this.dhis2);
            exporter.writeObjects(jw);
          }
        } jw.endArray();
      }
    }
  }
  
  public InputStream export() throws IOException
  {
    PipedOutputStream pos = new PipedOutputStream();
    PipedInputStream pis = new PipedInputStream(pos);

    exportThread = new Thread(new Runnable()
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
}
