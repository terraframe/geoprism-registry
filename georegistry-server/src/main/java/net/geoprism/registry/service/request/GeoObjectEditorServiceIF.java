package net.geoprism.registry.service.request;

import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.action.ChangeRequest;

@Component
public interface GeoObjectEditorServiceIF
{

  JsonObject updateGeoObject(String sessionId, String geoObjectCode, String geoObjectTypeCode, String actions, String listId, String notes);

  JsonObject createGeoObject(String sessionId, String ptn, String sTimeGo, String listId, String notes);

  ChangeRequest updateChangeRequest(ChangeRequest request, String notes, JsonArray jaActions);

  JsonObject updateGeoObjectInTrans(String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes);

}
