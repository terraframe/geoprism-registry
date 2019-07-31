package net.geoprism.registry.conversion;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;

public class AbstractBuilder
{
  public LocalizedValue convert(LocalStruct localStruct)
  {
    LocalizedValue label = new LocalizedValue(localStruct.getValue());
    label.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, localStruct.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      label.setValue(locale, localStruct.getValue(locale));
    }

    return label;
  }

  public LocalizedValue convert(String value, Map<String, String> map)
  {
    LocalizedValue localizedValue = new LocalizedValue(value);
    localizedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, map.get(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      localizedValue.setValue(locale, map.get(locale.toString()));
    }

    return localizedValue;
  }

}
