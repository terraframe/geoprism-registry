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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.request.PublishService;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.PublishDTO;

@RestController
@Validated
public class PublishController extends RunwaySpringController
{
  public static class UidBody
  {
    @NotBlank
    private String uid;

    public String getUid()
    {
      return uid;
    }

    public void setUid(String uid)
    {
      this.uid = uid;
    }
  }

  public static final String API_PATH = RegistryConstants.CONTROLLER_ROOT + "publish";

  @Autowired
  private PublishService     service;

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<List<PublishDTO>> getAll()
  {
    List<PublishDTO> dtos = this.service.getAll(this.getSessionId());

    return ResponseEntity.ok(dtos);
  }

  @GetMapping(API_PATH + "/get")
  public ResponseEntity<PublishDTO> get(@RequestParam(name = "uid") String uid)
  {
    PublishDTO dto = this.service.get(this.getSessionId(), uid);

    return ResponseEntity.ok(dto);
  }

  @PostMapping(API_PATH + "/create")
  public ResponseEntity<PublishDTO> create(@Valid @RequestBody PublishDTO dto)
  {
    PublishDTO publish = this.service.create(getSessionId(), dto);

    return ResponseEntity.ok(publish);
  }

  @PostMapping(API_PATH + "/create-new-version")
  public ResponseEntity<CommitDTO> createNewVersion(@Valid @RequestBody UidBody body)
  {
    CommitDTO commit = this.service.createNewVersion(getSessionId(), body.uid);

    return ResponseEntity.ok(commit);
  }

  @PostMapping(API_PATH + "/remove")
  public ResponseEntity<Void> remove(@Valid @RequestBody UidBody body)
  {
    this.service.delete(getSessionId(), body.uid);

    return ResponseEntity.ok(null);
  }
}
