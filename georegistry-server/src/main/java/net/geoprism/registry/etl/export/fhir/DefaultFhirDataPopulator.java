package net.geoprism.registry.etl.export.fhir;

import java.util.Collection;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
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
  public void populateOrganization(FhirExportContext context, Business row, Organization org)
  {
    org.addAlias(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE));

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
        }
      }
    }
  }

  @Override
  public void populateLocation(FhirExportContext context, Business row, Location location)
  {
    location.setMode(LocationMode.INSTANCE);
    location.setStatus(LocationStatus.ACTIVE);

    Collection<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      String attributeName = DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString();

      if (row.hasAttribute(attributeName))
      {
        String value = row.getValue(attributeName);

        if (value != null)
        {
          location.addAlias(value);
        }
      }
    }
  }

}
