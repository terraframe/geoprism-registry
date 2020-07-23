package net.geoprism.dhis2.dhis2adapter;

import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.Option;
import net.geoprism.dhis2.dhis2adapter.response.model.OptionSet;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;

public class DHIS2Objects
{
  public static final String ATTRIBUTES = "attributes";
  
  public static final String ORGANISATION_UNITS = "organisationUnits";
  
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
    else
    {
      throw new UnsupportedOperationException();
    }
  }
}
