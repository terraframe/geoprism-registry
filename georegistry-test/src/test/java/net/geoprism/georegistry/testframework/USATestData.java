package net.geoprism.georegistry.testframework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.Assert;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.generated.system.gis.geo.AllowedInAllPathsTableQuery;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTableQuery;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdRelationship;
import com.runwaysdk.util.ClasspathResource;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryService;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.ontology.ClassifierIsARelationshipAllPathsTableQuery;

public class USATestData extends TestDataSet
{
  public final String                      TEST_DATA_KEY      = "USATestData";

  public final TestGeoObjectTypeInfo       COUNTRY            = new TestGeoObjectTypeInfo("Country");

  public final TestGeoObjectTypeInfo       STATE              = new TestGeoObjectTypeInfo("State");

  public final TestGeoObjectTypeInfo       DISTRICT           = new TestGeoObjectTypeInfo("District", true);

  public final TestGeoObjectInfo           USA                = new TestGeoObjectInfo("USA", COUNTRY);

  public final TestGeoObjectInfo           COLORADO           = new TestGeoObjectInfo("Colorado", STATE);

  public final TestGeoObjectInfo           CO_D_ONE           = new TestGeoObjectInfo("ColoradoDistrictOne", DISTRICT);

  public final TestGeoObjectInfo           CO_D_TWO           = new TestGeoObjectInfo("ColoradoDistrictTwo", DISTRICT);

  public final TestGeoObjectInfo           CO_D_THREE         = new TestGeoObjectInfo("ColoradoDistrictThree", DISTRICT);

  public final TestGeoObjectInfo           WASHINGTON         = new TestGeoObjectInfo("Washington", STATE, "POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2))");

  public final TestGeoObjectInfo           WA_D_ONE           = new TestGeoObjectInfo("WashingtonDistrictOne", DISTRICT);

  public final TestGeoObjectInfo           WA_D_TWO           = new TestGeoObjectInfo("WashingtonDistrictTwo", DISTRICT);

  public TestGeoObjectTypeInfo[]           UNIVERSALS         = new TestGeoObjectTypeInfo[] { COUNTRY, STATE, DISTRICT };

  public TestGeoObjectInfo[]               GEOENTITIES        = new TestGeoObjectInfo[] { USA, COLORADO, WASHINGTON, CO_D_ONE, CO_D_TWO, CO_D_THREE, WA_D_ONE, WA_D_TWO };

  private GeometryType                     geometryType;

  private boolean                          includeData;

  public ClientSession                     adminSession       = null;

  public ClientRequestIF                   adminClientRequest = null;

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

    TestRegistryAdapterClient adapter = new TestRegistryAdapterClient();

    USATestData data = new USATestData(adapter, geometryType, includeData);
    data.setUp();

    RegistryService.getInstance().refreshMetadataCache();
    
    adapter.setClientRequest(data.adminClientRequest);
    adapter.refreshMetadataCache();
    adapter.getIdSerivce().populate(1000);

    return data;
  }

  public USATestData(TestRegistryAdapterClient adapter, GeometryType geometryType, boolean includeData)
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

    ConversionService.addParentReferenceToLeafType(LocatedIn.class.getSimpleName(), STATE.getUniversal(), DISTRICT.getUniversal());

    if (this.includeData)
    {
      for (TestGeoObjectInfo geo : GEOENTITIES)
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

  @Request
  public void cleanUp()
  {
    cleanUpInTrans();
  }

  @Transaction
  public void cleanUpInTrans()
  {
    if (STATE.getUniversal() != null && DISTRICT.getUniversal() != null)
    {
      ConversionService.removeParentReferenceToLeafType(LocatedIn.class.getSimpleName(), STATE.getUniversal(), DISTRICT.getUniversal());
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

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
