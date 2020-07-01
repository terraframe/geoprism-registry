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
package net.geoprism.registry.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.model.postgres.TreeServerGeoObject;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

/**
 * A wrapper class around a relational server geo object and a vertex server geo
 * object. Used to populate both the relational object and the vertex object
 * when importing data from the shapefile importer
 * 
 * @author terraframe
 */
public class CompositeServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF, VertexComponent
{
  private ServerGeoObjectIF     rSGO;

  private VertexServerGeoObject vSGO;

  public CompositeServerGeoObject(ServerGeoObjectIF rSGO, VertexServerGeoObject vSGO)
  {
    super();
    this.rSGO = rSGO;
    this.vSGO = vSGO;
  }

  public TreeServerGeoObject getRelationalServerGeoObject()
  {
    return (TreeServerGeoObject) this.rSGO;
  }

  public VertexServerGeoObject getVertexServerGeoObject()
  {
    return vSGO;
  }

  @Override
  public VertexObject getVertex()
  {
    return this.vSGO.getVertex();
  }

  @Override
  public GeoObject toGeoObject()
  {
    return this.rSGO.toGeoObject();
  }

  @Override
  public GeoObjectOverTime toGeoObjectOverTime()
  {
    return this.vSGO.toGeoObjectOverTime();
  }

  @Override
  public Date getCreateDate()
  {
    return this.rSGO.getCreateDate();
  }

  @Override
  public Date getLastUpdateDate()
  {
    return this.rSGO.getLastUpdateDate();
  }

  @Override
  public void setCode(String code)
  {
    this.rSGO.setCode(code);
    this.vSGO.setCode(code);
  }

  @Override
  public String getCode()
  {
    return this.rSGO.getCode();
  }

  @Override
  public void setUid(String uid)
  {
    this.rSGO.setUid(uid);
    this.vSGO.setUid(uid);
  }

  @Override
  public String getUid()
  {
    return this.rSGO.getUid();
  }

  @Override
  public void setGeometry(Geometry geometry)
  {
    this.rSGO.setGeometry(geometry);
    this.vSGO.setGeometry(geometry);
  }

  @Override
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    this.rSGO.setGeometry(geometry, startDate, endDate);
    this.vSGO.setGeometry(geometry, startDate, endDate);
  }

  @Override
  public Geometry getGeometry()
  {
    return this.rSGO.getGeometry();
  }

  @Override
  public void setStatus(GeoObjectStatus status)
  {
    this.rSGO.setStatus(status);
    this.vSGO.setStatus(status);
  }

  @Override
  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate)
  {
    this.rSGO.setStatus(status, startDate, endDate);
    this.vSGO.setStatus(status, startDate, endDate);
  }

  @Override
  public GeoObjectStatus getStatus()
  {
    return this.rSGO.getStatus();
  }

  @Override
  public void setDisplayLabel(LocalizedValue label)
  {
    this.rSGO.setDisplayLabel(label);
    this.vSGO.setDisplayLabel(label);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate)
  {
    this.rSGO.setDisplayLabel(value, startDate, endDate);
    this.vSGO.setDisplayLabel(value, startDate, endDate);
  }

  @Override
  public LocalizedValue getDisplayLabel()
  {
    return this.rSGO.getDisplayLabel();
  }

  @Override
  public String getRunwayId()
  {
    return this.rSGO.getRunwayId();
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.rSGO.getType();
  }

  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return this.rSGO.getMdAttributeDAOs();
  }

  @Override
  public Object getValue(String attributeName)
  {
    return this.rSGO.getValue(attributeName);
  }

  @Override
  public Object getValue(String attributeName, Date date)
  {
    return this.vSGO.getValue(attributeName, date);
  }

  @Override
  public ValueOverTimeCollection getValuesOverTime(String attributeName)
  {
    return this.vSGO.getValuesOverTime(attributeName);
  }

  @Override
  public void setValuesOverTime(String attributeName, ValueOverTimeCollection collection)
  {
    this.vSGO.setValuesOverTime(attributeName, collection);
  }

  @Override
  public void setValue(String attributeName, Object value)
  {
    this.rSGO.setValue(attributeName, value);
    this.vSGO.setValue(attributeName, value);
  }

  @Override
  public void setValue(String attributeName, Object value, Date startDate, Date endDate)
  {
    this.rSGO.setValue(attributeName, value, startDate, endDate);
    this.vSGO.setValue(attributeName, value, startDate, endDate);
  }

  @Override
  public String bbox(Date date)
  {
    return this.vSGO.bbox(date);
  }

  @Override
  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    return this.rSGO.getHierarchyTypeMap(relationshipTypes);
  }

  @Override
  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy)
  {
    return this.rSGO.getAncestorMap(hierarchy);
  }

  @Override
  public ServerChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    return this.rSGO.getChildGeoObjects(childrenTypes, recursive);
  }

  @Override
  public ServerParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    return this.vSGO.getParentGeoObjects(parentTypes, recursive);
  }

  @Override
  public ServerParentTreeNodeOverTime getParentsOverTime(String[] parentTypes, Boolean recursive)
  {
    return this.vSGO.getParentsOverTime(parentTypes, recursive);
  }

  @Override
  public void setParents(ServerParentTreeNodeOverTime parentsOverTime)
  {
    this.vSGO.setParents(parentsOverTime);
  }

  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    ServerParentTreeNode ptn = this.rSGO.addChild(cChild.rSGO, hierarchy);
    this.vSGO.addChild(cChild.vSGO, hierarchy);

    return ptn;
  }

  @Override
  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy, Date startDate, Date endDate)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    ServerParentTreeNode ptn = this.rSGO.addChild(cChild.rSGO, hierarchy, startDate, endDate);
    this.vSGO.addChild(cChild.vSGO, hierarchy, startDate, endDate);

    return ptn;
  }

  @Override
  public void removeChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    CompositeServerGeoObject cChild = (CompositeServerGeoObject) child;

    this.rSGO.removeChild(cChild.rSGO, hierarchyCode);
    this.vSGO.removeChild(cChild.vSGO, hierarchyCode);
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    this.rSGO.removeParent(cParent.rSGO, hierarchyType);
    this.vSGO.removeParent(cParent.vSGO, hierarchyType);
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    ServerParentTreeNode ptn = this.rSGO.addParent(cParent.rSGO, hierarchyType);
    this.vSGO.addParent(cParent.vSGO, hierarchyType);

    return ptn;
  }

  @Override
  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate)
  {
    CompositeServerGeoObject cParent = (CompositeServerGeoObject) parent;

    ServerParentTreeNode ptn = this.rSGO.addParent(cParent.rSGO, hierarchyType, startDate, endDate);
    this.vSGO.addParent(cParent.vSGO, hierarchyType, startDate, endDate);

    return ptn;
  }

  @Override
  public void lock()
  {
    this.rSGO.lock();
    this.vSGO.lock();
  }

  @Override
  public void unlock()
  {
    this.rSGO.unlock();
    this.vSGO.unlock();
  }

  @Override
  public void populate(GeoObject geoObject)
  {
    this.rSGO.populate(geoObject);
    this.vSGO.populate(geoObject);
  }

  @Override
  public void populate(GeoObjectOverTime goTime)
  {
    this.rSGO.populate(goTime);
    this.vSGO.populate(goTime);
  }

  @Override
  public void apply(boolean isImport)
  {
    this.rSGO.apply(isImport);
    this.vSGO.apply(isImport);
  }

  @Override
  public void setDate(Date date)
  {
    this.rSGO.setDate(date);
    this.vSGO.setDate(date);
  }

  @Override
  public void createExternalId(ExternalSystem system, String id)
  {
    this.vSGO.createExternalId(system, id);
  }

  @Override
  public String getExternalId(ExternalSystem system)
  {
    return this.vSGO.getExternalId(system);
  }
}
