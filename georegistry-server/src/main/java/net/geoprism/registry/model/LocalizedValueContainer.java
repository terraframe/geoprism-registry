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
package net.geoprism.registry.model;

import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.localization.LocalizedValueIF;

public class LocalizedValueContainer implements LocalizedValueIF
{
  private LocalizedValue value;

  public LocalizedValueContainer(LocalizedValue value)
  {
    this.value = value;
  }

  @Override
  public String getValue()
  {
    return this.value.getValue();
  }

  @Override
  public String getValue(Locale locale)
  {
    return this.value.getValue(locale);
  }

  @Override
  public void setValue(Locale locale, String value)
  {
    this.value.setValue(locale, value);
  }

  @Override
  public void setValue(String value)
  {
    this.value.setValue(value);
  }

  @Override
  public Map<String, String> getLocaleMap()
  {
    return this.value.getLocaleMap();
  }

  @Override
  public void setLocaleMap(Map<String, String> map)
  {
    this.value.setLocaleMap(map);
  }

  @Override
  public String getDefaultValue()
  {
    return this.value.getValue(LocalizedValue.DEFAULT_LOCALE);
  }

  @Override
  public void setDefaultValue(String value)
  {
    this.value.setValue(LocalizedValue.DEFAULT_LOCALE, value);
  }
}
