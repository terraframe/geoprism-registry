/**
 *
 */
package net.geoprism.registry.test;

import java.text.SimpleDateFormat;
import java.util.Date;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.GeoRegistryUtil;

public class AbstractTestClient
{
  private static final long     serialVersionUID = -433764579483802366L;

  @Autowired
  protected TestRegistryAdapter adapter;

  public TestRegistryAdapter getAdapter()
  {
    return adapter;
  }

  public String responseToString(ResponseEntity<String> resp)
  {
    return resp.getBody();
  }

  protected String dateToString(Date date)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    String sDate = format.format(date);

    return sDate;
  }

  protected HierarchyType responseToHierarchyType(ResponseEntity<String> resp)
  {
    return HierarchyType.fromJSON(responseToString(resp), this.adapter);
  }

  protected GeoObjectOverTime responseToGeoObjectOverTime(ResponseEntity<String> resp)
  {
    return GeoObjectOverTime.fromJSON(this.adapter, responseToString(resp));
  }

  protected GeoObject responseToGeoObject(ResponseEntity<String> resp)
  {
    return GeoObject.fromJSON(this.adapter, responseToString(resp));
  }

  protected GeoObjectType responseToGeoObjectType(ResponseEntity<String> resp)
  {
    return GeoObjectType.fromJSON( ( responseToString(resp) ), this.adapter);
  }

  protected GeoObjectType[] responseToGeoObjectTypes(ResponseEntity<String> resp)
  {
    return GeoObjectType.fromJSONArray( ( responseToString(resp) ), this.adapter);
  }

  protected ChildTreeNode responseToChildTreeNode(ResponseEntity<String> resp)
  {
    return ChildTreeNode.fromJSON( ( responseToString(resp) ), this.adapter);
  }

  protected ParentTreeNode responseToParentTreeNode(ResponseEntity<String> resp)
  {
    return ParentTreeNode.fromJSON( ( responseToString(resp) ), this.adapter);
  }

  protected HierarchyType[] responseToHierarchyTypes(ResponseEntity<String> resp)
  {
    return HierarchyType.fromJSONArray( ( responseToString(resp) ), this.adapter);
  }

  protected AttributeType responseToAttributeType(ResponseEntity<String> resp)
  {
    JsonObject attrObj = JsonParser.parseString(responseToString(resp)).getAsJsonObject();

    return AttributeType.parse(attrObj);
  }

  protected Term responseToTerm(ResponseEntity<String> resp)
  {
    JsonObject termObj = JsonParser.parseString(responseToString(resp)).getAsJsonObject();

    return Term.fromJSON(termObj);
  }

  protected String stringifyDate(Date date)
  {
    String sDate = null;

    if (date != null)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
      sDate = format.format(date);
    }

    return sDate;
  }

  protected String[] responseToStringArray(ResponseEntity<String> resp)
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
