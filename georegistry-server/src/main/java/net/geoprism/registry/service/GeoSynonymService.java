package net.geoprism.registry.service;

import org.json.JSONObject;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.graph.GeoVertexSynonym;

public class GeoSynonymService
{
  @Request(RequestType.SESSION)
  public JSONObject createGeoEntitySynonym(String sessionId, String entityId, String label)
  {
    return GeoVertexSynonym.createSynonym(entityId, label);
  }

  @Request(RequestType.SESSION)
  public void deleteGeoEntitySynonym(String sessionId, String synonymId, String vOid)
  {
    GeoVertexSynonym.deleteSynonym(synonymId, vOid);
  }

}