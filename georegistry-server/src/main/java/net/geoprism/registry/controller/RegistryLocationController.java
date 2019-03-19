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
package net.geoprism.registry.controller;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.business.ValueObjectDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.conversion.BasicJSONToComponentDTO;
import com.runwaysdk.mvc.conversion.ComponentDTOIFToBasicJSON;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;
import com.runwaysdk.transport.conversion.business.MutableDTOToMutable;
import com.runwaysdk.util.IDGenerator;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ExcludeConfiguration;
import net.geoprism.ontology.GeoEntityUtilDTO;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

/**
 * This controller is used by the location manager widget.
 * 
 * @author rrowlands
 *
 */
@Controller(url = "registrylocation")
public class RegistryLocationController
{
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityDTO.lock(request, entityId);

    ComponentDTOIFToBasicJSON componentDTOToJSON = ComponentDTOIFToBasicJSON.getConverter(entity, new ExcludeConfiguration(GeoEntityDTO.class, GeoEntityDTO.WKT));
    JSONObject joGeoEnt = componentDTOToJSON.populate();

    // Add the GeoObject to the response
    GeoObject go = getGeoObject(request.getSessionId(), entity.getOid());
    joGeoEnt.put("geoObject", serializeGo(request.getSessionId(), go));

    return new RestBodyResponse(joGeoEnt.toString());
  }

  @Request(RequestType.SESSION)
  private GeoObject getGeoObject(String sessionId, String id)
  {
    return ConversionService.getInstance().geoEntityToGeoObject(GeoEntity.get(id));
  }

  private JSONObject serializeGo(String sessionId, GeoObject go)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    JSONObject joGo = new JSONObject(go.toJSON(serializer).toString());
    joGo.remove("geometry");
    return joGo;
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "entity") String sEntity, @RequestParamter(name = "parentOid") String parentOid, @RequestParamter(name = "existingLayers") String existingLayers) throws JSONException
  {
    return applyInRequest(request.getSessionId(), request, sEntity, parentOid, existingLayers);
  }

  @Request(RequestType.SESSION)
  private ResponseIF applyInRequest(String sessionId, ClientRequestIF request, String sEntity, String parentOid, String existingLayers)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    BasicJSONToComponentDTO converter = BasicJSONToComponentDTO.getConverter(request.getSessionId(), Session.getCurrentLocale(), GeoEntityDTO.CLASS + "DTO", sEntity);
    GeoEntityDTO entityDTO = (GeoEntityDTO) converter.populate();

    MutableDTOToMutable dtoToComponent = MutableDTOToMutable.getConverter(request.getSessionId(), entityDTO);
    GeoEntity entity = (GeoEntity) dtoToComponent.populate();

    if (entityDTO.getGeoId() == null || entityDTO.getGeoId().length() == 0)
    {
      entityDTO.setGeoId(IDGenerator.nextID());
    }

    JSONObject joEntity = new JSONObject(sEntity);
    JSONObject geoObject = joEntity.getJSONObject("geoObject");
    JSONObject properties = geoObject.getJSONObject("properties");

    String statusCode = properties.getJSONObject("status").getString("code");

    GeoObject go = ConversionService.getInstance().geoEntityToGeoObject(entity);
    go.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(statusCode).get());

    if (joEntity.has("wkt"))
    {
      try
      {
        GeometryHelper geometryHelper = new GeometryHelper();

        Geometry geo = geometryHelper.parseGeometry(joEntity.getString("wkt"));

        if (go.getGeometryType().equals(GeometryType.MULTIPOLYGON))
        {
          go.setGeometry(geometryHelper.getGeoMultiPolygon(geo));
        }
        else if (go.getGeometryType().equals(GeometryType.POINT))
        {
          go.setGeometry(geometryHelper.getGeoPoint(geo));
        }
        else
        {
          go.setGeometry(geo);
        }

      }
      catch (Exception e)
      {
        // Ignore
      }
    }

    if (entityDTO.isNewInstance())
    {
      GeoObject goChild = RegistryService.getInstance().createGeoObject(request.getSessionId(), go.toJSON(serializer).toString());

      GeoObject goParent = getGeoObject(request.getSessionId(), parentOid);
      RegistryService.getInstance().addChild(request.getSessionId(), goParent.getUid(), goParent.getType().getCode(), goChild.getUid(), goChild.getType().getCode(), "LocatedIn");

      GeoEntityUtilDTO.refreshViews(request, existingLayers);

      JSONObject object = new JSONObject();
      object.put(GeoEntityDTO.TYPE, ValueObjectDTO.CLASS);
      object.put(GeoEntityDTO.OID, entityDTO.getOid());
      object.put(GeoEntityDTO.DISPLAYLABEL, entityDTO.getDisplayLabel().getValue());
      object.put(GeoEntityDTO.GEOID, entityDTO.getGeoId());
      object.put(GeoEntityDTO.UNIVERSAL, entityDTO.getUniversal().getDisplayLabel().getValue());

      object.put("geoObject", serializeGo(sessionId, goChild));

      return new RestBodyResponse(object);
    }
    else
    {
      String oid = entityDTO.getOid();

      RegistryService.getInstance().updateGeoObject(request.getSessionId(), go.toJSON(serializer).toString());

      GeoEntityUtilDTO.refreshViews(request, existingLayers);

      JSONObject object = new JSONObject();
      object.put(GeoEntityDTO.TYPE, ValueObjectDTO.CLASS);
      object.put(GeoEntityDTO.OID, entityDTO.getOid());
      object.put(GeoEntityDTO.DISPLAYLABEL, entityDTO.getDisplayLabel().getValue());
      object.put(GeoEntityDTO.GEOID, entityDTO.getGeoId());
      object.put(GeoEntityDTO.UNIVERSAL, entityDTO.getUniversal().getDisplayLabel().getValue());
      object.put("oid", oid);

      object.put("geoObject", serializeGo(sessionId, go));

      return new RestBodyResponse(object);
    }
  }
}
