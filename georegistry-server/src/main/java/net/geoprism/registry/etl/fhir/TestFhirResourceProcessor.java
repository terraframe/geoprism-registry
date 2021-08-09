package net.geoprism.registry.etl.fhir;

import java.util.Date;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;

import net.geoprism.registry.etl.FhirSyncImportConfig;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;

public class TestFhirResourceProcessor extends AbstractFhirResourceProcessor implements FhirResourceProcessor
{

  @Override
  public boolean supports(FhirSyncImportConfig config)
  {
    return true;
  }

  @Override
  protected void populate(ServerGeoObjectIF geoObject, Location location, Date lastUpdated)
  {
    LocalizedValue value = LocalizedValue.createEmptyLocalizedValue();
    value.setValue(LocalizedValue.DEFAULT_LOCALE, location.getName());

    geoObject.setDisplayLabel(value, lastUpdated, ValueOverTime.INFINITY_END_DATE);
  }

  @Override
  protected String getType(Organization organization)
  {
    Coding coding = organization.getTypeFirstRep().getCodingFirstRep();

    if (coding != null)
    {
      String code = coding.getCode();

      if (code != null)
      {
        return code;
      }
    }

    return "Country";
  }

  @Override
  protected String getType(Location location)
  {
    Coding coding = location.getTypeFirstRep().getCodingFirstRep();

    if (coding != null)
    {
      String code = coding.getCode();

      if (code != null)
      {
        return code;
      }
    }

    return "Country";
  }

  @Override
  protected Identifier getIdentifier(Organization organization)
  {
    return organization.getIdentifier().stream().filter(i -> i.getSystem().equals(this.getSystem().getSystem())).findFirst().orElse(null);
  }

  @Override
  protected Identifier getIdentifier(Location location)
  {
    return location.getIdentifier().stream().filter(i -> i.getSystem().equals(this.getSystem().getSystem())).findFirst().orElse(null);
  }

}
