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
package net.geoprism.registry.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.TreeNode;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ChangeRequestService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

@Controller(url = RegistryUrls.REGISTRY_CONTROLLER_URL)
public class RegistryController
{
  public static final String JSP_DIR   = "/WEB-INF/";

  public static final String INDEX_JSP = "net/geoprism/registry/index.jsp";

  private RegistryService    registryService;

  public RegistryController()
  {
    this.registryService = RegistryService.getInstance();
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF manage()
  {
    return new ViewResponse(JSP_DIR + INDEX_JSP);
  }

  /**
   * Submits a change request to the GeoRegistry. These actions will be reviewed
   * by an Administrator and if the actions are approved they may be executed
   * and accepted as formal changes to the GeoRegistry.
   * 
   * @param request
   * @param uid
   * @return
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.SUBMIT_CHANGE_REQUEST)
  public ResponseIF submitChangeRequest(ClientRequestIF request, @RequestParamter(name = RegistryUrls.SUBMIT_CHANGE_REQUEST_PARAM_ACTIONS) String actions) throws JSONException
  {
    new ChangeRequestService().submitChangeRequest(request.getSessionId(), actions);

    return new RestResponse();
  }
  
  /**
   * Returns an OauthServer configuration with the specified id. If an id is not provided, this endpoint will return all configurations (in your organization).
   * 
   * @param request
   * @param id
   * @return A json array of serialized OauthServer configurations.
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "oauth/get")
  public ResponseIF oauthGetAll(ClientRequestIF request, @RequestParamter(name = "id") String id) throws JSONException
  {
    String json = this.registryService.oauthGetAll(request.getSessionId(), id);

    return new RestBodyResponse(json);
  }
  
  /**
   * Returns information which is available to public users (without permissions) which will allow them to login as an oauth user.
   * 
   * @param request
   * @param id
   * @return A json array of OauthServer configurations with only publicly available information.
   * @throws JSONException
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "oauth/get-public")
  public ResponseIF oauthGetPublic(ClientRequestIF request, @RequestParamter(name = "id") String id) throws JSONException
  {
    String json = this.registryService.oauthGetPublic(request.getSessionId(), id);

    return new RestBodyResponse(json);
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
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_GET)
  public ResponseIF getGeoObject(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARAM_ID) String id, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE) String typeCode) throws JSONException
  {
    GeoObject geoObject = this.registryService.getGeoObject(request.getSessionId(), id, typeCode);

    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TIME_GET)
  public ResponseIF getGeoObjectOverTime(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TIME_GET_PARAM_ID) String id, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TIME_GET_PARAM_TYPE_CODE) String typeCode) throws JSONException
  {
    GeoObjectOverTime geoObject = this.registryService.getGeoObjectOverTime(request.getSessionId(), id, typeCode);

    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TIME_GET_CODE)
  public ResponseIF getGeoObjectOverTimeByCode(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TIME_GET_CODE_PARAM_CODE) String code, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TIME_GET_CODE_PARAM_TYPE_CODE) String typeCode) throws JSONException
  {
    GeoObjectOverTime geoObject = this.registryService.getGeoObjectOverTimeByCode(request.getSessionId(), code, typeCode);

    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "geoobject-time/newGeoObjectInstance")
  public ResponseIF newGeoObjectOverTime(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_NEW_INSTANCE_PARAM_TYPE_CODE) String typeCode)
  {
    String resp = this.registryService.newGeoObjectInstanceOverTime(request.getSessionId(), typeCode);

    return new RestBodyResponse(resp);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobject/get-bounds")
  public ResponseIF getGeoObjectBounds(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_CODE) String code, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE) String typeCode) throws JSONException
  {
    GeoObject geoObject = this.registryService.getGeoObjectByCode(request.getSessionId(), code, typeCode);

    String bounds = this.registryService.getGeoObjectBounds(request.getSessionId(), geoObject);

    return new RestBodyResponse(bounds);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobject-time/get-bounds")
  public ResponseIF getGeoObjectBoundsAtDate(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_CODE) String code, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE) String typeCode, @RequestParamter(name = "date") String date) throws JSONException, ParseException
  {
    GeoObject geoObject = this.registryService.getGeoObjectByCode(request.getSessionId(), code, typeCode);

    Date forDate = null;

    if (date != null && date.length() > 0)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      forDate = format.parse(date);
    }

    String bounds = this.registryService.getGeoObjectBoundsAtDate(request.getSessionId(), geoObject, forDate);

    return new RestBodyResponse(bounds);
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
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_GET_CODE)
  public ResponseIF getGeoObjectByCode(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_CODE) String code, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CODE_PARAM_TYPE_CODE) String typeCode) throws JSONException
  {
    GeoObject geoObject = this.registryService.getGeoObjectByCode(request.getSessionId(), code, typeCode);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
  }

  /**
   * Creates a new GeoObject in the Common Geo-Registry
   *
   * @pre
   * @post
   *
   * @param geoObject
   *          in GeoJSON format to be created.
   *
   * @returns
   * @throws //TODO
   **/
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_CREATE)
  public ResponseIF createGeoObject(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_CREATE_PARAM_GEOOBJECT) String jGeoObj)
  {
    GeoObject geoObject = this.registryService.createGeoObject(request.getSessionId(), jGeoObj);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TIME_CREATE)
  public ResponseIF createGeoObjectOverTime(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TIME_CREATE_PARAM_GEOOBJECT) String jGeoObj)
  {
    GeoObjectOverTime geoObject = this.registryService.createGeoObjectOverTime(request.getSessionId(), jGeoObj);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
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
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_NEW_INSTANCE)
  public ResponseIF newGeoObjectInstance(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_NEW_INSTANCE_PARAM_TYPE_CODE) String typeCode)
  {
    String resp = this.registryService.newGeoObjectInstance2(request.getSessionId(), typeCode);

    return new RestBodyResponse(resp);
  }

  /**
   * Update a new GeoObject in the Common Geo-Registry
   *
   * @pre
   * @post
   *
   * @param geoObject
   *          in GeoJSON format to be updated.
   *
   * @returns
   * @throws //TODO
   **/
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_UPDATE)
  public ResponseIF updateGeoObject(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_UPDATE_PARAM_GEOOBJECT) String jGeoObj)
  {
    GeoObject geoObject = this.registryService.updateGeoObject(request.getSessionId(), jGeoObj);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(geoObject.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TIME_UPDATE)
  public ResponseIF updateGeoObjectOverTime(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TIME_UPDATE_PARAM_GEOOBJECT) String jGeoObj)
  {
    GeoObjectOverTime goTime = this.registryService.updateGeoObjectOverTime(request.getSessionId(), jGeoObj);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(goTime.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_ADD_ATTRIBUTE)
  public ResponseIF createAttributeType(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_ADD_ATTRIBUTE_PARAM) String geoObjectTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_ADD_ATTRIBUTE_TYPE_PARAM) String attributeType)
  {
    AttributeType attrType = this.registryService.createAttributeType(request.getSessionId(), geoObjectTypeCode, attributeType);

    return new RestBodyResponse(attrType.toJSON());
  }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE)
  // public ResponseIF updateAttributeType(ClientRequestIF request,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE_PARAM) String geoObjTypeId,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE_TYPE_PARAM) String
  // attributeType)
  // {
  // AttributeType attrType =
  // this.registryService.updateAttributeType(request.getSessionId(),
  // geoObjTypeId, attributeType);
  //
  // return new RestBodyResponse(attrType.toJSON());
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE)
  // public ResponseIF deleteAttributeType(ClientRequestIF request,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE_PARAM) String geoObjTypeId,
  // @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE_TYPE_PARAM) String
  // attributeName)
  // {
  // this.registryService.deleteAttributeType(request.getSessionId(),
  // geoObjTypeId, attributeName);
  //
  // return new RestResponse();
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM)
  // public ResponseIF createTerm(ClientRequestIF request, @RequestParamter(name
  // = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARENT_PARAM) String
  // parentTermCode, @RequestParamter(name =
  // RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARAM) String termJSON)
  // {
  // Term term = this.registryService.createTerm(request.getSessionId(),
  // parentTermCode, termJSON);
  //
  // return new RestBodyResponse(term.toJSON());
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM)
  // public ResponseIF updateTerm(ClientRequestIF request, @RequestParamter(name
  // = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM_PARAM) String termJSON)
  // {
  // Term term = this.registryService.updateTerm(request.getSessionId(),
  // termJSON);
  //
  // return new RestBodyResponse(term.toJSON());
  // }
  //
  // @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON,
  // url=RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM)
  // public ResponseIF deleteTerm(ClientRequestIF request, @RequestParamter(name
  // = RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM_PARAM) String termCode)
  // {
  // this.registryService.deleteTerm(request.getSessionId(), termCode);
  //
  // return new RestResponse();
  // }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE)
  public ResponseIF updateAttributeType(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE_PARAM) String geoObjTypeId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE_TYPE_PARAM) String attributeType)
  {
    AttributeType attrType = this.registryService.updateAttributeType(request.getSessionId(), geoObjTypeId, attributeType);

    return new RestBodyResponse(attrType.toJSON());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE)
  public ResponseIF deleteAttributeType(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE_PARAM) String geoObjTypeId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE_TYPE_PARAM) String attributeName)
  {
    this.registryService.deleteAttributeType(request.getSessionId(), geoObjTypeId, attributeName);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM)
  public ResponseIF createTerm(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARENT_PARAM) String parentTermCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARAM) String termJSON)
  {
    Term term = this.registryService.createTerm(request.getSessionId(), parentTermCode, termJSON);

    return new RestBodyResponse(term.toJSON());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM)
  public ResponseIF updateTerm(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARENT_PARAM) String parentTermCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM_PARAM) String termJSON)
  {
    Term term = this.registryService.updateTerm(request.getSessionId(), parentTermCode, termJSON);

    return new RestBodyResponse(term.toJSON());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM)
  public ResponseIF deleteTerm(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM_PARENT_PARAM) String parentTermCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM_PARAM) String termCode)
  {
    this.registryService.deleteTerm(request.getSessionId(), parentTermCode, termCode);

    return new RestResponse();
  }

  /**
   * Get children of the given GeoObject
   *
   * @pre @post
   *
   * @param parentUid
   *          UID of the parent object for which the call fetches
   *          children. @param childrentTypes An array of GeoObjectType names of
   *          the types of children GeoObjects to fetch. If blank then return
   *          children of all types. @param recursive TRUE if recursive children
   *          of the given parent with the given types should be returned, FALSE
   *          if only single level children should be returned.
   * 
   * @returns @throws
   **/
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_GET_CHILDREN)
  public ResponseIF getChildGeoObjects(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENTID) String parentId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_PARENT_TYPE_CODE) String parentTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_CHILDREN_TYPES) String childrenTypes, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_CHILDREN_PARAM_RECURSIVE) Boolean recursive)
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

    TreeNode tn = this.registryService.getChildGeoObjects(request.getSessionId(), parentId, parentTypeCode, aChildTypes, recursive);

    return new RestBodyResponse(tn.toJSON());
  }

  /**
   * Get parents of the given GeoObject
   *
   * @pre @post
   *
   * @param childUid
   *          UID of the child object for which the call fetches parents. @param
   *          parentTypes An array of GeoObjectType names of the types of parent
   *          GeoObjects to fetch. If blank then return parents of all
   *          types. @param recursive TRUE if recursive parents of the given
   *          parent with the given types should be returned, FALSE if only
   *          single level parents should be returned.
   * @throws ParseException
   * 
   * @returns @throws
   **/
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_GET_PARENTS)
  public ResponseIF getParentGeoObjects(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILDID) String childId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_CHILD_TYPE_CODE) String childTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_PARENT_TYPES) String parentTypes, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARENTS_PARAM_RECURSIVE) Boolean recursive, @RequestParamter(name = "date") String date) throws ParseException
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

    Date forDate = null;

    if (date != null && date.length() > 0)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      forDate = format.parse(date);
    }

    TreeNode tn = this.registryService.getParentGeoObjects(request.getSessionId(), childId, childTypeCode, aParentTypes, recursive, forDate);

    return new RestBodyResponse(tn.toJSON());
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
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_GET_UIDS)
  public ResponseIF getUIDs(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_UIDS_PARAM_AMOUNT) Integer amount)
  {
    String[] ids = this.registryService.getUIDS(request.getSessionId(), amount);

    return new RestBodyResponse(ids);
  }

  /**
   * Creates a relationship between @parentUid and @childUid.
   *
   * @pre Both the parent and child have already been persisted / applied
   * @post A relationship will exist between @parent and @child
   *
   * @returns ParentTreeNode The new node which was created with the provided
   *          parent.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_ADD_CHILD)
  public ResponseIF addChild(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENTID) String parentId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENT_TYPE_CODE) String parentTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILDID) String childId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILD_TYPE_CODE) String childTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_HIERARCHY_CODE) String hierarchyRef)
  {
    ParentTreeNode pn = ServiceFactory.getGeoObjectService().addChild(request.getSessionId(), parentId, parentTypeCode, childId, childTypeCode, hierarchyRef);

    return new RestBodyResponse(pn.toJSON());
  }

  /**
   * Removes a relationship between @parentUid and @childUid.
   *
   * @pre Both the parent and child have already been persisted / applied
   * @post A relationship will not exist between @parent and @child
   *
   * @returns
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_REMOVE_CHILD)
  public ResponseIF removeChild(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENTID) String parentId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_PARENT_TYPE_CODE) String parentTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILDID) String childId, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_CHILD_TYPE_CODE) String childTypeCode, @RequestParamter(name = RegistryUrls.GEO_OBJECT_ADD_CHILD_PARAM_HIERARCHY_CODE) String hierarchyRef)
  {
    ServiceFactory.getGeoObjectService().removeChild(request.getSessionId(), parentId, parentTypeCode, childId, childTypeCode, hierarchyRef);

    return new RestResponse();
  }

  /**
   * Returns an array of {@link GeoOjectType} objects that define the given list
   * of types.
   *
   * @pre @post
   *
   * @param types
   *          A serialized json array of GeoObjectType codes. If blank then all
   *          GeoObjectType objects are returned.
   * @param hierarchies
   *          A serialized json array of HierarchyType codes. If blank then
   *          GeoObjectTypes belonging to all hierarchies are returned.
   *
   * @returns @throws
   **/
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_GET_ALL)
  public ResponseIF getGeoObjectTypes(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_GET_ALL_PARAM_TYPES) String types, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_GET_ALL_PARAM_HIERARCHIES) String hierarchies, @RequestParamter(name = "context") String context)
  {
    String[] aTypes = null;
    if (types != null)
    {
      JSONArray jaTypes = new JSONArray(types);

      aTypes = new String[jaTypes.length()];
      for (int i = 0; i < jaTypes.length(); i++)
      {
        aTypes[i] = jaTypes.getString(i);
      }
    }

    String[] aHierarchies = null;
    if (hierarchies != null)
    {
      JSONArray jaHierarchies = new JSONArray(hierarchies);

      aHierarchies = new String[jaHierarchies.length()];
      for (int i = 0; i < jaHierarchies.length(); i++)
      {
        aHierarchies[i] = jaHierarchies.getString(i);
      }
    }

    PermissionContext pContext = PermissionContext.get(context);

    GeoObjectType[] gots = this.registryService.getGeoObjectTypes(request.getSessionId(), aTypes, aHierarchies, pContext);

    JsonArray jarray = new JsonArray();
    for (int i = 0; i < gots.length; ++i)
    {
      JsonObject jo = this.registryService.serialize(request.getSessionId(), gots[i]);
      
      jarray.add(jo);
    }

    return new RestBodyResponse(jarray);
  }

  /**
   * Returns an array of (label, code) pairs that define all of the geo object
   * types in the system.
   *
   * @pre
   * @post
   *
   * @returns @throws
   **/
  @Endpoint(url = "geoobjecttype/list-types", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF listGeoObjectTypes(ClientRequestIF request, @RequestParamter(name = "includeAbstractTypes") Boolean includeAbstractTypes)
  {
    GeoObjectType[] gots = this.registryService.getGeoObjectTypes(request.getSessionId(), null, null, PermissionContext.READ);

    Arrays.sort(gots, new Comparator<GeoObjectType>()
    {
      @Override
      public int compare(GeoObjectType o1, GeoObjectType o2)
      {
        return o1.getLabel().getValue().compareTo(o2.getLabel().getValue());
      }
    });

    JsonArray jarray = new JsonArray();

    for (int i = 0; i < gots.length; ++i)
    {
      GeoObjectType geoObjectType = gots[i];

      if (!geoObjectType.getCode().equals("ROOT") && ( includeAbstractTypes || !geoObjectType.getIsAbstract() ))
      {
        JsonObject type = new JsonObject();
        type.addProperty("label", geoObjectType.getLabel().getValue());
        type.addProperty("code", geoObjectType.getCode());
        type.addProperty("orgCode", geoObjectType.getOrganizationCode());
        type.addProperty("superTypeCode", geoObjectType.getSuperTypeCode());

        jarray.add(type);
      }
    }

    return new RestBodyResponse(jarray);
  }

  /**
   * Creates a {@link GeoObjectType} from the given JSON.
   * 
   * @param request
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be created.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_CREATE)
  public ResponseIF createGeoObjectType(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_TYPE_CREATE_PARAM_GOT) String gtJSON) throws JSONException
  {
    GeoObjectType geoObjectType = this.registryService.createGeoObjectType(request.getSessionId(), gtJSON);

    return new RestBodyResponse(this.registryService.serialize(request.getSessionId(), geoObjectType));
  }

  /**
   * Updates the given {@link GeoObjectType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be updated.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_UPDATE)
  public ResponseIF updateGeoObjectType(ClientRequestIF request, @RequestParamter(name = "gtJSON") String gtJSON) throws JSONException
  {
    GeoObjectType geoObjectType = this.registryService.updateGeoObjectType(request.getSessionId(), gtJSON);

    return new RestBodyResponse(this.registryService.serialize(request.getSessionId(), geoObjectType));
  }

  /**
   * Deletes the {@link GeoObjectType} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link GeoObjectType} to delete.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.GEO_OBJECT_TYPE_DELETE)
  public RestResponse deleteGeoObjectType(ClientRequestIF request, @RequestParamter(name = "code") String code) throws JSONException
  {
    this.registryService.deleteGeoObjectType(request.getSessionId(), code);

    return new RestResponse();
  }

  /**
   * Returns an array of {@link HierarchyType} that define the given list of
   * types. If no types are provided then all will be returned.
   * 
   * @param types
   *          A serialized json array of HierarchyType codes that will be
   *          retrieved.
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.HIERARCHY_TYPE_GET_ALL)
  public ResponseIF getHierarchyTypes(ClientRequestIF request, @RequestParamter(name = "types") String types, @RequestParamter(name = "context") String context)
  {
    String[] aTypes = null;
    if (types != null)
    {
      JSONArray jaTypes = new JSONArray(types);

      aTypes = new String[jaTypes.length()];
      for (int i = 0; i < jaTypes.length(); i++)
      {
        aTypes[i] = jaTypes.getString(i);
      }
    }

    PermissionContext pContext = PermissionContext.get(context);

    HierarchyType[] hts = ServiceFactory.getHierarchyService().getHierarchyTypes(request.getSessionId(), aTypes, pContext);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    JsonArray jarray = new JsonArray();
    for (int i = 0; i < hts.length; ++i)
    {
      jarray.add(hts[i].toJSON(serializer));
    }

    return new RestBodyResponse(jarray);
  }

  /**
   * Create the {@link HierarchyType} from the given JSON.
   * 
   * @param sessionId
   * @param htJSON
   *          JSON of the {@link HierarchyType} to be created.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.HIERARCHY_TYPE_CREATE)
  public ResponseIF createHierarchyType(ClientRequestIF request, @RequestParamter(name = "htJSON") String htJSON)
  {
    HierarchyType hierarchyType = ServiceFactory.getHierarchyService().createHierarchyType(request.getSessionId(), htJSON);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(hierarchyType.toJSON(serializer));
  }

  /**
   * Updates the given {@link HierarchyType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link HierarchyType} to be updated.
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.HIERARCHY_TYPE_UPDATE)
  public ResponseIF updateHierarchyType(ClientRequestIF request, @RequestParamter(name = "htJSON") String htJSON)
  {
    HierarchyType hierarchyType = ServiceFactory.getHierarchyService().updateHierarchyType(request.getSessionId(), htJSON);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(hierarchyType.toJSON(serializer));
  }

  /**
   * Deletes the {@link HierarchyType} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link HierarchyType} to delete.
   * @return
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.HIERARCHY_TYPE_DELETE)
  public RestResponse deleteHierarchyType(ClientRequestIF request, @RequestParamter(name = "code") String code)
  {
    ServiceFactory.getHierarchyService().deleteHierarchyType(request.getSessionId(), code);

    return new RestResponse();
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
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = RegistryUrls.HIERARCHY_TYPE_ADD)
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
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = RegistryUrls.HIERARCHY_TYPE_REMOVE)
  public ResponseIF removeFromHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "parentGeoObjectTypeCode") String parentGeoObjectTypeCode, @RequestParamter(name = "childGeoObjectTypeCode") String childGeoObjectTypeCode)
  {
    HierarchyType ht = ServiceFactory.getHierarchyService().removeFromHierarchy(request.getSessionId(), hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode, true);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(ht.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobjecttype/get-ancestors")
  public ResponseIF getTypeAncestors(ClientRequestIF request, @RequestParamter(name = "code") String code, @RequestParamter(name = "hierarchyCode") String hierarchyCode, @RequestParamter(name = "includeInheritedTypes") Boolean includeInheritedTypes)
  {
    if (includeInheritedTypes == null)
    {
      includeInheritedTypes = false;
    }

    JsonArray response = new JsonArray();

    List<GeoObjectType> ancestors = this.registryService.getAncestors(request.getSessionId(), code, hierarchyCode, includeInheritedTypes);

    for (GeoObjectType ancestor : ancestors)
    {
      JsonObject object = new JsonObject();
      object.addProperty("label", ancestor.getLabel().getValue());
      object.addProperty("code", ancestor.getCode());

      response.add(object);
    }

    return new RestBodyResponse(response.toString());
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobjecttype/get-hierarchies")
  public ResponseIF getHierarchiesForType(ClientRequestIF request, @RequestParamter(name = "code") String code, @RequestParamter(name = "includeTypes") Boolean includeTypes)
  {
    JsonArray response = ServiceFactory.getHierarchyService().getHierarchiesForType(request.getSessionId(), code, includeTypes);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobjecttype/get-subtype-hierarchies")
  public ResponseIF getHierarchiesForSubtypes(ClientRequestIF request, @RequestParamter(name = "code") String code)
  {
    JsonArray response = ServiceFactory.getHierarchyService().getHierarchiesForSubtypes(request.getSessionId(), code);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobject/get-hierarchies")
  public ResponseIF getHierarchiesForGeoObject(ClientRequestIF request, @RequestParamter(name = "code") String code, @RequestParamter(name = "typeCode") String typeCode)
  {
    JsonArray response = this.registryService.getHierarchiesForGeoObject(request.getSessionId(), code, typeCode);

    return new RestBodyResponse(response);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobject/get-hierarchies-over-time")
  public ResponseIF getHierarchiesForGeoObjectOverTime(ClientRequestIF request, @RequestParamter(name = "code") String code, @RequestParamter(name = "typeCode") String typeCode)
  {
    JsonArray response = ServiceFactory.getHierarchyService().getHierarchiesForGeoObjectOverTime(request.getSessionId(), code, typeCode);

    return new RestBodyResponse(response);
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
  @Endpoint(url = "geoobject/suggestions", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getGeoObjectSuggestions(ClientRequestIF request, @RequestParamter(name = "text") String text, @RequestParamter(name = "type") String type, @RequestParamter(name = "parent") String parent, @RequestParamter(name = "parentTypeCode") String parentTypeCode, @RequestParamter(name = "hierarchy") String hierarchy, @RequestParamter(name = "date") String date)
  {
    Date forDate = null;

    if (date != null && date.length() > 0)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      try
      {
        forDate = format.parse(date);
      }
      catch (ParseException e)
      {
        throw new ProgrammingErrorException(e);
      }
    }

    JsonArray response = this.registryService.getGeoObjectSuggestions(request.getSessionId(), text, type, parent, parentTypeCode, hierarchy, forDate);

    return new RestBodyResponse(response);
  }

  /**
   * Returns a map with a list of all the types, all of the hierarchies, and all
   * of the locales currently installed in the system. This endpoint is used to
   * populate the hierarchy manager.
   * 
   * @param request
   * @return
   */
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "init")
  public ResponseIF init(ClientRequestIF request)
  {
    return new RestBodyResponse(this.registryService.initHierarchyManager(request.getSessionId()));
  }

  /**
   * Submit scheduled job conflict.
   * 
   * @param sessionId
   * @param conflict
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "registry/submit-conflict")
  public ResponseIF submitDataConflictResolution(ClientRequestIF request, @RequestParamter(name = "conflict") String conflict)
  {

    // TODO: set this method up

    HierarchyType hierarchyType = ServiceFactory.getHierarchyService().createHierarchyType(request.getSessionId(), conflict);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(hierarchyType.toJSON(serializer));
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
  @Endpoint(url = "organizations/get-all", method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF getOrganizations(ClientRequestIF request, @RequestParamter(name = "text") String text, @RequestParamter(name = "type") String type, @RequestParamter(name = "parent") String parent, @RequestParamter(name = "hierarchy") String hierarchy, @RequestParamter(name = "date") String date) throws ParseException
  {

    OrganizationDTO[] orgs = this.registryService.getOrganizations(request.getSessionId(), null);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    JsonArray orgsJson = new JsonArray();
    for (OrganizationDTO org : orgs)
    {
      orgsJson.add(org.toJSON(serializer));
    }

    return new RestBodyResponse(orgsJson);
  }

  /**
   * Submit new organization.
   * 
   * @param sessionId
   * @param json
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "orgainization/create")
  public ResponseIF submitNewOrganization(ClientRequestIF request, @RequestParamter(name = "json") String json)
  {
    OrganizationDTO org = this.registryService.createOrganization(request.getSessionId(), json);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(org.toJSON(serializer));
  }

  /**
   * Delete organization.
   * 
   * @param sessionId
   * @param json
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "orgainization/delete")
  public ResponseIF removeOrganization(ClientRequestIF request, @RequestParamter(name = "code") String code)
  {
    this.registryService.deleteOrganization(request.getSessionId(), code);

    return new RestResponse();
  }

  /**
   * Update organization.
   * 
   * @param sessionId
   * @param json
   */
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "orgainization/update")
  public ResponseIF updateOrganization(ClientRequestIF request, @RequestParamter(name = "json") String json)
  {
    OrganizationDTO org = this.registryService.updateOrganization(request.getSessionId(), json);
    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    return new RestBodyResponse(org.toJSON(serializer));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "geoobject/search")
  public ResponseIF search(ClientRequestIF request, @RequestParamter(name = RegistryUrls.GEO_OBJECT_GET_PARAM_TYPE_CODE) String typeCode, @RequestParamter(name = "text") String text, @RequestParamter(name = "date") String date) throws JSONException, ParseException
  {
    Date forDate = null;

    if (date != null && date.length() > 0)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
      format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

      forDate = format.parse(date);
    }

    List<GeoObject> results = this.registryService.search(request.getSessionId(), typeCode, text, forDate);

    CustomSerializer serializer = this.registryService.serializer(request.getSessionId());

    JsonArray response = new JsonArray();

    for (GeoObject result : results)
    {
      response.add(result.toJSON(serializer));
    }

    return new RestBodyResponse(response);
  }

}
