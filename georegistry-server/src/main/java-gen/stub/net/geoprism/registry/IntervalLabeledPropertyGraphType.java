package net.geoprism.registry;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.Pair;
import com.runwaysdk.dataaccess.transaction.Transaction;

public class IntervalLabeledPropertyGraphType extends IntervalLabeledPropertyGraphTypeBase
{
  @SuppressWarnings("unused")
  private static final long  serialVersionUID = -223471707;

  public static final String START_DATE       = "startDate";

  public static final String END_DATE         = "endDate";

  public IntervalLabeledPropertyGraphType()
  {
    super();
  }

  @Override
  public JsonObject toJSON(boolean includeEntries)
  {
    JsonObject object = super.toJSON(includeEntries);
    object.addProperty(GRAPH_TYPE, INTERVAL);
    object.add(INTERVALJSON, JsonParser.parseString(this.getIntervalJson()));

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    this.setIntervalJson(object.get(INTERVALJSON).getAsJsonArray().toString());
  }

  public List<Pair<Date, Date>> getIntervals()
  {
    List<Pair<Date, Date>> list = new LinkedList<Pair<Date, Date>>();

    JsonArray intervals = JsonParser.parseString(this.getIntervalJson()).getAsJsonArray();

    for (int i = 0; i < intervals.size(); i++)
    {
      JsonObject interval = intervals.get(i).getAsJsonObject();
      Date startDate = GeoRegistryUtil.parseDate(interval.get(START_DATE).getAsString());
      Date endDate = GeoRegistryUtil.parseDate(interval.get(END_DATE).getAsString());

      list.add(new Pair<Date, Date>(startDate, endDate));
    }

    list.sort(new Comparator<Pair<Date, Date>>()
    {
      @Override
      public int compare(Pair<Date, Date> o1, Pair<Date, Date> o2)
      {
        int compareTo = o1.getFirst().compareTo(o2.getFirst());

        if (compareTo == 0)
        {
          return o1.getSecond().compareTo(o2.getSecond());
        }

        return compareTo;
      }

    });

    return list;
  }

  @Override
  protected JsonObject formatVersionLabel(LabeledVersion version)
  {
    Date versionDate = version.getForDate();
    List<Pair<Date, Date>> intervals = this.getIntervals();

    for (Pair<Date, Date> interval : intervals)
    {
      Date startDate = interval.getFirst();
      Date endDate = interval.getSecond();

      if (GeoRegistryUtil.isBetweenInclusive(versionDate, startDate, endDate))
      {
        JsonObject range = new JsonObject();
        range.addProperty("startDate", GeoRegistryUtil.formatDate(startDate, false));
        range.addProperty("endDate", GeoRegistryUtil.formatDate(endDate, false));

        JsonObject object = new JsonObject();
        object.addProperty("type", "range");
        object.add("value", range);

        return object;
      }
    }

    throw new UnsupportedOperationException();
  }

  @Override
  @Transaction
  public void createEntries(JsonObject metadata)
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

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

    final JsonObject object = metadata;

    this.getIntervals().forEach((pair) -> {
      Date startDate = pair.getFirst();

      this.getOrCreateEntry(startDate, object);
    });
  }

  @Override
  public void apply()
  {
    List<Pair<Date, Date>> intervals = this.getIntervals();

    Iterator<Pair<Date, Date>> iterator = intervals.iterator();

    Pair<Date, Date> prev = null;

    while (iterator.hasNext())
    {
      Pair<Date, Date> pair = iterator.next();

      if (prev != null && ( prev.getSecond().after(pair.getFirst()) || prev.getSecond().equals(pair.getFirst()) ))
      {
        throw new UnsupportedOperationException("No overlaps");
      }

      prev = pair;
    }

    super.apply();
  }

}
