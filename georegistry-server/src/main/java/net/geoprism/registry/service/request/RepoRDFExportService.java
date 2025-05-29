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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.RepoRDFExportJob;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;

@Service
public class RepoRDFExportService
{
  private static final Logger logger = LoggerFactory.getLogger(RepoRDFExportService.class);
  
  @Request(RequestType.SESSION)
  public String export(String sessionId, GraphTypeReference[] graphTypeRefs, String[] gotCodes, GeometryExportType geomExportType)
  {
    var hist = RepoRDFExportJob.runNewJob(graphTypeRefs, gotCodes, geomExportType);
    
    return hist.getOid();
  }
  
  @Request(RequestType.SESSION)
  public InputStream exportDownload(String sessionId, String historyId)
  {
    var hist = ImportHistory.get(historyId);
    
    return hist.getImportFile().openNewStream();
  }
}
