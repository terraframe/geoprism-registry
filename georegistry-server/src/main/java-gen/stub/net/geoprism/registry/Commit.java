package net.geoprism.registry;

import com.runwaysdk.dataaccess.database.Database;

import net.geoprism.registry.model.SnapshotContainer;

public class Commit extends CommitBase implements SnapshotContainer<CommitHasSnapshot>
{
  public static final String         DOMAIN_EVENT_ENTRY_TABLE = "domainevententry";

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

  @Override
  public void delete()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("DELETE FROM " + DOMAIN_EVENT_ENTRY_TABLE);
    statement.append(" WHERE commit_id = '" + this.getUid() + "'");
    
    System.out.println(statement);

    Database.executeStatement(statement.toString());

    super.delete();
  }

}
