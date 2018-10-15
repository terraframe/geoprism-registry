package net.geoprism.georegistry;

import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.gis.geo.GeoEntity;

public class GeoObjectService
{
  @Request(RequestType.SESSION)
  public static GeoObject getGeoObject(String sessionId, String uid)
  {
    GeoEntity geo = GeoEntity.get(uid);
    
    GeoObject geoObject = AdapterConverter.getInstance().convertGeoObject(geo);
    
    return geoObject;
  }
}
