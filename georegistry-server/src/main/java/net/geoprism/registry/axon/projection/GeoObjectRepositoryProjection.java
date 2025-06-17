package net.geoprism.registry.axon.projection;

import org.apache.commons.lang3.StringUtils;
import org.axonframework.eventhandling.EventHandler;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;

import net.geoprism.registry.ListType;
import net.geoprism.registry.axon.aggregate.RunwayTransactionWrapper;
import net.geoprism.registry.axon.event.ApplyGeoObjectEvent;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

@Service
public class GeoObjectRepositoryProjection
{
  @Autowired
  private GeoObjectBusinessServiceIF service;

  @EventHandler
  public void addGeoObject(ApplyGeoObjectEvent event) throws Exception
  {
    RunwayTransactionWrapper.run(() -> {
      GeoObjectOverTime dto = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), event.getObject());
      
      ServerGeoObjectIF object = this.service.apply(dto, event.getIsNew(), event.getIsImport());
      
      final ServerGeoObjectType type = object.getType();
      
      if (!StringUtils.isBlank(event.getParents()))
      {
        ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(type, event.getParents());
        
        this.service.setParents(object, ptnOt);
      }
      
      // Update all of the working lists which have this record
      ListType.getForType(type).forEach(listType -> {
        listType.getWorkingVersions().forEach(version -> version.publishOrUpdateRecord(object));
      });      
    });
  }
}
