package net.geoprism.graph;

@com.runwaysdk.business.ClassSignature(hash = -840894984)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to RDFExportJob.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class RDFExportJobBase extends com.runwaysdk.system.scheduler.ExecutableJob
{
  public final static String CLASS = "net.geoprism.graph.RDFExportJob";
  public final static java.lang.String GEOMETRYEXPORTTYPE = "geometryExportType";
  public final static java.lang.String NAMESPACE = "namespace";
  public final static java.lang.String VERSION = "version";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -840894984;
  
  public RDFExportJobBase()
  {
    super();
  }
  
  public String getGeometryExportType()
  {
    return getValue(GEOMETRYEXPORTTYPE);
  }
  
  public void validateGeometryExportType()
  {
    this.validateAttribute(GEOMETRYEXPORTTYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getGeometryExportTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.graph.RDFExportJob.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(GEOMETRYEXPORTTYPE);
  }
  
  public void setGeometryExportType(String value)
  {
    if(value == null)
    {
      setValue(GEOMETRYEXPORTTYPE, "");
    }
    else
    {
      setValue(GEOMETRYEXPORTTYPE, value);
    }
  }
  
  public String getNamespace()
  {
    return getValue(NAMESPACE);
  }
  
  public void validateNamespace()
  {
    this.validateAttribute(NAMESPACE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getNamespaceMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.graph.RDFExportJob.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(NAMESPACE);
  }
  
  public void setNamespace(String value)
  {
    if(value == null)
    {
      setValue(NAMESPACE, "");
    }
    else
    {
      setValue(NAMESPACE, value);
    }
  }
  
  public net.geoprism.graph.LabeledPropertyGraphTypeVersion getVersion()
  {
    if (getValue(VERSION).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.graph.LabeledPropertyGraphTypeVersion.get(getValue(VERSION));
    }
  }
  
  public String getVersionOid()
  {
    return getValue(VERSION);
  }
  
  public void validateVersion()
  {
    this.validateAttribute(VERSION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getVersionMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.graph.RDFExportJob.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(VERSION);
  }
  
  public void setVersion(net.geoprism.graph.LabeledPropertyGraphTypeVersion value)
  {
    if(value == null)
    {
      setValue(VERSION, "");
    }
    else
    {
      setValue(VERSION, value.getOid());
    }
  }
  
  public void setVersionId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(VERSION, "");
    }
    else
    {
      setValue(VERSION, oid);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static RDFExportJobQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    RDFExportJobQuery query = new RDFExportJobQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static RDFExportJob get(String oid)
  {
    return (RDFExportJob) com.runwaysdk.business.Business.get(oid);
  }
  
  public static RDFExportJob getByKey(String key)
  {
    return (RDFExportJob) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static RDFExportJob lock(java.lang.String oid)
  {
    RDFExportJob _instance = RDFExportJob.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static RDFExportJob unlock(java.lang.String oid)
  {
    RDFExportJob _instance = RDFExportJob.get(oid);
    _instance.unlock();
    
    return _instance;
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
