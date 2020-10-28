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
package net.geoprism.registry.conversion;

import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

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
import com.runwaysdk.constants.MdAttributeLocalCharacterEmbeddedInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdAttributeReferenceInfo;
import com.runwaysdk.dataaccess.MdAttributeBlobDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeEncryptionDAOIF;
import com.runwaysdk.dataaccess.MdAttributeFileDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIndicatorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeStructDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeEnumerationDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeLocalCharacterEmbeddedDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeUUIDDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.metadata.graph.MdGeoVertex;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeUUID;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;

import net.geoprism.registry.ChainInheritanceException;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeoObjectTypeAssignmentException;
import net.geoprism.registry.InvalidMasterListCodeException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;

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
    // MdBusiness mdBusGeoEntity = MdBusiness.getMdBusiness(GeoEntity.CLASS);

    if (!universal.getIsLeafType())
    {
      // MdAttributeReference geoEntRefMdAttrRef = new MdAttributeReference();
      // geoEntRefMdAttrRef.setAttributeName(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      // geoEntRefMdAttrRef.getDisplayLabel().setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_LABEL);
      // geoEntRefMdAttrRef.getDescription().setValue("References a GeoEntity
      // for non-leaf Universal Types");
      // geoEntRefMdAttrRef.setMdBusiness(mdBusGeoEntity);
      // geoEntRefMdAttrRef.setDefiningMdClass(definingMdBusiness);
      // geoEntRefMdAttrRef.setRequired(false);
      // geoEntRefMdAttrRef.apply();
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
    labelMdAttr.setValue(MdAttributeLocalCharacterEmbeddedInfo.NAME, DefaultAttribute.DISPLAY_LABEL.getName());
    labelMdAttr.setStructValue(MdAttributeLocalCharacterEmbeddedInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.DISPLAY_LABEL.getDefaultLocalizedName());
    labelMdAttr.setStructValue(MdAttributeLocalCharacterEmbeddedInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.DISPLAY_LABEL.getDefaultDescription());
    labelMdAttr.setValue(MdAttributeLocalCharacterEmbeddedInfo.DEFINING_MD_CLASS, mdClass.getOid());
    labelMdAttr.setValue(MdAttributeLocalCharacterEmbeddedInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
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

    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(Session.getCurrentSession().getUser(), geoObjectType.getOrganizationCode());
    }

    String parentTypeCode = geoObjectType.getParentTypeCode();
    Boolean isAbstract = geoObjectType.getIsAbstract();

    ServerGeoObjectType superType = null;

    if (parentTypeCode != null && parentTypeCode.length() > 0)
    {
      superType = ServerGeoObjectType.get(parentTypeCode);
      geoObjectType.setGeometryType(superType.getGeometryType());
    }

    if (isAbstract && superType != null)
    {
      throw new ChainInheritanceException();
    }

    if (superType != null && !superType.getIsAbstract())
    {
      throw new GeoObjectTypeAssignmentException();
    }

    Universal universal = new Universal();
    universal.setUniversalId(geoObjectType.getCode());
    universal.setIsLeafType(false);
    universal.setIsGeometryEditable(geoObjectType.isGeometryEditable());

    // Set the owner of the universal to the id of the corresponding role of the
    // responsible organization.
    String organizationCode = geoObjectType.getOrganizationCode();
    setOwner(universal, organizationCode);

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
    mdBusiness.setIsAbstract(isAbstract);
    mdBusiness.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, universal.getDisplayLabel().getValue());
    mdBusiness.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, universal.getDescription().getValue());

    if (superType != null)
    {
      mdBusiness.setSuperMdBusiness(superType.getMdBusiness());
    }
    else
    {
      mdBusiness.setPublish(false);
    }

    mdBusiness.apply();

    // Add the default attributes.
    if (superType == null)
    {
      this.createDefaultAttributes(universal, mdBusiness);
    }

    universal.setMdBusiness(mdBusiness);

    universal.apply();

    // Create the MdGeoVertexClass
    MdGeoVertexDAO mdVertex = GeoVertexType.create(universal.getUniversalId(), universal.getOwnerOid(), isAbstract, superType);

    if (superType == null)
    {
      this.createDefaultAttributes(universal, mdVertex);

      assignSRAPermissions(mdVertex, mdBusiness);

      assignAll_RA_Permissions(mdVertex, mdBusiness, organizationCode);
      create_RM_GeoObjectTypeRole(mdVertex, organizationCode, geoObjectType.getCode());
      assign_RM_GeoObjectTypeRole(mdVertex, mdBusiness, organizationCode, geoObjectType.getCode());

      create_RC_GeoObjectTypeRole(mdVertex, organizationCode, geoObjectType.getCode());
      assign_RC_GeoObjectTypeRole(mdVertex, mdBusiness, organizationCode, geoObjectType.getCode());

      create_AC_GeoObjectTypeRole(mdVertex, organizationCode, geoObjectType.getCode());
      assign_AC_GeoObjectTypeRole(mdVertex, mdBusiness, organizationCode, geoObjectType.getCode());
    }

    // Build the parent class term root if it does not exist.
    TermConverter.buildIfNotExistdMdBusinessClassifier(mdBusiness);

    ServerGeoObjectType serverGeoObjectType = this.build(universal);

    return serverGeoObjectType;
  }

  private void create_AC_GeoObjectTypeRole(MdGeoVertexDAO mdGeoVertexDAO, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String acRoleName = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectTypeCode);

      Locale locale = Session.getCurrentLocale();
      String defaultDisplayLabel = mdGeoVertexDAO.getLocalValue(MdGeoVertexInfo.DISPLAY_LABEL, locale) + " API Consumer";

      Roles acOrgRole = new Roles();
      acOrgRole.setRoleName(acRoleName);
      acOrgRole.getDisplayLabel().setDefaultValue(defaultDisplayLabel);
      acOrgRole.apply();

      String orgRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);
      Roles orgRole = Roles.findRoleByName(orgRoleName);

      RoleDAO orgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(orgRole);
      RoleDAO acOrgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(acOrgRole);
      orgRoleDAO.addInheritance(acOrgRoleDAO);

      // Inherit the permissions from the root RC role
      RoleDAO rootAC_DAO = (RoleDAO) BusinessFacade.getEntityDAO(Roles.findRoleByName(RegistryConstants.API_CONSUMER_ROLE));
      rootAC_DAO.addInheritance(acOrgRoleDAO);
    }
  }

  private void assign_AC_GeoObjectTypeRole(MdGeoVertexDAO mdGeoVertexDAO, MdBusiness mdBusiness, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectTypeCode);

      RoleDAO rmRole = RoleDAO.findRole(rmRoleName).getBusinessDAO();

      rmRole.grantPermission(Operation.READ, mdGeoVertexDAO.getOid());
      rmRole.grantPermission(Operation.READ_ALL, mdGeoVertexDAO.getOid());

      rmRole.grantPermission(Operation.READ, mdBusiness.getOid());
      rmRole.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
    }
  }

  private void create_RC_GeoObjectTypeRole(MdGeoVertexDAO mdGeoVertexDAO, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rcRoleName = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectTypeCode);

      Locale locale = Session.getCurrentLocale();
      String defaultDisplayLabel = mdGeoVertexDAO.getLocalValue(MdGeoVertexInfo.DISPLAY_LABEL, locale) + " Registry Contributor";

      Roles rcOrgRole = new Roles();
      rcOrgRole.setRoleName(rcRoleName);
      rcOrgRole.getDisplayLabel().setDefaultValue(defaultDisplayLabel);
      rcOrgRole.apply();

      String orgRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);
      Roles orgRole = Roles.findRoleByName(orgRoleName);

      RoleDAO orgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(orgRole);
      RoleDAO rcOrgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(rcOrgRole);
      orgRoleDAO.addInheritance(rcOrgRoleDAO);

      // Inherit the permissions from the root RC role
      RoleDAO rootRC_DAO = (RoleDAO) BusinessFacade.getEntityDAO(Roles.findRoleByName(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE));
      rootRC_DAO.addInheritance(rcOrgRoleDAO);
    }
  }

  private void assign_RC_GeoObjectTypeRole(MdGeoVertexDAO mdGeoVertexDAO, MdBusiness mdBusiness, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectTypeCode);

      RoleDAO rmRole = RoleDAO.findRole(rmRoleName).getBusinessDAO();

      rmRole.grantPermission(Operation.READ, mdGeoVertexDAO.getOid());
      rmRole.grantPermission(Operation.READ_ALL, mdGeoVertexDAO.getOid());

      rmRole.grantPermission(Operation.READ, mdBusiness.getOid());
      rmRole.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
    }
  }

  private void create_RM_GeoObjectTypeRole(MdGeoVertexDAO mdGeoVertexDAO, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);

      Locale locale = Session.getCurrentLocale();
      String defaultDisplayLabel = mdGeoVertexDAO.getLocalValue(MdGeoVertexInfo.DISPLAY_LABEL, locale) + " Registry Maintainer";

      Roles rmOrgRole = new Roles();
      rmOrgRole.setRoleName(rmRoleName);
      rmOrgRole.getDisplayLabel().setDefaultValue(defaultDisplayLabel);
      rmOrgRole.apply();

      String orgRoleName = RegistryRole.Type.getRootOrgRoleName(organizationCode);
      Roles orgRole = Roles.findRoleByName(orgRoleName);

      RoleDAO orgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(orgRole);
      RoleDAO rmOrgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(rmOrgRole);
      orgRoleDAO.addInheritance(rmOrgRoleDAO);

      // Inherit the permissions from the root RM role
      RoleDAO rootRM_DAO = (RoleDAO) BusinessFacade.getEntityDAO(Roles.findRoleByName(RegistryConstants.REGISTRY_MAINTAINER_ROLE));
      rootRM_DAO.addInheritance(rmOrgRoleDAO);
    }
  }

  private void assign_RM_GeoObjectTypeRole(MdGeoVertexDAO mdGeoVertexDAO, MdBusiness mdBusiness, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);

      Roles rmRole = Roles.findRoleByName(rmRoleName);

      this.assignAllPermissions(mdBusiness, rmRole);
      this.assignAllPermissions(mdGeoVertexDAO, rmRole);
    }
  }

  /**
   * Assigns all permissions to the Organization's RA
   * 
   * @param mdGeoVertexDAO
   * @param mdBusiness
   * @param organizationCode
   */
  private void assignAll_RA_Permissions(MdGeoVertexDAO mdGeoVertexDAO, MdBusiness mdBusiness, String organizationCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      Organization organization = Organization.getByKey(organizationCode);
      Roles raRole = organization.getRegistryAdminiRole();

      this.assignAllPermissions(mdBusiness, raRole);
      this.assignAllPermissions(mdGeoVertexDAO, raRole);
    }
  }

  /**
   * Assigns all permissions to the {@link ComponentIF} to the given role.
   * 
   * Precondition: component is either a {@link MdGeoVertex} or a
   * {@link MdBusiness}.
   * 
   * @param component
   * @param role
   */
  private void assignAllPermissions(ComponentIF component, Roles role)
  {
    RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
    roleDAO.grantPermission(Operation.CREATE, component.getOid());
    roleDAO.grantPermission(Operation.DELETE, component.getOid());
    roleDAO.grantPermission(Operation.WRITE, component.getOid());
    roleDAO.grantPermission(Operation.WRITE_ALL, component.getOid());
  }

  public void assignSRAPermissions(MdGeoVertexDAO mdGeoVertexDAO, MdBusiness mdBusiness)
  {
    Roles sraRole = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);

    this.assignAllPermissions(mdBusiness, sraRole);
    this.assignAllPermissions(mdGeoVertexDAO, sraRole);
  }

  public ServerGeoObjectType build(Universal universal)
  {
    MdBusiness mdBusiness = universal.getMdBusiness();
    MdGeoVertexDAO mdVertex = GeoVertexType.getMdGeoVertex(universal.getUniversalId());
    com.runwaysdk.system.gis.geo.GeometryType geoPrismgeometryType = universal.getGeometryType().get(0);

    org.commongeoregistry.adapter.constants.GeometryType cgrGeometryType = GeometryTypeFactory.get(geoPrismgeometryType);

    LocalizedValue label = convert(universal.getDisplayLabel());
    LocalizedValue description = convert(universal.getDescription());

    String ownerActerOid = universal.getOwnerOid();

    String organizationCode = Organization.getRootOrganizationCode(ownerActerOid);

    MdGeoVertexDAOIF superType = mdVertex.getSuperClass();

    GeoObjectType geoObjType = new GeoObjectType(universal.getUniversalId(), cgrGeometryType, label, description, universal.getIsGeometryEditable(), organizationCode, ServiceFactory.getAdapter());
    geoObjType.setIsAbstract(mdBusiness.getIsAbstract());

    if (superType != null && !superType.definesType().equals(GeoVertex.CLASS))
    {
      ServerGeoObjectType parentType = ServerGeoObjectType.get(superType);

      geoObjType.setParentTypeCode(parentType.getCode());
    }

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
