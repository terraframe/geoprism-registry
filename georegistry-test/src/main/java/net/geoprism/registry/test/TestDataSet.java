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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.DefaultTerms.GeoObjectStatusTerm;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.junit.Assert;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.resource.ClasspathResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdClass;
import com.runwaysdk.system.metadata.MdClassQuery;
import com.runwaysdk.system.metadata.MdRelationship;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AbstractActionQuery;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.WMSService;

abstract public class TestDataSet
{
  protected int                              debugMode                       = 0;

  protected ArrayList<TestOrganizationInfo>  managedOrganizationInfos        = new ArrayList<TestOrganizationInfo>();
  
  protected ArrayList<TestOrganizationInfo>  managedOrganizationInfosExtras  = new ArrayList<TestOrganizationInfo>();
  
  protected ArrayList<TestGeoObjectInfo>     managedGeoObjectInfos           = new ArrayList<TestGeoObjectInfo>();

  protected ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypeInfos       = new ArrayList<TestGeoObjectTypeInfo>();

  protected ArrayList<TestGeoObjectInfo>     managedGeoObjectInfosExtras     = new ArrayList<TestGeoObjectInfo>();

  protected ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypeInfosExtras = new ArrayList<TestGeoObjectTypeInfo>();

  protected ArrayList<TestHierarchyTypeInfo> managedHierarchyTypeInfos       = new ArrayList<TestHierarchyTypeInfo>();
  
  protected ArrayList<TestHierarchyTypeInfo> managedHierarchyTypeInfosExtras = new ArrayList<TestHierarchyTypeInfo>();

  public TestRegistryAdapterClient           adapter;

  public ClientSession                       adminSession                    = null;

  public ClientRequestIF                     adminClientRequest              = null;

  protected boolean                          includeData;

  public static final String                 ADMIN_USER_NAME                 = "admin";

  public static final String                 ADMIN_PASSWORD                  = "_nm8P4gfdWxGqNRQ#8";

  public static final String                 WKT_DEFAULT_POLYGON             = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";

  public static final String                 WKT_DEFAULT_POINT               = "POINT (110 80)";

  public static final String                 WKT_DEFAULT_MULTIPOLYGON        = "MULTIPOLYGON (((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2)))";

  abstract public String getTestDataKey();

  {
    checkDuplicateClasspathResources();
  }

  public ArrayList<TestOrganizationInfo> getManagedOrganizations()
  {
    ArrayList<TestOrganizationInfo> all = new ArrayList<TestOrganizationInfo>();

    all.addAll(managedOrganizationInfos);
    all.addAll(managedOrganizationInfosExtras);

    return all;
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

  public ArrayList<TestGeoObjectTypeInfo> getManagedGeoObjectTypeExtras()
  {
    return managedGeoObjectTypeInfosExtras;
  }

  public ArrayList<TestHierarchyTypeInfo> getManagedHierarchyTypes()
  {
    ArrayList<TestHierarchyTypeInfo> all = new ArrayList<TestHierarchyTypeInfo>();

    all.addAll(managedHierarchyTypeInfos);
    all.addAll(managedHierarchyTypeInfosExtras);

    return all;
  }

  public ArrayList<TestHierarchyTypeInfo> getManagedHierarchyTypeExtras()
  {
    return managedHierarchyTypeInfosExtras;
  }

  @Request
  public void setUp()
  {
    setUpMetadata();

    setUpInstanceData();
  }

  @Request
  public void cleanUp()
  {
    tearDownMetadata();

    tearDownInstanceData();
  }

  @Request
  public void setUpMetadata()
  {
    // TODO : If you move this call into the 'setupInTrans' method it exposes a
    // bug in Runway which relates to transactions and MdAttributeLocalStructs
    tearDownMetadata();

    setUpOrgsInTrans();
    setUpMetadataInTrans();

    // TODO : Logging in inside of a request isn't good practice
    adminSession = ClientSession.createUserSession(ADMIN_USER_NAME, ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
    adminClientRequest = adminSession.getRequest();
    adapter.setClientRequest(this.adminClientRequest);

    RegistryService.getInstance().refreshMetadataCache();
    adapter.refreshMetadataCache();

    setUpClassRelationships();
    
    Assert.assertTrue(Universal.getByKey("USATestDataState").getAllAncestors("com.runwaysdk.system.gis.geo.USATestDataAdminCodeMetadata").getAll().size() > 0);

    RegistryService.getInstance().refreshMetadataCache();
    adapter.refreshMetadataCache();
  }

  public void setUpClassRelationships()
  {

  }

  @Transaction
  protected void setUpOrgsInTrans()
  {
    for (TestOrganizationInfo org : managedOrganizationInfos)
    {
      org.apply();
    }
  }
  
  @Transaction
  protected void setUpMetadataInTrans()
  {
    for (TestHierarchyTypeInfo ht : managedHierarchyTypeInfos)
    {
      ht.apply();
    }
    
    for (TestGeoObjectTypeInfo uni : managedGeoObjectTypeInfos)
    {
      uni.apply();
    }
  }

  @Request
  public void setUpInstanceData()
  {
    tearDownInstanceData();

    try
    {
      adapter.getIdService().populate(1000);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }

    setUpTestInTrans();

    RegistryService.getInstance().refreshMetadataCache();
    adapter.refreshMetadataCache();

    setUpRelationships();

    RegistryService.getInstance().refreshMetadataCache();
    adapter.refreshMetadataCache();

    setUpAfterApply();
  }

  @Transaction
  protected void setUpTestInTrans()
  {
    if (this.includeData)
    {
      for (TestGeoObjectInfo geo : managedGeoObjectInfos)
      {
        geo.apply(null);
      }
    }
  }

  protected void setUpRelationships()
  {

  }

  protected void setUpAfterApply()
  {

  }

  @Request
  public void tearDownMetadata()
  {
    cleanUpClassInTrans();
  }

  @Transaction
  protected void cleanUpClassInTrans()
  {
    for (TestGeoObjectTypeInfo got : managedGeoObjectTypeInfos)
    {
      if (got.isPersisted())
      {
        new WMSService().deleteDatabaseView(got.getServerObject());
      }
    }

    for (TestGeoObjectTypeInfo got : managedGeoObjectTypeInfosExtras)
    {
      got.delete();
    }

    LinkedList<TestGeoObjectTypeInfo> list = new LinkedList<>(managedGeoObjectTypeInfos);
    Collections.reverse(list);

    for (TestGeoObjectTypeInfo got : list)
    {
      got.delete();
    }
    
    for (TestHierarchyTypeInfo ht : this.getManagedHierarchyTypes())
    {
      ht.delete();
    }
    
    for (TestOrganizationInfo org : this.getManagedOrganizations())
    {
      org.delete();
    }

    if (adminSession != null)
    {
      adminSession.logout();
    }
  }

  @Request
  public void tearDownInstanceData()
  {
    cleanUpTestInTrans();
  }

  @Transaction
  protected void cleanUpTestInTrans()
  {
    if (this.includeData)
    {
      for (TestGeoObjectInfo go : managedGeoObjectInfos)
      {
        go.delete();
      }
    }
    for (TestGeoObjectInfo go : managedGeoObjectInfosExtras)
    {
      go.delete();
    }

    deleteAllActions();
    deleteAllChangeRequests();

    managedGeoObjectInfosExtras = new ArrayList<TestGeoObjectInfo>();
  }

//  private void rebuildAllpaths()
//  {
//    Classifier.getStrategy().initialize(ClassifierIsARelationship.CLASS);
//    Universal.getStrategy().initialize(com.runwaysdk.system.gis.geo.AllowedIn.CLASS);
//    GeoEntity.getStrategy().initialize(com.runwaysdk.system.gis.geo.LocatedIn.CLASS);
//
//    if (new AllowedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
//    {
//      Universal.getStrategy().reinitialize(com.runwaysdk.system.gis.geo.AllowedIn.CLASS);
//    }
//
//    if (new LocatedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
//    {
//      GeoEntity.getStrategy().reinitialize(com.runwaysdk.system.gis.geo.LocatedIn.CLASS);
//    }
//
//    if (new ClassifierIsARelationshipAllPathsTableQuery(new QueryFactory()).getCount() == 0)
//    {
//      Classifier.getStrategy().reinitialize(ClassifierIsARelationship.CLASS);
//    }
//  }

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
//    MdRelationship mdr = MdRelationship.getMdRelationship(relationshipType);
//
//    Assert.assertEquals(mdr.getTypeName(), compare.getCode());
//    Assert.assertEquals(mdr.getDescription().getValue(), compare.getDescription().getValue());
//    Assert.assertEquals(mdr.getDisplayLabel().getValue(), compare.getLabel().getValue());

    // compare.getRootGeoObjectTypes() // TODO
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(this, genKey, testUni);

    info.delete();

    this.managedGeoObjectInfosExtras.add(info);

    return info;
  }

  public TestGeoObjectInfo newTestGeoObjectInfo(String genKey, TestGeoObjectTypeInfo testUni, String wkt)
  {
    TestGeoObjectInfo info = new TestGeoObjectInfo(this, genKey, testUni, wkt, DefaultTerms.GeoObjectStatusTerm.PENDING.code, true);

    info.delete();

    this.managedGeoObjectInfosExtras.add(info);

    return info;
  }

  public TestGeoObjectTypeInfo newTestGeoObjectTypeInfo(String genKey, TestOrganizationInfo organization)
  {
    TestGeoObjectTypeInfo info = new TestGeoObjectTypeInfo(this, genKey, organization);

    info.delete();

    this.managedGeoObjectTypeInfosExtras.add(info);

    return info;
  }

  public TestHierarchyTypeInfo newTestHierarchyTypeInfo(String genKey, TestOrganizationInfo org)
  {
    TestHierarchyTypeInfo info = new TestHierarchyTypeInfo(this, genKey, org);

    info.delete();

    this.managedHierarchyTypeInfosExtras.add(info);

    return info;
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

  public MdClass getMdClassIfExist(String pack, String type)
  {
    MdClassQuery mbq = new MdClassQuery(new QueryFactory());
    mbq.WHERE(mbq.getPackageName().EQ(pack));
    mbq.WHERE(mbq.getTypeName().EQ(type));
    OIterator<? extends MdClass> it = mbq.getIterator();
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
  public void deleteMdClass(String pack, String type)
  {
    MdClass mdBiz = getMdClassIfExist(pack, type);

    if (mdBiz != null)
    {
      if (this.debugMode >= 1)
      {
        System.out.println("Deleting MdClass [" + pack + "." + type + "].");
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
