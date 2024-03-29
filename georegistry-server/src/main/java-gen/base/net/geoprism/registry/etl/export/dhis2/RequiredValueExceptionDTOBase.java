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

@com.runwaysdk.business.ClassSignature(hash = 112284904)
public abstract class RequiredValueExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.etl.export.dhis2.RequiredValueException";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 112284904;
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected RequiredValueExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public RequiredValueExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String DATELABEL = "dateLabel";
  public static final java.lang.String DHIS2ATTRLABELS = "dhis2AttrLabels";
  public static final java.lang.String OID = "oid";
  public String getDateLabel()
  {
    return getValue(DATELABEL);
  }
  
  public void setDateLabel(String value)
  {
    if(value == null)
    {
      setValue(DATELABEL, "");
    }
    else
    {
      setValue(DATELABEL, value);
    }
  }
  
  public boolean isDateLabelWritable()
  {
    return isWritable(DATELABEL);
  }
  
  public boolean isDateLabelReadable()
  {
    return isReadable(DATELABEL);
  }
  
  public boolean isDateLabelModified()
  {
    return isModified(DATELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getDateLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(DATELABEL).getAttributeMdDTO();
  }
  
  public String getDhis2AttrLabels()
  {
    return getValue(DHIS2ATTRLABELS);
  }
  
  public void setDhis2AttrLabels(String value)
  {
    if(value == null)
    {
      setValue(DHIS2ATTRLABELS, "");
    }
    else
    {
      setValue(DHIS2ATTRLABELS, value);
    }
  }
  
  public boolean isDhis2AttrLabelsWritable()
  {
    return isWritable(DHIS2ATTRLABELS);
  }
  
  public boolean isDhis2AttrLabelsReadable()
  {
    return isReadable(DHIS2ATTRLABELS);
  }
  
  public boolean isDhis2AttrLabelsModified()
  {
    return isModified(DHIS2ATTRLABELS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getDhis2AttrLabelsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(DHIS2ATTRLABELS).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{dateLabel}", this.getDateLabel().toString());
    template = template.replace("{dhis2AttrLabels}", this.getDhis2AttrLabels().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
