package net.geoprism.registry.service.business;

import java.util.HashMap;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.ImportColumnDTO;
import net.geoprism.registry.view.ImportTypeDTO;

public abstract class DataImportBusinessService
{
  protected ImportTypeDTO getType(ServerGeoObjectType geoObjectType)
  {
    final boolean includeCoordinates = geoObjectType.getGeometryType().equals(GeometryType.POINT) || geoObjectType.getGeometryType().equals(GeometryType.MULTIPOINT) || geoObjectType.getGeometryType().equals(GeometryType.MIXED);

    return getType(geoObjectType, includeCoordinates);

  }

  protected ImportTypeDTO getType(ServerGeoObjectType geoObjectType, boolean includeCoordinates)
  {
    ImportTypeDTO type = ImportConfiguration.toTypeDTO(geoObjectType, new HashMap<>());

    if (includeCoordinates)
    {
      ImportColumnDTO latitude = new ImportColumnDTO();
      latitude.setCode(GeoObjectImportConfiguration.LATITUDE);
      latitude.setBaseType(GeoObjectImportConfiguration.NUMERIC);
      latitude.setLabel(new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LATITUDE_KEY)));

      ImportColumnDTO longitude = new ImportColumnDTO();
      longitude.setCode(GeoObjectImportConfiguration.LONGITUDE);
      longitude.setBaseType(GeoObjectImportConfiguration.NUMERIC);
      longitude.setLabel(new LocalizedValue(LocalizationFacade.localize(GeoObjectImportConfiguration.LONGITUDE_KEY)));

      type.getAttributes().add(0, latitude);
      type.getAttributes().add(0, longitude);
    }

    return type;
  }

  protected ImportTypeDTO getType(ObjectClass pType)
  {
    return ImportConfiguration.toTypeDTO(pType, new HashMap<>());
  }

}
