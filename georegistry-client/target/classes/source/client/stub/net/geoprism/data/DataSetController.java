/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.system.metadata.MdAttributeIndicatorDTO;

import net.geoprism.MappableClassDTO;

@Controller(url = "data-set")
public class DataSetController
{
  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON, url = "get-all")
  public ResponseIF getAll(ClientRequestIF request) throws JSONException
  {
    String datasets = MappableClassDTO.getAllAsJSON(request);

    JSONObject object = new JSONObject();
    object.put("datasets", new JSONArray(datasets));

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "apply-update")
  public ResponseIF applyDatasetUpdate(ClientRequestIF request, String dataset) throws JSONException
  {
    JSONObject dsJSONObj = new JSONObject(dataset);
    String dsId = dsJSONObj.getString("oid");

    MappableClassDTO ds = MappableClassDTO.lock(request, dsId);
    MappableClassDTO.applyDatasetUpdate(request, dataset);
    ds.unlock();

    return new RestBodyResponse(new JSONObject(ds.getAsJSON()));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, String oid) throws JSONException
  {
    MappableClassDTO.remove(request, oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, String oid) throws JSONException
  {
    MappableClassDTO mappableClass = MappableClassDTO.lock(request, oid);

    return new RestBodyResponse(new JSONObject(mappableClass.getAsJSON()));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, String config) throws JSONException
  {
    MappableClassDTO.applyDatasetUpdate(request, config);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF cancel(ClientRequestIF request, String oid) throws JSONException
  {
    MappableClassDTO.unlock(request, oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "add-indicator")
  public ResponseIF addIndicator(ClientRequestIF request, String datasetId, String indicator) throws JSONException
  {
    String response = MappableClassDTO.addIndicator(request, datasetId, indicator);

    return new RestBodyResponse(new JSONObject(response));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "edit-attribute")
  public ResponseIF editAttribute(ClientRequestIF request, String oid) throws JSONException
  {
    String response = MappableClassDTO.lockIndicator(request, oid);

    return new RestBodyResponse(new JSONObject(response));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "remove-attribute")
  public ResponseIF removeAttribute(ClientRequestIF request, String oid) throws JSONException
  {
    MappableClassDTO.removeIndicator(request, oid);

    return new RestResponse();
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON, url = "unlock-attribute")
  public ResponseIF unlockAttribute(ClientRequestIF request, String oid) throws JSONException
  {
    MdAttributeIndicatorDTO.unlock(request, oid);

    return new RestResponse();
  }
}
