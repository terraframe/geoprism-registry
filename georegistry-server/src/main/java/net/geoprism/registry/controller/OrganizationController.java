package net.geoprism.registry.controller;

import java.text.ParseException;

import javax.servlet.http.HttpSession;

import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;

import net.geoprism.registry.service.OrganizationService;
import net.geoprism.registry.service.RegistryComponentService;

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
public class OrganizationController extends RunwaySpringController
{
  public static final String       API_PATH = "organization";

  @Autowired
  private OrganizationService      service;

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

  @GetMapping(API_PATH + "/get-all")
  public ResponseEntity<String> getOrganizations(HttpSession session) throws ParseException
  {
    String sessionId = this.getSessionId();

    OrganizationDTO[] orgs = this.service.getOrganizations(sessionId, null);
    CustomSerializer serializer = this.registryService.serializer(sessionId);

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

  @PostMapping(API_PATH + "/create")
  public ResponseEntity<String> submitNewOrganization(HttpSession session, @RequestBody String json)
  {
    String sessionId = this.getSessionId();

    OrganizationDTO org = this.service.createOrganization(sessionId, json);
    CustomSerializer serializer = this.registryService.serializer(sessionId);

    return new ResponseEntity<String>(org.toJSON(serializer).toString(), HttpStatus.CREATED);
  }

  /**
   * Delete organization.
   * 
   * @param sessionId
   * @param json
   */
  @PostMapping(API_PATH + "/delete")
  public ResponseEntity<Void> removeOrganization(HttpSession session, @RequestBody String code)
  {
    String sessionId = this.getSessionId();

    this.service.deleteOrganization(sessionId, code);

    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
  }

  /**
   * Update organization.
   * 
   * @param sessionId
   * @param json
   */
  @PostMapping(API_PATH + "/update")
  public ResponseEntity<String> updateOrganization(HttpSession session, @RequestBody String json)
  {
    String sessionId = this.getSessionId();

    OrganizationDTO org = this.service.updateOrganization(sessionId, json);
    CustomSerializer serializer = this.registryService.serializer(sessionId);

    return new ResponseEntity<String>(org.toJSON(serializer).toString(), HttpStatus.OK);
  }
}
