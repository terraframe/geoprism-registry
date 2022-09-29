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
package net.geoprism.registry.dhis2;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.EntityGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.LocaleGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.TypeReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.DHIS2Locale;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ImportStrategy;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnitGroup;
import net.geoprism.dhis2.dhis2adapter.response.model.Translation;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.dhis2.DHIS2FeatureService.DHIS2SyncError;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2SyncLevel;
import net.geoprism.registry.etl.ExportJobHasErrors;
import net.geoprism.registry.etl.NewGeoObjectInvalidSyncTypeError;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.etl.export.dhis2.MultipleLevelOneOrgUnitException;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class DHIS2SynchronizationManager
{
  private DHIS2TransportServiceIF dhis2;
  
  private DHIS2FeatureService service = new DHIS2FeatureService();
  
  private List<OrganisationUnit> ouLevel1 = null;
  
  private DHIS2SyncConfig dhis2Config;
  
  private ExportHistory history;
  
  private List<DHIS2Locale> dhis2Locales = new ArrayList<DHIS2Locale>();
  
  public DHIS2SynchronizationManager(DHIS2TransportServiceIF dhis2, DHIS2SyncConfig dhis2Config, ExportHistory history)
  {
    this.dhis2 = dhis2;
    this.history = history;
    this.dhis2Config = dhis2Config;
  }
  
  private Date todaysDate()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cal.setTime(new Date());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    
    return cal.getTime();
  }
  
  // https://play.dhis2.org/2.35.1/api/metadata.xml?organisationUnits=true&filter=level:eq:1
  private void init()
  {
    if (this.ouLevel1 != null)
    {
      return;
    }
    
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("filter", "level:eq:1"));
    
    try
    {
      MetadataGetResponse<OrganisationUnit> resp = this.dhis2.metadataGet(OrganisationUnit.class, params);
      
      this.service.validateDhis2Response(resp);
      
      List<OrganisationUnit> orgUnits = resp.getObjects();
      
      this.ouLevel1 = orgUnits;
      
      try
      {
        LocaleGetResponse localeResp = this.dhis2.localesGet();
        
        this.dhis2Locales = localeResp.getLocales();
      }
      catch (Throwable t) {
        
      }
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException | BadServerUriException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }
  }
  
  public void synchronize()
  {
    this.init();
    
    final ExternalSystem es = dhis2Config.getSystem();
    
    long rowIndex = 0;
    long total = 0;
    long exportCount = 0;
    
    SortedSet<DHIS2SyncLevel> levels = dhis2Config.getLevels();
    
    // First calculate the total number of records
    HashMap<Integer, Long> countAtLevel = new HashMap<Integer, Long>();
    int expectedLevel = 0;
    for (DHIS2SyncLevel level : levels)
    {
      if (level.getLevel() != expectedLevel)
      {
        throw new ProgrammingErrorException("Unexpected level number [" + level.getLevel() + "].");
      }
      
      if (level.getSyncType() != null && !DHIS2SyncLevel.Type.NONE.equals(level.getSyncType()))
      {
        long count = this.getCount(level.getGeoObjectType());
        total += count;
        
        countAtLevel.put(level.getLevel(), count);
      }
      
      expectedLevel++;
    }
    
    history.appLock();
    history.setWorkTotal(total);
    history.apply();
    
    // Now do the work
    for (DHIS2SyncLevel level : levels)
    {
      if (level.getSyncType() != null && !DHIS2SyncLevel.Type.NONE.equals(level.getSyncType()))
      {
        long skip = 0;
        long pageSize = 1000;
        long count = countAtLevel.get(level.getLevel());
  
        while (skip < count)
        {
          List<VertexServerGeoObject> objects = this.query(level.getGeoObjectType(), skip, pageSize);
          
          for (VertexServerGeoObject go : objects) {
            try
            {
              if ( !go.getInvalid() && (
                     this.dhis2Config.getSyncNonExistent() || go.getExists((dhis2Config.getDate() == null) ? this.todaysDate() : dhis2Config.getDate())
                   )
                 )
              {
                this.exportGeoObject(dhis2Config, level, levels, rowIndex, go);
                
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
              
              this.service.validateDhis2Response(resp);
              
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
                  
                  this.service.validateDhis2Response(resp2);
                }
              }
            }
            catch (InvalidLoginException e)
            {
              LoginException cgrlogin = new LoginException(e);
              throw cgrlogin;
            }
            catch (HTTPException | BadServerUriException e)
            {
              HttpError cgrhttp = new HttpError(e);
              throw cgrhttp;
            }
          }
  
          skip += pageSize;
          
          NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));
        }
      }
    }
    
    history.appLock();
    history.setWorkProgress(rowIndex);
    history.setExportedRecords(exportCount);
    history.clearStage();
    history.addStage(ExportStage.COMPLETE);
    history.apply();
    
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

    handleExportErrors();
  }
  
  private void handleExportErrors()
  {
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
  private void exportGeoObject(DHIS2SyncConfig dhis2Config, DHIS2SyncLevel level, SortedSet<DHIS2SyncLevel> levels, Long rowIndex, VertexServerGeoObject serverGo) throws DHIS2SyncError
  {
    String externalId = serverGo.getExternalId(dhis2Config.getSystem());
    boolean isNew = (externalId == null);

    if (isNew && level.getSyncType() != DHIS2SyncLevel.Type.ALL)
    {
      NewGeoObjectInvalidSyncTypeError err = new NewGeoObjectInvalidSyncTypeError();
      err.setGeoObject(serverGo.getDisplayLabel().getValue());
      throw err;
    }
    
    if (isNew && level.getLevel() == 0 && this.ouLevel1.size() > 0)
    {
      throw new MultipleLevelOneOrgUnitException();
    }

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(VertexServerGeoObject.class, new DHIS2GeoObjectJsonAdapters.DHIS2Serializer(dhis2, dhis2Config, level, this.dhis2Locales));
    
    JsonObject serializedOrgUnit = builder.create().toJsonTree(serverGo, serverGo.getClass()).getAsJsonObject();
    
    // DHIS2 requires that translations are sent directly through the translations api
//    JsonArray translations = this.serializedOrgUnit.get("translations").getAsJsonArray();
//    this.serializedOrgUnit.remove("translations");
    
//    externalId = serverGo.getExternalId(dhis2Config.getSystem());
    
    List<NameValuePair> orgUnitGetParams = new ArrayList<NameValuePair>();
    orgUnitGetParams.add(new BasicNameValuePair("translate", "false"));

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("mergeMode", "MERGE"));
    
    DHIS2ImportResponse resp = null;
    
    String submittedJson = null;
    
    try
    {
      try
      {
        JsonObject metadataPayload = new JsonObject();
        JsonArray orgUnits = new JsonArray();
        metadataPayload.add("organisationUnits", orgUnits);
        
        if (level.getSyncType() == DHIS2SyncLevel.Type.ALL)
        {
          this.mergeExistingDataIntoTranslations(serializedOrgUnit, externalId, level, rowIndex, serverGo);
          
          orgUnits.add(serializedOrgUnit);
        }
        else if (level.getSyncType() == DHIS2SyncLevel.Type.RELATIONSHIPS)
        {
          if (!serializedOrgUnit.has("parent"))
          {
            return; // Root level types do not have parents.
          }
          
          JsonObject orgUnitRelationships = new JsonObject();
          
          
          // These attributes must be included at the requirement of DHIS2 API
          orgUnitRelationships.addProperty("id", serializedOrgUnit.get("id").getAsString());
          orgUnitRelationships.addProperty("name", serializedOrgUnit.get("name").getAsString());
          orgUnitRelationships.addProperty("shortName", serializedOrgUnit.get("shortName").getAsString());
          orgUnitRelationships.addProperty("openingDate", serializedOrgUnit.get("openingDate").getAsString());
          
          // We don't want to actually update any of these attributes, so if we can fetch the object from DHIS2
          // then use the values they have in their server instead.
          EntityGetResponse<OrganisationUnit> orgUnitGetResp = dhis2.entityIdGet(DHIS2Objects.ORGANISATION_UNITS, externalId, OrganisationUnit.class, orgUnitGetParams);
          this.service.validateDhis2Response(orgUnitGetResp);
          OrganisationUnit existingOrgUnit = orgUnitGetResp.getEntity();
          orgUnitRelationships.addProperty("name", existingOrgUnit.getName());
          orgUnitRelationships.addProperty("shortName", existingOrgUnit.getShortName());
          orgUnitRelationships.addProperty("openingDate", DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate(existingOrgUnit.getOpeningDate()));
          
          // These attributes are the ones we need to include to change the relationship
          orgUnitRelationships.add("parent", serializedOrgUnit.get("parent").getAsJsonObject());
          orgUnitRelationships.addProperty("path", serializedOrgUnit.get("path").getAsString());
          orgUnitRelationships.addProperty("level", serializedOrgUnit.get("level").getAsInt());
          
          orgUnits.add(orgUnitRelationships);
        }
        else if (level.getSyncType() == DHIS2SyncLevel.Type.ORG_UNITS)
        {
          this.mergeExistingDataIntoTranslations(serializedOrgUnit, externalId, level, rowIndex, serverGo);
          
          JsonObject orgUnitAttributes = serializedOrgUnit.deepCopy();
          
          // Drop all attributes related to the parent / hierarchy
          orgUnitAttributes.remove("parent");
          orgUnitAttributes.remove("path");
          orgUnitAttributes.remove("level");
          
          orgUnits.add(orgUnitAttributes);
        }
        
        ImportStrategy strategy = isNew ? ImportStrategy.CREATE : ImportStrategy.UPDATE;
        params.add(new BasicNameValuePair("importStrategy", strategy.name()));
        
        submittedJson = metadataPayload.toString();
        
        resp = dhis2.metadataPost(params, new StringEntity(submittedJson, Charset.forName("UTF-8")));
        
        this.service.validateDhis2Response(resp);
        
        if (isNew && level.getLevel() == 1 && this.ouLevel1.size() > 0)
        {
          this.ouLevel1.add(new OrganisationUnit());
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
    catch (DHIS2SyncError e)
    {
      throw e;
    }
    catch (Throwable t)
    {
      DHIS2SyncError er = new DHIS2SyncError(rowIndex, resp, submittedJson, t, serverGo.getCode());
      throw er;
    }
  }

  /**
   * Submitting DHIS2 translations causes all existing translation data to be overwritten with what you're submitting. So we have to fetch the existing data and
   * merge what we're submitting into it.
   */
  private void mergeExistingDataIntoTranslations(JsonObject serializedOrgUnit, String externalId, DHIS2SyncLevel level, Long rowIndex, VertexServerGeoObject serverGo)
  {
    final JsonArray translations = serializedOrgUnit.get("translations").getAsJsonArray();
    String submittedJson = null;
    TypeReportResponse resp = null;
    
    try
    {
      try
      {
        if (translations != null && translations.size() > 0 && (level.getSyncType() == DHIS2SyncLevel.Type.ALL || level.getSyncType() == DHIS2SyncLevel.Type.ORG_UNITS))
        {
          EntityGetResponse<OrganisationUnit> orgUnitGetResp = this.dhis2.entityIdTranslationsGet(DHIS2Objects.ORGANISATION_UNITS, externalId, OrganisationUnit.class);
          this.service.validateDhis2Response(orgUnitGetResp);
          OrganisationUnit existingOrgUnit = orgUnitGetResp.getEntity();
          
          JsonArray submissionTranslations = new JsonArray();
          
          Map<Translation.Property, Map<String, JsonObject>> mapSubmissionTranslations = new HashMap<Translation.Property, Map<String, JsonObject>>();
          mapSubmissionTranslations.put(Translation.Property.NAME, new HashMap<String, JsonObject>());
          mapSubmissionTranslations.put(Translation.Property.DESCRIPTION, new HashMap<String, JsonObject>());
          mapSubmissionTranslations.put(Translation.Property.SHORT_NAME, new HashMap<String, JsonObject>());
          
          List<Translation> existingTranslations = existingOrgUnit.getTranslations();
          for (Translation existingTranslation : existingTranslations)
          {
            if (existingTranslation.getValue() != null && existingTranslation.getValue().length() > 0)
            {
              GsonBuilder builder = new GsonBuilder();
              JsonObject serialized = builder.create().toJsonTree(existingTranslation, Translation.class).getAsJsonObject();
              
              mapSubmissionTranslations.get(existingTranslation.getProperty()).put(existingTranslation.getLocale(), serialized);
            }
          }
          
          for (int i = 0; i < translations.size(); ++i)
          {
            JsonObject submissionTranslation = translations.get(i).getAsJsonObject();
            
            if (submissionTranslation.has("value") && submissionTranslation.get("value").getAsString().length() > 0)
            {
              Translation.Property property = Translation.Property.valueOf(submissionTranslation.get("property").getAsString());
              JsonObject existingTranslation = mapSubmissionTranslations.get(property).get(submissionTranslation.get("locale").getAsString());
              
              if (existingTranslation != null)
              {
                existingTranslation.addProperty("value", submissionTranslation.get("value").getAsString());
              }
              else
              {
                mapSubmissionTranslations.get(property).put(submissionTranslation.get("locale").getAsString(), submissionTranslation);
              }
            }
          }
          
          mapSubmissionTranslations.get(Translation.Property.NAME).values().stream().forEach(translation -> submissionTranslations.add(translation));
          mapSubmissionTranslations.get(Translation.Property.DESCRIPTION).values().stream().forEach(translation -> submissionTranslations.add(translation));
          mapSubmissionTranslations.get(Translation.Property.SHORT_NAME).values().stream().forEach(translation -> submissionTranslations.add(translation));
          
          serializedOrgUnit.add("translations", submissionTranslations);
        }
        else
        {
          serializedOrgUnit.remove("translations");
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
    catch (Throwable t)
    {
      DHIS2SyncError er = new DHIS2SyncError(rowIndex, resp, submittedJson, t, serverGo.getCode());
      throw er;
    }
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
      vSGO.setDate(dhis2Config.getDate());

      response.add(vSGO);
    }

    return response;
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
}
