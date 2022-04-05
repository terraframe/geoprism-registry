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
package net.geoprism.registry.model;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class BusinessObject
{
  public static String CODE = "code";

  private VertexObject vertex;

  private BusinessType type;

  public BusinessObject(VertexObject vertex, BusinessType type)
  {
    this.vertex = vertex;
    this.type = type;
  }

  public BusinessType getType()
  {
    return type;
  }

  public VertexObject getVertex()
  {
    return vertex;
  }

  public String getCode()
  {
    return this.getObjectValue(DefaultAttribute.CODE.getName());
  }

  public void setCode(String code)
  {
    this.setValue(DefaultAttribute.CODE.getName(), code);
  }

  public void setValue(String attributeName, Object value)
  {
    this.vertex.setValue(attributeName, value);
  }

  public <T> T getObjectValue(String attributeName)
  {
    return this.vertex.getObjectValue(attributeName);
  }

  public JsonObject toJSON()
  {
    return new JsonObject();
  }

  public void apply()
  {
    this.vertex.apply();
  }

  public void delete()
  {
    this.vertex.delete();
  }

  public void setGeoObject(ServerGeoObjectIF geoObject)
  {
    if (geoObject instanceof VertexServerGeoObject)
    {
      VertexObject geoVertex = ( (VertexServerGeoObject) geoObject ).getVertex();

      this.vertex.setValue(BusinessType.GEO_OBJECT, geoVertex);
    }
  }

  public VertexServerGeoObject getGeoObject()
  {
    String oid = this.vertex.getObjectValue(BusinessType.GEO_OBJECT);

    if (oid != null)
    {
      VertexObject geoVertex = VertexObject.get(GeoVertex.CLASS, oid);
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) geoVertex.getMdClass();
      ServerGeoObjectType vertexType = ServerGeoObjectType.get(mdVertex);

      return new VertexServerGeoObject(vertexType, geoVertex);
    }

    return null;
  }

  public static BusinessObject newInstance(BusinessType type)
  {
    VertexObject vertex = VertexObject.instantiate(VertexObjectDAO.newInstance(type.getMdVertexDAO()));

    return new BusinessObject(vertex, type);
  }

  public static BusinessObject get(BusinessType type, String attributeName, Object value)
  {
    MdVertexDAOIF mdVertex = type.getMdVertexDAO();
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + mdAttribute.getColumnName() + " = :" + attributeName);

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter(attributeName, value);

    VertexObject result = query.getSingleResult();

    if (result != null)
    {
      return new BusinessObject(result, type);
    }

    return null;
  }

  public static BusinessObject getByCode(BusinessType type, Object value)
  {
    return BusinessObject.get(type, DefaultAttribute.CODE.getName(), value);
  }

}
