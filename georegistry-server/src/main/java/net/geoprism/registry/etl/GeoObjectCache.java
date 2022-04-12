package net.geoprism.registry.etl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class GeoObjectCache
{
  public static final String SEPARATOR = "$@~";
  
  protected Map<String, ServerGeoObjectIF> cache;
  
  public GeoObjectCache(int cacheSize)
  {
    this.init(cacheSize);
  }
  
  public GeoObjectCache()
  {
    this.init(10000);
  }
  
  @SuppressWarnings("serial")
  private void init(int cacheSize)
  {
    this.cache = new LinkedHashMap<String, ServerGeoObjectIF>(cacheSize + 1, .75F, true)
    {
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
      {
        return size() > cacheSize;
      }
    };
  }
  
  public long getSize()
  {
    return this.cache.size();
  }
  
  public ServerGeoObjectIF getByCode(String code, String typeCode)
  {
    return this.cache.get(typeCode + SEPARATOR + code);
  }
  
  public ServerGeoObjectIF getOrFetchByCode(String code, String typeCode)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();
    
    ServerGeoObjectIF go = this.cache.get(typeCode + SEPARATOR + code);
    
    if (go == null)
    {
      go = service.getGeoObjectByCode(code, typeCode, true);
      
      this.cache.put(typeCode + SEPARATOR + code, go);
    }
    
    return go;
  }
}
