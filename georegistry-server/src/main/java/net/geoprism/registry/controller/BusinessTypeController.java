/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;

import net.geoprism.registry.service.BusinessTypeService;

@Controller(url = "business-type")
public class BusinessTypeController
{
  private BusinessTypeService service;

  public BusinessTypeController()
  {
    this.service = new BusinessTypeService();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-by-org")
  public ResponseIF getByOrg(ClientRequestIF request)
  {
    return new RestBodyResponse(service.listByOrg(request.getSessionId()));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF getAll(ClientRequestIF request)
  {
    return new RestBodyResponse(service.getAll(request.getSessionId()));
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get")
  public ResponseIF get(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    return new RestBodyResponse(service.get(request.getSessionId(), oid));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply")
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "type") String typeJSON)
  {
    return new RestBodyResponse(this.service.apply(request.getSessionId(), typeJSON));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove")
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.remove(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "edit")
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    return new RestBodyResponse(this.service.edit(request.getSessionId(), oid));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "unlock")
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    this.service.unlock(request.getSessionId(), oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "add-attribute")
  public ResponseIF createAttributeType(ClientRequestIF request, @RequestParamter(name = "typeCode") String typeCode, @RequestParamter(name = "attributeType") String attributeType)
  {
    AttributeType attrType = this.service.createAttributeType(request.getSessionId(), typeCode, attributeType);

    return new RestBodyResponse(attrType.toJSON());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "update-attribute")
  public ResponseIF updateAttributeType(ClientRequestIF request, @RequestParamter(name = "typeCode") String typeCode, @RequestParamter(name = "attributeType") String attributeType)
  {
    AttributeType attrType = this.service.updateAttributeType(request.getSessionId(), typeCode, attributeType);

    return new RestBodyResponse(attrType.toJSON());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove-attribute")
  public ResponseIF removeAttributeType(ClientRequestIF request, @RequestParamter(name = "typeCode") String typeCode, @RequestParamter(name = "attributeName") String attributeName)
  {
    this.service.removeAttributeType(request.getSessionId(), typeCode, attributeName);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF data(ClientRequestIF request, @RequestParamter(name = "typeCode", required = true) String typeCode, @RequestParamter(name = "criteria") String criteria)
  {
    JsonObject page = this.service.data(request.getSessionId(), typeCode, criteria);

    return new RestBodyResponse(page);
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-edge-types")
  public ResponseIF getEdgeTypes(ClientRequestIF request, @RequestParamter(name = "typeCode", required = true) String typeCode)
  {
    JsonArray edgeTypes = this.service.getEdgeTypes(request.getSessionId(), typeCode);
    
    return new RestBodyResponse(edgeTypes);
  }
  
}
