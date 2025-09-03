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
package net.geoprism.registry;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncLevel;
import net.geoprism.registry.etl.SynchronizationConfigFactory;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.etl.export.SeverGeoObjectJsonAdapters;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.permission.GPROrganizationPermissionService;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.view.JsonSerializable;

public class SynchronizationConfig extends SynchronizationConfigBase implements JsonSerializable
{
  public static enum Type {
    FHIR_IMPORT, FHIR_EXPORT, DHIS2, JENA, REVEAL
  }

  private static final long   serialVersionUID = -1221759231;

  private static final String SYSTEM_LABEL     = "systemLabel";

  public SynchronizationConfig()
  {
    super();
  }

  public void setOrganization(ServerOrganization value)
  {
    this.setOrganization(value.getOrganization());
  }

  public void populate(JsonObject json)
  {
    String orgCode = json.get(SynchronizationConfig.ORGANIZATION).getAsString();

    this.setOrganization(Organization.getByCode(orgCode));
    this.setSystem(json.get(SynchronizationConfig.SYSTEM).getAsString());
    this.setConfiguration(json.get(SynchronizationConfig.CONFIGURATION).getAsJsonObject().toString());
    this.setSynchronizationType(json.get(SynchronizationConfig.SYNCHRONIZATIONTYPE).getAsString());

    LocalizedValue label = LocalizedValue.fromJSON(json.get(SynchronizationConfig.LABEL).getAsJsonObject());

    RegistryLocalizedValueConverter.populate(this.getLabel(), label);
  }

  @Override
  public JsonObject toJSON()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Date.class, new SeverGeoObjectJsonAdapters.DateSerializer());
    builder.registerTypeAdapter(FhirSyncLevel.class, new FhirSyncLevel.Serializer());
    builder.registerTypeAdapter(DHIS2AttributeMapping.class, new DHIS2AttributeMapping.DHIS2AttributeMappingSerializer());
    builder.serializeNulls();

    Gson gson = builder.create();

    JsonObject object = new JsonObject();
    object.addProperty(SynchronizationConfig.OID, this.getOid());
    object.addProperty(SynchronizationConfig.ORGANIZATION, this.getOrganization().getCode());
    object.addProperty(SynchronizationConfig.SYSTEM, this.getSystem());
    object.add(SynchronizationConfig.LABEL, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getLabel()).toJSON());

    ExternalSystemSyncConfig config = this.toConfiguration();
    object.add(SynchronizationConfig.CONFIGURATION, gson.toJsonTree(config));

    ExternalSystem system = this.getExternalSystem();

    if (system != null)
    {
      LocalizedValue label = RegistryLocalizedValueConverter.convert(system.getEmbeddedComponent(ExternalSystem.LABEL));

      object.addProperty(SynchronizationConfig.TYPE, system.getClass().getSimpleName());
      object.addProperty(SynchronizationConfig.SYSTEM_LABEL, label.getValue());
    }

    return object;
  }

  public JsonObject getConfigurationJson()
  {
    JsonElement element = JsonParser.parseString(this.getConfiguration());

    return element.getAsJsonObject();
  }

  public ExternalSystem getExternalSystem()
  {
    return ExternalSystem.get(this.getSystem());
  }

  @SuppressWarnings("unchecked")
  public <T extends ExternalSystemSyncConfig> T toConfiguration()
  {
    ExternalSystem system = this.getExternalSystem();

    ExternalSystemSyncConfig config = SynchronizationConfigFactory.get(this.getSynchronizationType());
    config.setSystem(system);
    config.populate(this);

    return (T) config;
  }

}
