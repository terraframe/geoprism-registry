package net.geoprism.georegistry.testframework;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.AbstractResponseSerializer;
import com.runwaysdk.mvc.AbstractRestResponse;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.georegistry.RegistryController;

public class TestRegistryAdapterClient extends RegistryAdapter
{
  private static final long serialVersionUID = -433764579483802366L;

  protected RegistryController controller;
  
  protected ClientRequestIF clientRequest;

  public TestRegistryAdapterClient()
  {
    super(new TestRegistryClientIdService());
    ((TestRegistryClientIdService)this.getIdSerivce()).setClient(this);
    
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
    
    GeoObjectType[] gots = this.getGeoObjectTypes(new String[]{});

    for (GeoObjectType got : gots)
    {
      this.getMetadataCache().addGeoObjectType(got);
    }
    
    HierarchyType[] hts = this.getHierarchyTypes(new String[]{});
    
    for (HierarchyType ht : hts)
    {
      this.getMetadataCache().addHierarchyType(ht);
    }
  }
  
  public Set<String> getGeoObjectUids(int amount)
  {
    ResponseIF response = this.controller.getUIDs(this.clientRequest, amount);
    
    String sResp = responseToString(response);
    
    JsonArray ja = new JsonParser().parse(sResp).getAsJsonArray();
    
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < ja.size(); ++i)
    {
      set.add(ja.get(i).getAsString());
    }
    
    return set;
  }
  
  public GeoObject getGeoObject(String registryId, String code)
  {
    return responseToGeoObject(this.controller.getGeoObject(this.clientRequest, registryId, code));
  }
  
  public GeoObject getGeoObjectByCode(String code, String typeCode)
  {
    return responseToGeoObject(this.controller.getGeoObjectByCode(this.clientRequest, code, typeCode));
  }
  
  public GeoObject createGeoObject(String jGeoObj)
  {
    return responseToGeoObject(this.controller.createGeoObject(this.clientRequest, jGeoObj));
  }
  
  public GeoObject updateGeoObject(String jGeoObj)
  {
    return responseToGeoObject(this.controller.updateGeoObject(this.clientRequest, jGeoObj));
  }
  
  public GeoObjectType[] getGeoObjectTypes(String[] codes)
  {
    String saCodes = this.serialize(codes);
    
    return responseToGeoObjectTypes(this.controller.getGeoObjectTypes(this.clientRequest, saCodes));
  }
  
  public HierarchyType[] getHierarchyTypes(String[] codes)
  {
    String saCodes = this.serialize(codes);
    
    return responseToHierarchyTypes(this.controller.getHierarchyTypes(this.clientRequest, saCodes));
  }
  
  public JsonArray listGeoObjectTypes()
  {
    RestBodyResponse response = (RestBodyResponse) this.controller.listGeoObjectTypes(this.clientRequest);
    return (JsonArray) response.serialize();
  }
  
  public ChildTreeNode getChildGeoObjects(String parentId, String parentTypeCode, String[] childrenTypes, boolean recursive)
  {
    String saChildrenTypes = this.serialize(childrenTypes);
    
    return responseToChildTreeNode(this.controller.getChildGeoObjects(this.clientRequest, parentId, parentTypeCode, saChildrenTypes, recursive));
  }
  
  public ParentTreeNode getParentGeoObjects(String childId, String childTypeCode, String[] parentTypes, boolean recursive)
  {
    String saParentTypes = this.serialize(parentTypes);
    
    return responseToParentTreeNode(this.controller.getParentGeoObjects(this.clientRequest, childId, childTypeCode, saParentTypes, recursive));
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
  
  protected String responseToString(ResponseIF resp)
  {
    Object obj = AbstractResponseSerializer.serialize((AbstractRestResponse) resp);
    
    return obj.toString();
  }
  
  protected GeoObject responseToGeoObject(ResponseIF resp)
  {
    return GeoObject.fromJSON(this, responseToString(resp));
  }
  
  protected GeoObjectType responseToGeoObjectType(ResponseIF resp)
  {
    return GeoObjectType.fromJSON((responseToString(resp)), this);
  }
  
  protected GeoObjectType[] responseToGeoObjectTypes(ResponseIF resp)
  {
    return GeoObjectType.fromJSONArray((responseToString(resp)), this);
  }
  
  protected ChildTreeNode responseToChildTreeNode(ResponseIF resp)
  {
    return ChildTreeNode.fromJSON((responseToString(resp)), this);
  }
  
  protected ParentTreeNode responseToParentTreeNode(ResponseIF resp)
  {
    return ParentTreeNode.fromJSON((responseToString(resp)), this);
  }
  
  protected HierarchyType[] responseToHierarchyTypes(ResponseIF resp)
  {
    return HierarchyType.fromJSONArray((responseToString(resp)), this);
  }
  
  protected String[] responseToStringArray(ResponseIF resp)
  {
    String sResp = responseToString(resp);
    
    JsonArray ja = new JsonParser().parse(sResp).getAsJsonArray();
    
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
