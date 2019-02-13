package net.geoprism.georegistry.action;

import java.text.DateFormat;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.json.JSONObject;

import com.runwaysdk.session.Session;

import net.geoprism.georegistry.service.RegistryService;
import net.geoprism.georegistry.service.ServiceFactory;

public abstract class AbstractAction extends AbstractActionBase
{

  private static final long serialVersionUID = 1324056554;

  protected RegistryService registry;

  public AbstractAction()
  {
    super();

    this.registry = ServiceFactory.getRegistryService();
  }

  abstract public void execute();

  public AbstractAction(RegistryService registry)
  {
    this.registry = registry;
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

  public JSONObject serialize()
  {
    AllGovernanceStatus status = this.getApprovalStatus().get(0);
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Session.getCurrentLocale());

    JSONObject jo = new JSONObject();

    jo.put(AbstractAction.OID, this.getOid());
    jo.put("actionType", this.getType());
    jo.put("actionLabel", this.getMdClass().getDisplayLabel(Session.getCurrentLocale()));
    jo.put(AbstractAction.CREATEACTIONDATE, this.getCreateActionDate());
    jo.put(AbstractAction.CONTRIBUTORNOTES, this.getContributorNotes());
    jo.put(AbstractAction.MAINTAINERNOTES, this.getMaintainerNotes());
    jo.put("statusLabel", status.getDisplayLabel());
    jo.put(AbstractAction.CREATEACTIONDATE, format.format(this.getCreateActionDate()));

    return jo;
  }
  
  public void buildFromJson(JSONObject joAction)
  {
    this.clearApprovalStatus();
    this.addApprovalStatus(AllGovernanceStatus.valueOf(joAction.getString(AbstractAction.APPROVALSTATUS)));
    
    this.setContributorNotes(joAction.getString(AbstractAction.CONTRIBUTORNOTES));
    
    this.setMaintainerNotes(joAction.getString(AbstractAction.MAINTAINERNOTES));
  }

}
