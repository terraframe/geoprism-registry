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
package net.geoprism.registry.test;

import java.util.Date;
import java.util.Set;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.session.Request;

import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;

public class TestRegistryAdapterClient extends RegistryAdapter
{
  private static final long serialVersionUID = -433764579483802366L;

  public RegistryControllerWrapper controller;

  public ClientRequestIF    clientRequest;

  public TestRegistryAdapterClient()
  {
    super(new TestRegistryClientIdService());
    ( (TestRegistryClientIdService) this.getIdService() ).setClient(this);
  }

  public void setClientRequest(ClientRequestIF clientRequest)
  {
    this.clientRequest = clientRequest;
    this.controller = new RegistryControllerWrapper(this, this.clientRequest);
  }

  /**
   * Clears the metadata cache and populates it with the metadata from the
   * common geo-registry.
   * 
   */
  public void refreshMetadataCache()
  {
    refreshRequestMetadataCache();

    this.getMetadataCache().rebuild();

    GeoObjectType[] gots = this.getGeoObjectTypes(new String[] {}, new String[] {}, PermissionContext.READ);

    for (GeoObjectType got : gots)
    {
      this.getMetadataCache().addGeoObjectType(got);
    }

    HierarchyType[] hts = this.getHierarchyTypes(new String[] {});

    for (HierarchyType ht : hts)
    {
      this.getMetadataCache().addHierarchyType(ht);
    }
  }

  @Request
  private void refreshRequestMetadataCache()
  {
    ServiceFactory.getRegistryService().refreshMetadataCache();
  }

  public Set<String> getGeoObjectUids(int amount)
  {
    return this.controller.getUIDs(amount);
  }

  public JsonArray getGeoObjectSuggestions(String text, String type, String parent, Date startDate, Date endDate, String parentTypeCode, String hierarchy)
  {
    return this.controller.getGeoObjectSuggestions(text, type, parent, startDate, endDate, parentTypeCode, hierarchy);
  }

  public AttributeType createAttributeType(String geoObjectTypeCode, String attributeTypeJSON)
  {
    return this.controller.createAttributeType(geoObjectTypeCode, attributeTypeJSON);
  }

  public AttributeType updateAttributeType(String geoObjectTypeCode, String attributeTypeJSON)
  {
    return this.controller.updateAttributeType(geoObjectTypeCode, attributeTypeJSON);
  }

  public Term createTerm(String parentTermCode, String termJSON)
  {
    return this.controller.createTerm(parentTermCode, termJSON);
  }

  public Term updateTerm(String parentTermCode, String termJSON)
  {
    return this.controller.updateTerm(parentTermCode, termJSON);
  }

  public void deleteTerm(String parentTermCode, String termCode)
  {
    this.controller.deleteTerm(parentTermCode, termCode);
  }

  public GeoObjectType createGeoObjectType(String gtJSON)
  {
    return this.controller.createGeoObjectType(gtJSON);
  }

  public GeoObjectType updateGeoObjectType(String gtJSON)
  {
    return this.controller.updateGeoObjectType(gtJSON);
  }

  public GeoObject getGeoObject(String registryId, String code, Date date)
  {
    return this.controller.getGeoObject(registryId, code, date);
  }

  public GeoObjectOverTime getGeoObjectOverTime(String registryId, String typeCode)
  {
    return this.controller.getGeoObjectOverTime(registryId, typeCode);
  }

  public GeoObject getGeoObjectByCode(String code, String typeCode, Date date)
  {
    return this.getGeoObjectByCode(code, typeCode, date);
  }

  public GeoObjectOverTime getGeoObjectOverTimeByCode(String code, String typeCode)
  {
    return this.controller.getGeoObjectOverTimeByCode(code, typeCode);
  }

  public GeoObject createGeoObject(String jGeoObj, Date startDate, Date endDate)
  {
    return this.controller.createGeoObject(jGeoObj, startDate, endDate);
  }

  public GeoObjectOverTime createGeoObjectOverTime(String jGeoObj)
  {
    return this.controller.createGeoObjectOverTime(jGeoObj);
  }

  public GeoObject updateGeoObject(String jGeoObj, Date startDate, Date endDate)
  {
    return this.controller.updateGeoObject(jGeoObj, startDate, endDate);
  }

  public GeoObjectOverTime updateGeoObjectOverTime(String jGeoObj)
  {
    return this.controller.updateGeoObjectOverTime(jGeoObj);
  }

  public GeoObjectType[] getGeoObjectTypes(String[] codes, String[] hierarchies, PermissionContext pc)
  {
    return this.controller.getGeoObjectTypes(codes, hierarchies, pc);
  }

  public HierarchyType[] getHierarchyTypes(String[] codes)
  {
    return this.controller.getHierarchyTypes(codes);
  }

  public JsonObject hierarchyManagerInit()
  {
    return this.controller.hierarchyManagerInit();
  }

  public JsonArray getHierarchiesForGeoObjectOverTime(String code, String typeCode)
  {
    return this.controller.getHierarchiesForGeoObjectOverTime(code, typeCode);
  }

  public JsonArray listGeoObjectTypes()
  {
    return this.controller.listGeoObjectTypes();
  }

  public ChildTreeNode getChildGeoObjects(String parentId, String parentTypeCode, Date date, String[] childrenTypes, boolean recursive)
  {
    return this.controller.getChildGeoObjects(parentId, parentTypeCode, date, childrenTypes, recursive);
  }

  public ParentTreeNode getParentGeoObjects(String childId, String childTypeCode, Date date, String[] parentTypes, boolean recursive)
  {
    return this.controller.getParentGeoObjects(childId, childTypeCode, date, parentTypes, recursive);
  }

  public JsonObject getConfigForExternalSystem(String externalSystemId, String hierarchyTypeCode)
  {
    return this.controller.getConfigForExternalSystem(externalSystemId, hierarchyTypeCode);
  }

  public HierarchyType addToHierarchy(String hierarchyCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return this.controller.addToHierarchy(hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);
  }

  public ParentTreeNode addChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef)
  {
    return this.controller.addChild(parentId, parentTypeCode, childId, childTypeCode, hierarchyRef);
  }

  public void removeChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef)
  {
    this.controller.removeChild(parentId, parentTypeCode, childId, childTypeCode, hierarchyRef);
  }

}
