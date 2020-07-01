/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.DataExportJobQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportHistoryQuery;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.view.JsonWrapper;
import net.geoprism.registry.view.Page;

public class SynchronizationConfigService
{
  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, Integer pageNumber, Integer pageSize) throws JSONException
  {
    long count = SynchronizationConfig.getCount();
    List<SynchronizationConfig> results = SynchronizationConfig.getSynchronizationConfigsForOrg(pageNumber, pageSize);

    return new Page<SynchronizationConfig>(count, pageNumber, pageSize, results).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String json) throws JSONException
  {
    JsonElement element = JsonParser.parseString(json);

    SynchronizationConfig config = SynchronizationConfig.desieralize(element.getAsJsonObject());
    config.apply();

    return config.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid) throws JSONException
  {
    SynchronizationConfig config = SynchronizationConfig.get(oid);

    return config.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    SynchronizationConfig config = SynchronizationConfig.get(oid);
    Organization organization = config.getOrganization();

    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      ServiceFactory.getRolePermissionService().enforceRA(session.getUser(), organization.getCode());
    }

    config.delete();
  }

  @Request(RequestType.SESSION)
  public JsonObject edit(String sessionId, String oid)
  {
    JsonObject response = new JsonObject();

    if (oid != null && oid.length() > 0)
    {
      SynchronizationConfig config = SynchronizationConfig.lock(oid);

      response.add("config", config.toJSON());
    }

    JsonArray orgs = new JsonArray();

    List<Organization> organizations = Organization.getUserAdminOrganizations();

    for (Organization organization : organizations)
    {
      JsonArray hierarchies = new JsonArray();

      List<ServerHierarchyType> sHierachies = ServerHierarchyType.getForOrganization(organization);

      for (ServerHierarchyType hierarchy : sHierachies)
      {
        JsonObject object = new JsonObject();
        object.addProperty("label", hierarchy.getDisplayLabel().getValue());
        object.addProperty("code", hierarchy.getCode());

        hierarchies.add(object);
      }

      JsonArray systems = new JsonArray();

      List<ExternalSystem> esystems = ExternalSystem.getForOrganization(organization);

      for (ExternalSystem system : esystems)
      {
        if (system.isExportSupported())
        {
          LocalizedValue label = LocalizedValueConverter.convert(system.getEmbeddedComponent(ExternalSystem.LABEL));

          JsonObject object = new JsonObject();
          object.addProperty("label", label.getValue());
          object.addProperty("oid", system.getOid());
          object.addProperty("type", system.getMdClass().getTypeName());

          systems.add(object);
        }
      }

      JsonObject object = new JsonObject();
      object.addProperty("label", organization.getDisplayLabel().getValue());
      object.addProperty("code", organization.getCode());
      object.add("hierarchies", hierarchies);
      object.add("systems", systems);

      orgs.add(object);
    }

    response.add("orgs", orgs);

    return response;
  }

  @Request(RequestType.SESSION)
  public void unlock(String sessionId, String oid)
  {
    SynchronizationConfig.unlock(oid);
  }

  @Request(RequestType.SESSION)
  public JsonObject run(String sessionId, String oid)
  {
    SynchronizationConfig config = SynchronizationConfig.get(oid);

    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      ServiceFactory.getRolePermissionService().enforceRA(session.getUser(), config.getOrganization().getCode());
    }

    List<? extends DataExportJob> jobs = config.getJobs();

    DataExportJob job = jobs.get(0);
    job.appLock();
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.apply();

    ExportHistory hist = job.start(config);
    GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());

    return serializeHistory(hist, user, job);
  }

  @Request(RequestType.SESSION)
  public JsonObject getJobs(String sessionId, String configId, Integer pageSize, Integer pageNumber)
  {
    QueryFactory qf = new QueryFactory();

    DataExportJobQuery jQuery = new DataExportJobQuery(qf);
    jQuery.WHERE(jQuery.getConfig().EQ(configId));

    ExportHistoryQuery ihq = new ExportHistoryQuery(qf);
    ihq.WHERE(ihq.job(jQuery));
    ihq.restrictRows(pageSize, pageNumber);
    ihq.ORDER_BY_DESC(ihq.getCreateDate());

    try (OIterator<? extends ExportHistory> it = ihq.getIterator())
    {
      LinkedList<JsonWrapper> results = new LinkedList<JsonWrapper>();

      while (it.hasNext())
      {
        ExportHistory hist = it.next();
        DataExportJob job = (DataExportJob) hist.getAllJob().getAll().get(0);

        GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());

        results.add(new JsonWrapper(serializeHistory(hist, user, job)));
      }

      return new Page<JsonWrapper>(ihq.getCount(), pageNumber, pageSize, results).toJSON();
    }
  }

  protected JsonObject serializeHistory(ExportHistory hist, GeoprismUser user, ExecutableJob job)
  {
    JsonObject jo = new JsonObject();

    jo.addProperty("jobType", job.getType());
    jo.addProperty("stage", hist.getStage().get(0).name());
    jo.addProperty("status", hist.getStatus().get(0).name());
    jo.addProperty("author", user.getUsername());
    jo.addProperty("createDate", formatDate(hist.getCreateDate()));
    jo.addProperty("lastUpdateDate", formatDate(hist.getLastUpdateDate()));
    jo.addProperty("workProgress", hist.getWorkProgress());
    jo.addProperty("workTotal", hist.getWorkTotal());
    jo.addProperty("historyId", hist.getOid());
    jo.addProperty("jobId", job.getOid());

    if (hist.getStatus().get(0).equals(AllJobStatus.FAILURE) && hist.getErrorJson().length() > 0)
    {
      String errorJson = hist.getErrorJson();
      JsonObject error = JsonParser.parseString(errorJson).getAsJsonObject();

      JsonObject exception = new JsonObject();
      exception.addProperty("type", error.get("type").getAsString());
      exception.addProperty("message", hist.getLocalizedError(Session.getCurrentLocale()));

      jo.add("exception", exception);
    }

    return jo;
  }

  public static String formatDate(Date date)
  {
    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT, Session.getCurrentLocale());
    // format.setTimeZone(TimeZone.getTimeZone("GMT"));

    return format.format(date);
  }

}
