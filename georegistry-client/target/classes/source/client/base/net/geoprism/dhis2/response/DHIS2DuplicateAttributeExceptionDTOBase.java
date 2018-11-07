/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.dhis2.response;

@com.runwaysdk.business.ClassSignature(hash = 143517797)
public abstract class DHIS2DuplicateAttributeExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO 
{
  public final static String CLASS = "net.geoprism.dhis2.response.DHIS2DuplicateAttributeException";
  private static final long serialVersionUID = 143517797;
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DHIS2DuplicateAttributeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DHIS2ATTRS = "dhis2Attrs";
  public static java.lang.String OID = "oid";
  public String getDhis2Attrs()
  {
    return getValue(DHIS2ATTRS);
  }
  
  public void setDhis2Attrs(String value)
  {
    if(value == null)
    {
      setValue(DHIS2ATTRS, "");
    }
    else
    {
      setValue(DHIS2ATTRS, value);
    }
  }
  
  public boolean isDhis2AttrsWritable()
  {
    return isWritable(DHIS2ATTRS);
  }
  
  public boolean isDhis2AttrsReadable()
  {
    return isReadable(DHIS2ATTRS);
  }
  
  public boolean isDhis2AttrsModified()
  {
    return isModified(DHIS2ATTRS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getDhis2AttrsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(DHIS2ATTRS).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{dhis2Attrs}", this.getDhis2Attrs().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
