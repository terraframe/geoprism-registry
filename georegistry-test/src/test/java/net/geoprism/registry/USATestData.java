package net.geoprism.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.geoprism.georegistry.service.RegistryService;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.Assert;

import com.runwaysdk.ClasspathResource;
import com.runwaysdk.ClientSession;
import com.runwaysdk.business.Relationship;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdRelationship;

public class USATestData
{
  public static final String TEST_DATA_KEY = "USATestData";
  
  public static final TestUniversalInfo COUNTRY = new TestUniversalInfo("Country");
  
  public static final TestGeoEntityInfo USA = new TestGeoEntityInfo("USA", COUNTRY);
  
  public static final TestUniversalInfo STATE = new TestUniversalInfo("State");
  
  public static final TestUniversalInfo DISTRICT = new TestUniversalInfo("District");
  
  public static final TestGeoEntityInfo COLORADO = new TestGeoEntityInfo("Colorado", STATE);
  
  public static final TestGeoEntityInfo CO_D_ONE = new TestGeoEntityInfo("ColoradoDistrictOne", DISTRICT);
  
  public static final TestGeoEntityInfo CO_D_TWO = new TestGeoEntityInfo("ColoradoDistrictTwo", DISTRICT);
  
  public static final TestGeoEntityInfo CO_D_THREE = new TestGeoEntityInfo("ColoradoDistrictThree", DISTRICT);
  
  public static final TestGeoEntityInfo WASHINGTON = new TestGeoEntityInfo("Washington", STATE, "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))");
  
  public static final TestGeoEntityInfo WA_D_ONE = new TestGeoEntityInfo("WashingtonDistrictOne", DISTRICT);
  
  public static final TestGeoEntityInfo WA_D_TWO = new TestGeoEntityInfo("WashingtonDistrictTwo", DISTRICT);
  
  public static TestUniversalInfo[] UNIVERSALS = new TestUniversalInfo[]{COUNTRY, STATE, DISTRICT};
  
  public static TestGeoEntityInfo[] GEOENTITIES = new TestGeoEntityInfo[]{USA, COLORADO, WASHINGTON, CO_D_ONE, CO_D_TWO, CO_D_THREE, WA_D_ONE, WA_D_TWO};
  
  public RegistryService registryService;
  
  public ClientSession systemSession   = null;
  
  private ArrayList<TestGeoEntityInfo> customGeoInfos = new ArrayList<TestGeoEntityInfo>();

  private ArrayList<TestUniversalInfo> customUniInfos = new ArrayList<TestUniversalInfo>();
  
  static
  {
    checkDuplicateClasspathResources();
  }
  
  public USATestData()
  {
    
  }
  
  @Request
  public void setUp()
  {
    setUpInTrans();
  }
  @Transaction
  private void setUpInTrans()
  {
    cleanUp();
    
    for (TestUniversalInfo uni : UNIVERSALS)
    {
      uni.apply();
    }
    
    COUNTRY.getUniversal().addLink(Universal.getRoot(), AllowedIn.CLASS);
    COUNTRY.addChild(STATE, AllowedIn.CLASS);
    STATE.addChild(DISTRICT, AllowedIn.CLASS);
    
    for (TestGeoEntityInfo geo : GEOENTITIES)
    {
      geo.apply();
    }
    
    USA.getGeoEntity().addLink(GeoEntity.getRoot(), LocatedIn.CLASS);
    
    USA.addChild(COLORADO, LocatedIn.CLASS);
    COLORADO.addChild(CO_D_ONE, LocatedIn.CLASS);
    COLORADO.addChild(CO_D_TWO, LocatedIn.CLASS);
    COLORADO.addChild(CO_D_THREE, LocatedIn.CLASS);
    
    USA.addChild(WASHINGTON, LocatedIn.CLASS);
    WASHINGTON.addChild(WA_D_ONE, LocatedIn.CLASS);
    WASHINGTON.addChild(WA_D_TWO, LocatedIn.CLASS);
    
    registryService = new RegistryService();
    systemSession = ClientSession.createUserSession("admin", "_nm8P4gfdWxGqNRQ#8", new Locale[] { CommonProperties.getDefaultLocale() });
  }
  
  public static void assertEqualsHierarchyType(String relationshipType, HierarchyType compare)
  {
    MdRelationship allowedIn = MdRelationship.getMdRelationship(relationshipType);
    
    Assert.assertEquals(allowedIn.getKey(), compare.getCode());
    Assert.assertEquals(allowedIn.getDescription().getValue(), compare.getLocalizedDescription());
    Assert.assertEquals(allowedIn.getDisplayLabel().getValue(), compare.getLocalizedLabel());
    
//    compare.getRootGeoObjectTypes() // TODO
  }
  
  
  
  public static class TestUniversalInfo
  {
    private Universal universal;
    
    private String code;
    
    private String displayLabel;
    
    private String description;
    
    private String uid;
    
    private List<TestUniversalInfo> children;
    
    private TestUniversalInfo(String genKey)
    {
      this.code = TEST_DATA_KEY + "-" + genKey + "Code";
      this.displayLabel = TEST_DATA_KEY + " " + genKey + " Display Label";
      this.description = TEST_DATA_KEY + " " + genKey + " Description";
      this.children = new LinkedList<TestUniversalInfo>();
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
    
    public Universal getUniversal()
    {
      return this.universal;
    }
    
    public List<TestUniversalInfo> getChildren()
    {
      return this.children;
    }
    
    public Relationship addChild(TestUniversalInfo child, String relationshipType)
    {
      if (!this.children.contains(child))
      {
        this.children.add(child);
      }
      
      return child.getUniversal().addLink(universal, relationshipType);
    }
    
    public void assertEquals(GeoObjectType got)
    {
      Assert.assertEquals(code, got.getCode());
      Assert.assertEquals(displayLabel, got.getLocalizedLabel());
      Assert.assertEquals(description, got.getLocalizedDescription());
      // TOOD : check the uid
    }
    
    public void assertEquals(Universal uni)
    {
      Assert.assertEquals(code, uni.getKey());
      Assert.assertEquals(displayLabel, uni.getDisplayLabel().getValue());
      Assert.assertEquals(description, uni.getDescription().getValue());
    }
    
    public Universal apply()
    {
      universal = new Universal();
      universal.setUniversalId(this.getCode());
      universal.getDisplayLabel().setValue(this.getDisplayLabel());
      universal.getDescription().setValue(this.getDescription());
      universal.apply();
      
      this.setUid(universal.getOid());
      
      return universal;
    }
    
    public void delete()
    {
      deleteUniversal(this.getCode());
      this.children.clear();
    }

    public GeoObjectType newGeoObjectType()
    {
//      return RegistryService.getRegistryAdapter().getMetadataCache().getGeoObjectType(this.getUniversal().getKey()).get();
      return RegistryService.getConversionService().universalToGeoObjectType(this.getUniversal());
    }
  }
  
  public TestGeoEntityInfo newTestGeoEntityInfo(String genKey, TestUniversalInfo testUni)
  {
    TestGeoEntityInfo info = new TestGeoEntityInfo(genKey, testUni);
    
    info.delete();
    
    this.customGeoInfos.add(info);
    
    return info;
  }
  
  public TestGeoEntityInfo newTestGeoEntityInfo(String genKey, TestUniversalInfo testUni, String wkt)
  {
    TestGeoEntityInfo info = new TestGeoEntityInfo(genKey, testUni, wkt);
    
    info.delete();
    
    this.customGeoInfos.add(info);
    
    return info;
  }
  
  public TestUniversalInfo newTestUniversalInfo(String genKey)
  {
    TestUniversalInfo info = new TestUniversalInfo(genKey);
    
    info.delete();
    
    this.customUniInfos.add(info);
    
    return info;
  }
  
  public static class TestGeoEntityInfo
  {
    private String geoId;
    
    private String displayLabel;
    
    private String wkt;
    
    private String uid = null;
    
    private GeoEntity geoEntity;
    
    private TestUniversalInfo universal;
    
    private List<TestGeoEntityInfo> children;
    
    private List<TestGeoEntityInfo> parents;
    
    private TestGeoEntityInfo(String genKey, TestUniversalInfo testUni, String wkt)
    {
      initialize(genKey, testUni);
      this.wkt = wkt;
    }
    
    private TestGeoEntityInfo(String genKey, TestUniversalInfo testUni)
    {
      initialize(genKey, testUni);
    }
    
    private void initialize(String genKey, TestUniversalInfo testUni)
    {
      this.geoId = TEST_DATA_KEY + "-" + genKey + "Code";
      this.displayLabel = TEST_DATA_KEY + " " + genKey + " Display Label";
      this.wkt = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";
      this.universal = testUni;
      this.children = new LinkedList<TestGeoEntityInfo>();
      this.parents = new LinkedList<TestGeoEntityInfo>();
    }

    public String getGeoId() {
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
    
    public GeoEntity getGeoEntity()
    {
      return this.geoEntity;
    }
    
    public GeoObject newGeoObject()
    {
      GeoObject geoObj = RegistryService.getRegistryAdapter().newGeoObjectInstance(this.universal.getCode());
      
      geoObj.setWKTGeometry(this.getWkt());
      geoObj.setCode(this.getGeoId());
      geoObj.setLocalizedDisplayLabel(this.getDisplayLabel());
      
      if (uid != null)
      {
        geoObj.setUid(uid);
      }
      
      return geoObj;
    }
    
    public List<TestGeoEntityInfo> getChildren()
    {
      return this.children;
    }
    
    public List<TestGeoEntityInfo> getParents()
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
      for (TestGeoEntityInfo testChild : this.children)
      {
        if (ArrayUtils.contains(childrenTypes, testChild.getUniversal().getCode()))
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
      
      for (TestGeoEntityInfo testChild : this.children)
      {
        if (ArrayUtils.contains(childrenTypes, testChild.getGeoId()))
        {
          ChildTreeNode tnChild = null;
          for (ChildTreeNode compareChild : tnChildren)
          {
            if (testChild.getGeoId().equals(compareChild.getGeoObject().getCode()))
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
      for (TestGeoEntityInfo testParent : this.parents)
      {
        if (ArrayUtils.contains(parentTypes, testParent.getUniversal().getCode()))
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
      
      for (TestGeoEntityInfo testParent : this.parents)
      {
        if (ArrayUtils.contains(parentTypes, testParent.getGeoId()))
        {
          ParentTreeNode tnParent = null;
          for (ParentTreeNode compareParent : tnParents)
          {
            if (testParent.getGeoId().equals(compareParent.getGeoObject().getCode()))
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
    
    public void assertEquals(GeoObject geoObj)
    {
      Assert.assertEquals(this.getUid(), geoObj.getUid());
      Assert.assertEquals(this.getGeoId(), geoObj.getCode());
      Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
      Assert.assertEquals(this.getDisplayLabel(), geoObj.getLocalizedDisplayLabel());
      this.getUniversal().assertEquals(geoObj.getType());
    }
    
    public void assertEquals(GeoEntity geoEnt)
    {
      Assert.assertEquals(this.getUid(), geoEnt.getOid());
      Assert.assertEquals(this.getGeoId(), geoEnt.getGeoId());
      Assert.assertEquals(this.getDisplayLabel(), geoEnt.getDisplayLabel().getValue());
      this.getUniversal().assertEquals(geoEnt.getUniversal());
      
      Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoEnt.getWkt()));
      // TODO : Check MultiPolygon and Point ?
    }
    
    public Relationship addChild(TestGeoEntityInfo child, String relationshipType)
    {
      if (!this.children.contains(child))
      {
        children.add(child);
      }
      child.addParent(this);
      
      return child.getGeoEntity().addLink(geoEntity, relationshipType);
    }
    
    private void addParent(TestGeoEntityInfo parent)
    {
      if (!this.parents.contains(parent))
      {
        this.parents.add(parent);
      }
    }
    
    public TestUniversalInfo getUniversal()
    {
      return universal;
    }
    
    public GeoEntity apply()
    {
      geoEntity = new GeoEntity();
      geoEntity.setGeoId(this.getGeoId());
      geoEntity.getDisplayLabel().setValue(this.getDisplayLabel());
      geoEntity.setWkt(wkt);
      geoEntity.setUniversal(this.universal.getUniversal());
      geoEntity.apply();
      
      this.setUid(geoEntity.getOid());
      
      return geoEntity;
    }
    
    public void delete()
    {
      deleteGeoEntity(this.getGeoId());
      this.children.clear();
    }
  }
  
  @Request
  public void cleanUp()
  {
    cleanUpInTrans();
  }
  @Transaction
  public void cleanUpInTrans()
  {
    for (TestGeoEntityInfo geo : customGeoInfos)
    {
      geo.delete();
    }
    
    for (TestUniversalInfo uni : customUniInfos )
    {
      uni.delete();
    }
    
    for (TestUniversalInfo uni : UNIVERSALS)
    {
      uni.delete();
    }
    
    for (TestGeoEntityInfo geo : GEOENTITIES)
    {
      geo.delete();
    }
    
    if (systemSession != null)
    {
      systemSession.logout();
    }
  }
  
  public static void deleteUniversal(String key)
  {
    UniversalQuery uq = new UniversalQuery(new QueryFactory());
    uq.WHERE(uq.getKeyName().EQ(key));
    OIterator<? extends Universal> it = uq.getIterator();
    try
    {
      while(it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }
  
  public static void deleteGeoEntity(String key)
  {
    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    geq.WHERE(geq.getKeyName().EQ(key));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while(git.hasNext())
      {
        git.next().delete();
      }
    }
    finally
    {
      git.close();
    }
  }
  
  /**
   * Duplicate resources on the classpath may cause issues. This method checks the runwaysdk directory because conflicts there are most common.
   */
  public static void checkDuplicateClasspathResources()
  {
    Set<ClasspathResource> existingResources = new HashSet<ClasspathResource>();
    
    List<ClasspathResource> resources = ClasspathResource.getResourcesInPackage("runwaysdk");
    for (ClasspathResource resource : resources)
    {
      ClasspathResource existingRes = null;
      
      for (ClasspathResource existingResource : existingResources)
      {
        if (existingResource.getAbsolutePath().equals(resource.getAbsolutePath()))
        {
          existingRes = existingResource;
          break;
        }
      }
      
      if (existingRes != null)
      {
        System.out.println("WARNING : resource path [" + resource.getAbsolutePath() + "] is overloaded.  [" + resource.getPackageURL() + "] conflicts with existing resource [" + existingRes.getPackageURL() + "].");
      }
      
      existingResources.add(resource);
    }
  }
}
