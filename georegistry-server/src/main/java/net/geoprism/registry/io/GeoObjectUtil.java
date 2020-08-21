/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.metadata.MdTermDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTable;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityDisplayLabelQuery.GeoEntityDisplayLabelQueryStructIF;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdTerm;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.postgres.ValueObjectContainer;

public class GeoObjectUtil
{
  @SuppressWarnings("unchecked")
  public static String convertToTermString(AttributeTermType attributeType, Object value)
  {
    Iterator<String> codes = (Iterator<String>) value;
    StringBuilder builder = new StringBuilder();

    if (codes != null)
    {
      boolean first = true;

      while (codes.hasNext())
      {
        String code = codes.next();
        Term term = attributeType.getTermByCode(code).get();

        if (!first)
        {
          builder.append(",");
        }

        builder.append(term.getLabel());
        first = false;
      }

      return builder.toString();
    }

    return null;
  }

  public static Map<String, LocationInfo> getAncestorMap(GeoObject object, ServerHierarchyType hierarchy)
  {
    Map<String, LocationInfo> map = new HashMap<String, LocationInfo>();
    String packageName = DatabaseAllPathsStrategy.getPackageName((MdTerm) BusinessFacade.get(MdTermDAO.getMdTermDAO(GeoEntity.CLASS)));
    String typeName = DatabaseAllPathsStrategy.getTypeName(hierarchy.getEntityRelationshipDAO());

    ValueQuery vQuery = new ValueQuery(new QueryFactory());
    BusinessQuery aptQuery = new BusinessQuery(vQuery, packageName + "." + typeName);
    GeoEntityQuery parentQuery = new GeoEntityQuery(vQuery);
    GeoEntityQuery childQuery = new GeoEntityQuery(vQuery);
    UniversalQuery universalQuery = new UniversalQuery(vQuery);

    GeoEntityDisplayLabelQueryStructIF label = parentQuery.getDisplayLabel();

    vQuery.SELECT(parentQuery.getOid());
    vQuery.SELECT(parentQuery.getGeoId());
    vQuery.SELECT(universalQuery.getKeyName());
    vQuery.SELECT(label.get(MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.DISPLAY_LABEL.getName()));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      vQuery.SELECT(label.get(locale.toString(), DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
    }

    vQuery.AND(childQuery.getGeoId().EQ(object.getCode()));
    vQuery.AND(parentQuery.getUniversal().EQ(universalQuery));
    vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.PARENTTERM).EQ(parentQuery));
    vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.CHILDTERM).EQ(childQuery));

    OIterator<ValueObject> it = vQuery.getIterator();

    try
    {
      while (it.hasNext())
      {
        ValueObject vObject = it.next();
        String key = vObject.getValue(Universal.KEYNAME);

        map.put(key, new ValueObjectContainer(vObject));
      }
    }
    finally
    {
      it.close();
    }

    return map;
  }

}
