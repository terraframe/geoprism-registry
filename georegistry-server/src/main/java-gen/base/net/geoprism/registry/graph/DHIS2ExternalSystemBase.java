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
package net.geoprism.registry.graph;

@com.runwaysdk.business.ClassSignature(hash = -474237016)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to DHIS2ExternalSystem.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class DHIS2ExternalSystemBase extends net.geoprism.registry.graph.ExternalSystem
{
  public final static String CLASS = "net.geoprism.registry.graph.DHIS2ExternalSystem";
  public static final java.lang.String OAUTHSERVER = "oauthServer";
  public static final java.lang.String PASSWORD = "password";
  public static final java.lang.String URL = "url";
  public static final java.lang.String USERNAME = "username";
  public static final java.lang.String VERSION = "version";
  private static final long serialVersionUID = -474237016;
  
  public DHIS2ExternalSystemBase()
  {
    super();
  }
  
  public net.geoprism.account.OauthServer getOauthServer()
  {
    if (this.getObjectValue(OAUTHSERVER) == null)
    {
      return null;
    }
    else
    {
      return net.geoprism.account.OauthServer.get( (String) this.getObjectValue(OAUTHSERVER));
    }
  }
  
  public String getOauthServerOid()
  {
    return (String) this.getObjectValue(OAUTHSERVER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getOauthServerMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.DHIS2ExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(OAUTHSERVER);
  }
  
  public void setOauthServer(net.geoprism.account.OauthServer value)
  {
    this.setValue(OAUTHSERVER, value.getOid());
  }
  
  public void setOauthServerId(java.lang.String oid)
  {
    this.setValue(OAUTHSERVER, oid);
  }
  
  public String getPassword()
  {
    return (String) this.getObjectValue(PASSWORD);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getPasswordMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.DHIS2ExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(PASSWORD);
  }
  
  public void setPassword(String value)
  {
    this.setValue(PASSWORD, value);
  }
  
  public String getUrl()
  {
    return (String) this.getObjectValue(URL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getUrlMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.DHIS2ExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(URL);
  }
  
  public void setUrl(String value)
  {
    this.setValue(URL, value);
  }
  
  public String getUsername()
  {
    return (String) this.getObjectValue(USERNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getUsernameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.DHIS2ExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(USERNAME);
  }
  
  public void setUsername(String value)
  {
    this.setValue(USERNAME, value);
  }
  
  public String getVersion()
  {
    return (String) this.getObjectValue(VERSION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getVersionMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.DHIS2ExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(VERSION);
  }
  
  public void setVersion(String value)
  {
    this.setValue(VERSION, value);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static DHIS2ExternalSystem get(String oid)
  {
    return (DHIS2ExternalSystem) com.runwaysdk.business.graph.VertexObject.get(CLASS, oid);
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}
