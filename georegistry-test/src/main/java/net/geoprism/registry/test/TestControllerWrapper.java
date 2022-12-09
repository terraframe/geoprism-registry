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
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.ClientRequestIF;

import net.geoprism.registry.GeoRegistryUtil;

public class TestControllerWrapper
{
  private static final long serialVersionUID = -433764579483802366L;
  
  protected TestRegistryAdapterClient adapter;

  protected ClientRequestIF    clientRequest;

  public TestControllerWrapper(TestRegistryAdapterClient adapter, ClientRequestIF clientRequest)
  {
    this.adapter = adapter;
    this.clientRequest = clientRequest;
  }

  public void setClientRequest(ClientRequestIF clientRequest)
  {
    this.clientRequest = clientRequest;
  }
  
  public TestRegistryAdapterClient getAdpater()
  {
    return adapter;
  }

  public void setAdpater(TestRegistryAdapterClient adpater)
  {
    this.adapter = adpater;
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
