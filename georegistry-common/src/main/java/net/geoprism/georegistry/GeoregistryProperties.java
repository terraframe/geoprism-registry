package net.geoprism.georegistry;

import com.runwaysdk.configuration.ConfigurationManager;
import com.runwaysdk.configuration.ConfigurationReaderIF;
import com.runwaysdk.generation.loader.Reloadable;

public class GeoregistryProperties
{
  /**
   * The server.properties configuration file
   */
  private ConfigurationReaderIF            props;

  private GeoregistryProperties()
  {
    this.props = ConfigurationManager.getReader(GeoregistryConfigGroup.COMMON, "georegistry.properties");
  }

  private static class Singleton implements Reloadable
  {
    private static GeoregistryProperties INSTANCE = new GeoregistryProperties();

    private static GeoregistryProperties getInstance()
    {
      // INSTANCE will only ever be null if there is a problem. The if check is to allow for debugging.
      if (INSTANCE == null)
      {
        INSTANCE = new GeoregistryProperties();
      }

      return INSTANCE;
    }

    private static ConfigurationReaderIF getProps()
    {
      return getInstance().props;
    }
  }

//  public static Integer getTileThreads()
//  {
//    return Singleton.getProps().getInteger("tile.threads");
//  }
}
