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
package net.geoprism.registry;

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

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.etl.FhirSyncLevel;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.JsonSerializable;

public class SynchronizationConfig extends SynchronizationConfigBase implements JsonSerializable
{
  private static final long   serialVersionUID = -1221759231;

  private static final String SYSTEM_LABEL     = "systemLabel";

  public SynchronizationConfig()
  {
    super();
  }

  @Override
  @Transaction
  public void apply()
  {
    SessionIF session = Session.getCurrentSession();

    Organization organization = this.getOrganization();

    if (session != null && organization != null)
    {
      ServiceFactory.getRolePermissionService().enforceRA(organization.getCode());
    }

    super.apply();

    if (this.isNew())
    {
      DataExportJob job = new DataExportJob();
      job.setConfig(this);
      job.apply();
    }
  }

  @Override
  @Transaction
  public void delete()
  {
    List<? extends DataExportJob> jobs = getJobs();

    for (DataExportJob job : jobs)
    {
      job.delete();
    }

    super.delete();
  }

  public List<? extends DataExportJob> getJobs()
  {
    return DataExportJob.getAll(this);
  }

  private void populate(JsonObject json)
  {
    String orgCode = json.get(SynchronizationConfig.ORGANIZATION).getAsString();

    this.setOrganization(Organization.getByCode(orgCode));
    this.setSystem(json.get(SynchronizationConfig.SYSTEM).getAsString());
    this.setConfiguration(json.get(SynchronizationConfig.CONFIGURATION).getAsJsonObject().toString());

    if (json.has(SynchronizationConfig.ISIMPORT))
    {
      this.setIsImport(json.get(SynchronizationConfig.ISIMPORT).getAsBoolean());
    }
    else
    {
      this.setIsImport(false);
    }

    LocalizedValue label = LocalizedValue.fromJSON(json.get(SynchronizationConfig.LABEL).getAsJsonObject());

    LocalizedValueConverter.populate(this.getLabel(), label);
  }

  @Override
  public JsonObject toJSON()
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(FhirSyncLevel.class, new FhirSyncLevel.Serializer());
    builder.registerTypeAdapter(DHIS2AttributeMapping.class, new DHIS2AttributeMapping.DHIS2AttributeMappingDeserializer());

    Gson gson = builder.create();

    JsonObject object = new JsonObject();
    object.addProperty(SynchronizationConfig.OID, this.getOid());
    object.addProperty(SynchronizationConfig.ORGANIZATION, this.getOrganization().getCode());
    object.addProperty(SynchronizationConfig.SYSTEM, this.getSystem());
    object.add(SynchronizationConfig.LABEL, LocalizedValueConverter.convert(this.getLabel()).toJSON());
    object.addProperty(SynchronizationConfig.ISIMPORT, this.getIsImport() != null ? this.getIsImport() : false);

    ExternalSystemSyncConfig config = this.buildConfiguration();
    object.add(SynchronizationConfig.CONFIGURATION, gson.toJsonTree(config));

    ExternalSystem system = this.getExternalSystem();

    if (system != null)
    {
      LocalizedValue label = LocalizedValueConverter.convert(system.getEmbeddedComponent(ExternalSystem.LABEL));

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

  public ExternalSystemSyncConfig buildConfiguration()
  {
    ExternalSystem system = this.getExternalSystem();

    ExternalSystemSyncConfig config = system.configuration(this.getIsImport());
    config.setSystem(system);
    config.populate(this);

    return config;
  }

  public static long getCount()
  {
    List<Organization> organizations = Organization.getUserAdminOrganizations();

    if (organizations.size() > 0)
    {
      SynchronizationConfigQuery query = new SynchronizationConfigQuery(new QueryFactory());

      for (int i = 0; i < organizations.size(); i++)
      {
        Organization organization = organizations.get(i);
        query.OR(query.getOrganization().EQ(organization));
      }

      return query.getCount();
    }

    return 0;
  }

  public static List<SynchronizationConfig> getSynchronizationConfigsForOrg(Integer pageNumber, Integer pageSize)
  {
    List<Organization> organizations = Organization.getUserAdminOrganizations();

    if (organizations.size() > 0)
    {
      SynchronizationConfigQuery query = new SynchronizationConfigQuery(new QueryFactory());

      for (int i = 0; i < organizations.size(); i++)
      {
        Organization organization = organizations.get(i);
        query.OR(query.getOrganization().EQ(organization));
      }

      query.ORDER_BY_ASC(query.getLabel().localize());
      query.restrictRows(pageSize, pageNumber);

      try (OIterator<? extends SynchronizationConfig> it = query.getIterator())
      {
        return new LinkedList<SynchronizationConfig>(it.getAll());
      }
    }

    return new LinkedList<SynchronizationConfig>();
  }

  public static SynchronizationConfig deserialize(JsonObject json)
  {
    String oid = json.has(SynchronizationConfig.OID) ? json.get(SynchronizationConfig.OID).getAsString() : null;

    SynchronizationConfig config = ( oid != null ? SynchronizationConfig.get(oid) : new SynchronizationConfig() );
    config.populate(json);

    return config;
  }

  public static List<SynchronizationConfig> getAll(ExternalSystem system)
  {
    SynchronizationConfigQuery query = new SynchronizationConfigQuery(new QueryFactory());
    query.WHERE(query.getSystem().EQ(system.getOid()));

    try (OIterator<? extends SynchronizationConfig> it = query.getIterator())
    {
      return new LinkedList<SynchronizationConfig>(it.getAll());
    }
  }
}
