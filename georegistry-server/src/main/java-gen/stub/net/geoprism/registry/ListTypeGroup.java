package net.geoprism.registry;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.masterlist.ListAttributeGroup;
import net.geoprism.registry.masterlist.ListColumn;

public class ListTypeGroup extends ListTypeGroupBase
{
  private static class GroupComparator implements Comparator<ListTypeGroup>, Serializable
  {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ListTypeGroup o1, ListTypeGroup o2)
    {
      // Custom sorting logic
      if (o1 instanceof ListTypeGeoObjectTypeGroup)
      {
        if (o2 instanceof ListTypeGeoObjectTypeGroup)
        {
          return ( (ListTypeGeoObjectTypeGroup) o1 ).getLevel().compareTo( ( (ListTypeGeoObjectTypeGroup) o2 ).getLevel());
        }

        return -1;
      }

      return o1.getLabel().getValue().compareTo(o2.getLabel().getValue());
    }

  }

  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1657692026;

  public ListTypeGroup()
  {
    super();
  }

  @Override
  public void delete()
  {
    ListTypeAttribute.deleteAll(this);

    List<ListTypeGroup> children = this.getChildren();

    for (ListTypeGroup child : children)
    {
      child.delete();
    }

    List<ListTypeAttribute> attributes = this.getAttributes();

    for (ListTypeAttribute attribute : attributes)
    {
      attribute.delete();
    }

    super.delete();
  }

  public List<ListTypeGroup> getChildren()
  {
    ListTypeGroupQuery query = new ListTypeGroupQuery(new QueryFactory());
    query.WHERE(query.getParent().EQ(this));

    try (OIterator<? extends ListTypeGroup> it = query.getIterator())
    {
      List<ListTypeGroup> children = new LinkedList<ListTypeGroup>(it.getAll());

      Collections.sort(children, new GroupComparator());

      return children;
    }
  }

  public List<ListTypeAttribute> getAttributes()
  {
    ListTypeAttributeQuery query = new ListTypeAttributeQuery(new QueryFactory());
    query.WHERE(query.getListGroup().EQ(this));

    try (OIterator<? extends ListTypeAttribute> it = query.getIterator())
    {
      return new LinkedList<ListTypeAttribute>(it.getAll());
    }
  }

  public ListColumn toColumn()
  {
    ListAttributeGroup column = new ListAttributeGroup(this.getLabel().getValue());

    this.getChildren().forEach(child -> column.add(child.toColumn()));
    this.getAttributes().forEach(child -> column.add(child.toColumn()));

    return column;
  }

  public static void deleteAll(ListTypeVersion version)
  {
    getRoots(version).forEach(group -> group.delete());
  }

  public static List<ListTypeGroup> getRoots(ListTypeVersion version)
  {
    ListTypeGroupQuery query = new ListTypeGroupQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));
    query.AND(query.getParent().EQ((String) null));
    // query.ORDER_BY_ASC(query.getLabel().localize());

    try (OIterator<? extends ListTypeGroup> it = query.getIterator())
    {
      LinkedList<ListTypeGroup> roots = new LinkedList<ListTypeGroup>(it.getAll());

      Collections.sort(roots, new GroupComparator());

      return roots;
    }
  }

  public static ListTypeGroup create(ListTypeVersion version, ListTypeGroup parent, LocalizedValueIF label)
  {
    ListTypeGroup group = new ListTypeGroup();
    group.setVersion(version);
    group.setParent(parent);

    if (label != null)
    {
      LocalizedValueConverter.populate(group.getLabel(), label);
    }

    group.apply();

    return group;
  }

}
