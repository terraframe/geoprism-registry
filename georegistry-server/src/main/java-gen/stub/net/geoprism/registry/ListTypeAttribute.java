package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.localization.LocalizedValueIF;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.masterlist.ListAttribute;
import net.geoprism.registry.masterlist.ListColumn;

public class ListTypeAttribute extends ListTypeAttributeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1831725995;

  public ListTypeAttribute()
  {
    super();
  }

  public ListColumn toColumn()
  {
    MdAttributeConcreteDAOIF mdAttribute = MdAttributeConcreteDAO.get(this.getListAttributeOid());

    return new ListAttribute(mdAttribute, this.getLabel().getValue());
  }

  public static void deleteAll(ListTypeGroup group)
  {
    ListTypeAttributeQuery query = new ListTypeAttributeQuery(new QueryFactory());
    query.WHERE(query.getListGroup().EQ(group));

    OIterator<? extends ListTypeAttribute> it = query.getIterator();

    try
    {
      List<? extends ListTypeAttribute> attributes = it.getAll();

      for (ListTypeAttribute attribute : attributes)
      {
        attribute.delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public static List<ListTypeAttribute> getAll(ListTypeGroup group)
  {
    ListTypeAttributeQuery query = new ListTypeAttributeQuery(new QueryFactory());
    query.WHERE(query.getListGroup().EQ(group));
    // query.ORDER_BY_ASC(query.getLabel().localize());

    try (OIterator<? extends ListTypeAttribute> it = query.getIterator())
    {
      return new LinkedList<ListTypeAttribute>(it.getAll());
    }
  }

  public static void create(ListTypeGroup parent, MdAttributeDAOIF mdAttribute, SupportedLocaleIF locale, LocalizedValueIF label)
  {
    ListTypeAttribute attribute = new ListTypeAttribute();
    attribute.setListGroup(parent);
    attribute.setListAttributeId(mdAttribute.getOid());

    if (locale != null)
    {
      attribute.setLocale(locale.getLocale().toString());
    }

    LocalizedValueConverter.populate(attribute.getLabel(), label);

    attribute.apply();
  }

}
