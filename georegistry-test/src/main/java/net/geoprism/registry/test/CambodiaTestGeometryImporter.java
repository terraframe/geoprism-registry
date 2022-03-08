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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class CambodiaTestGeometryImporter
{
  public static CambodiaTestGeometryImporter INSTANCE = null;
  
  protected Map<String, String> geometries = new HashMap<String, String>();
  
  public static CambodiaTestGeometryImporter getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new CambodiaTestGeometryImporter();
    }
    
    return INSTANCE;
  }
  
  CambodiaTestGeometryImporter()
  {
    InputStream is = CambodiaTestGeometryImporter.class.getResourceAsStream("/test-data/cambodia.json");
    
    try
    {
      JsonArray geoms = JsonParser.parseString(IOUtils.toString(is, "UTF-8")).getAsJsonArray();
      
      for (int i = 0; i < geoms.size(); ++i)
      {
        JsonObject jo = geoms.get(i).getAsJsonObject();
        
        geometries.put(jo.get("code").getAsString(), jo.get("geometry").getAsString());
      }
    }
    catch (JsonSyntaxException | IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static String getGeometry(String code)
  {
    return getInstance().iGetGeometry(code);
  }
  
  public String iGetGeometry(String code)
  {
    return this.geometries.get(code);
  }
}
