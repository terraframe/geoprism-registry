package net.geoprism.registry.etl;

import com.runwaysdk.RunwayException;

import net.geoprism.registry.etl.GeoObjectImporter.GeoObjectParentErrorBuilder;

public class RecordedErrorException extends RunwayException
{

  private static final long serialVersionUID = 711088516551711518L;
  
  private Throwable error;
  
  private String objectJson;
  
  private String objectType;
  
  private GeoObjectParentErrorBuilder parentBuilder;
  
  public RecordedErrorException()
  {
    super("");
  }

  public void setError(Throwable t)
  {
    this.error = t;
  }

  public void setObjectJson(String objectJson)
  {
    this.objectJson = objectJson;
  }

  public void setObjectType(String objectType)
  {
    this.objectType = objectType;
  }

  public Throwable getError()
  {
    return error;
  }

  public String getObjectJson()
  {
    return objectJson;
  }

  public String getObjectType()
  {
    return objectType;
  }

  public GeoObjectParentErrorBuilder getParentBuilder()
  {
    return parentBuilder;
  }

  public void setParentBuilder(GeoObjectParentErrorBuilder parentBuilder)
  {
    this.parentBuilder = parentBuilder;
  }
  
}
