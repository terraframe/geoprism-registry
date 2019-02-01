package net.geoprism.georegistry.testframework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms.GeoObjectStatusTerm;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.Assert;

import com.runwaysdk.ClientSession;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.Relationship;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.constants.MdAttributeLocalCharacterInfo;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.generated.system.gis.geo.AllowedInAllPathsTableQuery;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTableQuery;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdBusinessQuery;
import com.runwaysdk.system.metadata.MdRelationship;
import com.runwaysdk.util.ClasspathResource;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import net.geoprism.georegistry.AdapterUtilities;
import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.ontology.ClassifierIsARelationshipAllPathsTableQuery;
import net.geoprism.registry.GeoObjectStatus;

public class USATestData extends TestUtilities
{
  public static final String               TEST_DATA_KEY      = "USATestData";

  public final TestGeoObjectTypeInfo       COUNTRY            = new TestGeoObjectTypeInfo("Country");

  public final TestGeoObjectTypeInfo       STATE              = new TestGeoObjectTypeInfo("State");

  public final TestGeoObjectTypeInfo       DISTRICT           = new TestGeoObjectTypeInfo("District", true);

  public final TestGeoObjectInfo           USA                = new TestGeoObjectInfo("USA", COUNTRY);

  public final TestGeoObjectInfo           CANADA             = new TestGeoObjectInfo("CANADA", COUNTRY);

  public final TestGeoObjectInfo           COLORADO           = new TestGeoObjectInfo("Colorado", STATE);

  public final TestGeoObjectInfo           CO_D_ONE           = new TestGeoObjectInfo("ColoradoDistrictOne", DISTRICT);

  public final TestGeoObjectInfo           CO_D_TWO           = new TestGeoObjectInfo("ColoradoDistrictTwo", DISTRICT);

  public final TestGeoObjectInfo           CO_D_THREE         = new TestGeoObjectInfo("ColoradoDistrictThree", DISTRICT);

  public final TestGeoObjectInfo           WASHINGTON         = new TestGeoObjectInfo("Washington", STATE, "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))");

  public final TestGeoObjectInfo           WA_D_ONE           = new TestGeoObjectInfo("WashingtonDistrictOne", DISTRICT);

  public final TestGeoObjectInfo           WA_D_TWO           = new TestGeoObjectInfo("WashingtonDistrictTwo", DISTRICT);

  public TestGeoObjectTypeInfo[]           UNIVERSALS         = new TestGeoObjectTypeInfo[] { COUNTRY, STATE, DISTRICT };

  public TestGeoObjectInfo[]               GEOENTITIES        = new TestGeoObjectInfo[] { USA, CANADA, COLORADO, WASHINGTON, CO_D_ONE, CO_D_TWO, CO_D_THREE, WA_D_ONE, WA_D_TWO };

  public RegistryAdapter                   adapter;

  private GeometryType                     geometryType;

  private boolean                          includeData;

  public ClientSession                     adminSession       = null;

  public ClientRequestIF                   adminClientRequest = null;

  private ArrayList<TestGeoObjectInfo>     customGeoInfos     = new ArrayList<TestGeoObjectInfo>();

  private ArrayList<TestGeoObjectTypeInfo> customUniInfos     = new ArrayList<TestGeoObjectTypeInfo>();

  static
  {
    checkDuplicateClasspathResources();
  }

  public static USATestData newTestData()
  {
    return USATestData.newTestData(GeometryType.POLYGON, true);
  }

  @Request
  public static USATestData newTestData(GeometryType geometryType, boolean includeData)
  {
    LocalProperties.setSkipCodeGenAndCompile(true);

    RegistryAdapter adapter = ServiceFactory.getAdapter();

    USATestData data = new USATestData(adapter, geometryType, includeData);
    data.setUp();

    RegistryService.getInstance().refreshMetadataCache();

    return data;
  }

  public USATestData(RegistryAdapter adapter, GeometryType geometryType, boolean includeData)
  {
    this.adapter = adapter;
    this.geometryType = geometryType;
    this.includeData = includeData;
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

    // rebuildAllpaths();

    for (TestGeoObjectTypeInfo uni : UNIVERSALS)
    {
      uni.apply(this.geometryType);
    }

    COUNTRY.getUniversal().addLink(Universal.getRoot(), AllowedIn.CLASS);
    COUNTRY.addChild(STATE, AllowedIn.CLASS);
    STATE.addChild(DISTRICT, AllowedIn.CLASS);

    ConversionService.addParentReferenceToLeafType(LocatedIn.class.getSimpleName(), STATE.universal, DISTRICT.universal);

    if (this.includeData)
    {
      for (TestGeoObjectInfo geo : GEOENTITIES)
      {
        geo.apply();
      }

      USA.getGeoEntity().addLink(GeoEntity.getRoot(), LocatedIn.CLASS);
      CANADA.getGeoEntity().addLink(GeoEntity.getRoot(), LocatedIn.CLASS);

      USA.addChild(COLORADO, LocatedIn.CLASS);
      COLORADO.addChild(CO_D_ONE, LocatedIn.CLASS);
      COLORADO.addChild(CO_D_TWO, LocatedIn.CLASS);
      COLORADO.addChild(CO_D_THREE, LocatedIn.CLASS);

      USA.addChild(WASHINGTON, LocatedIn.CLASS);
      WASHINGTON.addChild(WA_D_ONE, LocatedIn.CLASS);
      WASHINGTON.addChild(WA_D_TWO, LocatedIn.CLASS);
    }

    adminSession = ClientSession.createUserSession("admin", "_nm8P4gfdWxGqNRQ#8", new Locale[] { CommonProperties.getDefaultLocale() });
    adminClientRequest = adminSession.getRequest();
  }

  private void rebuildAllpaths()
  {
    Classifier.getStrategy().initialize(ClassifierIsARelationship.CLASS);
    Universal.getStrategy().initialize(AllowedIn.CLASS);
    GeoEntity.getStrategy().initialize(LocatedIn.CLASS);

    if (new AllowedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
    {
      Universal.getStrategy().reinitialize(AllowedIn.CLASS);
    }

    if (new LocatedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
    {
      GeoEntity.getStrategy().reinitialize(LocatedIn.CLASS);
    }

    if (new ClassifierIsARelationshipAllPathsTableQuery(new QueryFactory()).getCount() == 0)
    {
      Classifier.getStrategy().reinitialize(ClassifierIsARelationship.CLASS);
    }
  }

  public static void assertEqualsHierarchyType(String relationshipType, HierarchyType compare)
  {
    MdRelationship allowedIn = MdRelationship.getMdRelationship(relationshipType);

    Assert.assertEquals(allowedIn.getTypeName(), compare.getCode());
    Assert.assertEquals(allowedIn.getDescription().getValue(), compare.getLocalizedDescription());
    Assert.assertEquals(allowedIn.getDisplayLabel().getValue(), compare.getLocalizedLabel());

    // compare.getRootGeoObjectTypes() // TODO
  }

  public class TestGeoObjectTypeInfo
  {
    private Universal                   universal;

    private String                      code;

    private String                      displayLabel;

    private String                      description;

    private String                      uid;

    private GeometryType                geomType;

    private boolean                     isLeaf;

    private List<TestGeoObjectTypeInfo> children;

    private TestGeoObjectTypeInfo(String genKey)
    {
      this(genKey, false);
    }

    private TestGeoObjectTypeInfo(String genKey, boolean isLeaf)
    {
      this.code = TEST_DATA_KEY + genKey + "Code";
      this.displayLabel = TEST_DATA_KEY + " " + genKey + " Display Label";
      this.description = TEST_DATA_KEY + " " + genKey + " Description";
      this.children = new LinkedList<TestGeoObjectTypeInfo>();
      this.geomType = GeometryType.POLYGON;
      this.isLeaf = isLeaf;
    }

    public String getCode()
    {
      return code;
    }

    public String getDisplayLabel()
    {
      return displayLabel;
    }

    public String getDescription()
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
      return this.universal;
    }

    public List<TestGeoObjectTypeInfo> getChildren()
    {
      return this.children;
    }

    public Relationship addChild(TestGeoObjectTypeInfo child, String relationshipType)
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
    }

    public void assertEquals(Universal uni)
    {
      Assert.assertEquals(code, uni.getKey());
      Assert.assertEquals(displayLabel, uni.getDisplayLabel().getValue());
      Assert.assertEquals(description, uni.getDescription().getValue());
    }

    public Universal apply(GeometryType geometryType)
    {
      universal = AdapterUtilities.getInstance().createGeoObjectType(this.getGeoObjectType(geometryType));

      this.setUid(universal.getOid());

      return universal;
    }

    public void delete()
    {
      deleteUniversal(this.getCode());
      deleteMdBusiness(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);
      this.children.clear();
      this.universal = null;
    }

    public GeoObjectType getGeoObjectType(GeometryType geometryType)
    {
      // if (this.getUniversal() != null)
      // {
      // return
      // registryService.getConversionService().universalToGeoObjectType(this.getUniversal());
      // }
      // else
      // {
      return new GeoObjectType(this.getCode(), geometryType, this.getDisplayLabel(), this.getDescription(), this.getIsLeaf(), adapter);
      // }
    }
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(genKey, testUni);

    info.delete();

    this.customGeoInfos.add(info);

    return info;
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni, String wkt)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(genKey, testUni, wkt);

    info.delete();

    this.customGeoInfos.add(info);

    return info;
  }

  public TestGeoObjectTypeInfo newTestGeoObjectTypeInfo(String genKey)
  {
    TestGeoObjectTypeInfo info = new TestGeoObjectTypeInfo(genKey);

    info.delete();

    this.customUniInfos.add(info);

    return info;
  }

  public class TestGeoObjectInfo
  {
    private String                  geoId;

    private String                  displayLabel;

    private String                  wkt;

    private String                  registryId = null;

    private String                  oid;

    private GeoEntity               geoEntity;

    private Business                business;

    private TestGeoObjectTypeInfo   universal;

    private List<TestGeoObjectInfo> children;

    private List<TestGeoObjectInfo> parents;

    private TestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni, String wkt)
    {
      initialize(genKey, testUni);
      this.wkt = wkt;
    }

    private TestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni)
    {
      initialize(genKey, testUni);
    }

    private void initialize(String genKey, TestGeoObjectTypeInfo testUni)
    {
      this.geoId = TEST_DATA_KEY + genKey + "Code";
      this.displayLabel = TEST_DATA_KEY + " " + genKey + " Display Label";
      this.wkt = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";
      this.universal = testUni;
      this.children = new LinkedList<TestGeoObjectInfo>();
      this.parents = new LinkedList<TestGeoObjectInfo>();
    }

    public String getGeoId()
    {
      return geoId;
    }

    public String getDisplayLabel()
    {
      return displayLabel;
    }

    public String getWkt()
    {
      return wkt;
    }

    public String getRegistryId()
    {
      return registryId;
    }

    public void setRegistryId(String uid)
    {
      this.registryId = uid;
    }

    public GeoEntity getGeoEntity()
    {
      return this.geoEntity;
    }

    public Business getBusiness()
    {
      return this.business;
    }

    public String getOid()
    {
      return oid;
    }

    public GeoObject getGeoObject()
    {
      GeoObject geoObj = adapter.newGeoObjectInstance(this.universal.getCode());

      geoObj.setWKTGeometry(this.getWkt());
      geoObj.setCode(this.getGeoId());
      geoObj.setLocalizedDisplayLabel(this.getDisplayLabel());

      if (registryId != null)
      {
        geoObj.setUid(registryId);
      }

      return geoObj;
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
        if (ArrayUtils.contains(childrenTypes, testChild.getUniversal().getCode()))
        {
          numChildren++;
        }
      }

      if (numChildren != tnChildren.size())
      {
        System.out.println("Test");
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
        if (ArrayUtils.contains(childrenTypes, testChild.getUniversal().getCode()))
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
      for (TestGeoObjectInfo testParent : this.parents)
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

      for (TestGeoObjectInfo testParent : this.parents)
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

    public void assertEquals(GeoObject geoObj, GeoObjectStatusTerm status)
    {
      Assert.assertEquals(adapter.getMetadataCache().getTerm(status.code).get(), geoObj.getStatus());
    }

    public void assertEquals(GeoObject geoObj)
    {
      Assert.assertEquals(this.getRegistryId(), geoObj.getUid());
      Assert.assertEquals(this.getGeoId(), geoObj.getCode());
      Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoObj.getGeometry().toText()));
      Assert.assertEquals(this.getDisplayLabel(), geoObj.getLocalizedDisplayLabel());
      this.getUniversal().assertEquals(geoObj.getType());
    }

    public void assertEquals(GeoEntity geoEnt)
    {
      Assert.assertEquals(this.getRegistryId(), geoEnt.getOid());
      Assert.assertEquals(this.getGeoId(), geoEnt.getGeoId());
      Assert.assertEquals(this.getDisplayLabel(), geoEnt.getDisplayLabel().getValue());
      this.getUniversal().assertEquals(geoEnt.getUniversal());

      Assert.assertEquals(StringUtils.deleteWhitespace(this.getWkt()), StringUtils.deleteWhitespace(geoEnt.getWkt()));
      // TODO : Check MultiPolygon and Point ?
    }

    public Relationship addChild(TestGeoObjectInfo child, String relationshipType)
    {
      if (!this.children.contains(child))
      {
        children.add(child);
      }
      child.addParent(this);

      if (child.getUniversal().getIsLeaf())
      {
        String refAttrName = ConversionService.getParentReferenceAttributeName(LocatedIn.class.getSimpleName(), this.getUniversal().universal);

        Business business = child.getBusiness();
        business.setValue(refAttrName, geoEntity.getOid());
        business.apply();

        return null;
      }
      else
      {
        return child.getGeoEntity().addLink(geoEntity, relationshipType);
      }
    }

    private void addParent(TestGeoObjectInfo parent)
    {
      if (!this.parents.contains(parent))
      {
        this.parents.add(parent);
      }
    }

    public TestGeoObjectTypeInfo getUniversal()
    {
      return universal;
    }

    public GeoEntity apply()
    {
      if (!this.universal.getIsLeaf())
      {
        geoEntity = new GeoEntity();
        geoEntity.setGeoId(this.getGeoId());
        geoEntity.getDisplayLabel().setValue(this.getDisplayLabel());
        geoEntity.setWkt(wkt);
        geoEntity.setUniversal(this.universal.getUniversal());
        geoEntity.apply();

        this.oid = geoEntity.getOid();

        this.setRegistryId(UUID.randomUUID().toString());

        MdBusiness mdBiz = this.universal.getUniversal().getMdBusiness();
        this.business = new Business(mdBiz.definesType());
        this.business.setValue(RegistryConstants.UUID, this.getRegistryId());
        this.business.setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, geoEntity.getOid());
        this.business.setValue(DefaultAttribute.CODE.getName(), this.getGeoId());
        this.business.setValue(DefaultAttribute.STATUS.getName(), GeoObjectStatus.ACTIVE.getOid());
        this.business.apply();
      }
      else
      {
        try
        {
          this.setRegistryId(UUID.randomUUID().toString());

          // Read WKT and generate a GeoPoint and a GeoMultiPolygon from it.
          GeometryHelper geometryHelper = new GeometryHelper();
          Geometry geo = geometryHelper.parseGeometry(this.wkt);

          MdBusiness mdBiz = this.universal.getUniversal().getMdBusiness();
          this.business = new Business(mdBiz.definesType());
          this.business.setValue(RegistryConstants.UUID, this.getRegistryId());
          this.business.setValue(DefaultAttribute.CODE.getName(), this.getGeoId());
          this.business.setValue(DefaultAttribute.STATUS.getName(), GeoObjectStatus.ACTIVE.getOid());
          this.business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, geo);
          this.business.setStructValue(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName(), MdAttributeLocalCharacterInfo.DEFAULT_LOCALE, this.getDisplayLabel());
          this.business.apply();

          this.oid = business.getOid();
        }
        catch (ParseException e)
        {
          throw new RuntimeException(e);
        }
      }

      return geoEntity;
    }

    public void delete()
    {
      // Make sure we delete the business first, otherwise when we delete the
      // geoEntity it nulls out the reference in the table.
      if (this.getUniversal() != null && this.getUniversal().getUniversal() != null)
      {
        QueryFactory qf = new QueryFactory();
        BusinessQuery bq = qf.businessQuery(this.getUniversal().getUniversal().getMdBusiness().definesType());
        bq.WHERE(bq.aCharacter(DefaultAttribute.CODE.getName()).EQ(this.getGeoId()));
        OIterator<? extends Business> bit = bq.getIterator();
        try
        {
          while (bit.hasNext())
          {
            bit.next().delete();
          }
        }
        finally
        {
          bit.close();
        }
      }

      deleteGeoEntity(this.getGeoId());

      this.children.clear();
    }

    public OIterator<? extends Object> getChildren(String relationshipType)
    {
      if (this.getUniversal().getChildren().get(0).getIsLeaf())
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
  }

  @Request
  public void cleanUp()
  {
    cleanUpInTrans();
  }

  @Transaction
  public void cleanUpInTrans()
  {
    if (STATE.universal != null && DISTRICT.universal != null)
    {
      ConversionService.removeParentReferenceToLeafType(LocatedIn.class.getSimpleName(), STATE.universal, DISTRICT.universal);
    }

    for (TestGeoObjectInfo geo : customGeoInfos)
    {
      geo.delete();
    }

    for (TestGeoObjectTypeInfo uni : customUniInfos)
    {
      uni.delete();
    }

    for (TestGeoObjectTypeInfo uni : UNIVERSALS)
    {
      uni.delete();
    }

    for (TestGeoObjectInfo geo : GEOENTITIES)
    {
      geo.delete();
    }

    if (adminSession != null)
    {
      adminSession.logout();
    }
  }

  public static void deleteMdBusiness(String pack, String type)
  {
    MdBusinessQuery mbq = new MdBusinessQuery(new QueryFactory());
    mbq.WHERE(mbq.getPackageName().EQ(pack));
    mbq.WHERE(mbq.getTypeName().EQ(type));
    OIterator<? extends MdBusiness> it = mbq.getIterator();
    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public static void deleteUniversal(String code)
  {
    UniversalQuery uq = new UniversalQuery(new QueryFactory());
    uq.WHERE(uq.getUniversalId().EQ(code));
    OIterator<? extends Universal> it = uq.getIterator();
    try
    {
      while (it.hasNext())
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
      while (git.hasNext())
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
   * Duplicate resources on the classpath may cause issues. This method checks
   * the runwaysdk directory because conflicts there are most common.
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
        System.out.println("WARNING : resource path [" + resource.getAbsolutePath() + "] is overloaded.  [" + resource.getURL() + "] conflicts with existing resource [" + existingRes.getURL() + "].");
      }

      existingResources.add(resource);
    }
  }
}
