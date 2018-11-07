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
package net.geoprism.data.etl.excel;

@com.runwaysdk.business.ClassSignature(hash = -85583738)
public abstract class ExcelValueExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO 
{
  public final static String CLASS = "net.geoprism.data.etl.excel.ExcelValueException";
  private static final long serialVersionUID = -85583738;
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected ExcelValueExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public ExcelValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CELL = "cell";
  public static java.lang.String OID = "oid";
  public static java.lang.String MSG = "msg";
  public String getCell()
  {
    return getValue(CELL);
  }
  
  public void setCell(String value)
  {
    if(value == null)
    {
      setValue(CELL, "");
    }
    else
    {
      setValue(CELL, value);
    }
  }
  
  public boolean isCellWritable()
  {
    return isWritable(CELL);
  }
  
  public boolean isCellReadable()
  {
    return isReadable(CELL);
  }
  
  public boolean isCellModified()
  {
    return isModified(CELL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCellMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CELL).getAttributeMdDTO();
  }
  
  public String getMsg()
  {
    return getValue(MSG);
  }
  
  public void setMsg(String value)
  {
    if(value == null)
    {
      setValue(MSG, "");
    }
    else
    {
      setValue(MSG, value);
    }
  }
  
  public boolean isMsgWritable()
  {
    return isWritable(MSG);
  }
  
  public boolean isMsgReadable()
  {
    return isReadable(MSG);
  }
  
  public boolean isMsgModified()
  {
    return isModified(MSG);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getMsgMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(MSG).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{cell}", this.getCell().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{msg}", this.getMsg().toString());
    
    return template;
  }
  
}
