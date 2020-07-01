package net.geoprism.dhis2.dhis2adapter.response.model;

import java.util.List;

public class ObjectReport
{
  private String klass;
  
  private Integer index;
  
  private List<ErrorReport> errorReports;

  public Boolean hasErrorReports()
  {
    return this.errorReports != null && this.errorReports.size() > 0;
  }

  public String getKlass()
  {
    return klass;
  }

  public void setKlass(String klass)
  {
    this.klass = klass;
  }

  public Integer getIndex()
  {
    return index;
  }

  public void setIndex(Integer index)
  {
    this.index = index;
  }

  public List<ErrorReport> getErrorReports()
  {
    return errorReports;
  }

  public void setErrorReports(List<ErrorReport> errorReports)
  {
    this.errorReports = errorReports;
  }
  
}
