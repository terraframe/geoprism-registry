package net.geoprism.registry;

import net.geoprism.registry.model.SnapshotContainer;
import net.geoprism.registry.view.CommitDTO;

public class Commit extends CommitBase implements SnapshotContainer<CommitHasSnapshot>
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 372845489;

  public Commit()
  {
    super();
  }

  @Override
  public boolean createTablesWithSnapshot()
  {
    return false;
  }

  public CommitDTO toDTO(Publish publish)
  {
    return new CommitDTO(this.getUid(), publish.getUid(), this.getVersionNumber(), getLastOriginGlobalIndex(), this.getCreateDate());
  }

  public CommitDTO toDTO()
  {
    return this.toDTO(this.getPublish());
  }
}
