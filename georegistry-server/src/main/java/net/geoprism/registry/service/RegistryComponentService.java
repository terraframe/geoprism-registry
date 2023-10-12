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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.CGRApplication;
import net.geoprism.registry.permission.PermissionContext;

@Service
public class RegistryComponentService
{
  @Autowired
  private OrganizationServiceIF   organizationService;

  @Autowired
  private RegistryServiceIF       registryService;

  @Autowired
  private GeoObjectServiceIF      objectService;

  @Autowired
  private TermServiceIF           termService;

  @Autowired
  private GPRGeoObjectTypeService typeService;

  public String oauthGetAll(String sessionId, String id)
  {
    return registryService.oauthGetAll(sessionId, id);
  }

  public String oauthGetPublic(String sessionId, String id)
  {
    return registryService.oauthGetPublic(sessionId, id);
  }

  public JsonObject initHierarchyManager(String sessionId, Boolean publicOnly)
  {
    return registryService.initHierarchyManager(sessionId, publicOnly);
  }

  public GeoObject getGeoObject(String sessionId, String uid, String geoObjectTypeCode, Date date)
  {
    return this.objectService.getGeoObject(sessionId, uid, geoObjectTypeCode, date);
  }

  public GeoObject getGeoObjectByCode(String sessionId, String code, String typeCode, Date date)
  {
    return this.objectService.getGeoObjectByCode(sessionId, code, typeCode, date);
  }

  public GeoObject createGeoObject(String sessionId, String jGeoObj, Date startDate, Date endDate)
  {
    return this.objectService.createGeoObject(sessionId, jGeoObj, startDate, endDate);
  }

  public GeoObject updateGeoObject(String sessionId, String jGeoObj, Date startDate, Date endDate)
  {
    return this.objectService.updateGeoObject(sessionId, jGeoObj, startDate, endDate);
  }

  public String[] getUIDS(String sessionId, Integer amount)
  {
    return registryService.getUIDS(sessionId, amount);
  }

  public List<GeoObjectType> getAncestors(String sessionId, String code, String hierarchyCode, Boolean includeInheritedTypes, Boolean includeChild)
  {
    return this.typeService.getAncestors(sessionId, code, hierarchyCode, includeInheritedTypes, includeChild);
  }

  public ChildTreeNode getChildGeoObjects(String sessionId, String parentCode, String parentGeoObjectTypeCode, String hierarchyCode, String[] childrenTypes, Boolean recursive, Date date)
  {
    return this.objectService.getChildGeoObjects(sessionId, parentCode, parentGeoObjectTypeCode, hierarchyCode, childrenTypes, recursive, date);
  }

  public ParentTreeNode getParentGeoObjects(String sessionId, String childCode, String childGeoObjectTypeCode, String hierarchyCode, String[] parentTypes, boolean recursive, boolean includeInherited, Date date)
  {
    return this.objectService.getParentGeoObjects(sessionId, childCode, childGeoObjectTypeCode, hierarchyCode, parentTypes, recursive, includeInherited, date);
  }

  public OrganizationDTO[] getOrganizations(String sessionId, String[] codes)
  {
    return this.organizationService.getOrganizations(sessionId, codes);
  }

  public OrganizationDTO createOrganization(String sessionId, String json)
  {
    return this.organizationService.createOrganization(sessionId, json);
  }

  public OrganizationDTO updateOrganization(String sessionId, String json)
  {
    return this.organizationService.updateOrganization(sessionId, json);
  }

  public void deleteOrganization(String sessionId, String code)
  {
    this.organizationService.deleteOrganization(sessionId, code);
  }

  public JsonObject serialize(String sessionId, GeoObjectType got)
  {
    return registryService.serialize(sessionId, got);
  }

  public GeoObjectType[] getGeoObjectTypes(String sessionId, String[] codes, PermissionContext context)
  {
    return this.typeService.getGeoObjectTypes(sessionId, codes, context);
  }

  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    return this.typeService.createGeoObjectType(sessionId, gtJSON);
  }

  public void importTypes(String sessionId, String orgCode, InputStream istream)
  {
    this.typeService.importTypes(sessionId, orgCode, istream);
  }

  public InputStream exportTypes(String sessionId, String code)
  {
    return this.typeService.exportTypes(sessionId, code);
  }

  public GeoObjectType updateGeoObjectType(String sessionId, String gtJSON)
  {
    return this.typeService.updateGeoObjectType(sessionId, gtJSON);
  }

  public AttributeType createAttributeType(String sessionId, String geoObjectTypeCode, String attributeTypeJSON)
  {
    return this.typeService.createAttributeType(sessionId, geoObjectTypeCode, attributeTypeJSON);
  }

  public AttributeType updateAttributeType(String sessionId, String geoObjectTypeCode, String attributeTypeJSON)
  {
    return this.typeService.updateAttributeType(sessionId, geoObjectTypeCode, attributeTypeJSON);
  }

  public void deleteAttributeType(String sessionId, String gtId, String attributeName)
  {
    this.typeService.deleteAttributeType(sessionId, gtId, attributeName);
  }

  public Term createTerm(String sessionId, String parentTermCode, String termJSON)
  {
    return this.termService.createTerm(sessionId, parentTermCode, termJSON);
  }

  public Term updateTerm(String sessionId, String parentTermCode, String termJSON)
  {
    return this.termService.updateTerm(sessionId, parentTermCode, termJSON);
  }

  public void deleteTerm(String sessionId, String parentTermCode, String termCode)
  {
    this.termService.deleteTerm(sessionId, parentTermCode, termCode);
  }

  public void deleteGeoObjectType(String sessionId, String code)
  {
    this.typeService.deleteGeoObjectType(sessionId, code);
  }

  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String parentTypeCode, String hierarchyCode, Date startDate, Date endDate)
  {
    return registryService.getGeoObjectSuggestions(sessionId, text, typeCode, parentCode, parentTypeCode, hierarchyCode, startDate, endDate);
  }

  public GeoObject newGeoObjectInstance(String sessionId, String geoObjectTypeCode)
  {
    return this.objectService.newGeoObjectInstance(sessionId, geoObjectTypeCode);
  }

  public String newGeoObjectInstance2(String sessionId, String geoObjectTypeCode)
  {
    return this.objectService.newGeoObjectInstance2(sessionId, geoObjectTypeCode);
  }

  public String newGeoObjectInstanceOverTime(String sessionId, String typeCode)
  {
    return this.objectService.newGeoObjectInstanceOverTime(sessionId, typeCode);
  }

  public JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode, Date date)
  {
    return registryService.getHierarchiesForGeoObject(sessionId, code, typeCode, date);
  }

  public JsonArray getLocales(String sessionId)
  {
    return registryService.getLocales(sessionId);
  }

  public String getCurrentLocale(String sessionId)
  {
    return registryService.getCurrentLocale(sessionId);
  }

  public CustomSerializer serializer(String sessionId)
  {
    return registryService.serializer(sessionId);
  }

  public String getGeoObjectBounds(String sessionId, GeoObject geoObject)
  {
    return this.objectService.getGeoObjectBounds(sessionId, geoObject);
  }

  public String getGeoObjectBoundsAtDate(String sessionId, GeoObject geoObject, Date date)
  {
    return this.objectService.getGeoObjectBoundsAtDate(sessionId, geoObject, date);
  }

  public GeoObjectOverTime getGeoObjectOverTimeByCode(String sessionId, String code, String typeCode)
  {
    return this.objectService.getGeoObjectOverTimeByCode(sessionId, code, typeCode);
  }

  public GeoObjectOverTime updateGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    return this.objectService.updateGeoObjectOverTime(sessionId, jGeoObj);
  }

  public GeoObjectOverTime createGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    return this.objectService.createGeoObjectOverTime(sessionId, jGeoObj);
  }

  public GeoObjectOverTime getGeoObjectOverTime(String sessionId, String id, String typeCode)
  {
    return this.objectService.getGeoObjectOverTime(sessionId, id, typeCode);
  }

  public List<GeoObject> search(String sessionId, String typeCode, String text, Date date)
  {
    return registryService.search(sessionId, typeCode, text, date);
  }

  public String getLocalizationMap(String sessionId)
  {
    return registryService.getLocalizationMap(sessionId);
  }

  public JsonObject configuration(String sessionId, String contextPath)
  {
    return registryService.configuration(sessionId, contextPath);
  }

  public List<CGRApplication> getApplications(String sessionId)
  {
    return registryService.getApplications(sessionId);
  }
}
