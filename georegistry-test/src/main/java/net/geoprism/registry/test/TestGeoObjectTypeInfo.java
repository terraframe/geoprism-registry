/**
 *
 */
package net.geoprism.registry.test;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class TestGeoObjectTypeInfo
{
  private String                      code;

  private LocalizedValue              displayLabel;

  private LocalizedValue              description;

  private String                      uid;

  private GeometryType                geomType;

  private boolean                     isLeaf;

  private List<TestGeoObjectTypeInfo> children;

  private TestOrganizationInfo        organization;

  private boolean                     isAbstract = false;

  private boolean                     isPrivate  = false;

  private TestGeoObjectTypeInfo       superType;

  public TestGeoObjectTypeInfo(String orgCode, String gotCode)
  {
    this.initialize(orgCode, false, GeometryType.MULTIPOLYGON, new TestOrganizationInfo(orgCode, orgCode));
  }

  public TestGeoObjectTypeInfo(String gotCode, TestOrganizationInfo organization)
  {
    initialize(gotCode, false, GeometryType.MULTIPOLYGON, organization);
  }

  public TestGeoObjectTypeInfo(String gotCode, GeometryType geomType, TestOrganizationInfo organization)
  {
    initialize(gotCode, false, geomType, organization);
  }

  public TestGeoObjectTypeInfo(String gotCode, GeometryType geomType, TestOrganizationInfo organization, Boolean isAbstract)
  {
    initialize(gotCode, false, geomType, organization);

    this.isAbstract = true;
  }

  public TestGeoObjectTypeInfo(String gotCode, GeometryType geomType, boolean isPrivate, TestOrganizationInfo organization, TestGeoObjectTypeInfo superType)
  {
    initialize(gotCode, isPrivate, geomType, organization);

    this.superType = superType;
  }

  private void initialize(String genKey, boolean isPrivate, GeometryType geomType, TestOrganizationInfo organization)
  {
    LocalizedValue label = new LocalizedValue(genKey);
    label.setValue(LocalizedValue.DEFAULT_LOCALE, genKey);

    this.code = genKey;
    this.displayLabel = label;
    this.description = label;
    this.children = new LinkedList<TestGeoObjectTypeInfo>();
    this.geomType = geomType;
    this.isLeaf = false; // Leaf types are not supported anymore
    this.organization = organization;
    this.isPrivate = isPrivate;
  }

  public String getCode()
  {
    return code;
  }

  public LocalizedValue getDisplayLabel()
  {
    return displayLabel;
  }

  public LocalizedValue getDescription()
  {
    return description;
  }

  public GeometryType getGeometryType()
  {
    return geomType;
  }

  public boolean getIsLeaf()
  {
    return isLeaf;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public TestGeoObjectTypeInfo getSuperType()
  {
    return superType;
  }

  public void setSuperType(TestGeoObjectTypeInfo superType)
  {
    this.superType = superType;
  }

  public boolean isAbstract()
  {
    return isAbstract;
  }

  public void setAbstract(boolean isAbstract)
  {
    this.isAbstract = isAbstract;
  }

  public boolean isPrivate()
  {
    return isPrivate;
  }

  public void setPrivate(boolean isPrivate)
  {
    this.isPrivate = isPrivate;
  }

  public TestOrganizationInfo getOrganization()
  {
    return this.organization;
  }

  public ServerGeoObjectType getServerObject()
  {
    return this.getServerObject(false);
  }

  public ServerGeoObjectType getServerObject(boolean forceFetch)
  {
    return ServerGeoObjectType.get(code, true);
  }

  public GeoObjectType fetchDTO()
  {
    return this.getServerObject().toDTO();
  }

  // public ServerGeoObjectType getGeoObjectType()
  // {
  // if (this.getUniversal() != null)
  // {
  // return
  // registryService.getConversionService().universalToGeoObjectType(this.getUniversal());
  // }
  // else
  // {
  // return ServerGeoObjectType.get(this.getCode());
  // }
  // }

  public List<TestGeoObjectTypeInfo> getChildren()
  {
    return this.children;
  }

  public void addChild(TestGeoObjectTypeInfo child, TestHierarchyTypeInfo hierarchy)
  {
    if (!this.children.contains(child))
    {
      this.children.add(child);
    }

    HierarchyTypeBusinessServiceIF service = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);

    try
    {

      service.addToHierarchy(hierarchy.getServerObject(), this.getServerObject(), child.getServerObject());
    }
    catch (GeoObjectTypeAlreadyInHierarchyException e)
    {
      // IGNORE - This can happen on the FastTestDataset because its not cleaned
      // up after running
    }
  }

  public void assertEquals(GeoObjectType got)
  {
    Assert.assertEquals(code, got.getCode());
    Assert.assertEquals(displayLabel.getValue(), got.getLabel().getValue());
    Assert.assertEquals(description.getValue(), got.getDescription().getValue());
    Assert.assertEquals(this.organization.getCode(), got.getOrganizationCode());
  }

  public void assertEquals(Universal uni)
  {
    Assert.assertEquals(code, uni.getKey());
    Assert.assertEquals(displayLabel.getValue(), uni.getDisplayLabel().getValue());
    Assert.assertEquals(description.getValue(), uni.getDescription().getValue());
  }

  @Request
  public void apply()
  {
    applyInTrans();
  }

  @Transaction
  private void applyInTrans()
  {
    if (this.getServerObject() != null)
    {
      return;
    }

    GeoObjectType got = toDTO();

    GeoObjectTypeBusinessServiceIF service = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);

    ServerGeoObjectType type = service.create(got);

    this.setUid(type.getOid());
  }

  public GeoObjectType toDTO()
  {
    TestRegistryAdapter adapter = ServiceFactory.getBean(TestRegistryAdapter.class);

    String organizationCode = this.getOrganization().getCode();

    GeoObjectType got = new GeoObjectType(this.getCode(), this.geomType, this.getDisplayLabel(), this.getDescription(), true, organizationCode, adapter);
    got.setIsAbstract(this.isAbstract);
    got.setIsPrivate(this.isPrivate);

    if (this.getSuperType() != null)
    {
      got.setSuperTypeCode(this.getSuperType().code);
    }
    return got;
  }

  @Request
  public void delete()
  {
    deleteInTrans();
  }

  @Transaction
  private void deleteInTrans()
  {
    ServerGeoObjectType type = this.getServerObject(true);

    if (type != null)
    {
      GeoObjectTypeBusinessServiceIF service = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);
      service.deleteGeoObjectType(type.getCode());
    }

    // Universal uni = this.testDataSet.getUniversalIfExist(this.getCode());
    // MdClass mdBiz =
    // this.testDataSet.getMdClassIfExist(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE,
    // this.code);
    // if (mdBiz != null)
    // {
    // AttributeHierarchy.deleteByMdBusiness(MdBusinessDAO.get(mdBiz.getOid()));
    // }
    // new WMSService().deleteDatabaseViewIfExists(this.getCode());
    // if (uni != null)
    // {
    // this.testDataSet.deleteUniversal(this.getCode());
    // }
    // if (mdBiz != null)
    // {
    // this.testDataSet.deleteMdClass(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE,
    // this.code);
    // }
    // MdClass geoVertexType =
    // this.testDataSet.getMdClassIfExist(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE,
    // this.code);
    // if (geoVertexType != null)
    // {
    // this.testDataSet.deleteMdClass(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE,
    // this.code);
    // }

    this.children.clear();
  }

  public boolean isPersisted()
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(code);

    return type.getType().isAppliedToDb();
  }

  @Request
  public void assertApplied()
  {
    ServerGeoObjectType type = this.getServerObject(true);

    this.assertEquals(type.toDTO());
  }

  public TestAttributeTypeInfo getAttribute(String cgrAttrName)
  {
    return new TestAttributeTypeInfo(cgrAttrName, this);
  }
}
