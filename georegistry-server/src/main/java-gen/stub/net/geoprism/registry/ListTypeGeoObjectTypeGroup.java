package net.geoprism.registry;

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

}
