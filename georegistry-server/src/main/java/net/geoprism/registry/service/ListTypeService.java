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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.ListTileCache;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeEntry;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.etl.ListTypeJob;
import net.geoprism.registry.etl.ListTypeJobQuery;
import net.geoprism.registry.etl.PublishListTypeJob;
import net.geoprism.registry.etl.PublishListTypeJobQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.roles.CreateListPermissionException;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class ListTypeService
{
  @Request(RequestType.SESSION)
  public JsonArray listAll(String sessionId)
  {
    return ListType.list();
  }

  @Request(RequestType.SESSION)
  public JsonObject listForType(String sessionId, String typeCode)
  {
    return ListType.listForType(typeCode);
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, JsonObject list)
  {
    ListType mList = ListType.apply(list);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return mList.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    try
    {
      ListType listType = ListType.get(oid);

      this.enforceWritePermissions(listType);

      listType.delete();
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
    catch (DataNotFoundException e)
    {
      // Do nothing
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject createVersion(String sessionId, String oid)
  {
    ListTypeEntry entry = ListTypeEntry.get(oid);
    ListType listType = entry.getListType();

    if (!listType.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.enforceWritePermissions(listType);

    String version = entry.publish();

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return JsonParser.parseString(version).getAsJsonObject();
    // return entry.toJSON(false);
  }

  @Request(RequestType.SESSION)
  public void createPublishedVersions(String sessionId, String oid)
  {
    ListType listType = ListType.get(oid);

    this.enforceWritePermissions(listType);

    listType.getEntries().forEach(entry -> {
      entry.publish();
    });
  }

  @Request(RequestType.SESSION)
  public String createPublishedVersionsJob(String sessionId, String oid)
  {
    ListType listType = ListType.get(oid);

    this.enforceWritePermissions(listType);

    QueryFactory factory = new QueryFactory();

    PublishListTypeJobQuery query = new PublishListTypeJobQuery(factory);
    query.WHERE(query.getListType().EQ(listType));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));
    q.AND(q.job(query));

    if (q.getCount() > 0)
    {
      throw new DuplicateJobException("This master list has already been queued for publication");
    }

    PublishListTypeJob job = new PublishListTypeJob();
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.setListType(listType);
    job.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    final JobHistory history = job.start();
    return history.getOid();
  }

  @Request(RequestType.SESSION)
  public JsonObject getPublishJobs(String sessionId, String oid, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    QueryFactory qf = new QueryFactory();

    final ListTypeJobQuery query = new ListTypeJobQuery(qf);
    query.WHERE(query.getListType().EQ(oid));
    query.ORDER_BY_DESC(query.getCreateDate());
    // query.ORDER_BY(ihq.get(sortAttr), order);
    query.restrictRows(pageSize, pageNumber);

    JsonArray results = new JsonArray();

    try (OIterator<? extends ListTypeJob> it = query.getIterator())
    {
      while (it.hasNext())
      {
        results.add(it.next().toJson());
      }
    }

    JsonObject page = new JsonObject();
    page.addProperty("count", query.getCount());
    page.addProperty("pageNumber", query.getPageNumber());
    page.addProperty("pageSize", query.getPageSize());
    page.add("results", results);

    return page;
  }

  @Request(RequestType.SESSION)
  public JsonObject publishVersion(String sessionId, String oid)
  {
    ListTypeEntry version = ListTypeEntry.get(oid);

    this.enforceWritePermissions(version.getListType());

    QueryFactory factory = new QueryFactory();

    // PublishListTypeVersionJobQuery query = new
    // PublishListTypeVersionJobQuery(factory);
    // query.WHERE(query.getListTypeVersion().EQ(version));
    //
    // JobHistoryQuery q = new JobHistoryQuery(factory);
    // q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED,
    // AllJobStatus.RUNNING));
    // q.AND(q.job(query));
    //
    // if (q.getCount() > 0)
    // {
    // throw new DuplicateJobException("This master list version has already
    // been queued for publishing");
    // }
    //
    // PublishListTypeVersionJob job = new PublishListTypeVersionJob();
    // job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    // job.setListTypeVersion(version);
    // job.setListType(version.getListType());
    // job.apply();
    //
    // NotificationFacade.queue(new
    // GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));
    //
    // final JobHistory history = job.start();

    JsonObject resp = new JsonObject();
    // resp.addProperty("jobOid", history.getOid());
    return resp;
  }

  // @Request(RequestType.SESSION)
  // public String generateShapefile(String sessionId, String oid)
  // {
  // ListTypeVersion version = ListTypeVersion.get(oid);
  //
  // this.enforceWritePermissions(version.getListType(),
  // ListTypeVersion.PUBLISHED);
  //
  // QueryFactory factory = new QueryFactory();
  //
  // PublishShapefileJobQuery query = new PublishShapefileJobQuery(factory);
  // query.WHERE(query.getVersion().EQ(version));
  //
  // JobHistoryQuery q = new JobHistoryQuery(factory);
  // q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED,
  // AllJobStatus.RUNNING));
  // q.AND(q.job(query));
  //
  // if (q.getCount() > 0)
  // {
  // throw new DuplicateJobException("This master list version has already been
  // queued for generating a shapefile");
  // }
  //
  // PublishShapefileJob job = new PublishShapefileJob();
  // job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
  // job.setVersion(version);
  // job.setListType(version.getListType());
  // job.apply();
  //
  // NotificationFacade.queue(new
  // GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));
  //
  // final JobHistory history = job.start();
  // return history.getOid();
  // }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return ListType.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getEntries(String sessionId, String oid)
  {
    ListType listType = ListType.get(oid);

    return listType.toJSON(true);
  }

  @Request(RequestType.SESSION)
  public JsonArray getVersions(String sessionId, String oid)
  {
    ListTypeEntry listType = ListTypeEntry.get(oid);

    return listType.getVersionJson();
  }

  @Request(RequestType.SESSION)
  public JsonObject getEntry(String sessionId, String oid)
  {
    return ListTypeVersion.get(oid).toJSON(true);
  }

  @Request(RequestType.SESSION)
  public JsonObject getVersion(String sessionId, String oid)
  {
    return ListTypeVersion.get(oid).toJSON(true);
  }

  @Request(RequestType.SESSION)
  public String getBounds(String sessionId, String oid)
  {
    return ListTypeVersion.get(oid).bbox();
  }

  @Request(RequestType.SESSION)
  public JsonObject data(String sessionId, String oid, Boolean includeGeometries, Integer pageNumber, Integer pageSize, String filter, String sort)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);
    return version.data(pageNumber, pageSize, filter, sort, includeGeometries);
  }

  @Request(RequestType.SESSION)
  public JsonArray values(String sessionId, String oid, String value, String attributeName, String valueAttribute, String filterJson)
  {
    return ListTypeVersion.get(oid).values(value, attributeName, valueAttribute, filterJson);
  }

  @Request(RequestType.SESSION)
  public InputStream exportShapefile(String sessionId, String oid, String filterJson)
  {
    return GeoRegistryUtil.exportListTypeShapefile(oid, filterJson);
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId, String oid, String filterJson)
  {
    return GeoRegistryUtil.exportListTypeExcel(oid, filterJson);
  }

  @Request(RequestType.SESSION)
  public InputStream downloadShapefile(String sessionId, String oid)
  {
    return ListTypeVersion.get(oid).downloadShapefile();
  }

  @Request(RequestType.SESSION)
  public JsonObject progress(String sessionId, String oid)
  {
    return ProgressService.progress(oid).toJson();
  }

  @Request(RequestType.SESSION)
  public void removeVersion(String sessionId, String oid)
  {
    try
    {
      ListTypeVersion version = ListTypeVersion.get(oid);

      this.enforceWritePermissions(version.getEntry().getListType());

      version.delete();

      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
    catch (DataNotFoundException e)
    {
      // Do nothing
    }
  }

  @Request(RequestType.SESSION)
  public JsonArray getAllVersions(String sessionId)
  {
    JsonArray response = new JsonArray();

    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    try (OIterator<? extends ListType> it = query.getIterator())
    {

      while (it.hasNext())
      {
        ListType list = it.next();
        final boolean isMember = Organization.isMember(list.getOrganization());

        // TODO FIGURE out this behavior: Used for the context layers

        // if (isMember || list.getVisibility().equals(ListType.PUBLIC))
        // {
        // response.add(list.toJSON(ListTypeVersion.PUBLISHED));
        // }
      }
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public InputStream getTile(String sessionId, JSONObject object)
  {
    try
    {
      byte[] bytes = ListTileCache.getTile(object);

      if (bytes != null)
      {
        return new ByteArrayInputStream(bytes);
      }

      return new ByteArrayInputStream(new byte[] {});
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private void enforceWritePermissions(ListType listType)
  {
    ServerGeoObjectType geoObjectType = listType.getGeoObjectType();
    Organization organization = geoObjectType.getOrganization();

    if (!ServiceFactory.getGeoObjectPermissionService().canWrite(organization.getCode(), geoObjectType))
    {
      CreateListPermissionException ex = new CreateListPermissionException();
      ex.setOrganization(organization.getDisplayLabel().getValue());
      throw ex;
    }
  }
}
