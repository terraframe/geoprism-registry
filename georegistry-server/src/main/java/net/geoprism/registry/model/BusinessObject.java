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
package net.geoprism.registry.model;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.system.metadata.MdAttribute;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.conversion.LocalizedValueConverter;
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

  public String getLabel()
  {
    MdAttribute labelAttribute = this.type.getLabelAttribute();

    if (labelAttribute != null)
    {
      String attributeName = labelAttribute.getAttributeName();

      Object value = this.getObjectValue(attributeName);

      if (value != null)
      {
        if (value instanceof Date)
        {
          return GeoRegistryUtil.formatDate((Date) value, false);
        }

        return value.toString();
      }
    }

    return this.getCode();
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
    AttributeType at = this.type.getAttribute(attributeName);

    if (at instanceof AttributeLocalType)
    {
      LocalizedValueConverter.populate(this.vertex, attributeName, (LocalizedValue) value);
    }
    else
    {
      this.vertex.setValue(attributeName, value);
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getObjectValue(String attributeName)
  {
    AttributeType at = this.type.getAttribute(attributeName);

    if (at instanceof AttributeLocalType)
    {
      return (T) this.getValueLocalized(attributeName);
    }

    return this.vertex.getObjectValue(attributeName);
  }

  private LocalizedValue getValueLocalized(String attributeName)
  {
    GraphObject graphObject = vertex.getEmbeddedComponent(attributeName);

    if (graphObject == null)
    {
      return null;
    }

    return LocalizedValueConverter.convert(graphObject);
  }

  public JsonObject toJSON()
  {
    JsonObject data = new JsonObject();

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = this.type.getMdVertexDAO().definesAttributes();

    for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    {
      String attributeName = mdAttribute.definesAttribute();

      if (!attributeName.equals(CODE))
      {

        Object value = this.vertex.getObjectValue(attributeName);

        if (value != null)
        {
          if (mdAttribute instanceof MdAttributeTermDAOIF)
          {
            Classifier classifier = Classifier.get((String) value);

            data.addProperty(mdAttribute.definesAttribute(), classifier.getDisplayLabel().getValue());
          }
          else if (value instanceof Number)
          {
            data.addProperty(mdAttribute.definesAttribute(), (Number) value);
          }
          else if (value instanceof Boolean)
          {
            data.addProperty(mdAttribute.definesAttribute(), (Boolean) value);
          }
          else if (value instanceof String)
          {
            data.addProperty(mdAttribute.definesAttribute(), (String) value);
          }
          else if (value instanceof Character)
          {
            data.addProperty(mdAttribute.definesAttribute(), (Character) value);
          }
          else if (value instanceof Date)
          {
            data.addProperty(mdAttribute.definesAttribute(), GeoRegistryUtil.formatDate((Date) value, false));
          }
        }
      }
    }

    JsonObject object = new JsonObject();
    object.addProperty("code", this.getCode());
    object.addProperty("label", this.getLabel());
    object.add("data", data);

    return object;
  }

  public void apply()
  {
    this.vertex.apply();
  }

  public void delete()
  {
    this.vertex.delete();
  }

  public boolean exists(ServerGeoObjectIF geoObject)
  {
    if (geoObject != null && geoObject instanceof VertexServerGeoObject)
    {
      return getEdge(geoObject) != null;
    }

    return false;
  }

  protected EdgeObject getEdge(ServerGeoObjectIF geoObject)
  {
    VertexObject geoVertex = ( (VertexServerGeoObject) geoObject ).getVertex();

    String statement = "SELECT FROM " + this.type.getMdEdgeDAO().getDBClassName();
    statement += " WHERE out = :parent";
    statement += " AND in = :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", geoVertex.getRID());
    query.setParameter("child", this.getVertex().getRID());

    return query.getSingleResult();
  }

  public void addGeoObject(ServerGeoObjectIF geoObject)
  {
    if (geoObject != null && geoObject instanceof VertexServerGeoObject && !this.exists(geoObject))
    {
      VertexObject geoVertex = ( (VertexServerGeoObject) geoObject ).getVertex();

      geoVertex.addChild(this.vertex, this.type.getMdEdgeDAO()).apply();
    }
  }

  public void removeGeoObject(ServerGeoObjectIF geoObject)
  {
    if (geoObject != null && geoObject instanceof VertexServerGeoObject)
    {
      VertexObject geoVertex = ( (VertexServerGeoObject) geoObject ).getVertex();

      geoVertex.removeChild(this.vertex, this.type.getMdEdgeDAO());
    }
  }

  public List<VertexServerGeoObject> getGeoObjects()
  {
    List<VertexObject> geoObjects = this.vertex.getParents(this.type.getMdEdgeDAO(), VertexObject.class);

    return geoObjects.stream().map(geoVertex -> {
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) geoVertex.getMdClass();
      ServerGeoObjectType vertexType = ServerGeoObjectType.get(mdVertex);

      return new VertexServerGeoObject(vertexType, geoVertex);

    }).sorted((a, b) -> {
      return a.getDisplayLabel().getValue().compareTo(b.getDisplayLabel().getValue());
    }).collect(Collectors.toList());
  }

  public boolean exists(BusinessEdgeType type, BusinessObject parent, BusinessObject child)
  {
    return getEdge(type, parent, child) != null;
  }

  protected EdgeObject getEdge(BusinessEdgeType type, BusinessObject parent, BusinessObject child)
  {
    String statement = "SELECT FROM " + type.getMdEdgeDAO().getDBClassName();
    statement += " WHERE out = :parent";
    statement += " AND in = :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", parent.getVertex().getRID());
    query.setParameter("child", child.getVertex().getRID());

    return query.getSingleResult();
  }

  public void addParent(BusinessEdgeType type, BusinessObject parent)
  {
    if (parent != null && !this.exists(type, parent, this))
    {
      this.vertex.addParent(parent.getVertex(), type.getMdEdgeDAO()).apply();
    }
  }

  public void removeParent(BusinessEdgeType type, BusinessObject parent)
  {
    if (parent != null)
    {
      this.vertex.removeParent(parent.getVertex(), type.getMdEdgeDAO());
    }
  }

  public List<BusinessObject> getParents(BusinessEdgeType type)
  {
    List<VertexObject> vertexObjects = this.vertex.getParents(type.getMdEdgeDAO(), VertexObject.class);

    return vertexObjects.stream().map(vertex -> {
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) vertex.getMdClass();
      BusinessType businessType = BusinessType.getByMdVertex(mdVertex);

      return new BusinessObject(vertex, businessType);

    }).sorted((a, b) -> {
      return a.getLabel().compareTo(b.getLabel());
    }).collect(Collectors.toList());
  }

  public void addChild(BusinessEdgeType type, BusinessObject child)
  {
    if (child != null && !this.exists(type, this, child))
    {
      this.vertex.addChild(child.getVertex(), type.getMdEdgeDAO()).apply();
    }
  }

  public void removeChild(BusinessEdgeType type, BusinessObject child)
  {
    if (child != null)
    {
      this.vertex.removeChild(child.getVertex(), type.getMdEdgeDAO());
    }
  }

  public List<BusinessObject> getChildren(BusinessEdgeType type)
  {
    List<VertexObject> vertexObjects = this.vertex.getChildren(type.getMdEdgeDAO(), VertexObject.class);

    return vertexObjects.stream().map(vertex -> {
      MdVertexDAOIF mdVertex = (MdVertexDAOIF) vertex.getMdClass();
      BusinessType businessType = BusinessType.getByMdVertex(mdVertex);

      return new BusinessObject(vertex, businessType);

    }).sorted((a, b) -> {
      return a.getLabel().compareTo(b.getLabel());
    }).collect(Collectors.toList());
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
