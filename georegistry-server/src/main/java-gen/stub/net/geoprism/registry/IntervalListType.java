package net.geoprism.registry;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;

import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class IntervalListType extends IntervalListTypeBase
{
  private static final long serialVersionUID = 1083396269;

  public static String      START_DATE       = "startDate";

  public static String      END_DATE         = "endDate";

  public IntervalListType()
  {
    super();
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = super.toJSON();
    object.addProperty(LIST_TYPE, INTERVAL);
    object.add(INTERVALJSON, JsonParser.parseString(this.getIntervalJson()));

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    this.setIntervalJson(object.get(INTERVALJSON).getAsJsonArray().toString());
  }

  public LinkedHashMap<Date, Date> getIntervals()
  {
    LinkedHashMap<Date, Date> map = new LinkedHashMap<Date, Date>();
    JsonArray intervals = JsonParser.parseString(this.getIntervalJson()).getAsJsonArray();

    for (int i = 0; i < intervals.size(); i++)
    {
      JsonObject interval = intervals.get(i).getAsJsonObject();
      Date startDate = GeoRegistryUtil.parseDate(interval.get(START_DATE).getAsString());
      Date endDate = GeoRegistryUtil.parseDate(interval.get(END_DATE).getAsString());

      map.put(startDate, endDate);
    }

    return map;
  }

  @Override
  protected String formatVersionLabel(LabeledVersion version)
  {
    Date versionDate = version.getForDate();
    LinkedHashMap<Date, Date> intervals = this.getIntervals();
    Set<Entry<Date, Date>> entries = intervals.entrySet();

    for (Entry<Date, Date> entry : entries)
    {
      Date startDate = entry.getKey();
      Date endDate = entry.getValue();

      if (GeoRegistryUtil.isBetweenInclusive(versionDate, startDate, endDate))
      {
        return GeoRegistryUtil.formatDate(startDate, false) + " - " + GeoRegistryUtil.formatDate(endDate, false);
      }
    }

    throw new UnsupportedOperationException();
  }

  @Override
  @Transaction
  public void publishVersions()
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.PUBLISH_JOB_CHANGE, null));

    try
    {
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

      LinkedHashMap<Date, Date> intervals = this.getIntervals();

      intervals.forEach((startDate, endDate) -> {
        ListTypeEntry entry = this.getOrCreateEntry(endDate);
        ( (Session) Session.getCurrentSession() ).reloadPermissions();

        entry.publish();
      });

    }
    finally
    {
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }
  }

  @Override
  public void apply()
  {
    /*
     * Changing the interval requires that any existing published version be
     * deleted
     */
    if (!this.isNew() && this.isModified(IntervalListType.INTERVALJSON))
    {
      final List<ListTypeEntry> entries = this.getEntries();

      for (ListTypeEntry entry : entries)
      {
        entry.delete();
      }
    }

    super.apply();
  }
}
