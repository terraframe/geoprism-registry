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

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.controller.RegistryController;
import net.geoprism.registry.controller.SynchronizationConfigController;
import net.geoprism.registry.permission.PermissionContext;

public class RegistryControllerWrapper extends TestControllerWrapper
{

  private RegistryController controller = new RegistryController();
  
  public RegistryControllerWrapper(TestRegistryAdapterClient adapter, ClientRequestIF clientRequest)
  {
    super(adapter, clientRequest);
  }
  
  public Set<String> getUIDs(int amount)
  {
    ResponseIF response = this.controller.getUIDs(this.clientRequest, amount);

    String sResp = responseToString(response);

    JsonArray ja = JsonParser.parseString(sResp).getAsJsonArray();

    Set<String> set = new HashSet<String>();
    for (int i = 0; i < ja.size(); ++i)
    {
      set.add(ja.get(i).getAsString());
    }

    return set;
  }
  
  public JsonArray getGeoObjectSuggestions(String text, String type, String parent, Date startDate, Date endDate, String parentTypeCode, String hierarchy)
  {
    return JsonParser.parseString(responseToString(this.controller.getGeoObjectSuggestions(clientRequest, text, type, parent, parentTypeCode, hierarchy, stringifyDate(startDate), stringifyDate(endDate)))).getAsJsonArray();
  }

  public AttributeType createAttributeType(String geoObjectTypeCode, String attributeTypeJSON)
  {
    return responseToAttributeType(this.controller.createAttributeType(clientRequest, geoObjectTypeCode, attributeTypeJSON));
  }

  public AttributeType updateAttributeType(String geoObjectTypeCode, String attributeTypeJSON)
  {
    return responseToAttributeType(this.controller.updateAttributeType(clientRequest, geoObjectTypeCode, attributeTypeJSON));
  }

  public Term createTerm(String parentTermCode, String termJSON)
  {
    return responseToTerm(this.controller.createTerm(clientRequest, parentTermCode, termJSON));
  }

  public Term updateTerm(String parentTermCode, String termJSON)
  {
    return responseToTerm(this.controller.updateTerm(clientRequest, parentTermCode, termJSON));
  }

  public void deleteTerm(String parentTermCode, String termCode)
  {
    this.controller.deleteTerm(clientRequest, parentTermCode, termCode);
  }

  public GeoObjectType createGeoObjectType(String gtJSON)
  {
    return responseToGeoObjectType(this.controller.createGeoObjectType(this.clientRequest, gtJSON));
  }

  public GeoObjectType updateGeoObjectType(String gtJSON)
  {
    return responseToGeoObjectType(this.controller.updateGeoObjectType(clientRequest, gtJSON));
  }

  public GeoObject getGeoObject(String registryId, String code, Date date)
  {
    return responseToGeoObject(this.controller.getGeoObject(this.clientRequest, registryId, code, stringifyDate(date)));
  }

  public GeoObjectOverTime getGeoObjectOverTime(String registryId, String typeCode)
  {
    return responseToGeoObjectOverTime(this.controller.getGeoObjectOverTime(clientRequest, registryId, typeCode));
  }

  public GeoObject getGeoObjectByCode(String code, String typeCode, Date date)
  {
    return responseToGeoObject(this.controller.getGeoObjectByCode(this.clientRequest, code, typeCode, stringifyDate(date)));
  }

  public GeoObjectOverTime getGeoObjectOverTimeByCode(String code, String typeCode)
  {
    return responseToGeoObjectOverTime(this.controller.getGeoObjectOverTimeByCode(clientRequest, code, typeCode));
  }

  public GeoObject createGeoObject(String jGeoObj, Date startDate, Date endDate)
  {
    try
    {
      return responseToGeoObject(this.controller.createGeoObject(this.clientRequest, jGeoObj, stringifyDate(startDate), stringifyDate(endDate)));
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public GeoObjectOverTime createGeoObjectOverTime(String jGeoObj)
  {
    return responseToGeoObjectOverTime(this.controller.createGeoObjectOverTime(this.clientRequest, jGeoObj));
  }

  public GeoObject updateGeoObject(String jGeoObj, Date startDate, Date endDate)
  {
    try
    {
      return responseToGeoObject(this.controller.updateGeoObject(this.clientRequest, jGeoObj, stringifyDate(startDate), stringifyDate(endDate)));
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public GeoObjectOverTime updateGeoObjectOverTime(String jGeoObj)
  {
    return responseToGeoObjectOverTime(this.controller.updateGeoObjectOverTime(this.clientRequest, jGeoObj));
  }

  public GeoObjectType[] getGeoObjectTypes(String[] codes, String[] hierarchies, PermissionContext pc)
  {
    String saCodes = this.serialize(codes);
    String saHierarchies = this.serialize(hierarchies);

    if (pc == null)
    {
      pc = PermissionContext.READ;
    }

    return responseToGeoObjectTypes(this.controller.getGeoObjectTypes(this.clientRequest, saCodes, saHierarchies, pc.name()));
  }

  public HierarchyType[] getHierarchyTypes(String[] codes)
  {
    String saCodes = this.serialize(codes);

    return responseToHierarchyTypes(this.controller.getHierarchyTypes(this.clientRequest, saCodes, PermissionContext.READ.name()));
  }

  public JsonObject hierarchyManagerInit()
  {
    return JsonParser.parseString(responseToString(this.controller.init(this.clientRequest))).getAsJsonObject();
  }

  public JsonArray getHierarchiesForGeoObjectOverTime(String code, String typeCode)
  {
    return JsonParser.parseString(responseToString(this.controller.getHierarchiesForGeoObjectOverTime(this.clientRequest, code, typeCode))).getAsJsonArray();
  }

  public JsonArray listGeoObjectTypes()
  {
    RestBodyResponse response = (RestBodyResponse) this.controller.listGeoObjectTypes(this.clientRequest, true);
    return (JsonArray) response.serialize();
  }

  public ChildTreeNode getChildGeoObjects(String parentId, String parentTypeCode, Date date, String[] childrenTypes, boolean recursive)
  {
    String saChildrenTypes = this.serialize(childrenTypes);

    return responseToChildTreeNode(this.controller.getChildGeoObjects(this.clientRequest, parentId, parentTypeCode, stringifyDate(date), saChildrenTypes, recursive));
  }

  public ParentTreeNode getParentGeoObjects(String childId, String childTypeCode, Date date, String[] parentTypes, boolean recursive)
  {
    String saParentTypes = this.serialize(parentTypes);

    try
    {
      return responseToParentTreeNode(this.controller.getParentGeoObjects(this.clientRequest, childId, childTypeCode, stringifyDate(date), saParentTypes, recursive));
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }

  public JsonObject getConfigForExternalSystem(String externalSystemId, String hierarchyTypeCode)
  {
    return JsonParser.parseString(responseToString(new SynchronizationConfigController().getConfigForExternalSystem(this.clientRequest, externalSystemId, hierarchyTypeCode))).getAsJsonObject();
  }

  public HierarchyType addToHierarchy(String hierarchyCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    return responseToHierarchyType(this.controller.addToHierarchy(this.clientRequest, hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode));
  }

  public ParentTreeNode addChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef)
  {
    return responseToParentTreeNode(this.controller.addChild(this.clientRequest, parentId, parentTypeCode, childId, childTypeCode, hierarchyRef));
  }

  public void removeChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef)
  {
    this.controller.removeChild(this.clientRequest, parentId, parentTypeCode, childId, childTypeCode, hierarchyRef);
  }

}
