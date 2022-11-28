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
