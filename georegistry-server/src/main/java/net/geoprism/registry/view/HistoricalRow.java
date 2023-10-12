/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.view;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.etl.export.SeverGeoObjectJsonAdapters;

public class HistoricalRow implements JsonSerializable
{
  public static final String EVENT_ID     = "eventId";

  public static final String EVENT_DATE   = "eventDate";

  public static final String EVENT_TYPE   = "eventType";

  public static final String DESCRIPTION  = "description";

  public static final String BEFORE_TYPE  = "beforeType";

  public static final String BEFORE_CODE  = "beforeCode";

  public static final String BEFORE_LABEL = "beforeLabel";

  public static final String AFTER_TYPE   = "afterType";

  public static final String AFTER_CODE   = "afterCode";

  public static final String AFTER_LABEL  = "afterLabel";

  private Long               eventId;

  private Date               eventDate;

  private String             eventType;

  private LocalizedValue     description;

  private String             beforeType;

  private String             beforeCode;

  private LocalizedValue     beforeLabel;

  private String             afterType;

  private String             afterCode;

  private LocalizedValue     afterLabel;

  public Long getEventId()
  {
    return eventId;
  }

  public void setEventId(Long eventId)
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

  public String getLocalizedEventType()
  {
    String eventType = this.getEventType();

    String[] split = eventType.split("_");

    if (split.length > 1)
    {
      StringBuilder builder = new StringBuilder();
      builder.append(LocalizationFacade.localize("transition.event.type." + split[1].toLowerCase()));
      builder.append(" - ");
      builder.append(LocalizationFacade.localize("transition.event.type." + split[0].toLowerCase()));

      return builder.toString();
    }

    return LocalizationFacade.localize("transition.event.type." + eventType.toLowerCase());
  }

  @Override
  public JsonElement toJSON()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(LocalizedValue.class, new SeverGeoObjectJsonAdapters.LocalizedValueSerializer());
    builder.registerTypeAdapter(Date.class, new SeverGeoObjectJsonAdapters.DateSerializer());

    JsonObject json = builder.create().toJsonTree(this, this.getClass()).getAsJsonObject();
    json.addProperty(EVENT_TYPE, this.getLocalizedEventType());

    return json;
  }

  @SuppressWarnings("unchecked")
  public static HistoricalRow parse(Map<String, Object> row)
  {
    Date eventDate = (Date) row.get(EVENT_DATE);

    HistoricalRow ret = new HistoricalRow();
    ret.setEventId((Long) row.get(EVENT_ID));
    ret.setEventDate(eventDate);
    ret.setEventType((String) row.get(EVENT_TYPE));
    ret.setDescription(RegistryLocalizedValueConverter.convert((Map<String, Object>) row.get(DESCRIPTION)));
    ret.setBeforeLabel(parseLabel(row, eventDate, BEFORE_LABEL));
    ret.setBeforeCode((String) row.get(BEFORE_CODE));
    ret.setBeforeType((String) row.get(BEFORE_TYPE));
    ret.setAfterLabel(parseLabel(row, eventDate, AFTER_LABEL));
    ret.setAfterCode((String) row.get(AFTER_CODE));
    ret.setAfterType((String) row.get(AFTER_TYPE));

    return ret;
  }

  @SuppressWarnings("unchecked")
  private static LocalizedValue parseLabel(Map<String, Object> row, Date date, String attributeName)
  {
    List<Map<String, Object>> labels = (List<Map<String, Object>>) row.get(attributeName);

    return RegistryLocalizedValueConverter.convert(labels, date);
  }

}
