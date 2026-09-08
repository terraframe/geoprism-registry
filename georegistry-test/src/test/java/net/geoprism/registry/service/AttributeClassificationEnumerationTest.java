/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ConceptObjectService;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestGeoObjectTypeInfo;
import net.geoprism.registry.test.TestRegistryClient;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.DiscreteType;
import net.geoprism.registry.view.NodeDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.Page;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class AttributeClassificationEnumerationTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final String                 TEST_KEY = "ATTRCLASSTEST";

  public static TestGeoObjectTypeInfo        TEST_GOT = new TestGeoObjectTypeInfo("GOTTest_TEST1", FastTestDataset.ORG_CGOV);

  public static final TestGeoObjectInfo      TEST_GO  = new TestGeoObjectInfo(TEST_KEY + "_NeverNeverLand", TEST_GOT, FastTestDataset.SOURCE);

  private static AttributeClassificationType testClassification;

  @Autowired
  private TestRegistryClient                 client;

  @Autowired
  private GeoObjectTypeBusinessServiceIF     gotService;

  @Autowired
  private ConceptObjectService               service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    TEST_GOT.apply();

    ServerGeoObjectType got = TEST_GOT.getServerObject();
    testClassification = (AttributeClassificationType) gotService.createAttributeType(got, this.createAttributeClassificationType());
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    TEST_GOT.delete();

    super.afterClassSetup();
  }

  @Override
  public ConceptSetDTO mockConceptSet(String code, String label, String description)
  {
    ConceptSetDTO dto = super.mockConceptSet(code, label, description);
    dto.setDiscreteType(DiscreteType.ENUMERATION);

    return dto;
  }

  @Test
  @Request
  public void testSearch()
  {
    List<ConceptObject> results = this.cObjectService.search(testClassification, rootConcept.getCode());

    Assert.assertEquals(1, results.size());

    ConceptObject result = results.get(0);

    Assert.assertEquals(rootConcept.getCode(), result.getCode());

    Assert.assertEquals(1, this.cObjectService.search(testClassification, childConcept.getCode()).size());
  }

  @Test
  @Request
  public void testSearchConceptClass()
  {
    List<ConceptObject> results = this.cObjectService.search(cClass, rootConcept.getCode());

    Assert.assertEquals(1, results.size());

    ConceptObject result = results.get(0);

    Assert.assertEquals(rootConcept.getCode(), result.getCode());

    Assert.assertEquals(1, this.cObjectService.search(cClass, childConcept.getCode()).size());
  }

  @Test
  @Request
  public void testSearchConceptSet()
  {
    List<ConceptObject> results = this.cObjectService.search(cSet, TestDataSet.DEFAULT_OVER_TIME_DATE, rootConcept.getCode());

    Assert.assertEquals(1, results.size());

    ConceptObject result = results.get(0);

    Assert.assertEquals(rootConcept.getCode(), result.getCode());

    Assert.assertEquals(1, this.cObjectService.search(cSet, TestDataSet.DEFAULT_OVER_TIME_DATE, childConcept.getCode()).size());
  }

  @Test
  @Request
  public void testGetChildren()
  {
    List<ConceptObject> results = this.cObjectService.getChildren(rootConcept, testClassification, 20, 1);

    Assert.assertEquals(0, results.size());
  }

  @Test
  @Request
  public void testGetAncestorTree()
  {
    NodeDTO<ObjectOverTimeDTO> node = this.cObjectService.getAncestorTree(testClassification, childConcept, 20);

    Assert.assertEquals(childConcept.getCode(), node.getObject().getCode());
    Assert.assertEquals(0, node.getChildren().getResultSet().size());
  }

  @Test
  @Request
  public void testGetAncestorTreeRoot()
  {
    NodeDTO<ObjectOverTimeDTO> node = this.cObjectService.getAncestorTree(testClassification, rootConcept, 20);

    Assert.assertEquals(rootConcept.getCode(), node.getObject().getCode());
    Assert.assertEquals(0, node.getChildren().getResultSet().size());
  }

  @Test
  @Request
  public void testGetChildrenCount()
  {
    Assert.assertEquals(Integer.valueOf(0), this.cObjectService.getChildCount(rootConcept, testClassification));
  }

  @Test
  public void testGetChildrenPage()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        Page<ObjectOverTimeDTO> page = this.service.getChildren(request.getSessionId(), rootConcept.getCode(), TEST_GOT.getCode(), testClassification.getCode(), 20, 1);

        Assert.assertEquals(Long.valueOf(0), page.getCount());

      });
    }
  }

  @Test
  public void testRequestSearch()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        List<ObjectOverTimeDTO> results = this.service.search(request.getSessionId(), TEST_GOT.getCode(), testClassification.getCode(), childConcept.getCode());

        Assert.assertEquals(1, results.size());

        ObjectOverTimeDTO result = results.get(0);

        Assert.assertEquals(childConcept.getCode(), result.getCode());
      });
    }
  }

  @Test
  public void testRequestSearchClass()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        List<ObjectOverTimeDTO> results = this.service.search(request.getSessionId(), cClass.getCode(), childConcept.getCode());

        Assert.assertEquals(1, results.size());

        ObjectOverTimeDTO result = results.get(0);

        Assert.assertEquals(childConcept.getCode(), result.getCode());
      });
    }
  }

  @Test
  public void testRequestSearchSet()
  {
    TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA };

    for (TestUserInfo user : allowedUsers)
    {
      TestDataSet.runAsUser(user, (request) -> {
        List<ObjectOverTimeDTO> results = this.service.search(request.getSessionId(), cSet.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, childConcept.getCode());

        Assert.assertEquals(1, results.size());

        ObjectOverTimeDTO result = results.get(0);

        Assert.assertEquals(childConcept.getCode(), result.getCode());
      });
    }
  }

}
