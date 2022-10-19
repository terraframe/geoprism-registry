package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.masterlist.ListAttributeGroup;
import net.geoprism.registry.masterlist.ListColumn;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ListTypeGeoObjectTypeGroup extends ListTypeGeoObjectTypeGroupBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 622327581;

  public ListTypeGeoObjectTypeGroup()
  {
    super();
  }

  public ListColumn toColumn()
  {
    String label = this.getLabel().getValue();

    Integer level = this.getLevel();

    if (level != null)
    {
      label += " - " + level;
    }

    ListAttributeGroup column = new ListAttributeGroup(label);

    this.getChildren().forEach(child -> column.add(child.toColumn()));
    this.getAttributes().forEach(child -> column.add(child.toColumn()));

    return column;
  }

  public static ListTypeGeoObjectTypeGroup create(ListTypeVersion version, ListTypeGroup parent, ServerGeoObjectType type, Integer level)
  {
    ListTypeGeoObjectTypeGroup group = new ListTypeGeoObjectTypeGroup();
    group.setVersion(version);
    group.setUniversal(type.getUniversal());
    group.setLevel(level);
    group.setParent(parent);

    LocalizedValueConverter.populate(group.getLabel(), type.getLabel());

    group.apply();

    return group;
  }

  public static ListTypeGeoObjectTypeGroup getRoot(ListTypeVersion version, ServerGeoObjectType type)
  {
    ListTypeGeoObjectTypeGroupQuery query = new ListTypeGeoObjectTypeGroupQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));
    query.AND(query.getParent().EQ((String) null));
    query.AND(query.getUniversal().EQ(type.getUniversal()));

    try (OIterator<? extends ListTypeGeoObjectTypeGroup> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
