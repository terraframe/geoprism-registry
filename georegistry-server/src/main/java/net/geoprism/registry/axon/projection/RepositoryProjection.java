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
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteConceptObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectSetExternalIdEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.RemoveBusinessObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveBusinessObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveConceptObjectEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEdgeEvent;
import net.geoprism.registry.axon.event.repository.RemoveGeoObjectEvent;
import net.geoprism.registry.cache.BusinessObjectCache;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.GeoObjectCache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.BaseGeoObjectType;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.EdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GPRBusinessTypeBusinessService;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;

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
  private GPRBusinessTypeBusinessService    bTypeService;

  @Autowired
  private BusinessObjectBusinessServiceIF   bObjectService;

  @Autowired
  private EdgeTypeBusinessServiceIF         graphTypeService;

  @Autowired
  private ConceptClassBusinessServiceIF     cClassService;

  @Autowired
  private ConceptObjectBusinessServiceIF    cObjectService;

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
  public void handleApplyGeoObject(GeoObjectApplyEvent event)
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
  public void handleRemoveParent(GeoObjectRemoveParentEvent event)
  {
    ServerHierarchyType hierarchyType = this.hService.get(event.getEdgeTypeCode());

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
  public void handleUpdateParent(GeoObjectUpdateParentEvent event)
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeTypeCode());

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
  public void handleCreateParent(GeoObjectCreateParentEvent event)
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeTypeCode());
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
  public void handleSetExternalId(GeoObjectSetExternalIdEvent event)
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());
    String systemId = event.getSystemId();

    ExternalSystem system = ExternalSystem.getByExternalSystemId(systemId);

    this.gObjectService.createExternalId(object, system, event.getExternalId(), event.getStrategy());
  }

  @EventHandler
  @Transaction
  public void handleGeoObjectApplyEdge(GeoObjectApplyEdgeEvent event)
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
      Object sourceRid = getOrFetchGeoObjectRid(event.getSourceCode(), event.getSourceType());
      Object targetRid = getOrFetchGeoObjectRid(event.getTargetCode(), event.getTargetType());

      if (ImportStrategy.NEW_AND_UPDATE.equals(event.getStrategy()) && ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
      {
        // The only existing UNIQUE indexes that exist with edges are by uid. So
        // we have to look this up if we want an 'update' mechanism.
        EdgeObject edge = findEdge(targetRid, sourceRid, graphType, null, null);

        if (edge != null)
        {
          edge.setValue(GeoVertex.START_DATE, event.getStartDate());
          edge.setValue(GeoVertex.END_DATE, event.getEndDate());
          edge.apply();
        }
        else if (ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
        {
          throw new DataNotFoundException("Could not find an edge from " + targetRid + " to " + sourceRid);
        }
        else
        {
          this.newEdge(sourceRid, targetRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
        }
      }
      else
      {
        this.newEdge(sourceRid, targetRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
      }

    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteGeoObject(RemoteGeoObjectEvent event)
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
  public void handleRemoteParent(RemoteGeoObjectSetParentEvent event)
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
  public void handleRemoteCreateEdge(RemoteGeoObjectCreateEdgeEvent event)
  {
    final GraphType graphType = this.graphTypeService.getByCode(event.getEdgeType(), event.getEdgeTypeCode());

    if (!GeoprismProperties.getOrigin().equals(graphType.getOrigin()))
    {
      Object sourceRid = getOrFetchGeoObjectRid(event.getSourceCode(), event.getSourceType());
      Object targetRid = getOrFetchGeoObjectRid(event.getTargetCode(), event.getTargetType());
      DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      // Ensure the edge doesn't already exist
      if (!this.gObjectService.exists(graphType, event.getEdgeUid()))
      {
        this.newEdge(targetRid, sourceRid, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, false);
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

  private void newEdge(Object sourceRid, Object targetRid, EdgeType type, Date startDate, Date endDate, String uid, DataSource dataSource, Boolean validateOrigin)
  {
    if (validateOrigin && !type.getOrigin().equals(GeoprismProperties.getOrigin()))
    {
      throw new OriginException();
    }

    String clazz = type.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("CREATE EDGE " + clazz + " FROM :sourceRid TO :targetRid");
    statement.append(" SET startDate=:startDate, endDate=:endDate, oid=:oid, uid=:uid");

    if (dataSource != null)
    {
      statement.append(", dataSource=:dataSource");
    }

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    parameters.put("sourceRid", sourceRid);
    parameters.put("targetRid", targetRid);
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
  public void handleApplyConceptObject(ConceptObjectApplyEvent event)
  {
    ConceptClass type = this.cClassService.getByCodeOrThrow(event.getType());

    ObjectOverTimeDTO dto = event.getObject();

    ConceptObject object = event.getIsNew() ? this.cObjectService.newInstance(type) : this.cObjectService.getByCode(type, event.getCode());

    this.cObjectService.populate(object, dto);

    this.cObjectService.apply(object);
  }

  @EventHandler
  @Transaction
  public void handleApplyBusinessObject(BusinessObjectApplyEvent event)
  {
    BusinessType type = this.bTypeService.getByCodeOrThrow(event.getType());

    ObjectOverTimeDTO dto = event.getObject();

    BusinessObject object = event.getIsNew() ? this.bObjectService.newInstance(type) : this.bObjectService.getByCode(type, event.getCode());

    this.bObjectService.populate(object, dto);

    this.bObjectService.apply(object);
  }

  @EventHandler
  @Transaction
  public void handleBusinessObjectApplyEdge(BusinessObjectApplyEdgeEvent event)
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeTypeCode());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    if (event.getValidate())
    {
      VertexComponent source = edgeType.getIsParentGeoObject() ? //
          goCache.getOrFetchByCode(event.getSourceCode(), event.getSourceType()) : //
          boCache.getOrFetchByCode(event.getSourceCode(), event.getSourceType());

      VertexComponent target = edgeType.getIsChildGeoObject() ? //
          goCache.getOrFetchByCode(event.getTargetCode(), event.getTargetType()) : //
          boCache.getOrFetchByCode(event.getTargetCode(), event.getTargetType());

      this.bObjectService.addChild(source, edgeType, target, event.getEdgeUid(), event.getStartDate(), event.getEndDate(), dataSource);
    }
    else
    {
      Object sourceRid = edgeType.getIsParentGeoObject() ? //
          getOrFetchGeoObjectRid(event.getSourceCode(), event.getSourceType()) : //
          getOrFetchBusinessRid(event.getSourceCode(), event.getSourceType());

      Object targetRid = edgeType.getIsChildGeoObject() ? //
          getOrFetchGeoObjectRid(event.getTargetCode(), event.getTargetType()) : //
          getOrFetchBusinessRid(event.getTargetCode(), event.getTargetType());

      this.newEdge(sourceRid, targetRid, edgeType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteBusinessObject(RemoteBusinessObjectEvent event)
  {
    BusinessType type = this.bTypeService.getByCodeOrThrow(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      ObjectAtTimeDTO dto = event.getObject();

      BusinessObject object = this.bObjectService.getByCode(type, event.getCode());

      if (object == null)
      {
        object = this.bObjectService.newInstance(type);
      }

      this.bObjectService.populate(object, dto, event.getStartDate(), event.getEndDate());

      this.bObjectService.apply(object, false);
    }
    else
    {
      logger.info("Skipping remote business object: [" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteConceptObject(RemoteConceptObjectEvent event)
  {
    ConceptClass type = this.cClassService.getByCodeOrThrow(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      ObjectAtTimeDTO dto = event.getObject();

      ConceptObject object = this.cObjectService.getByCode(type, event.getCode());

      if (object == null)
      {
        object = this.cObjectService.newInstance(type);
      }

      this.cObjectService.populate(object, dto, event.getStartDate(), event.getEndDate());

      this.cObjectService.apply(object, false);
    }
    else
    {
      logger.info("Skipping remote business object: [" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteBusinessObjectApplyEdge(RemoteBusinessObjectApplyEdgeEvent event)
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(edgeType.getOrigin()))
    {
      Object sourceRid = edgeType.getIsParentGeoObject() ? //
          getOrFetchGeoObjectRid(event.getSourceCode(), event.getSourceType()) : //
          getOrFetchBusinessRid(event.getSourceCode(), event.getSourceType());

      Object targetRid = edgeType.getIsChildGeoObject() ? //
          getOrFetchGeoObjectRid(event.getTargetCode(), event.getTargetType()) : //
          getOrFetchBusinessRid(event.getTargetCode(), event.getTargetType());

      DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      if (!this.bObjectService.exists(edgeType, event.getEdgeUid()))
      {
        this.newEdge(sourceRid, targetRid, edgeType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, false);
      }
    }
    else
    {
      logger.info("Skipping remote create edge: [" + event.getEdgeType() + "][" + event.getSourceType() + "][" + event.getSourceCode() + "]");
    }
  }

  private Object getOrFetchBusinessRid(String code, String businessTypeCode)
  {
    BusinessType businessType = this.bTypeService.getByCodeOrThrow(businessTypeCode);

    String typeDbClassName = businessType.getMdVertexDAO().getDBClassName();

    Optional<Object> optional = this.goRidCache.get(businessType.getCode() + "$#!" + code);

    return optional.orElseGet(() -> {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);

      Object rid = query.getSingleResult();

      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Business-Object with code " + code + " on table " + typeDbClassName);
      }

      this.goRidCache.put(businessType.getCode() + "$#!" + code, rid);

      return rid;
    });
  }

  public static EdgeObject findEdge(Object targetRid, Object sourceRid, GraphType type, Date startDate, Date endDate)
  {
    String clazz = type.getMdEdgeDAO().getDBClassName();

    String statement = "SELECT FROM " + clazz + " WHERE out = :sourceRid AND in = :targetRid";

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("sourceRid", sourceRid);
    parameters.put("targetRid", targetRid);

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString(), parameters);

    return query.getSingleResult();
  }

  public void handleRemoveGeoObjectEvent(RemoveGeoObjectEvent event)
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());

    if (object != null)
    {
      object.delete();
    }
  }

  public void handleRemoveBusinessObjectEvent(RemoveBusinessObjectEvent event)
  {
    BusinessType type = this.bTypeService.getByCodeOrThrow(event.getType());

    BusinessObject object = this.bObjectService.getByCode(type, event.getCode());

    if (object != null)
    {
      this.bObjectService.delete(object);
    }
  }

  public void handleRemoveConceptObjectEvent(RemoveConceptObjectEvent event)
  {
    ConceptClass type = this.cClassService.getByCodeOrThrow(event.getType());

    ConceptObject object = this.cObjectService.getByCode(type, event.getCode());

    if (object != null)
    {
      this.cObjectService.delete(object);
    }
  }

  public void handleRemoveGeoObjectEvent(RemoveGeoObjectEdgeEvent event)
  {
    GraphType graphType = this.graphTypeService.getByCode(event.getEdgeClassType(), event.getEdgeTypeCode());

    Map<String, Object> parameters = new HashMap<String, Object>();

    String clazz = graphType.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("DELETE EDGE " + clazz);

    if (!StringUtils.isEmpty(event.getSourceCode()))
    {
      VertexServerGeoObject object = (VertexServerGeoObject) this.gObjectService.getGeoObjectByCode(event.getSourceCode(), event.getSourceType());
      parameters.put("parentRid", object.getVertex().getRID());

      statement.append(" FROM :parentRid");
    }

    if (!StringUtils.isEmpty(event.getTargetCode()))
    {
      VertexServerGeoObject object = (VertexServerGeoObject) this.gObjectService.getGeoObjectByCode(event.getTargetCode(), event.getTagetType());
      parameters.put("childRid", object.getVertex().getRID());

      statement.append(" TO :childRid");
    }

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    service.command(request, statement.toString(), parameters);
  }

  public void handleRemoveBusinessObjectEvent(RemoveBusinessObjectEdgeEvent event)
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeTypeCode());
    ObjectClass parentType = this.edgeService.getParent(edgeType);
    ObjectClass childType = this.edgeService.getChild(edgeType);

    String clazz = edgeType.getMdEdgeDAO().getDBClassName();

    Object sourceRid = ( parentType instanceof BaseGeoObjectType ) ? //
        this.gObjectService.getGeoObjectByCode(event.getSourceCode(), event.getSourceType()).getVertex().getRID() : //
        this.bObjectService.getByCode(this.bTypeService.getByCodeOrThrow(event.getSourceType()), event.getSourceCode()).getVertex().getRID();

    Object targetRid = ( childType instanceof BaseGeoObjectType ) ? //
        this.gObjectService.getGeoObjectByCode(event.getTargetCode(), event.getTargetType()).getVertex().getRID() : //
        this.bObjectService.getByCode(this.bTypeService.getByCodeOrThrow(event.getTargetType()), event.getTargetCode()).getVertex().getRID();

    StringBuilder statement = new StringBuilder();
    statement.append("DELETE EDGE " + clazz);
    statement.append(" FROM :sourceRid");
    statement.append(" TO :targetRid");
    statement.append(" WHERE :startDate = startDate");
    statement.append(" AND :endDate = endDate");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("sourceRid", sourceRid);
    parameters.put("targetRid", targetRid);
    parameters.put("startDate", event.getStartDate());
    parameters.put("endDate", event.getEndDate());

    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();

    service.command(request, statement.toString(), parameters);
  }

  public void clearCache()
  {
    this.boCache.clear();
    this.goCache.clear();
    this.goRidCache.clear();
  }

}