package net.geoprism.registry.service.request;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.ontology.ClassifierDTO;
import net.geoprism.registry.service.business.GPRClassifierBusinessService;

@Service
@Primary
public class GPRClassifierService extends ClassifierService
{
  @Autowired
  private GPRClassifierBusinessService service;

  @Request(RequestType.SESSION)
  public List<JSONObject> getClassifierSuggestions(String sessionId, String typeCode, String attributeCode, String text, Integer limit)
  {
    List<ValueObject> suggestions = this.service.getClassifierSuggestions(typeCode, attributeCode, text, limit);

    return suggestions.stream().map(result -> {
      JSONObject object = new JSONObject();
      object.put("label", result.getValue(ClassifierDTO.DISPLAYLABEL));
      object.put("value", result.getValue(ClassifierDTO.OID));

      return object;
    }).collect(Collectors.toList());
  }

}
