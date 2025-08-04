package net.geoprism.registry;

public class CommitHasDependency extends CommitHasDependencyBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1219639379;
  
  public CommitHasDependency(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public CommitHasDependency(net.geoprism.registry.Commit parent, net.geoprism.registry.Commit child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
