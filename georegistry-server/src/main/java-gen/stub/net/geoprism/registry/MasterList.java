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
package net.geoprism.registry;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
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
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.BusinessInfo;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdTableInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMomentDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeUUIDDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.MdAttributePointDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.metadata.MdAttributeGeometry;
import com.runwaysdk.system.gis.metadata.MdAttributeLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPoint;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPolygon;
import com.runwaysdk.system.gis.metadata.MdAttributePoint;
import com.runwaysdk.system.gis.metadata.MdAttributePolygon;
import com.runwaysdk.system.metadata.MdAttribute;
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeDouble;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeLong;
import com.runwaysdk.system.metadata.MdBusiness;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.DefaultConfiguration;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.masterlist.MasterListAttributeComparator;
import net.geoprism.registry.masterlist.TableMetadata;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.GeoObjectIterator;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.LocaleSerializer;
import net.geoprism.registry.service.ServiceFactory;

public class MasterList extends MasterListBase
{
  private static final long serialVersionUID = 190790165;

  public static String      LEAF             = "leaf";

  public static String      ORIGINAL_OID     = "originalOid";

  public static String      TYPE_CODE        = "typeCode";

  public static String      ATTRIBUTES       = "attributes";

  public static String      PREFIX           = "ml_";

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
    MasterListAttributeGroup.deleteAll(this);

    MdBusiness mdTable = this.getMdBusiness();

    super.delete();

    if (mdTable != null)
    {
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      mdTable.delete();
    }
  }

  @Transaction
  public JsonObject publish()
  {
    this.lock();

    try
    {
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

      Universal universal = this.getUniversal();
      GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

      // Add the type ancestor fields
      Map<HierarchyType, List<GeoObjectType>> ancestorMap = this.getAncestorMap(type);
      Collection<AttributeType> attributes = type.getAttributeMap().values();

      GeoObjectQuery query = new GeoObjectQuery(type, universal);

      Long count = query.getCount();
      long current = 0;

      ProgressService.put(this.getOid(), new Progress(0L, count, ""));

      try
      {

        GeoObjectIterator objects = query.getIterator();

        try
        {
          while (objects.hasNext())
          {
            Business business = new Business(mdBusiness.definesType());

            GeoObject object = objects.next();

            publish(object, business, attributes, ancestorMap, locales, objects.currentOid());

            ProgressService.put(this.getOid(), new Progress(current++, count, ""));
          }
        }
        finally
        {
          objects.close();
        }

        this.setPublishDate(new Date());
        this.apply();

        return this.toJSON();
      }
      finally
      {
        ProgressService.remove(this.getOid());
      }
    }
    finally
    {
      this.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void publish(GeoObject object, Business business, Collection<AttributeType> attributes, Map<HierarchyType, List<GeoObjectType>> ancestorMap, List<Locale> locales, String runwayId)
  {
    business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, object.getGeometry());

    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();

      business.setValue(ORIGINAL_OID, runwayId);

      if (this.isValid(attribute))
      {
        Object value = object.getValue(name);

        if (value != null)
        {

          if (attribute instanceof AttributeTermType)
          {
            Iterator<String> codes = (Iterator<String>) value;

            if (codes.hasNext())
            {
              String code = codes.next();

              Term term = ( (AttributeTermType) attribute ).getTermByCode(code).get();
              LocalizedValue label = term.getLabel();

              this.setValue(business, name, term.getCode());
              this.setValue(business, name + DEFAULT_LOCALE, label.getValue(LocalizedValue.DEFAULT_LOCALE));

              for (Locale locale : locales)
              {
                this.setValue(business, name + locale.toString(), label.getValue(locale));
              }
            }
          }
          else if (attribute instanceof AttributeLocalType)
          {
            LocalizedValue label = (LocalizedValue) value;

            this.setValue(business, name + DEFAULT_LOCALE, label.getValue(LocalizedValue.DEFAULT_LOCALE));

            for (Locale locale : locales)
            {
              this.setValue(business, name + locale.toString(), label.getValue(locale));
            }
          }
          else
          {
            this.setValue(business, name, value);
          }
        }
      }
    }

    Set<Entry<HierarchyType, List<GeoObjectType>>> entries = ancestorMap.entrySet();

    for (Entry<HierarchyType, List<GeoObjectType>> entry : entries)
    {
      HierarchyType hierarchy = entry.getKey();
      // List<GeoObjectType> parents = entry.getValue();
      Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchy);

      Set<Entry<String, ValueObject>> locations = map.entrySet();

      for (Entry<String, ValueObject> location : locations)
      {
        String pCode = location.getKey();
        ValueObject vObject = location.getValue();

        if (vObject != null)
        {
          String attributeName = hierarchy.getCode().toLowerCase() + pCode.toLowerCase();

          this.setValue(business, attributeName, vObject.getValue(GeoEntity.GEOID));
          this.setValue(business, attributeName + DEFAULT_LOCALE, vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName()));

          for (Locale locale : locales)
          {
            this.setValue(business, attributeName + locale.toString(), vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
          }
        }
      }
    }

    business.apply();
  }

  private void setValue(Business business, String name, Object value)
  {
    if (business.hasAttribute(name))
    {
      business.setValue(name, value);
    }
  }

  @Transaction
  public void updateRecord(GeoObject object)
  {
    MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Universal universal = this.getUniversal();
    GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);
    String runwayId = ServiceFactory.getIdService().registryIdToRunwayId(object.getUid(), type);

    // Add the type ancestor fields
    Map<HierarchyType, List<GeoObjectType>> ancestorMap = this.getAncestorMap(type);
    Collection<AttributeType> attributes = type.getAttributeMap().values();

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
    query.WHERE(query.aCharacter(DefaultAttribute.CODE.getName()).EQ(object.getCode()));

    List<Business> records = query.getIterator().getAll();

    for (Business record : records)
    {
      record.appLock();
      try
      {
        this.publish(object, record, attributes, ancestorMap, locales, runwayId);
      }
      finally
      {
        record.unlock();
      }
    }
  }

  @Transaction
  public void publishRecord(GeoObject object)
  {
    MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Universal universal = this.getUniversal();
    GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);
    String runwayId = ServiceFactory.getIdService().registryIdToRunwayId(object.getUid(), type);

    // Add the type ancestor fields
    Map<HierarchyType, List<GeoObjectType>> ancestorMap = this.getAncestorMap(type);
    Collection<AttributeType> attributes = type.getAttributeMap().values();

    Business business = new Business(mdBusiness.definesType());

    this.publish(object, business, attributes, ancestorMap, locales, runwayId);
  }

  private TableMetadata createTable()
  {
    TableMetadata metadata = new TableMetadata();

    Locale currentLocale = Session.getCurrentLocale();

    String viewName = this.getTableName();

    // Create the MdTable
    MdBusinessDAO mdTableDAO = MdBusinessDAO.newInstance();
    mdTableDAO.setValue(MdTableInfo.NAME, viewName);
    mdTableDAO.setValue(MdTableInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdTableDAO.setStructValue(MdTableInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, this.getDisplayLabel().getValue());
    mdTableDAO.setValue(MdTableInfo.TABLE_NAME, viewName);
    mdTableDAO.setValue(MdTableInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.apply();

    MdBusiness mdBusiness = (MdBusiness) BusinessFacade.get(mdTableDAO);

    MdAttributeUUIDDAO mdAttributeOriginalId = MdAttributeUUIDDAO.newInstance();
    mdAttributeOriginalId.setValue(MdAttributeCharacterInfo.NAME, ORIGINAL_OID);
    mdAttributeOriginalId.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
    mdAttributeOriginalId.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Original oid");
    mdAttributeOriginalId.apply();

    metadata.setMdBusiness(mdBusiness);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Universal universal = this.getUniversal();
    GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

    this.createMdAttributeFromAttributeType(mdBusiness, type.getGeometryType());

    Collection<AttributeType> attributeTypes = type.getAttributeMap().values();

    for (AttributeType attributeType : attributeTypes)
    {
      if (this.isValid(attributeType))
      {
        this.createMdAttributeFromAttributeType(metadata, attributeType, type, locales);
      }
    }

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();

        HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hCode).get();
        String hierarchyLabel = hierarchyType.getLabel().getValue(currentLocale);

        for (String pCode : pCodes)
        {
          GeoObjectType got = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(pCode).get();
          String typeLabel = got.getLabel().getValue(currentLocale);
          String attributeName = hCode.toLowerCase() + pCode.toLowerCase();
          String label = typeLabel + " (" + hierarchyLabel + ")";

          String codeDescription = LocalizationFacade.getFromBundles("masterlist.code.description");
          codeDescription = codeDescription.replaceAll("\\{typeLabel\\}", typeLabel);
          codeDescription = codeDescription.replaceAll("\\{hierarchyLabel\\}", hierarchyLabel);

          String labelDescription = LocalizationFacade.getFromBundles("masterlist.label.description");
          labelDescription = labelDescription.replaceAll("\\{typeLabel\\}", typeLabel);
          labelDescription = labelDescription.replaceAll("\\{hierarchyLabel\\}", hierarchyLabel);

          MdAttributeCharacterDAO mdAttributeCode = MdAttributeCharacterDAO.newInstance();
          mdAttributeCode.setValue(MdAttributeCharacterInfo.NAME, attributeName);
          mdAttributeCode.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeCode.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeCode.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label);
          mdAttributeCode.addItem(MdAttributeCharacterInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
          mdAttributeCode.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, codeDescription);
          mdAttributeCode.apply();

          MdAttributeCharacterDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + DEFAULT_LOCALE);
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (defaultLocale)");
          mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", "default"));
          mdAttributeDefaultLocale.apply();

          for (Locale locale : locales)
          {
            MdAttributeCharacterDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + locale.toString());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
            mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (" + locale + ")");
            mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", locale.toString()));
            mdAttributeLocale.apply();
          }

          // MdAttributeUUIDDAO mdAttributeOid =
          // MdAttributeUUIDDAO.newInstance();
          // mdAttributeOid.setValue(MdAttributeUUIDInfo.NAME, attributeName +
          // "Oid");
          // mdAttributeOid.setValue(MdAttributeUUIDInfo.DEFINING_MD_CLASS,
          // mdTableDAO.getOid());
          // mdAttributeOid.setStructValue(MdAttributeUUIDInfo.DISPLAY_LABEL,
          // MdAttributeLocalInfo.DEFAULT_LOCALE, label);
          // mdAttributeOid.apply();
        }
      }
    }

    return metadata;
  }

  public void removeAttributeType(TableMetadata metadata, AttributeType attributeType)
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(metadata.getMdBusiness().getOid());

    if (! ( attributeType instanceof AttributeTermType || attributeType instanceof AttributeLocalType ))
    {
      removeAttribute(mdBusiness, attributeType.getName());
    }
    else if (attributeType instanceof AttributeTermType)
    {
      removeAttribute(mdBusiness, attributeType.getName());
      removeAttribute(mdBusiness, attributeType.getName() + DEFAULT_LOCALE);

      for (Locale locale : locales)
      {
        removeAttribute(mdBusiness, attributeType.getName() + locale.toString());
      }
    }
    else if (attributeType instanceof AttributeLocalType)
    {
      removeAttribute(mdBusiness, attributeType.getName() + DEFAULT_LOCALE);

      for (Locale locale : locales)
      {
        removeAttribute(mdBusiness, attributeType.getName() + locale.toString());
      }
    }

  }

  private void removeAttribute(MdBusinessDAOIF mdBusiness, String name)
  {
    MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(name);

    if (mdAttribute != null)
    {
      MasterListAttributeGroup.remove(mdAttribute);

      mdAttribute.getBusinessDAO().delete();
    }
  }

  public void createMdAttributeFromAttributeType(TableMetadata metadata, AttributeType attributeType, GeoObjectType type, List<Locale> locales)
  {
    MdBusiness mdBusiness = metadata.getMdBusiness();

    if (! ( attributeType instanceof AttributeTermType || attributeType instanceof AttributeLocalType ))
    {
      MdAttributeConcrete mdAttribute = null;

      if (attributeType.getType().equals(AttributeCharacterType.TYPE))
      {
        mdAttribute = new MdAttributeCharacter();
        MdAttributeCharacter mdAttributeCharacter = (MdAttributeCharacter) mdAttribute;
        mdAttributeCharacter.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
      }
      else if (attributeType.getType().equals(AttributeDateType.TYPE))
      {
        mdAttribute = new MdAttributeDateTime();
      }
      else if (attributeType.getType().equals(AttributeIntegerType.TYPE))
      {
        mdAttribute = new MdAttributeLong();
      }
      else if (attributeType.getType().equals(AttributeFloatType.TYPE))
      {
        AttributeFloatType attributeFloatType = (AttributeFloatType) attributeType;

        mdAttribute = new MdAttributeDouble();
        mdAttribute.setValue(MdAttributeDoubleInfo.LENGTH, Integer.toString(attributeFloatType.getPrecision()));
        mdAttribute.setValue(MdAttributeDoubleInfo.DECIMAL, Integer.toString(attributeFloatType.getScale()));
      }
      else if (attributeType.getType().equals(AttributeBooleanType.TYPE))
      {
        mdAttribute = new MdAttributeBoolean();
      }
      else
      {
        throw new UnsupportedOperationException("Unsupported type [" + attributeType.getType() + "]");
      }

      mdAttribute.setAttributeName(attributeType.getName());

      ServiceFactory.getConversionService().populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
      ServiceFactory.getConversionService().populate(mdAttribute.getDescription(), attributeType.getDescription());

      mdAttribute.setDefiningMdClass(mdBusiness);
      mdAttribute.apply();
    }
    else if (attributeType instanceof AttributeTermType)
    {
      MdAttributeCharacter cloneAttribute = new MdAttributeCharacter();
      cloneAttribute.setValue(MdAttributeConcreteInfo.NAME, attributeType.getName());
      cloneAttribute.setValue(MdAttributeCharacterInfo.SIZE, "255");
      cloneAttribute.addIndexType(MdAttributeIndices.NON_UNIQUE_INDEX);
      ServiceFactory.getConversionService().populate(cloneAttribute.getDisplayLabel(), attributeType.getLabel());
      ServiceFactory.getConversionService().populate(cloneAttribute.getDescription(), attributeType.getDescription());
      cloneAttribute.setDefiningMdClass(mdBusiness);
      cloneAttribute.apply();

      metadata.addPair(cloneAttribute, cloneAttribute);

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + DEFAULT_LOCALE);
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDisplayLabel(), attributeType.getLabel(), " (defaultLocale)");
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription(), " (defaultLocale)");
      mdAttributeDefaultLocale.apply();

      metadata.addPair(mdAttributeDefaultLocale, cloneAttribute);

      for (Locale locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDisplayLabel(), attributeType.getLabel(), " (" + locale.toString() + ")");
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();

        metadata.addPair(mdAttributeLocale, cloneAttribute);
      }

      // MdAttributeUUID mdAttributeOid = new MdAttributeUUID();
      // mdAttributeOid.setValue(MdAttributeConcreteInfo.NAME,
      // attributeType.getName() + "Oid");
      // ServiceFactory.getConversionService().populate(mdAttributeOid.getDisplayLabel(),
      // attributeType.getLabel());
      // ServiceFactory.getConversionService().populate(mdAttributeOid.getDescription(),
      // attributeType.getDescription());
      // mdAttributeOid.setDefiningMdClass(mdBusiness);
      // mdAttributeOid.apply();
    }
    else if (attributeType instanceof AttributeLocalType)
    {
      boolean isDisplayLabel = attributeType.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName());

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + DEFAULT_LOCALE);
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (defaultLocale)");
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription(), " (defaultLocale)");
      mdAttributeDefaultLocale.apply();

      for (Locale locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (" + locale.toString() + ")");
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();
      }
    }
  }

  public void createMdAttributeFromAttributeType(MdBusiness mdBusiness, GeometryType attributeType)
  {
    MdAttributeGeometry mdAttribute = null;

    if (attributeType.equals(GeometryType.POINT))
    {
      mdAttribute = new MdAttributePoint();
    }
    else if (attributeType.equals(GeometryType.MULTIPOINT))
    {
      mdAttribute = new MdAttributeMultiPoint();
    }
    else if (attributeType.equals(GeometryType.LINE))
    {
      mdAttribute = new MdAttributeLineString();
    }
    else if (attributeType.equals(GeometryType.MULTILINE))
    {
      mdAttribute = new MdAttributeMultiLineString();
    }
    else if (attributeType.equals(GeometryType.POLYGON))
    {
      mdAttribute = new MdAttributePolygon();
    }
    else if (attributeType.equals(GeometryType.MULTIPOLYGON))
    {
      mdAttribute = new MdAttributeMultiPolygon();
    }

    mdAttribute.setAttributeName(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    mdAttribute.getDisplayLabel().setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    mdAttribute.setDefiningMdClass(mdBusiness);
    mdAttribute.setSrid(4326);
    mdAttribute.apply();
  }

  public JsonArray getHierarchiesAsJson()
  {
    if (this.getHierarchies() != null && this.getHierarchies().length() > 0)
    {
      return new JsonParser().parse(this.getHierarchies()).getAsJsonArray();
    }

    return new JsonArray();
  }

  private JsonArray getAttributesAsJson()
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Map<String, JsonArray> dependencies = new HashMap<String, JsonArray>();
    Map<String, String> baseAttributes = new HashMap<String, String>();
    List<String> attributesOrder = new LinkedList<String>();

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get(DefaultAttribute.CODE.getName()).getAsString();

        String previous = null;

        for (String pCode : pCodes)
        {
          String attributeName = hCode.toLowerCase() + pCode.toLowerCase();

          baseAttributes.put(attributeName, attributeName);
          baseAttributes.put(attributeName + DEFAULT_LOCALE, attributeName);

          for (Locale locale : locales)
          {
            baseAttributes.put(attributeName + locale.toString(), attributeName);
          }

          attributesOrder.add(attributeName);
          attributesOrder.add(attributeName + DEFAULT_LOCALE);

          for (Locale locale : locales)
          {
            attributesOrder.add(attributeName + locale.toString());
          }

          if (previous != null)
          {
            addDependency(dependencies, attributeName, previous);
            addDependency(dependencies, attributeName + DEFAULT_LOCALE, previous);

            for (Locale locale : locales)
            {
              addDependency(dependencies, attributeName + locale.toString(), previous);
            }
          }

          previous = attributeName;
        }

        if (previous != null)
        {
          addDependency(dependencies, DefaultAttribute.CODE.getName(), previous);
          addDependency(dependencies, DefaultAttribute.DISPLAY_LABEL.getName() + DEFAULT_LOCALE, previous);

          for (Locale locale : locales)
          {
            addDependency(dependencies, DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString(), previous);
          }
        }
      }
    }

    baseAttributes.put(DefaultAttribute.CODE.getName(), DefaultAttribute.CODE.getName());
    baseAttributes.put(DefaultAttribute.DISPLAY_LABEL.getName() + DEFAULT_LOCALE, DefaultAttribute.CODE.getName());

    for (Locale locale : locales)
    {
      baseAttributes.put(DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString(), DefaultAttribute.CODE.getName());
    }

    attributesOrder.add(DefaultAttribute.CODE.getName());
    attributesOrder.add(DefaultAttribute.DISPLAY_LABEL.getName() + DEFAULT_LOCALE);

    for (Locale locale : locales)
    {
      attributesOrder.add(DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString());
    }

    JsonArray attributes = new JsonArray();
    String mdBusinessId = this.getMdBusinessOid();

    if (mdBusinessId != null && mdBusinessId.length() > 0)
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(mdBusinessId);
      List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered();

      Collections.sort(mdAttributes, new MasterListAttributeComparator(attributesOrder, mdAttributes));

      MdAttributeConcreteDAOIF mdGeometry = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

      if (mdGeometry instanceof MdAttributePointDAOIF)
      {
        JsonObject longitude = new JsonObject();
        longitude.addProperty(NAME, "longitude");
        longitude.addProperty(LABEL, LocalizationFacade.getFromBundles(GeoObjectConfiguration.LONGITUDE_KEY));
        longitude.addProperty(TYPE, "none");

        attributes.add(longitude);

        JsonObject latitude = new JsonObject();
        latitude.addProperty(NAME, "latitude");
        latitude.addProperty(LABEL, LocalizationFacade.getFromBundles(GeoObjectConfiguration.LATITUDE_KEY));
        latitude.addProperty(TYPE, "none");

        attributes.add(latitude);
      }

      for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
      {
        if (this.isValid(mdAttribute))
        {
          String attributeName = mdAttribute.definesAttribute();

          try
          {
            MasterListAttributeGroup group = MasterListAttributeGroup.getByKey(mdAttribute.getOid());

            if (group != null)
            {
              baseAttributes.put(mdAttribute.definesAttribute(), group.getSourceAttribute().getAttributeName());
            }
          }
          catch (DataNotFoundException e)
          {
            // Ignore
          }

          JsonObject attribute = new JsonObject();
          attribute.addProperty(NAME, attributeName);
          attribute.addProperty(LABEL, mdAttribute.getDisplayLabel(Session.getCurrentLocale()));
          attribute.addProperty(TYPE, baseAttributes.containsKey(attributeName) ? "list" : "input");
          attribute.addProperty(BASE, baseAttributes.containsKey(attributeName) ? baseAttributes.get(attributeName) : attributeName);
          attribute.add(DEPENDENCY, dependencies.containsKey(attributeName) ? dependencies.get(attributeName) : new JsonArray());

          if (mdAttribute instanceof MdAttributeMomentDAOIF)
          {
            attribute.addProperty(TYPE, "date");
            attribute.add(VALUE, new JsonObject());
          }

          attributes.add(attribute);
        }
      }
    }

    return attributes;
  }

  private void addDependency(Map<String, JsonArray> dependencies, String attributeName, String dependency)
  {
    if (!dependencies.containsKey(attributeName))
    {
      dependencies.put(attributeName, new JsonArray());
    }

    dependencies.get(attributeName).add(dependency);
  }

  private Map<HierarchyType, List<GeoObjectType>> getAncestorMap(GeoObjectType type)
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

  private List<String> getParentCodes(JsonObject hierarchy)
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

  private boolean isValid(AttributeType attributeType)
  {
    if (attributeType.getName().equals(DefaultAttribute.UID.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.STATUS.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    return true;
  }

  public boolean isValid(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() || mdAttribute.definesAttribute().equals(DefaultAttribute.UID.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.STATUS.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(ORIGINAL_OID))
    {
      return false;
    }

    // if (mdAttribute.definesAttribute().endsWith("Oid"))
    // {
    // return false;
    // }

    if (mdAttribute.definesAttribute().equals(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.OWNER))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.KEY))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.DOMAIN))
    {
      return false;
    }

    return true;
  }

  private String getTableName()
  {
    int count = 0;

    String name = PREFIX + count + this.getUniversal().getMdBusiness().getTableName();

    while (Database.tableExists(name))
    {
      count++;

      name = PREFIX + count + this.getUniversal().getMdBusiness().getTableName();
    }

    return name;
  }

  public JsonObject data(Integer pageNumber, Integer pageSize, String filterJson, String sort)
  {
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Session.getCurrentLocale());
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    NumberFormat numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());

    JsonArray results = new JsonArray();

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

    BusinessQuery query = this.buildQuery(filterJson);

    if (sort != null && sort.length() > 0)
    {
      JsonObject jObject = new JsonParser().parse(sort).getAsJsonObject();
      String attribute = jObject.get("attribute").getAsString();
      String order = jObject.get("order").getAsString();

      if (order.equalsIgnoreCase("DESC"))
      {
        query.ORDER_BY_DESC(query.getS(attribute));
      }
      else
      {
        query.ORDER_BY_ASC(query.getS(attribute));
      }

      if (!attribute.equals(DefaultAttribute.CODE.getName()))
      {
        query.ORDER_BY_ASC(query.aCharacter(DefaultAttribute.CODE.getName()));
      }
    }

    OIterator<Business> iterator = query.getIterator(pageSize, pageNumber);

    try
    {
      while (iterator.hasNext())
      {
        Business row = iterator.next();
        JsonObject object = new JsonObject();

        MdAttributeConcreteDAOIF mdGeometry = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

        if (mdGeometry instanceof MdAttributePointDAOIF)
        {
          Point point = (Point) row.getObjectValue(mdGeometry.definesAttribute());

          if (point != null)
          {
            object.addProperty("longitude", point.getX());
            object.addProperty("latitude", point.getY());
          }
        }
        object.addProperty(ORIGINAL_OID, row.getValue(ORIGINAL_OID));

        for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
        {
          if (this.isValid(mdAttribute))
          {
            String attributeName = mdAttribute.definesAttribute();
            Object value = row.getObjectValue(attributeName);

            if (value != null)
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
                object.addProperty(mdAttribute.definesAttribute(), dateFormat.format((Date) value));
              }
            }
          }
        }

        results.add(object);
      }
    }
    finally
    {
      iterator.close();
    }

    JsonObject page = new JsonObject();
    page.addProperty("pageNumber", pageNumber);
    page.addProperty("pageSize", pageSize);
    page.addProperty("filter", filterJson);
    page.addProperty("count", query.getCount());
    page.add("results", results);

    return page;
  }

  public BusinessQuery buildQuery(String filterJson)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());

    DateFormat filterFormat = new SimpleDateFormat("YYYY-MM-DD");
    filterFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    if (filterJson != null && filterJson.length() > 0)
    {
      JsonArray filters = new JsonParser().parse(filterJson).getAsJsonArray();

      for (int i = 0; i < filters.size(); i++)
      {
        JsonObject filter = filters.get(i).getAsJsonObject();

        String attribute = filter.get("attribute").getAsString();

        if (mdBusiness.definesAttribute(attribute) instanceof MdAttributeMomentDAOIF)
        {
          JsonObject jObject = filter.get("value").getAsJsonObject();

          try
          {
            if (jObject.has("start") && !jObject.get("start").isJsonNull())
            {
              String date = jObject.get("start").getAsString();

              if (date.length() > 0)
              {
                query.WHERE(query.aDateTime(attribute).GE(filterFormat.parse(date)));
              }
            }

            if (jObject.has("end") && !jObject.get("end").isJsonNull())
            {
              String date = jObject.get("end").getAsString();

              if (date.length() > 0)
              {
                query.WHERE(query.aDateTime(attribute).LE(filterFormat.parse(date)));
              }
            }
          }
          catch (ParseException e)
          {
            throw new ProgrammingErrorException(e);
          }
        }
        else
        {
          String value = filter.get("value").getAsString();

          query.WHERE(query.get(attribute).EQ(value));
        }
      }
    }
    return query;
  }

  public JsonArray values(String value, String attributeName, String valueAttribute, String filterJson)
  {
    DateFormat filterFormat = new SimpleDateFormat("YYYY-MM-DD");
    filterFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    JsonArray results = new JsonArray();

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    ValueQuery vQuery = new ValueQuery(new QueryFactory());

    BusinessQuery query = new BusinessQuery(vQuery, mdBusiness.definesType());

    vQuery.SELECT_DISTINCT(query.get(attributeName, "label"), query.get(valueAttribute, "value"));

    vQuery.FROM(query);

    if (filterJson != null && filterJson.length() > 0)
    {
      JsonArray filters = new JsonParser().parse(filterJson).getAsJsonArray();

      for (int i = 0; i < filters.size(); i++)
      {
        JsonObject filter = filters.get(i).getAsJsonObject();

        String attribute = filter.get("attribute").getAsString();

        if (mdBusiness.definesAttribute(attribute) instanceof MdAttributeMomentDAOIF)
        {
          JsonObject jObject = filter.get("value").getAsJsonObject();

          try
          {
            if (jObject.has("start") && !jObject.get("start").isJsonNull())
            {
              String date = jObject.get("start").getAsString();

              if (date.length() > 0)
              {
                vQuery.WHERE(query.aDateTime(attribute).GE(filterFormat.parse(date)));
              }
            }

            if (jObject.has("end") && !jObject.get("end").isJsonNull())
            {
              String date = jObject.get("end").getAsString();

              if (date.length() > 0)
              {
                vQuery.WHERE(query.aDateTime(attribute).LE(filterFormat.parse(date)));
              }
            }
          }
          catch (ParseException e)
          {
            throw new ProgrammingErrorException(e);
          }
        }
        else
        {
          String v = filter.get("value").getAsString();

          vQuery.WHERE(query.get(attribute).EQ(v));
        }
      }
    }

    if (value != null && value.length() > 0)
    {
      vQuery.WHERE(query.aCharacter(attributeName).LIKEi("%" + value + "%"));
    }

    vQuery.ORDER_BY_ASC(query.get(attributeName));

    OIterator<ValueObject> it = vQuery.getIterator(100, 1);

    try
    {
      while (it.hasNext())
      {
        ValueObject vObject = it.next();

        JsonObject result = new JsonObject();
        result.addProperty("label", vObject.getValue("label"));
        result.addProperty("value", vObject.getValue("value"));

        results.add(result);
      }
    }
    finally
    {
      it.close();
    }

    return results;
  }

  public JsonObject toJSON()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    Locale locale = Session.getCurrentLocale();
    LocaleSerializer serializer = new LocaleSerializer(locale);

    ConversionService service = ServiceFactory.getConversionService();

    GeoObjectType type = service.universalToGeoObjectType(this.getUniversal());

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(MasterList.OID, this.getOid());
    }

    object.addProperty(MasterList.TYPE_CODE, type.getCode());
    object.addProperty(MasterList.LEAF, type.isLeaf());
    object.add(MasterList.DISPLAYLABEL, service.convert(this.getDisplayLabel()).toJSON(serializer));
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

    object.add(MasterList.ATTRIBUTES, this.getAttributesAsJson());

    return object;
  }

  public static MasterList fromJSON(JsonObject object)
  {
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

      String typeCode = object.get(MasterList.TYPE_CODE).getAsString();
      GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(typeCode).get();

      Universal universal = ServiceFactory.getConversionService().getUniversalFromGeoObjectType(type);

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

      list.setUniversal(universal);
      ServiceFactory.getConversionService().populate(list.getDisplayLabel(), label);
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

    TableMetadata metadata = null;

    if (list.isNew())
    {
      metadata = list.createTable();

      list.setMdBusiness(metadata.getMdBusiness());
    }

    list.apply();

    if (metadata != null)
    {
      Map<MdAttribute, MdAttribute> pairs = metadata.getPairs();

      Set<Entry<MdAttribute, MdAttribute>> entries = pairs.entrySet();

      for (Entry<MdAttribute, MdAttribute> entry : entries)
      {
        MasterListAttributeGroup.create(list, entry.getValue(), entry.getKey());
      }
    }

    if (list.isNew())
    {
      MasterList.assignDefaultRolePermissions(list.getMdBusiness());
    }

    return list;
  }

  private static void assignDefaultRolePermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    maintainer.grantPermission(Operation.CREATE, component.getOid());
    maintainer.grantPermission(Operation.DELETE, component.getOid());
    maintainer.grantPermission(Operation.WRITE, component.getOid());
    maintainer.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    consumer.grantPermission(Operation.READ, component.getOid());
    consumer.grantPermission(Operation.READ_ALL, component.getOid());

    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    contributor.grantPermission(Operation.READ, component.getOid());
    contributor.grantPermission(Operation.READ_ALL, component.getOid());
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

  public static void createMdAttribute(GeoObjectType type, Universal universal, AttributeType attributeType)
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    List<? extends MasterList> lists = query.getIterator().getAll();

    for (MasterList list : lists)
    {
      TableMetadata metadata = new TableMetadata();
      metadata.setMdBusiness(list.getMdBusiness());

      list.createMdAttributeFromAttributeType(metadata, attributeType, type, locales);

      Map<MdAttribute, MdAttribute> pairs = metadata.getPairs();

      Set<Entry<MdAttribute, MdAttribute>> entries = pairs.entrySet();

      for (Entry<MdAttribute, MdAttribute> entry : entries)
      {
        MasterListAttributeGroup.create(list, entry.getValue(), entry.getKey());
      }
    }
  }

  public static void deleteMdAttribute(Universal universal, AttributeType attributeType)
  {
    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    List<? extends MasterList> lists = query.getIterator().getAll();

    for (MasterList list : lists)
    {
      TableMetadata metadata = new TableMetadata();
      metadata.setMdBusiness(list.getMdBusiness());

      list.removeAttributeType(metadata, attributeType);
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
