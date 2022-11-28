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

@com.runwaysdk.business.ClassSignature(hash = -1622223670)
public abstract class ParentCodeExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.io.ParentCodeException";
  private static final long serialVersionUID = -1622223670;
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected ParentCodeExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public ParentCodeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String CONTEXT = "context";
  public static final java.lang.String OID = "oid";
  public static final java.lang.String PARENTCODE = "parentCode";
  public static final java.lang.String PARENTTYPE = "parentType";
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
  
  public String getParentCode()
  {
    return getValue(PARENTCODE);
  }
  
  public void setParentCode(String value)
  {
    if(value == null)
    {
      setValue(PARENTCODE, "");
    }
    else
    {
      setValue(PARENTCODE, value);
    }
  }
  
  public boolean isParentCodeWritable()
  {
    return isWritable(PARENTCODE);
  }
  
  public boolean isParentCodeReadable()
  {
    return isReadable(PARENTCODE);
  }
  
  public boolean isParentCodeModified()
  {
    return isModified(PARENTCODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getParentCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PARENTCODE).getAttributeMdDTO();
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
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{parentCode}", this.getParentCode().toString());
    template = template.replace("{parentType}", this.getParentType().toString());
    
    return template;
  }
  
}
