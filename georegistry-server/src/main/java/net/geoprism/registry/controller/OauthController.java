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

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.service.request.RegistryComponentService;

@RestController
@Validated
public class OauthController extends RunwaySpringController
{
  public static final String API_PATH = "oauth";

  @Autowired
  private RegistryComponentService    service;

  /**
   * Returns an OauthServer configuration with the specified id. If an id is not
   * provided, this endpoint will return all configurations (in your
   * organization).
   * 
   * @param request
   * @param id
   * @return A json array of serialized OauthServer configurations.
   * @throws JSONException
   */
  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> oauthGetAll(@RequestParam(required = false) String id)
  {
    String json = this.service.oauthGetAll(this.getSessionId(), id);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

  /**
   * Returns information which is available to public users (without
   * permissions) which will allow them to login as an oauth user.
   * 
   * @param request
   * @param id
   * @return A json array of OauthServer configurations with only publicly
   *         available information.
   * @throws JSONException
   */
  @GetMapping(API_PATH + "/get-public")  
  public ResponseEntity<String> oauthGetPublic(@RequestParam(required = false) String id)
  {
    String json = this.service.oauthGetPublic(this.getSessionId(), id);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

}
