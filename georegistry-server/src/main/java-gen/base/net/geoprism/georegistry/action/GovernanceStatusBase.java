package net.geoprism.georegistry.action;

@com.runwaysdk.business.ClassSignature(hash = 614961706)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to GovernanceStatus.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class GovernanceStatusBase extends com.runwaysdk.system.EnumerationMaster
{
  public final static String CLASS = "net.geoprism.georegistry.action.GovernanceStatus";
  private static final long serialVersionUID = 614961706;
  
  public GovernanceStatusBase()
  {
    super();
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static GovernanceStatusQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    GovernanceStatusQuery query = new GovernanceStatusQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static GovernanceStatus get(String oid)
  {
    return (GovernanceStatus) com.runwaysdk.business.Business.get(oid);
  }
  
  public static GovernanceStatus getByKey(String key)
  {
    return (GovernanceStatus) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static GovernanceStatus getEnumeration(String enumName)
  {
    return (GovernanceStatus) com.runwaysdk.business.Business.getEnumeration(net.geoprism.georegistry.action.GovernanceStatus.CLASS ,enumName);
  }
  
  public static GovernanceStatus lock(java.lang.String oid)
  {
    GovernanceStatus _instance = GovernanceStatus.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static GovernanceStatus unlock(java.lang.String oid)
  {
    GovernanceStatus _instance = GovernanceStatus.get(oid);
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
