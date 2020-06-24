package net.geoprism.registry.etl.export;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import net.geoprism.dhis2.dhis2adapter.HTTPException;
import net.geoprism.dhis2.dhis2adapter.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.RemoteConnectionException;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.postgres.GeoObjectQuery;

public class DataExportJob extends DataExportJobBase
{
  private static final long   serialVersionUID = -1821569567;

  // Set to VALIDATE and the remote DHIS2 server will not commit, only
  // "dry-run".
  private static final String IMPORT_MODE      = "VALIDATE";

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

  public OIterator<GeoObject> postgresQuery(ServerGeoObjectType got)
  {
    GeoObjectQuery goq = new GeoObjectQuery(got);

    goq.orderBy(DefaultAttribute.LAST_UPDATE_DATE.getName(), SortOrder.ASC);

    return goq.getIterator();
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    DHIS2SyncConfig config = (DHIS2SyncConfig) this.getConfig().buildConfiguration();

    DHIS2ExternalSystem system = config.getSystem();

    HTTPConnector connector = new HTTPConnector();
    connector.setServerUrl(system.getUrl());
    connector.setCredentials(system.getUsername(), system.getPassword());

    DHIS2Facade facade = new DHIS2Facade(connector, "33");

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("importMode", IMPORT_MODE));

    try
    {
      MetadataImportResponse resp = facade.metadataPost(params, null); // TODO
                                                                       // EntityInputStream

      if (resp.getStatusCode() != 200)
      {
        ExportRemoteException re = new ExportRemoteException();
        re.setRemoteError(resp.getError()); // TODO : resp.getError throws
                                            // UnsupportedOp
      }
    }
    catch (InvalidLoginException | HTTPException e)
    {
      RemoteConnectionException rce = new RemoteConnectionException(e);
      throw rce;
    }
  }

  private InputStream generateJsonPayload(DHIS2SyncConfig config)
  {
    // TODO

    JsonObject payload = new JsonObject();
    JsonArray orgUnits = new JsonArray();
    payload.add("organisationUnits", orgUnits);

    List<SyncLevel> levels = config.getLevels();

    for (SyncLevel level : levels)
    {
      ServerGeoObjectType got = level.getGeoObjectType();

      // OIterator<GeoObject> it = postgresQuery(got);
      // try
      // {
      // for (GeoObject go : it)
      // {
      // GsonBuilder builder = new GsonBuilder();
      // builder.registerTypeAdapter(GeoObject.class, new
      // DHIS2GeoObjectJsonAdapters.DHIS2Serializer(got, config.getHierarchy(),
      // config.getSystem()));
      //
      // orgUnits.add(builder.create().toJsonTree(go));
      // }
      // }
      // finally
      // {
      // it.close();
      // }
    }

    return null;
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
