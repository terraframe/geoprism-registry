/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.data.etl;

@com.runwaysdk.business.ClassSignature(hash = 1581466292)
public abstract class TargetFieldClassifierBindingDTOBase extends net.geoprism.data.etl.TargetFieldBasicBindingDTO 
{
  public final static String CLASS = "net.geoprism.data.etl.TargetFieldClassifierBinding";
  private static final long serialVersionUID = 1581466292;
  
  protected TargetFieldClassifierBindingDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected TargetFieldClassifierBindingDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String PACKAGENAME = "packageName";
  public String getPackageName()
  {
    return getValue(PACKAGENAME);
  }
  
  public void setPackageName(String value)
  {
    if(value == null)
    {
      setValue(PACKAGENAME, "");
    }
    else
    {
      setValue(PACKAGENAME, value);
    }
  }
  
  public boolean isPackageNameWritable()
  {
    return isWritable(PACKAGENAME);
  }
  
  public boolean isPackageNameReadable()
  {
    return isReadable(PACKAGENAME);
  }
  
  public boolean isPackageNameModified()
  {
    return isModified(PACKAGENAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getPackageNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(PACKAGENAME).getAttributeMdDTO();
  }
  
  public static net.geoprism.data.etl.TargetFieldClassifierBindingDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.data.etl.TargetFieldClassifierBindingDTO) dto;
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
  
  public static net.geoprism.data.etl.TargetFieldClassifierBindingQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.data.etl.TargetFieldClassifierBindingQueryDTO) clientRequest.getAllInstances(net.geoprism.data.etl.TargetFieldClassifierBindingDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.data.etl.TargetFieldClassifierBindingDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.data.etl.TargetFieldClassifierBindingDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.data.etl.TargetFieldClassifierBindingDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.data.etl.TargetFieldClassifierBindingDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.data.etl.TargetFieldClassifierBindingDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.data.etl.TargetFieldClassifierBindingDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
