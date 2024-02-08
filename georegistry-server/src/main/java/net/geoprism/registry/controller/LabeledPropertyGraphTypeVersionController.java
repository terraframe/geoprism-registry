package net.geoprism.registry.controller;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.geoprism.registry.controller.LabeledPropertyGraphTypeController.OidBody;
import net.geoprism.registry.service.request.LabeledPropertyGraphTypeVersionServiceIF;

@RestController
@Validated
public class LabeledPropertyGraphTypeVersionController extends RunwaySpringController
{
  public static final String                       API_PATH = "lpg-version";

  @Autowired
  private LabeledPropertyGraphTypeVersionServiceIF service;

  @GetMapping(API_PATH + "/tile")
  public ResponseEntity<InputStreamResource> tile(@RequestParam Integer x, @RequestParam Integer y, @RequestParam Integer z, @NotEmpty @RequestParam String config) throws JSONException
  {
    JSONObject object = new JSONObject(config);
    object.put("x", x);
    object.put("y", y);
    object.put("z", z);

    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/x-protobuf");

    InputStreamResource isr = new InputStreamResource(this.service.getTile(this.getSessionId(), object));
    return new ResponseEntity<InputStreamResource>(isr, headers, HttpStatus.OK);
  }

  @PostMapping(API_PATH + "/create-tiles")
  public ResponseEntity<Void> createTiles(@Valid @RequestBody OidBody body)
  {
    this.service.createTiles(getSessionId(), body.getOid());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

}
