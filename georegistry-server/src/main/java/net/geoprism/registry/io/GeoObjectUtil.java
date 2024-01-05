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
package net.geoprism.registry.io;

import java.util.Iterator;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class GeoObjectUtil
{
  @SuppressWarnings("unchecked")
  public static String convertToTermString(AttributeTermType attributeType, Object value)
  {
    StringBuilder builder = new StringBuilder();
    boolean first = true;

    if (value instanceof Classifier)
    {
      if (!first)
      {
        builder.append(",");
      }

      builder.append( ( (Classifier) value ).getDisplayLabel().getValue());
      first = false;
    }
    else
    {
      Iterator<String> codes = (Iterator<String>) value;

      if (codes != null)
      {

        while (codes.hasNext())
        {
          String code = codes.next();
          Term term = attributeType.getTermByCode(code).get();

          if (!first)
          {
            builder.append(",");
          }

          builder.append(term.getLabel());
          first = false;
        }

        return builder.toString();
      }
    }

    return null;
  }

  public static String convertToTermString(AttributeClassificationType attributeType, Object value)
  {
    if (value instanceof VertexObject)
    {
      LocalizedValue localized = RegistryLocalizedValueConverter.convert( ( (VertexObject) value ).getEmbeddedComponent(AbstractClassification.DISPLAYLABEL));
      return localized.getValue();
    }
    else if (value instanceof String)
    {
      ClassificationBusinessServiceIF service = ServiceFactory.getBean(ClassificationBusinessServiceIF.class);
      Classification classification = service.get(attributeType, (String) value);

      return convertToTermString(attributeType, classification.getVertex());
    }

    return null;
  }
}
