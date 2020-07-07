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
package net.geoprism.registry.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.ObjectHasDataException;
import net.geoprism.registry.Organization;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.etl.ExternalSystemSyncConfig;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.JsonSerializable;

public abstract class ExternalSystem extends ExternalSystemBase implements JsonSerializable
{
  private static final long serialVersionUID = 1516759164;

  public ExternalSystem()
  {
    super();
  }

  public abstract ExternalSystemSyncConfig configuration();

  public abstract boolean isExportSupported();

  @Override
  @Transaction
  public void apply()
  {
    Organization organization = this.getOrganization();

    if (organization != null)
    {
      SessionIF session = Session.getCurrentSession();

      if (session != null)
      {
        ServiceFactory.getRolePermissionService().enforceRA(session.getUser(), organization.getCode());
      }
    }

    super.apply();
  }

  @Override
  @Transaction
  public void delete()
  {
    if (getReferencedDataCount() > 0)
    {
      throw new ObjectHasDataException();
    }
    
    List<SynchronizationConfig> configs = SynchronizationConfig.getAll(this);

    for (SynchronizationConfig config : configs)
    {
      config.delete();
    }

    super.delete();
  }
  
  public long getReferencedDataCount()
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT COUNT(*) FROM " + mdEdge.getDBClassName());

    builder.append(" WHERE out = :system");
    
    final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());
    
    query.setParameter("system", this.getRID());

    return query.getSingleResult();
  }

  protected void populate(JsonObject json)
  {
    String orgCode = json.get(ExternalSystem.ORGANIZATION).getAsString();

    this.setId(json.get(ExternalSystem.ID).getAsString());
    this.setOrganization(Organization.getByCode(orgCode));

    LocalizedValue label = LocalizedValue.fromJSON(json.get(ExternalSystem.LABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(json.get(ExternalSystem.DESCRIPTION).getAsJsonObject());

    LocalizedValueConverter.populate(this, ExternalSystem.LABEL, label);
    LocalizedValueConverter.populate(this, ExternalSystem.DESCRIPTION, description);
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty("type", this.getMdClass().getTypeName());
    object.addProperty(ExternalSystem.OID, this.getOid());
    object.addProperty(ExternalSystem.ID, this.getId());
    object.addProperty(ExternalSystem.ORGANIZATION, this.getOrganization().getCode());
    object.add(ExternalSystem.LABEL, LocalizedValueConverter.convert(this.getEmbeddedComponent(LABEL)).toJSON());
    object.add(ExternalSystem.DESCRIPTION, LocalizedValueConverter.convert(this.getEmbeddedComponent(DESCRIPTION)).toJSON());

    return object;
  }

  public static List<ExternalSystem> getExternalSystemsForOrg(Integer pageNumber, Integer pageSize)
  {
    List<Organization> organizations = Organization.getUserAdminOrganizations();

    if (organizations.size() > 0)
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
      MdAttributeDAOIF oAttribute = mdVertex.definesAttribute(ExternalSystem.ORGANIZATION);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdVertex.getDBClassName());

      for (int i = 0; i < organizations.size(); i++)
      {
        if (i == 0)
        {
          builder.append(" WHERE " + oAttribute.getColumnName() + " = :org" + i);
        }
        else
        {
          builder.append(" OR " + oAttribute.getColumnName() + " = :org" + i);
        }
      }

      builder.append(" ORDER BY id");
      builder.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

      final GraphQuery<ExternalSystem> query = new GraphQuery<ExternalSystem>(builder.toString());

      for (int i = 0; i < organizations.size(); i++)
      {
        Organization organization = organizations.get(i);

        query.setParameter("org" + i, organization.getOid());
      }

      return query.getResults();
    }

    return new LinkedList<ExternalSystem>();
  }

  public static long getCount()
  {
    List<Organization> organizations = Organization.getUserAdminOrganizations();

    if (organizations.size() > 0)
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
      MdAttributeDAOIF oAttribute = mdVertex.definesAttribute(ExternalSystem.ORGANIZATION);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

      for (int i = 0; i < organizations.size(); i++)
      {
        if (i == 0)
        {
          builder.append(" WHERE " + oAttribute.getColumnName() + " = :org" + i);
        }
        else
        {
          builder.append(" OR " + oAttribute.getColumnName() + " = :org" + i);
        }
      }

      final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());

      for (int i = 0; i < organizations.size(); i++)
      {
        Organization organization = organizations.get(i);

        query.setParameter("org" + i, organization.getOid());
      }

      return query.getSingleResult();
    }

    return 0L;
  }

  public static ExternalSystem desieralize(JsonObject json)
  {
    String type = json.get("type").getAsString();
    String oid = json.has(ExternalSystem.OID) ? json.get(ExternalSystem.OID).getAsString() : null;

    ExternalSystem system = getExternalSystem(oid, type);
    system.populate(json);

    return system;
  }

  private static ExternalSystem getExternalSystem(String oid, String type)
  {
    if (oid != null && oid.length() > 0)
    {
      return ExternalSystem.get(oid);
    }

    if (type.equals(RevealExternalSystem.class.getSimpleName()))
    {
      return new RevealExternalSystem();
    }

    return new DHIS2ExternalSystem();
  }

  public static ExternalSystem getByExternalSystemId(String id)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
    MdAttributeDAOIF attribute = mdVertex.definesAttribute(ExternalSystem.ID);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + mdVertex.getDBClassName());

    builder.append(" WHERE " + attribute.getColumnName() + " = :id");

    final GraphQuery<ExternalSystem> query = new GraphQuery<ExternalSystem>(builder.toString());

    query.setParameter("id", id);

    ExternalSystem es = query.getSingleResult();

    if (es == null)
    {
      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setDataIdentifier(id);
      ex.setTypeLabel(mdVertex.getDisplayLabel(Session.getCurrentLocale()));
      ex.setAttributeLabel(attribute.getDisplayLabel(Session.getCurrentLocale()));
      throw ex;
    }

    return es;
  }

  public static List<ExternalSystem> getForOrganization(Organization organization)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
    MdAttributeDAOIF oAttribute = mdVertex.definesAttribute(ExternalSystem.ORGANIZATION);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + mdVertex.getDBClassName());
    builder.append(" WHERE " + oAttribute.getColumnName() + " = :org");
    builder.append(" ORDER BY id");

    final GraphQuery<ExternalSystem> query = new GraphQuery<ExternalSystem>(builder.toString());
    query.setParameter("org", organization.getOid());

    return query.getResults();
  }

}
