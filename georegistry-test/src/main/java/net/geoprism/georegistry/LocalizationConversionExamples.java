package net.geoprism.georegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.dataaccess.cache.globalcache.ehcache.CacheShutdown;
import com.runwaysdk.localization.LocalizationExcelExporter;
import com.runwaysdk.localization.LocalizationExcelImporter;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.configuration.ConfigurationBuilder;
import com.runwaysdk.localization.configuration.SpreadsheetConfiguration;
import com.runwaysdk.localization.converters.PropertiesFileToXMLLocalizationConverter;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdAttributeLocal;
import com.runwaysdk.system.metadata.MdLocalizable;

public class LocalizationConversionExamples
{
  public static void main(String[] args) throws Exception
  {
    doAll();
//    doConvert();
//    doLocalize();
  }
  
  @Request
  public static void doLocalize() throws Exception
  {
    LocalizedValueStore.localize("net.geoprism.CronPicker.everyMinute");
  }
  
  @Request
  public static void doConvert() throws Exception
  {
    try
    {
      PropertiesFileToXMLLocalizationConverter.main(new String[] {
          "--propertyFile=/home/rich/dev/workspace/georegistry/envcfg/messages-extension.properties",
          "--xmlFile=/home/rich/dev/tmp/xmlOut.xml",
          "--tagName=" + LocalizedValueStore.TAG_NAME_UI_TEXT
      });
    }
    finally
    {
      CacheShutdown.shutdown();
    }
  }
  
  @Request
  public static void doAll() throws Exception
  {
    try
    {
//      File clientProps = new File("/home/rich/dev/workspace/geoprism/geoprism-web/src/main/resources/clientExceptions.properties");
//      LocalizedValueStore.importPropertiesIntoStore(new FileInputStream(clientProps), LocalizedValueStore.TAG_NAME_RUNWAY_EXCEPTION);
//      
//      File commonsProps = new File("/home/rich/dev/workspace/geoprism/geoprism-web/src/main/resources/commonExceptions.properties");
//      LocalizedValueStore.importPropertiesIntoStore(new FileInputStream(commonsProps), LocalizedValueStore.TAG_NAME_RUNWAY_EXCEPTION);
//      
//      File serverProps = new File("/home/rich/dev/workspace/geoprism/geoprism-web/src/main/resources/serverExceptions.properties");
//      LocalizedValueStore.importPropertiesIntoStore(new FileInputStream(serverProps), LocalizedValueStore.TAG_NAME_RUNWAY_EXCEPTION);
//      
//      
//      File messagesProps = new File("/home/rich/dev/workspace/georegistry/envcfg/dev/messages.properties");
//      LocalizedValueStore.importPropertiesIntoStore(new FileInputStream(messagesProps), LocalizedValueStore.TAG_NAME_UI_TEXT);
      
      
      File file = new File("/home/rich/dev/tmp/localOut.xlsx");
      
      if (file.exists())
      {
        file.delete();
      }
      
      ConfigurationBuilder builder = new ConfigurationBuilder();
      builder.addAttributeLocalTab("Custom Exceptions", (MdAttributeLocal) BusinessFacade.get(MdLocalizable.getMessageMd()));
      builder.addLocalizedValueStoreTab("Runway Exceptions", LocalizedValueStore.TAG_NAME_RUNWAY_EXCEPTION);
      builder.addLocalizedValueStoreTab("UI Text", LocalizedValueStore.TAG_NAME_UI_TEXT);
      SpreadsheetConfiguration config = builder.build();
    
      LocalizationExcelExporter exporter = new LocalizationExcelExporter(config, new FileOutputStream(file));
      exporter.export();
      
      LocalizationExcelImporter importer = new LocalizationExcelImporter(config, new FileInputStream(file));
      importer.doImport();
    }
    finally
    {
      CacheShutdown.shutdown();
    }
  }
}
