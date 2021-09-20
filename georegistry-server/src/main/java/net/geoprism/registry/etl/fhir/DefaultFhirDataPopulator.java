package net.geoprism.registry.etl.fhir;

import java.util.Collection;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationMode;
import org.hl7.fhir.r4.model.Location.LocationStatus;
import org.hl7.fhir.r4.model.Organization;

import com.runwaysdk.business.Business;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.registry.MasterListVersion;

public class DefaultFhirDataPopulator implements FhirDataPopulator
{
  @Override
  public boolean supports(MasterListVersion version)
  {
    return false;
  }

  @Override
  public void configure(FhirExportContext context, MasterListVersion version, boolean resolveIds)
  {
  }

  @Override
  public void populate(Business row, Facility facility)
  {
    Organization org = facility.getOrganization();
    org.addAlias(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE));

    Location location = facility.getLocation();
    location.setMode(LocationMode.INSTANCE);
    location.setStatus(LocationStatus.ACTIVE);
    location.addAlias(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE));

    Collection<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      String attributeName = DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString();

      if (row.hasAttribute(attributeName))
      {
        String value = row.getValue(attributeName);

        if (value != null)
        {
          org.addAlias(value);
          location.addAlias(value);
        }
      }
    }
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
