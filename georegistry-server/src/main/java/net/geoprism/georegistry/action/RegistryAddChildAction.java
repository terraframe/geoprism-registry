package net.geoprism.georegistry.action;

import net.geoprism.georegistry.service.RegistryService;

import org.commongeoregistry.adapter.action.AddChildAction;

public class RegistryAddChildAction extends RegistryAction
{
    private final AddChildAction action;
    
    private final RegistryService registry;
    
    private final String sessionId;

    public RegistryAddChildAction(AddChildAction action, RegistryService registry, String sessionId)
    {
      this.action = action;
      this.registry = registry;
      this.sessionId = sessionId;
    }

    @Override
    public void execute()
    {
      String parentId = this.action.getParentId();
      String childId = this.action.getChildId();
      String hierarchyId = this.action.getHierarchyId(); // TODO : This is a code, not an id
      
      this.registry.addChild(sessionId, parentId, childId, hierarchyId);
    }
}
