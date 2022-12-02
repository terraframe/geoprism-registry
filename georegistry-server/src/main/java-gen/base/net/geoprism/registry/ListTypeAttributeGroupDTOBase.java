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

@com.runwaysdk.business.ClassSignature(hash = -1802750321)
public abstract class ListTypeAttributeGroupDTOBase extends net.geoprism.registry.ListTypeGroupDTO
{
  public final static String CLASS = "net.geoprism.registry.ListTypeAttributeGroup";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1802750321;
  
  protected ListTypeAttributeGroupDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeAttributeGroupDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String TYPEATTRIBUTE = "typeAttribute";
  public com.runwaysdk.system.metadata.MdAttributeDTO getTypeAttribute()
  {
    if(getValue(TYPEATTRIBUTE) == null || getValue(TYPEATTRIBUTE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdAttributeDTO.get(getRequest(), getValue(TYPEATTRIBUTE));
    }
  }
  
  public String getTypeAttributeOid()
  {
    return getValue(TYPEATTRIBUTE);
  }
  
  public void setTypeAttribute(com.runwaysdk.system.metadata.MdAttributeDTO value)
  {
    if(value == null)
    {
      setValue(TYPEATTRIBUTE, "");
    }
    else
    {
      setValue(TYPEATTRIBUTE, value.getOid());
    }
  }
  
  public boolean isTypeAttributeWritable()
  {
    return isWritable(TYPEATTRIBUTE);
  }
  
  public boolean isTypeAttributeReadable()
  {
    return isReadable(TYPEATTRIBUTE);
  }
  
  public boolean isTypeAttributeModified()
  {
    return isModified(TYPEATTRIBUTE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getTypeAttributeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(TYPEATTRIBUTE).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.ListTypeAttributeGroupDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.ListTypeAttributeGroupDTO) dto;
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
  
  public static net.geoprism.registry.ListTypeAttributeGroupQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.ListTypeAttributeGroupQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.ListTypeAttributeGroupDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.ListTypeAttributeGroupDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeAttributeGroupDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.ListTypeAttributeGroupDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.ListTypeAttributeGroupDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeAttributeGroupDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.ListTypeAttributeGroupDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
