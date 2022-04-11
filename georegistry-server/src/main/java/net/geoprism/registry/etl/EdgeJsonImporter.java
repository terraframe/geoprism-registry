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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class EdgeJsonImporter
{
  private static final Logger logger = LoggerFactory.getLogger(EdgeJsonImporter.class);
  
  protected GeoObjectCache sourceCache = new GeoObjectCache();
  
  protected GeoObjectCache targetCache = new GeoObjectCache();

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

          ServerGeoObjectIF source = sourceCache.getByCode(sourceCode, sourceTypeCode);
          ServerGeoObjectIF target = targetCache.getByCode(targetCode, targetTypeCode);

          source.addGraphChild(target, graphType, this.startDate, this.endDate, this.validate);
        }
      }
    }
  }

}
