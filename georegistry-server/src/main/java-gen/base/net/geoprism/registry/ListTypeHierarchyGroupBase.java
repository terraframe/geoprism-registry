package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 1864894876)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ListTypeHierarchyGroup.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ListTypeHierarchyGroupBase extends net.geoprism.registry.ListTypeGroup
{
  public final static String CLASS = "net.geoprism.registry.ListTypeHierarchyGroup";
  public final static java.lang.String GRAPHHIERARCHY = "graphHierarchy";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1864894876;
  
  public ListTypeHierarchyGroupBase()
  {
    super();
  }
  
  public net.geoprism.registry.graph.HierarchicalRelationshipType getGraphHierarchy()
  {
    return (net.geoprism.registry.graph.HierarchicalRelationshipType)com.runwaysdk.business.graph.VertexObject.get("net.geoprism.registry.graph.HierarchicalRelationshipType", getValue(GRAPHHIERARCHY));
  }
  
  public void validateGraphHierarchy()
  {
    this.validateAttribute(GRAPHHIERARCHY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF getGraphHierarchyMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.ListTypeHierarchyGroup.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeGraphReferenceDAOIF)mdClassIF.definesAttribute(GRAPHHIERARCHY);
  }
  
  public void setGraphHierarchy(net.geoprism.registry.graph.HierarchicalRelationshipType value)
  {
    if(value == null)
    {
      setValue(GRAPHHIERARCHY, "");
    }
    else
    {
      setValue(GRAPHHIERARCHY, value.getOid());
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static ListTypeHierarchyGroupQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    ListTypeHierarchyGroupQuery query = new ListTypeHierarchyGroupQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static ListTypeHierarchyGroup get(String oid)
  {
    return (ListTypeHierarchyGroup) com.runwaysdk.business.Business.get(oid);
  }
  
  public static ListTypeHierarchyGroup getByKey(String key)
  {
    return (ListTypeHierarchyGroup) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static ListTypeHierarchyGroup lock(java.lang.String oid)
  {
    ListTypeHierarchyGroup _instance = ListTypeHierarchyGroup.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static ListTypeHierarchyGroup unlock(java.lang.String oid)
  {
    ListTypeHierarchyGroup _instance = ListTypeHierarchyGroup.get(oid);
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
