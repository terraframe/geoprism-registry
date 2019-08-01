/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.json.JSONException;
import org.json.JSONObject;

import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.business.ValueObjectDTO;
import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.controller.ServletMethod;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestBodyResponse;
import com.runwaysdk.mvc.conversion.ComponentDTOIFToBasicJSON;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.WritePermissionExceptionDTO;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityDTO;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.ExcludeConfiguration;
import net.geoprism.ontology.GeoEntityUtilDTO;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

/**
 * This controller is used by the location manager widget.
 * 
 * @author rrowlands
 *
 */
@Controller(url = "registrylocation")
public class RegistryLocationController
{
  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF fetchGeoObjectFromGeoEntity(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO entity = GeoEntityDTO.get(request, entityId);

    JSONObject joResp = new JSONObject();

    GeoObject go = getGeoObject(request.getSessionId(), entity.getOid());

    // Add the GeoObject to the response
    joResp.put("geoObject", serializeGo(request.getSessionId(), go));
    joResp.put("geoObjectType", new JSONObject(go.getType().toJSON().toString()));
    joResp.put("parentTreeNode", addParentInfoToExistingGO(request.getSessionId(), go));

    return new RestBodyResponse(joResp.toString());
  }

  @Request(RequestType.SESSION)
  private JSONObject addParentInfoToExistingGO(String sessionId, GeoObject child)
  {
    ParentTreeNode ptnChild = RegistryService.getInstance().getParentGeoObjects(sessionId, child.getUid(), child.getType().getCode(), null, false);

    // TODO : We can't show all available options because the auto-complete
    // doesn't know what the GeoObjectType is and can't search without it.
    // HierarchyType[] hts =
    // ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    // for (HierarchyType ht : hts)
    // {
    // boolean alreadyExists = false;
    // for (ParentTreeNode ptn : ptnChild.getParents())
    // {
    // if (ptn.getHierachyType().getCode().equals(ht.getCode()))
    // {
    // alreadyExists = true;
    // }
    // }
    //
    // if (!alreadyExists)
    // {
    // ParentTreeNode ptnParent = new ParentTreeNode(null, ht);
    // ptnChild.addParent(ptnParent);
    // }
    // }

    return new JSONObject(ptnChild.toJSON().toString());
  }

  @Endpoint(method = ServletMethod.POST, error = ErrorSerialization.JSON)
  public ResponseIF editNewGeoObject(ClientRequestIF request, @RequestParamter(name = "universalId") String universalId, @RequestParamter(name = "jsParent") String sjsParent, @RequestParamter(name = "mdRelationshipId") String mdRelationshipId) throws JSONException
  {
    String resp = editNewGeoObjectInReq(request.getSessionId(), universalId, sjsParent, mdRelationshipId);

    return new RestBodyResponse(resp);
  }

  @Request(RequestType.SESSION)
  private String editNewGeoObjectInReq(String sessionId, String universalId, String sjsParent, String mdRelationshipId)
  {
    Universal uni = Universal.get(universalId);

    String gotCode = uni.getKey();

    GeoObject newGo = ServiceFactory.getAdapter().newGeoObjectInstance(gotCode);

    List<Locale> locales = LocalizationFacade.getInstalledLocales();
    for (Locale locale : locales)
    {
      newGo.setDisplayLabel(locale.toString(), "");
    }
    newGo.setDisplayLabel(LocalizedValue.DEFAULT_LOCALE, "");

    JSONObject joResp = new JSONObject();

    // Add the GeoObject to the response
    joResp.put("newGeoObject", serializeGo(sessionId, newGo));
    joResp.put("geoObjectType", new JSONObject(newGo.getType().toJSON().toString()));
    joResp.put("parentTreeNode", addParentInfoToNewGO(sessionId, mdRelationshipId, sjsParent, newGo));

    return joResp.toString();
  }

  @Request(RequestType.SESSION)
  private JSONObject addParentInfoToNewGO(String sessionId, String mdRelationshipId, String sjsParent, GeoObject newGo)
  {
    ServerHierarchyType currentHt = ServerHierarchyType.get(MdTermRelationship.get(mdRelationshipId));
    JSONObject jsParent = new JSONObject(sjsParent);
    GeoObject goParent = ConversionService.getInstance().geoEntityToGeoObject(GeoEntity.get(jsParent.getString("oid")));

    ParentTreeNode ptnChild = new ParentTreeNode(newGo, null);
    ptnChild.addParent(new ParentTreeNode(goParent, currentHt.getType()));

    // TODO : We can't show all available options because the auto-complete
    // doesn't know what the GeoObjectType is and can't search without it.
    // ParentTreeNode ptnChild = new ParentTreeNode(newGo, null);
    //
    // HierarchyType[] hts =
    // ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    // for (HierarchyType ht : hts)
    // {
    // if (currentHt.getCode().equals(ht.getCode()))
    // {
    // ParentTreeNode ptnParent = new ParentTreeNode(goParent, ht);
    // ptnChild.addParent(ptnParent);
    // }
    // else
    // {
    // ParentTreeNode ptnParent = new ParentTreeNode(null, ht);
    // ptnChild.addParent(ptnParent);
    // }
    // }

    return new JSONObject(ptnChild.toJSON().toString());
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF edit(ClientRequestIF request, @RequestParamter(name = "entityId") String entityId) throws JSONException
  {
    GeoEntityDTO entity;
    try
    {
      entity = GeoEntityDTO.lock(request, entityId);
    }
    catch (WritePermissionExceptionDTO e)
    {
      entity = GeoEntityDTO.get(request, entityId);
    }

    ComponentDTOIFToBasicJSON componentDTOToJSON = ComponentDTOIFToBasicJSON.getConverter(entity, new ExcludeConfiguration(GeoEntityDTO.class, GeoEntityDTO.WKT));
    JSONObject joGeoEnt = componentDTOToJSON.populate();

    // Add the GeoObject to the response
    GeoObject go = getGeoObject(request.getSessionId(), entity.getOid());
    joGeoEnt.put("geoObject", serializeGo(request.getSessionId(), go));

    return new RestBodyResponse(joGeoEnt.toString());
  }

  @Request(RequestType.SESSION)
  private GeoObject getGeoObject(String sessionId, String id)
  {
    GeoObject go = ConversionService.getInstance().geoEntityToGeoObject(GeoEntity.get(id));

    return RegistryService.getInstance().getGeoObjectByCode(sessionId, go.getCode(), go.getType().getCode());
  }

  private JSONObject serializeGo(String sessionId, GeoObject go)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    JSONObject joGo = new JSONObject(go.toJSON(serializer).toString());
    joGo.remove("geometry");
    return joGo;
  }

  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "isNew") Boolean isNew, @RequestParamter(name = "geoObject") String sjsGO, @RequestParamter(name = "parentOid") String parentOid, @RequestParamter(name = "existingLayers") String existingLayers, @RequestParamter(name = "parentTreeNode") String sjsPTN) throws JSONException
  {
    return applyInRequest(request.getSessionId(), request, isNew, sjsGO, parentOid, existingLayers, sjsPTN);
  }

  @Request(RequestType.SESSION)
  private ResponseIF applyInRequest(String sessionId, ClientRequestIF request, Boolean isNew, String sjsGO, String parentOid, String existingLayers, String sjsPTN)
  {
    return applyInTrans(request.getSessionId(), request, isNew, sjsGO, parentOid, existingLayers, sjsPTN);
  }

  @Transaction
  private ResponseIF applyInTrans(String sessionId, ClientRequestIF request, Boolean isNew, String sjsGO, String parentOid, String existingLayers, String sjsPTN)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);

    GeoObject go = GeoObject.fromJSON(ServiceFactory.getAdapter(), sjsGO);

    // TODO
    // if (entityDTO.getGeoId() == null || entityDTO.getGeoId().length() == 0)
    // {
    // entityDTO.setGeoId(IDGenerator.nextID());
    // }

    GeoEntityUtilDTO.refreshViews(request, existingLayers);

    if (isNew)
    {
      go = RegistryService.getInstance().createGeoObject(request.getSessionId(), go.toJSON(serializer).toString());

      // GeoObject goParent = getGeoObject(request.getSessionId(), parentOid);
      // RegistryService.getInstance().addChild(request.getSessionId(),
      // goParent.getUid(), goParent.getType().getCode(), goChild.getUid(),
      // goChild.getType().getCode(), "LocatedIn");

      ParentTreeNode ptn = ParentTreeNode.fromJSON(sjsPTN, ServiceFactory.getAdapter());
      new GeoObjectEditorController().applyPtn(sessionId, ptn);
    }
    else
    {
      go = new GeoObjectEditorController().applyInReq(sessionId, sjsPTN, go.toJSON(serializer).toString(), false, null);
    }

    JSONObject object = new JSONObject();
    object.put(GeoEntityDTO.TYPE, ValueObjectDTO.CLASS);
    object.put(GeoEntityDTO.OID, go.getUid());
    object.put(GeoEntityDTO.DISPLAYLABEL, go.getDisplayLabel().getValue());
    object.put(GeoEntityDTO.GEOID, go.getCode());
    object.put(GeoEntityDTO.UNIVERSAL, go.getType().getLabel().getValue());

    object.put("geoObject", serializeGo(sessionId, go));

    return new RestBodyResponse(object);
  }
}
