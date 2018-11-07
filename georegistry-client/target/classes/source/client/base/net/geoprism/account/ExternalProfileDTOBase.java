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
package net.geoprism.account;

@com.runwaysdk.business.ClassSignature(hash = -837290465)
public abstract class ExternalProfileDTOBase extends com.runwaysdk.system.SingleActorDTO 
{
  public final static String CLASS = "net.geoprism.account.ExternalProfile";
  private static final long serialVersionUID = -837290465;
  
  protected ExternalProfileDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ExternalProfileDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DISPLAYNAME = "displayName";
  public static java.lang.String REMOTEID = "remoteId";
  public static java.lang.String SERVER = "server";
  public static java.lang.String USERNAME = "username";
  public String getDisplayName()
  {
    return getValue(DISPLAYNAME);
  }
  
  public void setDisplayName(String value)
  {
    if(value == null)
    {
      setValue(DISPLAYNAME, "");
    }
    else
    {
      setValue(DISPLAYNAME, value);
    }
  }
  
  public boolean isDisplayNameWritable()
  {
    return isWritable(DISPLAYNAME);
  }
  
  public boolean isDisplayNameReadable()
  {
    return isReadable(DISPLAYNAME);
  }
  
  public boolean isDisplayNameModified()
  {
    return isModified(DISPLAYNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getDisplayNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(DISPLAYNAME).getAttributeMdDTO();
  }
  
  public String getRemoteId()
  {
    return getValue(REMOTEID);
  }
  
  public void setRemoteId(String value)
  {
    if(value == null)
    {
      setValue(REMOTEID, "");
    }
    else
    {
      setValue(REMOTEID, value);
    }
  }
  
  public boolean isRemoteIdWritable()
  {
    return isWritable(REMOTEID);
  }
  
  public boolean isRemoteIdReadable()
  {
    return isReadable(REMOTEID);
  }
  
  public boolean isRemoteIdModified()
  {
    return isModified(REMOTEID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getRemoteIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(REMOTEID).getAttributeMdDTO();
  }
  
  public net.geoprism.account.OauthServerDTO getServer()
  {
    if(getValue(SERVER) == null || getValue(SERVER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.account.OauthServerDTO.get(getRequest(), getValue(SERVER));
    }
  }
  
  public String getServerId()
  {
    return getValue(SERVER);
  }
  
  public void setServer(net.geoprism.account.OauthServerDTO value)
  {
    if(value == null)
    {
      setValue(SERVER, "");
    }
    else
    {
      setValue(SERVER, value.getOid());
    }
  }
  
  public boolean isServerWritable()
  {
    return isWritable(SERVER);
  }
  
  public boolean isServerReadable()
  {
    return isReadable(SERVER);
  }
  
  public boolean isServerModified()
  {
    return isModified(SERVER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getServerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(SERVER).getAttributeMdDTO();
  }
  
  public String getUsername()
  {
    return getValue(USERNAME);
  }
  
  public void setUsername(String value)
  {
    if(value == null)
    {
      setValue(USERNAME, "");
    }
    else
    {
      setValue(USERNAME, value);
    }
  }
  
  public boolean isUsernameWritable()
  {
    return isWritable(USERNAME);
  }
  
  public boolean isUsernameReadable()
  {
    return isReadable(USERNAME);
  }
  
  public boolean isUsernameModified()
  {
    return isModified(USERNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getUsernameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(USERNAME).getAttributeMdDTO();
  }
  
  public static final java.lang.String login(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String serverId, java.lang.String code, java.lang.String locales, java.lang.String redirectBase)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{serverId, code, locales, redirectBase};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.account.ExternalProfileDTO.CLASS, "login", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static net.geoprism.account.ExternalProfileDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.account.ExternalProfileDTO) dto;
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
  
  public static net.geoprism.account.ExternalProfileQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.account.ExternalProfileQueryDTO) clientRequest.getAllInstances(net.geoprism.account.ExternalProfileDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.account.ExternalProfileDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.account.ExternalProfileDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.account.ExternalProfileDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.account.ExternalProfileDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.account.ExternalProfileDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.account.ExternalProfileDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
