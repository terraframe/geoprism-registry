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

@com.runwaysdk.business.ClassSignature(hash = 912924407)
public abstract class ClassifierProblemViewDTOBase extends com.runwaysdk.business.ViewDTO 
{
  public final static String CLASS = "net.geoprism.ontology.ClassifierProblemView";
  private static final long serialVersionUID = 912924407;
  
  protected ClassifierProblemViewDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CLASSIFIERID = "classifierId";
  public static java.lang.String CONCRETEID = "concreteId";
  public static java.lang.String OID = "oid";
  public static java.lang.String PROBLEM = "problem";
  public static java.lang.String PROBLEMNAME = "problemName";
  public String getClassifierId()
  {
    return getValue(CLASSIFIERID);
  }
  
  public void setClassifierId(String value)
  {
    if(value == null)
    {
      setValue(CLASSIFIERID, "");
    }
    else
    {
      setValue(CLASSIFIERID, value);
    }
  }
  
  public boolean isClassifierIdWritable()
  {
    return isWritable(CLASSIFIERID);
  }
  
  public boolean isClassifierIdReadable()
  {
    return isReadable(CLASSIFIERID);
  }
  
  public boolean isClassifierIdModified()
  {
    return isModified(CLASSIFIERID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getClassifierIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CLASSIFIERID).getAttributeMdDTO();
  }
  
  public String getConcreteId()
  {
    return getValue(CONCRETEID);
  }
  
  public void setConcreteId(String value)
  {
    if(value == null)
    {
      setValue(CONCRETEID, "");
    }
    else
    {
      setValue(CONCRETEID, value);
    }
  }
  
  public boolean isConcreteIdWritable()
  {
    return isWritable(CONCRETEID);
  }
  
  public boolean isConcreteIdReadable()
  {
    return isReadable(CONCRETEID);
  }
  
  public boolean isConcreteIdModified()
  {
    return isModified(CONCRETEID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getConcreteIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CONCRETEID).getAttributeMdDTO();
  }
  
  public String getProblem()
  {
    return getValue(PROBLEM);
  }
  
  public void setProblem(String value)
  {
    if(value == null)
    {
      setValue(PROBLEM, "");
    }
    else
    {
      setValue(PROBLEM, value);
    }
  }
  
  public boolean isProblemWritable()
  {
    return isWritable(PROBLEM);
  }
  
  public boolean isProblemReadable()
  {
    return isReadable(PROBLEM);
  }
  
  public boolean isProblemModified()
  {
    return isModified(PROBLEM);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProblemMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROBLEM).getAttributeMdDTO();
  }
  
  public String getProblemName()
  {
    return getValue(PROBLEMNAME);
  }
  
  public void setProblemName(String value)
  {
    if(value == null)
    {
      setValue(PROBLEMNAME, "");
    }
    else
    {
      setValue(PROBLEMNAME, value);
    }
  }
  
  public boolean isProblemNameWritable()
  {
    return isWritable(PROBLEMNAME);
  }
  
  public boolean isProblemNameReadable()
  {
    return isReadable(PROBLEMNAME);
  }
  
  public boolean isProblemNameModified()
  {
    return isModified(PROBLEMNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getProblemNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PROBLEMNAME).getAttributeMdDTO();
  }
  
  public static ClassifierProblemViewDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.ViewDTO dto = (com.runwaysdk.business.ViewDTO)clientRequest.get(oid);
    
    return (ClassifierProblemViewDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createSessionComponent(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
}
