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
package net.geoprism.dhis2.dhis2adapter;

import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.Option;
import net.geoprism.dhis2.dhis2adapter.response.model.OptionSet;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnitGroup;

public class DHIS2Objects
{
  public static final String ATTRIBUTES = "attributes";
  
  public static final String ORGANISATION_UNITS = "organisationUnits";
  
  public static final String ORGANISATION_UNIT_GROUPS = "organisationUnitGroups";
  
  public static final String OPTIONS = "options";
  
  public static final String OPTIONSETS = "optionSets";
  
  public static String getPluralObjectNameFromClass(Class<?> dhis2Type)
  {
    if (dhis2Type.equals(Attribute.class))
    {
      return DHIS2Objects.ATTRIBUTES;
    }
    else if (dhis2Type.equals(Option.class))
    {
      return DHIS2Objects.OPTIONS;
    }
    else if (dhis2Type.equals(OptionSet.class))
    {
      return DHIS2Objects.OPTIONSETS;
    }
    else if (dhis2Type.equals(OrganisationUnit.class))
    {
      return DHIS2Objects.ORGANISATION_UNITS;
    }
    else if (dhis2Type.equals(OrganisationUnitGroup.class))
    {
      return DHIS2Objects.ORGANISATION_UNIT_GROUPS;
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }
}
