package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.UpdateAction;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

public class RegistryUpdateAction extends RegistryAction
{
    private UpdateAction action;

    public RegistryUpdateAction(UpdateAction action)
    {
      this.action = action;
    }

    @Override
    public void execute(RegistryService registry, RegistryAdapter adapter, ConversionService conversionService)
    {
      
    }
}
