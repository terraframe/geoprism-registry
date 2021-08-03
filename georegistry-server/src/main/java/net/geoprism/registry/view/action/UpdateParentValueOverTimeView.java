package net.geoprism.registry.view.action;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.action.InvalidChangeRequestException;
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
    final ServerHierarchyType hierarchyType = ServerHierarchyType.get(parentView.getHierarchyCode());
    
    if (this.action.equals(UpdateActionType.DELETE))
    {
      EdgeObject edge = this.getEdgeByOid(this.oid, go, hierarchyType);
      
      if (edge == null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      edge.delete();
    }
    else if (this.action.equals(UpdateActionType.UPDATE))
    {
      EdgeObject edge = this.getEdgeByOid(this.oid, go, hierarchyType);
      
      if (edge == null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      final VertexServerGeoObject newParent = this.getNewValueAsGO();
      final String parentCode = newParent == null ? null : newParent.getCode();
      
      String currentCode = edge.getParent().getObjectValue(DefaultAttribute.CODE.getName());
      
      if (this.newValue != null
          && (currentCode != parentCode))
      {
        edge.delete();
        
        if (newParent != null)
        {
          go.addParent(newParent, hierarchyType, this.newStartDate, this.newEndDate);
        }
        
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
      final VertexServerGeoObject newParent = this.getNewValueAsGO();
      
      if (newParent == null || this.newStartDate == null || this.newEndDate == null)
      {
        throw new InvalidChangeRequestException();
      }
      
      EdgeObject edge = go.getEdge(newParent, hierarchyType, this.newStartDate, this.newEndDate);
      
      if (edge != null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      go.addParent(newParent, hierarchyType, this.newStartDate, this.newEndDate);
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported action type [" + this.action + "].");
    }
  }
  
  public VertexServerGeoObject getNewValueAsGO()
  {
    if (this.newValue != null && !this.newValue.isJsonNull())
    {
      String[] newValueSplit = (this.getNewValue().getAsString()).split(VALUE_SPLIT_TOKEN);
      String parentTypeCode = newValueSplit[0];
      String parentCode = newValueSplit[1];
      
      ServerGeoObjectType parentType = ServerGeoObjectType.get(parentTypeCode);
      final VertexServerGeoObject parent = new VertexGeoObjectStrategy(parentType).getGeoObjectByCode(parentCode);
      
      return parent;
    }
    
    return null;
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
