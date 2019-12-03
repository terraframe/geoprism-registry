package net.geoprism.registry.conversion;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.attributes.entity.AttributeLocal;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdGraphClassDAO;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.service.ServiceFactory;

public class LocalizedValueConverter
{
  public Term getTerm(String code)
  {
    return ServiceFactory.getAdapter().getMetadataCache().getTerm(code).get();
  }

  public Term geoObjectStatusToTerm(GeoObjectStatus gos)
  {
    return geoObjectStatusToTerm(gos.getEnumName());
  }

  public Term geoObjectStatusToTerm(String termCode)
  {
    if (termCode.equals(GeoObjectStatus.ACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.INACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.NEW.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code);
    }
    else if (termCode.equals(GeoObjectStatus.PENDING.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.PENDING.code);
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status [" + termCode + "].");
    }
  }

  public static LocalizedValue convert(LocalStruct localStruct)
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

  public static LocalizedValue convert(String value, Map<String, String> map)
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

  public static void populate(MdGraphClassDAO mdClass, String attributeName, LocalizedValue label)
  {
    AttributeLocal attributeLocal = (AttributeLocal) mdClass.getAttribute(attributeName);
    LocalStruct struct = (LocalStruct) BusinessFacade.get(attributeLocal.getStructDAO());

    populate(struct, label);
  }

  public static void populate(LocalStruct struct, LocalizedValue label)
  {
    struct.setValue(label.getValue());
    struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      if (label.contains(locale))
      {
        struct.setValue(locale, label.getValue(locale));
      }
    }
  }

  public static void populate(LocalStruct struct, LocalizedValue label, String suffix)
  {
    struct.setValue(label.getValue());
    struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE) + suffix);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      if (label.contains(locale))
      {
        struct.setValue(locale, label.getValue(locale) + suffix);
      }
    }
  }

  public static LocalizedValue convert(GraphObject graphObject)
  {
    String attributeName = Session.getCurrentLocale().toString();
    String value = (String) ( graphObject.hasAttribute(attributeName) ? graphObject.getObjectValue(attributeName) : graphObject.getObjectValue(MdAttributeLocalInfo.DEFAULT_LOCALE) );

    LocalizedValue localizedValue = new LocalizedValue(value);
    localizedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, value);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      localizedValue.setValue(locale, (String) graphObject.getObjectValue(locale.toString()));
    }

    return localizedValue;
  }

  public static void populate(GraphObject graphObject, String attributeName, LocalizedValue value)
  {
    graphObject.setEmbeddedValue(attributeName, MdAttributeLocalInfo.DEFAULT_LOCALE, value.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      if (value.contains(locale))
      {
        graphObject.setEmbeddedValue(attributeName, locale.toString(), value.getValue(locale));
      }
    }
  }

  public static void populate(GraphObject graphObject, String attributeName, LocalizedValue value, Date startDate, Date endDate)
  {
    graphObject.setEmbeddedValue(attributeName, MdAttributeLocalInfo.DEFAULT_LOCALE, value.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE), startDate, endDate);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      if (value.contains(locale))
      {
        graphObject.setEmbeddedValue(attributeName, locale.toString(), value.getValue(locale), startDate, endDate);
      }
    }
  }

}
