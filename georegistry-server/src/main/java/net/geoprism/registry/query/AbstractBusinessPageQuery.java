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
package net.geoprism.registry.query;

import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.AttributeDoesNotExistException;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.query.ComponentQuery;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableBoolean;
import com.runwaysdk.query.SelectableChar;
import com.runwaysdk.query.SelectableMoment;
import com.runwaysdk.query.ValueQuery;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public abstract class AbstractBusinessPageQuery<T extends JsonSerializable>
{
  private JsonObject      criteria;

  private MdBusinessDAOIF mdBusiness;

  public AbstractBusinessPageQuery(MdBusinessDAOIF mdBusiness)
  {
    this(mdBusiness, new JsonObject());
  }

  public AbstractBusinessPageQuery(MdBusinessDAOIF mdBusiness, JsonObject criteria)
  {
    super();
    this.mdBusiness = mdBusiness;
    this.criteria = criteria;
  }

  public MdBusinessDAOIF getMdBusiness()
  {
    return mdBusiness;
  }

  public JsonObject getCriteria()
  {
    return criteria;
  }

  protected abstract List<T> getResults(final List<? extends Business> list);

  public Page<T> getPage()
  {
    BusinessQuery query = this.getQuery();

    int pageSize = 10;
    int pageNumber = 1;

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.get("first").getAsInt();
      pageSize = criteria.get("rows").getAsInt();
      pageNumber = ( first / pageSize ) + 1;
    }

    long count = query.getCount();

    try (OIterator<? extends Business> iterator = query.getIterator(pageSize, pageNumber))
    {
      return new Page<T>(count, pageNumber, pageSize, this.getResults(iterator.getAll()));
    }
  }

  public BusinessQuery getQuery()
  {
    BusinessQuery query = new QueryFactory().businessQuery(this.mdBusiness.definesType());

    return getQuery(query, query);
  }

  public BusinessQuery getQuery(ValueQuery vQuery)
  {
    return getQuery(vQuery, new BusinessQuery(vQuery, mdBusiness.definesType()));
  }

  protected BusinessQuery getQuery(ComponentQuery qQuery, BusinessQuery query)
  {
    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.get("sortField").getAsString();
      SortOrder order = criteria.get("sortOrder").getAsInt() == 1 ? SortOrder.ASC : SortOrder.DESC;

      qQuery.ORDER_BY(query.getS(field), order);
    }
    else if (criteria.has("multiSortMeta"))
    {
      JsonArray sorts = criteria.get("multiSortMeta").getAsJsonArray();

      for (int i = 0; i < sorts.size(); i++)
      {
        JsonObject sort = sorts.get(i).getAsJsonObject();

        String field = sort.get("field").getAsString();
        SortOrder order = sort.get("order").getAsInt() == 1 ? SortOrder.ASC : SortOrder.DESC;

        qQuery.ORDER_BY(query.getS(field), order);
      }
    }

    if (criteria.has("filters"))
    {
      JsonObject filters = criteria.get("filters").getAsJsonObject();
      Iterator<String> keys = filters.keySet().iterator();

      while (keys.hasNext())
      {
        String attributeName = keys.next();

        Selectable attribute = this.getSelectable(query, attributeName);

        if (attribute != null)
        {
          JsonObject filter = filters.get(attributeName).getAsJsonObject();
          String mode = filter.get("matchMode").getAsString();

          if (attribute instanceof SelectableMoment)
          {
            JsonObject value = filter.get("value").getAsJsonObject();

            if (value.has("startDate") && !value.get("startDate").isJsonNull())
            {
              String date = value.get("startDate").getAsString();

              if (date.length() > 0)
              {
                qQuery.WHERE( ( (SelectableMoment) attribute ).GE(GeoRegistryUtil.parseDate(date)));
              }
            }

            if (value.has("endDate") && !value.get("endDate").isJsonNull())
            {
              String date = value.get("endDate").getAsString();

              if (date.length() > 0)
              {
                qQuery.WHERE( ( (SelectableMoment) attribute ).LE(GeoRegistryUtil.parseDate(date)));
              }
            }
          }
          else if (attribute instanceof SelectableBoolean)
          {
            String value = filter.get("value").getAsString();

            filterBoolean(qQuery, attribute, Boolean.valueOf(value));
          }
          else if (mode.equals("contains"))
          {
            String value = filter.get("value").getAsString();

            SelectableChar selectable = (SelectableChar) attribute;

            qQuery.WHERE(selectable.LIKEi("%" + value + "%"));
          }
          else if (mode.equals("equals"))
          {
            String value = filter.get("value").getAsString();

            qQuery.WHERE(attribute.EQ(value));
          }
        }
      }
    }
    return query;
  }

  public Selectable getSelectable(BusinessQuery query, String attributeName)
  {
    try
    {
      return query.get(attributeName);
    }
    catch (AttributeDoesNotExistException e)
    {
      return null;
    }
  }

  protected void filterBoolean(ComponentQuery qQuery, Selectable attribute, Boolean value)
  {
    qQuery.WHERE( ( (SelectableBoolean) attribute ).EQ(value));
  }

}
