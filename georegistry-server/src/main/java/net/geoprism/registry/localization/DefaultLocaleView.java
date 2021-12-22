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
package net.geoprism.registry.localization;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.conversion.LocalizedValueConverter;

public class DefaultLocaleView extends LocaleView
{
  public static final String LABEL = "locale.defaultLocale.label";
  
  public static final String DISPLAY_LANGUAGE = "locale.defaultLocale.displayLanguage";
  
  public static final String DISPLAY_COUNTRY = "locale.defaultLocale.displayCountry";
  
  public static final String DISPLAY_VARIANT = "locale.defaultLocale.displayVariant";
  
  protected LocalizedValueIF countryLabel;
  protected LocalizedValueIF languageLabel;
  protected LocalizedValueIF variantLabel;
  
  public DefaultLocaleView()
  {
    LocalizedValueIF sessionLabel = LocalizationFacade.localizeAll(LABEL);
    this.label = LocalizedValueConverter.convert(sessionLabel.getValue(), sessionLabel.getLocaleMap());
    
    this.languageLabel = LocalizationFacade.localizeAll(DISPLAY_LANGUAGE);
    this.countryLabel = LocalizationFacade.localizeAll(DISPLAY_COUNTRY);
    this.variantLabel = LocalizationFacade.localizeAll(DISPLAY_VARIANT);
    
    this.isDefaultLocale = true;
  }
  
  public JsonObject toJson()
  {
    JsonObject jo = new JsonObject();
    
    Locale sessionLocale = Session.getCurrentLocale();
    
    jo.addProperty("isDefaultLocale", this.isDefaultLocale);
    
    jo.addProperty("toString", MdAttributeLocalInfo.DEFAULT_LOCALE);
    jo.addProperty("tag", MdAttributeLocalInfo.DEFAULT_LOCALE);
    
    JsonObject joLanguage = new JsonObject();
    joLanguage.addProperty("label", this.languageLabel.getValue(sessionLocale));
    joLanguage.addProperty("code", MdAttributeLocalInfo.DEFAULT_LOCALE);
    jo.add("language", joLanguage);
    
    JsonObject joCountry = new JsonObject();
    joCountry.addProperty("label", this.countryLabel.getValue(sessionLocale));
    joCountry.addProperty("code", MdAttributeLocalInfo.DEFAULT_LOCALE);
    jo.add("country", joCountry);
    
    JsonObject joVariant = new JsonObject();
    joVariant.addProperty("label", this.variantLabel.getValue(sessionLocale));
    joVariant.addProperty("code", MdAttributeLocalInfo.DEFAULT_LOCALE);
    jo.add("variant", joVariant);
    
    jo.add("label", label.toJSON());
    
    return jo;
  }
  
  public static DefaultLocaleView fromJson(String json)
  {
    JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
    
    DefaultLocaleView lv = new DefaultLocaleView();
    
    lv.setLabel(LocalizedValue.fromJSON(jo.get("label").getAsJsonObject()));
    
    return lv;
  }
}
