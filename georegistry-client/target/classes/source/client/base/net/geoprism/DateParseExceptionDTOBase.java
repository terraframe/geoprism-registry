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
package net.geoprism;

@com.runwaysdk.business.ClassSignature(hash = -1014216876)
public abstract class DateParseExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO 
{
  public final static String CLASS = "net.geoprism.DateParseException";
  private static final long serialVersionUID = -1014216876;
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DateParseExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DateParseExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static java.lang.String INPUT = "input";
  public static java.lang.String PATTERN = "pattern";
  public String getInput()
  {
    return getValue(INPUT);
  }
  
  public void setInput(String value)
  {
    if(value == null)
    {
      setValue(INPUT, "");
    }
    else
    {
      setValue(INPUT, value);
    }
  }
  
  public boolean isInputWritable()
  {
    return isWritable(INPUT);
  }
  
  public boolean isInputReadable()
  {
    return isReadable(INPUT);
  }
  
  public boolean isInputModified()
  {
    return isModified(INPUT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getInputMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(INPUT).getAttributeMdDTO();
  }
  
  public String getPattern()
  {
    return getValue(PATTERN);
  }
  
  public void setPattern(String value)
  {
    if(value == null)
    {
      setValue(PATTERN, "");
    }
    else
    {
      setValue(PATTERN, value);
    }
  }
  
  public boolean isPatternWritable()
  {
    return isWritable(PATTERN);
  }
  
  public boolean isPatternReadable()
  {
    return isReadable(PATTERN);
  }
  
  public boolean isPatternModified()
  {
    return isModified(PATTERN);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getPatternMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PATTERN).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{input}", this.getInput().toString());
    template = template.replace("{pattern}", this.getPattern().toString());
    
    return template;
  }
  
}
