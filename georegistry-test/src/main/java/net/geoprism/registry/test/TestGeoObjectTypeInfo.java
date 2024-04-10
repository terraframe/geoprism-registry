/**
 *
 */
package net.geoprism.registry.test;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdClass;

import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class TestGeoObjectTypeInfo extends TestCachedObject<ServerGeoObjectType>
{
  private Universal                   universal;

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
    this.code = genKey;
    this.displayLabel = new LocalizedValue(genKey);
    this.description = new LocalizedValue(genKey);
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

  public Universal getUniversal()
  {
    if (this.universal != null)
    {
      return this.universal;
    }
    else
    {
      // return Universal.getByKey(this.getCode());
      return TestDataSet.getUniversalIfExist(this.getCode());
    }
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
    if (this.getCachedObject() != null && !forceFetch)
    {
      return this.getCachedObject();
    }
    else
    {
      Optional<ServerGeoObjectType> got = ServiceFactory.getMetadataCache().getGeoObjectType(code);

      if (got.isPresent())
      {
        this.setCachedObject(got.get());

        return this.getCachedObject();
      }
      else
      {
        Universal uni = TestDataSet.getUniversalIfExist(getCode());

        if (uni == null)
        {
          return null;
        }

        GeoObjectTypeBusinessServiceIF service = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);

        this.setCachedObject(service.build(uni));

        return this.getCachedObject();
      }
    }
  }

  public GeoObjectType fetchDTO()
  {
    return this.getServerObject().getType();
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

    service.addToHierarchy(hierarchy.getServerObject(), this.getServerObject(), child.getServerObject());
    // this.getServerObject().add(child.getServerObject(),
    // hierarchy.getServerObject());
    // return child.getUniversal().addLink(universal,
    // hierarchy.getServerObject());
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

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addGeoObjectType(this.getCachedObject());
  }

  @Transaction
  private void applyInTrans()
  {
    if (this.getServerObject() != null)
    {
      return;
    }

    String organizationCode = this.getOrganization().getCode();

    GeoObjectType got = new GeoObjectType(this.getCode(), this.geomType, this.getDisplayLabel(), this.getDescription(), true, organizationCode, ServiceFactory.getAdapter());
    got.setIsAbstract(this.isAbstract);
    got.setIsPrivate(this.isPrivate);

    if (this.getSuperType() != null)
    {
      got.setSuperTypeCode(this.getSuperType().code);
    }

    GeoObjectTypeBusinessServiceIF service = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);

    this.setCachedObject(service.create(got));

    universal = this.getCachedObject().getUniversal();

    this.setUid(universal.getOid());
  }

  @Request
  public void delete()
  {
    deleteInTrans();

    this.setCachedObject(null);
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
    this.universal = null;
  }

  public boolean isPersisted()
  {
    Boolean exists = ServiceFactory.getMetadataCache().getGeoObjectType(code).isPresent();

    if (exists)
    {
      MdClass universal = TestDataSet.getMdClassIfExist(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);

      exists = universal != null;
    }

    return exists;
  }

  @Request
  public void assertApplied()
  {
    ServerGeoObjectType type = this.getServerObject(true);

    this.assertEquals(type.getType());
  }

  public TestAttributeTypeInfo getAttribute(String cgrAttrName)
  {
    return new TestAttributeTypeInfo(cgrAttrName, this);
  }
}
