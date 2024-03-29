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
package net.geoprism.registry.io;

@com.runwaysdk.business.ClassSignature(hash = 1603249531)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to LatLonException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class LatLonExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.io.LatLonException";
  public static final java.lang.String LAT = "lat";
  public static final java.lang.String LON = "lon";
  public static final java.lang.String OID = "oid";
  private static final long serialVersionUID = 1603249531;
  
  public LatLonExceptionBase()
  {
    super();
  }
  
  public LatLonExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public LatLonExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public LatLonExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getLat()
  {
    return getValue(LAT);
  }
  
  public void validateLat()
  {
    this.validateAttribute(LAT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getLatMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.io.LatLonException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(LAT);
  }
  
  public void setLat(String value)
  {
    if(value == null)
    {
      setValue(LAT, "");
    }
    else
    {
      setValue(LAT, value);
    }
  }
  
  public String getLon()
  {
    return getValue(LON);
  }
  
  public void validateLon()
  {
    this.validateAttribute(LON);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getLonMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.io.LatLonException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(LON);
  }
  
  public void setLon(String value)
  {
    if(value == null)
    {
      setValue(LON, "");
    }
    else
    {
      setValue(LON, value);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.io.LatLonException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{lat}", this.getLat());
    message = replace(message, "{lon}", this.getLon());
    message = replace(message, "{oid}", this.getOid());
    return message;
  }
  
}
