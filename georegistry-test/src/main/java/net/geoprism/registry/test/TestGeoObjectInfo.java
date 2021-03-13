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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.service.ServiceFactory;

public class TestGeoObjectInfo
{
  private String                  code;

  private String                  displayLabel;

  private String                  wkt;

  private String                  registryId = null;

  private TestGeoObjectTypeInfo   geoObjectType;

  private List<TestGeoObjectInfo> children;

  private List<TestGeoObjectInfo> parents;

  private ServerGeoObjectIF       serverGO;

  private String                  statusCode;

  private Boolean                 isNew;

  private Date                    date;

  private HashMap<String, Object> defaultValues;

  public TestGeoObjectInfo(String code, TestGeoObjectTypeInfo testUni, String wkt, String statusCode, Boolean isNew)
  {
    initialize(code, testUni, statusCode, isNew);
    this.wkt = wkt;
  }

  public TestGeoObjectInfo(String code, TestGeoObjectTypeInfo testUni)
  {
    initialize(code, testUni, DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, true);
  }

  private void initialize(String code, TestGeoObjectTypeInfo testUni, String statusCode, Boolean isNew)
  {
    this.code = code;
    this.displayLabel = code;
    this.geoObjectType = testUni;
    this.children = new LinkedList<TestGeoObjectInfo>();
    this.parents = new LinkedList<TestGeoObjectInfo>();
    this.statusCode = statusCode;
    this.isNew = isNew;
    this.date = TestDataSet.DEFAULT_OVER_TIME_DATE;
    this.defaultValues = new HashMap<String, Object>();

    this.registryId = null;
    this.serverGO = null;

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

  public void setDefaultValue(String attr, Object value)
  {
    this.defaultValues.put(attr, value);
  }

  public Object getDefaultValue(String attr)
  {
    return this.defaultValues.get(attr);
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

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public Date getDate()
  {
    return date;
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

  @Request
  public GeoObject fetchGeoObject()
  {
    return this.getServerObject().toGeoObject();
  }

  @Request
  public GeoObjectOverTime fetchGeoObjectOverTime()
  {
    return this.getServerObject().toGeoObjectOverTime();
  }

  /**
   * Constructs a new GeoObject and populates all attributes from the data
   * contained within this test wrapper.
   */
  @Request
  public GeoObject newGeoObject(RegistryAdapter adapter)
  {
    GeoObject geoObj = adapter.newGeoObjectInstance(this.geoObjectType.getCode());

    this.populate(geoObj);

    return geoObj;
  }

  /**
   * Constructs a new GeoObject and populates all attributes from the data
   * contained within this test wrapper.
   */
  @Request
  public GeoObjectOverTime newGeoObjectOverTime(RegistryAdapter adapter)
  {
    GeoObjectOverTime geoObj = adapter.newGeoObjectOverTimeInstance(this.geoObjectType.getCode());

    this.populate(geoObj);

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
  
  public void childTreeNodeAssert(ChildTreeNode tn, List<TestGeoObjectInfo> expectedChildren)
  {
    this.assertEquals(tn.getGeoObject());
    // TODO : HierarchyType?

    List<ChildTreeNode> tnChildren = tn.getChildren();

    Assert.assertEquals(expectedChildren.size(), tnChildren.size());

    for (TestGeoObjectInfo expectedChild : expectedChildren)
    {
      ChildTreeNode tnChild = null;
      for (ChildTreeNode compareChild : tnChildren)
      {
        if (expectedChild.getCode().equals(compareChild.getGeoObject().getCode()))
        {
          tnChild = compareChild;
        }
      }

      if (tnChild == null)
      {
        Assert.fail("The ChildTreeNode did not contain a child that we expected to find.");
      }
    }
  }

  public void parentTreeNodeAssert(ParentTreeNode tn, List<TestGeoObjectInfo> expectedParents)
  {
    this.assertEquals(tn.getGeoObject());
    // TODO : HierarchyType?

    List<ParentTreeNode> tnParents = tn.getParents();

    Assert.assertEquals(expectedParents.size(), tnParents.size());

    for (TestGeoObjectInfo expectedParent : expectedParents)
    {
      ParentTreeNode tnParent = null;
      for (ParentTreeNode compareParent : tnParents)
      {
        if (expectedParent.getCode().equals(compareParent.getGeoObject().getCode()))
        {
          tnParent = compareParent;
        }
      }

      if (tnParent == null)
      {
        Assert.fail("The ParentTreeNode did not contain a parent that we expected to find.");
      }
    }
  }

  /**
   * Asserts that the given GeoObject contains all the same data which is
   * defined by this test object.
   */
  public void assertEquals(GeoObjectOverTime geoObj)
  {
    Assert.assertEquals(geoObj.toJSON().toString(), GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), geoObj.toJSON().toString()).toJSON().toString());
    // Assert.assertEquals(this.getRegistryId(), geoObj.getUid());
    Assert.assertEquals(this.getCode(), geoObj.getCode());
    Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry(date).toText()));
    Assert.assertEquals(this.getDisplayLabel(), geoObj.getDisplayLabel(this.date).getValue());
    this.getGeoObjectType().assertEquals(geoObj.getType());

    // Assert.assertEquals(status.code, geoObj.getStatus().getCode());
  }

  /**
   * Asserts that the given GeoObject contains all the same data which is
   * defined by this test object.
   */
  public void assertEquals(GeoObject geoObj)
  {
    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(ServiceFactory.getAdapter(), geoObj.toJSON().toString()).toJSON().toString());
    // Assert.assertEquals(this.getRegistryId(), geoObj.getUid());
    Assert.assertEquals(this.getCode(), geoObj.getCode());
    Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
    Assert.assertEquals(this.getDisplayLabel(), geoObj.getLocalizedDisplayLabel());
    this.getGeoObjectType().assertEquals(geoObj.getType());

    // Assert.assertEquals(status.code, geoObj.getStatus().getCode());
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

    this.getServerObject().addChild(child.getServerObject(), hierarchy.getServerObject(), date, ValueOverTime.INFINITY_END_DATE);
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
    this.serverGO = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService()).getGeoObjectByCode(this.getCode(), this.getGeoObjectType().getCode());
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
  public void apply()
  {
    ServerGeoObjectIF localServerGO = applyInTrans(date);

    this.registryId = localServerGO.getUid();

    this.isNew = false;
  }

  @Transaction
  private ServerGeoObjectIF applyInTrans(Date date)
  {
    ServerGeoObjectService service = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService());

    if (date == null)
    {
      return service.apply(this.newGeoObject(ServiceFactory.getAdapter()), this.isNew, false);
    }

    return service.apply(this.newGeoObjectOverTime(ServiceFactory.getAdapter()), this.isNew, false);
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

          biz.delete();
        }
      }
      finally
      {
        bit.close();
      }
    }

    TestDataSet.deleteGeoEntity(this.getCode());

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

    this.isNew = true;
  }

  public void clean()
  {
    // this.children.clear();
    // this.parents.clear();
    // this.defaultValues = new HashMap<String, Object>();

    this.isNew = true;

    this.registryId = null;
    this.serverGO = null;
  }

  /**
   * Populates the GeoObject with the values contained in this test object.
   * 
   * @param go
   */
  public void populate(GeoObject geoObj)
  {
    geoObj.setWKTGeometry(this.getWkt());
    geoObj.setCode(this.getCode());
    geoObj.getDisplayLabel().setValue(this.getDisplayLabel());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, this.getDisplayLabel());
    geoObj.setStatus(this.statusCode);

    if (registryId != null)
    {
      geoObj.setUid(registryId);
    }

    for (String attrName : this.defaultValues.keySet())
    {
      geoObj.setValue(attrName, this.defaultValues.get(attrName));
    }
  }

  /**
   * Populates the GeoObject with the values contained in this test object.
   * 
   * @param go
   */
  public void populate(GeoObjectOverTime geoObj)
  {
    final LocalizedValue label = new LocalizedValue(this.getDisplayLabel());
    label.setValue(LocalizedValue.DEFAULT_LOCALE, this.getDisplayLabel());

    final Geometry geometry = this.getGeometry();

    geoObj.setGeometry(geometry, date, ValueOverTime.INFINITY_END_DATE);
    geoObj.setCode(this.getCode());
    geoObj.setDisplayLabel(label, date, ValueOverTime.INFINITY_END_DATE);
    
    geoObj.getAllValues(DefaultAttribute.STATUS.getName()).clear();
    geoObj.setStatus(this.statusCode, date, ValueOverTime.INFINITY_END_DATE);

    if (registryId != null)
    {
      geoObj.setUid(registryId);
    }

    for (String attrName : this.defaultValues.keySet())
    {
      geoObj.setValue(attrName, this.defaultValues.get(attrName), date, ValueOverTime.INFINITY_END_DATE);
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
    ServerGeoObjectIF serverGO = this.getServerObject();

    this.assertEquals(serverGO.toGeoObject());
  }
}