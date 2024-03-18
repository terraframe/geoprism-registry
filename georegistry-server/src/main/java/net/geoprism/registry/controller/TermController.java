/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;

import net.geoprism.ontology.ClassifierDTO;
import net.geoprism.registry.service.request.GPRClassifierService;

@RestController
@Validated
public class TermController extends RunwaySpringController
{
  public static class SynonymBody
  {
    @NotEmpty
    private String synonymId;

    public String getSynonymId()
    {
      return synonymId;
    }

    public void setSynonymId(String synonymId)
    {
      this.synonymId = synonymId;
    }
  }

  public static class ClassifierSynonymBody
  {
    @NotEmpty
    private String classifierId;

    @NotEmpty
    private String label;

    public String getClassifierId()
    {
      return classifierId;
    }

    public void setClassifierId(String classifierId)
    {
      this.classifierId = classifierId;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }
  }

  @Autowired
  protected GPRClassifierService classifierService;

  public static final String     API_PATH = "term";

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseEntity<String> createClassifierSynonym(@Valid @RequestBody ClassifierSynonymBody body)
  {
    String response = this.classifierService.createSynonym(this.getClientRequest().getSessionId(), body.getClassifierId(), body.getLabel());

    JSONObject object = new JSONObject(response);

    return new ResponseEntity<String>(object.toString(), HttpStatus.OK);
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseEntity<Void> deleteClassifierSynonym(@Valid @RequestBody SynonymBody body)
  {
    this.classifierService.deleteSynonym(this.getClientRequest().getSessionId(), body.getSynonymId());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/getClassifierSuggestions")
  public ResponseEntity<String> getClassifierSuggestions(@NotEmpty @RequestParam String importType, @NotEmpty @RequestParam String typeCode, @NotEmpty @RequestParam String attributeCode, @RequestParam(required = false) String text, @RequestParam(required = false) Integer limit)
  {
    List<JSONObject> results = this.classifierService.getClassifierSuggestions(this.getSessionId(), importType, typeCode, attributeCode, text, limit);

    return new ResponseEntity<String>(results.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/validateCategoryName")
  public ResponseEntity<Void> validateCategoryName(@NotEmpty @RequestParam String name, @NotEmpty @RequestParam String oid)
  {
    ClassifierDTO.validateCategoryName(this.getClientRequest(), name, oid);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
