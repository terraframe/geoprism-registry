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
package net.geoprism.registry.test;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.junit.Assert;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.model.postgres.TreeServerGeoObject;
import net.geoprism.registry.service.ServerGeoObjectService;

public class TestGeoObjectInfo
{
  private final TestDataSet       testDataSet;

  private String                  code;

  private String                  displayLabel;

  private String                  wkt;

  private String                  registryId = null;

  private String                  oid;

  private GeoEntity               geoEntity;

  private Business                business;

  private TestGeoObjectTypeInfo   geoObjectType;

  private List<TestGeoObjectInfo> children;

  private List<TestGeoObjectInfo> parents;

  private ServerGeoObjectIF       serverGO;

  private String                  statusCode;

  private Boolean                 isNew;

  protected TestGeoObjectInfo(TestDataSet testDataSet, String genKey, TestGeoObjectTypeInfo testUni, String wkt, String statusCode, Boolean isNew)
  {
    this.testDataSet = testDataSet;
    initialize(genKey, testUni, statusCode, isNew);
    this.wkt = wkt;
  }

  protected TestGeoObjectInfo(TestDataSet testDataSet, String genKey, TestGeoObjectTypeInfo testUni)
  {
    this.testDataSet = testDataSet;
    initialize(genKey, testUni, DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, true);
  }

  private void initialize(String genKey, TestGeoObjectTypeInfo testUni, String statusCode, Boolean isNew)
  {
    this.code = this.testDataSet.getTestDataKey() + genKey;
    this.displayLabel = this.testDataSet.getTestDataKey() + " " + genKey;
    this.geoObjectType = testUni;
    this.children = new LinkedList<TestGeoObjectInfo>();
    this.parents = new LinkedList<TestGeoObjectInfo>();
    this.statusCode = statusCode;
    this.isNew = isNew;

    GeometryType geom = this.getGeoObjectType().getGeometryType();
    if (geom == GeometryType.POLYGON)
    {
      this.wkt = TestDataSet.WKT_DEFAULT_POLYGON;
    }
    else if (geom == GeometryType.POINT)
    {
      this.wkt = TestDataSet.WKT_DEFAULT_POINT;
    }
    else if (geom == GeometryType.MULTIPOLYGON)
    {
      this.wkt = TestDataSet.WKT_DEFAULT_MULTIPOLYGON;
    }
    else
    {
      throw new UnsupportedOperationException("Add a default geometry if you want to use this geometry type.");
    }
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public void setDisplayLabel(String displayLabel)
  {
    this.displayLabel = displayLabel;
  }

  public void setChildren(List<TestGeoObjectInfo> children)
  {
    this.children = children;
  }

  public void setParents(List<TestGeoObjectInfo> parents)
  {
    this.parents = parents;
  }

  public String getCode()
  {
    return code;
  }

  public String getDisplayLabel()
  {
    return displayLabel;
  }

  public String getWkt()
  {
    return wkt;
  }

  public void setWkt(String wkt)
  {
    this.wkt = wkt;
  }

  public String getRegistryId()
  {
    return registryId;
  }

  public void setRegistryId(String uid)
  {
    this.registryId = uid;
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  /**
   * Returns the GeoEntity that implements this test GeoObject. Will return null
   * if the test object has not been applied.
   */
  public GeoEntity getGeoEntity()
  {
    return this.geoEntity;
  }

  /**
   * Returns the business object that implements this test GeoObject and
   * contains the additional CGR attributes like Status. Will return null if the
   * test object has not been applied.
   */
  public Business getBusiness()
  {
    return this.business;
  }

  private Geometry getGeometry()
  {
    try
    {
      final WKTReader reader = new WKTReader(new GeometryFactory());
      return reader.read(this.getWkt());
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  /**
   * Constructs a new GeoObject and populates all attributes from the data
   * contained within this test wrapper.
   */
  public GeoObject asGeoObject()
  {
    GeoObject geoObj = this.testDataSet.adapter.newGeoObjectInstance(this.geoObjectType.getCode());

    geoObj.setWKTGeometry(this.getWkt());
    geoObj.setCode(this.getCode());
    geoObj.getDisplayLabel().setValue(this.getDisplayLabel());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, this.getDisplayLabel());
    geoObj.setStatus(this.statusCode);

    if (registryId != null)
    {
      geoObj.setUid(registryId);
    }

    return geoObj;
  }

  /**
   * Constructs a new GeoObject and populates all attributes from the data
   * contained within this test wrapper.
   */
  public GeoObjectOverTime asGeoObject(Date date)
  {
    GeoObjectOverTime geoObj = this.testDataSet.adapter.newGeoObjectOverTimeInstance(this.geoObjectType.getCode());

    final LocalizedValue label = new LocalizedValue(this.getDisplayLabel());
    label.setValue(LocalizedValue.DEFAULT_LOCALE, this.getDisplayLabel());

    final Geometry geometry = this.getGeometry();

    geoObj.setGeometry(geometry, date, ValueOverTime.INFINITY_END_DATE);
    geoObj.setCode(this.getCode());
    geoObj.setDisplayLabel(label, date, ValueOverTime.INFINITY_END_DATE);
    geoObj.setStatus(this.statusCode, date, ValueOverTime.INFINITY_END_DATE);

    if (registryId != null)
    {
      geoObj.setUid(registryId);
    }

    return geoObj;
  }

  /**
   * Returns all test GeoObject children that this test dataset is aware of. May
   * be out of sync with the database if operations were performed outside of
   * the test framework.
   */
  public List<TestGeoObjectInfo> getChildren()
  {
    return this.children;
  }

  /**
   * Returns all test GeoObject parents that this test dataset is aware of. May
   * be out of sync with the database if operations were performed outside of
   * the test framework.
   */
  public List<TestGeoObjectInfo> getParents()
  {
    return this.parents;
  }

  public void assertEquals(ChildTreeNode tn, String[] childrenTypes, boolean recursive)
  {
    this.assertEquals(tn.getGeoObject());
    // TODO : HierarchyType?

    List<ChildTreeNode> tnChildren = tn.getChildren();

    // Check array size
    int numChildren = 0;
    for (TestGeoObjectInfo testChild : this.children)
    {
      if (childrenTypes == null || ArrayUtils.contains(childrenTypes, testChild.getGeoObjectType().getCode()))
      {
        numChildren++;
      }
    }

    Assert.assertEquals(numChildren, tnChildren.size());

    // Check to make sure all the children match types in our type array
    for (ChildTreeNode compareChild : tnChildren)
    {
      String code = compareChild.getGeoObject().getType().getCode();

      if (childrenTypes != null && !ArrayUtils.contains(childrenTypes, code))
      {
        Assert.fail("Unexpected child with code [" + code + "]. Does not match expected childrenTypes array [" + StringUtils.join(childrenTypes, ", ") + "].");
      }
    }

    for (TestGeoObjectInfo testChild : this.children)
    {
      if (childrenTypes == null || ArrayUtils.contains(childrenTypes, testChild.getGeoObjectType().getCode()))
      {
        ChildTreeNode tnChild = null;
        for (ChildTreeNode compareChild : tnChildren)
        {
          if (testChild.getCode().equals(compareChild.getGeoObject().getCode()))
          {
            tnChild = compareChild;
          }
        }

        if (tnChild == null)
        {
          Assert.fail("The ChildTreeNode did not contain a child that we expected to find.");
        }
        else if (recursive)
        {
          testChild.assertEquals(tnChild, childrenTypes, recursive);
        }
        else
        {
          testChild.assertEquals(tnChild.getGeoObject());
          USATestData.assertEqualsHierarchyType(LocatedIn.CLASS, tnChild.getHierachyType());
        }
      }
    }
  }

  public void assertEquals(ParentTreeNode tn, String[] parentTypes, boolean recursive)
  {
    this.assertEquals(tn.getGeoObject());
    // TODO : HierarchyType?

    List<ParentTreeNode> tnParents = tn.getParents();

    // Check array size
    int numParents = 0;
    for (TestGeoObjectInfo testParent : this.parents)
    {
      if (parentTypes == null || ArrayUtils.contains(parentTypes, testParent.getGeoObjectType().getCode()))
      {
        numParents++;
      }
    }
    Assert.assertEquals(numParents, tnParents.size());

    // Check to make sure all the children match types in our type array
    if (parentTypes != null)
    {
      for (ParentTreeNode compareParent : tnParents)
      {
        String code = compareParent.getGeoObject().getType().getCode();

        if (!ArrayUtils.contains(parentTypes, code))
        {
          Assert.fail("Unexpected child with code [" + code + "]. Does not match expected childrenTypes array [" + StringUtils.join(parentTypes, ", ") + "].");
        }
      }
    }

    for (TestGeoObjectInfo testParent : this.parents)
    {
      if (parentTypes == null || ArrayUtils.contains(parentTypes, testParent.getCode()))
      {
        ParentTreeNode tnParent = null;
        for (ParentTreeNode compareParent : tnParents)
        {
          if (testParent.getCode().equals(compareParent.getGeoObject().getCode()))
          {
            tnParent = compareParent;
          }
        }

        if (tnParent == null)
        {
          Assert.fail("The ParentTreeNode did not contain a child that we expected to find.");
        }
        else if (recursive)
        {
          testParent.assertEquals(tnParent, parentTypes, recursive);
        }
        else
        {
          testParent.assertEquals(tnParent.getGeoObject());
          USATestData.assertEqualsHierarchyType(LocatedIn.CLASS, tnParent.getHierachyType());
        }
      }
    }
  }

  /**
   * Asserts that the given GeoObject contains all the same data which is
   * defined by this test object. Does not check the GeoObject status (for
   * now...)
   */
  public void assertEquals(GeoObject geoObj)
  {
    assertEquals(geoObj, null);
  }

  /**
   * Asserts that the given GeoObject contains all the same data which is
   * defined by this test object. If status is provided we will assert it as
   * well.
   */
  public void assertEquals(GeoObject geoObj, DefaultTerms.GeoObjectStatusTerm status)
  {
    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(this.testDataSet.adapter, geoObj.toJSON().toString()).toJSON().toString());
    Assert.assertEquals(this.getRegistryId(), geoObj.getUid());
    Assert.assertEquals(this.getCode(), geoObj.getCode());
    Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
    Assert.assertEquals(this.getDisplayLabel(), geoObj.getLocalizedDisplayLabel());
    this.getGeoObjectType().assertEquals(geoObj.getType());

    if (status != null)
    {
      Assert.assertEquals(status.code, geoObj.getStatus().getCode());
    }
  }

  public void assertEquals(GeoEntity geoEnt)
  {
    Assert.assertEquals(this.getRegistryId(), geoEnt.getOid());
    Assert.assertEquals(this.getCode(), geoEnt.getGeoId());
    Assert.assertEquals(this.getDisplayLabel(), geoEnt.getDisplayLabel().getValue());
    this.getGeoObjectType().assertEquals(geoEnt.getUniversal());

    Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoEnt.getWkt()));
    // TODO : Check MultiPolygon and Point ?
  }

  public void addChild(TestGeoObjectInfo child, TestHierarchyTypeInfo hierarchy)
  {
    if (!this.children.contains(child))
    {
      children.add(child);
    }
    child.addParent(this);

    // if (child.getGeoObjectType().getIsLeaf())
    // {
    // ServerHierarchyType hierarchyType =
    // ServerHierarchyType.get(LocatedIn.class.getSimpleName());
    // String refAttrName =
    // hierarchyType.getParentReferenceAttributeName(this.getGeoObjectType().getUniversal());
    //
    // Business business = child.getBusiness();
    // business.lock();
    // business.setValue(refAttrName, geoEntity.getOid());
    // business.apply();
    //
    // return null;
    // }
    // else
    // {
    // return child.getGeoEntity().addLink(geoEntity, relationshipType);
    // }

    this.getServerObject().addChild(child.getServerObject(), hierarchy.getServerObject(), new Date(), new Date());
  }

  private void addParent(TestGeoObjectInfo parent)
  {
    if (!this.parents.contains(parent))
    {
      this.parents.add(parent);
    }
  }

  public TestGeoObjectTypeInfo getGeoObjectType()
  {
    return geoObjectType;
  }

  public ServerGeoObjectIF getServerObject()
  {
    // if (this.serverGO == null)
    // {
    this.serverGO = new ServerGeoObjectService().getGeoObjectByCode(this.getCode(), this.getGeoObjectType().getCode());
    // }

    return this.serverGO;
  }

  /**
   * Applies the GeoObject which is represented by this test data into the
   * database.
   * 
   * @postcondition Subsequent calls to this.getBusiness will return the
   *                business object which stores additional CGR attributes on
   *                this GeoObject
   * @postcondition Subsequent calls to this.getGeoEntity will return the
   *                GeoEntity which backs this GeoObject
   * @postcondition The applied GeoObject's status will be equal to ACTIVE
   */
  @Request
  public void apply(Date date)
  {
    ServerGeoObjectIF localServerGO = applyInTrans(date);

    if (!this.geoObjectType.getIsLeaf())
    {
      this.geoEntity = GeoEntity.get(localServerGO.getRunwayId());
      this.business = TreeServerGeoObject.getBusiness(this.geoEntity);

      this.oid = this.geoEntity.getOid();
    }
    else
    {
      this.business = Business.get(localServerGO.getRunwayId());
      this.oid = this.business.getOid();
    }

    this.registryId = localServerGO.getUid();

    this.isNew = false;
  }

  @Transaction
  private ServerGeoObjectIF applyInTrans(Date date)
  {
    if (this.testDataSet.debugMode >= 1)
    {
      System.out.println("Applying TestGeoObjectInfo [" + this.getCode() + "].");
    }

    if (date == null)
    {
      return new ServerGeoObjectService().apply(this.asGeoObject(), this.isNew, false);
    }

    return new ServerGeoObjectService().apply(this.asGeoObject(date), this.isNew, false);
  }

  /**
   * Cleans up all data in the database which is used to represent this
   * GeoObject. If the
   * 
   * @postcondition Subsequent calls to this.getGeoEntity will return null
   * @postcondition Subsequent calls to this.getBusiness will return null
   */
  @Request
  public void delete()
  {
    deleteInTrans();
  }

  // @Transaction
  // private void deleteInTrans()
  // {
  // if (this.testDataSet.debugMode >= 1)
  // {
  // System.out.println("Deleting TestGeoObjectInfo [" + this.getCode() + "].");
  // }
  //
  // ServerGeoObjectIF serverGO = this.getServerObject();
  //
  // if (serverGO != null)
  // {
  // serverGO.delete();
  // }
  //
  // this.children.clear();
  //
  // this.business = null;
  // this.geoEntity = null;
  // }
  //
  // private ServerGeoObjectIF getServerObject()
  // {
  //
  // }

  @Transaction
  private void deleteInTrans()
  {
    if (this.testDataSet.debugMode >= 1)
    {
      System.out.println("Deleting TestGeoObjectInfo [" + this.getCode() + "].");
    }

    // Make sure we delete the business first, otherwise when we delete the
    // geoEntity it nulls out the reference in the table.
    if (this.getGeoObjectType() != null && this.getGeoObjectType().getUniversal() != null)
    {
      QueryFactory qf = new QueryFactory();
      BusinessQuery bq = qf.businessQuery(this.getGeoObjectType().getUniversal().getMdBusiness().definesType());
      bq.WHERE(bq.aCharacter(DefaultAttribute.CODE.getName()).EQ(this.getCode()));
      OIterator<? extends Business> bit = bq.getIterator();
      try
      {
        while (bit.hasNext())
        {
          Business biz = bit.next();

          if (this.testDataSet.debugMode >= 2)
          {
            System.out.println("Deleting Business object with key [" + biz.getKey() + "].");
          }

          biz.delete();
        }
      }
      finally
      {
        bit.close();
      }
    }

    this.testDataSet.deleteGeoEntity(this.getCode());

    // if (this.serverGO instanceof CompositeServerGeoObject)
    // {
    // ((CompositeServerGeoObject)this.serverGO).getVertex().getVertex().delete();
    // }
    // else if (this.serverGO instanceof VertexServerGeoObject)
    // {
    // ((VertexServerGeoObject)this.serverGO).getVertex().delete();
    // }

    ServerGeoObjectType serverGOTT = this.geoObjectType.getServerObject();

    if (serverGOTT != null)
    {
      VertexObject vertex = VertexServerGeoObject.getVertexByCode(serverGOTT, this.getCode());
      if (vertex != null)
      {
        vertex.delete();
      }
    }

    this.children.clear();

    this.business = null;
    this.geoEntity = null;
    this.isNew = true;
  }

  /**
   * Fetches all children of this test object from the database and returns them
   * as GeoEntities.
   */
  @Request
  public OIterator<? extends Object> getChildrenAsGeoEntity(String relationshipType)
  {
    if (this.getGeoObjectType().getChildren().get(0).getIsLeaf())
    {
      String oid = this.business.getValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      GeoEntity entity = GeoEntity.get(oid);

      return new ListIterator<GeoEntity>(Arrays.asList(new GeoEntity[] { entity }));
    }
    else
    {
      return this.geoEntity.getChildren(relationshipType);
    }
  }

  /**
   * Asserts that the GeoObject that this test wrapper represents has been
   * applied and exists in the database with all attributes set to the values
   * that this test object contains.
   */
  @Request
  public void assertApplied()
  {
    GeoEntity myGeo = this.getGeoEntity();
    if (myGeo == null)
    {
      myGeo = GeoEntity.getByKey(this.getCode());
    }

    Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(myGeo.getWkt()));
    Assert.assertEquals(this.getCode(), myGeo.getGeoId());
    Assert.assertEquals(this.getDisplayLabel(), myGeo.getDisplayLabel().getValue());
  }
}