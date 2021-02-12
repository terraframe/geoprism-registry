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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
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
import com.runwaysdk.session.Session;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.conversion.SupportedLocaleCache;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;

public class GeoObjectImportConfiguration extends ImportConfiguration
{
  public static final String       PARENT_EXCLUSION       = "##PARENT##";

  public static final String       START_DATE             = "startDate";

  public static final String       END_DATE               = "endDate";

  public static final String       TARGET                 = "target";

  public static final String       BASE_TYPE              = "baseType";

  public static final String       TEXT                   = "text";

  public static final String       LATITUDE               = "latitude";

  public static final String       LONGITUDE              = "longitude";

  public static final String       NUMERIC                = "numeric";

  public static final String       HIERARCHIES            = "hierarchies";

  public static final String       HIERARCHY              = "hierarchy";

  public static final String       LOCATIONS              = "locations";

  public static final String       TYPE                   = "type";

  public static final String       HAS_POSTAL_CODE        = "hasPostalCode";

  public static final String       POSTAL_CODE            = "postalCode";

  public static final String       SHEET                  = "sheet";

  public static final String       EXCLUSIONS             = "exclusions";

  public static final String       VALUE                  = "value";

  public static final String       LONGITUDE_KEY          = "georegistry.longitude.label";

  public static final String       LATITUDE_KEY           = "georegistry.latitude.label";

  public static final String       DATE_FORMAT            = "yyyy-MM-dd";

  public static final String       MATCH_STRATEGY         = "matchStrategy";

  public static final String       REVEAL_GEOMETRY_COLUMN = "revealGeometryColumn";

  private String                   revealGeometryColumn;

  private ServerGeoObjectType      type;

  private GeoObject                root;

  private Map<String, Set<String>> exclusions;

  private boolean                  includeCoordinates;

  private List<Location>           locations;

  private ServerHierarchyType      hierarchy;

  private Boolean                  postalCode;

  private Date                     startDate;

  private Date                     endDate;

  public GeoObjectImportConfiguration()
  {
    this.includeCoordinates = false;
    this.functions = new HashMap<String, ShapefileFunction>();
    this.locations = new LinkedList<Location>();
    this.exclusions = new HashMap<String, Set<String>>();
    this.postalCode = false;
  }

  public boolean isIncludeCoordinates()
  {
    return includeCoordinates;
  }

  public void setIncludeCoordinates(boolean includeCoordinates)
  {
    this.includeCoordinates = includeCoordinates;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public Date getStartDate()
  {
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
    this.startDate = startDate;
  }

  public Date getEndDate()
  {
    return endDate;
  }

  public void setEndDate(Date endDate)
  {
    this.endDate = endDate;
  }

  public GeoObject getRoot()
  {
    return root;
  }

  public void setRoot(GeoObject root)
  {
    this.root = root;
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

  public void addParent(Location location)
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

  public Boolean isPostalCode()
  {
    return postalCode;
  }

  public void setPostalCode(Boolean postalCode)
  {
    this.postalCode = postalCode;
  }

  public String getRevealGeometryColumn()
  {
    return revealGeometryColumn;
  }

  public void setRevealGeometryColumn(String revealGeometryColumn)
  {
    this.revealGeometryColumn = revealGeometryColumn;
  }

  @Request
  @Override
  public JSONObject toJSON()
  {
    JSONObject config = new JSONObject();

    super.toJSON(config);

    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    JSONObject type = new JSONObject(this.type.toJSON(new ImportAttributeSerializer(Session.getCurrentLocale(), this.includeCoordinates, false, true, SupportedLocaleCache.getLocales())).toString());
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

    config.put(GeoObjectImportConfiguration.TYPE, type);
    config.put(GeoObjectImportConfiguration.LOCATIONS, locations);
    config.put(GeoObjectImportConfiguration.POSTAL_CODE, this.isPostalCode());

    if (this.getStartDate() != null)
    {
      config.put(GeoObjectImportConfiguration.START_DATE, format.format(this.getStartDate()));
    }

    if (this.getEndDate() != null)
    {
      config.put(GeoObjectImportConfiguration.END_DATE, format.format(this.getEndDate()));
    }

    if (this.hierarchy != null)
    {
      config.put(GeoObjectImportConfiguration.HIERARCHY, this.getHierarchy().getCode());
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

    if (this.getRevealGeometryColumn() != null)
    {
      config.put(REVEAL_GEOMETRY_COLUMN, revealGeometryColumn);
    }

    return config;
  }

  @Request
  public GeoObjectImportConfiguration fromJSON(String json, boolean includeCoordinates)
  {
    super.fromJSON(json);

    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    JSONObject config = new JSONObject(json);
    JSONObject type = config.getJSONObject(TYPE);
    JSONArray locations = config.has(LOCATIONS) ? config.getJSONArray(LOCATIONS) : new JSONArray();
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);
    String code = type.getString(GeoObjectType.JSON_CODE);
    ServerGeoObjectType got = ServerGeoObjectType.get(code);

    this.setType(got);
    this.setIncludeCoordinates(includeCoordinates);
    this.setPostalCode(config.has(POSTAL_CODE) && config.getBoolean(POSTAL_CODE));

    if (config.has(REVEAL_GEOMETRY_COLUMN))
    {
      this.setRevealGeometryColumn(config.getString(REVEAL_GEOMETRY_COLUMN));
    }

    try
    {
      if (config.has(GeoObjectImportConfiguration.START_DATE))
      {
        this.setStartDate(format.parse(config.getString(GeoObjectImportConfiguration.START_DATE)));
      }

      if (config.has(GeoObjectImportConfiguration.END_DATE))
      {
        this.setEndDate(format.parse(config.getString(GeoObjectImportConfiguration.END_DATE)));
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
        List<GeoObjectType> ancestors = got.getTypeAncestors(hierarchyType, true);

        this.setHierarchy(hierarchyType);

        if (ancestors.size() > 0)
        {
          this.setRoot(null);
        }
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
        ServerHierarchyType pHierarchy = got.findHierarchy(this.hierarchy, pType);

        String target = location.getString(TARGET);
        ParentMatchStrategy matchStrategy = ParentMatchStrategy.valueOf(location.getString(MATCH_STRATEGY));

        // This is supported for testing reasons. On a live server all data
        // coming in with use BasicColumnFunctions
        if (location.has("type") && location.getString("type").equals(ConstantShapefileFunction.class.getName()))
        {
          this.addParent(new Location(pType, pHierarchy, new ConstantShapefileFunction(target), matchStrategy));
        }
        else
        {
          this.addParent(new Location(pType, pHierarchy, new BasicColumnFunction(target), matchStrategy));
        }
      }
    }
    
    // If the hierarchy is inherited, we need to resolve the hierarchy inheritance chain and set them properly on the Location objects
    // To do this, we must start from the bottom and resolve upwards
    ServerHierarchyType ht = this.hierarchy;
    for (int i = this.locations.size()-1; i >= 0; --i)
    {
      Location loc = this.locations.get(i);
      
      ht = got.findHierarchy(ht, loc.getType());
      loc.setHierarchy(ht);
    }

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
    else if (attributeType.equals(AttributeTermType.TYPE) || attributeType.equals(AttributeCharacterType.TYPE) || attributeType.equals(AttributeLocalType.TYPE))
    {
      return GeoObjectImportConfiguration.TEXT;
    }
    else if (attributeType.equals(AttributeFloatType.TYPE) || attributeType.equals(AttributeIntegerType.TYPE))
    {
      return GeoObjectImportConfiguration.NUMERIC;
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
      return GeoObjectImportConfiguration.TEXT;
    }
    else if (Number.class.isAssignableFrom(clazz))
    {
      return GeoObjectImportConfiguration.NUMERIC;
    }
    else if (Date.class.isAssignableFrom(clazz))
    {
      return AttributeDateType.TYPE;
    }

    throw new UnsupportedOperationException("Unsupported type [" + type.getBinding().getName() + "]");
  }

  public static AttributeFloatType latitude()
  {
    LocalizedValue label = new LocalizedValue(LocalizationFacade.getFromBundles(LATITUDE_KEY));
    LocalizedValue description = new LocalizedValue("");

    return new AttributeFloatType(GeoObjectImportConfiguration.LATITUDE, label, description, false, false, false);
  }

  public static AttributeFloatType longitude()
  {
    LocalizedValue label = new LocalizedValue(LocalizationFacade.getFromBundles(LONGITUDE_KEY));
    LocalizedValue description = new LocalizedValue("");

    return new AttributeFloatType(GeoObjectImportConfiguration.LONGITUDE, label, description, false, false, false);
  }
}
