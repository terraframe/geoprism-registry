package net.geoprism.registry.service.business;

import java.util.Locale;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.attributes.AttributeValueException;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.registry.ChainInheritanceException;
import net.geoprism.registry.CodeLengthException;
import net.geoprism.registry.DuplicateGeoObjectTypeException;
import net.geoprism.registry.GeoObjectTypeAssignmentException;
import net.geoprism.registry.ListType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.GeometryTypeFactory;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.ChangeRequestService;
import net.geoprism.registry.service.request.SearchService;
import net.geoprism.registry.service.request.SerializedListTypeCache;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Service
@Primary
public class GPRGeoObjectTypeBusinessService extends GeoObjectTypeBusinessService implements GeoObjectTypeBusinessServiceIF
{
  @Autowired
  private GPROrganizationBusinessService gprOrgService;

  @Autowired
  private ChangeRequestService           changeService;

  @Override
  protected void delete(ServerGeoObjectType type)
  {
    super.delete(type);

    SerializedListTypeCache.getInstance().clear();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));
  }

  @Transaction
  @Override
  protected void deleteInTransaction(ServerGeoObjectType type)
  {
    super.deleteInTransaction(type);

    ListType.markAllAsInvalid(null, type);

    new SearchService().clear(type.getCode());

    changeService.markAllAsInvalid(type);
  }

  @Override
  protected void update(ServerGeoObjectType serverGeoObjectType, GeoObjectType geoObjectTypeNew)
  {
    super.update(serverGeoObjectType, geoObjectTypeNew);

    SerializedListTypeCache.getInstance().clear();
  }

  @Override
  @Transaction
  public ServerGeoObjectType create(GeoObjectType geoObjectType)
  {
    if (!isValidName(geoObjectType.getCode()))
    {
      throw new AttributeValueException("The geo object type code has an invalid character", geoObjectType.getCode());
    }

    if (geoObjectType.getCode().length() > 64)
    {
      // Setting the typename on the MdBusiness creates this limitation.
      CodeLengthException ex = new CodeLengthException();
      ex.setLength(64);
      throw ex;
    }

    String organizationCode = geoObjectType.getOrganizationCode();

    ServerOrganization organization = ServerOrganization.getByCode(organizationCode);

    if (organization != null && !organization.getEnabled())
    {
      throw new UnsupportedOperationException();
    }

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(organizationCode, geoObjectType.getIsPrivate());

    String superTypeCode = geoObjectType.getSuperTypeCode();
    Boolean isAbstract = geoObjectType.getIsAbstract();

    ServerGeoObjectType superType = null;

    if (superTypeCode != null && superTypeCode.length() > 0)
    {
      superType = ServerGeoObjectType.get(superTypeCode);
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
    RegistryLocalizedValueConverter.setOwner(universal, organizationCode);

    LocalizedValueConverter.populate(universal.getDisplayLabel(), geoObjectType.getLabel());
    LocalizedValueConverter.populate(universal.getDescription(), geoObjectType.getDescription());

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

    try
    {
      // The DuplicateDataException on code was found to be thrown here.
      // I've created a larger try/catch here just in case.
      mdBusiness.apply();

      // Add the default attributes.
      if (superType == null)
      {
        this.createDefaultAttributes(universal, mdBusiness);
      }

      universal.setMdBusiness(mdBusiness);

      universal.apply();

      GeoObjectTypeMetadata metadata = new GeoObjectTypeMetadata();
      metadata.setIsPrivate(geoObjectType.getIsPrivate());
      metadata.setUniversal(universal);
      metadata.apply();
    }
    catch (DuplicateDataException ex)
    {
      DuplicateGeoObjectTypeException ex2 = new DuplicateGeoObjectTypeException();
      ex2.setDuplicateValue(geoObjectType.getCode());
      throw ex2;
    }

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

    if (!isAbstract)
    {
      // DefaultAttribute.CODE
      MdAttributeCharacter businessCodeMdAttr = new MdAttributeCharacter();
      businessCodeMdAttr.setAttributeName(DefaultAttribute.CODE.getName());
      businessCodeMdAttr.getDisplayLabel().setValue(DefaultAttribute.CODE.getDefaultLocalizedName());
      businessCodeMdAttr.getDescription().setValue(DefaultAttribute.CODE.getDefaultDescription());
      businessCodeMdAttr.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
      businessCodeMdAttr.setDefiningMdClass(mdBusiness);
      businessCodeMdAttr.setRequired(true);
      businessCodeMdAttr.addIndexType(MdAttributeIndices.UNIQUE_INDEX);
      businessCodeMdAttr.apply();

      // DefaultAttribute.CODE
      MdAttributeCharacterDAO vertexCodeMdAttr = MdAttributeCharacterDAO.newInstance();
      vertexCodeMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.CODE.getName());
      vertexCodeMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultLocalizedName());
      vertexCodeMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultDescription());
      vertexCodeMdAttr.setValue(MdAttributeCharacterInfo.SIZE, MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
      vertexCodeMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdVertex.getOid());
      vertexCodeMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
      vertexCodeMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.UNIQUE_INDEX.getOid());
      vertexCodeMdAttr.apply();
    }

    // Build the parent class term root if it does not exist.
    TermConverter.buildIfNotExistdMdBusinessClassifier(mdBusiness);

    ServerGeoObjectType serverGeoObjectType = this.build(universal);

    return serverGeoObjectType;
  }

  @Transaction
  @Override
  public void deleteMdAttributeFromAttributeType(ServerGeoObjectType serverType, String attributeName)
  {
    Optional<AttributeType> optional = serverType.getType().getAttribute(attributeName);

    super.deleteMdAttributeFromAttributeType(serverType, attributeName);

    if (optional.isPresent())
    {
      ListType.deleteMdAttribute(serverType.getUniversal(), optional.get());
    }
  }

  public String getAdminRoleName(ServerGeoObjectType sgot)
  {
    ServerGeoObjectType superType = sgot.getSuperType();

    if (superType != null)
    {
      return gprOrgService.getRegistryAdminRoleName(superType.getOrganization());
    }

    return gprOrgService.getRegistryAdminRoleName(sgot.getOrganization());
  }

  public String getMaintainerRoleName(ServerGeoObjectType sgot)
  {
    ServerGeoObjectType superType = sgot.getSuperType();

    if (superType != null)
    {
      return getMaintainerRoleName(superType);
    }

    return RegistryRole.Type.getRM_RoleName(sgot.getOrganization().getCode(), sgot.getCode());
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

  public void assignSRAPermissions(MdGeoVertexDAO mdGeoVertexDAO, MdBusiness mdBusiness)
  {
    Roles sraRole = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);

    this.assignAllPermissions(mdBusiness, sraRole);
    this.assignAllPermissions(mdGeoVertexDAO, sraRole);
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

  @Transaction
  @Override
  public MdAttributeConcrete createMdAttributeFromAttributeType(ServerGeoObjectType serverType, AttributeType attributeType)
  {
    MdAttributeConcrete mdAttribute = super.createMdAttributeFromAttributeType(serverType, attributeType);

    ListType.createMdAttribute(serverType, attributeType);

    return mdAttribute;
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
      Roles raRole = gprOrgService.getRegistryAdminRole(organization);

      this.assignAllPermissions(mdBusiness, raRole);
      this.assignAllPermissions(mdGeoVertexDAO, raRole);
    }
  }
}
