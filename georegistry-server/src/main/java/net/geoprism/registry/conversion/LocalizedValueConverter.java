/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.attributes.entity.AttributeLocal;
import com.runwaysdk.dataaccess.graph.GraphObjectDAO;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdGraphClassDAO;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.Organization;
import net.geoprism.registry.service.ServiceFactory;

public class LocalizedValueConverter
{

  public Term getTerm(String code)
  {
    return ServiceFactory.getMetadataCache().getTerm(code).get();
  }

  public Term geoObjectStatusToTerm(GeoObjectStatus gos)
  {
    return geoObjectStatusToTerm(gos.getEnumName());
  }

  public Term geoObjectStatusToTerm(String termCode)
  {
    if (termCode.equals(GeoObjectStatus.ACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.INACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.NEW.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code);
    }
    else if (termCode.equals(GeoObjectStatus.PENDING.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.PENDING.code);
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status [" + termCode + "].");
    }
  }

  public static LocalizedValue convert(LocalStruct localStruct)
  {
    LocalizedValue label = new LocalizedValue(localStruct.getValue());
    label.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, localStruct.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      label.setValue(locale, localStruct.getValue(locale));
    }

    return label;
  }

  public static LocalizedValue convert(String value, Map<String, String> map)
  {
    LocalizedValue localizedValue = new LocalizedValue(value);
    localizedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, map.get(MdAttributeLocalInfo.DEFAULT_LOCALE));

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      localizedValue.setValue(locale, map.get(locale.toString()));
    }

    return localizedValue;
  }

  public static void populate(MdGraphClassDAO mdClass, String attributeName, LocalizedValue label)
  {
    AttributeLocal attributeLocal = (AttributeLocal) mdClass.getAttribute(attributeName);
    LocalStruct struct = (LocalStruct) BusinessFacade.get(attributeLocal.getStructDAO());

    populate(struct, label);
  }

  public static void populate(LocalStruct struct, LocalizedValue label)
  {
    struct.setValue(label.getValue());
    struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      if (label.contains(locale))
      {
        struct.setValue(locale, label.getValue(locale));
      }
    }
  }

  public static void populate(LocalStruct struct, LocalizedValue label, String suffix)
  {
    struct.setValue(label.getValue());
    struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE) + suffix);

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      if (label.contains(locale))
      {
        struct.setValue(locale, label.getValue(locale) + suffix);
      }
    }
  }
  
  public static LocalizedValue convert(GraphObjectDAO graphObject)
  {
    String attributeName = Session.getCurrentLocale().toString();
    String defaultLocale = (String) graphObject.getObjectValue(MdAttributeLocalInfo.DEFAULT_LOCALE);
    String value = (String) ( graphObject.hasAttribute(attributeName) && graphObject.getObjectValue(attributeName) != null ? graphObject.getObjectValue(attributeName) : defaultLocale );

    LocalizedValue localizedValue = new LocalizedValue(value);
    localizedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, defaultLocale);

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      localizedValue.setValue(locale, (String) graphObject.getObjectValue(locale.toString()));
    }

    return localizedValue;
  }

  public static LocalizedValue convert(GraphObject graphObject)
  {
    String attributeName = Session.getCurrentLocale().toString();
    String defaultLocale = graphObject.getObjectValue(MdAttributeLocalInfo.DEFAULT_LOCALE);
    String value = (String) ( graphObject.hasAttribute(attributeName) && graphObject.getObjectValue(attributeName) != null ? graphObject.getObjectValue(attributeName) : defaultLocale );

    LocalizedValue localizedValue = new LocalizedValue(value);
    localizedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, defaultLocale);

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      localizedValue.setValue(locale, (String) graphObject.getObjectValue(locale.toString()));
    }

    return localizedValue;
  }

  public static void populate(GraphObject graphObject, String attributeName, LocalizedValue value)
  {
    graphObject.setEmbeddedValue(attributeName, MdAttributeLocalInfo.DEFAULT_LOCALE, value.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      if (value.contains(locale))
      {
        graphObject.setEmbeddedValue(attributeName, locale.toString(), value.getValue(locale));
      }
    }
  }

  public static void populate(GraphObject graphObject, String attributeName, LocalizedValue value, Date startDate, Date endDate)
  {
    graphObject.setEmbeddedValue(attributeName, MdAttributeLocalInfo.DEFAULT_LOCALE, value.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE), startDate, endDate);

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      if (value.contains(locale))
      {
        graphObject.setEmbeddedValue(attributeName, locale.toString(), value.getValue(locale), startDate, endDate);
      }
    }
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
      registryRole.setOrganizationLabel(LocalizedValueConverter.convert(organization.getDisplayLabel()));
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
      registryRole.setGeoObjectTypeLabel(LocalizedValueConverter.convert(universal.getDisplayLabel()));
    }
  }

}
