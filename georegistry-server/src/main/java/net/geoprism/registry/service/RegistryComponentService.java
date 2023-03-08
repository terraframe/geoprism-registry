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
package net.geoprism.registry.service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.permission.PermissionContext;

@Component
public class RegistryComponentService
{
  private RegistryService service = new RegistryService();

  public synchronized void initialize()
  {
    service.initialize();
  }

  public void refreshMetadataCache()
  {
    service.refreshMetadataCache();
  }

  public String oauthGetAll(String sessionId, String id)
  {
    return service.oauthGetAll(sessionId, id);
  }

  public String oauthGetPublic(String sessionId, String id)
  {
    return service.oauthGetPublic(sessionId, id);
  }

  public JsonObject initHierarchyManager(String sessionId, Boolean publicOnly)
  {
    return service.initHierarchyManager(sessionId, publicOnly);
  }

  public GeoObject getGeoObject(String sessionId, String uid, String geoObjectTypeCode, Date date)
  {
    return service.getGeoObject(sessionId, uid, geoObjectTypeCode, date);
  }

  public GeoObject getGeoObjectByCode(String sessionId, String code, String typeCode, Date date)
  {
    return service.getGeoObjectByCode(sessionId, code, typeCode, date);
  }

  public GeoObject createGeoObject(String sessionId, String jGeoObj, Date startDate, Date endDate)
  {
    return service.createGeoObject(sessionId, jGeoObj, startDate, endDate);
  }

  public GeoObject updateGeoObject(String sessionId, String jGeoObj, Date startDate, Date endDate)
  {
    return service.updateGeoObject(sessionId, jGeoObj, startDate, endDate);
  }

  public String[] getUIDS(String sessionId, Integer amount)
  {
    return service.getUIDS(sessionId, amount);
  }

  public List<GeoObjectType> getAncestors(String sessionId, String code, String hierarchyCode, Boolean includeInheritedTypes, Boolean includeChild)
  {
    return service.getAncestors(sessionId, code, hierarchyCode, includeInheritedTypes, includeChild);
  }

  public ChildTreeNode getChildGeoObjects(String sessionId, String parentCode, String parentGeoObjectTypeCode, String hierarchyCode, String[] childrenTypes, Boolean recursive, Date date)
  {
    return service.getChildGeoObjects(sessionId, parentCode, parentGeoObjectTypeCode, hierarchyCode, childrenTypes, recursive, date);
  }

  public ParentTreeNode getParentGeoObjects(String sessionId, String childCode, String childGeoObjectTypeCode, String hierarchyCode, String[] parentTypes, boolean recursive, Date date)
  {
    return service.getParentGeoObjects(sessionId, childCode, childGeoObjectTypeCode, hierarchyCode, parentTypes, recursive, date);
  }

  public OrganizationDTO[] getOrganizations(String sessionId, String[] codes)
  {
    return service.getOrganizations(sessionId, codes);
  }

  public OrganizationDTO createOrganization(String sessionId, String json)
  {
    return service.createOrganization(sessionId, json);
  }

  public OrganizationDTO updateOrganization(String sessionId, String json)
  {
    return service.updateOrganization(sessionId, json);
  }

  public void deleteOrganization(String sessionId, String code)
  {
    service.deleteOrganization(sessionId, code);
  }

  public GeoObjectType[] getGeoObjectTypes(String sessionId, String[] codes, PermissionContext context)
  {
    return service.getGeoObjectTypes(sessionId, codes, context);
  }

  public JsonObject serialize(String sessionId, GeoObjectType got)
  {
    return service.serialize(sessionId, got);
  }

  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    return service.createGeoObjectType(sessionId, gtJSON);
  }

  public void importTypes(String sessionId, String orgCode, InputStream istream)
  {
    service.importTypes(sessionId, orgCode, istream);
  }

  public GeoObjectType updateGeoObjectType(String sessionId, String gtJSON)
  {
    return service.updateGeoObjectType(sessionId, gtJSON);
  }

  public AttributeType createAttributeType(String sessionId, String geoObjectTypeCode, String attributeTypeJSON)
  {
    return service.createAttributeType(sessionId, geoObjectTypeCode, attributeTypeJSON);
  }

  public AttributeType updateAttributeType(String sessionId, String geoObjectTypeCode, String attributeTypeJSON)
  {
    return service.updateAttributeType(sessionId, geoObjectTypeCode, attributeTypeJSON);
  }

  public void deleteAttributeType(String sessionId, String gtId, String attributeName)
  {
    service.deleteAttributeType(sessionId, gtId, attributeName);
  }

  public Term createTerm(String sessionId, String parentTermCode, String termJSON)
  {
    return service.createTerm(sessionId, parentTermCode, termJSON);
  }

  public Term updateTerm(String sessionId, String parentTermCode, String termJSON)
  {
    return service.updateTerm(sessionId, parentTermCode, termJSON);
  }

  public void deleteTerm(String sessionId, String parentTermCode, String termCode)
  {
    service.deleteTerm(sessionId, parentTermCode, termCode);
  }

  public void deleteGeoObjectType(String sessionId, String code)
  {
    service.deleteGeoObjectType(sessionId, code);
  }

  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String parentTypeCode, String hierarchyCode, Date startDate, Date endDate)
  {
    return service.getGeoObjectSuggestions(sessionId, text, typeCode, parentCode, parentTypeCode, hierarchyCode, startDate, endDate);
  }

  public GeoObject newGeoObjectInstance(String sessionId, String geoObjectTypeCode)
  {
    return service.newGeoObjectInstance(sessionId, geoObjectTypeCode);
  }

  public String newGeoObjectInstance2(String sessionId, String geoObjectTypeCode)
  {
    return service.newGeoObjectInstance2(sessionId, geoObjectTypeCode);
  }

  public String newGeoObjectInstanceOverTime(String sessionId, String typeCode)
  {
    return service.newGeoObjectInstanceOverTime(sessionId, typeCode);
  }

  public JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode, Date date)
  {
    return service.getHierarchiesForGeoObject(sessionId, code, typeCode, date);
  }

  public JsonArray getLocales(String sessionId)
  {
    return service.getLocales(sessionId);
  }

  public String getCurrentLocale(String sessionId)
  {
    return service.getCurrentLocale(sessionId);
  }

  public CustomSerializer serializer(String sessionId)
  {
    return service.serializer(sessionId);
  }

  public String getGeoObjectBounds(String sessionId, GeoObject geoObject)
  {
    return service.getGeoObjectBounds(sessionId, geoObject);
  }

  public String getGeoObjectBoundsAtDate(String sessionId, GeoObject geoObject, Date date)
  {
    return service.getGeoObjectBoundsAtDate(sessionId, geoObject, date);
  }

  public GeoObjectOverTime getGeoObjectOverTimeByCode(String sessionId, String code, String typeCode)
  {
    return service.getGeoObjectOverTimeByCode(sessionId, code, typeCode);
  }

  public GeoObjectOverTime updateGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    return service.updateGeoObjectOverTime(sessionId, jGeoObj);
  }

  public GeoObjectOverTime createGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    return service.createGeoObjectOverTime(sessionId, jGeoObj);
  }

  public GeoObjectOverTime getGeoObjectOverTime(String sessionId, String id, String typeCode)
  {
    return service.getGeoObjectOverTime(sessionId, id, typeCode);
  }

  public List<GeoObject> search(String sessionId, String typeCode, String text, Date date)
  {
    return service.search(sessionId, typeCode, text, date);
  }

  public String getLocalizationMap(String sessionId)
  {
    return service.getLocalizationMap(sessionId);
  }

  public JsonObject configuration(String sessionId, String contextPath)
  {
    return service.configuration(sessionId, contextPath);
  }

}
