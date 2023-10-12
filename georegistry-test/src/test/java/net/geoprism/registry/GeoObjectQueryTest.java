/**
 *
 */
package net.geoprism.registry;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.GeoVertexSynonym;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerLookupRestriction;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectQueryTest implements InstanceTestClassListener
{
  protected USATestData testData;

  @Override
  public void beforeClassSetup()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();
  }

  @Override
  public void afterClassSetup()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
  }

  @After
  public void tearDown()
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
  }

  @Test
  @Request
  public void testQueryTreeNodes()
  {
    ServerGeoObjectType type = USATestData.STATE.getServerObject();
    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);

    List<ServerGeoObjectIF> results = query.getResults();

    Assert.assertEquals(2, results.size());

    ServerGeoObjectIF result = results.get(0);

    Assert.assertEquals(USATestData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel().getValue());

    Assert.assertEquals(true, result.getExists());
  }

  @Test
  @Request
  public void testQueryLeafNodes()
  {
    ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();
    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);

    List<ServerGeoObjectIF> results = query.getResults();

    String[] expectedCodes = new String[] { USATestData.CO_D_ONE.getCode(), USATestData.CO_D_TWO.getCode(), USATestData.CO_D_THREE.getCode(), USATestData.WA_D_ONE.getCode(), USATestData.WA_D_TWO.getCode() };
    Assert.assertEquals(expectedCodes.length, results.size());

    for (ServerGeoObjectIF result : results)
    {
      Assert.assertTrue(ArrayUtils.contains(expectedCodes, result.getCode()));
      Assert.assertNotNull(result.getGeometry());
      Assert.assertTrue(result.getDisplayLabel().getValue().length() > 0);
    }
  }

  @Test
  @Request
  public void testTreeCodeRestriction()
  {
    ServerGeoObjectType type = USATestData.STATE.getServerObject();

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
    query.setRestriction(new ServerCodeRestriction(USATestData.COLORADO.getCode()));

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertEquals(USATestData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel().getValue());

    Assert.assertEquals(true, result.getExists());
  }

  @Test
  @Request
  public void testLeafCodeRestriction()
  {
    ServerGeoObjectType type = USATestData.DISTRICT.getServerObject();
    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
    query.setRestriction(new ServerCodeRestriction(USATestData.CO_D_ONE.getCode()));

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertEquals(USATestData.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(USATestData.CO_D_ONE.getDisplayLabel(), result.getDisplayLabel().getValue());

    Assert.assertEquals(true, result.getExists());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionBySynonym()
  {
    String label = "TEST";

    JSONObject jSynonym = GeoVertexSynonym.createSynonym(USATestData.STATE.getCode(), USATestData.COLORADO.getCode(), label);
    String vOid = jSynonym.getString("vOid");

    try
    {
      ServerGeoObjectType type = USATestData.STATE.getServerObject();

      VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
      query.setRestriction(new ServerSynonymRestriction(label, USATestData.COLORADO.getDate()));

      ServerGeoObjectIF result = query.getSingleResult();

      Assert.assertEquals(USATestData.COLORADO.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel().getValue());

      Assert.assertEquals(true, result.getExists());
    }
    finally
    {
      GeoVertexSynonym.deleteSynonym(vOid);
    }
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByDisplayLabel()
  {
    ServerGeoObjectType type = USATestData.STATE.getServerObject();

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
    query.setRestriction(new ServerSynonymRestriction(USATestData.COLORADO.getDisplayLabel(), USATestData.COLORADO.getDate()));

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertEquals(USATestData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel().getValue());

    Assert.assertEquals(true, result.getExists());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByCode()
  {
    ServerGeoObjectType type = USATestData.STATE.getServerObject();

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
    query.setRestriction(new ServerSynonymRestriction(USATestData.COLORADO.getCode(), USATestData.COLORADO.getDate()));

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertEquals(USATestData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel().getValue());
    Assert.assertEquals(true, result.getExists());
  }

  @Test
  @Request
  public void testLookupRestriction()
  {
    ServerGeoObjectType type = USATestData.STATE.getServerObject();

    ServerLookupRestriction restriction = new ServerLookupRestriction("Co", USATestData.COLORADO.getDate(), USATestData.USA.getCode(), USATestData.HIER_ADMIN.getServerObject());

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
    query.setRestriction(restriction);

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertEquals(USATestData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(USATestData.COLORADO.getDisplayLabel(), result.getDisplayLabel().getValue());

    Assert.assertEquals(true, result.getExists());
  }

  @Test
  @Request
  public void testFailLookupRestriction()
  {
    ServerGeoObjectType type = USATestData.STATE.getServerObject();

    ServerLookupRestriction restriction = new ServerLookupRestriction(USATestData.CANADA.getCode(), USATestData.CANADA.getDate());

    VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
    query.setRestriction(restriction);

    ServerGeoObjectIF result = query.getSingleResult();

    Assert.assertNull(result);
  }

}
