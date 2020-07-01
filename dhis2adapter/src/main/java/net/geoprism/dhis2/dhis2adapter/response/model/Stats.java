package net.geoprism.dhis2.dhis2adapter.response.model;

public class Stats
{
  private long created;
  
  private long updated;
  
  private long deleted;
  
  private long ignored;
  
  private long total;

  public long getCreated()
  {
    return created;
  }

  public void setCreated(long created)
  {
    this.created = created;
  }

  public long getUpdated()
  {
    return updated;
  }

  public void setUpdated(long updated)
  {
    this.updated = updated;
  }

  public long getDeleted()
  {
    return deleted;
  }

  public void setDeleted(long deleted)
  {
    this.deleted = deleted;
  }

  public long getIgnored()
  {
    return ignored;
  }

  public void setIgnored(long ignored)
  {
    this.ignored = ignored;
  }

  public long getTotal()
  {
    return total;
  }

  public void setTotal(long total)
  {
    this.total = total;
  }
  
}
