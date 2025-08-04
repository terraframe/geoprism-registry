package net.geoprism.registry.axon.projection;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.ListType;
import net.geoprism.registry.OriginException;
import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectSetExternalIdEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.GeoObjectCache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

@Service
public class GeoObjectRepositoryProjection
{
  @Autowired
  private HierarchyTypeBusinessServiceIF hService;

  @Autowired
  private GPRGeoObjectBusinessServiceIF  service;

  private GeoObjectCache                 goCache;

  private Cache<String, Object>          goRidCache;

  public GeoObjectRepositoryProjection()
  {
    this.goCache = new GeoObjectCache();
    this.goRidCache = new LRUCache<String, Object>(1000);
  }

  @EventHandler
  @Transaction
  public void applyGeoObject(GeoObjectApplyEvent event) throws Exception
  {
    GeoObjectOverTime dto = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    ServerGeoObjectIF object = this.service.apply(dto, event.getIsNew(), event.getIsImport(), true);

    final ServerGeoObjectType type = object.getType();

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object, type);
    }
  }

  @EventHandler
  @Transaction
  public void removeParent(GeoObjectRemoveParentEvent event) throws Exception
  {
    ServerHierarchyType hierarchyType = this.hService.get(event.getEdgeType());

    ServerGeoObjectIF object = this.service.getGeoObjectByCode(event.getCode(), event.getType());
    EdgeObject edge = object.getEdge(hierarchyType, event.getEdgeUid());

    if (edge == null)
    {
      throw new ExecuteOutOfDateChangeRequestException();
    }

    edge.delete();

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void updateParent(GeoObjectUpdateParentEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.service.getGeoObject(event.getCode(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeType());

    EdgeObject edge = object.getEdge(hierarchy, event.getEdgeUid());

    if (edge == null)
    {
      throw new ExecuteOutOfDateChangeRequestException();
    }

    if (event.getParentType() != null && event.getParentCode() != null)
    {
      VertexServerGeoObject newParent = (VertexServerGeoObject) this.service.getGeoObjectByCode(event.getParentCode(), event.getParentType());

      // Parent values can only be changed by deleting the current edge and
      // creating a new one unfortunately
      if (!edge.getParent().getOid().equals(newParent.getRunwayId()))
      {
        Date _newStartDate = event.getStartDate();
        Date _newEndDate = event.getEndDate();

        if (_newStartDate == null)
        {
          _newStartDate = edge.getObjectValue(GeoVertex.START_DATE);
        }

        if (_newEndDate == null)
        {
          _newEndDate = edge.getObjectValue(GeoVertex.END_DATE);
        }

        edge.delete();

        // We unfortunately can't use this method because we have to bypass
        // the votc reordering and validation
        // go.addParent(newParent, hierarchyType, _newStartDate,
        // _newEndDate);

        EdgeObject newEdge = object.getVertex().addParent( ( (VertexComponent) newParent ).getVertex(), hierarchy.getObjectEdge());
        newEdge.setValue(GeoVertex.START_DATE, _newStartDate);
        newEdge.setValue(GeoVertex.END_DATE, _newEndDate);
        newEdge.setValue(DefaultAttribute.UID.getName(), event.getEdgeUid());
        newEdge.apply();
      }
    }
    else
    {
      if (event.getStartDate() != null)
      {
        edge.setValue(GeoVertex.START_DATE, event.getStartDate());
      }

      if (event.getEndDate() != null)
      {
        edge.setValue(GeoVertex.END_DATE, event.getEndDate());
      }

      edge.apply();
    }

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void createParent(GeoObjectCreateParentEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.service.getGeoObjectByCode(event.getCode(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeType());
    VertexServerGeoObject newParent = (VertexServerGeoObject) this.service.getGeoObjectByCode(event.getParentCode(), event.getParentType());

    if (event.getValidate())
    {
      this.service.addParent(object, newParent, hierarchy, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), true);
    }
    else
    {
      MdEdgeDAOIF mdEdge = hierarchy.getMdEdgeDAO();

      this.service.addParentRaw(object, newParent.getVertex(), mdEdge, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), true);
    }

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void setExternalId(GeoObjectSetExternalIdEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.service.getGeoObjectByCode(event.getCode(), event.getType());
    String systemId = event.getSystemId();

    ExternalSystem system = ExternalSystem.getByExternalSystemId(systemId);

    this.service.createExternalId(object, system, event.getExternalId(), event.getStrategy());
  }

  @EventHandler
  @Transaction
  public void createEdge(GeoObjectCreateEdgeEvent event) throws Exception
  {
    final GraphType graphType = GraphType.getByCode(event.getEdgeType(), event.getEdgeTypeCode());

    if (event.getValidate())
    {
      ServerGeoObjectIF source = goCache.getOrFetchByCode(event.getSourceCode(), event.getSourceType());
      ServerGeoObjectIF target = goCache.getOrFetchByCode(event.getTargetCode(), event.getTargetType());

      source.addGraphChild(target, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), event.getValidate());
    }
    else
    {
      Object childRid = getOrFetchRid(event.getSourceCode(), event.getSourceType());
      Object parentRid = getOrFetchRid(event.getTargetCode(), event.getTargetType());

      this.newEdge(childRid, parentRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), true);
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteGeoObject(RemoteGeoObjectEvent event) throws Exception
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      GeoObject dto = GeoObject.fromJSON(ServiceFactory.getAdapter(), event.getObject());

      ServerGeoObjectIF object = this.service.getGeoObjectByCode(event.getCode(), event.getType(), false);

      if (object == null)
      {
        object = this.service.newInstance(type);
      }

      this.service.populate(object, dto, event.getStartDate(), event.getEndDate());
      this.service.apply(object, false, false);
    }
    else
    {
      System.out.println("Skipping remote geo object: [" + event.getType() + "][" + event.getCode() + "] - [" + event.getIsNew() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteParent(RemoteGeoObjectSetParentEvent event) throws Exception
  {
    ServerHierarchyType hierarchyType = this.hService.get(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(hierarchyType.getOrigin()))
    {
      ServerGeoObjectIF object = this.service.getGeoObjectByCode(event.getCode(), event.getType());

      String edgeUid = event.getEdgeUid();

      EdgeObject edge = object.getEdge(hierarchyType, edgeUid);

      if (edge != null)
      {
        edge.delete();
      }

      if (!StringUtils.isBlank(event.getParentCode()) && !StringUtils.isBlank(event.getParentType()))
      {
        ServerGeoObjectIF parent = this.service.getGeoObjectByCode(event.getParentCode(), event.getParentType());

        this.service.addParent(object, parent, hierarchyType, event.getStartDate(), event.getEndDate(), edgeUid, false);
      }
    }
    else
    {
      System.out.println("Skipping remote set parent: [" + event.getEdgeType() + "][" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void createRemoteCreateEdge(RemoteGeoObjectCreateEdgeEvent event) throws Exception
  {
    final GraphType graphType = GraphType.getByCode(event.getEdgeType(), event.getEdgeTypeCode());

    if (!GeoprismProperties.getOrigin().equals(graphType.getOrigin()))
    {
      Object childRid = getOrFetchRid(event.getSourceCode(), event.getSourceType());
      Object parentRid = getOrFetchRid(event.getTargetCode(), event.getTargetType());

      this.newEdge(childRid, parentRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), false);
    }
    else
    {
      System.out.println("Skipping remote create edge: [" + event.getEdgeType() + "][" + event.getSourceType() + "][" + event.getSourceCode() + "]");
    }
  }

  protected void updateWorkingLists(ServerGeoObjectIF object)
  {
    this.updateWorkingLists(object, object.getType());
  }

  protected void updateWorkingLists(ServerGeoObjectIF object, final ServerGeoObjectType type)
  {
    // Update all of the working lists which have this record
    ListType.getForType(type).forEach(listType -> {
      listType.getWorkingVersions().forEach(version -> version.publishOrUpdateRecord(object));
    });
  }

  private Object getOrFetchRid(String code, String typeCode)
  {
    String typeDbClassName = ServerGeoObjectType.get(typeCode).getDBClassName();

    Optional<Object> optional = this.goRidCache.get(typeCode + "$#!" + code);

    return optional.orElseGet(() -> {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);

      Object rid = query.getSingleResult();

      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Geo-Object with code " + code + " on table " + typeDbClassName);
      }

      this.goRidCache.put(typeCode + "$#!" + code, rid);

      return rid;
    });
  }

  public void newEdge(Object childRid, Object parentRid, GraphType type, Date startDate, Date endDate, String uid, Boolean validateOrigin)
  {
    if (validateOrigin && !type.getOrigin().equals(GeoprismProperties.getOrigin()))
    {
      throw new OriginException();
    }

    String clazz = type.getMdEdgeDAO().getDBClassName();

    String statement = "CREATE EDGE " + clazz + " FROM :childRid TO :parentRid";
    statement += " SET startDate=:startDate, endDate=:endDate, oid=:oid, uid=:uid";

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    parameters.put("childRid", childRid);
    parameters.put("parentRid", parentRid);
    parameters.put("startDate", startDate);
    parameters.put("endDate", endDate);
    parameters.put("uid", uid);

    service.command(request, statement, parameters);
  }

  public void clearCache()
  {
    this.goCache.clear();
    this.goRidCache.clear();
  }

}
