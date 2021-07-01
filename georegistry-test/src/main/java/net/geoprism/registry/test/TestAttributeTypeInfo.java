/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

public class TestAttributeTypeInfo
{
  private String name;
  
  private String label;
  
  private String type;
  
  private TestGeoObjectTypeInfo got;
  
  private AttributeType dto;
  
  private MdAttributeConcreteDAOIF serverObject;
  
  public TestAttributeTypeInfo(String attributeName, TestGeoObjectTypeInfo got)
  {
    this.name = attributeName;
    this.got = got;
  }
  
  public TestAttributeTypeInfo(AttributeType at, TestGeoObjectTypeInfo got)
  {
    this.name = at.getName();
    this.dto = at;
    this.got = got;
    this.type = at.getType();
  }

  public TestAttributeTypeInfo(String name, String label, TestGeoObjectTypeInfo got, String type)
  {
    this.name = name;
    this.label = label;
    this.got = got;
    this.type = type;
  }

  public String getAttributeName()
  {
    return name;
  }

  public void setAttributeName(String attributeName)
  {
    this.name = attributeName;
  }
  
  public AttributeType fetchDTO()
  {
    Optional<AttributeType> optional = got.fetchDTO().getAttribute(this.name);
    
    if (optional.isPresent())
    {
      return optional.get();
    }
    else
    {
      return null;
    }
  }
  
  public MdAttributeConcreteDAOIF getServerObject()
  {
    if (serverObject == null)
    {
      serverObject = got.getServerObject().getMdAttribute(this.name);
    }
    
    return serverObject;
  }
  
  public void apply()
  {
    if (this.fetchDTO() == null)
    {
      if (this.type.equals(AttributeTermType.TYPE))
      {
        TestDataSet.createTermAttribute(name, label, got, null);
      }
      else
      {
        TestDataSet.createAttribute(this.name, this.label, this.got, this.type);
      }
    }
  }
  
  public void applyTerm(Term attrRoot)
  {
    if (this.fetchDTO() == null)
    {
      TestDataSet.createTermAttribute(this.name, this.label, this.got, attrRoot);
    }
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public TestGeoObjectTypeInfo getGeoObjectType()
  {
    return got;
  }

  public void setGeoObjectType(TestGeoObjectTypeInfo got)
  {
    this.got = got;
  }

  public Term getRootTerm()
  {
    return ( (AttributeTermType) this.fetchDTO() ).getRootTerm();
  }
}
