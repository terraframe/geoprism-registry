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
package net.geoprism.registry.service.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.runwaysdk.Pair;
import com.runwaysdk.constants.DeployProperties;
import com.runwaysdk.dataaccess.io.FileReadException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.vault.VaultFileDAO;
import com.runwaysdk.vault.VaultFileDAOIF;

import net.geoprism.SystemLogoSingleton;

@Service
public class AssetService
{
  private static Logger logger = LoggerFactory.getLogger(AssetService.class);

  @Request(RequestType.SESSION)
  public void uploadBannerAndCache(String sessionId, InputStream fileStream, String fileName)
  {
    SystemLogoSingleton.uploadBanner(fileStream, fileName);
  }

  @Request(RequestType.SESSION)
  public void uploadMiniLogoAndCache(String sessionId, InputStream fileStream, String fileName)
  {
    SystemLogoSingleton.uploadMiniLogo(fileStream, fileName);
  }

  @Request(RequestType.SESSION)
  public Pair<String, InputStream> getBannerFileFromCache(String sessionId)
  {
    try
    {
      SystemLogoSingleton instance = SystemLogoSingleton.getInstance();

      if (instance == null || StringUtils.isEmpty(instance.getBannerVaultId()))
      {
        File file = new File(DeployProperties.getDeployPath() + "/assets/splash_logo_icon.png");

        return new Pair<String, InputStream>(file.getName(), new FileInputStream(file));
      }

      try
      {
        VaultFileDAOIF file = VaultFileDAO.get(instance.getBannerVaultId());

        return new Pair<String, InputStream>(file.getExtension() + "." + file.getExtension(), file.getFileStream());
      }
      catch (FileReadException e)
      {
        logger.error("Unable to retrieve banner file", e);

        File file = new File(DeployProperties.getDeployPath() + "/assets/splash_logo_icon.png");

        return new Pair<String, InputStream>(file.getName(), new FileInputStream(file));
      }
    }
    catch (FileNotFoundException e)
    {
      logger.error("Unable to retrieve banner file", e);

      return null;
    }

  }

  @Request(RequestType.SESSION)
  public Pair<String, InputStream> getMiniLogoFileFromCache(String sessionId)
  {
    try
    {

      SystemLogoSingleton instance = SystemLogoSingleton.getInstance();

      if (instance == null || StringUtils.isEmpty(instance.getMiniLogoVaultId()))
      {
        File file = new File(DeployProperties.getDeployPath() + "/assets/splash_logo_icon.png");

        return new Pair<String, InputStream>(file.getName(), new FileInputStream(file));
      }

      try
      {
        VaultFileDAOIF file = VaultFileDAO.get(instance.getMiniLogoVaultId());

        return new Pair<String, InputStream>(file.getFileName() + "." + file.getExtension(), file.getFileStream());
      }
      catch (FileReadException e)
      {
        logger.error("Unable to retrieve logo file", e);

        File file = new File(DeployProperties.getDeployPath() + "/assets/splash_logo_icon.png");

        return new Pair<String, InputStream>(file.getName(), new FileInputStream(file));
      }
    }
    catch (FileNotFoundException e)
    {
      logger.error("Unable to retrieve logo file", e);

      return null;
    }

  }

  @Request(RequestType.SESSION)
  public void removeBannerFileFromCache(String sessionId)
  {
    SystemLogoSingleton.removeBanner();
  }

  @Request(RequestType.SESSION)
  public void removeMiniLogoFileFromCache(String sessionId)
  {
    SystemLogoSingleton.removeMiniLogo();
  }

  @Request(RequestType.SESSION)
  public boolean hasBanner(String sessionId)
  {
    SystemLogoSingleton instance = SystemLogoSingleton.getInstance();

    if (instance == null || instance.getBannerVaultId().equals(""))
    {
      return false;
    }

    return true;
  }

  @Request(RequestType.SESSION)
  public boolean hasMiniLogo(String sessionId)
  {
    SystemLogoSingleton instance = SystemLogoSingleton.getInstance();

    if (instance == null || instance.getMiniLogoVaultId().equals(""))
    {
      return false;
    }

    return true;
  }
}
