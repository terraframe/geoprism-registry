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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.ComponentQuery;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.SelectableBoolean;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.permission.GPROrganizationPermissionService;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.JsonWrapper;

public class ListTypeVersionPageQuery extends AbstractBusinessPageQuery<JsonSerializable>
{
  private ListTypeVersion                          version;

  private SimpleDateFormat                         format;

  private NumberFormat                             numberFormat;

  private Boolean                                  showInvalid;

  private Boolean                                  includeGeometries;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  public ListTypeVersionPageQuery(ListTypeVersion version, JsonObject criteria, Boolean showInvalid, Boolean includeGeometries)
  {
    super(MdBusinessDAO.get(version.getMdBusinessOid()), criteria);

    this.showInvalid = showInvalid;
    this.version = version;
    this.includeGeometries = includeGeometries;
    this.format = new SimpleDateFormat("yyyy-MM-dd");
    this.format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    this.numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());
    this.mdAttributes = this.getMdBusiness().definesAttributes();
    
    GPROrganizationPermissionService permissions = ServiceFactory.getBean(GPROrganizationPermissionService.class);
    
    // If the list isn't public and the user isn't a member of the organization the remove all non code and display label attributes
    if(version.getListVisibility().equals(ListType.PRIVATE) && !permissions.isMemberOrSRA(version.getListType().getOrganization())) {
      this.mdAttributes = this.mdAttributes.stream().filter(mdAttribute -> {
        String attributeName = mdAttribute.definesAttribute();
        
        return attributeName.equals("code") || attributeName.contains("displayLabel"); 
      }).collect(Collectors.toList());      
    }

  }

  @Override
  protected void filterBoolean(ComponentQuery qQuery, Selectable selectable, Boolean value)
  {
    String attributeName = selectable.getMdAttributeIF().definesAttribute();

    if (this.showInvalid || !attributeName.equals(DefaultAttribute.INVALID.getName()))
    {
      super.filterBoolean(qQuery, selectable, value);
    }
  }

  @Override
  protected BusinessQuery getQuery(ComponentQuery qQuery, BusinessQuery query)
  {
    super.getQuery(qQuery, query);

    if (!showInvalid)
    {
      Selectable attribute = query.get(DefaultAttribute.INVALID.getName());

      qQuery.WHERE( ( (SelectableBoolean) attribute ).EQ(Boolean.FALSE));
    }

    return query;
  }

  @Override
  protected List<JsonSerializable> getResults(List<? extends Business> results)
  {

    return results.stream().map(row -> {
      JsonObject object = new JsonObject();

      MdAttributeConcreteDAOIF mdGeometry = this.getMdBusiness().definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

      if (includeGeometries)
      {
        Geometry geom = (Geometry) row.getObjectValue(mdGeometry.definesAttribute());

        if (geom != null)
        {
          GeoJsonWriter gw = new GeoJsonWriter();
          String json = gw.write(geom);

          JsonObject geojson = JsonParser.parseString(json).getAsJsonObject();

          object.add("geometry", geojson);
        }
      }
      object.addProperty(ListTypeVersion.ORIGINAL_OID, row.getValue(ListTypeVersion.ORIGINAL_OID));
      object.addProperty(DefaultAttribute.UID.getName(), row.getValue(DefaultAttribute.UID.getName()));

      for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
      {
        if (this.version.isValid(mdAttribute))
        {
          String attributeName = mdAttribute.definesAttribute();
          Object value = row.getObjectValue(attributeName);
          String rawValue = row.getValue(attributeName);

          // For some reason row.getObjectValue returns false for NULL boolean
          // attributes. As such we need to also do a check to see if the raw
          // value is not NULL
          if (value != null && rawValue != null && rawValue.length() > 0)
          {

            if (value instanceof Double)
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
      }

      return new JsonWrapper(object);

    }).collect(Collectors.toList());
  }

}
