/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.ServletException;

import org.apache.commons.lang3.LocaleUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.MdLocalizableInfo;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.localization.LocalizationExcelExporter;
import com.runwaysdk.localization.LocalizationExcelImporter;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.configuration.AttributeLocalQueryCriteria;
import com.runwaysdk.localization.configuration.AttributeLocalTabConfiguration;
import com.runwaysdk.localization.configuration.ConfigurationBuilder;
import com.runwaysdk.localization.configuration.SpreadsheetConfiguration;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.InputStreamResponse;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Operations;
import com.runwaysdk.system.PostalCode;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.metadata.MdAttributeLocal;
import com.runwaysdk.system.metadata.MdAttributeLocalQuery;
import com.runwaysdk.system.metadata.MdLocalizable;
import com.runwaysdk.system.metadata.Metadata;

import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.WMSService;

@Controller(url = "localization")
public class RegistryLocalizationController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF importSpreadsheet(ClientRequestIF request, @RequestParamter(name = "file") MultipartFileParameter file)
  {
    importSpreadsheetInRequest(request.getSessionId(), file);

    return new RestResponse();
  }

  @Request(RequestType.SESSION)
  public void importSpreadsheetInRequest(String sessionId, MultipartFileParameter file)
  {
    try
    {
      LocalizationExcelImporter importer = new LocalizationExcelImporter(buildConfig(), file.getInputStream());
      importer.doImport();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getNewLocaleInformation(ClientRequestIF request) throws IOException, ServletException
  {
    JSONObject json = new JSONObject();

    JSONArray languages = new JSONArray();
    JSONArray countries = new JSONArray();

    for (Locale locale : LocalizationFacade.getAvailableLanguagesSorted())
    {
      JSONObject jobj = new JSONObject();
      jobj.put("key", locale.getLanguage());
      jobj.put("label", locale.getDisplayLanguage());
      languages.put(jobj);
    }

    for (Locale locale : LocalizationFacade.getAvailableCountriesSorted())
    {
      JSONObject jobj = new JSONObject();
      jobj.put("key", locale.getCountry());
      jobj.put("label", locale.getDisplayCountry());
      countries.put(jobj);
    }

    json.put("languages", languages);
    json.put("countries", countries);

    return new RestBodyResponse(json);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-locales")
  public ResponseIF getLocales(ClientRequestIF request) throws IOException, ServletException
  {
    final JsonArray locales = ServiceFactory.getRegistryService().getLocales(request.getSessionId());

    return new RestBodyResponse(locales);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF installLocale(ClientRequestIF request, @RequestParamter(name = "language") String language, @RequestParamter(name = "country") String country, @RequestParamter(name = "variant") String variant) throws IOException, ServletException
  {
    installLocaleInRequest(request.getSessionId(), language, country, variant);

    return new RestResponse();
  }

  @Request(RequestType.SESSION)
  public void installLocaleInRequest(String sessionId, String language, String country, String variant)
  {
    String localeString = language;
    if (country != null)
    {
      localeString += "_" + country;
      if (variant != null)
      {
        localeString += "_" + variant;
      }
    }

    Locale locale = LocaleUtils.toLocale(localeString);

    com.runwaysdk.LocalizationFacade.install(locale);

    new WMSService().createAllWMSLayers(true);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // Refresh the entire metadata cache
    ServiceFactory.getRegistryService().refreshMetadataCache();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF exportSpreadsheet(ClientRequestIF request)
  {
    return exportSpreadsheetInRequest(request.getSessionId());
  }

  @Request(RequestType.SESSION)
  public InputStreamResponse exportSpreadsheetInRequest(String sessionId)
  {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    BufferedOutputStream buffer = new BufferedOutputStream(bytes);

    LocalizationExcelExporter exporter = new LocalizationExcelExporter(buildConfig(), buffer);
    exporter.export();

    ByteArrayInputStream is = new ByteArrayInputStream(bytes.toByteArray());

    return new InputStreamResponse(is, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "localization.xlsx");
  }

  private SpreadsheetConfiguration buildConfig()
  {
    ConfigurationBuilder builder = new ConfigurationBuilder();

    addRegistryExceptions(builder);

    builder.addLocalizedValueStoreTab("Core Exceptions", LocalizedValueStore.TAG_NAME_ALL_RUNWAY_EXCEPTIONS);

    builder.addLocalizedValueStoreTab("UI Text", Arrays.asList(LocalizedValueStore.TAG_NAME_UI_TEXT));

    addRegistryMetadata(builder);

    return builder.build();
  }

  private void addRegistryExceptions(ConfigurationBuilder builder)
  {
    MdAttributeLocal exceptionsLocal = (MdAttributeLocal) BusinessFacade.get(MdLocalizable.getMessageMd());
    ArrayList<MdAttributeLocal> exceptionsLocalAL = new ArrayList<MdAttributeLocal>();
    exceptionsLocalAL.add(exceptionsLocal);
    AttributeLocalTabConfiguration localTabConfig = builder.addAttributeLocalTab("Exceptions", exceptionsLocal.getAttributeName(), exceptionsLocalAL);

    AttributeLocalQueryCriteria myCriteria = new AttributeLocalQueryCriteria();
    myCriteria.definingTypeMustNotInclude(GeoEntity.CLASS);
    myCriteria.definingTypeMustNotInclude(LocalizedValueStore.CLASS);
    myCriteria.definingTypeMustNotInclude(PostalCode.CLASS);
    myCriteria.definingTypeMustNotInclude(Operations.CLASS);
    myCriteria.entityKeyMustInclude("net.geoprism.registry");
    myCriteria.entityKeyMustInclude("com.runwaysdk.localization");
    myCriteria.entityKeyMustInclude("net.geoprism.gis");
    myCriteria.entityKeyMustInclude("net.geoprism.ontology");
    myCriteria.entityKeyMustInclude("net.geoprism.registry"); // Yes its
                                                              // confusing but
                                                              // these are OR
                                                              // not AND
    localTabConfig.addQueryCriteria(myCriteria);
  }

  private void addRegistryMetadata(ConfigurationBuilder builder)
  {
    QueryFactory qf = new QueryFactory();

    MdAttributeLocalQuery localQuery = new MdAttributeLocalQuery(qf);

    // localQuery.WHERE(localQuery.getKeyName().LIKE("net.geoprism.registry%"));
    //
    // localQuery.WHERE(localQuery.getAttributeName().EQ("displayLabel"));

    localQuery.WHERE(localQuery.getAttributeName().NE(MdLocalizableInfo.MESSAGE));
    localQuery.WHERE(localQuery.getAttributeName().NE(Metadata.DESCRIPTION));

    ArrayList<MdAttributeLocal> locals = new ArrayList<MdAttributeLocal>();
    OIterator<? extends MdAttributeLocal> it = localQuery.getIterator();
    try
    {
      while (it.hasNext())
      {
        locals.add(it.next());
      }
    }
    finally
    {
      it.close();
    }

    AttributeLocalTabConfiguration localTabConfig = builder.addDynamicAttributeLocalTab("Registry Metadata", locals);

    AttributeLocalQueryCriteria myCriteria = new AttributeLocalQueryCriteria();
    myCriteria.definingTypeMustNotInclude(GeoEntity.CLASS);
    myCriteria.definingTypeMustNotInclude(LocalizedValueStore.CLASS);
    myCriteria.definingTypeMustNotInclude(PostalCode.CLASS);
    myCriteria.definingTypeMustNotInclude(Operations.CLASS);
    myCriteria.entityKeyMustInclude("net.geoprism.registry");
    myCriteria.entityKeyMustInclude("net.geoprism.registry"); // Yes its
                                                              // confusing but
                                                              // these are OR
                                                              // not AND
    localTabConfig.addQueryCriteria(myCriteria);
  }
}
