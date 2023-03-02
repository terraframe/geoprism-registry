/**
 *
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
