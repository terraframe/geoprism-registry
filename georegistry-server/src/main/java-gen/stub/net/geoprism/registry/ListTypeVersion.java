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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.JsonDateUtil;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.Pair;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.rbac.Authenticate;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.BusinessInfo;
import com.runwaysdk.constants.Constants;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDateTimeUtil;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.constants.MdAttributeFloatInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdTableInfo;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMomentDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeFloatDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeUUIDDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.localization.LocalizedValueStoreStoreValue;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.query.AttributeBoolean;
import com.runwaysdk.query.BasicCondition;
import com.runwaysdk.query.ComponentQuery;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.F;
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
import com.runwaysdk.system.gis.metadata.MdAttributeShape;
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeDouble;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeLong;
import com.runwaysdk.system.metadata.MdBusiness;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.DefaultConfiguration;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.command.GeoserverCreateWMSCommand;
import net.geoprism.registry.command.GeoserverRemoveWMSCommand;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.curation.CurationService;
import net.geoprism.registry.curation.ListCurationHistory;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.localization.DefaultLocaleView;
import net.geoprism.registry.masterlist.ListAttribute;
import net.geoprism.registry.masterlist.ListAttributeGroup;
import net.geoprism.registry.masterlist.ListColumn;
import net.geoprism.registry.masterlist.TableMetadata;
import net.geoprism.registry.masterlist.TableMetadata.Attribute;
import net.geoprism.registry.masterlist.TableMetadata.AttributeGroup;
import net.geoprism.registry.masterlist.TableMetadata.Group;
import net.geoprism.registry.masterlist.TableMetadata.HierarchyGroup;
import net.geoprism.registry.masterlist.TableMetadata.HolderGroup;
import net.geoprism.registry.masterlist.TableMetadata.TypeGroup;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.ListTypeVersionPageQuery;
import net.geoprism.registry.query.graph.BasicVertexQuery;
import net.geoprism.registry.query.graph.BasicVertexRestriction;
import net.geoprism.registry.service.SerializedListTypeCache;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.ListTypeShapefileExporter;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class ListTypeVersion extends ListTypeVersionBase implements TableEntity, LabeledVersion
{
  private static final long  serialVersionUID = -351397872;

  private static Logger      logger           = LoggerFactory.getLogger(ListTypeVersion.class);

  public static final String PREFIX           = "lt_";

  public static final String ORIGINAL_OID     = "originalOid";

  public static final String LEAF             = "leaf";

  public static final String TYPE_CODE        = "typeCode";

  public static final String ORG_CODE         = "orgCode";

  public static final String ATTRIBUTES       = "attributes";

  public static final String HIERARCHIES      = "hierarchies";

  public static final String DEFAULT_LOCALE   = "DefaultLocale";

  public static final String PERIOD           = "period";

  public static final String PUBLISHED        = "PUBLISHED";

  public static final String EXPLORATORY      = "EXPLORATORY";

  public ListTypeVersion()
  {
    super();
  }

  @Override
  public void apply()
  {
    if (this.getWorking())
    {
      if (this.getListVisibility() == null || this.getListVisibility().length() == 0)
      {
        this.setListVisibility(ListType.PRIVATE);
      }

      if (this.getGeospatialVisibility() == null || this.getGeospatialVisibility().length() == 0)
      {
        this.setGeospatialVisibility(ListType.PRIVATE);
      }
    }

    super.apply();

    if (this.getGeospatialVisibility().equals(ListType.PUBLIC))
    {
      new GeoserverCreateWMSCommand(this).doIt();
    }

    SerializedListTypeCache.getInstance().remove(this.getListTypeOid());
  }

  private String getTableName()
  {
    int count = 0;

    MdBusiness mdBusiness = this.getListType().getUniversal().getMdBusiness();

    String name = PREFIX + count + mdBusiness.getTableName();

    if (name.length() > 25)
    {
      name = name.substring(0, 25);
    }

    while (Database.tableExists(name))
    {
      count++;

      name = PREFIX + count + mdBusiness.getTableName();

      if (name.length() > 25)
      {
        name = name.substring(0, 25);
      }
    }

    return name;
  }

  private boolean isValid(AttributeType attributeType)
  {
    if (attributeType.getName().equals(DefaultAttribute.UID.getName()))
    {
      return true;
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

    if (attributeType.getName().equals(DefaultAttribute.EXISTS.getName()))
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

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.EXISTS.getName()))
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

  public static TableMetadata createMdAttributeFromAttributeType(ListTypeVersion version, ServerGeoObjectType type, AttributeType attributeType, Collection<SupportedLocaleIF> locales, LocalizedValueStoreStoreValue defaultLocaleLabel)
  {
    TableMetadata metadata = new TableMetadata(version.getMdBusiness(), type);

    createMdAttributeFromAttributeType(metadata, attributeType, type, locales, defaultLocaleLabel);

    if (metadata != null)
    {
      ListTypeGeoObjectTypeGroup root = ListTypeGeoObjectTypeGroup.getRoot(version, type);

      // TODO IS THERE A BETTER WAY TO DETERMINE THE EMPTY HOLDER GROUP
      ListTypeGroup holder = root.getChildren().stream().filter(p -> {
        return p.getLabel().getDefaultValue().equals("");
      }).findFirst().get();

      // Create the Hierarchy Attribute metadata
      List<Group> groups = metadata.getGroups();
      Group typeGroup = groups.get(0);

      for (Group group : typeGroup.getChildren())
      {
        if (group instanceof HolderGroup)
        {
          group.getChildren().forEach(g -> g.create(version, holder));
        }
        else
        {
          group.create(version, root);
        }
      }
    }

    return metadata;
  }

  protected static void createMdAttributeFromAttributeType(TableMetadata metadata, AttributeType attributeType, ServerGeoObjectType type, Collection<SupportedLocaleIF> locales, LocalizedValueStoreStoreValue defaultLocaleLabel)
  {
    MdBusiness mdBusiness = metadata.getMdBusiness();

    if (! ( attributeType instanceof AttributeTermType || attributeType instanceof AttributeClassificationType || attributeType instanceof AttributeLocalType ))
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

      if (! ( attributeType.getName().equals(DefaultAttribute.UID.getName()) || attributeType.getName().equals(DefaultAttribute.INVALID.getName()) ))
      {
        metadata.getRoot().addChild(new Attribute(mdAttribute, 2));
      }
    }
    else if (attributeType instanceof AttributeTermType || attributeType instanceof AttributeClassificationType)
    {
      AttributeGroup attributeGroup = metadata.getRoot().addChild(new AttributeGroup(attributeType));

      MdAttributeCharacter cloneAttribute = new MdAttributeCharacter();
      cloneAttribute.setValue(MdAttributeConcreteInfo.NAME, attributeType.getName());
      cloneAttribute.setValue(MdAttributeCharacterInfo.SIZE, "255");
      cloneAttribute.addIndexType(MdAttributeIndices.NON_UNIQUE_INDEX);
      LocalizedValueConverter.populate(cloneAttribute.getDisplayLabel(), attributeType.getLabel());
      LocalizedValueConverter.populate(cloneAttribute.getDescription(), attributeType.getDescription());
      cloneAttribute.setDefiningMdClass(mdBusiness);
      cloneAttribute.apply();

      attributeGroup.addChild(new Attribute(cloneAttribute, LocalizationFacade.localizeAll("data.property.label.code")));

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + DEFAULT_LOCALE);
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDisplayLabel(), attributeType.getLabel(), " (" + defaultLocaleLabel.getValue() + ")");
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription(), " (" + defaultLocaleLabel.getValue() + ")");
      mdAttributeDefaultLocale.apply();

      attributeGroup.addChild(new Attribute(mdAttributeDefaultLocale, defaultLocaleLabel));

      for (SupportedLocaleIF locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.getLocale().toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        LocalizedValueConverter.populate(mdAttributeLocale.getDisplayLabel(), attributeType.getLabel(), " (" + locale.getDisplayLabel().getValue() + ")");
        LocalizedValueConverter.populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();

        attributeGroup.addChild(new Attribute(mdAttributeLocale, locale));
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

      AttributeGroup attributeGroup = metadata.getRoot().addChild(new AttributeGroup(attributeType));

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + DEFAULT_LOCALE);
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (" + defaultLocaleLabel + ")");
      LocalizedValueConverter.populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription(), " (" + defaultLocaleLabel + ")");
      mdAttributeDefaultLocale.apply();

      attributeGroup.addChild(new Attribute(mdAttributeDefaultLocale, defaultLocaleLabel));

      for (SupportedLocaleIF locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.getLocale().toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        LocalizedValueConverter.populate(mdAttributeLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (" + locale.getDisplayLabel().getValue() + ")");
        LocalizedValueConverter.populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();

        attributeGroup.addChild(new Attribute(mdAttributeLocale, locale));
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
    else if (attributeType.equals(GeometryType.MIXED))
    {
      mdAttribute = new MdAttributeShape();
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported geometry type [" + attributeType + "]");
    }

    mdAttribute.setAttributeName(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    mdAttribute.getDisplayLabel().setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    mdAttribute.setDefiningMdClass(mdBusiness);
    mdAttribute.setSrid(4326);
    mdAttribute.apply();
  }

  private TableMetadata createTable()
  {
    LocalizedValueStore lvs = LocalizedValueStore.getByKey(DefaultLocaleView.LABEL);
    LocalizedValueStoreStoreValue storeValue = lvs.getStoreValue();
    String defaultLocaleLabel = storeValue.getValue();

    ListType masterlist = this.getListType();

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

    Collection<SupportedLocaleIF> locales = LocalizationFacade.getSupportedLocales();

    ServerGeoObjectType type = masterlist.getGeoObjectType();

    TableMetadata metadata = new TableMetadata(mdBusiness, type);

    this.createMdAttributeFromAttributeType(mdBusiness, type.getGeometryType());

    Collection<AttributeType> attributeTypes = type.getAttributeMap().values();

    for (AttributeType attributeType : attributeTypes)
    {
      if (this.isValid(attributeType))
      {
        createMdAttributeFromAttributeType(metadata, attributeType, type, locales, storeValue);
      }
    }

    if ( ( type.getGeometryType().equals(GeometryType.MULTIPOINT) || type.getGeometryType().equals(GeometryType.POINT) ) && masterlist.getIncludeLatLong())
    {
      MdAttributeFloatDAO mdAttributeLatitude = MdAttributeFloatDAO.newInstance();
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.NAME, "latitude");
      mdAttributeLatitude.setStructValue(MdAttributeFloatInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Latitude");
      mdAttributeLatitude.setStructValue(MdAttributeFloatInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "Latitude");
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.REQUIRED, MdAttributeBooleanInfo.FALSE);
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.IMMUTABLE, MdAttributeBooleanInfo.FALSE);
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.REJECT_ZERO, MdAttributeBooleanInfo.FALSE);
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.REJECT_NEGATIVE, MdAttributeBooleanInfo.FALSE);
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.REJECT_POSITIVE, MdAttributeBooleanInfo.FALSE);
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.LENGTH, "12");
      mdAttributeLatitude.setValue(MdAttributeFloatInfo.DECIMAL, "8");
      mdAttributeLatitude.apply();

      metadata.getRoot().addChild(new Attribute(mdAttributeLatitude, 2));

      MdAttributeFloatDAO mdAttributeLongitude = MdAttributeFloatDAO.newInstance();
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.NAME, "longitude");
      mdAttributeLongitude.setStructValue(MdAttributeFloatInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Longitude");
      mdAttributeLongitude.setStructValue(MdAttributeFloatInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "Longitude");
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.REQUIRED, MdAttributeBooleanInfo.FALSE);
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.IMMUTABLE, MdAttributeBooleanInfo.FALSE);
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.REJECT_ZERO, MdAttributeBooleanInfo.FALSE);
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.REJECT_NEGATIVE, MdAttributeBooleanInfo.FALSE);
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.REJECT_POSITIVE, MdAttributeBooleanInfo.FALSE);
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.LENGTH, "12");
      mdAttributeLongitude.setValue(MdAttributeFloatInfo.DECIMAL, "8");
      mdAttributeLongitude.apply();

      metadata.getRoot().addChild(new Attribute(mdAttributeLongitude, 2));
    }

    JsonArray hierarchies = masterlist.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<Pair<String, Integer>> pCodes = masterlist.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();

        ServerHierarchyType hierarchyType = ServiceFactory.getMetadataCache().getHierachyType(hCode).get();
        String hierarchyLabel = hierarchyType.getDisplayLabel().getValue(currentLocale);

        HierarchyGroup hierarchyGroup = metadata.addRootHierarchyGroup(hierarchyType);

        int level = 0;

        for (Pair<String, Integer> pair : pCodes)
        {
          level++;

          String pCode = pair.getFirst();

          ServerGeoObjectType got = ServerGeoObjectType.get(pCode);

          TypeGroup typeGroup = hierarchyGroup.addTypeGroup(got, level);

          String typeLabel = got.getLabel().getValue(currentLocale);
          String attributeName = hCode.toLowerCase() + pCode.toLowerCase();
          String label = typeLabel + " (" + hierarchyLabel + ")";

          String codeDescription = LocalizationFacade.localize("masterlist.code.description");
          codeDescription = codeDescription.replaceAll("\\{typeLabel\\}", typeLabel);
          codeDescription = codeDescription.replaceAll("\\{hierarchyLabel\\}", hierarchyLabel);

          String labelDescription = LocalizationFacade.localize("masterlist.label.description");
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

          typeGroup.addChild(new Attribute(mdAttributeCode, LocalizationFacade.localizeAll("data.property.label.code")));

          MdAttributeCharacterDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + DEFAULT_LOCALE);
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (" + defaultLocaleLabel + ")");
          mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", "default"));
          mdAttributeDefaultLocale.apply();

          typeGroup.addChild(new Attribute(mdAttributeDefaultLocale, storeValue));

          for (SupportedLocaleIF locale : locales)
          {
            String localeLabel = locale.getDisplayLabel().getValue();

            MdAttributeCharacterDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + locale.getLocale().toString());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
            mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (" + localeLabel + ")");
            mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", localeLabel));
            mdAttributeLocale.apply();

            typeGroup.addChild(new Attribute(mdAttributeLocale, locale));

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

        ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);

        String hierarchyLabel = hierarchyType.getLabel().getValue(currentLocale);

        HierarchyGroup hierarchyGroup = metadata.addRootHierarchyGroup(hierarchyType);

        HolderGroup holderGroup = hierarchyGroup.addChild(new HolderGroup());

        String attributeName = hCode.toLowerCase();

        String codeDescription = LocalizationFacade.localize("masterlist.code.description");
        codeDescription = codeDescription.replaceAll("\\{typeLabel\\}", "");
        codeDescription = codeDescription.replaceAll("\\{hierarchyLabel\\}", hierarchyLabel);

        String labelDescription = LocalizationFacade.localize("masterlist.label.description");
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

        holderGroup.addChild(new Attribute(mdAttributeCode, LocalizationFacade.localizeAll("data.property.label.code")));

        MdAttributeCharacterDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
        mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + DEFAULT_LOCALE);
        mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
        mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " (" + defaultLocaleLabel + ")");
        mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", "default"));
        mdAttributeDefaultLocale.apply();

        holderGroup.addChild(new Attribute(mdAttributeDefaultLocale, storeValue));

        for (SupportedLocaleIF locale : locales)
        {
          String localeLabel = locale.getDisplayLabel().getValue();

          MdAttributeCharacterDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
          mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + locale.getLocale().toString());
          mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " (" + localeLabel + ")");
          mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, labelDescription.replaceAll("\\{locale\\}", localeLabel));
          mdAttributeLocale.apply();

          holderGroup.addChild(new Attribute(mdAttributeLocale, locale));
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
    // List<ExecutableJob> jobs = this.getJobs();
    //
    // for (ExecutableJob job : jobs)
    // {
    // job.delete();
    // }

    // Delete tile cache
    ListTileCache.deleteTiles(this);

    ListTypeGroup.deleteAll(this);

    MdBusiness mdTable = this.getMdBusiness();

    super.delete();

    if (mdTable != null)
    {
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      mdTable.delete();
    }

    if (this.getGeospatialVisibility().equals(ListType.PUBLIC))
    {
      new GeoserverRemoveWMSCommand(this).doIt();
    }

    SerializedListTypeCache.getInstance().remove(this.getListTypeOid());
  }

  // public List<ExecutableJob> getJobs()
  // {
  // LinkedList<ExecutableJob> jobs = new LinkedList<ExecutableJob>();
  //
  // PublishShapefileJobQuery psjq = new PublishShapefileJobQuery(new
  // QueryFactory());
  // psjq.WHERE(psjq.getVersion().EQ(this));
  //
  // try (OIterator<? extends PublishShapefileJob> it = psjq.getIterator())
  // {
  // jobs.addAll(it.getAll());
  // }
  //
  // PublishListTypeVersionJobQuery pmlvj = new
  // PublishListTypeVersionJobQuery(new QueryFactory());
  // pmlvj.WHERE(pmlvj.getListTypeVersion().EQ(this));
  //
  // try (OIterator<? extends PublishListTypeVersionJob> it =
  // pmlvj.getIterator())
  // {
  // jobs.addAll(it.getAll());
  // }
  //
  // return jobs;
  // }

  public File generateShapefile()
  {
    String filename = this.getOid() + ".zip";

    final ListType list = this.getListType();

    final File directory = list.getShapefileDirectory();
    if (!directory.mkdirs())
    {
      logger.debug("Unable to create directory [" + directory.getAbsolutePath() + "]");
    }

    final File file = new File(directory, filename);

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> this.isValid(mdAttribute)).collect(Collectors.toList());

    try
    {
      ListTypeShapefileExporter exporter = new ListTypeShapefileExporter(this, mdBusiness, mdAttributes, null, null);

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

    final ListType list = this.getListType();

    final File directory = list.getShapefileDirectory();
    if (!directory.mkdirs())
    {
      logger.debug("Unable to create directory [" + directory.getAbsolutePath() + "]");
    }

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
  @Authenticate
  public String publish()
  {
    return this.publishNoAuth();
  }

  @Transaction
  public String publishNoAuth()
  {
    this.lock();

    try
    {
      ListType masterlist = this.getListType();

      if (!masterlist.isValid())
      {
        throw new InvalidMasterListException();
      }

      // Delete tile cache
      ListTileCache.deleteTiles(this);

      ListCurationHistory.deleteAll(this);

      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      MdAttributeConcreteDAO status = (MdAttributeConcreteDAO) mdBusiness.definesAttribute("status");
      if (status != null)
      {
        status.delete();
      }

      MdAttributeConcreteDAO statusDefaultLocale = (MdAttributeConcreteDAO) mdBusiness.definesAttribute("statusDefaultLocale");
      if (statusDefaultLocale != null)
      {
        statusDefaultLocale.delete();
      }

      ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());

      Collection<Locale> locales = LocalizationFacade.getInstalledLocales();

      // Add the type ancestor fields
      Map<ServerHierarchyType, List<ServerGeoObjectType>> ancestorMap = masterlist.getAncestorMap(type);
      Collection<AttributeType> attributes = type.getAttributeMap().values();
      Set<ServerHierarchyType> hierarchiesOfSubTypes = type.getHierarchiesOfSubTypes();

      // ServerGeoObjectService service = new ServerGeoObjectService();
      // ServerGeoObjectQuery query = service.createQuery(type,
      // this.getPeriod());

      Date forDate = this.getForDate();

      BasicVertexRestriction restriction = masterlist.getRestriction(type, forDate);
      BasicVertexQuery query = new BasicVertexQuery(type, forDate);
      query.setRestriction(restriction);
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
          query = new BasicVertexQuery(type, forDate);
          query.setRestriction(restriction);
          query.setLimit(pageSize);
          query.setSkip(skip);

          // List<GeoObjectStatus> validStats = new
          // ArrayList<GeoObjectStatus>();
          // validStats.add(GeoObjectStatus.ACTIVE);
          // validStats.add(GeoObjectStatus.INACTIVE);
          // validStats.add(GeoObjectStatus.PENDING);
          // validStats.add(GeoObjectStatus.NEW);
          // query.setRestriction(new ServerStatusRestriction(validStats,
          // this.getForDate(), JoinOp.OR));

          List<ServerGeoObjectIF> results = query.getResults();

          for (ServerGeoObjectIF result : results)
          {
            if (result.getExists(forDate))
            {
              Business business = new Business(mdBusiness.definesType());

              publish(masterlist, type, result, business, attributes, ancestorMap, hierarchiesOfSubTypes, locales);

              Thread.yield();

              ProgressService.put(this.getOid(), new Progress(current++, count, ""));
            }
          }

          skip += pageSize;
        }

        this.setPublishDate(new Date());
        this.apply();

        return this.toJSON(true).toString();
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

  private void publish(ListType listType, ServerGeoObjectType type, ServerGeoObjectIF go, Business business, Collection<AttributeType> attributes, Map<ServerHierarchyType, List<ServerGeoObjectType>> ancestorMap, Set<ServerHierarchyType> hierarchiesOfSubTypes, Collection<Locale> locales)
  {
    VertexServerGeoObject vertexGo = (VertexServerGeoObject) go;

    business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, go.getGeometry());

    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();

      business.setValue(ORIGINAL_OID, go.getRunwayId());

      if (this.isValid(attribute))
      {
        Object value = go.getValue(name, this.getForDate());

        if (value != null)
        {
          if (value instanceof LocalizedValue && ( (LocalizedValue) value ).isNull())
          {
            continue;
          }

          if (attribute instanceof AttributeTermType)
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
          else if (attribute instanceof AttributeClassificationType)
          {
            String classificationTypeCode = ( (AttributeClassificationType) attribute ).getClassificationType();
            ClassificationType classificationType = ClassificationType.getByCode(classificationTypeCode);
            Classification classification = Classification.getByOid(classificationType, (String) value);

            LocalizedValue label = classification.getDisplayLabel();

            this.setValue(business, name, classification.getCode());
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

    Set<Entry<ServerHierarchyType, List<ServerGeoObjectType>>> entries = ancestorMap.entrySet();

    for (Entry<ServerHierarchyType, List<ServerGeoObjectType>> entry : entries)
    {
      ServerHierarchyType hierarchy = entry.getKey();

      Map<String, LocationInfo> map = vertexGo.getAncestorMap(hierarchy, entry.getValue());

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
      ServerParentTreeNode node = go.getParentsForHierarchy(hierarchy, false, this.getForDate());
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

    if (type.getGeometryType().equals(GeometryType.MULTIPOINT) || type.getGeometryType().equals(GeometryType.POINT) && listType.getIncludeLatLong())
    {
      Geometry geom = vertexGo.getGeometry();

      if (geom instanceof MultiPoint)
      {
        MultiPoint mp = (MultiPoint) geom;

        Coordinate[] coords = mp.getCoordinates();

        Coordinate firstCoord = coords[0];

        this.setValue(business, "latitude", String.valueOf(firstCoord.y));
        this.setValue(business, "longitude", String.valueOf(firstCoord.x));
      }
      else if (geom instanceof Point)
      {
        Point point = (Point) geom;

        Coordinate firstCoord = point.getCoordinate();

        this.setValue(business, "latitude", String.valueOf(firstCoord.y));
        this.setValue(business, "longitude", String.valueOf(firstCoord.x));
      }
    }

    business.apply();
  }

  private void setValue(Business business, String name, Object value)
  {
    if (business.hasAttribute(name))
    {
      if (value != null)
      {
        business.setValue(name, value);
      }
      else
      {
        business.setValue(name, "");
      }
    }
  }

  @Transaction
  public void updateRecord(ServerGeoObjectIF object)
  {
    // Only working lists can be updated from changes to the graph objects
    if (this.getWorking())
    {
      object.setDate(this.getForDate());

      // Delete tile cache
      ListTileCache.deleteTiles(this);

      ListType masterlist = this.getListType();
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      Collection<Locale> locales = LocalizationFacade.getInstalledLocales();

      // Add the type ancestor fields
      ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());
      Set<ServerHierarchyType> hierarchiesOfSubTypes = type.getHierarchiesOfSubTypes();
      Map<ServerHierarchyType, List<ServerGeoObjectType>> ancestorMap = masterlist.getAncestorMap(type);
      Collection<AttributeType> attributes = type.getAttributeMap().values();

      BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
      query.WHERE(query.aCharacter(DefaultAttribute.CODE.getName()).EQ(object.getCode()));

      List<Business> records = query.getIterator().getAll();

      for (Business record : records)
      {
        try
        {
          record.appLock();

          this.publish(masterlist, type, object, record, attributes, ancestorMap, hierarchiesOfSubTypes, locales);
        }
        finally
        {
          record.unlock();
        }
      }
    }
  }

  @Transaction
  public void publishRecord(ServerGeoObjectIF object)
  {
    // Only working lists can be updated from changes to the graph objects
    if (this.getWorking())
    {
      object.setDate(this.getForDate());

      // Delete tile cache
      ListTileCache.deleteTiles(this);

      ListType masterlist = this.getListType();
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      Collection<Locale> locales = LocalizationFacade.getInstalledLocales();

      // Add the type ancestor fields
      ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());
      Map<ServerHierarchyType, List<ServerGeoObjectType>> ancestorMap = masterlist.getAncestorMap(type);
      Set<ServerHierarchyType> hierarchiesOfSubTypes = type.getHierarchiesOfSubTypes();
      Collection<AttributeType> attributes = type.getAttributeMap().values();

      Business business = new Business(mdBusiness.definesType());

      this.publish(masterlist, type, object, business, attributes, ancestorMap, hierarchiesOfSubTypes, locales);
    }
  }

  public void parse(JsonObject object)
  {
    this.parseMetadata("list", object.get(ListType.LIST_METADATA).getAsJsonObject());
    this.parseMetadata("geospatial", object.get(ListType.GEOSPATIAL_METADATA).getAsJsonObject());
  }

  private void parseMetadata(String prefix, JsonObject object)
  {
    if (object.has("visibility"))
    {
      this.setValue(prefix + "Visibility", object.get("visibility").getAsString());
    }

    if (object.has("master"))
    {
      this.setValue(prefix + "Master", object.get("master").getAsBoolean());
    }

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

  public JsonObject toJSON(boolean includeAttribute)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    String filename = this.getOid() + ".zip";
    ListType masterlist = this.getListType();
    final File directory = masterlist.getShapefileDirectory();
    final File file = new File(directory, filename);

    ServerGeoObjectType type = masterlist.getGeoObjectType();
    boolean isMember = Organization.isMember(masterlist.getOrganization());

    JsonObject object = new JsonObject();

    if (this.isAppliedToDB())
    {
      object.addProperty(ListTypeVersion.OID, this.getOid());
    }

    object.addProperty(ListType.DISPLAYLABEL, masterlist.getDisplayLabel().getValue());
    object.addProperty(ListTypeVersion.TYPE_CODE, type.getCode());
    object.addProperty(ListTypeVersion.ORG_CODE, type.getOrganization().getCode());
    object.addProperty(ListTypeVersion.LISTTYPE, masterlist.getOid());
    object.addProperty(ListTypeVersion.FORDATE, format.format(this.getForDate()));
    object.addProperty(ListTypeVersion.CREATEDATE, format.format(this.getCreateDate()));
    object.addProperty(ListTypeVersion.VERSIONNUMBER, this.getVersionNumber());
    object.addProperty(ListTypeVersion.WORKING, this.getWorking());
    object.addProperty("geometryType", type.getGeometryType().name());
    object.addProperty("isGeometryEditable", type.isGeometryEditable());
    object.addProperty("isAbstract", type.getIsAbstract());
    object.addProperty("shapefile", file.exists());
    object.addProperty("isMember", isMember);
    object.add(ListTypeVersion.PERIOD, masterlist.formatVersionLabel(this));
    object.add(ListType.LIST_METADATA, this.toMetadataJSON("list"));
    object.add(ListType.GEOSPATIAL_METADATA, this.toMetadataJSON("geospatial"));

    Progress progress = ProgressService.get(this.getOid());

    if (progress != null)
    {
      object.add("refreshProgress", progress.toJson());
    }

    if (type.getSuperType() != null)
    {
      object.addProperty("superTypeCode", type.getSuperType().getCode());
    }

    if (type.getIsAbstract())
    {
      JsonArray subtypes = new JsonArray();

      for (ServerGeoObjectType subtype : type.getSubtypes())
      {
        JsonObject jo = new JsonObject();
        jo.addProperty("code", subtype.getCode());
        jo.addProperty("label", subtype.getLabel().getValue());
        subtypes.add(jo);
      }

      object.add("subtypes", subtypes);
    }

    if (this.getPublishDate() != null)
    {
      object.addProperty(ListTypeVersion.PUBLISHDATE, format.format(this.getPublishDate()));
    }

    if (includeAttribute)
    {
      object.add(ListTypeVersion.ATTRIBUTES, this.getAttributesAsJson());
    }

    if (this.getWorking() && masterlist.doesActorHaveWritePermission())
    {
      object.add("curation", new CurationService().getListCurationInfo(this));
    }
    else
    {
      object.add("curation", new JsonObject());
    }

    return object;
  }

  private JsonObject toMetadataJSON(String prefix)
  {
    JsonObject object = new JsonObject();
    object.addProperty("visibility", this.getValue(prefix + "Visibility"));
    object.addProperty("master", Boolean.parseBoolean(this.getValue(prefix + "Master")));
    object.add("label", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Label")).toJSON());
    object.add("description", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Description")).toJSON());
    object.add("process", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Process")).toJSON());
    object.add("progress", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Progress")).toJSON());
    object.add("accessConstraints", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "AccessConstraints")).toJSON());
    object.add("useConstraints", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "UseConstraints")).toJSON());
    object.add("acknowledgements", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Acknowledgements")).toJSON());
    object.add("disclaimer", LocalizedValueConverter.convertNoAutoCoalesce((LocalStruct) this.getStruct(prefix + "Disclaimer")).toJSON());
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

  private JsonArray getAttributesAsJson()
  {
    List<ListColumn> columns = new LinkedList<ListColumn>();

    String mdBusinessId = this.getMdBusinessOid();

    if (mdBusinessId != null && mdBusinessId.length() > 0)
    {
      Set<String> attributeIds = new TreeSet<String>();

      List<ListTypeGroup> groups = ListTypeGroup.getRoots(this);
      columns.addAll(groups.stream().map(group -> group.toColumn()).collect(Collectors.toList()));
      columns.forEach(column -> attributeIds.addAll(column.getColumnsIds()));

      MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(mdBusinessId);
      List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributesOrdered();

      // Any attribute not already in a group is standalone
      for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
      {
        if (this.isValid(mdAttribute) && !attributeIds.contains(mdAttribute.getOid()))
        {
          ListAttribute attribute = new ListAttribute(mdAttribute);

          ListAttributeGroup subgroup = new ListAttributeGroup();
          subgroup.add(attribute);

          ListAttributeGroup group = new ListAttributeGroup("", mdAttribute.definesAttribute());
          group.add(subgroup);

          columns.add(group);
        }
      }
    }

    JsonArray array = columns.stream().map(m -> m.toJSON()).collect(() -> new JsonArray(), (a, e) -> a.add(e), (listA, listB) -> {
    });

    return array;
  }

  public void removeAttributeType(AttributeType attributeType)
  {
    this.removeAttributeType(this.getMdBusinessOid(), attributeType);
  }

  public void removeAttributeType(String mdBusinessOid, AttributeType attributeType)
  {
    Collection<Locale> locales = LocalizationFacade.getInstalledLocales();

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(mdBusinessOid);

    if (! ( attributeType instanceof AttributeTermType || attributeType instanceof AttributeClassificationType || attributeType instanceof AttributeLocalType ))
    {
      removeAttribute(mdBusiness, attributeType.getName());
    }
    else if (attributeType instanceof AttributeTermType || attributeType instanceof AttributeClassificationType)
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

    SerializedListTypeCache.getInstance().remove(this.getListTypeOid());
  }

  private void removeAttribute(MdBusinessDAOIF mdBusiness, String name)
  {
    MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(name);

    if (mdAttribute != null)
    {
      ListTypeAttribute.remove(mdAttribute);

      mdAttribute.getBusinessDAO().delete();
    }
  }

  public BusinessQuery buildQuery(JsonObject criteria)
  {
    return new ListTypeVersionPageQuery(this, criteria, false, false).getQuery();
  }

  public JsonArray bbox(String uid)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    String tableName = mdBusiness.getTableName();

    // collect all the views and extend the bounding box
    ValueQuery union = new ValueQuery(new QueryFactory());
    union.SELECT(union.aSQLClob(GeoserverFacade.GEOM_COLUMN, GeoserverFacade.GEOM_COLUMN, GeoserverFacade.GEOM_COLUMN));
    union.FROM(tableName, tableName);

    if (uid != null && uid.length() > 0)
    {
      MdAttributeConcreteDAOIF attribute = mdBusiness.definesAttribute(DefaultAttribute.UID.getName());
      String columName = attribute.getColumnName();
      union.WHERE(union.aSQLCharacter(columName, columName).EQ(uid));
    }

    ValueQuery collected = new ValueQuery(union.getQueryFactory());
    collected.SELECT(collected.aSQLAggregateClob("collected", "st_collect(" + GeoserverFacade.GEOM_COLUMN + ")", "collected"));
    collected.FROM("(" + union.getSQL() + ")", "unioned");

    ValueQuery outer = new ValueQuery(union.getQueryFactory());
    outer.SELECT(union.aSQLAggregateDouble("minx", "st_xmin(collected)"), union.aSQLAggregateDouble("miny", "st_ymin(collected)"), union.aSQLAggregateDouble("maxx", "st_xmax(collected)"), union.aSQLAggregateDouble("maxy", "st_ymax(collected)"));

    outer.FROM("(" + collected.getSQL() + ")", "collected");

    try (OIterator<? extends ValueObject> iter = outer.getIterator())
    {
      ValueObject o = iter.next();

      try
      {
        JsonArray bboxArr = new JsonArray();
        bboxArr.add(Double.parseDouble(o.getValue("minx")));
        bboxArr.add(Double.parseDouble(o.getValue("miny")));
        bboxArr.add(Double.parseDouble(o.getValue("maxx")));
        bboxArr.add(Double.parseDouble(o.getValue("maxy")));

        return bboxArr;
      }
      catch (JSONException ex)
      {
        throw new ProgrammingErrorException(ex);
      }
    }
    catch (Exception e)
    {
      return null;
      // throw new NoLayerDataException();
    }
  }

  public JsonArray values(String value, String attributeName, JsonObject criteria)
  {
    ValueQuery vQuery = new ValueQuery(new QueryFactory());
    BusinessQuery query = new ListTypeVersionPageQuery(this, criteria, true, false).getQuery(vQuery);

    DateFormat filterFormat = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    filterFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    JsonArray results = new JsonArray();

    vQuery.SELECT_DISTINCT(query.get(attributeName, "label"));
    vQuery.FROM(query);

    if (value != null && value.length() > 0)
    {
      vQuery.WHERE(F.UPPER(query.aCharacter(attributeName)).LIKEi("%" + value.toUpperCase() + "%"));
    }

    // vQuery.ORDER_BY_ASC(query.get(attributeName));

    try (OIterator<ValueObject> it = vQuery.getIterator(100, 1))
    {
      while (it.hasNext())
      {
        ValueObject vObject = it.next();

        results.add(vObject.getValue("label"));
        // JsonObject result = new JsonObject();
        // result.addProperty("label", vObject.getValue("label"));
        // result.addProperty("value", vObject.getValue("value"));
        //
        // results.add(result);
      }
    }

    return results;
  }

  public Page<JsonSerializable> data(JsonObject criteria, Boolean showInvalid, Boolean includeGeometries)
  {
    if (includeGeometries == null)
    {
      includeGeometries = Boolean.FALSE;
    }

    return new ListTypeVersionPageQuery(this, criteria, showInvalid, includeGeometries).getPage();
  }

  public BusinessQuery buildQuery(String filterJson)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());

    Map<MdAttributeConcreteDAOIF, Condition> conditionMap = this.buildQueryConditionsFromFilter(filterJson, null, query, mdBusiness);

    for (Condition condition : conditionMap.values())
    {
      query.WHERE(condition);
    }

    return query;
  }

  private Map<MdAttributeConcreteDAOIF, Condition> buildQueryConditionsFromFilter(String filterJson, String ignoreAttribute, ComponentQuery query, MdBusinessDAOIF mdBusiness)
  {
    Map<MdAttributeConcreteDAOIF, Condition> conditionMap = new HashMap<MdAttributeConcreteDAOIF, Condition>();

    if (filterJson != null && filterJson.length() > 0)
    {
      DateFormat filterFormat = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
      filterFormat.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      JsonArray filters = JsonParser.parseString(filterJson).getAsJsonArray();

      for (int i = 0; i < filters.size(); i++)
      {
        JsonObject filter = filters.get(i).getAsJsonObject();

        String attribute = filter.get("attribute").getAsString();

        if (ignoreAttribute == null || !attribute.equals(ignoreAttribute))
        {
          MdAttributeConcreteDAOIF mdAttr = mdBusiness.definesAttribute(attribute);

          BasicCondition condition = null;

          if (mdAttr instanceof MdAttributeMomentDAOIF)
          {
            JsonObject jObject = filter.get("value").getAsJsonObject();

            try
            {
              if (jObject.has("start") && !jObject.get("start").isJsonNull())
              {
                String date = jObject.get("start").getAsString();

                if (date.length() > 0)
                {
                  condition = query.aDateTime(attribute).GE(filterFormat.parse(date));
                }
              }

              if (jObject.has("end") && !jObject.get("end").isJsonNull())
              {
                String date = jObject.get("end").getAsString();

                if (date.length() > 0)
                {
                  condition = query.aDateTime(attribute).LE(filterFormat.parse(date));
                }
              }
            }
            catch (ParseException e)
            {
              throw new ProgrammingErrorException(e);
            }
          }
          else if (mdAttr instanceof MdAttributeBooleanDAOIF)
          {
            String value = filter.get("value").getAsString();

            Boolean bVal = Boolean.valueOf(value);

            condition = ( (AttributeBoolean) query.get(attribute) ).EQ(bVal);
          }
          else
          {
            String value = filter.get("value").getAsString();

            condition = query.get(attribute).EQ(value);
          }

          if (condition != null)
          {
            if (conditionMap.containsKey(mdAttr))
            {
              conditionMap.put(mdAttr, conditionMap.get(mdAttr).OR(condition));
            }
            else
            {
              conditionMap.put(mdAttr, condition);
            }
          }
        }
      }
    }

    return conditionMap;
  }

  public JsonObject record(String uid)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    NumberFormat numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
    query.WHERE(query.get(DefaultAttribute.UID.getName()).EQ(uid));

    ListType listType = this.getListType();

    if (this.getWorking() && listType.doesActorHaveExploratoryPermission())
    {
      ServerGeoObjectIF geoObject = new ServerGeoObjectService().getGeoObject(uid, listType.getGeoObjectType().getCode());

      if (geoObject != null)
      {
        geoObject.setDate(this.getForDate());

        JsonObject record = new JsonObject();
        record.addProperty("recordType", "GEO_OBJECT");
        record.addProperty("code", geoObject.getCode());
        record.addProperty("typeCode", geoObject.getType().getCode());
        record.addProperty("uid", uid);
        record.add("displayLabel", geoObject.getDisplayLabel().toJSON());

        return record;
      }
    }

    JsonObject record = new JsonObject();
    record.addProperty("forDate", JsonDateUtil.format(this.getForDate()));
    record.addProperty("recordType", "LIST");
    record.addProperty("version", this.getOid());
    record.addProperty("edit", this.getWorking() && listType.doesActorHaveExploratoryPermission());
    record.add("typeLabel", LocalizedValueConverter.convertNoAutoCoalesce(listType.getDisplayLabel()).toJSON());
    record.add("attributes", this.getAttributesAsJson());

    // We can't return the type of the list here because the front-end needs
    // to know the concrete type. There is a corner case here where the
    // Geo-Object could potentially not exist.
    String typeCode = new ServerGeoObjectService().getGeoObject(uid, listType.getGeoObjectType().getCode()).getType().getCode();
    record.addProperty("typeCode", typeCode);

    try (OIterator<Business> iterator = query.getIterator())
    {
      if (iterator.hasNext())
      {
        Business row = iterator.next();
        JsonObject object = new JsonObject();

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
                object.addProperty(mdAttribute.definesAttribute(), format.format((Date) value));
              }
            }
          }
        }

        record.add("data", object);
      }
    }

    JsonArray bbox = this.bbox(uid);
    if (bbox == null || bbox.size() == 0)
    {
      bbox = this.bbox(null);
    }
    record.add("bbox", bbox);

    return record;
  }

  @Transaction
  public static ListTypeVersion create(ListTypeEntry listEntry, boolean working, int versionNumber, JsonObject metadata)
  {
    ListType listType = listEntry.getListType();

    ListTypeVersion version = new ListTypeVersion();
    version.setEntry(listEntry);
    version.setListType(listType);
    version.setForDate(listEntry.getForDate());
    version.setVersionNumber(versionNumber);
    version.setWorking(working);

    if (metadata != null)
    {
      version.parse(metadata);
    }

    ServerGeoObjectType type = listType.getGeoObjectType();

    if (type.getIsPrivate() && ( version.getListVisibility().equals(ListType.PUBLIC) || version.getGeospatialVisibility().equals(ListType.PUBLIC) ))
    {
      throw new UnsupportedOperationException("A list version cannot be public if the Geo-Object Type is private");
    }

    TableMetadata tableMetadata = null;

    tableMetadata = version.createTable();

    version.setMdBusiness(tableMetadata.getMdBusiness());

    version.apply();

    if (tableMetadata != null)
    {
      // Create the Hierarchy Attribute metadata
      List<Group> groups = tableMetadata.getGroups();

      for (Group group : groups)
      {
        group.create(version, null);
      }
    }

    ListTypeVersion.assignDefaultRolePermissions(version.getMdBusiness());

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

  public static List<? extends ListTypeVersion> getAll()
  {
    ListTypeVersionQuery query = new ListTypeVersionQuery(new QueryFactory());

    try (OIterator<? extends ListTypeVersion> it = query.getIterator())
    {
      return it.getAll();
    }
  }

  public static String getMetadataLabel(String attributeName, Locale locale)
  {
    if (attributeName.equals(ListTypeVersion.LISTLABEL) || attributeName.equals(ListTypeVersion.GEOSPATIALLABEL))
    {
      return LocalizationFacade.localize("masterlist.label", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTORIGINATOR) || attributeName.equals(ListTypeVersion.GEOSPATIALORIGINATOR))
    {
      return LocalizationFacade.localize("list.type.originator", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTDESCRIPTION) || attributeName.equals(ListTypeVersion.GEOSPATIALDESCRIPTION))
    {
      return LocalizationFacade.localize("masterlist.listAbstract", locale);
    }
    else if (attributeName.equals(ListType.DESCRIPTION) || attributeName.equals(ListType.GEOSPATIALDESCRIPTION))
    {
      return LocalizationFacade.localize("masterlist.listAbstract", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTPROCESS) || attributeName.equals(ListTypeVersion.GEOSPATIALPROCESS))
    {
      return LocalizationFacade.localize("masterlist.process", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTACCESSCONSTRAINTS) || attributeName.equals(ListTypeVersion.GEOSPATIALACCESSCONSTRAINTS))
    {
      return LocalizationFacade.localize("masterlist.accessConstraints", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTUSECONSTRAINTS) || attributeName.equals(ListTypeVersion.GEOSPATIALUSECONSTRAINTS))
    {
      return LocalizationFacade.localize("masterlist.useConstraints", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTDISCLAIMER) || attributeName.equals(ListTypeVersion.GEOSPATIALDISCLAIMER))
    {
      return LocalizationFacade.localize("masterlist.disclaimer", locale);
    }
    else if (attributeName.equals(ListTypeVersion.LISTACKNOWLEDGEMENTS) || attributeName.equals(ListTypeVersion.GEOSPATIALACKNOWLEDGEMENTS))
    {
      return LocalizationFacade.localize("masterlist.acknowledgements", locale);
    }
    else
    {
      return MdBusinessDAO.getMdBusinessDAO(ListTypeVersion.CLASS).definesAttribute(attributeName).getDisplayLabel(locale);
    }
  }

}
