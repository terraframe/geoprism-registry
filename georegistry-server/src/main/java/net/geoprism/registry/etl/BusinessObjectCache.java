package net.geoprism.registry.etl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.BusinessObject;

public class BusinessObjectCache
{
  public static final String            SEPARATOR = "$@~";

  protected Map<String, BusinessObject> cache;

  public BusinessObjectCache(int cacheSize)
  {
    this.init(cacheSize);
  }

  public BusinessObjectCache()
  {
    this.init(10000);
  }

  @SuppressWarnings("serial")
  private void init(int cacheSize)
  {
    this.cache = new LinkedHashMap<String, BusinessObject>(cacheSize + 1, .75F, true)
    {
      public boolean removeEldestEntry(@SuppressWarnings("rawtypes")
      Map.Entry eldest)
      {
        return size() > cacheSize;
      }
    };
  }

  public long getSize()
  {
    return this.cache.size();
  }

  public BusinessObject getByCode(String code, String typeCode)
  {
    return this.cache.get(typeCode + SEPARATOR + code);
  }

  public BusinessObject getOrFetchByCode(String code, String typeCode)
  {
    BusinessObject go = this.cache.get(typeCode + SEPARATOR + code);

    if (go == null)
    {
      BusinessType businessType = BusinessType.getByCode(typeCode);
      go = BusinessObject.getByCode(businessType, code);

      this.cache.put(typeCode + SEPARATOR + code, go);
    }

    return go;
  }
}
