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
package net.geoprism.registry.query.graph;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeGraphRefDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLocalEmbeddedDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphObjectDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.JsonWrapper;

public class BusinessObjectPageQuery extends AbstractGraphPageQuery<HashMap<String, Object>, JsonSerializable>
{
  private SimpleDateFormat                         format;

  private NumberFormat                             numberFormat;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  public BusinessObjectPageQuery(BusinessType businessType, JsonObject criteria)
  {
    super(businessType.getMdVertexDAO().definesType(), criteria);

    this.format = new SimpleDateFormat("yyyy-MM-dd");
    this.format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    this.numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());

    this.mdAttributes = businessType.getMdVertexDAO().definesAttributes();
  }

  @SuppressWarnings("unchecked")
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
          else if (mdAttribute instanceof MdAttributeLocalEmbeddedDAOIF || mdAttribute instanceof MdAttributeClassificationDAOIF)
          {
            LocalizedValue localizedValue = LocalizedValueConverter.convert((HashMap<String, ?>) value);

            object.addProperty(mdAttribute.definesAttribute(), localizedValue.getValue());
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
    return mdAttribute.getColumnName();
  }

  public void addSelectAttributes(final MdVertexDAOIF mdVertex, StringBuilder statement)
  {
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdVertex.definesAttributes();

    List<String> columnNames = mdAttributes.stream().filter(attribute -> {
      return !attribute.definesAttribute().equals("seq");
    }).map(mdAttribute -> {
      // Hardcoded assumption that the referenced class has a displayLabel
      // This should work because the only referenced classes will be
      // Classification types
      if (mdAttribute instanceof MdAttributeGraphRefDAOIF)
      {
        return this.getColumnName(mdAttribute) + ".displayLabel AS " + mdAttribute.getColumnName();
      }

      return this.getColumnName(mdAttribute) + " AS " + mdAttribute.getColumnName();
    }).collect(Collectors.toList());

    statement.append(String.join(", ", columnNames));
  }

}
