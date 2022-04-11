package net.geoprism.registry.etl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class GeoObjectCache
{
  protected Map<String, ServerGeoObjectIF> cache;
  
  public GeoObjectCache(int cacheSize)
  {
    this.init(cacheSize);
  }
  
  public GeoObjectCache()
  {
    this.init(10000);
  }
  
  private void init(int cacheSize)
  {
    this.cache = new LinkedHashMap<String, ServerGeoObjectIF>(cacheSize + 1, .75F, true);
  }
  
  public ServerGeoObjectIF getByCode(String code, String typeCode)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();
    
    ServerGeoObjectIF go = this.cache.get(code);
    
    if (go == null)
    {
      go = service.getGeoObjectByCode(code, typeCode, true);
      
      this.cache.put(go.getCode(), go);
    }
    
    return go;
  }
}
