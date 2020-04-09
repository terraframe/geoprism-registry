package net.geoprism.registry.conversion;

import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;

import net.geoprism.registry.Organization;
import net.geoprism.registry.SRAException;

public class OrganizationConverter extends LocalizedValueConverter
{
  
  public OrganizationDTO build(Organization organization)
  {
    String code = organization.getCode();

    LocalizedValue label = convert(organization.getDisplayLabel());

    LocalizedValue contactInfo = convert(organization.getContactInfo());
    
    return new OrganizationDTO(code, label, contactInfo);
  }
  
  @Transaction
  public Organization create(String json)
  {
    OrganizationDTO organizationDTO = OrganizationDTO.fromJSON(json);
    
    return this.create(organizationDTO);
  }
  
  public Organization fromDTO(OrganizationDTO organizationDTO)
  {
    Organization organization = new Organization();
    
    organization.setCode(organizationDTO.getCode());
 
    populate(organization.getDisplayLabel(), organizationDTO.getLabel());
    populate(organization.getContactInfo(), organizationDTO.getContactInfo());
    
    return organization;
  }
  
  @Transaction
  public Organization create(OrganizationDTO organizationDTO)
  {
    final Organization organization = this.fromDTO(organizationDTO);
    
    // They must be SRA role to perform this
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      boolean hasSRA = false;
      
      Set<RoleDAOIF> roles = Session.getCurrentSession().getUser().authorizedRoles();
      
      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();
        
        if (RegistryRole.Type.isSRA_Role(roleName))
        {
          hasSRA = true;
        }
      }
      
      if (!hasSRA)
      {
        SRAException ex = new SRAException();
        throw ex;
      }
    }
    
    organization.apply();
    
    return organization;
  }
  
  @Transaction
  public Organization update(OrganizationDTO organizationDTO)
  {
    Organization organization = Organization.getByKey(organizationDTO.getCode());
    
    organization.lock();
    organization.setCode(organizationDTO.getCode());
 
    populate(organization.getDisplayLabel(), organizationDTO.getLabel());
    populate(organization.getContactInfo(), organizationDTO.getContactInfo());
    
    organization.apply();
    organization.unlock();
    
    return organization;
  }
  
}
