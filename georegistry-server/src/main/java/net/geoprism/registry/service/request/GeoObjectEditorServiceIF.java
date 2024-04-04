package net.geoprism.registry.service.request;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
public interface GeoObjectEditorServiceIF
{

  JsonObject updateGeoObject(String sessionId, String geoObjectCode, String geoObjectTypeCode, String actions, String listId, String notes);

  JsonObject createGeoObject(String sessionId, String ptn, String sTimeGo, String listId, String notes);
}
