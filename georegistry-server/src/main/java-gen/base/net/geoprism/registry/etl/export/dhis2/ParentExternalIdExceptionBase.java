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
package net.geoprism.registry.etl.export.dhis2;

@com.runwaysdk.business.ClassSignature(hash = 551262736)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ParentExternalIdException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ParentExternalIdExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.etl.export.dhis2.ParentExternalIdException";
  public static java.lang.String OID = "oid";
  public static java.lang.String PARENTLABEL = "parentLabel";
  private static final long serialVersionUID = 551262736;
  
  public ParentExternalIdExceptionBase()
  {
    super();
  }
  
  public ParentExternalIdExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public ParentExternalIdExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public ParentExternalIdExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.export.dhis2.ParentExternalIdException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getParentLabel()
  {
    return getValue(PARENTLABEL);
  }
  
  public void validateParentLabel()
  {
    this.validateAttribute(PARENTLABEL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getParentLabelMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.export.dhis2.ParentExternalIdException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(PARENTLABEL);
  }
  
  public void setParentLabel(String value)
  {
    if(value == null)
    {
      setValue(PARENTLABEL, "");
    }
    else
    {
      setValue(PARENTLABEL, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{oid}", this.getOid());
    message = replace(message, "{parentLabel}", this.getParentLabel());
    return message;
  }
  
}
