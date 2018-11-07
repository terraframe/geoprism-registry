/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.sidebar;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.configuration.ConfigurationManager;

public class JSONMenuProvider
{
  public ArrayList<MenuItem> getMenu() {
    try
    {
      String json = FileUtils.readFileToString(new File(ConfigurationManager.getResource(ConfigurationManager.ConfigGroup.ROOT, "geoprism/sidebar.txt").getPath()));
      return this.toMenu(new JSONArray(json));
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public ArrayList<MenuItem> toMenu(JSONArray array) throws JSONException {
    ArrayList<MenuItem> menu = new ArrayList<MenuItem>();
    
    for (int i = 0; i < array.length(); ++i) {
      JSONObject item = array.getJSONObject(i);
      
      String uri = null;
      if (item.has("uri")) {
        uri = item.getString("uri");
      }
      
      MenuItem parent = new MenuItem(item.getString("name"), uri, null);
      menu.add(parent);
      
      if (item.has("children")) {
        JSONArray children = item.getJSONArray("children");
        parent.addChildren(this.toMenu(children));
      }
    }
    
    return menu;
  }
}
