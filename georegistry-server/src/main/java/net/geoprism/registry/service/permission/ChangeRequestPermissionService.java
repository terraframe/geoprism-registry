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
package net.geoprism.registry.service.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.model.ServerGeoObjectType;

@Service
public class ChangeRequestPermissionService
{
  public static enum ChangeRequestPermissionAction {
    EXECUTE, WRITE_APPROVAL_STATUS, READ_APPROVAL_STATUS, READ_DETAILS, WRITE_DETAILS, WRITE_CODE, READ_DOCUMENTS, WRITE_DOCUMENTS, READ_CONTRIBUTOR_NOTES, WRITE_CONTRIBUTOR_NOTES, READ_MAINTAINER_NOTES, WRITE_MAINTAINER_NOTES, READ, WRITE, SUBMIT, DELETE
  }

  @Autowired
  private RolePermissionService permissions;

  public Set<ChangeRequestPermissionAction> getPermissions(ChangeRequest cr)
  {
    final String orgCode = cr.getOrganizationCode();
    final String gotCode = cr.getGeoObjectTypeCode();
    ServerGeoObjectType type = null;

    if (gotCode != null)
    {
      type = ServerGeoObjectType.get(gotCode, true);
    }

    HashSet<ChangeRequestPermissionAction> actions = new HashSet<ChangeRequestPermissionAction>();

    final AllGovernanceStatus status = cr.getGovernanceStatus();

    if (this.permissions.isSRA())
    {
      actions.addAll(Arrays.asList(ChangeRequestPermissionAction.values()));

      actions.remove(ChangeRequestPermissionAction.DELETE);
      actions.remove(ChangeRequestPermissionAction.WRITE_CONTRIBUTOR_NOTES);
      actions.remove(ChangeRequestPermissionAction.WRITE_DETAILS);

      if (status.equals(AllGovernanceStatus.ACCEPTED))
      {
        actions.remove(ChangeRequestPermissionAction.EXECUTE);
        actions.remove(ChangeRequestPermissionAction.WRITE_MAINTAINER_NOTES);
      }
    }
    else if (this.permissions.isRA(orgCode))
    {
      actions.addAll(Arrays.asList(ChangeRequestPermissionAction.values()));

      actions.remove(ChangeRequestPermissionAction.DELETE);
      actions.remove(ChangeRequestPermissionAction.WRITE_CONTRIBUTOR_NOTES);
      actions.remove(ChangeRequestPermissionAction.WRITE_DETAILS);

      if (status.equals(AllGovernanceStatus.ACCEPTED))
      {
        actions.remove(ChangeRequestPermissionAction.EXECUTE);
        actions.remove(ChangeRequestPermissionAction.WRITE_MAINTAINER_NOTES);
      }
    }
    else if (this.permissions.isRM(orgCode, type))
    {
      actions.addAll(Arrays.asList(ChangeRequestPermissionAction.values()));

      actions.remove(ChangeRequestPermissionAction.DELETE);
      actions.remove(ChangeRequestPermissionAction.WRITE_CONTRIBUTOR_NOTES);
      actions.remove(ChangeRequestPermissionAction.WRITE_DETAILS);

      if (status.equals(AllGovernanceStatus.ACCEPTED))
      {
        actions.remove(ChangeRequestPermissionAction.EXECUTE);
        actions.remove(ChangeRequestPermissionAction.WRITE_MAINTAINER_NOTES);
      }
    }
    else if (this.permissions.isRC(orgCode, type) || this.permissions.isAC(orgCode, type))
    {
      actions.addAll(Arrays.asList(ChangeRequestPermissionAction.READ, ChangeRequestPermissionAction.WRITE, ChangeRequestPermissionAction.READ_APPROVAL_STATUS, ChangeRequestPermissionAction.READ_DETAILS, ChangeRequestPermissionAction.WRITE_DETAILS, ChangeRequestPermissionAction.READ_DOCUMENTS, ChangeRequestPermissionAction.WRITE_DOCUMENTS, ChangeRequestPermissionAction.READ_MAINTAINER_NOTES, ChangeRequestPermissionAction.READ_CONTRIBUTOR_NOTES, ChangeRequestPermissionAction.WRITE_CONTRIBUTOR_NOTES, ChangeRequestPermissionAction.WRITE_CODE, ChangeRequestPermissionAction.SUBMIT, ChangeRequestPermissionAction.DELETE));

      SessionIF session = Session.getCurrentSession();
      if (session == null || session.getUser() == null || cr.getCreatedBy() == null || !cr.getCreatedBy().getOid().equals(session.getUser().getOid()))
      {
        actions.remove(ChangeRequestPermissionAction.DELETE);
      }

      if (status.equals(AllGovernanceStatus.ACCEPTED) || status.equals(AllGovernanceStatus.REJECTED) || status.equals(AllGovernanceStatus.INVALID) || status.equals(AllGovernanceStatus.PARTIAL))
      {
        actions.remove(ChangeRequestPermissionAction.WRITE_CONTRIBUTOR_NOTES);
        actions.remove(ChangeRequestPermissionAction.WRITE_DETAILS);
        actions.remove(ChangeRequestPermissionAction.DELETE);
      }
    }

    if (orgCode == null || gotCode == null)
    {
      actions.removeAll(Arrays.asList(ChangeRequestPermissionAction.EXECUTE, ChangeRequestPermissionAction.WRITE_APPROVAL_STATUS, ChangeRequestPermissionAction.WRITE_DETAILS));

      if (gotCode == null)
      {
        actions.remove(ChangeRequestPermissionAction.READ_DETAILS);
      }

      if (this.permissions.isSRA() || this.permissions.isRA() || this.permissions.isRM())
      {
        actions.add(ChangeRequestPermissionAction.DELETE);
      }
    }

    return actions;
  }
}
