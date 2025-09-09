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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.ListType;
import net.geoprism.registry.OriginException;
import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectSetExternalIdEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.cache.BusinessObjectCache;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.GeoObjectCache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.GPRBusinessTypeBusinessService;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GraphTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

@Service
public class RepositoryProjection
{
  private static Logger                     logger = LoggerFactory.getLogger(RepositoryProjection.class);

  @Autowired
  private HierarchyTypeBusinessServiceIF    hService;

  @Autowired
  private GPRGeoObjectBusinessServiceIF     gObjectService;

  @Autowired
  private DataSourceBusinessServiceIF       sourceService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF edgeService;

  @Autowired
  private GPRBusinessTypeBusinessService    typeService;

  @Autowired
  private BusinessObjectBusinessServiceIF   bObjectService;

  @Autowired
  private GraphTypeBusinessServiceIF        graphTypeService;

  private final GeoObjectCache              goCache;

  private final BusinessObjectCache         boCache;

  private final Cache<String, Object>       goRidCache;

  public RepositoryProjection()
  {
    this.boCache = new BusinessObjectCache();
    this.goCache = new GeoObjectCache();
    this.goRidCache = new LRUCache<String, Object>(1000);
  }

  @EventHandler
  @Transaction
  public void handleApplyGeoObject(GeoObjectApplyEvent event) throws Exception
  {
    GeoObjectOverTime dto = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    ServerGeoObjectType type = ServerGeoObjectType.get(dto.getType().getCode());

    ServerGeoObjectIF object = this.gObjectService.fromDTO(type, dto, event.getIsNew());

    this.gObjectService.apply(object, event.getIsImport(), true);

    // ServerGeoObjectIF object = this.gObjectService.apply(dto,
    // event.getIsNew(), event.getIsImport(), true);
    //
    // final ServerGeoObjectType type = object.getType();

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object, type);
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoveParent(GeoObjectRemoveParentEvent event) throws Exception
  {
    ServerHierarchyType hierarchyType = this.hService.get(event.getEdgeType());

    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
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
  public void handleUpdateParent(GeoObjectUpdateParentEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeType());

    EdgeObject edge = object.getEdge(hierarchy, event.getEdgeUid());

    if (edge == null)
    {
      throw new ExecuteOutOfDateChangeRequestException();
    }

    if (event.getParentType() != null && event.getParentCode() != null)
    {
      VertexServerGeoObject newParent = (VertexServerGeoObject) this.gObjectService.getGeoObjectByCode(event.getParentCode(), event.getParentType());

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
        newEdge.setValue(DefaultAttribute.DATA_SOURCE.getName(), this.sourceService.getByCode(event.getDataSource()).orElse(null));
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

      if (!StringUtils.isBlank(event.getDataSource()))
      {
        edge.setValue(DefaultAttribute.DATA_SOURCE.getName(), this.sourceService.getByCode(event.getDataSource()).orElse(null));
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
  public void handleCreateParent(GeoObjectCreateParentEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeType());
    VertexServerGeoObject newParent = (VertexServerGeoObject) this.gObjectService.getGeoObjectByCode(event.getParentCode(), event.getParentType());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    if (event.getValidate())
    {
      this.gObjectService.addParent(object, newParent, hierarchy, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
    }
    else
    {
      MdEdgeDAOIF mdEdge = hierarchy.getMdEdgeDAO();

      this.gObjectService.addParentRaw(object, newParent.getVertex(), mdEdge, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
    }

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void handleSetExternalId(GeoObjectSetExternalIdEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
    String systemId = event.getSystemId();

    ExternalSystem system = ExternalSystem.getByExternalSystemId(systemId);

    this.gObjectService.createExternalId(object, system, event.getExternalId(), event.getStrategy());
  }

  @EventHandler
  @Transaction
  public void handleCreateEdge(GeoObjectApplyEdgeEvent event) throws Exception
  {
    final GraphType graphType = this.graphTypeService.getByCode(event.getEdgeType(), event.getEdgeTypeCode());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    if (event.getValidate())
    {
      ServerGeoObjectIF source = goCache.getOrFetchByCode(event.getSourceCode(), event.getSourceType());
      ServerGeoObjectIF target = goCache.getOrFetchByCode(event.getTargetCode(), event.getTargetType());

      if (ImportStrategy.NEW_AND_UPDATE.equals(event.getStrategy()) && ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
      {
        // The only existing UNIQUE indexes that exist with edges are by uid. So
        // we have to look this up if we want an 'update' mechanism.
        EdgeObject edge = source.getEdge(target, graphType, null, null);

        if (edge != null)
        {
          edge.setValue(GeoVertex.START_DATE, event.getStartDate());
          edge.setValue(GeoVertex.END_DATE, event.getEndDate());
          edge.apply();
        }
        else if (ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
        {
          throw new DataNotFoundException("Could not find an edge from " + event.getSourceCode() + " to " + event.getTargetCode());
        }
        else
        {
          source.addGraphChild(target, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, event.getValidate());
        }
      }
      else
      {
        source.addGraphChild(target, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, event.getValidate());
      }
    }
    else
    {
      Object childRid = getOrFetchGeoObjectRid(event.getSourceCode(), event.getSourceType());
      Object parentRid = getOrFetchGeoObjectRid(event.getTargetCode(), event.getTargetType());

      if (ImportStrategy.NEW_AND_UPDATE.equals(event.getStrategy()) && ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
      {
        // The only existing UNIQUE indexes that exist with edges are by uid. So
        // we have to look this up if we want an 'update' mechanism.
        EdgeObject edge = findEdge(childRid, parentRid, graphType, null, null);

        if (edge != null)
        {
          edge.setValue(GeoVertex.START_DATE, event.getStartDate());
          edge.setValue(GeoVertex.END_DATE, event.getEndDate());
          edge.apply();
        }
        else if (ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
        {
          throw new DataNotFoundException("Could not find an edge from " + childRid + " to " + parentRid);
        }
        else
        {
          this.newEdge(childRid, parentRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
        }
      }
      else
      {
        this.newEdge(childRid, parentRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
      }

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

      ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType(), false);

      if (object == null)
      {
        object = this.gObjectService.newInstance(type);
      }

      this.gObjectService.populate(object, dto, event.getStartDate(), event.getEndDate());
      this.gObjectService.apply(object, false, false);
    }
    else
    {
      logger.info("Skipping remote geo object: [" + event.getType() + "][" + event.getCode() + "] - [" + event.getIsNew() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteParent(RemoteGeoObjectSetParentEvent event) throws Exception
  {
    ServerHierarchyType hierarchyType = this.hService.get(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(hierarchyType.getOrigin()))
    {
      ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());

      String edgeUid = event.getEdgeUid();

      EdgeObject edge = object.getEdge(hierarchyType, edgeUid);

      if (edge != null)
      {
        edge.delete();
      }

      if (!StringUtils.isBlank(event.getParentCode()) && !StringUtils.isBlank(event.getParentType()))
      {
        ServerGeoObjectIF parent = this.gObjectService.getGeoObjectByCode(event.getParentCode(), event.getParentType());
        DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

        this.gObjectService.addParent(object, parent, hierarchyType, event.getStartDate(), event.getEndDate(), edgeUid, dataSource, false);
      }
    }
    else
    {
      logger.info("Skipping remote set parent: [" + event.getEdgeType() + "][" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteCreateEdge(RemoteGeoObjectCreateEdgeEvent event) throws Exception
  {
    final GraphType graphType = this.graphTypeService.getByCode(event.getEdgeType(), event.getEdgeTypeCode());

    if (!GeoprismProperties.getOrigin().equals(graphType.getOrigin()))
    {
      Object childRid = getOrFetchGeoObjectRid(event.getSourceCode(), event.getSourceType());
      Object parentRid = getOrFetchGeoObjectRid(event.getTargetCode(), event.getTargetType());
      DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      // Ensure the edge doesn't already exist
      if (!this.gObjectService.exists(graphType, event.getEdgeUid()))
      {
        this.newEdge(childRid, parentRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, false);
      }
    }
    else
    {
      logger.info("Skipping remote create edge: [" + event.getEdgeType() + "][" + event.getSourceType() + "][" + event.getSourceCode() + "]");
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

  private Object getOrFetchGeoObjectRid(String code, String typeCode)
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

  private void newEdge(Object childRid, Object parentRid, GraphType type, Date startDate, Date endDate, String uid, DataSource dataSource, Boolean validateOrigin)
  {
    if (validateOrigin && !type.getOrigin().equals(GeoprismProperties.getOrigin()))
    {
      throw new OriginException();
    }

    String clazz = type.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("CREATE EDGE " + clazz + " FROM :childRid TO :parentRid");
    statement.append(" SET startDate=:startDate, endDate=:endDate, oid=:oid, uid=:uid");

    if (dataSource != null)
    {
      statement.append(", dataSource=:dataSource");
    }

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    parameters.put("childRid", childRid);
    parameters.put("parentRid", parentRid);
    parameters.put("startDate", startDate);
    parameters.put("endDate", endDate);
    parameters.put("uid", uid);

    if (dataSource != null)
    {
      parameters.put("dataSource", dataSource.getRID());
    }

    service.command(request, statement.toString(), parameters);
  }

  @EventHandler
  @Transaction
  public void handleApplyBusinessObject(BusinessObjectApplyEvent event) throws Exception
  {
    BusinessType type = this.typeService.getByCode(event.getType());

    JsonObject json = JsonParser.parseString(event.getObject()).getAsJsonObject();

    BusinessObject object = event.getIsNew() ? this.bObjectService.newInstance(type) : this.bObjectService.getByCode(type, event.getCode());

    this.bObjectService.populate(object, json);

    this.bObjectService.apply(object);
  }

  @EventHandler
  @Transaction
  public void handleAddGeoObjectEdge(BusinessObjectAddGeoObjectEvent event) throws Exception
  {
    BusinessType type = this.typeService.getByCode(event.getType());
    BusinessObject object = this.bObjectService.getByCode(type, event.getCode());
    ServerGeoObjectIF geoObject = this.gObjectService.getGeoObjectByCode(event.getGeoObjectCode(), event.getGeoObjectType());

    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    DataSource source = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    this.bObjectService.addGeoObject(object, edgeType, geoObject, event.getDirection(), event.getEdgeUid(), source, true);
  }

  @EventHandler
  @Transaction
  public void handleCreateEdge(BusinessObjectCreateEdgeEvent event) throws Exception
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    if (event.getValidate())
    {
      BusinessObject source = boCache.getOrFetchByCode(event.getSourceCode(), event.getSourceType());
      BusinessObject target = boCache.getOrFetchByCode(event.getTargetCode(), event.getTargetType());

      this.bObjectService.addChild(source, edgeType, target, event.getEdgeUid(), dataSource);
    }
    else
    {
      Object parentRid = getOrFetchBusinessRid(event.getSourceCode(), event.getSourceType());
      Object childRid = getOrFetchBusinessRid(event.getTargetCode(), event.getTargetType());

      this.newEdge(childRid, parentRid, event.getEdgeUid(), edgeType, dataSource, true);
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteBusinessObject(RemoteBusinessObjectEvent event) throws Exception
  {
    System.out.println("Repository Projection - Handling remote geo object");

    BusinessType type = this.typeService.getByCode(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      JsonObject json = JsonParser.parseString(event.getObject()).getAsJsonObject();

      BusinessObject object = this.bObjectService.getByCode(type, event.getCode());

      if (object == null)
      {
        object = this.bObjectService.newInstance(type);
      }

      this.bObjectService.populate(object, json);

      this.bObjectService.apply(object, false);
    }
    else
    {
      logger.info("Skipping remote business object: [" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteAddGeoObjectEdge(RemoteBusinessObjectAddGeoObjectEvent event) throws Exception
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(edgeType.getOrigin()))
    {
      BusinessType type = this.typeService.getByCode(event.getType());
      BusinessObject object = this.bObjectService.getByCode(type, event.getCode());
      ServerGeoObjectIF geoObject = this.gObjectService.getGeoObjectByCode(event.getGeoObjectCode(), event.getGeoObjectType());

      DataSource source = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      this.bObjectService.addGeoObject(object, edgeType, geoObject, event.getDirection(), event.getEdgeUid(), source, false);
    }
    else
    {
      logger.info("Skipping remote add geo object: [" + event.getEdgeType() + "][" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteCreateEdge(RemoteBusinessObjectCreateEdgeEvent event) throws Exception
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(edgeType.getOrigin()))
    {
      Object parentRid = getOrFetchBusinessRid(event.getSourceCode(), event.getSourceType());
      Object childRid = getOrFetchBusinessRid(event.getTargetCode(), event.getTargetType());
      DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      if (!this.bObjectService.exists(edgeType, event.getEdgeUid()))
      {
        this.newEdge(childRid, parentRid, event.getEdgeUid(), edgeType, dataSource, false);
      }
    }
    else
    {
      logger.info("Skipping remote create edge: [" + event.getEdgeType() + "][" + event.getSourceType() + "][" + event.getSourceCode() + "]");
    }
  }

  private Object getOrFetchBusinessRid(String code, String businessTypeCode)
  {
    BusinessType businessType = this.typeService.getByCode(businessTypeCode);

    String typeDbClassName = businessType.getMdVertexDAO().getDBClassName();

    Optional<Object> optional = this.goRidCache.get(businessType.getCode() + "$#!" + code);

    return optional.orElseGet(() -> {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);

      Object rid = query.getSingleResult();

      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Geo-Object with code " + code + " on table " + typeDbClassName);
      }

      this.goRidCache.put(businessType.getCode() + "$#!" + code, rid);

      return rid;
    });
  }

  private void newEdge(Object childRid, Object parentRid, String uid, BusinessEdgeType type, DataSource dataSource, boolean validateOrigin)
  {
    if (validateOrigin && !type.getOrigin().equals(GeoprismProperties.getOrigin()))
    {
      throw new OriginException();
    }

    String clazz = type.getMdEdgeDAO().getDBClassName();

    String statement = "CREATE EDGE " + clazz + " FROM :parentRid TO :childRid";
    statement += " SET oid=:oid, uid=:uid, dataSource=:dataSource";

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    parameters.put("uid", uid);
    parameters.put("parentRid", parentRid);
    parameters.put("childRid", childRid);
    parameters.put("dataSource", dataSource.getRID());

    service.command(request, statement, parameters);
  }

  public static EdgeObject findEdge(Object childRid, Object parentRid, GraphType type, Date startDate, Date endDate)
  {
    String clazz = type.getMdEdgeDAO().getDBClassName();

    String statement = "SELECT FROM " + clazz + " WHERE out = :childRid AND in = :parentRid";

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("childRid", childRid);
    parameters.put("parentRid", parentRid);

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    return query.getSingleResult();
  }

  public void clearCache()
  {
    this.boCache.clear();
    this.goCache.clear();
    this.goRidCache.clear();
  }

}