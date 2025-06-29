package net.geoprism.registry.axon.projection;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.axon.event.repository.BusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.repository.BusinessObjectApplyEvent;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GPRBusinessTypeBusinessService;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;

@Service
public class BusinessObjectRepositoryProjection
{
  @Autowired
  private BusinessEdgeTypeBusinessServiceIF edgeService;

  @Autowired
  private GPRBusinessTypeBusinessService    typeService;

  @Autowired
  private BusinessObjectBusinessServiceIF   service;

  @Autowired
  private GeoObjectBusinessServiceIF        gObjectService;

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

    BusinessEdgeType edgeType = this.edgeService.getByCode(event.getEdgeType());

    this.service.addGeoObject(object, edgeType, geoObject, event.getDirection(), true);
  }

}
