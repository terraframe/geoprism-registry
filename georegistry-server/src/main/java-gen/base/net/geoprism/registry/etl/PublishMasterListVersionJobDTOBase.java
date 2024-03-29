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
package net.geoprism.registry.etl;

@com.runwaysdk.business.ClassSignature(hash = -1422296829)
public abstract class PublishMasterListVersionJobDTOBase extends net.geoprism.registry.etl.MasterListJobDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.PublishMasterListVersionJob";
  private static final long serialVersionUID = -1422296829;
  
  protected PublishMasterListVersionJobDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected PublishMasterListVersionJobDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String MASTERLISTVERSION = "masterListVersion";
  public net.geoprism.registry.MasterListVersionDTO getMasterListVersion()
  {
    if(getValue(MASTERLISTVERSION) == null || getValue(MASTERLISTVERSION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.MasterListVersionDTO.get(getRequest(), getValue(MASTERLISTVERSION));
    }
  }
  
  public String getMasterListVersionOid()
  {
    return getValue(MASTERLISTVERSION);
  }
  
  public void setMasterListVersion(net.geoprism.registry.MasterListVersionDTO value)
  {
    if(value == null)
    {
      setValue(MASTERLISTVERSION, "");
    }
    else
    {
      setValue(MASTERLISTVERSION, value.getOid());
    }
  }
  
  public boolean isMasterListVersionWritable()
  {
    return isWritable(MASTERLISTVERSION);
  }
  
  public boolean isMasterListVersionReadable()
  {
    return isReadable(MASTERLISTVERSION);
  }
  
  public boolean isMasterListVersionModified()
  {
    return isModified(MASTERLISTVERSION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getMasterListVersionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(MASTERLISTVERSION).getAttributeMdDTO();
  }
  
  public static net.geoprism.registry.etl.PublishMasterListVersionJobDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.etl.PublishMasterListVersionJobDTO) dto;
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
  
  public static net.geoprism.registry.etl.PublishMasterListVersionJobQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.etl.PublishMasterListVersionJobQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.etl.PublishMasterListVersionJobDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.etl.PublishMasterListVersionJobDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.PublishMasterListVersionJobDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.etl.PublishMasterListVersionJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.etl.PublishMasterListVersionJobDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.etl.PublishMasterListVersionJobDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.etl.PublishMasterListVersionJobDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
