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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.util.FileIO;

public class SystemLogoSingletonDTO extends SystemLogoSingletonDTOBase 
{
  private static final long   serialVersionUID = -1855290440;

  private static File         bannerCache      = null;

  private static File         miniLogoCache    = null;

  private static final String IMAGES_TEMP_DIR  = "uploaded_images";

  public static final String getImagesTempDir(HttpServletRequest request)
  {
    if (request != null)
    {
      return request.getContextPath() + "/" + IMAGES_TEMP_DIR + "/";
    }

    return getImagesTempDir();
  }

  public static final String getImagesTempDir()
  {
    return LocalProperties.getJspDir() + "/../" + IMAGES_TEMP_DIR + "/";
  }

  final static Logger logger = LoggerFactory.getLogger(SystemLogoSingletonDTO.class);

  public SystemLogoSingletonDTO(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }

  /**
   * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
   * 
   * @param businessDTO
   *          The BusinessDTO to duplicate
   * @param clientRequest
   *          The clientRequest this DTO should use to communicate with the server.
   */
  protected SystemLogoSingletonDTO(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }

  /**
   * Uploads a banner file to the server for persistance. Calling this method will also populate the client-side cache
   * for future calls to getBannerFilePath.
   * 
   * @param clientRequest
   * @param fileStream
   * @param fileName
   */
  public static void uploadBannerAndCache(com.runwaysdk.constants.ClientRequestIF clientRequest, java.io.InputStream fileStream, java.lang.String fileName)
  {
    String tempDir = LocalProperties.getJspDir() + "/../uploaded_images";
    new File(tempDir).mkdir();
    bannerCache = new File(tempDir, fileName);

    try
    {
      // Write the file locally to our cache
      FileOutputStream fos = new FileOutputStream(bannerCache);
      BufferedOutputStream buffer = new BufferedOutputStream(fos);
      IOUtils.copy(fileStream, buffer);
      buffer.close();
      fos.close();

      // Send the cache file to the server for vault persistance.
      FileInputStream serverInput = new FileInputStream(bannerCache);
      SystemLogoSingletonDTOBase.uploadBanner(clientRequest, serverInput, fileName);
      serverInput.close();
    }
    catch (IOException e)
    {
      logger.error("Error creating image file [" + fileName + "].", e);
      return;
    }
  }

  /**
   * Uploads a mini logo file to the server for persistance. Calling this method will also populate the client-side
   * cache for future calls to getMiniLogoFilePath.
   * 
   * @param clientRequest
   * @param fileStream
   * @param fileName
   */
  public static void uploadMiniLogoAndCache(com.runwaysdk.constants.ClientRequestIF clientRequest, java.io.InputStream fileStream, java.lang.String fileName)
  {
    String tempDir = LocalProperties.getJspDir() + "/../uploaded_images";
    new File(tempDir).mkdir();
    miniLogoCache = new File(tempDir, fileName);

    try
    {
      // Write the file locally to our cache
      FileOutputStream fos = new FileOutputStream(miniLogoCache);
      BufferedOutputStream buffer = new BufferedOutputStream(fos);
      IOUtils.copy(fileStream, buffer);
      buffer.close();
      fos.close();

      // Send the cache file to the server for vault persistance.
      FileInputStream serverInput = new FileInputStream(miniLogoCache);
      SystemLogoSingletonDTOBase.uploadMiniLogo(clientRequest, serverInput, fileName);
      serverInput.close();
    }
    catch (IOException e)
    {
      logger.error("Error creating image file [" + fileName + "].", e);
      return;
    }
  }

  public static synchronized void removeBannerFileFromCache(ClientRequestIF clientRequest, HttpServletRequest request)
  {
    if (bannerCache != null)
    {
      String path = getImagesTempDir(request) + bannerCache.getName();

      FileUtils.deleteQuietly(new File(path));

      bannerCache = null;
    }

    SystemLogoSingletonDTOBase.removeBanner(clientRequest);
  }

  public static synchronized void removeMiniLogoFileFromCache(ClientRequestIF clientRequest, HttpServletRequest request)
  {
    if (miniLogoCache != null)
    {
      String path = getImagesTempDir(request) + miniLogoCache.getName();

      FileUtils.deleteQuietly(new File(path));

      miniLogoCache = null;
    }

    SystemLogoSingletonDTOBase.removeMiniLogo(clientRequest);
  }

  /**
   * Calling this method will give you an img src path ready for use in html that will contain the uploaded logo, if one
   * has been uploaded. If the file does exist, it will be cached client-side. Subsequent calls will return the
   * client-side cached file. This file may be deleted and refetched from the server again at any point. If no banner
   * has been uploaded this method will return null.
   * 
   * TODO: The cache needs to be cleared on an interval (say every 6 hours or so) if there are multiple client machines
   * because uploading a logo from one client will not populate on the other client until the cache is cleared.
   * 
   * @param clientRequest
   * @return a file or null
   */
  public static String getBannerFileFromCache(ClientRequestIF clientRequest, HttpServletRequest request)
  {
    if (bannerCache != null)
    {
      return getImagesTempDir(request) + bannerCache.getName();
    }

    InputStream stream = SystemLogoSingletonDTOBase.getBannerFile(clientRequest);
    if (stream == null)
    {
      return null;
    }

    String fileName = SystemLogoSingletonDTOBase.getBannerFilename(clientRequest);
    if (fileName == null)
    {
      return null;
    }

    // Write the file to our temp dir
    String tempDir = LocalProperties.getJspDir() + "/../" + IMAGES_TEMP_DIR;
    new File(tempDir).mkdir();
    bannerCache = new File(tempDir, fileName);

    FileOutputStream fos;
    try
    {
      fos = new FileOutputStream(bannerCache);
      BufferedOutputStream buffer = new BufferedOutputStream(fos);
      FileIO.write(buffer, stream);
    }
    catch (IOException e)
    {
      logger.error("Error creating image file [" + fileName + "].", e);
      return null;
    }

    return getImagesTempDir(request) + bannerCache.getName();
  }

  /**
   * Calling this method will give you an img src path ready for use in html that will contain the uploaded logo, if one
   * has been uploaded. If the file does exist, it will be cached client-side. Subsequent calls will return the
   * client-side cached file. This file may be deleted and refetched from the server again at any point. If no logo has
   * been uploaded this method will return null.
   * 
   * TODO: The cache needs to be cleared on an interval (say every 6 hours or so) if there are multiple client machines
   * because uploading a logo from one client will not populate on the other client until the cache is cleared.
   * 
   * @param clientRequest
   * @return a file path or null
   */
  public static String getMiniLogoFileFromCache(ClientRequestIF clientRequest, HttpServletRequest request)
  {
    if (miniLogoCache != null)
    {
      return getImagesTempDir(request) + miniLogoCache.getName();
    }

    InputStream stream = SystemLogoSingletonDTOBase.getMiniLogoFile(clientRequest);
    if (stream == null)
    {
      return null;
    }

    String fileName = SystemLogoSingletonDTOBase.getMiniLogoFilename(clientRequest);
    if (fileName == null)
    {
      return null;
    }

    // Write the file to our temp dir
    String tempDir = LocalProperties.getJspDir() + "/../uploaded_images";
    new File(tempDir).mkdir();
    miniLogoCache = new File(tempDir, fileName);

    FileOutputStream fos;
    try
    {
      fos = new FileOutputStream(miniLogoCache);
      BufferedOutputStream buffer = new BufferedOutputStream(fos);
      FileIO.write(buffer, stream);
    }
    catch (IOException e)
    {
      logger.error("Error creating image file [" + fileName + "].", e);
      return null;
    }

    return getImagesTempDir(request) + miniLogoCache.getName();
  }
}