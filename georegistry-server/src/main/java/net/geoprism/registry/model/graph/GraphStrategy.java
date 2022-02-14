package net.geoprism.registry.model.graph;

import java.util.Date;

import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerParentGraphNode;

public interface GraphStrategy
{

  public ServerChildGraphNode getChildren(VertexServerGeoObject geoObject, Boolean recursive, Date date);

  public ServerParentGraphNode getParents(VertexServerGeoObject geoObject, Boolean recursive, Date date);

  public ServerParentGraphNode addChild(VertexServerGeoObject geoObject, VertexServerGeoObject child, Date startDate, Date endDate);

  public ServerParentGraphNode addParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate);

  public void removeParent(VertexServerGeoObject geoObject, VertexServerGeoObject parent, Date startDate, Date endDate);
}
