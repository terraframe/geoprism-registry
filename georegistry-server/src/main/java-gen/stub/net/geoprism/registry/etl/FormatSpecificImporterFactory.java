package net.geoprism.registry.etl;

import com.runwaysdk.resource.ApplicationResource;

public class FormatSpecificImporterFactory
{
  public static enum FormatImporterType {
    SHAPEFILE,
    EXCEL,
    DHIS2
  }
  
  public static FormatSpecificImporterIF getImporter(String type, ApplicationResource resource, ImportProgressListenerIF progress)
  {
    if (type.equals(FormatImporterType.SHAPEFILE.name()))
    {
      return new ShapefileImporter(resource, progress);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }
}
