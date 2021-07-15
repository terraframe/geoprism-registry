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
package net.geoprism.registry.etl.export.fhir;

import java.util.Base64;
import java.util.Base64.Encoder;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Base64BinaryType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.OIterator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonWriter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.etl.FhirResponseException;
import net.geoprism.registry.graph.FhirExternalSystem;

public class MasterListFhirExporter
{
  private static Logger     logger = LoggerFactory.getLogger(MasterListFhirExporter.class);

  private MasterList        list;

  private MasterListVersion version;

  private FhirDataPopulator populator;

  private FhirExportContext context;

  public MasterListFhirExporter(MasterListVersion version, FhirExternalSystem system, FhirDataPopulator populator)
  {
    this.version = version;
    this.populator = populator;
    this.list = version.getMasterlist();

    FhirContext ctx = FhirContext.forR4();

    IRestfulClientFactory factory = ctx.getRestfulClientFactory();
    factory.setSocketTimeout(-1);

    IGenericClient client = factory.newGenericClient(system.getUrl());

    this.context = new FhirExportContext(system, client);

    this.populator.configure(context, version);
  }

  public MasterList getList()
  {
    return list;
  }

  public void setList(MasterList list)
  {
    this.list = list;
  }

  public void export()
  {
    Bundle collection = createBundle();

    try
    {
      this.context.getClient().transaction().withBundle(collection).execute();
    }
    catch (BaseServerResponseException e)
    {
      FhirResponseException exception = new FhirResponseException(e);
      exception.setErrorMessage(e.getMessage());
      throw exception;
    }
  }

  public Bundle createBundle()
  {
    Bundle bundle = new Bundle();

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

        String system = this.context.getSystem();

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

    return bundle;
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
      request.setUrl(resource.getResourceType().name() + "?identifier=" + identifier.getValue());
      entry.setRequest(request);
    }
  }
}
