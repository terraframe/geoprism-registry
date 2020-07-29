package net.geoprism.dhis2.dhis2adapter;

import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

public class FakeIdConnector implements ConnectorIF
{

  @Override
  public DHIS2Response httpGet(String string, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    Integer limit = null;
    
    for (int i = 0; i < params.size(); ++i)
    {
      NameValuePair param = params.get(i);
      
      if (param.getName().equals("limit"))
      {
        limit = Integer.valueOf(param.getValue());
      }
    }
    
    JsonArray codes = new JsonArray();
    
    for (int i = 0; i < limit; ++i)
    {
      codes.add(UUID.randomUUID().toString().substring(0, 11));
    }
    
    JsonObject resp = new JsonObject();
    resp.add("codes", codes);
    
    return new DHIS2Response(resp.toString(), 200);
  }

  @Override
  public DHIS2Response httpPost(String string, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DHIS2Response httpPatch(String string, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }
  
}
