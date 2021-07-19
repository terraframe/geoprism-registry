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

import org.json.JSONException;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.registry.geoobject.ServerGeoObjectService;

@Controller(url = "geoobject-editor")
public class GeoObjectEditorController
{
  /**
   * Submits an edit to a Geo-Object. If you are a Registry Contributor, this will create a ChangeRequest with a CreateGeoObjectAction. If you
   * have edit permissions, this will attempt to create the Geo-Object immediately. If you do not have permissions for either a {@link net.geoprism.registry.CGRPermissionException}
   * exception will be thrown.
   * 
   * @param parentTreeNode A serialized ParentTreeNodeOverTime, which specifies the parent information.
   * @param geoObject A serialized GeoObjectOverTime, which specifies the GeoObject to create.
   * @param masterListId If this should also refresh a master list after editing, you may provide that master list id here.
   * @param notes If you are attempting to create a Change Request, you may provide notes for that request here.
   * @throws net.geoprism.registry.CGRPermissionException
   * @return A JsonObject with {isChangeRequest: Boolean, changeRequestId: String}
   * @throws JSONException
   * @throws net.geoprism.registry.CGRPermissionException
   */
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF createGeoObject(ClientRequestIF request, @RequestParamter(name = "parentTreeNode") String parentTreeNode, @RequestParamter(name = "geoObject") String geoObject, @RequestParamter(name = "masterListId") String masterListId, @RequestParamter(name = "notes") String notes) throws JSONException
  {
    JsonObject resp = new ServerGeoObjectService().createGeoObject(request.getSessionId(), parentTreeNode, geoObject, masterListId, notes);

    return new RestBodyResponse(resp);
  }
  
  /**
   * Submits an update to an existing Geo-Object. If you are a Registry Contributor, this will create a ChangeRequest with an UpdateAttributeAction. If you
   * have edit permissions, this will attempt to apply the edit immediately. If you do not have permissions for either a {@link net.geoprism.registry.CGRPermissionException}
   * exception will be thrown.
   * 
   * @param geoObjectCode The code of the Geo-Object to update.
   * @param geoObjectTypeCode The code of the type of the Geo-Object to update.
   * @param actions A serialized json array containing the actions to perform on the Geo-Object.
   * @param masterListId If this should also refresh a master list after editing, you may provide that master list id here.
   * @param notes If you are attempting to create a Change Request, you may provide notes for that request here.
   * @return A JsonObject with {isChangeRequest: Boolean, changeRequestId: String}
   * @throws JSONException
   * @throws net.geoprism.registry.CGRPermissionException
   */
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF updateGeoObject(ClientRequestIF request, @RequestParamter(name = "geoObjectCode") String geoObjectCode, @RequestParamter(name = "geoObjectTypeCode") String geoObjectTypeCode, @RequestParamter(name = "actions") String actions, @RequestParamter(name = "masterListId") String masterListId, @RequestParamter(name = "notes") String notes) throws JSONException
  {
    JsonObject resp = new ServerGeoObjectService().updateGeoObject(request.getSessionId(), geoObjectCode, geoObjectTypeCode, actions, masterListId, notes);

    return new RestBodyResponse(resp);
  }
}
