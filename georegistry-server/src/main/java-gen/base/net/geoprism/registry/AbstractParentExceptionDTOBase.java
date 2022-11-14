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
package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 349020726)
public abstract class AbstractParentExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.AbstractParentException";
  private static final long serialVersionUID = 349020726;
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected AbstractParentExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public AbstractParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String CHILDGEOOBJECTTYPELABEL = "childGeoObjectTypeLabel";
  public static final java.lang.String HIERARCHYTYPELABEL = "hierarchyTypeLabel";
  public static final java.lang.String OID = "oid";
  public static final java.lang.String PARENTGEOOBJECTTYPELABEL = "parentGeoObjectTypeLabel";
  public String getChildGeoObjectTypeLabel()
  {
    return getValue(CHILDGEOOBJECTTYPELABEL);
  }
  
  public void setChildGeoObjectTypeLabel(String value)
  {
    if(value == null)
    {
      setValue(CHILDGEOOBJECTTYPELABEL, "");
    }
    else
    {
      setValue(CHILDGEOOBJECTTYPELABEL, value);
    }
  }
  
  public boolean isChildGeoObjectTypeLabelWritable()
  {
    return isWritable(CHILDGEOOBJECTTYPELABEL);
  }
  
  public boolean isChildGeoObjectTypeLabelReadable()
  {
    return isReadable(CHILDGEOOBJECTTYPELABEL);
  }
  
  public boolean isChildGeoObjectTypeLabelModified()
  {
    return isModified(CHILDGEOOBJECTTYPELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getChildGeoObjectTypeLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CHILDGEOOBJECTTYPELABEL).getAttributeMdDTO();
  }
  
  public String getHierarchyTypeLabel()
  {
    return getValue(HIERARCHYTYPELABEL);
  }
  
  public void setHierarchyTypeLabel(String value)
  {
    if(value == null)
    {
      setValue(HIERARCHYTYPELABEL, "");
    }
    else
    {
      setValue(HIERARCHYTYPELABEL, value);
    }
  }
  
  public boolean isHierarchyTypeLabelWritable()
  {
    return isWritable(HIERARCHYTYPELABEL);
  }
  
  public boolean isHierarchyTypeLabelReadable()
  {
    return isReadable(HIERARCHYTYPELABEL);
  }
  
  public boolean isHierarchyTypeLabelModified()
  {
    return isModified(HIERARCHYTYPELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getHierarchyTypeLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(HIERARCHYTYPELABEL).getAttributeMdDTO();
  }
  
  public String getParentGeoObjectTypeLabel()
  {
    return getValue(PARENTGEOOBJECTTYPELABEL);
  }
  
  public void setParentGeoObjectTypeLabel(String value)
  {
    if(value == null)
    {
      setValue(PARENTGEOOBJECTTYPELABEL, "");
    }
    else
    {
      setValue(PARENTGEOOBJECTTYPELABEL, value);
    }
  }
  
  public boolean isParentGeoObjectTypeLabelWritable()
  {
    return isWritable(PARENTGEOOBJECTTYPELABEL);
  }
  
  public boolean isParentGeoObjectTypeLabelReadable()
  {
    return isReadable(PARENTGEOOBJECTTYPELABEL);
  }
  
  public boolean isParentGeoObjectTypeLabelModified()
  {
    return isModified(PARENTGEOOBJECTTYPELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getParentGeoObjectTypeLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PARENTGEOOBJECTTYPELABEL).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{childGeoObjectTypeLabel}", this.getChildGeoObjectTypeLabel().toString());
    template = template.replace("{hierarchyTypeLabel}", this.getHierarchyTypeLabel().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{parentGeoObjectTypeLabel}", this.getParentGeoObjectTypeLabel().toString());
    
    return template;
  }
  
}
