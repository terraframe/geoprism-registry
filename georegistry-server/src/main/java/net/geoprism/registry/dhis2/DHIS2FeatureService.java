/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.dhis2;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.RunwayException;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnitGroup;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.ExportJobHasErrors;
import net.geoprism.registry.etl.NewGeoObjectInvalidSyncTypeError;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.UnexpectedRemoteResponse;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class DHIS2FeatureService
{
  public static final String[] OAUTH_INCOMPATIBLE_VERSIONS = new String[] {"2.35.0", "2.35.1"};
  
  public static final int LAST_TESTED_DHIS2_API_VERSION = 35;
  
  private static final Logger logger = LoggerFactory.getLogger(DHIS2FeatureService.class);
  
  public DHIS2FeatureService()
  {
    
  }
  
  public static class DHIS2SyncError extends RunwayException
  {
    private static final long serialVersionUID = 8463740942015611693L;

    protected DHIS2ImportResponse    response;
    
    protected String          submittedJson;

    protected Throwable       error;
    
    protected String          geoObjectCode;
    
    protected Long            rowIndex;

    public DHIS2SyncError(Long rowIndex, DHIS2ImportResponse response, String submittedJson, Throwable t, String geoObjectCode)
    {
      super("");
      this.response = response;
      this.submittedJson = submittedJson;
      this.error = t;
      this.geoObjectCode = geoObjectCode;
      this.rowIndex = rowIndex;
    }
  }
  
  private void recordExportError(DHIS2SyncError ee, ExportHistory history)
  {
    DHIS2ImportResponse resp = ee.response;
    Throwable ex = ee.error;
    String geoObjectCode = ee.geoObjectCode;
    
    ExportError exportError = new ExportError();

    if (ee.submittedJson != null)
    {
      exportError.setSubmittedJson(ee.submittedJson);
    }
    
    if (resp != null)
    {
      if (resp.getResponse() != null && resp.getResponse().length() > 0)
      {
        exportError.setResponseJson(resp.getResponse());
        
        if (resp.hasErrorReports())
        {
          List<ErrorReport> reports = resp.getErrorReports();
          
          ErrorReport report = reports.get(0);
          
          exportError.setErrorMessage(report.getMessage());
        }
      }
      
      exportError.setErrorCode(resp.getStatusCode());
    }
    
    exportError.setCode(geoObjectCode);
    
    if (ex != null)
    {
      exportError.setErrorJson(JobHistory.exceptionToJson(ex).toString());
    }
    
    exportError.setRowIndex(ee.rowIndex);
    
    exportError.setHistory(history);
    
    exportError.apply();
  }
  
  private long getCount(ServerGeoObjectType got)
  {
    MdVertexDAOIF mdVertex = got.getMdVertex();
    
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

    return new GraphQuery<Long>(statement.toString()).getSingleResult();
  }
  
  private List<VertexServerGeoObject> query(ServerGeoObjectType got, long skip, long pageSize)
  {
    MdVertexDAOIF mdVertex = got.getMdVertex();
    MdAttributeDAOIF mdAttribute = MdAttributeDAO.getByKey(GeoVertex.CLASS + "." + GeoVertex.LASTUPDATEDATE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" ORDER BY " + mdAttribute.getColumnName() + ", oid ASC");
    statement.append(" SKIP " + skip + " LIMIT " + pageSize);

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    List<VertexObject> vObjects = query.getResults();

    List<VertexServerGeoObject> response = new LinkedList<VertexServerGeoObject>();

    for (VertexObject vObject : vObjects)
    {
      VertexServerGeoObject vSGO = new VertexServerGeoObject(got, vObject);
      vSGO.setDate(ValueOverTime.INFINITY_END_DATE);

      response.add(vSGO);
    }

    return response;
  }
  
  public void synchronize(DHIS2TransportServiceIF dhis2, DHIS2SyncConfig dhis2Config, ExportHistory history)
  {
    final ExternalSystem es = dhis2Config.getSystem();
    
    long rowIndex = 0;
    long total = 0;
    long exportCount = 0;
    
    SortedSet<SyncLevel> levels = dhis2Config.getLevels();
    
    Boolean includeTranslations = LocalizationFacade.getInstalledLocales().size() > 0;

    for (SyncLevel level : levels)
    {
      long skip = 0;
      long pageSize = 1000;

      long count = this.getCount(level.getGeoObjectType());
      total += count;

      while (skip < count)
      {
        List<VertexServerGeoObject> objects = this.query(level.getGeoObjectType(), skip, pageSize);
        
        for (VertexServerGeoObject go : objects) {
          try
          {
            this.exportGeoObject(dhis2, dhis2Config, level, rowIndex, go, includeTranslations);
            
            exportCount++;
            
            history.appLock();
            history.setWorkProgress(rowIndex);
            history.setExportedRecords(exportCount);
            history.apply();
            
            if (level.getOrgUnitGroupId() != null && level.getOrgUnitGroupId().length() > 0)
            {
              final String externalId = go.getExternalId(es);
              
              level.getOrCreateOrgUnitGroupIdSet(level.getOrgUnitGroupId()).add(externalId);
            }
          }
          catch (DHIS2SyncError ee)
          {
            recordExportError(ee, history);
          }
          
          rowIndex++;
        };
        
        // Export OrgUnitGroup changes
        if (level.getOrgUnitGroupIdSet().size() > 0)
        {
          try
          {
            Map<String, Set<String>> orgUnitGroupIdSet = level.getOrgUnitGroupIdSet();
            
            
            // Fetch and populate all the org unit groups with the ids of org units that we will be exporting
            MetadataGetResponse<OrganisationUnitGroup> resp = dhis2.metadataGet(OrganisationUnitGroup.class);
            
            if (!resp.isSuccess())
            {
              UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
              throw re;
            }
            
            List<OrganisationUnitGroup> orgUnitGroups = resp.getObjects();
            
            if (orgUnitGroups != null)
            {
              Iterator<? extends OrganisationUnitGroup> it = orgUnitGroups.iterator();
              while (it.hasNext())
              {
                OrganisationUnitGroup group = it.next();
                
                if (orgUnitGroupIdSet.containsKey(group.getId()))
                {
                  orgUnitGroupIdSet.get(group.getId()).addAll(group.getOrgUnitIds());
                  group.setOrgUnitIds(orgUnitGroupIdSet.get(group.getId()));
                  orgUnitGroupIdSet.remove(group.getId());
                }
                else
                {
                  it.remove();
                }
              }
              
              if (orgUnitGroups.size() > 0)
              {
                JsonObject payload = new JsonObject();
                
                JsonArray jaOrgUnitGroups = new JsonArray();
                
                for (OrganisationUnitGroup group : orgUnitGroups)
                {
                  GsonBuilder builder = new GsonBuilder();
                  JsonObject joOrgUnitGroup = builder.create().toJsonTree(group, group.getClass()).getAsJsonObject();
                  
                  joOrgUnitGroup.remove("created");
                  joOrgUnitGroup.remove("lastUpdated");
                  joOrgUnitGroup.remove("symbol");
                  joOrgUnitGroup.remove("publicAccess");
                  joOrgUnitGroup.remove("user");
                  joOrgUnitGroup.remove("userGroupAccesses");
                  joOrgUnitGroup.remove("attributeValues");
                  joOrgUnitGroup.remove("translations");
                  joOrgUnitGroup.remove("userAccesses");
                  
                  jaOrgUnitGroups.add(joOrgUnitGroup);
                }
                
                payload.add(DHIS2Objects.ORGANISATION_UNIT_GROUPS, jaOrgUnitGroups);
                
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                
                MetadataImportResponse resp2 = dhis2.metadataPost(params, new StringEntity(payload.toString(), Charset.forName("UTF-8")));
                
                if (!resp2.isSuccess())
                {
                  if (resp2.hasMessage())
                  {
                    ExportRemoteException ere = new ExportRemoteException();
                    ere.setRemoteError(resp2.getMessage());
                    throw ere;
                  }
                  else
                  {
                    UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
                    throw re;
                  }
                }
              }
            }
          }
          catch (InvalidLoginException e)
          {
            LoginException cgrlogin = new LoginException(e);
            throw cgrlogin;
          }
          catch (HTTPException e)
          {
            HttpError cgrhttp = new HttpError(e);
            throw cgrhttp;
          }
        }

        skip += pageSize;
        
        NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));
      }
    }
    
    history.appLock();
    history.setWorkTotal(total);
    history.setWorkProgress(rowIndex);
    history.setExportedRecords(exportCount);
    history.clearStage();
    history.addStage(ExportStage.COMPLETE);
    history.apply();
    
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

    ExportErrorQuery query = new ExportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(history));
    Boolean hasErrors = query.getCount() > 0;
    
    if (hasErrors)
    {
      ExportJobHasErrors ex = new ExportJobHasErrors();
      
      throw ex;
    }
  }
  
  /**
   * It's important that we try our best to maintain an accurate state between
   * our database and the DHIS2 database. The DHIS2Serailizer will create new
   * ids and save them as externalIds for GeoObjects that do not have
   * externalIds. If our push to DHIS2 for this new GeoObject fails for whatever
   * reason, then we want to rollback our database so that we do not store the
   * id which does not exist in the DHIS2 database.
   * 
   * TODO : In the future perhaps we should directly ask DHIS2 if an object
   * exists and then we'll know
   * 
   * @param level
   * @param go
   * @return
   * @throws ExportError
   */
  @Transaction
  public void exportGeoObject(DHIS2TransportServiceIF dhis2, DHIS2SyncConfig dhis2Config, SyncLevel level, Long rowIndex, VertexServerGeoObject serverGo, Boolean includeTranslations) throws DHIS2SyncError
  {
    DHIS2ImportResponse resp = null;
    
    JsonObject orgUnitJsonTree = null;
    String orgUnitJson = null;
    
    String externalId = null;
    
    try
    {
      externalId = serverGo.getExternalId(dhis2Config.getSystem());
      boolean isNew = (externalId == null);

      if (isNew && level.getSyncType() != SyncLevel.Type.ALL)
      {
        NewGeoObjectInvalidSyncTypeError err = new NewGeoObjectInvalidSyncTypeError();
        err.setGeoObject(serverGo.getDisplayLabel().getValue());
        throw err;
      }

      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapter(VertexServerGeoObject.class, new DHIS2GeoObjectJsonAdapters.DHIS2Serializer(dhis2, dhis2Config, level, level.getGeoObjectType(), dhis2Config.getHierarchy(), dhis2Config.getSystem()));
      
      orgUnitJsonTree = builder.create().toJsonTree(serverGo, serverGo.getClass()).getAsJsonObject();
      orgUnitJson = orgUnitJsonTree.toString();
      
      externalId = serverGo.getExternalId(dhis2Config.getSystem());

      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("mergeMode", "MERGE"));
      
      try
      {
        JsonObject metadataPayload = new JsonObject();
        JsonArray orgUnits = new JsonArray();
        metadataPayload.add("organisationUnits", orgUnits);
        
        if (level.getSyncType() == SyncLevel.Type.ALL)
        {
          orgUnits.add(orgUnitJsonTree);
        }
        else if (level.getSyncType() == SyncLevel.Type.RELATIONSHIPS)
        {
          if (!orgUnitJsonTree.has("parent"))
          {
            return; // Root level types do not have parents.
          }
          
          JsonObject orgUnitRelationships = new JsonObject();
          
          
          // These attributes must be included at the requirement of DHIS2 API
          orgUnitRelationships.addProperty("id", orgUnitJsonTree.get("id").getAsString());
          orgUnitRelationships.addProperty("name", orgUnitJsonTree.get("name").getAsString());
          orgUnitRelationships.addProperty("shortName", orgUnitJsonTree.get("shortName").getAsString());
          orgUnitRelationships.addProperty("openingDate", orgUnitJsonTree.get("openingDate").getAsString());
          
          // We don't want to actually update any of these attributes, so if we can fetch the object from DHIS2
          // then use the values they have in their server instead.
          DHIS2Response orgUnitGetResp = dhis2.entityIdGet(DHIS2Objects.ORGANISATION_UNITS, externalId, params);
          if (orgUnitGetResp.isSuccess())
          {
            JsonObject jo = JsonParser.parseString(orgUnitGetResp.getResponse()).getAsJsonObject();
            
            orgUnitRelationships.addProperty("name", jo.get("name").getAsString());
            orgUnitRelationships.addProperty("shortName", jo.get("shortName").getAsString());
            orgUnitRelationships.addProperty("openingDate", jo.get("openingDate").getAsString());
          }
          
          // These attributes are the ones we need to include to change the relationship
          orgUnitRelationships.add("parent", orgUnitJsonTree.get("parent").getAsJsonObject());
          orgUnitRelationships.addProperty("path", orgUnitJsonTree.get("path").getAsString());
          orgUnitRelationships.addProperty("level", orgUnitJsonTree.get("level").getAsInt());
          
          orgUnits.add(orgUnitRelationships);
        }
        else if (level.getSyncType() == SyncLevel.Type.ORG_UNITS)
        {
          JsonObject orgUnitAttributes = orgUnitJsonTree.deepCopy();
          
          // Drop all attributes related to the parent / hierarchy
          orgUnitAttributes.remove("parent");
          orgUnitAttributes.remove("path");
          orgUnitAttributes.remove("level");
          
          orgUnits.add(orgUnitAttributes);
        }
        
        resp = dhis2.metadataPost(params, new StringEntity(metadataPayload.toString(), Charset.forName("UTF-8")));
      }
      catch (InvalidLoginException e)
      {
        LoginException cgrlogin = new LoginException(e);
        throw cgrlogin;
      }
      catch (HTTPException e)
      {
        HttpError cgrhttp = new HttpError(e);
        throw cgrhttp;
      }

      if (!resp.isSuccess())
      {
        if (resp.hasMessage())
        {
          ExportRemoteException ere = new ExportRemoteException();
          ere.setRemoteError(resp.getMessage());
          throw ere;
        }
        else
        {
          UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
          throw re;
        }
      }
    }
    catch (Throwable t)
    {
      DHIS2SyncError er = new DHIS2SyncError(rowIndex, resp, orgUnitJson, t, serverGo.getCode());
      throw er;
    }
  }
  
  public DHIS2TransportServiceIF getTransportService(DHIS2ExternalSystem es)
  {
    DHIS2TransportServiceIF dhis2;
    
    try
    {
      dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(es);
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException | UnexpectedResponseException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }
    
    return dhis2;
  }
  
  public void setExternalSystemDhis2Version(DHIS2ExternalSystem es)
  {
    this.setExternalSystemDhis2Version(getTransportService(es), es);
  }
  
  public void setExternalSystemDhis2Version(DHIS2TransportServiceIF dhis2, DHIS2ExternalSystem es)
  {
    es.setVersion(dhis2.getVersionRemoteServer());
    es.apply();
  }

  @Request(RequestType.SESSION)
  public JsonObject getSystemCapabilities(String sessionId, String systemJSON)
  {
    JsonObject capabilities = new JsonObject();
    
    JsonObject jo = JsonParser.parseString(systemJSON).getAsJsonObject();

    ExternalSystem system = ExternalSystem.desieralize(jo);
    
    if (system instanceof DHIS2ExternalSystem)
    {
      DHIS2ExternalSystem dhis2System = (DHIS2ExternalSystem) system;
      
      DHIS2TransportServiceIF dhis2 = getTransportService(dhis2System);
      
      String version = dhis2.getVersionRemoteServer();
      
      if (ArrayUtils.contains(DHIS2FeatureService.OAUTH_INCOMPATIBLE_VERSIONS, version))
      {
        capabilities.addProperty("oauth", false);
      }
      else
      {
        capabilities.addProperty("oauth", true);
      }
    }
    else
    {
      capabilities.addProperty("oauth", false);
    }
    
    return capabilities;
  }
}
