package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractAction;
import org.commongeoregistry.adapter.action.AddChildAction;
import org.commongeoregistry.adapter.action.DeleteAction;
import org.commongeoregistry.adapter.action.UpdateAction;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;

abstract public class RegistryAction
{
    public static RegistryAction convert(AbstractAction action)
    {
      if (action instanceof UpdateAction)
      {
        return new RegistryUpdateAction((UpdateAction) action);
      }
      else if (action instanceof AddChildAction)
      {
        return new RegistryAddChildAction((AddChildAction) action);
      }
      else if (action instanceof DeleteAction)
      {
        return new RegistryDeleteAction((DeleteAction) action);
      }
      else
      {
        throw new UnsupportedOperationException(action.getClass().getName());
      }
    }

    abstract public void execute(RegistryService registry, RegistryAdapter adapter, ConversionService conversionService);
}
