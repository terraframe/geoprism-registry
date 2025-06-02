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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.graph.RDFExportJob;
import net.geoprism.graph.RepoRDFExportJob;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.view.ImportHistoryView;
import net.geoprism.registry.view.RDFExport;

@Service
public class RDFExportService
{
  private static final Logger logger = LoggerFactory.getLogger(RDFExportService.class);

  @Request(RequestType.SESSION)
  public InputStream exportDownload(String sessionId, String historyId)
  {
    var hist = ImportHistory.get(historyId);

    return hist.getImportFile().openNewStream();
  }

  @Request(RequestType.SESSION)
  public ImportHistoryView export(String sessionId, RDFExport config)
  {
    ImportHistory hist = RepoRDFExportJob.runNewJob(config);

    return new ImportHistoryView(hist.getOid());
  }

  @Request(RequestType.SESSION)
  public ImportHistoryView export(String sessionId, String versionId, GeometryExportType geomExportType)
  {
    ImportHistory hist = RDFExportJob.runNewJob(versionId, geomExportType);
    
    return new ImportHistoryView(hist.getOid());
  }
}
