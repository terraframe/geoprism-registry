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

import java.io.InputStream;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.service.business.ExcelBusinessService;

@Service
public class ExcelService
{
  @Autowired
  private ExcelBusinessService service;

  @Request(RequestType.SESSION)
  public JSONObject getExcelConfiguration(String sessionId, String type, Date startDate, Date endDate, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    return this.service.getExcelConfiguration(type, startDate, endDate, fileName, fileStream, strategy, copyBlank);
  }

  @Request(RequestType.SESSION)
  public JSONObject getBusinessTypeConfiguration(String sessionId, String type, Date date, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    return this.service.getBusinessTypeConfiguration(type, date, fileName, fileStream, strategy, copyBlank);
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId, String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportSpreadsheet(code, hierarchyCode);
  }
}
