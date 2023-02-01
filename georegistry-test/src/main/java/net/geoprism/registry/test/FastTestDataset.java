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

import java.sql.Savepoint;
import java.util.Date;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.DuplicateGraphPathException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.Universal;

/**
 * The purpose of this dataset is to provide very basic data which provides for
 * very fast running of tests. This is achieved by creating the most basic data
 * needed for tests, and then not cleaning up the metadata at the end, and
 * re-using it for future iterations. DO NOT use this dataset if you'll be
 * adding / removing attributes or otherwise damaging the metadata. We will
 * however automatically clean up instance data like GeoObjects and the like.
 * 
 * @author rrowlands
 *
 */
public class FastTestDataset extends TestDataSet
{
  public static final String                    TEST_DATA_KEY         = "FAST";

  public static final TestOrganizationInfo      ORG_CGOV              = new TestOrganizationInfo(TEST_DATA_KEY + "_" + "CGOV", "Central Government");

  public static final TestOrganizationInfo      ORG_MOHA              = new TestOrganizationInfo(TEST_DATA_KEY + "_" + "MOHA", "Ministry of Home Affairs");

  public static final TestHierarchyTypeInfo     HIER_ADMIN            = new TestHierarchyTypeInfo(TEST_DATA_KEY + "Admin", ORG_CGOV);

  public static final TestHierarchyTypeInfo     HIER_HEALTH_ADMIN     = new TestHierarchyTypeInfo(TEST_DATA_KEY + "HealthAdmin", ORG_MOHA);
  
  public static final TestHierarchyTypeInfo     HIER_SPLIT_PARENT     = new TestHierarchyTypeInfo(TEST_DATA_KEY + "SplitParent", ORG_CGOV); // Used for testing inheritance
  
  public static final TestHierarchyTypeInfo     HIER_SPLIT_CHILD      = new TestHierarchyTypeInfo(TEST_DATA_KEY + "SplitChild", ORG_CGOV); // Used for testing inheritance

  public static final TestGeoObjectTypeInfo     COUNTRY               = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Country", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public static final TestGeoObjectTypeInfo     PROVINCE              = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Province", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public static final TestGeoObjectTypeInfo     PROVINCE_PRIVATE      = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "ProvincePrivate", GeometryType.MULTIPOLYGON, true, ORG_CGOV, null);

  public static final TestGeoObjectTypeInfo     DISTRICT              = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "District", GeometryType.MULTIPOLYGON, ORG_CGOV);

  public static final TestGeoObjectTypeInfo     HOSPITAL              = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "Hospital", GeometryType.MULTIPOLYGON, ORG_MOHA);

  public static final TestGeoObjectInfo         CAMBODIA              = new TestGeoObjectInfo(TEST_DATA_KEY + "Cambodia", COUNTRY);

  public static final TestGeoObjectInfo         PROV_CENTRAL          = new TestGeoObjectInfo(TEST_DATA_KEY + "CentralProvince", PROVINCE);

  public static final TestGeoObjectInfo         PROV_WESTERN          = new TestGeoObjectInfo(TEST_DATA_KEY + "WesternProvince", PROVINCE);

  public static final TestGeoObjectInfo         PROV_CENTRAL_PRIVATE  = new TestGeoObjectInfo(TEST_DATA_KEY + "CentralProvincePrivate", PROVINCE_PRIVATE);

  public static final TestGeoObjectInfo         DIST_CENTRAL          = new TestGeoObjectInfo(TEST_DATA_KEY + "CentralDistrict", DISTRICT);

  public static final TestGeoObjectInfo         CENTRAL_HOSPITAL      = new TestGeoObjectInfo(TEST_DATA_KEY + "CentralHospital", HOSPITAL);

  public static final TestAttributeTypeInfo     AT_National_Anthem    = new TestAttributeTypeInfo("NationalAnthem", "National Anthem", COUNTRY, AttributeCharacterType.TYPE);

  public static final TestAttributeTypeInfo     AT_PHONE_COUNTRY_CODE = new TestAttributeTypeInfo("PhoneCountryCode", "Phone Country Code", COUNTRY, AttributeIntegerType.TYPE);

  public static final TestAttributeTypeInfo     AT_UN_MEMBER          = new TestAttributeTypeInfo("UnMember", "UN Member", COUNTRY, AttributeBooleanType.TYPE);

  public static final TestAttributeTypeInfo     AT_GDP                = new TestAttributeTypeInfo("GDP", "Gross Domestic Product", COUNTRY, AttributeFloatType.TYPE);

  public static final TestAttributeTypeInfo     AT_DATE_OF_FORMATION  = new TestAttributeTypeInfo("DateOfFormation", "Date Of Formation", COUNTRY, AttributeDateType.TYPE);

  public static final TestAttributeTermTypeInfo AT_RELIGION           = new TestAttributeTermTypeInfo("Religion", "Religion", COUNTRY);

  public static final TestUserInfo              USER_CGOV_RA          = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovra", "cgovra", TEST_DATA_KEY + "cgovra@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG_CGOV.getCode()) });

  public static final TestUserInfo              USER_CGOV_RM          = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovrm", "cgovrm", TEST_DATA_KEY + "cgovrm@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(ORG_CGOV.getCode(), COUNTRY.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CGOV.getCode(), PROVINCE.getCode()) });

  public static final TestUserInfo              USER_CGOV_RC          = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovrc", "cgovrc", TEST_DATA_KEY + "cgovrc@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(ORG_CGOV.getCode(), COUNTRY.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CGOV.getCode(), PROVINCE.getCode()) });

  public static final TestUserInfo              USER_CGOV_AC          = new TestUserInfo(TEST_DATA_KEY + "_" + "cgovac", "cgovac", TEST_DATA_KEY + "cgovac@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(ORG_CGOV.getCode(), COUNTRY.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CGOV.getCode(), PROVINCE.getCode()) });

  public static final TestUserInfo              USER_MOHA_RA          = new TestUserInfo(TEST_DATA_KEY + "_" + "mohara", "mohara", TEST_DATA_KEY + "mohara@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG_MOHA.getCode()) });

  public static final TestUserInfo              USER_MOHA_RM          = new TestUserInfo(TEST_DATA_KEY + "_" + "moharm", "moharm", TEST_DATA_KEY + "moharm@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(ORG_MOHA.getCode(), HOSPITAL.getCode()) });

  public static final TestUserInfo              USER_MOHA_RC          = new TestUserInfo(TEST_DATA_KEY + "_" + "moharc", "moharc", TEST_DATA_KEY + "moharc@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(ORG_MOHA.getCode(), HOSPITAL.getCode()) });

  public static final TestUserInfo              USER_MOHA_AC          = new TestUserInfo(TEST_DATA_KEY + "_" + "mohaac", "mohaac", TEST_DATA_KEY + "mohaac@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(ORG_MOHA.getCode(), HOSPITAL.getCode()) });

  public static final TestUserInfo              USER_CGOV_RM_PRIVATE  = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + "cgovrmprivate", "cgovrmprivate", FastTestDataset.TEST_DATA_KEY + "cgovrmprivate@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.COUNTRY.getCode()), RegistryRole.Type.getRM_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.PROVINCE_PRIVATE.getCode()) });

  public static final TestUserInfo              USER_CGOV_RC_PRIVATE  = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + "cgovrcprivate", "cgovrcprivate", FastTestDataset.TEST_DATA_KEY + "cgovrcprivate@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.COUNTRY.getCode()), RegistryRole.Type.getRC_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.PROVINCE_PRIVATE.getCode()) });

  public static final TestUserInfo              USER_CGOV_AC_PRIVATE  = new TestUserInfo(FastTestDataset.TEST_DATA_KEY + "_" + "cgovacprivate", "cgovacprivate", FastTestDataset.TEST_DATA_KEY + "cgovacprivate@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.COUNTRY.getCode()), RegistryRole.Type.getAC_RoleName(FastTestDataset.ORG_CGOV.getCode(), FastTestDataset.PROVINCE_PRIVATE.getCode()) });

  public static final TestTermInfo              T_Religion            = new TestTermInfo("Religion", AT_RELIGION);

  public static final TestTermInfo              T_Buddhism            = new TestTermInfo("Buddhism", AT_RELIGION);

  public static final TestTermInfo              T_Islam               = new TestTermInfo("Islam", AT_RELIGION);

  public static final TestTermInfo              T_Christianity        = new TestTermInfo("Chistianity", AT_RELIGION);

  public static final TestTermInfo              T_Other               = new TestTermInfo("Other", AT_RELIGION);

  {
    managedOrganizationInfos.add(ORG_CGOV);
    managedOrganizationInfos.add(ORG_MOHA);

    managedHierarchyTypeInfos.add(HIER_ADMIN);
    managedHierarchyTypeInfos.add(HIER_HEALTH_ADMIN);
    managedHierarchyTypeInfos.add(HIER_SPLIT_PARENT);
    managedHierarchyTypeInfos.add(HIER_SPLIT_CHILD);

    managedGeoObjectTypeInfos.add(COUNTRY);
    managedGeoObjectTypeInfos.add(PROVINCE);
    managedGeoObjectTypeInfos.add(PROVINCE_PRIVATE);
    managedGeoObjectTypeInfos.add(DISTRICT);
    managedGeoObjectTypeInfos.add(HOSPITAL);

    managedGeoObjectInfos.add(CAMBODIA);
    managedGeoObjectInfos.add(PROV_CENTRAL);
    managedGeoObjectInfos.add(PROV_WESTERN);
    managedGeoObjectInfos.add(PROV_CENTRAL_PRIVATE);
    managedGeoObjectInfos.add(CENTRAL_HOSPITAL);
    managedGeoObjectInfos.add(DIST_CENTRAL);

    managedUsers.add(USER_CGOV_RA);
    managedUsers.add(USER_CGOV_RM);
    managedUsers.add(USER_CGOV_RC);
    managedUsers.add(USER_CGOV_AC);

    managedUsers.add(USER_MOHA_RA);
    managedUsers.add(USER_MOHA_RM);
    managedUsers.add(USER_MOHA_RC);
    managedUsers.add(USER_MOHA_AC);
    managedUsers.add(USER_CGOV_RM_PRIVATE);
    managedUsers.add(USER_CGOV_RC_PRIVATE);
    managedUsers.add(USER_CGOV_AC_PRIVATE);

    AT_RELIGION.addManagedTerm(T_Religion);
    AT_RELIGION.addManagedTerm(T_Buddhism);
    AT_RELIGION.addManagedTerm(T_Islam);
    AT_RELIGION.addManagedTerm(T_Christianity);
    AT_RELIGION.addManagedTerm(T_Other);
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
      COUNTRY.addChild(PROVINCE_PRIVATE, HIER_ADMIN);
      PROVINCE.addChild(DISTRICT, HIER_ADMIN);

      COUNTRY.getUniversal().addLink(Universal.getRoot(), HIER_HEALTH_ADMIN.getServerObject().getUniversalType());
      COUNTRY.addChild(PROVINCE, HIER_HEALTH_ADMIN);
      PROVINCE.addChild(HOSPITAL, HIER_HEALTH_ADMIN);
      PROVINCE.addChild(DISTRICT, HIER_HEALTH_ADMIN);
      
      COUNTRY.getUniversal().addLink(Universal.getRoot(), HIER_SPLIT_PARENT.getServerObject().getUniversalType());
      COUNTRY.addChild(PROVINCE, HIER_SPLIT_PARENT);
      
      PROVINCE.getUniversal().addLink(Universal.getRoot(), HIER_SPLIT_CHILD.getServerObject().getUniversalType());
      PROVINCE.addChild(DISTRICT, HIER_SPLIT_CHILD);
      
      PROVINCE.getServerObject().setInheritedHierarchy(HIER_SPLIT_CHILD.getServerObject(), HIER_SPLIT_PARENT.getServerObject());
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
      // CAMBODIA.getGeoEntity().addLink(GeoEntity.getRoot(),
      // HIER_ADMIN.getServerObject().getEntityType());

      CAMBODIA.addChild(PROV_CENTRAL, HIER_ADMIN);
      CAMBODIA.addChild(PROV_WESTERN, HIER_ADMIN);
      CAMBODIA.addChild(PROV_CENTRAL_PRIVATE, HIER_ADMIN);
      PROV_CENTRAL.addChild(DIST_CENTRAL, HIER_ADMIN);

      CAMBODIA.addChild(PROV_CENTRAL, HIER_HEALTH_ADMIN);
      PROV_CENTRAL.addChild(CENTRAL_HOSPITAL, HIER_HEALTH_ADMIN);
      PROV_CENTRAL.addChild(DIST_CENTRAL, HIER_HEALTH_ADMIN);
      
      CAMBODIA.addChild(PROV_CENTRAL, HIER_SPLIT_PARENT);
      CAMBODIA.addChild(PROV_WESTERN, HIER_SPLIT_PARENT);
      PROV_CENTRAL.addChild(DIST_CENTRAL, HIER_SPLIT_CHILD);
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

    AT_National_Anthem.apply();
    CAMBODIA.setDefaultValue(AT_National_Anthem.getAttributeName(), "Nokor Reach");

    AT_PHONE_COUNTRY_CODE.apply();
    CAMBODIA.setDefaultValue(AT_PHONE_COUNTRY_CODE.getAttributeName(), 855L);

    AT_GDP.apply();
    CAMBODIA.setDefaultValue(AT_GDP.getAttributeName(), 26.730D);

    AT_UN_MEMBER.apply();
    CAMBODIA.setDefaultValue(AT_UN_MEMBER.getAttributeName(), true);

    AT_DATE_OF_FORMATION.apply();
    CAMBODIA.setDefaultValue(AT_DATE_OF_FORMATION.getAttributeName(), new Date()); // TODO

    AT_RELIGION.apply();

    CAMBODIA.setDefaultValue(AT_RELIGION.getAttributeName(), T_Buddhism.fetchTerm());
  }

  @Override
  protected void cleanUpClassInTrans()
  {
  }
}
