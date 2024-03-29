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
package net.geoprism.registry.geoobject;

@com.runwaysdk.business.ClassSignature(hash = 846776347)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ImportOutOfRangeException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ImportOutOfRangeExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.geoobject.ImportOutOfRangeException";
  public static final java.lang.String ENDDATE = "endDate";
  public static final java.lang.String OID = "oid";
  public static final java.lang.String STARTDATE = "startDate";
  private static final long serialVersionUID = 846776347;
  
  public ImportOutOfRangeExceptionBase()
  {
    super();
  }
  
  public ImportOutOfRangeExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public ImportOutOfRangeExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public ImportOutOfRangeExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getEndDate()
  {
    return getValue(ENDDATE);
  }
  
  public void validateEndDate()
  {
    this.validateAttribute(ENDDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getEndDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ImportOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(ENDDATE);
  }
  
  public void setEndDate(String value)
  {
    if(value == null)
    {
      setValue(ENDDATE, "");
    }
    else
    {
      setValue(ENDDATE, value);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ImportOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getStartDate()
  {
    return getValue(STARTDATE);
  }
  
  public void validateStartDate()
  {
    this.validateAttribute(STARTDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getStartDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ImportOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(STARTDATE);
  }
  
  public void setStartDate(String value)
  {
    if(value == null)
    {
      setValue(STARTDATE, "");
    }
    else
    {
      setValue(STARTDATE, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{endDate}", this.getEndDate());
    message = replace(message, "{oid}", this.getOid());
    message = replace(message, "{startDate}", this.getStartDate());
    return message;
  }
  
}
