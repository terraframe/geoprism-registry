/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.fhir;

import java.io.IOException;
import java.io.Writer;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Identifier.IdentifierUse;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.etl.FhirResponseException;

public class MasterListFhirExporter
{
  private MasterList        list;

  private MasterListVersion version;

  private FhirDataPopulator populator;

  private FhirConnection    connection;

  public MasterListFhirExporter(MasterListVersion version, FhirConnection connection, FhirDataPopulator populator, boolean resolveIds)
  {
    this.version = version;
    this.connection = connection;
    this.populator = populator;
    this.list = version.getMasterlist();

    this.populator.configure(connection, version, resolveIds);
  }

  public MasterList getList()
  {
    return list;
  }

  public void setList(MasterList list)
  {
    this.list = list;
  }

  public long export()
  {
    Bundle collection = new Bundle();

    this.populateBundle(collection);

    try
    {
      Bundle result = this.connection.getClient().transaction().withBundle(collection).execute();

      return result.getEntry().size();
    }
    catch (BaseServerResponseException e)
    {
      FhirResponseException exception = new FhirResponseException(e);
      exception.setErrorMessage(e.getMessage());
      throw exception;
    }
  }

  public void write(Bundle bundle, Writer writer)
  {
    FhirContext ctx = this.connection.getFhirContext();
    IParser parser = ctx.newJsonParser();

    // Serialize it
    try
    {
      parser.encodeResourceToWriter(bundle, writer);
    }
    catch (DataFormatException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void populateBundle(Bundle bundle)
  {
    BusinessQuery query = this.version.buildQuery(null);
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    OIterator<Business> objects = query.getIterator();

    try
    {

      while (objects.hasNext())
      {
        Business row = objects.next();
        String code = row.getValue(DefaultAttribute.CODE.getName());

        Identifier identifier = new Identifier();
        identifier.setValue(code);
        identifier.setUse(IdentifierUse.USUAL);
        identifier.setType(new CodeableConcept().addCoding(new Coding("http://terminology.hl7.org/CodeSystem/v2-0203", "RI", "Resource identifier")));

        String system = this.connection.getSystem();

        if (system != null)
        {
          identifier.setSystem(system);
        }

        Facility facility = createFacility(row, identifier);

        this.populator.populate(row, facility);

        // Add the organization and its corresponding location to the bundle
        createEntries(bundle, facility, identifier);

        this.populator.createExtraResources(row, bundle, facility);
      }
    }
    finally
    {
      objects.close();
    }

    this.populator.finish(bundle);
  }

  private Facility createFacility(Business row, Identifier identifier)
  {
    String code = row.getValue(DefaultAttribute.CODE.getName());

    Organization org = new Organization();
    org.setId(new IdType(org.getResourceType().name(), code));
    org.setName(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE));
    org.addIdentifier(identifier);

    Location location = new Location();
    location.setId(new IdType(location.getResourceType().name(), code));
    location.setName(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE));
    location.setManagingOrganization(new Reference(org.getIdElement()));
    location.addIdentifier(identifier);

    Geometry geometry = row.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    if (geometry != null)
    {
      Point centroid = geometry.getCentroid();

      GeoJsonWriter writer = new GeoJsonWriter();
      String geojson = writer.write(geometry);

      Encoder encoder = Base64.getEncoder();

      // Create a location
      Attachment attachment = new Attachment();
      attachment.setContentType("application/json");
      attachment.setDataElement(new Base64BinaryType(encoder.encodeToString(geojson.getBytes())));
      attachment.setTitle("Geojson");

      Extension extension = new Extension("http://hl7.org/fhir/StructureDefinition/location-boundary-geojson");
      extension.setValue(attachment);

      location.setPosition(new LocationPositionComponent(new DecimalType(centroid.getX()), new DecimalType(centroid.getY())));
      location.addExtension(extension);
    }

    return new Facility(org, location);
  }

  private static void createEntries(Bundle bundle, Facility facility, Identifier identifier)
  {
    Resource[] resources = new Resource[] { facility.getOrganization(), facility.getLocation() };

    for (Resource resource : resources)
    {

      IdType resourceID = resource.getIdElement();

      BundleEntryComponent entry = bundle.addEntry();
      entry.setFullUrl(resource.fhirType() + "/" + resourceID.getIdPart());
      entry.setResource(resource);

      BundleEntryRequestComponent request = entry.getRequest();
      request.setMethod(HTTPVerb.PUT);
      request.setUrl(resource.getResourceType().name() + "?identifier=" + identifier.getSystem() + "|" + identifier.getValue());
      entry.setRequest(request);
    }
  }
}
