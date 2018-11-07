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
package net.geoprism.configuration;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.configuration.CommonsConfigurationResolver;
import com.runwaysdk.configuration.ConfigurationManager.ConfigGroupIF;
import com.runwaysdk.constants.DeployProperties;
import com.runwaysdk.configuration.RunwayConfigurationException;

/**
 * Using IOC, extends Runway's configuration resolving mechanism to allow support for resolving configuration files external to the deployable artifact.
 * 
 * @author Richard Rowlands
 */
public class GeoprismConfigurationResolver extends CommonsConfigurationResolver
{
  private static Logger logger = LoggerFactory.getLogger(GeoprismConfigurationResolver.class);
  
  private File externalConfigDir;

  public GeoprismConfigurationResolver()
  {
    String sConfigDir = System.getProperty("geoprism.config.dir");
    
    if (sConfigDir != null)
    {
      File deployedPath = this.getDeployedPath();
      String appName = deployedPath.getName();
      
      externalConfigDir = new File(sConfigDir, appName);
      
      // No funny business!
      if (!externalConfigDir.exists() || !externalConfigDir.isDirectory())
      {
        logger.error("geoprism.config.dir was specified as [" + externalConfigDir.getAbsolutePath() + "] but that directory does not exist. Using default resource loader strategy.");
        externalConfigDir = null;
      }
    }
    
    if (externalConfigDir == null)
    {
      logger.info("Geoprism external config dir not set. Using default resource loader strategy.");
    }
    else
    {
      logger.info("Geoprism external config set to [" + externalConfigDir.getAbsolutePath() + "].");
    }
  }
  
  public void setExternalConfigDir(File externalConfigDir)
  {
    // No funny business!
    if (!externalConfigDir.exists() || !externalConfigDir.isDirectory())
    {
      logger.error("Geoprism external config dir was manuallly specified as [" + externalConfigDir.getAbsolutePath() + "] but that directory does not exist. Ignoring the command.");
    }
    else
    {
      this.externalConfigDir = externalConfigDir;
      logger.info("Geoprism external config dir has been manually set to [" + externalConfigDir + "].");
    }
  }

  @Override
  public URL getResource(ConfigGroupIF location, String name)
  {
    URL urlBase = null;
    RunwayConfigurationException ex = null;
    try
    {
      urlBase = super.getResource(location, name);
    }
    catch (RunwayConfigurationException e)
    {
      ex = e;
    }
    
    if (externalConfigDir != null)
    {
      File fOverride = new File(externalConfigDir, location.getPath() + name);
      
      if (fOverride.exists())
      {
        try
        {
          if (urlBase == null)
          {
            return fOverride.toURI().toURL();
          }
          
          MergeUtility merger = new MergeUtility();
          InputStream urlBaseStream = urlBase.openStream();
          InputStream baseStream = urlBaseStream;
          FileInputStream overrideStream = new FileInputStream(fOverride);
          
          try
          {
            // Resolve includes
//            Properties baseProps = new Properties();
//            baseProps.load(baseStream);
//            
//            Iterator<Object> i = baseProps.keySet().iterator();
//            while (i.hasNext())
//            {
//              String key = (String) i.next();
//              
//              String value = baseProps.getProperty(key);
//              if (key.equals("include") && !value.equals("$REMOVE$"))
//              {
//                ByteArrayOutputStream noInclude = new ByteArrayOutputStream();
//                baseProps.remove(key);
//                baseProps.store(noInclude, null);
//                baseStream = new ByteArrayInputStream(noInclude.toByteArray());
//                
//                ByteArrayOutputStream resolvedInclude = new ByteArrayOutputStream();
//                URL includeURL = this.getResource(GeoprismConfigGroup.ROOT, location.getPath() + value);
//                merger.mergeStreams(includeURL.openStream(), baseStream, resolvedInclude, FilenameUtils.getExtension(fOverride.getAbsolutePath()));
//                baseStream = new ByteArrayInputStream(resolvedInclude.toByteArray());
//              }
//            }
          
            // Override
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            merger.mergeStreams(baseStream, overrideStream, os, FilenameUtils.getExtension(fOverride.getAbsolutePath()));
            
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            
            return new URL(null, "inputstream://" + name, new InputStreamURLStreamHandler(is));
          }
          finally
          {
            // Byte array streams don't need to be closed, but our original streams do.
            urlBaseStream.close();
            overrideStream.close();
          }
        }
        catch (IOException e)
        {
          logger.error("Unexpected error.", e);
        }
      }
    }
    
    if (urlBase == null && ex != null)
    {
      throw ex;
    }
    
    return urlBase;
  }

  private class InputStreamURLStreamHandler extends URLStreamHandler {
    InputStream is;
    
    public InputStreamURLStreamHandler(InputStream is)
    {
      this.is = is;
    }
    
    @Override
    protected URLConnection openConnection(URL u) throws IOException
    {
      return new InputStreamURLConnection(is, u);
    }
  }

  private class InputStreamURLConnection extends URLConnection
  {
    InputStream is;
    
    public InputStreamURLConnection(InputStream is, URL url)
    {
      super(url);
      
      this.is = is;
    }

    @Override
    public void connect() throws IOException
    {
      
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
      return is;
    }
  }
}
