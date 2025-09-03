package net.geoprism.registry;

public class SynchronizationHasProcessedCommit extends SynchronizationHasProcessedCommitBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 616314669;
  
  public SynchronizationHasProcessedCommit(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public SynchronizationHasProcessedCommit(net.geoprism.registry.SynchronizationConfig parent, net.geoprism.registry.Commit child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
