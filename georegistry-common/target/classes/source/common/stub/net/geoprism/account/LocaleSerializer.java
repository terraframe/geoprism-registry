/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.account;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocaleSerializer
{
  public static String serialize(Locale... locales)
  {
    try
    {
      JSONArray array = new JSONArray();

      for (Locale locale : locales)
      {
        JSONObject object = new JSONObject();
        object.put("language", locale.getLanguage());
        object.put("country", locale.getCountry());
        object.put("variant", locale.getVariant());

        array.put(object);
      }

      return array.toString();
    }
    catch (JSONException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static Locale[] deserialize(String serialized)
  {
    try
    {
      JSONArray array = new JSONArray(serialized);

      Locale[] locales = new Locale[array.length()];

      for (int i = 0; i < array.length(); i++)
      {
        JSONObject object = array.getJSONObject(i);

        String language = object.getString("language");
        String country = object.getString("country");
        String variant = object.getString("variant");

        locales[i] = new Locale(language, country, variant);
      }

      return locales;
    }
    catch (JSONException e)
    {
      throw new RuntimeException(e);
    }
  }
}
