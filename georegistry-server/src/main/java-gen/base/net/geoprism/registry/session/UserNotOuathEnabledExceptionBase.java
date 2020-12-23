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
package net.geoprism.registry.session;

@com.runwaysdk.business.ClassSignature(hash = 2047158763)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to UserNotOuathEnabledException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class UserNotOuathEnabledExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.session.UserNotOuathEnabledException";
  public static java.lang.String OAUTHSERVER = "oauthServer";
  public static java.lang.String OID = "oid";
  public static java.lang.String USERNAME = "username";
  private static final long serialVersionUID = 2047158763;
  
  public UserNotOuathEnabledExceptionBase()
  {
    super();
  }
  
  public UserNotOuathEnabledExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public UserNotOuathEnabledExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public UserNotOuathEnabledExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getOauthServer()
  {
    return getValue(OAUTHSERVER);
  }
  
  public void validateOauthServer()
  {
    this.validateAttribute(OAUTHSERVER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getOauthServerMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.session.UserNotOuathEnabledException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(OAUTHSERVER);
  }
  
  public void setOauthServer(String value)
  {
    if(value == null)
    {
      setValue(OAUTHSERVER, "");
    }
    else
    {
      setValue(OAUTHSERVER, value);
    }
  }
  
  public String getOid()
  {
    return getValue(OID);
  }
  
  public void validateOid()
  {
    this.validateAttribute(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.session.UserNotOuathEnabledException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getUsername()
  {
    return getValue(USERNAME);
  }
  
  public void validateUsername()
  {
    this.validateAttribute(USERNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getUsernameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.session.UserNotOuathEnabledException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(USERNAME);
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
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{oauthServer}", this.getOauthServer());
    message = replace(message, "{oid}", this.getOid());
    message = replace(message, "{username}", this.getUsername());
    return message;
  }
  
}
