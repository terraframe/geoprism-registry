package net.geoprism.registry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.GeoprismProperties;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.etl.ListTypeJob;
import net.geoprism.registry.etl.ListTypeJobQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.roles.CreateListPermissionException;
import net.geoprism.registry.roles.UpdateListPermissionException;
import net.geoprism.registry.service.LocaleSerializer;
import net.geoprism.registry.service.ServiceFactory;

public abstract class ListType extends ListTypeBase
{
  private static final long  serialVersionUID = 190790165;

  public static final String TYPE_CODE        = "typeCode";

  public static final String ATTRIBUTES       = "attributes";

  public static final String NAME             = "name";

  public static final String LABEL            = "label";

  public static final String VALUE            = "value";

  public static final String TYPE             = "type";

  public static final String BASE             = "base";

  public static final String DEPENDENCY       = "dependency";

  public static final String DEFAULT_LOCALE   = "DefaultLocale";

  public static final String VERSIONS         = "versions";

  public static final String PUBLIC           = "PUBLIC";

  public static final String PRIVATE          = "PRIVATE";

  public static final String LIST_TYPE        = "list-type";

  public static final String SINGLE           = "single";

  public static final String INTERVAL         = "interval";

  public static final String INCREMENTAL      = "incremental";

  public ListType()
  {
    super();
  }

  protected abstract String formatVersionLabel(LabeledVersion version);

  public abstract void publishVersions();

  @Override
  @Transaction
  public void apply()
  {
    if (!isValidName(this.getCode()))
    {
      throw new InvalidMasterListCodeException("The master list code has an invalid character");
    }

    super.apply();
  }

  @Override
  @Transaction
  public void delete()
  {
    // Delete all jobs
    List<ListTypeJob> jobs = this.getJobs();

    for (ListTypeJob job : jobs)
    {
      job.delete();
    }

    List<ListTypeEntry> entries = this.getEntries();

    for (ListTypeEntry entry : entries)
    {
      entry.delete();
    }

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

      List<String> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();
        ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);

        List<GeoObjectType> dtoAncestors = type.getTypeAncestors(hierarchyType, true);

        List<ServerGeoObjectType> ancestors = new LinkedList<ServerGeoObjectType>();

        for (GeoObjectType ancestor : dtoAncestors)
        {
          ancestors.add(ServerGeoObjectType.get(ancestor));
        }

        map.put(hierarchyType, ancestors);
      }
    }

    return map;
  }

  public List<String> getParentCodes(JsonObject hierarchy)
  {
    List<String> list = new LinkedList<String>();

    JsonArray parents = hierarchy.get("parents").getAsJsonArray();

    for (int i = 0; i < parents.size(); i++)
    {
      JsonObject parent = parents.get(i).getAsJsonObject();

      if (parent.has("selected") && parent.get("selected").getAsBoolean())
      {
        list.add(parent.get("code").getAsString());
      }
    }

    return list;
  }

  protected void parse(JsonObject object)
  {
    String typeCode = object.get(ListType.TYPE_CODE).getAsString();
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    this.setUniversal(type.getUniversal());
    LocalizedValueConverter.populate(this.getDisplayLabel(), LocalizedValue.fromJSON(object.get(ListType.DISPLAYLABEL).getAsJsonObject()));
    LocalizedValueConverter.populate(this.getDescription(), LocalizedValue.fromJSON(object.get(ListType.DESCRIPTION).getAsJsonObject()));
    this.setCode(object.get(ListType.CODE).getAsString());
    this.setHierarchies(object.get(ListType.HIERARCHIES).getAsJsonArray().toString());
    this.setOrganizationId(object.get(ListType.ORGANIZATION).getAsString());

    if (object.has(ListType.SUBTYPEHIERARCHIES) && !object.get(ListType.SUBTYPEHIERARCHIES).isJsonNull())
    {
      this.setSubtypeHierarchies(object.get(ListType.SUBTYPEHIERARCHIES).getAsJsonArray().toString());
    }

    // Parse the list metadata
    {
      this.setListOriginator(object.get(ListType.LISTORIGINATOR).getAsString());
      this.getListLabel().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTLABEL).getAsJsonObject()).getLocaleMap());
      this.getListDescription().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTDESCRIPTION).getAsJsonObject()).getLocaleMap());
      this.getListProcess().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTPROCESS).getAsJsonObject()).getLocaleMap());
      this.getListProgress().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTPROGRESS).getAsJsonObject()).getLocaleMap());
      this.getListAccessConstraints().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTACCESSCONSTRAINTS).getAsJsonObject()).getLocaleMap());
      this.getListUseConstraints().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTUSECONSTRAINTS).getAsJsonObject()).getLocaleMap());
      this.getListAcknowledgements().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTACKNOWLEDGEMENTS).getAsJsonObject()).getLocaleMap());
      this.getListDisclaimer().setLocaleMap(LocalizedValue.fromJSON(object.get(ListType.LISTDISCLAIMER).getAsJsonObject()).getLocaleMap());
      this.setListContactName(object.get(ListType.LISTCONTACTNAME).getAsString());
      this.setListOrganization(object.get(ListType.LISTORGANIZATION).getAsString());
      this.setListTelephoneNumber(object.get(ListType.LISTTELEPHONENUMBER).getAsString());
      this.setListEmail(object.get(ListType.LISTEMAIL).getAsString());

      if (!object.get(ListType.LISTCOLLECTIONDATE).isJsonNull())
      {
        this.setListCollectionDate(GeoRegistryUtil.parseDate(object.get(ListType.LISTCOLLECTIONDATE).getAsString()));
      }
    }

    // TODO
    // Parse the geospatial metadata
  }

  public JsonObject toJSON()
  {
    Locale locale = Session.getCurrentLocale();
    LocaleSerializer serializer = new LocaleSerializer(locale);

    ServerGeoObjectType type = ServerGeoObjectType.get(this.getUniversal());

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      final Organization org = this.getOrganization();

      object.addProperty(ListType.OID, this.getOid());
      object.addProperty(ListType.ORGANIZATION, org.getOid());
    }
    else
    {
      object.addProperty(ListType.ORGANIZATION, this.getOrganizationOid());
    }

    object.addProperty("write", this.doesActorHaveWritePermission());
    object.addProperty("read", this.doesActorHaveReadPermission());
    object.addProperty("exploratory", this.doesActorHaveExploratoryPermission());
    object.add("typeLabel", type.getLabel().toJSON(serializer));
    object.addProperty(ListType.TYPE_CODE, type.getCode());
    object.add(ListType.DISPLAYLABEL, LocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON(serializer));
    object.add(ListType.DESCRIPTION, LocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()).toJSON(serializer));
    object.addProperty(ListType.CODE, this.getCode());
    object.add(ListType.HIERARCHIES, this.getHierarchiesAsJson());
    object.add(ListType.SUBTYPEHIERARCHIES, this.getSubtypeHierarchiesAsJson());

    // Include the list metadata
    {
      object.add(ListType.LISTLABEL, LocalizedValueConverter.convertNoAutoCoalesce(this.getListLabel()).toJSON(serializer));
      object.add(ListType.LISTDESCRIPTION, LocalizedValueConverter.convertNoAutoCoalesce(this.getListDescription()).toJSON(serializer));
      object.add(ListType.LISTPROCESS, LocalizedValueConverter.convertNoAutoCoalesce(this.getListProcess()).toJSON(serializer));
      object.add(ListType.LISTPROGRESS, LocalizedValueConverter.convertNoAutoCoalesce(this.getListProgress()).toJSON(serializer));
      object.add(ListType.LISTACCESSCONSTRAINTS, LocalizedValueConverter.convertNoAutoCoalesce(this.getListAccessConstraints()).toJSON(serializer));
      object.add(ListType.LISTUSECONSTRAINTS, LocalizedValueConverter.convertNoAutoCoalesce(this.getListUseConstraints()).toJSON(serializer));
      object.add(ListType.LISTACKNOWLEDGEMENTS, LocalizedValueConverter.convertNoAutoCoalesce(this.getListAcknowledgements()).toJSON(serializer));
      object.add(ListType.LISTDISCLAIMER, LocalizedValueConverter.convertNoAutoCoalesce(this.getListDisclaimer()).toJSON(serializer));
      object.addProperty(ListType.LISTCOLLECTIONDATE, GeoRegistryUtil.formatDate(this.getListCollectionDate(), false));
      object.addProperty(ListType.LISTORIGINATOR, this.getListOrganization());
      object.addProperty(ListType.LISTCONTACTNAME, this.getListContactName());
      object.addProperty(ListType.LISTORGANIZATION, this.getListOrganization());
      object.addProperty(ListType.LISTTELEPHONENUMBER, this.getListTelephoneNumber());
      object.addProperty(ListType.LISTEMAIL, this.getListEmail());
    }

    return object;
  }

  public JsonArray getEntryJson()
  {
    List<ListTypeEntry> entries = this.getEntries();

    JsonArray jVersions = new JsonArray();

    for (ListTypeEntry entry : entries)
    {
      jVersions.add(entry.toJSON());
    }

    return jVersions;
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
    return ListTypeEntry.create(this, forDate);
  }

  @Transaction
  public ListTypeEntry getOrCreateEntry(Date forDate)
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

    return ListTypeEntry.create(this, forDate);
  }

  public ServerGeoObjectType getGeoObjectType()
  {
    return ServerGeoObjectType.get(this.getUniversal());
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
    ServerGeoObjectType type = this.getGeoObjectType();

    return ServiceFactory.getGeoObjectPermissionService().canWrite(type.getOrganization().getCode(), type);
  }

  public boolean doesActorHaveExploratoryPermission()
  {
    ServerGeoObjectType type = this.getGeoObjectType();

    if (new RolePermissionService().isRC(type.getOrganization().getCode(), type))
    {
      return ServiceFactory.getGeoObjectPermissionService().canRead(type.getOrganization().getCode(), type);
    }

    return ServiceFactory.getGeoObjectPermissionService().canWrite(type.getOrganization().getCode(), type);
  }

  public boolean doesActorHaveReadPermission()
  {
    ServerGeoObjectType type = this.getGeoObjectType();

    return ServiceFactory.getGeoObjectPermissionService().canRead(type.getOrganization().getCode(), type);
  }

  public void markAsInvalid(ServerHierarchyType hierarchyType, ServerGeoObjectType type)
  {
    boolean isValid = true;

    JsonArray hierarchies = this.getHierarchiesAsJson();
    ServerGeoObjectType masterlistType = this.getGeoObjectType();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
      String hCode = hierarchy.get("code").getAsString();
      Optional<ServerHierarchyType> ht = ServiceFactory.getMetadataCache().getHierachyType(hCode);

      if (ht.isPresent())
      {
        ServerHierarchyType actualHierarchy = masterlistType.findHierarchy(ht.get(), type);

        if (hCode.equals(hierarchyType.getCode()) || actualHierarchy.getCode().equals(hierarchyType.getCode()))
        {
          List<String> pCodes = this.getParentCodes(hierarchy);

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
    ServerGeoObjectType masterlistType = this.getGeoObjectType();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();
      String hCode = hierarchy.get("code").getAsString();

      Optional<ServerHierarchyType> ht = ServiceFactory.getMetadataCache().getHierachyType(hCode);

      if (ht.isPresent())
      {
        List<String> pCodes = this.getParentCodes(hierarchy);

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

    list.parse(object);

    return list;
  }

  @Transaction
  public static ListType create(JsonObject object)
  {
    ListType list = ListType.fromJSON(object);

    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      list.enforceActorHasPermission(Operation.CREATE);
    }

    list.apply();

    return list;
  }

  @Transaction
  public static void deleteAll(Universal universal)
  {
    ListTypeQuery query = new ListTypeQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

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
    query.ORDER_BY_DESC(query.getDisplayLabel().localize());

    OIterator<? extends ListType> it = query.getIterator();

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    try
    {
      while (it.hasNext())
      {
        ListType list = it.next();

        JsonObject object = new JsonObject();
        object.addProperty("label", list.getDisplayLabel().getValue());
        object.addProperty("oid", list.getOid());

        object.addProperty("createDate", format.format(list.getCreateDate()));
        object.addProperty("lasteUpdateDate", format.format(list.getLastUpdateDate()));

        response.add(object);
      }
    }
    finally
    {
      it.close();
    }

    return response;
  }

  public static JsonArray listByOrg(String orgCode)
  {
    JsonArray response = new JsonArray();

    Organization org = Organization.getByCode(orgCode);

    final boolean isMember = Organization.isMember(org);

    ListTypeQuery query = new ListTypeQuery(new QueryFactory());
    query.WHERE(query.getOrganization().EQ(org));

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    final JsonArray lists = new JsonArray();

    try (OIterator<? extends ListType> it = query.getIterator())
    {
      it.getAll().stream().sorted((a, b) -> {
        return a.getDisplayLabel().getValue().compareTo(b.getDisplayLabel().getValue());
      }).filter(f -> {
        // TODO Make visible if the type has a public version???
        // return isMember;

        return true;
      }).forEach(list -> {
        JsonObject object = new JsonObject();
        object.addProperty("label", list.getDisplayLabel().getValue());
        object.addProperty("oid", list.getOid());
        object.addProperty("createDate", format.format(list.getCreateDate()));
        object.addProperty("lasteUpdateDate", format.format(list.getLastUpdateDate()));
        object.addProperty("write", list.doesActorHaveWritePermission());
        object.addProperty("read", list.doesActorHaveReadPermission());

        lists.add(object);
      });
    }

    JsonObject object = new JsonObject();
    object.addProperty("oid", org.getOid());
    object.addProperty("code", org.getCode());
    object.addProperty("label", org.getDisplayLabel().getValue());
    object.addProperty("write", Organization.isRegistryAdmin(org) || Organization.isRegistryMaintainer(org));
    object.add("lists", lists);

    response.add(object);

    return response;
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
  }

}
