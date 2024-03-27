/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.request;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.curation.CurationProblem;
import net.geoprism.registry.curation.CurationProblemQuery;
import net.geoprism.registry.curation.ListCurationHistory;
import net.geoprism.registry.curation.ListCurationJob;
import net.geoprism.registry.etl.ImportError.ErrorResolution;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectEditorBusinessService;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.view.Page;

@Service
public class CurationService
{
  @Autowired
  private RolePermissionService          permissions;

  @Autowired
  private GeoObjectEditorBusinessService service;

  public JsonObject getListCurationInfo(ListTypeVersion version)
  {
    final ListType listType = version.getListType();
    final ServerGeoObjectType serverGOT = listType.getServerGeoObjectType();
    final String orgCode = listType.getOrganization().getCode();

    this.checkPermissions(orgCode, serverGOT);

    JsonObject json = new JsonObject();

    ListCurationHistory history = ListCurationJob.getMostRecent(version.getOid());

    boolean isRunning = false;

    if (history != null)
    {
      if (history.getStatus().contains(AllJobStatus.RUNNING))
      {
        isRunning = true;
      }

      json.addProperty("lastRun", GeoRegistryUtil.formatDate(history.getCreateDate(), false));

      json.addProperty("curationId", history.getOid());
    }

    boolean hasRunPermission = this.permissions.isSRA() || this.permissions.isRA(orgCode) || this.permissions.isRM(orgCode, serverGOT);

    json.addProperty("canRun", !isRunning && hasRunPermission);

    return json;
  }

  @Request(RequestType.SESSION)
  public JsonObject details(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    final ListCurationHistory hist = ListCurationHistory.get(historyId);
    final ListTypeVersion version = hist.getVersion();
    final ListType listType = version.getListType();
    final ListCurationJob job = (ListCurationJob) hist.getAllJob().getAll().get(0);
    final GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());
    final ServerGeoObjectType serverGOT = listType.getServerGeoObjectType();
    final String orgCode = listType.getOrganization().getCode();

    this.checkPermissions(orgCode, serverGOT);

    JsonObject jo = this.serializeHistory(hist, user, job);

    jo.add("page", this.page(sessionId, historyId, onlyUnresolved, pageSize, pageNumber));

    return jo;
  }

  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    final ListCurationHistory hist = ListCurationHistory.get(historyId);
    final ListTypeVersion version = hist.getVersion();
    final ListType listType = version.getListType();
    final ServerGeoObjectType serverGOT = listType.getServerGeoObjectType();
    final String orgCode = listType.getOrganization().getCode();

    this.checkPermissions(orgCode, serverGOT);

    CurationProblemQuery query = new CurationProblemQuery(new QueryFactory());

    query.WHERE(query.getHistory().EQ(historyId));

    if (onlyUnresolved)
    {
      query.WHERE(query.getResolution().EQ(ErrorResolution.UNRESOLVED.name()));
    }

    query.ORDER_BY(query.getProblemType(), SortOrder.ASC);

    query.restrictRows(pageSize, pageNumber);

    try (OIterator<? extends CurationProblem> it = query.getIterator())
    {
      List<CurationProblem> results = new LinkedList<>(it.getAll());

      return new Page<CurationProblem>(query.getCount(), query.getPageNumber(), query.getPageSize(), results).toJSON();
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject curate(String sessionId, String listTypeVersionId)
  {
    final ListTypeVersion version = ListTypeVersion.get(listTypeVersionId);
    final ListType listType = version.getListType();
    final ServerGeoObjectType serverGOT = listType.getServerGeoObjectType();
    final String orgCode = listType.getOrganization().getCode();

    this.checkPermissions(orgCode, serverGOT);

    ListCurationJob job = new ListCurationJob();
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.apply();

    ListCurationHistory history = job.start(version);

    return this.serializeHistory(history, GeoprismUser.get(job.getRunAsUser().getOid()), job);
  }

  protected JsonObject serializeHistory(ListCurationHistory hist, GeoprismUser user, ExecutableJob job)
  {
    JsonObject jo = new JsonObject();

    jo.addProperty("status", hist.getStatus().get(0).name());
    jo.addProperty("lastRun", GeoRegistryUtil.formatDate(hist.getCreateDate(), false));
    jo.addProperty("lastRunBy", user.getUsername());
    jo.addProperty("historyId", hist.getOid());
    jo.addProperty("jobId", job.getOid());
    jo.addProperty("workProgress", hist.getWorkProgress());
    jo.addProperty("workTotal", hist.getWorkTotal());

    if (hist.getStatus().get(0).equals(AllJobStatus.FAILURE) && hist.getErrorJson().length() > 0)
    {
      JsonObject exception = new JsonObject();

      exception.add("type", JsonParser.parseString(hist.getErrorJson()).getAsJsonObject().get("type"));
      exception.addProperty("message", hist.getLocalizedError(Session.getCurrentLocale()));

      jo.add("exception", exception);
    }

    return jo;
  }

  private void checkPermissions(String orgCode, ServerGeoObjectType type)
  {
    if (this.permissions.isRA())
    {
      this.permissions.enforceRA(orgCode);
    }
    else if (this.permissions.isRM())
    {
      this.permissions.enforceRM(orgCode, type);
    }
    else
    {
      this.permissions.enforceRM();
    }
  }

  @Request(RequestType.SESSION)
  public void submitProblemResolution(String sessionId, String json)
  {
    submitProblemResolution(json);
  }

  @Transaction
  private void submitProblemResolution(String json)
  {
    JsonObject config = JsonParser.parseString(json).getAsJsonObject();

    ListCurationHistory hist = ListCurationHistory.get(config.get("historyId").getAsString());
    ListTypeVersion version = hist.getVersion();

    // this.checkPermissions(hist.getOrganization().getCode(),
    // hist.getServerGeoObjectType());

    // CurationProblem err =
    // CurationProblem.get(config.get("problemId").getAsString());

    String resolution = config.get("resolution").getAsString();

    if (resolution.equals(ErrorResolution.APPLY_GEO_OBJECT.name()))
    {
      String geoObjectCode = config.get("code").getAsString();
      String geoObjectTypeCode = config.get("typeCode").getAsString();
      String actions = config.get("actions").getAsJsonArray().toString();

      this.service.updateGeoObject(geoObjectCode, geoObjectTypeCode, actions, version.getOid(), null);

      // err.appLock();
      // err.setResolution(resolution);
      // err.apply();
    }
    else
    {
      throw new UnsupportedOperationException("Invalid import resolution [" + resolution + "].");
    }
  }

  @Request(RequestType.SESSION)
  public void setResolution(String sessionId, String problemId, String resolution)
  {
    setResolution(problemId, resolution);
  }

  @Transaction
  private void setResolution(String problemId, String resolution)
  {
    CurationProblem err = CurationProblem.get(problemId);

    if (ErrorResolution.valueOf(resolution) != null)
    {
      err.appLock();
      err.setResolution(resolution);
      err.apply();
    }
    else
    {
      throw new UnsupportedOperationException("Invalid import resolution [" + resolution + "].");
    }
  }

}
