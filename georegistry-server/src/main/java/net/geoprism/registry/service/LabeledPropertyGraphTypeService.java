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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.LabeledPropertyGraphType;
import net.geoprism.registry.LabeledPropertyGraphTypeEntry;
import net.geoprism.registry.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.etl.DuplicateJobException;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJob;
import net.geoprism.registry.etl.PublishLabeledPropertyGraphTypeVersionJobQuery;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Component
public class LabeledPropertyGraphTypeService
{
  @Request(RequestType.SESSION)
  public JsonArray listAll(String sessionId)
  {
    return LabeledPropertyGraphType.list();
  }

  @Request(RequestType.SESSION)
  public JsonObject listForType(String sessionId, String typeCode)
  {
    return LabeledPropertyGraphType.listForType(typeCode);
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, JsonObject list)
  {
    LabeledPropertyGraphType mList = LabeledPropertyGraphType.apply(list);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // Auto publish the working versions of the lists
    List<LabeledPropertyGraphTypeVersion> versions = mList.getVersions();
    for (LabeledPropertyGraphTypeVersion version : versions)
    {
      this.publishVersion(sessionId, version.getOid());
    }

    return mList.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject createEntries(String sessionId, String oid)
  {
    LabeledPropertyGraphType mList = LabeledPropertyGraphType.get(oid);
    mList.createEntries();

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return mList.toJSON(true);
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    try
    {
      LabeledPropertyGraphType listType = LabeledPropertyGraphType.get(oid);

      listType.delete();
      // Refresh the users session
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
    catch (DataNotFoundException e)
    {
      e.printStackTrace();
      // Do nothing
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject createVersion(String sessionId, String oid)
  {
    LabeledPropertyGraphTypeEntry entry = LabeledPropertyGraphTypeEntry.get(oid);
    LabeledPropertyGraphType listType = entry.getGraphType();

    if (!listType.isValid())
    {
      throw new InvalidMasterListException();
    }

    String version = entry.publish();

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return JsonParser.parseString(version).getAsJsonObject();
  }

  @Request(RequestType.SESSION)
  public JsonObject getPublishJobs(String sessionId, String oid, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    QueryFactory qf = new QueryFactory();

    final PublishLabeledPropertyGraphTypeVersionJobQuery query = new PublishLabeledPropertyGraphTypeVersionJobQuery(qf);
    query.WHERE(query.getGraphType().EQ(oid));
    query.ORDER_BY_DESC(query.getCreateDate());
    // query.ORDER_BY(ihq.get(sortAttr), order);
    query.restrictRows(pageSize, pageNumber);

    try (OIterator<? extends PublishLabeledPropertyGraphTypeVersionJob> it = query.getIterator())
    {
      List<JsonSerializable> results = new LinkedList<>(it.getAll());

      return new Page<JsonSerializable>(query.getCount(), query.getPageNumber(), query.getPageSize(), results).toJSON();
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject getPublishJob(String sessionId, String historyOid)
  {
    JobHistory history = JobHistory.get(historyOid);

    try (OIterator<? extends ExecutableJob> it = history.getAllJob())
    {
      PublishLabeledPropertyGraphTypeVersionJob job = (PublishLabeledPropertyGraphTypeVersionJob) it.next();

      return job.toJSON(history);
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject publishVersion(String sessionId, String oid)
  {
    LabeledPropertyGraphTypeVersion version = LabeledPropertyGraphTypeVersion.get(oid);
    JobHistory history = version.createPublishJob();

    JsonObject resp = new JsonObject();
    resp.addProperty("jobOid", history.getOid());
    return resp;
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return LabeledPropertyGraphType.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getEntries(String sessionId, String oid)
  {
    LabeledPropertyGraphType listType = LabeledPropertyGraphType.get(oid);

    return listType.toJSON(true);
  }

  @Request(RequestType.SESSION)
  public JsonArray getVersions(String sessionId, String oid)
  {
    LabeledPropertyGraphTypeEntry listType = LabeledPropertyGraphTypeEntry.get(oid);

    return listType.getVersionJson();
  }

  @Request(RequestType.SESSION)
  public JsonObject getEntry(String sessionId, String oid)
  {
    return LabeledPropertyGraphTypeVersion.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getVersion(String sessionId, String oid)
  {
    return LabeledPropertyGraphTypeVersion.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonArray fetchVersionsAsListVersion(String sessionId, String oids)
  {
    JsonArray ja = new JsonArray();

    for (String oid : StringUtils.split(oids, ","))
    {
      ja.add(serializeVersionAsListVersion(LabeledPropertyGraphTypeVersion.get(oid)));
    }

    return ja;
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
      LabeledPropertyGraphTypeVersion version = LabeledPropertyGraphTypeVersion.get(oid);

      version.remove();

      // Refresh the users session
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
    catch (DataNotFoundException e)
    {
      // Do nothing
    }
  }

  private JsonObject serializeVersionAsListVersion(LabeledPropertyGraphTypeVersion version)
  {
    JsonObject object = new JsonObject();
    object.addProperty("oid", version.getOid());
    object.addProperty("forDate", GeoRegistryUtil.formatDate(version.getForDate(), false));
    object.addProperty("versionNumber", version.getVersionNumber());
    return object;
  }

}
