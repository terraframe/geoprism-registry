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
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEdgeEvent;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class BusinessGeoObjectEdgeJsonImporter
{
  private static final Logger               logger = LoggerFactory.getLogger(BusinessGeoObjectEdgeJsonImporter.class);

  private ApplicationResource               resource;

  private EventGateway                      gateway;

  private String                            defaultEdgeTypeCode;

  private DataSource                        source;

  private BusinessEdgeTypeBusinessServiceIF service;

  public BusinessGeoObjectEdgeJsonImporter(ApplicationResource resource, String defaultEdgeTypeCode, DataSource source)
  {
    this.resource = resource;
    this.source = source;
    this.defaultEdgeTypeCode = defaultEdgeTypeCode;
    this.gateway = ServiceFactory.getBean(EventGateway.class);
    this.service = ServiceFactory.getBean(BusinessEdgeTypeBusinessServiceIF.class);
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
        Date startDate = GeoRegistryUtil.parseDate(joEdge.get("startDate").getAsString());
        Date endDate = GeoRegistryUtil.parseDate(joEdge.get("endDate").getAsString());
        String edgeTypeCode = joEdge.has("edgeType") ? joEdge.get("edgeType").getAsString() : defaultEdgeTypeCode;

        BusinessObjectApplyEdgeEvent event = new BusinessObjectApplyEdgeEvent(sourceCode, sourceTypeCode, edgeTypeCode, targetCode, targetTypeCode, startDate, endDate, source.getCode(), ImportStrategy.NEW_ONLY, true);

        this.gateway.publish(GenericEventMessage.asEventMessage(event));

        if (j % 500 == 0)
        {
          logger.info("Imported record " + j + ".");
        }
      }
    }

  }
}
