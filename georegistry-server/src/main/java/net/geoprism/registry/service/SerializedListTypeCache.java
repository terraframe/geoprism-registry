/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.ListType;

public class SerializedListTypeCache
{
  private static SerializedListTypeCache INSTANCE = null;

  protected Map<String, JsonObject>      cache;

  public SerializedListTypeCache(int cacheSize)
  {
    this.init(cacheSize);
  }

  public SerializedListTypeCache()
  {
    this.init(10000);
  }

  public static synchronized SerializedListTypeCache getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new SerializedListTypeCache();
    }

    return INSTANCE;
  }

  @SuppressWarnings("serial")
  private void init(int cacheSize)
  {
    this.cache = Collections.synchronizedMap(new LinkedHashMap<String, JsonObject>(cacheSize + 1, .75F, true)
    {
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes")
      Map.Entry eldest)
      {
        return size() > cacheSize;
      }
    });
  }

  public long getSize()
  {
    return this.cache.size();
  }

  public JsonObject getByOid(String oid)
  {
    return this.cache.get(this.hash(oid));
  }

  protected String hash(String oid)
  {
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      String username = session.getUser().getSingleActorName();
      String locale = session.getLocale().toLanguageTag();
      String roles = StringUtils.join(session.getUser().assignedRoles(), ",");

      return oid + username + locale + roles;
    }
    else
    {
      return "SYSTEM" + oid;
    }
  }

  public JsonObject getOrFetchByOid(String oid)
  {
    synchronized (this.cache)
    {
      JsonObject json = this.cache.get(this.hash(oid));

      if (json == null)
      {
        json = ListType.get(oid).toJSON(true);

        this.cache.put(this.hash(oid), json);
      }

      return json;
    }
  }

  public void clear()
  {
    this.cache.clear();
  }

  public void remove(String oid)
  {
    this.cache.remove(this.hash(oid));
  }
}
