package net.geoprism.registry.model.graph;

import java.util.Date;

import net.geoprism.registry.model.ServerGraphNode;

public interface GraphStrategy
{

  public <T extends ServerGraphNode> T getChildren(VertexServerGeoObject geoObject, Boolean recursive, Date date);

  public <T extends ServerGraphNode> T getParents(VertexServerGeoObject geoObject, Boolean recursive, Date date);

  public <T extends ServerGraphNode> T addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate);

  public <T extends ServerGraphNode> T addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate);

  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate);
}
