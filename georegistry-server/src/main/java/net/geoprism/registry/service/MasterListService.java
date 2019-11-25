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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListQuery;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.progress.ProgressService;

public class MasterListService
{

  @Request(RequestType.SESSION)
  public JsonArray listAll(String sessionId)
  {
    return list();
  }

  public JsonArray list()
  {
    JsonArray response = new JsonArray();

    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getDisplayLabel().localize());

    OIterator<? extends MasterList> it = query.getIterator();

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    try
    {
      while (it.hasNext())
      {
        MasterList list = it.next();

        JsonObject object = new JsonObject();
        object.addProperty("label", list.getDisplayLabel().getValue());
        object.addProperty("oid", list.getOid());

        object.addProperty("createDate", format.format(list.getCreateDate()));
        object.addProperty("lasteUpdateDate", format.format(list.getLastUpdateDate()));

        response.add(object);
      }
    }
    finally
    {
      it.close();
    }

    return response;
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
  public JsonObject createVersion(String sessionId, String oid, Date forDate)
  {
    MasterList masterList = MasterList.get(oid);
    MasterListVersion version = masterList.getOrCreateVersion(forDate);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return version.toJSON(false);
  }

  @Request(RequestType.SESSION)
  public JsonObject publish(String sessionId, String oid)
  {
    MasterListVersion version = MasterListVersion.get(oid);

    return version.publish();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid)
  {
    return MasterList.get(oid).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getVersions(String sessionId, String oid)
  {
    return MasterList.get(oid).toJSON(true);
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
