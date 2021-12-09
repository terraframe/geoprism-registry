package com.runwaysdk.build.domain;

import java.util.List;

import com.runwaysdk.build.DatabaseBuilder;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.session.Request;

public class ExistsAndInvalidInstanceDataPatchChecker
{
  public static void main(String[] args)
  {
    patchInstanceData();
  }
  
  @Request
  public static void patchInstanceData()
  {
    final String patchExistsAndInvalid = "0001625168828072";
    final String patchExistsAndInvalidInstanceData = "0001639003382316";
    
    List<String> values = com.runwaysdk.dataaccess.database.Database.getPropertyValue(DatabaseBuilder.RUNWAY_METADATA_VERSION_TIMESTAMP_PROPERTY);

    if (values.contains(patchExistsAndInvalid))
    {
      com.runwaysdk.dataaccess.database.Database.addPropertyValue(com.runwaysdk.dataaccess.database.Database.VERSION_NUMBER, MdAttributeCharacterInfo.CLASS, patchExistsAndInvalidInstanceData, DatabaseBuilder.RUNWAY_METADATA_VERSION_TIMESTAMP_PROPERTY);
    }
  }
}
