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

import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.etl.EdgeJsonImporter;
import net.geoprism.registry.service.business.ETLBusinessService;

@Service
public class ETLService
{
  @Autowired
  protected ETLBusinessService             service;

  @Request(RequestType.SESSION)
  public void cancelImport(String sessionId, String json)
  {
    this.service.cancelImport(json);
  }

  @Request(RequestType.SESSION)
  public JsonObject reImport(String sessionId, MultipartFile file, String json)
  {
    this.service.reImport(file, json);

    return this.service.doImport(json);
  }

  @Request(RequestType.SESSION)
  public JsonObject doImport(String sessionId, String json)
  {
    return this.service.doImport(json);
  }

  @Request(RequestType.SESSION)
  public JsonObject getActiveImports(String sessionId, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    return this.service.getActiveImports(pageSize, pageNumber, sortAttr, isAscending);
  }

  @Request(RequestType.SESSION)
  public JsonObject getCompletedImports(String sessionId, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    return this.service.getCompletedImports(pageSize, pageNumber, sortAttr, isAscending);
  }

  @Request(RequestType.SESSION)
  public JsonObject getImportErrors(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    return this.service.getImportErrors(historyId, onlyUnresolved, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject getValidationProblems(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    return this.service.getValidationProblems(historyId, onlyUnresolved, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject getExportErrors(String sessionId, String historyId, int pageSize, int pageNumber)
  {
    return this.service.getExportErrors(historyId, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject getImportDetails(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
    return this.service.getImportDetails(historyId, onlyUnresolved, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public JsonObject getExportDetails(String sessionId, String historyId, int pageSize, int pageNumber)
  {
    return this.service.getExportDetails(historyId, pageSize, pageNumber);
  }

  @Request(RequestType.SESSION)
  public void submitImportErrorResolution(String sessionId, String json)
  {
    this.service.submitImportErrorResolution(json);
  }

  @Request(RequestType.SESSION)
  public void submitValidationProblemResolution(String sessionId, String json)
  {
    this.service.submitValidationProblemResolution(json);
  }

  @Request(RequestType.SESSION)
  public void resolveImport(String sessionId, String historyId)
  {
    this.service.resolveImport(historyId);
  }

  @Request(RequestType.SESSION)
  public void importEdgeJson(String sessionId, Date startDate, Date endDate, ApplicationResource resource)
  {
    try
    {
      EdgeJsonImporter importer = new EdgeJsonImporter(resource, startDate, endDate, true);
      importer.importData();
    }
    catch (JsonSyntaxException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

}
