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
package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = -1250897842)
public abstract class ListTypeHierarchyGroupDTOBase extends net.geoprism.registry.ListTypeGroupDTO
{
  public final static String CLASS = "net.geoprism.registry.ListTypeHierarchyGroup";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1250897842;
  
  protected ListTypeHierarchyGroupDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeHierarchyGroupDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String HIERARCHY = "hierarchy";
  public net.geoprism.registry.HierarchicalRelationshipTypeDTO getHierarchy()
  {
    if(getValue(HIERARCHY) == null || getValue(HIERARCHY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.HierarchicalRelationshipTypeDTO.get(getRequest(), getValue(HIERARCHY));
    }
  }
  
  public String getHierarchyOid()
  {
    return getValue(HIERARCHY);
  }
  
  public void setHierarchy(net.geoprism.registry.HierarchicalRelationshipTypeDTO value)
  {
    if(value == null)
    {
      setValue(HIERARCHY, "");
    }
    else
    {
      setValue(HIERARCHY, value.getOid());
    }
  }
  
  public boolean isHierarchyWritable()
  {
    return isWritable(HIERARCHY);
  }
  
  public boolean isHierarchyReadable()
  {
    return isReadable(HIERARCHY);
  }
  
  public boolean isHierarchyModified()
  {
    return isModified(HIERARCHY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getHierarchyMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(HIERARCHY).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.ListTypeHierarchyGroupDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.ListTypeHierarchyGroupDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createBusiness(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
  public static net.geoprism.registry.ListTypeHierarchyGroupQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.ListTypeHierarchyGroupQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.ListTypeHierarchyGroupDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.ListTypeHierarchyGroupDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeHierarchyGroupDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.ListTypeHierarchyGroupDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.ListTypeHierarchyGroupDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeHierarchyGroupDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.ListTypeHierarchyGroupDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
