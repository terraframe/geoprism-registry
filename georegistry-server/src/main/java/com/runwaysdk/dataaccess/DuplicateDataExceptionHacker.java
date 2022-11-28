package com.runwaysdk.dataaccess;

public class DuplicateDataExceptionHacker
{
  public static MdClassDAOIF getMdClassDAOIF(DuplicateDataException ex)
  {
    return ex.mdClassDAOIF;
  }
}
