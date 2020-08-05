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
package net.geoprism.registry.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.AbstractResponseSerializer;
import com.runwaysdk.mvc.AbstractRestResponse;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.controller.RegistryController;
import net.geoprism.registry.permission.PermissionContext;

public class TestRegistryAdapterClient extends RegistryAdapter
{
  private static final long serialVersionUID = -433764579483802366L;

  public RegistryController controller;

  public ClientRequestIF    clientRequest;

  public TestRegistryAdapterClient()
  {
    super(new TestRegistryClientIdService());
    ( (TestRegistryClientIdService) this.getIdService() ).setClient(this);

    this.controller = new RegistryController();
  }

  public void setClientRequest(ClientRequestIF clientRequest)
  {
    this.clientRequest = clientRequest;
  }

  /**
   * Clears the metadata cache and populates it with the metadata from the
   * common geo-registry.
   * 
   */
  public void refreshMetadataCache()
  {
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

  public Set<String> getGeoObjectUids(int amount)
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

  public JSONArray getGeoObjectSuggestions(String text, String type, String parent, String hierarchy, String date)
  {
    try
    {
      return new JSONArray(responseToString(this.controller.getGeoObjectSuggestions(clientRequest, text, type, parent, hierarchy, date)));
    }
    catch (JSONException | ParseException e)
    {
      throw new RuntimeException(e);
    }
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
  
  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    return responseToGeoObjectType(this.controller.createGeoObjectType(this.clientRequest, gtJSON));
  }
  
  public GeoObjectType updateGeoObjectType(String gtJSON)
  {
    return responseToGeoObjectType(this.controller.updateGeoObjectType(clientRequest, gtJSON));
  }

  public GeoObject getGeoObject(String registryId, String code)
  {
    return responseToGeoObject(this.controller.getGeoObject(this.clientRequest, registryId, code));
  }

  public GeoObject getGeoObjectByCode(String code, String typeCode)
  {
    return responseToGeoObject(this.controller.getGeoObjectByCode(this.clientRequest, code, typeCode));
  }

  public GeoObjectOverTime getGeoObjectOverTimeByCode(String code, String typeCode)
  {
    return responseToGeoObjectOverTime(this.controller.getGeoObjectOverTimeByCode(this.clientRequest, code, typeCode));
  }

  public GeoObject createGeoObject(String jGeoObj)
  {
    return responseToGeoObject(this.controller.createGeoObject(this.clientRequest, jGeoObj));
  }

  public GeoObject updateGeoObject(String jGeoObj)
  {
    return responseToGeoObject(this.controller.updateGeoObject(this.clientRequest, jGeoObj));
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

  public JSONArray getHierarchiesForGeoObjectOverTime(String code, String typeCode)
  {
    return new JSONArray(responseToString(this.controller.getHierarchiesForGeoObjectOverTime(this.clientRequest, code, typeCode)));
  }

  public JsonArray listGeoObjectTypes()
  {
    RestBodyResponse response = (RestBodyResponse) this.controller.listGeoObjectTypes(this.clientRequest, true);
    return (JsonArray) response.serialize();
  }

  public ChildTreeNode getChildGeoObjects(String parentId, String parentTypeCode, String[] childrenTypes, boolean recursive)
  {
    String saChildrenTypes = this.serialize(childrenTypes);

    return responseToChildTreeNode(this.controller.getChildGeoObjects(this.clientRequest, parentId, parentTypeCode, saChildrenTypes, recursive));
  }

  public ParentTreeNode getParentGeoObjects(String childId, String childTypeCode, String[] parentTypes, boolean recursive, String date)
  {
    String saParentTypes = this.serialize(parentTypes);

    try
    {
      return responseToParentTreeNode(this.controller.getParentGeoObjects(this.clientRequest, childId, childTypeCode, saParentTypes, recursive, date));
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }

  public ParentTreeNode addChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef)
  {
    return responseToParentTreeNode(this.controller.addChild(this.clientRequest, parentId, parentTypeCode, childId, childTypeCode, hierarchyRef));
  }

  public void removeChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef)
  {
    this.controller.removeChild(this.clientRequest, parentId, parentTypeCode, childId, childTypeCode, hierarchyRef);
  }

  public void submitChangeRequest(List<AbstractActionDTO> actions)
  {
    String sActions = AbstractActionDTO.serializeActions(actions).toString();

    this.controller.submitChangeRequest(this.clientRequest, sActions);
  }

  public String responseToString(ResponseIF resp)
  {
    Object obj = AbstractResponseSerializer.serialize((AbstractRestResponse) resp);

    return obj.toString();
  }

  protected String dateToString(Date date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    String sDate = format.format(date);

    return sDate;
  }

  protected GeoObjectOverTime responseToGeoObjectOverTime(ResponseIF resp)
  {
    return GeoObjectOverTime.fromJSON(this, responseToString(resp));
  }

  protected GeoObject responseToGeoObject(ResponseIF resp)
  {
    return GeoObject.fromJSON(this, responseToString(resp));
  }

  protected GeoObjectType responseToGeoObjectType(ResponseIF resp)
  {
    return GeoObjectType.fromJSON( ( responseToString(resp) ), this);
  }

  protected GeoObjectType[] responseToGeoObjectTypes(ResponseIF resp)
  {
    return GeoObjectType.fromJSONArray( ( responseToString(resp) ), this);
  }

  protected ChildTreeNode responseToChildTreeNode(ResponseIF resp)
  {
    return ChildTreeNode.fromJSON( ( responseToString(resp) ), this);
  }

  protected ParentTreeNode responseToParentTreeNode(ResponseIF resp)
  {
    return ParentTreeNode.fromJSON( ( responseToString(resp) ), this);
  }

  protected HierarchyType[] responseToHierarchyTypes(ResponseIF resp)
  {
    return HierarchyType.fromJSONArray( ( responseToString(resp) ), this);
  }
  
  private AttributeType responseToAttributeType(ResponseIF resp)
  {
    JsonObject attrObj = JsonParser.parseString(responseToString(resp)).getAsJsonObject();
    
    return AttributeType.parse(attrObj);
  }
  
  private Term responseToTerm(ResponseIF resp)
  {
    JsonObject termObj = JsonParser.parseString(responseToString(resp)).getAsJsonObject();
    
    return Term.fromJSON(termObj);
  }

  protected String[] responseToStringArray(ResponseIF resp)
  {
    String sResp = responseToString(resp);

    JsonArray ja = JsonParser.parseString(sResp).getAsJsonArray();

    String[] sa = new String[ja.size()];
    for (int i = 0; i < ja.size(); ++i)
    {
      sa[i] = ja.get(i).getAsString();
    }

    return sa;
  }

  protected String serialize(String[] array)
  {
    if (array == null)
    {
      return null;
    }

    JsonArray ja = new JsonArray();

    for (String s : array)
    {
      ja.add(s);
    }

    return ja.toString();
  }
  
}
