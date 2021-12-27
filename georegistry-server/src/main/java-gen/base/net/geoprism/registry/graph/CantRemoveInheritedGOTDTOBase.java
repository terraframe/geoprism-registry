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

@com.runwaysdk.business.ClassSignature(hash = -1006816161)
public abstract class CantRemoveInheritedGOTDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.graph.CantRemoveInheritedGOT";
  private static final long serialVersionUID = -1006816161;
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected CantRemoveInheritedGOTDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GOTCODE = "gotCode";
  public static java.lang.String HIERCODE = "hierCode";
  public static java.lang.String INHERITEDHIERARCHYLIST = "inheritedHierarchyList";
  public static java.lang.String OID = "oid";
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
  
  public String getHierCode()
  {
    return getValue(HIERCODE);
  }
  
  public void setHierCode(String value)
  {
    if(value == null)
    {
      setValue(HIERCODE, "");
    }
    else
    {
      setValue(HIERCODE, value);
    }
  }
  
  public boolean isHierCodeWritable()
  {
    return isWritable(HIERCODE);
  }
  
  public boolean isHierCodeReadable()
  {
    return isReadable(HIERCODE);
  }
  
  public boolean isHierCodeModified()
  {
    return isModified(HIERCODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getHierCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(HIERCODE).getAttributeMdDTO();
  }
  
  public String getInheritedHierarchyList()
  {
    return getValue(INHERITEDHIERARCHYLIST);
  }
  
  public void setInheritedHierarchyList(String value)
  {
    if(value == null)
    {
      setValue(INHERITEDHIERARCHYLIST, "");
    }
    else
    {
      setValue(INHERITEDHIERARCHYLIST, value);
    }
  }
  
  public boolean isInheritedHierarchyListWritable()
  {
    return isWritable(INHERITEDHIERARCHYLIST);
  }
  
  public boolean isInheritedHierarchyListReadable()
  {
    return isReadable(INHERITEDHIERARCHYLIST);
  }
  
  public boolean isInheritedHierarchyListModified()
  {
    return isModified(INHERITEDHIERARCHYLIST);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getInheritedHierarchyListMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(INHERITEDHIERARCHYLIST).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{gotCode}", this.getGotCode().toString());
    template = template.replace("{hierCode}", this.getHierCode().toString());
    template = template.replace("{inheritedHierarchyList}", this.getInheritedHierarchyList().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
