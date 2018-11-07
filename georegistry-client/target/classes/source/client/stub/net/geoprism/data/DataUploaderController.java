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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.business.ValueObjectDTO;
import com.runwaysdk.business.ValueQueryDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.MultipartFileParameter;
import com.runwaysdk.controller.ServletMethod;

import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;

import net.geoprism.ContentStream;
import net.geoprism.DataUploaderDTO;
import net.geoprism.InputStreamResponse;
import net.geoprism.ontology.ClassifierDTO;
import net.geoprism.ontology.GeoEntityUtilDTO;
import net.geoprism.util.ProgressFacade;
import net.geoprism.util.ProgressState;

@Controller(url = "uploader")
public class DataUploaderController 
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF getAttributeInformation(ClientRequestIF request, @RequestParamter(name = "file") MultipartFileParameter file) throws IOException, JSONException
  {
    String fileName = file.getFilename();
    InputStream stream = file.getInputStream();

    try
    {
      JSONObject object = new JSONObject();
      object.put("information", new JSONObject(DataUploaderDTO.getAttributeInformation(request, fileName, stream)));
      object.put("options", new JSONObject(DataUploaderDTO.getOptionsJSON(request)));
      object.put("classifiers", new JSONArray(ClassifierDTO.getCategoryClassifiersAsJSON(request)));

      return new RestBodyResponse(object);
    }
    finally
    {
      /*
       * Just in case the stream isn't closed by the server method
       */
      stream.close();
    }
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF getErrorFile(ClientRequestIF request, @RequestParamter(name = "oid") String oid) throws JSONException
  {
    InputStream stream = DataUploaderDTO.getErrorFile(request, oid);

    return new InputStreamResponse(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", null);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF importData(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration) throws JSONException
  {
    ContentStream stream = (ContentStream) DataUploaderDTO.importData(request, configuration);

    return new InputStreamResponse(stream, stream.getContentType(), "Test.xlsx");
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF cancelImport(ClientRequestIF request, @RequestParamter(name = "configuration") String configuration)
  {
    DataUploaderDTO.cancelImport(request, configuration);

    return new RestBodyResponse("");
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF getSavedConfiguration(ClientRequestIF request, @RequestParamter(name = "oid") String oid, @RequestParamter(name = "sheetName") String sheetName) throws JSONException
  {
    String configuration = DataUploaderDTO.getSavedConfiguration(request, oid, sheetName);

    JSONObject object = new JSONObject();
    object.put("datasets", new JSONObject(configuration));

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF createGeoEntity(ClientRequestIF request, @RequestParamter(name = "parentOid") String parentOid, @RequestParamter(name = "universalId") String universalId, @RequestParamter(name = "label") String label) throws JSONException
  {
    String entityId = DataUploaderDTO.createGeoEntity(request, parentOid, universalId, label);

    JSONObject object = new JSONObject();
    object.put("entityId", entityId);

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF createGeoEntitySynonym(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId, @RequestParamter(name = "label") String label) throws JSONException
  {
    String response = DataUploaderDTO.createGeoEntitySynonym(request, entityId, label);

    JSONObject object = new JSONObject(response);

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF deleteGeoEntity(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId)
  {
    DataUploaderDTO.deleteGeoEntity(request, entityId);

    return new RestBodyResponse("");
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF deleteGeoEntitySynonym(ClientRequestIF request, @RequestParamter(name = "synonymId") String synonymId)
  {
    DataUploaderDTO.deleteGeoEntitySynonym(request, synonymId);

    return new RestBodyResponse("");
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF createClassifierSynonym(ClientRequestIF request, @RequestParamter(name = "classifierId") String classifierId, @RequestParamter(name = "label") String label) throws JSONException
  {
    String response = DataUploaderDTO.createClassifierSynonym(request, classifierId, label);

    JSONObject object = new JSONObject(response);

    return new RestBodyResponse(object);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF deleteClassifierSynonym(ClientRequestIF request, @RequestParamter(name = "synonymId") String synonymId)
  {
    DataUploaderDTO.deleteClassifierSynonym(request, synonymId);

    return new RestBodyResponse("");
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF getClassifierSuggestions(ClientRequestIF request, @RequestParamter(name = "mdAttributeId") String mdAttributeId, @RequestParamter(name = "text") String text, @RequestParamter(name = "limit") Integer limit) throws JSONException
  {
    JSONArray response = new JSONArray();

    ValueQueryDTO query = ClassifierDTO.getClassifierSuggestions(request, mdAttributeId, text, limit);
    List<ValueObjectDTO> results = query.getResultSet();

    for (ValueObjectDTO result : results)
    {
      JSONObject object = new JSONObject();
      object.put("label", result.getValue(ClassifierDTO.DISPLAYLABEL));
      object.put("value", result.getValue(ClassifierDTO.OID));

      response.put(object);
    }

    return new RestBodyResponse(response);
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF validateDatasetName(ClientRequestIF request, @RequestParamter(name = "name") String name, @RequestParamter(name = "oid") String oid)
  {
    DataUploaderDTO.validateDatasetName(request, name, oid);

    return new RestBodyResponse("");
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF validateCategoryName(ClientRequestIF request, @RequestParamter(name = "name") String name, @RequestParamter(name = "oid") String oid)
  {
    ClassifierDTO.validateCategoryName(request, name, oid);

    return new RestBodyResponse("");
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF getGeoEntitySuggestions(ClientRequestIF request, @RequestParamter(name = "parentOid") String parentOid, @RequestParamter(name = "universalId") String universalId, @RequestParamter(name = "text") String text, @RequestParamter(name = "limit") Integer limit) throws JSONException
  {
    JSONArray response = new JSONArray();

    ValueQueryDTO query = GeoEntityUtilDTO.getGeoEntitySuggestions(request, parentOid, universalId, text, limit);
    List<ValueObjectDTO> results = query.getResultSet();

    for (ValueObjectDTO result : results)
    {
      JSONObject object = new JSONObject();
      object.put("text", result.getValue(GeoEntityDTO.DISPLAYLABEL));
      object.put("data", result.getValue(GeoEntityDTO.OID));

      response.put(object);
    }

    return new RestBodyResponse(response);
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF progress(@RequestParamter(name = "oid") String oid) throws JSONException
  {
    ProgressState progress = ProgressFacade.get(oid);

    if (progress == null)
    {
      progress = new ProgressState(oid);
    }

    return new RestBodyResponse(progress.toJSON());
  }
}
