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
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.resource.ApplicationResource;

import net.geoprism.registry.axon.command.repository.BusinessObjectCompositeCommand;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.service.business.ServiceFactory;

public class BusinessGeoobjectEdgeJsonImporter
{
  private static final Logger logger = LoggerFactory.getLogger(BusinessGeoobjectEdgeJsonImporter.class);

  private ApplicationResource resource;

  private CommandGateway      gateway;

  private String              defaultEdgeTypeCode;

  private DataSource          source;

  public BusinessGeoobjectEdgeJsonImporter(ApplicationResource resource, String defaultEdgeTypeCode, DataSource source)
  {
    this.resource = resource;
    this.source = source;
    this.gateway = ServiceFactory.getBean(CommandGateway.class);
    this.defaultEdgeTypeCode = defaultEdgeTypeCode;
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

        String geoCode = joEdge.get("source").getAsString();
        String geoTypeCode = joEdge.get("sourceType").getAsString();
        String businessCode = joEdge.get("target").getAsString();
        String businessTypeCode = joEdge.get("targetType").getAsString();
        String edgeTypeCode = joEdge.has("edgeType") ? joEdge.get("edgeType").getAsString() : defaultEdgeTypeCode;

        BusinessObjectAddGeoObjectEvent event = new BusinessObjectAddGeoObjectEvent(businessCode, businessTypeCode, edgeTypeCode, geoCode, geoTypeCode, EdgeDirection.PARENT, source.getCode());

        this.gateway.sendAndWait(new BusinessObjectCompositeCommand(businessCode, businessTypeCode, Arrays.asList(event)));

        if (j % 500 == 0)
        {
          logger.info("Imported record " + j + ".");
        }
      }
    }

  }
}
