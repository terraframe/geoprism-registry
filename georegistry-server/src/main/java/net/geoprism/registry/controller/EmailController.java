/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.controller;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.geoprism.email.service.EmailSettingView;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.EmailServiceIF;

@RestController
@Validated
public class EmailController extends RunwaySpringController
{
  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "email";

  @Autowired
  protected EmailServiceIF   service;

  @GetMapping(API_PATH + "/editDefault")
  public ResponseEntity<EmailSettingView> editDefault() throws JSONException
  {
    EmailSettingView view = this.service.editDefault(this.getClientRequest().getSessionId());

    return new ResponseEntity<EmailSettingView>(view, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/apply")
  public ResponseEntity<EmailSettingView> apply(@Valid @RequestBody EmailSettingView view) throws JSONException
  {
    view = this.service.apply(this.getClientRequest().getSessionId(), view, true);

    return new ResponseEntity<EmailSettingView>(view, HttpStatus.OK);
  }

}
