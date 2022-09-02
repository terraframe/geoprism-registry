package net.geoprism.registry.etl;

import java.util.Date;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.localization.LocalizationFacade;

public class DHIS2EndDateAttributeMapping extends DHIS2VOTDateAttributeMapping
{
  @Override
  public String getAttributeMappingStrategy()
  {
    return DHIS2EndDateAttributeMapping.class.getName();
  }
  
  @Override
  protected String getLabel()
  {
    return LocalizationFacade.localize("sync.attr.mapStrategy.endDate");
  }
  
  @Override
  protected Date getVOTDate(ValueOverTime vot)
  {
    return vot.getEndDate();
  }
}
