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
package net.geoprism.registry.test.graph;

import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.RequestParamter;

import net.geoprism.registry.controller.DirectedAcyclicGraphController;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerParentGraphNode;
import net.geoprism.registry.test.TestControllerWrapper;
import net.geoprism.registry.test.TestRegistryAdapterClient;

public class DirectedAcyclicGraphControllerWrapper extends TestControllerWrapper
{

  private DirectedAcyclicGraphController controller = new DirectedAcyclicGraphController();
  
  public DirectedAcyclicGraphControllerWrapper(TestRegistryAdapterClient adapter, ClientRequestIF clientRequest)
  {
    super(adapter, clientRequest);
  }
  
  public ServerChildGraphNode getChildren(String parentCode, String parentTypeCode, String directedGraphCode, Boolean recursive, Date date)
  {
    return ServerChildGraphNode.fromJSON(JsonParser.parseString(responseToString(this.controller.getChildren(this.clientRequest, parentCode, parentTypeCode, directedGraphCode, recursive, stringifyDate(date)))).getAsJsonObject());
  }
  
  public ServerParentGraphNode getParents(String childCode, String childTypeCode, String directedGraphCode, Boolean recursive, Date date)
  {
    return ServerParentGraphNode.fromJSON(JsonParser.parseString(responseToString(this.controller.getParents(this.clientRequest, childCode, childTypeCode, directedGraphCode, recursive, stringifyDate(date)))).getAsJsonObject());
  }
  
  public ServerParentGraphNode addChild(String parentCode, String parentTypeCode, String childCode, String childTypeCode, String directedGraphCode, Date startDate, Date endDate)
  {
    return ServerParentGraphNode.fromJSON(JsonParser.parseString(responseToString(this.controller.addChild(this.clientRequest, parentCode, parentTypeCode, childCode, childTypeCode, directedGraphCode, stringifyDate(startDate), stringifyDate(endDate)))).getAsJsonObject());
  }
  
  public void removeChild(String parentCode, String parentTypeCode, String childCode, String childTypeCode, String directedGraphCode, Date startDate, Date endDate)
  {
    this.controller.removeChild(this.clientRequest, parentCode, parentTypeCode, childCode, childTypeCode, directedGraphCode, stringifyDate(startDate), stringifyDate(endDate));
  }
  
  

}
