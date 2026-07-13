package net.geoprism.registry.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonObject;

import jakarta.validation.constraints.NotBlank;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.model.graph.ServerObjectVertex;
import net.geoprism.registry.service.request.ObjectService;
import net.geoprism.registry.view.ObjectAndTypeDTO;
import net.geoprism.registry.view.ObjectClassDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;

public abstract class ObjectController<V extends ServerObjectVertex, T extends ObjectClass, D extends ObjectClassDTO> extends RunwaySpringController
{
  private ObjectService<V, T, D> service;

  public ObjectController(ObjectService<V, T, D> service)
  {
    this.service = service;
  }

  protected ObjectService<V, T, D> getService()
  {
    return service;
  }

  @GetMapping("/get")
  public ResponseEntity<ObjectOverTimeDTO> get( //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "code") String code)
  {
    ObjectOverTimeDTO response = service.get(this.getSessionId(), typeCode, code);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/get-type-and-object")
  public ResponseEntity<ObjectAndTypeDTO> getTypeAndObject( //
      @NotBlank @RequestParam(name = "typeCode") String typeCode, //
      @NotBlank @RequestParam(name = "code") String code)
  {
    ObjectAndTypeDTO response = service.getTypeAndObject(this.getSessionId(), typeCode, code);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/data")
  public ResponseEntity<String> data(@NotBlank @RequestParam(name = "typeCode") String typeCode, @RequestParam(required = false, name = "criteria") String criteria)
  {
    JsonObject page = this.service.data(this.getSessionId(), typeCode, criteria);

    return new ResponseEntity<String>(page.toString(), HttpStatus.OK);
  }

}
