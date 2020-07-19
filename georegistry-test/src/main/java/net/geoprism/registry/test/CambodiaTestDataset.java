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

import org.commongeoregistry.adapter.constants.GeometryType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

public class CambodiaTestDataset extends TestDataSet
{
  public static final String                TEST_DATA_KEY    = "Cambodia";
  
  public final TestOrganizationInfo  ORG_CENTRAL          = new TestOrganizationInfo("CentralGovernmentOrg", "Central Government");
  
  public final TestOrganizationInfo  ORG_MOH          = new TestOrganizationInfo("MinistryOfHealthOrg", "Ministry Of Health");
  
  public final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo("AdminDivisions", "Administrative Divisions", ORG_CENTRAL);
  
  public final TestHierarchyTypeInfo HIER_MOH       = new TestHierarchyTypeInfo("MinistryOfHealth", "Ministry Of Health", ORG_CENTRAL);
  
  public final TestGeoObjectTypeInfo GOT_Country          = new TestGeoObjectTypeInfo("Country", GeometryType.MULTIPOLYGON, ORG_CENTRAL);

  public final TestGeoObjectTypeInfo GOT_Province            = new TestGeoObjectTypeInfo("Province", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public final TestGeoObjectTypeInfo GOT_District            = new TestGeoObjectTypeInfo("District", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public final TestGeoObjectTypeInfo GOT_Commune            = new TestGeoObjectTypeInfo("Commune", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public final TestGeoObjectTypeInfo GOT_Village            = new TestGeoObjectTypeInfo("Village", GeometryType.MULTIPOINT, ORG_CENTRAL);

  public final TestGeoObjectInfo     GO_Cambodia              = new TestGeoObjectInfo("Cambodia", GOT_Country);
  
  public final TestGeoObjectInfo     GO_Oddar_Meanchey              = new TestGeoObjectInfo("Oddar Meanchey", GOT_Province);

  {
    managedOrganizationInfos.add(ORG_CENTRAL);
    managedOrganizationInfos.add(ORG_MOH);
    
    managedHierarchyTypeInfos.add(HIER_ADMIN);
    managedHierarchyTypeInfos.add(HIER_MOH);
    
    managedGeoObjectTypeInfos.add(GOT_Country);
    managedGeoObjectTypeInfos.add(GOT_Province);
    managedGeoObjectTypeInfos.add(GOT_District);
    managedGeoObjectTypeInfos.add(GOT_Commune);
    managedGeoObjectTypeInfos.add(GOT_Village);

    managedGeoObjectInfos.add(GO_Cambodia);
    managedGeoObjectInfos.add(GO_Oddar_Meanchey);
  }
  
  public static void main(String[] args)
  {
    CambodiaTestDataset data = CambodiaTestDataset.newTestData();
    data.setUpMetadata();
    data.setUpInstanceData();
    
    
  }

  public static CambodiaTestDataset newTestData()
  {
    return new CambodiaTestDataset();
  }
  
  @Transaction
  @Override
  public void setUpClassRelationships()
  {
    GOT_Country.getUniversal().addLink(Universal.getRoot(), HIER_ADMIN.getServerObject().getUniversalType());
    
    GOT_Country.addChild(GOT_Province, HIER_ADMIN);
    GOT_Province.addChild(GOT_District, HIER_ADMIN);
    GOT_District.addChild(GOT_Commune, HIER_ADMIN);
    GOT_Commune.addChild(GOT_Village, HIER_ADMIN);
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
    GO_Cambodia.getGeoEntity().addLink(GeoEntity.getRoot(), HIER_ADMIN.getServerObject().getEntityType());
    
    GO_Cambodia.addChild(GO_Oddar_Meanchey, HIER_ADMIN);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
