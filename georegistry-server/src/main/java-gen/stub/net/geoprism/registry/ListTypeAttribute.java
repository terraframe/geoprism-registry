/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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

    return new ListAttribute(mdAttribute, this.getLabel().getValue(), this.getRowspan());
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

  public static void create(ListTypeGroup parent, MdAttributeDAOIF mdAttribute, SupportedLocaleIF locale, LocalizedValueIF label, Integer rowspan)
  {
    ListTypeAttribute attribute = new ListTypeAttribute();
    attribute.setListGroup(parent);
    attribute.setListAttributeId(mdAttribute.getOid());
    attribute.setRowspan(rowspan);

    if (locale != null)
    {
      attribute.setLocale(locale.getLocale().toString());
    }

    LocalizedValueConverter.populate(attribute.getLabel(), label);

    attribute.apply();
  }

  public static void remove(MdAttributeConcreteDAOIF mdAttribute)
  {
    ListTypeAttributeQuery query = new ListTypeAttributeQuery(new QueryFactory());
    query.WHERE(query.getListAttribute().EQ(mdAttribute.getOid()));

    try (OIterator<? extends ListTypeAttribute> it = query.getIterator())
    {
      it.forEach(t -> {
        t.delete();

        ListTypeGroup parent = t.getListGroup();

        if (parent.getChildren().size() == 0)
        {
          parent.delete();
        }
      });
    }
  }

}
