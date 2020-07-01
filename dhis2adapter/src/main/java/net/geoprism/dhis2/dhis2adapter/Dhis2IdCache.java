package net.geoprism.dhis2.dhis2adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;


/**
 * This class is responsible for maintaining a cache of new DHIS2 ids that can be fetched at will.
 * 
 * @author rrowlands
 */
public class Dhis2IdCache
{
  // Max number of ids stored in the cache
  public static final Integer FETCH_NUM = 1000;
  
  Stack<String> cache = new Stack<String>();
  
  DHIS2Facade dhis2;
  
  public Dhis2IdCache(DHIS2Facade dhis2)
  {
    this.dhis2 = dhis2;
  }
  
  /**
   * Fetches more ids from DHIS2 and adds them to our internal cache.
   * 
   * @throws HTTPException 
   * @throws InvalidLoginException 
   * @throws UnexpectedResponseException 
   */
  public DHIS2Response fetchIds() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    List<NameValuePair> nvp = new ArrayList<NameValuePair>();
    nvp.add(new BasicNameValuePair("limit", String.valueOf(FETCH_NUM)));
    
    DHIS2Response response = dhis2.apiGet("system/id.json", nvp);
    
    if (response.getStatusCode() != 200)
    {
      throw new HTTPException("Unable to get new ids from DHIS2. " + response.getResponse());
    }
    
    JsonObject json = response.getJsonObject();
    
    if (json.has("codes"))
    {
      JsonArray codes = json.get("codes").getAsJsonArray();
      
      for (int i = 0; i < codes.size(); ++i)
      {
        String id = codes.get(i).getAsString();
        cache.push(id);
      }
    }
    else
    {
      UnexpectedResponseException ex = new UnexpectedResponseException();
      
      ex.setResponse(response);
      
      if (json.has("message"))
      {
        ex.setErrorMessage(json.get("message").getAsString());
      }
      else
      {
        ex.setErrorMessage(json.toString().substring(0, 500));
      }
      
      throw ex;
    }
    
    return response;
  }
  
  /**
   * Fetches the next id from the cache. If the cache is empty, this method will fetch more ids and return one when it becomes available, haulting execution in the meantime.
   * 
   * @throws UnexpectedResponseException 
   * @throws InvalidLoginException 
   * @throws HTTPException 
   */
  public String next() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    if (cache.isEmpty())
    {
      this.fetchIds();
    }
    
    return cache.pop();
  }
  
  /**
   * Returns the number of ids in the cache
   */
  public Integer getNumIds()
  {
    return cache.size();
  }
}