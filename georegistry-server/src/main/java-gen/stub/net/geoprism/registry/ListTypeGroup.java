package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.masterlist.ListAttributeGroup;
import net.geoprism.registry.masterlist.ListColumn;
import net.geoprism.registry.model.ServerHierarchyType;

public class ListTypeGroup extends ListTypeGroupBase
{
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
    // query.ORDER_BY_ASC(query.getLabel().localize());

    try (OIterator<? extends ListTypeGroup> it = query.getIterator())
    {
      return new LinkedList<ListTypeGroup>(it.getAll());
    }
  }

  public List<ListTypeAttribute> getAttributes()
  {
    ListTypeAttributeQuery query = new ListTypeAttributeQuery(new QueryFactory());
    query.WHERE(query.getListGroup().EQ(this));
    // query.ORDER_BY_ASC(query.getLabel().localize());

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
      return new LinkedList<ListTypeGroup>(it.getAll());
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
