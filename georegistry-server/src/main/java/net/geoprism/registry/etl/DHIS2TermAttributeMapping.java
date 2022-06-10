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
package net.geoprism.registry.etl;

import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.etl.export.dhis2.MissingDHIS2TermMapping;
import net.geoprism.registry.etl.export.dhis2.MissingDHIS2TermOrgUnitGroupMapping;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ServiceFactory;

public class DHIS2TermAttributeMapping extends DHIS2AttributeMapping
{
  
  private static final Logger logger = LoggerFactory.getLogger(DHIS2TermAttributeMapping.class);
  
  private Map<String, String> terms;
  
  private boolean isOrgUnitGroup = false;
  
  public String getTermMapping(String classifierId)
  {
    if (this.terms == null)
    {
      return null;
    }
    
    return this.terms.get(classifierId);
  }

  @Override
  public String getAttributeMappingStrategy()
  {
    if (attributeMappingStrategy == null || attributeMappingStrategy.length() == 0)
    {
      return DHIS2TermAttributeMapping.class.getName();
    }
    else
    {
      return attributeMappingStrategy;
    }
  }
  
  public Map<String, String> getTerms()
  {
    return terms;
  }

  public void setTerms(Map<String, String> terms)
  {
    this.terms = terms;
  }

  public boolean isOrgUnitGroup()
  {
    return isOrgUnitGroup;
  }

  public void setIsOrgUnitGroup(boolean isOrgUnitGroup)
  {
    this.isOrgUnitGroup = isOrgUnitGroup;
  }
  
  public boolean isStandardAttribute()
  {
    return false;
  }
  
  public boolean isCustomAttribute()
  {
    return this.isOrgUnitGroup() || super.isCustomAttribute();
  }

  @Override
  public void writeCustomAttributes(JsonArray attributeValues, VertexServerGeoObject serverGo, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel syncLevel, String lastUpdateDate, String createDate)
  {
    ServerGeoObjectType got = syncLevel.getGeoObjectType();
    AttributeType attr = got.getAttribute(this.getCgrAttrName()).get();
    
    Object value = serverGo.getValue(attr.getName());
    
    if (value == null || (value instanceof String && ((String)value).length() == 0))
    {
      return;
    }
    
    if (this.isOrgUnitGroup())
    {
      if (attr instanceof AttributeTermType)
      {
        String termId = this.getTermId(value);
        String orgUnitGroupId = this.getTermMapping(termId);

        if (orgUnitGroupId == null)
        {
//          MissingDHIS2TermOrgUnitGroupMapping ex = new MissingDHIS2TermOrgUnitGroupMapping();
//          ex.setTermCode(termId);
//          throw ex;
        }
        else
        {
          Set<String> orgUnitGroupIdSet = syncLevel.getOrgUnitGroupIdSet(orgUnitGroupId);
          if (orgUnitGroupIdSet == null)
          {
            orgUnitGroupIdSet = syncLevel.newOrgUnitGroupIdSet(orgUnitGroupId);
          }
  
          orgUnitGroupIdSet.add(serverGo.getExternalId(dhis2Config.getSystem()));
        }
      }
      else
      {
        logger.error("Unsupported attribute type [" + attr.getClass().getName() + "] with name [" + attr.getName() + "] when matched to OrgUnitGroup.");
        return;
      }
    }
    else
    {
      super.writeCustomAttributes(attributeValues, serverGo, dhis2Config, syncLevel, lastUpdateDate, createDate);
    }
  }
  
  @Override
  protected void writeAttributeValue(AttributeType attr, String propertyName, Object value, JsonObject av)
  {
    if (attr instanceof AttributeTermType)
    {
      String termId = this.getTermId(value);

      String termMapping = this.getTermMapping(termId);

      if (termMapping == null)
      {
//        MissingDHIS2TermMapping ex = new MissingDHIS2TermMapping();
//        ex.setTermCode(termId);
//        throw ex;
      }
      else
      {
        av.addProperty("value", termMapping);
      }
    }
    else
    {
      logger.error("Unsupported attribute type [" + attr.getClass().getName() + "] with name [" + attr.getName() + "] for mapping type [" + DHIS2TermAttributeMapping.class.getName() + "].");
      return;
    }
  }
  
  protected String getTermId(Object value)
  {
    if (value == null)
    {
      return null;
    }
    else if (value instanceof Classifier)
    {
      return ((Classifier)value).getClassifierId();
    }
    else
    {
      throw new ProgrammingErrorException("Unsupported value type [" + value.getClass().getName() + "].");
    }
  }

}
