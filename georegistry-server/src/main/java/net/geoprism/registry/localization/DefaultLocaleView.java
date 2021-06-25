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
  protected LocalizedValueIF countryLabel;
  protected LocalizedValueIF languageLabel;
  protected LocalizedValueIF variantLabel;
  
  public DefaultLocaleView()
  {
    LocalizedValueIF sessionLabel = LocalizationFacade.localizeAll("locale.defaultLocale.label");
    this.label = LocalizedValueConverter.convert(sessionLabel.getValue(), sessionLabel.getLocaleMap());
    
    this.languageLabel = LocalizationFacade.localizeAll("locale.defaultLocale.displayLanguage");
    this.countryLabel = LocalizationFacade.localizeAll("locale.defaultLocale.displayCountry");
    this.variantLabel = LocalizationFacade.localizeAll("locale.defaultLocale.displayVariant");
    
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
