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
import com.runwaysdk.dataaccess.RelationshipDAO;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.dataaccess.transaction.TransactionState;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.ListType;
import net.geoprism.registry.OriginException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteConceptObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectApplyExternalIdEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectRemoveExternalIdEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.ConceptObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyExternalIdEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveExternalIdEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.ImportHistoryEvent;
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
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;
import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;

@Service
public class RepositoryProjection
{
  public static final String                GEO_CACHE      = "geo-cache";

  public static final String                BUSINESS_CACHE = "business-cache";

  public static final String                RID_CACHE      = "rid-cache";

  private static Logger                     logger         = LoggerFactory.getLogger(RepositoryProjection.class);

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

  @Autowired
  private SourceAuthorityBusinessServiceIF  authorityService;

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

    if (event.getIsImport())
    {
      String geometryId = object.getValue(DefaultAttribute.GEOMETRY.getName(), event.getStartDate());

      if (!StringUtils.isBlank(geometryId) && !StringUtils.isBlank(event.getHistoryId()))
      {
        RelationshipDAO.newInstance(event.getHistoryId(), geometryId, RegistryConstants.JOB_HISTORY_GEOMETRY).apply();
      }
    }

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
          _newStartDate = edge.getObjectValue(EdgeType.START_DATE);
        }

        if (_newEndDate == null)
        {
          _newEndDate = edge.getObjectValue(EdgeType.END_DATE);
        }

        edge.delete();

        // We unfortunately can't use this method because we have to bypass
        // the votc reordering and validation
        // go.addParent(newParent, hierarchyType, _newStartDate,
        // _newEndDate);

        EdgeObject newEdge = object.getVertex().addParent( ( (VertexComponent) newParent ).getVertex(), hierarchy.getObjectEdge());
        newEdge.setValue(EdgeType.START_DATE, _newStartDate);
        newEdge.setValue(EdgeType.END_DATE, _newEndDate);
        newEdge.setValue(DefaultAttribute.UID.getName(), event.getEdgeUid());
        newEdge.setValue(DefaultAttribute.DATA_SOURCE.getName(), this.sourceService.getByCode(event.getDataSource()).orElse(null));
        newEdge.apply();
      }
    }
    else
    {
      if (event.getStartDate() != null)
      {
        edge.setValue(EdgeType.START_DATE, event.getStartDate());
      }

      if (event.getEndDate() != null)
      {
        edge.setValue(EdgeType.END_DATE, event.getEndDate());
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
  public void handleApplyExternalId(GeoObjectApplyExternalIdEvent event)
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());

    this.authorityService.getByCode(event.getAuthority()).ifPresent(authority -> {
      this.gObjectService.applyExternalId(object, event.getAuthority(), event.getExternalId(), event.getStrategy(), true);
    });
  }

  @EventHandler
  @Transaction
  public void handleRemoveExternalId(GeoObjectRemoveExternalIdEvent event)
  {
    ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());

    this.authorityService.getByCode(event.getAuthority()).ifPresent(authority -> {
      this.gObjectService.removeExternalId(object, event.getAuthority(), true);
    });
  }

  @EventHandler
  @Transaction
  public void handleGeoObjectApplyEdge(GeoObjectApplyEdgeEvent event)
  {
    final GraphType graphType = this.graphTypeService.getByCode(event.getEdgeType(), event.getEdgeTypeCode());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    ServerGeoObjectIF source = this.getGeoObjectCache().getOrFetchByCode(event.getSourceCode(), event.getSourceType());
    ServerGeoObjectIF target = this.getGeoObjectCache().getOrFetchByCode(event.getTargetCode(), event.getTargetType());

    if (event.getValidate())
    {

      if (ImportStrategy.NEW_AND_UPDATE.equals(event.getStrategy()) && ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
      {
        // The only existing UNIQUE indexes that exist with edges are by uid. So
        // we have to look this up if we want an 'update' mechanism.
        EdgeObject edge = source.getEdge(target, graphType, null, null);

        if (edge != null)
        {
          edge.setValue(EdgeType.START_DATE, event.getStartDate());
          edge.setValue(EdgeType.END_DATE, event.getEndDate());
          edge.apply();
        }
        else if (ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
        {
          throw new DataNotFoundException("Could not find an edge from " + event.getSourceCode() + " to " + event.getTargetCode());
        }
        else
        {
          if (graphType instanceof ServerHierarchyType)
          {
            // For hierarchies the edge points to the parent from the child
            this.gObjectService.addParent(target, source, (ServerHierarchyType) graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, event.getValidate());
          }
          else
          {
            source.addGraphChild(target, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, event.getValidate());
          }
        }
      }
      else
      {
        if (graphType instanceof ServerHierarchyType)
        {
          // For hierarchies the edge points to the parent from the child
          this.gObjectService.addParent(target, source, (ServerHierarchyType) graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, event.getValidate());
        }
        else
        {
          source.addGraphChild(target, graphType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, event.getValidate());
        }
      }
    }
    else
    {
      Object targetRid = target.getVertex().getRID();
      Object sourceRid = source.getVertex().getRID();

      if (ImportStrategy.NEW_AND_UPDATE.equals(event.getStrategy()) && ImportStrategy.UPDATE_ONLY.equals(event.getStrategy()))
      {
        // The only existing UNIQUE indexes that exist with edges are by uid. So
        // we have to look this up if we want an 'update' mechanism.
        EdgeObject edge = findEdge(targetRid, sourceRid, graphType, null, null);

        if (edge != null)
        {
          edge.setValue(EdgeType.START_DATE, event.getStartDate());
          edge.setValue(EdgeType.END_DATE, event.getEndDate());
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

    if (StringUtils.isNotBlank(event.getHistoryId()))
    {
      createImportHistoryRelationship(event, source);
      createImportHistoryRelationship(event, target);
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
  public void handleRemoteGeoObjectApplyExternalIdEvent(RemoteGeoObjectApplyExternalIdEvent event)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());

      this.authorityService.getByCode(event.getAuthority()).ifPresent(authority -> {
        this.gObjectService.applyExternalId(object, event.getAuthority(), event.getExternalId(), ImportStrategy.NEW_AND_UPDATE, false);
      });
    }
    else
    {
      logger.info("Skipping remote create external ids: [" + event.getType() + "][" + event.getCode() + "][" + event.getAuthority() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteGeoObjectRemoveExternalIdEvent(RemoteGeoObjectRemoveExternalIdEvent event)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      ServerGeoObjectIF object = this.gObjectService.getGeoObjectByCode(event.getCode(), event.getType());

      this.authorityService.getByCode(event.getAuthority()).ifPresent(authority -> {
        this.gObjectService.removeExternalId(object, event.getAuthority(), false);
      });
    }
    else
    {
      logger.info("Skipping remote create external ids: [" + event.getType() + "][" + event.getCode() + "][" + event.getAuthority() + "]");
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

    Optional<Object> optional = this.getRidCache().get(typeCode + "$#!" + code);

    return optional.orElseGet(() -> {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);

      Object rid = query.getSingleResult();

      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Geo-Object with code " + code + " on table " + typeDbClassName);
      }

      this.getRidCache().put(typeCode + "$#!" + code, rid);

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

    ConceptObject object = event.getIsNew() ? this.cObjectService.newInstance(type) : this.cObjectService.getByCode(type, event.getCode()).orElseThrow();

    this.cObjectService.populate(object, dto);

    this.cObjectService.apply(object);
  }

  @EventHandler
  @Transaction
  public void handleApplyBusinessObject(BusinessObjectApplyEvent event)
  {
    BusinessType type = this.bTypeService.getByCodeOrThrow(event.getType());

    ObjectOverTimeDTO dto = event.getObject();

    BusinessObject object = event.getIsNew() ? this.bObjectService.newInstance(type) : this.bObjectService.getByCode(type, event.getCode()).orElseThrow();

    this.bObjectService.populate(object, dto);

    this.bObjectService.apply(object);
  }

  @EventHandler
  @Transaction
  public void handleBusinessObjectApplyEdge(BusinessObjectApplyEdgeEvent event)
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeTypeCode());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    VertexComponent source = edgeType.getIsParentGeoObject() ? //
        this.getGeoObjectCache().getOrFetchByCode(event.getSourceCode(), event.getSourceType()) : //
        this.getBusinessObjectCache().getOrFetchByCode(event.getSourceCode(), event.getSourceType());

    VertexComponent target = edgeType.getIsChildGeoObject() ? //
        this.getGeoObjectCache().getOrFetchByCode(event.getTargetCode(), event.getTargetType()) : //
        this.getBusinessObjectCache().getOrFetchByCode(event.getTargetCode(), event.getTargetType());

    if (event.getValidate())
    {
      this.bObjectService.addChild(source, edgeType, target, event.getEdgeUid(), event.getStartDate(), event.getEndDate(), dataSource);
    }
    else
    {
      Object sourceRid = source.getVertex().getRID();
      Object targetRid = target.getVertex().getRID();

      this.newEdge(sourceRid, targetRid, edgeType, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), dataSource, true);
    }

    if (StringUtils.isNotBlank(event.getHistoryId()))
    {
      if (edgeType.getIsParentGeoObject())
      {
        createImportHistoryRelationship(event, (ServerGeoObjectIF) source);
      }

      if (edgeType.getIsChildGeoObject())
      {
        createImportHistoryRelationship(event, (ServerGeoObjectIF) target);
      }
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

      BusinessObject object = this.bObjectService.getByCode(type, event.getCode()).orElse(null);

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

      ConceptObject object = this.cObjectService.getByCode(type, event.getCode()).orElse(null);

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

    Optional<Object> optional = this.getRidCache().get(businessType.getCode() + "$#!" + code);

    return optional.orElseGet(() -> {
      GraphQuery<Object> query = new GraphQuery<Object>("select @rid from " + typeDbClassName + " where code=:code;");
      query.setParameter("code", code);

      Object rid = query.getSingleResult();

      if (rid == null)
      {
        throw new DataNotFoundException("Could not find Business-Object with code " + code + " on table " + typeDbClassName);
      }

      this.getRidCache().put(businessType.getCode() + "$#!" + code, rid);

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
      this.gObjectService.delete(object);
    }
  }

  public void handleRemoveBusinessObjectEvent(RemoveBusinessObjectEvent event)
  {
    BusinessType type = this.bTypeService.getByCodeOrThrow(event.getType());

    this.bObjectService.getByCode(type, event.getCode()).ifPresent(object -> {
      this.bObjectService.delete(object);
    });
  }

  public void handleRemoveConceptObjectEvent(RemoveConceptObjectEvent event)
  {
    ConceptClass type = this.cClassService.getByCodeOrThrow(event.getType());

    this.cObjectService.getByCode(type, event.getCode()).ifPresent(object -> {
      this.cObjectService.delete(object);
    });
  }

  public void handleRemoveGeoObjectEdgeEvent(RemoveGeoObjectEdgeEvent event)
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
        this.bObjectService.getByCode(this.bTypeService.getByCodeOrThrow(event.getSourceType()), event.getSourceCode()).orElseThrow().getRID();

    Object targetRid = ( childType instanceof BaseGeoObjectType ) ? //
        this.gObjectService.getGeoObjectByCode(event.getTargetCode(), event.getTargetType()).getVertex().getRID() : //
        this.bObjectService.getByCode(this.bTypeService.getByCodeOrThrow(event.getTargetType()), event.getTargetCode()).orElseThrow().getRID();

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

  private void createImportHistoryRelationship(ImportHistoryEvent event, ServerGeoObjectIF sgo)
  {
    if (sgo != null)
    {
      String geometryId = sgo.getValue(DefaultAttribute.GEOMETRY.getName(), event.getStartDate());

      if (!StringUtils.isBlank(geometryId) && !StringUtils.isBlank(event.getHistoryId()))
      {
        RelationshipDAO relationship = RelationshipDAO.newInstance(event.getHistoryId(), geometryId, RegistryConstants.JOB_HISTORY_GEOMETRY);
        relationship.setValidate(false);
        relationship.apply();
      }
    }
  }

  private GeoObjectCache getGeoObjectCache()
  {
    TransactionState state = TransactionState.getCurrentTransactionState();

    if (state != null)
    {
      GeoObjectCache cache = (GeoObjectCache) state.getTransactionObject(GEO_CACHE);

      if (cache != null)
      {
        return cache;
      }
    }

    return new GeoObjectCache(1);
  }

  private BusinessObjectCache getBusinessObjectCache()
  {
    TransactionState state = TransactionState.getCurrentTransactionState();

    if (state != null)
    {
      BusinessObjectCache cache = (BusinessObjectCache) state.getTransactionObject(BUSINESS_CACHE);

      if (cache != null)
      {
        return cache;
      }
    }

    return new BusinessObjectCache(1);
  }

  @SuppressWarnings("unchecked")
  private Cache<String, Object> getRidCache()
  {
    TransactionState state = TransactionState.getCurrentTransactionState();

    if (state != null)
    {
      Cache<String, Object> cache = (Cache<String, Object>) state.getTransactionObject(BUSINESS_CACHE);

      if (cache != null)
      {
        return cache;
      }
    }

    return new LRUCache<String, Object>(10);
  }
}