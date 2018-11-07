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
package net.geoprism.prism;

import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.mvc.ViewResponse;
import com.runwaysdk.system.metadata.MdAttributeIndicatorDTO;

import net.geoprism.GeoprismUserDTO;
import net.geoprism.MappableClassDTO;
import net.geoprism.RoleConstants;

@Controller(url = "prism")
public class PrismController
{
  public static final String JSP_DIR         = "/WEB-INF/";

  public static final String LAYOUT          = "WEB-INF/templates/basicLayout.jsp";

  public static final String MENU            = "net/geoprism/userMenu/userMenu.jsp";

  public static final String DATA_MANAGEMENT = "net/geoprism/userMenu/data-management.jsp";

  public static final String DASHBOARDS      = "net/geoprism/userMenu/userDashboards.jsp";

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF home(ClientRequestIF request)
  {
    ViewResponse response = new ViewResponse("/WEB-INF/net/geoprism/prism/admin.jsp");
    response.set("appname", "my-app");
    response.set("base", "prism/home");

    return response;
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF management(ClientRequestIF request)
  {
    ViewResponse response = new ViewResponse("/WEB-INF/net/geoprism/prism/prism.jsp");
    response.set("appname", "my-app");
    response.set("base", "prism/management");

    return response;
  }

  @Endpoint(method = ServletMethod.GET)
  public ResponseIF admin(ClientRequestIF request)
  {
    ViewResponse response = new ViewResponse("/WEB-INF/net/geoprism/prism/admin.jsp");
    response.set("appname", "admin-app");
    response.set("base", "prism/admin");
    response.set("admin", GeoprismUserDTO.isRoleMemeber(request, RoleConstants.ADIM_ROLE));

    return response;
  }

  @Endpoint(method = ServletMethod.GET, error = ErrorSerialization.JSON)
  public ResponseIF datasets(ClientRequestIF request) throws JSONException
  {
    String datasets = MappableClassDTO.getAllAsJSON(request);

    return new RestBodyResponse(new JSONObject(datasets));
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    MappableClassDTO.remove(request, oid);

    return new RestResponse();
  }

  @Endpoint(url = "edit-dataset", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    MappableClassDTO mappableClass = MappableClassDTO.lock(request, oid);

    return new RestBodyResponse(new JSONObject(mappableClass.getAsJSON()));
  }

  @Endpoint(url = "xport-dataset", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF xport(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    MappableClassDTO.xport(request, oid);

    return new RestResponse();
  }

  @Endpoint(url = "unlock-dataset", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF cancel(ClientRequestIF request, @RequestParamter(name = "oid") String oid)
  {
    MappableClassDTO.unlock(request, oid);

    return new RestResponse();
  }

  @Endpoint(url = "apply-dataset", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF applyDatasetUpdate(ClientRequestIF request, @RequestParamter(name = "dataset") String dataset) throws JSONException
  {
    JSONObject dsJSONObj = new JSONObject(dataset);
    String dsId = dsJSONObj.getString("oid");

    MappableClassDTO ds = MappableClassDTO.lock(request, dsId);
    MappableClassDTO.applyDatasetUpdate(request, dataset);
    ds.unlock();

    return new RestBodyResponse(new JSONObject(ds.getAsJSON()));
  }

  @Endpoint(url = "add-indicator", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF addIndicator(ClientRequestIF request, @RequestParamter(name = "datasetId") String datasetId, @RequestParamter(name = "indicator") String indicator) throws JSONException
  {
    String response = MappableClassDTO.addIndicator(request, datasetId, indicator);

    return new RestBodyResponse(new JSONObject(response));
  }

  @Endpoint(url = "edit-attribute", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF editAttribute(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    String response = MappableClassDTO.lockIndicator(request, oid);

    return new RestBodyResponse(new JSONObject(response));
  }

  @Endpoint(url = "remove-attribute", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF removeAttribute(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    MappableClassDTO.removeIndicator(request, oid);

    return new RestResponse();
  }

  @Endpoint(url = "unlock-attribute", method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF unlockAttribute(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    MdAttributeIndicatorDTO.unlock(request, oid);

    return new RestResponse();
  }
}
