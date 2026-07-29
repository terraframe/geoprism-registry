package net.geoprism.registry.axon.event.repository;

import java.util.Date;

public interface ImportHistoryEvent
{

  public Date getStartDate();

  public Date getEndDate();

  public String getHistoryId();

}
