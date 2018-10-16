package net.geoprism.georegistry.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.runwaysdk.util.IDGenerator;

public class IdService
{
  private static Map<String, IdService> services = new HashMap<String, IdService>();
  
  private Set<String> ids = new HashSet<String>();
  
  private String sessionId;
  
  public IdService(String sessionId)
  {
    this.sessionId = sessionId;
  }

  public String[] getUIDS(Integer amount)
  {
    String[] ids = new String[amount];
    
    for (int i = 0; i < amount; ++i)
    {
      String id = IDGenerator.nextID();
      
      ids[i] = id;
      this.ids.add(id);
    }
    
    return ids;
  }
  
  public boolean isIssuedId(String id)
  {
    return ids.contains(id);
  }

  public static synchronized IdService getInstance(String sessionId)
  {
    if (services.containsKey(sessionId))
    {
      return services.get(sessionId);
    }
    else
    {
      IdService service = new IdService(sessionId);
      
      services.put(sessionId, service);
      
      return service;
    }
  }
}
