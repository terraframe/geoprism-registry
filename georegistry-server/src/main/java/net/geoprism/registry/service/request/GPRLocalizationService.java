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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.runwaysdk.MessageExceptionDTO;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdLocalizableInfo;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationExcelExporter;
import com.runwaysdk.localization.LocalizationExcelImporter;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.localization.configuration.AttributeLocalQueryCriteria;
import com.runwaysdk.localization.configuration.AttributeLocalTabConfiguration;
import com.runwaysdk.localization.configuration.ConfigurationBuilder;
import com.runwaysdk.localization.configuration.SpreadsheetConfiguration;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.Operations;
import com.runwaysdk.system.PostalCode;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.metadata.MdAttributeLocal;
import com.runwaysdk.system.metadata.MdAttributeLocalQuery;
import com.runwaysdk.system.metadata.MdLocalizable;
import com.runwaysdk.system.metadata.Metadata;
import com.runwaysdk.system.metadata.SupportedLocale;

import net.geoprism.registry.localization.DefaultLocaleView;
import net.geoprism.registry.localization.LocaleView;
import net.geoprism.registry.localization.LocalizationImportMessagesException;
import net.geoprism.registry.service.permission.RolePermissionService;

@Service
@Primary
public class GPRLocalizationService extends net.geoprism.localization.LocalizationService
{
  @Autowired
  private RolePermissionService permissions;

  @Autowired
  private GraphRepoServiceIF    repoService;

  public void importSpreadsheet(String sessionId, MultipartFile file)
  {
    try
    {
      importSpreadsheetInRequest(sessionId, file);
    }
    catch (MessageExceptionDTO e)
    {
      throwMessageError(sessionId, e);
    }
  }

  @Request(RequestType.SESSION)
  private void throwMessageError(String sessionId, MessageExceptionDTO e)
  {
    LocalizationImportMessagesException ex = new LocalizationImportMessagesException();
    ex.setMessages(StringUtils.join(e.getMessageStrings(), "\\n"));
    throw ex;
  }

  @Request(RequestType.SESSION)
  public void importSpreadsheetInRequest(String sessionId, MultipartFile file)
  {
    permissions.enforceSRA();

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

  private void refreshCaches(boolean refreshWMS)
  {
    if (refreshWMS)
    {
      new WMSService().createAllWMSLayers(true);
    }

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // Refresh the entire metadata cache
    this.repoService.refreshMetadataCache();
    SerializedListTypeCache.getInstance().clear();
  }

  @Request(RequestType.SESSION)
  public LocaleView editLocaleInRequest(String sessionId, String json)
  {
    permissions.enforceSRA();

    LocaleView view = LocaleView.fromJson(json);

    if (view.isDefaultLocale())
    {
      LocaleView lv = editDefaultLocaleInTransaction(view);

      refreshCaches(false);

      return lv;
    }
    else
    {
      SupportedLocaleIF supportedLocale = editLocaleInTransaction(view);

      refreshCaches(false);

      return LocaleView.fromSupportedLocale(supportedLocale);
    }
  }

  @Transaction
  private LocaleView editDefaultLocaleInTransaction(LocaleView view)
  {
    LocalizedValueStore lvs = LocalizedValueStore.getByKey(DefaultLocaleView.LABEL);

    lvs.lock();
    lvs.getStoreValue().setLocaleMap(view.getLabel().getLocaleMap());
    lvs.apply();

    view.getLabel().setValue(lvs.getStoreValue().getValue());

    return view;
  }

  @Transaction
  private SupportedLocaleIF editLocaleInTransaction(LocaleView view)
  {
    SupportedLocaleIF supportedLocale = (SupportedLocale) com.runwaysdk.localization.LocalizationFacade.getSupportedLocale(view.getLocale());

    supportedLocale.appLock();
    view.populate(supportedLocale);
    supportedLocale.apply();

    // Be careful what you put here. We definitely don't want to refresh any
    // caches until after the transaction is over, especially given that we are
    // holding onto a lot of database locks and such right now.

    // Don't build the view inside the transaction either. Unless you like
    // deadlocks
    // return LocaleView.fromSupportedLocale(supportedLocale);

    return supportedLocale;
  }

  @Request(RequestType.SESSION)
  public LocaleView installLocaleInRequest(String sessionId, String json)
  {
    permissions.enforceSRA();

    LocaleView view = LocaleView.fromJson(json);

    if (view.isDefaultLocale())
    {
      return view;
    }

    SupportedLocaleIF supportedLocale = installLocaleInTransaction(view);

    refreshCaches(true);

    return LocaleView.fromSupportedLocale(supportedLocale);
  }

  @Transaction
  private SupportedLocaleIF installLocaleInTransaction(LocaleView view)
  {
    SupportedLocaleIF supportedLocale = (SupportedLocale) com.runwaysdk.localization.LocalizationFacade.install(view.getLocale());

    supportedLocale.appLock();
    view.populate(supportedLocale);
    supportedLocale.apply();

    // Be careful what you put here. We definitely don't want to refresh any
    // caches until after the transaction is over, especially given that we are
    // holding onto a lot of database locks and such right now.

    // Don't build the view inside the transaction either. Unless you like
    // deadlocks
    // return LocaleView.fromSupportedLocale(supportedLocale);

    return supportedLocale;
  }

  @Request(RequestType.SESSION)
  public void uninstallLocaleInRequest(String sessionId, String json)
  {
    permissions.enforceSRA();

    LocaleView view = LocaleView.fromJson(json);

    uninstallLocaleInTransaction(view);

    refreshCaches(true);
  }

  @Transaction
  private void uninstallLocaleInTransaction(LocaleView view)
  {
    com.runwaysdk.localization.LocalizationFacade.uninstall(view.getLocale());

    // Be careful what you put here. We definitely don't want to refresh any
    // caches until after the transaction is over, especially given that we are
    // holding onto a lot of database locks and such right now.
  }

  @Request(RequestType.SESSION)
  public InputStream exportSpreadsheet(String sessionId)
  {
    permissions.enforceSRA();

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    BufferedOutputStream buffer = new BufferedOutputStream(bytes);

    LocalizationExcelExporter exporter = new LocalizationExcelExporter(buildConfig(), buffer);
    exporter.export();

    return new ByteArrayInputStream(bytes.toByteArray());
  }

  @Request(RequestType.SESSION)
  public void setLocale(String sessionId, String locale)
  {
    SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      if (locale != null && locale.length() > 0)
      {
        Set<String> locales = GPRLocalizationService.getLocaleNames();

        if (locales.contains(locale))
        {
          ( (Session) session ).setLocale(LocaleUtils.toLocale(locale));
        }
      }
      else
      {
        ( (Session) session ).setLocale(CommonProperties.getDefaultLocale());
      }
    }

  }

  public static synchronized Set<String> getLocaleNames()
  {
    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    Set<String> list = new HashSet<String>();

    list.add(MdAttributeLocalInfo.DEFAULT_LOCALE);

    for (Locale locale : locales)
    {
      list.add(locale.toString());
    }

    return list;
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
    myCriteria.entityKeyMustInclude("net.geoprism.ontology"); // Yes its
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
    myCriteria.entityKeyMustInclude("Roles.cgr"); // Yes its
                                                  // confusing but
                                                  // these are OR
                                                  // not AND
    localTabConfig.addQueryCriteria(myCriteria);
  }

}
