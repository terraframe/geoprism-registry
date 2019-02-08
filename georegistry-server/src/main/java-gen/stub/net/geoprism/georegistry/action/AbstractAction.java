package net.geoprism.georegistry.action;

import org.commongeoregistry.adapter.action.AbstractActionDTO;

import com.runwaysdk.session.Session;

import net.geoprism.georegistry.service.RegistryService;
import net.geoprism.georegistry.service.ServiceFactory;

public abstract class AbstractAction extends AbstractActionBase
{
  
  private static final long serialVersionUID = 1324056554;
  
  protected RegistryService registry;
  
  protected String sessionId;
  
  public AbstractAction()
  {
    super();
    
    this.registry = ServiceFactory.getRegistryService();
    
    if (Session.getCurrentSession() != null)
    {
      this.sessionId = Session.getCurrentSession().getOid();
    }
  }
  
  public AbstractAction(RegistryService registry, String sessionId)
  {
    this.registry = registry;
    
    this.sessionId = sessionId;
  }
  
  public void setSessionId(String sessionId)
  {
    this.sessionId = sessionId;
  }
  
  public static AbstractAction dtoToRegistry(AbstractActionDTO actionDTO)
  {
    AbstractAction action = ActionFactory.newAction(actionDTO.getActionType());
    
    action.buildFromDTO(actionDTO);
    
    return action;
  }
  
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    this.setApiVersion(dto.getApiVersion());
    this.setCreateActionDate(dto.getCreateActionDate());
  }

  abstract public void execute();
  
}
