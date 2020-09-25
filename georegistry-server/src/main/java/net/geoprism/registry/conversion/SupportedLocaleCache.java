package net.geoprism.registry.conversion;

import java.util.List;
import java.util.Locale;

import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;

public class SupportedLocaleCache
{
  private static List<Locale> locales = null;

  public static synchronized List<Locale> getLocales()
  {
    if (locales == null)
    {
      locales = SupportedLocaleDAO.getSupportedLocales();
    }
    
    return locales;
  }
  
  public static synchronized void clear()
  {
    locales = null;
  }

}
