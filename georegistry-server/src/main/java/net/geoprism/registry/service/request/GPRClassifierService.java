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
  public List<JSONObject> getClassifierSuggestions(String sessionId, String importType, String typeCode, String attributeCode, String text, Integer limit)
  {
    List<ValueObject> suggestions = importType.equals("BUSINESS") ? 
        this.service.getBusinessClassifierSuggestions(typeCode, attributeCode, text, limit)
        : this.service.getGeoObjectClassifierSuggestions(typeCode, attributeCode, text, limit);

    return suggestions.stream().map(result -> {
      JSONObject object = new JSONObject();
      object.put("label", result.getValue(ClassifierDTO.DISPLAYLABEL));
      object.put("value", result.getValue(ClassifierDTO.OID));

      return object;
    }).collect(Collectors.toList());
  }

}
