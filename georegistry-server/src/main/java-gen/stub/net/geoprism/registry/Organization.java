package net.geoprism.registry;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.system.Actor;
import com.runwaysdk.system.Roles;
import java.util.List;
import com.runwaysdk.query.QueryFactory;

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
   * @return
   */
  public static Organization getByCode(String code)
  {
    return getByKey(code);
  }
  
  /**
   * Creates a {@link RoleDAO} for this {@link Organization} and a Registry Administrator {@link RoleDAO} for this {@link Organization}.
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
   * Removes the {@link RoleDAO}s for this {@link Organization} and  Registry Administrator for this {@link Organization}.
   */
  public void delete()
  {
    try
    {
      Roles raOrgRole = this.getRegistryAdminiRole();
      raOrgRole.delete();
    }
    // Heads up: clean up
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e) {}
    try
    {
      Roles orgRole = this.getRole();
      orgRole.delete();
    }
    // Heads up: clean up
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e) {}
      
    super.delete();
  }
  
  /**
   * Returns the role name for this {@link Organization}.
   * 
   * @return role name for this {@link Organization}.
   */
  public String getRoleName()
  {
    return getRoleName(this.getCode());
  }
  
  /**
   * Constructs a role name for the {@link Organization} with the given code.
   * 
   * @param organizationCode {@link Organization} code.
   * 
   * @return role name for the {@link Organization} with the given code.
   */
  public static String getRoleName(String organizationCode)
  {
    return RegistryConstants.REGISTRY_ROOT_ORG_ROLE+"."+organizationCode;
  }

  /**
   * Returns the {@link Roles} for this {@link Organization}.
   * 
   * @return the {@link Roles} for this {@link Organization}.
   */
  public Roles getRole()
  {
    return Roles.findRoleByName(this.getRoleName());
//    return RoleDAO.findRole(this.getRoleName());
  }
  
  /**
   * Returns the {@link RoleDAOIF} for the {@link Organization} with the given code.
   * 
   * @param organizationCode
   * 
   * @return the {@link RoleDAOIF} for the {@link Organization} with the given code.
   */
  public static RoleDAOIF getRole(String organizationCode)
  {
    return RoleDAO.findRole(getRoleName(organizationCode));
  }
  

  /**
   * Returns the {@link RoleDAOIF} name for the Registry Administrator for this {@link Organization}.
   * 
   * @return the {@link RoleDAOIF} name for the Registry Administrator for this  {@link Organization}.
   */
  public String getRegistryAdminRoleName()
  {
    return getRegistryAdminRoleName(this.getCode());
  }
  
  /**
   * Constructs a {@link RoleDAOIF} for the Registry Administrator for the {@link Organization} with the given code.
   * 
   * @param organizationCode {@link Organization} code.
   * 
   * @return {@link RoleDAOIF} for the Registry Administrator for the {@link Organization} with the given code.
   */
  public static String getRegistryAdminRoleName(String organizationCode)
  {
    String organizationRoleName = getRoleName(organizationCode);
    
    return organizationRoleName+"."+RegistryConstants.REGISTRY_ORG_RA_ROLE_SUFFIX;
  }

  /**
   * Returns the Registry Administrator {@link Roles} for this {@link Organization}.
   * 
   * @return the Registry Administrator {@link Roles} for this {@link Organization}.
   */
  public Roles getRegistryAdminiRole()
  {
    return Roles.findRoleByName(this.getRegistryAdminRoleName());
  }
  
  /**
   * Returns the Registry Administrator {@link Roles} for this {@link Organization}.
   * 
   * @param organizationCode
   * 
   * @return the Registry Administrator {@link Roles} for this {@link Organization}.
   */
  public static Roles getRegistryAdminiRole(String organizationCode)
  {
    return Roles.findRoleByName(getRegistryAdminRoleName(organizationCode));
  }
  
  /**
   * Creates a {@link RoleDAOIF} for this {@link Organization}.
   * 
   * Precondition: a {@link RoleDAOIF}  does not exist for this {@link Organization}.
   * Precondition: the display label for the default locale has a value for this {@link Organization}
   * 
   */
  private void createOrganizationRole()
  {
    String roleName = this.getRoleName();
    
    String defaultDisplayLabel = this.getDisplayLabel().getDefaultValue();
    
    RoleDAO orgRole = RoleDAO.createRole(roleName, defaultDisplayLabel);
    
    RoleDAO rootOrgRole = (RoleDAO)RoleDAO.findRole(RegistryConstants.REGISTRY_ROOT_ORG_ROLE);
    
    rootOrgRole.addInheritance(orgRole);
  }
  
  /**
   * Creates a Registry Administrator {@link RoleDAOIF} for this {@link Organization}.
   * 
   * Precondition: a {@link RoleDAOIF} does not exist for this {@link Organization}.
   * Precondition: the display label for the default locale has a value for this {@link Organization}
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
    
    Roles orgRole = (Roles)this.getRole();
    RoleDAO orgRoleDAO = (RoleDAO)BusinessFacade.getEntityDAO(orgRole);
    
    RoleDAO raOrgRoleDAO = (RoleDAO)BusinessFacade.getEntityDAO(raOrgRole);
    orgRoleDAO.addInheritance(raOrgRoleDAO);
  }
  
  /**
   * If the given actor OID is a role that represents an {@link Organization}, then return the corresponding {@link Organization} code
   * or NULL otherwise.
   * 
   * Precondition: Assumes that the actor id, if it is associated with an organization, is the root organization role and not
   * a sub-role of the root organization. 
   * 
   * @param actorOid OID of an actor.
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
   * If the given actor OID is a role that represents an {@link Organization}, then return the corresponding {@link Organization} 
   * or NULL otherwise.
   * 
   * Precondition: Assumes that the actor id, if it is associated with an organization, is the root organization role and not
   * a sub-role of the root organization. 
   * 
   * @param actorOid OID of an actor.
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
    if (!(actor instanceof Roles))
    {
      return null;
    }
    
    Roles role = (Roles)actor;
    String roleName = role.getRoleName();
    
    // If the role name does not contain the organization root name, then it is not a role that pertains to an organization.
    if (roleName.indexOf(RegistryConstants.REGISTRY_ROOT_ORG_ROLE) <= -1)
    {
      return null;
    }
    
    String organizationCode = roleName.substring(RegistryConstants.REGISTRY_ROOT_ORG_ROLE.length()+1, roleName.length());
    
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

}
