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
package net.geoprism.registry.service.business;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.CreatePermissionException;
import com.runwaysdk.session.ReadPermissionException;
import com.runwaysdk.session.WritePermissionException;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.OriginException;
import net.geoprism.registry.etl.export.GeoObjectExportFormat;
import net.geoprism.registry.etl.export.GeoObjectJsonExporter;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.ExternalId;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;

@Service
@Primary
public class GPRGeoObjectBusinessService extends GeoObjectBusinessService implements GPRGeoObjectBusinessServiceIF
{
  @Autowired
  private SearchService                    searchService;

  @Autowired
  private SourceAuthorityBusinessServiceIF authorityService;

  @Override
  public JsonObject getAll(String gotCode, String hierarchyCode, Date since, Boolean includeLevel, String format, String externalSystemId, Integer pageNumber, Integer pageSize)
  {
    GeoObjectExportFormat goef = null;
    if (format != null && format.length() > 0)
    {
      goef = GeoObjectExportFormat.valueOf(format);
    }

    Map<Type, Object> typeAdapters = null;

    GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(gotCode, hierarchyCode, since, includeLevel, goef, typeAdapters, pageSize, pageNumber);

    try
    {
      return exporter.export();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void apply(ServerGeoObjectIF sgo, boolean isImport, boolean validateOrigin)
  {
    final boolean isNew = sgo.getVertex().isNew() || sgo.getVertex().getObjectValue(GeoVertex.CREATEDATE) == null;

    try
    {
      super.apply(sgo, isImport, validateOrigin);
    }
    catch (CreatePermissionException ex)
    {
      CreateGeoObjectPermissionException goex = new CreateGeoObjectPermissionException();
      goex.setGeoObjectType(sgo.getType().getLabel().getValue());
      goex.setOrganization(sgo.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (WritePermissionException ex)
    {
      WriteGeoObjectPermissionException goex = new WriteGeoObjectPermissionException();
      goex.setGeoObjectType(sgo.getType().getLabel().getValue());
      goex.setOrganization(sgo.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (ReadPermissionException ex)
    {
      ReadGeoObjectPermissionException goex = new ReadGeoObjectPermissionException();
      goex.setGeoObjectType(sgo.getType().getLabel().getValue());
      goex.setOrganization(sgo.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }

    if (!sgo.getInvalid())
    {
      this.searchService.insert((VertexServerGeoObject) sgo, isNew);
    }
    else if (!isNew)
    {
      this.searchService.remove(sgo.getCode());
    }
  }

  @Override
  public Optional<VertexServerGeoObject> getByExternalId(String externalId, String authorityCode, ServerGeoObjectType type)
  {
    SourceAuthority authority = this.authorityService.getByCodeOrThrow(authorityCode);

    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    StringBuilder statement = new StringBuilder();

    if (type != null)
    {
      statement.append("SELECT FROM (");
    }

    statement.append("SELECT expand(in) FROM (");
    statement.append("SELECT expand(outE('" + mdEdge.getDBClassName() + "')[id = '" + externalId + "']) FROM :authority)");

    if (type != null)
    {
      statement.append(") WHERE @class='" + type.getMdVertexDAO().getDBClassName() + "'");
    }

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("authority", authority.getRID());

    VertexObject vo = query.getSingleResult();

    if (vo != null)
    {
      if (type == null)
      {
        type = ServerGeoObjectType.get((MdVertexDAOIF) vo.getMdClass());
      }

      return Optional.of(new VertexServerGeoObject(type, vo, new TreeMap<>()));
    }

    return Optional.empty();
  }

  private Optional<ExternalId> getExternalIdEdge(ServerGeoObjectIF sgo, SourceAuthority authority)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "')[out = :parent])";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", authority.getRID());
    query.setParameter("child", sgo.getVertex().getRID());

    return Optional.ofNullable(query.getSingleResult()).map(e -> new ExternalId(e));
  }

  @Override
  public List<ExternalId> getAllExternalIds(ServerGeoObjectIF sgo)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", sgo.getVertex().getRID());

    return query.getResults().stream().map(edge -> new ExternalId(edge)).collect(Collectors.toList());
  }

  @Override
  public void applyExternalId(ServerGeoObjectIF sgo, String authorityCode, String id, ImportStrategy importStrategy, boolean validateOrigin)
  {
    if (validateOrigin)
    {
      if (!sgo.getType().getOrigin().equals(GeoprismProperties.getOrigin()))
      {
        throw new OriginException();
      }
    }

    SourceAuthority authority = this.authorityService.getByCodeOrThrow(authorityCode);

    if (importStrategy.equals(ImportStrategy.NEW_ONLY))
    {
      ExternalId externalId = new ExternalId(sgo.getVertex().addParent(authority, GeoVertex.EXTERNAL_ID));
      externalId.setExternalId(id);
      externalId.apply();
    }
    else
    {
      ExternalId externalId = getExternalIdEdge(sgo, authority).orElseGet(() -> {
        return new ExternalId(sgo.getVertex().addParent(authority, GeoVertex.EXTERNAL_ID));
      });

      externalId.setExternalId(id);
      externalId.apply();
    }
  }

  @Override
  public void removeExternalId(ServerGeoObjectIF sgo, String authorityCode, boolean validateOrigin)
  {
    if (validateOrigin)
    {
      if (!sgo.getType().getOrigin().equals(GeoprismProperties.getOrigin()))
      {
        throw new OriginException();
      }
    }

    SourceAuthority authority = this.authorityService.getByCodeOrThrow(authorityCode);

    getExternalIdEdge(sgo, authority).ifPresent(ExternalId::delete);
  }

  @Override
  public String getExternalId(ServerGeoObjectIF sgo, String authorityCode)
  {
    SourceAuthority authority = this.authorityService.getByCodeOrThrow(authorityCode);

    return getExternalIdEdge(sgo, authority).map(e -> e.getExternalId()).orElse(null);
  }

  @Override
  public GeoObjectOverTime toGeoObjectOverTime(ServerGeoObjectIF sgo, boolean generateUid, boolean includeExternalIds)
  {
    GeoObjectOverTime geoObj = super.toGeoObjectOverTime(sgo, generateUid, includeExternalIds);

    if (includeExternalIds)
    {
      for (ExternalId id : getAllExternalIds(sgo))
      {
        geoObj.addAlternateId(id.toDTO());
      }
    }

    return geoObj;
  }

  @Override
  public GeoObject toGeoObject(ServerGeoObjectIF sgo, Date date, boolean includeExternalIds)
  {
    GeoObject geoObj = super.toGeoObject(sgo, date, includeExternalIds);

    if (includeExternalIds)
    {
      for (ExternalId id : getAllExternalIds(sgo))
      {
        geoObj.addAlternateId(id.toDTO());
      }
    }

    return geoObj;
  }

  @Override
  public boolean exists(GraphType graphType, String uid)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + graphType.getMdEdgeDAO().getDBClassName());
    statement.append(" WHERE uid = :uid");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("uid", uid);

    Long count = query.getSingleResult();

    return count > 0;
  }

}
