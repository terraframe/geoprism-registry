package net.geoprism.registry.axon.event.repository;

import java.util.Date;

public abstract class AbstractHierarchyEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  public abstract Date getStartDate();

  public abstract Date getEndDate();

  @Override
  public Boolean isValidFor(Date date)
  {
    return ( date.after(this.getStartDate()) && date.before(this.getEndDate()) ) || date.equals(this.getStartDate()) || date.equals(this.getEndDate());
  }
}
