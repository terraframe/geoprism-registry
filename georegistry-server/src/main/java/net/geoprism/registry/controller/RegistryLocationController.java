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
package net.geoprism.registry.controller;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;

import net.geoprism.InputStreamResponse;
import net.geoprism.ontology.GeoEntityUtilDTO;
import net.geoprism.registry.service.LocationService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.LocationInformation;

/**
 * This controller is used by the location manager widget.
 * 
 * @author rrowlands
 *
 */
@Controller(url = "registrylocation")
public class RegistryLocationController
{
  private LocationService service = new LocationService();

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF fetchGeoObjectFromGeoEntity(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityDTO.get(request, entityId);

    JSONObject joResp = new JSONObject();

    GeoObject go = service.getGeoObject(request.getSessionId(), entity.getOid());

    // Add the GeoObject to the response
    joResp.put("geoObject", service.serializeGo(request.getSessionId(), go));
    joResp.put("geoObjectType", new JSONObject(go.getType().toJSON().toString()));
    joResp.put("parentTreeNode", service.addParentInfoToExistingGO(request.getSessionId(), go));

    return new RestBodyResponse(joResp.toString());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF editNewGeoObject(ClientRequestIF request, @RequestParamter(name = "universalId") String universalId, @RequestParamter(name = "jsParent") String sjsParent, @RequestParamter(name = "mdRelationshipId") String mdRelationshipId) throws JSONException
  {
    String resp = service.editNewGeoObjectInReq(request.getSessionId(), universalId, sjsParent, mdRelationshipId);

    return new RestBodyResponse(resp);
  }

  // @Endpoint(error = ErrorSerialization.JSON)
  // public ResponseIF edit(ClientRequestIF request, @RequestParamter(name =
  // "entityId") String entityId) throws JSONException
  // {
  // GeoEntityDTO entity;
  // try
  // {
  // entity = GeoEntityDTO.lock(request, entityId);
  // }
  // catch (WritePermissionExceptionDTO e)
  // {
  // entity = GeoEntityDTO.get(request, entityId);
  // }
  //
  // ComponentDTOIFToBasicJSON componentDTOToJSON =
  // ComponentDTOIFToBasicJSON.getConverter(entity, new
  // ExcludeConfiguration(GeoEntityDTO.class, GeoEntityDTO.WKT));
  // JSONObject joGeoEnt = componentDTOToJSON.populate();
  //
  // // Add the GeoObject to the response
  // GeoObject go = getGeoObject(request.getSessionId(), entity.getOid());
  // joGeoEnt.put("geoObject", serializeGo(request.getSessionId(), go));
  //
  // return new RestBodyResponse(joGeoEnt.toString());
  // }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "isNew") Boolean isNew, @RequestParamter(name = "geoObject") String sjsGO, @RequestParamter(name = "parentOid") String parentOid, @RequestParamter(name = "existingLayers") String existingLayers, @RequestParamter(name = "parentTreeNode") String sjsPTN) throws JSONException
  {
    return service.applyInRequest(request.getSessionId(), isNew, sjsGO, parentOid, existingLayers, sjsPTN);
  }

  /**
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   */

  /**
   * @param request
   * @param code
   *          Code of the geo object being selected
   * @param typeCode
   *          Type code of the geo object being selected
   * @param date
   *          Date in which to use for values and parent lookup
   * @param childTypeCode
   *          Type code of the desired children geo objects
   * @param hierarchyCode
   *          Hierarchy code of the desired children geo objects
   * @return
   * @throws JSONException
   * @throws ParseException
   */
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF select(ClientRequestIF request, @RequestParamter(name = "code") String code, @RequestParamter(name = "typeCode") String typeCode, @RequestParamter(name = "date") String date, @RequestParamter(name = "childTypeCode") String childTypeCode, @RequestParamter(name = "hierarchyCode") String hierarchyCode) throws JSONException, ParseException
  {
    // ServerGeoObjectIF parent = service.getGeoObjectByEntityId(oid);

    LocationInformation information = service.getLocationInformation(request.getSessionId(), code, typeCode, parseDate(date), childTypeCode, hierarchyCode);
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(request.getSessionId());

    return new RestBodyResponse(information.toJson(serializer));
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF roots(ClientRequestIF request, @RequestParamter(name = "date") String date, @RequestParamter(name = "typeCode") String typeCode, @RequestParamter(name = "hierarchyCode") String hierarchyCode) throws JSONException, ParseException
  {
    // ServerGeoObjectIF parent = service.getGeoObjectByEntityId(oid);

    LocationInformation information = service.getLocationInformation(request.getSessionId(), parseDate(date), typeCode, hierarchyCode);
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(request.getSessionId());

    return new RestBodyResponse(information.toJson(serializer));
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF data(ClientRequestIF request, @RequestParamter(name = "x") Integer x, @RequestParamter(name = "y") Integer y, @RequestParamter(name = "z") Integer z, @RequestParamter(name = "config") String config) throws JSONException
  {
    JSONObject object = new JSONObject(config);
    object.put("x", x);
    object.put("y", y);
    object.put("z", z);

    InputStream istream = GeoEntityUtilDTO.getData(request, object.toString());

    return new InputStreamResponse(istream, "application/x-protobuf", null);
  }

  private Date parseDate(String date) throws ParseException
  {
    if (date != null && date.length() > 0)
    {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

      return dateFormat.parse(date);
    }

    return ValueOverTime.INFINITY_END_DATE;
  }
}
