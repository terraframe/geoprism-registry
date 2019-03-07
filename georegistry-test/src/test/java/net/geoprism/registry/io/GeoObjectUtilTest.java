package net.geoprism.registry.io;

import java.io.IOException;
import java.util.Map;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;

import junit.framework.Assert;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.USATestData;

public class GeoObjectUtilTest
{
  private static USATestData     testData;

  private static ClientRequestIF adminCR;

  @BeforeClass
  public static void setUp()
  {
    testData = USATestData.newTestData(GeometryType.POLYGON, true);

    adminCR = testData.adminClientRequest;

    reload();
  }

  @Request
  public static void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(adminCR.getSessionId()).reloadPermissions();
  }

  @AfterClass
  public static void tearDown() throws IOException
  {
    testData.cleanUp();
  }

  @Test
  @Request
  public void testGetAncestorMapForTreeType()
  {
    GeoObjectType type = testData.AREA.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchy = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_A_ONE.getCode(), type.getCode());

    Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchy);

    Assert.assertEquals(5, map.size());

    // Validate the county values
    Assert.assertTrue(map.containsKey(testData.COUNTY.getCode()));

    ValueObject vObject = map.get(testData.COUNTY.getCode());

    Assert.assertEquals(testData.CO_C_ONE.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.CO_C_ONE.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));

    // Validate the state values
    Assert.assertTrue(map.containsKey(testData.STATE.getCode()));

    vObject = map.get(testData.STATE.getCode());

    Assert.assertEquals(testData.COLORADO.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));

    // Validate the country values
    Assert.assertTrue(map.containsKey(testData.COUNTRY.getCode()));

    vObject = map.get(testData.COUNTRY.getCode());

    Assert.assertEquals(testData.USA.getCode(), vObject.getValue(GeoEntity.GEOID));
    Assert.assertEquals(testData.USA.getDisplayLabel(), vObject.getValue(GeoEntity.DISPLAYLABEL));
  }

  @Request
  @Test(expected = UnsupportedOperationException.class)
  public void testGetAncestorMapForTreeLeaf()
  {
    GeoObjectType type = testData.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchy = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObject object = ServiceFactory.getUtilities().getGeoObjectByCode(testData.CO_D_ONE.getCode(), type.getCode());

    GeoObjectUtil.getAncestorMap(object, hierarchy);
  }
}
