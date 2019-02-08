package net.geoprism.georegistry.service;


import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.GeoserverService;

public class WMSService
{

  GeoserverService service = GeoserverFacade.getService();
  
  public void createAllWMSLayers()
  {
//    GeoserverService service = GeoserverFacade.getService();
    
    // TODO: get all layers (GeoObjectTypes) and iterate over them creating geoserver layers for each.
    // TODO: modify the GeoserverFacade and GeoserverRestService to publish layers without a style input
    
    // TODO: iterator
    // this.createGeoserverLayer(type, databaseViewName);
  }
  
  public void createWMSLayer(String type, String databaseViewName)
  {
    //service.publishLayer(layer, styleName);
    
  }
  
  public void deleteAllWMSLayers()
  {
    // TODO: get all layers (GeoObjectTypes) and iterate over them creating geoserver layers for each.
    
    // TODO: iterator
    //this.deleteWMSLayer(layerName);
  }
  
  public void deleteWMSLayer(String layerName)
  {
    service.removeLayer(layerName);
  }
  
  public void createLayerDatabaseView(GeoObjectType type)
  {
    
  }

}
