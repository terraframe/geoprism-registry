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
package net.geoprism.registry.etl.upload;

@com.runwaysdk.business.ClassSignature(hash = 749648032)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to InvalidDhis2PathException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class InvalidDhis2PathExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.etl.upload.InvalidDhis2PathException";
  public static java.lang.String DHIS2PATH = "dhis2Path";
  public static java.lang.String OID = "oid";
  private static final long serialVersionUID = 749648032;
  
  public InvalidDhis2PathExceptionBase()
  {
    super();
  }
  
  public InvalidDhis2PathExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidDhis2PathExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidDhis2PathExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getDhis2Path()
  {
    return getValue(DHIS2PATH);
  }
  
  public void validateDhis2Path()
  {
    this.validateAttribute(DHIS2PATH);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getDhis2PathMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.upload.InvalidDhis2PathException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(DHIS2PATH);
  }
  
  public void setDhis2Path(String value)
  {
    if(value == null)
    {
      setValue(DHIS2PATH, "");
    }
    else
    {
      setValue(DHIS2PATH, value);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.upload.InvalidDhis2PathException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{dhis2Path}", this.getDhis2Path());
    message = replace(message, "{oid}", this.getOid());
    return message;
  }
  
}
