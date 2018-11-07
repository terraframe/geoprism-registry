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
package net.geoprism.ontology;

@com.runwaysdk.business.ClassSignature(hash = 82083749)
public abstract class PossibleAmbiguousSynonymDTOBase extends com.runwaysdk.business.WarningDTO 
{
  public final static String CLASS = "net.geoprism.ontology.PossibleAmbiguousSynonym";
  private static final long serialVersionUID = 82083749;
  
  public PossibleAmbiguousSynonymDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CLASSIFIERLABEL = "classifierLabel";
  public static java.lang.String OID = "oid";
  public static java.lang.String SYNONYMLABEL = "synonymLabel";
  public String getClassifierLabel()
  {
    return getValue(CLASSIFIERLABEL);
  }
  
  public void setClassifierLabel(String value)
  {
    if(value == null)
    {
      setValue(CLASSIFIERLABEL, "");
    }
    else
    {
      setValue(CLASSIFIERLABEL, value);
    }
  }
  
  public boolean isClassifierLabelWritable()
  {
    return isWritable(CLASSIFIERLABEL);
  }
  
  public boolean isClassifierLabelReadable()
  {
    return isReadable(CLASSIFIERLABEL);
  }
  
  public boolean isClassifierLabelModified()
  {
    return isModified(CLASSIFIERLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getClassifierLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CLASSIFIERLABEL).getAttributeMdDTO();
  }
  
  public String getSynonymLabel()
  {
    return getValue(SYNONYMLABEL);
  }
  
  public void setSynonymLabel(String value)
  {
    if(value == null)
    {
      setValue(SYNONYMLABEL, "");
    }
    else
    {
      setValue(SYNONYMLABEL, value);
    }
  }
  
  public boolean isSynonymLabelWritable()
  {
    return isWritable(SYNONYMLABEL);
  }
  
  public boolean isSynonymLabelReadable()
  {
    return isReadable(SYNONYMLABEL);
  }
  
  public boolean isSynonymLabelModified()
  {
    return isModified(SYNONYMLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSynonymLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SYNONYMLABEL).getAttributeMdDTO();
  }
  
}
