/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.request;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.service.business.ExcelBusinessService;
import net.geoprism.registry.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationView;
import net.geoprism.registry.view.TypedObjectImportConfigurationDTO;

@Service
public class ExcelService
{
  @Autowired
  private ExcelBusinessService service;

  @Request(RequestType.SESSION)
  public GeoObjectImportConfigurationDTO getExcelConfiguration(String sessionId, String fileName, InputStream fileStream, ImportConfigurationView view)
  {
    return this.service.getExcelConfiguration(fileName, fileStream, view);
  }

  @SuppressWarnings("unchecked")
  @Request(RequestType.SESSION)
  public <T extends TypedObjectImportConfigurationDTO> T getImportConfiguration(String sessionId, String fileName, InputStream fileStream, ImportConfigurationView view)
  {
    return (T) this.service.getImportConfiguration(fileName, fileStream, view);
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId, String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportSpreadsheet(code, hierarchyCode);
  }
}
