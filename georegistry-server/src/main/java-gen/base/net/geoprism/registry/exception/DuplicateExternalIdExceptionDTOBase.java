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
package net.geoprism.registry.exception;

@com.runwaysdk.business.ClassSignature(hash = 761878757)
public abstract class DuplicateExternalIdExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.exception.DuplicateExternalIdException";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 761878757;
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateExternalIdExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateExternalIdExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String EXTERNALID = "externalId";
  public static java.lang.String EXTERNALSYSTEM = "externalSystem";
  public static java.lang.String OID = "oid";
  public String getExternalId()
  {
    return getValue(EXTERNALID);
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
  
  public boolean isExternalIdWritable()
  {
    return isWritable(EXTERNALID);
  }
  
  public boolean isExternalIdReadable()
  {
    return isReadable(EXTERNALID);
  }
  
  public boolean isExternalIdModified()
  {
    return isModified(EXTERNALID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getExternalIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(EXTERNALID).getAttributeMdDTO();
  }
  
  public String getExternalSystem()
  {
    return getValue(EXTERNALSYSTEM);
  }
  
  public void setExternalSystem(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALSYSTEM, "");
    }
    else
    {
      setValue(EXTERNALSYSTEM, value);
    }
  }
  
  public boolean isExternalSystemWritable()
  {
    return isWritable(EXTERNALSYSTEM);
  }
  
  public boolean isExternalSystemReadable()
  {
    return isReadable(EXTERNALSYSTEM);
  }
  
  public boolean isExternalSystemModified()
  {
    return isModified(EXTERNALSYSTEM);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getExternalSystemMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(EXTERNALSYSTEM).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{externalId}", this.getExternalId().toString());
    template = template.replace("{externalSystem}", this.getExternalSystem().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
