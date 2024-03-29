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

@com.runwaysdk.business.ClassSignature(hash = 853223658)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ExternalParentReferenceException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ExternalParentReferenceExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.etl.upload.ExternalParentReferenceException";
  public static final java.lang.String CONTEXT = "context";
  public static final java.lang.String EXTERNALID = "externalId";
  public static final java.lang.String OID = "oid";
  public static final java.lang.String PARENTTYPE = "parentType";
  private static final long serialVersionUID = 853223658;
  
  public ExternalParentReferenceExceptionBase()
  {
    super();
  }
  
  public ExternalParentReferenceExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public ExternalParentReferenceExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public ExternalParentReferenceExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getContext()
  {
    return getValue(CONTEXT);
  }
  
  public void validateContext()
  {
    this.validateAttribute(CONTEXT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getContextMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.upload.ExternalParentReferenceException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(CONTEXT);
  }
  
  public void setContext(String value)
  {
    if(value == null)
    {
      setValue(CONTEXT, "");
    }
    else
    {
      setValue(CONTEXT, value);
    }
  }
  
  public String getExternalId()
  {
    return getValue(EXTERNALID);
  }
  
  public void validateExternalId()
  {
    this.validateAttribute(EXTERNALID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getExternalIdMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.upload.ExternalParentReferenceException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(EXTERNALID);
  }
  
  public void setExternalId(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALID, "");
    }
    else
    {
      setValue(EXTERNALID, value);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.upload.ExternalParentReferenceException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getParentType()
  {
    return getValue(PARENTTYPE);
  }
  
  public void validateParentType()
  {
    this.validateAttribute(PARENTTYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getParentTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.etl.upload.ExternalParentReferenceException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(PARENTTYPE);
  }
  
  public void setParentType(String value)
  {
    if(value == null)
    {
      setValue(PARENTTYPE, "");
    }
    else
    {
      setValue(PARENTTYPE, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{context}", this.getContext());
    message = replace(message, "{externalId}", this.getExternalId());
    message = replace(message, "{oid}", this.getOid());
    message = replace(message, "{parentType}", this.getParentType());
    return message;
  }
  
}
