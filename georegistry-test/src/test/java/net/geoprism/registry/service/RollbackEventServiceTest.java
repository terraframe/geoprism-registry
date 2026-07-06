/**
 *
 */
package net.geoprism.registry.service;

import java.util.SortedSet;

import org.axonframework.eventhandling.GapAwareTrackingToken;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.EventDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.RollbackCheckpoint;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.axon.config.RegistryEventStore;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.model.ServerChildGraphNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.service.business.RollbackEventService;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class RollbackEventServiceTest extends EventDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private RollbackEventService service;

  @Autowired
  private RegistryEventStore   store;

  @Override
  public void setUp()
  {
    // Do not create any data
  }

  @After
  @Request
  public void tearDown()
  {
    if (pObject != null)
    {
      this.bObjectService.delete(pObject);

      pObject = null;
    }

    if (cObject != null)
    {
      this.bObjectService.delete(cObject);

      cObject = null;
    }

    this.store.truncate();

    this.projection.clearCache();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testRollbackCreateGeoObject()
  {
    USATestData.USA.apply();

    Assert.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode(), false));
    Assert.assertEquals(Long.valueOf(1), this.store.size());

    GapAwareTrackingToken start = (GapAwareTrackingToken) this.store.createTailToken();

    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(start.getIndex());

    this.service.rollback(dto);

    Assert.assertNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode(), false));

    Assert.assertEquals(Long.valueOf(0), this.store.size());
  }

  @Test
  @Request
  public void testRollbackUpdatedGeoObject()
  {
    ServerGeoObjectIF object = USATestData.USA.apply();
    object.setDisplayLabel(new LocalizedValue("Updated Label"), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    long startIndex = this.store.createHeadToken().position().getAsLong();

    Assert.assertEquals(Long.valueOf(1), this.store.size());
    this.applyGeoObject(object);

    Assert.assertNotNull(this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode(), false));
    Assert.assertEquals(Long.valueOf(2), this.store.size());

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    ServerGeoObjectIF test = this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode(), false);

    Assert.assertNotNull(test);
    Assert.assertEquals(USATestData.USA.getDisplayLabel(), test.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getValue());

    Assert.assertEquals(Long.valueOf(1), this.store.size());
  }

  @Test
  @Request
  public void testRollbackCreateBusinessObject()
  {
    // Index before the import
    pObject = createBusinessObject("P_CODE");

    Assert.assertNotNull(this.bObjectService.getByCode(btype, pObject.getCode()));
    Assert.assertEquals(Long.valueOf(1), this.store.size());

    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(this.store.createTailToken().position().getAsLong());

    this.service.rollback(dto);

    Assert.assertNull(this.bObjectService.getByCode(btype, pObject.getCode()));

    Assert.assertEquals(Long.valueOf(0), this.store.size());
  }

  @Test
  @Request
  public void testRollbackUpdatedBusinessObject()
  {
    pObject = createBusinessObject("P_CODE");

    long startIndex = this.store.createHeadToken().position().getAsLong();

    Assert.assertEquals(Long.valueOf(1), this.store.size());

    pObject.setValue("testBoolean", true);

    applyBusinessObject(pObject, false);

    Assert.assertEquals(Long.valueOf(2), this.store.size());

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    BusinessObject test = this.bObjectService.getByCode(btype, pObject.getCode());

    Assert.assertNotNull(test);
    Assert.assertEquals(false, test.getValue("testBoolean"));

    Assert.assertEquals(Long.valueOf(1), this.store.size());
  }

  @Test
  @Request
  public void testRollbackCreateConceptObject()
  {
    // Index before the import
    concept = createConceptObject("CONCEPT");
    
    Assert.assertNotNull(this.cObjectService.getByCode(cClass, concept.getCode()));
    Assert.assertEquals(Long.valueOf(1), this.store.size());
    
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(this.store.createTailToken().position().getAsLong());
    
    this.service.rollback(dto);
    
    Assert.assertNull(this.cObjectService.getByCode(cClass, concept.getCode()));
    
    Assert.assertEquals(Long.valueOf(0), this.store.size());
  }
  
  @Test
  @Request
  public void testRollbackUpdatedConceptObject()
  {
    concept = createConceptObject("CONCEPT");
    
    long startIndex = this.store.createHeadToken().position().getAsLong();
    
    Assert.assertEquals(Long.valueOf(1), this.store.size());
    
    concept.setValue("testBoolean", true);
    
    applyConceptObject(concept, false);
    
    Assert.assertEquals(Long.valueOf(2), this.store.size());
    
    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);
    
    this.service.rollback(dto);
    
    ConceptObject test = this.cObjectService.getByCode(cClass, concept.getCode());
    
    Assert.assertNotNull(test);
    Assert.assertEquals(false, test.getValue("testBoolean"));
    
    Assert.assertEquals(Long.valueOf(1), this.store.size());
  }
  
  @Test
  @Request
  public void testRollbackCreateParentEvent()
  {
    USATestData.USA.apply();

    ServerGeoObjectIF child = USATestData.COLORADO.apply();

    long startIndex = this.store.createHeadToken().position().getAsLong();

    USATestData.USA.addChild(USATestData.COLORADO, USATestData.HIER_ADMIN);

    Assert.assertEquals(Long.valueOf(3), this.store.size());

    Assert.assertEquals(1, child.getEdges(USATestData.HIER_ADMIN.getServerObject()).size());

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    ServerGeoObjectIF test = this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode(), false);

    Assert.assertNotNull(test);

    Assert.assertEquals(0, test.getEdges(USATestData.HIER_ADMIN.getServerObject()).size());

    Assert.assertEquals(Long.valueOf(2), this.store.size());
  }

  @Test
  @Request
  public void testRollbackRemoveParentEvent()
  {
    USATestData.USA.apply();

    ServerGeoObjectIF child = USATestData.COLORADO.apply();

    String edgeId = USATestData.USA.addChild(USATestData.COLORADO, USATestData.HIER_ADMIN);

    long startIndex = this.store.createHeadToken().position().getAsLong();

    USATestData.USA.removeChild(USATestData.COLORADO, USATestData.HIER_ADMIN, edgeId);

    Assert.assertEquals(Long.valueOf(4), this.store.size());

    Assert.assertEquals(0, child.getEdges(USATestData.HIER_ADMIN.getServerObject()).size());

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    ServerGeoObjectIF test = this.gObjectService.getGeoObjectByCode(USATestData.COLORADO.getCode(), USATestData.COLORADO.getGeoObjectType().getCode(), false);

    Assert.assertNotNull(test);

    Assert.assertEquals(1, test.getEdges(USATestData.HIER_ADMIN.getServerObject()).size());

    Assert.assertEquals(Long.valueOf(3), this.store.size());
  }

  @Test
  @Request
  public void testRollbackUpdateParentEvent()
  {
    USATestData.WASHINGTON.apply();
    ServerGeoObjectIF parent = USATestData.COLORADO.apply();

    USATestData.CO_A_ONE.apply();

    String edgeId = USATestData.COLORADO.addChild(USATestData.CO_A_ONE, USATestData.HIER_ADMIN);

    long startIndex = this.store.createHeadToken().position().getAsLong();

    USATestData.CO_A_ONE.updateParent(USATestData.WASHINGTON, USATestData.HIER_ADMIN, edgeId, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    Assert.assertEquals(Long.valueOf(5), this.store.size());

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    ServerGeoObjectIF test = this.gObjectService.getGeoObjectByCode(USATestData.CO_A_ONE.getCode(), USATestData.CO_A_ONE.getGeoObjectType().getCode(), false);

    Assert.assertNotNull(test);

    SortedSet<EdgeObject> edges = test.getEdges(USATestData.HIER_ADMIN.getServerObject());

    Assert.assertEquals(1, edges.size());

    Assert.assertEquals(parent.getRunwayId(), edges.first().getParent().getOid());

    Assert.assertEquals(Long.valueOf(4), this.store.size());
  }

  @Test
  @Request
  public void testRollbackCreateEdgeEvent()
  {
    USATestData.USA.apply();

    ServerGeoObjectIF source = USATestData.COLORADO.apply();

    long startIndex = this.store.createHeadToken().position().getAsLong();

    this.addDirectedAcyclicEdge(USATestData.COLORADO, USATestData.USA);

    Assert.assertEquals(Long.valueOf(3), this.store.size());

    ServerChildGraphNode node = source.getGraphChildren(dagType, false, USATestData.DEFAULT_OVER_TIME_DATE);

    Assert.assertEquals(1, node.getChildren().size());

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    ServerGeoObjectIF test = this.gObjectService.getGeoObjectByCode(USATestData.USA.getCode(), USATestData.USA.getGeoObjectType().getCode(), false);

    Assert.assertNotNull(test);

    Assert.assertEquals(0, test.getEdges(USATestData.HIER_ADMIN.getServerObject()).size());

    Assert.assertEquals(Long.valueOf(2), this.store.size());
  }

  @Test
  @Request
  public void testRollbackBusinessObjectCreateEdgeEvent()
  {
    pObject = createBusinessObject("P_CODE");
    cObject = createBusinessObject("C_CODE");
    USATestData.COLORADO.apply();

    long startIndex = this.store.createHeadToken().position().getAsLong();

    this.addBusinessEdge();

    Assert.assertEquals(Long.valueOf(5), this.store.size());

    Assert.assertEquals(1, this.getEdgeCount(bEdgeType, pObject.getVertex().getRID(), cObject.getVertex().getRID()));
    Assert.assertEquals(1, this.getEdgeCount(bGeoEdgeType, USATestData.COLORADO.getServerObject().getVertex().getRID(), cObject.getVertex().getRID()));

    // Rollback the last event
    RollbackCheckpoint dto = new RollbackCheckpoint();
    dto.setGlobalIndex(startIndex);

    this.service.rollback(dto);

    Assert.assertEquals(0, this.getEdgeCount(bEdgeType, pObject.getVertex().getRID(), cObject.getVertex().getRID()));
    Assert.assertEquals(0, this.getEdgeCount(bGeoEdgeType, USATestData.COLORADO.getServerObject().getVertex().getRID(), cObject.getVertex().getRID()));

    Assert.assertEquals(Long.valueOf(3), this.store.size());
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testLock()
  {
    try
    {
      this.store.setLock(true);

      pObject = createBusinessObject("P_CODE");
    }
    finally
    {
      this.store.setLock(false);
    }

  }

  public long getEdgeCount(BusinessEdgeType type, Object outRid, Object inRid)
  {
    MdEdge mdEdge = type.getMdEdge();

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT COUNT(*) FROM " + mdEdge.getDbClassName());

    builder.append(" WHERE out = :outRid");
    builder.append(" AND in = :inRid");

    final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());

    query.setParameter("outRid", outRid);
    query.setParameter("inRid", inRid);

    return query.getSingleResult();
  }

}
