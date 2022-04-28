/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

/**
 * This is a singleton instance that caches {@link GeoObjectType} objects for creating {@link GeoObject}s and 
 * that caches {@link HierarchyType}.
 * 
 * @author nathan
 *
 */
public class MetadataCache implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -8829469298178067536L;
  
  private Map<String, OrganizationDTO> organizationMap;
  private Map<String, GeoObjectType> geoGeoObjectTypeMap;
  private Map<String, HierarchyType> hierarchyTypeMap;
  private Map<String, Term> termMap;
  private RegistryAdapter adapter;
  
  public MetadataCache(RegistryAdapter adapter)
  {
    this.adapter = adapter;
  }
  
  /** 
   * Clears the metadata cache.
   */
  public void rebuild()
  {
    this.organizationMap = new ConcurrentHashMap<String, OrganizationDTO>();
    this.geoGeoObjectTypeMap = new ConcurrentHashMap<String, GeoObjectType>();
    this.hierarchyTypeMap = new ConcurrentHashMap<String, HierarchyType>();
    this.termMap = new ConcurrentHashMap<String, Term>();
  }
  
  public void addTerm(Term term) 
  {
    this.termMap.put(term.getCode(), term);
  }
    
  public Optional<Term> getTerm(String code) 
  {
    return Optional.of(this.termMap.get(code));
  }

  public void addOrganization(OrganizationDTO organization) 
  {
    this.organizationMap.put(organization.getCode(), organization);
  }
    
  public Optional<OrganizationDTO> getOrganization(String code) 
  {
    return Optional.of(this.organizationMap.get(code));
  }
  
  public List<OrganizationDTO> getAllOrganizations()
  {
//    return this.organizationMap.values().toArray(new OrganizationDTO[this.organizationMap.values().size()]);
    
    return new ArrayList<OrganizationDTO>(this.organizationMap.values());
  }
  
  public void removeOrganization(String code)
  {
    this.organizationMap.remove(code);
  }
  
  public void addGeoObjectType(GeoObjectType geoObjectType) 
  {
    this.geoGeoObjectTypeMap.put(geoObjectType.getCode(), geoObjectType);
  }
    
  public Optional<GeoObjectType> getGeoObjectType(String code) 
  {
    return Optional.of(this.geoGeoObjectTypeMap.get(code));
  }
  
  public void removeGeoObjectType(String code)
  {
    this.geoGeoObjectTypeMap.remove(code);
  }
  
  public void addHierarchyType(HierarchyType hierarchyType) 
  {
    this.hierarchyTypeMap.put(hierarchyType.getCode(), hierarchyType);
  }
  
  public Optional<HierarchyType> getHierachyType(String code) 
  {
    return Optional.of(this.hierarchyTypeMap.get(code));
  }
  
  public void removeHierarchyType(String code)
  {
    this.hierarchyTypeMap.remove(code);
  }

  public List<OrganizationDTO> getAllOrganizationsTypes()
  {
//    return this.organizationMap.values().toArray(new OrganizationDTO[this.organizationMap.values().size()]);
    
    return new ArrayList<OrganizationDTO>(this.organizationMap.values());
  }
  
  public List<String> getAllOrganizationCodes()
  {
    List<OrganizationDTO> organizations = this.getAllOrganizationsTypes();
    
    List<String> codes = new ArrayList<String>(organizations.size());
    
    for (int i = 0; i < organizations.size(); ++i)
    {
      codes.add(organizations.get(i).getCode());
    }
 
    return codes;
  }
  
  public List<GeoObjectType> getAllGeoObjectTypes()
  {
//    return this.geoGeoObjectTypeMap.values().toArray(new GeoObjectType[this.geoGeoObjectTypeMap.values().size()]);
    
    return new ArrayList<GeoObjectType>(this.geoGeoObjectTypeMap.values());
  }
  
  public List<String> getAllGeoObjectTypeCodes()
  {
    List<GeoObjectType> gots = this.getAllGeoObjectTypes();
    
    List<String> codes = new ArrayList<String>(gots.size());
    
    for (int i = 0; i < gots.size(); ++i)
    {
      codes.add(gots.get(i).getCode());
    }
    
    return codes;
  }

  public List<HierarchyType> getAllHierarchyTypes()
  {
//    return this.hierarchyTypeMap.values().toArray(new HierarchyType[this.hierarchyTypeMap.values().size()]);
    
    return new ArrayList<HierarchyType>(this.hierarchyTypeMap.values());
  }
}
