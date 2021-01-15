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
package net.geoprism.registry.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;

import net.geoprism.GeoprismUser;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.Attribute;
import net.geoprism.dhis2.dhis2adapter.response.model.Option;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnitGroup;
import net.geoprism.dhis2.dhis2adapter.response.model.ValueType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.dhis2.DHIS2ServiceFactory;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.DataExportJobQuery;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportHistoryQuery;
import net.geoprism.registry.etl.export.HttpError;
import net.geoprism.registry.etl.export.LoginException;
import net.geoprism.registry.etl.export.UnexpectedRemoteResponse;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache;
import net.geoprism.registry.etl.export.dhis2.DHIS2OptionCache.IntegratedOptionSet;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.AttributeTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.view.JsonWrapper;
import net.geoprism.registry.view.Page;

public class SynchronizationConfigService
{
  @Request(RequestType.SESSION)
  public JsonObject page(String sessionId, Integer pageNumber, Integer pageSize) throws JSONException
  {
    long count = SynchronizationConfig.getCount();
    List<SynchronizationConfig> results = SynchronizationConfig.getSynchronizationConfigsForOrg(pageNumber, pageSize);

    return new Page<SynchronizationConfig>(count, pageNumber, pageSize, results).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String json) throws JSONException
  {
    JsonElement element = JsonParser.parseString(json);

    SynchronizationConfig config = SynchronizationConfig.deserialize(element.getAsJsonObject());
    config.apply();

    return config.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject get(String sessionId, String oid) throws JSONException
  {
    SynchronizationConfig config = SynchronizationConfig.get(oid);

    return config.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String oid)
  {
    SynchronizationConfig config = SynchronizationConfig.get(oid);
    Organization organization = config.getOrganization();

    ServiceFactory.getRolePermissionService().enforceRA(organization.getCode());

    config.delete();
  }

  @Request(RequestType.SESSION)
  public JsonObject getConfigForExternalSystem(String sessionId, String externalSystemId, String hierarchyTypeCode)
  {
    JsonObject ret = new JsonObject();

    // Add GeoObjectTypes
    GeoObjectType[] gots = ServiceFactory.getRegistryService().getGeoObjectTypes(sessionId, null, new String[] { hierarchyTypeCode }, PermissionContext.WRITE);
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    JsonArray jarray = new JsonArray();
    for (int i = 0; i < gots.length; ++i)
    {
      jarray.add(gots[i].toJSON(serializer));
    }

    ret.add("types", jarray);

    // Add DHIS2 OrgUnitGroups
    DHIS2ExternalSystem system = DHIS2ExternalSystem.get(externalSystemId);

    try
    {
      DHIS2TransportServiceIF dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(system);
      
      JsonArray jaGroups = new JsonArray();

      MetadataGetResponse<OrganisationUnitGroup> resp = dhis2.<OrganisationUnitGroup> metadataGet(OrganisationUnitGroup.class);

      List<OrganisationUnitGroup> groups = resp.getObjects();

      for (OrganisationUnitGroup group : groups)
      {
        JsonObject joGroup = new JsonObject();

        joGroup.addProperty("id", group.getId());

        joGroup.addProperty("name", group.getName());

        jaGroups.add(joGroup);
      }

      ret.add("orgUnitGroups", jaGroups);
    }
    catch (InvalidLoginException e)
    {
      LoginException cgrlogin = new LoginException(e);
      throw cgrlogin;
    }
    catch (HTTPException | UnexpectedResponseException | IllegalArgumentException e)
    {
      HttpError cgrhttp = new HttpError(e);
      throw cgrhttp;
    }

    return ret;
  }

  @Request(RequestType.SESSION)
  public JsonArray getCustomAttributeConfiguration(String sessionId, String dhis2SystemOid, String geoObjectTypeCode)
  {
    DHIS2ExternalSystem system = DHIS2ExternalSystem.get(dhis2SystemOid);

    JsonArray response = new JsonArray();

    ServerGeoObjectType got = ServerGeoObjectType.get(geoObjectTypeCode);

    Map<String, AttributeType> cgrAttrs = got.getAttributeMap();

    List<Attribute> dhis2Attrs = null;

    for (AttributeType cgrAttr : cgrAttrs.values())
    {
      if (!cgrAttr.getIsDefault())
      {
        JsonObject joAttr = new JsonObject();
        joAttr.addProperty("name", cgrAttr.getName());
        joAttr.addProperty("label", cgrAttr.getLabel().getValue());
        joAttr.addProperty("type", cgrAttr.getType());
        joAttr.addProperty("typeLabel", AttributeTypeMetadata.get().getTypeEnumDisplayLabel(cgrAttr.getType()));

        DHIS2TransportServiceIF dhis2;
        
        try
        {
          dhis2 = DHIS2ServiceFactory.buildDhis2TransportService(system);
        }
        catch (InvalidLoginException e)
        {
          LoginException cgrlogin = new LoginException(e);
          throw cgrlogin;
        }
        catch (HTTPException | UnexpectedResponseException | IllegalArgumentException e)
        {
          HttpError cgrhttp = new HttpError(e);
          throw cgrhttp;
        }

        if (dhis2Attrs == null)
        {
          dhis2Attrs = getDHIS2Attributes(dhis2);
        }

        DHIS2OptionCache optionCache = new DHIS2OptionCache(dhis2);

        JsonArray jaDhis2Attrs = new JsonArray();
        for (Attribute dhis2Attr : dhis2Attrs)
        {
          if (!dhis2Attr.getOrganisationUnitAttribute())
          {
            continue;
          }

          boolean valid = false;

          JsonObject jo = new JsonObject();

          if (cgrAttr instanceof AttributeBooleanType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.BOOLEAN) || dhis2Attr.getValueType().equals(ValueType.TRUE_ONLY) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeIntegerType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.INTEGER) || dhis2Attr.getValueType().equals(ValueType.INTEGER_POSITIVE) || dhis2Attr.getValueType().equals(ValueType.INTEGER_NEGATIVE) || dhis2Attr.getValueType().equals(ValueType.INTEGER_ZERO_OR_POSITIVE) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeFloatType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.NUMBER) || dhis2Attr.getValueType().equals(ValueType.UNIT_INTERVAL) || dhis2Attr.getValueType().equals(ValueType.PERCENTAGE) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeDateType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.DATE) || dhis2Attr.getValueType().equals(ValueType.DATETIME) || dhis2Attr.getValueType().equals(ValueType.TIME) || dhis2Attr.getValueType().equals(ValueType.AGE) ))
          {
            valid = true;
          }
          else if (cgrAttr instanceof AttributeTermType && dhis2Attr.getOptionSetId() != null)
          {
            valid = true;

            JsonArray jaDhis2Options = new JsonArray();

            IntegratedOptionSet set = optionCache.getOptionSet(dhis2Attr.getOptionSetId());

            SortedSet<Option> options = set.getOptions();

            for (Option option : options)
            {
              JsonObject joDhis2Option = new JsonObject();
              joDhis2Option.addProperty("code", option.getCode());
              joDhis2Option.addProperty("name", option.getName());
              joDhis2Option.addProperty("id", option.getName());
              jaDhis2Options.add(joDhis2Option);
            }

            jo.add("options", jaDhis2Options);
          }
          else if (cgrAttr instanceof AttributeCharacterType && dhis2Attr.getOptionSetId() == null && ( dhis2Attr.getValueType().equals(ValueType.TEXT) || dhis2Attr.getValueType().equals(ValueType.LONG_TEXT) || dhis2Attr.getValueType().equals(ValueType.LETTER) || dhis2Attr.getValueType().equals(ValueType.PHONE_NUMBER) || dhis2Attr.getValueType().equals(ValueType.EMAIL) || dhis2Attr.getValueType().equals(ValueType.USERNAME) || dhis2Attr.getValueType().equals(ValueType.URL) ))
          {
            valid = true;
          }

          if (valid)
          {
            jo.addProperty("dhis2Id", dhis2Attr.getId());
            jo.addProperty("code", dhis2Attr.getCode());
            jo.addProperty("name", dhis2Attr.getName());
            jaDhis2Attrs.add(jo);
          }
        }

        joAttr.add("dhis2Attrs", jaDhis2Attrs);

        if (cgrAttr instanceof AttributeTermType)
        {
          JsonArray terms = new JsonArray();

          List<Term> children = ( (AttributeTermType) cgrAttr ).getTerms();

          for (Term child : children)
          {
            JsonObject joTerm = new JsonObject();
            joTerm.addProperty("label", child.getLabel().getValue());
            joTerm.addProperty("code", child.getCode());
            terms.add(joTerm);
          }

          joAttr.add("terms", terms);
        }

        response.add(joAttr);
      }
    }

    return response;
  }

  private List<Attribute> getDHIS2Attributes(DHIS2TransportServiceIF dhis2)
  {
    try
    {
      MetadataGetResponse<Attribute> resp = dhis2.<Attribute> metadataGet(Attribute.class);

      if (!resp.isSuccess())
      {
        // if (resp.hasMessage())
        // {
        // ExportRemoteException ere = new ExportRemoteException();
        // ere.setRemoteError(resp.getMessage());
        // throw ere;
        // }
        // else
        // {
        UnexpectedRemoteResponse re = new UnexpectedRemoteResponse();
        throw re;
        // }
      }

      return resp.getObjects();
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

  @Request(RequestType.SESSION)
  public JsonObject edit(String sessionId, String oid)
  {
    JsonObject response = new JsonObject();

    if (oid != null && oid.length() > 0)
    {
      SynchronizationConfig config = SynchronizationConfig.lock(oid);

      response.add("config", config.toJSON());
    }

    JsonArray orgs = new JsonArray();

    List<Organization> organizations = Organization.getUserAdminOrganizations();

    for (Organization organization : organizations)
    {
      JsonArray hierarchies = new JsonArray();

      List<ServerHierarchyType> sHierachies = ServerHierarchyType.getForOrganization(organization);

      for (ServerHierarchyType hierarchy : sHierachies)
      {
        JsonObject object = new JsonObject();
        object.addProperty("label", hierarchy.getDisplayLabel().getValue());
        object.addProperty("code", hierarchy.getCode());

        hierarchies.add(object);
      }

      JsonArray systems = new JsonArray();

      List<ExternalSystem> esystems = ExternalSystem.getForOrganization(organization);

      for (ExternalSystem system : esystems)
      {
        if (system.isExportSupported())
        {
          LocalizedValue label = LocalizedValueConverter.convert(system.getEmbeddedComponent(ExternalSystem.LABEL));

          JsonObject object = new JsonObject();
          object.addProperty("label", label.getValue());
          object.addProperty("oid", system.getOid());
          object.addProperty("type", system.getMdClass().getTypeName());

          systems.add(object);
        }
      }

      JsonObject object = new JsonObject();
      object.addProperty("label", organization.getDisplayLabel().getValue());
      object.addProperty("code", organization.getCode());
      object.add("hierarchies", hierarchies);
      object.add("systems", systems);

      orgs.add(object);
    }

    response.add("orgs", orgs);

    return response;
  }

  @Request(RequestType.SESSION)
  public void unlock(String sessionId, String oid)
  {
    SynchronizationConfig.unlock(oid);
  }

  @Request(RequestType.SESSION)
  public JsonObject run(String sessionId, String oid)
  {
    SynchronizationConfig config = SynchronizationConfig.get(oid);

    ServiceFactory.getRolePermissionService().enforceRA(config.getOrganization().getCode());

    List<? extends DataExportJob> jobs = config.getJobs();

    DataExportJob job = jobs.get(0);
    job.appLock();
    job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
    job.apply();

    ExportHistory hist = job.start(config);
    GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());

    return serializeHistory(hist, user, job);
  }

  @Request(RequestType.SESSION)
  public JsonObject getJobs(String sessionId, String configId, Integer pageSize, Integer pageNumber)
  {
    QueryFactory qf = new QueryFactory();

    DataExportJobQuery jQuery = new DataExportJobQuery(qf);
    jQuery.WHERE(jQuery.getConfig().EQ(configId));

    ExportHistoryQuery ihq = new ExportHistoryQuery(qf);
    ihq.WHERE(ihq.job(jQuery));
    ihq.restrictRows(pageSize, pageNumber);
    ihq.ORDER_BY_DESC(ihq.getCreateDate());

    try (OIterator<? extends ExportHistory> it = ihq.getIterator())
    {
      LinkedList<JsonWrapper> results = new LinkedList<JsonWrapper>();

      while (it.hasNext())
      {
        ExportHistory hist = it.next();
        DataExportJob job = (DataExportJob) hist.getAllJob().getAll().get(0);

        GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());

        results.add(new JsonWrapper(serializeHistory(hist, user, job)));
      }

      return new Page<JsonWrapper>(ihq.getCount(), pageNumber, pageSize, results).toJSON();
    }
  }

  protected JsonObject serializeHistory(ExportHistory hist, GeoprismUser user, ExecutableJob job)
  {
    JsonObject jo = new JsonObject();

    jo.addProperty("jobType", job.getType());
    jo.addProperty("stage", hist.getStage().get(0).name());
    jo.addProperty("status", hist.getStatus().get(0).name());
    jo.addProperty("author", user.getUsername());
    jo.addProperty("createDate", formatDate(hist.getCreateDate()));
    jo.addProperty("lastUpdateDate", formatDate(hist.getLastUpdateDate()));
    jo.addProperty("workProgress", hist.getWorkProgress());
    jo.addProperty("workTotal", hist.getWorkTotal());
    jo.addProperty("historyId", hist.getOid());
    jo.addProperty("jobId", job.getOid());

    if (hist.getStatus().get(0).equals(AllJobStatus.FAILURE) && hist.getErrorJson().length() > 0)
    {
      String errorJson = hist.getErrorJson();
      JsonObject error = JsonParser.parseString(errorJson).getAsJsonObject();

      JsonObject exception = new JsonObject();
      exception.addProperty("type", error.get("type").getAsString());
      exception.addProperty("message", hist.getLocalizedError(Session.getCurrentLocale()));

      jo.add("exception", exception);
    }

    return jo;
  }

  public static String formatDate(Date date)
  {
    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT, Session.getCurrentLocale());
    // format.setTimeZone(TimeZone.getTimeZone("GMT"));

    return format.format(date);
  }

}
