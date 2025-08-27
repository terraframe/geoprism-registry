package net.geoprism.registry.axon.projection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.OriginException;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectCreateEdgeEvent;
import net.geoprism.registry.cache.BusinessObjectCache;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.GPRBusinessTypeBusinessService;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

@Service
public class BusinessObjectRepositoryProjection
{
  private static Logger                     logger     = LoggerFactory.getLogger(BusinessObjectRepositoryProjection.class);

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF edgeService;

  @Autowired
  private GPRBusinessTypeBusinessService    typeService;

  @Autowired
  private BusinessObjectBusinessServiceIF   service;

  @Autowired
  private GeoObjectBusinessServiceIF        gObjectService;

  @Autowired
  private DataSourceBusinessServiceIF       sourceService;

  private BusinessObjectCache               goCache    = new BusinessObjectCache();

  private Cache<String, Object>             goRidCache = new LRUCache<String, Object>(1000);

  public BusinessObjectRepositoryProjection()
  {
    this.goCache = new BusinessObjectCache();
    this.goRidCache = new LRUCache<String, Object>(1000);
  }

  @EventHandler
  @Transaction
  public void apply(BusinessObjectApplyEvent event) throws Exception
  {
    BusinessType type = this.typeService.getByCode(event.getType());

    JsonObject json = JsonParser.parseString(event.getObject()).getAsJsonObject();

    BusinessObject object = event.getIsNew() ? this.service.newInstance(type) : this.service.getByCode(type, event.getCode());

    this.service.populate(object, json);

    this.service.apply(object);
  }

  @EventHandler
  @Transaction
  public void addGeoObject(BusinessObjectAddGeoObjectEvent event) throws Exception
  {
    BusinessType type = this.typeService.getByCode(event.getType());
    BusinessObject object = this.service.getByCode(type, event.getCode());
    ServerGeoObjectIF geoObject = this.gObjectService.getGeoObjectByCode(event.getGeoObjectCode(), event.getGeoObjectType());

    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    DataSource source = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    this.service.addGeoObject(object, edgeType, geoObject, event.getDirection(), event.getEdgeUid(), source, true);
  }

  @EventHandler
  @Transaction
  public void createEdge(BusinessObjectCreateEdgeEvent event) throws Exception
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());
    DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

    if (event.getValidate())
    {
      BusinessObject source = goCache.getOrFetchByCode(event.getSourceCode(), event.getSourceType());
      BusinessObject target = goCache.getOrFetchByCode(event.getTargetCode(), event.getTargetType());

      this.service.addChild(source, edgeType, target, event.getEdgeUid(), dataSource);
    }
    else
    {
      Object parentRid = getOrFetchRid(event.getSourceCode(), event.getSourceType());
      Object childRid = getOrFetchRid(event.getTargetCode(), event.getTargetType());

      this.newEdge(childRid, parentRid, event.getEdgeUid(), edgeType, dataSource, true);
    }
  }

  @EventHandler
  @Transaction
  public void handleRemoteBusinessObject(RemoteBusinessObjectEvent event) throws Exception
  {
    BusinessType type = this.typeService.getByCode(event.getType());

    if (!GeoprismProperties.getOrigin().equals(type.getOrigin()))
    {
      JsonObject json = JsonParser.parseString(event.getObject()).getAsJsonObject();

      BusinessObject object = this.service.getByCode(type, event.getCode());

      if (object == null)
      {
        object = this.service.newInstance(type);
      }

      this.service.populate(object, json);

      this.service.apply(object, false);
    }
    else
    {
      logger.info("Skipping remote business object: [" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void addGeoObject(RemoteBusinessObjectAddGeoObjectEvent event) throws Exception
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(edgeType.getOrigin()))
    {
      BusinessType type = this.typeService.getByCode(event.getType());
      BusinessObject object = this.service.getByCode(type, event.getCode());
      ServerGeoObjectIF geoObject = this.gObjectService.getGeoObjectByCode(event.getGeoObjectCode(), event.getGeoObjectType());

      DataSource source = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      this.service.addGeoObject(object, edgeType, geoObject, event.getDirection(), event.getEdgeUid(), source, false);
    }
    else
    {
      logger.info("Skipping remote add geo object: [" + event.getEdgeType() + "][" + event.getType() + "][" + event.getCode() + "]");
    }
  }

  @EventHandler
  @Transaction
  public void createEdge(RemoteBusinessObjectCreateEdgeEvent event) throws Exception
  {
    BusinessEdgeType edgeType = this.edgeService.getByCodeOrThrow(event.getEdgeType());

    if (!GeoprismProperties.getOrigin().equals(edgeType.getOrigin()))
    {
      Object parentRid = getOrFetchRid(event.getSourceCode(), event.getSourceType());
      Object childRid = getOrFetchRid(event.getTargetCode(), event.getTargetType());
      DataSource dataSource = this.sourceService.getByCode(event.getDataSource()).orElse(null);

      if (!this.service.exists(edgeType, event.getEdgeUid()))
      {
        this.newEdge(childRid, parentRid, event.getEdgeUid(), edgeType, dataSource, false);
      }
    }
    else
    {
      logger.info("Skipping remote create edge: [" + event.getEdgeType() + "][" + event.getSourceType() + "][" + event.getSourceCode() + "]");
    }
  }

  private Object getOrFetchRid(String code, String businessTypeCode)
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

  public void clearCache()
  {
    this.goCache.clear();
    this.goRidCache.clear();
  }

}
