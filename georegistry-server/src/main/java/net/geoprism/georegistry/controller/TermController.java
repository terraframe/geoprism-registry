package net.geoprism.georegistry.controller;

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
