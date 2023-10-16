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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.TreeNode;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.service.GeoObjectServiceIF;
import net.geoprism.registry.service.HierarchyTypeServiceIF;
import net.geoprism.registry.service.RegistryComponentService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.spring.JsonObjectDeserializer;
import net.geoprism.registry.spring.NullableDateDeserializer;

@RestController
@Validated
public class GeoObjectController extends RunwaySpringController
{
  public static final String API_PATH = "geoobject";

  public static class RelationshipBody
  {
    @NotEmpty
    private String parentCode;

    @NotEmpty
    private String parentTypeCode;

    @NotEmpty
    private String childCode;

    @NotEmpty
    private String childTypeCode;

    @NotEmpty
    private String hierarchyCode;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date   startDate;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date   endDate;

    public String getParentCode()
    {
      return parentCode;
    }

    public void setParentCode(String parentCode)
    {
      this.parentCode = parentCode;
    }

    public String getParentTypeCode()
    {
      return parentTypeCode;
    }

    public void setParentTypeCode(String parentTypeCode)
    {
      this.parentTypeCode = parentTypeCode;
    }

    public String getChildCode()
    {
      return childCode;
    }

    public void setChildCode(String childCode)
    {
      this.childCode = childCode;
    }

    public String getChildTypeCode()
    {
      return childTypeCode;
    }

    public void setChildTypeCode(String childTypeCode)
    {
      this.childTypeCode = childTypeCode;
    }

    public String getHierarchyCode()
    {
      return hierarchyCode;
    }

    public void setHierarchyCode(String hierarchyCode)
    {
      this.hierarchyCode = hierarchyCode;
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }
  }

  public static class GeoObjectBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject geoObject;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date       startDate;

    @JsonDeserialize(using = NullableDateDeserializer.class)
    private Date       endDate;

    public JsonObject getGeoObject()
    {
      return geoObject;
    }

    public void setGeoObject(JsonObject geoObject)
    {
      this.geoObject = geoObject;
    }

    public Date getStartDate()
    {
      return startDate;
    }

    public void setStartDate(Date startDate)
    {
      this.startDate = startDate;
    }

    public Date getEndDate()
    {
      return endDate;
    }

    public void setEndDate(Date endDate)
    {
      this.endDate = endDate;
    }

  }

  public static class TypeCodeBody
  {
    @NotEmpty
    private String typeCode;

    public String getTypeCode()
    {
      return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
      this.typeCode = typeCode;
    }
  }

  @Autowired
  private RegistryComponentService service;

  @Autowired
  private GeoObjectServiceIF       objectService;
  
  @Autowired
  private HierarchyTypeServiceIF      hierService;

  /**
   * Returns a paginated response of all GeoObjects matching the provided
   * criteria.
   **/
  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getAll(@NotEmpty
  @RequestParam String typeCode,
      @NotEmpty
      @RequestParam String hierarchyCode, @RequestParam(required = false) Long updatedSince, @RequestParam(required = false) Boolean includeLevel, @RequestParam(required = false) String format, @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) String externalSystemId)
  {
    Date dUpdatedSince = null;
    if (updatedSince != null)
    {
      dUpdatedSince = new Date(updatedSince);
    }

    JsonObject jo = this.objectService.getAll(this.getSessionId(), typeCode, hierarchyCode, dUpdatedSince, includeLevel, format, externalSystemId, pageNumber, pageSize);

    return new ResponseEntity<String>(jo.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/exists-at-range")
  public ResponseEntity<String> doesGeoObjectExistAtRange(@RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate, @NotEmpty
  @RequestParam String typeCode,
      @NotEmpty
      @RequestParam String code)
  {
    JsonObject stats = this.objectService.doesGeoObjectExistAtRange(this.getSessionId(), startDate, endDate, typeCode, code);

    return new ResponseEntity<String>(stats.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/has-duplicate-label")
  public ResponseEntity<String> hasDuplicateLabel(@RequestParam Date date, @NotEmpty
  @RequestParam String typeCode, @RequestParam(required = false) String code,
      @NotEmpty
      @RequestParam String label)
  {
    JsonObject stats = this.objectService.hasDuplicateLabel(this.getSessionId(), date, typeCode, code, label);

    return new ResponseEntity<String>(stats.toString(), HttpStatus.OK);
  }

  /**
   * Returns an array of (label, entityId) pairs that under the given
   * parent/hierarchy and have the given label.
   * 
   * @throws ParseException
   *
   * @pre
   * @post
   *
   * @returns @throws
   **/
  @GetMapping(API_PATH + "/suggestions")
  public ResponseEntity<String> getGeoObjectSuggestions(@RequestParam(required = false) String text, @NotEmpty
  @RequestParam String type, @RequestParam(required = false) String parent, @RequestParam(required = false) String parentTypeCode, @RequestParam(required = false) String hierarchy, @RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate)
  {
    JsonArray response = this.service.getGeoObjectSuggestions(this.getSessionId(), text, type, parent, parentTypeCode, hierarchy, startDate, endDate);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-hierarchies")
  public ResponseEntity<String> getHierarchiesForGeoObject(@NotEmpty
  @RequestParam String code,
      @NotEmpty
      @RequestParam String typeCode,
      @NotNull
      @RequestParam Date date)
  {
    JsonArray response = this.service.getHierarchiesForGeoObject(this.getSessionId(), code, typeCode, date);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-hierarchies-over-time")
  public ResponseEntity<String> getHierarchiesForGeoObjectOverTime(@NotEmpty
  @RequestParam String code,
      @NotEmpty
      @RequestParam String typeCode)
  {
    JsonArray response = hierService.getHierarchiesForGeoObjectOverTime(this.getSessionId(), code, typeCode);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/search")
  public ResponseEntity<String> search(@NotEmpty
  @RequestParam String typeCode,
      @NotEmpty
      @RequestParam String text,
      @NotNull
      @RequestParam Date date) throws JSONException, ParseException
  {
    List<GeoObject> results = this.service.search(this.getSessionId(), typeCode, text, date);

    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonArray response = new JsonArray();

    for (GeoObject result : results)
    {
      response.add(result.toJSON(serializer));
    }

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-bounds")
  public ResponseEntity<String> getGeoObjectBounds(@NotEmpty
  @RequestParam String code,
      @NotEmpty
      @RequestParam String typeCode) throws JSONException
  {
    GeoObject geoObject = this.service.getGeoObjectByCode(this.getSessionId(), code, typeCode, null);

    String bounds = this.service.getGeoObjectBounds(this.getSessionId(), geoObject);

    return new ResponseEntity<String>(bounds.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/business-objects")
  public ResponseEntity<String> getBusinessObjects(@NotEmpty
  @RequestParam String typeCode,
      @NotEmpty
      @RequestParam String code,
      @NotEmpty
      @RequestParam String businessTypeCode)
  {
    JsonArray objects = this.objectService.getBusinessObjects(this.getSessionId(), typeCode, code, businessTypeCode);

    return new ResponseEntity<String>(objects.toString(), HttpStatus.OK);
  }

  /**
   * Update a new GeoObject in the Common Geo-Registry
   *
   * @pre
   * @post
   *
   * @param geoObject
   *          in GeoJSON format to be updated.
   * @throws ParseException
   *
   * @returns
   * @throws //TODO
   **/
  @PostMapping(RegistryUrls.GEO_OBJECT_UPDATE)
  public ResponseEntity<String> updateGeoObject(@Valid
  @RequestBody GeoObjectBody body)
  {
    GeoObject geoObject = this.service.updateGeoObject(this.getSessionId(), body.geoObject.toString(), body.startDate, body.endDate);
    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * Creates a new GeoObject in the Common Geo-Registry
   *
   * @pre
   * @post
   *
   * @param geoObject
   *          in GeoJSON format to be created.
   * @throws ParseException
   *
   * @returns
   * @throws //TODO
   **/
  @PostMapping(RegistryUrls.GEO_OBJECT_CREATE)
  public ResponseEntity<String> createGeoObject(@Valid
  @RequestBody GeoObjectBody body)
  {
    GeoObject geoObject = this.service.createGeoObject(this.getSessionId(), body.geoObject.toString(), body.startDate, body.endDate);
    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * TODO : Not part of the official API (yet). Currently used for the GeoObject
   * editing widget when creating a new GeoObject. The return value is a custom
   * serialized json format because ParentTreeNode doesn't quite fit our needs
   * (It allows for a GeoObject but not a GeoObjectType)
   * 
   * @param request
   * @param typeCode
   * @return
   */
  @PostMapping(RegistryUrls.GEO_OBJECT_NEW_INSTANCE)
  public ResponseEntity<String> newGeoObjectInstance(@Valid
  @RequestBody TypeCodeBody body)
  {
    String resp = this.service.newGeoObjectInstance2(this.getSessionId(), body.typeCode);

    return new ResponseEntity<String>(resp, HttpStatus.OK);
  }

  /**
   * Get children of the given GeoObject
   *
   * @param parentCode
   *          Code of the parent object for which the call fetches children.
   * @param parentTypeCode
   *          Code of the parent object's type for which the call fetches
   *          children.
   * @param hierarchyCode
   *          Code of the hierarchy to search for children. If none is provided,
   *          all are returned.
   * @param date
   *          Date for which the relationships must exist
   * @param childrenTypes
   *          An array of GeoObjectType names of the types of children
   *          GeoObjects to fetch. If blank then return children of all types.
   * @param recursive
   *          TRUE if recursive children of the given parent should be returned,
   *          FALSE if only single level children should be returned.
   * 
   * @returns {@link TreeNode} children
   **/
  @GetMapping(RegistryUrls.GEO_OBJECT_GET_CHILDREN)
  public ResponseEntity<String> getChildGeoObjects(@NotEmpty
  @RequestParam String parentCode,
      @NotEmpty
      @RequestParam String parentTypeCode, @RequestParam(required = false) String hierarchyCode, @RequestParam(required = false) Date date, @RequestParam(required = false) String childrenTypes,
      @NotNull
      @RequestParam Boolean recursive)
  {
    String[] aChildTypes = null;

    if (childrenTypes != null)
    {
      JSONArray jaChildTypes = new JSONArray(childrenTypes);

      aChildTypes = new String[jaChildTypes.length()];
      for (int i = 0; i < jaChildTypes.length(); i++)
      {
        aChildTypes[i] = jaChildTypes.getString(i);
      }
    }

    TreeNode tn = this.service.getChildGeoObjects(this.getSessionId(), parentCode, parentTypeCode, hierarchyCode, aChildTypes, recursive, date);

    return new ResponseEntity<String>(tn.toJSON().toString(), HttpStatus.OK);
  }

  /**
   * Get the parents of the given GeoObject
   *
   * @param childCode
   *          Code of the child object for which the call fetches children.
   * @param childTypeCode
   *          Code of the child object's type for which the call fetches
   *          children.
   * @param hierarchyCode
   *          Code of the hierarchy to search for children. If none is provided,
   *          all are returned.
   * @param date
   *          Date for which the relationships must exist
   * @param childrenTypes
   *          An array of GeoObjectType names of the types of children
   *          GeoObjects to fetch. If blank then return children of all types.
   * @param recursive
   *          TRUE if recursive parents of the given child should be returned,
   *          FALSE if only single level children should be returned.
   * 
   * @returns {@link TreeNode} children
   **/
  @GetMapping(RegistryUrls.GEO_OBJECT_GET_PARENTS)
  public ResponseEntity<String> getParentGeoObjects(@NotEmpty
  @RequestParam String childCode,
      @NotEmpty
      @RequestParam String childTypeCode, @RequestParam(required = false) String hierarchyCode, @RequestParam(required = false) Date date, @RequestParam(required = false) String parentTypes,
      @NotNull
      @RequestParam Boolean recursive)
  {
    String[] aParentTypes = null;

    if (parentTypes != null)
    {
      JSONArray jaParentTypes = new JSONArray(parentTypes);

      aParentTypes = new String[jaParentTypes.length()];
      for (int i = 0; i < jaParentTypes.length(); i++)
      {
        aParentTypes[i] = jaParentTypes.getString(i);
      }
    }

    TreeNode tn = this.service.getParentGeoObjects(this.getSessionId(), childCode, childTypeCode, hierarchyCode, aParentTypes, recursive, recursive, date);

    return new ResponseEntity<String>(tn.toJSON().toString(), HttpStatus.OK);
  }

  /**
   * Creates a relationship between @parentUid and @childUid.
   *
   * @pre Both the parent and child have already been persisted / applied
   * @post A relationship will exist between @parent and @child
   *
   * @returns ParentTreeNode The new node which was created with the provided
   *          parent.
   * @param startDate
   *          TODO
   * @param endDate
   *          TODO
   */
  @PostMapping(RegistryUrls.GEO_OBJECT_ADD_CHILD)
  public ResponseEntity<String> addChild(@Valid
  @RequestBody RelationshipBody body)
  {
    ParentTreeNode pn = this.objectService.addChild(this.getSessionId(), body.parentCode, body.parentTypeCode, body.childCode, body.childTypeCode, body.hierarchyCode, body.startDate, body.endDate);

    return new ResponseEntity<String>(pn.toJSON().toString(), HttpStatus.OK);
  }

  /**
   * Removes a relationship between @parentUid and @childUid.
   *
   * @pre Both the parent and child have already been persisted / applied
   * @post A relationship will not exist between @parent and @child
   *
   * @returns
   */
  @PostMapping(RegistryUrls.GEO_OBJECT_REMOVE_CHILD)
  public ResponseEntity<Void> removeChild(@Valid
  @RequestBody RelationshipBody body)
  {
    this.objectService.removeChild(this.getSessionId(), body.parentCode, body.parentTypeCode, body.childCode, body.childTypeCode, body.hierarchyCode, body.startDate, body.endDate);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  /**
   * Get list of valid UIDs for use in creating new GeoObjects. The Common
   * Geo-Registry will only accept newly created GeoObjects with a UID that was
   * issued from the Common GeoRegistry.
   *
   * @pre @post
   *
   * @param amount
   *          Number of globally unique ids that the Common Geo-Registry will
   *          issue to the mobile device.
   *
   * @returns @throws
   **/
  @GetMapping(RegistryUrls.GEO_OBJECT_GET_UIDS)
  public ResponseEntity<String> getUIDs(@NotNull
  @RequestParam Integer amount)
  {
    String[] ids = this.service.getUIDS(this.getSessionId(), amount);

    JsonArray response = Arrays.asList(ids).stream().collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> {
    });

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * Returns a GeoObject with the given uid.
   *
   * @pre @post
   *
   * @param uid
   *          The UID of the GeoObject.
   *
   * @returns a GeoObject in GeoJSON format with the given uid. @throws
   **/
  @GetMapping(RegistryUrls.GEO_OBJECT_GET)
  public ResponseEntity<String> getGeoObject(@NotEmpty
  @RequestParam String id,
      @NotEmpty
      @RequestParam String typeCode, @RequestParam(required = false) Date date)
  {
    GeoObject geoObject = this.service.getGeoObject(this.getSessionId(), id, typeCode, date);

    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * Returns a GeoObject with the given code.
   *
   * @pre @post
   *
   * @param uid
   *          The UID of the GeoObject.
   *
   * @returns a GeoObject in GeoJSON format with the given uid. @throws
   **/
  @GetMapping(RegistryUrls.GEO_OBJECT_GET_CODE)
  public ResponseEntity<String> getGeoObjectByCode(@NotEmpty
  @RequestParam String code,
      @NotEmpty
      @RequestParam String typeCode, @RequestParam(required = false) Date date)
  {
    GeoObject geoObject = this.service.getGeoObjectByCode(this.getSessionId(), code, typeCode, date);
    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject response = geoObject.toJSON(serializer);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

}
