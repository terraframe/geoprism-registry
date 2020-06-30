package net.geoprism.dhis2.dhis2adapter.response;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class EntityImportResponse extends HTTPResponse
{

  public EntityImportResponse(String response, int statusCode)
  {
    super(response, statusCode);
  }
  
  public EntityImportResponse(HTTPResponse http)
  {
    super(http.getResponse(), http.getStatusCode());
  }
  
  public String getKlass()
  {
    if (!this.getJsonObject().has("response"))
    {
      return null;
    }
    
    return this.getJsonObject().get("response").getAsJsonObject().get("klass").getAsString();
  }
  
  public Boolean hasErrorReports()
  {
    if (!this.getJsonObject().has("response"))
    {
      return false;
    }
    
    return this.getJsonObject().get("response").getAsJsonObject().has("errorReports") && this.getErrorReports().size() > 0;
  }
  
  public List<ErrorReport> getErrorReports()
  {
    if (!this.getJsonObject().has("response"))
    {
      return null;
    }
    
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();
    
    Type listType = new TypeToken<List<ErrorReport>>() {}.getType();
    
    return gson.fromJson(this.getJsonObject().get("response").getAsJsonObject().get("errorReports"), listType);
  }
  
  public static class ErrorReport
  {
    protected String message;
    
    protected String mainKlass;
    
    protected String errorCode;
    
    protected String mainId;
    
    protected String errorProperty;
    
    protected String[] errorProperties;

    public String getMessage()
    {
      return message;
    }

    public void setMessage(String message)
    {
      this.message = message;
    }

    public String getMainKlass()
    {
      return mainKlass;
    }

    public void setMainKlass(String mainKlass)
    {
      this.mainKlass = mainKlass;
    }

    public String getErrorCode()
    {
      return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
      this.errorCode = errorCode;
    }

    public String getMainId()
    {
      return mainId;
    }

    public void setMainId(String mainId)
    {
      this.mainId = mainId;
    }

    public String getErrorProperty()
    {
      return errorProperty;
    }

    public void setErrorProperty(String errorProperty)
    {
      this.errorProperty = errorProperty;
    }

    public String[] getErrorProperties()
    {
      return errorProperties;
    }

    public void setErrorProperties(String[] errorProperties)
    {
      this.errorProperties = errorProperties;
    }
  }

}
