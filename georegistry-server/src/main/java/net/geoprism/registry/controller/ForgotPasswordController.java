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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.ForgotPasswordServiceIF;

@RestController
@Validated
public class ForgotPasswordController extends RunwaySpringController
{
  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "forgotpassword";

  static class CompleteBody
  {
    private String token;

    private String newPassword;

    public CompleteBody()
    {
    }

    public String getToken()
    {
      return token;
    }

    public void setToken(String token)
    {
      this.token = token;
    }

    public String getNewPassword()
    {
      return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
      this.newPassword = newPassword;
    }
  }

  @Autowired
  protected ForgotPasswordServiceIF service;

  @PostMapping(API_PATH + "/initiate")
  public ResponseEntity<Void> initiate(@RequestBody String username) throws JSONException
  {
    this.service.initiate(this.getSessionId(), username);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/complete")
  public ResponseEntity<Void> complete(@Valid @RequestBody CompleteBody body) throws JSONException
  {
    this.service.complete(this.getSessionId(), body.token, body.newPassword);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
