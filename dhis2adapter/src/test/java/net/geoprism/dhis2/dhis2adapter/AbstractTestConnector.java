package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

abstract public class AbstractTestConnector implements ConnectorIF
{

  private boolean initialized = false;
  
  private String versionResponse;
  
  public AbstractTestConnector(String versionResponse)
  {
    this.versionResponse = versionResponse;
  }
  
  @Override
  public DHIS2Response httpGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    if (!initialized && url.contains("system/info"))
    {
      this.initialized = true;
      return new DHIS2Response(this.versionResponse, 200);
    }
    else
    {
      return this.httpGetSubclass(url, params);
    }
  }

  protected abstract DHIS2Response httpGetSubclass(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException;

  @Override
  public DHIS2Response httpPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DHIS2Response httpPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public DHIS2Response httpDelete(String string, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

}
