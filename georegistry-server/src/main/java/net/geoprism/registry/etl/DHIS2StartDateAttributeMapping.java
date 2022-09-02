package net.geoprism.registry.etl;

import java.util.Date;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.localization.LocalizationFacade;

public class DHIS2StartDateAttributeMapping extends DHIS2VOTDateAttributeMapping
{
  @Override
  protected String getLabel()
  {
    return LocalizationFacade.localize("sync.attr.mapStrategy.startDate");
  }
  
  @Override
  public String getAttributeMappingStrategy()
  {
    return DHIS2StartDateAttributeMapping.class.getName();
  }
  
  protected Date getVOTDate(ValueOverTime vot)
  {
    return vot.getStartDate();
  }
}
