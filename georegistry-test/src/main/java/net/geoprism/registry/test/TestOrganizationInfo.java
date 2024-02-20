/**
 *
 */
package net.geoprism.registry.test;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class TestOrganizationInfo
{
  private String             code;

  private String             displayLabel;

  private String             oid;

  private ServerOrganization serverObj;

  public TestOrganizationInfo(String code)
  {
    initialize(code);
  }

  public TestOrganizationInfo(String code, String displayLabel)
  {
    this.code = code;
    this.displayLabel = displayLabel;
  }

  private void initialize(String code)
  {
    this.code = code;
    this.displayLabel = code + " Display Label";
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getDisplayLabel()
  {
    return displayLabel;
  }

  public void setDisplayLabel(String displayLabel)
  {
    this.displayLabel = displayLabel;
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public ServerOrganization getServerObject()
  {
    return this.getServerObject(false);
  }

  public ServerOrganization getServerObject(boolean forceFetch)
  {
    if (this.serverObj != null && !forceFetch)
    {
      return this.serverObj;
    }
    
    OrganizationQuery query = new OrganizationQuery(new QueryFactory());

    query.WHERE(query.getCode().EQ(this.getCode()));

    List<? extends Organization> orgs = query.getIterator().getAll();

    if (orgs.size() > 0)
    {
      this.serverObj = ServerOrganization.get(orgs.get(0));
      return this.serverObj;
    }
    else
    {
      return null;
    }
  }

  @Request
  public void delete()
  {
    deleteInTrans();
  }

  @Transaction
  private void deleteInTrans()
  {
    ServerOrganization org = getServerObject(true);

    if (org != null)
    {
      OrganizationBusinessServiceIF service = ServiceFactory.getBean(OrganizationBusinessServiceIF.class);
      service.delete(org);
    }
  }

  public OrganizationDTO toDTO()
  {
    LocalizedValue displayLabel = new LocalizedValue(this.displayLabel);
    LocalizedValue contactInfo = new LocalizedValue(this.displayLabel);

    OrganizationDTO dto = new OrganizationDTO(this.code, displayLabel, contactInfo);

    return dto;
  }

  @Request
  public ServerOrganization apply()
  {
    if (this.getServerObject(true) != null)
    {
      return this.serverObj;
    }

    OrganizationBusinessServiceIF service = ServiceFactory.getBean(OrganizationBusinessServiceIF.class);

    this.serverObj = service.create(this.toDTO());
    
    return this.serverObj;
  }
}
