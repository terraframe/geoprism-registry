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

import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.ontology.Classifier;

public class DHIS2OptionSetAttributeMapping extends DHIS2AttributeMapping
{
  
  private static final Logger logger = LoggerFactory.getLogger(DHIS2OptionSetAttributeMapping.class);
  
  private Map<String, String> terms;
  
  protected String getLabel()
  {
    return LocalizationFacade.localize("sync.attr.targetTypeOptionSet");
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
    return DHIS2OptionSetAttributeMapping.class.getName();
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
  protected void writeAttributeValue(AttributeType attr, String propertyName, Object value, JsonObject av, DHIS2SyncConfig dhis2Config)
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
      logger.error("Unsupported attribute type [" + attr.getClass().getName() + "] with name [" + attr.getName() + "] for mapping type [" + DHIS2OptionSetAttributeMapping.class.getName() + "].");
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
