package net.geoprism.dhis2.dhis2adapter;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;

public interface ConnectorIF
{

  DHIS2Response httpGet(String string, List<NameValuePair> params) throws InvalidLoginException, HTTPException;

  DHIS2Response httpPost(String string, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException;

  DHIS2Response httpPatch(String string, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException;
  
}
