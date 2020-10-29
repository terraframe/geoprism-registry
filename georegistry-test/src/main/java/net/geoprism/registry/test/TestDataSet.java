/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.ClientSession;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.business.rbac.UserDAOIF;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.constants.LocalProperties;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.resource.ClasspathResource;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.VaultFileQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdClass;
import com.runwaysdk.system.metadata.MdClassQuery;

import net.geoprism.GeoprismUser;
import net.geoprism.GeoprismUserQuery;
import net.geoprism.gis.geoserver.GeoserverFacade;
import net.geoprism.gis.geoserver.NullGeoserverService;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.ontology.ClassifierQuery;
import net.geoprism.registry.IdRecord;
import net.geoprism.registry.IdRecordQuery;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.Organization;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.UserInfoQuery;
import net.geoprism.registry.action.AbstractAction;
import net.geoprism.registry.action.AbstractActionQuery;
import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.WMSService;

abstract public class TestDataSet
{
  public static interface RequestExecutor
  {
    public void execute(ClientRequestIF request, TestRegistryAdapterClient adapter) throws Exception;
  }

  public static final String                 ADMIN_USER_NAME                 = "admin";

  public static final String                 ADMIN_PASSWORD                  = "_nm8P4gfdWxGqNRQ#8";

  public static final TestUserInfo           ADMIN_USER                      = new TestUserInfo(ADMIN_USER_NAME, ADMIN_PASSWORD, null, null);

  public static final String                 WKT_DEFAULT_POLYGON             = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";

  public static final String                 WKT_DEFAULT_POINT               = "POINT (110 80)";

  public static final String                 WKT_DEFAULT_MULTIPOLYGON        = "MULTIPOLYGON (((1 1,5 1,5 5,1 5,1 1),(2 2, 3 2, 3 3, 2 3,2 2)))";

  public static final String                 WKT_POLYGON_2                   = "MULTIPOLYGON(((1 1,10 1,10 10,1 10,1 1),(2 2, 3 2, 3 3, 2 3,2 2)))";

  protected int                              debugMode                       = 0;

  protected ArrayList<TestOrganizationInfo>  managedOrganizationInfos        = new ArrayList<TestOrganizationInfo>();

  protected ArrayList<TestOrganizationInfo>  managedOrganizationInfosExtras  = new ArrayList<TestOrganizationInfo>();

  protected ArrayList<TestGeoObjectInfo>     managedGeoObjectInfos           = new ArrayList<TestGeoObjectInfo>();

  protected ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypeInfos       = new ArrayList<TestGeoObjectTypeInfo>();

  protected ArrayList<TestGeoObjectInfo>     managedGeoObjectInfosExtras     = new ArrayList<TestGeoObjectInfo>();

  protected ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypeInfosExtras = new ArrayList<TestGeoObjectTypeInfo>();

  protected ArrayList<TestHierarchyTypeInfo> managedHierarchyTypeInfos       = new ArrayList<TestHierarchyTypeInfo>();

  protected ArrayList<TestHierarchyTypeInfo> managedHierarchyTypeInfosExtras = new ArrayList<TestHierarchyTypeInfo>();

  protected ArrayList<TestUserInfo>          managedUsers                    = new ArrayList<TestUserInfo>();

  public TestRegistryAdapterClient           adapter                         = new TestRegistryAdapterClient();

  public ClientSession                       clientSession                   = null;

  public ClientRequestIF                     clientRequest                   = null;

  public static Date                         DEFAULT_OVER_TIME_DATE;

  static
  {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.clear();
    cal.set(2020, Calendar.APRIL, 4);

    DEFAULT_OVER_TIME_DATE = cal.getTime();
  }

  abstract public String getTestDataKey();

  public TestDataSet()
  {
    checkDuplicateClasspathResources();
    LocalProperties.setSkipCodeGenAndCompile(true);
    GeoserverFacade.setService(new NullGeoserverService());
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

  public ArrayList<TestUserInfo> getManagedUsers()
  {
    ArrayList<TestUserInfo> all = new ArrayList<TestUserInfo>();

    all.addAll(managedUsers);

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

  // @Request
  // public void setUp()
  // {
  // setUpMetadata();
  //
  // setUpInstanceData();
  // }
  //
  // @Request
  // public void cleanUp()
  // {
  // tearDownMetadata();
  //
  // tearDownInstanceData();
  // }

  public void logIn()
  {
    this.logIn(null);
  }

  public void logIn(TestUserInfo user)
  {
    if (user == null)
    {
      this.clientSession = ClientSession.createUserSession(ADMIN_USER_NAME, ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });
      this.clientRequest = clientSession.getRequest();
      this.adapter.setClientRequest(this.clientRequest);
    }
    else
    {
      this.clientSession = ClientSession.createUserSession(user.getUsername(), user.getPassword(), new Locale[] { CommonProperties.getDefaultLocale() });
      this.clientRequest = clientSession.getRequest();
      this.adapter.setClientRequest(this.clientRequest);
    }

    adapter.refreshMetadataCache();

    TestDataSet.populateAdapterIds(user, adapter);
  }

  public void logOut()
  {
    if (clientSession != null && clientRequest != null && clientRequest.isLoggedIn())
    {
      clientSession.logout();
    }
  }

  @Request
  public void setUpMetadata()
  {
    tearDownMetadata();

    setUpOrgsInTrans();
    setUpMetadataInTrans();

    RegistryService.getInstance().refreshMetadataCache();

    setUpClassRelationships();

    RegistryService.getInstance().refreshMetadataCache();
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

    for (TestUserInfo user : managedUsers)
    {
      user.apply();
    }
  }

  @Request
  public void setUpInstanceData()
  {
    tearDownInstanceData();

    setUpTestInTrans();

    RegistryService.getInstance().refreshMetadataCache();

    setUpRelationships();

    RegistryService.getInstance().refreshMetadataCache();

    setUpAfterApply();
  }

  @Transaction
  protected void setUpTestInTrans()
  {
    for (TestGeoObjectInfo geo : managedGeoObjectInfos)
    {
      geo.apply();
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
    for (TestGeoObjectTypeInfo got : managedGeoObjectTypeInfosExtras)
    {
      got.delete();
    }

    LinkedList<TestGeoObjectTypeInfo> list = new LinkedList<>(managedGeoObjectTypeInfos);
    Collections.reverse(list);

    for (TestHierarchyTypeInfo ht : this.getManagedHierarchyTypes())
    {
      ht.delete();
    }

    for (TestGeoObjectTypeInfo got : list)
    {
      got.delete();
    }

    for (TestOrganizationInfo org : this.getManagedOrganizations())
    {
      org.delete();
    }

    for (TestUserInfo user : this.getManagedUsers())
    {
      user.delete();
    }
  }

  @Request
  public void reloadPermissions()
  {
    SessionFacade.getSessionForRequest(this.clientRequest.getSessionId()).reloadPermissions();
  }

  public void tearDownInstanceData()
  {
    tearDownInstanceDataInRequest();
  }

  @Request
  public void tearDownInstanceDataInRequest()
  {
    cleanUpTestInTrans();
  }

  @Transaction
  protected void cleanUpTestInTrans()
  {
    // for (TestGeoObjectInfo go : managedGeoObjectInfos)
    // {
    // go.delete();
    // }
    // for (TestGeoObjectInfo go : managedGeoObjectInfosExtras)
    // {
    // go.delete();
    // }

    deleteAllGeoObjects();

    deleteAllActions();
    deleteAllChangeRequests();

    managedGeoObjectInfosExtras = new ArrayList<TestGeoObjectInfo>();
  }

  @Request
  private void deleteAllGeoObjects()
  {
    ArrayList<TestGeoObjectTypeInfo> managedGeoObjectTypes = new ArrayList<>(this.getManagedGeoObjectTypes());

    Collections.reverse(managedGeoObjectTypes);

    for (TestGeoObjectTypeInfo type : managedGeoObjectTypes)
    {
      ServerGeoObjectType got = type.getServerObject(true);

      if (got == null)
      {
        continue;
      }

      MdVertexDAOIF mdVertex = got.getMdVertex();

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT FROM " + mdVertex.getDBClassName());

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

      List<VertexObject> vObjects = query.getResults();

      for (VertexObject vObject : vObjects)
      {
        VertexServerGeoObject serverGo = new VertexServerGeoObject(got, vObject);

        QueryFactory qf = new QueryFactory();
        BusinessQuery bq = qf.businessQuery(type.getServerObject().getUniversal().getMdBusiness().definesType());
        bq.WHERE(bq.aCharacter(DefaultAttribute.CODE.getName()).EQ(serverGo.getCode()));
        OIterator<? extends Business> bit = bq.getIterator();
        try
        {
          while (bit.hasNext())
          {
            Business biz = bit.next();

            biz.delete();
          }
        }
        finally
        {
          bit.close();
        }

        vObject.delete();

        TestDataSet.deleteGeoEntity(serverGo.getCode());
      }
    }

    for (TestGeoObjectInfo go : this.getManagedGeoObjects())
    {
      go.clean();
    }
  }

  // private void rebuildAllpaths()
  // {
  // Classifier.getStrategy().initialize(ClassifierIsARelationship.CLASS);
  // Universal.getStrategy().initialize(com.runwaysdk.system.gis.geo.AllowedIn.CLASS);
  // GeoEntity.getStrategy().initialize(com.runwaysdk.system.gis.geo.LocatedIn.CLASS);
  //
  // if (new AllowedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
  // {
  // Universal.getStrategy().reinitialize(com.runwaysdk.system.gis.geo.AllowedIn.CLASS);
  // }
  //
  // if (new LocatedInAllPathsTableQuery(new QueryFactory()).getCount() == 0)
  // {
  // GeoEntity.getStrategy().reinitialize(com.runwaysdk.system.gis.geo.LocatedIn.CLASS);
  // }
  //
  // if (new ClassifierIsARelationshipAllPathsTableQuery(new
  // QueryFactory()).getCount() == 0)
  // {
  // Classifier.getStrategy().reinitialize(ClassifierIsARelationship.CLASS);
  // }
  // }

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

  @Request
  public static void assertEqualsHierarchyType(String relationshipType, HierarchyType compare)
  {
    // MdRelationship mdr = MdRelationship.getMdRelationship(relationshipType);
    //
    // Assert.assertEquals(mdr.getTypeName(), compare.getCode());
    // Assert.assertEquals(mdr.getDescription().getValue(),
    // compare.getDescription().getValue());
    // Assert.assertEquals(mdr.getDisplayLabel().getValue(),
    // compare.getLabel().getValue());

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
    TestGeoObjectInfo info = new TestGeoObjectInfo(genKey, testUni, wkt, DefaultTerms.GeoObjectStatusTerm.PENDING.code, true);

    info.delete();

    this.managedGeoObjectInfosExtras.add(info);

    return info;
  }

  public TestGeoObjectTypeInfo newTestGeoObjectTypeInfo(String genKey, TestOrganizationInfo organization)
  {
    TestGeoObjectTypeInfo info = new TestGeoObjectTypeInfo(genKey, organization);

    info.delete();

    this.managedGeoObjectTypeInfosExtras.add(info);

    return info;
  }

  public TestHierarchyTypeInfo newTestHierarchyTypeInfo(String genKey, TestOrganizationInfo org)
  {
    TestHierarchyTypeInfo info = new TestHierarchyTypeInfo(genKey, org);

    info.delete();

    this.managedHierarchyTypeInfosExtras.add(info);

    return info;
  }

  @Request
  public static void deleteGeoEntity(String key)
  {
    GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
    geq.WHERE(geq.getKeyName().EQ(key));
    OIterator<? extends GeoEntity> git = geq.getIterator();
    try
    {
      while (git.hasNext())
      {
        GeoEntity ge = git.next();

        ge.delete();
      }
    }
    finally
    {
      git.close();
    }
  }

  public static Classifier getClassifierIfExist(String classifierId)
  {
    ClassifierQuery query = new ClassifierQuery(new QueryFactory());
    query.WHERE(query.getClassifierId().EQ(classifierId));
    OIterator<? extends Classifier> it = query.getIterator();
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
  public static GeoprismUser createUser(String username, String password, String email, String[] roleNameArray)
  {
    GeoprismUser geoprismUser = new GeoprismUser();
    geoprismUser.setUsername(username);
    geoprismUser.setPassword(password);
    geoprismUser.setFirstName(username);
    geoprismUser.setLastName(username);
    geoprismUser.setEmail(email);
    geoprismUser.apply();

    if (roleNameArray != null)
    {
      List<Roles> newRoles = new LinkedList<Roles>();

      Set<String> roleIdSet = new HashSet<String>();
      for (String roleName : roleNameArray)
      {
        Roles role = Roles.findRoleByName(roleName);

        roleIdSet.add(role.getOid());
        newRoles.add(role);
      }

      UserDAOIF user = UserDAO.get(geoprismUser.getOid());

      Set<String> organizationSet = new HashSet<String>();
      for (Roles role : newRoles)
      {
        RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
        roleDAO.assignMember(user);

        RegistryRole registryRole = new RegistryRoleConverter().build(role);
        if (registryRole != null)
        {
          String organizationCode = registryRole.getOrganizationCode();

          if (organizationCode != null && !organizationCode.equals("") && !organizationSet.contains(organizationCode))
          {
            Organization organization = Organization.getByCode(organizationCode);
            organization.addUsers(geoprismUser).apply();
            organizationSet.add(organizationCode);
          }
        }
      }
    }

    UserInfo info = new UserInfo();
    info.setGeoprismUser(geoprismUser);
    info.apply();

    return geoprismUser;
  }

  @Request
  public static void deleteUser(String username)
  {
    QueryFactory qf = new QueryFactory();

    ValueQuery vq = new ValueQuery(qf);

    UserInfoQuery uiq = new UserInfoQuery(qf);

    GeoprismUserQuery guq = new GeoprismUserQuery(qf);

    vq.SELECT(uiq.getOid("userInfoOid"));
    vq.SELECT(guq.getOid("geoprismUserOid"));

    vq.WHERE(guq.getUsername().EQ(username));
    vq.AND(uiq.getGeoprismUser().EQ(guq));

    OIterator<? extends ValueObject> it = vq.getIterator();

    try
    {
      while (it.hasNext())
      {
        ValueObject vo = it.next();

        UserInfo ui = UserInfo.get(vo.getValue("userInfoOid"));
        GeoprismUser gu = GeoprismUser.get(vo.getValue("geoprismUserOid"));

        // Delete all referenced IdRecords
        IdRecordQuery irq = new IdRecordQuery(new QueryFactory());
        irq.WHERE(irq.getOwner().EQ(gu));
        OIterator<? extends IdRecord> reqit = irq.getIterator();
        try
        {
          while (reqit.hasNext())
          {
            reqit.next().delete();
          }
        }
        finally
        {
          reqit.close();
        }

        // Delete all referenced VaultFiles
        VaultFileQuery vfq = new VaultFileQuery(new QueryFactory());
        vfq.WHERE(vfq.getOwner().EQ(gu));
        OIterator<? extends VaultFile> vfit = vfq.getIterator();
        try
        {
          while (vfit.hasNext())
          {
            vfit.next().delete();
          }
        }
        finally
        {
          vfit.close();
        }

        ui.delete();
        gu.delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Request
  public static void deleteClassifier(String classifierId)
  {
    Classifier clazz = getClassifierIfExist(classifierId);

    if (clazz != null)
    {
      clazz.delete();
    }
  }

  public static MdClass getMdClassIfExist(String pack, String type)
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
  public static void deleteMdClass(String pack, String type)
  {
    MdClass mdBiz = getMdClassIfExist(pack, type);

    if (mdBiz != null)
    {
      mdBiz.delete();
    }
  }

  @Request
  public static Universal getUniversalIfExist(String universalId)
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
  public static void deleteUniversal(String code)
  {
    Universal uni = getUniversalIfExist(code);

    if (uni != null)
    {
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

  @Request
  public static void deleteExternalSystems(String systemId)
  {
    try
    {
      final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
      MdAttributeDAOIF attribute = mdVertex.definesAttribute(ExternalSystem.ID);

      StringBuilder builder = new StringBuilder();
      builder.append("SELECT FROM " + mdVertex.getDBClassName());

      builder.append(" WHERE " + attribute.getColumnName() + " = :id");

      final GraphQuery<ExternalSystem> query = new GraphQuery<ExternalSystem>(builder.toString());

      query.setParameter("id", systemId);

      List<ExternalSystem> list = query.getResults();

      for (ExternalSystem es : list)
      {
        TestDataSet.deleteExternalIds(es);

        es.delete(false);
      }
    }
    catch (net.geoprism.registry.DataNotFoundException ex)
    {
      // Do nothing
    }
  }

  @Request
  public static void deleteExternalIds(ExternalSystem system)
  {
    final MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + mdEdge.getDBClassName());

    if (system != null)
    {
      builder.append(" WHERE out = :system");
    }

    final GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(builder.toString());

    if (system != null)
    {
      query.setParameter("system", system.getRID());
    }

    List<EdgeObject> edges = query.getResults();

    for (EdgeObject edge : edges)
    {
      edge.delete();
    }
  }

  @Request
  public static void refreshTerms(AttributeTermType attribute)
  {
    attribute.setRootTerm(new TermConverter(TermConverter.buildClassifierKeyFromTermCode(attribute.getRootTerm().getCode())).build());
  }

  public static TestAttributeTypeInfo createAttribute(String name, String label, TestGeoObjectTypeInfo got, String type)
  {
    AttributeType at = AttributeType.factory(name, new LocalizedValue(label), new LocalizedValue("Description for " + name), type, false, false, false);

    String attributeTypeJSON = at.toJSON().toString();

    at = got.getServerObject().createAttributeType(attributeTypeJSON);

    return new TestAttributeTypeInfo(at, got);
  }

  public static TestAttributeTypeInfo createTermAttribute(String name, String label, TestGeoObjectTypeInfo got, Term attrRoot)
  {
    final String type = AttributeTermType.TYPE;

    AttributeTermType att = (AttributeTermType) AttributeType.factory(name, new LocalizedValue(label), new LocalizedValue("Description for " + name), type, false, false, false);
    if (attrRoot != null)
    {
      att.setRootTerm(attrRoot);
    }

    String attributeTypeJSON = att.toJSON().toString();

    att = (AttributeTermType) got.getServerObject().createAttributeType(attributeTypeJSON);

    return new TestAttributeTypeInfo(att, got);
  }

  public static Term createTerm(TestAttributeTypeInfo termAttr, String classifierId, String displayLabel)
  {
    Classifier parentTerm = TestDataSet.getClassifierIfExist(termAttr.getRootTerm().getCode());

    Classifier child = TestDataSet.getClassifierIfExist(classifierId);
    if (child == null)
    {
      child = new Classifier();
      child.setClassifierId(classifierId);
      child.setClassifierPackage(parentTerm.getKey());
      child.getDisplayLabel().setDefaultValue(displayLabel);
      child.apply();

      child.addLink(parentTerm, ClassifierIsARelationship.CLASS).apply();
    }

    return new TermConverter(child.getKeyName()).build();
  }

  public static Term createAttributeRootTerm(TestGeoObjectTypeInfo got, TestAttributeTypeInfo attr)
  {
    MdBusiness mdBiz = got.getServerObject().getMdBusiness();

    Classifier mdBizClassy = TermConverter.buildIfNotExistdMdBusinessClassifier(mdBiz);

    Classifier classifier = TermConverter.buildIfNotExistAttribute(mdBiz, attr.getAttributeName(), mdBizClassy);

    return new TermConverter(classifier.getKeyName()).build();
  }

  public static void runAsUser(TestUserInfo user, RequestExecutor executor)
  {
    ClientSession session = null;

    try
    {
      session = ClientSession.createUserSession(user.getUsername(), user.getPassword(), new Locale[] { CommonProperties.getDefaultLocale() });

      ClientRequestIF request = session.getRequest();

      TestRegistryAdapterClient adapter = new TestRegistryAdapterClient();
      adapter.setClientRequest(request);

      adapter.refreshMetadataCache();

      try
      {
        executor.execute(request, adapter);
      }
      catch (RuntimeException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
    finally
    {
      if (session != null)
      {
        session.logout();
      }
    }
  }

  public static void populateAdapterIds(TestUserInfo user, TestRegistryAdapterClient adapter)
  {
    boolean isRAorRM = false;

    if (user == null)
    {
      isRAorRM = true;
    }
    else
    {
      for (String roleName : user.getRoleNameArray())
      {
        if (RegistryRole.Type.isRM_Role(roleName) || RegistryRole.Type.isRA_Role(roleName))
        {
          isRAorRM = true;
          break;
        }
      }
    }

    if (isRAorRM)
    {
      try
      {
        adapter.getIdService().populate(1000);
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }
    }
  }

}
