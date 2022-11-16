package net.geoprism.registry;

import java.util.Collections;
import java.util.Comparator;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

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

//    Integer level = this.getLevel();
//
//    if (level != null)
//    {
//      label += " - " + level;
//    }
//
    ListAttributeGroup column = new ListAttributeGroup(label);

    this.getChildren().forEach(child -> column.add(child.toColumn()));
    this.getAttributes().forEach(child -> column.add(child.toColumn()));

    Collections.sort(column.getColumns(), new Comparator<ListColumn>()
    {

      @Override
      public int compare(ListColumn o1, ListColumn o2)
      {
        if (o1.getName() != null && o1.getName().equals(DefaultAttribute.CODE.getName()))
        {
          return -1;
        }

        if (o2.getName() != null && o2.getName().equals(DefaultAttribute.CODE.getName()))
        {
          return 1;
        }

        return o1.getLabel().compareTo(o2.getLabel());
      }
    });

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
