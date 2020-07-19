package net.geoprism.registry.etl.export.dhis2;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.TypeReportResponse;

public interface DHIS2ServiceIF
{
  public String getDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException;

  public DHIS2Response systemInfo() throws InvalidLoginException, HTTPException;

  public ObjectReportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException;

  public DHIS2Response entityIdGet(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException;

  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException;

  public TypeReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException;

  public MetadataImportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException;
  
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type) throws InvalidLoginException, HTTPException;
  
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type, List<NameValuePair> params) throws InvalidLoginException, HTTPException;
  
  public DHIS2Response apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException;
  
  public DHIS2ImportResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException;
  
  public DHIS2ImportResponse apiPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException;
  
  public DHIS2ImportResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException;
}
