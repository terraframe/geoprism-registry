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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.business.ValueObjectDTO;
import com.runwaysdk.business.ValueQueryDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;

import net.geoprism.DataUploaderDTO;
import net.geoprism.ontology.ClassifierDTO;

public class TermController
{
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
  public ResponseIF validateCategoryName(ClientRequestIF request, @RequestParamter(name = "name") String name, @RequestParamter(name = "oid") String oid)
  {
    ClassifierDTO.validateCategoryName(request, name, oid);

    return new RestBodyResponse("");
  }
}
