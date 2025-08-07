package net.geoprism.registry;

public class CommitHasSnapshot extends CommitHasSnapshotBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1429865669;
  
  public CommitHasSnapshot(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public CommitHasSnapshot(net.geoprism.registry.Commit parent, net.geoprism.graph.MetadataSnapshot child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
