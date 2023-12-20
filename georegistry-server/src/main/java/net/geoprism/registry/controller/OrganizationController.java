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

import java.io.IOException;
import java.text.ParseException;

import javax.validation.Valid;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.controller.DirectedAcyclicGraphTypeController.CodeBody;
import net.geoprism.registry.model.OrganizationView;
import net.geoprism.registry.service.request.GPROrganizationService;
import net.geoprism.registry.service.request.RegistryComponentService;
import net.geoprism.registry.view.Page;

/**
 * Differences in Spring MVC as compared to Runway MVC:
 * 
 * 1. Void return not supported 2. parameters as POST not supported
 * (https://stackoverflow.com/questions/44694305/how-to-accept-json-post-parameters-as-requestparam-in-spring-servlet)
 * 3. Support for better views and error responses
 * 
 * @author rrowlands
 */
@RestController
@Validated
public class OrganizationController extends RunwaySpringController
{
  public static class MoveOrganizationBody
  {
    @NotEmpty
    String code;

    @NotEmpty
    String parentCode;

    public String getCode()
    {
      return code;
    }

    public void setCode(String code)
    {
      this.code = code;
    }

    public String getParentCode()
    {
      return parentCode;
    }

    public void setParentCode(String parentCode)
    {
      this.parentCode = parentCode;
    }
  }

  public static final String       API_PATH = "organization";

  @Autowired
  private GPROrganizationService   service;

  @Autowired
  private RegistryComponentService registryService;

  /**
   * Returns an array of (label, entityId) pairs that under the given
   * parent/hierarchy and have the given label.
   * 
   * @throws ParseException
   *
   * @pre
   * @post
   *
   * @returns @throws
   **/
  @ResponseBody
  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getOrganizations() throws ParseException
  {
    OrganizationDTO[] orgs = this.service.getOrganizations(this.getSessionId(), null);
    CustomSerializer serializer = this.registryService.serializer(this.getSessionId());

    JsonArray orgsJson = new JsonArray();
    for (OrganizationDTO org : orgs)
    {
      orgsJson.add(org.toJSON(serializer));
    }

    return new ResponseEntity<String>(orgsJson.toString(), HttpStatus.OK);
  }

  /**
   * Submit new organization.
   * 
   * @param sessionId
   * @param json
   */
  @ResponseBody
  @PostMapping(API_PATH + "/create")
  public ResponseEntity<String> submitNewOrganization(@RequestBody String json)
  {
    String sessionId = this.getSessionId();

    OrganizationDTO org = this.service.createOrganization(sessionId, json);
    CustomSerializer serializer = this.registryService.serializer(sessionId);

    return new ResponseEntity<String>(org.toJSON(serializer).toString(), HttpStatus.CREATED);
  }

  // /**
  // * Delete organization.
  // *
  // * @param sessionId
  // * @param json
  // */
  // @PostMapping(API_PATH + "/delete")
  // public ResponseEntity<Void> removeOrganization(@RequestBody String code)
  // {
  // this.service.deleteOrganization(this.getSessionId(), code);
  //
  // return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  // }

  /**
   * Update organization.
   * 
   * @param sessionId
   * @param json
   */
  @ResponseBody
  @PostMapping(API_PATH + "/update")
  public ResponseEntity<String> updateOrganization(@RequestBody String json)
  {
    String sessionId = this.getSessionId();

    OrganizationDTO org = this.service.updateOrganization(sessionId, json);
    CustomSerializer serializer = this.registryService.serializer(sessionId);

    return new ResponseEntity<String>(org.toJSON(serializer).toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/move")
  public ResponseEntity<Void> move(@Valid @RequestBody MoveOrganizationBody body)
  {
    this.service.move(this.getSessionId(), body.code, body.parentCode);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @PostMapping(API_PATH + "/remove-parent")
  public ResponseEntity<Void> removeParent(@Valid @RequestBody CodeBody body)
  {
    this.service.removeAllParents(this.getSessionId(), body.getCode());

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(API_PATH + "/get-children")
  public ResponseEntity<String> getChildren(@RequestParam(required = false) String code, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    JsonObject page = this.service.getChildren(this.getSessionId(), code, pageSize, pageNumber);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestor-tree")
  public ResponseEntity<String> getAncestorTree(@RequestParam(required = false) String rootCode, @NotEmpty @RequestParam String code, @RequestParam(required = false) Integer pageSize)
  {
    JsonObject page = this.service.getAncestorTree(this.getSessionId(), rootCode, code, pageSize);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/export-tree")
  public ResponseEntity<String> exportToJson()
  {
    JsonArray nodes = this.service.exportToJson(this.getSessionId());

    return new ResponseEntity<String>(nodes.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/import-tree")
  public ResponseEntity<Void> importJsonTree(@Valid @RequestBody String json)
  {
    this.service.importJsonTree(this.getSessionId(), json);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/import-file")
  public ResponseEntity<Void> importFile(@ModelAttribute MultipartFile file) throws IOException
  {
    this.service.importFile(this.getSessionId(), file);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/page")
  public ResponseEntity<String> page(@RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer pageNumber)
  {
    Page<OrganizationView> page = this.service.getPage(this.getSessionId(), pageSize, pageNumber);

    return new ResponseEntity<String>(page.toJSON().toString(), HttpStatus.OK);
  }
}
