package net.geoprism.registry.etl.fhir;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class MCSDFhirDataPopulator extends AbstractFhirDataPopulator implements FhirDataPopulator
{
  private int                          count = 0;

  private ArrayList<HealthcareService> services;

  private List<ServerHierarchyType>    hierarchies;

  public MCSDFhirDataPopulator()
  {
    super();

    this.services = new ArrayList<HealthcareService>();
    this.hierarchies = new LinkedList<ServerHierarchyType>();
  }

  @Override
  public boolean supports(MasterListVersion version)
  {
    return true;
  }

  @Override
  public void configure(FhirExportContext context, MasterListVersion version, boolean resolveIds)
  {
    super.configure(context, version, resolveIds);

    this.services.add(createService("GEN", "General Practice", "17"));
    this.services.add(createService("ED", "Emergency Department", "14"));
    this.services.add(createService("DEN", "Dental", "10"));
    this.services.add(createService("MEN", "Mental Health", "22"));

    MasterList list = version.getMasterlist();

    JsonArray hierarchies = list.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      String hCode = hierarchy.get("code").getAsString();

      List<String> pCodes = list.getParentCodes(hierarchy);

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

      organization.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.FacilityOrganization");
      organization.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:facility", "Facility")));
    }
    else
    {
      location.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionLocation");
      location.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:jurisdiction", "Jurisdiction")));

      organization.getMeta().addProfile("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.JurisdictionsOrganization");
      organization.addType(new CodeableConcept().addCoding(new Coding("urn:ietf:rfc:3986", "urn:ihe:iti:mcsd:2019:jurisdiction", "Jurisdiction")));
    }

    if (this.hierarchies.size() > 1)
    {
      for (ServerHierarchyType hierarchy : this.hierarchies)
      {
        this.addHierarchyExtension(row, facility, hierarchy);
      }
    }
    else if (this.hierarchies.size() == 1)
    {
      this.setPartOf(row, facility, this.hierarchies.get(0));
    }
  }

  @Override
  public void createExtraResources(Business row, Bundle bundle, Facility facility)
  {
    int index = this.count % this.services.size();

    boolean isPoint = facility.getOrganization().getMeta().getProfile().stream().filter(p -> p.getValue().equals("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.FacilityOrganization")).count() > 0;

    if (isPoint)
    {
      HealthcareService service = services.get(index);
      service.addLocation(new Reference(facility.getLocation().getIdElement()));

      this.count++;
    }
  }

  @Override
  public void finish(Bundle bundle)
  {
    for (HealthcareService resource : services)
    {
      IdType resourceID = resource.getIdElement();
      Identifier identifier = resource.getIdentifier().get(0);

      BundleEntryComponent entry = bundle.addEntry();
      entry.setFullUrl(resource.fhirType() + "/" + resourceID.getIdPart());
      entry.setResource(resource);

      BundleEntryRequestComponent request = entry.getRequest();
      request.setMethod(HTTPVerb.PUT);
      request.setUrl(resource.getResourceType().name() + "?identifier=" + identifier.getValue());
      entry.setRequest(request);
    }
  }

  private HealthcareService createService(String code, String name, String category)
  {
    HealthcareService service = new HealthcareService();
    service.setId(new IdType(service.getResourceType().name(), UUID.randomUUID().toString()));
    service.addIdentifier().setSystem(this.getContext().getSystem()).setValue(code);
    service.setName(name);
    service.addCategory(new CodeableConcept().setText(name).addCoding(new Coding("http://terminology.hl7.org/CodeSystem/service-category", category, name)));

    return service;
  }
}
