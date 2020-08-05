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
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.DuplicateGraphPathException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

import junit.framework.Assert;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.conversion.TermConverter;

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
  public static final String                TEST_DATA_KEY    = "FAST";
  
  public static final TestOrganizationInfo  ORG_CGOV         = new TestOrganizationInfo(TEST_DATA_KEY + "_" + "CGOV", "Central Government");
  
  public static final TestUserInfo          USER_CGOV_RA      = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovra", "cgovra", TEST_DATA_KEY + "cgovra@noreply.com", new String[] {RegistryRole.Type.getRA_RoleName(ORG_CGOV.getCode())});
  
  public static final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo(TEST_DATA_KEY + "Admin", ORG_CGOV);
  
  public static final TestGeoObjectTypeInfo COUNTRY          = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Country", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public static final TestGeoObjectTypeInfo PROVINCE            = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Province", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public static final TestGeoObjectInfo     CAMBODIA         = new TestGeoObjectInfo(TEST_DATA_KEY + "Cambodia", COUNTRY);

  public static final TestGeoObjectInfo     PROV_CENTRAL     = new TestGeoObjectInfo(TEST_DATA_KEY + "CentralProvince", PROVINCE);
  
  public static final TestAttributeTypeInfo AT_National_Anthem = new TestAttributeTypeInfo("NationalAnthem", "National Anthem", COUNTRY, AttributeCharacterType.TYPE);
  
  public static final TestAttributeTypeInfo AT_PHONE_COUNTRY_CODE = new TestAttributeTypeInfo("PhoneCountryCode", "Phone Country Code", COUNTRY, AttributeIntegerType.TYPE);
  
  public static final TestAttributeTypeInfo AT_UN_MEMBER = new TestAttributeTypeInfo("UnMember", "UN Member", COUNTRY, AttributeBooleanType.TYPE);
  
  public static final TestAttributeTypeInfo AT_GDP = new TestAttributeTypeInfo("GDP", "Gross Domestic Product", COUNTRY, AttributeFloatType.TYPE);
  
  public static final TestAttributeTypeInfo AT_DATE_OF_FORMATION = new TestAttributeTypeInfo("DateOfFormation", "Date Of Formation", COUNTRY, AttributeDateType.TYPE);
  
  public static final TestAttributeTypeInfo AT_RELIGION = new TestAttributeTypeInfo("Religion", "Religion", COUNTRY, AttributeTermType.TYPE);
  
  public static final TestUserInfo          USER_CGOV_RM      = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovrm", "cgovrm", TEST_DATA_KEY + "cgovrm@noreply.com", new String[] {RegistryRole.Type.getRM_RoleName(ORG_CGOV.getCode(), COUNTRY.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CGOV.getCode(), PROVINCE.getCode())});
  
  public static final TestUserInfo          USER_CGOV_RC      = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovrc", "cgovrc", TEST_DATA_KEY + "cgovrc@noreply.com", new String[] {RegistryRole.Type.getRC_RoleName(ORG_CGOV.getCode(), COUNTRY.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CGOV.getCode(), PROVINCE.getCode())});
  
  public static final TestUserInfo          USER_CGOV_AC      = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovac", "cgovac", TEST_DATA_KEY + "cgovac@noreply.com", new String[] {RegistryRole.Type.getAC_RoleName(ORG_CGOV.getCode(), COUNTRY.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CGOV.getCode(), PROVINCE.getCode())});
  
  public Term T_Religion;
  
  public Term T_Buddhism;
  
  public Term T_Islam;
  
  public Term T_Christianity;
  
  public Term T_Other;
  
  {
    managedOrganizationInfos.add(ORG_CGOV);
    
    managedHierarchyTypeInfos.add(HIER_ADMIN);
    
    managedGeoObjectTypeInfos.add(COUNTRY);
    managedGeoObjectTypeInfos.add(PROVINCE);

    managedGeoObjectInfos.add(CAMBODIA);
    managedGeoObjectInfos.add(PROV_CENTRAL);
    
    managedUsers.add(USER_CGOV_RA);
    managedUsers.add(USER_CGOV_RM);
    managedUsers.add(USER_CGOV_RC);
    managedUsers.add(USER_CGOV_AC);
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
  
  @Transaction
  @Override
  protected void setUpMetadataInTrans()
  {
    super.setUpMetadataInTrans();
    
    this.AT_National_Anthem.apply();
    CAMBODIA.setDefaultValue(this.AT_National_Anthem.getAttributeName(), "Nokor Reach");
    
    this.AT_PHONE_COUNTRY_CODE.apply();
    CAMBODIA.setDefaultValue(this.AT_PHONE_COUNTRY_CODE.getAttributeName(), 855L);
    
    this.AT_GDP.apply();
    CAMBODIA.setDefaultValue(this.AT_GDP.getAttributeName(), 26.730D);
    
    this.AT_UN_MEMBER.apply();
    CAMBODIA.setDefaultValue(this.AT_UN_MEMBER.getAttributeName(), true);
    
    this.AT_DATE_OF_FORMATION.apply();
    CAMBODIA.setDefaultValue(this.AT_DATE_OF_FORMATION.getAttributeName(), new Date()); // TODO
    
    
    this.AT_RELIGION.apply();
    
    createReligionTerms();
    
    this.CAMBODIA.setDefaultValue(AT_RELIGION.getAttributeName(), T_Buddhism);
  }
  
  @Override
  protected void cleanUpClassInTrans()
  {
  }
  
  public void createReligionTerms()
  {
    T_Religion = TestDataSet.createAttributeRootTerm(this.COUNTRY, this.AT_RELIGION);
    
    T_Buddhism = TestDataSet.createTerm(this.AT_RELIGION, "Buddhism", "Buddhism");
    T_Islam = TestDataSet.createTerm(this.AT_RELIGION, "Islam", "Islam");
    T_Christianity = TestDataSet.createTerm(this.AT_RELIGION, "Christianity", "Christianity");
    T_Other = TestDataSet.createTerm(this.AT_RELIGION, "Other", "Other");
    
    Classifier rootClassy = TestDataSet.getClassifierIfExist(this.AT_RELIGION.getRootTerm().getCode());
    List<? extends Classifier> childClassifiers = rootClassy.getAllIsAChild().getAll();
    Assert.assertEquals(4, childClassifiers.size());
  }
}
