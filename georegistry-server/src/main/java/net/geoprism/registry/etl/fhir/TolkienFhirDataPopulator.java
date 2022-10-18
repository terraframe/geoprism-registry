/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.Pair;
import com.runwaysdk.business.Business;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class TolkienFhirDataPopulator extends AbstractFhirDataPopulator implements FhirDataPopulator
{
  private List<ServerHierarchyType> hierarchies;

  public TolkienFhirDataPopulator()
  {
    super();

    this.hierarchies = new LinkedList<ServerHierarchyType>();
  }

  @Override
  public String getLabel()
  {
    return "Tolkien Export Implementation";
  }

  @Override
  public void configure(FhirConnection context, ListTypeVersion version, boolean resolveIds)
  {
    super.configure(context, version, resolveIds);

    ListType list = version.getListType();

    JsonArray hierarchies = list.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      String hCode = hierarchy.get("code").getAsString();

      List<Pair<String, Integer>> pCodes = list.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        this.hierarchies.add(ServerHierarchyType.get(hCode));
      }
    }
  }

  @Override
  public void populate(Business row, Facility facility)
  {
    super.populate(row, facility);

    ServerGeoObjectType type = this.getList().getGeoObjectType();
    String label = type.getLabel().getValue();
    String system = this.getContext().getSystem();

    CodeableConcept concept = new CodeableConcept().setText(label).addCoding(new Coding(system, type.getCode(), label));

    Location location = facility.getLocation();
    location.addType(concept);
    location.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.Location");

    Organization organization = facility.getOrganization();
    organization.addType(concept);
    organization.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.Organization");

    if (type.getGeometryType().equals(GeometryType.MULTIPOINT))
    {
      location.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.FacilityLocation");
      location.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:facility", "Facility")));
      location.setPhysicalType(new CodeableConcept().setText("Building").addCoding(new Coding("http://terminology.hl7.org/CodeSystem/location-physical-type", "bu", "Building")));

      organization.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.FacilityOrganization");
      organization.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:facility", "Facility")));
    }
    else
    {
      location.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionLocation");
      location.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:jurisdiction", "Jurisdiction")));
      location.setPhysicalType(new CodeableConcept().setText("Jurisdiction").addCoding(new Coding("http://terminology.hl7.org/CodeSystem/location-physical-type", "jdn", "Jurisdiction")));

      organization.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionsOrganization");
      organization.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:jurisdiction", "Jurisdiction")));
    }

    // if (this.hierarchies.size() > 1)
    {
      for (ServerHierarchyType hierarchy : this.hierarchies)
      {
        this.addHierarchyExtension(row, facility, hierarchy);
      }
    }
    // else if (this.hierarchies.size() == 1)
    // {
    // this.setPartOf(row, facility, this.hierarchies.get(0));
    // }
  }

  @Override
  public void createExtraResources(Business row, Bundle bundle, Facility facility)
  {
  }

  @Override
  public void finish(Bundle bundle)
  {
  }
}
