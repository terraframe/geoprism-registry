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

import com.google.gson.JsonArray;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.RevealExternalSystem;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public interface ServerGeoObjectIF
{
  public ServerGeoObjectType getType();

  public GeoObject toGeoObject();

  public GeoObjectOverTime toGeoObjectOverTime();

  public String getCode();

  public void setCode(String code);

  public GeoObjectStatus getStatus();

  public void setStatus(GeoObjectStatus status);

  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate);

  public void setGeometry(Geometry geometry);

  public void setGeometry(Geometry geometry, Date startDate, Date endDate);

  public Geometry getGeometry();

  public String getUid();

  public void setUid(String uid);

  public String getRunwayId();

  public Object getValue(String attributeName);

  public Object getValue(String attributeName, Date date);

  public ValueOverTimeCollection getValuesOverTime(String attributeName);

  public void setValuesOverTime(String attributeName, ValueOverTimeCollection collection);

  public void setValue(String attributeName, Object value);

  public void setValue(String attributeName, Object value, Date startDate, Date endDate);

  public void setDisplayLabel(LocalizedValue label);

  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate);

  public LocalizedValue getDisplayLabel();

  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs();

  public String bbox(Date date);

  public ServerChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive);

  public ServerParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive);

  public ServerParentTreeNodeOverTime getParentsOverTime(String[] parentTypes, Boolean recursive);

  public void setParents(ServerParentTreeNodeOverTime parentsOverTime);

  public ServerParentTreeNode addChild(ServerGeoObjectIF child, ServerHierarchyType hierarchy);

  public ServerParentTreeNode addChild(ServerGeoObjectIF entity, ServerHierarchyType hierarchy, Date startDate, Date endDate);

  public void removeChild(ServerGeoObjectIF child, String hierarchyCode);

  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType);

  public ServerParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType, Date startDate, Date endDate);

  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType);

  public void lock();

  public void unlock();

  public void populate(GeoObject geoObject);

  public void populate(GeoObjectOverTime goTime);

  public void apply(boolean isImport);

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes);

  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy);

  public JsonArray getHierarchiesForGeoObject();

  public void setDate(Date date);

  public void createExternalId(ExternalSystem system, String id);

  public String getExternalId(ExternalSystem system);
}
