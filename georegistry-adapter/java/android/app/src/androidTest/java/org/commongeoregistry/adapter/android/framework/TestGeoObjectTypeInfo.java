package org.commongeoregistry.adapter.android.framework;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;

import java.util.LinkedList;
import java.util.List;

public class TestGeoObjectTypeInfo
{
  private USATestData testData;
  private String code;

  private String displayLabel;

  private String description;

  private String uid;

  private GeometryType geom;

  private List<TestGeoObjectTypeInfo> children;

  TestGeoObjectTypeInfo(USATestData testData, String genKey, GeometryType geom)
  {
    this.initialize(testData, genKey, geom);
  }

  TestGeoObjectTypeInfo(USATestData testData, String genKey) {
    this.initialize(testData, genKey, GeometryType.MULTIPOLYGON);
  }

  private void initialize(USATestData testData, String genKey, GeometryType geom)
  {
    this.testData = testData;
    this.code = USATestData.TEST_DATA_KEY + genKey;
    this.displayLabel = USATestData.TEST_DATA_KEY + " " + genKey;
    this.description = USATestData.TEST_DATA_KEY + " " + genKey;
    this.children = new LinkedList<TestGeoObjectTypeInfo>();
    this.geom = geom;
  }

  public String getCode() {
    return code;
  }

  public String getDisplayLabel() {
    return displayLabel;
  }

  public String getDescription() {
    return description;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public List<TestGeoObjectTypeInfo> getChildren()
  {
    return this.children;
  }

  public void addChild(TestGeoObjectTypeInfo child)
  {
    if (!this.children.contains(child))
    {
      this.children.add(child);
    }
  }

  public void assertEquals(GeoObjectType got)
  {
    Assert.assertEquals(code, got.getCode());
    Assert.assertEquals(displayLabel, got.getLabel().getValue());
    Assert.assertEquals(description, got.getDescription().getValue());
    // TOOD : check the uid
  }

  public GeoObjectType getGeoObjectType()
  {
    return testData.client.getMetadataCache().getGeoObjectType(this.code).get();
  }

  public void delete()
  {

  }

  public GeometryType getGeometryType() {
    return this.geom;
  }
}
