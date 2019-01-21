package net.geoprism.georegistry.testframework;

import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.mvc.AbstractResponseSerializer;
import com.runwaysdk.mvc.AbstractRestResponse;
import com.runwaysdk.mvc.ResponseIF;

import net.geoprism.georegistry.service.RegistryService;
import net.geoprism.georegistry.service.ServiceFactory;

public class TestUtilities
{
  public String responseToString(ResponseIF resp)
  {
    Object obj = AbstractResponseSerializer.serialize((AbstractRestResponse) resp);
    
    return obj.toString();
  }
  
  public GeoObject responseToGeoObject(ResponseIF resp)
  {
    return GeoObject.fromJSON(ServiceFactory.getAdapter(), responseToString(resp));
  }
  
  public GeoObjectType responseToGeoObjectType(ResponseIF resp)
  {
    return GeoObjectType.fromJSON((responseToString(resp)), ServiceFactory.getAdapter());
  }
  
  public GeoObjectType[] responseToGeoObjectTypes(ResponseIF resp)
  {
    return GeoObjectType.fromJSONArray((responseToString(resp)), ServiceFactory.getAdapter());
  }
  
  public ChildTreeNode responseToChildTreeNode(ResponseIF resp)
  {
    return ChildTreeNode.fromJSON((responseToString(resp)), ServiceFactory.getAdapter());
  }
  
  public ParentTreeNode responseToParentTreeNode(ResponseIF resp)
  {
    return ParentTreeNode.fromJSON((responseToString(resp)), ServiceFactory.getAdapter());
  }
  
  public HierarchyType[] responseToHierarchyTypes(ResponseIF resp)
  {
    return HierarchyType.fromJSONArray((responseToString(resp)), ServiceFactory.getAdapter());
  }
  
  public String[] responseToStringArray(ResponseIF resp)
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
  
  public String serialize(String[] array)
  {
    JsonArray ja = new JsonArray();
    
    for (String s : array)
    {
      ja.add(s);
    }
    
    return ja.toString();
  }
}
