package net.geoprism.registry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdRelationshipDAO;

import net.geoprism.registry.axon.event.repository.BusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.ConceptObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.GeoObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptClassBusinessServiceIF;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;

public abstract class DatasetTest
{

  @Autowired
  protected ConceptClassBusinessServiceIF     cClassService;

  @Autowired
  protected BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  protected BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  protected ConceptObjectBusinessServiceIF    cObjectService;

  @Autowired
  protected BusinessObjectBusinessServiceIF   bObjectService;

  @Autowired
  protected GPRGeoObjectBusinessServiceIF     gObjectService;

  @Autowired
  protected EventGateway                      gateway;

  protected ConceptObject createConceptObject(String code, ConceptClass type, DataSource dataSource, Date startDate, Date endDate)
  {
    ConceptObject object = this.cObjectService.newInstance(type);
    object.setCode(code);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), dataSource, startDate, endDate);
    return applyConceptObject(object, true);
  }

  protected ConceptObject applyConceptObject(ConceptObject object, boolean isNew)
  {
    ConceptObjectEventBuilder builder = new ConceptObjectEventBuilder(cObjectService);
    builder.setObject(object, isNew);
    builder.setAttributeUpdate(true);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });

    return this.cObjectService.getByCode(object.getType(), builder.getCode()).orElse(null);
  }

  protected BusinessObject createBusinessObject(String code, BusinessType type, DataSource dataSource, Date startDate, Date endDate)
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setCode(code);
    object.setValue("testBoolean", false);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), dataSource, startDate, endDate);

    return applyBusinessObject(object, true);
  }

  protected void addExternalId(String externalId, ServerGeoObjectIF object, SourceAuthority authority)
  {
    ServerGeoObjectEventBuilder builder = new ServerGeoObjectEventBuilder(this.gObjectService);
    builder.setObject(object);
    builder.addExternalId(authority, externalId, ImportStrategy.NEW_ONLY);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });
  }

  protected BusinessObject applyBusinessObject(BusinessObject object, boolean isNew)
  {
    BusinessObjectEventBuilder builder = new BusinessObjectEventBuilder(bObjectService);
    builder.setObject(object, isNew);
    builder.setAttributeUpdate(true);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });

    return this.bObjectService.getByCode(object.getType(), builder.getCode()).orElse(null);
  }

  protected ServerGeoObjectIF applyGeoObject(ServerGeoObjectIF object)
  {
    GeoObjectOverTime dto = this.gObjectService.toGeoObjectOverTime(object);

    GeoObjectEventBuilder builder = new GeoObjectEventBuilder(this.gObjectService);
    builder.setObject(dto, false);
    builder.setAttributeUpdate(true);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });

    return this.gObjectService.getGeoObjectByCode(object.getCode(), object.getType());
  }

  protected void createBusinessEdges(BusinessObject child, Date startDate, Date endDate, DataSource dataSource, List<Pair<VertexComponent, BusinessEdgeType>> targets)
  {
    BusinessObjectEventBuilder builder = new BusinessObjectEventBuilder(bObjectService);
    builder.setObject(child);

    for (Pair<VertexComponent, BusinessEdgeType> target : targets)
    {
      builder.addParent(target.getFirst(), target.getSecond(), startDate, endDate, dataSource, false);
    }

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });
  }

  protected void createConceptEdges(ConceptObject child, Date startDate, Date endDate, DataSource dataSource, List<Pair<ConceptObject, ConceptEdgeType>> targets)
  {
    ConceptObjectEventBuilder builder = new ConceptObjectEventBuilder(cObjectService);
    builder.setObject(child);

    for (Pair<ConceptObject, ConceptEdgeType> target : targets)
    {
      builder.addParent(target.getFirst(), target.getSecond(), startDate, endDate, dataSource, false);
    }

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });
  }

  public long getJobHistoryGeometryCount(ImportHistory hist) throws SQLException
  {
    MdRelationshipDAOIF mdRelationship = MdRelationshipDAO.getMdRelationshipDAO(RegistryConstants.JOB_HISTORY_GEOMETRY);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdRelationship.getTableName());
    statement.append(" WHERE parent_oid = '" + hist.getOid() + "'");

    try (ResultSet results = Database.query(statement.toString()))
    {
      results.next();

      return results.getLong(1);
    }
  }

}
