package net.geoprism.registry;

import java.util.Date;
import java.util.List;

import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.runwaysdk.Pair;

import net.geoprism.registry.axon.event.repository.BusinessObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.GeoObjectEventBuilder;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

public abstract class DatasetTest
{

  @Autowired
  protected BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  protected BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  protected BusinessObjectBusinessServiceIF   bObjectService;

  @Autowired
  protected GeoObjectBusinessServiceIF        gObjectService;

  @Autowired
  protected EventGateway                      gateway;

  protected BusinessObject createBusinessObject(String code, BusinessType type, DataSource dataSource)
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setCode(code);
    object.setValue("testBoolean", false);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), dataSource);

    return applyBusinessObject(object, true);
  }

  protected BusinessObject applyBusinessObject(BusinessObject object, boolean isNew)
  {
    BusinessObjectEventBuilder builder = new BusinessObjectEventBuilder(bObjectService);
    builder.setObject(object, isNew);
    builder.setAttributeUpdate(true);

    builder.build().stream().forEach(event -> {
      gateway.publish(GenericEventMessage.asEventMessage(event));
    });

    return this.bObjectService.getByCode(object.getType(), builder.getCode());
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

}
