/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.action;

import java.util.Arrays;
import java.util.Set;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.service.permission.ChangeRequestPermissionService;
import net.geoprism.registry.service.permission.ChangeRequestPermissionService.ChangeRequestPermissionAction;
import net.geoprism.registry.service.request.RegistryService;
import net.geoprism.registry.service.request.RegistryServiceIF;

public abstract class AbstractAction extends AbstractActionBase
{

  private static final long serialVersionUID = 1324056554;

  protected RegistryServiceIF registry;

  public AbstractAction()
  {
    super();

    this.registry = ServiceFactory.getBean(RegistryServiceIF.class);
;
  }

  abstract public void execute();

  public AllGovernanceStatus getGovernanceStatus()
  {
    return this.getApprovalStatus().get(0);
  }
  
  protected abstract String getMessage();

  public AbstractAction(RegistryService registry)
  {
    this.registry = registry;
  }
  
  protected boolean doesGOTExist(String code)
  {
    return ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(code).isPresent();
  }

  public static AbstractAction dtoToRegistry(AbstractActionDTO actionDTO)
  {
    AbstractAction action = ActionFactory.newAction(actionDTO.getActionType());

    action.buildFromDTO(actionDTO);

    return action;
  }
  
  public ChangeRequest getChangeRequest()
  {
    return this.getAllRequest().next();
  }

  protected void buildFromDTO(AbstractActionDTO dto)
  {
    this.setApiVersion(dto.getApiVersion());
    this.setCreateActionDate(dto.getCreateActionDate());
    this.setContributorNotes(dto.getContributorNotes());
    this.setMaintainerNotes(dto.getMaintainerNotes());
  }

  /*
   * TODO : We should be converting to a DTO and then serializing, that way we
   * only have to have the serialization logic in one place.
   */
  abstract public JsonObject toJson();

  /*
   * TODO : We should be converting to a DTO and then using 'buildFromDTO', that
   * way we only have to have the serialization logic in one place.
   */
  public void buildFromJson(JSONObject joAction)
  {
    Set<ChangeRequestPermissionAction> perms = ServiceFactory.getBean(ChangeRequestPermissionService.class).getPermissions(this.getAllRequest().next());
    
    if (perms.containsAll(Arrays.asList(
        ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS
      )))
    {
      this.clearApprovalStatus();
      this.addApprovalStatus(AllGovernanceStatus.valueOf(joAction.getString(AbstractAction.APPROVALSTATUS)));
    }
    
    if (perms.containsAll(Arrays.asList(
        ChangeRequestPermissionAction.WRITE_CONTRIBUTOR_NOTES
      )))
    {
      this.setContributorNotes(joAction.getString(AbstractAction.CONTRIBUTORNOTES));
    }
    
    if (perms.containsAll(Arrays.asList(
        ChangeRequestPermissionAction.WRITE_MAINTAINER_NOTES
      )))
    {
      this.setMaintainerNotes(joAction.getString(AbstractAction.MAINTAINERNOTES));
      this.setAdditionalNotes(joAction.getString(AbstractAction.ADDITIONALNOTES));
    }
  }

}
