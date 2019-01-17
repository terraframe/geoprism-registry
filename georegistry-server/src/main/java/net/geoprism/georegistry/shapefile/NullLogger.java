package net.geoprism.georegistry.shapefile;

import net.geoprism.data.importer.GISImportLoggerIF;

public class NullLogger implements GISImportLoggerIF
{

  @Override
  public void log(String featureId, Throwable t)
  {
  }

  @Override
  public void log(String featureId, String message)
  {
  }

  @Override
  public boolean hasLogged()
  {
    return true;
  }

  @Override
  public void close()
  {
  }

}
