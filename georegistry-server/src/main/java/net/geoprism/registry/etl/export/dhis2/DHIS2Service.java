package net.geoprism.registry.etl.export.dhis2;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.DHIS2Bridge;
import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.TypeReportResponse;

public class DHIS2Service implements DHIS2ServiceIF
{
  private DHIS2Bridge dhis2;
  
  public DHIS2Service(HTTPConnector connector, String version)
  {
    this.dhis2 = new DHIS2Bridge(connector, version);
  }

  @Override
  public String getDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    return this.getDhis2Id();
  }

  @Override
  public DHIS2Response systemInfo() throws InvalidLoginException, HTTPException
  {
    return this.dhis2.systemInfo();
  }

  @Override
  public ObjectReportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityPost(entityName, params, payload);
  }

  @Override
  public DHIS2Response entityIdGet(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityIdGet(entityName, entityId, params);
  }

  @Override
  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityIdPatch(entityName, entityId, params, payload);
  }

  @Override
  public TypeReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.entityTranslations(entityName, entityId, params, payload);
  }

  @Override
  public MetadataImportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.metadataPost(params, payload);
  }

  @Override
  public <T> MetadataGetResponse<T> metadataGet(String objectNamePlural) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.metadataGet(objectNamePlural);
  }

  @Override
  public <T> MetadataGetResponse<T> metadataGet(String objectNamePlural, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.metadataGet(objectNamePlural, params);
  }

  @Override
  public DHIS2Response apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiGet(url, params);
  }

  @Override
  public DHIS2ImportResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiPost(url, params, body);
  }

  @Override
  public DHIS2ImportResponse apiPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiPut(url, params, body);
  }

  @Override
  public DHIS2ImportResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    return this.dhis2.apiPatch(url, params, body);
  }
}
