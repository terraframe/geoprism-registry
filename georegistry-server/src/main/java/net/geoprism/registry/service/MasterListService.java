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

import java.io.InputStream;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryQuery;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.etl.PublishMasterListJob;
import net.geoprism.registry.etl.PublishMasterListJobQuery;
import net.geoprism.registry.progress.ProgressService;

public class MasterListService
{

  @Request(RequestType.SESSION)
  public JsonArray listAll(String sessionId)
  {
    return MasterList.list();
  }

  @Request(RequestType.SESSION)
  public JsonArray listByOrg(String sessionId)
  {
    return MasterList.listByOrg();
  }

  @Request(RequestType.SESSION)
  public JsonObject create(String sessionId, JsonObject list)
  {
    MasterList mList = MasterList.create(list);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return mList.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    try
    {
      MasterList.get(oid).delete();

      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
    catch (DataNotFoundException e)
    {
      // Do nothing
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject createExploratoryVersion(String sessionId, String oid, Date forDate)
  {
    MasterList masterList = MasterList.get(oid);
    MasterListVersion version = masterList.createVersion(forDate, MasterListVersion.EXPLORATORY);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return version.toJSON(false);
  }

  @Request(RequestType.SESSION)
  public void createPublishedVersions(String sessionId, String oid)
  {
    MasterList masterList = MasterList.get(oid);
    masterList.publishFrequencyVersions();
  }

  @Request(RequestType.SESSION)
  public String createPublishedVersionsJob(String sessionId, String oid)
  {
    MasterList masterList = MasterList.get(oid);

    QueryFactory factory = new QueryFactory();

    PublishMasterListJobQuery query = new PublishMasterListJobQuery(factory);
    query.WHERE(query.getMasterList().EQ(masterList));

    JobHistoryQuery q = new JobHistoryQuery(factory);
    q.WHERE(q.getStatus().containsAny(AllJobStatus.NEW, AllJobStatus.QUEUED, AllJobStatus.RUNNING));
    q.AND(q.job(query));

    if (q.getCount() > 0)
    {
      throw new ProgrammingErrorException("This master list has already been queued for publication");
    }

    PublishMasterListJob job = new PublishMasterListJob();
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.setMasterList(masterList);
    job.apply();

    final JobHistory history = job.start();
    return history.getOid();
  }

  @Request(RequestType.SESSION)
  public JSONObject getPublishJobs(String sessionId, String oid, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    final SortOrder order = isAscending ? SortOrder.ASC : SortOrder.DESC;

    QueryFactory qf = new QueryFactory();

    final PublishMasterListJobQuery query = new PublishMasterListJobQuery(qf);
    query.WHERE(query.getMasterList().EQ(oid));
    // query.ORDER_BY(ihq.get(sortAttr), order);
    query.restrictRows(pageSize, pageNumber);

    OIterator<? extends PublishMasterListJob> it = query.getIterator();

    JSONArray results = new JSONArray();

    while (it.hasNext())
    {
      results.put(it.next().toJSON());
    }

    JSONObject page = new JSONObject();
    page.put("count", query.getCount());
    page.put("pageNumber", query.getPageNumber());
    page.put("pageSize", query.getPageSize());
    page.put("results", results);

    return page;
  }

  @Request(RequestType.SESSION)
  public JsonObject publish(String sessionId, String oid)
  {
    MasterListVersion version = MasterListVersion.get(oid);

    return version.publish();
  }

  @Request(RequestType.SESSION)
  public JsonObject generateShapefile(String sessionId, String oid)
  {
    MasterListVersion version = MasterListVersion.get(oid);
    version.generateShapefile();

    return version.toJSON(false);
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return MasterList.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getVersions(String sessionId, String oid, String versionType)
  {
    return MasterList.get(oid).toJSON(versionType);
  }

  @Request(RequestType.SESSION)
  public JsonObject getVersion(String sessionId, String oid)
  {
    return MasterListVersion.get(oid).toJSON(true);
  }

  @Request(RequestType.SESSION)
  public JsonObject data(String sessionId, String oid, Integer pageNumber, Integer pageSize, String filter, String sort)
  {
    MasterListVersion version = MasterListVersion.get(oid);
    return version.data(pageNumber, pageSize, filter, sort);
  }

  @Request(RequestType.SESSION)
  public JsonArray values(String sessionId, String oid, String value, String attributeName, String valueAttribute, String filterJson)
  {
    return MasterListVersion.get(oid).values(value, attributeName, valueAttribute, filterJson);
  }

  @Request(RequestType.SESSION)
  public InputStream exportShapefile(String sessionId, String oid, String filterJson)
  {
    return GeoRegistryUtil.exportMasterListShapefile(oid, filterJson);
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId, String oid, String filterJson)
  {
    return GeoRegistryUtil.exportMasterListExcel(oid, filterJson);
  }

  @Request(RequestType.SESSION)
  public InputStream downloadShapefile(String sessionId, String oid)
  {
    return MasterListVersion.get(oid).downloadShapefile();
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
      MasterListVersion.get(oid).delete();

      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
    catch (DataNotFoundException e)
    {
      // Do nothing
    }

  }

}
