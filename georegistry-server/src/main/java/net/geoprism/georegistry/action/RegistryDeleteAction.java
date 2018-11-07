package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.DeleteAction;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

public class RegistryDeleteAction extends RegistryAction
{
    private DeleteAction action;

    public RegistryDeleteAction(DeleteAction action)
    {
      this.action = action;
    }

    @Override
    public void execute(RegistryService registry, RegistryAdapter adapter, ConversionService conversionService)
    {
      
    }
}
