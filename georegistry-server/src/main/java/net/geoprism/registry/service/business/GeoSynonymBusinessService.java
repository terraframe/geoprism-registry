/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.graph.GeoVertexSynonym;

@Service
public class GeoSynonymBusinessService
{
  @Request(RequestType.SESSION)
  public JSONObject createGeoEntitySynonym(String typeCode, String code, String label)
  {
    return GeoVertexSynonym.createSynonym(typeCode, code, label);
  }

  @Request(RequestType.SESSION)
  public void deleteGeoEntitySynonym(String vOid)
  {
    GeoVertexSynonym.deleteSynonym(vOid);
  }

}