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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.Pair;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.constants.Constants;
import com.runwaysdk.constants.MdAttributeDateTimeUtil;
import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.LocalizedValueStoreStoreValue;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.etl.ListTypeJob;
import net.geoprism.registry.etl.ListTypeJobQuery;
import net.geoprism.registry.graph.AttributeBooleanType;
import net.geoprism.registry.graph.AttributeClassificationType;
import net.geoprism.registry.graph.AttributeDateType;
import net.geoprism.registry.graph.AttributeTermType;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.model.localization.DefaultLocaleView;
import net.geoprism.registry.query.graph.AttributeValueRestriction;
import net.geoprism.registry.query.graph.BasicVertexRestriction;
import net.geoprism.registry.query.graph.CompositeRestriction;
import net.geoprism.registry.roles.CreateListPermissionException;
import net.geoprism.registry.roles.UpdateListPermissionException;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.service.request.LocaleSerializer;
import net.geoprism.registry.service.request.SerializedListTypeCache;
import net.geoprism.registry.service.business.ServiceFactory;

public abstract class ListType extends ListTypeBase
{
  private static final long  serialVersionUID    = 190790165;

  public static final String TYPE_CODE           = "typeCode";

  public static final String ATTRIBUTES          = "attributes";

  public static final String NAME                = "name";

  public static final String LABEL               = "label";

  public static final String VALUE               = "value";

  public static final String TYPE                = "type";

  public static final String BASE                = "base";

  public static final String DEPENDENCY          = "dependency";

  public static final String DEFAULT_LOCALE      = "DefaultLocale";

  public static final String VERSIONS            = "versions";

  public static final String PUBLIC              = "PUBLIC";

  public static final String PRIVATE             = "PRIVATE";

  public static final String LIST_TYPE           = "listType";

  public static final String LIST_METADATA       = "listMetadata";

  public static final String GEOSPATIAL_METADATA = "geospatialMetadata";

  public static final String SINGLE              = "single";

  public static final String INTERVAL            = "interval";

  public static final String INCREMENTAL         = "incremental";

  public static final String FILTER              = "filter";

  public ListType()
  {
    super();
  }

  protected abstract JsonObject formatVersionLabel(LabeledVersion version);

  public abstract void createEntries(JsonObject metadata);

  public void setOrganization(ServerOrganization value)
  {
    this.setOrganization(value.getOrganization());
  }

  @Override
  @Transaction
  public void apply()
  {
    if (!isValidName(this.getCode()))
    {
      throw new InvalidMasterListCodeException("The list code has an invalid character");
    }

    SerializedListTypeCache.getInstance().remove(this.getOid());

    super.apply();
  }

  @Override
  @Transaction
  public void delete()
  {
    // Validate there are no public versions
    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(this));
    query.AND(query.getWorking().EQ(false));
    query.AND(OR.get(query.getListVisibility().EQ(ListType.PUBLIC), query.getGeospatialVisibility().EQ(ListType.PUBLIC)));

    long count = query.getCount();

    if (count > 0)
    {
      throw new CannotDeletePublicListTypeException();
    }

    // Delete all jobs
    this.getJobs().forEach(job -> {
      job.delete();
    });

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

  public List<ListTypeEntry> getEntries()
  {
    ListTypeEntryQuery query = new ListTypeEntryQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(this));
    query.ORDER_BY_DESC(query.getForDate());

    try (OIterator<? extends ListTypeEntry> it = query.getIterator())
    {
      return new LinkedList<ListTypeEntry>(it.getAll());
    }
  }

  public List<ListTypeVersion> getVersions()
  {
    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(this));
    query.ORDER_BY_DESC(query.getForDate());

    try (OIterator<? extends ListTypeVersion> it = query.getIterator())
    {
      return new LinkedList<ListTypeVersion>(it.getAll());
    }
  }

  public List<ListTypeVersion> getWorkingVersions()
  {
    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(this));
    query.WHERE(query.getWorking().EQ(true));
    query.ORDER_BY_DESC(query.getForDate());

    try (OIterator<? extends ListTypeVersion> it = query.getIterator())
    {
      return new LinkedList<ListTypeVersion>(it.getAll());
    }
  }

  public List<ListTypeJob> getJobs()
  {
    ListTypeJobQuery query = new ListTypeJobQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(this));

    try (OIterator<? extends ListTypeJob> it = query.getIterator())
    {
      return new LinkedList<ListTypeJob>(it.getAll());
    }
  }

  public JsonArray getHierarchiesAsJson()
  {
    if (this.getHierarchies() != null && this.getHierarchies().length() > 0)
    {
      return JsonParser.parseString(this.getHierarchies()).getAsJsonArray();
    }

    return new JsonArray();
  }

  public JsonArray getSubtypeHierarchiesAsJson()
  {
    if (this.getSubtypeHierarchies() != null && this.getSubtypeHierarchies().length() > 0)
    {
      return JsonParser.parseString(this.getSubtypeHierarchies()).getAsJsonArray();
    }

    return new JsonArray();
  }

  public Map<ServerHierarchyType, List<ServerGeoObjectType>> getAncestorMap(ServerGeoObjectType type)
  {
    Map<ServerHierarchyType, List<ServerGeoObjectType>> map = new HashMap<>();

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<Pair<String, Integer>> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();
        ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);

        List<ServerGeoObjectType> ancestors = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class).getTypeAncestors(type, hierarchyType, true);

        map.put(hierarchyType, ancestors);
      }
    }

    return map;
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
    String typeCode = object.get(ListType.TYPE_CODE).getAsString();
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    this.setGeoObjectType(type.getType());
    RegistryLocalizedValueConverter.populate(this.getDisplayLabel(), LocalizedValue.fromJSON(object.get(ListType.DISPLAYLABEL).getAsJsonObject()));
    RegistryLocalizedValueConverter.populate(this.getDescription(), LocalizedValue.fromJSON(object.get(ListType.DESCRIPTION).getAsJsonObject()));
    this.setCode(object.get(ListType.CODE).getAsString());
    this.setHierarchies(object.get(ListType.HIERARCHIES).getAsJsonArray().toString());
    this.setOrganization(Organization.getByCode(object.get(ListType.ORGANIZATION).getAsString()));

    if (object.has(ListType.INCLUDELATLONG))
    {
      this.setIncludeLatLong(object.get(ListType.INCLUDELATLONG).getAsBoolean());
    }

    if (object.has(ListType.SUBTYPEHIERARCHIES) && !object.get(ListType.SUBTYPEHIERARCHIES).isJsonNull())
    {
      this.setSubtypeHierarchies(object.get(ListType.SUBTYPEHIERARCHIES).getAsJsonArray().toString());
    }

    if (object.has(ListType.FILTER) && !object.get(ListType.FILTER).isJsonNull())
    {
      this.setFilterJson(object.get(ListType.FILTER).getAsJsonArray().toString());
    }

    // Parse the list metadata
    // this.parseMetadata("list", object.get(LIST_METADATA).getAsJsonObject());
    // this.parseMetadata("geospatial",
    // object.get(GEOSPATIAL_METADATA).getAsJsonObject());
  }

  private void parseMetadata(String prefix, JsonObject object)
  {
    ( (LocalStruct) this.getStruct(prefix + "Label") ).setLocaleMap(LocalizedValue.fromJSON(object.get("label").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "Description") ).setLocaleMap(LocalizedValue.fromJSON(object.get("description").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "Process") ).setLocaleMap(LocalizedValue.fromJSON(object.get("process").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "Progress") ).setLocaleMap(LocalizedValue.fromJSON(object.get("progress").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "AccessConstraints") ).setLocaleMap(LocalizedValue.fromJSON(object.get("accessConstraints").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "UseConstraints") ).setLocaleMap(LocalizedValue.fromJSON(object.get("useConstraints").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "Acknowledgements") ).setLocaleMap(LocalizedValue.fromJSON(object.get("acknowledgements").getAsJsonObject()).getLocaleMap());
    ( (LocalStruct) this.getStruct(prefix + "Disclaimer") ).setLocaleMap(LocalizedValue.fromJSON(object.get("disclaimer").getAsJsonObject()).getLocaleMap());
    this.setValue(prefix + "ContactName", object.get("contactName").getAsString());
    this.setValue(prefix + "Organization", object.get("organization").getAsString());
    this.setValue(prefix + "TelephoneNumber", object.get("telephoneNumber").getAsString());
    this.setValue(prefix + "Email", object.get("email").getAsString());

    if (!object.get("originator").isJsonNull())
    {
      this.setValue(prefix + "Originator", object.get("originator").getAsString());
    }

    if (!object.get("collectionDate").isJsonNull())
    {
      SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATETIME_FORMAT);
      Date collectionDate = GeoRegistryUtil.parseDate(object.get("collectionDate").getAsString());

      if (collectionDate != null)
      {
        this.setValue(prefix + "CollectionDate", formatter.format(collectionDate));
      }
    }

    if (prefix.equals("geospatial"))
    {
      this.setGeospatialTopicCategories(!object.get("topicCategories").isJsonNull() ? object.get("topicCategories").getAsString() : null);
      this.setGeospatialPlaceKeywords(!object.get("placeKeywords").isJsonNull() ? object.get("placeKeywords").getAsString() : null);
      this.setGeospatialUpdateFrequency(!object.get("updateFrequency").isJsonNull() ? object.get("updateFrequency").getAsString() : null);
      this.setGeospatialLineage(!object.get("lineage").isJsonNull() ? object.get("lineage").getAsString() : null);
      this.setGeospatialLanguages(!object.get("languages").isJsonNull() ? object.get("languages").getAsString() : null);
      this.setGeospatialScaleResolution(!object.get("scaleResolution").isJsonNull() ? object.get("scaleResolution").getAsString() : null);
      this.setGeospatialSpatialRepresentation(!object.get("spatialRepresentation").isJsonNull() ? object.get("spatialRepresentation").getAsString() : null);
      this.setGeospatialReferenceSystem(!object.get("referenceSystem").isJsonNull() ? object.get("referenceSystem").getAsString() : null);
      this.setGeospatialReportSpecification(!object.get("reportSpecification").isJsonNull() ? object.get("reportSpecification").getAsString() : null);
      this.setGeospatialDistributionFormat(!object.get("distributionFormat").isJsonNull() ? object.get("distributionFormat").getAsString() : null);
    }

  }

  public final JsonObject toJSON()
  {
    return this.toJSON(false);
  }

  public JsonObject toJSON(boolean includeEntries)
  {
    Locale locale = Session.getCurrentLocale();
    LocaleSerializer serializer = new LocaleSerializer(locale);

    ServerGeoObjectType type = this.getServerGeoObjectType();
    ServerGeoObjectType superType = type.getSuperType();
    ServerOrganization org = type.getOrganization();

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(ListType.OID, this.getOid());
    }

    object.addProperty(ListType.ORGANIZATION, org.getCode());
    object.addProperty("write", this.doesActorHaveWritePermission());
    object.addProperty("read", this.doesActorHaveReadPermission());
    object.addProperty("exploratory", this.doesActorHaveExploratoryPermission());
    object.addProperty("typeLabel", type.getLabel().getValue());
    object.addProperty("typePrivate", type.getIsPrivate());
    object.addProperty(ListType.TYPE_CODE, type.getCode());
    object.add(ListType.DISPLAYLABEL, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON(serializer));
    object.add(ListType.DESCRIPTION, RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()).toJSON(serializer));
    object.addProperty(ListType.CODE, this.getCode());
    object.add(ListType.HIERARCHIES, this.getHierarchiesAsJson());
    object.add(ListType.SUBTYPEHIERARCHIES, this.getSubtypeHierarchiesAsJson());

    if (this.getFilterJson() != null && this.getFilterJson().length() > 0)
    {
      object.add(ListType.FILTER, JsonParser.parseString(this.getFilterJson()).getAsJsonArray());
    }

    if (type.getGeometryType().equals(GeometryType.MULTIPOINT) || type.getGeometryType().equals(GeometryType.POINT))
    {
      object.addProperty(ListType.INCLUDELATLONG, this.getIncludeLatLong());
    }

    if (superType != null)
    {
      object.addProperty("superTypeCode", superType.getCode());
    }

    // Include the list metadata
    object.add(ListType.LIST_METADATA, this.toMetadataJSON("list", serializer));
    object.add(ListType.GEOSPATIAL_METADATA, this.toMetadataJSON("geospatial", serializer));

    if (includeEntries)
    {
      List<ListTypeEntry> entries = this.getEntries();

      JsonArray jEntries = new JsonArray();

      for (ListTypeEntry entry : entries)
      {
        jEntries.add(entry.toJSON());
      }

      object.add("entries", jEntries);
    }

    return object;
  }

  protected final JsonObject toMetadataJSON(String prefix, CustomSerializer serializer)
  {
    JsonObject object = new JsonObject();
    object.add("label", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Label")).toJSON(serializer));
    object.add("description", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Description")).toJSON(serializer));
    object.add("process", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Process")).toJSON(serializer));
    object.add("progress", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Progress")).toJSON(serializer));
    object.add("accessConstraints", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "AccessConstraints")).toJSON(serializer));
    object.add("useConstraints", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "UseConstraints")).toJSON(serializer));
    object.add("acknowledgements", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Acknowledgements")).toJSON(serializer));
    object.add("disclaimer", RegistryLocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Disclaimer")).toJSON(serializer));
    object.addProperty("collectionDate", GeoRegistryUtil.formatDate(MdAttributeDateTimeUtil.getTypeSafeValue(getValue(prefix + "CollectionDate")), false));
    object.addProperty("organization", this.getValue(prefix + "Organization"));
    object.addProperty("contactName", this.getValue(prefix + "ContactName"));
    object.addProperty("telephoneNumber", this.getValue(prefix + "TelephoneNumber"));
    object.addProperty("email", this.getValue(prefix + "Email"));
    object.addProperty("originator", this.getValue(prefix + "Originator"));

    if (prefix.equals("geospatial"))
    {
      object.addProperty("topicCategories", this.getGeospatialTopicCategories());
      object.addProperty("placeKeywords", this.getGeospatialPlaceKeywords());
      object.addProperty("updateFrequency", this.getGeospatialUpdateFrequency());
      object.addProperty("lineage", this.getGeospatialLineage());
      object.addProperty("languages", this.getGeospatialLanguages());
      object.addProperty("scaleResolution", this.getGeospatialScaleResolution());
      object.addProperty("spatialRepresentation", this.getGeospatialSpatialRepresentation());
      object.addProperty("referenceSystem", this.getGeospatialReferenceSystem());
      object.addProperty("reportSpecification", this.getGeospatialReportSpecification());
      object.addProperty("distributionFormat", this.getGeospatialDistributionFormat());
    }

    return object;
  }

  // private void createMdAttributeFromAttributeType(ServerGeoObjectType type,
  // AttributeType attributeType, Collection<Locale> locales)
  // {
  // List<ListTypeVersion> versions = this.getVersions();
  //
  // for (ListTypeVersion version : versions)
  // {
  // ListTypeVersion.createMdAttributeFromAttributeType(version, type,
  // attributeType, locales);
  // }
  // }
  //
  // private void removeAttributeType(AttributeType attributeType)
  // {
  // List<ListTypeVersion> versions = this.getVersions();
  //
  // for (ListTypeVersion version : versions)
  // {
  // version.removeAttributeType(attributeType);
  // }
  // }

  @Transaction
  @Authenticate
  public ListTypeEntry createEntry(Date forDate)
  {
    return ListTypeEntry.create(this, forDate, null);
  }

  @Transaction
  public ListTypeEntry getOrCreateEntry(Date forDate, JsonObject metadata)
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    ListTypeEntryQuery query = new ListTypeEntryQuery(new QueryFactory());
    query.WHERE(query.getListType().EQ(this));
    query.AND(query.getForDate().EQ(forDate));

    try (OIterator<? extends ListTypeEntry> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return ListTypeEntry.create(this, forDate, metadata);
  }

  public ServerGeoObjectType getServerGeoObjectType()
  {
    return ServerGeoObjectType.get(this.getGeoObjectType().getCode());
  }

  public void enforceActorHasPermission(Operation op)
  {
    if (!doesActorHaveWritePermission())
    {
      if (op.equals(Operation.CREATE))
      {
        CreateListPermissionException ex = new CreateListPermissionException();
        ex.setOrganization(this.getOrganization().getDisplayLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.WRITE))
      {
        UpdateListPermissionException ex = new UpdateListPermissionException();
        ex.setOrganization(this.getOrganization().getDisplayLabel().getValue());
        throw ex;
      }
    }
  }

  public boolean doesActorHaveWritePermission()
  {
    return doesActorHaveWritePermissions(this.getServerGeoObjectType());
  }

  public boolean doesActorHaveExploratoryPermission()
  {
    ServerGeoObjectType type = this.getServerGeoObjectType();

    if (new RolePermissionService().isRC(type.getOrganization().getCode(), type))
    {
      return ServiceFactory.getGeoObjectPermissionService().canRead(type.getOrganization().getCode(), type);
    }

    return doesActorHaveWritePermissions(type);
  }

  public boolean doesActorHaveReadPermission()
  {
    ServerGeoObjectType type = this.getServerGeoObjectType();

    return ServiceFactory.getGeoObjectPermissionService().canRead(type.getOrganization().getCode(), type);
  }

  public void markAsInvalid(ServerHierarchyType hierarchyType, ServerGeoObjectType type)
  {
    boolean isValid = true;

    JsonArray hierarchies = this.getHierarchiesAsJson();
    ServerGeoObjectType masterlistType = this.getServerGeoObjectType();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
      String hCode = hierarchy.get("code").getAsString();
      ServerHierarchyType hType = ServerHierarchyType.get(hCode, false);

      if (hType != null)
      {
        ServerHierarchyType actualHierarchy = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class).findHierarchy(masterlistType, hType, type);

        if (hCode.equals(hierarchyType.getCode()) || actualHierarchy.getCode().equals(hierarchyType.getCode()))
        {
          List<String> pCodes = this.getParentCodes(hierarchy).stream().map(p -> p.getFirst()).collect(Collectors.toList());

          if (pCodes.contains(type.getCode()) || type.getCode().equals(masterlistType.getCode()))
          {
            isValid = false;
          }
        }
      }
      else
      {
        isValid = false;
      }
    }

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

    JsonArray hierarchies = this.getHierarchiesAsJson();
    ServerGeoObjectType masterlistType = this.getServerGeoObjectType();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
      String hCode = hierarchy.get("code").getAsString();

      ServerHierarchyType hType = ServerHierarchyType.get(hCode, false);

      if (hType != null)
      {
        List<String> pCodes = this.getParentCodes(hierarchy).stream().map(p -> p.getFirst()).collect(Collectors.toList());

        if (pCodes.contains(type.getCode()) || type.getCode().equals(masterlistType.getCode()))
        {
          isValid = false;
        }
      }
      else
      {
        isValid = false;
      }
    }

    if (!isValid)
    {
      this.appLock();
      this.setValid(false);
      this.apply();
    }
  }

  public void markAsInvalid(ServerHierarchyType hierarchyType)
  {
    boolean isValid = true;

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
      String hCode = hierarchy.get("code").getAsString();

      if (hierarchyType.getCode().equals(hCode))
      {
        isValid = false;
      }
    }

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

  private void createMdAttributeFromAttributeType(ServerGeoObjectType type, AttributeType attributeType, Collection<SupportedLocaleIF> locales)
  {
    LocalizedValueStore lvs = LocalizedValueStore.getByKey(DefaultLocaleView.LABEL);
    LocalizedValueStoreStoreValue defaultLocaleLabel = lvs.getStoreValue();

    List<ListTypeVersion> versions = this.getVersions();

    for (ListTypeVersion version : versions)
    {
      if (version.getWorking())
      {
        ListTypeVersion.createMdAttributeFromAttributeType(version, type, attributeType.toDTO(), locales, defaultLocaleLabel);
      }
    }
  }

  private void removeAttributeType(AttributeType attributeType)
  {
    List<ListTypeVersion> versions = this.getVersions();

    for (ListTypeVersion version : versions)
    {
      if (version.getWorking())
      {
        version.removeAttributeType(attributeType.toDTO());
      }
    }
  }

  public BasicVertexRestriction getRestriction(ServerGeoObjectType type, Date forDate)
  {
    String filterJson = this.getFilterJson();

    if (filterJson != null && filterJson.length() > 0)
    {

      JsonArray filters = JsonParser.parseString(filterJson).getAsJsonArray();

      CompositeRestriction restriction = new CompositeRestriction();

      for (int i = 0; i < filters.size(); i++)
      {
        JsonObject filter = filters.get(i).getAsJsonObject();
        String attributeName = filter.get("attribute").getAsString();
        String operation = filter.get("operation").getAsString();

        AttributeType attributeType = type.getAttribute(attributeName).get();
        MdAttributeDAOIF mdAttribute = type.getMdVertex().definesAttribute(attributeName);

        if (attributeType instanceof AttributeDateType)
        {
          String value = filter.get("value").getAsString();

          Date date = GeoRegistryUtil.parseDate(value, false);

          restriction.add(new AttributeValueRestriction(mdAttribute, operation, date, forDate));

        }
        else if (attributeType instanceof AttributeBooleanType)
        {
          String value = filter.get("value").getAsString();

          Boolean bVal = Boolean.valueOf(value);

          restriction.add(new AttributeValueRestriction(mdAttribute, operation, bVal, forDate));
        }
        else if (attributeType instanceof AttributeTermType)
        {
          String code = filter.get("value").getAsString();

          Classifier root = ( (AttributeTermType) attributeType ).getRootTerm();
          String parent = TermConverter.buildClassifierKeyFromTermCode(root.getKey());

          String classifierKey = Classifier.buildKey(parent, code);
          Classifier classifier = Classifier.getByKey(classifierKey);

          restriction.add(new AttributeValueRestriction(mdAttribute, operation, classifier.getOid(), forDate));
        }
        else if (attributeType instanceof AttributeClassificationType)
        {
          JsonObject object = filter.get("value").getAsJsonObject();
          Term term = Term.fromJSON(object);
          MdClassificationDAOIF mdClassification = ( (MdAttributeClassificationDAOIF) mdAttribute ).getMdClassificationDAOIF();
          ClassificationType classificationType = new ClassificationType(mdClassification);

          ClassificationBusinessServiceIF service = ServiceFactory.getBean(ClassificationBusinessServiceIF.class);

          Classification classification = service.get(classificationType, term.getCode());

          restriction.add(new AttributeValueRestriction(mdAttribute, operation, classification.getVertex().getRID(), forDate));
        }
        else
        {
          String value = filter.get("value").getAsString();

          restriction.add(new AttributeValueRestriction(mdAttribute, operation, value, forDate));
        }

      }

      if (restriction.getRestrictions().size() > 0)
      {
        return restriction;
      }
    }

    return null;
  }

  public static ListType fromJSON(JsonObject object)
  {
    ListType list = null;

    if (object.has("oid") && !object.get("oid").isJsonNull())
    {
      String oid = object.get("oid").getAsString();

      list = ListType.lock(oid);
    }
    else if (object.get(ListType.LIST_TYPE).getAsString().equals(ListType.SINGLE))
    {
      list = new SingleListType();
    }
    else if (object.get(ListType.LIST_TYPE).getAsString().equals(ListType.INCREMENTAL))
    {
      list = new IncrementalListType();
    }
    else if (object.get(ListType.LIST_TYPE).getAsString().equals(ListType.INTERVAL))
    {
      list = new IntervalListType();
    }
    else
    {
      throw new UnsupportedOperationException("Unknown list type");
    }

    list.parse(object);

    return list;
  }

  @Transaction
  public static ListType apply(JsonObject object)
  {
    ListType list = ListType.fromJSON(object);

    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      list.enforceActorHasPermission(Operation.CREATE);
    }

    boolean isNew = list.isNew() && !list.isAppliedToDB();

    list.apply();

    list.createEntries(isNew ? object : null);

    return list;
  }

  @Transaction
  public static void deleteAll(ServerGeoObjectType type)
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());
    query.WHERE(query.getGeoObjectType().EQ(type.getOid()));

    List<? extends ListType> lists = query.getIterator().getAll();

    for (ListType list : lists)
    {
      list.delete();
    }
  }

  // public static void createMdAttribute(ServerGeoObjectType type,
  // AttributeType attributeType)
  // {
  // Collection<Locale> locales = LocalizationFacade.getInstalledLocales();
  //
  // ListTypeQuery query = new ListTypeQuery(new QueryFactory());
  // query.WHERE(query.getUniversal().EQ(type.getUniversal()));
  //
  // List<? extends ListType> lists = query.getIterator().getAll();
  //
  // for (ListType list : lists)
  // {
  // list.createMdAttributeFromAttributeType(type, attributeType, locales);
  // }
  // }
  //
  // public static void deleteMdAttribute(Universal universal, AttributeType
  // attributeType)
  // {
  // ListTypeQuery query = new ListTypeQuery(new QueryFactory());
  // query.WHERE(query.getUniversal().EQ(universal));
  //
  // List<? extends ListType> lists = query.getIterator().getAll();
  //
  // for (ListType list : lists)
  // {
  // list.removeAttributeType(attributeType);
  // }
  // }

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

    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    try (OIterator<? extends ListType> it = query.getIterator())
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

      it.getAll().stream().filter(list -> list.doesActorHaveReadPermission()).sorted((a, b) -> {
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

  public static JsonObject listForType(String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    ServerOrganization org = type.getOrganization();

    final JsonArray lists = new JsonArray();
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());
    query.WHERE(query.getGeoObjectType().EQ(type.getOid()));

    try (OIterator<? extends ListType> it = query.getIterator())
    {
      it.getAll().stream().sorted((a, b) -> {

        int compareTo = a.getType().compareTo(b.getType());

        if (compareTo == 0)
        {
          return a.getDisplayLabel().getValue().compareTo(b.getDisplayLabel().getValue());
        }

        return compareTo;
      }).filter(f -> {
        // TODO Make visible if the type has a public version???
        // return isMember;

        return true;
      }).forEach(list -> {
        // JsonObject object = new JsonObject();
        // object.addProperty("label", list.getDisplayLabel().getValue());
        // object.addProperty("oid", list.getOid());
        // object.addProperty("createDate",
        // format.format(list.getCreateDate()));
        // object.addProperty("lasteUpdateDate",
        // format.format(list.getLastUpdateDate()));
        // object.addProperty("write", list.doesActorHaveWritePermission());
        // object.addProperty("read", list.doesActorHaveReadPermission());
        //
        lists.add(list.toJSON());
      });
    }

    JsonObject object = new JsonObject();
    object.addProperty("orgCode", org.getCode());
    object.addProperty("orgLabel", org.getDisplayLabel().getValue());
    object.addProperty("typeCode", type.getCode());
    object.addProperty("typeLabel", type.getLabel().getValue());
    object.addProperty("geometryType", type.getGeometryType().name());
    object.addProperty("write", doesActorHaveWritePermissions(type));
    object.add("lists", lists);

    return object;
  }

  @Transaction
  public static void markAllAsInvalid(ServerHierarchyType hierarchyType, ServerGeoObjectType type)
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());
    query.WHERE(query.getValid().EQ((Boolean) null));
    query.OR(query.getValid().EQ(true));

    try (OIterator<? extends ListType> iterator = query.getIterator())
    {
      while (iterator.hasNext())
      {
        ListType masterlist = iterator.next();

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

    SerializedListTypeCache.getInstance().clear();
  }

  public static List<ListType> getForType(ServerGeoObjectType type)
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());
    query.WHERE(query.getGeoObjectType().EQ(type.getOid()));

    try (OIterator<? extends ListType> it = query.getIterator())
    {
      return new LinkedList<ListType>(it.getAll());
    }
  }

  public static void createMdAttribute(ServerGeoObjectType type, AttributeType attributeType)
  {
    Collection<SupportedLocaleIF> locales = LocalizationFacade.getSupportedLocales();

    List<ListType> lists = getForType(type);

    for (ListType list : lists)
    {
      list.createMdAttributeFromAttributeType(type, attributeType, locales);
    }
  }

  public static void deleteMdAttribute(ServerGeoObjectType type, AttributeType attributeType)
  {
    List<ListType> lists = getForType(type);

    for (ListType list : lists)
    {
      list.removeAttributeType(attributeType);
    }
  }

  private static boolean doesActorHaveWritePermissions(ServerGeoObjectType type)
  {
    return ServiceFactory.getGeoObjectPermissionService().canWrite(type.getOrganization().getCode(), type);
  }
}
