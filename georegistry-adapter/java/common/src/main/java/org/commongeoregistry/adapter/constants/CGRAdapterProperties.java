/**
 *
 */
package org.commongeoregistry.adapter.constants;

import java.io.IOException;
import java.util.Properties;

public class CGRAdapterProperties
{
  private static CGRAdapterProperties instance;
  
  private Properties props;
  
  public CGRAdapterProperties()
  {
    props = new Properties();
    
    try
    {
      props.load(CGRAdapterProperties.class.getClassLoader().getResourceAsStream("cgradapter.properties"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public static synchronized CGRAdapterProperties getInstance()
  {
    if (instance == null)
    {
      instance = new CGRAdapterProperties();
    }
    
    return instance;
  }
  
  public static String getApiVersion()
  {
    return CGRAdapterProperties.getInstance().props.getProperty("apiVersion");
  }
}
