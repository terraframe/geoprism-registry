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
package net.geoprism.registry.etl.upload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.io.ConstantShapefileFunction;
import net.geoprism.registry.io.LocalizedValueFunction;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class BusinessObjectImportConfiguration extends ImportConfiguration
{
  public static final String                                   PARENT_EXCLUSION = "##PARENT##";

  public static final String                                   DATE             = "date";

  public static final String                                   TARGET           = "target";

  public static final String                                   BASE_TYPE        = "baseType";

  public static final String                                   TEXT             = "text";

  public static final String                                   NUMERIC          = "numeric";

  public static final String                                   HIERARCHY        = "hierarchy";

  public static final String                                   LOCATIONS        = "locations";

  public static final String                                   TYPE             = "type";

  public static final String                                   SHEET            = "sheet";

  public static final String                                   EXCLUSIONS       = "exclusions";

  public static final String                                   VALUE            = "value";

  public static final String                                   DATE_FORMAT      = "yyyy-MM-dd";

  public static final String                                   MATCH_STRATEGY   = "matchStrategy";

  private BusinessType                                     type;

  private Map<String, Set<String>>                             exclusions;

  private List<Location>                                       locations;

  private ServerHierarchyType                                  hierarchy;

  private Date                                                 date;

  private LinkedList<BusinessObjectRecordedErrorException> errors           = new LinkedList<BusinessObjectRecordedErrorException>();

  public BusinessObjectImportConfiguration()
  {
    this.functions = new HashMap<String, ShapefileFunction>();
    this.locations = new LinkedList<Location>();
    this.exclusions = new HashMap<String, Set<String>>();
  }

  public BusinessType getType()
  {
    return type;
  }

  public void setType(BusinessType type)
  {
    this.type = type;
  }

  public Date getDate()
  {
    return date;
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  public Map<String, Set<String>> getExclusions()
  {
    return exclusions;
  }

  public Set<String> getExclusions(String attributeName)
  {
    return exclusions.get(attributeName);
  }

  public void setExclusions(Map<String, Set<String>> exclusions)
  {
    this.exclusions = exclusions;
  }

  public void addExclusion(String attributeName, String value)
  {
    if (!this.exclusions.containsKey(attributeName))
    {
      this.exclusions.put(attributeName, new TreeSet<String>());
    }

    this.exclusions.get(attributeName).add(value);
  }

  public boolean isExclusion(String attributeName, String value)
  {
    return ( this.exclusions.get(attributeName) != null && this.exclusions.get(attributeName).contains(value) );
  }

  public void addLocation(Location location)
  {
    this.locations.add(location);
  }

  public List<Location> getLocations()
  {
    return this.locations;
  }

  public ServerHierarchyType getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(ServerHierarchyType hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  /**
   * Be careful when using this method because if an import was resumed half-way
   * through then this won't include errors which were created last time the
   * import ran. You probably want to query the database instead.
   * 
   * @return
   */
  public LinkedList<BusinessObjectRecordedErrorException> getExceptions()
  {
    return this.errors;
  }

  public void addException(BusinessObjectRecordedErrorException e)
  {
    this.errors.add(e);
  }

  @Override
  public boolean hasExceptions()
  {
    return this.errors.size() > 0;
  }

  @Request
  @Override
  public JSONObject toJSON()
  {
    JSONObject config = new JSONObject();

    super.toJSON(config);

    SimpleDateFormat format = new SimpleDateFormat(BusinessObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JSONObject type = new JSONObject(this.type.toJSON(true, true).toString());
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);
      String attributeName = attribute.getString(AttributeType.JSON_CODE);

      if (this.functions.containsKey(attributeName))
      {
        ShapefileFunction function = this.functions.get(attributeName);

        if (function instanceof LocalizedValueFunction)
        {
          String locale = attribute.getString("locale");

          ShapefileFunction localeFunction = ( (LocalizedValueFunction) function ).getFunction(locale);

          if (localeFunction != null)
          {
            attribute.put(TARGET, localeFunction.toJson());
          }
        }
        else
        {
          attribute.put(TARGET, function.toJson());
        }
      }
    }

    JSONArray locations = new JSONArray();

    for (Location location : this.locations)
    {
      locations.put(location.toJSON());
    }

    config.put(BusinessObjectImportConfiguration.TYPE, type);
    config.put(BusinessObjectImportConfiguration.LOCATIONS, locations);

    if (this.getDate() != null)
    {
      config.put(BusinessObjectImportConfiguration.DATE, format.format(this.getDate()));
    }

    if (this.hierarchy != null)
    {
      config.put(BusinessObjectImportConfiguration.HIERARCHY, this.getHierarchy().getCode());
    }

    if (this.exclusions.size() > 0)
    {
      JSONArray exclusions = new JSONArray();

      this.exclusions.forEach((key, set) -> {
        set.forEach(value -> {
          JSONObject object = new JSONObject();
          object.put(AttributeType.JSON_CODE, key);
          object.put(VALUE, value);

          exclusions.put(object);
        });
      });

      config.put(EXCLUSIONS, exclusions);
    }

    return config;
  }

  @Request
  public BusinessObjectImportConfiguration fromJSON(String json, boolean includeCoordinates)
  {
    super.fromJSON(json);

    SimpleDateFormat format = new SimpleDateFormat(BusinessObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JSONObject config = new JSONObject(json);
    JSONObject type = config.getJSONObject(TYPE);
    JSONArray locations = config.has(LOCATIONS) ? config.getJSONArray(LOCATIONS) : new JSONArray();
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);
    String code = type.getString(GeoObjectType.JSON_CODE);
    BusinessType businessType = BusinessType.getByCode(code);

    this.setType(businessType);

    try
    {
      if (config.has(BusinessObjectImportConfiguration.DATE))
      {
        this.setDate(format.parse(config.getString(BusinessObjectImportConfiguration.DATE)));
      }
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }

    if (config.has(HIERARCHY))
    {
      String hCode = config.getString(HIERARCHY);

      if (hCode.length() > 0)
      {
        ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);

        this.setHierarchy(hierarchyType);
      }
    }

    if (config.has(EXCLUSIONS))
    {
      JSONArray exclusions = config.getJSONArray(EXCLUSIONS);

      for (int i = 0; i < exclusions.length(); i++)
      {
        JSONObject exclusion = exclusions.getJSONObject(i);
        String attributeName = exclusion.getString(AttributeType.JSON_CODE);
        String value = exclusion.getString(VALUE);

        this.addExclusion(attributeName, value);
      }
    }

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);

      if (attribute.has(TARGET))
      {
        String attributeName = attribute.getString(AttributeType.JSON_CODE);

        // In the case of a spreadsheet, this ends up being the column header
        String target = attribute.getString(TARGET);

        if (attribute.has("locale"))
        {
          String locale = attribute.getString("locale");

          if (this.getFunction(attributeName) == null)
          {
            this.setFunction(attributeName, new LocalizedValueFunction());
          }

          LocalizedValueFunction function = (LocalizedValueFunction) this.getFunction(attributeName);
          function.add(locale, new BasicColumnFunction(target));
        }
        else
        {
          this.setFunction(attributeName, new BasicColumnFunction(target));
        }
      }
    }

    for (int i = 0; i < locations.length(); i++)
    {
      JSONObject location = locations.getJSONObject(i);

      if (location.has(TARGET) && location.getString(TARGET).length() > 0 && location.has(MATCH_STRATEGY) && location.getString(MATCH_STRATEGY).length() > 0)
      {
        String pCode = location.getString(AttributeType.JSON_CODE);
        ServerGeoObjectType pType = ServerGeoObjectType.get(pCode);

        String target = location.getString(TARGET);
        ParentMatchStrategy matchStrategy = ParentMatchStrategy.valueOf(location.getString(MATCH_STRATEGY));

        // This is supported for testing reasons. On a live server all data
        // coming in with use BasicColumnFunctions
        if (location.has("type") && location.getString("type").equals(ConstantShapefileFunction.class.getName()))
        {
          this.addLocation(new Location(pType, this.hierarchy, new ConstantShapefileFunction(target), matchStrategy));
        }
        else
        {
          this.addLocation(new Location(pType, this.hierarchy, new BasicColumnFunction(target), matchStrategy));
        }
      }
    }

    // If the hierarchy is inherited, we need to resolve the hierarchy
    // inheritance chain and set them properly on the Location objects
    // To do this, we must start from the bottom and resolve upwards
    // ServerHierarchyType ht = this.hierarchy;
    // for (int i = this.locations.size() - 1; i >= 0; --i)
    // {
    // Location loc = this.locations.get(i);
    //
    // ht = got.findHierarchy(ht, loc.getType());
    // loc.setHierarchy(ht);
    // }

    return this;
  }

  @Override
  public void validate()
  {
    super.validate();
  }

  public static String getBaseType(String attributeType)
  {
    if (attributeType.equals(AttributeBooleanType.TYPE))
    {
      return AttributeBooleanType.TYPE;
    }
    else if (attributeType.equals(AttributeClassificationType.TYPE) || attributeType.equals(AttributeTermType.TYPE) || attributeType.equals(AttributeCharacterType.TYPE) || attributeType.equals(AttributeLocalType.TYPE))
    {
      return BusinessObjectImportConfiguration.TEXT;
    }
    else if (attributeType.equals(AttributeFloatType.TYPE) || attributeType.equals(AttributeIntegerType.TYPE))
    {
      return BusinessObjectImportConfiguration.NUMERIC;
    }

    return AttributeDateType.TYPE;
  }

  public static String getBaseType(org.opengis.feature.type.AttributeType type)
  {
    Class<?> clazz = type.getBinding();

    if (Boolean.class.isAssignableFrom(clazz))
    {
      return AttributeBooleanType.TYPE;
    }
    else if (String.class.isAssignableFrom(clazz))
    {
      return BusinessObjectImportConfiguration.TEXT;
    }
    else if (Number.class.isAssignableFrom(clazz))
    {
      return BusinessObjectImportConfiguration.NUMERIC;
    }
    else if (Date.class.isAssignableFrom(clazz))
    {
      return AttributeDateType.TYPE;
    }

    throw new UnsupportedOperationException("Unsupported type [" + type.getBinding().getName() + "]");
  }

  @Override
  public void enforceCreatePermissions()
  {
    // TODO determine permissions
  }

  @Override
  public void enforceExecutePermissions()
  {
    // TODO determine permissions
  }

  @Override
  public void populate(ImportHistory history)
  {
    Organization org = type.getOrganization();

    history.setOrganization(org);
    history.setGeoObjectTypeCode(type.getCode());
  }
}
