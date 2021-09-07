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
package net.geoprism.registry.localization;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.LocaleUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.conversion.LocalizedValueConverter;

public class LocaleView
{
  
  protected Locale locale;
  
  protected LocalizedValue label;
  
  protected boolean isDefaultLocale = false;
  
  public LocaleView()
  {
    
  }
  
  public Locale getLocale()
  {
    return locale;
  }

  public void setLocale(Locale locale)
  {
    this.locale = locale;
  }

  public LocalizedValue getLabel()
  {
    return label;
  }

  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }
  
  /**
   * Populates the SupportedLocaleIF with the display labels contained in this view.
   */
  public void populate(SupportedLocaleIF supportedLocale)
  {
    supportedLocale.getDisplayLabel().setLocaleMap(this.label.getLocaleMap());
  }
  
  /**
   * Populates this LocalizationView from values contained within the SupportedLocaleIF
   */
  public void populateThis(SupportedLocaleIF supportedLocale)
  {
    this.setLocale(supportedLocale.getLocale());
    
    this.label = LocalizedValueConverter.convert(supportedLocale.getDisplayLabel().getValue(), supportedLocale.getDisplayLabel().getLocaleMap());
  }
  
  public JsonObject toJson()
  {
    JsonObject jo = new JsonObject();
    
    Locale sessionLocale = Session.getCurrentLocale();
    
    jo.addProperty("isDefaultLocale", this.isDefaultLocale);
    
    jo.addProperty("toString", this.locale.toString());
    jo.addProperty("tag", this.locale.toLanguageTag());
    
    JsonObject joLanguage = new JsonObject();
    joLanguage.addProperty("label", this.locale.getDisplayLanguage(sessionLocale));
    joLanguage.addProperty("code", this.locale.getLanguage());
    jo.add("language", joLanguage);
    
    JsonObject joCountry = new JsonObject();
    joCountry.addProperty("label", this.locale.getDisplayCountry(sessionLocale));
    joCountry.addProperty("code", this.locale.getCountry());
    jo.add("country", joCountry);
    
    JsonObject joVariant = new JsonObject();
    joVariant.addProperty("label", this.locale.getDisplayVariant(sessionLocale));
    joVariant.addProperty("code", this.locale.getVariant());
    jo.add("variant", joVariant);
    
    jo.add("label", label.toJSON());
    
    return jo;
  }
  
  public static LocaleView fromJson(String json)
  {
    JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
    
    if (jo.get("isDefaultLocale").getAsBoolean())
    {
      return DefaultLocaleView.fromJson(json);
    }
    
    LocaleView lv = new LocaleView();
    
    String language = jo.get("language").getAsJsonObject().get("code").getAsString();
    String country = jo.get("country").getAsJsonObject().get("code").getAsString();
    
    String variant = null;
    if (jo.has("variant") && jo.get("variant").getAsJsonObject().has("code"))
    {
      variant = jo.get("variant").getAsJsonObject().get("code").getAsString();
    }
    
    Locale locale = LocalizationFacade.getLocale(language, country, variant);
    lv.setLocale(locale);
    
    lv.setLabel(LocalizedValue.fromJSON(jo.get("label").getAsJsonObject()));
    
    return lv;
  }
  
  public static LocaleView fromSupportedLocale(SupportedLocaleIF locale)
  {
    LocaleView lv = new LocaleView();
    
    lv.populateThis(locale);
    
    return lv;
  }
  
}
