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
package net.geoprism.registry.curation;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;

import net.geoprism.DataUploader;
import net.geoprism.GeoprismUser;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.etl.DataImportJob;
import net.geoprism.registry.etl.ImportError;
import net.geoprism.registry.etl.ImportErrorQuery;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportHistoryQuery;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.ParentReferenceProblem;
import net.geoprism.registry.etl.TermReferenceProblem;
import net.geoprism.registry.etl.ValidationProblem;
import net.geoprism.registry.etl.ValidationProblemQuery;
import net.geoprism.registry.etl.ImportError.ErrorResolution;
import net.geoprism.registry.etl.ValidationProblem.ValidationResolution;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.service.GeoSynonymService;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class CurationService
{
  public JsonObject getListCurationInfo(ListTypeVersion version)
  {
    ListType listType = version.getListType();
    ServerGeoObjectType serverGOT = listType.getGeoObjectType();
    
    JsonObject json = new JsonObject();
    
    ListCurationHistory history = ListCurationJob.getMostRecent(version.getOid());
    
    boolean isRunning = false;
    
    if (history != null)
    {
      if (history.getStatus().contains(AllJobStatus.RUNNING))
      {
        isRunning = true;
        
        Progress progress = ProgressService.get(history.getOid());
        
        if (progress != null)
        {
          json.add("progress", progress.toJson());
        }
      }
      
      json.addProperty("lastRun", GeoRegistryUtil.formatDate(history.getCreateDate(), false));
      
      json.addProperty("curationId", history.getOid());
    }
    
    final RolePermissionService perms = ServiceFactory.getRolePermissionService();
    final String orgCode = listType.getOrganization().getCode();
    boolean hasRunPermission = perms.isSRA() || perms.isRA(orgCode) || perms.isRM(orgCode, serverGOT);
    
    json.addProperty("canRun", !isRunning && hasRunPermission);
    
    return json;
  }
  
  @Request(RequestType.SESSION)
  public JsonObject getCurationResults(String sessionId, String historyId, boolean onlyUnresolved, int pageSize, int pageNumber)
  {
//    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());
//
//    query.WHERE(query.getHistory().EQ(historyId));
//
//    if (onlyUnresolved)
//    {
//      query.WHERE(query.getResolution().EQ(ErrorResolution.UNRESOLVED.name()));
//    }
//
//    query.ORDER_BY(query.getRowIndex(), SortOrder.ASC);
//
//    query.restrictRows(pageSize, pageNumber);
//
    JsonObject page = new JsonObject();
//    page.addProperty("count", query.getCount());
//    page.addProperty("pageNumber", query.getPageNumber());
//    page.addProperty("pageSize", query.getPageSize());
//
//    JsonArray ja = new JsonArray();
//
//    OIterator<? extends ImportError> it = query.getIterator();
//    while (it.hasNext())
//    {
//      ImportError err = it.next();
//
//      ja.add(err.toJson());
//    }
//
//    page.add("results", ja);
//
    return page;
  }
}
