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
package net.geoprism.registry.service.business;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.axon.event.repository.ObjectRemoveEdgeEvent;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.view.TypeInfo;

@Service
public class EventBusinessService
{
  @Autowired
  private EventGateway                gateway;

  @Autowired
  private DataSourceBusinessServiceIF sourceService;

  public void publish(RepositoryEvent... events)
  {
    publish(Arrays.asList(events));
  }

  public void publish(List<RepositoryEvent> events)
  {
    this.gateway.publish(events.stream().map(GenericEventMessage::asEventMessage).toList());
  }

  public void remove(EdgeType edgeType, EdgeObject edge)
  {
    VertexObject target = edge.getChild();
    VertexObject source = edge.getParent();

    String edgeUid = edge.getObjectValue(DefaultAttribute.UID.getName());
    Date startDate = edge.getObjectValue(EdgeType.START_DATE);
    Date endDate = edge.getObjectValue(EdgeType.END_DATE);
    String dataSource = this.sourceService.get(edge.getObjectValue(DefaultAttribute.DATA_SOURCE.getName())).map(s -> s.getCode()).orElse(null);

    String targetCode = target.getObjectValue(DefaultAttribute.CODE.getName());
    String targetTypeCode = target.getMdClass().getTypeName();
    TypeInfo targetType = new TypeInfo(edgeType.getTargetType(), targetTypeCode);

    String sourceCode = source.getObjectValue(DefaultAttribute.CODE.getName());
    String sourceTypeCode = source.getMdClass().getTypeName();
    TypeInfo sourceType = new TypeInfo(edgeType.getSourceType(), sourceTypeCode);

    // Create the event
    ObjectRemoveEdgeEvent event = new ObjectRemoveEdgeEvent(edgeUid, sourceCode, sourceType, edgeType.getTypeInfo(), targetCode, targetType, startDate, endDate, dataSource);

    // Publish the event
    this.gateway.publish(GenericEventMessage.asEventMessage(event));

  }

}
