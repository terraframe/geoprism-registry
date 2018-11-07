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

@com.runwaysdk.business.ClassSignature(hash = 1712396787)
public abstract class DHIS2UnexpectedResponseExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO 
{
  public final static String CLASS = "net.geoprism.dhis2.response.DHIS2UnexpectedResponseException";
  private static final long serialVersionUID = 1712396787;
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DHIS2UnexpectedResponseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DHIS2RESPONSE = "dhis2Response";
  public static java.lang.String OID = "oid";
  public String getDhis2Response()
  {
    return getValue(DHIS2RESPONSE);
  }
  
  public void setDhis2Response(String value)
  {
    if(value == null)
    {
      setValue(DHIS2RESPONSE, "");
    }
    else
    {
      setValue(DHIS2RESPONSE, value);
    }
  }
  
  public boolean isDhis2ResponseWritable()
  {
    return isWritable(DHIS2RESPONSE);
  }
  
  public boolean isDhis2ResponseReadable()
  {
    return isReadable(DHIS2RESPONSE);
  }
  
  public boolean isDhis2ResponseModified()
  {
    return isModified(DHIS2RESPONSE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getDhis2ResponseMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(DHIS2RESPONSE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{dhis2Response}", this.getDhis2Response().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
