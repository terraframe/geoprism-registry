/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdTermRelationshipQuery;

import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class TestHierarchyTypeInfo
{
  private String               code;

  private String               displayLabel;

  private String               oid;

  private TestOrganizationInfo org;

  private ServerHierarchyType  serverObj;

  public TestHierarchyTypeInfo(String genKey, TestOrganizationInfo org)
  {
    initialize(genKey, org);
  }

  public TestHierarchyTypeInfo(String code, String displayLabel, TestOrganizationInfo org)
  {
    this.code = code;
    this.displayLabel = displayLabel;
    this.org = org;
  }

  private void initialize(String genKey, TestOrganizationInfo org)
  {
    this.code = genKey + "Code";
    this.displayLabel = genKey + " Display Label";
    this.org = org;
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

  public ServerHierarchyType getServerObject()
  {
    return this.getServerObject(false);
  }

  public ServerHierarchyType getServerObject(boolean forceFetch)
  {
    if (this.serverObj != null && !forceFetch)
    {
      return this.serverObj;
    }

    Optional<ServerHierarchyType> hierarchyType = ServiceFactory.getMetadataCache().getHierachyType(code);

    if (hierarchyType.isPresent())
    {
      if (this.doesMdTermRelationshipExist())
      {
        this.serverObj = ServerHierarchyType.get(getCode());
        return this.serverObj;
      }
    }

    return null;
  }

  public Boolean doesMdTermRelationshipExist()
  {
    String universalKey = ServerHierarchyType.buildMdTermRelUniversalKey(this.getCode());

    MdTermRelationshipQuery uniQuery = new MdTermRelationshipQuery(new QueryFactory());
    uniQuery.WHERE(uniQuery.getKeyName().EQ(universalKey));

    return uniQuery.getCount() > 0;
  }

  public TestOrganizationInfo getOrganization()
  {
    return this.org;
  }

  public HierarchyType toDTO()
  {
    LocalizedValue displayLabel = new LocalizedValue(this.displayLabel);
    LocalizedValue description = new LocalizedValue(this.displayLabel);

    HierarchyType ht = new HierarchyType(this.code, displayLabel, description, this.getOrganization().getCode());

    return ht;
  }

  @Request
  public void apply()
  {
    if (this.getServerObject(true) != null)
    {
      return;
    }

    HierarchyType dto = this.toDTO();

    this.serverObj = new ServerHierarchyTypeBuilder().createHierarchyType(dto);
  }

  @Request
  public void setRoot(TestGeoObjectTypeInfo type)
  {
    this.serverObj.addToHierarchy(RootGeoObjectType.INSTANCE, type.getServerObject());
  }

  @Request
  public void removeRoot(TestGeoObjectTypeInfo type)
  {
    this.serverObj.removeChild(RootGeoObjectType.INSTANCE, type.getServerObject(), true);
  }

  @Request
  public void delete()
  {
    deleteInTrans();
  }

  @Transaction
  private void deleteInTrans()
  {
    ServerHierarchyType serverHOT = getServerObject(true);

    if (serverHOT != null)
    {
      serverHOT.delete();
    }

    this.serverObj = null;
  }
}
