package net.geoprism.registry.service.business;

import java.util.Locale;
import java.util.Optional;

import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.registry.ListType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.action.AllGovernanceStatus;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.request.SearchService;
import net.geoprism.registry.service.request.SerializedListTypeCache;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Service
@Primary
public class GPRGeoObjectTypeBusinessService extends GeoObjectTypeBusinessService implements GeoObjectTypeBusinessServiceIF
{
  @Autowired
  private GPROrganizationBusinessService gprOrgService;

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

    this.markAllAsInvalid(type);
  }

  @Override
  protected GeoObjectType update(ServerGeoObjectType serverGeoObjectType, GeoObjectType geoObjectTypeNew)
  {
    GeoObjectType dto = super.update(serverGeoObjectType, geoObjectTypeNew);

    SerializedListTypeCache.getInstance().clear();

    return dto;
  }

  @Override
  @Transaction
  public ServerGeoObjectType create(GeoObjectType dto)
  {
    ServerGeoObjectType type = super.create(dto);

    ServerGeoObjectType superType = type.getSuperType();

    if (superType == null)
    {
      MdVertex mdVertex = type.getType().getMdVertex();
      String organizationCode = type.getOrganizationCode();

      assignSRAPermissions(mdVertex);

      assignAll_RA_Permissions(mdVertex, organizationCode);
      create_RM_GeoObjectTypeRole(type, organizationCode, dto.getCode());
      assign_RM_GeoObjectTypeRole(mdVertex, organizationCode, dto.getCode());

      create_RC_GeoObjectTypeRole(type, organizationCode, dto.getCode());
      assign_RC_GeoObjectTypeRole(mdVertex, organizationCode, dto.getCode());

      create_AC_GeoObjectTypeRole(type, organizationCode, dto.getCode());
      assign_AC_GeoObjectTypeRole(mdVertex, organizationCode, dto.getCode());
    }

    return type;
  }

  @Override
  @Transaction
  public void removeAttribute(ServerGeoObjectType serverType, String attributeName)
  {
    Optional<net.geoprism.registry.graph.AttributeType> optional = serverType.getAttribute(attributeName);

    super.removeAttribute(serverType, attributeName);

    if (optional.isPresent())
    {
      ListType.deleteMdAttribute(serverType, optional.get());
    }
  }

  public String getAdminRoleName(ServerGeoObjectType sgot)
  {
    ServerGeoObjectType superType = sgot.getSuperType();

    if (superType != null)
    {
      return gprOrgService.getRegistryAdminRoleName(superType.getOrganization().getOrganization());
    }

    return gprOrgService.getRegistryAdminRoleName(sgot.getOrganization().getOrganization());
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

  private void create_AC_GeoObjectTypeRole(ServerGeoObjectType type, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String acRoleName = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectTypeCode);

      Locale locale = Session.getCurrentLocale();
      String defaultDisplayLabel = type.getLabel().getValue(locale) + " API Consumer";

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

  private void assign_AC_GeoObjectTypeRole(ComponentIF component, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectTypeCode);

      RoleDAO rmRole = RoleDAO.findRole(rmRoleName).getBusinessDAO();

      rmRole.grantPermission(Operation.READ, component.getOid());
      rmRole.grantPermission(Operation.READ_ALL, component.getOid());
    }
  }

  private void create_RC_GeoObjectTypeRole(ServerGeoObjectType type, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rcRoleName = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectTypeCode);

      Locale locale = Session.getCurrentLocale();
      String defaultDisplayLabel = type.getLabel().getValue(locale) + " Registry Contributor";

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

  private void assign_RC_GeoObjectTypeRole(ComponentIF component, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectTypeCode);

      RoleDAO rmRole = RoleDAO.findRole(rmRoleName).getBusinessDAO();

      rmRole.grantPermission(Operation.READ, component.getOid());
      rmRole.grantPermission(Operation.READ_ALL, component.getOid());
    }
  }

  private void create_RM_GeoObjectTypeRole(ServerGeoObjectType type, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);

      Locale locale = Session.getCurrentLocale();
      String defaultDisplayLabel = type.getLabel().getValue(locale) + " Registry Maintainer";

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

  public void assignSRAPermissions(ComponentIF mdGeoVertexDAO)
  {
    Roles sraRole = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);

    this.assignAllPermissions(mdGeoVertexDAO, sraRole);
  }

  private void assign_RM_GeoObjectTypeRole(ComponentIF component, String organizationCode, String geoObjectTypeCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      String rmRoleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);

      Roles rmRole = Roles.findRoleByName(rmRoleName);

      this.assignAllPermissions(component, rmRole);
    }
  }

  @Override
  @Transaction
  public net.geoprism.registry.graph.AttributeType createAttributeTypeFromDTO(ServerGeoObjectType type, AttributeType dto)
  {
    net.geoprism.registry.graph.AttributeType attributeType = super.createAttributeTypeFromDTO(type, dto);

    ListType.createMdAttribute(type, attributeType);

    return attributeType;
  }

  /**
   * Assigns all permissions to the Organization's RA
   * 
   * @param mdGeoVertexDAO
   * @param mdBusiness
   * @param organizationCode
   */
  private void assignAll_RA_Permissions(ComponentIF mdGeoVertexDAO, String organizationCode)
  {
    if (organizationCode != null && !organizationCode.trim().equals(""))
    {
      Organization organization = Organization.getByKey(organizationCode);
      Roles raRole = gprOrgService.getRegistryAdminRole(organization);

      this.assignAllPermissions(mdGeoVertexDAO, raRole);
    }
  }
  
  @Transaction
  public void markAllAsInvalid(ServerGeoObjectType type)
  {
    String reason = LocalizationFacade.localize("changeRequest.invalidate.deleteReferencedGeoObjectType");

    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    crq.WHERE(crq.getApprovalStatus().containsExactly(AllGovernanceStatus.PENDING));

    try (OIterator<? extends ChangeRequest> it = crq.getIterator())
    {
      for (ChangeRequest cr : it)
      {
        if (cr.getGeoObjectTypeCode().equals(type.getCode()))
        {
          cr.invalidate(reason);
        }
      }
    }
  }


}
