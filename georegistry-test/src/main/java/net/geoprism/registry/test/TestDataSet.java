package net.geoprism.registry.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.DefaultTerms.GeoObjectStatusTerm;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.Assert;

import com.runwaysdk.ClientSession;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.Relationship;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.MdAttributeLocalCharacterInfo;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
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

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.ontology.ClassifierIsARelationshipAllPathsTableQuery;
import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AbstractActionQuery;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.WMSService;

abstract public class TestDataSet
{
  protected int                              debugMode                       = 0;

  protected ArrayList<TestGeoObjectInfo>     managedGeoObjectInfos           = new ArrayList<TestGeoObjectInfo>();

  protected ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypeInfos       = new ArrayList<TestGeoObjectTypeInfo>();

  protected ArrayList<TestGeoObjectInfo>     managedGeoObjectInfosExtras     = new ArrayList<TestGeoObjectInfo>();

  protected ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypeInfosExtras = new ArrayList<TestGeoObjectTypeInfo>();

  public TestRegistryAdapterClient           adapter;

  public ClientSession                       adminSession                    = null;

  public ClientRequestIF                     adminClientRequest              = null;

  protected GeometryType                     geometryType;                                                            // TODO
                                                                                                                      // :
                                                                                                                      // This
                                                                                                                      // doesn't
                                                                                                                      // seem
                                                                                                                      // like
                                                                                                                      // it
                                                                                                                      // should
                                                                                                                      // be
                                                                                                                      // necessary

  protected boolean                          includeData;

  public static final String                 ADMIN_USER_NAME                 = "admin";

  public static final String                 ADMIN_PASSWORD                  = "_nm8P4gfdWxGqNRQ#8";

  abstract public String getTestDataKey();

  {
    checkDuplicateClasspathResources();
  }

  public ArrayList<TestGeoObjectInfo> getManagedGeoObjects()
  {
    ArrayList<TestGeoObjectInfo> all = new ArrayList<TestGeoObjectInfo>();

    all.addAll(managedGeoObjectInfos);
    all.addAll(managedGeoObjectInfosExtras);

    return all;
  }

  public ArrayList<TestGeoObjectTypeInfo> getManagedGeoObjectTypes()
  {
    ArrayList<TestGeoObjectTypeInfo> all = new ArrayList<TestGeoObjectTypeInfo>();

    all.addAll(managedGeoObjectTypeInfos);
    all.addAll(managedGeoObjectTypeInfosExtras);

    return all;
  }

  @Request
  public void setUp()
  {
    setUpClass();

    setUpTest();
  }

  @Request
  public void cleanUp()
  {
    cleanUpClass();

    cleanUpTest();
  }

  @Request
  public void setUpClass()
  {
    // TODO : If you move this call into the 'setupInTrans' method it exposes a
    // bug in Runway which relates to transactions and MdAttributeLocalStructs
    cleanUpClass();

    setUpClassInTrans();
  }

  @Transaction
  protected void setUpClassInTrans()
  {
    for (TestGeoObjectTypeInfo uni : managedGeoObjectTypeInfos)
    {
      uni.apply(this.geometryType);
    }

    adminSession = ClientSession.createUserSession(ADMIN_USER_NAME, ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    adminClientRequest = adminSession.getRequest();
  }

  @Request
  public void setUpTest()
  {
    cleanUpTest();
    setUpTestInTrans();

    RegistryService.getInstance().refreshMetadataCache();

    adapter.setClientRequest(this.adminClientRequest);
    adapter.refreshMetadataCache();

    try
    {
      adapter.getIdService().populate(1000);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Transaction
  protected void setUpTestInTrans()
  {
    if (this.includeData)
    {
      for (TestGeoObjectInfo geo : managedGeoObjectInfos)
      {
        geo.apply();
      }
    }
  }

  @Request
  public void cleanUpClass()
  {
    cleanUpClassInTrans();
  }

  @Transaction
  protected void cleanUpClassInTrans()
  {
    for (TestGeoObjectTypeInfo got : managedGeoObjectTypeInfos)
    {
      new WMSService().deleteDatabaseView(got.getGeoObjectType(geometryType));
    }

    LinkedList<TestGeoObjectTypeInfo> list = new LinkedList<>(managedGeoObjectTypeInfos);
    Collections.reverse(list);

    for (TestGeoObjectTypeInfo got : list)
    {
      got.delete();
    }

    if (adminSession != null)
    {
      adminSession.logout();
    }
  }

  @Request
  public void cleanUpTest()
  {
    cleanUpTestInTrans();
  }

  @Transaction
  protected void cleanUpTestInTrans()
  {
    for (TestGeoObjectTypeInfo got : managedGeoObjectTypeInfosExtras)
    {
      got.delete();
    }

    for (TestGeoObjectInfo go : this.getManagedGeoObjects())
    {
      go.delete();
    }

    deleteAllActions();
    deleteAllChangeRequests();

    managedGeoObjectInfosExtras = new ArrayList<TestGeoObjectInfo>();
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

  @Request
  public static void deleteAllActions()
  {
    AbstractActionQuery aaq = new AbstractActionQuery(new QueryFactory());

    OIterator<? extends AbstractAction> it = aaq.getIterator();

    while (it.hasNext())
    {
      it.next().delete();
    }
  }

  @Request
  public static void deleteAllChangeRequests()
  {
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());

    OIterator<? extends ChangeRequest> it = crq.getIterator();

    while (it.hasNext())
    {
      it.next().delete();
    }
  }

  public void setDebugMode(int level)
  {
    this.debugMode = level;
  }

  public void assertGeoObjectStatus(GeoObject geoObj, GeoObjectStatusTerm status)
  {
    Assert.assertEquals(adapter.getMetadataCache().getTerm(status.code).get(), geoObj.getStatus());
  }

  @Request
  public static void assertEqualsHierarchyType(String relationshipType, HierarchyType compare)
  {
    MdRelationship allowedIn = MdRelationship.getMdRelationship(relationshipType);

    Assert.assertEquals(allowedIn.getTypeName(), compare.getCode());
    Assert.assertEquals(allowedIn.getDescription().getValue(), compare.getDescription().getValue());
    Assert.assertEquals(allowedIn.getDisplayLabel().getValue(), compare.getLabel().getValue());

    // compare.getRootGeoObjectTypes() // TODO
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(genKey, testUni);

    info.delete();

    this.managedGeoObjectInfosExtras.add(info);

    return info;
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni, String wkt)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(genKey, testUni, wkt);

    info.delete();

    this.managedGeoObjectInfosExtras.add(info);

    return info;
  }

  public TestGeoObjectTypeInfo newTestGeoObjectTypeInfo(String genKey)
  {
    TestGeoObjectTypeInfo info = new TestGeoObjectTypeInfo(genKey);

    info.delete();

    this.managedGeoObjectTypeInfosExtras.add(info);

    return info;
  }

  public class TestGeoObjectTypeInfo
  {
    private Universal                   universal;

    private String                      code;

    private LocalizedValue              displayLabel;

    private LocalizedValue              description;

    private String                      uid;

    private GeometryType                geomType;

    private boolean                     isLeaf;

    private List<TestGeoObjectTypeInfo> children;

    protected TestGeoObjectTypeInfo(String genKey)
    {
      this(genKey, false);
    }

    protected TestGeoObjectTypeInfo(String genKey, boolean isLeaf)
    {
      this.code = getTestDataKey() + genKey + "Code";
      this.displayLabel = new LocalizedValue(getTestDataKey() + " " + genKey + " Display Label");
      this.description = new LocalizedValue(getTestDataKey() + " " + genKey + " Description");
      this.children = new LinkedList<TestGeoObjectTypeInfo>();
      this.geomType = GeometryType.POLYGON;
      this.isLeaf = isLeaf;
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
      Assert.assertEquals(displayLabel.getValue(), got.getLabel().getValue());
      Assert.assertEquals(description.getValue(), got.getDescription().getValue());
    }

    public void assertEquals(Universal uni)
    {
      Assert.assertEquals(code, uni.getKey());
      Assert.assertEquals(displayLabel.getValue(), uni.getDisplayLabel().getValue());
      Assert.assertEquals(description.getValue(), uni.getDescription().getValue());
    }

    @Request
    public void apply(GeometryType geometryType)
    {
      applyInTrans(geometryType);
    }

    @Transaction
    private void applyInTrans(GeometryType geometryType)
    {
      if (TestDataSet.this.debugMode >= 1)
      {
        System.out.println("Applying TestGeoObjectTypeInfo [" + this.getCode() + "].");
      }

      universal = AdapterUtilities.getInstance().createGeoObjectType(this.getGeoObjectType(geometryType));

      this.setUid(universal.getOid());
    }

    @Request
    public void delete()
    {
      deleteInTrans();
    }

    @Transaction
    private void deleteInTrans()
    {
      if (TestDataSet.this.debugMode >= 1)
      {
        System.out.println("Deleting TestGeoObjectTypeInfo [" + this.getCode() + "].");
      }

      Universal uni = getUniversalIfExist(this.getCode());
      if (uni != null)
      {
        deleteUniversal(this.getCode());
      }
      MdBusiness mdBiz = getMdBusinessIfExist(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);
      if (mdBiz != null)
      {
        AttributeHierarchy.deleteByMdBusiness(MdBusinessDAO.get(mdBiz.getOid()));
        deleteMdBusiness(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);
      }

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

  public class TestGeoObjectInfo
  {
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

    protected TestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni, String wkt)
    {
      initialize(genKey, testUni);
      this.wkt = wkt;
    }

    protected TestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni)
    {
      initialize(genKey, testUni);
    }

    private void initialize(String genKey, TestGeoObjectTypeInfo testUni)
    {
      this.code = getTestDataKey() + genKey + "Code";
      this.displayLabel = getTestDataKey() + " " + genKey + " Display Label";
      this.wkt = "POLYGON ((10000 10000, 12300 40000, 16800 50000, 12354 60000, 13354 60000, 17800 50000, 13300 40000, 11000 10000, 10000 10000))";
      this.geoObjectType = testUni;
      this.children = new LinkedList<TestGeoObjectInfo>();
      this.parents = new LinkedList<TestGeoObjectInfo>();
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

    /**
     * Returns the GeoEntity that implements this test GeoObject. Will return
     * null if the test object has not been applied.
     */
    public GeoEntity getGeoEntity()
    {
      return this.geoEntity;
    }

    /**
     * Returns the business object that implements this test GeoObject and
     * contains the additional CGR attributes like Status. Will return null if
     * the test object has not been applied.
     */
    public Business getBusiness()
    {
      return this.business;
    }

    /**
     * Constructs a new GeoObject and populates all attributes from the data
     * contained within this test wrapper.
     */
    public GeoObject asGeoObject()
    {
      GeoObject geoObj = adapter.newGeoObjectInstance(this.geoObjectType.getCode());

      geoObj.setWKTGeometry(this.getWkt());
      geoObj.setCode(this.getCode());
      geoObj.getDisplayLabel().setValue(this.getDisplayLabel());
      geoObj.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, this.getDisplayLabel());

      if (registryId != null)
      {
        geoObj.setUid(registryId);
      }

      return geoObj;
    }

    /**
     * Returns all test GeoObject children that this test dataset is aware of.
     * May be out of sync with the database if operations were performed outside
     * of the test framework.
     */
    public List<TestGeoObjectInfo> getChildren()
    {
      return this.children;
    }

    /**
     * Returns all test GeoObject parents that this test dataset is aware of.
     * May be out of sync with the database if operations were performed outside
     * of the test framework.
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
      Assert.assertEquals(geoObj.toJSON().toString(), GeoObject.fromJSON(adapter, geoObj.toJSON().toString()).toJSON().toString());
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

    public Relationship addChild(TestGeoObjectInfo child, String relationshipType)
    {
      if (!this.children.contains(child))
      {
        children.add(child);
      }
      child.addParent(this);

      if (child.getGeoObjectType().getIsLeaf())
      {
        String refAttrName = ConversionService.getParentReferenceAttributeName(LocatedIn.class.getSimpleName(), this.getGeoObjectType().getUniversal());

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

    public TestGeoObjectTypeInfo getGeoObjectType()
    {
      return geoObjectType;
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
      applyInTrans();
    }

    @Transaction
    private void applyInTrans()
    {
      if (TestDataSet.this.debugMode >= 1)
      {
        System.out.println("Applying TestGeoObjectInfo [" + this.getCode() + "].");
      }

      if (!this.geoObjectType.getIsLeaf())
      {
        geoEntity = new GeoEntity();
        geoEntity.setGeoId(this.getCode());
        geoEntity.getDisplayLabel().setValue(this.getDisplayLabel());
        geoEntity.setWkt(wkt);
        geoEntity.setUniversal(this.geoObjectType.getUniversal());
        geoEntity.apply();

        this.setRegistryId(UUID.randomUUID().toString());

        MdBusiness mdBiz = this.geoObjectType.getUniversal().getMdBusiness();
        this.business = new Business(mdBiz.definesType());
        this.business.setValue(RegistryConstants.UUID, this.getRegistryId());
        this.business.setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, geoEntity.getOid());
        this.business.setValue(DefaultAttribute.CODE.getName(), this.getCode());
        this.business.setValue(DefaultAttribute.STATUS.getName(), GeoObjectStatus.ACTIVE.getOid());
        this.business.apply();

        this.oid = geoEntity.getOid();
      }
      else
      {
        try
        {
          this.setRegistryId(UUID.randomUUID().toString());

          // Read WKT and generate a GeoPoint and a GeoMultiPolygon from it.
          GeometryHelper geometryHelper = new GeometryHelper();
          Geometry geo = geometryHelper.parseGeometry(this.wkt);

          MdBusiness mdBiz = this.geoObjectType.getUniversal().getMdBusiness();
          this.business = BusinessFacade.newBusiness(mdBiz.definesType());
          // this.business = new Business(mdBiz.definesType());
          this.business.setValue(RegistryConstants.UUID, this.getRegistryId());
          this.business.setValue(DefaultAttribute.CODE.getName(), this.getCode());
          this.business.setValue(DefaultAttribute.STATUS.getName(), GeoObjectStatus.ACTIVE.getOid());
          this.business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, geo);
          this.business.setStructValue(DefaultAttribute.DISPLAY_LABEL.getName(), MdAttributeLocalCharacterInfo.DEFAULT_LOCALE, this.getDisplayLabel());
          // ((AttributeLocal)BusinessFacade.getEntityDAO(this.business).getAttributeIF(DefaultAttribute.DISPLAY_LABEL.getName())).setDefaultValue(this.getDisplayLabel());

          this.business.apply();

          this.oid = business.getOid();
        }
        catch (ParseException e)
        {
          throw new RuntimeException(e);
        }
      }
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

    @Transaction
    private void deleteInTrans()
    {
      if (TestDataSet.this.debugMode >= 1)
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

            if (TestDataSet.this.debugMode >= 2)
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

      deleteGeoEntity(this.getCode());

      this.children.clear();

      this.business = null;
      this.geoEntity = null;
    }

    /**
     * Fetches all children of this test object from the database and returns
     * them as GeoEntities.
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

  @Request
  public void deleteGeoEntity(String key)
  {
    if (this.debugMode >= 1)
    {
      System.out.println("Deleting All GeoEntities by key [" + key + "].");
    }

    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    geq.WHERE(geq.getKeyName().EQ(key));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while (git.hasNext())
      {
        GeoEntity ge = git.next();

        if (this.debugMode >= 2)
        {
          System.out.println("Deleting GeoEntity with geoId [" + ge.getGeoId() + "].");
        }

        ge.delete();
      }
    }
    finally
    {
      git.close();
    }
  }

  public MdBusiness getMdBusinessIfExist(String pack, String type)
  {
    MdBusinessQuery mbq = new MdBusinessQuery(new QueryFactory());
    mbq.WHERE(mbq.getPackageName().EQ(pack));
    mbq.WHERE(mbq.getTypeName().EQ(type));
    OIterator<? extends MdBusiness> it = mbq.getIterator();
    try
    {
      while (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

  @Request
  public void deleteMdBusiness(String pack, String type)
  {
    MdBusiness mdBiz = getMdBusinessIfExist(pack, type);

    if (mdBiz != null)
    {
      if (this.debugMode >= 1)
      {
        System.out.println("Deleting MdBusiness [" + pack + "." + type + "].");
      }

      mdBiz.delete();
    }
  }

  @Request
  public Universal getUniversalIfExist(String universalId)
  {
    UniversalQuery uq = new UniversalQuery(new QueryFactory());
    uq.WHERE(uq.getUniversalId().EQ(universalId));
    OIterator<? extends Universal> it = uq.getIterator();
    try
    {
      while (it.hasNext())
      {
        return it.next();
      }
    }
    finally
    {
      it.close();
    }

    return null;
  }

  @Request
  public void deleteUniversal(String code)
  {
    Universal uni = getUniversalIfExist(code);

    if (uni != null)
    {
      if (this.debugMode >= 1)
      {
        System.out.println("Deleting Universal [" + code + "].");
      }

      MasterList.deleteAll(uni);

      uni = Universal.get(uni.getOid());
      uni.delete();
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
