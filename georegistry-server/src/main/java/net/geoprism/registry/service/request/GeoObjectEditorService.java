package net.geoprism.registry.service.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.GeoObjectEditorBusinessService;

@Service
public class GeoObjectEditorService implements GeoObjectEditorServiceIF
{
  @Autowired
  private GeoObjectEditorBusinessService service;

  @Override
  @Request(RequestType.SESSION)
  public JsonObject createGeoObject(String sessionId, String ptn, String sTimeGo, String masterListId, String notes)
  {
    return this.service.createGeoObject(ptn, sTimeGo, masterListId, notes);
  }

  @Override
  @Request(RequestType.SESSION)
  public JsonObject updateGeoObject(String sessionId, String geoObjectCode, String geoObjectTypeCode, String actions, String masterListId, String notes)
  {
    return this.service.updateGeoObject(geoObjectCode, geoObjectTypeCode, actions, masterListId, notes);
  }

}
