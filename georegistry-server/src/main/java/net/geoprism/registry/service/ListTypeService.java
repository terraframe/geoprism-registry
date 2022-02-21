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
package net.geoprism.registry.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.DefaultSerializer;
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
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.ListTypeVersionQuery;
import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.etl.ListTypeJob;
import net.geoprism.registry.etl.ListTypeJobQuery;
import net.geoprism.registry.etl.PublishListTypeVersionJob;
import net.geoprism.registry.etl.PublishListTypeVersionJobQuery;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.roles.CreateListPermissionException;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;
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
    
    // Auto publish the working versions of the lists 
    List<ListTypeVersion> versions = mList.getVersions();
    for (ListTypeVersion version : versions) {
      if (version.getWorking()) {
        this.publishVersion(sessionId, version.getOid());
      }
    }

    return mList.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject createEntries(String sessionId, String oid)
  {
    ListType mList = ListType.get(oid);
    mList.createEntries(null);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return mList.toJSON(true);
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
  public JsonObject createVersion(String sessionId, String oid, String metadata)
  {
    ListTypeEntry entry = ListTypeEntry.get(oid);
    ListType listType = entry.getListType();

    if (!listType.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.enforceWritePermissions(listType);

    String version = entry.publish(metadata);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return JsonParser.parseString(version).getAsJsonObject();
    // return entry.toJSON(false);
  }

  @Request(RequestType.SESSION)
  public JsonObject applyVersion(String sessionId, String oid, String metadata)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);
    ListType listType = version.getListType();

    if (!listType.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.enforceWritePermissions(listType);

    version.appLock();

    try
    {
      version.parse(JsonParser.parseString(metadata).getAsJsonObject());
    }
    finally
    {
      version.apply();
    }

    return version.toJSON(false);
  }

  // @Request(RequestType.SESSION)
  // public void createPublishedVersions(String sessionId, String oid, )
  // {
  // ListType listType = ListType.get(oid);
  //
  // this.enforceWritePermissions(listType);
  //
  // listType.getEntries().forEach(entry -> {
  // entry.publish(config);
  // });
  // }

  @Request(RequestType.SESSION)
  public String createPublishedVersionsJob(String sessionId, String oid)
  {
    ListType listType = ListType.get(oid);

    this.enforceWritePermissions(listType);

    QueryFactory factory = new QueryFactory();

    // PublishListTypeJobQuery query = new PublishListTypeJobQuery(factory);
    // query.WHERE(query.getListType().EQ(listType));
    //
    // JobHistoryQuery q = new JobHistoryQuery(factory);
    // q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED,
    // AllJobStatus.RUNNING));
    // q.AND(q.job(query));
    //
    // if (q.getCount() > 0)
    // {
    // throw new DuplicateJobException("This master list has already been queued
    // for publication");
    // }
    //
    // PublishListTypeJob job = new PublishListTypeJob();
    // job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    // job.setListType(listType);
    // job.apply();
    //
    // NotificationFacade.queue(new
    // GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));
    //
    // final JobHistory history = job.start();
    // return history.getOid();

    return null;
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

    try (OIterator<? extends ListTypeJob> it = query.getIterator())
    {
      List<JsonSerializable> results = new LinkedList<>(it.getAll());

      return new Page<JsonSerializable>(query.getCount(), query.getPageNumber(), query.getPageSize(), results).toJSON();
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject publishVersion(String sessionId, String oid)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);

    // Only a working version can be republished.
    if (!version.getWorking())
    {
      throw new UnsupportedOperationException();
    }
    
    this.enforceReadPermissions(version.getListType());

    QueryFactory factory = new QueryFactory();

    PublishListTypeVersionJobQuery query = new PublishListTypeVersionJobQuery(factory);
    query.WHERE(query.getVersion().EQ(version));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));
    q.AND(q.job(query));

    if (q.getCount() > 0)
    {
      throw new DuplicateJobException("This version has already been queued for publishing");
    }

    PublishListTypeVersionJob job = new PublishListTypeVersionJob();
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.setVersion(version);
    job.setListType(version.getListType());
    job.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    final JobHistory history = job.start();

    JsonObject resp = new JsonObject();
    resp.addProperty("jobOid", history.getOid());
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
  public JsonArray getBounds(String sessionId, String oid, String uid)
  {
    return ListTypeVersion.get(oid).bbox(uid);
  }

  @Request(RequestType.SESSION)
  public JsonObject data(String sessionId, String oid, String criteria, Boolean includeGeometries)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);
    Page<JsonSerializable> page = version.data(JsonParser.parseString(criteria).getAsJsonObject(), includeGeometries);

    return page.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject record(String sessionId, String oid, String uid)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);

    if (version.getWorking())
    {
      ListType type = version.getListType();

      if (type.doesActorHaveExploratoryPermission())
      {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

        ServerGeoObjectType geoObjectType = type.getGeoObjectType();
        ServerGeoObjectIF geoObject = new ServerGeoObjectService().getGeoObject(uid, geoObjectType.getCode());

        JsonObject object = new JsonObject();
        object.addProperty("recordType", "GEO_OBJECT");
        object.add("type", geoObject.getType().toJSON(new DefaultSerializer()));
        object.addProperty("code", geoObject.getCode());
        object.addProperty(ListTypeVersion.FORDATE, format.format(version.getForDate()));
        
        // Add geometry so we can zoom to it
        object.add("geoObject", geoObject.toGeoObject(version.getForDate()).toJSON());

        return object;
      }
    }

    return version.record(uid);
  }

  @Request(RequestType.SESSION)
  public JsonArray values(String sessionId, String oid, String value, String attributeName, String json)
  {
    ListTypeVersion version = ListTypeVersion.get(oid);
    JsonObject criteria = ( json != null ) ? JsonParser.parseString(json).getAsJsonObject() : new JsonObject();

    return version.values(value, attributeName, criteria);
  }

  @Request(RequestType.SESSION)
  public InputStream exportShapefile(String sessionId, String oid, String criteria)
  {
    return GeoRegistryUtil.exportListTypeShapefile(oid, criteria);
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId, String oid, String criteria)
  {
    return GeoRegistryUtil.exportListTypeExcel(oid, criteria);
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

      if (version.getWorking())
      {
        throw new UnsupportedOperationException("Working versions cannot be deleted");
      }

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
  public JsonArray getPublicVersions(String sessionId, String listOid)
  {
    JsonArray response = new JsonArray();

    ListType listType = ListType.get(listOid);
    final boolean isMember = Organization.isMember(listType.getOrganization());

    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(listType));
    query.AND(query.getWorking().EQ(false));
    query.ORDER_BY_DESC(query.getListType());
    query.ORDER_BY_DESC(query.getForDate());
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends ListTypeVersion> it = query.getIterator())
    {

      while (it.hasNext())
      {
        ListTypeVersion list = it.next();

        if (isMember || list.getListVisibility().equals(ListType.PUBLIC))
        {
          response.add(list.toJSON(false));
        }
      }
    }

    return response;
  }

  @Request(RequestType.SESSION)
  public JsonArray getGeospatialVersions(String sessionId, String startDate, String endDate)
  {
    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
    // query.WHERE(query.getWorking().EQ(false));

    if (startDate != null && startDate.length() > 0)
    {
      query.AND(query.getForDate().GE(GeoRegistryUtil.parseDate(startDate)));
    }

    if (endDate != null && endDate.length() > 0)
    {
      query.AND(query.getForDate().LE(GeoRegistryUtil.parseDate(endDate)));
    }

    query.ORDER_BY_DESC(query.getListType());
    query.ORDER_BY_DESC(query.getForDate());
    query.ORDER_BY_DESC(query.getVersionNumber());

    Map<String, Set<String>> orgMap = new LinkedHashMap<String, Set<String>>();
    Map<String, Set<String>> typeMap = new LinkedHashMap<String, Set<String>>();
    Map<String, JsonObject> listMap = new LinkedHashMap<String, JsonObject>();

    try (OIterator<? extends ListTypeVersion> it = query.getIterator())
    {

      while (it.hasNext())
      {
        ListTypeVersion version = it.next();
        ListType listType = version.getListType();
        final boolean isMember = Organization.isMember(listType.getOrganization());

        if ( ( version.getWorking() && listType.doesActorHaveExploratoryPermission() ) || ( isMember || version.getGeospatialVisibility().equals(ListType.PUBLIC) ))
        {
          if (!listMap.containsKey(listType.getOid()))
          {
            JsonObject object = new JsonObject();
            object.addProperty("label", listType.getDisplayLabel().getValue());
            object.addProperty("oid", listType.getOid());
            object.add("versions", new JsonArray());

            listMap.put(listType.getOid(), object);
            
            String gotCode = listType.getGeoObjectType().getCode();
            if (!typeMap.containsKey(gotCode))
            {
              typeMap.put(gotCode, new HashSet<String>());
            }
            typeMap.get(gotCode).add(listType.getOid());
            
            String orgCode = listType.getOrganization().getCode();
            if (!orgMap.containsKey(orgCode))
            {
              orgMap.put(orgCode, new HashSet<String>());
            }
            orgMap.get(orgCode).add(gotCode);
          }

          JsonObject object = new JsonObject();
          object.addProperty("oid", version.getOid());
          object.addProperty("forDate", GeoRegistryUtil.formatDate(version.getForDate(), false));
          object.addProperty("versionNumber", version.getVersionNumber());

          listMap.get(listType.getOid()).get("versions").getAsJsonArray().add(object);
        }
      }
    }
    
    JsonArray jaOrgs = new JsonArray();

    for (String orgCode : orgMap.keySet())
    {
      Organization org = ServiceFactory.getMetadataCache().getOrganization(orgCode).get();
      
      JsonObject joOrg = new JsonObject();
      
      joOrg.addProperty("orgCode", orgCode);
      
      joOrg.add("orgLabel", LocalizedValueConverter.convertNoAutoCoalesce(org.getDisplayLabel()).toJSON());
      
      JsonArray jaTypes = new JsonArray();
      
      for (String gotCode : orgMap.get(orgCode))
      {
        ServerGeoObjectType type = ServiceFactory.getMetadataCache().getGeoObjectType(gotCode).get();
        
        JsonObject joType = new JsonObject();
        
        joType.addProperty("typeCode", gotCode);
        
        joType.add("typeLabel", type.getLabel().toJSON());
        
        JsonArray jaLists = new JsonArray();
        
        for (String listOid : typeMap.get(gotCode))
        {
          JsonObject joListType = listMap.get(listOid);
          
          JsonArray jaVersions = joListType.get("versions").getAsJsonArray();
          
          if (jaVersions.size() > 0)
          {
            jaLists.add(joListType);
          }
        }
        
        joType.add("lists", jaLists);
        
        if (jaLists.size() > 0)
        {
          jaTypes.add(joType);
        }
      }
      
      joOrg.add("types", jaTypes);
      
      if (jaTypes.size() > 0)
      {
        jaOrgs.add(joOrg);
      }
    }
    
    return jaOrgs;
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
  
  private void enforceReadPermissions(ListType listType)
  {
    ServerGeoObjectType geoObjectType = listType.getGeoObjectType();
    Organization organization = geoObjectType.getOrganization();

    if (!ServiceFactory.getGeoObjectPermissionService().canRead(organization.getCode(), geoObjectType))
    {
      CreateListPermissionException ex = new CreateListPermissionException();
      ex.setOrganization(organization.getDisplayLabel().getValue());
      throw ex;
    }
  }
}