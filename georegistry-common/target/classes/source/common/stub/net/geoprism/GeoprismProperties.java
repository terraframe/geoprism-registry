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
package net.geoprism;

import net.geoprism.configuration.GeoprismConfigGroup;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;


public class GeoprismProperties
{
  private ConfigurationReaderIF props;

  private static class Singleton 
  {
    private static GeoprismProperties INSTANCE = new GeoprismProperties();
  }

  public GeoprismProperties()
  {
    this.props = ConfigurationManager.getReader(GeoprismConfigGroup.COMMON, "geoprism.properties");
  }

  public static String getEmailFrom()
  {
    return Singleton.INSTANCE.props.getString("email.from");
  }

  public static String getEmailTo()
  {
    return Singleton.INSTANCE.props.getString("email.to");
  }

  public static String getEmailUsername()
  {
    return Singleton.INSTANCE.props.getString("email.username");
  }

  public static String getEmailPassword()
  {
    return Singleton.INSTANCE.props.getString("email.password");
  }

  public static String getEmailServer()
  {
    return Singleton.INSTANCE.props.getString("email.server");
  }

  public static Integer getEmailPort()
  {
    return Singleton.INSTANCE.props.getInteger("email.port");
  }

  public static Integer getForgotPasswordExpireTime()
  {
    return Singleton.INSTANCE.props.getInteger("forgotPassword.expireTime");
  }

  public static Boolean getEncrypted()
  {
    return Singleton.INSTANCE.props.getBoolean("email.encrypted");
  }

  public static int getClassifierCacheSize()
  {
    return Singleton.INSTANCE.props.getInteger("classifier.cache.size", 10);
  }

  public static boolean getSolrLookup()
  {
    return Singleton.INSTANCE.props.getBoolean("solr.lookup", false);
  }
}
