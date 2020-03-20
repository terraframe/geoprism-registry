package net.geoprism.registry.conversion;

import net.geoprism.registry.Organization;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;

import com.runwaysdk.dataaccess.transaction.Transaction;

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
  
  @Transaction
  public Organization create(OrganizationDTO organizationDTO)
  {
    Organization organization = new Organization();
    
    organization.setCode(organizationDTO.getCode());
 
    populate(organization.getDisplayLabel(), organizationDTO.getLabel());
    populate(organization.getContactInfo(), organizationDTO.getContactInfo());
    
    organization.apply();
    
    return organization;
  }
  
  @Transaction
  public Organization update(OrganizationDTO organizationDTO)
  {
   
    Organization organization = Organization.getByKey(organizationDTO.getCode());
 
    populate(organization.getDisplayLabel(), organizationDTO.getLabel());
    populate(organization.getContactInfo(), organizationDTO.getContactInfo());
    
    organization.apply();
    
    return organization;
  }
  
}
