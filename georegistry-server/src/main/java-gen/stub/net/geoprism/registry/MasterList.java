/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.LocaleSerializer;
import net.geoprism.registry.service.ServiceFactory;

public class MasterList extends MasterListBase
{
  private static final long serialVersionUID = 190790165;

  public static String      LEAF             = "leaf";

  public static String      TYPE_CODE        = "typeCode";

  public static String      ATTRIBUTES       = "attributes";

  public static String      NAME             = "name";

  public static String      LABEL            = "label";

  public static String      VALUE            = "value";

  public static String      TYPE             = "type";

  public static String      BASE             = "base";

  public static String      DEPENDENCY       = "dependency";

  public static String      DEFAULT_LOCALE   = "DefaultLocale";

  public MasterList()
  {
    super();
  }

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
    List<MasterListVersion> versions = this.getVersions();

    for (MasterListVersion version : versions)
    {
      version.delete();
    }

    super.delete();
  }

  public List<MasterListVersion> getVersions()
  {
    MasterListVersionQuery query = new MasterListVersionQuery(new QueryFactory());
    query.WHERE(query.getMasterlist().EQ(this));

    try (OIterator<? extends MasterListVersion> it = query.getIterator())
    {
      return new LinkedList<MasterListVersion>(it.getAll());
    }
  }

  public JsonArray getHierarchiesAsJson()
  {
    if (this.getHierarchies() != null && this.getHierarchies().length() > 0)
    {
      return new JsonParser().parse(this.getHierarchies()).getAsJsonArray();
    }

    return new JsonArray();
  }

  public Map<HierarchyType, List<GeoObjectType>> getAncestorMap(ServerGeoObjectType type)
  {
    Map<HierarchyType, List<GeoObjectType>> map = new HashMap<>();

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();
        HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hCode).get();

        map.put(hierarchyType, ServiceFactory.getUtilities().getAncestors(type, hCode));
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

  public JsonObject data(Integer pageNumber, Integer pageSize, String filterJson, String sort)
  {
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Session.getCurrentLocale());
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    NumberFormat numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());

    JsonArray results = new JsonArray();

    // MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());
    // List<? extends MdAttributeConcreteDAOIF> mdAttributes =
    // mdBusiness.definesAttributes();
    //
    // BusinessQuery query = this.buildQuery(filterJson);
    //
    // if (sort != null && sort.length() > 0)
    // {
    // JsonObject jObject = new JsonParser().parse(sort).getAsJsonObject();
    // String attribute = jObject.get("attribute").getAsString();
    // String order = jObject.get("order").getAsString();
    //
    // if (order.equalsIgnoreCase("DESC"))
    // {
    // query.ORDER_BY_DESC(query.getS(attribute));
    // }
    // else
    // {
    // query.ORDER_BY_ASC(query.getS(attribute));
    // }
    //
    // if (!attribute.equals(DefaultAttribute.CODE.getName()))
    // {
    // query.ORDER_BY_ASC(query.aCharacter(DefaultAttribute.CODE.getName()));
    // }
    // }
    //
    // OIterator<Business> iterator = query.getIterator(pageSize, pageNumber);
    //
    // try
    // {
    // while (iterator.hasNext())
    // {
    // Business row = iterator.next();
    // JsonObject object = new JsonObject();
    //
    // MdAttributeConcreteDAOIF mdGeometry =
    // mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    //
    // if (mdGeometry instanceof MdAttributePointDAOIF)
    // {
    // Point point = (Point) row.getObjectValue(mdGeometry.definesAttribute());
    //
    // if (point != null)
    // {
    // object.addProperty("longitude", point.getX());
    // object.addProperty("latitude", point.getY());
    // }
    // }
    // object.addProperty(ORIGINAL_OID, row.getValue(ORIGINAL_OID));
    //
    // for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    // {
    // if (this.isValid(mdAttribute))
    // {
    // String attributeName = mdAttribute.definesAttribute();
    // Object value = row.getObjectValue(attributeName);
    //
    // if (value != null)
    // {
    //
    // if (value instanceof Double)
    // {
    // object.addProperty(mdAttribute.definesAttribute(),
    // numberFormat.format((Double) value));
    // }
    // else if (value instanceof Number)
    // {
    // object.addProperty(mdAttribute.definesAttribute(), (Number) value);
    // }
    // else if (value instanceof Boolean)
    // {
    // object.addProperty(mdAttribute.definesAttribute(), (Boolean) value);
    // }
    // else if (value instanceof String)
    // {
    // object.addProperty(mdAttribute.definesAttribute(), (String) value);
    // }
    // else if (value instanceof Character)
    // {
    // object.addProperty(mdAttribute.definesAttribute(), (Character) value);
    // }
    // else if (value instanceof Date)
    // {
    // object.addProperty(mdAttribute.definesAttribute(),
    // dateFormat.format((Date) value));
    // }
    // }
    // }
    // }
    //
    // results.add(object);
    // }
    // }
    // finally
    // {
    // iterator.close();
    // }

    JsonObject page = new JsonObject();
    // page.addProperty("pageNumber", pageNumber);
    // page.addProperty("pageSize", pageSize);
    // page.addProperty("filter", filterJson);
    // page.addProperty("count", query.getCount());
    // page.add("results", results);

    return page;
  }

  public BusinessQuery buildQuery(String filterJson)
  {
    // MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());
    //
    // BusinessQuery query = new
    // QueryFactory().businessQuery(mdBusiness.definesType());
    //
    // DateFormat filterFormat = new
    // SimpleDateFormat(GeoObjectConfiguration.DATE_FORMAT);
    // filterFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    //
    // if (filterJson != null && filterJson.length() > 0)
    // {
    // JsonArray filters = new JsonParser().parse(filterJson).getAsJsonArray();
    //
    // for (int i = 0; i < filters.size(); i++)
    // {
    // JsonObject filter = filters.get(i).getAsJsonObject();
    //
    // String attribute = filter.get("attribute").getAsString();
    //
    // if (mdBusiness.definesAttribute(attribute) instanceof
    // MdAttributeMomentDAOIF)
    // {
    // JsonObject jObject = filter.get("value").getAsJsonObject();
    //
    // try
    // {
    // if (jObject.has("start") && !jObject.get("start").isJsonNull())
    // {
    // String date = jObject.get("start").getAsString();
    //
    // if (date.length() > 0)
    // {
    // query.WHERE(query.aDateTime(attribute).GE(filterFormat.parse(date)));
    // }
    // }
    //
    // if (jObject.has("end") && !jObject.get("end").isJsonNull())
    // {
    // String date = jObject.get("end").getAsString();
    //
    // if (date.length() > 0)
    // {
    // query.WHERE(query.aDateTime(attribute).LE(filterFormat.parse(date)));
    // }
    // }
    // }
    // catch (ParseException e)
    // {
    // throw new ProgrammingErrorException(e);
    // }
    // }
    // else
    // {
    // String value = filter.get("value").getAsString();
    //
    // query.WHERE(query.get(attribute).EQ(value));
    // }
    // }
    // }
    // return query;
    return null;
  }

  public JsonArray values(String value, String attributeName, String valueAttribute, String filterJson)
  {
    DateFormat filterFormat = new SimpleDateFormat(GeoObjectConfiguration.DATE_FORMAT);
    filterFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    JsonArray results = new JsonArray();

    // MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());
    //
    // ValueQuery vQuery = new ValueQuery(new QueryFactory());
    //
    // BusinessQuery query = new BusinessQuery(vQuery,
    // mdBusiness.definesType());
    //
    // vQuery.SELECT_DISTINCT(query.get(attributeName, "label"),
    // query.get(valueAttribute, "value"));
    //
    // vQuery.FROM(query);
    //
    // if (filterJson != null && filterJson.length() > 0)
    // {
    // JsonArray filters = new JsonParser().parse(filterJson).getAsJsonArray();
    //
    // for (int i = 0; i < filters.size(); i++)
    // {
    // JsonObject filter = filters.get(i).getAsJsonObject();
    //
    // String attribute = filter.get("attribute").getAsString();
    //
    // if (mdBusiness.definesAttribute(attribute) instanceof
    // MdAttributeMomentDAOIF)
    // {
    // JsonObject jObject = filter.get("value").getAsJsonObject();
    //
    // try
    // {
    // if (jObject.has("start") && !jObject.get("start").isJsonNull())
    // {
    // String date = jObject.get("start").getAsString();
    //
    // if (date.length() > 0)
    // {
    // vQuery.WHERE(query.aDateTime(attribute).GE(filterFormat.parse(date)));
    // }
    // }
    //
    // if (jObject.has("end") && !jObject.get("end").isJsonNull())
    // {
    // String date = jObject.get("end").getAsString();
    //
    // if (date.length() > 0)
    // {
    // vQuery.WHERE(query.aDateTime(attribute).LE(filterFormat.parse(date)));
    // }
    // }
    // }
    // catch (ParseException e)
    // {
    // throw new ProgrammingErrorException(e);
    // }
    // }
    // else
    // {
    // String v = filter.get("value").getAsString();
    //
    // vQuery.WHERE(query.get(attribute).EQ(v));
    // }
    // }
    // }
    //
    // if (value != null && value.length() > 0)
    // {
    // vQuery.WHERE(query.aCharacter(attributeName).LIKEi("%" + value + "%"));
    // }
    //
    // vQuery.ORDER_BY_ASC(query.get(attributeName));
    //
    // OIterator<ValueObject> it = vQuery.getIterator(100, 1);
    //
    // try
    // {
    // while (it.hasNext())
    // {
    // ValueObject vObject = it.next();
    //
    // JsonObject result = new JsonObject();
    // result.addProperty("label", vObject.getValue("label"));
    // result.addProperty("value", vObject.getValue("value"));
    //
    // results.add(result);
    // }
    // }
    // finally
    // {
    // it.close();
    // }

    return results;
  }

  public JsonObject toJSON()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    Locale locale = Session.getCurrentLocale();
    LocaleSerializer serializer = new LocaleSerializer(locale);

    ServerGeoObjectType type = ServerGeoObjectType.get(this.getUniversal());

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(MasterList.OID, this.getOid());
    }

    object.addProperty(MasterList.TYPE_CODE, type.getCode());
    object.addProperty(MasterList.LEAF, type.isLeaf());
    object.add(MasterList.DISPLAYLABEL, LocalizedValueConverter.convert(this.getDisplayLabel()).toJSON(serializer));
    object.addProperty(MasterList.CODE, this.getCode());
    object.addProperty(MasterList.LISTABSTRACT, this.getListAbstract());
    object.addProperty(MasterList.PROCESS, this.getProcess());
    object.addProperty(MasterList.PROGRESS, this.getProgress());
    object.addProperty(MasterList.ACCESSCONSTRAINTS, this.getAccessConstraints());
    object.addProperty(MasterList.USECONSTRAINTS, this.getUseConstraints());
    object.addProperty(MasterList.ACKNOWLEDGEMENTS, this.getAcknowledgements());
    object.addProperty(MasterList.DISCLAIMER, this.getDisclaimer());
    object.addProperty(MasterList.CONTACTNAME, this.getContactName());
    object.addProperty(MasterList.ORGANIZATION, this.getOrganization());
    object.addProperty(MasterList.TELEPHONENUMBER, this.getTelephoneNumber());
    object.addProperty(MasterList.EMAIL, this.getEmail());
    object.add(MasterList.HIERARCHIES, this.getHierarchiesAsJson());

    if (this.getRepresentativityDate() != null)
    {
      object.addProperty(MasterList.REPRESENTATIVITYDATE, format.format(this.getRepresentativityDate()));
    }

    if (this.getPublishDate() != null)
    {
      object.addProperty(MasterList.PUBLISHDATE, format.format(this.getPublishDate()));
    }

    return object;
  }

  private void createMdAttributeFromAttributeType(ServerGeoObjectType type, AttributeType attributeType, List<Locale> locales)
  {
    List<MasterListVersion> versions = this.getVersions();

    for (MasterListVersion version : versions)
    {
      version.createMdAttributeFromAttributeType(type, attributeType, locales);
    }
  }

  private void removeAttributeType(AttributeType attributeType)
  {
    List<MasterListVersion> versions = this.getVersions();

    for (MasterListVersion version : versions)
    {
      version.removeAttributeType(attributeType);
    }
  }

  public MasterListVersion getOrCreateVersion(Date forDate)
  {
    MasterListVersionQuery query = new MasterListVersionQuery(new QueryFactory());
    query.WHERE(query.getMasterlist().EQ(this));
    query.AND(query.getForDate().EQ(forDate));

    try (OIterator<? extends MasterListVersion> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return MasterListVersion.create(this, forDate);
  }

  public static MasterList fromJSON(JsonObject object)
  {
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

      String typeCode = object.get(MasterList.TYPE_CODE).getAsString();
      ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

      LocalizedValue label = LocalizedValue.fromJSON(object.get(MasterList.DISPLAYLABEL).getAsJsonObject());

      MasterList list = null;

      if (object.has("oid") && !object.get("oid").isJsonNull())
      {
        String oid = object.get("oid").getAsString();

        list = MasterList.lock(oid);
      }
      else
      {
        list = new MasterList();
      }

      list.setUniversal(type.getUniversal());
      LocalizedValueConverter.populate(list.getDisplayLabel(), label);
      list.setCode(object.get(MasterList.CODE).getAsString());
      list.setListAbstract(object.get(MasterList.LISTABSTRACT).getAsString());
      list.setProcess(object.get(MasterList.PROCESS).getAsString());
      list.setProgress(object.get(MasterList.PROGRESS).getAsString());
      list.setAccessConstraints(object.get(MasterList.ACCESSCONSTRAINTS).getAsString());
      list.setUseConstraints(object.get(MasterList.USECONSTRAINTS).getAsString());
      list.setAcknowledgements(object.get(MasterList.ACKNOWLEDGEMENTS).getAsString());
      list.setDisclaimer(object.get(MasterList.DISCLAIMER).getAsString());
      list.setContactName(object.get(MasterList.CONTACTNAME).getAsString());
      list.setOrganization(object.get(MasterList.ORGANIZATION).getAsString());
      list.setTelephoneNumber(object.get(MasterList.TELEPHONENUMBER).getAsString());
      list.setEmail(object.get(MasterList.EMAIL).getAsString());
      list.setHierarchies(object.get(MasterList.HIERARCHIES).getAsJsonArray().toString());

      if (object.has(MasterList.REPRESENTATIVITYDATE))
      {
        if (!object.get(MasterList.REPRESENTATIVITYDATE).isJsonNull())
        {
          String date = object.get(MasterList.REPRESENTATIVITYDATE).getAsString();

          if (date.length() > 0)
          {
            list.setRepresentativityDate(format.parse(date));
          }
          else
          {
            list.setRepresentativityDate(null);
          }
        }
        else
        {
          list.setRepresentativityDate(null);
        }
      }

      if (object.has(MasterList.PUBLISHDATE))
      {
        if (!object.get(MasterList.PUBLISHDATE).isJsonNull())
        {
          String date = object.get(MasterList.PUBLISHDATE).getAsString();

          if (date.length() > 0)
          {
            list.setPublishDate(format.parse(date));
          }
          else
          {
            list.setPublishDate(null);
          }
        }
        else
        {
          list.setPublishDate(null);
        }
      }

      return list;
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  public static MasterList create(JsonObject object)
  {
    MasterList list = MasterList.fromJSON(object);
    list.apply();

    return list;
  }

  @Transaction
  public static void deleteAll(Universal universal)
  {
    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    List<? extends MasterList> lists = query.getIterator().getAll();

    for (MasterList list : lists)
    {
      list.delete();
    }
  }

  public static void createMdAttribute(ServerGeoObjectType type, AttributeType attributeType)
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(type.getUniversal()));

    List<? extends MasterList> lists = query.getIterator().getAll();

    for (MasterList list : lists)
    {
      list.createMdAttributeFromAttributeType(type, attributeType, locales);
    }
  }

  public static void deleteMdAttribute(Universal universal, AttributeType attributeType)
  {
    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    List<? extends MasterList> lists = query.getIterator().getAll();

    for (MasterList list : lists)
    {
      list.removeAttributeType(attributeType);
    }
  }

  public static boolean isValidName(String name)
  {
    if (name.contains(" ") || name.contains("<") || name.contains(">") || name.contains("-") || name.contains("+") || name.contains("=") || name.contains("!") || name.contains("@") || name.contains("#") || name.contains("$") || name.contains("%") || name.contains("^") || name.contains("&") || name.contains("*") || name.contains("?") || name.contains(";") || name.contains(":") || name.contains(",") || name.contains("^") || name.contains("{") || name.contains("}") || name.contains("]") || name.contains("[") || name.contains("`") || name.contains("~") || name.contains("|") || name.contains("/") || name.contains("\\"))
    {
      return false;
    }

    return true;
  }
}
