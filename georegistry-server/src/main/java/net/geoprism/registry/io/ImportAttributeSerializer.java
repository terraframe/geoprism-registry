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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.SupportedLocaleIF;

import net.geoprism.registry.localization.DefaultLocaleView;
import net.geoprism.registry.service.LocaleSerializer;

public class ImportAttributeSerializer extends LocaleSerializer implements CustomSerializer
{
  private Set<String>  filter;

  private boolean      includeCoordinates;

  private GeoObjectType type;
  
  public ImportAttributeSerializer(Locale locale, boolean includeCoordinates, GeoObjectType type)
  {
    this(locale, includeCoordinates, false, type);
  }

  public ImportAttributeSerializer(Locale locale, boolean includeCoordinates, boolean includeUid, GeoObjectType type)
  {
    super(locale);

    this.type = type;
    this.includeCoordinates = includeCoordinates;

    this.filter = new TreeSet<String>();
    this.filter.add(DefaultAttribute.LAST_UPDATE_DATE.getName());
    this.filter.add(DefaultAttribute.CREATE_DATE.getName());
    this.filter.add(DefaultAttribute.SEQUENCE.getName());
    this.filter.add(DefaultAttribute.TYPE.getName());
    
    for (AttributeType attr : type.getAttributeMap().values())
    {
      if (attr instanceof AttributeLocalType)
      {
        this.filter.add(attr.getName());
      }
    }

    this.filter.add(DefaultAttribute.INVALID.getName());
    this.filter.add(DefaultAttribute.EXISTS.getName());

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
    JsonArray jaAttrs = super.serialize(type, attributes);

    for (AttributeType attr : this.type.getAttributeMap().values())
    {
      if (attr instanceof AttributeLocalType)
      {
        serializeLocalAttribute(attr, jaAttrs);
      }
    }

    return jaAttrs;
  }

  private void serializeLocalAttribute(AttributeType attr, JsonArray jaAttrs)
  {
    jaAttrs.add(this.serializeLocale(attr, LocalizedValue.DEFAULT_LOCALE, LocalizationFacade.localize(DefaultLocaleView.LABEL)));
    
    for (SupportedLocaleIF locale : LocalizationFacade.getSupportedLocales())
    {
      jaAttrs.add(this.serializeLocale(attr, locale.getLocale().toString(), locale.getDisplayLabel().getValue()));
    }
  }

  public JsonObject serializeLocale(AttributeType displayLabel, String key, String label)
  {
    JsonObject attribute = displayLabel.toJSON(this);
    attribute.addProperty("locale", key);
    attribute.addProperty(AttributeType.JSON_CODE, displayLabel.getName());
//    attribute.addProperty(AttributeType.JSON_REQUIRED, key.equals(LocalizedValue.DEFAULT_LOCALE));

    JsonObject jaLabel = attribute.get(AttributeType.JSON_LOCALIZED_LABEL).getAsJsonObject();
    String value = jaLabel.get(LocalizedValue.LOCALIZED_VALUE).getAsString();
    value += " (" + label + ")";
    jaLabel.addProperty(LocalizedValue.LOCALIZED_VALUE, value);
    
    return attribute;
  }

  @Override
  public Collection<AttributeType> attributes(GeoObjectType type)
  {
    List<AttributeType> attributes = type.getAttributeMap().values().stream().filter(attributeType -> !this.filter.contains(attributeType.getName())).collect(Collectors.toList());

    if (this.includeCoordinates)
    {
      attributes.add(0, GeoObjectImportConfiguration.latitude());
      attributes.add(0, GeoObjectImportConfiguration.longitude());
    }

    return attributes;
  }
}
