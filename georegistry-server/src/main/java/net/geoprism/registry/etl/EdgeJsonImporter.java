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
package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
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

import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public class EdgeJsonImporter
{
  private static final Logger logger = LoggerFactory.getLogger(EdgeJsonImporter.class);
  
  protected GeoObjectCache goCache = new GeoObjectCache();
  
  protected Map<String, Object> goRidCache = new LinkedHashMap<String, Object>() {
    public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
    {
      final int cacheSize = 25000;
      return size() > cacheSize;
    }
  };

  private ApplicationResource resource;

  private Date                startDate;

  private Date                endDate;

  private boolean             validate;

  public EdgeJsonImporter(ApplicationResource resource, Date startDate, Date endDate, boolean validate)
  {
    this.resource = resource;
    this.startDate = startDate;
    this.endDate = endDate;
    this.validate = validate;
  }

  public void importData() throws JsonSyntaxException, IOException
  {
    try (InputStream stream = resource.openNewStream())
    {
      JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

      JsonArray graphTypes = data.get("graphTypes").getAsJsonArray();

      for (int i = 0; i < graphTypes.size(); ++i)
      {
        JsonObject joGraphType = graphTypes.get(i).getAsJsonObject();

        final String graphTypeClass = joGraphType.get("graphTypeClass").getAsString();
        final String code = joGraphType.get("code").getAsString();

        final GraphType graphType = GraphType.getByCode(graphTypeClass, code);

        JsonArray edges = joGraphType.get("edges").getAsJsonArray();

        logger.info("About to import [" + edges.size() + "] edges as MdEdge [" + graphType.getCode() + "].");

        for (int j = 0; j < edges.size(); ++j)
        {
          JsonObject joEdge = edges.get(j).getAsJsonObject();

          String sourceCode = joEdge.get("source").getAsString();
          String sourceTypeCode = joEdge.get("sourceType").getAsString();
          String targetCode = joEdge.get("target").getAsString();
          String targetTypeCode = joEdge.get("targetType").getAsString();

          if (validate)
          {
            ServerGeoObjectIF source = goCache.getOrFetchByCode(sourceCode, sourceTypeCode);
            ServerGeoObjectIF target = goCache.getOrFetchByCode(targetCode, targetTypeCode);
  
            source.addGraphChild(target, graphType, this.startDate, this.endDate, this.validate);
          }
          else
          {
            Object childRid = getOrFetchRid(sourceCode, sourceTypeCode);
            Object parentRid = getOrFetchRid(targetCode, targetTypeCode);
            
            this.newEdge(childRid, parentRid, graphType, startDate, endDate);
          }
          
          if (j % 500 == 0)
          {
            logger.info("Imported record " + j + ".");
          }
        }
      }
    }
  }
  
  private Object getOrFetchRid(String code, String typeCode)
  {
    String typeDbClassName = ServerGeoObjectType.get(typeCode).getDBClassName();
    
    Object rid = this.goRidCache.get(typeCode + "$#!" + code);
    
    if (rid == null)
    {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);
      
      rid = query.getSingleResult();
      
      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Geo-Object with code " + code + " on table " + typeDbClassName);
      }
      
      this.goRidCache.put(typeCode + "$#!" + code, rid);
    }
    
    return rid;
  }
  
  public void newEdge(Object childRid, Object parentRid, GraphType type, Date startDate, Date endDate)
  {
    String clazz = type.getMdEdgeDAO().getDBClassName();
    
    String statement = "CREATE EDGE " + clazz + " FROM :childRid TO :parentRid";
    statement += " SET startDate=:startDate, endDate=:endDate, oid=:oid";
    
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    parameters.put("childRid", childRid);
    parameters.put("parentRid", parentRid);
    parameters.put("startDate", startDate);
    parameters.put("endDate", endDate);

    service.command(request, statement, parameters);
  }

}
