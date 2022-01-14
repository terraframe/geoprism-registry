package net.geoprism.registry.query.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.query.OrderBy.SortOrder;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public abstract class AbstractGraphPageQuery<K, T extends JsonSerializable>
{
  private String     type;

  private JsonObject criteria;

  public AbstractGraphPageQuery(String type)
  {
    this(type, new JsonObject());
  }

  public AbstractGraphPageQuery(String type, JsonObject criteria)
  {
    super();
    this.type = type;
    this.criteria = criteria;
  }

  protected abstract List<T> getResults(final GraphQuery<K> query);

  protected abstract String getColumnName(MdAttributeDAOIF mdAttribute);

  protected abstract void addSelectAttributes(final MdVertexDAOIF mdVertex, StringBuilder statement);

  public Long getCount()
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(this.type);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName() + "");

    Map<String, Object> parameters = new HashMap<String, Object>();

    if (criteria.has("filters"))
    {
      JsonObject filters = criteria.get("filters").getAsJsonObject();

      this.addCriteria(mdVertex, filters, statement, parameters);
    }

    final GraphQuery<Long> query = new GraphQuery<Long>(statement.toString(), parameters);

    return query.getSingleResult();
  }

  private void addCriteria(final MdVertexDAOIF mdVertex, JsonObject filters, StringBuilder statement, Map<String, Object> parameters)
  {
    Iterator<String> keys = filters.keySet().iterator();

    int i = 0;

    while (keys.hasNext())
    {
      String attributeName = keys.next();

      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

      if (mdAttribute != null)
      {
        String columnName = this.getColumnName(mdAttribute);

        JsonObject filter = filters.get(attributeName).getAsJsonObject();

        String mode = filter.get("matchMode").getAsString();

        if (mode.equals("between"))
        {
          JsonObject value = filter.get("value").getAsJsonObject();

          if (value.has("startDate") && !value.get("startDate").isJsonNull())
          {
            String date = value.get("startDate").getAsString();

            if (date.length() > 0)
            {
              parameters.put(attributeName + "StartDate", GeoRegistryUtil.parseDate(date));

              statement.append( ( ( i == 0 ) ? " WHERE " : " AND " ) + columnName + " >= :" + attributeName + "StartDate");

              i++;
            }
          }

          if (value.has("endDate") && !value.get("endDate").isJsonNull())
          {
            String date = value.get("endDate").getAsString();

            if (date.length() > 0)
            {
              parameters.put(attributeName + "EndDate", GeoRegistryUtil.parseDate(date));

              statement.append( ( ( i == 0 ) ? " WHERE " : " AND " ) + columnName + " <= :" + attributeName + "EndDate");

              i++;
            }
          }

        }
        else if (mode.equals("contains"))
        {
          parameters.put(attributeName, "%" + filter.get("value").getAsString().toUpperCase() + "%");

          statement.append( ( ( i == 0 ) ? " WHERE " : " AND " ) + columnName + ".toUpperCase() LIKE :" + attributeName);

          i++;
        }
        else if (mode.equals("equals"))
        {
          parameters.put(attributeName, filter.get("value").getAsString());

          statement.append( ( ( i == 0 ) ? " WHERE " : " AND " ) + columnName + " = :" + attributeName);

          i++;
        }

      }
    }
  }

  public Page<T> getPage()
  {
    int pageSize = 10;
    int pageNumber = 1;

    Long count = this.getCount();

    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(this.type);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT ");
    this.addSelectAttributes(mdVertex, statement);
    statement.append(" FROM " + mdVertex.getDBClassName() + "");

    Map<String, Object> parameters = new HashMap<String, Object>();

    if (criteria.has("filters"))
    {
      JsonObject filters = criteria.get("filters").getAsJsonObject();

      this.addCriteria(mdVertex, filters, statement, parameters);
    }

    if (criteria.has("sortField") && criteria.has("sortOrder"))
    {
      String field = criteria.get("sortField").getAsString();
      SortOrder order = criteria.get("sortOrder").getAsInt() == 1 ? SortOrder.ASC : SortOrder.DESC;
      MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(field);

      statement.append(" ORDER BY " + this.getColumnName(mdAttribute) + " " + order.name());
    }
    else if (criteria.has("multiSortMeta"))
    {
      JsonArray sorts = criteria.get("multiSortMeta").getAsJsonArray();

      for (int i = 0; i < sorts.size(); i++)
      {
        JsonObject sort = sorts.get(i).getAsJsonObject();

        String field = sort.get("field").getAsString();
        SortOrder order = sort.get("order").getAsInt() == 1 ? SortOrder.ASC : SortOrder.DESC;
        MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(field);

        if (i == 0)
        {
          statement.append(" ORDER BY " + this.getColumnName(mdAttribute) + " " + order.name());
        }
        else
        {
          statement.append(", " + this.getColumnName(mdAttribute) + " " + order.name());
        }
      }
    }

    if (criteria.has("first") && criteria.has("rows"))
    {
      int first = criteria.get("first").getAsInt();
      int rows = criteria.get("rows").getAsInt();

      statement.append(" SKIP " + first + " LIMIT " + rows);

      pageNumber = ( first / rows ) + 1;
    }

    final GraphQuery<K> query = new GraphQuery<K>(statement.toString(), parameters);

    return new Page<T>(count, pageNumber, pageSize, this.getResults(query));
  }
}
