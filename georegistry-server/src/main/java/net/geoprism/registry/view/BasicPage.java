package net.geoprism.registry.view;

import java.util.List;

public class BasicPage<T>
{
  private Integer pageSize;

  private Integer pageNumber;

  private List<T> resultSet;

  private Long    count;

  public Integer getPageSize()
  {
    return pageSize;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
  }

  public Integer getPageNumber()
  {
    return pageNumber;
  }

  public void setPageNumber(Integer pageNumber)
  {
    this.pageNumber = pageNumber;
  }

  public List<T> getResultSet()
  {
    return resultSet;
  }

  public void setResultSet(List<T> resultSet)
  {
    this.resultSet = resultSet;
  }

  public Long getCount()
  {
    return count;
  }

  public void setCount(Long count)
  {
    this.count = count;
  }

}
