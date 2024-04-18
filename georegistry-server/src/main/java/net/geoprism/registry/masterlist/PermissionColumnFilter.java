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
package net.geoprism.registry.masterlist;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeAttribute;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.service.permission.GPROrganizationPermissionService;
import net.geoprism.registry.service.business.ServiceFactory;

public class PermissionColumnFilter implements ColumnFilter
{
  private boolean isPrivate;

  public PermissionColumnFilter(ListTypeVersion version)
  {
    GPROrganizationPermissionService permissions = ServiceFactory.getBean(GPROrganizationPermissionService.class);

    this.isPrivate = version.getListVisibility().equals(ListType.PRIVATE) && !permissions.isMemberOrSRA(version.getListType().getOrganization());
  }

  @Override
  public boolean isValid(ListTypeAttribute attribute)
  {
    if (this.isPrivate)
    {
      MdAttributeConcreteDAOIF mdAttribute = MdAttributeConcreteDAO.get(attribute.getListAttributeOid());
      String attributeName = mdAttribute.definesAttribute();

      return attributeName.equals("code") || attributeName.contains("displayLabel");
    }

    return true;
  }

  @Override
  public boolean isValid(ListColumn column)
  {
    if (column instanceof ListAttributeGroup)
    {
      return ( (ListAttributeGroup) column ).getColumns().size() > 0;
    }

    return true;
  }

}
