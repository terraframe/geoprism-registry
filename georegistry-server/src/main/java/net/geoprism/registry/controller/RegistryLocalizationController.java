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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.localization.LocaleView;
import net.geoprism.registry.service.CGRLocalizationService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class RegistryLocalizationController extends RunwaySpringController
{
  public static final String API_PATH = "localization";
  
  public static class LocaleBody 
  {
    @NotNull
    private String locale;
    
    public String getLocale()
    {
      return locale;
    }
    
    public void setLocale(String locale)
    {
      this.locale = locale;
    }
  }
  
  public static class JsonObjectBody 
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    JsonObject json;
    
    public JsonObject getJson()
    {
      return json;
    }
    
    public void setJson(JsonObject json)
    {
      this.json = json;
    }
  }

  
  @Autowired
  private CGRLocalizationService service;

  @PostMapping(API_PATH + "/importSpreadsheet")    
  public ResponseEntity<Void> importSpreadsheet(@ModelAttribute MultipartFile file)
  {
    service.importSpreadsheet(this.getSessionId(), file);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/set-locale")      
  public ResponseEntity<Void> setLocale(@Valid @RequestBody LocaleBody body)
  {
    service.setLocale(this.getSessionId(), body.getLocale());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/getNewLocaleInformation")
  public ResponseEntity<String> getNewLocaleInformation()
  {
    JsonObject json = this.service.getNewLocaleInformation(this.getSessionId());

    return new ResponseEntity<String>(json.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-locales")        
  public ResponseEntity<String> getLocales()
  {
    final JsonArray locales = ServiceFactory.getRegistryService().getLocales(this.getSessionId());

    return new ResponseEntity<String>(locales.toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/editLocale")      
  public ResponseEntity<String> editLocale(@Valid @RequestBody JsonObjectBody body)
  {
    LocaleView lv = service.editLocaleInRequest(this.getSessionId(), body.json.toString());

    return new ResponseEntity<String>(lv.toJson().toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/installLocale")        
  public ResponseEntity<String> installLocale(@Valid @RequestBody JsonObjectBody body)
  {
    LocaleView lv = service.installLocaleInRequest(this.getSessionId(), body.json.toString());

    return new ResponseEntity<String>(lv.toJson().toString(), HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/uninstallLocale")        
  public ResponseEntity<Void> uninstallLocale(@Valid @RequestBody JsonObjectBody body)
  {
    service.uninstallLocaleInRequest(this.getSessionId(), body.json.toString());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/exportSpreadsheet")          
  public ResponseEntity<InputStreamResource> exportSpreadsheet()
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=localization.xlsx");

    InputStreamResource isr = new InputStreamResource(service.exportSpreadsheet(this.getSessionId()));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);    
  }
}
