/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.conversion;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;

import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.Organization;
import net.geoprism.registry.graph.GraphOrganization;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.ServiceFactory;

public class OrganizationConverter extends RegistryLocalizedValueConverter
{

  public OrganizationDTO build(Organization organization)
  {
    String code = organization.getCode();

    LocalizedValue label = convert(organization.getDisplayLabel());

    LocalizedValue contactInfo = convert(organization.getContactInfo());

    return new OrganizationDTO(code, label, contactInfo);
  }

  @Transaction
  public ServerOrganization create(String json)
  {
    OrganizationDTO organizationDTO = OrganizationDTO.fromJSON(json);

    return this.create(organizationDTO);
  }

  public ServerOrganization fromDTO(OrganizationDTO organizationDTO)
  {
    ServerOrganization organization = new ServerOrganization(new Organization(), new GraphOrganization());

    organization.setCode(organizationDTO.getCode());
    organization.setDisplayLabel(organizationDTO.getLabel());
    organization.setContactInfo(organizationDTO.getContactInfo());

    return organization;
  }

  @Transaction
  public ServerOrganization create(OrganizationDTO organizationDTO)
  {
    final ServerOrganization organization = this.fromDTO(organizationDTO);

    ServiceFactory.getOrganizationPermissionService().enforceActorCanCreate();

    organization.apply();

    return organization;
  }

  @Transaction
  public ServerOrganization update(OrganizationDTO organizationDTO)
  {
    ServerOrganization organization = ServerOrganization.getByCode(organizationDTO.getCode());

    ServiceFactory.getOrganizationPermissionService().enforceActorCanUpdate();

    try
    {
      organization.lock();

      organization.setCode(organizationDTO.getCode());
      organization.setDisplayLabel(organizationDTO.getLabel());
      organization.setContactInfo(organizationDTO.getContactInfo());
      organization.apply();
    }
    finally
    {
      organization.unlock();
    }

    return organization;
  }

}
