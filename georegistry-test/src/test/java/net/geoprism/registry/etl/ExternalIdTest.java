/**
 *
 */
package net.geoprism.registry.etl;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.ExternalId;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class ExternalIdTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GPRGeoObjectBusinessServiceIF objectService;

  @Test
  @Request
  public void testAddExternalId()
  {
    String authority = FastTestDataset.AUTHORITY.getCode();
    String expected = "EXTERNAL ID";

    ServerGeoObjectIF serverGO = FastTestDataset.PROV_CENTRAL.getServerObject();

    this.objectService.applyExternalId(serverGO, authority, expected, ImportStrategy.NEW_ONLY, false);

    try
    {
      Assertions.assertEquals(expected, this.objectService.getExternalId(serverGO, authority));

      Assertions.assertNotNull(this.objectService.getByExternalId(expected, authority, FastTestDataset.PROV_CENTRAL.getGeoObjectType().getServerObject()).get());

      List<ExternalId> ids = this.objectService.getAllExternalIds(serverGO);

      Assertions.assertEquals(1, ids.size());

      ExternalId id = ids.get(0);

      Assertions.assertEquals(expected, id.getExternalId());
      Assertions.assertEquals(authority, id.getParent().getCode());
    }
    finally
    {
      this.objectService.removeExternalId(serverGO, authority, false);
    }

    // Ensure the delete worked
    Assertions.assertTrue(this.objectService.getByExternalId(expected, authority, FastTestDataset.PROV_CENTRAL.getGeoObjectType().getServerObject()).isEmpty());
  }

  @Test
  @Request
  public void testVertexExternalIdRestriction()
  {
    String externalId = "EXTERNAL ID";
    String authority = FastTestDataset.AUTHORITY.getCode();

    ServerGeoObjectIF serverGO = FastTestDataset.PROV_CENTRAL.getServerObject();

    this.objectService.applyExternalId(serverGO, authority, externalId, ImportStrategy.NEW_ONLY, false);

    try
    {
      VertexGeoObjectQuery query = new VertexGeoObjectQuery(FastTestDataset.PROVINCE.getServerObject(), new Date());
      query.setRestriction(new ServerExternalIdRestriction(FastTestDataset.PROVINCE.getServerObject(), FastTestDataset.AUTHORITY.getSourceAuthority(), externalId));

      ServerGeoObjectIF result = query.getSingleResult();

      Assertions.assertNotNull(result);
      Assertions.assertEquals(serverGO.getCode(), result.getCode());
    }
    finally
    {
      this.objectService.removeExternalId(serverGO, authority, false);
    }

  }

}
