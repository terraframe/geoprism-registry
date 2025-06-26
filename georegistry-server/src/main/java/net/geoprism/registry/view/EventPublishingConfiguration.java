package net.geoprism.registry.view;

import java.util.Date;

public class EventPublishingConfiguration
{
  private Date date;

  private Date startDate;

  private Date endDate;

  public EventPublishingConfiguration(Date date, Date startDate, Date endDate)
  {
    this.date = date;
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

}
