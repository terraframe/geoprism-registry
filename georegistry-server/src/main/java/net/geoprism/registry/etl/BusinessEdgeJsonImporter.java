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

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.axon.command.repository.BusinessObjectCompositeCommand;
import net.geoprism.registry.axon.event.repository.BusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.projection.BusinessObjectRepositoryProjection;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class BusinessEdgeJsonImporter
{
  private static final Logger                logger = LoggerFactory.getLogger(BusinessEdgeJsonImporter.class);

  private ApplicationResource                resource;

  private boolean                            validate;

  private BusinessEdgeTypeBusinessServiceIF  edgeTypeService;

  private CommandGateway                     gateway;

  private BusinessObjectRepositoryProjection projection;

  public BusinessEdgeJsonImporter(ApplicationResource resource, boolean validate)
  {
    this.resource = resource;
    this.validate = validate;

    this.edgeTypeService = ServiceFactory.getBean(BusinessEdgeTypeBusinessServiceIF.class);
    this.gateway = ServiceFactory.getBean(CommandGateway.class);
    this.projection = ServiceFactory.getBean(BusinessObjectRepositoryProjection.class);
  }

  public void importData() throws JsonSyntaxException, IOException
  {
    try (InputStream stream = resource.openNewStream())
    {
      JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

      JsonArray edgeTypes = data.get("edgeTypes").getAsJsonArray();

      for (int i = 0; i < edgeTypes.size(); ++i)
      {
        JsonObject joGraphType = edgeTypes.get(i).getAsJsonObject();

        final String code = joGraphType.get("code").getAsString();

        final BusinessEdgeType edgeType = this.edgeTypeService.getByCodeOrThrow(code);
        BusinessType sourceType = this.edgeTypeService.getParent(edgeType).toBusinessType();
        BusinessType targetType = this.edgeTypeService.getChild(edgeType).toBusinessType();

        JsonArray edges = joGraphType.get("edges").getAsJsonArray();

        logger.info("About to import [" + edges.size() + "] edges as MdEdge [" + edgeType.getCode() + "].");

        for (int j = 0; j < edges.size(); ++j)
        {
          JsonObject joEdge = edges.get(j).getAsJsonObject();

          String sourceCode = joEdge.get("source").getAsString();
          String targetCode = joEdge.get("target").getAsString();

          BusinessObjectCreateEdgeEvent event = new BusinessObjectCreateEdgeEvent(sourceCode, sourceType.getCode(), edgeType.getCode(), targetCode, targetType.getCode(), validate);
          
          this.gateway.sendAndWait(new BusinessObjectCompositeCommand(sourceCode, sourceType.getCode(), Arrays.asList(event)));

          if (j % 50 == 0)
          {
            logger.info("Imported record " + j + ".");
          }
        }
      }
    }
    finally
    {
      this.projection.clearCache();
    }
  }

}
