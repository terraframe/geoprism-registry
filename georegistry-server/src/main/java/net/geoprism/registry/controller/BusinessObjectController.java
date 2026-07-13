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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.service.request.BusinessObjectService;
import net.geoprism.registry.view.BusinessTypeDTO;

@RestController
@Validated
@RequestMapping(RegistryConstants.CONTROLLER_ROOT + "business-object")
public class BusinessObjectController extends ObjectController<BusinessObject, BusinessType, BusinessTypeDTO>
{
  public BusinessObjectController(BusinessObjectService service)
  {
    super(service);
  }

  @Override
  protected BusinessObjectService getService()
  {
    return (BusinessObjectService) super.getService();
  }

  @GetMapping("/get-parents")
  public ResponseEntity<String> getParents( //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "code") String code, //
      @NotBlank @RequestParam(name = "edgeTypeCode") String edgeTypeCode, //
      @NotBlank @RequestParam(name = "date") String date)
  {
    JsonArray parents = this.getService().getParents(this.getSessionId(), typeCode, code, edgeTypeCode, GeoRegistryUtil.parseDate(date, true));

    return new ResponseEntity<String>(parents.toString(), HttpStatus.OK);
  }

  @GetMapping("/get-children")
  public ResponseEntity<String> getChildren( //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "code") String code, //
      @NotBlank @RequestParam(name = "edgeTypeCode") String edgeTypeCode, //
      @NotBlank @RequestParam(name = "date") String date)
  {
    JsonArray parents = this.getService().getChildren(this.getSessionId(), typeCode, code, edgeTypeCode, GeoRegistryUtil.parseDate(date, true));

    return new ResponseEntity<String>(parents.toString(), HttpStatus.OK);
  }

}
