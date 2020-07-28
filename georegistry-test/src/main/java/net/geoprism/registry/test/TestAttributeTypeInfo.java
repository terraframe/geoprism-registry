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

import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

public class TestAttributeTypeInfo
{
  private String name;
  
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
  }

  public String getAttributeName()
  {
    return name;
  }

  public void setAttributeName(String attributeName)
  {
    this.name = attributeName;
  }
  
  public AttributeType toDTO()
  {
    if (dto == null)
    {
      dto = got.toDTO().getAttribute(this.name).get();
    }
    
    return dto;
  }
  
  public MdAttributeConcreteDAOIF getServerObject()
  {
    if (serverObject == null)
    {
      serverObject = got.getServerObject().getMdAttribute(this.name);
    }
    
    return serverObject;
  }
}
