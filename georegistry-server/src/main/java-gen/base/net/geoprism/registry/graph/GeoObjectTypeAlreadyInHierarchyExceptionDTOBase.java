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
package net.geoprism.registry.graph;

@com.runwaysdk.business.ClassSignature(hash = -464488860)
public abstract class GeoObjectTypeAlreadyInHierarchyExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException";
  private static final long serialVersionUID = -464488860;
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public GeoObjectTypeAlreadyInHierarchyExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String GOTCODE = "gotCode";
  public static final java.lang.String OID = "oid";
  public String getGotCode()
  {
    return getValue(GOTCODE);
  }
  
  public void setGotCode(String value)
  {
    if(value == null)
    {
      setValue(GOTCODE, "");
    }
    else
    {
      setValue(GOTCODE, value);
    }
  }
  
  public boolean isGotCodeWritable()
  {
    return isWritable(GOTCODE);
  }
  
  public boolean isGotCodeReadable()
  {
    return isReadable(GOTCODE);
  }
  
  public boolean isGotCodeModified()
  {
    return isModified(GOTCODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGotCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GOTCODE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{gotCode}", this.getGotCode().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
