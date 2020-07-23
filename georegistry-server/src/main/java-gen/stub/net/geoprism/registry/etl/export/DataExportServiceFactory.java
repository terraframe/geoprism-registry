package net.geoprism.registry.etl.export;

import net.geoprism.dhis2.dhis2adapter.HTTPConnector;
import net.geoprism.registry.etl.export.dhis2.DHIS2Service;
import net.geoprism.registry.etl.export.dhis2.DHIS2ServiceIF;

public class DataExportServiceFactory
{
  private static DataExportServiceFactory instance;
  
  private DHIS2ServiceIF dhis2;
  
  private void initialize()
  {
  }
  
  public static synchronized DataExportServiceFactory getInstance()
  {
    if (instance == null)
    {
      instance = new DataExportServiceFactory();
      instance.initialize();
    }
    
    return instance;
  }
  
  public synchronized DHIS2ServiceIF instanceGetDhis2Service(HTTPConnector connector, String version)
  {
    if (this.dhis2 == null)
    {
      this.dhis2 = new DHIS2Service(connector, version);
    }
    
    return this.dhis2;
  }
  
  public static DHIS2ServiceIF getDhis2Service(HTTPConnector connector, String version)
  {
    return getInstance().instanceGetDhis2Service(connector, version);
  }
  
  public static void setDhis2Service(DHIS2ServiceIF dhis2)
  {
    getInstance().dhis2 = dhis2;
  }
}
