package net.geoprism.registry.service.business;

import java.util.Date;

import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.axon.event.repository.ObjectRemoveEdgeEvent;
import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.view.TypeInfo;

@Service
public class EventBusinessService
{
  @Autowired
  private EventGateway gateway;

  public void remove(EdgeType edgeType, EdgeObject edge)
  {
    VertexObject target = edge.getChild();
    VertexObject source = edge.getParent();

    String edgeUid = edge.getObjectValue(DefaultAttribute.UID.getName());
    Date startDate = edge.getObjectValue(EdgeType.START_DATE);
    Date endDate = edge.getObjectValue(EdgeType.END_DATE);

    String targetCode = target.getObjectValue(DefaultAttribute.CODE.getName());
    String targetTypeCode = target.getMdClass().getTypeName();
    TypeInfo targetType = new TypeInfo(edgeType.getTargetType(), targetTypeCode);

    String sourceCode = source.getObjectValue(DefaultAttribute.CODE.getName());
    String sourceTypeCode = source.getMdClass().getTypeName();
    TypeInfo sourceType = new TypeInfo(edgeType.getSourceType(), sourceTypeCode);

    // Create the event
    ObjectRemoveEdgeEvent event = new ObjectRemoveEdgeEvent(edgeUid, targetCode, targetType, sourceCode, sourceType, edgeType.getTypeInfo(), startDate, endDate);

    // Publish the event
    this.gateway.publish(GenericEventMessage.asEventMessage(event));

  }
}
