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

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.UserInviteService;
import net.geoprism.spring.core.JsonArrayDeserializer;
import net.geoprism.spring.core.JsonObjectDeserializer;

@RestController
@Validated
public class UserInviteController extends RunwaySpringController
{
  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "invite-user";

  public static class InviteCompleteBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject user;

    @NotEmpty
    String     token;

    public JsonObject getUser()
    {
      return user;
    }

    public void setUser(JsonObject user)
    {
      this.user = user;
    }

    public String getToken()
    {
      return token;
    }

    public void setToken(String token)
    {
      this.token = token;
    }

  }

  public static class InviteUserBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject invite;

    @JsonDeserialize(using = JsonArrayDeserializer.class)
    JsonArray  roleIds;

    public JsonObject getInvite()
    {
      return invite;
    }

    public void setInvite(JsonObject invite)
    {
      this.invite = invite;
    }

    public JsonArray getRoleIds()
    {
      return roleIds;
    }

    public void setRoleIds(JsonArray roleIds)
    {
      this.roleIds = roleIds;
    }
  }

  @Autowired
  protected UserInviteService service;

  @PostMapping(API_PATH + "/initiate")
  public ResponseEntity<Void> initiate(@Valid @RequestBody InviteUserBody body)
  {
    this.service.initiate(this.getSessionId(), body.invite.toString(), body.roleIds.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/complete")
  public ResponseEntity<Void> complete(@Valid @RequestBody InviteCompleteBody body)
  {
    this.service.complete(this.getSessionId(), body.token, body.user.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
