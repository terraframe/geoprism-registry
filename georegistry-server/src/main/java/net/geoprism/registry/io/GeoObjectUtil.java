/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;

import com.runwaysdk.business.graph.VertexObject;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class GeoObjectUtil
{
  public static String convertToTermString(AttributeClassificationType attributeType, Object value)
  {
    if (value instanceof VertexObject)
    {
      LocalizedValue localized = RegistryLocalizedValueConverter.convert( ( (VertexObject) value ).getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName()));
      return localized.getValue();
    }
    else if (value instanceof String)
    {
      ConceptObjectBusinessServiceIF service = ServiceFactory.getBean(ConceptObjectBusinessServiceIF.class);
      ConceptObject classification = service.getByCode(attributeType, (String) value).get();

      return convertToTermString(attributeType, classification.getVertex());
    }

    return null;
  }
}
