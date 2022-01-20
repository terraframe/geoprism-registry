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
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.etl.UnresolvableResourceException;
import net.geoprism.registry.model.ServerHierarchyType;

public abstract class AbstractFhirDataPopulator extends BasicFhirDataPopulator implements FhirDataPopulator
{
  private FhirConnection      context;

  private ListTypeVersion     version;

  private boolean             resolveIds;

  private ListType            list;

  private Map<String, String> identifiers;

  public AbstractFhirDataPopulator()
  {
    super();

    this.version = null;
    this.list = null;
    this.context = null;
    this.resolveIds = true;
    this.identifiers = new HashMap<String, String>();
  }

  @Override
  public void configure(FhirConnection context, ListTypeVersion version, boolean resolveIds)
  {
    this.context = context;
    this.version = version;
    this.resolveIds = resolveIds;
    this.list = this.version.getListType();
  }

  public ListType getList()
  {
    return list;
  }

  public ListTypeVersion getVersion()
  {
    return version;
  }

  public FhirConnection getContext()
  {
    return context;
  }

  protected Extension createHierarchyTypeExtension(String code, ServerHierarchyType type)
  {
    String literal = this.getLiteralIdentifier(code);

    if (literal != null)
    {
      CodeableConcept concept = new CodeableConcept();
      concept.addCoding(new Coding().setCode(type.getCode()));
      concept.setText(type.getDisplayLabel().getValue());

      Reference reference = new Reference();
      reference.setReference(literal);

      Extension rootExt = new Extension("http://ihe.net/fhir/StructureDefinition/IHE.mCSD.hierarchy.extension");
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

  private String getLiteralIdentifier(String code)
  {
    if (this.resolveIds)
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

    return "Organization/" + code;
  }

  public void addHierarchyExtension(Business row, Facility facility, ServerHierarchyType hierarchyType)
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
              facility.getOrganization().addExtension(this.createHierarchyTypeExtension(code, hierarchyType));

              return;
            }
          }
        }
      }
    }
  }

  public void setPartOf(Business row, Facility facility, ServerHierarchyType hierarchyType)
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
              String literal = this.getLiteralIdentifier(code);

              if (literal != null)
              {
                Reference reference = new Reference();
                reference.setReference(literal);

                facility.getOrganization().setPartOf(reference);

                return;
              }
            }
          }
        }
      }
    }
  }

}
