/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.graph;

import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;

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
  public static JSONObject createSynonym(String typeCode, String code, String label)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    VertexGeoObjectStrategy strategy = new VertexGeoObjectStrategy(type);
    VertexServerGeoObject object = strategy.getGeoObjectByCode(code);
    final String oid = object.addSynonym(label);

    JSONObject jObject = new JSONObject();
    jObject.put("synonymId", oid);
    
    jObject.put("label", object.getDisplayLabel().getValue());
    
    // jObject.put("ancestors", object.getA new
    // JSONArray(GeoEntityUtil.getAncestorsAsJSON(code)));

    JSONObject response = new JSONObject(jObject);
    response.put("vOid", oid);

    return response;
  }

  @Transaction
  public static void deleteSynonym(String vOid)
  {
    GeoVertexSynonym.get(vOid).delete();
  }

}
