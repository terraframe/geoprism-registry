package net.geoprism.registry.view.action;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateParentValueOverTimeView extends UpdateValueOverTimeView
{
  public void execute(UpdateParentView cotView, VertexServerGeoObject go)
  {
    final ServerHierarchyType hierarchyType = ServerHierarchyType.get(cotView.getHierarchyCode());
    final VertexServerGeoObject parent = new VertexGeoObjectStrategy(go.getType()).getGeoObjectByCode(String.valueOf(this.getNewValue()));
    
    if (this.action.equals(UpdateActionType.DELETE))
    {
      EdgeObject edge = this.getEdgeByOid((String) this.newValue, go, hierarchyType);
      
      if (edge == null)
      {
        // TODO throw an exception?
      }
      
      edge.delete();
    }
    else if (this.action.equals(UpdateActionType.UPDATE))
    {
      EdgeObject edge = this.getEdgeByOid((String) this.newValue, go, hierarchyType);
      
      if (edge == null)
      {
        // TODO throw an exception?
      }
      
      String currentCode = edge.getParent().getObjectValue(DefaultAttribute.CODE.getName());
      
      if (currentCode != this.newValue)
      {
        edge.delete();
        go.addParent(parent, hierarchyType, this.newStartDate, this.newEndDate);
        return;
      }
      
      if (newStartDate != null)
      {
        edge.setValue(GeoVertex.START_DATE, newStartDate);
      }
      
      if (newEndDate != null)
      {
        edge.setValue(GeoVertex.END_DATE, newEndDate);
      }
      
      edge.apply();
    }
    else if (this.action.equals(UpdateActionType.CREATE))
    {
      EdgeObject edge = go.getEdge(parent, hierarchyType, this.newStartDate, this.newEndDate);
      
      if (edge != null)
      {
        // TODO throw an exception?
      }
      
      go.addParent(parent, hierarchyType, this.newStartDate, this.newEndDate);
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported action type [" + this.action + "].");
    }
  }
  
  private EdgeObject getEdgeByOid(String edgeRid, VertexServerGeoObject go, ServerHierarchyType hierarchyType)
  {
    String statement = "SELECT expand(inE('" + hierarchyType.getMdEdge().getDBClassName() + "'))";
    statement += " FROM :child";
    statement += " WHERE :edgerid = @rid";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", go.getVertex().getRID());
    query.setParameter("edgerid", edgeRid);
    
    return query.getSingleResult();
  }
  
  @Override
  public void execute(UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go)
  {
    throw new UnsupportedOperationException("Cannot invoke this execute method.");
  }
}
