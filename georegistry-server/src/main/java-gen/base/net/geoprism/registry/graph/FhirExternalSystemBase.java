package net.geoprism.registry.graph;

@com.runwaysdk.business.ClassSignature(hash = 816558371)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to FhirExternalSystem.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class FhirExternalSystemBase extends net.geoprism.registry.graph.ExternalSystem
{
  public final static String CLASS = "net.geoprism.registry.graph.FhirExternalSystem";
  public static java.lang.String SYSTEM = "system";
  public static java.lang.String URL = "url";
  private static final long serialVersionUID = 816558371;
  
  public FhirExternalSystemBase()
  {
    super();
  }
  
  public String getSystem()
  {
    return (String) this.getObjectValue(SYSTEM);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getSystemMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.FhirExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(SYSTEM);
  }
  
  public void setSystem(String value)
  {
    this.setValue(SYSTEM, value);
  }
  
  public String getUrl()
  {
    return (String) this.getObjectValue(URL);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getUrlMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.graph.FhirExternalSystem.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(URL);
  }
  
  public void setUrl(String value)
  {
    this.setValue(URL, value);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static FhirExternalSystem get(String oid)
  {
    return (FhirExternalSystem) com.runwaysdk.business.graph.VertexObject.get(CLASS, oid);
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}