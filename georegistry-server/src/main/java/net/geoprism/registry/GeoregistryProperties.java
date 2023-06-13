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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.ConfigurationException;
import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;

public class GeoregistryProperties
{
  private ConfigurationReaderIF props;

  private static class Singleton
  {
    private static GeoregistryProperties INSTANCE = new GeoregistryProperties();
  }
  
  private String mapBounds = null;

  public GeoregistryProperties()
  {
    this.props = ConfigurationManager.getReader(GeoregistryConfigGroup.COMMON, "georegistry.properties");
  }

  public static int getRefreshSessionRecordCount()
  {
    return Singleton.INSTANCE.props.getInteger("import.refreshSessionRecordCount", 10000);
  }

  public static String getGoogleAnalyticsToken()
  {
    String token = Singleton.INSTANCE.props.getString("cgr.google.analytics.token", "");

    return token;
  }

  public static Boolean isBusinessDataEnabled()
  {
    return Singleton.INSTANCE.props.getBoolean("enable.business.data", false);
  }

  public static String getCustomFont()
  {
    String font = Singleton.INSTANCE.props.getString("custom.font", "");

    return font;
  }
  
  public static Boolean isSearchEnabled()
  {
    return Singleton.INSTANCE.props.getBoolean("cgr.search.enabled", true);
  }
  
  public static Boolean isGraphVisualizerEnabled()
  {
    return Singleton.INSTANCE.props.getBoolean("cgr.graph.visualizer.enabled", false);
  }
  
  public static int getMaxNumberOfPoints()
  {
    return Singleton.INSTANCE.props.getInteger("max.geometry.points", 1200000);
  }

  
  public static List<String> getCorsWhitelist()
  {
    String whitelist = Singleton.INSTANCE.props.getString("cgr.cors.whitelist", "");

    if (whitelist == null || whitelist.length() == 0)
    {
      return new ArrayList<String>();
    }

    return Arrays.asList(whitelist.split(","));
  }
  
  /**
   * Can be used to provide the default map bounds for all map activities such as the location manager.
   * This parameter should be specified as a <a href="https://docs.mapbox.com/mapbox-gl-js/api/geography/#lnglatboundslike">LngLatBoundsLike</a>
   * as it will be fed directly to the bounds parameter of the <a href="https://docs.mapbox.com/mapbox-gl-js/api/map/">Mapbox constructor</a>.
   * 
   * @see <a href="https://docs.mapbox.com/playground/geocoding/">Create your own bounds here</a>
   */
  public synchronized static String getDefaultMapBounds()
  {
    if (Singleton.INSTANCE.mapBounds == null)
    {
      String sBounds = Singleton.INSTANCE.props.getString("cgr.map.bounds", "[[-108.9497822,40.919108654],[-102.02574403,37.14964020]]"); // Default is Colorado, the headquarters of TerraFrame!
      
      JsonArray jaBounds = JsonParser.parseString(sBounds).getAsJsonArray();
      
      if (jaBounds.size() != 2)
      {
        throw new ConfigurationException("cgr.map.bounds must contain an array of two arrays.");
      }
      
      for (int i = 0; i < jaBounds.size(); ++i)
      {
        JsonArray lngLatLike = jaBounds.get(i).getAsJsonArray();
        
        Float lat = Float.parseFloat(lngLatLike.get(1).getAsString().trim());
        Float lng = Float.parseFloat(lngLatLike.get(0).getAsString().trim());
        
        if (lat > 90 || lat < -90)
        {
          throw new ConfigurationException("Latitude must be greater than -90 and less than 90.");
        }
        else if (lng > 180 || lng < -180)
        {
          throw new ConfigurationException("Longitude be greater than -180 and less than 180.");
        }
      }
      
      Singleton.INSTANCE.mapBounds = sBounds;
    }
    
    return Singleton.INSTANCE.mapBounds;
  }

}
