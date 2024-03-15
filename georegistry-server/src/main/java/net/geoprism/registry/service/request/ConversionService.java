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
package net.geoprism.registry.service.request;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.service.request.ServiceFactory;

public class ConversionService
{
  public ConversionService()
  {
  }

  // public void populate(LocalStruct struct, LocalizedValue label)
  // {
  // struct.setValue(label.getValue());
  // struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE,
  // label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));
  //
  // List<Locale> locales = LocalizationFacade.getInstalledLocales();
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
  // List<Locale> locales = LocalizationFacade.getInstalledLocales();
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
    if (code == null)
    {
      return null;
    }

//    return ServiceFactory.getMetadataCache().getTerm(code).get();
    return null;
  }

  // public GeoObject getGeoObjectById(String uid, String geoObjectTypeCode)
  // {
  // Universal universal = Universal.getByKey(geoObjectTypeCode);
  //
  // MdBusiness mdBusiness = universal.getMdBusiness();
  //
  //
  //
  // }

  public Classifier termToClassifier(AttributeTermType attr, Term term)
  {
    Term root = attr.getRootTerm();
    String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

    String classifierKey = Classifier.buildKey(parent, term.getCode());
    Classifier classifier = Classifier.getByKey(classifierKey);

    return classifier;
  }
}
