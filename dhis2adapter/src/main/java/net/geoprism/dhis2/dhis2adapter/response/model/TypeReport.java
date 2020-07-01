package net.geoprism.dhis2.dhis2adapter.response.model;

import java.util.ArrayList;
import java.util.List;

public class TypeReport
{
  
  private String klass;
  
  private List<ObjectReport> objectReports;
  
  private Stats stats;

  public String getKlass()
  {
    return klass;
  }

  public void setKlass(String klass)
  {
    this.klass = klass;
  }

  public List<ObjectReport> getObjectReports()
  {
    return objectReports;
  }

  public void setObjectReports(List<ObjectReport> objectReports)
  {
    this.objectReports = objectReports;
  }

  public Stats getStats()
  {
    return stats;
  }

  public void setStats(Stats stats)
  {
    this.stats = stats;
  }

  public Boolean hasErrorReports()
  {
    for (ObjectReport or : this.objectReports)
    {
      if (or.hasErrorReports())
      {
        return true;
      }
    }
    
    return false;
  }

  public List<ErrorReport> getErrorReports()
  {
    List<ErrorReport> reports = new ArrayList<ErrorReport>();
    
    for (ObjectReport or : this.objectReports)
    {
      if (or.hasErrorReports())
      {
        reports.addAll(or.getErrorReports());
      }
    }
    
    return reports;
  }
  
}
