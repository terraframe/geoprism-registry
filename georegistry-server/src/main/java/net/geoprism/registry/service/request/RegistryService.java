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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.json.RunwayJsonAdapters;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.account.OauthServer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.gis.geoserver.GeoserverProperties;
import net.geoprism.registry.CGRApplication;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.model.localization.DefaultLocaleView;
import net.geoprism.registry.model.localization.LocaleView;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

@Service
public class RegistryService implements RegistryServiceIF
{
  @Autowired
  private GeoObjectBusinessServiceIF     service;

  @Autowired
  private GeoObjectTypeBusinessServiceIF gTypeService;

  @Autowired
  private HierarchyTypeServiceIF         hTypeService;

  @Autowired
  private OrganizationServiceIF          orgService;

  private String buildOauthServerUrl(OauthServer server)
  {
    try
    {
      String redirect = GeoprismProperties.getRemoteServerUrl() + "api/session/ologin";

      JSONObject state = new JSONObject();
      state.put(OauthServerIF.SERVER_ID, server.getOid());

      AuthenticationRequestBuilder builder = OAuthClientRequest.authorizationLocation(server.getAuthorizationLocation());
      builder.setClientId(server.getClientId());
      builder.setRedirectURI(redirect);
      builder.setResponseType("code");
      builder.setState(state.toString());

      OAuthClientRequest request = builder.buildQueryMessage();

      return request.getLocationUri();
    }
    catch (OAuthSystemException | JSONException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Request(RequestType.SESSION)
  public String oauthGetAll(String sessionId, String id)
  {
    final JsonArray ja = new JsonArray();

    if (id == null || id.length() == 0)
    {
      OauthServer[] servers = OauthServer.getAll();

      for (OauthServer server : servers)
      {
        Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
        JsonObject json = (JsonObject) gson2.toJsonTree(server);

        json.addProperty("url", buildOauthServerUrl(server));

        ja.add(json);
      }
    }
    else
    {
      OauthServer server = OauthServer.get(id);

      Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
      JsonObject json = (JsonObject) gson2.toJsonTree(server);

      json.addProperty("url", buildOauthServerUrl(server));

      ja.add(json);
    }

    return ja.toString();
  }

  @Override
  @Request(RequestType.SESSION)
  public String oauthGetPublic(String sessionId, String id)
  {
    final JsonArray ja = new JsonArray();

    if (id == null || id.length() == 0)
    {
      OauthServer[] servers = OauthServer.getAll();

      // Arrays.asList(servers).stream().filter(s ->
      // FhirExternalSystem.isFhirOauth() )

      for (OauthServer server : servers)
      {
        if (!FhirExternalSystem.isFhirOauth(server))
        {
          JsonObject json = new JsonObject();

          json.add("label", RegistryLocalizedValueConverter.convert(server.getDisplayLabel()).toJSON());
          json.addProperty("url", buildOauthServerUrl(server));

          ja.add(json);
        }
      }
    }
    else
    {
      OauthServer server = OauthServer.get(id);

      JsonObject json = new JsonObject();

      json.add("label", RegistryLocalizedValueConverter.convert(server.getDisplayLabel()).toJSON());
      json.addProperty("url", buildOauthServerUrl(server));

      ja.add(json);
    }

    return ja.toString();
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject initHierarchyManager(String sessionId, Boolean publicOnly)
  {
    final JsonArray types = new JsonArray();
    final JsonArray hierarchies = new JsonArray();
    final JsonArray organizations = new JsonArray();
    final CustomSerializer serializer = this.serializer(sessionId);

    OrganizationDTO[] orgDtos = orgService.getOrganizations(sessionId, null);

    if (publicOnly && UserInfo.isPublicUser())
    {
      ServiceFactory.getMetadataCache().getAllGeoObjectTypes().stream().filter(got -> !got.getIsPrivate()).forEach(got -> {
        types.add(got.toJSON(serializer));
      });
    }
    else
    {
      List<GeoObjectType> gots = this.gTypeService.getGeoObjectTypes(null, PermissionContext.READ);
      HierarchyType[] hts = this.hTypeService.getHierarchyTypes(sessionId, null, PermissionContext.READ);

      for (GeoObjectType got : gots)
      {
        JsonObject joGot = got.toJSON(serializer);

        JsonArray relatedHiers = new JsonArray();

        for (HierarchyType ht : hts)
        {
          List<HierarchyNode> hns = ht.getRootGeoObjectTypes();

          for (HierarchyNode hn : hns)
          {
            if (hn.hierarchyHasGeoObjectType(got.getCode(), true))
            {
              relatedHiers.add(ht.getCode());
            }
          }
        }

        joGot.add("relatedHierarchies", relatedHiers);

        types.add(joGot);
      }

      for (HierarchyType ht : hts)
      {
        hierarchies.add(ht.toJSON(serializer));
      }

    }

    for (OrganizationDTO dto : orgDtos)
    {
      if (dto.getEnabled() == null || dto.getEnabled())
      {
        organizations.add(dto.toJSON(serializer));
      }
    }

    JsonObject response = new JsonObject();
    response.add("types", types);
    response.add("hierarchies", hierarchies);
    response.add("organizations", organizations);
    response.add("locales", this.getLocales(sessionId));

    return response;
  }

  @Override
  @Request(RequestType.SESSION)
  public String[] getUIDS(String sessionId, Integer amount)
  {
    return ServiceFactory.getIdService().getUids(amount);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String parentTypeCode, String hierarchyCode, Date startDate, Date endDate)
  {
    return VertexServerGeoObject.getGeoObjectSuggestions(text, typeCode, parentCode, parentTypeCode, hierarchyCode, startDate, endDate);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode, Date date)
  {
    ServerGeoObjectIF geoObject = this.service.getGeoObjectByCode(code, typeCode);

    return this.service.getHierarchiesForGeoObject(geoObject, date);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject serialize(String sessionId, GeoObjectType got)
  {
    return got.toJSON(this.serializer(sessionId));
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonArray getLocales(String sessionId)
  {
    return getLocales();
  }

  private JsonArray getLocales()
  {
    Set<SupportedLocaleIF> locales = LocalizationFacade.getSupportedLocales();

    JsonArray array = new JsonArray();

    array.add(new DefaultLocaleView().toJson());

    for (SupportedLocaleIF locale : locales)
    {
      array.add(LocaleView.fromSupportedLocale(locale).toJson());
    }

    return array;
  }

  @Override
  @Request(RequestType.SESSION)
  public String getCurrentLocale(String sessionId)
  {
    Locale locale = Session.getCurrentLocale();

    return locale.toString();
  }

  @Override
  @Request(RequestType.SESSION)
  public CustomSerializer serializer(String sessionId)
  {
    Locale locale = Session.getCurrentLocale();

    return new LocaleSerializer(locale);
  }

  @Override
  @Request(RequestType.SESSION)
  public List<GeoObject> search(String sessionId, String typeCode, String text, Date date)
  {
    List<GeoObject> objects = new LinkedList<GeoObject>();

    if (date == null)
    {
      date = ValueOverTime.INFINITY_END_DATE;
    }

    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    ServerGeoObjectQuery query = this.service.createQuery(type, date);
    query.setRestriction(new ServerSynonymRestriction(text, date));
    query.setLimit(20);

    List<ServerGeoObjectIF> results = query.getResults();

    for (ServerGeoObjectIF result : results)
    {
      objects.add(this.service.toGeoObject(result, date));
    }

    return objects;
  }

  @Override
  @Request(RequestType.SESSION)
  public String getLocalizationMap(String sessionId)
  {
    return new net.geoprism.localization.LocalizationService().getAllView();
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject configuration(String sessionId, String contextPath)
  {
    Locale locale = Session.getCurrentLocale();

    JsonObject config = new JsonObject();
    config.addProperty("contextPath", contextPath);
    config.addProperty("locale", locale.toString());
    config.add("locales", this.getLocales());
    config.addProperty("searchEnabled", GeoregistryProperties.isSearchEnabled());
    config.addProperty("graphVisualizerEnabled", GeoregistryProperties.isGraphVisualizerEnabled());
    config.addProperty("enableBusinessData", GeoregistryProperties.isBusinessDataEnabled());
    config.addProperty("enableLabeledPropertyGraph", GeoregistryProperties.isLabeledPropertyGraphEnabled());
    config.addProperty("mapboxAccessToken", GeoserverProperties.getMapboxglAccessToken());
    config.add("defaultMapBounds", JsonParser.parseString(GeoregistryProperties.getDefaultMapBounds()));
    config.add("localization", JsonParser.parseString(getLocalizationMap(sessionId)));
    config.addProperty("googleanalyticstoken", GeoregistryProperties.getGoogleAnalyticsToken());
    config.addProperty("customFont", GeoregistryProperties.getCustomFont());

    return config;
  }

  @Override
  @Request(RequestType.SESSION)
  public List<CGRApplication> getApplications(String sessionId)
  {
    return CGRApplication.getApplications();
  }
}
