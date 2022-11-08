package net.geoprism.registry.dhis2;

import com.google.gson.JsonArray;

import net.geoprism.dhis2.dhis2adapter.response.ImportReportResponse;
import net.geoprism.registry.etl.DHIS2SyncLevel;

public class DHIS2SynchronizationManagerProxy
{
  public static void processMetadataImportResponse(DHIS2SynchronizationManager manager, DHIS2SyncLevel level, String submittedJson, ImportReportResponse resp, final JsonArray orgUnitsPayload, final JsonArray orgUnitGroupsPayload)
  {
    manager.processMetadataImportResponse(level, submittedJson, resp, orgUnitsPayload, orgUnitGroupsPayload);
  }
}
