/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerHierarchyType;

public abstract class ExternalSystemSyncConfig
{
  private ExternalSystem      system;

  private Organization        organization;

  private ServerHierarchyType hierarchy;

  private LocalizedValue      label;

  public ExternalSystem getSystem()
  {
    return system;
  }
  
  public void setSystem(ExternalSystem system)
  {
    this.system = system;
  }

  public Organization getOrganization()
  {
    return organization;
  }

  public void setOrganization(Organization organization)
  {
    this.organization = organization;
  }

  public ServerHierarchyType getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(ServerHierarchyType hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  public LocalizedValue getLabel()
  {
    return label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  public void populate(SynchronizationConfig config)
  {
    this.setHierarchy(config.getServerHierarchyType());
    this.setLabel(LocalizedValueConverter.convert(config.getLabel()));
    this.setOrganization(config.getOrganization());
  }

}
