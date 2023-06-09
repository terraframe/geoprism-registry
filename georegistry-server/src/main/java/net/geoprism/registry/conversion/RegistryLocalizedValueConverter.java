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
package net.geoprism.registry.conversion;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.LocalStructDTO;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.attributes.entity.AttributeLocal;
import com.runwaysdk.dataaccess.graph.GraphObjectDAO;
import com.runwaysdk.dataaccess.metadata.MetadataDAO;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.LocalizedValueContainer;
import net.geoprism.registry.service.ServiceFactory;

public class RegistryLocalizedValueConverter extends LocalizedValueConverter
{

  public Term getTerm(String code)
  {
    return ServiceFactory.getMetadataCache().getTerm(code).get();
  }

  /**
   * Set the owner to the corresponding {@link Organization} role for the given
   * code, or if code is null then the owner field is not set.
   * 
   * @param business
   * @param organizationCode
   */
  protected static void setOwner(Business business, String organizationCode)
  {
    Organization organization = null;
    Roles orgRole = null;
    if (organizationCode != null && !organizationCode.equals(""))
    {
      organization = Organization.getByKey(organizationCode);
      orgRole = organization.getRole();
      business.setOwner(orgRole);
    }
  }

  /**
   * Populates the {@link Organization} display label on the given
   * {@link RegistryRole} object.
   * 
   * @param registryRole
   */
  public static void populateOrganizationDisplayLabel(RegistryRole registryRole)
  {
    String organizationCode = registryRole.getOrganizationCode();
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      Organization organization = Organization.getByCode(organizationCode);
      registryRole.setOrganizationLabel(RegistryLocalizedValueConverter.convert(organization.getDisplayLabel()));
    }
  }

  /**
   * Populates the {@link GeoObjectType} display label on the given
   * {@link RegistryRole} object.
   * 
   * @param registryRole
   */
  public static void populateGeoObjectTypeLabel(RegistryRole registryRole)
  {
    String geoObjectTypeCode = registryRole.getGeoObjectTypeCode();
    if (geoObjectTypeCode != null && !geoObjectTypeCode.trim().equals(""))
    {
      Universal universal = Universal.getByKey(geoObjectTypeCode);
      registryRole.setGeoObjectTypeLabel(RegistryLocalizedValueConverter.convert(universal.getDisplayLabel()));
    }
  }

  public static LocalizedValue convert(LocalizedValueIF val)
  {
    if (val instanceof LocalStruct)
    {
      return RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) val);
    }
    else if (val instanceof LocalizedValueContainer)
    {
      return ((LocalizedValueContainer) val).getLocalizedValue();
    }
    else
    {
      throw new UnsupportedOperationException("TODO");
    }
  }

}
