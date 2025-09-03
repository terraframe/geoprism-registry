/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either SynchronizationConfig 3 of the License, or (at
 * your option) any later SynchronizationConfig.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.SynchronizationConfig.Type;
import net.geoprism.registry.SynchronizationConfigQuery;
import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.etl.export.DataExportJob;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.service.permission.GPROrganizationPermissionService;
import net.geoprism.registry.service.permission.RolePermissionService;

@Service
public class SynchronizationConfigBusinessService implements SynchronizationConfigBusinessServiceIF
{

  @Autowired
  private RolePermissionService            rPermissions;

  @Autowired
  private GPROrganizationPermissionService oPermissions;

  @Override
  public List<SynchronizationConfig> getSynchronizations(Publish publish)
  {
    SynchronizationConfigQuery query = new SynchronizationConfigQuery(new QueryFactory());
    query.WHERE(query.getSynchronizationType().EQ(Type.JENA.name()));

    try (OIterator<? extends SynchronizationConfig> it = query.getIterator())
    {
      return it.getAll().stream().map(t -> (SynchronizationConfig) t) //
          .filter(s -> {
            JenaExportConfig config = s.toConfiguration();

            return config.getPublishUid().equals(publish.getUid());
          }).toList();
    }
  }

  @Override
  @Transaction
  public void apply(SynchronizationConfig config)
  {
    SessionIF session = Session.getCurrentSession();

    Organization organization = config.getOrganization();

    if (session != null && organization != null)
    {
      this.rPermissions.enforceRA(organization.getCode());
    }

    config.apply();

    if (config.isNew())
    {
      DataExportJob job = new DataExportJob();
      job.setConfig(config);
      job.apply();
    }
  }

  @Override
  @Transaction
  public void delete(SynchronizationConfig synchronization)
  {
    this.getJobs(synchronization).stream().forEach(DataExportJob::delete);

    synchronization.delete();
  }

  @Override
  public List<? extends DataExportJob> getJobs(SynchronizationConfig synchronization)
  {
    return DataExportJob.getAll(synchronization);
  }

  @Override
  public long getCount()
  {
    List<Organization> organizations = oPermissions.getUserAdminOrganizations();

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

  @Override
  public List<SynchronizationConfig> getSynchronizationConfigsForOrg(Integer pageNumber, Integer pageSize)
  {
    List<Organization> organizations = oPermissions.getUserAdminOrganizations();

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

  @Override
  public SynchronizationConfig deserialize(JsonObject json)
  {
    return deserialize(json, false);
  }

  @Override
  public SynchronizationConfig deserialize(JsonObject json, boolean lock)
  {
    String oid = json.has(SynchronizationConfig.OID) ? json.get(SynchronizationConfig.OID).getAsString() : null;

    SynchronizationConfig config = ( oid != null ? ( lock ? SynchronizationConfig.lock(oid) : SynchronizationConfig.get(oid) ) : new SynchronizationConfig() );
    config.populate(json);

    return config;
  }

  @Override
  public List<SynchronizationConfig> getAll(ExternalSystem system)
  {
    SynchronizationConfigQuery query = new SynchronizationConfigQuery(new QueryFactory());
    query.WHERE(query.getSystem().EQ(system.getOid()));

    try (OIterator<? extends SynchronizationConfig> it = query.getIterator())
    {
      return new LinkedList<SynchronizationConfig>(it.getAll());
    }
  }

}
