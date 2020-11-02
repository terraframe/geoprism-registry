/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Common Geo Registry Adapter(tm). If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

/**
 * This is a singleton instance that caches {@link ServerGeoObjectType} objects
 * for creating {@link GeoObject}s and that caches {@link ServerHierarchyType}.
 * 
 * @author nathan
 *
 */
public class ServerMetadataCache implements Serializable
{
  /**
   * 
   */
  private static final long                serialVersionUID = -8829469298178067536L;

  private Map<String, Organization>        organizationMap;

  private Map<String, ServerGeoObjectType> geoGeoObjectTypeMap;

  private Map<String, ServerHierarchyType> hierarchyTypeMap;

  private RegistryAdapter                  adapter;

  public ServerMetadataCache(RegistryAdapter adapter)
  {
    this.adapter = adapter;
  }

  /**
   * Clears the metadata cache.
   */
  public void rebuild()
  {
    this.organizationMap = new ConcurrentHashMap<String, Organization>();
    this.geoGeoObjectTypeMap = new ConcurrentHashMap<String, ServerGeoObjectType>();
    this.hierarchyTypeMap = new ConcurrentHashMap<String, ServerHierarchyType>();

    getAdapter().getMetadataCache().rebuild();
  }

  private RegistryAdapter getAdapter()
  {
    return this.adapter;
  }

  public void addTerm(Term term)
  {
    getAdapter().getMetadataCache().addTerm(term);
  }

  public Optional<Term> getTerm(String code)
  {
    return getAdapter().getMetadataCache().getTerm(code);
  }

  public void addOrganization(Organization organization)
  {
    this.organizationMap.put(organization.getCode(), organization);

    getAdapter().getMetadataCache().addOrganization(organization.toDTO());
  }

  public Optional<Organization> getOrganization(String code)
  {
    return Optional.of(this.organizationMap.get(code));
  }

  public List<Organization> getAllOrganizations()
  {
    // return this.organizationMap.values().toArray(new
    // Organization[this.organizationMap.values().size()]);

    return new ArrayList<Organization>(this.organizationMap.values());
  }

  public void removeOrganization(String code)
  {
    this.organizationMap.remove(code);

    getAdapter().getMetadataCache().removeOrganization(code);
  }

  public void addGeoObjectType(ServerGeoObjectType geoObjectType)
  {
    this.geoGeoObjectTypeMap.put(geoObjectType.getCode(), geoObjectType);

    getAdapter().getMetadataCache().addGeoObjectType(geoObjectType.getType());
  }

  public Optional<ServerGeoObjectType> getGeoObjectType(String code)
  {
    return Optional.of(this.geoGeoObjectTypeMap.get(code));
  }

  public void removeGeoObjectType(String code)
  {
    this.geoGeoObjectTypeMap.remove(code);

    getAdapter().getMetadataCache().removeGeoObjectType(code);
  }

  public void addHierarchyType(ServerHierarchyType hierarchyType)
  {
    this.hierarchyTypeMap.put(hierarchyType.getCode(), hierarchyType);

    getAdapter().getMetadataCache().addHierarchyType(hierarchyType.getType());
  }

  public Optional<ServerHierarchyType> getHierachyType(String code)
  {
    return Optional.of(this.hierarchyTypeMap.get(code));
  }

  public void removeHierarchyType(String code)
  {
    this.hierarchyTypeMap.remove(code);

    getAdapter().getMetadataCache().removeHierarchyType(code);
  }

  public List<Organization> getAllOrganizationsTypes()
  {
    // return this.organizationMap.values().toArray(new
    // Organization[this.organizationMap.values().size()]);

    return new ArrayList<Organization>(this.organizationMap.values());
  }

  public List<String> getAllOrganizationCodes()
  {
    List<Organization> organizations = this.getAllOrganizationsTypes();

    List<String> codes = new ArrayList<String>(organizations.size());

    for (int i = 0; i < organizations.size(); ++i)
    {
      codes.add(organizations.get(i).getCode());
    }

    return codes;
  }

  public List<ServerGeoObjectType> getAllGeoObjectTypes()
  {
    // return this.geoGeoObjectTypeMap.values().toArray(new
    // GeoObjectType[this.geoGeoObjectTypeMap.values().size()]);

    return new ArrayList<ServerGeoObjectType>(this.geoGeoObjectTypeMap.values());
  }

  public List<String> getAllGeoObjectTypeCodes()
  {
    List<ServerGeoObjectType> gots = this.getAllGeoObjectTypes();

    List<String> codes = new ArrayList<String>(gots.size());

    for (int i = 0; i < gots.size(); ++i)
    {
      codes.add(gots.get(i).getCode());
    }

    return codes;
  }

  public List<ServerHierarchyType> getAllHierarchyTypes()
  {
    return new ArrayList<ServerHierarchyType>(this.hierarchyTypeMap.values());
  }
}
