package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.SortedSet;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;

import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.view.TypeInfo;

public class ServerGeoObjectEventBuilder extends AbstractGeoObjectEventBuilder<ServerGeoObjectIF>
{
  public ServerGeoObjectEventBuilder(GeoObjectBusinessServiceIF service)
  {
    super(service);
  }

  @Override
  public String getCode()
  {
    return this.getOrThrow().getCode();
  }

  @Override
  public TypeInfo getType()
  {
    return this.getOrThrow().getType().getTypeInfo();
  }

  @Override
  protected JsonObject toJSON()
  {
    return toDTO().toJSON();
  }

  protected GeoObjectOverTime toDTO()
  {
    return this.service.toGeoObjectOverTime(getOrThrow(), false, false);
  }

  @Override
  protected void removeAllEdges(ServerHierarchyType hierarchyType)
  {
    // Delete the current edges and recreate the new ones
    final SortedSet<EdgeObject> edges = this.getOrThrow().getEdges(hierarchyType);

    for (EdgeObject edge : edges)
    {
      Date startDate = edge.getObjectValue(EdgeType.START_DATE);
      Date endDate = edge.getObjectValue(EdgeType.END_DATE);
      String uid = edge.getObjectValue(DefaultAttribute.UID.getName());

      this.addEvent(new GeoObjectRemoveParentEvent(getCode(), getType(), uid, hierarchyType.getTypeInfo(), startDate, endDate));
    }
  }

}
