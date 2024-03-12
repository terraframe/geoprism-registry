/**
 *
 */
package net.geoprism.registry.etl;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.RevealExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ExternalSystemTest extends FastDatasetTest implements InstanceTestClassListener
{
  public static final String            EXTERNAL_SYSTEM_ID = "ExternalSystemTest";

  @Autowired
  private GPRGeoObjectBusinessServiceIF objectService;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    TestDataSet.deleteExternalSystems(EXTERNAL_SYSTEM_ID);

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testCreateRevealExternalSystem()
  {
    RevealExternalSystem system = new RevealExternalSystem();
    system.setId(EXTERNAL_SYSTEM_ID);
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.apply();

    ExternalSystem test = ExternalSystem.get(system.getOid());

    Assert.assertEquals(system.getId(), test.getId());
  }

  @Test
  @Request
  public void testAddExternalId()
  {
    RevealExternalSystem system = new RevealExternalSystem();
    system.setId(EXTERNAL_SYSTEM_ID);
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.apply();

    String expected = "EXTERNAL ID";

    ServerGeoObjectIF serverGO = FastTestDataset.PROV_CENTRAL.getServerObject();

    this.objectService.createExternalId(serverGO, system, expected, ImportStrategy.NEW_ONLY);

    String actual = this.objectService.getExternalId(serverGO, system);

    Assert.assertEquals(expected, actual);
  }

  @Test
  @Request
  public void testVertexExternalIdRestriction()
  {
    RevealExternalSystem system = new RevealExternalSystem();
    system.setId(EXTERNAL_SYSTEM_ID);
    system.setOrganization(FastTestDataset.ORG_CGOV.getServerObject());
    system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
    system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
    system.apply();

    String externalId = "EXTERNAL ID";

    ServerGeoObjectIF serverGO = FastTestDataset.PROV_CENTRAL.getServerObject();

    this.objectService.createExternalId(serverGO, system, externalId, ImportStrategy.NEW_ONLY);

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(FastTestDataset.PROVINCE.getServerObject(), new Date());
    query.setRestriction(new ServerExternalIdRestriction(FastTestDataset.PROVINCE.getServerObject(), system, externalId));

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertNotNull(result);
    Assert.assertEquals(serverGO.getCode(), result.getCode());
  }

}
