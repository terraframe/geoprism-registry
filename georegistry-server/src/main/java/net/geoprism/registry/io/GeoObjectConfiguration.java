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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.GeoObjectLocationProblem;

public class GeoObjectConfiguration
{
  public static final String             PARENT_EXCLUSION  = "##PARENT##";

  public static final String             START_DATE        = "startDate";

  public static final String             END_DATE          = "endDate";

  public static final String             TARGET            = "target";

  public static final String             BASE_TYPE         = "baseType";

  public static final String             TEXT              = "text";

  public static final String             LATITUDE          = "latitude";

  public static final String             LONGITUDE         = "longitude";

  public static final String             NUMERIC           = "numeric";

  public static final String             FILENAME          = "filename";

  public static final String             HIERARCHIES       = "hierarchies";

  public static final String             HIERARCHY         = "hierarchy";

  public static final String             DIRECTORY         = "directory";

  public static final String             LOCATIONS         = "locations";

  public static final String             TYPE              = "type";

  public static final String             HAS_POSTAL_CODE   = "hasPostalCode";

  public static final String             POSTAL_CODE       = "postalCode";

  public static final String             SHEET             = "sheet";

  public static final String             TERM_PROBLEMS     = "termProblems";

  public static final String             EXCLUSIONS        = "exclusions";

  public static final String             VALUE             = "value";

  public static final String             LOCATION_PROBLEMS = "locationProblems";

  public static final String             LONGITUDE_KEY     = "georegistry.longitude.label";

  public static final String             LATITUDE_KEY      = "georegistry.latitude.label";

  public static final String             DATE_FORMAT       = "yyyy-MM-dd";

  private Map<String, ShapefileFunction> functions;

  private ServerGeoObjectType            type;

  private GeoObject                      root;

  private String                         filename;

  private String                         directory;

  private Map<String, Set<String>>       exclusions;

  private Set<TermProblem>               termProblems;

  private Set<GeoObjectLocationProblem>  locationProblems;

  private boolean                        includeCoordinates;

  private List<Location>                 locations;

  private ServerHierarchyType            hierarchy;

  private Boolean                        postalCode;

  private Date                           startDate;

  private Date                           endDate;

  public GeoObjectConfiguration()
  {
    this.includeCoordinates = false;
    this.functions = new HashMap<String, ShapefileFunction>();
    this.termProblems = new TreeSet<TermProblem>();
    this.locationProblems = new TreeSet<GeoObjectLocationProblem>();
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

  public String getDirectory()
  {
    return directory;
  }

  public void setDirectory(String directory)
  {
    this.directory = directory;
  }

  public String getFilename()
  {
    return filename;
  }

  public void setFilename(String filename)
  {
    this.filename = filename;
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

  public Map<String, ShapefileFunction> getFunctions()
  {
    return functions;
  }

  public ShapefileFunction getFunction(String attributeName)
  {
    return this.functions.get(attributeName);
  }

  public void setFunction(String attributeName, ShapefileFunction function)
  {
    this.functions.put(attributeName, function);
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

  public Set<TermProblem> getTermProblems()
  {
    return termProblems;
  }

  public void addProblem(TermProblem problem)
  {
    this.termProblems.add(problem);
  }

  public Set<GeoObjectLocationProblem> getLocationProblems()
  {
    return locationProblems;
  }

  public void addProblem(GeoObjectLocationProblem problem)
  {
    this.locationProblems.add(problem);
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

  public boolean hasProblems()
  {
    return this.termProblems.size() > 0 || this.locationProblems.size() > 0;
  }

  public Boolean isPostalCode()
  {
    return postalCode;
  }

  public void setPostalCode(Boolean postalCode)
  {
    this.postalCode = postalCode;
  }

  @Request
  public JsonObject toJson()
  {
    SimpleDateFormat format = new SimpleDateFormat(GeoObjectConfiguration.DATE_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    JsonObject type = this.type.toJSON(new ImportAttributeSerializer(Session.getCurrentLocale(), this.includeCoordinates, SupportedLocaleDAO.getSupportedLocales()));
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();
      String attributeName = attribute.get(AttributeType.JSON_CODE).getAsString();

      if (this.functions.containsKey(attributeName))
      {
        ShapefileFunction function = this.functions.get(attributeName);

        if (function instanceof LocalizedValueFunction)
        {
          String locale = attribute.get("locale").getAsString();

          ShapefileFunction localeFunction = ( (LocalizedValueFunction) function ).getFunction(locale);

          if (localeFunction != null)
          {
            attribute.addProperty(TARGET, localeFunction.toJson());
          }
        }
        else
        {
          attribute.addProperty(TARGET, function.toJson());
        }
      }
    }

    JsonArray locations = new JsonArray();

    for (Location location : this.locations)
    {
      locations.add(location.toJson());
    }

    JsonObject config = new JsonObject();
    config.add(GeoObjectConfiguration.TYPE, type);
    config.add(GeoObjectConfiguration.LOCATIONS, locations);
    config.addProperty(GeoObjectConfiguration.DIRECTORY, this.getDirectory());
    config.addProperty(GeoObjectConfiguration.FILENAME, this.getFilename());
    config.addProperty(GeoObjectConfiguration.POSTAL_CODE, this.isPostalCode());

    if (this.getStartDate() != null)
    {
      config.addProperty(GeoObjectConfiguration.START_DATE, format.format(this.getStartDate()));
    }

    if (this.getEndDate() != null)
    {
      config.addProperty(GeoObjectConfiguration.END_DATE, format.format(this.getEndDate()));
    }

    if (this.hierarchy != null)
    {
      config.addProperty(GeoObjectConfiguration.HIERARCHY, this.getHierarchy().getCode());
    }

    if (this.exclusions.size() > 0)
    {
      JsonArray exclusions = new JsonArray();

      this.exclusions.forEach((key, set) -> {
        set.forEach(value -> {
          JsonObject object = new JsonObject();
          object.addProperty(AttributeType.JSON_CODE, key);
          object.addProperty(VALUE, value);

          exclusions.add(object);
        });
      });

      config.add(EXCLUSIONS, exclusions);
    }

    if (this.termProblems.size() > 0)
    {
      JsonArray problems = new JsonArray();

      for (TermProblem problem : this.termProblems)
      {
        problems.add(problem.toJSON());
      }

      config.add(GeoObjectConfiguration.TERM_PROBLEMS, problems);
    }

    if (this.locationProblems.size() > 0)
    {
      JsonArray problems = new JsonArray();

      for (GeoObjectLocationProblem problem : this.locationProblems)
      {
        problems.add(problem.toJSON());
      }

      config.add(GeoObjectConfiguration.LOCATION_PROBLEMS, problems);
    }

    return config;
  }

  @Request
  public static GeoObjectConfiguration parse(String json, boolean includeCoordinates)
  {
    SimpleDateFormat format = new SimpleDateFormat(GeoObjectConfiguration.DATE_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    JsonObject config = new JsonParser().parse(json).getAsJsonObject();
    JsonObject type = config.get(TYPE).getAsJsonObject();
    JsonArray locations = config.has(LOCATIONS) ? config.get(LOCATIONS).getAsJsonArray() : new JsonArray();
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();
    String code = type.get(GeoObjectType.JSON_CODE).getAsString();
    ServerGeoObjectType got = ServerGeoObjectType.get(code);

    GeoObjectConfiguration configuration = new GeoObjectConfiguration();
    configuration.setDirectory(config.get(DIRECTORY).getAsString());
    configuration.setFilename(config.get(FILENAME).getAsString());
    configuration.setType(got);
    configuration.setIncludeCoordinates(includeCoordinates);
    configuration.setPostalCode(config.has(POSTAL_CODE) && config.get(POSTAL_CODE).getAsBoolean());

    try
    {
      if (config.has(GeoObjectConfiguration.START_DATE))
      {
        configuration.setStartDate(format.parse(config.get(GeoObjectConfiguration.START_DATE).getAsString()));
      }

      if (config.has(GeoObjectConfiguration.END_DATE))
      {
        configuration.setEndDate(format.parse(config.get(GeoObjectConfiguration.END_DATE).getAsString()));
      }
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }

    if (config.has(HIERARCHY))
    {
      String hCode = config.get(HIERARCHY).getAsString();

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);
      List<GeoObjectType> ancestors = ServiceFactory.getUtilities().getAncestors(got, hCode);

      configuration.setHierarchy(hierarchyType);

      if (ancestors.size() > 0)
      {
        GeoObjectType rootType = ancestors.get(0);
        GeoObjectQuery query = new GeoObjectQuery(ServerGeoObjectType.get(rootType));
        GeoObject root = query.getSingleResult();

        configuration.setRoot(root);
      }
    }

    if (config.has(EXCLUSIONS))
    {
      JsonArray exclusions = config.get(EXCLUSIONS).getAsJsonArray();

      for (int i = 0; i < exclusions.size(); i++)
      {
        JsonObject exclusion = exclusions.get(i).getAsJsonObject();
        String attributeName = exclusion.get(AttributeType.JSON_CODE).getAsString();
        String value = exclusion.get(VALUE).getAsString();

        configuration.addExclusion(attributeName, value);
      }
    }

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();

      if (attribute.has(TARGET))
      {
        String attributeName = attribute.get(AttributeType.JSON_CODE).getAsString();
        String target = attribute.get(TARGET).getAsString();

        if (attribute.has("locale"))
        {
          String locale = attribute.get("locale").getAsString();

          if (configuration.getFunction(attributeName) == null)
          {
            configuration.setFunction(attributeName, new LocalizedValueFunction());
          }

          LocalizedValueFunction function = (LocalizedValueFunction) configuration.getFunction(attributeName);
          function.add(locale, new BasicColumnFunction(target));
        }
        else
        {
          configuration.setFunction(attributeName, new BasicColumnFunction(target));
        }
      }
    }

    for (int i = 0; i < locations.size(); i++)
    {
      JsonObject location = locations.get(i).getAsJsonObject();

      if (location.has(TARGET))
      {
        String pCode = location.get(AttributeType.JSON_CODE).getAsString();
        ServerGeoObjectType pType = ServerGeoObjectType.get(pCode);

        String target = location.get(TARGET).getAsString();

        configuration.addParent(new Location(pType, new BasicColumnFunction(target)));
      }
    }

    return configuration;
  }

  public static String getBaseType(String attributeType)
  {
    if (attributeType.equals(AttributeBooleanType.TYPE))
    {
      return AttributeBooleanType.TYPE;
    }
    else if (attributeType.equals(AttributeTermType.TYPE) || attributeType.equals(AttributeCharacterType.TYPE) || attributeType.equals(AttributeLocalType.TYPE))
    {
      return GeoObjectConfiguration.TEXT;
    }
    else if (attributeType.equals(AttributeFloatType.TYPE) || attributeType.equals(AttributeIntegerType.TYPE))
    {
      return GeoObjectConfiguration.NUMERIC;
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
      return GeoObjectConfiguration.TEXT;
    }
    else if (Number.class.isAssignableFrom(clazz))
    {
      return GeoObjectConfiguration.NUMERIC;
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

    return new AttributeFloatType(GeoObjectConfiguration.LATITUDE, label, description, false, false, false);
  }

  public static AttributeFloatType longitude()
  {
    LocalizedValue label = new LocalizedValue(LocalizationFacade.getFromBundles(LONGITUDE_KEY));
    LocalizedValue description = new LocalizedValue("");

    return new AttributeFloatType(GeoObjectConfiguration.LONGITUDE, label, description, false, false, false);
  }
}
