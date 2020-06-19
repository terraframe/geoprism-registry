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
package net.geoprism.registry;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.ClientSession;
import com.runwaysdk.constants.CommonProperties;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import junit.framework.Assert;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.geoobject.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.RevealExternalSystem;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.graph.VertexExternalIdRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.test.TestDataSet;

public class ExternalSystemTest
{
  // public static RegistryAdapter adapter = null;

  public static RegistryService service      = null;

  public static ClientSession   adminSession = null;

  public static final String    DISTRICT     = "District";

  public static final String    VILLAGE      = "Village";

  public static final String    MOI_ORG_CODE = "MOI";

  @BeforeClass
  public static void setUpClass()
  {
    adminSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { CommonProperties.getDefaultLocale() });

    service = RegistryService.getInstance();
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (adminSession != null)
    {
      adminSession.logout();
    }
  }

  @Before
  public void setUp()
  {
    deleteGeoObjectType(VILLAGE);
    deleteOrganization(MOI_ORG_CODE);
  }

  @After
  public void tearDown() throws IOException
  {
    deleteGeoObjectType(VILLAGE);
    deleteOrganization(MOI_ORG_CODE);
  }

  /**
   * Precondition: Needs to be called within {@link Request} and
   * {@link Transaction} annotations.
   * 
   * @param organizationCode
   * 
   */
  @Request
  public static void deleteOrganization(String organizationCode)
  {
    Organization organization = null;

    try
    {
      organization = Organization.getByKey(organizationCode);
      organization.delete();
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
    }
  }

  /**
   * Precondition: Needs to be called within {@link Request} and
   * {@link Transaction} annotations.
   * 
   * @param organizationCode
   * 
   * @return created and persisted {@link Organization} object.
   */
  @Request
  public static Organization createOrganization(String organizationCode)
  {
    Organization organization = new Organization();
    organization.setCode(organizationCode);
    organization.getDisplayLabel().setDefaultValue(organizationCode);
    organization.getContactInfo().setDefaultValue("Contact Fred at 555...");
    organization.apply();

    return organization;
  }

  @Request
  public static void createGeoObjectType(String organizationCode, String geoObjectTypeCode)
  {
    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
    GeoObjectType province = MetadataFactory.newGeoObjectType(geoObjectTypeCode, GeometryType.POLYGON, new LocalizedValue(geoObjectTypeCode + " DisplayLabel"), new LocalizedValue(""), true, organizationCode, registry);

    ServerGeoObjectType serverGeoObjectType = new ServerGeoObjectTypeConverter().create(province.toJSON().toString());

    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(serverGeoObjectType.getType());
  }

  @Request
  public static void deleteGeoObjectType(String geoObjectTypeCode)
  {
    try
    {
      ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);

      if (type != null)
      {
        type.delete();
      }
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
    }
  }

  @Test
  @Request
  public void testCreateRevealExternalSystem()
  {

    try
    {
      Organization organization = createOrganization(MOI_ORG_CODE);

      RevealExternalSystem system = new RevealExternalSystem();
      system.setId("Test");
      system.setOrganization(organization);
      system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
      system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
      system.apply();

      try
      {
        ExternalSystem test = ExternalSystem.get(system.getOid());

        Assert.assertEquals(system.getId(), test.getId());
      }
      finally
      {
        system.delete();
      }
    }
    finally
    {
      deleteOrganization(MOI_ORG_CODE);
    }
  }

  @Test
  @Request
  public void testAddExternalId()
  {
    ServerGeoObjectService service = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService());

    try
    {
      Organization organization = createOrganization(MOI_ORG_CODE);
      createGeoObjectType(MOI_ORG_CODE, VILLAGE);

      RevealExternalSystem system = new RevealExternalSystem();
      system.setId("Test");
      system.setOrganization(organization);
      system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
      system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
      system.apply();

      try
      {
        String expected = "EXTERNAL ID";

        ServerGeoObjectType type = ServerGeoObjectType.get(VILLAGE);

        ServerGeoObjectIF serverGO = service.newInstance(type);
        serverGO.setCode("00");
        serverGO.setDisplayLabel(createLocalizedValue("Test Label"));
        serverGO.setUid(ServiceFactory.getIdService().getUids(1)[0]);
        serverGO.setStatus(GeoObjectStatus.ACTIVE);
        serverGO.apply(false);

        serverGO.createExternalId(system, expected);

        String actual = serverGO.getExternalId(system);

        Assert.assertEquals(expected, actual);
      }
      finally
      {
        system.delete();
      }
    }
    finally
    {
      deleteGeoObjectType(VILLAGE);
      deleteOrganization(MOI_ORG_CODE);
    }
  }

  @Test
  @Request
  public void testVertexExternalIdRestriction()
  {
    ServerGeoObjectService service = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService());

    try
    {
      Organization organization = createOrganization(MOI_ORG_CODE);
      createGeoObjectType(MOI_ORG_CODE, VILLAGE);

      RevealExternalSystem system = new RevealExternalSystem();
      system.setId("Test");
      system.setOrganization(organization);
      system.getEmbeddedComponent(ExternalSystem.LABEL).setValue("defaultLocale", "Test");
      system.getEmbeddedComponent(ExternalSystem.DESCRIPTION).setValue("defaultLocale", "Test");
      system.apply();

      try
      {
        String externalId = "EXTERNAL ID";

        ServerGeoObjectType type = ServerGeoObjectType.get(VILLAGE);

        ServerGeoObjectIF serverGO = service.newInstance(type);
        serverGO.setCode("00");
        serverGO.setDisplayLabel(createLocalizedValue("Test Label"));
        serverGO.setUid(ServiceFactory.getIdService().getUids(1)[0]);
        serverGO.setStatus(GeoObjectStatus.ACTIVE);
        serverGO.apply(false);

        serverGO.createExternalId(system, externalId);

        VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, new Date());
        query.setRestriction(new ServerExternalIdRestriction(system, externalId));

        ServerGeoObjectIF result = query.getSingleResult();

        Assert.assertNotNull(result);
        Assert.assertEquals(serverGO.getCode(), result.getCode());
      }
      finally
      {
        system.delete();
      }
    }
    finally
    {
      deleteGeoObjectType(VILLAGE);
      deleteOrganization(MOI_ORG_CODE);
    }
  }

  private LocalizedValue createLocalizedValue(String text)
  {
    LocalizedValue value = new LocalizedValue(text);
    value.setValue(LocalizedValue.DEFAULT_LOCALE, text);
    return value;
  }

}
