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
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;

import net.geoprism.registry.cache.ServerMetadataCache;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.hierarchy.HierarchyService;
import net.geoprism.registry.permission.GeoObjectPermissionService;
import net.geoprism.registry.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.permission.GeoObjectRelationshipPermissionService;
import net.geoprism.registry.permission.GeoObjectRelationshipPermissionServiceIF;
import net.geoprism.registry.permission.GeoObjectTypePermissionService;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.GeoObjectTypeRelationshipPermissionService;
import net.geoprism.registry.permission.GeoObjectTypeRelationshipPermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionService;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.permission.OrganizationPermissionService;
import net.geoprism.registry.permission.RolePermissionService;

public class ServiceFactory
{
  private static ServiceFactory                        instance;

  private ConversionService                            cs;

  private RegistryIdService                            idService;

  private RegistryService                              registryService;

  private RegistryAdapter                              adapter;

  private AccountService                               accountService;

  private HierarchyService                             hierarchyService;

  private GeoObjectPermissionServiceIF                 goPermissionServ;

  private HierarchyTypePermissionServiceIF             hierarchyPermServ;

  private OrganizationPermissionService                orgServ;

  private ServerGeoObjectService                       serverGoService;

  private GeoObjectRelationshipPermissionServiceIF     goRelPermissionServ;

  private GeoObjectTypeRelationshipPermissionServiceIF goTypeRelPermissionServ;

  private GeoObjectTypePermissionServiceIF             goTypePermissionServ;

  private RolePermissionService                        rolePermissionServ;

  private ServerMetadataCache                          metadataCache;

  private void initialize()
  {
    this.registryService = new RegistryService();
    this.cs = new ConversionService();
    this.idService = new RegistryIdService();

    this.adapter = new RegistryAdapterServer(this.idService);

    this.accountService = new AccountService();

    this.goPermissionServ = new GeoObjectPermissionService();

    this.serverGoService = new ServerGeoObjectService(goPermissionServ);

    this.hierarchyService = new HierarchyService();

    this.orgServ = new OrganizationPermissionService();

    this.hierarchyPermServ = new HierarchyTypePermissionService();

    this.goRelPermissionServ = new GeoObjectRelationshipPermissionService();

    this.goTypeRelPermissionServ = new GeoObjectTypeRelationshipPermissionService();

    this.goTypePermissionServ = new GeoObjectTypePermissionService();

    this.rolePermissionServ = new RolePermissionService();

    this.metadataCache = new ServerMetadataCache(this.adapter);
    this.metadataCache.rebuild();

    this.registryService.initialize(this.adapter);
  }

  public static synchronized ServiceFactory getInstance()
  {
    if (instance == null)
    {
      instance = new ServiceFactory();
      instance.initialize();
    }

    return instance;
  }

  public static RegistryAdapter getAdapter()
  {
    return ServiceFactory.getInstance().adapter;
  }

  public static RegistryService getRegistryService()
  {
    return ServiceFactory.getInstance().registryService;
  }

  public static AccountService getAccountService()
  {
    return ServiceFactory.getInstance().accountService;
  }

  public static ConversionService getConversionService()
  {
    return ServiceFactory.getInstance().cs;
  }

  public static RegistryIdService getIdService()
  {
    return ServiceFactory.getInstance().idService;
  }

  public static HierarchyService getHierarchyService()
  {
    return ServiceFactory.getInstance().hierarchyService;
  }

  public static ServerGeoObjectService getGeoObjectService()
  {
    return ServiceFactory.getInstance().serverGoService;
  }

  public static GeoObjectPermissionServiceIF getGeoObjectPermissionService()
  {
    return ServiceFactory.getInstance().goPermissionServ;
  }

  public static GeoObjectRelationshipPermissionServiceIF getGeoObjectRelationshipPermissionService()
  {
    return ServiceFactory.getInstance().goRelPermissionServ;
  }

  public static GeoObjectTypeRelationshipPermissionServiceIF getGeoObjectTypeRelationshipPermissionService()
  {
    return ServiceFactory.getInstance().goTypeRelPermissionServ;
  }

  public static OrganizationPermissionService getOrganizationPermissionService()
  {
    return ServiceFactory.getInstance().orgServ;
  }

  public static HierarchyTypePermissionServiceIF getHierarchyPermissionService()
  {
    return ServiceFactory.getInstance().hierarchyPermServ;
  }

  public static GeoObjectTypePermissionServiceIF getGeoObjectTypePermissionService()
  {
    return ServiceFactory.getInstance().goTypePermissionServ;
  }

  public static RolePermissionService getRolePermissionService()
  {
    return ServiceFactory.getInstance().rolePermissionServ;
  }

  public static ServerMetadataCache getMetadataCache()
  {
    return ServiceFactory.getInstance().metadataCache;
  }
}
