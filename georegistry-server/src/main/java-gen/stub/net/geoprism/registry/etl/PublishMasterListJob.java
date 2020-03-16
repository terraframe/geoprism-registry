package net.geoprism.registry.etl;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.model.ServerGeoObjectType;

public class PublishMasterListJob extends PublishMasterListJobBase
{
  private static final long serialVersionUID = 500653024;

  public PublishMasterListJob()
  {
    super();
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    this.getMasterList().publishFrequencyVersions();
  }

  public JSONObject toJSON()
  {
    final MasterList masterlist = this.getMasterList();
    final ServerGeoObjectType type = masterlist.getGeoObjectType();
    final JobHistory history = this.getAllJobHistory().getAll().get(0);
    final GeoprismUser user = GeoprismUser.get(this.getRunAsUser().getOid());

    try
    {
      final JSONObject object = new JSONObject();
      object.put(PublishMasterListJob.OID, this.getOid());
      object.put(PublishMasterListJob.MASTERLIST, this.getMasterListOid());
      object.put(PublishMasterListJob.TYPE, type.getLabel().getValue());
      object.put(JobHistory.STATUS, history.getStatus().get(0).getDisplayLabel());
      object.put("author", user.getUsername());
      object.put("createDate", history.getCreateDate());
      object.put("lastUpdateDate", history.getLastUpdateDate());
      object.put("workProgress", history.getWorkProgress());
      object.put("workTotal", history.getWorkTotal());
      object.put("historyoryId", history.getOid());

      return object;
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
}
