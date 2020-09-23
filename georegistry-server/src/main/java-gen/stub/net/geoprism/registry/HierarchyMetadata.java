package net.geoprism.registry;

import java.util.List;

import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.model.ServerHierarchyType;

public class HierarchyMetadata extends HierarchyMetadataBase
{
  private static final long serialVersionUID = -1833634695;

  public HierarchyMetadata()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    return this.getMdTermRelationshipOid();
  }

  public static ServerHierarchyType getHierarchyType(String key)
  {
    HierarchyMetadata hierarchy = HierarchyMetadata.getByKey(key);
    MdTermRelationship mdTermRelationship = hierarchy.getMdTermRelationship();

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(mdTermRelationship);
    return hierarchyType;
  }

  public static void deleteByRelationship(MdTermRelationship mdRelationship)
  {
    HierarchyMetadataQuery query = new HierarchyMetadataQuery(new QueryFactory());
    query.WHERE(query.getMdTermRelationship().EQ(mdRelationship));

    List<? extends HierarchyMetadata> hierarchies = query.getIterator().getAll();

    for (HierarchyMetadata hierarchy : hierarchies)
    {
      hierarchy.delete();
    }
  }

}
