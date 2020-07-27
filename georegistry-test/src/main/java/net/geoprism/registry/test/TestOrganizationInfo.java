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
package net.geoprism.registry.test;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;

import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.conversion.OrganizationConverter;

public class TestOrganizationInfo
{
  private String                  code;
  
  private String                  displayLabel;
  
  private String                  oid;
  
  private Organization            serverObj;
  
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
  
  public Organization getServerObject()
  {
    return this.getServerObject(false);
  }
  
  public Organization getServerObject(boolean forceFetch)
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
      this.serverObj = orgs.get(0);
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
    Organization org = getServerObject(true);
    
    if (org != null)
    {
      org.delete();
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
  public void apply()
  {
    this.serverObj = new OrganizationConverter().create(this.toDTO());
  }
}
