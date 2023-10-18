/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;

public class DHIS2OrgUnitGroupAttributeMapping extends DHIS2AttributeMapping
{

  private static final Logger           logger = LoggerFactory.getLogger(DHIS2OrgUnitGroupAttributeMapping.class);

  private Map<String, String>           terms;

  private transient GPRGeoObjectBusinessServiceIF objectService;

  public DHIS2OrgUnitGroupAttributeMapping()
  {
    this.objectService = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);
  }

  protected String getLabel()
  {
    return LocalizationFacade.localize("sync.attr.targetTypeOrgUnitGroup");
  }

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
    return DHIS2OrgUnitGroupAttributeMapping.class.getName();
  }

  public Map<String, String> getTerms()
  {
    return terms;
  }

  public void setTerms(Map<String, String> terms)
  {
    this.terms = terms;
  }

  public boolean isStandardAttribute()
  {
    return false;
  }

  public boolean isCustomAttribute()
  {
    return true;
  }

  @Override
  public void writeCustomAttributes(JsonArray attributeValues, VertexServerGeoObject serverGo, Date date, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel syncLevel, String lastUpdateDate, String createDate)
  {
    ServerGeoObjectType got = syncLevel.getGeoObjectType();
    AttributeType attr = got.getAttribute(this.getCgrAttrName()).get();

    Object value = this.getAttributeValue(serverGo, date, attr, got);

    if (value == null || ( value instanceof String && ( (String) value ).length() == 0 ))
    {
      return;
    }

    if (attr instanceof AttributeTermType)
    {
      String termId = this.getTermId(value);
      String orgUnitGroupId = this.getTermMapping(termId);

      if (orgUnitGroupId == null)
      {
        // MissingDHIS2TermOrgUnitGroupMapping ex = new
        // MissingDHIS2TermOrgUnitGroupMapping();
        // ex.setTermCode(termId);
        // throw ex;
      }
      else
      {
        Set<String> orgUnitGroupIdSet = syncLevel.getOrgUnitGroupIdSet(orgUnitGroupId);
        if (orgUnitGroupIdSet == null)
        {
          orgUnitGroupIdSet = syncLevel.newOrgUnitGroupIdSet(orgUnitGroupId);
        }

        orgUnitGroupIdSet.add(this.objectService.getExternalId(serverGo, dhis2Config.getSystem()));
      }
    }
    else
    {
      logger.error("Unsupported attribute type [" + attr.getClass().getName() + "] with name [" + attr.getName() + "] when matched to OrgUnitGroup.");
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
      return ( (Classifier) value ).getClassifierId();
    }
    else
    {
      throw new ProgrammingErrorException("Unsupported value type [" + value.getClass().getName() + "].");
    }
  }

}
