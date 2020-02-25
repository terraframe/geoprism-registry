package net.geoprism.registry.etl;

import net.geoprism.registry.io.GeoObjectImportConfiguration;


public class ObjectImporterFactory
{
  public static enum ObjectImportType {
    GEO_OBJECT
  }
  
  public static ObjectImporterIF getImporter(String type, ImportConfiguration config, ImportProgressListenerIF progress)
  {
    if (type.equals(ObjectImportType.GEO_OBJECT.name()))
    {
      return new GeoObjectImporter((GeoObjectImportConfiguration) config, progress);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }
}
