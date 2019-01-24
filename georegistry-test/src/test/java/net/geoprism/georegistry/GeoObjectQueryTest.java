package net.geoprism.georegistry;

import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.georegistry.testframework.USATestData;
import net.geoprism.registry.GeoObjectStatus;

public class GeoObjectQueryTest
{
  protected USATestData     tutil;

  protected ClientRequestIF adminCR;

  @Before
  public void setUp()
  {
    this.tutil = USATestData.newTestData();

    this.adminCR = tutil.adminClientRequest;
  }

  @After
  public void tearDown()
  {
    if (this.tutil != null)
    {
      tutil.cleanUp();
    }
  }

  @Test
  @Request
  public void testQueryTreeNodes()
  {
    GeoObjectType type = this.tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = this.tutil.STATE.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);

    OIterator<GeoObject> it = query.getIterator();

    try
    {
      List<GeoObject> results = it.getAll();

      Assert.assertEquals(2, results.size());

      GeoObject result = results.get(0);

      Assert.assertEquals(this.tutil.COLORADO.getGeoId(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(this.tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

      Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
      Assert.assertEquals(expected, result.getStatus());
    }
    finally
    {
      it.close();
    }

  }

  @Test
  @Request
  public void testQueryLeafNodes()
  {
    GeoObjectType type = this.tutil.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = this.tutil.DISTRICT.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);

    OIterator<GeoObject> it = query.getIterator();

    try
    {
      List<GeoObject> results = it.getAll();

      Assert.assertEquals(5, results.size());

      GeoObject result = results.get(0);

      Assert.assertEquals(this.tutil.CO_D_ONE.getGeoId(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(this.tutil.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

      Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
      Assert.assertEquals(expected, result.getStatus());
    }
    finally
    {
      it.close();
    }

  }
}
