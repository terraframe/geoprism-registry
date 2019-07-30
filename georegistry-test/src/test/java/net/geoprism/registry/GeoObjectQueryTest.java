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

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Synonym;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.io.SynonymRestriction;
import net.geoprism.registry.query.CodeRestriction;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.query.LookupRestriction;
import net.geoprism.registry.query.OidRestrction;
import net.geoprism.registry.query.UidRestriction;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.test.USATestData;

public class GeoObjectQueryTest
{
  protected static USATestData     tutil;

  protected static ClientRequestIF adminCR;

  @BeforeClass
  public static void setUp()
  {
    tutil = USATestData.newTestData();

    adminCR = tutil.adminClientRequest;
  }

  @AfterClass
  public static void tearDown()
  {
    if (tutil != null)
    {
      tutil.cleanUp();
    }
  } 

  @Test
  @Request
  public void testQueryTreeNodes()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);

    OIterator<GeoObject> it = query.getIterator();

    try
    {
      List<GeoObject> results = it.getAll();

      Assert.assertEquals(2, results.size());

      GeoObject result = results.get(0);

      Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

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
    GeoObjectType type = tutil.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.DISTRICT.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);

    OIterator<GeoObject> it = query.getIterator();

    try
    {
      List<GeoObject> results = it.getAll();

      Assert.assertEquals(5, results.size());

      GeoObject result = results.get(0);

      Assert.assertEquals(tutil.CO_D_ONE.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(tutil.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

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
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new UidRestriction(tutil.COLORADO.getRegistryId()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLeafUidRestriction()
  {
    GeoObjectType type = tutil.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.DISTRICT.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new UidRestriction(tutil.CO_D_ONE.getRegistryId()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeOidRestriction()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    String oid = ServiceFactory.getIdService().registryIdToRunwayId(tutil.COLORADO.getRegistryId(), type);

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new OidRestrction(oid));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLeafOidRestriction()
  {
    GeoObjectType type = tutil.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.DISTRICT.getUniversal();

    String oid = ServiceFactory.getIdService().registryIdToRunwayId(tutil.CO_D_ONE.getRegistryId(), type);

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new OidRestrction(oid));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeCodeRestriction()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new CodeRestriction(tutil.COLORADO.getCode()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLeafCodeRestriction()
  {
    GeoObjectType type = tutil.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.DISTRICT.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new CodeRestriction(tutil.CO_D_ONE.getCode()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

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

    Synonym.create(synonym, tutil.COLORADO.getGeoEntity().getOid());

    try
    {
      GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
      Universal universal = tutil.STATE.getUniversal();

      GeoObjectQuery query = new GeoObjectQuery(type, universal);
      query.setRestriction(new SynonymRestriction(label));

      GeoObject result = query.getSingleResult();

      Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
      Assert.assertNotNull(result.getGeometry());
      Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

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
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new SynonymRestriction(tutil.COLORADO.getDisplayLabel()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByCode()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new SynonymRestriction(tutil.COLORADO.getCode()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByCodeWithParent()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    MdTermRelationship mdRelationship = MdTermRelationship.getByKey(LocatedIn.CLASS);
    SynonymRestriction restriction = new SynonymRestriction(tutil.COLORADO.getCode(), tutil.USA.asGeoObject(), mdRelationship);

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testTreeSynonymRestrictionByCodeWithAncestor()
  {
    GeoObjectType type = tutil.AREA.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.AREA.getUniversal();

    MdTermRelationship mdRelationship = MdTermRelationship.getByKey(LocatedIn.CLASS);
    SynonymRestriction restriction = new SynonymRestriction(tutil.CO_A_ONE.getCode(), tutil.COLORADO.asGeoObject(), mdRelationship);

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.CO_A_ONE.getCode(), result.getCode());
    Assert.assertEquals(tutil.CO_A_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());
  }

  @Test
  @Request
  public void testFailTreeSynonymRestrictionByCodeWithParent()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    MdTermRelationship mdRelationship = MdTermRelationship.getByKey(LocatedIn.CLASS);
    SynonymRestriction restriction = new SynonymRestriction(tutil.COLORADO.getCode(), tutil.CANADA.asGeoObject(), mdRelationship);

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertNull(result);
  }

  @Test
  @Request
  public void testLeafSynonymRestriction()
  {
    GeoObjectType type = tutil.DISTRICT.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.DISTRICT.getUniversal();
    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(new SynonymRestriction(tutil.CO_D_ONE.getDisplayLabel()));

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.CO_D_ONE.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.CO_D_ONE.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testLookupRestriction()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    LookupRestriction restriction = new LookupRestriction("Co", tutil.USA.getCode(), LocatedIn.class.getSimpleName());

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertEquals(tutil.COLORADO.getCode(), result.getCode());
    Assert.assertNotNull(result.getGeometry());
    Assert.assertEquals(tutil.COLORADO.getDisplayLabel(), result.getLocalizedDisplayLabel());

    Term expected = ServiceFactory.getConversionService().geoObjectStatusToTerm(GeoObjectStatus.ACTIVE);
    Assert.assertEquals(expected, result.getStatus());
  }

  @Test
  @Request
  public void testFailLookupRestriction()
  {
    GeoObjectType type = tutil.STATE.getGeoObjectType(GeometryType.POLYGON);
    Universal universal = tutil.STATE.getUniversal();

    LookupRestriction restriction = new LookupRestriction("Co", tutil.CANADA.getCode(), LocatedIn.class.getSimpleName());

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    query.setRestriction(restriction);

    GeoObject result = query.getSingleResult();

    Assert.assertNull(result);
  }

}
