package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.CreateAction;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonObject;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

public class RegistryCreateAction extends RegistryAction
{
    private final CreateAction action;

    public RegistryCreateAction(CreateAction action)
    {
      this.action = action;
    }

    @Override
    public void execute(RegistryService registry, RegistryAdapter adapter, ConversionService conversionService)
    {
      String type = this.action.getObjType();
      JsonObject json = this.action.getObjJson();
      
      
    }
}
