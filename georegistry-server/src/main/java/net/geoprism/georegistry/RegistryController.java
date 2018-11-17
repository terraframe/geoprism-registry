/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.georegistry;

import net.geoprism.georegistry.service.RegistryService;

import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.TreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.JsonArray;
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

@Controller(url = RegistryUrls.REGISTRY_CONTROLLER_URL)
public class RegistryController
{
  public static final String       JSP_DIR   = "/WEB-INF/";

  public static final String       INDEX_JSP = "net/geoprism/registry/index.jsp";
	  
  private RegistryService registryService;
  
  public RegistryController()
  {
    this.registryService = new RegistryService();
  }
  
  @Endpoint(method = ServletMethod.GET)
  public ResponseIF hierarchies()
  {
    return new ViewResponse(JSP_DIR + INDEX_JSP);
  }
  
  /**
   * Returns a GeoObject with the given uid.
   *
   * @pre
   * @post
   *
   * @param uid The UID of the GeoObject.
   *
   * @returns a GeoObject in GeoJSON format with the given uid.
   * @throws
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_GET)
   public ResponseIF getGeoObject(ClientRequestIF request, @RequestParamter(name = "uid") String uid) throws JSONException
   {
     GeoObject geoObject = this.registryService.getGeoObject(request.getSessionId(), uid);
     
     return new RestBodyResponse(geoObject.toJSON());
   }
   
   /**
   * Update a new GeoObject in the Common Geo-Registry
   *
   * @pre 
   * @post 
   *
   * @param geoObject in GeoJSON format to be updated.
   *
   * @returns 
   * @throws //TODO
   **/
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_UPDATE)
   public ResponseIF updateGeoObject(ClientRequestIF request, @RequestParamter(name = "geoObject") String jGeoObj)
   {
     GeoObject geoObject = this.registryService.updateGeoObject(request.getSessionId(), jGeoObj);
     
     return new RestBodyResponse(geoObject.toJSON());
   }
   
   /**
   * Get children of the given GeoObject
   *
   * @pre 
   * @post 
   *
   * @param parentUid UID of the parent object for which the call fetches children.
   * @param childrentTypes An array of GeoObjectType names of the types of children GeoObjects to fetch. If blank then return children of all types.
   * @param recursive TRUE if recursive children of the given parent with the given types should be returned, FALSE if only single level children should be returned.  
   * 
   * @returns
   * @throws
   * 
   * Example 1: https://localhost:8443/georegistry/cgr/geoobject/getchildren?parentUid=addfa9b7-e234-354d-a6e9-c7f2af00050a&childrenTypes=Cambodia_Province&recursive=true
   * Example 2: https://localhost:8443/georegistry/cgr/geoobject/getchildren?parentUid=addfa9b7-e234-354d-a6e9-c7f2af00050a&childrenTypes=Cambodia_Province&childrenTypes=Cambodia_District&recursive=true
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_GET_CHILDREN)
   public ResponseIF getChildGeoObjects(ClientRequestIF request, @RequestParamter(name = "parentUid") String parentUid, @RequestParamter(name = "childrenTypes") String childrenTypes, @RequestParamter(name = "recursive") Boolean recursive)
   {
	 
	 JSONArray childrenTypesJSON = null;
	 String[] childrenTypesArray = null;
	 try
	 {
	   childrenTypesJSON = new JSONArray(childrenTypes);
	 }
	 catch(JSONException e)
	 {
		// TODO Replace with more specific exception
		 throw new ProgrammingErrorException(childrenTypes.concat(" can't be parsed."), e);
	 }
	 
	 if(childrenTypesJSON != null)
	 {
       childrenTypesArray = new String[childrenTypesJSON.length()];
	   for (int i = 0; i < childrenTypesJSON.length(); i++) {
		 childrenTypesArray[i] = childrenTypesJSON.getString(i);
	   }
	 }
	 
     TreeNode tn = this.registryService.getChildGeoObjects(request.getSessionId(), parentUid, childrenTypesArray, recursive);
     
     return new RestBodyResponse(tn.toJSON());
   }
    
   /**
   * Get parents of the given GeoObject
   *
   * @pre 
   * @post 
   *
   * @param childUid UID of the child object for which the call fetches parents.
   * @param parentTypes An array of GeoObjectType names of the types of parent GeoObjects to fetch. If blank then return parents of all types.
   * @param recursive TRUE if recursive parents of the given parent with the given types should be returned, FALSE if only single level parents should be returned.  
   * 
   * @returns
   * @throws
   **/   
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_GET_PARENTS)
   public ResponseIF getParentGeoObjects(ClientRequestIF request, @RequestParamter(name = "childUid") String childUid, @RequestParamter(name = "parentTypes") String[] parentTypes, @RequestParamter(name = "recursive") Boolean recursive)
   {
     TreeNode tn = this.registryService.getParentGeoObjects(request.getSessionId(), childUid, parentTypes, recursive);
     
     return new RestBodyResponse(tn.toJSON());
   }
   
   
   /**
   * Get list of valid UIDs for use in creating new GeoObjects. The Common Geo-Registry will only accept newly created GeoObjects with a UID that was issued from the Common GeoRegistry.
   *
   * @pre 
   * @post 
   *
   * @param amount Number of globally unique ids that the Common Geo-Registry will issue to the mobile device.
   *
   * @returns
   * @throws
   **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_GET_UIDS)
   public ResponseIF getUIDs(ClientRequestIF request, @RequestParamter(name = "amount") Integer amount)
   {
     String[] ids = this.registryService.getUIDS(request.getSessionId(), amount);
     
     return new RestBodyResponse(ids);
   }   
   
   /**
    * Creates a relationship between @parentCode and @childCode.
    *
    * @pre Both parentCode and childCode have already been persisted / applied
    * @post A relationship will exist between @parentCode and @childCode
    *
    * @returns ParentTreeNode The new node which was created with the provided parent.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_ADD_CHILD)
   public ResponseIF addChild(ClientRequestIF request, @RequestParamter(name = "parent") String parentRef, @RequestParamter(name = "child") String childRef, @RequestParamter(name = "hierarchy") String hierarchyRef)
   {
     ParentTreeNode pn = this.registryService.addChild(request.getSessionId(), parentRef, childRef, hierarchyRef);
     
     return new RestBodyResponse(pn.toJSON());
   }
   
   /**
    * Return GeoOjectType objects that define the given list of types.
    *
     * @pre 
    * @post 
    *
    * @param types An array of GeoObjectType codes. If blank then all GeoObjectType objects are returned.
    *
     * @returns
    * @throws
    **/
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_TYPE_GET_ALL)
   public ResponseIF getGeoObjectTypes(ClientRequestIF request, @RequestParamter(name = "types") String[] types)
   {
     GeoObjectType[] gots = this.registryService.getGeoObjectTypes(request.getSessionId(), types);
     
     JsonArray jarray = new JsonArray();
     for (int i = 0; i < gots.length; ++i)
     {
       jarray.add(gots[i].toJSON());
     }
     
     return new RestBodyResponse(jarray);
   }
   
   /**
    * Creates a {@link GeoObjectType} from the given JSON.
    * 
    * @param request
    * @param gtJSON JSON of the {@link GeoObjectType} to be created.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_TYPE_CREATE)
   public ResponseIF createGeoObjectType(ClientRequestIF request, @RequestParamter(name = "gtJSON") String gtJSON) throws JSONException
   {
     GeoObjectType geoObjectType = this.registryService.createGeoObjectType(request.getSessionId(), gtJSON);
     
     return new RestBodyResponse(geoObjectType.toJSON());
   }
   
   /**
    * Updates the given {@link GeoObjectType} represented as JSON.
    * 
    * @param sessionId
    * @param gtJSON JSON of the {@link GeoObjectType} to be updated.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_TYPE_UPDATE)
   public ResponseIF updateGeoObjectType(ClientRequestIF request, @RequestParamter(name = "gtJSON") String gtJSON)  throws JSONException
   {
     GeoObjectType geoObjectType = this.registryService.updateGeoObjectType(request.getSessionId(), gtJSON);
     
     return new RestBodyResponse(geoObjectType.toJSON());
   }
   
   /**
    * Deletes the {@link GeoObjectType} with the given code.
    * 
    * @param sessionId
    * @param code code of the {@link GeoObjectType} to delete.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.GEO_OBJECT_TYPE_DELETE)
   public RestResponse deleteGeoObjectType(ClientRequestIF request, @RequestParamter(name = "code") String code)  throws JSONException
   {
     this.registryService.deleteGeoObjectType(request.getSessionId(), code);
     
     return new RestResponse();
   }   
    
   /**
    * Returns HierarchyTypes that define the given list of types. If no types are provided then all will be returned.
    */
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.HIERARCHY_TYPE_GET_ALL)
   public ResponseIF getHierarchyTypes(ClientRequestIF request, @RequestParamter(name = "types") String types)
   {
	   
	   JSONArray childrenTypesJSON = null;
		 String[] childrenTypesArray = null;
		 try
		 {
		   childrenTypesJSON = new JSONArray(types);
		 }
		 catch(JSONException e)
		 {
			// TODO Replace with more specific exception
			 throw new ProgrammingErrorException(types.concat(" can't be parsed."), e);
		 }
		 
		 if(childrenTypesJSON != null)
		 {
	       childrenTypesArray = new String[childrenTypesJSON.length()];
		   for (int i = 0; i < childrenTypesJSON.length(); i++) {
			 childrenTypesArray[i] = childrenTypesJSON.getString(i);
		   }
		 }
		 
     HierarchyType[] hts = this.registryService.getHierarchyTypes(request.getSessionId(), childrenTypesArray);
     
     JsonArray jarray = new JsonArray();
     for (int i = 0; i < hts.length; ++i)
     {
       jarray.add(hts[i].toJSON());
     }
     
     return new RestBodyResponse(jarray);
   }
   
   /**
    * Create the {@link HierarchyType} from the given JSON.
    * 
    * @param sessionId
    * @param htJSON JSON of the {@link HierarchyType} to be created.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.HIERARCHY_TYPE_CREATE)
   public ResponseIF createHierarchyType(ClientRequestIF request, @RequestParamter(name = "htJSON") String htJSON)
   {
     HierarchyType hierarchyType = this.registryService.createHierarchyType(request.getSessionId(), htJSON);
     
     return new RestBodyResponse(hierarchyType.toJSON());
   }
   
   /**
    * Updates the given {@link HierarchyType} represented as JSON.
    * 
    * @param sessionId
    * @param gtJSON JSON of the {@link HierarchyType} to be updated.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.HIERARCHY_TYPE_UPDATE)
   public ResponseIF updateHierarchyType(ClientRequestIF request, @RequestParamter(name = "htJSON") String htJSON)
   {
     HierarchyType hierarchyType = this.registryService.updateHierarchyType(request.getSessionId(), htJSON);
     
     return new RestBodyResponse(hierarchyType.toJSON());
   }
   
   /**
    * Deletes the {@link HierarchyType} with the given code.
    * 
    * @param sessionId
    * @param code code of the {@link HierarchyType} to delete.
 * @return 
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.HIERARCHY_TYPE_DELETE)
   public RestResponse deleteHierarchyType(ClientRequestIF request, @RequestParamter(name = "code") String code)
   {
     this.registryService.deleteHierarchyType(request.getSessionId(), code);
     
     return new RestResponse();
   }
   
   /**
    * Adds the {@link GeoObjectType} with the given child code to the
    * parent {@link GeoObjectType} with the given code for the 
    * given {@link HierarchyType} code.
    * 
    * @param sessionId
    * @param hierarchyCode code of the {@link HierarchyType} the child is being added to.
    * @param parentGeoObjectTypeCode parent {@link GeoObjectType}.
    * @param childGeoObjectTypeCode child {@link GeoObjectType}.
    */
   @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url=RegistryUrls.HIERARCHY_TYPE_ADD)
   public ResponseIF addToHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, 
       @RequestParamter(name = "parentGeoObjectTypeCode") String parentGeoObjectTypeCode,  
       @RequestParamter(name = "childGeoObjectTypeCode") String childGeoObjectTypeCode)
   {
     HierarchyType ht = this.registryService.addToHierarchy(request.getSessionId(), hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);
     
     return new RestBodyResponse(ht.toJSON());
   }
   
   /**
    * Removes the {@link GeoObjectType} with the given child code from the
    * parent {@link GeoObjectType} with the given code for the 
    * given {@link HierarchyType} code.
    * 
    * @param sessionId
    * @param hierarchyCode code of the {@link HierarchyType} the child is being added to.
    * @param parentGeoObjectTypeCode parent {@link GeoObjectType}.
    * @param childGeoObjectTypeCode child {@link GeoObjectType}.
    */
   @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url=RegistryUrls.HIERARCHY_TYPE_REMOVE)
   public ResponseIF removeFromHierarchy(ClientRequestIF request, @RequestParamter(name = "hierarchyCode") String hierarchyCode, 
       @RequestParamter(name = "parentGeoObjectTypeCode") String parentGeoObjectTypeCode,  
       @RequestParamter(name = "childGeoObjectTypeCode") String childGeoObjectTypeCode)
   {
     HierarchyType ht = this.registryService.removeFromHierarchy(request.getSessionId(), hierarchyCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);
     
     return new RestBodyResponse(ht.toJSON());
   }

}
