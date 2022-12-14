package net.geoprism.registry.controller;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.service.RegistryComponentService;

@RestController
@Validated
public class OauthController extends RunwaySpringController
{
  public static final String API_PATH = "oauth";

  @Autowired
  private RegistryComponentService    service;

  /**
   * Returns an OauthServer configuration with the specified id. If an id is not
   * provided, this endpoint will return all configurations (in your
   * organization).
   * 
   * @param request
   * @param id
   * @return A json array of serialized OauthServer configurations.
   * @throws JSONException
   */
  @GetMapping(API_PATH + "/get")
  public ResponseEntity<String> oauthGetAll(@RequestParam(required = false) String id)
  {
    String json = this.service.oauthGetAll(this.getSessionId(), id);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

  /**
   * Returns information which is available to public users (without
   * permissions) which will allow them to login as an oauth user.
   * 
   * @param request
   * @param id
   * @return A json array of OauthServer configurations with only publicly
   *         available information.
   * @throws JSONException
   */
  @GetMapping(API_PATH + "/get-public")  
  public ResponseEntity<String> oauthGetPublic(@RequestParam(required = false) String id)
  {
    String json = this.service.oauthGetPublic(this.getSessionId(), id);

    return new ResponseEntity<String>(json, HttpStatus.OK);
  }

}
