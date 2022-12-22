package net.geoprism.registry.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.hibernate.validator.constraints.NotEmpty;
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
  private RegistryComponentService    service;

  /**
   * Create the {@link HierarchyType} from the given JSON.
   * 
   * @param sessionId
   * @param htJSON
   *          JSON of the {@link HierarchyType} to be created.
   * @throws IOException
   */
  @PostMapping("cgr/import-types")
  public ResponseEntity<Void> importTypes(@Valid @ModelAttribute ImportTypeBody body) throws IOException
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
    OrganizationDTO[] orgs = new OrganizationService().getOrganizations(this.getSessionId(), null); // TODO : This violates autowiring principles
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
    String response = this.service.getCurrentLocale(this.getSessionId());
    
    return new ResponseEntity<String>(response, HttpStatus.OK);
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
  

}
