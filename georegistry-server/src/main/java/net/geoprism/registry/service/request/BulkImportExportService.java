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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.BulkExportSection;
import net.geoprism.registry.service.business.BulkImportExportBusinessServiceIF;

@Service
public class BulkImportExportService implements BulkImportExportServiceIF
{
  @Autowired
  private BulkImportExportBusinessServiceIF service;

  @Override
  @Request(RequestType.SESSION)
  public InputStream exportData(String sessionId, Set<BulkExportSection> sections)
  {
    try
    {
      Path path = Files.createTempFile("gpr-dump", ".zip");

      this.service.createExport(path.toFile(), sections);

      return Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  @Request(RequestType.SESSION)
  public void deleteData(String sessionId)
  {
    this.service.deleteData();
  }

  @Override
  @Request(RequestType.SESSION)
  public String importData(String sessionId, InputStream istream)
  {
    return this.service.importData(istream).toJSON().toString();
  }

}
