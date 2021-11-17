package net.geoprism.registry;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.model.ServerGeoObjectType;

public class ListTypeEntry extends ListTypeEntryBase implements LabeledVersion
{
  private static final long serialVersionUID = 1112663869;

  public static String      VERSIONS         = "versions";

  public ListTypeEntry()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    // Delete all versions
    this.getVersions().forEach(version -> version.delete());

    super.delete();
  }

  public JsonObject toJSON()
  {
    ListType listType = this.getListType();

    ServerGeoObjectType type = ServerGeoObjectType.get(listType.getUniversal());

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(ListTypeVersion.OID, this.getOid());
    }

    object.addProperty(ListType.DISPLAYLABEL, listType.getDisplayLabel().getValue());
    object.addProperty(ListTypeVersion.TYPE_CODE, type.getCode());
    object.addProperty(ListTypeVersion.ORG_CODE, type.getOrganization().getCode());
    object.addProperty(ListTypeVersion.LISTTYPE, listType.getOid());
    object.addProperty(ListTypeVersion.FORDATE, GeoRegistryUtil.formatDate(this.getForDate(), false));
    object.addProperty(ListTypeVersion.CREATEDATE, GeoRegistryUtil.formatDate(this.getCreateDate(), false));
    object.addProperty(ListTypeVersion.PERIOD, listType.formatVersionLabel(this));
    object.addProperty(ListTypeVersion.WORKING, this.getWorkingOid());

    List<ListTypeVersion> versions = this.getVersions();

    JsonArray jVersions = new JsonArray();

    for (ListTypeVersion entry : versions)
    {
      jVersions.add(entry.toJSON(false));
    }

    object.add(ListTypeEntry.VERSIONS, jVersions);

    return object;
  }

  public List<ListTypeVersion> getVersions()
  {
    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());
    query.WHERE(query.getEntry().EQ(this));
    query.AND(query.getWorking().EQ(false));
    query.ORDER_BY_DESC(query.getVersionNumber());

    try (OIterator<? extends ListTypeVersion> it = query.getIterator())
    {
      return new LinkedList<ListTypeVersion>(it.getAll());
    }
  }

  public JsonArray getVersionJson()
  {
    List<ListTypeVersion> versions = this.getVersions();

    JsonArray jVersions = new JsonArray();

    for (ListTypeVersion entry : versions)
    {
      jVersions.add(entry.toJSON(false));
    }

    return jVersions;
  }

  // @Transaction
  // @Authenticate
  private ListTypeVersion createVersion(JsonObject metadata)
  {
    List<ListTypeVersion> versions = this.getVersions();

    int versionNumber = versions.size() > 0 ? versions.get(0).getVersionNumber() + 1 : 1;

    return ListTypeVersion.create(this, false, versionNumber, metadata);
  }

  @Override
  @Transaction
  @Authenticate
  public String publish(String metadata)
  {
    // Create a new version and publish it
    ListTypeVersion version = this.createVersion(JsonParser.parseString(metadata).getAsJsonObject());

    return version.publish();
  }

  @Transaction
  public static ListTypeEntry create(ListType list, Date forDate)
  {
    ListTypeEntry entry = new ListTypeEntry();
    entry.setListType(list);
    entry.setForDate(forDate);
    entry.apply();

    entry.setWorking(ListTypeVersion.create(entry, true, 0, null));
    entry.apply();

    return entry;
  }

}