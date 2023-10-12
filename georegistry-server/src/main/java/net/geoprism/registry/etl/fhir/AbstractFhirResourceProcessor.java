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
package net.geoprism.registry.etl.fhir;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Date;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;

import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;

public abstract class AbstractFhirResourceProcessor implements FhirResourceProcessor
{
  private FhirExternalSystem     system;

  private GeoObjectBusinessServiceIF service;

  protected abstract String getType(Location location);

  protected abstract String getType(Organization organization);

  protected abstract Identifier getIdentifier(Location location);

  protected abstract Identifier getIdentifier(Organization organization);

  protected abstract void populate(ServerGeoObjectIF geoObject, Location location, Date lastUpdated);

  public void configure(FhirExternalSystem system)
  {
    this.service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);
    this.system = system;
  }

  public FhirExternalSystem getSystem()
  {
    return system;
  }
  
  public GeoObjectBusinessServiceIF getService()
  {
    return service;
  }

  protected Geometry getGeometry(Location location, ServerGeoObjectType type)
  {
    Extension extension = location.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/location-boundary-geojson");

    if (extension != null)
    {
      Attachment value = (Attachment) extension.getValue();

      if (value.hasData())
      {
        Decoder decoder = Base64.getDecoder();
        byte[] binary = decoder.decode(value.getDataElement().getValueAsString());
        try
        {
          String geojson = new String(binary, "UTF-8");

          GeoJsonReader reader = new GeoJsonReader();
          return reader.read(geojson);
        }
        catch (ParseException | UnsupportedEncodingException e)
        {
          e.printStackTrace();
        }
      }
    }

    return null;
  }

  @Override
  public void process(Location location)
  {
    Identifier identifier = this.getIdentifier(location);

    if (identifier != null)
    {
      String typeCode = this.getType(location);
      String code = identifier.getValue();

      ServerGeoObjectIF geoObject = this.getService().getGeoObjectByCode(code, typeCode, false);

      if (geoObject == null)
      {
        geoObject = this.getService().newInstance(ServerGeoObjectType.get(typeCode));
        geoObject.setCode(code);
      }

      Geometry geometry = this.getGeometry(location, geoObject.getType());

      Date lastUpdated = location.getMeta().getLastUpdated();

      this.populate(geoObject, location, lastUpdated);

      geoObject.setGeometry(geometry, lastUpdated, ValueOverTime.INFINITY_END_DATE);
      
      this.service.apply(geoObject, true);
    }
  }

  @Override
  public void process(Organization organization)
  {
  }

}
