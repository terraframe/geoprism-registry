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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.json.JSONException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

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
  public ResponseIF search(ClientRequestIF request, @RequestParamter(name = "text") String text, @RequestParamter(name = "date") String date) throws JSONException, ParseException
  {
    List<GeoObject> results = service.search(request.getSessionId(), text, parseDate(date));
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(request.getSessionId());

    JsonArray features = results.stream().collect(() -> new JsonArray(), (array, element) -> array.add(element.toJSON(serializer)), (listA, listB) -> {
    });

    JsonObject featureCollection = new JsonObject();
    featureCollection.addProperty("type", "FeatureCollection");
    featureCollection.add("features", features);

    return new RestBodyResponse(featureCollection);
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
