package net.geoprism.registry.etl.export.fhir;

import org.hl7.fhir.r4.model.Organization;

import com.runwaysdk.business.Business;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class FhirExportFactory
{

  public static FhirDataPopulator getPopulator(final MasterListVersion version)
  {
    MasterList list = version.getMasterlist();
    ServerGeoObjectType type = list.getGeoObjectType();

    if (!type.getCode().equals("Country"))
    {
      return new AbstractFhirDataPopulator(version)
      {
        @Override
        public void populateOrganization(FhirExportContext context, Business row, Organization org)
        {
          this.addHierarchyValue(context, row, org, ServerHierarchyType.get("Around"));
        }
      };
    }

    return new DefaultFhirDataPopulator();
  }
}
