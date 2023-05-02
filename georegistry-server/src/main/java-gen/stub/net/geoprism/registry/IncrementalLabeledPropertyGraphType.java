package net.geoprism.registry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.transaction.Transaction;

public class IncrementalLabeledPropertyGraphType extends IncrementalLabeledPropertyGraphTypeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 794228873;
  
  public IncrementalLabeledPropertyGraphType()
  {
    super();
  }
  @Override
  protected JsonObject formatVersionLabel(LabeledVersion version)
  {
    JsonObject object = new JsonObject();

//    List<ChangeFrequency> frequency = this.getFrequency();
//
//    if (frequency.contains(ChangeFrequency.ANNUAL))
//    {
//      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//      calendar.setTime(version.getForDate());
//
//      object.addProperty("type", "text");
//      object.addProperty("value", Integer.toString(calendar.get(Calendar.YEAR)));
//    }
//    else if (frequency.contains(ChangeFrequency.BIANNUAL))
//    {
//      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//      calendar.setTime(version.getForDate());
//
//      int halfYear = ( calendar.get(Calendar.MONTH) / 6 ) + 1;
//
//      object.addProperty("type", "text");
//      object.addProperty("value", "H" + halfYear + " " + Integer.toString(calendar.get(Calendar.YEAR)));
//    }
//    else if (frequency.contains(ChangeFrequency.QUARTER))
//    {
//      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//      calendar.setTime(version.getForDate());
//
//      int quarter = ( calendar.get(Calendar.MONTH) / 3 ) + 1;
//
//      object.addProperty("type", "text");
//      object.addProperty("value", "Q" + quarter + " " + Integer.toString(calendar.get(Calendar.YEAR)));
//    }
//    else if (frequency.contains(ChangeFrequency.MONTHLY))
//    {
//      Calendar calendar = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
//      calendar.setTime(version.getForDate());
//      calendar.set(Calendar.DAY_OF_MONTH, 1);
//
//      Date startOfWeek = calendar.getTime();
//
//      calendar.add(Calendar.MONTH, 1);
//      calendar.add(Calendar.DAY_OF_YEAR, -1);
//
//      Date endOfWeek = calendar.getTime();
//
//      JsonObject range = new JsonObject();
//      range.addProperty("startDate", GeoRegistryUtil.formatDate(startOfWeek, false));
//      range.addProperty("endDate", GeoRegistryUtil.formatDate(endOfWeek, false));
//
//      object.addProperty("type", "range");
//      object.add("value", range);
//    }
//    else
//    {
      object.addProperty("type", "date");
      object.addProperty("value", GeoRegistryUtil.formatDate(version.getForDate(), false));
//    }

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

  public ChangeFrequency toFrequency()
  {
    if (this.getFrequency().size() > 0)
    {
      return this.getFrequency().get(0);
    }

    return ChangeFrequency.ANNUAL;
  }

  private Pair<Date, Date> getDateRange()
  {
//    Pair<Date, Date> range = VertexServerGeoObject.getDataRange(objectType);
//
//    // Only use the publishing start date if there is an actual range of data
//    if (this.getPublishingStartDate() != null && range != null)
//    {
//      return new Pair<Date, Date>(this.getPublishingStartDate(), range.getSecond());
//    }
//    return range;
    
    return new Pair<Date, Date>(this.getPublishingStartDate(), this.getPublishingStartDate());

  }

  @Override
  @Transaction
  public void createEntries(JsonObject metadata)
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    Pair<Date, Date> range = this.getDateRange();

    if (metadata == null)
    {
      List<LabeledPropertyGraphTypeEntry> entries = this.getEntries();

      if (entries.size() > 0)
      {

        LabeledPropertyGraphTypeEntry entry = entries.get(0);
        LabeledPropertyGraphTypeVersion working = entry.getWorking();

        metadata = working.toJSON(false);
      }
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
    object.addProperty(IncrementalLabeledPropertyGraphType.FREQUENCY, this.toFrequency().name());
    object.addProperty(IncrementalLabeledPropertyGraphType.PUBLISHINGSTARTDATE, format.format(this.getPublishingStartDate()));
    object.addProperty(GRAPH_TYPE, INCREMENTAL);

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    if (object.has(IncrementalLabeledPropertyGraphType.FREQUENCY) && !object.get(IncrementalLabeledPropertyGraphType.FREQUENCY).isJsonNull())
    {
      final String frequency = object.get(IncrementalLabeledPropertyGraphType.FREQUENCY).getAsString();

      final boolean same = this.getFrequency().stream().anyMatch(f -> {
        return f.name().equals(frequency);
      });

      if (!same)
      {
        this.clearFrequency();
        this.addFrequency(ChangeFrequency.valueOf(frequency));
      }
    }

    if (object.has(IncrementalLabeledPropertyGraphType.PUBLISHINGSTARTDATE))
    {
      if (!object.get(IncrementalLabeledPropertyGraphType.PUBLISHINGSTARTDATE).isJsonNull())
      {
        String date = object.get(IncrementalLabeledPropertyGraphType.PUBLISHINGSTARTDATE).getAsString();

        this.setPublishingStartDate(GeoRegistryUtil.parseDate(date));
      }
      else
      {
        this.setPublishingStartDate(null);
      }
    }
  }


}
