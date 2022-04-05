package net.geoprism.registry.service;

import java.util.Date;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGraphNode;

public abstract class GraphService
{
  public abstract GraphType getGraphType(String code);

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String parentCode, String parentGeoObjectTypeCode, String graphTypeCode, Boolean recursive, Date date)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    ServerGraphNode node = parent.getGraphChildren(graphType, recursive, date);
    return node.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getParents(String sessionId, String childCode, String childGeoObjectTypeCode, String graphTypeCode, Boolean recursive, Date date)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF child = service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    ServerGraphNode node = child.getGraphChildren(graphType, recursive, date);
    return node.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject addChild(String sessionId, String parentCode, String parentGeoObjectTypeCode, String childCode, String childGeoObjectTypeCode, String graphTypeCode, Date startDate, Date endDate)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);
    ServerGeoObjectIF child = service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    ServerGraphNode node = parent.addGraphChild(child, graphType, startDate, endDate, true);
    return node.toJSON();
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String parentCode, String parentGeoObjectTypeCode, String childCode, String childGeoObjectTypeCode, String graphTypeCode, Date startDate, Date endDate)
  {
    ServerGeoObjectService service = ServiceFactory.getGeoObjectService();

    ServerGeoObjectIF parent = service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);
    ServerGeoObjectIF child = service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);
    GraphType graphType = this.getGraphType(graphTypeCode);

    // ServiceFactory.getGeoObjectRelationshipPermissionService().enforceCanAddChild(ht.getOrganization().getCode(),
    // parent.getType(), child.getType());

    parent.removeGraphChild(child, graphType, startDate, endDate);
  }

}
