package net.geoprism.registry.view.action;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateParentValueOverTimeView extends UpdateValueOverTimeView
{
  public static final String VALUE_SPLIT_TOKEN = "_~VST~_";
  
  @Override
  public void execute(UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go)
  {
    UpdateParentView parentView = (UpdateParentView) cotView;
    
    String[] newValueSplit = (this.getNewValue().getAsString()).split(VALUE_SPLIT_TOKEN);
    String parentTypeCode = newValueSplit[0];
    String parentCode = newValueSplit[1];
    
    ServerGeoObjectType parentType = ServerGeoObjectType.get(parentTypeCode);
    
    final ServerHierarchyType hierarchyType = ServerHierarchyType.get(parentView.getHierarchyCode());
    final VertexServerGeoObject parent = new VertexGeoObjectStrategy(parentType).getGeoObjectByCode(parentCode);
    
    if (this.action.equals(UpdateActionType.DELETE))
    {
      EdgeObject edge = this.getEdgeByOid(this.oid, go, hierarchyType);
      
      if (edge == null)
      {
        // TODO throw an exception?
      }
      
      edge.delete();
    }
    else if (this.action.equals(UpdateActionType.UPDATE))
    {
      EdgeObject edge = this.getEdgeByOid(this.oid, go, hierarchyType);
      
      if (edge == null)
      {
        // TODO throw an exception?
      }
      
      String currentCode = edge.getParent().getObjectValue(DefaultAttribute.CODE.getName());
      
      if (currentCode != parentCode)
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
  
  private EdgeObject getEdgeByOid(String edgeOid, VertexServerGeoObject go, ServerHierarchyType hierarchyType)
  {
    String statement = "SELECT FROM (";
    statement += "SELECT expand(inE('" + hierarchyType.getMdEdge().getDBClassName() + "')) FROM :child";
    statement += ") WHERE :edgeoid = oid";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", go.getVertex().getRID());
    query.setParameter("edgeoid", edgeOid);
    
    return query.getSingleResult();
  }
}
