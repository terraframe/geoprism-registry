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

import org.jaitools.jts.CoordinateSequence2D;

import com.google.gson.JsonArray;
import com.runwaysdk.ConfigurationException;
import com.runwaysdk.Pair;
import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;


public class GeoregistryProperties
{
  private ConfigurationReaderIF props;

  private static class Singleton 
  {
    private static GeoregistryProperties INSTANCE = new GeoregistryProperties();
  }
  
  private Pair<Float, Float> mapCenter = null;

  public GeoregistryProperties()
  {
    this.props = ConfigurationManager.getReader(GeoregistryConfigGroup.COMMON, "georegistry.properties");
  }

  public static int getRefreshSessionRecordCount()
  {
    return Singleton.INSTANCE.props.getInteger("import.refreshSessionRecordCount", 10000);
  }
  
  public static String getRemoteServerUrl()
  {
    String url = Singleton.INSTANCE.props.getString("cgr.remote.url", "https://localhost:8443/georegistry/");
    
    if (!url.endsWith("/"))
    {
      url = url + "/";
    }
    
    return url;
  }
  
  public static String getGoogleAnalyticsToken()
  {
    String token = Singleton.INSTANCE.props.getString("cgr.google.analytics.token", "");
    
    return token;
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
   * Can be used to provide the default map center for all map activities such as the location manager.
   * This parameter should be specified in the format of lat (<float>), long (<float>). Any additional
   * spaces (for example after the comma or at the end) will be trimmed. The point is assumed to be in
   * EPSG:3857 projection.
   * 
   * @return A Pair where first is lat and second is long.
   */
  public synchronized static Pair<Float, Float> getMapCenter()
  {
    if (Singleton.INSTANCE.mapCenter == null)
    {
      String sCenter = Singleton.INSTANCE.props.getString("cgr.map.center", "39.5501,-105.7821"); // Default is Denver, CO the headquarters of TerraFrame!
      
      String[] aCenter = sCenter.split(",");
      
      if (aCenter.length != 2)
      {
        throw new ConfigurationException("cgr.map.center must contain an array of two floats.");
      }
      
      Float lat = Float.parseFloat(aCenter[0].trim());
      Float lng = Float.parseFloat(aCenter[1].trim());
      
      if (lat > 90 || lat < -90)
      {
        throw new ConfigurationException("Latitude must be greater than -90 and less than 90.");
      }
      else if (lng > 180 || lng < -180)
      {
        throw new ConfigurationException("Longitude be greater than -180 and less than 180.");
      }
      
      Singleton.INSTANCE.mapCenter = new Pair<Float, Float>(lat, lng);
    }
    
    return Singleton.INSTANCE.mapCenter;
  }
  
  /**
   * Returns the map center as a serialized array where [long, lat].
   * 
   * @see https://docs.mapbox.com/mapbox-gl-js/api/geography/#lnglat
   * @see https://datatracker.ietf.org/doc/html/rfc7946#section-4
   */
  public static JsonArray getMapCenterAsJsonLngLat()
  {
    Pair<Float, Float> center = getMapCenter();
  
    JsonArray ja = new JsonArray();
    
    ja.add(center.getSecond());
    ja.add(center.getFirst());
    
    return ja;
  }
}
