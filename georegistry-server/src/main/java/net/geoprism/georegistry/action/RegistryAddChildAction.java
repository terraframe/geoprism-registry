package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AddChildAction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

public class RegistryAddChildAction extends RegistryAction
{
    private final AddChildAction action;

    public RegistryAddChildAction(AddChildAction action)
    {
      this.action = action;
    }

    @Override
    public void execute(RegistryService registry, RegistryAdapter adapter, ConversionService conversionService)
    {
      String parentId = this.action.getParentId();
      String childId = this.action.getChildId();
    }
}
