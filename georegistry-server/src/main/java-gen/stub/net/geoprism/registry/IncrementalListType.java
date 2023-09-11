/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class IncrementalListType extends IncrementalListTypeBase
{
  private static final long serialVersionUID = -1103613490;

  public IncrementalListType()
  {
    super();
  }

  @Override
  protected JsonObject formatVersionLabel(LabeledVersion version)
  {
    JsonObject object = new JsonObject();

    // List<ChangeFrequency> frequency = this.getFrequency();
    //
    // if (frequency.contains(ChangeFrequency.ANNUAL))
    // {
    // Calendar calendar =
    // Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    // calendar.setTime(version.getForDate());
    //
    // object.addProperty("type", "text");
    // object.addProperty("value",
    // Integer.toString(calendar.get(Calendar.YEAR)));
    // }
    // else if (frequency.contains(ChangeFrequency.BIANNUAL))
    // {
    // Calendar calendar =
    // Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    // calendar.setTime(version.getForDate());
    //
    // int halfYear = ( calendar.get(Calendar.MONTH) / 6 ) + 1;
    //
    // object.addProperty("type", "text");
    // object.addProperty("value", "H" + halfYear + " " +
    // Integer.toString(calendar.get(Calendar.YEAR)));
    // }
    // else if (frequency.contains(ChangeFrequency.QUARTER))
    // {
    // Calendar calendar =
    // Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    // calendar.setTime(version.getForDate());
    //
    // int quarter = ( calendar.get(Calendar.MONTH) / 3 ) + 1;
    //
    // object.addProperty("type", "text");
    // object.addProperty("value", "Q" + quarter + " " +
    // Integer.toString(calendar.get(Calendar.YEAR)));
    // }
    // else if (frequency.contains(ChangeFrequency.MONTHLY))
    // {
    // Calendar calendar =
    // Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    // calendar.setTime(version.getForDate());
    // calendar.set(Calendar.DAY_OF_MONTH, 1);
    //
    // Date startOfWeek = calendar.getTime();
    //
    // calendar.add(Calendar.MONTH, 1);
    // calendar.add(Calendar.DAY_OF_YEAR, -1);
    //
    // Date endOfWeek = calendar.getTime();
    //
    // JsonObject range = new JsonObject();
    // range.addProperty("startDate", GeoRegistryUtil.formatDate(startOfWeek,
    // false));
    // range.addProperty("endDate", GeoRegistryUtil.formatDate(endOfWeek,
    // false));
    //
    // object.addProperty("type", "range");
    // object.add("value", range);
    // }
    // else
    // {
    object.addProperty("type", "date");
    object.addProperty("value", GeoRegistryUtil.formatDate(version.getForDate(), false));
    // }

    return object;
  }

  public List<Date> getFrequencyDates(Date startDate, Date endDate)
  {
    final Date today = new Date();
    LinkedList<Date> dates = new LinkedList<Date>();

    if (startDate != null && endDate != null)
    {
      Calendar end = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
      end.setTime(endDate);

      if (end.getTime().after(today))
      {
        end.setTime(today);
      }

      List<ChangeFrequency> frequencies = this.getFrequency();

      if (frequencies.contains(ChangeFrequency.ANNUAL))
      {
        Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
        calendar.setTime(startDate);

        while (calendar.before(end) || calendar.equals(end))
        {
          dates.add(calendar.getTime());

          calendar.add(Calendar.YEAR, 1);
        }
      }
      else if (frequencies.contains(ChangeFrequency.BIANNUAL))
      {
        Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
        calendar.setTime(startDate);

        while (calendar.before(end) || calendar.equals(end))
        {
          dates.add(calendar.getTime());

          calendar.add(Calendar.MONTH, 6);
        }
      }
      else if (frequencies.contains(ChangeFrequency.QUARTER))
      {
        Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
        calendar.setTime(startDate);

        while (calendar.before(end) || calendar.equals(end))
        {
          dates.add(calendar.getTime());

          calendar.add(Calendar.MONTH, 3);
        }
      }
      else if (frequencies.contains(ChangeFrequency.MONTHLY))
      {
        Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
        calendar.setTime(startDate);

        while (calendar.before(end) || calendar.equals(end))
        {
          dates.add(calendar.getTime());

          calendar.add(Calendar.MONTH, 1);
        }
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }

    return dates;
  }

  private Calendar getEndOfYear(Date date)
  {
    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.setTime(date);
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    calendar.set(Calendar.DAY_OF_MONTH, 31);

    return calendar;
  }

  private Calendar getEndOfHalfYear(Date date)
  {
    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.setTime(date);

    moveToEndOfHalfYear(calendar);

    return calendar;
  }

  private Calendar getEndOfQuarter(Date date)
  {
    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.setTime(date);

    moveToEndOfQuarter(calendar);

    return calendar;
  }

  private Calendar getEndOfMonth(Date date)
  {
    Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    calendar.setTime(date);

    moveToEndOfMonth(calendar);

    return calendar;
  }

  private void moveToEndOfMonth(Calendar calendar)
  {
    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
  }

  private void moveToEndOfQuarter(Calendar calendar)
  {
    int quarter = ( calendar.get(Calendar.MONTH) / 3 ) + 1;
    int month = ( quarter * 3 ) - 1;

    calendar.set(Calendar.MONTH, month);
    moveToEndOfMonth(calendar);
  }

  private void moveToEndOfHalfYear(Calendar calendar)
  {
    int halfYear = ( calendar.get(Calendar.MONTH) / 6 ) + 1;
    int month = ( halfYear * 6 ) - 1;

    calendar.set(Calendar.MONTH, month);
    moveToEndOfMonth(calendar);
  }

  public ChangeFrequency toFrequency()
  {
    if (this.getFrequency().size() > 0)
    {
      return this.getFrequency().get(0);
    }

    return ChangeFrequency.ANNUAL;
  }

  private Pair<Date, Date> getDateRange(final ServerGeoObjectType objectType)
  {
    Pair<Date, Date> range = VertexServerGeoObject.getDataRange(objectType);

    // Only use the publishing start date if there is an actual range of data
    if (this.getPublishingStartDate() != null && range != null)
    {
      return new Pair<Date, Date>(this.getPublishingStartDate(), range.getSecond());
    }
    return range;
  }

  @Override
  @Transaction
  public void createEntries(JsonObject metadata)
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    final ServerGeoObjectType objectType = this.getGeoObjectType();
    Pair<Date, Date> range = this.getDateRange(objectType);

    if (metadata == null)
    {
      List<ListTypeEntry> entries = this.getEntries();

      if (entries.size() > 0)
      {

        ListTypeEntry entry = entries.get(0);
        ListTypeVersion working = entry.getWorking();

        metadata = working.toJSON(false);
      }
    }

    if (range == null && this.getPublishingStartDate() !=  null)
    {
      range = new Pair<Date, Date>(this.getPublishingStartDate(), this.getPublishingStartDate());
    }

    if (range != null)
    {
      Date endDate = range.getSecond();

      if (endDate.after(new Date()))
      {
        endDate = GeoRegistryUtil.getCurrentDate();
      }

      List<Date> dates = this.getFrequencyDates(range.getFirst(), endDate);

      for (Date date : dates)
      {
        this.getOrCreateEntry(date, metadata);
      }
    }
    else
    {
      throw new EmptyListException();
    }    
  }

  @Override
  public JsonObject toJSON(boolean includeEntries)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JsonObject object = super.toJSON(includeEntries);
    object.addProperty(IncrementalListType.FREQUENCY, this.toFrequency().name());
    object.addProperty(IncrementalListType.PUBLISHINGSTARTDATE, format.format(this.getPublishingStartDate()));
    object.addProperty(LIST_TYPE, INCREMENTAL);

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    if (object.has(IncrementalListType.FREQUENCY) && !object.get(IncrementalListType.FREQUENCY).isJsonNull())
    {
      final String frequency = object.get(IncrementalListType.FREQUENCY).getAsString();

      final boolean same = this.getFrequency().stream().anyMatch(f -> {
        return f.name().equals(frequency);
      });

      if (!same)
      {
        this.clearFrequency();
        this.addFrequency(ChangeFrequency.valueOf(frequency));
      }
    }

    if (object.has(IncrementalListType.PUBLISHINGSTARTDATE))
    {
      if (!object.get(IncrementalListType.PUBLISHINGSTARTDATE).isJsonNull())
      {
        String date = object.get(IncrementalListType.PUBLISHINGSTARTDATE).getAsString();

        this.setPublishingStartDate(GeoRegistryUtil.parseDate(date));
      }
      else
      {
        this.setPublishingStartDate(null);
      }
    }
  }

}
