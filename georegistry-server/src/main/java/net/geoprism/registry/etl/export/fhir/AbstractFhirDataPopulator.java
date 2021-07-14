package net.geoprism.registry.etl.export.fhir;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.etl.UnresolvableResourceException;
import net.geoprism.registry.model.ServerHierarchyType;

public abstract class AbstractFhirDataPopulator extends DefaultFhirDataPopulator implements FhirDataPopulator
{
  private MasterListVersion   version;

  private MasterList          list;

  private Map<String, String> identifiers;

  public AbstractFhirDataPopulator(MasterListVersion version)
  {
    super();

    this.version = version;
    this.list = version.getMasterlist();
    this.identifiers = new HashMap<String, String>();
  }

  public MasterList getList()
  {
    return list;
  }

  public MasterListVersion getVersion()
  {
    return version;
  }

  protected Extension createHierarchyTypeExtension(FhirExportContext context, String code, ServerHierarchyType type)
  {
    String literal = this.getLiteralIdentifier(context, code);

    if (literal != null)
    {
      CodeableConcept concept = new CodeableConcept();
      concept.addCoding(new Coding().setCode(type.getCode()));
      concept.setText(type.getDisplayLabel().getValue());

      Reference reference = new Reference();
      reference.setReference(literal);

      Extension rootExt = new Extension("http://ihe.net/fhir/StructureDefinition/IHE_mCSD_hierarchy_extension");
      rootExt.addExtension(new Extension("part-of", reference));
      rootExt.addExtension(new Extension("hierarchy-type", concept));

      return rootExt;
    }
    else
    {
      UnresolvableResourceException exception = new UnresolvableResourceException();
      exception.setIdentifier(code);
      throw exception;
    }
  }

  private String getLiteralIdentifier(FhirExportContext context, String code)
  {
    if (!this.identifiers.containsKey(code))
    {
      IGenericClient client = context.getClient();
      IBaseBundle result = client.search().forResource(Organization.class).count(1).where(Organization.IDENTIFIER.exactly().systemAndValues(context.getSystem(), code)).execute();
      List<IBaseResource> resources = BundleUtil.toListOfResources(client.getFhirContext(), result);

      if (resources.size() > 0)
      {
        IBaseResource resource = resources.get(0);
        IIdType idElement = resource.getIdElement();

        this.identifiers.put(code, idElement.getResourceType() + "/" + idElement.getIdPart());
      }
    }

    return this.identifiers.get(code);
  }

  public void addHierarchyValue(FhirExportContext context, Business row, Organization org, ServerHierarchyType hierarchyType)
  {
    JsonArray hierarchies = this.list.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      String hCode = hierarchy.get("code").getAsString();

      if (hCode.equals(hierarchyType.getCode()))
      {
        List<String> pCodes = this.list.getParentCodes(hierarchy);
        Collections.reverse(pCodes);

        // Get the lowest one with a code
        for (String pCode : pCodes)
        {
          String attributeName = hCode.toLowerCase() + pCode.toLowerCase();

          if (row.hasAttribute(attributeName))
          {
            String code = row.getValue(attributeName);

            if (code != null)
            {
              org.addExtension(this.createHierarchyTypeExtension(context, code, hierarchyType));

              return;
            }
          }
        }
      }
    }

  }

}
