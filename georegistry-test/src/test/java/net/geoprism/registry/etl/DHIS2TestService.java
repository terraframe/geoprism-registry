/**
 *
 */
package net.geoprism.registry.etl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.dhis2.dhis2adapter.DHIS2Objects;
import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.IncompatibleServerVersionException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2ImportResponse;
import net.geoprism.dhis2.dhis2adapter.response.DHIS2Response;
import net.geoprism.dhis2.dhis2adapter.response.EntityGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.LocaleGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.MetadataGetResponse;
import net.geoprism.dhis2.dhis2adapter.response.ObjectReportResponse;
import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;
import net.geoprism.registry.etl.export.dhis2.DHIS2TransportServiceIF;

public class DHIS2TestService implements DHIS2TransportServiceIF
{
  public static final String SIERRA_LEONE_ID = "ImspTQPwCqd";
  
  public static final String BO_ID = "O6uvpzGd5pu";
  
  public static final String ATTRIBUTE_COLOR_ID = "uhA8DG5EtXa";
  
  private LinkedList<Dhis2Payload> payloads = new LinkedList<Dhis2Payload>();
  
  private Map<Method, LinkedList<DHIS2MockResponseProvider>> responses = new HashMap<>();
  
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
  
  public enum Method {
    METADATA_POST;
  }
  
  public class DHIS2MockResponseProvider
  {
    private String response;
    
    private int httpCode;
    
    public DHIS2MockResponseProvider(String resp, int httpCode)
    {
      this.response = resp;
      this.httpCode = httpCode;
    }

    public String getResponse()
    {
      return response;
    }

    public void setResponse(String response)
    {
      this.response = response;
    }

    public int getHttpCode()
    {
      return httpCode;
    }

    public void setHttpCode(int httpCode)
    {
      this.httpCode = httpCode;
    }
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
  public <T> EntityGetResponse<T> entityIdGet(String entityName, String entityId, Class<?> entityType, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    JsonObject joResp = new JsonObject();
    
    if (entityName.equals(DHIS2Objects.ORGANISATION_UNITS))
    {
      JsonArray respOrgUnits = new JsonArray();
      joResp.add(DHIS2Objects.ORGANISATION_UNITS, respOrgUnits);
      
      if (entityId.equals(SIERRA_LEONE_ID))
      {
        OrganisationUnit root = new OrganisationUnit();
        root.setName("Sierra Leone");
        root.setShortName("Sierra Leone");
        root.setId(SIERRA_LEONE_ID);
        root.setOpeningDate(new Date());
        
        JsonArray translations = new JsonArray();
        JsonObject name = new JsonObject();
        name.addProperty("property", "NAME");
        name.addProperty("locale", "en_GB");
        name.addProperty("value", "Sierra Leone");
        translations.add(name);
        JsonObject shortName = new JsonObject();
        shortName.addProperty("property", "SHORT_NAME");
        shortName.addProperty("locale", "en_GB");
        shortName.addProperty("value", "Sierra Leone");
        translations.add(shortName);
        root.setTranslations(translations);
        
        JsonArray attributeValues = new JsonArray();
        JsonObject av = new JsonObject();
        av.addProperty("value", "blue");
        JsonObject attr = new JsonObject();
        attr.addProperty("id", ATTRIBUTE_COLOR_ID);
        av.add("attribute", attr);
        attributeValues.add(av);
        root.setAttributeValues(attributeValues);
        
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(DHIS2Constants.DATE_FORMAT);
        joResp = builder.create().toJsonTree(root, root.getClass()).getAsJsonObject();
      }
      else if (entityId.equals(BO_ID))
      {
        OrganisationUnit root = new OrganisationUnit();
        root.setName("Bo");
        root.setShortName("Bo");
        root.setId(BO_ID);
        root.setOpeningDate(new Date());
        
        JsonArray translations = new JsonArray();
        JsonObject name = new JsonObject();
        name.addProperty("property", "NAME");
        name.addProperty("locale", "en_GB");
        name.addProperty("value", "Bo");
        translations.add(name);
        JsonObject shortName = new JsonObject();
        shortName.addProperty("property", "SHORT_NAME");
        shortName.addProperty("locale", "en_GB");
        shortName.addProperty("value", "Bo");
        translations.add(shortName);
        root.setTranslations(translations);
        
        JsonArray attributeValues = new JsonArray();
        JsonObject av = new JsonObject();
        av.addProperty("value", "blue");
        JsonObject attr = new JsonObject();
        attr.addProperty("id", ATTRIBUTE_COLOR_ID);
        av.add("attribute", attr);
        attributeValues.add(av);
        root.setAttributeValues(attributeValues);
        
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(DHIS2Constants.DATE_FORMAT);
        joResp = builder.create().toJsonTree(root, root.getClass()).getAsJsonObject();
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }
    else
    {
      throw new UnsupportedOperationException();
    }
    
    return new EntityGetResponse<T>(joResp.toString(), 200, entityType);
  }

  @Override
  public ObjectReportResponse entityIdPatch(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImportReportResponse entityTranslations(String entityName, String entityId, List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImportReportResponse metadataPost(List<NameValuePair> params, HttpEntity payload) throws InvalidLoginException, HTTPException
  {
    ImportReportResponse response = null;
    
    try
    {
      String data = IOUtils.toString(payload.getContent(), Charset.forName("UTF-8"));
      
      this.payloads.add(new Dhis2Payload(params, data));
      
      LinkedList<DHIS2MockResponseProvider> providers = this.responses.getOrDefault(Method.METADATA_POST, new LinkedList<DHIS2MockResponseProvider>());
      if (!providers.isEmpty())
      {
        DHIS2MockResponseProvider provider = providers.removeFirst();
        response = new ImportReportResponse(provider.getResponse(), provider.getHttpCode());
      }
      else
      {
        response = new ImportReportResponse(getSuccessJson().toString(), 200);
      }
    }
    catch (UnsupportedOperationException | IOException e)
    {
      e.printStackTrace();
    }
    
    return response; 
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
      else if (objectNamePlural.equals(DHIS2Objects.ORGANISATION_UNIT_GROUPS))
      {
        InputStream data = Thread.currentThread().getContextClassLoader().getResourceAsStream("dhis2/default/organisationUnitGroups.json");
        
        String resp = IOUtils.toString(data, "UTF-8");
        
        return new MetadataGetResponse<T>(resp, 200, objectNamePlural, dhis2Type);
      }
      else
      {
        JsonObject jo = new JsonObject();
        
        JsonObject system = new JsonObject();
        system.addProperty("id", "eed3d451-4ff5-4193-b951-ffcc68954299");
        system.addProperty("rev", "86fbbf7");
        system.addProperty("version", "2.31.9");
        system.addProperty("date", "2020-07-17T23:03:46.853");
        
        jo.add("system", system);
        
        jo.add(objectNamePlural, new JsonArray());
        
        return new MetadataGetResponse<T>(jo.toString(), 200, objectNamePlural, dhis2Type);
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
  
  @Override
  public DHIS2Response entityIdDelete(String entityName, String entityId, List<NameValuePair> params) throws InvalidLoginException, HTTPException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getVersionRemoteServer()
  {
    return "2.31.9";
  }

  @Override
  public Integer getVersionRemoteServerApi()
  {
    return 31;
  }

  @Override
  public Integer getVersionApiCompat()
  {
    return 31;
  }

  @Override
  public void initialize() throws UnexpectedResponseException, InvalidLoginException, HTTPException, IncompatibleServerVersionException
  {
    // Do nothing
  }

  @Override
  public void setVersionApiCompat(Integer versionApiCompat) throws IncompatibleServerVersionException
  {
    // Do nothing
  }

  @Override
  public String getRemoteServerUrl()
  {
    return null;
  }

  @Override
  public LocaleGetResponse localesGet() throws InvalidLoginException, HTTPException, BadServerUriException
  {
    try
    {
      String jsonLocales = IOUtils.toString(DHIS2TestService.class.getResourceAsStream("/dhis2/locales-get.json"), "UTF-8");
      
      return new LocaleGetResponse(jsonLocales, 200);
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void addResponse(Method method, String response, int httpCode)
  {
    LinkedList<DHIS2MockResponseProvider> list = this.responses.getOrDefault(method, new LinkedList<DHIS2MockResponseProvider>());
    list.add(new DHIS2MockResponseProvider(response, httpCode));
    this.responses.put(method, list);
  }

}
