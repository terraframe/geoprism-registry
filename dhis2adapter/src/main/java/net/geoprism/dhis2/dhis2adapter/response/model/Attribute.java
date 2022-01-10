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
package net.geoprism.dhis2.dhis2adapter.response.model;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * 
 * https://play.dhis2.org/2.31.9/api/26/metadata.json?attributes=true
 * 
 * @author rrowlands
 *
 */
public class Attribute
{
  
  private Date lastUpdated;
  
  private String id;
  
  private Date created;
  
  private String name;
  
  private String code;
  
  private Boolean indicatorAttribute;
  
  private Boolean indicatorGroupAttribute;
  
  private String publicAccess;
  
  private Boolean userGroupAttribute;
  
  private Boolean dataElementAttribute;
  
  private Boolean constantAttribute;
  
  private ValueType valueType;
  
  private Boolean categoryOptionAttribute;
  
  private Boolean optionSetAttribute;
  
  private Boolean sqlViewAttribute;
  
  private Boolean legendSetAttribute;
  
  private Boolean trackedEnttiyAttributeAttribute;
  
  private Boolean organisationUnitAttribute;
  
  private Boolean dataSetAttribute;
  
  private Boolean documentAttribute;
  
  private Boolean unique;
  
  private Boolean validationRuleGroupAttribute;
  
  private Boolean dataElementGroupAttribute;
  
  private Boolean sectionAttribute;
  
  private Boolean trackedEntityTypeAttribute;
  
  private Boolean userAttribute;
  
  private Boolean mandatory;
  
  private Boolean categoryOptionGroupAttribute;
  
  private Boolean programStageAttribute;
  
  private Boolean programAttribute;
  
  private Boolean categoryAttribute;
  
  private Boolean categoryOptionComboAttribute;
  
  private Boolean categoryOptionGroupSetAttribute;
  
  private Boolean validationRuleAttribute;
  
  private Boolean programIndicatorAttribute;
  
  private Boolean organisationUnitGroupAttribute;
  
  private Boolean dataElementGroupSetAttribute;
  
  private Boolean organisationUnitGroupSetAttribute;
  
  private Boolean optionAttribute;
  
  private JsonObject user;
  
  private JsonArray translations;
  
  private JsonArray userGroupAccesses;
  
  private JsonArray userAccesses;
  
  private JsonObject optionSet;

  public String getOptionSetId()
  {
    if (this.optionSet != null && this.optionSet.has("id"))
    {
      return this.optionSet.get("id").getAsString();
    }
    
    return null;
  }
  
  public Date getLastUpdated()
  {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated)
  {
    this.lastUpdated = lastUpdated;
  }

  public Date getCreated()
  {
    return created;
  }

  public void setCreated(Date created)
  {
    this.created = created;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public Boolean getIndicatorAttribute()
  {
    return indicatorAttribute;
  }

  public void setIndicatorAttribute(Boolean indicatorAttribute)
  {
    this.indicatorAttribute = indicatorAttribute;
  }

  public Boolean getIndicatorGroupAttribute()
  {
    return indicatorGroupAttribute;
  }

  public void setIndicatorGroupAttribute(Boolean indicatorGroupAttribute)
  {
    this.indicatorGroupAttribute = indicatorGroupAttribute;
  }

  public String getPublicAccess()
  {
    return publicAccess;
  }

  public void setPublicAccess(String publicAccess)
  {
    this.publicAccess = publicAccess;
  }

  public Boolean getUserGroupAttribute()
  {
    return userGroupAttribute;
  }

  public void setUserGroupAttribute(Boolean userGroupAttribute)
  {
    this.userGroupAttribute = userGroupAttribute;
  }

  public Boolean getDataElementAttribute()
  {
    return dataElementAttribute;
  }

  public void setDataElementAttribute(Boolean dataElementAttribute)
  {
    this.dataElementAttribute = dataElementAttribute;
  }

  public Boolean getConstantAttribute()
  {
    return constantAttribute;
  }

  public void setConstantAttribute(Boolean constantAttribute)
  {
    this.constantAttribute = constantAttribute;
  }

  public ValueType getValueType()
  {
    return valueType;
  }

  public void setValueType(ValueType valueType)
  {
    this.valueType = valueType;
  }

  public Boolean getCategoryOptionAttribute()
  {
    return categoryOptionAttribute;
  }

  public void setCategoryOptionAttribute(Boolean categoryOptionAttribute)
  {
    this.categoryOptionAttribute = categoryOptionAttribute;
  }

  public Boolean getOptionSetAttribute()
  {
    return optionSetAttribute;
  }

  public void setOptionSetAttribute(Boolean optionSetAttribute)
  {
    this.optionSetAttribute = optionSetAttribute;
  }

  public Boolean getSqlViewAttribute()
  {
    return sqlViewAttribute;
  }

  public void setSqlViewAttribute(Boolean sqlViewAttribute)
  {
    this.sqlViewAttribute = sqlViewAttribute;
  }

  public Boolean getLegendSetAttribute()
  {
    return legendSetAttribute;
  }

  public void setLegendSetAttribute(Boolean legendSetAttribute)
  {
    this.legendSetAttribute = legendSetAttribute;
  }

  public Boolean getTrackedEnttiyAttributeAttribute()
  {
    return trackedEnttiyAttributeAttribute;
  }

  public void setTrackedEnttiyAttributeAttribute(Boolean trackedEnttiyAttributeAttribute)
  {
    this.trackedEnttiyAttributeAttribute = trackedEnttiyAttributeAttribute;
  }

  public Boolean getOrganisationUnitAttribute()
  {
    return organisationUnitAttribute;
  }

  public void setOrganisationUnitAttribute(Boolean organisationUnitAttribute)
  {
    this.organisationUnitAttribute = organisationUnitAttribute;
  }

  public Boolean getDataSetAttribute()
  {
    return dataSetAttribute;
  }

  public void setDataSetAttribute(Boolean dataSetAttribute)
  {
    this.dataSetAttribute = dataSetAttribute;
  }

  public Boolean getDocumentAttribute()
  {
    return documentAttribute;
  }

  public void setDocumentAttribute(Boolean documentAttribute)
  {
    this.documentAttribute = documentAttribute;
  }

  public Boolean getUnique()
  {
    return unique;
  }

  public void setUnique(Boolean unique)
  {
    this.unique = unique;
  }

  public Boolean getValidationRuleGroupAttribute()
  {
    return validationRuleGroupAttribute;
  }

  public void setValidationRuleGroupAttribute(Boolean validationRuleGroupAttribute)
  {
    this.validationRuleGroupAttribute = validationRuleGroupAttribute;
  }

  public Boolean getDataElementGroupAttribute()
  {
    return dataElementGroupAttribute;
  }

  public void setDataElementGroupAttribute(Boolean dataElementGroupAttribute)
  {
    this.dataElementGroupAttribute = dataElementGroupAttribute;
  }

  public Boolean getSectionAttribute()
  {
    return sectionAttribute;
  }

  public void setSectionAttribute(Boolean sectionAttribute)
  {
    this.sectionAttribute = sectionAttribute;
  }

  public Boolean getTrackedEntityTypeAttribute()
  {
    return trackedEntityTypeAttribute;
  }

  public void setTrackedEntityTypeAttribute(Boolean trackedEntityTypeAttribute)
  {
    this.trackedEntityTypeAttribute = trackedEntityTypeAttribute;
  }

  public Boolean getUserAttribute()
  {
    return userAttribute;
  }

  public void setUserAttribute(Boolean userAttribute)
  {
    this.userAttribute = userAttribute;
  }

  public Boolean getMandatory()
  {
    return mandatory;
  }

  public void setMandatory(Boolean mandatory)
  {
    this.mandatory = mandatory;
  }

  public Boolean getCategoryOptionGroupAttribute()
  {
    return categoryOptionGroupAttribute;
  }

  public void setCategoryOptionGroupAttribute(Boolean categoryOptionGroupAttribute)
  {
    this.categoryOptionGroupAttribute = categoryOptionGroupAttribute;
  }

  public Boolean getProgramStageAttribute()
  {
    return programStageAttribute;
  }

  public void setProgramStageAttribute(Boolean programStageAttribute)
  {
    this.programStageAttribute = programStageAttribute;
  }

  public Boolean getProgramAttribute()
  {
    return programAttribute;
  }

  public void setProgramAttribute(Boolean programAttribute)
  {
    this.programAttribute = programAttribute;
  }

  public Boolean getCategoryAttribute()
  {
    return categoryAttribute;
  }

  public void setCategoryAttribute(Boolean categoryAttribute)
  {
    this.categoryAttribute = categoryAttribute;
  }

  public Boolean getCategoryOptionComboAttribute()
  {
    return categoryOptionComboAttribute;
  }

  public void setCategoryOptionComboAttribute(Boolean categoryOptionComboAttribute)
  {
    this.categoryOptionComboAttribute = categoryOptionComboAttribute;
  }

  public Boolean getCategoryOptionGroupSetAttribute()
  {
    return categoryOptionGroupSetAttribute;
  }

  public void setCategoryOptionGroupSetAttribute(Boolean categoryOptionGroupSetAttribute)
  {
    this.categoryOptionGroupSetAttribute = categoryOptionGroupSetAttribute;
  }

  public Boolean getValidationRuleAttribute()
  {
    return validationRuleAttribute;
  }

  public void setValidationRuleAttribute(Boolean validationRuleAttribute)
  {
    this.validationRuleAttribute = validationRuleAttribute;
  }

  public Boolean getProgramIndicatorAttribute()
  {
    return programIndicatorAttribute;
  }

  public void setProgramIndicatorAttribute(Boolean programIndicatorAttribute)
  {
    this.programIndicatorAttribute = programIndicatorAttribute;
  }

  public Boolean getOrganisationUnitGroupAttribute()
  {
    return organisationUnitGroupAttribute;
  }

  public void setOrganisationUnitGroupAttribute(Boolean organisationUnitGroupAttribute)
  {
    this.organisationUnitGroupAttribute = organisationUnitGroupAttribute;
  }

  public Boolean getDataElementGroupSetAttribute()
  {
    return dataElementGroupSetAttribute;
  }

  public void setDataElementGroupSetAttribute(Boolean dataElementGroupSetAttribute)
  {
    this.dataElementGroupSetAttribute = dataElementGroupSetAttribute;
  }

  public Boolean getOrganisationUnitGroupSetAttribute()
  {
    return organisationUnitGroupSetAttribute;
  }

  public void setOrganisationUnitGroupSetAttribute(Boolean organisationUnitGroupSetAttribute)
  {
    this.organisationUnitGroupSetAttribute = organisationUnitGroupSetAttribute;
  }

  public Boolean getOptionAttribute()
  {
    return optionAttribute;
  }

  public void setOptionAttribute(Boolean optionAttribute)
  {
    this.optionAttribute = optionAttribute;
  }

  public JsonObject getUser()
  {
    return user;
  }

  public void setUser(JsonObject user)
  {
    this.user = user;
  }

  public JsonArray getTranslations()
  {
    return translations;
  }

  public void setTranslations(JsonArray translations)
  {
    this.translations = translations;
  }

  public JsonArray getUserGroupAccesses()
  {
    return userGroupAccesses;
  }

  public void setUserGroupAccesses(JsonArray userGroupAccesses)
  {
    this.userGroupAccesses = userGroupAccesses;
  }

  public JsonArray getUserAccesses()
  {
    return userAccesses;
  }

  public void setUserAccesses(JsonArray userAccesses)
  {
    this.userAccesses = userAccesses;
  }
  
}
