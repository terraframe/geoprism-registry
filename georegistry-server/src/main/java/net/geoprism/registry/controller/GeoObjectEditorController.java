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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONException;
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

import net.geoprism.registry.service.GeoObjectEditorServiceIF;
import net.geoprism.registry.spring.JsonArrayDeserializer;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class GeoObjectEditorController extends RunwaySpringController
{
  public static final String API_PATH = "geoobject-editor";

  public static final class CreateGeoObjectBody
  {
    @JsonDeserialize(using = JsonArrayDeserializer.class)
    private JsonArray  parentTreeNode;

    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject geoObject;

    String             masterListId;

    String             notes;

    public JsonArray getParentTreeNode()
    {
      return parentTreeNode;
    }

    public void setParentTreeNode(JsonArray parentTreeNode)
    {
      this.parentTreeNode = parentTreeNode;
    }

    public JsonObject getGeoObject()
    {
      return geoObject;
    }

    public void setGeoObject(JsonObject geoObject)
    {
      this.geoObject = geoObject;
    }

    public String getMasterListId()
    {
      return masterListId;
    }

    public void setMasterListId(String masterListId)
    {
      this.masterListId = masterListId;
    }

    public String getNotes()
    {
      return notes;
    }

    public void setNotes(String notes)
    {
      this.notes = notes;
    }
  }

  public static final class UpdateGeoObjectBody
  {
    @NotEmpty
    private String    geoObjectCode;

    @NotEmpty
    private String    geoObjectTypeCode;

    @JsonDeserialize(using = JsonArrayDeserializer.class)
    private JsonArray actions;

    private String    masterListId;

    private String    notes;

    public String getGeoObjectCode()
    {
      return geoObjectCode;
    }

    public void setGeoObjectCode(String geoObjectCode)
    {
      this.geoObjectCode = geoObjectCode;
    }

    public String getGeoObjectTypeCode()
    {
      return geoObjectTypeCode;
    }

    public void setGeoObjectTypeCode(String geoObjectTypeCode)
    {
      this.geoObjectTypeCode = geoObjectTypeCode;
    }

    public JsonArray getActions()
    {
      return actions;
    }

    public void setActions(JsonArray actions)
    {
      this.actions = actions;
    }

    public String getMasterListId()
    {
      return masterListId;
    }

    public void setMasterListId(String masterListId)
    {
      this.masterListId = masterListId;
    }

    public String getNotes()
    {
      return notes;
    }

    public void setNotes(String notes)
    {
      this.notes = notes;
    }

  }

  @Autowired
  private GeoObjectEditorServiceIF service;

  /**
   * Submits an edit to a Geo-Object. If you are a Registry Contributor, this
   * will create a ChangeRequest with a CreateGeoObjectAction. If you have edit
   * permissions, this will attempt to create the Geo-Object immediately. If you
   * do not have permissions for either a
   * {@link net.geoprism.registry.CGRPermissionException} exception will be
   * thrown.
   * 
   * @param parentTreeNode
   *          A serialized ParentTreeNodeOverTime, which specifies the parent
   *          information.
   * @param geoObject
   *          A serialized GeoObjectOverTime, which specifies the GeoObject to
   *          create.
   * @param masterListId
   *          If this should also refresh a master list after editing, you may
   *          provide that master list id here.
   * @param notes
   *          If you are attempting to create a Change Request, you may provide
   *          notes for that request here.
   * @throws net.geoprism.registry.CGRPermissionException
   * @return A JsonObject with {isChangeRequest: Boolean, changeRequestId:
   *         String}
   * @throws JSONException
   * @throws net.geoprism.registry.CGRPermissionException
   */
  @PostMapping(API_PATH + "/create-geo-object")
  public ResponseEntity<String> createGeoObject(@Valid @RequestBody CreateGeoObjectBody body)
  {
    JsonObject resp = this.service.createGeoObject(this.getSessionId(), body.parentTreeNode.toString(), body.geoObject.toString(), body.masterListId, body.notes);

    return new ResponseEntity<String>(resp.toString(), HttpStatus.OK);
  }

  /**
   * Submits an update to an existing Geo-Object. If you are a Registry
   * Contributor, this will create a ChangeRequest with an
   * UpdateAttributeAction. If you have edit permissions, this will attempt to
   * apply the edit immediately. If you do not have permissions for either a
   * {@link net.geoprism.registry.CGRPermissionException} exception will be
   * thrown.
   * 
   * @param geoObjectCode
   *          The code of the Geo-Object to update.
   * @param geoObjectTypeCode
   *          The code of the type of the Geo-Object to update.
   * @param actions
   *          A serialized json array containing the actions to perform on the
   *          Geo-Object.
   * @param masterListId
   *          If this should also refresh a master list after editing, you may
   *          provide that master list id here.
   * @param notes
   *          If you are attempting to create a Change Request, you may provide
   *          notes for that request here.
   * @return A JsonObject with {isChangeRequest: Boolean, changeRequestId:
   *         String}
   * @throws JSONException
   * @throws net.geoprism.registry.CGRPermissionException
   */
  @PostMapping(API_PATH + "/update-geo-object")
  public ResponseEntity<String> updateGeoObject(@Valid @RequestBody UpdateGeoObjectBody body) throws JSONException
  {
    JsonObject resp = this.service.updateGeoObject(this.getSessionId(), body.geoObjectCode, body.geoObjectTypeCode, body.actions.toString(), body.masterListId, body.notes);

    return new ResponseEntity<String>(resp.toString(), HttpStatus.OK);
  }
}
