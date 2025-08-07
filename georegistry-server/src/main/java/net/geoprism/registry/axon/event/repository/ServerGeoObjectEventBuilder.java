package net.geoprism.registry.axon.event.repository;

import java.util.Date;
import java.util.SortedSet;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;

import net.geoprism.registry.etl.upload.ClassifierVertexCache;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

public class ServerGeoObjectEventBuilder extends AbstractGeoObjectEventBuilder<ServerGeoObjectIF>
{
  private ClassifierVertexCache cache;

  public ServerGeoObjectEventBuilder(GeoObjectBusinessServiceIF service)
  {
    this(service, null);
  }

  public ServerGeoObjectEventBuilder(GeoObjectBusinessServiceIF service, ClassifierVertexCache cache)
  {
    super(service);

    this.cache = cache;
  }

  @Override
  public String getCode()
  {
    return this.getOrThrow().getCode();
  }

  @Override
  public String getType()
  {
    return this.getOrThrow().getType().getCode();
  }

  @Override
  protected JsonObject toJSON()
  {
    return toDTO().toJSON();
  }

  protected GeoObjectOverTime toDTO()
  {
    return this.service.toGeoObjectOverTime(getOrThrow(), false, this.cache);
  }

  @Override
  protected void removeAllEdges(ServerHierarchyType hierarchyType)
  {
    // Delete the current edges and recreate the new ones
    final SortedSet<EdgeObject> edges = this.getOrThrow().getEdges(hierarchyType);

    for (EdgeObject edge : edges)
    {
      Date startDate = edge.getObjectValue(GeoVertex.START_DATE);
      Date endDate = edge.getObjectValue(GeoVertex.END_DATE);
      String uid = edge.getObjectValue(DefaultAttribute.UID.getName());

      this.addEvent(new GeoObjectRemoveParentEvent(getCode(), getType(), uid, hierarchyType.getCode(), startDate, endDate));
    }
  }

}
