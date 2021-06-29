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
