/**
 *
 */
package net.geoprism.registry.test;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.controller.GenericRestController;
import net.geoprism.registry.controller.GeoObjectController;
import net.geoprism.registry.controller.GeoObjectController.GeoObjectBody;
import net.geoprism.registry.controller.GeoObjectController.RelationshipBody;
import net.geoprism.registry.controller.GeoObjectOverTimeController;
import net.geoprism.registry.controller.GeoObjectOverTimeController.GeoObjectOverTimeBody;
import net.geoprism.registry.controller.GeoObjectTypeController;
import net.geoprism.registry.controller.GeoObjectTypeController.AttributeBody;
import net.geoprism.registry.controller.GeoObjectTypeController.DeleteTermBody;
import net.geoprism.registry.controller.GeoObjectTypeController.GeoObjectTypeBody;
import net.geoprism.registry.controller.GeoObjectTypeController.TermBody;
import net.geoprism.registry.controller.HierarchyTypeController;
import net.geoprism.registry.controller.HierarchyTypeController.HierarchyTypeNodeBody;
import net.geoprism.registry.controller.SynchronizationConfigController;
import net.geoprism.registry.permission.PermissionContext;

@Component
public class TestRegistryClient extends AbstractTestClient
{
  @Autowired
  private GenericRestController       restController;

  @Autowired
  private GeoObjectController         geoObjectController;

  @Autowired
  private GeoObjectTypeController     geoObjectTypeController;

  @Autowired
  private GeoObjectOverTimeController geoObjectTimeController;

  @Autowired
  private HierarchyTypeController     hierarchyController;
  
  @Autowired
  private SynchronizationConfigController synchronizationConfigController;

  public Set<String> getUIDs(int amount)
  {
    ResponseEntity<String> response = this.geoObjectController.getUIDs(amount);

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
    return JsonParser.parseString(responseToString(this.geoObjectController.getGeoObjectSuggestions(text, type, parent, parentTypeCode, hierarchy, startDate, endDate))).getAsJsonArray();
  }

  public AttributeType createAttributeType(String geoObjectTypeCode, String attributeTypeJSON)
  {
    AttributeBody body = new AttributeBody();
    body.setGeoObjTypeCode(geoObjectTypeCode);
    body.setAttributeType(JsonParser.parseString(attributeTypeJSON).getAsJsonObject());

    return responseToAttributeType(this.geoObjectTypeController.createAttributeType(body));
  }

  public AttributeType updateAttributeType(String geoObjectTypeCode, String attributeTypeJSON)
  {
    AttributeBody body = new AttributeBody();
    body.setGeoObjTypeCode(geoObjectTypeCode);
    body.setAttributeType(JsonParser.parseString(attributeTypeJSON).getAsJsonObject());

    return responseToAttributeType(this.geoObjectTypeController.updateAttributeType(body));
  }

  public Term createTerm(String parentTermCode, String termJSON)
  {
    TermBody body = new TermBody();
    body.setParentTermCode(parentTermCode);
    body.setTermJSON(JsonParser.parseString(termJSON).getAsJsonObject());

    return responseToTerm(this.geoObjectTypeController.createTerm(body));
  }

  public Term updateTerm(String parentTermCode, String termJSON)
  {
    TermBody body = new TermBody();
    body.setParentTermCode(parentTermCode);
    body.setTermJSON(JsonParser.parseString(termJSON).getAsJsonObject());

    return responseToTerm(this.geoObjectTypeController.updateTerm(body));
  }

  public void deleteTerm(String parentTermCode, String termCode)
  {
    DeleteTermBody body = new DeleteTermBody();
    body.setParentTermCode(parentTermCode);
    body.setTermCode(termCode);

    this.geoObjectTypeController.deleteTerm(body);
  }

  public GeoObjectType createGeoObjectType(String gtJSON)
  {
    GeoObjectTypeBody body = new GeoObjectTypeBody();
    body.setGtJSON(JsonParser.parseString(gtJSON).getAsJsonObject());

    return responseToGeoObjectType(this.geoObjectTypeController.createGeoObjectType(body));
  }

  public GeoObjectType updateGeoObjectType(String gtJSON)
  {
    GeoObjectTypeBody body = new GeoObjectTypeBody();
    body.setGtJSON(JsonParser.parseString(gtJSON).getAsJsonObject());

    return responseToGeoObjectType(this.geoObjectTypeController.updateGeoObjectType(body));
  }

  public GeoObject getGeoObject(String registryId, String code, Date date)
  {
    return responseToGeoObject(this.geoObjectController.getGeoObject(registryId, code, date));
  }

  public GeoObjectOverTime getGeoObjectOverTime(String registryId, String typeCode)
  {
    return responseToGeoObjectOverTime(this.geoObjectTimeController.getGeoObjectOverTime(registryId, typeCode));
  }

  public GeoObject getGeoObjectByCode(String code, String typeCode, Date date)
  {
    return responseToGeoObject(this.geoObjectController.getGeoObjectByCode(code, typeCode, date));
  }

  public GeoObjectOverTime getGeoObjectOverTimeByCode(String code, String typeCode)
  {
    return responseToGeoObjectOverTime(this.geoObjectTimeController.getGeoObjectOverTimeByCode(code, typeCode));
  }

  public GeoObject createGeoObject(String jGeoObj, Date startDate, Date endDate)
  {
    GeoObjectBody body = new GeoObjectBody();
    body.setGeoObject(JsonParser.parseString(jGeoObj).getAsJsonObject());
    body.setStartDate(startDate);
    body.setEndDate(endDate);

    return responseToGeoObject(this.geoObjectController.createGeoObject(body));
  }

  public GeoObjectOverTime createGeoObjectOverTime(String jGeoObj)
  {
    GeoObjectOverTimeBody body = new GeoObjectOverTimeBody();
    body.setGeoObject(JsonParser.parseString(jGeoObj).getAsJsonObject());

    return responseToGeoObjectOverTime(this.geoObjectTimeController.createGeoObjectOverTime(body));
  }

  public GeoObject updateGeoObject(String jGeoObj, Date startDate, Date endDate)
  {
    GeoObjectBody body = new GeoObjectBody();
    body.setGeoObject(JsonParser.parseString(jGeoObj).getAsJsonObject());
    body.setStartDate(startDate);
    body.setEndDate(endDate);

    return responseToGeoObject(this.geoObjectController.updateGeoObject(body));
  }

  public GeoObjectOverTime updateGeoObjectOverTime(String jGeoObj)
  {
    GeoObjectOverTimeBody body = new GeoObjectOverTimeBody();
    body.setGeoObject(JsonParser.parseString(jGeoObj).getAsJsonObject());

    return responseToGeoObjectOverTime(this.geoObjectTimeController.updateGeoObjectOverTime(body));
  }

  public GeoObjectType[] getGeoObjectTypes(String[] codes, PermissionContext pc)
  {
    String saCodes = this.serialize(codes);

    if (pc == null)
    {
      pc = PermissionContext.READ;
    }

    return responseToGeoObjectTypes(this.geoObjectTypeController.getGeoObjectTypes(saCodes, pc.name()));
  }

  public HierarchyType[] getHierarchyTypes(String[] codes)
  {
    String saCodes = this.serialize(codes);

    return responseToHierarchyTypes(this.hierarchyController.getHierarchyTypes(saCodes, PermissionContext.READ.name()));
  }

  public JsonObject hierarchyManagerInit()
  {
    return JsonParser.parseString(responseToString(this.restController.init())).getAsJsonObject();
  }

  public JsonArray getHierarchiesForGeoObjectOverTime(String code, String typeCode)
  {
    return JsonParser.parseString(responseToString(this.geoObjectController.getHierarchiesForGeoObjectOverTime(code, typeCode))).getAsJsonArray();
  }

  public JsonArray listGeoObjectTypes()
  {
    ResponseEntity<String> response = this.geoObjectTypeController.listGeoObjectTypes(true);

    return JsonParser.parseString(response.getBody()).getAsJsonArray();
  }

  public ChildTreeNode getChildGeoObjects(String parentId, String parentTypeCode, String hierarchyCode, Date date, String[] childrenTypes, boolean recursive)
  {
    String saChildrenTypes = this.serialize(childrenTypes);

    return responseToChildTreeNode(this.geoObjectController.getChildGeoObjects(parentId, parentTypeCode, hierarchyCode, date, saChildrenTypes, recursive));
  }

  public ParentTreeNode getParentGeoObjects(String childId, String childTypeCode, String hierarchyCode, Date date, String[] parentTypes, boolean recursive)
  {
    String saParentTypes = this.serialize(parentTypes);

    return responseToParentTreeNode(this.geoObjectController.getParentGeoObjects(childId, childTypeCode, hierarchyCode, date, saParentTypes, recursive));
  }

  public JsonObject getConfigForExternalSystem(String externalSystemId, String hierarchyTypeCode)
  {
    return JsonParser.parseString(responseToString(synchronizationConfigController.getConfigForExternalSystem(externalSystemId, hierarchyTypeCode))).getAsJsonObject();
  }

  public HierarchyType addToHierarchy(String hierarchyCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    HierarchyTypeNodeBody body = new HierarchyTypeNodeBody();
    body.setHierarchyCode(hierarchyCode);
    body.setChildGeoObjectTypeCode(childGeoObjectTypeCode);
    body.setParentGeoObjectTypeCode(parentGeoObjectTypeCode);

    return responseToHierarchyType(this.hierarchyController.addToHierarchy(body));
  }

  public ParentTreeNode addChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef, Date startDate, Date endDate)
  {
    RelationshipBody body = new RelationshipBody();
    body.setParentCode(parentId);
    body.setParentTypeCode(parentTypeCode);
    body.setChildCode(childId);
    body.setChildTypeCode(childTypeCode);
    body.setHierarchyCode(hierarchyRef);
    body.setStartDate(startDate);
    body.setEndDate(endDate);

    return responseToParentTreeNode(this.geoObjectController.addChild(body));
  }

  public void removeChild(String parentId, String parentTypeCode, String childId, String childTypeCode, String hierarchyRef, Date startDate, Date endDate)
  {
    RelationshipBody body = new RelationshipBody();
    body.setParentCode(parentId);
    body.setParentTypeCode(parentTypeCode);
    body.setChildCode(childId);
    body.setChildTypeCode(childTypeCode);
    body.setHierarchyCode(hierarchyRef);
    body.setStartDate(startDate);
    body.setEndDate(endDate);

    this.geoObjectController.removeChild(body);
  }

  public GeoObject newGeoObjectInstance(String code)
  {
    return this.getAdapter().newGeoObjectInstance(code);
  }
}
