package net.geoprism.registry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.geoprism.DefaultConfiguration;
import net.geoprism.registry.conversion.LocalizedValueConverter;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.dataaccess.EntityDAOIF;
import com.runwaysdk.dataaccess.cache.ObjectCache;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionIF;
import com.runwaysdk.system.Actor;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.Universal;


public class Organization extends OrganizationBase
{
  private static final long serialVersionUID = -640706555;

  public Organization()
  {
    super();
  }

  /**
   * Builds the this object's key name.
   */
  @Override
  public String buildKey()
  {
    return this.getCode();
  }

  /**
   * Returns the [@link Organization} with the given code
   * 
   * @return
   */
  public static Organization getByCode(String code)
  {
    return getByKey(code);
  }

  /**
   * Creates a {@link RoleDAO} for this {@link Organization} and a Registry
   * Administrator {@link RoleDAO} for this {@link Organization}.
   */
  public void apply()
  {
    super.apply();

    if (this.isNew())
    {
      this.createOrganizationRole();
      this.createRegistryAdminOrganizationRole();
    }
  }

  /**
   * Removes the {@link RoleDAO}s for this {@link Organization} and Registry
   * Administrator for this {@link Organization}.
   */
  public void delete()
  {
    try
    {
      Roles raOrgRole = this.getRegistryAdminiRole();
      raOrgRole.delete();
    }
    // Heads up: clean up
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
    }
    try
    {
      Roles orgRole = this.getRole();
      orgRole.delete();
    }
    // Heads up: clean up
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
    }

    super.delete();
  }

  /**
   * Returns the role name for this {@link Organization}.
   * 
   * @return role name for this {@link Organization}.
   */
  public String getRoleName()
  {
    return RegistryRole.Type.getRootOrgRoleName(this.getCode());
  }

  /**
   * Returns the {@link Roles} for this {@link Organization}.
   * 
   * @return the {@link Roles} for this {@link Organization}.
   */
  public Roles getRole()
  {
    return Roles.findRoleByName(this.getRoleName());
  }

  /**
   * Returns the {@link RoleDAOIF} for the {@link Organization} with the given
   * code.
   * 
   * @param organizationCode
   * 
   * @return the {@link RoleDAOIF} for the {@link Organization} with the given
   *         code.
   */
  public static RoleDAOIF getRole(String organizationCode)
  {
    return RoleDAO.findRole(RegistryRole.Type.getRootOrgRoleName(organizationCode));
  }

  /**
   * Returns the {@link RoleDAOIF} name for the Registry Administrator for this
   * {@link Organization}.
   * 
   * @return the {@link RoleDAOIF} name for the Registry Administrator for this
   *         {@link Organization}.
   */
  public String getRegistryAdminRoleName()
  {
    return RegistryRole.Type.getRA_RoleName(this.getCode());
  }

  /**
   * Returns the Registry Administrator {@link Roles} for this
   * {@link Organization}.
   * 
   * @return the Registry Administrator {@link Roles} for this
   *         {@link Organization}.
   */
  public Roles getRegistryAdminiRole()
  {
    return Roles.findRoleByName(this.getRegistryAdminRoleName());
  }

  /**
   * Returns the Registry Administrator {@link Roles} for this
   * {@link Organization}.
   * 
   * @param organizationCode
   * 
   * @return the Registry Administrator {@link Roles} for this
   *         {@link Organization}.
   */
  public static Roles getRegistryAdminiRole(String organizationCode)
  {
    return Roles.findRoleByName(RegistryRole.Type.getRA_RoleName(organizationCode));
  }

  /**
   * Return a map of {@link GeoObjectType} codes and labels for this {@link Organization}.
   * 
   * @return a map of {@link GeoObjectType} codes and labels for this {@link Organization}.
   */
  public Map<String, LocalizedValue> getGeoObjectTypes()
  {
    // For performance, get all of the universals defined
    List<? extends EntityDAOIF> universalList =  ObjectCache.getCachedEntityDAOs(Universal.CLASS);
    
    Map<String, LocalizedValue> typeCodeMap = new HashMap<String, LocalizedValue>();
    
    for (EntityDAOIF entityDAOIF : universalList)
    {
      Universal universal = (Universal)BusinessFacade.get(entityDAOIF);

      // Check to see if the universal is owned by the organization role.
      String ownerId = universal.getOwnerOid();
      Roles organizationRole = this.getRole();
      if (ownerId.equals(organizationRole.getOid()))
      {

        String geoObjectTypeCode = universal.getUniversalId();
        typeCodeMap.put(geoObjectTypeCode, LocalizedValueConverter.convert(universal.getDisplayLabel()));
      }
    }
    
    return typeCodeMap;
  }
  
  /**
   * Creates a {@link RoleDAOIF} for this {@link Organization}.
   * 
   * Precondition: a {@link RoleDAOIF} does not exist for this
   * {@link Organization}. Precondition: the display label for the default
   * locale has a value for this {@link Organization}
   * 
   */
  private void createOrganizationRole()
  {
    String roleName = this.getRoleName();

    String defaultDisplayLabel = this.getDisplayLabel().getDefaultValue();

    RoleDAO orgRole = RoleDAO.createRole(roleName, defaultDisplayLabel);

    RoleDAO rootOrgRole = (RoleDAO) RoleDAO.findRole(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE);

    rootOrgRole.addInheritance(orgRole);
  }

  /**
   * Creates a Registry Administrator {@link RoleDAOIF} for this
   * {@link Organization}.
   * 
   * Precondition: a {@link RoleDAOIF} does not exist for this
   * {@link Organization}. Precondition: the display label for the default
   * locale has a value for this {@link Organization}
   * 
   */
  private void createRegistryAdminOrganizationRole()
  {
    String registryAdminRoleName = this.getRegistryAdminRoleName();

    String defaultDisplayLabel = this.getDisplayLabel().getDefaultValue() + " Registry Admin";

    // Heads up: clean up move to Roles.java?
    Roles raOrgRole = new Roles();
    raOrgRole.setRoleName(registryAdminRoleName);
    raOrgRole.getDisplayLabel().setDefaultValue(defaultDisplayLabel);
    raOrgRole.apply();

    Roles orgRole = (Roles) this.getRole();
    RoleDAO orgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(orgRole);

    RoleDAO raOrgRoleDAO = (RoleDAO) BusinessFacade.getEntityDAO(raOrgRole);
    orgRoleDAO.addInheritance(raOrgRoleDAO);

    // Inherit the permissions from the root RA role
    RoleDAO rootRA_DAO = (RoleDAO) BusinessFacade.getEntityDAO(Roles.findRoleByName(RegistryConstants.REGISTRY_ADMIN_ROLE));
    rootRA_DAO.addInheritance(raOrgRoleDAO);
  }

  /**
   * If the given actor OID is a role that represents an {@link Organization},
   * then return the corresponding {@link Organization} code or NULL otherwise.
   * 
   * Precondition: Assumes that the actor id, if it is associated with an
   * organization, is the root organization role and not a sub-role of the root
   * organization.
   * 
   * @param actorOid
   *          OID of an actor.
   * 
   * @return the corresponding {@link Organization} code or NULL otherwise.
   */
  public static String getRootOrganizationCode(String actorOid)
  {
    Organization organization = getRootOrganization(actorOid);

    if (organization == null)
    {
      return null;
    }
    else
    {
      return organization.getCode();
    }
  }

  /**
   * If the given actor OID is a role that represents an {@link Organization},
   * then return the corresponding {@link Organization} or NULL otherwise.
   * 
   * Precondition: Assumes that the actor id, if it is associated with an
   * organization, is the root organization role and not a sub-role of the root
   * organization.
   * 
   * @param actorOid
   *          OID of an actor.
   * 
   * @return the corresponding {@link Organization} or NULL otherwise.
   */
  public static Organization getRootOrganization(String actorOid)
  {
    Actor actor = null;

    try
    {
      actor = Actor.get(actorOid);
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
      return null;
    }

    // If the actor is not a role, then it does not represent an organization
    if (! ( actor instanceof Roles ))
    {
      return null;
    }

    Roles role = (Roles) actor;
    String roleName = role.getRoleName();

    // If the role name does not contain the organization root name, then it is
    // not a role that pertains to an organization.
    if (roleName.indexOf(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE) <= -1)
    {
      return null;
    }

    String organizationCode = roleName.substring(RegistryRole.Type.REGISTRY_ROOT_ORG_ROLE.length() + 1, roleName.length());

    try
    {
      return Organization.getByCode(organizationCode);
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
      return null;
    }
  }

  public static List<? extends Organization> getOrganizations()
  {    
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel().localize());

    return query.getIterator().getAll();
  }

  /**
   * Returns all of the organizations as {@link Organization} objects from the cache unsorted
   * instead of fetching from the database.
   * @return
   */
  public static List<Organization> getOrganizationsFromCache()
  {
    // For performance, get all of the universals defined
    List<? extends EntityDAOIF> organizationDAOs =  ObjectCache.getCachedEntityDAOs(Organization.CLASS);
    
    List<Organization> organizationList = new LinkedList<Organization>();
    
    for (EntityDAOIF entityDAOIF : organizationDAOs)
    {
      Organization organization = (Organization)BusinessFacade.get(entityDAOIF);
      organizationList.add(organization);
    }
    
    return organizationList;
  }
  
  public OrganizationDTO toDTO()
  {
    return new OrganizationDTO(this.getCode(), LocalizedValueConverter.convert(this.getDisplayLabel()), LocalizedValueConverter.convert(this.getContactInfo()));
  }

  public static List<Organization> getUserAdminOrganizations()
  {
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());
    query.ORDER_BY_ASC(query.getDisplayLabel().localize());

    try (final OIterator<? extends Organization> iterator = query.getIterator())
    {
      final List<? extends Organization> orgs = iterator.getAll();

      List<Organization> result = orgs.stream().filter(o -> {
        return Organization.isRegistryAdmin(o);
      }).collect(Collectors.toList());

      return result;
    }
  }
  
  /**
   * Returns true if the provided actor has permission to this organization. 
   */
  public boolean doesActorHavePermission(SingleActorDAOIF actor)
  {
    Set<RoleDAOIF> roles = actor.authorizedRoles();
    
    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();
      
      if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
      {
        String orgCode = RegistryRole.Type.parseOrgCode(roleName);
        
        if (orgCode.equals(this.getCode()))
        {
          return true;
        }
      }
      else if (RegistryRole.Type.isSRA_Role(roleName))
      {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Throws an exception if the provided actor does not have permissions to this
   * organization. Uses {{Organization.doesActorHavePermission}} to check permissions.
   * 
   * @param actor
   */
  public void enforceActorHasPermission(SingleActorDAOIF actor)
  {
    if (!this.doesActorHavePermission(actor))
    {
      OrganizationRAException ex = new OrganizationRAException();
      throw ex;
    }
  }

  /**
   * @param org
   * @return If the current user is part of the registry admin role for the
   *         given organization
   */
  public static boolean isRegistryAdmin(Organization org)
  {
    String roleName = RegistryRole.Type.getRA_RoleName((org.getCode()));

    final SessionIF session = Session.getCurrentSession();

    if (session != null)
    {
      return session.userHasRole(roleName);
    }

    return true;
  }

  /**
   * @param org
   * @return If the current user is a member of the given organization
   */
  public static boolean isMember(Organization org)
  {
    return true;
  }
}
