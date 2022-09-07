package net.geoprism.registry.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;

import net.geoprism.registry.ListType;

public class SerializedListTypeCache
{
  public static final String SEPARATOR = "$@~";
  
  public static SerializedListTypeCache INSTANCE = null;
  
  protected Map<String, JsonObject> cache;
  
  public SerializedListTypeCache(int cacheSize)
  {
    this.init(cacheSize);
  }
  
  public SerializedListTypeCache()
  {
    this.init(10000);
  }
  
  public static synchronized SerializedListTypeCache getInstance()
  {
    if (INSTANCE == null)
    {
      INSTANCE = new SerializedListTypeCache();
    }
    
    return INSTANCE;
  }
  
  @SuppressWarnings("serial")
  private void init(int cacheSize)
  {
    this.cache = new LinkedHashMap<String, JsonObject>(cacheSize + 1, .75F, true)
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
  
  public JsonObject getByOid(String oid)
  {
    return this.cache.get(this.hash(oid));
  }
  
  protected String hash(String oid)
  {
    SessionIF session = Session.getCurrentSession();
    
    if (session != null)
    {
      String username = session.getUser().getSingleActorName();
      String locale = session.getLocale().toLanguageTag();
      String roles = StringUtils.join(session.getUser().assignedRoles(), ",");
      
      return oid + username + locale + roles;
    }
    else
    {
      return "SYSTEM" + oid;
    }
  }
  
  public JsonObject getOrFetchByOid(String oid)
  {
    JsonObject json = this.cache.get(this.hash(oid));
    
    if (json == null)
    {
      json = ListType.get(oid).toJSON(true);
      
      this.cache.put(this.hash(oid), json);
    }
    
    return json;
  }
  
  public void clear()
  {
    this.cache.clear();
  }
  
  public void remove(String oid)
  {
    this.cache.remove(this.hash(oid));
  }
}
