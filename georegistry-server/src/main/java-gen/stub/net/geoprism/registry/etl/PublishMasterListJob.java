/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
