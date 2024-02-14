/**
 *
 */
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.transaction.Transaction;

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
  
  public static final TestGeoObjectTypeInfo GOT_Commune           = new TestGeoObjectTypeInfo("Commune", GeometryType.MULTIPOLYGON, ORG_CENTRAL);
  
  public static final TestGeoObjectTypeInfo GOT_Village           = new TestGeoObjectTypeInfo("Village", GeometryType.MULTIPOINT, ORG_CENTRAL);
  
  public static final TestGeoObjectTypeInfo GOT_Hospital          = new TestGeoObjectTypeInfo("Hospital", GeometryType.MULTIPOLYGON, ORG_MOH);

  public static final TestGeoObjectInfo     GO_Cambodia           = new TestGeoObjectInfo("Cambodia", "855", GOT_Country, CambodiaTestGeometryImporter.getGeometry("855"), true, false);
  
  public static final TestGeoObjectInfo     GO_Oddar_Meanchey     = new TestGeoObjectInfo("Oddar Meanchey", "KH-22", GOT_Province, CambodiaTestGeometryImporter.getGeometry("KH-22"), true, false);
  
  public static final TestGeoObjectInfo     GO_Siem_Reap          = new TestGeoObjectInfo("Siem Reap", "KH-17", GOT_Province, TestDataSet.WKT_DEFAULT_MULTIPOLYGON, true, false);
  
  public static final TestGeoObjectInfo     GO_Pursat             = new TestGeoObjectInfo("Pursat", "KH-15", GOT_Province, TestDataSet.WKT_DEFAULT_MULTIPOLYGON, true, false);
  
  public static final TestGeoObjectInfo     GO_Banteay_Meanchey   = new TestGeoObjectInfo("Banteay Meanchey", "KH-1", GOT_Province, TestDataSet.WKT_DEFAULT_MULTIPOLYGON, true, false);
  
  public static final TestGeoObjectInfo     GO_Battambang         = new TestGeoObjectInfo("Battambang", "KH-2", GOT_Province, TestDataSet.WKT_DEFAULT_MULTIPOLYGON, true, false);
  
  public static final TestGeoObjectInfo     GO_PreahVihear         = new TestGeoObjectInfo("Preah Vihear", "KH-13", GOT_Province, TestDataSet.WKT_DEFAULT_MULTIPOLYGON, true, false);
  
  public static final TestGeoObjectInfo     GO_BanteayAmpil       = new TestGeoObjectInfo("Banteay Ampil", "2202", GOT_District, CambodiaTestGeometryImporter.getGeometry("2202"), true, false);
  
  public static final TestGeoObjectInfo     GO_ChongKal           = new TestGeoObjectInfo("Chong Kal", "2203", GOT_District, CambodiaTestGeometryImporter.getGeometry("2203"), true, false);
  
  public static final TestGeoObjectInfo     GO_AnlongVeng         = new TestGeoObjectInfo("Anlong Veng", "2201", GOT_District, CambodiaTestGeometryImporter.getGeometry("2201"), true, false);
  
  public static final TestGeoObjectInfo     GO_SamraongMunicipality = new TestGeoObjectInfo("Samraong Municipality", "2204", GOT_District, CambodiaTestGeometryImporter.getGeometry("2204"), true, false);
  
  public static final TestGeoObjectInfo     GO_TrapeangPrasat     = new TestGeoObjectInfo("Trapeang Prasat", "2205", GOT_District, CambodiaTestGeometryImporter.getGeometry("2205"), true, false);
  
  public static final TestUserInfo          USER_CGOV_RM          = new TestUserInfo("cgovrm", "cgovrm", "cgovrm@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Country.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Province.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_District.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Commune.getCode()), RegistryRole.Type.getRM_RoleName(ORG_CENTRAL.getCode(), GOT_Village.getCode()) });

  public static final TestUserInfo          USER_CGOV_RC          = new TestUserInfo("cgovrc", "cgovrc", "cgovrc@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Country.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Province.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_District.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Commune.getCode()), RegistryRole.Type.getRC_RoleName(ORG_CENTRAL.getCode(), GOT_Village.getCode()) });

  public static final TestUserInfo          USER_CGOV_AC          = new TestUserInfo("cgovac", "cgovac", "cgovac@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Country.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Province.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_District.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Commune.getCode()), RegistryRole.Type.getAC_RoleName(ORG_CENTRAL.getCode(), GOT_Village.getCode()) });
  
  public static final TestUserInfo          USER_MOHA_RA          = new TestUserInfo("mohra", "mohra", "mohara@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG_MOH.getCode()) });
  
  public static final TestUserInfo          USER_MOHA_RM          = new TestUserInfo("mohrm", "mohrm", "moharm@noreply.com", new String[] { RegistryRole.Type.getRM_RoleName(ORG_MOH.getCode(), GOT_Hospital.getCode()) });

  public static final TestUserInfo          USER_MOHA_RC          = new TestUserInfo("mohrc", "mohrc", "moharc@noreply.com", new String[] { RegistryRole.Type.getRC_RoleName(ORG_MOH.getCode(), GOT_Hospital.getCode()) });

  public static final TestUserInfo          USER_MOHA_AC          = new TestUserInfo("mohac", "mohac", "mohaac@noreply.com", new String[] { RegistryRole.Type.getAC_RoleName(ORG_MOH.getCode(), GOT_Hospital.getCode()) });
  
  public static final TestAttributeTypeInfo AT_National_Anthem    = new TestAttributeTypeInfo("NationalAnthem", "National Anthem", GOT_Country, AttributeCharacterType.TYPE);
  
  public static final TestAttributeTypeInfo AT_SHORT_NAME         = new TestAttributeTypeInfo("shortName", "Short Name", GOT_Country, AttributeLocalType.TYPE);

  public static final TestAttributeTypeInfo AT_PHONE_COUNTRY_CODE = new TestAttributeTypeInfo("PhoneCountryCode", "Phone Country Code", GOT_Country, AttributeIntegerType.TYPE);

  public static final TestAttributeTypeInfo AT_UN_MEMBER          = new TestAttributeTypeInfo("UnMember", "UN Member", GOT_Country, AttributeBooleanType.TYPE);

  public static final TestAttributeTypeInfo AT_GDP                = new TestAttributeTypeInfo("GDP", "Gross Domestic Product", GOT_Country, AttributeFloatType.TYPE);

  public static final TestAttributeTypeInfo AT_DATE_OF_FORMATION  = new TestAttributeTypeInfo("DateOfFormation", "Date Of Formation", GOT_Country, AttributeDateType.TYPE);
  
  public static final TestAttributeTermTypeInfo AT_RELIGION       = new TestAttributeTermTypeInfo("Religion", "Religion", GOT_Country);
  
  public static final TestTermInfo                       T_Religion = new TestTermInfo("Religion", AT_RELIGION);

  public static final TestTermInfo                       T_Buddhism = new TestTermInfo("Buddhism", AT_RELIGION);

  public static final TestTermInfo                       T_Islam = new TestTermInfo("Islam", AT_RELIGION);

  public static final TestTermInfo                       T_Christianity = new TestTermInfo("Chistianity", AT_RELIGION);

  public static final TestTermInfo                       T_Other = new TestTermInfo("Other", AT_RELIGION);

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
    managedGeoObjectInfos.add(GO_Siem_Reap);
    managedGeoObjectInfos.add(GO_Pursat);
    managedGeoObjectInfos.add(GO_Banteay_Meanchey);
    managedGeoObjectInfos.add(GO_Battambang);
    managedGeoObjectInfos.add(GO_PreahVihear);
    managedGeoObjectInfos.add(GO_BanteayAmpil);
    managedGeoObjectInfos.add(GO_ChongKal);
    managedGeoObjectInfos.add(GO_AnlongVeng);
    managedGeoObjectInfos.add(GO_SamraongMunicipality);
    managedGeoObjectInfos.add(GO_TrapeangPrasat);
    
    managedUsers.add(USER_ORG_RA);
    managedUsers.add(USER_MOH_RA);
    
    managedUsers.add(USER_CGOV_RM);
    managedUsers.add(USER_CGOV_RC);
    managedUsers.add(USER_CGOV_AC);
    
    managedUsers.add(USER_MOHA_RA);
    managedUsers.add(USER_MOHA_RM);
    managedUsers.add(USER_MOHA_RC);
    managedUsers.add(USER_MOHA_AC);
    
    AT_RELIGION.addManagedTerm(T_Religion);
    AT_RELIGION.addManagedTerm(T_Buddhism);
    AT_RELIGION.addManagedTerm(T_Islam);
    AT_RELIGION.addManagedTerm(T_Christianity);
    AT_RELIGION.addManagedTerm(T_Other);
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
    GOT_Country.addChild(GOT_Province, HIER_ADMIN);
    GOT_Province.addChild(GOT_District, HIER_ADMIN);
    GOT_District.addChild(GOT_Commune, HIER_ADMIN);
    GOT_Commune.addChild(GOT_Village, HIER_ADMIN);
    
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
    
    // Provinces
    GO_Cambodia.addChild(GO_Oddar_Meanchey, HIER_ADMIN);
    GO_Cambodia.addChild(GO_Siem_Reap, HIER_ADMIN);
    GO_Cambodia.addChild(GO_Pursat, HIER_ADMIN);
    GO_Cambodia.addChild(GO_Banteay_Meanchey, HIER_ADMIN);
    GO_Cambodia.addChild(GO_Battambang, HIER_ADMIN);
    GO_Cambodia.addChild(GO_PreahVihear, HIER_ADMIN);
    
    // Districts
    GO_Oddar_Meanchey.addChild(GO_BanteayAmpil, HIER_ADMIN);
    GO_Oddar_Meanchey.addChild(GO_ChongKal, HIER_ADMIN);
    GO_Oddar_Meanchey.addChild(GO_AnlongVeng, HIER_ADMIN);
    GO_Oddar_Meanchey.addChild(GO_SamraongMunicipality, HIER_ADMIN);
    GO_Oddar_Meanchey.addChild(GO_TrapeangPrasat, HIER_ADMIN);
  }
  
  @Transaction
  @Override
  protected void setUpMetadataInTrans()
  {
    super.setUpMetadataInTrans();
    
    AT_National_Anthem.apply();
    GO_Cambodia.setDefaultValue(AT_National_Anthem.getAttributeName(), "Nokor Reach");
    
    AT_SHORT_NAME.apply();
    GO_Cambodia.setDefaultValue(AT_SHORT_NAME.getAttributeName(), "Cam");

    AT_PHONE_COUNTRY_CODE.apply();
    GO_Cambodia.setDefaultValue(AT_PHONE_COUNTRY_CODE.getAttributeName(), 855L);

    AT_GDP.apply();
    GO_Cambodia.setDefaultValue(AT_GDP.getAttributeName(), 26.730D);

    AT_UN_MEMBER.apply();
    GO_Cambodia.setDefaultValue(AT_UN_MEMBER.getAttributeName(), true);

    AT_DATE_OF_FORMATION.apply();
    GO_Cambodia.setDefaultValue(AT_DATE_OF_FORMATION.getAttributeName(), TestDataSet.DEFAULT_OVER_TIME_DATE);

    AT_RELIGION.apply();

    GO_Cambodia.setDefaultValue(AT_RELIGION.getAttributeName(), T_Buddhism.fetchTerm());
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
