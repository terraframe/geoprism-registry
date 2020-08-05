/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.SmartExceptionDTO;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdBusinessInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMomentDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestHierarchyTypeInfo;

public class HierarchyServiceTest
{

  public static final TestGeoObjectTypeInfo TEST_GOT = new TestGeoObjectTypeInfo("HMST_Country", FastTestDataset.ORG_CGOV);

  public static final TestHierarchyTypeInfo TEST_HT  = new TestHierarchyTypeInfo("HMST_ReportDiv", FastTestDataset.ORG_CGOV);

  protected static FastTestDataset          testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    testData.tearDownMetadata();
  }

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    deleteExtraMetadata();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    deleteExtraMetadata();

    testData.tearDownInstanceData();
  }

  private void deleteExtraMetadata()
  {
    TEST_HT.delete();
    TEST_GOT.delete();
  }

  @Test
  public void testCreateHierarchyType()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, testData.adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

    Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

    Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

    HierarchyType hierarchy = hierarchies[0];

    Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());

    // test the types that were created
    String mdTermRelUniversal = ServerHierarchyType.buildMdTermRelUniversalKey(reportingDivision.getCode());
    String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
    String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode();
    Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
  }

  @Test
  public void testCreateHierarchyTypeWithOrganization()
  {
    HierarchyType reportingDivision = null;

    String organizationCode = testData.ORG_CGOV.getCode();

    reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, testData.adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    ServiceFactory.getHierarchyService().createHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType[] hierarchies = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), new String[] { TEST_HT.getCode() }, PermissionContext.READ);

    Assert.assertNotNull("The created hierarchy was not returned", hierarchies);

    Assert.assertEquals("The wrong number of hierarchies were returned.", 1, hierarchies.length);

    HierarchyType hierarchy = hierarchies[0];

    Assert.assertEquals("Reporting Division", hierarchy.getLabel().getValue());

    Assert.assertEquals(organizationCode, hierarchy.getOrganizationCode());

    // test the types that were created
    String mdTermRelUniversal = ServerHierarchyType.buildMdTermRelUniversalKey(reportingDivision.getCode());
    String expectedMdTermRelUniversal = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    Assert.assertEquals("The type name of the MdTermRelationshp defining the universals was not correctly defined for the given code.", expectedMdTermRelUniversal, mdTermRelUniversal);

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(reportingDivision.getCode());
    String expectedMdTermRelGeoEntity = GISConstants.GEO_PACKAGE + "." + reportingDivision.getCode();
    Assert.assertEquals("The type name of the MdTermRelationshp defining the geoentities was not correctly defined for the given code.", expectedMdTermRelGeoEntity, mdTermRelGeoEntity);
  }

  @Test(expected = SmartExceptionDTO.class)
  public void testCreateHierarchyTypeAsDifferentOrg()
  {
    String organizationCode = FastTestDataset.ORG_CGOV.getCode();

    HierarchyType reportingDivision = MetadataFactory.newHierarchyType(TEST_HT.getCode(), new LocalizedValue("Reporting Division"), new LocalizedValue("The rporting division hieracy..."), organizationCode, testData.adapter);
    String gtJSON = reportingDivision.toJSON().toString();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {

      ServiceFactory.getHierarchyService().createHierarchyType(request.getSessionId(), gtJSON);
    });
  }

  @Test
  public void testUpdateHierarchyType()
  {
    HierarchyType reportingDivision = FastTestDataset.HIER_ADMIN.toDTO();
    String gtJSON = reportingDivision.toJSON().toString();

    reportingDivision.setLabel(new LocalizedValue("Reporting Division 2"));

    reportingDivision.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    gtJSON = reportingDivision.toJSON().toString();

    reportingDivision = ServiceFactory.getHierarchyService().updateHierarchyType(testData.clientSession.getSessionId(), gtJSON);

    Assert.assertNotNull("The created hierarchy was not returned", reportingDivision);
    Assert.assertEquals("Reporting Division 2", reportingDivision.getLabel().getValue());
    Assert.assertEquals("The rporting division hieracy 2", reportingDivision.getDescription().getValue());
  }

  @Test(expected = SmartExceptionDTO.class)
  public void testUpdateHierarchyTypeAsDifferentOrg()
  {
    HierarchyType reportingDivision = FastTestDataset.HIER_ADMIN.toDTO();
    reportingDivision.setLabel(new LocalizedValue("Reporting Division 2"));
    reportingDivision.setDescription(new LocalizedValue("The rporting division hieracy 2"));

    final String updateJSON = reportingDivision.toJSON().toString();

    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {

      ServiceFactory.getHierarchyService().updateHierarchyType(request.getSessionId(), updateJSON);
    });
  }

  @Test
  public void testGetHierarchyTypeAsDifferentOrgWithWriteContext()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {
      HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(request.getSessionId(), null, PermissionContext.WRITE);

      Assert.assertEquals(0, hierarchyTypes.length);
    });
  }

  @Test
  public void testGetHierarchyTypeAsDifferentOrgWithReadContext()
  {
    FastTestDataset.runAsUser(FastTestDataset.USER_MOHA_RA, (request, adapter) -> {
      HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(request.getSessionId(), null, PermissionContext.READ);

      Assert.assertEquals(1, hierarchyTypes.length);
    });
  }

  @Test
  public void testAddToHierarchy()
  {
    String organizationCode = testData.ORG_CGOV.getCode();

    GeoObjectType country = MetadataFactory.newGeoObjectType(TEST_GOT.getCode(), GeometryType.POLYGON, new LocalizedValue("Country Test"), new LocalizedValue("Some Description"), true, organizationCode, testData.adapter);

    // Create the GeoObjectTypes
    String gtJSON = country.toJSON().toString();
    country = testData.adapter.createGeoObjectType(testData.clientSession.getSessionId(), gtJSON);

    HierarchyType adminHierarchy = ServiceFactory.getHierarchyService().addToHierarchy(testData.clientSession.getSessionId(), testData.HIER_ADMIN.getCode(), Universal.ROOT, country.getCode());

    List<HierarchyType.HierarchyNode> rootGots = adminHierarchy.getRootGeoObjectTypes();

    for (HierarchyType.HierarchyNode node : rootGots)
    {
      if (node.getGeoObjectType().getCode().equals(country.getCode()))
      {
        return;
      }
    }

    Assert.fail("We did not find the child we just added.");
  }

  @Request
  private void checkReferenceAttribute(String hierarchyTypeCode, String parentCode, String childCode)
  {
    Universal parentUniversal = Universal.getByKey(parentCode);
    Universal childUniversal = Universal.getByKey(childCode);

    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyTypeCode);

    String refAttrName = hierarchyType.getParentReferenceAttributeName(parentUniversal);

    MdBusiness childMdBusiness = childUniversal.getMdBusiness();
    MdBusinessDAOIF childMdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(childMdBusiness);

    try
    {
      MdAttributeConcreteDAOIF mdAttribute = childMdBusinessDAOIF.definesAttribute(refAttrName);

      Assert.assertNotNull("By adding a leaf type as a child of a non-leaf type, a reference attribute [" + refAttrName + "] to the parent was not defined on the child.", mdAttribute);

    }
    catch (DataNotFoundException e)
    {
      Assert.fail("Attribute that implements GeoObject.UID does not exist. It should be defined on the business class");
    }
  }

  @Test
  public void testHierarchyType()
  {
    HierarchyType[] hierarchyTypes = ServiceFactory.getHierarchyService().getHierarchyTypes(testData.clientSession.getSessionId(), null, PermissionContext.READ);

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      System.out.println(hierarchyType.toJSON());
    }
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelUniversal()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();

    String mdTermRelUniversalType = ServerHierarchyType.buildMdTermRelUniversalKey(locatedInClassName);

    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", AllowedIn.CLASS, mdTermRelUniversalType);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelUniversal_To_HierarchyCode()
  {
    String allowedInClass = AllowedIn.CLASS;

    String hierarchyCode = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(allowedInClass);

    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testLocatedInCode_To_MdTermRelGeoEntity()
  {
    String locatedInClassName = LocatedIn.class.getSimpleName();

    String mdTermRelGeoEntity = ServerHierarchyType.buildMdTermRelGeoEntityKey(locatedInClassName);

    Assert.assertEquals("HierarchyCode LocatedIn did not get converted to the AllowedIn Universal relationshipType.", LocatedIn.CLASS, mdTermRelGeoEntity);
  }

  /**
   * The hardcoded {@link AllowedIn} and {@link LocatedIn} relationship do not
   * follow the common geo registry convention.
   */
  @Test
  public void testToMdTermRelGeoEntity_To_HierarchyCode()
  {
    String locatedInClass = LocatedIn.CLASS;

    String hierarchyCode = ServerHierarchyType.buildHierarchyKeyFromMdTermRelGeoEntity(locatedInClass);

    Assert.assertEquals("AllowedIn relationship type did not get converted into the LocatedIn  hierarchy code", LocatedIn.class.getSimpleName(), hierarchyCode);
  }
}
