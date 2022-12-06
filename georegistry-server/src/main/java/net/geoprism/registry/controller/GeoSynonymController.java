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

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.controller.BusinessTypeController.OidBody;
import net.geoprism.registry.service.GeoSynonymService;

@RestController
@Validated
public class GeoSynonymController extends RunwaySpringController
{
  public static final String API_PATH = "geo-synonym";
  
  public static final class SynonymBody
  {
    @NotEmpty
    private String typeCode;

    @NotEmpty
    private String code;

    @NotEmpty
    private String label;

    public String getTypeCode()
    {
      return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
      this.typeCode = typeCode;
    }

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
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
  private GeoSynonymService service;

  @PostMapping(API_PATH + "/create-geo-entity-synonym")
  public ResponseEntity<String> createGeoEntitySynonym(@Valid @RequestBody SynonymBody body)
  {
    JSONObject response = service.createGeoEntitySynonym(this.getSessionId(), body.typeCode, body.code, body.label);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/delete-geo-entity-synonym")
  public ResponseEntity<Void> deleteGeoEntitySynonym( @Valid @RequestBody OidBody body)
  {
    service.deleteGeoEntitySynonym(this.getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
