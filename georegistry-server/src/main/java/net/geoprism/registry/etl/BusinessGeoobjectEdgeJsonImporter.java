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
package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.service.ServiceFactory;

public class BusinessGeoobjectEdgeJsonImporter
{
  private static final Logger   logger   = LoggerFactory.getLogger(BusinessGeoobjectEdgeJsonImporter.class);

  protected BusinessObjectCache boCache  = new BusinessObjectCache();

  protected GeoObjectCache      goCache  = new GeoObjectCache();

  protected Map<String, Object> ridCache = new LinkedHashMap<String, Object>()
                                         {
                                           public boolean removeEldestEntry(@SuppressWarnings("rawtypes")
                                           Map.Entry eldest)
                                           {
                                             final int cacheSize = 10000;
                                             return size() > cacheSize;
                                           }
                                         };

  private ApplicationResource   resource;

  public BusinessGeoobjectEdgeJsonImporter(ApplicationResource resource)
  {
    this.resource = resource;
  }

  public void importData() throws JsonSyntaxException, IOException
  {
    try (InputStream stream = resource.openNewStream())
    {
      JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

      JsonArray edges = data.get("edges").getAsJsonArray();

      for (int j = 0; j < edges.size(); ++j)
      {
        JsonObject joEdge = edges.get(j).getAsJsonObject();

        String sourceCode = joEdge.get("source").getAsString();
        String sourceTypeCode = joEdge.get("sourceType").getAsString();
        String targetCode = joEdge.get("target").getAsString();
        String targetTypeCode = joEdge.get("targetType").getAsString();

        BusinessType targetType = BusinessType.getByCode(targetTypeCode);

        String sourceDbClassName = ServiceFactory.getMetadataCache().getGeoObjectType(sourceTypeCode).get().getMdVertex().getDBClassName();
        String targetDbClassName = targetType.getMdVertexDAO().getDBClassName();

        Object parentRid = getOrFetchRid(sourceCode, sourceDbClassName);
        Object childRid = getOrFetchRid(targetCode, targetDbClassName);

        this.newEdge(childRid, parentRid, targetType);

        long cacheSize = ridCache.size();

        if (j % 500 == 0)
        {
          logger.info("Imported record " + j + ". Cache size is " + cacheSize);
        }
      }
    }

  }

  private Object getOrFetchRid(String code, String typeDbClassName)
  {

    Object rid = this.ridCache.get(typeDbClassName + "$#!" + code);

    if (rid == null)
    {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);

      rid = query.getSingleResult();

      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Geo-Object with code " + code + " on table " + typeDbClassName);
      }

      this.ridCache.put(typeDbClassName + "$#!" + code, rid);
    }

    return rid;
  }

  public void newEdge(Object childRid, Object parentRid, BusinessType type)
  {
    String clazz = type.getMdEdgeDAO().getDBClassName();

    String statement = "CREATE EDGE " + clazz + " FROM :parentRid TO :childRid";
    statement += " SET oid=:oid";

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    parameters.put("childRid", childRid);
    parameters.put("parentRid", parentRid);

    service.command(request, statement, parameters);
  }

}
