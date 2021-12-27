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
package net.geoprism.dhis2.dhis2adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
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
  
  DHIS2Bridge dhis2;
  
  public Dhis2IdCache(DHIS2Bridge dhis2)
  {
    this.dhis2 = dhis2;
  }
  
  /**
   * Fetches more ids from DHIS2 and adds them to our internal cache.
   * 
   * @throws HTTPException 
   * @throws InvalidLoginException 
   * @throws UnexpectedResponseException 
   * @throws BadServerUriException 
   */
  public DHIS2Response fetchIds() throws HTTPException, InvalidLoginException, UnexpectedResponseException, BadServerUriException
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
      throw new UnexpectedResponseException(response);
    }
    
    return response;
  }
  
  /**
   * Fetches the next id from the cache. If the cache is empty, this method will fetch more ids and return one when it becomes available, haulting execution in the meantime.
   * 
   * @throws UnexpectedResponseException 
   * @throws InvalidLoginException 
   * @throws HTTPException 
   * @throws BadServerUriException 
   */
  public String next() throws HTTPException, InvalidLoginException, UnexpectedResponseException, BadServerUriException
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