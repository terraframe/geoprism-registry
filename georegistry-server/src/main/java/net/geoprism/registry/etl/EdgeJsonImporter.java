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

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class EdgeJsonImporter
{
  private static final Logger logger = LoggerFactory.getLogger(EdgeJsonImporter.class);

  private InputStream         stream;

  private GraphType           graphType;

  private Date                startDate;

  private Date                endDate;

  public EdgeJsonImporter(InputStream stream, GraphType graphType, Date startDate, Date endDate)
  {
    this.stream = stream;
    this.graphType = graphType;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public void importData() throws JsonSyntaxException, IOException
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

    JsonArray edges = data.get("edges").getAsJsonArray();

    logger.info("About to import [" + edges.size() + "] edges as MdEdge [" + this.graphType.getCode() + "].");

    for (int i = 0; i < edges.size(); ++i)
    {
      JsonObject joEdge = edges.get(i).getAsJsonObject();

      String sourceCode = joEdge.get("source").getAsString();
      String sourceTypeCode = joEdge.get("sourceType").getAsString();
      String targetCode = joEdge.get("target").getAsString();
      String targetTypeCode = joEdge.get("targetType").getAsString();

      ServerGeoObjectIF source = service.getGeoObjectByCode(sourceCode, sourceTypeCode);
      ServerGeoObjectIF target = service.getGeoObjectByCode(targetCode, targetTypeCode);

      source.addGraphChild(target, this.graphType, this.startDate, this.endDate);
    }
  }

}
