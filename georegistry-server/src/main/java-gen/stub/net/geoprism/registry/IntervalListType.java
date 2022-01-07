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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;

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
  public JsonObject toJSON(boolean includeEntries)
  {
    JsonObject object = super.toJSON(includeEntries);
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
  public void createEntries()
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.getIntervals().forEach((startDate, endDate) -> {
      this.getOrCreateEntry(endDate);
    });
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
