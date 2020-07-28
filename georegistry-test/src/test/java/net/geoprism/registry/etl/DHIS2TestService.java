package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.TypeReportResponse;
import net.geoprism.registry.etl.export.dhis2.DHIS2ServiceIF;

public class DHIS2TestService implements DHIS2ServiceIF
{

  private LinkedList<Dhis2Payload> payloads = new LinkedList<Dhis2Payload>();
  
  public Dhis2Payload getLastPayload()
  {
    return this.payloads.getLast();
  }
  
  public LinkedList<Dhis2Payload> getPayloads()
  {
    return this.payloads;
  }
  
  public JsonObject getSuccessJson()
  {
    JsonObject jo = new JsonObject();
    jo.addProperty("status", "SUCCESS");
    
    return jo;
  }
  
  public class Dhis2Payload
  {
    private List<NameValuePair> params;
    
    private String data;
    
    protected Dhis2Payload(List<NameValuePair> params, String data)
    {
      this.params = params;
      this.data = data;
    }

    public List<NameValuePair> getParams()
    {
      return params;
    }

    public void setParams(List<NameValuePair> params)
    {
      this.params = params;
    }

    public String getData()
    {
      return data;
    }

    public void setData(String payload)
    {
      this.data = payload;
    }
  }
  
  @Override
  public String getDhis2Id() throws HTTPException, InvalidLoginException, UnexpectedResponseException
  {
    return "GenTest" + IDGenerator.nextID();
  }

  @Override
  public DHIS2Response systemInfo() throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectReportResponse entityPost(String entityName, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DHIS2Response entityIdGet(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public MetadataImportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    try
    {
      String data = IOUtils.toString(payload.getContent(), Charset.forName("UTF-8"));
      
      this.payloads.add(new Dhis2Payload(params, data));
    }
    catch (UnsupportedOperationException | IOException e)
    {
      e.printStackTrace();
    }
    
    return new MetadataImportResponse(getSuccessJson().toString(), 200);
  }

  @Override
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type) throws InvalidLoginException, HTTPException
  {
    return this.metadataGet(dhis2Type, null);
  }

  @Override
  public <T> MetadataGetResponse<T> metadataGet(Class<?> dhis2Type, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    String objectNamePlural = DHIS2Objects.getPluralObjectNameFromClass(dhis2Type);
    
    try
    {
      if (objectNamePlural.equals(DHIS2Objects.OPTIONS))
      {
        InputStream data = Thread.currentThread().getContextClassLoader().getResourceAsStream("dhis2/2.31.9/options.json");
        
        String resp = IOUtils.toString(data, "UTF-8");
        
        return new MetadataGetResponse<T>(resp, 200, objectNamePlural, dhis2Type);
      }
      else if (objectNamePlural.equals(DHIS2Objects.OPTIONSETS))
      {
        InputStream data = Thread.currentThread().getContextClassLoader().getResourceAsStream("dhis2/2.31.9/optionsets.json");
        
        String resp = IOUtils.toString(data, "UTF-8");
        
        return new MetadataGetResponse<T>(resp, 200, objectNamePlural, dhis2Type);
      }
      else if (objectNamePlural.equals(DHIS2Objects.ATTRIBUTES))
      {
        InputStream data = Thread.currentThread().getContextClassLoader().getResourceAsStream("dhis2/2.31.9/attributes.json");
        
        String resp = IOUtils.toString(data, "UTF-8");
        
        return new MetadataGetResponse<T>(resp, 200, objectNamePlural, dhis2Type);
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Override
  public DHIS2Response apiGet(String url, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DHIS2ImportResponse apiPost(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DHIS2ImportResponse apiPut(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public DHIS2ImportResponse apiPatch(String url, List<NameValuePair> params, HttpEntity body) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

}
