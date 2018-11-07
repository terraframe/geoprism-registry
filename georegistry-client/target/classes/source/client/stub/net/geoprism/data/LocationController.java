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
package net.geoprism.data;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.business.ValueObjectDTO;
import com.runwaysdk.business.ValueQueryDTO;
import com.runwaysdk.constants.ClientRequestIF;

import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.ParseType;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;
import com.runwaysdk.system.gis.geo.GeoEntityViewDTO;
import com.runwaysdk.system.gis.geo.LocatedInDTO;
import com.runwaysdk.system.gis.geo.SynonymDTO;
import com.runwaysdk.system.gis.geo.UniversalDTO;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.ExcludeConfiguration;
import net.geoprism.InputStreamResponse;
import net.geoprism.ListSerializable;
import net.geoprism.gis.geoserver.GeoserverProperties;
import net.geoprism.ontology.GeoEntityUtilDTO;

@Controller(url = "location")
public class LocationController 
{
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF select(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "universalId") String universalId, @RequestParamter(name = "existingLayers") String existingLayers) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityUtilDTO.getEntity(request, oid);

    return this.getLocationInformation(request, entity, universalId, existingLayers);
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF open(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "existingLayers") String existingLayers) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityUtilDTO.getEntity(request, oid);
    List<GeoEntityDTO> ancestors = Arrays.asList(GeoEntityUtilDTO.getOrderedAncestors(request, entity.getOid()));

    RestResponse response = this.getLocationInformation(request, entity, null, existingLayers);
    response.set("ancestors", new ListSerializable(ancestors), new GeoEntityJsonConfiguration());

    return response;
  }

  private RestResponse getLocationInformation(ClientRequestIF request, GeoEntityDTO entity, String universalId, String existingLayers) throws JSONException
  {
    List<? extends UniversalDTO> universals = entity.getUniversal().getAllContains();

    if ( ( universalId == null || universalId.length() == 0 ) && universals.size() > 0)
    {
      universalId = universals.get(0).getOid();
    }

    // String geometries = GeoEntityUtilDTO.publishLayers(request, entity.getOid(), universalId, existingLayers);

    ValueQueryDTO children = GeoEntityUtilDTO.getChildren(request, entity.getOid(), universalId, 200);

    RestResponse response = new RestResponse();
    response.set("children", children);
    response.set("bbox", GeoEntityUtilDTO.getChildrenBBOX(request, entity.getOid(), universalId) );
    response.set("universals", new ListSerializable(universals));
    response.set("entity", new GeoEntitySerializable(entity), new GeoEntityJsonConfiguration());
    response.set("universal", ( universalId != null && universalId.length() > 0 ) ? universalId : "");
    response.set("workspace", GeoserverProperties.getWorkspace());
    // response.set("geometries", new JSONStringImpl(geometries));
    // response.set("layers", object.get("layers"));
    																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																													
    return response;
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF suggestions(ClientRequestIF request, @RequestParamter(name = "text") String text, @RequestParamter(name = "limit") Integer limit) throws JSONException
  {
    ValueQueryDTO results = GeoEntityUtilDTO.getGeoEntitySuggestions(request, null, null, text, limit);

    return new RestBodyResponse(results);
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "entity", parser = ParseType.BASIC_JSON) GeoEntityDTO entity, @RequestParamter(name = "parentOid") String parentOid, @RequestParamter(name = "existingLayers") String existingLayers) throws JSONException
  {
    if (entity.getGeoId() == null || entity.getGeoId().length() == 0)
    {
      entity.setGeoId(IDGenerator.nextID());
    }

    if (entity.isNewInstance())
    {
      GeoEntityViewDTO dto = GeoEntityDTO.create(request, entity, parentOid, LocatedInDTO.CLASS);

      GeoEntityUtilDTO.refreshViews(request, existingLayers);

      JSONObject object = new JSONObject();
      object.put(GeoEntityDTO.TYPE, ValueObjectDTO.CLASS);
      object.put(GeoEntityDTO.OID, dto.getGeoEntityId());
      object.put(GeoEntityDTO.DISPLAYLABEL, dto.getGeoEntityDisplayLabel());
      object.put(GeoEntityDTO.GEOID, entity.getGeoId());
      object.put(GeoEntityDTO.UNIVERSAL, dto.getUniversalDisplayLabel());

      return new RestBodyResponse(object);
    }
    else
    {
      String oid = entity.getOid();

      entity.apply();

      GeoEntityUtilDTO.refreshViews(request, existingLayers);

      JSONObject object = new JSONObject();
      object.put(GeoEntityDTO.TYPE, ValueObjectDTO.CLASS);
      object.put(GeoEntityDTO.OID, entity.getOid());
      object.put(GeoEntityDTO.DISPLAYLABEL, entity.getDisplayLabel().getValue());
      object.put(GeoEntityDTO.GEOID, entity.getGeoId());
      object.put(GeoEntityDTO.UNIVERSAL, entity.getUniversal().getDisplayLabel().getValue());
      object.put("oid", oid);

      return new RestBodyResponse(object);
    }
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF applyGeometries(ClientRequestIF request, @RequestParamter(name = "featureCollection") String featureCollection)
  {
     GeoEntityUtilDTO.applyGeometries(request, featureCollection);

    return new RestBodyResponse("");
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF openEditingSession(ClientRequestIF request, @RequestParamter(name = "config") String config)
  {
    InputStream istream = GeoEntityUtilDTO.openEditingSession(request, config.toString());

    return new InputStreamResponse(istream, "application/x-protobuf", null);
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF cancelEditingSession(ClientRequestIF request, @RequestParamter(name = "config") String config)
  {
    GeoEntityUtilDTO.cancelEditingSession(request, config.toString());

    return new RestBodyResponse("");
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF viewSynonyms(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityDTO.get(request, entityId);
    
    List<? extends SynonymDTO> synonyms = entity.getAllSynonym();
    for (SynonymDTO syn : synonyms)
    {
      syn.lock();
    }
    
    ListSerializable list = new ListSerializable(synonyms);
    
    return new RestBodyResponse(list);
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF applyEditSynonyms(ClientRequestIF request, @RequestParamter(name = "synonyms") String sjsonSynonyms) throws JSONException
  {
    JSONObject jobjSynonyms = new JSONObject(sjsonSynonyms);
    
    String sParent = jobjSynonyms.getString("parent");
    
    JSONArray synonyms = jobjSynonyms.getJSONArray("synonyms");
    
    for (int i = 0; i < synonyms.length(); ++i)
    {
      JSONObject synonym = synonyms.getJSONObject(i);
      
      String oid = synonym.getString("oid");
      if (oid.length() == 64)
      {
        SynonymDTO syn = SynonymDTO.get(request, oid);
        syn.getDisplayLabel().setValue(synonym.getString("displayLabel"));
        syn.apply();
      }
      else
      {
        SynonymDTO syn = new SynonymDTO(request);
        syn.getDisplayLabel().setValue(synonym.getString("displayLabel"));
        
        SynonymDTO.create(request, syn, sParent);
      }
    }
    
    JSONArray deleted = jobjSynonyms.getJSONArray("deleted");
    
    for (int i = 0; i < deleted.length(); ++i)
    {
      String delId = deleted.getString(i);
      
      SynonymDTO.get(request, delId).delete();
    }
    
    return new RestBodyResponse("");
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF cancelEditSynonyms(ClientRequestIF request, @RequestParamter(name = "synonyms") String synonymsJSONArray) throws JSONException
  {
    JSONArray synonyms = new JSONArray(synonymsJSONArray);
    
    for (int i = 0; i < synonyms.length(); ++i)
    {
      SynonymDTO.get(request, synonyms.getString(i)).unlock();
    }
    
    return new RestBodyResponse("");
  }
  
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityDTO.lock(request, entityId);

    return new RestBodyResponse(entity, new ExcludeConfiguration(GeoEntityDTO.class, GeoEntityDTO.WKT));
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF unlock(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO.unlock(request, entityId);

    return new RestBodyResponse("");
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF remove(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId, @RequestParamter(name = "existingLayers") String existingLayers) throws JSONException
  {
    GeoEntityUtilDTO.deleteGeoEntity(request, entityId);

    GeoEntityUtilDTO.refreshViews(request, existingLayers);

    return new RestBodyResponse("");
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
}
