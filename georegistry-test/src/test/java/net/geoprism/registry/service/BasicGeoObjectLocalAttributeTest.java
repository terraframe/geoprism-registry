package net.geoprism.registry.service;

import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.business.generation.GenerationFacade;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.localization.LocaleView;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.request.GPRLocalizationService;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicGeoObjectLocalAttributeTest implements InstanceTestClassListener
{
  protected static final Locale          testLocale = Locale.SIMPLIFIED_CHINESE;

  private static ServerGeoObjectType     type;

  private static AttributeLocalType      attributeLocal;

  private static SupportedLocaleIF       locale;

  private static Boolean                 skipCodeGen;

  @Autowired
  private GPRLocalizationService         localizationService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;

  @Autowired
  private GeoObjectBusinessServiceIF     service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    skipCodeGen = LocalProperties.isSkipCodeGenAndCompile();

    LocalProperties.setSkipCodeGenAndCompile(true);

    USATestData.ORG_NPS.apply();

    type = this.typeService.create(USATestData.COUNTRY.toDTO());

    attributeLocal = this.typeService.createAttributeType(type, new AttributeLocalType("testLocal", new LocalizedValue("Test Local"), new LocalizedValue("Test Local"), false, false, false));

    LocaleView lv = new LocaleView();
    lv.setLocale(testLocale);
    lv.setLabel(new LocalizedValue("Test Locale"));

    locale = this.localizationService.installLocale(lv);
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (locale != null)
    {
      LocaleView lv = new LocaleView();
      lv.setLocale(testLocale);
      lv.setLabel(new LocalizedValue("Test Locale"));

      this.localizationService.uninstallLocale(lv);
    }

    this.typeService.deleteGeoObjectType(type.getCode());

    USATestData.ORG_NPS.delete();

    LocalProperties.setSkipCodeGenAndCompile(skipCodeGen);
  }

  @Test
  @Request
  public void testCreateDeleteGeoObject()
  {
    LocalizedValue value = new LocalizedValue("Test Value");
    value.setValue(testLocale, "Locale 1 value");
    value.setValue(LocalizedValue.DEFAULT_LOCALE, "Default locale value");

    ServerGeoObjectIF object = this.service.newInstance(type);

    try
    {
      object.setInvalid(false);
      object.setCode(USATestData.USA.getCode());
      object.setDisplayLabel(new LocalizedValue(USATestData.USA.getDisplayLabel()), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setExists(true, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setGeometry(USATestData.USA.getGeometry(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setValue(attributeLocal.getName(), value, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      this.service.apply(object, false);

      ServerGeoObjectIF test = this.service.getGeoObject(object.getUid(), type.getCode());

      Assert.assertNotNull(test);

      LocalizedValue testLocalValue = test.getValue(attributeLocal.getName(), USATestData.DEFAULT_OVER_TIME_DATE);

      Assert.assertEquals(value.getValue(LocalizedValue.DEFAULT_LOCALE), testLocalValue.getValue(LocalizedValue.DEFAULT_LOCALE));
      Assert.assertEquals(value.getValue(testLocale), testLocalValue.getValue(testLocale));
    }
    finally
    {
      object.delete();
    }
  }
}
