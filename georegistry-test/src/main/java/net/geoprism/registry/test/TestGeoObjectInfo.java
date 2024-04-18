/**
 *
 */
package net.geoprism.registry.test;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.junit.Assert;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class TestGeoObjectInfo extends TestCachedObject<ServerGeoObjectIF>
{
  private String                  code;

  private String                  displayLabel;

  private String                  wkt;

  private String                  registryId = null;

  private TestGeoObjectTypeInfo   geoObjectType;

  private List<TestGeoObjectInfo> children;

  private List<TestGeoObjectInfo> parents;

  private Boolean                 exists;

  private Boolean                 isNew;

  private Date                    date;

  private HashMap<String, Object> defaultValues;

  public TestGeoObjectInfo(String label, String code, TestGeoObjectTypeInfo testUni, String wkt, Boolean exists, Boolean isNew)
  {
    initialize(code, testUni, exists, isNew);
    this.displayLabel = label;
    this.wkt = wkt;
  }

  public TestGeoObjectInfo(String code, TestGeoObjectTypeInfo testUni, String wkt, Boolean exists, Boolean isNew)
  {
    initialize(code, testUni, exists, isNew);
    this.wkt = wkt;
  }

  public TestGeoObjectInfo(String code, TestGeoObjectTypeInfo testUni)
  {
    initialize(code, testUni, true, true);
  }

  private void initialize(String code, TestGeoObjectTypeInfo testUni, Boolean exists, Boolean isNew)
  {
    if (code.contains(" "))
    {
      throw new ProgrammingErrorException("This will cause a confusing error downstream. Your code can't have a space in it.");
    }

    this.code = code;
    this.displayLabel = code;
    this.geoObjectType = testUni;
    this.children = new LinkedList<TestGeoObjectInfo>();
    this.parents = new LinkedList<TestGeoObjectInfo>();
    this.exists = exists;
    this.isNew = isNew;
    this.date = TestDataSet.DEFAULT_OVER_TIME_DATE;
    this.defaultValues = new HashMap<String, Object>();

    this.registryId = null;
    this.setCachedObject(null);

    GeometryType geom = this.getGeoObjectType().getGeometryType();
    if (geom == GeometryType.MULTIPOINT)
    {
      this.wkt = TestDataSet.WKT_DEFAULT_MULTIPOINT;
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

  public void setExists(boolean exists)
  {
    this.exists = exists;
  }

  public boolean getExists()
  {
    return this.exists;
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

  public void removeDefaultValue(String attr)
  {
    this.defaultValues.remove(attr);
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

  public Geometry getGeometry()
  {
    if (this.getWkt() == null)
    {
      return null;
    }

    try
    {
      final WKTReader reader = new WKTReader(new GeometryFactory());

      Geometry geometry = reader.read(this.getWkt());
      geometry.setSRID(4326);
      return geometry;
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Request
  public GeoObject fetchGeoObject()
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    return service.toGeoObject(this.getServerObject(), TestDataSet.DEFAULT_OVER_TIME_DATE);
  }

  @Request
  public GeoObjectOverTime fetchGeoObjectOverTime()
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    return service.toGeoObjectOverTime(this.getServerObject());
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
        Assert.fail("The ChildTreeNode did not contain a child that we expected to find [" + expectedChild.getCode() + "].");
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
        Assert.fail("The ParentTreeNode did not contain a parent [" + expectedParent.getCode() + "] that we expected to find.");
      }
    }
  }

  /**
   * Asserts that the given GeoObject contains all the same data which is
   * defined by this test object.
   */
  public void assertEquals(GeoObjectOverTime geoObj)
  {
    TestRegistryAdapter adapter = ServiceFactory.getBean(TestRegistryAdapter.class);

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObjectOverTime.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
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
    TestRegistryAdapter adapter = ServiceFactory.getBean(TestRegistryAdapter.class);

    Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
    // Assert.assertEquals(this.getRegistryId(), geoObj.getUid());
    Assert.assertEquals(this.getCode(), geoObj.getCode());
    Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
    Assert.assertEquals(this.getDisplayLabel(), geoObj.getLocalizedDisplayLabel());
    this.getGeoObjectType().assertEquals(geoObj.getType());
    Assert.assertEquals(this.getExists(), geoObj.getExists());

    // Assert.assertEquals(status.code, geoObj.getStatus().getCode());
  }

  @Request
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

    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    service.addChild(this.getServerObject(), child.getServerObject(), hierarchy.getServerObject(), date, TestDataSet.DEFAULT_END_TIME_DATE);
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
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    this.setCachedObject(service.getGeoObjectByCode(this.getCode(), this.getGeoObjectType().getCode(), false));
    // }

    return this.getCachedObject();
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
  public ServerGeoObjectIF apply()
  {
    ServerGeoObjectIF localServerGO = applyInTrans(date);

    this.registryId = localServerGO.getUid();

    this.isNew = false;

    return localServerGO;
  }

  @Transaction
  private ServerGeoObjectIF applyInTrans(Date date)
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);
    TestRegistryAdapter adapter = ServiceFactory.getBean(TestRegistryAdapter.class);

    if (date == null)
    {
      return service.apply(this.newGeoObject(adapter), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, this.isNew, false);
    }

    return service.apply(this.newGeoObjectOverTime(adapter), this.isNew, false);
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
      GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

      ServerGeoObjectIF vertex = service.getGeoObjectByCode(this.getCode(), serverGOTT, false);

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
    this.setCachedObject(null);
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
    geoObj.setExists(this.exists);

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

    if (geometry != null)
    {
      geoObj.setGeometry(geometry, date, TestDataSet.DEFAULT_END_TIME_DATE);
    }

    geoObj.setCode(this.getCode());
    geoObj.setDisplayLabel(label, date, TestDataSet.DEFAULT_END_TIME_DATE);

    geoObj.setExists(this.exists, date, TestDataSet.DEFAULT_END_TIME_DATE);

    if (registryId != null)
    {
      geoObj.setUid(registryId);
    }

    for (String attrName : this.defaultValues.keySet())
    {
      Object value = this.defaultValues.get(attrName);

      if (value != null)
      {
        geoObj.setValue(attrName, value, date, TestDataSet.DEFAULT_END_TIME_DATE);
      }
    }
  }

  /**
   * Don't use this method. Specify your date range.
   */
  @Request
  public void assertApplied()
  {
    this.assertApplied(TestDataSet.DEFAULT_OVER_TIME_DATE);
  }

  /**
   * Asserts that the GeoObject that this test wrapper represents has been
   * applied and exists in the database with all attributes set to the values
   * that this test object contains.
   */
  @Request
  public void assertApplied(Date startDate)
  {
    GeoObjectBusinessServiceIF service = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    ServerGeoObjectIF serverGO = this.getServerObject();

    this.assertEquals(service.toGeoObject(serverGO, startDate));
  }
}