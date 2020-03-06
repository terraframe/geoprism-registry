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
package net.geoprism.registry.conversion;

import java.util.List;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.FrequencyType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeEnumerationInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdAttributeReferenceInfo;
import com.runwaysdk.dataaccess.MdAttributeBlobDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeEncryptionDAOIF;
import com.runwaysdk.dataaccess.MdAttributeFileDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIndicatorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLocalDAOIF;
import com.runwaysdk.dataaccess.MdAttributeStructDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.MdLocalStructDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeEnumerationDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeLocalCharacterEmbeddedDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeUUIDDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.mapping.GeoserverFacade;
import com.runwaysdk.system.gis.metadata.MdAttributeGeometry;
import com.runwaysdk.system.gis.metadata.MdAttributeLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPoint;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPolygon;
import com.runwaysdk.system.gis.metadata.MdAttributePoint;
import com.runwaysdk.system.gis.metadata.MdAttributePolygon;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeLocalCharacter;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdAttributeUUID;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;

import net.geoprism.DefaultConfiguration;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.InvalidMasterListCodeException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.WMSService;

public class ServerGeoObjectTypeConverter extends LocalizedValueConverter
{
  /**
   * Adds default attributes to the given {@link MdBusinessDAO} according to the
   * Common Geo-Registry specification for {@link GeoObject}.
   * 
   * @param mdBusinessDAO
   *          {@link MdBusinessDAO} that will define the default attributes.
   */
  @Transaction
  public void createDefaultAttributes(Universal universal, MdBusiness definingMdBusiness)
  {
    MdBusiness mdBusGeoEntity = MdBusiness.getMdBusiness(GeoEntity.CLASS);

    if (!universal.getIsLeafType())
    {
      MdAttributeReference geoEntRefMdAttrRef = new MdAttributeReference();
      geoEntRefMdAttrRef.setAttributeName(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      geoEntRefMdAttrRef.getDisplayLabel().setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_LABEL);
      geoEntRefMdAttrRef.getDescription().setValue("References a GeoEntity for non-leaf Universal Types");
      geoEntRefMdAttrRef.setMdBusiness(mdBusGeoEntity);
      geoEntRefMdAttrRef.setDefiningMdClass(definingMdBusiness);
      geoEntRefMdAttrRef.setRequired(false);
      geoEntRefMdAttrRef.apply();
    }

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from
    // the {@code GeoObject#OID};
    MdAttributeUUID uuidMdAttr = new MdAttributeUUID();
    uuidMdAttr.setAttributeName(RegistryConstants.UUID);
    uuidMdAttr.getDisplayLabel().setValue(RegistryConstants.UUID_LABEL);
    uuidMdAttr.getDescription().setValue("The universal unique identifier of the feature.");
    uuidMdAttr.setDefiningMdClass(definingMdBusiness);
    uuidMdAttr.setRequired(true);
    uuidMdAttr.addIndexType(MdAttributeIndices.UNIQUE_INDEX);
    uuidMdAttr.apply();

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    MdAttributeCharacter codeMdAttr = new MdAttributeCharacter();
    codeMdAttr.setAttributeName(DefaultAttribute.CODE.getName());
    codeMdAttr.getDisplayLabel().setValue(DefaultAttribute.CODE.getDefaultLocalizedName());
    codeMdAttr.getDescription().setValue(DefaultAttribute.CODE.getDefaultDescription());
    codeMdAttr.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
    codeMdAttr.setDefiningMdClass(definingMdBusiness);
    codeMdAttr.setRequired(true);
    codeMdAttr.addIndexType(MdAttributeIndices.UNIQUE_INDEX);
    codeMdAttr.apply();

    // DefaultAttribute.TYPE - This is the display label of the Universal.
    // BusObject.mdBusiness.Universal.displayLabel

    // DefaultAttribute.CREATED_DATE - The create data on the Business Object?

    // DefaultAttribute.UPDATED_DATE - The update data on the Business Object?

    // DefaultAttribute.STATUS

    MdEnumeration geoObjectStatusEnum = MdEnumeration.getMdEnumeration(GeoObjectStatus.CLASS);

    MdAttributeEnumeration objStatusNdAttrEnum = new MdAttributeEnumeration();
    objStatusNdAttrEnum.setAttributeName(DefaultAttribute.STATUS.getName());
    objStatusNdAttrEnum.getDisplayLabel().setValue(DefaultAttribute.STATUS.getDefaultLocalizedName());
    objStatusNdAttrEnum.getDescription().setValue(DefaultAttribute.STATUS.getDefaultDescription());
    objStatusNdAttrEnum.setRequired(true);
    objStatusNdAttrEnum.setMdEnumeration(geoObjectStatusEnum);
    objStatusNdAttrEnum.setSelectMultiple(false);
    objStatusNdAttrEnum.setDefiningMdClass(definingMdBusiness);
    objStatusNdAttrEnum.apply();

    if (universal.getIsLeafType())
    {
      // DefaultAttribute.DISPLAY_LABEL
      MdAttributeLocalCharacter labelMdAttr = new MdAttributeLocalCharacter();
      labelMdAttr.setAttributeName(DefaultAttribute.DISPLAY_LABEL.getName());
      labelMdAttr.getDisplayLabel().setValue(DefaultAttribute.DISPLAY_LABEL.getDefaultLocalizedName());
      labelMdAttr.getDescription().setValue(DefaultAttribute.DISPLAY_LABEL.getDefaultDescription());
      labelMdAttr.setDefiningMdClass(definingMdBusiness);
      labelMdAttr.setRequired(true);
      labelMdAttr.apply();

      com.runwaysdk.system.gis.geo.GeometryType geometryType = universal.getGeometryType().get(0);

      MdAttributeGeometry mdAttributeGeometry;

      if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POINT))
      {
        mdAttributeGeometry = new MdAttributePoint();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_POINT_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.LINE))
      {
        mdAttributeGeometry = new MdAttributeLineString();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_LINE_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POLYGON))
      {
        mdAttributeGeometry = new MdAttributePolygon();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_POLYGON_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT))
      {
        mdAttributeGeometry = new MdAttributeMultiPoint();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTIPOINT_ATTRIBUTE_LABEL);

      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTILINE))
      {
        mdAttributeGeometry = new MdAttributeMultiLineString();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTILINE_ATTRIBUTE_LABEL);

      }
      else // geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON
      {
        mdAttributeGeometry = new MdAttributeMultiPolygon();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTIPOLYGON_ATTRIBUTE_LABEL);
      }

      mdAttributeGeometry.setAttributeName(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
      mdAttributeGeometry.setRequired(false);
      mdAttributeGeometry.setDefiningMdClass(definingMdBusiness);
      mdAttributeGeometry.setSrid(GeoserverFacade.SRS_CODE);
      mdAttributeGeometry.apply();
    }
  }

  @Transaction
  public void createDefaultAttributes(Universal universal, MdGraphClassDAOIF mdClass)
  {
    MdAttributeUUIDDAO uuidMdAttr = MdAttributeUUIDDAO.newInstance();
    uuidMdAttr.setValue(MdAttributeConcreteInfo.NAME, RegistryConstants.UUID);
    uuidMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, RegistryConstants.UUID_LABEL);
    uuidMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, RegistryConstants.UUID_LABEL);
    uuidMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
    uuidMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
    uuidMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.UNIQUE_INDEX.getOid());
    uuidMdAttr.apply();

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    MdAttributeCharacterDAO codeMdAttr = MdAttributeCharacterDAO.newInstance();
    codeMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.CODE.getName());
    codeMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultLocalizedName());
    codeMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultDescription());
    codeMdAttr.setValue(MdAttributeCharacterInfo.SIZE, MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
    codeMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
    codeMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
    codeMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.UNIQUE_INDEX.getOid());
    codeMdAttr.apply();

    MdEnumeration geoObjectStatusEnum = MdEnumeration.getMdEnumeration(GeoObjectStatus.CLASS);

    MdAttributeEnumerationDAO objStatusNdAttrEnum = MdAttributeEnumerationDAO.newInstance();
    objStatusNdAttrEnum.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.STATUS.getName());
    objStatusNdAttrEnum.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.STATUS.getDefaultLocalizedName());
    objStatusNdAttrEnum.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.STATUS.getDefaultDescription());
    objStatusNdAttrEnum.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
    objStatusNdAttrEnum.setValue(MdAttributeEnumerationInfo.MD_ENUMERATION, geoObjectStatusEnum.getOid());
    objStatusNdAttrEnum.setValue(MdAttributeEnumerationInfo.SELECT_MULTIPLE, MdAttributeBooleanInfo.FALSE);
    objStatusNdAttrEnum.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
    objStatusNdAttrEnum.apply();

    // DefaultAttribute.DISPLAY_LABEL
    MdAttributeLocalCharacterEmbeddedDAO labelMdAttr = MdAttributeLocalCharacterEmbeddedDAO.newInstance();
    labelMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.DISPLAY_LABEL.getName());
    labelMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.DISPLAY_LABEL.getDefaultLocalizedName());
    labelMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.DISPLAY_LABEL.getDefaultDescription());
    labelMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
    labelMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
    labelMdAttr.apply();
  }

  @Transaction
  public ServerGeoObjectType create(String json)
  {
    GeoObjectType geoObjectType = GeoObjectType.fromJSON(json, ServiceFactory.getAdapter());

    return this.create(geoObjectType);
  }

  @Transaction
  public ServerGeoObjectType create(GeoObjectType geoObjectType)
  {
    if (!MasterList.isValidName(geoObjectType.getCode()))
    {
      throw new InvalidMasterListCodeException("The geo object type code has an invalid character");
    }

    Universal universal = new Universal();
    universal.setUniversalId(geoObjectType.getCode());
    universal.setIsLeafType(geoObjectType.isLeaf());
    universal.setIsGeometryEditable(geoObjectType.isGeometryEditable());

    populate(universal.getDisplayLabel(), geoObjectType.getLabel());
    populate(universal.getDescription(), geoObjectType.getDescription());

    com.runwaysdk.system.gis.geo.GeometryType geometryType = GeometryTypeFactory.get(geoObjectType.getGeometryType());

    // Clear the default value
    universal.clearGeometryType();
    universal.addGeometryType(geometryType);

    MdBusiness mdBusiness = new MdBusiness();
    mdBusiness.setPackageName(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE);
    // The CODE name becomes the class name
    mdBusiness.setTypeName(universal.getUniversalId());
    mdBusiness.setGenerateSource(false);
    mdBusiness.setPublish(false);
    mdBusiness.setIsAbstract(false);
    mdBusiness.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, universal.getDisplayLabel().getValue());
    mdBusiness.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, universal.getDescription().getValue());
    mdBusiness.apply();

    // Add the default attributes.
    this.createDefaultAttributes(universal, mdBusiness);

    universal.setMdBusiness(mdBusiness);

    universal.apply();

    // Create the permissions for the new MdBusiness
    assignDefaultRolePermissions(mdBusiness);

    if (geoObjectType.isLeaf())
    {
      MdBusinessDAOIF mdBusinessDAO = MdBusinessDAO.get(mdBusiness.getOid());
      MdAttributeLocalDAOIF displayLabel = (MdAttributeLocalDAOIF) mdBusinessDAO.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName());
      MdLocalStructDAOIF mdStruct = displayLabel.getMdStructDAOIF();

      assignDefaultRolePermissions(mdStruct);
    }

    // Create the MdGeoVertexClass
    MdGeoVertexDAO mdVertex = GeoVertexType.create(universal.getUniversalId());
    this.createDefaultAttributes(universal, mdVertex);

    assignDefaultRolePermissions(mdVertex);

    // Build the parent class term root if it does not exist.
    TermConverter.buildIfNotExistdMdBusinessClassifier(mdBusiness);

    ServerGeoObjectType serverGeoObjectType = this.build(universal);

    new WMSService().createDatabaseView(serverGeoObjectType, true);

    return serverGeoObjectType;
  }

  public void assignDefaultRolePermissions(ComponentIF component)
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

    // // TODO: Actual hierarchy role
    // RoleDAO hierarchyRole =
    // RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_PREFIX +
    // "LocatedIn").getBusinessDAO();
    // hierarchyRole.grantPermission(Operation.CREATE, mdBusiness.getOid());
    // hierarchyRole.grantPermission(Operation.DELETE, mdBusiness.getOid());
    // hierarchyRole.grantPermission(Operation.WRITE, mdBusiness.getOid());
    // hierarchyRole.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
  }

  public ServerGeoObjectType build(Universal universal)
  {
    MdBusiness mdBusiness = universal.getMdBusiness();
    MdGeoVertexDAO mdVertex = GeoVertexType.getMdGeoVertex(universal.getUniversalId());
    com.runwaysdk.system.gis.geo.GeometryType geoPrismgeometryType = universal.getGeometryType().get(0);

    org.commongeoregistry.adapter.constants.GeometryType cgrGeometryType = GeometryTypeFactory.get(geoPrismgeometryType);

    LocalizedValue label = convert(universal.getDisplayLabel());
    LocalizedValue description = convert(universal.getDescription());
    GeoObjectType geoObjType = new GeoObjectType(universal.getUniversalId(), cgrGeometryType, label, description, universal.getIsLeafType(), universal.getIsGeometryEditable(), ServiceFactory.getAdapter());

    geoObjType = this.convertAttributeTypes(universal, geoObjType, mdBusiness);

    return new ServerGeoObjectType(geoObjType, universal, mdBusiness, mdVertex);
  }

  public GeoObjectType convertAttributeTypes(Universal uni, GeoObjectType gt, MdBusiness mdBusiness)
  {
    if (mdBusiness != null)
    {
      MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

      // Standard attributes are defined by default on the GeoObjectType

      AttributeTypeConverter builder = new AttributeTypeConverter();

      List<? extends MdAttributeConcreteDAOIF> definedMdAttributeList = mdBusinessDAOIF.getAllDefinedMdAttributes();

      for (MdAttributeConcreteDAOIF mdAttribute : definedMdAttributeList)
      {
        if (this.convertMdAttributeToAttributeType(mdAttribute))
        {
          AttributeType attributeType = builder.build(mdAttribute);

          if (attributeType != null)
          {
            gt.addAttribute(attributeType);
          }
        }
      }
    }

    return gt;
  }

  /**
   * True if the given {@link MdAttributeConcreteDAOIF} should be converted to
   * an {@link AttributeType}, false otherwise. Standard attributes such as
   * {@link DefaultAttribute} are already defined on a {@link GeoObjectType} and
   * do not need to be converted. This method also returns true if the attribute
   * is not a system attribute.
   * 
   * @return True if the given {@link MdAttributeConcreteDAOIF} should be
   *         converted to an {@link AttributeType}, false otherwise.
   */
  private boolean convertMdAttributeToAttributeType(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() || mdAttribute instanceof MdAttributeStructDAOIF || mdAttribute instanceof MdAttributeEncryptionDAOIF || mdAttribute instanceof MdAttributeIndicatorDAOIF || mdAttribute instanceof MdAttributeBlobDAOIF || mdAttribute instanceof MdAttributeGeometryDAOIF || mdAttribute instanceof MdAttributeFileDAOIF || mdAttribute instanceof MdAttributeTimeDAOIF || mdAttribute instanceof MdAttributeUUIDDAOIF || mdAttribute.getType().equals(MdAttributeReferenceInfo.CLASS))
    {
      return false;
    }
    else if (mdAttribute.definesAttribute().equals(ComponentInfo.KEY) || mdAttribute.definesAttribute().equals(ComponentInfo.TYPE))
    {
      return false;
    }
    else
    {
      return true;
    }
  }

}
