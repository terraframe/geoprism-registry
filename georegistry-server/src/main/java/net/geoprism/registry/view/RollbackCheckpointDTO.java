package net.geoprism.registry.view;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.geoprism.registry.spring.DateTimeSerializer;

public class RollbackCheckpointDTO
{
  private String oid;

  private String historyId;

  private String filename;

  @JsonSerialize(using = DateTimeSerializer.class)
  private Date   importDate;

  @JsonSerialize(using = DateTimeSerializer.class)
  private Date   checkpointDate;

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public String getHistoryId()
  {
    return historyId;
  }

  public void setHistoryId(String historyId)
  {
    this.historyId = historyId;
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  public Date getImportDate()
  {
    return importDate;
  }

  public void setImportDate(Date importDate)
  {
    this.importDate = importDate;
  }

  public Date getCheckpointDate()
  {
    return checkpointDate;
  }

  public void setCheckpointDate(Date checkpointDate)
  {
    this.checkpointDate = checkpointDate;
  }

}
