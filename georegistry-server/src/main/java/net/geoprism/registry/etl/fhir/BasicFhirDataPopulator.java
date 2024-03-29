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

import net.geoprism.registry.ListTypeVersion;

public class BasicFhirDataPopulator implements FhirDataPopulator
{
  @Override
  public String getLabel()
  {
    return "Basic Export Implementation";
  }

  @Override
  public void configure(FhirConnection context, ListTypeVersion version, boolean resolveIds)
  {
  }

  @Override
  public void populate(Business row, Facility facility)
  {
    Organization org = facility.getOrganization();
    org.addAlias(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + ListTypeVersion.DEFAULT_LOCALE));

    Location location = facility.getLocation();
    location.setMode(LocationMode.INSTANCE);
    location.setStatus(LocationStatus.ACTIVE);
    location.addAlias(row.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + ListTypeVersion.DEFAULT_LOCALE));

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
