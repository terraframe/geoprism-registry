package net.geoprism.registry.graph;

import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.DataUploader;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class GeoVertexSynonym extends GeoVertexSynonymBase
{
  private static final long serialVersionUID = -1951346601;

  public GeoVertexSynonym()
  {
    super();
  }

  @Transaction
  public static JSONObject createSynonym(String entityId, String label)
  {
    GeoEntity entity = GeoEntity.get(entityId);

    ServerGeoObjectType type = ServerGeoObjectType.get(entity.getUniversal());
    VertexGeoObjectStrategy strategy = new VertexGeoObjectStrategy(type);

    VertexServerGeoObject object = strategy.getGeoObjectByCode(entity.getGeoId());
    final String oid = object.addSynonym(label);

    String synonym = DataUploader.createGeoEntitySynonym(entityId, label);

    JSONObject response = new JSONObject(synonym);
    response.put("vOid", oid);

    return response;
  }

  @Transaction
  public static void deleteSynonym(String synonymId, String vOid)
  {
    DataUploader.deleteGeoEntitySynonym(synonymId);

    GeoVertexSynonym.get(vOid).delete();
  }

}
