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

@com.runwaysdk.business.ClassSignature(hash = -854962680)
public abstract class PublishShapefileJobDTOBase extends net.geoprism.registry.etl.MasterListJobDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.PublishShapefileJob";
  private static final long serialVersionUID = -854962680;
  
  protected PublishShapefileJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected PublishShapefileJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String VERSION = "version";
  public net.geoprism.registry.MasterListVersionDTO getVersion()
  {
    if(getValue(VERSION) == null || getValue(VERSION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.MasterListVersionDTO.get(getRequest(), getValue(VERSION));
    }
  }
  
  public String getVersionOid()
  {
    return getValue(VERSION);
  }
  
  public void setVersion(net.geoprism.registry.MasterListVersionDTO value)
  {
    if(value == null)
    {
      setValue(VERSION, "");
    }
    else
    {
      setValue(VERSION, value.getOid());
    }
  }
  
  public boolean isVersionWritable()
  {
    return isWritable(VERSION);
  }
  
  public boolean isVersionReadable()
  {
    return isReadable(VERSION);
  }
  
  public boolean isVersionModified()
  {
    return isModified(VERSION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getVersionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(VERSION).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.etl.PublishShapefileJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.PublishShapefileJobDTO) dto;
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
  
  public static net.geoprism.registry.etl.PublishShapefileJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.PublishShapefileJobQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.PublishShapefileJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.PublishShapefileJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.PublishShapefileJobDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.PublishShapefileJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.PublishShapefileJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.PublishShapefileJobDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.PublishShapefileJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
