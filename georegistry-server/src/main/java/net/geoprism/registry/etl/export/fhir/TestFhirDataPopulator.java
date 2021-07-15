package net.geoprism.registry.etl.export.fhir;

import com.runwaysdk.business.Business;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class TestFhirDataPopulator extends AbstractFhirDataPopulator implements FhirDataPopulator
{

  public TestFhirDataPopulator()
  {
    super();
  }

  @Override
  public boolean supports(MasterListVersion version)
  {
    MasterList list = version.getMasterlist();
    ServerGeoObjectType type = list.getGeoObjectType();

    return !type.getCode().equals("Country");
  }

  @Override
  public void populate(Business row, Facility facility)
  {
    super.populate(row, facility);
    
    this.addHierarchyValue(row, facility, ServerHierarchyType.get("Around"));
  }

}
