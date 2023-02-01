package net.geoprism.registry.controller;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.RegistryUrls;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.geoprism.registry.controller.DirectedAcyclicGraphTypeController.CodeBody;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.RegistryComponentService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.spring.JsonObjectDeserializer;

@RestController
@Validated
public class GeoObjectTypeController extends RunwaySpringController
{
  public static class GeoObjectTypeBody
  {
    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject gtJSON;

    public JsonObject getGtJSON()
    {
      return gtJSON;
    }

    public void setGtJSON(JsonObject gtJSON)
    {
      this.gtJSON = gtJSON;
    }
  }

  public static class AttributeBody
  {
    @NotEmpty
    private String     geoObjTypeCode;

    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject attributeType;

    public String getGeoObjTypeCode()
    {
      return geoObjTypeCode;
    }

    public void setGeoObjTypeCode(String geoObjTypeCode)
    {
      this.geoObjTypeCode = geoObjTypeCode;
    }

    public JsonObject getAttributeType()
    {
      return attributeType;
    }

    public void setAttributeType(JsonObject attributeType)
    {
      this.attributeType = attributeType;
    }

  }

  public static class TermBody
  {
    @NotEmpty
    private String     parentTermCode;

    @NotNull
    @JsonDeserialize(using = JsonObjectDeserializer.class)
    private JsonObject termJSON;

    public String getParentTermCode()
    {
      return parentTermCode;
    }

    public void setParentTermCode(String parentTermCode)
    {
      this.parentTermCode = parentTermCode;
    }

    public JsonObject getTermJSON()
    {
      return termJSON;
    }

    public void setTermJSON(JsonObject termJSON)
    {
      this.termJSON = termJSON;
    }
  }

  public static class DeleteTermBody
  {
    @NotEmpty
    private String parentTermCode;

    @NotEmpty
    private String termCode;

    public String getParentTermCode()
    {
      return parentTermCode;
    }

    public void setParentTermCode(String parentTermCode)
    {
      this.parentTermCode = parentTermCode;
    }

    public String getTermCode()
    {
      return termCode;
    }

    public void setTermCode(String termCode)
    {
      this.termCode = termCode;
    }

  }

  public static class AttributeNameBody
  {
    @NotEmpty
    private String geoObjTypeId;

    @NotEmpty
    private String attributeName;

    public String getGeoObjTypeId()
    {
      return geoObjTypeId;
    }

    public void setGeoObjTypeId(String geoObjTypeId)
    {
      this.geoObjTypeId = geoObjTypeId;
    }

    public String getAttributeName()
    {
      return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
      this.attributeName = attributeName;
    }

  }

  public static final String API_PATH = "geoobjecttype";

  @Autowired
  private RegistryComponentService    service;

  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_ADD_ATTRIBUTE)
  public ResponseEntity<String> createAttributeType(@Valid @RequestBody AttributeBody body)
  {
    AttributeType attrType = this.service.createAttributeType(this.getSessionId(), body.geoObjTypeCode, body.attributeType.toString());

    JsonObject response = attrType.toJSON();
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_UPDATE_ATTRIBUTE)
  public ResponseEntity<String> updateAttributeType(@Valid
  @RequestBody AttributeBody body)
  {
    AttributeType attrType = this.service.updateAttributeType(this.getSessionId(), body.geoObjTypeCode, body.attributeType.toString());

    JsonObject response = attrType.toJSON();
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_DELETE_ATTRIBUTE)
  public ResponseEntity<Void> deleteAttributeType(@Valid
  @RequestBody AttributeNameBody body)
  {
    this.service.deleteAttributeType(this.getSessionId(), body.geoObjTypeId, body.attributeName);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_ADD_TERM)
  public ResponseEntity<String> createTerm(@Valid @RequestBody TermBody body)
  {
    Term term = this.service.createTerm(this.getSessionId(), body.parentTermCode, body.termJSON.toString());

    JsonObject response = term.toJSON();
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_UPDATE_TERM)
  public ResponseEntity<String> updateTerm(@Valid
  @RequestBody TermBody body)
  {
    Term term = this.service.updateTerm(this.getSessionId(), body.parentTermCode, body.termJSON.toString());

    JsonObject response = term.toJSON();
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_DELETE_TERM)
  public ResponseEntity<Void> deleteTerm(@Valid @RequestBody DeleteTermBody body)
  {
    this.service.deleteTerm(this.getSessionId(), body.parentTermCode, body.termCode);

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  /**
   * Returns an array of {@link GeoOjectType} objects that define the given list
   * of types.
   *
   * @pre @post
   *
   * @param types
   *          A serialized json array of GeoObjectType codes. If blank then all
   *          GeoObjectType objects are returned.
   * @param hierarchies
   *          A serialized json array of HierarchyType codes. If blank then
   *          GeoObjectTypes belonging to all hierarchies are returned.
   *
   * @returns @throws
   **/
  @GetMapping(RegistryUrls.GEO_OBJECT_TYPE_GET_ALL)
  public ResponseEntity<String> getGeoObjectTypes(
      @RequestParam(required = false) String types,
      @RequestParam(required = false) String context)
  {
    String[] aTypes = null;
    if (types != null)
    {
      JSONArray jaTypes = new JSONArray(types);

      aTypes = new String[jaTypes.length()];
      for (int i = 0; i < jaTypes.length(); i++)
      {
        aTypes[i] = jaTypes.getString(i);
      }
    }

    PermissionContext pContext = PermissionContext.get(context);

    GeoObjectType[] gots = this.service.getGeoObjectTypes(this.getSessionId(), aTypes, pContext);

    JsonArray jarray = new JsonArray();
    for (int i = 0; i < gots.length; ++i)
    {
      JsonObject jo = this.service.serialize(this.getSessionId(), gots[i]);

      jarray.add(jo);
    }

    return new ResponseEntity<String>(jarray.toString(), HttpStatus.OK);
  }

  /**
   * Returns an array of (label, code) pairs that define all of the geo object
   * types in the system.
   *
   * @pre
   * @post
   *
   * @returns @throws
   **/
  @GetMapping(API_PATH + "/list-types")
  public ResponseEntity<String> listGeoObjectTypes(@NotNull
  @RequestParam Boolean includeAbstractTypes)
  {
    GeoObjectType[] gots = this.service.getGeoObjectTypes(this.getSessionId(), null, PermissionContext.READ);

    Arrays.sort(gots, new Comparator<GeoObjectType>()
    {
      @Override
      public int compare(GeoObjectType o1, GeoObjectType o2)
      {
        return o1.getLabel().getValue().compareTo(o2.getLabel().getValue());
      }
    });

    JsonArray jarray = new JsonArray();

    for (int i = 0; i < gots.length; ++i)
    {
      GeoObjectType geoObjectType = gots[i];

      if (!geoObjectType.getCode().equals("ROOT") && ( includeAbstractTypes || !geoObjectType.getIsAbstract() ))
      {
        JsonObject type = new JsonObject();
        type.addProperty("label", geoObjectType.getLabel().getValue());
        type.addProperty("code", geoObjectType.getCode());
        type.addProperty("orgCode", geoObjectType.getOrganizationCode());
        type.addProperty("superTypeCode", geoObjectType.getSuperTypeCode());

        jarray.add(type);
      }
    }

    return new ResponseEntity<String>(jarray.toString(), HttpStatus.OK);
  }

  /**
   * Creates a {@link GeoObjectType} from the given JSON.
   * 
   * @param request
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be created.
   */
  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_CREATE)
  public ResponseEntity<String> createGeoObjectType(@Valid
  @RequestBody GeoObjectTypeBody body)
  {
    GeoObjectType geoObjectType = this.service.createGeoObjectType(this.getSessionId(), body.gtJSON.toString());

    JsonObject response = this.service.serialize(this.getSessionId(), geoObjectType);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * Updates the given {@link GeoObjectType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be updated.
   */
  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_UPDATE)
  public ResponseEntity<String> updateGeoObjectType(@Valid
  @RequestBody GeoObjectTypeBody body)
  {
    GeoObjectType geoObjectType = this.service.updateGeoObjectType(this.getSessionId(), body.gtJSON.toString());

    JsonObject response = this.service.serialize(this.getSessionId(), geoObjectType);
    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  /**
   * Deletes the {@link GeoObjectType} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link GeoObjectType} to delete.
   */
  @PostMapping(RegistryUrls.GEO_OBJECT_TYPE_DELETE)
  public ResponseEntity<Void> deleteGeoObjectType(@Valid
  @RequestBody CodeBody body)
  {
    this.service.deleteGeoObjectType(this.getSessionId(), body.getCode());

    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-ancestors")
  public ResponseEntity<String> getTypeAncestors(@NotEmpty
  @RequestParam String code,
      @NotEmpty
      @RequestParam String hierarchyCode, @RequestParam(required = false, defaultValue = "false") Boolean includeInheritedTypes, @RequestParam(required = false, defaultValue = "false") Boolean includeChild)
  {
    JsonArray response = new JsonArray();

    List<GeoObjectType> ancestors = this.service.getAncestors(this.getSessionId(), code, hierarchyCode, includeInheritedTypes, includeChild);

    for (GeoObjectType ancestor : ancestors)
    {
      JsonObject object = new JsonObject();
      object.addProperty("label", ancestor.getLabel().getValue());
      object.addProperty("code", ancestor.getCode());

      response.add(object);
    }

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-hierarchies")
  public ResponseEntity<String> getHierarchiesForType(@NotEmpty
  @RequestParam String code,
      @NotEmpty
      @RequestParam Boolean includeTypes)
  {
    JsonArray response = ServiceFactory.getHierarchyService().getHierarchiesForType(this.getSessionId(), code, includeTypes);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

  @GetMapping(API_PATH + "/get-subtype-hierarchies")
  public ResponseEntity<String> getHierarchiesForSubtypes(@NotEmpty
  @RequestParam String code)
  {
    JsonArray response = ServiceFactory.getHierarchyService().getHierarchiesForSubtypes(this.getSessionId(), code);

    return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
  }

}
