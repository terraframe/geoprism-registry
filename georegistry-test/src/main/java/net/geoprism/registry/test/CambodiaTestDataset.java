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

import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.Universal;

import junit.framework.Assert;
import net.geoprism.ontology.Classifier;

public class CambodiaTestDataset extends TestDataSet
{
  public static final String                TEST_DATA_KEY    = "Cambodia";
  
  public static final TestOrganizationInfo  ORG_CENTRAL          = new TestOrganizationInfo("CentralGovernmentOrg", "Central Government");
  
  public static final TestUserInfo          USER_ORG_RA      = new TestUserInfo("cgovra", "cgovra", "cgovra@noreply.com", new String[] {RegistryRole.Type.getRA_RoleName(ORG_CENTRAL.getCode())});
  
  public static final TestOrganizationInfo  ORG_MOH          = new TestOrganizationInfo("MinistryOfHealthOrg", "Ministry Of Health");
  
  public static final TestUserInfo          USER_MOH_RA      = new TestUserInfo("mohra", "mohra", "mohra@noreply.com", new String[] {RegistryRole.Type.getRA_RoleName(ORG_MOH.getCode())});
  
  public static final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo("AdminDivisions", "Administrative Divisions", ORG_CENTRAL);
  
  public static final TestHierarchyTypeInfo HIER_MOH       = new TestHierarchyTypeInfo("MinistryOfHealth", "Ministry Of Health", ORG_MOH);
  
  public static final TestGeoObjectTypeInfo GOT_Country          = new TestGeoObjectTypeInfo("Country", GeometryType.MULTIPOLYGON, ORG_CENTRAL);

  public static final TestGeoObjectTypeInfo GOT_Province            = new TestGeoObjectTypeInfo("Province", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public static final TestGeoObjectTypeInfo GOT_District            = new TestGeoObjectTypeInfo("District", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public static final TestGeoObjectTypeInfo GOT_Commune            = new TestGeoObjectTypeInfo("Commune", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public static final TestGeoObjectTypeInfo GOT_Village            = new TestGeoObjectTypeInfo("Village", GeometryType.MULTIPOINT, ORG_CENTRAL);
  
  public static final TestGeoObjectTypeInfo GOT_Hospital              = new TestGeoObjectTypeInfo("Hospital", GeometryType.MULTIPOLYGON, ORG_MOH);

  public static final TestGeoObjectInfo     GO_Cambodia              = new TestGeoObjectInfo("Cambodia", GOT_Country);
  
  public static final TestGeoObjectInfo     GO_Oddar_Meanchey              = new TestGeoObjectInfo("Oddar Meanchey", GOT_Province);
  
  public static final TestUserInfo          USER_CGOV_RM          = new TestUserInfo("cgovrm", "cgovrm", "cgovrm@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Country.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Province.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_District.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Commune.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Village.getCode()) });

  public static final TestUserInfo          USER_CGOV_RC          = new TestUserInfo("cgovrc", "cgovrc", "cgovrc@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Country.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Province.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_District.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Commune.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Village.getCode()) });

  public static final TestUserInfo          USER_CGOV_AC          = new TestUserInfo("cgovac", "cgovac", "cgovac@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Country.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Province.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_District.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Commune.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Village.getCode()) });
  
  public static final TestUserInfo          USER_MOHA_RA          = new TestUserInfo("mohra", "mohra", "mohara@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG_MOH.getCode()) });
  
  public static final TestUserInfo          USER_MOHA_RM          = new TestUserInfo("mohrm", "mohrm", "moharm@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(ORG_MOH.getCode(), GOT_Hospital.getCode()) });

  public static final TestUserInfo          USER_MOHA_RC          = new TestUserInfo("mohrc", "mohrc", "moharc@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(ORG_MOH.getCode(), GOT_Hospital.getCode()) });

  public static final TestUserInfo          USER_MOHA_AC          = new TestUserInfo("mohac", "mohac", "mohaac@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(ORG_MOH.getCode(), GOT_Hospital.getCode()) });
  
  public TestAttributeTypeInfo AT_National_Anthem;
  
  public TestAttributeTypeInfo AT_PHONE_COUNTRY_CODE;
  
  public TestAttributeTypeInfo AT_UN_MEMBER;
  
  public TestAttributeTypeInfo AT_GDP;
  
  public TestAttributeTypeInfo AT_DATE_OF_FORMATION;
  
  public TestAttributeTypeInfo AT_RELIGION;
  
  public Term T_Religion;
  
  public Term T_Buddhism;
  
  public Term T_Islam;
  
  public Term T_Christianity;
  
  public Term T_Other;

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
    
    managedGeoObjectTypeInfos.add(GOT_Hospital);

    managedGeoObjectInfos.add(GO_Cambodia);
    managedGeoObjectInfos.add(GO_Oddar_Meanchey);
    
    managedUsers.add(USER_ORG_RA);
    managedUsers.add(USER_MOH_RA);
    
    managedUsers.add(USER_CGOV_RM);
    managedUsers.add(USER_CGOV_RC);
    managedUsers.add(USER_CGOV_AC);
    
    managedUsers.add(USER_MOHA_RA);
    managedUsers.add(USER_MOHA_RM);
    managedUsers.add(USER_MOHA_RC);
    managedUsers.add(USER_MOHA_AC);
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
    
    GOT_Country.getUniversal().addLink(Universal.getRoot(), HIER_MOH.getServerObject().getUniversalType());
    
    GOT_Country.addChild(GOT_Province, HIER_MOH);
    GOT_Province.addChild(GOT_District, HIER_MOH);
    GOT_District.addChild(GOT_Commune, HIER_MOH);
    GOT_Commune.addChild(GOT_Hospital, HIER_MOH);
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
//    GO_Cambodia.getGeoEntity().addLink(GeoEntity.getRoot(), HIER_ADMIN.getServerObject().getEntityType());
    
    GO_Cambodia.addChild(GO_Oddar_Meanchey, HIER_ADMIN);
  }
  
  @Transaction
  @Override
  protected void setUpMetadataInTrans()
  {
    super.setUpMetadataInTrans();
    
    this.AT_National_Anthem = TestDataSet.createAttribute("NationalAnthem", "National Anthem", GOT_Country, AttributeCharacterType.TYPE);
    GO_Cambodia.setDefaultValue(this.AT_National_Anthem.getAttributeName(), "Nokor Reach");
    
    this.AT_PHONE_COUNTRY_CODE = TestDataSet.createAttribute("PhoneCountryCode", "Phone Country Code", GOT_Country, AttributeIntegerType.TYPE);
    GO_Cambodia.setDefaultValue(this.AT_PHONE_COUNTRY_CODE.getAttributeName(), 855L);
    
    this.AT_GDP = TestDataSet.createAttribute("GDP", "Gross Domestic Product", GOT_Country, AttributeFloatType.TYPE);
    GO_Cambodia.setDefaultValue(this.AT_GDP.getAttributeName(), 26.730D);
    
    this.AT_UN_MEMBER = TestDataSet.createAttribute("UnMember", "UN Member", GOT_Country, AttributeBooleanType.TYPE);
    GO_Cambodia.setDefaultValue(this.AT_UN_MEMBER.getAttributeName(), true);
    
    this.AT_DATE_OF_FORMATION = TestDataSet.createAttribute("DateOfFormation", "Date Of Formation", GOT_Country, AttributeDateType.TYPE);
    GO_Cambodia.setDefaultValue(this.AT_DATE_OF_FORMATION.getAttributeName(), new Date()); // TODO
    
    createTermAttribute();
  }
  
  public void createTermAttribute()
  {
    this.AT_RELIGION = TestDataSet.createTermAttribute("Religion", "Religion", GOT_Country, T_Religion);
    
    
    T_Religion = TestDataSet.createAttributeRootTerm(this.GOT_Country, this.AT_RELIGION);
    
    T_Buddhism = TestDataSet.createTerm(this.AT_RELIGION, "Buddhism", "Buddhism");
    T_Islam = TestDataSet.createTerm(this.AT_RELIGION, "Islam", "Islam");
    T_Christianity = TestDataSet.createTerm(this.AT_RELIGION, "Christianity", "Christianity");
    T_Other = TestDataSet.createTerm(this.AT_RELIGION, "Other", "Other");
    
    Classifier rootClassy = TestDataSet.getClassifierIfExist(this.AT_RELIGION.getRootTerm().getCode());
    List<? extends Classifier> childClassifiers = rootClassy.getAllIsAChild().getAll();
    Assert.assertEquals(4, childClassifiers.size());
    
    
    GO_Cambodia.setDefaultValue(AT_RELIGION.getAttributeName(), T_Buddhism);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
