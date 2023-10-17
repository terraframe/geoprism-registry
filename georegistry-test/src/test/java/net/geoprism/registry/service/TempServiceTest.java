/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.controller.GeoObjectOverTimeController;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.TestRegistryClient;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class TempServiceTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final TestGeoObjectInfo TEST_GO = new TestGeoObjectInfo("GOSERV_TEST_GO", FastTestDataset.COUNTRY);

  @Autowired
  private TestRegistryClient            client;

  @Autowired
  private RegistryComponentService      service;

  @Autowired
  private GeoObjectOverTimeController   controller;
  
  @Autowired private GeoObjectBusinessServiceIF goService;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    TEST_GO.delete();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
    
    System.out.println();
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testGetGeoObjectOverTimeByCode_Server()
  {
    ServerGeoObjectIF goServer = goService.getGeoObjectByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode(), true);

    GeoObjectOverTime geoObj = goService.toGeoObjectOverTime(goServer);

    Assert.assertEquals(true, geoObj.getExists(TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

  @Test
  public void testGetGeoObjectOverTimeByCode_AutoService()
  {
    GeoObjectOverTime geoObj = service.getGeoObjectOverTimeByCode(testData.clientRequest.getSessionId(), FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

    Assert.assertEquals(true, geoObj.getExists(TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

  @Test
  public void testGetGeoObjectOverTimeByCode_Controller()
  {
    ResponseEntity<String> test = controller.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

    Assert.assertNotNull(test.getBody());
  }

  @Test
  public void testGetGeoObjectOverTimeByCode()
  {
    GeoObjectOverTime geoObj = client.getGeoObjectOverTimeByCode(FastTestDataset.CAMBODIA.getCode(), FastTestDataset.CAMBODIA.getGeoObjectType().getCode());

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObjectOverTime.fromJSON(client.getAdapter(), geoObj.toJSON().toString()).toJSON().toString());
    Assert.assertEquals(true, geoObj.getExists(TestDataSet.DEFAULT_OVER_TIME_DATE));
  }

}
