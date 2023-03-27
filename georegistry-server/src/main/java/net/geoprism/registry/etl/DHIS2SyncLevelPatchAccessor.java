package net.geoprism.registry.etl;

public class DHIS2SyncLevelPatchAccessor
{
  public static String getOrgUnitGroupId(DHIS2SyncLevel syncLevel)
  {
    return syncLevel.getOrgUnitGroupId();
  }
}
