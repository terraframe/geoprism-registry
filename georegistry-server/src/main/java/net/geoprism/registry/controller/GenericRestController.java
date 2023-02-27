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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.ClientConfigurationService;
import net.geoprism.GeoprismApplication;
import net.geoprism.RoleViewDTO;
import net.geoprism.registry.service.AccountService;
import net.geoprism.registry.service.ExternalSystemService;
import net.geoprism.registry.service.OrganizationService;
import net.geoprism.registry.service.RegistryComponentService;

@RestController
@Validated
public class GenericRestController extends RunwaySpringController
{
  public static final class ImportTypeBody
  {
    @NotEmpty(message = "orgCode requires a value")
    private String        orgCode;

    @NotNull(message = "file requires a value")
    private MultipartFile file;

    public String getOrgCode()
    {
      return orgCode;
    }

    public void setOrgCode(String orgCode)
    {
      this.orgCode = orgCode;
    }

    public MultipartFile getFile()
    {
      return file;
    }

    public void setFile(MultipartFile file)
    {
      this.file = file;
    }
  }

  @Autowired
  private RegistryComponentService service;

  /**
   * Create the {@link HierarchyType} from the given JSON.
   * 
   * @param sessionId
   * @param htJSON
   *          JSON of the {@link HierarchyType} to be created.
   * @throws IOException
   */
  @PostMapping("cgr/import-types")
  public ResponseEntity<Void> importTypes(@Valid
  @ModelAttribute ImportTypeBody body) throws IOException
  {
    try (InputStream istream = body.file.getInputStream())
    {
      service.importTypes(this.getSessionId(), body.orgCode, istream);

      return new ResponseEntity<Void>(HttpStatus.OK);
    }
  }

  /**
   * Returns a map with a list of all the types, all of the hierarchies, and all
   * of the locales currently installed in the system. This endpoint is used to
   * populate the hierarchy manager.
   * 
   * @param request
   * @return
   */
  @GetMapping("cgr/init")
  public ResponseEntity<String> init()
  {
    JsonObject response = this.service.initHierarchyManager(this.getSessionId());
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("cgr/init-settings")
  public ResponseEntity<String> initSettings()
  {
    OrganizationDTO[] orgs = new OrganizationService().getOrganizations(this.getSessionId(), null); // TODO
                                                                                                    // :
                                                                                                    // This
                                                                                                    // violates
                                                                                                    // autowiring
                                                                                                    // principles
    JsonArray jaLocales = this.service.getLocales(this.getSessionId());
    JsonObject esPage = new ExternalSystemService().page(this.getSessionId(), 1, 10);
    JsonObject sraPage = JsonParser.parseString(AccountService.getInstance().getSRAs(this.getSessionId(), 1, 10)).getAsJsonObject();
    CustomSerializer serializer = this.service.serializer(this.getSessionId());

    JsonObject settingsView = new JsonObject();

    JsonArray orgsJson = new JsonArray();
    for (OrganizationDTO org : orgs)
    {
      orgsJson.add(org.toJSON(serializer));
    }
    settingsView.add("organizations", orgsJson);

    settingsView.add("locales", jaLocales);

    settingsView.add("externalSystems", esPage);

    settingsView.add("sras", sraPage);

    return new ResponseEntity<String>(settingsView.toString(), HttpStatus.OK);
  }

  @GetMapping("cgr/current-locale")
  public ResponseEntity<String> getCurrentLocale()
  {
    String locale = this.service.getCurrentLocale(this.getSessionId());

    JsonArray response = new JsonArray();
    response.add(locale);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("cgr/locales")
  public ResponseEntity<String> getLocales()
  {
    JsonArray response = this.service.getLocales(this.getSessionId());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("cgr/localization-map")
  public ResponseEntity<String> localizationMap()
  {
    String response = this.service.getLocalizationMap(this.getSessionId());

    return new ResponseEntity<String>(response, HttpStatus.OK);
  }

  @GetMapping("cgr/configuration")
  public ResponseEntity<String> getConfiguration(HttpServletRequest request)
  {
    JsonObject response = this.service.configuration(this.getSessionId(), request.getContextPath());

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping("cgr/applications")
  public ResponseEntity<String> applications()
  {
    Set<String> roleNames = this.getAssignedRoleNames();

    List<GeoprismApplication> allApplications = ClientConfigurationService.getApplications(this.getClientRequest());

    JSONArray response = new JSONArray();

    for (GeoprismApplication application : allApplications)
    {
      if (application.isValid(roleNames))
      {
        response.put(application.toJSON());
      }
    }

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  private Set<String> getAssignedRoleNames()
  {
    try
    {
      Set<String> roleNames = new TreeSet<String>();

      JSONArray jaUserRoles = new JSONArray(RoleViewDTO.getCurrentRoles(this.getClientRequest()));
      for (int i = 0; i < jaUserRoles.length(); ++i)
      {
        String roleName = jaUserRoles.getString(i);

        roleNames.add(roleName);
      }

      return roleNames;
    }
    catch (JSONException e)
    {
      throw new RuntimeException(e);
    }
  }

}
