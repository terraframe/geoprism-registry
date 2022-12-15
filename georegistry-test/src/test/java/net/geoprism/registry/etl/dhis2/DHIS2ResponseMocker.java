/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl.dhis2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.dhis2.dhis2adapter.response.model.OrganisationUnit;

public class DHIS2ResponseMocker
{
  /**
   * Generates a successful metadata import response from DHIS2 where the orgUnit(s) with ids were applied successfully.
   * 
   * @param externalIds
   * @return
   */
  public static JsonObject getMetadataOrgUnitSuccess(String... externalIds)
  {
    String base = "{\n"
        + "    \"httpStatus\": \"OK\",\n"
        + "    \"httpStatusCode\": 200,\n"
        + "    \"status\": \"OK\",\n"
        + "    \"response\": {\n"
        + "        \"responseType\": \"ImportReport\",\n"
        + "        \"typeReports\": [\n"
        + "            {\n"
        + "                \"klass\": \"org.hisp.dhis.organisationunit.OrganisationUnit\",\n"
        + "                \"stats\": {\n"
        + "                    \"created\": 0,\n"
        + "                    \"updated\": 0,\n"
        + "                    \"deleted\": 0,\n"
        + "                    \"ignored\": 0,\n"
        + "                    \"total\": 0\n"
        + "                },\n"
        + "                \"objectReports\": [\n"
        + "                ]\n"
        + "            }\n"
        + "        ],\n"
        + "        \"stats\": {\n"
        + "            \"created\": 0,\n"
        + "            \"updated\": 0,\n"
        + "            \"deleted\": 0,\n"
        + "            \"ignored\": 0,\n"
        + "            \"total\": 0\n"
        + "        },\n"
        + "        \"status\": \"OK\"\n"
        + "    }\n"
        + "}";
    
    JsonObject joResp = JsonParser.parseString(base).getAsJsonObject();
    
    JsonObject stats = joResp.get("response").getAsJsonObject().get("stats").getAsJsonObject();
    stats.addProperty("updated", externalIds.length);
    stats.addProperty("total", externalIds.length);
    
    JsonObject typeReport = joResp.get("response").getAsJsonObject().get("typeReports").getAsJsonArray().get(0).getAsJsonObject();
    
    stats = typeReport.get("stats").getAsJsonObject();
    stats.addProperty("updated", externalIds.length);
    stats.addProperty("total", externalIds.length);
    
    JsonArray objectReports = typeReport.get("objectReports").getAsJsonArray();
    
    for (int i = 0; i < externalIds.length; ++i) {
      JsonObject objectReport = new JsonObject();
      
      objectReport.addProperty("klass", OrganisationUnit.CLASS);
      objectReport.addProperty("index", i);
      objectReport.addProperty("uid", externalIds[i]);
      objectReport.add("errorReports", new JsonArray());
      
      objectReports.add(objectReport);
    }
    
    return joResp;
  }
}
