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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
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
import org.json.JSONArray;
import org.json.JSONException;

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
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.MdAttributePointDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
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
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.command.GeoserverCreateWMSCommand;
import net.geoprism.registry.command.GeoserverRemoveWMSCommand;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.SupportedLocaleCache;
import net.geoprism.registry.etl.PublishShapefileJob;
import net.geoprism.registry.etl.PublishShapefileJobQuery;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.masterlist.MasterListAttributeComparator;
import net.geoprism.registry.masterlist.TableMetadata;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.MasterListShapefileExporter;

public class MasterListVersion extends MasterListVersionBase
{
  private static final long serialVersionUID = -351397872;

  public static String      PREFIX           = "ml_";

  public static String      ORIGINAL_OID     = "originalOid";

  public static String      LEAF             = "leaf";

  public static String      TYPE_CODE        = "typeCode";

  public static String      ORG_CODE         = "orgCode";

  public static String      ATTRIBUTES       = "attributes";

  public static String      NAME             = "name";

  public static String      LABEL            = "label";

  public static String      VALUE            = "value";

  public static String      TYPE             = "type";

  public static String      BASE             = "base";

  public static String      DEPENDENCY       = "dependency";

  public static String      DEFAULT_LOCALE   = "DefaultLocale";

  public static String      PERIOD           = "period";

  public static String      PUBLISHED        = "PUBLISHED";

  public static String      EXPLORATORY      = "EXPLORATORY";

  public MasterListVersion()
  {
    super();
  }

  @Override
  public void apply()
  {
    super.apply();

    if (this.getVersionType().equals(MasterListVersion.PUBLISHED))
    {
      new GeoserverCreateWMSCommand(this).doIt();
    }
  }

  private String getTableName()
  {
    int count = 0;

    MdBusiness mdBusiness = this.getMasterlist().getUniversal().getMdBusiness();

    String name = PREFIX + count + mdBusiness.getTableName();

    if (name.length() > 29)
    {
      name = name.substring(0, 29);
    }

    while (Database.tableExists(name))
    {
      count++;

      name = PREFIX + count + mdBusiness.getTableName();
    }

    return name;
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
      return true;
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

  public void createMdAttributeFromAttributeType(ServerGeoObjectType type, AttributeType attributeType, List<Locale> locales)
  {
    TableMetadata metadata = new TableMetadata();
    metadata.setMdBusiness(this.getMdBusiness());

    this.createMdAttributeFromAttributeType(metadata, attributeType, type, locales);

    Map<MdAttribute, MdAttribute> pairs = metadata.getPairs();

    Set<Entry<MdAttribute, MdAttribute>> entries = pairs.entrySet();

    for (Entry<MdAttribute, MdAttribute> entry : entries)
    {
      MasterListAttributeGroup.create(this, entry.getValue(), entry.getKey());
    }
  }

  public void createMdAttributeFromAttributeType(TableMetadata metadata, AttributeType attributeType, ServerGeoObjectType type, List<Locale> locales)
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

      LocalizedValueConverter.populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
      LocalizedValueConverter.populate(mdAttribute.getDescription(), attributeType.getDescription());

      mdAttribute.setDefiningMdClass(mdBusiness);
      mdAttribute.apply();
    }
    else if (attributeType instanceof AttributeTermType)
    {
      MdAttributeCharacter cloneAttribute = new MdAttributeCharacter();
      cloneAttribute.setValue(MdAttributeConcreteInfo.NAME, attributeType.getName());
      cloneAttribute.setValue(MdAttributeCharacterInfo.SIZE, "255");
      cloneAttribute.addIndexType(MdAttributeIndices.NON_UNIQUE_INDEX);
      LocalizedValueConverter.populate(cloneAttribute.getDisplayLabel(), attributeType.getLabel());
      LocalizedValueConverter.populate(cloneAttribute.getDescription(), attributeType.getDescription());
      cloneAttribute.setDefiningMdClass(mdBusiness);
      cloneAttribute.apply();

      metadata.addPair(cloneAttribute, cloneAttribute);

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + DEFAULT_LOCALE);
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDisplayLabel(), attributeType.getLabel(), " (defaultLocale)");
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription(), " (defaultLocale)");
      mdAttributeDefaultLocale.apply();

      metadata.addPair(mdAttributeDefaultLocale, cloneAttribute);

      for (Locale locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        LocalizedValueConverter.populate(mdAttributeLocale.getDisplayLabel(), attributeType.getLabel(), " (" + locale.toString() + ")");
        LocalizedValueConverter.populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();

        metadata.addPair(mdAttributeLocale, cloneAttribute);
      }

      // MdAttributeUUID mdAttributeOid = new MdAttributeUUID();
      // mdAttributeOid.setValue(MdAttributeConcreteInfo.NAME,
      // attributeType.getName() + "Oid");
      // AbstractBuilder.populate(mdAttributeOid.getDisplayLabel(),
      // attributeType.getLabel());
      // AbstractBuilder.populate(mdAttributeOid.getDescription(),
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
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (defaultLocale)");
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription(), " (defaultLocale)");
      mdAttributeDefaultLocale.apply();

      for (Locale locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        LocalizedValueConverter.populate(mdAttributeLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (" + locale.toString() + ")");
        LocalizedValueConverter.populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
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

  private TableMetadata createTable()
  {
    MasterList masterlist = this.getMasterlist();

    TableMetadata metadata = new TableMetadata();

    Locale currentLocale = Session.getCurrentLocale();

    String viewName = this.getTableName();

    // Create the MdTable
    MdBusinessDAO mdTableDAO = MdBusinessDAO.newInstance();
    mdTableDAO.setValue(MdTableInfo.NAME, viewName);
    mdTableDAO.setValue(MdTableInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdTableDAO.setStructValue(MdTableInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, masterlist.getDisplayLabel().getValue());
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

    List<Locale> locales = SupportedLocaleCache.getLocales();

    ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());

    this.createMdAttributeFromAttributeType(mdBusiness, type.getGeometryType());

    Collection<AttributeType> attributeTypes = type.getAttributeMap().values();

    for (AttributeType attributeType : attributeTypes)
    {
      if (this.isValid(attributeType))
      {
        this.createMdAttributeFromAttributeType(metadata, attributeType, type, locales);
      }
    }

    JsonArray hierarchies = masterlist.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = masterlist.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();

        ServerHierarchyType hierarchyType = ServiceFactory.getMetadataCache().getHierachyType(hCode).get();
        String hierarchyLabel = hierarchyType.getDisplayLabel().getValue(currentLocale);

        for (String pCode : pCodes)
        {
          ServerGeoObjectType got = ServerGeoObjectType.get(pCode);
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
        }
      }
    }

    JsonArray subtypeHierarchies = masterlist.getSubtypeHierarchiesAsJson();

    for (int i = 0; i < subtypeHierarchies.size(); i++)
    {
      JsonObject hierarchy = subtypeHierarchies.get(i).getAsJsonObject();

      if (hierarchy.has("selected") && hierarchy.get("selected").getAsBoolean())
      {
        String hCode = hierarchy.get("code").getAsString();

        HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hCode).get();
        String hierarchyLabel = hierarchyType.getLabel().getValue(currentLocale);

        String attributeName = hCode.toLowerCase();

        String codeDescription = LocalizationFacade.getFromBundles("masterlist.code.description");
        codeDescription = codeDescription.replaceAll("\\{typeLabel\\}", "");
        codeDescription = codeDescription.replaceAll("\\{hierarchyLabel\\}", hierarchyLabel);

        String labelDescription = LocalizationFacade.getFromBundles("masterlist.label.description");
        labelDescription = labelDescription.replaceAll("\\{typeLabel\\}", "");
        labelDescription = labelDescription.replaceAll("\\{hierarchyLabel\\}", hierarchyLabel);

        MdAttributeCharacterDAO mdAttributeCode = MdAttributeCharacterDAO.newInstance();
        mdAttributeCode.setValue(MdAttributeCharacterInfo.NAME, attributeName);
        mdAttributeCode.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
        mdAttributeCode.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeCode.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel);
        mdAttributeCode.addItem(MdAttributeCharacterInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
        mdAttributeCode.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, codeDescription);
        mdAttributeCode.apply();

        MdAttributeCharacterDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
        mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + DEFAULT_LOCALE);
        mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
        mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " (defaultLocale)");
        mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", "default"));
        mdAttributeDefaultLocale.apply();

        for (Locale locale : locales)
        {
          MdAttributeCharacterDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
          mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + locale.toString());
          mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " (" + locale + ")");
          mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", locale.toString()));
          mdAttributeLocale.apply();
        }
      }
    }

    return metadata;
  }

  @Override
  @Transaction
  public void delete()
  {
    // Delete all jobs
    List<PublishShapefileJob> jobs = this.getJobs();

    for (PublishShapefileJob job : jobs)
    {
      job.delete();
    }

    // Delete tile cache
    TileCache.deleteTiles(this);

    MasterListAttributeGroup.deleteAll(this);

    MdBusiness mdTable = this.getMdBusiness();

    super.delete();

    if (mdTable != null)
    {
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      mdTable.delete();
    }

    if (this.getVersionType().equals(MasterListVersion.PUBLISHED))
    {
      new GeoserverRemoveWMSCommand(this).doIt();
    }
  }

  public List<PublishShapefileJob> getJobs()
  {
    PublishShapefileJobQuery query = new PublishShapefileJobQuery(new QueryFactory());
    query.WHERE(query.getMasterList().EQ(this));

    try (OIterator<? extends PublishShapefileJob> it = query.getIterator())
    {
      return new LinkedList<PublishShapefileJob>(it.getAll());
    }
  }

  public File generateShapefile()
  {
    String filename = this.getOid() + ".zip";

    final MasterList list = this.getMasterlist();

    final File directory = list.getShapefileDirectory();
    directory.mkdirs();

    final File file = new File(directory, filename);

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> this.isValid(mdAttribute)).collect(Collectors.toList());

    try
    {
      MasterListShapefileExporter exporter = new MasterListShapefileExporter(this, mdBusiness, mdAttributes, null);

      try (final InputStream istream = exporter.export())
      {
        try (final FileOutputStream fos = new FileOutputStream(file))
        {
          IOUtils.copy(istream, fos);
        }
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return file;
  }

  public InputStream downloadShapefile()
  {
    String filename = this.getOid() + ".zip";

    final MasterList list = this.getMasterlist();

    final File directory = list.getShapefileDirectory();
    directory.mkdirs();

    final File file = new File(directory, filename);

    try
    {
      return new FileInputStream(file);
    }
    catch (FileNotFoundException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  public JsonObject publish()
  {
    this.lock();

    try
    {
      MasterList masterlist = this.getMasterlist();

      if (!masterlist.isValid())
      {
        throw new InvalidMasterListException();
      }

      // Delete tile cache
      TileCache.deleteTiles(this);

      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());

      List<Locale> locales = SupportedLocaleCache.getLocales();

      // Add the type ancestor fields
      Map<HierarchyType, List<GeoObjectType>> ancestorMap = masterlist.getAncestorMap(type);
      Collection<AttributeType> attributes = type.getAttributeMap().values();
      Set<ServerHierarchyType> hierarchiesOfSubTypes = type.getHierarchiesOfSubTypes();

      // ServerGeoObjectService service = new ServerGeoObjectService();
      // ServerGeoObjectQuery query = service.createQuery(type,
      // this.getPeriod());

      VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, this.getForDate());
      Long count = query.getCount();

      if (count == null)
      {
        count = 0L;
      }

      long current = 0;

      try
      {
        ProgressService.put(this.getOid(), new Progress(0L, count, ""));
        int pageSize = 1000;

        long skip = 0;

        while (skip < count)
        {
          query = new VertexGeoObjectQuery(type, this.getForDate());
          query.setLimit(pageSize);
          query.setSkip(skip);

          List<ServerGeoObjectIF> results = query.getResults();

          for (ServerGeoObjectIF result : results)
          {
            Business business = new Business(mdBusiness.definesType());

            publish(result, business, attributes, ancestorMap, hierarchiesOfSubTypes, locales);

            Thread.yield();

            ProgressService.put(this.getOid(), new Progress(current++, count, ""));
          }

          skip += pageSize;
        }

        this.setPublishDate(new Date());
        this.apply();

        return this.toJSON(true);
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

  private void publish(ServerGeoObjectIF object, Business business, Collection<AttributeType> attributes, Map<HierarchyType, List<GeoObjectType>> ancestorMap, Set<ServerHierarchyType> hierarchiesOfSubTypes, List<Locale> locales)
  {
    business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, object.getGeometry());

    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();

      business.setValue(ORIGINAL_OID, object.getRunwayId());

      if (this.isValid(attribute))
      {
        Object value = object.getValue(name);

        if (value != null)
        {
          if (name.equals(DefaultAttribute.STATUS.getName()))
          {
            GeoObjectStatus status = (GeoObjectStatus) value;
            Term term = ServiceFactory.getConversionService().geoObjectStatusToTerm(status);
            LocalizedValue label = term.getLabel();

            this.setValue(business, name, term.getCode());
            this.setValue(business, name + DEFAULT_LOCALE, label.getValue(LocalizedValue.DEFAULT_LOCALE));

            for (Locale locale : locales)
            {
              this.setValue(business, name + locale.toString(), label.getValue(locale));
            }
          }
          else if (attribute instanceof AttributeTermType)
          {
            Classifier classifier = (Classifier) value;

            Term term = ( (AttributeTermType) attribute ).getTermByCode(classifier.getClassifierId()).get();
            LocalizedValue label = term.getLabel();

            this.setValue(business, name, term.getCode());
            this.setValue(business, name + DEFAULT_LOCALE, label.getValue(LocalizedValue.DEFAULT_LOCALE));

            for (Locale locale : locales)
            {
              this.setValue(business, name + locale.toString(), label.getValue(locale));
            }
          }
          else if (attribute instanceof AttributeLocalType)
          {
            LocalizedValue label = (LocalizedValue) value;

            String defaultLocale = label.getValue(LocalizedValue.DEFAULT_LOCALE);

            if (defaultLocale == null)
            {
              defaultLocale = "";
            }

            this.setValue(business, name + DEFAULT_LOCALE, defaultLocale);

            for (Locale locale : locales)
            {
              String localeValue = label.getValue(locale);

              if (localeValue == null)
              {
                localeValue = "";
              }

              this.setValue(business, name + locale.toString(), localeValue);
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
      ServerHierarchyType hierarchy = ServerHierarchyType.get(entry.getKey());

      // List<GeoObjectType> parents = entry.getValue();
      Map<String, LocationInfo> map = object.getAncestorMap(hierarchy, true);

      Set<Entry<String, LocationInfo>> locations = map.entrySet();

      for (Entry<String, LocationInfo> location : locations)
      {
        String pCode = location.getKey();
        LocationInfo vObject = location.getValue();

        if (vObject != null)
        {
          String attributeName = hierarchy.getCode().toLowerCase() + pCode.toLowerCase();

          this.setValue(business, attributeName, vObject.getCode());
          this.setValue(business, attributeName + DEFAULT_LOCALE, vObject.getLabel());

          for (Locale locale : locales)
          {
            this.setValue(business, attributeName + locale.toString(), vObject.getLabel(locale));
          }
        }
      }
    }

    for (ServerHierarchyType hierarchy : hierarchiesOfSubTypes)
    {
      ServerParentTreeNode node = object.getParentsForHierarchy(hierarchy, false);
      List<ServerParentTreeNode> parents = node.getParents();

      if (parents.size() > 0)
      {
        ServerParentTreeNode parent = parents.get(0);

        String attributeName = hierarchy.getCode().toLowerCase();
        ServerGeoObjectIF geoObject = parent.getGeoObject();
        LocalizedValue label = geoObject.getDisplayLabel();

        this.setValue(business, attributeName, geoObject.getCode());
        this.setValue(business, attributeName + DEFAULT_LOCALE, label.getValue(DEFAULT_LOCALE));

        for (Locale locale : locales)
        {
          this.setValue(business, attributeName + locale.toString(), label.getValue(locale));
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
  public void updateRecord(ServerGeoObjectIF object)
  {
    object.setDate(this.getForDate());

    // Delete tile cache
    TileCache.deleteTiles(this);

    MasterList masterlist = this.getMasterlist();
    MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
    List<Locale> locales = SupportedLocaleCache.getLocales();

    // Add the type ancestor fields
    ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());
    Set<ServerHierarchyType> hierarchiesOfSubTypes = type.getHierarchiesOfSubTypes();
    Map<HierarchyType, List<GeoObjectType>> ancestorMap = masterlist.getAncestorMap(type);
    Collection<AttributeType> attributes = type.getAttributeMap().values();

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
    query.WHERE(query.aCharacter(DefaultAttribute.CODE.getName()).EQ(object.getCode()));

    List<Business> records = query.getIterator().getAll();

    for (Business record : records)
    {
      try
      {
        record.appLock();

        this.publish(object, record, attributes, ancestorMap, hierarchiesOfSubTypes, locales);
      }
      finally
      {
        record.unlock();
      }
    }
  }

  @Transaction
  public void publishRecord(ServerGeoObjectIF object)
  {
    object.setDate(this.getForDate());

    // Delete tile cache
    TileCache.deleteTiles(this);

    MasterList masterlist = this.getMasterlist();
    MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
    List<Locale> locales = SupportedLocaleCache.getLocales();

    // Add the type ancestor fields
    ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());
    Map<HierarchyType, List<GeoObjectType>> ancestorMap = masterlist.getAncestorMap(type);
    Set<ServerHierarchyType> hierarchiesOfSubTypes = type.getHierarchiesOfSubTypes();
    Collection<AttributeType> attributes = type.getAttributeMap().values();

    Business business = new Business(mdBusiness.definesType());

    this.publish(object, business, attributes, ancestorMap, hierarchiesOfSubTypes, locales);
  }

  public JsonObject toJSON(boolean includeAttribute)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));

    String filename = this.getOid() + ".zip";
    MasterList masterlist = this.getMasterlist();
    final File directory = masterlist.getShapefileDirectory();
    final File file = new File(directory, filename);

    ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(MasterListVersion.OID, this.getOid());
    }

    object.addProperty(MasterList.DISPLAYLABEL, masterlist.getDisplayLabel().getValue());
    object.addProperty(MasterListVersion.TYPE_CODE, type.getCode());
    object.addProperty(MasterListVersion.ORG_CODE, type.getOrganization().getCode());
    object.addProperty(MasterListVersion.MASTERLIST, masterlist.getOid());
    object.addProperty(MasterListVersion.FORDATE, format.format(this.getForDate()));
    object.addProperty(MasterListVersion.CREATEDATE, format.format(this.getCreateDate()));
    object.addProperty(MasterListVersion.PERIOD, this.getPeriod(masterlist, format));
    object.addProperty("isGeometryEditable", type.isGeometryEditable());
    object.addProperty("isAbstract", type.getIsAbstract());
    object.addProperty("shapefile", file.exists());

    if (type.getSuperType() != null)
    {
      object.addProperty("superTypeCode", type.getSuperType().getCode());
    }

    if (this.getPublishDate() != null)
    {
      object.addProperty(MasterListVersion.PUBLISHDATE, format.format(this.getPublishDate()));
    }

    if (includeAttribute)
    {
      object.add(MasterListVersion.ATTRIBUTES, this.getAttributesAsJson());
    }

    return object;
  }

  private String getPeriod(MasterList masterlist, SimpleDateFormat format)
  {
    List<ChangeFrequency> frequency = masterlist.getFrequency();

    if (frequency.contains(ChangeFrequency.ANNUAL))
    {
      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calendar.setTime(this.getForDate());

      return Integer.toString(calendar.get(Calendar.YEAR));
    }
    else if (frequency.contains(ChangeFrequency.BIANNUAL))
    {
      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calendar.setTime(this.getForDate());

      int halfYear = ( calendar.get(Calendar.MONTH) / 6 ) + 1;

      return "H" + halfYear + " " + Integer.toString(calendar.get(Calendar.YEAR));
    }
    else if (frequency.contains(ChangeFrequency.QUARTER))
    {
      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calendar.setTime(this.getForDate());

      int quarter = ( calendar.get(Calendar.MONTH) / 3 ) + 1;

      return "Q" + quarter + " " + Integer.toString(calendar.get(Calendar.YEAR));
    }
    else if (frequency.contains(ChangeFrequency.MONTHLY))
    {
      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      calendar.setTime(this.getForDate());
      calendar.set(Calendar.DAY_OF_MONTH, 1);

      Date startOfWeek = calendar.getTime();

      calendar.add(Calendar.MONTH, 1);
      calendar.add(Calendar.DAY_OF_YEAR, -1);

      Date endOfWeek = calendar.getTime();

      return format.format(startOfWeek) + " - " + format.format(endOfWeek);
    }

    return format.format(this.getForDate());
  }

  private JsonArray getAttributesAsJson()
  {
    List<Locale> locales = SupportedLocaleCache.getLocales();
    MasterList list = this.getMasterlist();

    Map<String, JsonArray> dependencies = new HashMap<String, JsonArray>();
    Map<String, String> baseAttributes = new HashMap<String, String>();
    List<String> attributesOrder = new LinkedList<String>();

    JsonArray hierarchies = list.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = list.getParentCodes(hierarchy);

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
        longitude.addProperty(LABEL, LocalizationFacade.getFromBundles(GeoObjectImportConfiguration.LONGITUDE_KEY));
        longitude.addProperty(TYPE, "none");

        attributes.add(longitude);

        JsonObject latitude = new JsonObject();
        latitude.addProperty(NAME, "latitude");
        latitude.addProperty(LABEL, LocalizationFacade.getFromBundles(GeoObjectImportConfiguration.LATITUDE_KEY));
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
    // if (!dependencies.containsKey(attributeName))
    // {
    // dependencies.put(attributeName, new JsonArray());
    // }
    //
    // dependencies.get(attributeName).add(dependency);
  }

  public void removeAttributeType(AttributeType attributeType)
  {
    TableMetadata metadata = new TableMetadata();
    metadata.setMdBusiness(this.getMdBusiness());

    this.removeAttributeType(metadata, attributeType);
  }

  public void removeAttributeType(TableMetadata metadata, AttributeType attributeType)
  {
    List<Locale> locales = SupportedLocaleCache.getLocales();

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

  public BusinessQuery buildQuery(String filterJson)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());

    DateFormat filterFormat = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    filterFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    if (filterJson != null && filterJson.length() > 0)
    {
      JsonArray filters = JsonParser.parseString(filterJson).getAsJsonArray();

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

  public String bbox()
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    double[] geometry = GeoserverFacade.getBBOX(mdBusiness.getTableName());

    if (geometry != null)
    {
      try
      {
        JSONArray bboxArr = new JSONArray();
        bboxArr.put(geometry[0]);
        bboxArr.put(geometry[1]);
        bboxArr.put(geometry[2]);
        bboxArr.put(geometry[3]);

        return bboxArr.toString();
      }
      catch (JSONException ex)
      {
        throw new ProgrammingErrorException(ex);
      }
    }

    return null;
  }

  public JsonArray values(String value, String attributeName, String valueAttribute, String filterJson)
  {
    DateFormat filterFormat = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    filterFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    JsonArray results = new JsonArray();

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    ValueQuery vQuery = new ValueQuery(new QueryFactory());

    BusinessQuery query = new BusinessQuery(vQuery, mdBusiness.definesType());

    vQuery.SELECT_DISTINCT(query.get(attributeName, "label"), query.get(valueAttribute, "value"));

    vQuery.FROM(query);

    if (filterJson != null && filterJson.length() > 0)
    {
      JsonArray filters = JsonParser.parseString(filterJson).getAsJsonArray();

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
      JsonObject jObject = JsonParser.parseString(sort).getAsJsonObject();
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

  @Transaction
  public static MasterListVersion create(MasterList list, Date forDate, String versionType)
  {
    MasterListVersion version = new MasterListVersion();
    version.setMasterlist(list);
    version.setForDate(forDate);
    version.setVersionType(versionType);

    TableMetadata metadata = null;

    // if (version.isNew())
    // {
    metadata = version.createTable();

    version.setMdBusiness(metadata.getMdBusiness());
    // }

    version.apply();

    if (metadata != null)
    {
      Map<MdAttribute, MdAttribute> pairs = metadata.getPairs();

      Set<Entry<MdAttribute, MdAttribute>> entries = pairs.entrySet();

      for (Entry<MdAttribute, MdAttribute> entry : entries)
      {
        MasterListAttributeGroup.create(version, entry.getValue(), entry.getKey());
      }
    }

    // if (version.isNew())
    // {
    MasterListVersion.assignDefaultRolePermissions(version.getMdBusiness());
    // }

    return version;
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

  public static List<? extends MasterListVersion> getAll(String versionType)
  {
    MasterListVersionQuery query = new MasterListVersionQuery(new QueryFactory());
    query.WHERE(query.getVersionType().EQ(versionType));

    try (OIterator<? extends MasterListVersion> it = query.getIterator())
    {
      return it.getAll();
    }
  }
}
