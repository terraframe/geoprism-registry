package net.geoprism.georegistry.service;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;

import com.google.gson.JsonObject;

public class LocaleSerializer extends DefaultSerializer implements CustomSerializer
{
  private Locale locale;

  public LocaleSerializer(Locale locale)
  {
    super();
    this.locale = locale;
  }

  @Override
  public void configure(LocalizedValue localizedValue, JsonObject object)
  {
    String value = localizedValue.getValue(this.locale);

    if (value != null)
    {
      object.addProperty(LocalizedValue.LOCALIZED_VALUE, value);
    }
  }

}
