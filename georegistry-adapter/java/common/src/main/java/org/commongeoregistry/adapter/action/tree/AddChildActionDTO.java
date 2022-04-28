/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.action.tree;

import java.util.Date;

import org.commongeoregistry.adapter.JsonDateUtil;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.constants.RegistryUrls;

import com.google.gson.JsonObject;

public class AddChildActionDTO extends AbstractActionDTO
{
  private String childCode;

  private String childTypeCode;

  private String parentCode;

  private String hierarchyCode;

  private String parentTypeCode;

  private Date   startDate;

  private Date   endDate;

  public AddChildActionDTO()
  {
    super(RegistryUrls.GEO_OBJECT_ADD_CHILD);
  }

  @Override
  protected void buildJson(JsonObject json)
  {
    super.buildJson(json);

    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILDCODE, this.childCode);
    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILD_TYPE_CODE, this.childTypeCode);
    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENTCODE, this.parentCode);
    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENT_TYPE_CODE, this.parentTypeCode);
    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_HIERARCHY_CODE, this.hierarchyCode);
    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_START_DATE, JsonDateUtil.format(startDate));
    json.addProperty(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_END_DATE, JsonDateUtil.format(endDate));
  }

  @Override
  protected void buildFromJson(JsonObject json)
  {
    super.buildFromJson(json);

    this.childCode = json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILDCODE).getAsString();
    this.childTypeCode = json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILD_TYPE_CODE).getAsString();
    this.parentCode = json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENTCODE).getAsString();
    this.parentTypeCode = json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENT_TYPE_CODE).getAsString();
    this.hierarchyCode = json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_HIERARCHY_CODE).getAsString();
    this.startDate = JsonDateUtil.parse(json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_START_DATE).getAsString());
    this.endDate = JsonDateUtil.parse(json.get(RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_END_DATE).getAsString());
  }

  public void setChildCode(String childCode)
  {
    this.childCode = childCode;
  }

  public void setChildTypeCode(String childTypeCode)
  {
    this.childTypeCode = childTypeCode;
  }

  public void setParentCode(String parentCode)
  {
    this.parentCode = parentCode;
  }

  public void setHierarchyCode(String hierarchyCode)
  {
    this.hierarchyCode = hierarchyCode;
  }

  public void setParentTypeCode(String parentTypeCode)
  {
    this.parentTypeCode = parentTypeCode;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public String getChildCode()
  {
    return childCode;
  }

  public String getChildTypeCode()
  {
    return childTypeCode;
  }

  public String getParentCode()
  {
    return parentCode;
  }

  public String getParentTypeCode()
  {
    return parentTypeCode;
  }

  public String getHierarchyCode()
  {
    return hierarchyCode;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }
}
