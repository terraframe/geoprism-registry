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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.AllJobStatus;

import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.configuration.ImportReportMode;
import net.geoprism.dhis2.dhis2adapter.configuration.ImportStrategy;
import net.geoprism.dhis2.dhis2adapter.configuration.MergeMode;
import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.EntityGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.LocaleGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.DHIS2Locale;
import net.geoprism.dhis2.dhis2adapter.response.model.ErrorReport;
import net.geoprism.dhis2.dhis2adapter.response.model.ObjectReport;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnitGroup;
import net.geoprism.dhis2.dhis2adapter.response.model.TypeReport;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.dhis2.DHIS2FeatureService.DHIS2SyncError;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2SyncLevel;
import net.geoprism.registry.etl.GeoObjectCache;
import net.geoprism.registry.etl.NewGeoObjectInvalidSyncTypeError;
import net.geoprism.registry.etl.export.ExportError;
import net.geoprism.registry.etl.export.ExportErrorQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.UnexpectedRemoteResponse;
import net.geoprism.registry.etl.export.dhis2.DHIS2GeoObjectJsonAdapters;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.etl.export.dhis2.MultipleLevelOneOrgUnitException;
import net.geoprism.registry.etl.export.dhis2.NoParentException;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.graph.helper.WhereCriteriaBuilder;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class DHIS2SynchronizationManager
{
  private DHIS2TransportServiceIF            dhis2;

  private DHIS2FeatureService                service        = new DHIS2FeatureService();

  private List<OrganisationUnit>             ouLevel1       = null;

  private DHIS2SyncConfig                    dhis2Config;

  private List<DHIS2Locale>                  dhis2Locales   = new ArrayList<DHIS2Locale>();

  protected ExportHistory                    history;

  protected SynchronizationProgressMonitorIF progressListener;

  // Links Geo-Object reference (typeCode + SEPARATOR + goUid) -> externalId
  private BidiMap<String, String>            newExternalIds = new DualHashBidiMap<String, String>();

  private GPRGeoObjectBusinessServiceIF      objectService;

  public DHIS2SynchronizationManager(DHIS2TransportServiceIF dhis2, DHIS2SyncConfig dhis2Config, ExportHistory history, SynchronizationProgressMonitorIF progressListener)
  {
    this.dhis2 = dhis2;
    this.history = history;
    this.progressListener = progressListener;
    this.dhis2Config = dhis2Config;
    this.objectService = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);
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

      LocaleGetResponse localeResp = this.dhis2.localesGet();

      this.dhis2Locales = localeResp.getLocales();
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

  public AllJobStatus synchronize()
  {
    this.init();

    HashMap<Integer, Long> countAtLevel = calculateWorkTotal();

    try
    {
      synchronizePerLevel(countAtLevel);
    }
    finally
    {
      this.progressListener.finalize();
    }

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

    return calculateJobStatus();
  }

  private void synchronizePerLevel(HashMap<Integer, Long> countAtLevel)
  {
    final SortedSet<DHIS2SyncLevel> levels = dhis2Config.getLevels();
    final ExternalSystem es = dhis2Config.getSystem();

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

          JsonObject metadataPayload = new JsonObject();

          metadataPayload.add(DHIS2Objects.ORGANISATION_UNITS, new JsonArray());

          try
          {
            // Add OrganisationUnits
            for (VertexServerGeoObject go : objects)
            {
              try
              {
                this.exportGeoObject(metadataPayload, dhis2Config, level, levels, go);
              }
              catch (Throwable t)
              {
                if (t instanceof DHIS2SyncError)
                {
                  this.progressListener.recordError((DHIS2SyncError) t, ErrorType.ERROR);
                }
                else if (t instanceof NoParentException)
                {
                  this.progressListener.recordError(new DHIS2SyncError(this.progressListener.getRowNumber(), null, null, t, go.getCode()), ErrorType.WARNING);
                }
                else
                {
                  this.progressListener.recordError(new DHIS2SyncError(this.progressListener.getRowNumber(), null, null, t, go.getCode()), ErrorType.ERROR);
                }
              }

              Collection<String> groupIds = level.getOrgUnitGroupIds();

              if (groupIds != null && groupIds.size() > 0)
              {
                String externalId;

                String goRef = go.getType().getCode() + GeoObjectCache.SEPARATOR + go.getUid();

                if (this.newExternalIds.containsKey(goRef))
                {
                  externalId = this.newExternalIds.get(goRef);
                }
                else
                {
                  externalId = this.objectService.getExternalId(go, es);
                }

                if (externalId != null && externalId.length() > 0)
                {
                  groupIds.forEach(groupId -> level.getOrCreateOrgUnitGroupIdSet(groupId).add(externalId));
                }
              }

              this.progressListener.setRowNumber(this.progressListener.getRowNumber() + 1);
            }

            syncOrgUnitGroups(level, metadataPayload);

            this.submitMetadata(level, metadataPayload);
          }
          catch (DHIS2SyncError ee)
          {
            this.progressListener.recordError(ee, ErrorType.ERROR);
          }

          skip += pageSize;

          NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));
        }
      }
    }
  }

  private HashMap<Integer, Long> calculateWorkTotal()
  {
    final SortedSet<DHIS2SyncLevel> levels = dhis2Config.getLevels();
    long total = 0;

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

    this.progressListener.setWorkTotal(total);

    return countAtLevel;
  }

  private void syncOrgUnitGroups(DHIS2SyncLevel level, JsonObject metadataPayload)
  {
    if (level.getOrgUnitGroupIdSet().size() > 0)
    {
      try
      {
        Map<String, Set<String>> orgUnitGroupIdSet = level.getOrgUnitGroupIdSet();

        // Fetch and populate all the org unit groups with the ids of org units
        // that we will be exporting
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

            metadataPayload.add(DHIS2Objects.ORGANISATION_UNIT_GROUPS, jaOrgUnitGroups);
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
  }

  private AllJobStatus calculateJobStatus()
  {
    ExportErrorQuery errorQuery = new ExportErrorQuery(new QueryFactory());
    errorQuery.WHERE(errorQuery.getHistory().EQ(this.history));
    errorQuery.WHERE(errorQuery.getErrorType().EQ(ErrorType.ERROR.name()));

    if (errorQuery.getCount() > 0)
    {
      return AllJobStatus.FAILURE;
    }

    ExportErrorQuery warningQuery = new ExportErrorQuery(new QueryFactory());
    warningQuery.WHERE(warningQuery.getHistory().EQ(this.history));
    warningQuery.WHERE(warningQuery.getErrorType().EQ(ErrorType.WARNING.name()));

    if (warningQuery.getCount() > 0)
    {
      return AllJobStatus.WARNING;
    }

    return AllJobStatus.SUCCESS;
  }

  private void submitMetadata(DHIS2SyncLevel level, JsonObject payload)
  {
    if (payload.get(DHIS2Objects.ORGANISATION_UNITS).getAsJsonArray().size() == 0)
    {
      return;
    }

    String submittedJson = null;
    ImportReportResponse resp = null;

    try
    {
      try
      {
        final JsonArray orgUnitsPayload = payload.get(DHIS2Objects.ORGANISATION_UNITS).getAsJsonArray();
        final JsonArray orgUnitGroupsPayload = payload.has(DHIS2Objects.ORGANISATION_UNIT_GROUPS) ? payload.get(DHIS2Objects.ORGANISATION_UNIT_GROUPS).getAsJsonArray() : new JsonArray();

        // Submit
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("mergeMode", MergeMode.REPLACE.name())); // If
                                                                                   // you
                                                                                   // submit
                                                                                   // translations
                                                                                   // with
                                                                                   // mergeMode=MERGE
                                                                                   // it
                                                                                   // will
                                                                                   // corrupt
                                                                                   // the
                                                                                   // database
        params.add(new BasicNameValuePair("importStrategy", ImportStrategy.CREATE_AND_UPDATE.name()));
        params.add(new BasicNameValuePair("importReportMode", ImportReportMode.FULL.name())); // We
                                                                                              // want
                                                                                              // it
                                                                                              // to
                                                                                              // tell
                                                                                              // us
                                                                                              // if
                                                                                              // an
                                                                                              // object
                                                                                              // was
                                                                                              // applied.

        submittedJson = payload.toString();

        resp = dhis2.metadataPost(params, new StringEntity(submittedJson, Charset.forName("UTF-8")));

        processMetadataImportResponse(level, submittedJson, resp, orgUnitsPayload, orgUnitGroupsPayload);
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
      if (t instanceof DHIS2SyncError)
      {
        throw (DHIS2SyncError) t;
      }
      else
      {
        DHIS2SyncError er = new DHIS2SyncError(-1L, resp, submittedJson, t, null);
        throw er;
      }
    }
  }

  protected void processMetadataImportResponse(DHIS2SyncLevel level, String submittedJson, ImportReportResponse resp, final JsonArray orgUnitsPayload, final JsonArray orgUnitGroupsPayload)
  {
    long successfulImports = 0L;

    // Process Response
    if (resp.hasTypeReports())
    {
      for (TypeReport tr : resp.getTypeReports())
      {
        boolean passedValidation = tr.getStats().getCreated() > 0 || tr.getStats().getUpdated() > 0;

        for (ObjectReport or : tr.getObjectReports())
        {
          try
          {
            if (tr.getKlass().endsWith("OrganisationUnit"))
            {
              if (or.hasErrorReports())
              {
                String submittedJsonRow = null;
                final ErrorReport er = or.getErrorReports().get(0);
                final JsonObject serializedOR = new GsonBuilder().create().toJsonTree(or, or.getClass()).getAsJsonObject();

                // Fetch the Geo-Object it's referencing
                VertexServerGeoObject serverGo;
                if (this.newExternalIds.containsValue(or.getUid())) // Create
                                                                    // new
                                                                    // organisation
                                                                    // unit
                {
                  String goRef = this.newExternalIds.getKey(or.getUid());
                  String typeCode = goRef.split(GeoObjectCache.SEPARATOR.replace("$", "\\$"))[0];
                  String goUid = goRef.split(GeoObjectCache.SEPARATOR.replace("$", "\\$"))[1];

                  serverGo = new VertexGeoObjectStrategy(ServerGeoObjectType.get(typeCode)).getGeoObjectByUid(goUid);
                }
                else // Update existing organisation unit
                {
                  serverGo = this.objectService.getByExternalId(or.getUid(), this.dhis2Config.getSystem(), level.getGeoObjectType());
                }

                // Find the relevant submitted json
                for (int i = 0; i < orgUnitsPayload.size(); ++i)
                {
                  JsonObject submitted = orgUnitsPayload.get(i).getAsJsonObject();

                  if (submitted.get("id").getAsString().equals(or.getUid()))
                  {
                    submittedJsonRow = submitted.toString();
                    break;
                  }
                }

                this.progressListener.recordError(er.getMessage(), resp.getStatusCode(), serializedOR.toString(), submittedJsonRow, Long.valueOf(or.getIndex()), ( serverGo == null ? or.getUid() : serverGo.getCode() ), ErrorType.ERROR);
              }
              else if (passedValidation)
              {
                successfulImports++;

                if (this.newExternalIds.containsValue(or.getUid()))
                {
                  String goRef = this.newExternalIds.getKey(or.getUid());
                  String typeCode = goRef.split(GeoObjectCache.SEPARATOR.replace("$", "\\$"))[0];
                  String goUid = goRef.split(GeoObjectCache.SEPARATOR.replace("$", "\\$"))[1];

                  VertexServerGeoObject serverGo = new VertexGeoObjectStrategy(ServerGeoObjectType.get(typeCode)).getGeoObjectByUid(goUid);

                  this.objectService.createExternalId(serverGo, this.dhis2Config.getSystem(), or.getUid(), net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy.NEW_ONLY);
                }
              }
            }
            else
            {
              if (or.hasErrorReports())
              {
                String submittedJsonRow = null;
                final ErrorReport er = or.getErrorReports().get(0);
                final JsonObject serializedOR = new GsonBuilder().create().toJsonTree(or, or.getClass()).getAsJsonObject();

                // Find the relevant submitted json
                if (tr.getKlass().endsWith("OrganisationUnitGroup"))
                {
                  for (int i = 0; i < orgUnitGroupsPayload.size(); ++i)
                  {
                    JsonObject submitted = orgUnitGroupsPayload.get(i).getAsJsonObject();

                    if (submitted.get("id").getAsString().equals(or.getUid()))
                    {
                      submittedJsonRow = submitted.toString();
                      break;
                    }
                  }
                }

                this.progressListener.recordError(er.getMessage(), resp.getStatusCode(), serializedOR.toString(), submittedJsonRow, Long.valueOf(or.getIndex()), null, ErrorType.ERROR);
              }
            }
          }
          catch (Throwable t)
          {
            this.progressListener.recordError(new DHIS2SyncError(or.getIndex() == null ? null : Long.valueOf(or.getIndex()), null, submittedJson, t, null), ErrorType.ERROR);
          }
        }
      }
    }
    else
    {
      this.service.validateDhis2Response(resp);
    }

    this.progressListener.setExportedRecords(this.progressListener.getExportedRecords() + successfulImports);
  }

  /**
   * It's important that we try our best to maintain an accurate state between
   * our database and the DHIS2 database. The DHIS2Serailizer will create new
   * ids and add them to the 'newExternalIds' map, which we will apply if the
   * object is successfully applied in DHIS2.
   * 
   * @param level
   * @param go
   * @return
   * @throws ExportError
   */
  private void exportGeoObject(JsonObject payload, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel level, SortedSet<DHIS2SyncLevel> levels, VertexServerGeoObject serverGo) throws DHIS2SyncError
  {
    final JsonArray orgUnitsPayload = payload.get(DHIS2Objects.ORGANISATION_UNITS).getAsJsonArray();

    String externalId = this.objectService.getExternalId(serverGo, dhis2Config.getSystem());
    boolean isNew = ( externalId == null );

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

    OrganisationUnit existingOrgUnit = null;
    if (!isNew)
    {
      List<NameValuePair> orgUnitGetParams = new ArrayList<NameValuePair>();
      orgUnitGetParams.add(new BasicNameValuePair("translate", "false"));

      List<String> fields = Arrays.stream(new String[] { "translations", "attributeValues" }).collect(Collectors.toCollection(ArrayList::new));
      if (level.getSyncType() == DHIS2SyncLevel.Type.RELATIONSHIPS)
      {
        fields.addAll(Arrays.asList(new String[] { "id", "name", "shortName", "openingDate" }));
      }
      orgUnitGetParams.add(new BasicNameValuePair("fields", StringUtils.join(fields, ",")));

      EntityGetResponse<OrganisationUnit> orgUnitGetResp = null;
      try
      {
        orgUnitGetResp = dhis2.entityIdGet(DHIS2Objects.ORGANISATION_UNITS, externalId, OrganisationUnit.class, orgUnitGetParams);

        this.service.validateDhis2Response(orgUnitGetResp);
      }
      catch (InvalidLoginException | HTTPException | BadServerUriException | ExportRemoteException | UnexpectedRemoteResponse e)
      {
        DHIS2SyncError er = new DHIS2SyncError(this.progressListener.getRowNumber(), orgUnitGetResp, null, e, serverGo.getCode());
        throw er;
      }

      existingOrgUnit = orgUnitGetResp.getEntity();
    }

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(VertexServerGeoObject.class, new DHIS2GeoObjectJsonAdapters.DHIS2Serializer(dhis2, dhis2Config, level, this.dhis2Locales, this.newExternalIds));

    JsonObject serializedOrgUnit = builder.create().toJsonTree(serverGo, serverGo.getClass()).getAsJsonObject();

    if (!isNew)
    {
      serializedOrgUnit.add("translations", this.service.mergeTranslations(existingOrgUnit.getTranslations(), serializedOrgUnit.get("translations").getAsJsonArray()));
      serializedOrgUnit.add("attributeValues", this.service.mergeAttributeValues(existingOrgUnit.getAttributeValues(), serializedOrgUnit.get("attributeValues").getAsJsonArray()));
    }

    if (level.getSyncType() == DHIS2SyncLevel.Type.ALL)
    {
      orgUnitsPayload.add(serializedOrgUnit);
    }
    else if (!isNew && level.getSyncType() == DHIS2SyncLevel.Type.RELATIONSHIPS)
    {
      if (!serializedOrgUnit.has("parent"))
      {
        return; // Root level types do not have parents.
      }

      JsonObject orgUnitRelationships = new JsonObject();

      // We don't want to actually update any of these attributes, so we use the
      // values they have in their server instead, since they are required
      orgUnitRelationships.addProperty("id", existingOrgUnit.getId());
      orgUnitRelationships.addProperty("name", existingOrgUnit.getName());
      orgUnitRelationships.addProperty("shortName", existingOrgUnit.getShortName());
      orgUnitRelationships.addProperty("openingDate", DHIS2GeoObjectJsonAdapters.DHIS2Serializer.formatDate(existingOrgUnit.getOpeningDate()));

      // These attributes are the ones we need to include to change the
      // relationship
      orgUnitRelationships.add("parent", serializedOrgUnit.get("parent").getAsJsonObject());
      orgUnitRelationships.addProperty("path", serializedOrgUnit.get("path").getAsString());
      orgUnitRelationships.addProperty("level", serializedOrgUnit.get("level").getAsInt());

      orgUnitsPayload.add(orgUnitRelationships);
    }
    else if (level.getSyncType() == DHIS2SyncLevel.Type.ORG_UNITS)
    {
      JsonObject orgUnitAttributes = serializedOrgUnit.deepCopy();

      // Drop all attributes related to the parent / hierarchy
      orgUnitAttributes.remove("parent");
      orgUnitAttributes.remove("path");
      orgUnitAttributes.remove("level");

      orgUnitsPayload.add(orgUnitAttributes);
    }

    if (isNew && level.getLevel() == 1 && this.ouLevel1.size() > 0)
    {
      this.ouLevel1.add(new OrganisationUnit());
    }
  }

  private void addWhereCriteria(StringBuilder builder, Map<String, Object> params)
  {
    final Date date = ( dhis2Config.getDate() == null ) ? this.todaysDate() : dhis2Config.getDate();

    final WhereCriteriaBuilder where = new WhereCriteriaBuilder();

    if (!this.dhis2Config.getSyncNonExistent())
    {
      where.AND("exists_cot CONTAINS (value=true AND :date BETWEEN startDate AND endDate )");
      params.put("date", date);
    }

    where.AND("invalid=false");

    builder.append(where.getSQL());
  }

  private long getCount(ServerGeoObjectType got)
  {
    final Map<String, Object> params = new HashMap<String, Object>();

    MdVertexDAOIF mdVertex = got.getMdVertex();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

    this.addWhereCriteria(statement, params);

    return new GraphQuery<Long>(statement.toString(), params).getSingleResult();
  }

  private List<VertexServerGeoObject> query(ServerGeoObjectType got, long skip, long pageSize)
  {
    final Map<String, Object> params = new HashMap<String, Object>();

    MdVertexDAOIF mdVertex = got.getMdVertex();
    MdAttributeDAOIF mdAttribute = MdAttributeDAO.getByKey(GeoVertex.CLASS + "." + GeoVertex.LASTUPDATEDATE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    this.addWhereCriteria(statement, params);
    statement.append(" ORDER BY " + mdAttribute.getColumnName() + ", oid ASC");
    statement.append(" SKIP " + skip + " LIMIT " + pageSize);

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString(), params);

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
}
