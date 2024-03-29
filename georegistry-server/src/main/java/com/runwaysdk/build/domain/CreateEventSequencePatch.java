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
package com.runwaysdk.build.domain;

import org.apache.commons.collections4.map.HashedMap;

import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.transition.TransitionEvent;

public class CreateEventSequencePatch
{
  public static void main(String[] args)
  {
    new CreateEventSequencePatch().doIt();
  }

  @Request
  private void doIt()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("CREATE SEQUENCE " + TransitionEvent.EVENT_SEQUENCE + " TYPE ORDERED");

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest graphRequest = service.getGraphDBRequest();

    service.command(graphRequest, statement.toString(), new HashedMap<>());
  }
}
