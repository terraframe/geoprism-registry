/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.service.LocaleSerializer;

public class ImportAttributeSerializer extends LocaleSerializer implements CustomSerializer
{
  private Set<String>  filter;

  private boolean      includeCoordinates;

  private List<Locale> locales;

  public ImportAttributeSerializer(Locale locale, boolean includeCoordinates, List<Locale> locales)
  {
    this(locale, includeCoordinates, false, locales);
  }

  public ImportAttributeSerializer(Locale locale, boolean includeCoordinates, boolean includeUid, List<Locale> locales)
  {
    super(locale);

    this.includeCoordinates = includeCoordinates;
    this.locales = locales;

    this.filter = new TreeSet<String>();
    this.filter.add(DefaultAttribute.STATUS.getName());
    this.filter.add(DefaultAttribute.LAST_UPDATE_DATE.getName());
    this.filter.add(DefaultAttribute.CREATE_DATE.getName());
    this.filter.add(DefaultAttribute.SEQUENCE.getName());
    this.filter.add(DefaultAttribute.TYPE.getName());
    this.filter.add(DefaultAttribute.DISPLAY_LABEL.getName());

    if (!includeUid)
    {
      this.filter.add(DefaultAttribute.UID.getName());
    }
  }

  public Set<String> getFilter()
  {
    return filter;
  }

  @Override
  public JsonArray serialize(GeoObjectType type, Collection<AttributeType> attributes)
  {
    JsonArray attrs = super.serialize(type, attributes);

    /*
     * Add a display label attribute for each locale
     */
    AttributeType displayLabel = type.getAttribute(DefaultAttribute.DISPLAY_LABEL.getName()).get();

    attrs.add(this.serializeLocaleAttribute(displayLabel, LocalizedValue.DEFAULT_LOCALE));

    for (Locale locale : this.locales)
    {
      String key = locale.toString();

      attrs.add(this.serializeLocaleAttribute(displayLabel, key));
    }

    return attrs;
  }

  public JsonObject serializeLocaleAttribute(AttributeType displayLabel, String key)
  {
    JsonObject attribute = displayLabel.toJSON(this);
    attribute.addProperty("locale", key);
    attribute.addProperty(AttributeType.JSON_CODE, displayLabel.getName());
    attribute.addProperty(AttributeType.JSON_REQUIRED, key.equals(LocalizedValue.DEFAULT_LOCALE));

    JsonObject label = attribute.get(AttributeType.JSON_LOCALIZED_LABEL).getAsJsonObject();
    String value = label.get(LocalizedValue.LOCALIZED_VALUE).getAsString();
    value += " (" + key + ")";

    label.addProperty(LocalizedValue.LOCALIZED_VALUE, value);
    return attribute;
  }

  @Override
  public Collection<AttributeType> attributes(GeoObjectType type)
  {
    List<AttributeType> attributes = type.getAttributeMap().values().stream().filter(attributeType -> !this.filter.contains(attributeType.getName())).collect(Collectors.toList());

    if (this.includeCoordinates)
    {
      attributes.add(0, GeoObjectConfiguration.latitude());
      attributes.add(0, GeoObjectConfiguration.longitude());
    }

    return attributes;
  }
}
