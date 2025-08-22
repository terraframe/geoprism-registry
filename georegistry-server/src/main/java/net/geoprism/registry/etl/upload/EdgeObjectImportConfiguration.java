/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
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
import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.GraphType;

public class EdgeObjectImportConfiguration extends ImportConfiguration
{
  public static final String                               PARENT_EXCLUSION = "##PARENT##";
  
  public static final String                          DATA_SOURCE            = "dataSource";

  public static final String                               START_DATE             = "startDate";
  
  public static final String                               END_DATE             = "endDate";

  public static final String                               TARGET           = "target";

  public static final String                               BASE_TYPE        = "baseType";

  public static final String                               TEXT             = "text";

  public static final String                               NUMERIC          = "numeric";

  public static final String                               GRAPH_TYPE_CODE  = "graphTypeCode";
  
  public static final String                               GRAPH_TYPE_CLASS = "graphTypeClass";

  public static final String                               DIRECTION        = "direction";

  public static final String                               LOCATIONS        = "locations";
  
  public static final String                               TYPE                   = "type";

  public static final String                               SHEET            = "sheet";

  public static final String                               EXCLUSIONS       = "exclusions";

  public static final String                               VALUE            = "value";

  public static final String                               DATE_FORMAT      = "yyyy-MM-dd";

  public static final String                               MATCH_STRATEGY   = "matchStrategy";
  
  public static final String                               VALIDATE         = "validate";
  
  
  public static final String                               EDGE_SOURCE      = "edgeSource";
  public static final String                               EDGE_SOURCE_STRATEGY = "edgeSourceStrategy";
  public static final String                               EDGE_SOURCE_TYPE = "edgeSourceType";
  public static final String                               EDGE_SOURCE_TYPE_STRATEGY = "edgeSourceTypeStrategy";
  public static final String                               EDGE_TARGET      = "edgeTarget";
  public static final String                               EDGE_TARGET_STRATEGY = "edgeTargetStrategy";
  public static final String                               EDGE_TARGET_TYPE = "edgeTargetType";
  public static final String                               EDGE_TARGET_TYPE_STRATEGY = "edgeTargetTypeStrategy";
  
  private String edgeSource;
  private String edgeSourceStrategy;
  private String edgeSourceType;
  private String edgeSourceTypeStrategy;
  
  private String edgeTarget;
  private String edgeTargetStrategy;
  private String edgeTargetType;
  private String edgeTargetTypeStrategy;

  private Map<String, Set<String>>                         exclusions;

  private List<Location>                                   locations;
  
  private GraphType                                        graphType;

  private Date                                             startDate;
  
  private Date                                             endDate;
  
  private boolean                                          validate;
  
  private LinkedList<EdgeObjectRecordedErrorException>     errors           = new LinkedList<EdgeObjectRecordedErrorException>();
  
  public EdgeObjectImportConfiguration()
  {
    this.functions = new HashMap<String, ShapefileFunction>();
    this.locations = new LinkedList<Location>();
    this.exclusions = new HashMap<String, Set<String>>();
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

  public String getEdgeSource()
  {
    return edgeSource;
  }

  public void setEdgeSource(String edgeSource)
  {
    this.edgeSource = edgeSource;
  }

  public String getEdgeSourceStrategy()
  {
    return edgeSourceStrategy;
  }

  public void setEdgeSourceStrategy(String edgeSourceStrategy)
  {
    this.edgeSourceStrategy = edgeSourceStrategy;
  }

  public String getEdgeSourceType()
  {
    return edgeSourceType;
  }

  public void setEdgeSourceType(String edgeSourceType)
  {
    this.edgeSourceType = edgeSourceType;
  }

  public String getEdgeSourceTypeStrategy()
  {
    return edgeSourceTypeStrategy;
  }

  public void setEdgeSourceTypeStrategy(String edgeSourceTypeStrategy)
  {
    this.edgeSourceTypeStrategy = edgeSourceTypeStrategy;
  }

  public String getEdgeTarget()
  {
    return edgeTarget;
  }

  public void setEdgeTarget(String edgeTarget)
  {
    this.edgeTarget = edgeTarget;
  }

  public String getEdgeTargetStrategy()
  {
    return edgeTargetStrategy;
  }

  public void setEdgeTargetStrategy(String edgeTargetStrategy)
  {
    this.edgeTargetStrategy = edgeTargetStrategy;
  }

  public String getEdgeTargetType()
  {
    return edgeTargetType;
  }

  public void setEdgeTargetType(String edgeTargetType)
  {
    this.edgeTargetType = edgeTargetType;
  }

  public String getEdgeTargetTypeStrategy()
  {
    return edgeTargetTypeStrategy;
  }

  public void setEdgeTargetTypeStrategy(String edgeTargetTypeStrategy)
  {
    this.edgeTargetTypeStrategy = edgeTargetTypeStrategy;
  }

  public boolean isValidate()
  {
    return validate;
  }

  public void setValidate(boolean validate)
  {
    this.validate = validate;
  }

  public GraphType getGraphType()
  {
    return graphType;
  }

  public void setGraphType(GraphType graphType)
  {
    this.graphType = graphType;
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

  /**
   * Be careful when using this method because if an import was resumed half-way
   * through then this won't include errors which were created last time the
   * import ran. You probably want to query the database instead.
   * 
   * @return
   */
  public LinkedList<EdgeObjectRecordedErrorException> getExceptions()
  {
    return this.errors;
  }

  public void addException(EdgeObjectRecordedErrorException e)
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

    SimpleDateFormat format = new SimpleDateFormat(EdgeObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    // TODO : We might want to serialize some things as attributes IDK. Otherwise get rid of this code.
//    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);
//
//    for (int i = 0; i < attributes.length(); i++)
//    {
//      JSONObject attribute = attributes.getJSONObject(i);
//      String attributeName = attribute.getString(AttributeType.JSON_CODE);
//
//      if (this.functions.containsKey(attributeName))
//      {
//        ShapefileFunction function = this.functions.get(attributeName);
//
//        if (function instanceof LocalizedValueFunction)
//        {
//          String locale = attribute.getString("locale");
//
//          ShapefileFunction localeFunction = ( (LocalizedValueFunction) function ).getFunction(locale);
//
//          if (localeFunction != null)
//          {
//            attribute.put(TARGET, localeFunction.toJson());
//          }
//        }
//        else
//        {
//          attribute.put(TARGET, function.toJson());
//        }
//      }
//    }

    JSONArray locations = new JSONArray();

    for (Location location : this.locations)
    {
      locations.put(location.toJSON());
    }

    config.put(EdgeObjectImportConfiguration.LOCATIONS, locations);

    if (this.getGraphType() != null)
    {
      config.put(EdgeObjectImportConfiguration.GRAPH_TYPE_CODE, this.getGraphType().getCode());
      config.put(EdgeObjectImportConfiguration.GRAPH_TYPE_CLASS, GraphType.getTypeCode(this.getGraphType()));
    }

    if (this.getStartDate() != null)
    {
      config.put(EdgeObjectImportConfiguration.START_DATE, format.format(this.getStartDate()));
    }
    if (this.getEndDate() != null)
    {
      config.put(EdgeObjectImportConfiguration.END_DATE, format.format(this.getEndDate()));
    }

    config.put(EdgeObjectImportConfiguration.VALIDATE, this.isValidate());

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
    
    config.put(EDGE_SOURCE, edgeSource);
    config.put(EDGE_SOURCE_STRATEGY, edgeSourceStrategy);
    config.put(EDGE_SOURCE_TYPE, edgeSourceType);
    config.put(EDGE_SOURCE_TYPE_STRATEGY, edgeSourceTypeStrategy);

    config.put(EDGE_TARGET, edgeTarget);
    config.put(EDGE_TARGET_STRATEGY, edgeTargetStrategy);
    config.put(EDGE_TARGET_TYPE, edgeTargetType);
    config.put(EDGE_TARGET_TYPE_STRATEGY, edgeTargetTypeStrategy);

    return config;
  }

  @Request
  public EdgeObjectImportConfiguration fromJSON(String json, boolean includeCoordinates)
  {
    super.fromJSON(json);

    SimpleDateFormat format = new SimpleDateFormat(EdgeObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JSONObject config = new JSONObject(json);
//    JSONArray locations = config.has(LOCATIONS) ? config.getJSONArray(LOCATIONS) : new JSONArray();
    
    edgeSource = config.optString(EDGE_SOURCE);
    edgeSourceStrategy = config.optString(EDGE_SOURCE_STRATEGY);
    edgeSourceType = config.optString(EDGE_SOURCE_TYPE);
    edgeSourceTypeStrategy = config.optString(EDGE_SOURCE_TYPE_STRATEGY);

    edgeTarget = config.optString(EDGE_TARGET);
    edgeTargetStrategy = config.optString(EDGE_TARGET_STRATEGY);
    edgeTargetType = config.optString(EDGE_TARGET_TYPE);
    edgeTargetTypeStrategy = config.optString(EDGE_TARGET_TYPE_STRATEGY);
    
    this.setValidate(config.has(VALIDATE) ? config.getBoolean(VALIDATE) : false);

    this.setGraphType(GraphType.getByCode(config.getString(EdgeObjectImportConfiguration.GRAPH_TYPE_CLASS), config.getString(EdgeObjectImportConfiguration.GRAPH_TYPE_CODE)));
    
    try
    {
      if (config.has(EdgeObjectImportConfiguration.START_DATE))
      {
        this.setStartDate(format.parse(config.getString(EdgeObjectImportConfiguration.START_DATE)));
      }
      if (config.has(EdgeObjectImportConfiguration.END_DATE))
      {
        this.setEndDate(format.parse(config.getString(EdgeObjectImportConfiguration.END_DATE)));
      }
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }

    // TODO : We might want to serialize some things as attributes. Otherwise get rid of this code.
//    for (int i = 0; i < attributes.length(); i++)
//    {
//      JSONObject attribute = attributes.getJSONObject(i);
//
//      if (attribute.has(TARGET))
//      {
//        String attributeName = attribute.getString(AttributeType.JSON_CODE);
//
//        // In the case of a spreadsheet, this ends up being the column header
//        String target = attribute.getString(TARGET);
//
//        if (attribute.has("locale"))
//        {
//          String locale = attribute.getString("locale");
//
//          if (this.getFunction(attributeName) == null)
//          {
//            this.setFunction(attributeName, new LocalizedValueFunction());
//          }
//
//          LocalizedValueFunction function = (LocalizedValueFunction) this.getFunction(attributeName);
//          function.add(locale, new BasicColumnFunction(target));
//        }
//        else
//        {
//          this.setFunction(attributeName, new BasicColumnFunction(target));
//        }
//      }
//    }

    // TODO : We might want to use locations. Otherwise get rid of them.
//    for (int i = 0; i < locations.length(); i++)
//    {
//      JSONObject location = locations.getJSONObject(i);
//
//      if (location.has(TARGET) && location.getString(TARGET).length() > 0 && location.has(MATCH_STRATEGY) && location.getString(MATCH_STRATEGY).length() > 0)
//      {
//        String pCode = location.getString(AttributeType.JSON_CODE);
//        ServerGeoObjectType pType = ServerGeoObjectType.get(pCode);
//
//        String target = location.getString(TARGET);
//        ParentMatchStrategy matchStrategy = ParentMatchStrategy.valueOf(location.getString(MATCH_STRATEGY));
//
//        // This is supported for testing reasons. On a live server all data
//        // coming in with use BasicColumnFunctions
//        if (location.has("type") && location.getString("type").equals(ConstantShapefileFunction.class.getName()))
//        {
//          this.addLocation(new Location(pType, this.hierarchy, new ConstantShapefileFunction(target), matchStrategy));
//        }
//        else
//        {
//          this.addLocation(new Location(pType, this.hierarchy, new BasicColumnFunction(target), matchStrategy));
//        }
//      }
//    }

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
      return EdgeObjectImportConfiguration.TEXT;
    }
    else if (attributeType.equals(AttributeFloatType.TYPE) || attributeType.equals(AttributeIntegerType.TYPE))
    {
      return EdgeObjectImportConfiguration.NUMERIC;
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
      return EdgeObjectImportConfiguration.TEXT;
    }
    else if (Number.class.isAssignableFrom(clazz))
    {
      return EdgeObjectImportConfiguration.NUMERIC;
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
//    Organization org = graphType.getOrganization().getOrganization();
//
//    history.setOrganization(org);
//    history.setGeoObjectTypeCode(type.getCode());
  }
}
