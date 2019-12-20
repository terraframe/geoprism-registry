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
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultTerms;

import com.runwaysdk.dataaccess.ProgrammingErrorException;

import net.geoprism.registry.GeoObjectStatus;

public class ConversionService
{
  public ConversionService()
  {
  }

  public static ConversionService getInstance()
  {
    return ServiceFactory.getConversionService();
  }

  // public void populate(LocalStruct struct, LocalizedValue label)
  // {
  // struct.setValue(label.getValue());
  // struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE,
  // label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));
  //
  // List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();
  //
  // for (Locale locale : locales)
  // {
  // if (label.contains(locale))
  // {
  // struct.setValue(locale, label.getValue(locale));
  // }
  // }
  // }
  //
  // public void populate(LocalStruct struct, LocalizedValue label, String
  // suffix)
  // {
  // struct.setValue(label.getValue());
  // struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE,
  // label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE) + suffix);
  //
  // List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();
  //
  // for (Locale locale : locales)
  // {
  // if (label.contains(locale))
  // {
  // struct.setValue(locale, label.getValue(locale) + suffix);
  // }
  // }
  // }

  public Term getTerm(String code)
  {
    return ServiceFactory.getAdapter().getMetadataCache().getTerm(code).get();
  }

  // public GeoObject getGeoObjectById(String uuid, String geoObjectTypeCode)
  // {
  // Universal universal = Universal.getByKey(geoObjectTypeCode);
  //
  // MdBusiness mdBusiness = universal.getMdBusiness();
  //
  //
  //
  // }

  public GeoObjectStatus termToGeoObjectStatus(Term term)
  {
    if (term == null)
    {
      return null;
    }
    
    return this.termToGeoObjectStatus(term.getCode());
  }

  public GeoObjectStatus termToGeoObjectStatus(String termCode)
  {
    if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code))
    {
      return GeoObjectStatus.ACTIVE;
    }
    else if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code))
    {
      return GeoObjectStatus.INACTIVE;
    }
    else if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.NEW.code))
    {
      return GeoObjectStatus.NEW;
    }
    else if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.PENDING.code))
    {
      return GeoObjectStatus.PENDING;
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status Term [" + termCode + "].");
    }
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
}
