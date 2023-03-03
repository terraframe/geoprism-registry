/**
 *
 */
package net.geoprism.registry.test;

import java.util.Date;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.dataaccess.transaction.Transaction;

/**
 * This dataset defines a parent Type and Geo-Object called ALL, and then creates children Types and Geo-Objects for each of the different attribute types.
 * The ALL parent contians all custom attributes of all different data types. Each child contains one of the custom attributes.
 * 
 * All of the data is deleted and created again on class setup/teardown.
 * 
 * @author rrowlands
 */
public class AllAttributesDataset extends TestDataSet
{
  public static final String                TEST_DATA_KEY                = "AllAttr";

  public static final TestOrganizationInfo  ORG                          = new TestOrganizationInfo(TEST_DATA_KEY + "Org");

  public static final TestUserInfo          USER_ORG_RA                  = new TestUserInfo(TEST_DATA_KEY + "_" + "ra", "ra", TEST_DATA_KEY + "@noreply.com", new String[] { RegistryRole.Type.getRA_RoleName(ORG.getCode()) });

  public static final TestHierarchyTypeInfo HIER                         = new TestHierarchyTypeInfo(TEST_DATA_KEY + "Hier", ORG);

  public static final TestGeoObjectTypeInfo GOT_ALL                      = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "All", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectTypeInfo GOT_CHAR                     = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "CHAR", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectTypeInfo GOT_LOCAL                    = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "LOCAL", GeometryType.MULTIPOLYGON, ORG);
  
  public static final TestGeoObjectTypeInfo GOT_INT                      = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "INT", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectTypeInfo GOT_FLOAT                    = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "FLOAT", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectTypeInfo GOT_BOOL                     = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "BOOL", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectTypeInfo GOT_DATE                     = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "DATE", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectTypeInfo GOT_TERM                     = new TestGeoObjectTypeInfo(TEST_DATA_KEY + "TERM", GeometryType.MULTIPOLYGON, ORG);

  public static final TestGeoObjectInfo     GO_ALL                       = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_ALL", GOT_ALL);

  public static final TestGeoObjectInfo     GO_CHAR                      = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_CHAR", GOT_CHAR);
  
  public static final TestGeoObjectInfo     GO_LOCAL                     = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_LOCAL", GOT_LOCAL);

  public static final TestGeoObjectInfo     GO_INT                       = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_INT", GOT_INT);

  public static final TestGeoObjectInfo     GO_FLOAT                     = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_FLOAT", GOT_FLOAT);

  public static final TestGeoObjectInfo     GO_BOOL                      = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_BOOL", GOT_BOOL);

  public static final TestGeoObjectInfo     GO_DATE                      = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_DATE", GOT_DATE);

  public static final TestGeoObjectInfo     GO_TERM                      = new TestGeoObjectInfo(TEST_DATA_KEY + "GO_TERM", GOT_TERM);

  public static final TestAttributeTypeInfo              AT_ALL_CHAR = new TestAttributeTypeInfo("testcharacterall", "testcharacterall", GOT_ALL, AttributeCharacterType.TYPE);

  public static final TestAttributeTypeInfo              AT_GO_CHAR = new TestAttributeTypeInfo("testcharacter", "testcharacter", GOT_CHAR, AttributeCharacterType.TYPE);
  
  public static final TestAttributeTypeInfo              AT_ALL_LOCAL = new TestAttributeTypeInfo("testlocalall", "testlocalall", GOT_ALL, AttributeLocalType.TYPE);

  public static final TestAttributeTypeInfo              AT_GO_LOCAL = new TestAttributeTypeInfo("testlocal", "testlocal", GOT_LOCAL, AttributeLocalType.TYPE);

  public static final TestAttributeTypeInfo              AT_ALL_INT = new TestAttributeTypeInfo("testintegerall", "testintegerall", GOT_ALL, AttributeIntegerType.TYPE);

  public static final TestAttributeTypeInfo              AT_GO_INT = new TestAttributeTypeInfo("testinteger", "testinteger", GOT_INT, AttributeIntegerType.TYPE);

  public static final TestAttributeTypeInfo              AT_ALL_FLOAT = new TestAttributeTypeInfo("testfloatall", "testfloatall", GOT_ALL, AttributeFloatType.TYPE);

  public static final TestAttributeTypeInfo              AT_GO_FLOAT = new TestAttributeTypeInfo("testfloat", "testfloat", GOT_FLOAT, AttributeFloatType.TYPE);

  public static final TestAttributeTypeInfo              AT_ALL_BOOL = new TestAttributeTypeInfo("testbooleanall", "testbooleanall", GOT_ALL, AttributeBooleanType.TYPE);

  public static final TestAttributeTypeInfo              AT_GO_BOOL = new TestAttributeTypeInfo("testboolean", "testboolean", GOT_BOOL, AttributeBooleanType.TYPE);

  public static final TestAttributeTypeInfo              AT_ALL_DATE = new TestAttributeTypeInfo("testdateall", "testdateall", GOT_ALL, AttributeDateType.TYPE);

  public static final TestAttributeTypeInfo              AT_GO_DATE = new TestAttributeTypeInfo("testdate", "testdate", GOT_DATE, AttributeDateType.TYPE);

  public static final TestAttributeTermTypeInfo          AT_ALL_TERM = new TestAttributeTermTypeInfo("testtermall", "testtermall", GOT_ALL);

  public static final TestAttributeTermTypeInfo          AT_GO_TERM = new TestAttributeTermTypeInfo("testterm", "testterm", GOT_TERM);

  // Terms for the ALL GOT
  public static final TestTermInfo                TERM_ALL_VAL1 = new TestTermInfo(TEST_DATA_KEY + "ALL_VAL1", AT_ALL_TERM);

  public static final TestTermInfo                TERM_ALL_VAL2 = new TestTermInfo(TEST_DATA_KEY + "ALL_VAL2", AT_ALL_TERM);

  // Terms for the Term GOT
  public static final TestTermInfo                TERM_TERM_VAL1 = new TestTermInfo(TEST_DATA_KEY + "GO_VAL1", AT_GO_TERM);

  public static final TestTermInfo                TERM_TERM_VAL2 = new TestTermInfo(TEST_DATA_KEY + "GO_VAL2", AT_GO_TERM);

  private final static String               ROOT_TEST_TERM_CLASSIFIER_ID = TEST_DATA_KEY + "_ROOT";

  public static final Date                  GO_DATE_VALUE                = new Date();

  {
    managedOrganizationInfos.add(ORG);

    managedHierarchyTypeInfos.add(HIER);

    managedGeoObjectTypeInfos.add(GOT_ALL);
    managedGeoObjectTypeInfos.add(GOT_CHAR);
    managedGeoObjectTypeInfos.add(GOT_LOCAL);
    managedGeoObjectTypeInfos.add(GOT_INT);
    managedGeoObjectTypeInfos.add(GOT_FLOAT);
    managedGeoObjectTypeInfos.add(GOT_BOOL);
    managedGeoObjectTypeInfos.add(GOT_DATE);
    managedGeoObjectTypeInfos.add(GOT_TERM);

    managedGeoObjectInfos.add(GO_ALL);
    managedGeoObjectInfos.add(GO_CHAR);
    managedGeoObjectInfos.add(GO_LOCAL);
    managedGeoObjectInfos.add(GO_INT);
    managedGeoObjectInfos.add(GO_FLOAT);
    managedGeoObjectInfos.add(GO_BOOL);
    managedGeoObjectInfos.add(GO_DATE);
    managedGeoObjectInfos.add(GO_TERM);

    managedUsers.add(USER_ORG_RA);
    
    AT_ALL_TERM.addManagedTerm(TERM_ALL_VAL1);
    AT_ALL_TERM.addManagedTerm(TERM_ALL_VAL2);
    AT_GO_TERM.addManagedTerm(TERM_TERM_VAL1);
    AT_GO_TERM.addManagedTerm(TERM_TERM_VAL2);
  }

  public static AllAttributesDataset newTestData()
  {
    return new AllAttributesDataset();
  }

  @Transaction
  @Override
  protected void setUpMetadataInTrans()
  {
    super.setUpMetadataInTrans();

    AT_ALL_CHAR.apply();
    GO_ALL.setDefaultValue(AT_ALL_CHAR.getAttributeName(), "Test Attribute Text Value 123");
    AT_GO_CHAR.apply();
    GO_CHAR.setDefaultValue(AT_GO_CHAR.getAttributeName(), "Test Attribute Text Value 123");
    
    AT_ALL_LOCAL.apply();
    GO_ALL.setDefaultValue(AT_ALL_LOCAL.getAttributeName(), "Test All Attribute Local Value 123");
    AT_GO_LOCAL.apply();
    GO_LOCAL.setDefaultValue(AT_GO_LOCAL.getAttributeName(), "Test Attribute Local Value 123");

    AT_ALL_INT.apply();
    GO_ALL.setDefaultValue(AT_ALL_INT.getAttributeName(), 123L);
    AT_GO_INT.apply();
    GO_INT.setDefaultValue(AT_GO_INT.getAttributeName(), 123L);

    AT_ALL_FLOAT.apply();
    GO_ALL.setDefaultValue(AT_ALL_FLOAT.getAttributeName(), 123.123D);
    AT_GO_FLOAT.apply();
    GO_FLOAT.setDefaultValue(AT_GO_FLOAT.getAttributeName(), 123.123D);

    AT_ALL_BOOL.apply();
    GO_ALL.setDefaultValue(AT_ALL_BOOL.getAttributeName(), true);
    AT_GO_BOOL.apply();
    GO_BOOL.setDefaultValue(AT_GO_BOOL.getAttributeName(), true);

    AT_ALL_DATE.apply();
    GO_ALL.setDefaultValue(AT_ALL_DATE.getAttributeName(), GO_DATE_VALUE);
    AT_GO_DATE.apply();
    GO_DATE.setDefaultValue(AT_GO_DATE.getAttributeName(), GO_DATE_VALUE);

    AT_ALL_TERM.apply();
    AT_GO_TERM.apply();
    
    GO_ALL.setDefaultValue(AT_ALL_TERM.getAttributeName(), TERM_ALL_VAL1.fetchTerm());
    GO_TERM.setDefaultValue(AT_GO_TERM.getAttributeName(), TERM_TERM_VAL1.fetchTerm());
  }

  @Transaction
  @Override
  protected void cleanUpClassInTrans()
  {
    super.cleanUpClassInTrans();

    deleteTestTerms();
  }

  public void deleteTestTerms()
  {
    TestDataSet.deleteClassifier(ROOT_TEST_TERM_CLASSIFIER_ID);
    TestDataSet.deleteClassifier(TEST_DATA_KEY + "ALL_VAL1");
    TestDataSet.deleteClassifier(TEST_DATA_KEY + "ALL_VAL2");
    TestDataSet.deleteClassifier(TEST_DATA_KEY + "_TERMVAL1");
    TestDataSet.deleteClassifier(TEST_DATA_KEY + "_TERMVAL2");
  }

  @Transaction
  @Override
  public void setUpClassRelationships()
  {
    HIER.setRoot(GOT_ALL);

    GOT_ALL.addChild(GOT_CHAR, HIER);
    GOT_ALL.addChild(GOT_LOCAL, HIER);
    GOT_ALL.addChild(GOT_INT, HIER);
    GOT_ALL.addChild(GOT_FLOAT, HIER);
    GOT_ALL.addChild(GOT_BOOL, HIER);
    GOT_ALL.addChild(GOT_DATE, HIER);
    GOT_ALL.addChild(GOT_TERM, HIER);
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
//    GO_ALL.getGeoEntity().addLink(GeoEntity.getRoot(), HIER.getServerObject().getEntityType());

    GO_ALL.addChild(GO_CHAR, HIER);
    GO_ALL.addChild(GO_LOCAL, HIER);
    GO_ALL.addChild(GO_INT, HIER);
    GO_ALL.addChild(GO_FLOAT, HIER);
    GO_ALL.addChild(GO_BOOL, HIER);
    GO_ALL.addChild(GO_DATE, HIER);
    GO_ALL.addChild(GO_TERM, HIER);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
