/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.controller;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

@Controller(url = "hierarchy")
public class HierarchyController
{
  private RegistryService registryService;

  public HierarchyController()
  {
    this.registryService = RegistryService.getInstance();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "groupedTypes")
  public ResponseIF getHierarchyGroupedTypes(ClientRequestIF request)
  {
    JsonArray ja = ServiceFactory.getHierarchyService().getHierarchyGroupedTypes(request.getSessionId());

    return new RestBodyResponse(ja);
  }

  /**
   * Adds the {@link GeoObjectType} with the given child code to the parent
   * {@link GeoObjectType} with the given code for the given
   * {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyCode
   *          code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode
   *          parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode
   *          child {@link GeoObjectType}.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "add")
  public ResponseIF addToHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "parentGeoObjectTypeCode") String parentGeoObjectTypeCode, @RequestParamter(name = "childGeoObjectTypeCode") String childGeoObjectTypeCode)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().addToHierarchy(request.getSessionId(), hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(ht.toJSON(serializer));
  }

  /**
   * Removes the {@link GeoObjectType} with the given child code from the parent
   * {@link GeoObjectType} with the given code for the given
   * {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyCode
   *          code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode
   *          parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode
   *          child {@link GeoObjectType}.
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF removeFromHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "parentGeoObjectTypeCode") String parentGeoObjectTypeCode, @RequestParamter(name = "childGeoObjectTypeCode") String childGeoObjectTypeCode)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().removeFromHierarchy(request.getSessionId(), hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, true);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(ht.toJSON(serializer));
  }

  /**
   * Inserts the {@link GeoObjectType} 'middleGeoObjectTypeCode' into the
   * hierarchy as the child of 'parentGeoObjectTypeCode' and the new parent for
   * 'youngestGeoObjectTypeCode'. If an existing parent/child relationship
   * already exists between 'youngestGeoObjectTypeCode' and
   * 'parentgeoObjectTypeCode', it will first be removed.
   * 
   * @param sessionId
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType}
   * @param parentGeoObjectTypeCode
   *          parent {@link GeoObjectType}.
   * @param middleGeoObjectTypeCode
   *          middle child {@link GeoObjectType} after this method returns
   * @param youngestGeoObjectTypeCode
   *          youngest child {@link GeoObjectType} after this method returns
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "insertBetweenTypes")
  public ResponseIF insertBetweenTypes(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "parentGeoObjectTypeCode") String parentGeoObjectTypeCode, @RequestParamter(name = "middleGeoObjectTypeCode") String middleGeoObjectTypeCode, @RequestParamter(name = "youngestGeoObjectTypeCode") String youngestGeoObjectTypeCode)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().insertBetweenTypes(request.getSessionId(), hierarchyCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(ht.toJSON(serializer));
  }

  /**
   * Modifies a hierarchy to inherit from another hierarchy at the given
   * GeoObjectType
   * 
   * @param request
   *          Session Request
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType} being modified.
   * @param inheritedHierarchyTypeCode
   *          code of the {@link HierarchyType} being inherited.
   * @param geoObjectTypeCode
   *          code of the root {@link GeoObjectType}.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "setInherited")
  public ResponseIF setInheritedHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyTypeCode") String hierarchyTypeCode, @RequestParamter(name = "inheritedHierarchyTypeCode") String inheritedHierarchyTypeCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().setInheritedHierarchy(request.getSessionId(), hierarchyTypeCode, inheritedHierarchyTypeCode, geoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(ht.toJSON(serializer));
  }

  /**
   * Modifies a hierarchy to remove inheritance from another hierarchy for the
   * given root
   * 
   * @param sessionId
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType} being modified.
   * @param geoObjectTypeCode
   *          code of the root {@link GeoObjectType}.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "removeInherited")
  public ResponseIF removeInheritedHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyTypeCode") String hierarchyTypeCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().removeInheritedHierarchy(request.getSessionId(), hierarchyTypeCode, geoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(ht.toJSON(serializer));
  }
}
