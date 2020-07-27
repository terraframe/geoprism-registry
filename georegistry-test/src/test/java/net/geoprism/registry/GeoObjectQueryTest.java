/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.util.List;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.postgres.CodeRestriction;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.query.postgres.LookupRestriction;
import net.geoprism.registry.query.postgres.OidRestrction;
import net.geoprism.registry.query.postgres.SynonymRestriction;
import net.geoprism.registry.query.postgres.UidRestriction;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.USATestData;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Synonym;

public class GeoObjectQueryTest
{
  protected static USATestData     testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();
  }
  
  @AfterClass
  public static void cleanUpClass()
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
    ServerGeoObjectType type = testData.STATE.getServerObject();
    GeoObjectQuery query = new GeoObjectQuery(type);

    OIterator<GeoObject> it = query.getIterator();

    try
    {
      List<GeoObject> results = it.getAll();

      Assert.assertEquals(2, results.size());

      GeoObject result = results.get(0);

      Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

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
    ServerGeoObjectType type = testData.DISTRICT.getServerObject();
    GeoObjectQuery query = new GeoObjectQuery(type);

    OIterator<GeoObject> it = query.getIterator();

    try
    {
      List<GeoObject> results = it.getAll();

      Assert.assertEquals(5, results.size());

      GeoObject result = results.get(0);

      Assert.assertEquals(testData.CO_D_ONE.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(testData.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

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
  public void testTreeUidRestriction()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new UidRestriction(testData.COLORADO.getRegistryId()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLeafUidRestriction()
  {
    ServerGeoObjectType type = testData.DISTRICT.getServerObject();
    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new UidRestriction(testData.CO_D_ONE.getRegistryId()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeOidRestriction()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    String oid = ServiceFactory.getIdService().registryIdToRunwayId(testData.COLORADO.getRegistryId(), type.getType());

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new OidRestrction(oid));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLeafOidRestriction()
  {
    ServerGeoObjectType type = testData.DISTRICT.getServerObject();

    String oid = ServiceFactory.getIdService().registryIdToRunwayId(testData.CO_D_ONE.getRegistryId(), type.getType());

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new OidRestrction(oid));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeCodeRestriction()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new CodeRestriction(testData.COLORADO.getCode()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLeafCodeRestriction()
  {
    ServerGeoObjectType type = testData.DISTRICT.getServerObject();
    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new CodeRestriction(testData.CO_D_ONE.getCode()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionBySynonym()
  {
    String label = "TEST";

    Synonym synonym = new Synonym();
    synonym.getDisplayLabel().setValue(label);

    Synonym.create(synonym, testData.COLORADO.getGeoEntity().getOid());

    try
    {
      ServerGeoObjectType type = testData.STATE.getServerObject();

      GeoObjectQuery query = new GeoObjectQuery(type);
      query.setRestriction(new SynonymRestriction(label));

      GeoObject result = query.getSingleResult();

      Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

      Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
      Assert.assertEquals(expected, result.getStatus());
    }
    finally
    {
      synonym.delete();
    }
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByDisplayLabel()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new SynonymRestriction(testData.COLORADO.getDisplayLabel()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByCode()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new SynonymRestriction(testData.COLORADO.getCode()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

//  @Test
//  @Request
//  public void testTreeSynonymRestrictionByCodeWithParent()
//  {
//    ServerGeoObjectType type = testData.STATE.getServerObject(GeometryType.POLYGON);
//
//    MdTermRelationship mdRelationship = MdTermRelationship.getByKey(LocatedIn.CLASS);
//    SynonymRestriction restriction = new SynonymRestriction(testData.COLORADO.getCode(), testData.USA.asGeoObject(), mdRelationship);
//
//    GeoObjectQuery query = new GeoObjectQuery(type);
//    query.setRestriction(restriction);
//
//    GeoObject result = query.getSingleResult();
//
//    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
//    Assert.assertNotNull(result.getGeometry());
//    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());
//
//    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
//    Assert.assertEquals(expected, result.getStatus());
//  }
//
//  @Test
//  @Request
//  public void testTreeSynonymRestrictionByCodeWithAncestor()
//  {
//    ServerGeoObjectType type = testData.AREA.getServerObject(GeometryType.POLYGON);
//
//    MdTermRelationship mdRelationship = MdTermRelationship.getByKey(LocatedIn.CLASS);
//    SynonymRestriction restriction = new SynonymRestriction(testData.CO_A_ONE.getCode(), testData.COLORADO.asGeoObject(), mdRelationship);
//
//    GeoObjectQuery query = new GeoObjectQuery(type);
//    query.setRestriction(restriction);
//
//    GeoObject result = query.getSingleResult();
//
//    Assert.assertEquals(testData.CO_A_ONE.getCode(), result.getCode());
//    Assert.assertEquals(testData.CO_A_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());
//  }
//
//  @Test
//  @Request
//  public void testFailTreeSynonymRestrictionByCodeWithParent()
//  {
//    ServerGeoObjectType type = testData.STATE.getServerObject(GeometryType.POLYGON);
//
//    MdTermRelationship mdRelationship = MdTermRelationship.getByKey(LocatedIn.CLASS);
//    SynonymRestriction restriction = new SynonymRestriction(testData.COLORADO.getCode(), testData.CANADA.asGeoObject(), mdRelationship);
//
//    GeoObjectQuery query = new GeoObjectQuery(type);
//    query.setRestriction(restriction);
//
//    GeoObject result = query.getSingleResult();
//
//    Assert.assertNull(result);
//  }

  @Test
  @Request
  public void testLeafSynonymRestriction()
  {
    ServerGeoObjectType type = testData.DISTRICT.getServerObject();
    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(new SynonymRestriction(testData.CO_D_ONE.getDisplayLabel()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLookupRestriction()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    LookupRestriction restriction = new LookupRestriction("Co", testData.USA.getCode(), LocatedIn.class.getSimpleName());

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(testData.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(testData.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testFailLookupRestriction()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();

    LookupRestriction restriction = new LookupRestriction("Co", testData.CANADA.getCode(), LocatedIn.class.getSimpleName());

    GeoObjectQuery query = new GeoObjectQuery(type);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertNull(result);
  }

}
