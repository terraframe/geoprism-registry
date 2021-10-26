package net.geoprism.registry.view;

import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class HistoricalRow
{
  private String         eventId;

  private Date           eventDate;

  private String         eventType;

  private LocalizedValue description;

  private String         beforeType;

  private String         beforeCode;

  private LocalizedValue beforeLabel;

  private String         afterType;

  private String         afterCode;

  private LocalizedValue afterLabel;

  public String getEventId()
  {
    return eventId;
  }

  public void setEventId(String eventId)
  {
    this.eventId = eventId;
  }

  public Date getEventDate()
  {
    return eventDate;
  }

  public void setEventDate(Date eventDate)
  {
    this.eventDate = eventDate;
  }

  public LocalizedValue getDescription()
  {
    return description;
  }

  public void setDescription(LocalizedValue description)
  {
    this.description = description;
  }

  public String getBeforeType()
  {
    return beforeType;
  }

  public void setBeforeType(String beforeType)
  {
    this.beforeType = beforeType;
  }

  public String getBeforeCode()
  {
    return beforeCode;
  }

  public void setBeforeCode(String beforeCode)
  {
    this.beforeCode = beforeCode;
  }

  public LocalizedValue getBeforeLabel()
  {
    return beforeLabel;
  }

  public void setBeforeLabel(LocalizedValue beforeLabel)
  {
    this.beforeLabel = beforeLabel;
  }

  public String getAfterType()
  {
    return afterType;
  }

  public void setAfterType(String afterType)
  {
    this.afterType = afterType;
  }

  public String getAfterCode()
  {
    return afterCode;
  }

  public void setAfterCode(String afterCode)
  {
    this.afterCode = afterCode;
  }

  public LocalizedValue getAfterLabel()
  {
    return afterLabel;
  }

  public void setAfterLabel(LocalizedValue afterLabel)
  {
    this.afterLabel = afterLabel;
  }

  public String getEventType()
  {
    return eventType;
  }

  public void setEventType(String eventType)
  {
    this.eventType = eventType;
  }

  public static HistoricalRow parse(List<?> row)
  {
    // statement.append("SELECT event.oid AS eventId");
    // statement.append(", event.eventDate AS eventDate");
    // statement.append(", transitionType AS eventType");
    // statement.append(", event.description AS description");
    // statement.append(", event.beforeTypeCode AS beforeType");
    // statement.append(", source.code AS beforeCode");
    // statement.append(", source.displayLabel_cot.value[0] AS beforeLabel");
    // statement.append(", event.afterTypeCode AS afterType");
    // statement.append(", target.code AS afterCode");
    // statement.append(", target.displayLabel_cot.value[0] AS afterLabel");

    HistoricalRow ret = new HistoricalRow();
    ret.setEventId((String) row.get(0));
    ret.setEventDate((Date) row.get(1));
    ret.setEventType((String) row.get(2));

    return ret;
  }
}
