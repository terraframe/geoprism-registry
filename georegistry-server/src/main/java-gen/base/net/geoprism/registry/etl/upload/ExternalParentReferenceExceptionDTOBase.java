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

@com.runwaysdk.business.ClassSignature(hash = 848104042)
public abstract class ExternalParentReferenceExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.upload.ExternalParentReferenceException";
  private static final long serialVersionUID = 848104042;
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected ExternalParentReferenceExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public ExternalParentReferenceExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CONTEXT = "context";
  public static java.lang.String EXTERNALID = "externalId";
  public static java.lang.String OID = "oid";
  public static java.lang.String PARENTTYPE = "parentType";
  public String getContext()
  {
    return getValue(CONTEXT);
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
  
  public boolean isContextWritable()
  {
    return isWritable(CONTEXT);
  }
  
  public boolean isContextReadable()
  {
    return isReadable(CONTEXT);
  }
  
  public boolean isContextModified()
  {
    return isModified(CONTEXT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getContextMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CONTEXT).getAttributeMdDTO();
  }
  
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
  
  public String getParentType()
  {
    return getValue(PARENTTYPE);
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
  
  public boolean isParentTypeWritable()
  {
    return isWritable(PARENTTYPE);
  }
  
  public boolean isParentTypeReadable()
  {
    return isReadable(PARENTTYPE);
  }
  
  public boolean isParentTypeModified()
  {
    return isModified(PARENTTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getParentTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PARENTTYPE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{context}", this.getContext().toString());
    template = template.replace("{externalId}", this.getExternalId().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{parentType}", this.getParentType().toString());
    
    return template;
  }
  
}
