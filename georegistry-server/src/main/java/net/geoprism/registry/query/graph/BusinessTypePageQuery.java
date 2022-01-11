package net.geoprism.registry.query.graph;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.JsonWrapper;

public class BusinessTypePageQuery extends AbstractGraphPageQuery<HashMap<String, Object>, JsonSerializable>
{
  private SimpleDateFormat                         format;

  private NumberFormat                             numberFormat;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  public BusinessTypePageQuery(BusinessType businessType, JsonObject criteria)
  {
    super(businessType.getMdVertexDAO().definesType(), criteria);

    this.format = new SimpleDateFormat("yyyy-MM-dd");
    this.format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    this.numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());

    this.mdAttributes = businessType.getMdVertexDAO().definesAttributes();
  }

  protected List<JsonSerializable> getResults(final GraphQuery<HashMap<String, Object>> query)
  {
    List<HashMap<String, Object>> results = query.getResults();

    return results.stream().map(row -> {
      JsonObject object = new JsonObject();

      object.addProperty(DefaultAttribute.CODE.getName(), (String) row.get(DefaultAttribute.CODE.getName()));

      for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
      {
        String attributeName = mdAttribute.definesAttribute();

        Object value = row.get(attributeName);

        if (value != null)
        {
          if (mdAttribute instanceof MdAttributeTermDAOIF)
          {
            Classifier classifier = Classifier.get((String) value);

            object.addProperty(mdAttribute.definesAttribute(), classifier.getDisplayLabel().getValue());
          }
          else if (value instanceof Double)
          {
            object.addProperty(mdAttribute.definesAttribute(), numberFormat.format((Double) value));
          }
          else if (value instanceof Number)
          {
            object.addProperty(mdAttribute.definesAttribute(), (Number) value);
          }
          else if (value instanceof Boolean)
          {
            object.addProperty(mdAttribute.definesAttribute(), (Boolean) value);
          }
          else if (value instanceof String)
          {
            object.addProperty(mdAttribute.definesAttribute(), (String) value);
          }
          else if (value instanceof Character)
          {
            object.addProperty(mdAttribute.definesAttribute(), (Character) value);
          }
          else if (value instanceof Date)
          {
            object.addProperty(mdAttribute.definesAttribute(), format.format((Date) value));
          }
        }
      }

      return new JsonWrapper(object);

    }).collect(Collectors.toList());
  }

  @Override
  protected String getColumnName(MdAttributeDAOIF mdAttribute)
  {
    if (mdAttribute.definesAttribute().equals(BusinessType.GEO_OBJECT))
    {
      return BusinessType.GEO_OBJECT + "." + DefaultAttribute.CODE.getName();
    }

    return mdAttribute.getColumnName();
  }

  public void addSelectAttributes(final MdVertexDAOIF mdVertex, StringBuilder statement)
  {
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdVertex.definesAttributes();

    List<String> columnNames = mdAttributes.stream().filter(attribute -> {
      return !attribute.definesAttribute().equals("seq");
    }).map(mdAttribute -> {
      return this.getColumnName(mdAttribute) + " AS " + mdAttribute.getColumnName();
    }).collect(Collectors.toList());

    statement.append(String.join(", ", columnNames));
  }

}
