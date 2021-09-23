package net.geoprism.registry.etl;

import java.text.SimpleDateFormat;

import org.json.JSONException;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class FhirExportJob extends FhirExportJobBase
{
  private static final long serialVersionUID = -954830596;

  public FhirExportJob()
  {
    super();
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    FhirExternalSystem system = this.getExternalSystem();

    this.getVersion().exportToFhir(system, this.getImplementation());
  }

  @Override
  public void afterJobExecute(JobHistory history)
  {
    super.afterJobExecute(history);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));
  }

  public JsonObject toJson()
  {
    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    final MasterListVersion version = this.getVersion();
    final MasterList masterlist = version.getMasterlist();

    final ServerGeoObjectType type = masterlist.getGeoObjectType();
    final JobHistory history = this.getAllJobHistory().getAll().get(0);
    final GeoprismUser user = GeoprismUser.get(this.getRunAsUser().getOid());

    try
    {
      final JsonObject object = new JsonObject();
      object.addProperty(FhirExportJob.OID, this.getOid());
      object.add(FhirExportJob.VERSION, this.getVersion().toJSON(false));
      object.addProperty(FhirExportJob.IMPLEMENTATION, this.getImplementation());
      object.addProperty(FhirExportJob.TYPE, type.getLabel().getValue());
      object.addProperty(JobHistory.STATUS, history.getStatus().get(0).getDisplayLabel());
      object.addProperty("date", format.format(version.getPublishDate()));
      object.addProperty("author", user.getUsername());
      object.addProperty("createDate", format.format(history.getCreateDate()));
      object.addProperty("lastUpdateDate", format.format(history.getLastUpdateDate()));
      object.addProperty("workProgress", history.getWorkProgress());
      object.addProperty("workTotal", history.getWorkTotal());
      object.addProperty("historyoryId", history.getOid());

      if (history.getErrorJson() != null && history.getErrorJson().length() > 0)
      {
        object.addProperty("message", history.getLocalizedError(Session.getCurrentLocale()));
      }

      return object;
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

}
