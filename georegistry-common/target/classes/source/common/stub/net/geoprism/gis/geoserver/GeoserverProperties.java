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
package net.geoprism.gis.geoserver;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.LogFactory;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;
import com.runwaysdk.configuration.RunwayConfigurationException;


import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStoreManager;
import net.geoprism.configuration.GeoprismConfigGroup;

public class GeoserverProperties 
{
  /**
   * The server.properties configuration file
   */
  private ConfigurationReaderIF            props;

  private static GeoServerRESTPublisher    publisher;

  private static GeoServerRESTReader       restReader;

  private static GeoServerRESTStoreManager manager;

  public static final String               SLD_EXTENSION = ".sld";

  private GeoserverProperties()
  {
    this.props = ConfigurationManager.getReader(GeoprismConfigGroup.COMMON, "geoserver.properties");
  }

  private static class Singleton 
  {
    private static GeoserverProperties INSTANCE = new GeoserverProperties();

    private static GeoserverProperties getInstance()
    {
      // INSTANCE will only ever be null if there is a problem. The if check is to allow for debugging.
      if (INSTANCE == null)
      {
        INSTANCE = new GeoserverProperties();
      }

      return INSTANCE;
    }

    private static ConfigurationReaderIF getProps()
    {
      return getInstance().props;
    }
  }

  public static Integer getDecimalLength()
  {
    return Singleton.getProps().getInteger("geoserver.decimal.length");
  }

  public static Integer getDecimalPrecision()
  {
    return Singleton.getProps().getInteger("geoserver.decimal.precision");
  }

  public static String getWorkspace()
  {
    return Singleton.getProps().getString("geoserver.workspace");
  }

  public static Integer getSessionMapLimit()
  {
    return Singleton.getProps().getInteger("geoserver.session.map.limit");
  }

  public static Integer getSavedMapLimit()
  {
    return Singleton.getProps().getInteger("geoserver.saved.map.limit");
  }

  public static String getStore()
  {
    return Singleton.getProps().getString("geoserver.store");
  }

  public static String getAdminUser()
  {
    return Singleton.getProps().getString("admin.user");
  }

  public static String getAdminPassword()
  {
    return Singleton.getProps().getString("admin.password");
  }

  public static String getRemotePath()
  {
    return Singleton.getProps().getString("geoserver.remote.path");
  }

  public static String getLocalPath()
  {
    return Singleton.getProps().getString("geoserver.local.path");
  }

  public static String getGeoserverSLDDir()
  {
    return Singleton.getProps().getString("geoserver.sld.dir");
  }

  public static String getGeoserverGWCDir()
  {
    return Singleton.getProps().getString("geoserver.gwc.dir");
  }

  /**
   * Labeling VendorOption group http://docs.geoserver.org/latest/en/user/styling/sld-reference/labeling.html
   */
  public static String getLabelGroup()
  {
    return Singleton.getProps().getString("geoserver.labeling.group");
  }

  /**
   * Labeling VendorOption conflict-resolution
   * http://docs.geoserver.org/latest/en/user/styling/sld-reference/labeling.html
   */
  public static String getLabelConflictResolution()
  {
    return Singleton.getProps().getString("geoserver.labeling.conflict-resolution");
  }

  /**
   * Labeling VendorOption spaceAround http://docs.geoserver.org/latest/en/user/styling/sld-reference/labeling.html
   */
  public static String getLabelSpaceAround()
  {
    return Singleton.getProps().getString("geoserver.labeling.spaceAround");
  }

  /**
   * Labeling VendorOption goodnessOfFit http://docs.geoserver.org/latest/en/user/styling/sld-reference/labeling.html
   */
  public static String getLabelGoodnessOfFit()
  {
    return Singleton.getProps().getString("geoserver.labeling.goodnessOfFit");
  }

  /**
   * Labeling VendorOption autoWrap http://docs.geoserver.org/latest/en/user/styling/sld-reference/labeling.html
   */
  public static String getLabelAutoWrap()
  {
    return Singleton.getProps().getString("geoserver.labeling.autoWrap");
  }

  /**
   * Returns the Geoserver REST publisher.
   * 
   * @return
   */
  public static synchronized GeoServerRESTPublisher getPublisher()
  {
    if (publisher == null)
    {
      publisher = new GeoServerRESTPublisher(getLocalPath(), getAdminUser(), getAdminPassword());
    }

    return publisher;
  }

  /**
   * Returns the Geoserver REST publisher.
   * 
   * @return
   */
  public static synchronized GeoServerRESTStoreManager getManager()
  {
    if (manager == null)
    {
      try
      {
        URL restURL = new URL(getLocalPath());

        manager = new GeoServerRESTStoreManager(restURL, getAdminUser(), getAdminPassword());
      }
      catch (MalformedURLException e)
      {
        throw new RuntimeException(e);
      }
    }

    return manager;
  }

  /**
   * Returns the Geoserver REST reader.
   */
  public static synchronized GeoServerRESTReader getReader()
  {
    if (restReader == null)
    {
      try
      {
        restReader = new GeoServerRESTReader(getLocalPath(), getAdminUser(), getAdminPassword());
      }
      catch (MalformedURLException e)
      {
        // We don't know if this is being called via client or server code, so log
        // the error and throw an exception to the calling code for its error handling mechanism.
        String msg = "The " + GeoserverProperties.class.getSimpleName() + "." + GeoServerRESTReader.class.getSimpleName() + " is null.";
        LogFactory.getLog(GeoserverProperties.class.getClass()).error(msg, e);

        throw new RunwayConfigurationException(msg);
      }
    }

    return restReader;
  }

  /**
   * System keystore path
   *
   */
  public static String getGeoserverKeystorePath()
  {
    return Singleton.getProps().getString("geoserver.keystore.path");
  }

  /**
   * System keystore pass
   *
   */
  public static String getGeoserverKeystorePass()
  {
    return Singleton.getProps().getString("geoserver.keystore.pass");
  }
  
  /**
   * MapboxGL access token
   */
  public static String getMapboxglAccessToken()
  {
    return Singleton.getProps().getString("mapboxgl.accessToken");
  }
}
