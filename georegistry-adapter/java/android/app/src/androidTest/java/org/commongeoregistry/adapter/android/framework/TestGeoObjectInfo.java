package org.commongeoregistry.adapter.android.framework;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.http.AuthenticationException;
import org.commongeoregistry.adapter.http.ServerResponseException;
import org.junit.Assert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class TestGeoObjectInfo
{
  private USATestData testData;
  private String geoId;

  private String displayLabel;

  private String wkt;

  private String uid = null;

  private TestGeoObjectTypeInfo typeInfo;

  private List<TestGeoObjectInfo> children;

  private List<TestGeoObjectInfo> parents;

  TestGeoObjectInfo(USATestData testData, String genKey, TestGeoObjectTypeInfo typeInfo, String wkt)
  {
    this.testData = testData;
    initialize(genKey, typeInfo);
    this.wkt = wkt;
  }

  public void delete()
  {

  }

  public void setDisplayLabel(String label)
  {
    this.displayLabel = label;
  }

  TestGeoObjectInfo(USATestData testData, String genKey, TestGeoObjectTypeInfo typeInfo)
  {
    this.testData = testData;
    initialize(genKey, typeInfo);
  }

  private void initialize(String genKey, TestGeoObjectTypeInfo typeInfo)
  {
    this.geoId = USATestData.TEST_DATA_KEY + genKey;
    this.displayLabel = USATestData.TEST_DATA_KEY + " " + genKey;
    this.wkt = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";
    this.typeInfo = typeInfo;
    this.children = new LinkedList<TestGeoObjectInfo>();
    this.parents = new LinkedList<TestGeoObjectInfo>();

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

  public String getCode() {
    return geoId;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public String getWkt() {
    return wkt;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public GeoObject newGeoObject()
  {
    GeoObject geoObj = testData.client.newGeoObjectInstance(this.typeInfo.getCode());

    geoObj.setWKTGeometry(this.getWkt());
    geoObj.setCode(this.getCode());
    geoObj.getDisplayLabel().setValue(this.getDisplayLabel());
    geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, this.getDisplayLabel());


    if (uid != null)
    {
      geoObj.setUid(uid);
    }

    return geoObj;
  }

  public GeoObjectOverTime newGeoObjectOverTime()
  {
    GeoObjectOverTime geoObj = testData.client.newGeoObjectOverTimeInstance(this.typeInfo.getCode());

    geoObj.setWKTGeometry(this.getWkt(), null);
    geoObj.setCode(this.getCode());
    geoObj.getDisplayLabel(null).setValue(this.getDisplayLabel());
    geoObj.setDisplayLabel(new LocalizedValue(this.getDisplayLabel()), null, null);
    geoObj.setStatus(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code, null, null);

    if (uid != null)
    {
      geoObj.setUid(uid);
    }

    return geoObj;
  }

  public TestGeoObjectTypeInfo getGeoObjectType()
  {
    return this.typeInfo;
  }

  public List<TestGeoObjectInfo> getChildren()
  {
    return this.children;
  }

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
      if (ArrayUtils.contains(childrenTypes, testChild.getGeoObjectType().getCode()))
      {
        numChildren++;
      }
    }
    Assert.assertEquals(numChildren, tnChildren.size());

    // Check to make sure all the children match types in our type array
    for (ChildTreeNode compareChild : tnChildren)
    {
      String code = compareChild.getGeoObject().getType().getCode();

      if (!ArrayUtils.contains(childrenTypes, code))
      {
        Assert.fail("Unexpected child with code [" + code + "]. Does not match expected childrenTypes array [" + StringUtils.join(childrenTypes, ", ") + "].");
      }
    }

    for (TestGeoObjectInfo testChild : this.children)
    {
      if (ArrayUtils.contains(childrenTypes, testChild.getCode()))
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
//            USATestData.assertEqualsHierarchyTyp, tnChild.getHierachyType()); // TODO
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
      if (ArrayUtils.contains(parentTypes, testParent.getGeoObjectType().getCode()))
      {
        numParents++;
      }
    }
    Assert.assertEquals(numParents, tnParents.size());

    // Check to make sure all the children match types in our type array
    for (ParentTreeNode compareParent : tnParents)
    {
      String code = compareParent.getGeoObject().getType().getCode();

      if (!ArrayUtils.contains(parentTypes, code))
      {
        Assert.fail("Unexpected child with code [" + code + "]. Does not match expected childrenTypes array [" + StringUtils.join(parentTypes, ", ") + "].");
      }
    }

    for (TestGeoObjectInfo testParent : this.parents)
    {
      if (ArrayUtils.contains(parentTypes, testParent.getCode()))
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
//            USATestData.assertEqualsHierarchyTyp, tnParent.getHierachyType()); // TODO
        }
      }
    }
  }

  public void assertEquals(GeoObject geoObj)
  {
    Assert.assertEquals(this.getUid(), geoObj.getUid());
    Assert.assertEquals(this.getCode(), geoObj.getCode());
//      Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
    Assert.assertEquals(this.getDisplayLabel(), geoObj.getLocalizedDisplayLabel());
    this.getGeoObjectType().assertEquals(geoObj.getType());
    // TODO : state?
  }

  public void addChild(TestGeoObjectInfo child)
  {
    if (!this.children.contains(child))
    {
      children.add(child);
    }
    child.addParent(this);
  }

  private void addParent(TestGeoObjectInfo parent)
  {
    if (!this.parents.contains(parent))
    {
      this.parents.add(parent);
    }
  }

  public void fetchUid() throws AuthenticationException, ServerResponseException, IOException {
    this.setUid(testData.client.getGeoObjectByCode(this.getCode(), this.getGeoObjectType().getCode()).getUid());
  }

  public void assertEquals(GeoObjectOverTime geoObj) {
    Assert.assertEquals(this.getUid(), geoObj.getUid());
    Assert.assertEquals(this.getCode(), geoObj.getCode());
//      Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
    Assert.assertEquals(this.getDisplayLabel(), geoObj.getDisplayLabel(null).getValue());
    this.getGeoObjectType().assertEquals(geoObj.getType());
    // TODO : state?
  }
}
