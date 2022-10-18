package net.geoprism.registry;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerHierarchyType;

public class ListTypeHierarchyGroup extends ListTypeHierarchyGroupBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -422963119;

  public ListTypeHierarchyGroup()
  {
    super();
  }

  public ServerHierarchyType getServerHierarchyType()
  {
    return ServerHierarchyType.get(this.getHierarchy());
  }

  public static ListTypeHierarchyGroup create(ListTypeVersion version, ServerHierarchyType hierarchy)
  {
    ListTypeHierarchyGroup group = new ListTypeHierarchyGroup();
    group.setVersion(version);
    group.setHierarchy(hierarchy.getHierarchicalRelationshipType());

    LocalizedValueConverter.populate(group.getLabel(), hierarchy.getLabel());
    
    System.out.println(group.getHierarchyOid());

    group.apply();

    return group;
  }

}
