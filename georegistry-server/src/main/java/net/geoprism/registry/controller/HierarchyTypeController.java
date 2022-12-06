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

import javax.validation.Valid;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

@RestController
@Validated
public class HierarchyTypeController extends RunwaySpringController
{
  public static class HierarchyTypeNodeBody
  {
    @NotEmpty
    String hierarchyCode;

    @NotEmpty
    String parentGeoObjectTypeCode;

    @NotEmpty
    String childGeoObjectTypeCode;

    public String getHierarchyCode()
    {
      return hierarchyCode;
    }

    public void setHierarchyCode(String hierarchyCode)
    {
      this.hierarchyCode = hierarchyCode;
    }

    public String getParentGeoObjectTypeCode()
    {
      return parentGeoObjectTypeCode;
    }

    public void setParentGeoObjectTypeCode(String parentGeoObjectTypeCode)
    {
      this.parentGeoObjectTypeCode = parentGeoObjectTypeCode;
    }

    public String getChildGeoObjectTypeCode()
    {
      return childGeoObjectTypeCode;
    }

    public void setChildGeoObjectTypeCode(String childGeoObjectTypeCode)
    {
      this.childGeoObjectTypeCode = childGeoObjectTypeCode;
    }
  }

  public static class HierarchyTypeInsertBody
  {
    @NotEmpty
    String hierarchyCode;

    @NotEmpty
    String parentGeoObjectTypeCode;

    @NotEmpty
    String middleGeoObjectTypeCode;

    @NotEmpty
    String youngestGeoObjectTypeCode;

    public String getHierarchyCode()
    {
      return hierarchyCode;
    }

    public void setHierarchyCode(String hierarchyCode)
    {
      this.hierarchyCode = hierarchyCode;
    }

    public String getParentGeoObjectTypeCode()
    {
      return parentGeoObjectTypeCode;
    }

    public void setParentGeoObjectTypeCode(String parentGeoObjectTypeCode)
    {
      this.parentGeoObjectTypeCode = parentGeoObjectTypeCode;
    }

    public String getMiddleGeoObjectTypeCode()
    {
      return middleGeoObjectTypeCode;
    }

    public void setMiddleGeoObjectTypeCode(String middleGeoObjectTypeCode)
    {
      this.middleGeoObjectTypeCode = middleGeoObjectTypeCode;
    }

    public String getYoungestGeoObjectTypeCode()
    {
      return youngestGeoObjectTypeCode;
    }

    public void setYoungestGeoObjectTypeCode(String youngestGeoObjectTypeCode)
    {
      this.youngestGeoObjectTypeCode = youngestGeoObjectTypeCode;
    }
  }

  public static class HierarchyTypeInheritedBody
  {
    @NotEmpty
    String hierarchyTypeCode;
    
    @NotEmpty
    String geoObjectTypeCode;
    
    public String getHierarchyTypeCode()
    {
      return hierarchyTypeCode;
    }
    
    public void setHierarchyTypeCode(String hierarchyTypeCode)
    {
      this.hierarchyTypeCode = hierarchyTypeCode;
    }
    
    public String getGeoObjectTypeCode()
    {
      return geoObjectTypeCode;
    }
    
    public void setGeoObjectTypeCode(String geoObjectTypeCode)
    {
      this.geoObjectTypeCode = geoObjectTypeCode;
    }
  }
  
  public static class HierarchyTypeSetInheritedBody extends HierarchyTypeInheritedBody
  {
    @NotEmpty
    String inheritedHierarchyTypeCode;

    public String getInheritedHierarchyTypeCode()
    {
      return inheritedHierarchyTypeCode;
    }

    public void setInheritedHierarchyTypeCode(String inheritedHierarchyTypeCode)
    {
      this.inheritedHierarchyTypeCode = inheritedHierarchyTypeCode;
    }
  }

  public static final String API_PATH = "hierarchytype";

  @Autowired
  private RegistryService    registryService;

  @GetMapping(API_PATH + "/groupedTypes")
  public ResponseEntity<String> getHierarchyGroupedTypes()
  {
    JsonArray ja = ServiceFactory.getHierarchyService().getHierarchyGroupedTypes(this.getSessionId());

    return new ResponseEntity<String>(ja.toString(), HttpStatus.OK);
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
  /**
   * @param hierarchyCode
   * @param parentGeoObjectTypeCode
   * @param childGeoObjectTypeCode
   * @return
   */
  @PostMapping(API_PATH + "/add")
  public ResponseEntity<String> addToHierarchy(@Valid
  @RequestBody HierarchyTypeNodeBody body)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().addToHierarchy(this.getSessionId(), body.hierarchyCode, body.parentGeoObjectTypeCode, body.childGeoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(this.getSessionId());

    JsonObject response = ht.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
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
  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<String> removeFromHierarchy(@Valid
  @RequestBody HierarchyTypeNodeBody body)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().removeFromHierarchy(this.getSessionId(), body.hierarchyCode, body.parentGeoObjectTypeCode, body.childGeoObjectTypeCode, true);
    CustomSerializer serializer = this.registryService.serializer(this.getSessionId());

    JsonObject response = ht.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
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
  @PostMapping(API_PATH + "/insertBetweenTypes")
  public ResponseEntity<String> insertBetweenTypes(@Valid @RequestBody HierarchyTypeInsertBody body)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().insertBetweenTypes(this.getSessionId(), body.hierarchyCode, body.parentGeoObjectTypeCode, body.middleGeoObjectTypeCode, body.youngestGeoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(this.getSessionId());

    JsonObject response = ht.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
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
  @PostMapping(API_PATH + "/setInherited")
  public ResponseEntity<String> setInheritedHierarchy(@Valid @RequestBody HierarchyTypeSetInheritedBody body)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().setInheritedHierarchy(this.getSessionId(), body.hierarchyTypeCode, body.inheritedHierarchyTypeCode, body.geoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(this.getSessionId());

    JsonObject response = ht.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
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
  @PostMapping(API_PATH + "/removeInherited")
  public ResponseEntity<String> removeInheritedHierarchy(@Valid @RequestBody HierarchyTypeInheritedBody body)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().removeInheritedHierarchy(this.getSessionId(), body.hierarchyTypeCode, body.geoObjectTypeCode);
    CustomSerializer serializer = this.registryService.serializer(this.getSessionId());

    JsonObject response = ht.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }
}
