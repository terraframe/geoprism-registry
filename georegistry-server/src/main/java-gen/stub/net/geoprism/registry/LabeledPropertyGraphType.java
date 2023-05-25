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
package net.geoprism.registry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.Pair;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.StrategyConfiguration;
import net.geoprism.registry.graph.TreeStrategyConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.LocaleSerializer;

public abstract class LabeledPropertyGraphType extends LabeledPropertyGraphTypeBase
{
  private static final long  serialVersionUID = 190790165;

  public static final String GRAPH_TYPE       = "graphType";

  public static final String SINGLE           = "single";

  public static final String INTERVAL         = "interval";

  public static final String INCREMENTAL      = "incremental";

  public static final String TREE             = "TREE";

  public LabeledPropertyGraphType()
  {
    super();
  }

  protected abstract JsonObject formatVersionLabel(LabeledVersion version);

  public abstract void createEntries();

  public void setStrategyConfiguration(StrategyConfiguration configuration)
  {
    super.setStrategyConfiguration(configuration.toJson().toString());
  }

  public StrategyConfiguration toStrategyConfiguration()
  {
    if (this.getStrategyType().equals(TREE))
    {
      return TreeStrategyConfiguration.parse(this.getStrategyConfiguration());
    }

    throw new UnsupportedOperationException();
  }

  @Override
  @Transaction
  public void apply()
  {
    if (!isValidName(this.getCode()))
    {
      throw new InvalidMasterListCodeException("The list code has an invalid character");
    }

    super.apply();
  }

  @Override
  @Transaction
  public void delete()
  {
    // Validate there are no public versions
    // LabeledPropertyGraphTypeVersionQuery query = new
    // LabeledPropertyGraphTypeVersionQuery(new QueryFactory());
    // query.WHERE(query.getGraphType().EQ(this));
    //
    // long count = query.getCount();
    //
    // if (count > 0)
    // {
    // throw new CannotDeletePublicLabeledPropertyGraphTypeException();
    // }

    this.getEntries().forEach(entry -> {
      entry.delete();
    });

    super.delete();

    final File directory = this.getShapefileDirectory();

    if (directory.exists())
    {
      try
      {
        FileUtils.deleteDirectory(directory);
      }
      catch (IOException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }
  }

  public File getShapefileDirectory()
  {
    final File root = GeoprismProperties.getGeoprismFileStorage();
    final File directory = new File(root, "shapefiles");

    return new File(directory, this.getOid());
  }

  public List<LabeledPropertyGraphTypeEntry> getEntries()
  {
    LabeledPropertyGraphTypeEntryQuery query = new LabeledPropertyGraphTypeEntryQuery(new QueryFactory());
    query.WHERE(query.getGraphType().EQ(this));
    query.ORDER_BY_DESC(query.getForDate());

    try (OIterator<? extends LabeledPropertyGraphTypeEntry> it = query.getIterator())
    {
      return new LinkedList<LabeledPropertyGraphTypeEntry>(it.getAll());
    }
  }

  public List<LabeledPropertyGraphTypeVersion> getVersions()
  {
    LabeledPropertyGraphTypeVersionQuery query = new LabeledPropertyGraphTypeVersionQuery(new QueryFactory());
    query.WHERE(query.getGraphType().EQ(this));
    query.ORDER_BY_DESC(query.getForDate());

    try (OIterator<? extends LabeledPropertyGraphTypeVersion> it = query.getIterator())
    {
      return new LinkedList<LabeledPropertyGraphTypeVersion>(it.getAll());
    }
  }

  // public List<LabeledPropertyGraphTypeVersion> getWorkingVersions()
  // {
  // LabeledPropertyGraphTypeVersionQuery query = new
  // LabeledPropertyGraphTypeVersionQuery(new QueryFactory());
  // query.WHERE(query.getLabeledPropertyGraphType().EQ(this));
  // query.WHERE(query.getWorking().EQ(true));
  // query.ORDER_BY_DESC(query.getForDate());
  //
  // try (OIterator<? extends LabeledPropertyGraphTypeVersion> it =
  // query.getIterator())
  // {
  // return new LinkedList<LabeledPropertyGraphTypeVersion>(it.getAll());
  // }
  // }

  public JsonElement getStrategyConfigurationAsJson()
  {
    if (this.getStrategyConfiguration() != null && this.getStrategyConfiguration().length() > 0)
    {
      return JsonParser.parseString(this.getStrategyConfiguration());
    }

    return new JsonObject();
  }

  public List<Pair<String, Integer>> getParentCodes(JsonObject hierarchy)
  {
    List<Pair<String, Integer>> list = new LinkedList<Pair<String, Integer>>();

    JsonArray parents = hierarchy.get("parents").getAsJsonArray();

    for (int i = 0; i < parents.size(); i++)
    {
      JsonObject parent = parents.get(i).getAsJsonObject();

      if (parent.has("selected") && parent.get("selected").getAsBoolean())
      {
        list.add(new Pair<String, Integer>(parent.get("code").getAsString(), Integer.valueOf(i + 1)));
      }
    }

    return list;
  }

  protected void parse(JsonObject object)
  {
    LocalizedValueConverter.populate(this.getDisplayLabel(), LocalizedValue.fromJSON(object.get(LabeledPropertyGraphType.DISPLAYLABEL).getAsJsonObject()));
    LocalizedValueConverter.populate(this.getDescription(), LocalizedValue.fromJSON(object.get(LabeledPropertyGraphType.DESCRIPTION).getAsJsonObject()));
    this.setCode(object.get(LabeledPropertyGraphType.CODE).getAsString());
    this.setHierarchy(object.get(LabeledPropertyGraphType.HIERARCHY).getAsString());

    this.setStrategyType(object.get(LabeledPropertyGraphType.STRATEGYTYPE).getAsString());
    this.setStrategyConfiguration(object.get(LabeledPropertyGraphType.STRATEGYCONFIGURATION).toString());
  }

  public final JsonObject toJSON()
  {
    return this.toJSON(false);
  }

  public JsonObject toJSON(boolean includeEntries)
  {
    Locale locale = Session.getCurrentLocale();
    LocaleSerializer serializer = new LocaleSerializer(locale);

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(LabeledPropertyGraphType.OID, this.getOid());
    }

    object.add(LabeledPropertyGraphType.DISPLAYLABEL, LocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON(serializer));
    object.add(LabeledPropertyGraphType.DESCRIPTION, LocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()).toJSON(serializer));
    object.addProperty(LabeledPropertyGraphType.CODE, this.getCode());
    object.addProperty(LabeledPropertyGraphType.HIERARCHY, this.getHierarchy());
    object.addProperty(LabeledPropertyGraphType.STRATEGYTYPE, this.getStrategyType());
    object.add(LabeledPropertyGraphType.STRATEGYCONFIGURATION, this.getStrategyConfigurationAsJson());

    if (includeEntries)
    {
      List<LabeledPropertyGraphTypeEntry> entries = this.getEntries();

      JsonArray jEntries = new JsonArray();

      for (LabeledPropertyGraphTypeEntry entry : entries)
      {
        jEntries.add(entry.toJSON());
      }

      object.add("entries", jEntries);
    }

    return object;
  }

  // private void createMdAttributeFromAttributeType(ServerGeoObjectType type,
  // AttributeType attributeType, Collection<Locale> locales)
  // {
  // List<LabeledPropertyGraphTypeVersion> versions = this.getVersions();
  //
  // for (LabeledPropertyGraphTypeVersion version : versions)
  // {
  // LabeledPropertyGraphTypeVersion.createMdAttributeFromAttributeType(version,
  // type,
  // attributeType, locales);
  // }
  // }
  //
  // private void removeAttributeType(AttributeType attributeType)
  // {
  // List<LabeledPropertyGraphTypeVersion> versions = this.getVersions();
  //
  // for (LabeledPropertyGraphTypeVersion version : versions)
  // {
  // version.removeAttributeType(attributeType);
  // }
  // }

  @Transaction
  @Authenticate
  public LabeledPropertyGraphTypeEntry createEntry(Date forDate)
  {
    return LabeledPropertyGraphTypeEntry.create(this, forDate);
  }

  @Transaction
  public LabeledPropertyGraphTypeEntry getOrCreateEntry(Date forDate)
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    LabeledPropertyGraphTypeEntryQuery query = new LabeledPropertyGraphTypeEntryQuery(new QueryFactory());
    query.WHERE(query.getGraphType().EQ(this));
    query.AND(query.getForDate().EQ(forDate));

    try (OIterator<? extends LabeledPropertyGraphTypeEntry> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return LabeledPropertyGraphTypeEntry.create(this, forDate);
  }

  public void setHierarchyType(ServerHierarchyType type)
  {
    this.setHierarchy(type.getCode());
  }

  public ServerHierarchyType getHierarchyType()
  {
    return ServerHierarchyType.get(this.getHierarchy());
  }

  public void markAsInvalid(ServerHierarchyType hierarchyType, ServerGeoObjectType type)
  {
    boolean isValid = true;


    if (!isValid)
    {
      this.appLock();
      this.setValid(false);
      this.apply();
    }
  }

  public void markAsInvalid(ServerGeoObjectType type)
  {
    boolean isValid = true;


    if (!isValid)
    {
      this.appLock();
      this.setValid(false);
      this.apply();
    }
  }

  public void markAsInvalid(ServerHierarchyType hierarchyType)
  {
    boolean isValid = this.getHierarchy().equals(hierarchyType.getCode());


    if (!isValid)
    {
      this.appLock();
      this.setValid(false);
      this.apply();
    }
  }

  public boolean isValid()
  {
    return ( this.getValid() == null || this.getValid() );
  }

  // public BasicVertexRestriction getRestriction(ServerGeoObjectType type, Date
  // forDate)
  // {
  // String filterJson = this.getFilterJson();
  //
  // if (filterJson != null && filterJson.length() > 0)
  // {
  //
  // JsonArray filters = JsonParser.parseString(filterJson).getAsJsonArray();
  //
  // CompositeRestriction restriction = new CompositeRestriction();
  //
  // for (int i = 0; i < filters.size(); i++)
  // {
  // JsonObject filter = filters.get(i).getAsJsonObject();
  // String attributeName = filter.get("attribute").getAsString();
  // String operation = filter.get("operation").getAsString();
  //
  // AttributeType attributeType = type.getAttribute(attributeName).get();
  // MdAttributeDAOIF mdAttribute =
  // type.getMdVertex().definesAttribute(attributeName);
  //
  // if (attributeType instanceof AttributeDateType)
  // {
  // String value = filter.get("value").getAsString();
  //
  // Date date = GeoRegistryUtil.parseDate(value, false);
  //
  // restriction.add(new AttributeValueRestriction(mdAttribute, operation, date,
  // forDate));
  //
  // }
  // else if (attributeType instanceof AttributeBooleanType)
  // {
  // String value = filter.get("value").getAsString();
  //
  // Boolean bVal = Boolean.valueOf(value);
  //
  // restriction.add(new AttributeValueRestriction(mdAttribute, operation, bVal,
  // forDate));
  // }
  // else if (attributeType instanceof AttributeTermType)
  // {
  // String code = filter.get("value").getAsString();
  //
  // Term root = ( (AttributeTermType) attributeType ).getRootTerm();
  // String parent =
  // TermConverter.buildClassifierKeyFromTermCode(root.getCode());
  //
  // String classifierKey = Classifier.buildKey(parent, code);
  // Classifier classifier = Classifier.getByKey(classifierKey);
  //
  // restriction.add(new AttributeValueRestriction(mdAttribute, operation,
  // classifier.getOid(), forDate));
  // }
  // else if (attributeType instanceof AttributeClassificationType)
  // {
  // JsonObject object = filter.get("value").getAsJsonObject();
  // Term term = Term.fromJSON(object);
  // MdClassificationDAOIF mdClassification = ( (MdAttributeClassificationDAOIF)
  // mdAttribute ).getMdClassificationDAOIF();
  // MdEdgeDAOIF mdEdge = mdClassification.getReferenceMdEdgeDAO();
  // ClassificationType classificationType = new
  // ClassificationType(mdClassification);
  //
  // Classification classification = Classification.get(classificationType,
  // term.getCode());
  //
  // restriction.add(new AttributeValueRestriction(mdAttribute, operation,
  // classification.getVertex().getRID(), forDate));
  // }
  // else
  // {
  // String value = filter.get("value").getAsString();
  //
  // restriction.add(new AttributeValueRestriction(mdAttribute, operation,
  // value, forDate));
  // }
  //
  // }
  //
  // if (restriction.getRestrictions().size() > 0)
  // {
  // return restriction;
  // }
  // }
  //
  // return null;
  // }
  //
  public static LabeledPropertyGraphType fromJSON(JsonObject object)
  {
    LabeledPropertyGraphType list = null;

    if (object.has("oid") && !object.get("oid").isJsonNull())
    {
      String oid = object.get("oid").getAsString();

      list = LabeledPropertyGraphType.lock(oid);
    }
    else if (object.get(LabeledPropertyGraphType.GRAPH_TYPE).getAsString().equals(LabeledPropertyGraphType.SINGLE))
    {
      list = new SingleLabeledPropertyGraphType();
    }
    else if (object.get(LabeledPropertyGraphType.GRAPH_TYPE).getAsString().equals(LabeledPropertyGraphType.INCREMENTAL))
    {
      list = new IncrementalLabeledPropertyGraphType();
    }
    else if (object.get(LabeledPropertyGraphType.GRAPH_TYPE).getAsString().equals(LabeledPropertyGraphType.INTERVAL))
    {
      list = new IntervalLabeledPropertyGraphType();
    }
    else
    {
      throw new UnsupportedOperationException("Unknown list type");
    }

    list.parse(object);

    return list;
  }

  @Transaction
  public static LabeledPropertyGraphType apply(JsonObject object)
  {
    LabeledPropertyGraphType list = LabeledPropertyGraphType.fromJSON(object);
    list.apply();

    list.createEntries();

    return list;
  }

  public static boolean isValidName(String name)
  {
    if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("-") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
    {
      return false;
    }

    return true;
  }

  public static JsonArray list()
  {
    JsonArray response = new JsonArray();

    LabeledPropertyGraphTypeQuery query = new LabeledPropertyGraphTypeQuery(new QueryFactory());

    try (OIterator<? extends LabeledPropertyGraphType> it = query.getIterator())
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

      it.getAll().stream().sorted((a, b) -> {
        return a.getDisplayLabel().getValue().compareTo(b.getDisplayLabel().getValue());
      }).forEach(list -> {
        JsonObject object = new JsonObject();
        object.addProperty("label", list.getDisplayLabel().getValue());
        object.addProperty("oid", list.getOid());

        object.addProperty("createDate", format.format(list.getCreateDate()));
        object.addProperty("lasteUpdateDate", format.format(list.getLastUpdateDate()));

        response.add(object);
      });
    }

    return response;
  }


  @Transaction
  public static void markAllAsInvalid(ServerHierarchyType hierarchyType, ServerGeoObjectType type)
  {
    LabeledPropertyGraphTypeQuery query = new LabeledPropertyGraphTypeQuery(new QueryFactory());
    query.WHERE(query.getValid().EQ((Boolean) null));
    query.OR(query.getValid().EQ(true));

    try (OIterator<? extends LabeledPropertyGraphType> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        LabeledPropertyGraphType masterlist = iterator.next();

        if (hierarchyType != null && type != null)
        {
          masterlist.markAsInvalid(hierarchyType, type);
        }
        else if (hierarchyType != null)
        {
          masterlist.markAsInvalid(hierarchyType);
        }
        else if (type != null)
        {
          masterlist.markAsInvalid(type);
        }
      }
    }
  }
}
