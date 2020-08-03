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

import java.sql.Savepoint;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.DuplicateGraphPathException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

/**
 * The purpose of this dataset is to provide very basic data which provides for very fast running
 * of tests. This is achieved by creating the most basic data needed for tests, and then not cleaning
 * up the metadata at the end, and re-using it for future iterations. DO NOT use this dataset if you'll
 * be adding / removing attributes or otherwise damaging the metadata. We will however automatically
 * clean up instance data like GeoObjects and the like.
 * 
 * @author rrowlands
 *
 */
public class FastTestDataset extends TestDataSet
{
  public final String                TEST_DATA_KEY    = "FAST";
  
  public final TestOrganizationInfo  ORG_CGOV         = new TestOrganizationInfo(TEST_DATA_KEY + "_" + "CGOV", "Central Government");
  
  public final TestUserInfo          USER_CGOV_RA      = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovra", "cgovra", TEST_DATA_KEY + "@noreply.com", new String[] {RegistryRole.Type.getRA_RoleName(ORG_CGOV.getCode())});
  
  public final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo(TEST_DATA_KEY + "Admin", ORG_CGOV);
  
  public final TestGeoObjectTypeInfo COUNTRY          = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Country", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public final TestGeoObjectTypeInfo PROVINCE            = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Province", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public final TestGeoObjectInfo     CAMBODIA         = new TestGeoObjectInfo(TEST_DATA_KEY + "Cambodia", COUNTRY);

  public final TestGeoObjectInfo     PROV_CENTRAL     = new TestGeoObjectInfo(TEST_DATA_KEY + "CentralProvince", PROVINCE);
  
  {
    managedOrganizationInfos.add(ORG_CGOV);
    
    managedHierarchyTypeInfos.add(HIER_ADMIN);
    
    managedGeoObjectTypeInfos.add(COUNTRY);
    managedGeoObjectTypeInfos.add(PROVINCE);

    managedGeoObjectInfos.add(CAMBODIA);
    managedGeoObjectInfos.add(PROV_CENTRAL);
    
    managedUsers.add(USER_CGOV_RA);
  }
  
  public static FastTestDataset newTestData()
  {
    return new FastTestDataset();
  }
  
  @Transaction
  @Override
  public void setUpClassRelationships()
  {
    Savepoint sp = Database.setSavepoint();
    
    try
    {
      COUNTRY.getUniversal().addLink(Universal.getRoot(), HIER_ADMIN.getServerObject().getUniversalType());
      COUNTRY.addChild(PROVINCE, HIER_ADMIN);
    }
    catch (DuplicateGraphPathException ex)
    {
      Database.rollbackSavepoint(sp);
    }
    finally
    {
      Database.releaseSavepoint(sp);
    }
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
    Savepoint sp = Database.setSavepoint();
    
    try
    {
      CAMBODIA.getGeoEntity().addLink(GeoEntity.getRoot(), HIER_ADMIN.getServerObject().getEntityType());
  
      CAMBODIA.addChild(PROV_CENTRAL, HIER_ADMIN);
    }
    catch (DuplicateGraphPathException ex)
    {
      Database.rollbackSavepoint(sp);
    }
    finally
    {
      Database.releaseSavepoint(sp);
    }
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
  
  
  
  @Override
  protected void cleanUpClassInTrans()
  {
    if (clientSession != null)
    {
      clientSession.logout();
    }
  }
}
