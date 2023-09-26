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
package net.geoprism.registry.service;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.json.RunwayJsonAdapters;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeMultiTerm;
import com.runwaysdk.system.metadata.MdAttributeTerm;

import net.geoprism.account.OauthServer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.gis.geoserver.GeoserverProperties;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.CGRApplication;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.HierarchicalRelationshipType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.conversion.OrganizationConverter;
import net.geoprism.registry.conversion.RegistryAttributeTypeConverter;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.geoobjecttype.GeoObjectTypeService;
import net.geoprism.registry.graph.FhirExternalSystem;
import net.geoprism.registry.hierarchy.HierarchyService;
import net.geoprism.registry.localization.DefaultLocaleView;
import net.geoprism.registry.localization.LocaleView;
import net.geoprism.registry.model.OrganizationMetadata;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.permission.UserPermissionService.RepoPermissionAction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;
import net.geoprism.registry.xml.XMLExporter;

public class RegistryService
{
  private ServerGeoObjectService service = new ServerGeoObjectService();

  @Request
  public synchronized void initialize()
  {
    refreshMetadataCache();
  }

  public void refreshMetadataCache()
  {
    ServiceFactory.getMetadataCache().rebuild();

    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    OIterator<? extends Universal> it = uq.getIterator();

    try
    {
      while (it.hasNext())
      {
        Universal uni = it.next();

        if (uni.getKey().equals(Universal.ROOT_KEY))
        {
          continue;
        }

        ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(uni);

        ServiceFactory.getMetadataCache().addGeoObjectType(type);
      }
    }
    finally
    {
      it.close();
    }

    // We must build the hierarchy types which are inherited first
    // Otherwise you will end up with a NPE when building the hierarchies
    // which inherit the inherited hierarchy if it hasn't been built
    HierarchicalRelationshipType.getInheritedTypes().forEach(relationship -> {
      ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(relationship, false);

      ServiceFactory.getMetadataCache().addHierarchyType(ht);
    });

    HierarchicalRelationshipType.getAll().forEach(relationship -> {
      ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(relationship, false);

      if (!ServiceFactory.getMetadataCache().getHierachyType(ht.getCode()).isPresent())
      {
        ServiceFactory.getMetadataCache().addHierarchyType(ht);
      }
    });

    // Due to inherited hierarchy references, this has to wait until all types
    // exist in the cache.
    // for (ServerHierarchyType type :
    // ServiceFactory.getMetadataCache().getAllHierarchyTypes())
    // {
    //// type.buildHierarchyNodes();
    // }

    try
    {
      // This is, unfortunately, a big hack. Some patch items need to occur
      // before the organization class is defined
      MdClassDAO.getMdClassDAO(Organization.CLASS);

      OrganizationQuery oQ = new OrganizationQuery(qf);
      OIterator<? extends Organization> it3 = oQ.getIterator();

      try
      {
        while (it3.hasNext())
        {
          Organization organization = it3.next();

          ServiceFactory.getMetadataCache().addOrganization(ServerOrganization.get(organization));
        }
      }
      finally
      {
        it3.close();
      }
    }
    catch (com.runwaysdk.dataaccess.cache.DataNotFoundException e)
    {
      // skip for now
    }
  }

  private String buildOauthServerUrl(OauthServer server)
  {
    try
    {
      String redirect = GeoprismProperties.getRemoteServerUrl() + "api/session/ologin";

      JSONObject state = new JSONObject();
      state.put(OauthServerIF.SERVER_ID, server.getOid());

      AuthenticationRequestBuilder builder = OAuthClientRequest.authorizationLocation(server.getAuthorizationLocation());
      builder.setClientId(server.getClientId());
      builder.setRedirectURI(redirect);
      builder.setResponseType("code");
      builder.setState(state.toString());

      OAuthClientRequest request = builder.buildQueryMessage();

      return request.getLocationUri();
    }
    catch (OAuthSystemException | JSONException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Request(RequestType.SESSION)
  public String oauthGetAll(String sessionId, String id)
  {
    final JsonArray ja = new JsonArray();

    if (id == null || id.length() == 0)
    {
      OauthServer[] servers = OauthServer.getAll();

      for (OauthServer server : servers)
      {
        Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
        JsonObject json = (JsonObject) gson2.toJsonTree(server);

        json.addProperty("url", buildOauthServerUrl(server));

        ja.add(json);
      }
    }
    else
    {
      OauthServer server = OauthServer.get(id);

      Gson gson2 = new GsonBuilder().registerTypeAdapter(OauthServer.class, new RunwayJsonAdapters.RunwayDeserializer()).create();
      JsonObject json = (JsonObject) gson2.toJsonTree(server);

      json.addProperty("url", buildOauthServerUrl(server));

      ja.add(json);
    }

    return ja.toString();
  }

  @Request(RequestType.SESSION)
  public String oauthGetPublic(String sessionId, String id)
  {
    final JsonArray ja = new JsonArray();

    if (id == null || id.length() == 0)
    {
      OauthServer[] servers = OauthServer.getAll();

      // Arrays.asList(servers).stream().filter(s ->
      // FhirExternalSystem.isFhirOauth() )

      for (OauthServer server : servers)
      {
        if (!FhirExternalSystem.isFhirOauth(server))
        {
          JsonObject json = new JsonObject();

          json.add("label", RegistryLocalizedValueConverter.convert(server.getDisplayLabel()).toJSON());
          json.addProperty("url", buildOauthServerUrl(server));

          ja.add(json);
        }
      }
    }
    else
    {
      OauthServer server = OauthServer.get(id);

      JsonObject json = new JsonObject();

      json.add("label", RegistryLocalizedValueConverter.convert(server.getDisplayLabel()).toJSON());
      json.addProperty("url", buildOauthServerUrl(server));

      ja.add(json);
    }

    return ja.toString();
  }

  @Request(RequestType.SESSION)
  public JsonObject initHierarchyManager(String sessionId, Boolean publicOnly)
  {
    final JsonArray types = new JsonArray();
    final JsonArray hierarchies = new JsonArray();
    final JsonArray organizations = new JsonArray();
    final CustomSerializer serializer = this.serializer(sessionId);

    OrganizationDTO[] orgDtos = new OrganizationService().getOrganizations(sessionId, null);

    if (publicOnly && UserInfo.isPublicUser())
    {
      ServiceFactory.getAdapter().getMetadataCache().getAllGeoObjectTypes().stream().filter(got -> !got.getIsPrivate()).forEach(got -> {
        types.add(got.toJSON(serializer));
      });
    }
    else
    {
      GeoObjectType[] gots = this.getGeoObjectTypes(sessionId, null, PermissionContext.READ);
      HierarchyType[] hts = ServiceFactory.getHierarchyService().getHierarchyTypes(sessionId, null, PermissionContext.READ);

      for (GeoObjectType got : gots)
      {
        JsonObject joGot = got.toJSON(serializer);

        JsonArray relatedHiers = new JsonArray();

        for (HierarchyType ht : hts)
        {
          List<HierarchyNode> hns = ht.getRootGeoObjectTypes();

          for (HierarchyNode hn : hns)
          {
            if (hn.hierarchyHasGeoObjectType(got.getCode(), true))
            {
              relatedHiers.add(ht.getCode());
            }
          }
        }

        joGot.add("relatedHierarchies", relatedHiers);

        types.add(joGot);
      }

      for (HierarchyType ht : hts)
      {
        hierarchies.add(ht.toJSON(serializer));
      }

    }

    for (OrganizationDTO dto : orgDtos)
    {
      organizations.add(dto.toJSON(serializer));
    }

    JsonObject response = new JsonObject();
    response.add("types", types);
    response.add("hierarchies", hierarchies);
    response.add("organizations", organizations);
    response.add("locales", this.getLocales(sessionId));

    return response;
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String uid, String geoObjectTypeCode, Date date)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(uid, geoObjectTypeCode);

    // TODO: Figure out if this should be deleted. It is impossible to fall into
    // the following branch because this.service.getGeoObject will throw an
    // exception if the object is null

    // if (object == null)
    // {
    // net.geoprism.registry.DataNotFoundException ex = new
    // net.geoprism.registry.DataNotFoundException();
    // ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
    // ex.setDataIdentifier(uid);
    // ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.UID.getName()));
    // throw ex;
    // }

    final GeoObjectPermissionServiceIF pService = ServiceFactory.getGeoObjectPermissionService();
    pService.enforceCanRead(object.getType().getOrganization().getCode(), object.getType());

    ServerGeoObjectType type = object.getType();

    GeoObject geoObject = object.toGeoObject(date);
    geoObject.setWritable(pService.canCreateCR(type.getOrganization().getCode(), type));

    return geoObject;
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObjectByCode(String sessionId, String code, String typeCode, Date date)
  {
    ServerGeoObjectIF object = service.getGeoObjectByCode(code, typeCode, true);

    ServiceFactory.getGeoObjectPermissionService().enforceCanRead(object.getType().getOrganization().getCode(), object.getType());

    return object.toGeoObject(date);
  }

  @Request(RequestType.SESSION)
  public GeoObject createGeoObject(String sessionId, String jGeoObj, Date startDate, Date endDate)
  {
    GeoObject geoObject = GeoObject.fromJSON(ServiceFactory.getAdapter(), jGeoObj);

    ServerGeoObjectIF object = service.apply(geoObject, startDate, endDate, true, false);

    return object.toGeoObject(startDate);
  }

  @Request(RequestType.SESSION)
  public GeoObject updateGeoObject(String sessionId, String jGeoObj, Date startDate, Date endDate)
  {
    GeoObject geoObject = GeoObject.fromJSON(ServiceFactory.getAdapter(), jGeoObj);

    ServerGeoObjectIF object = service.apply(geoObject, startDate, endDate, false, false);

    return object.toGeoObject(startDate);
  }

  @Request(RequestType.SESSION)
  public String[] getUIDS(String sessionId, Integer amount)
  {
    return RegistryIdService.getInstance().getUids(amount);
  }

  @Request(RequestType.SESSION)
  public List<GeoObjectType> getAncestors(String sessionId, String code, String hierarchyCode, Boolean includeInheritedTypes, Boolean includeChild)
  {
    ServerGeoObjectType child = ServerGeoObjectType.get(code);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyCode);

    List<ServerGeoObjectType> ancestors = child.getTypeAncestors(hierarchyType, includeInheritedTypes);

    if (includeChild)
    {
      ancestors.add(child);
    }

    return ancestors.stream().map(stype -> stype.getType()).collect(Collectors.toList());
  }

  @Request(RequestType.SESSION)
  public ChildTreeNode getChildGeoObjects(String sessionId, String parentCode, String parentGeoObjectTypeCode, String hierarchyCode, String[] childrenTypes, Boolean recursive, Date date)
  {
    ServerGeoObjectIF object = this.service.getGeoObjectByCode(parentCode, parentGeoObjectTypeCode, true);

    if (date != null)
    {
      object.setDate(date);
    }

    ServerHierarchyType sht = null;
    if (!StringUtils.isEmpty(hierarchyCode))
    {
      sht = ServerHierarchyType.get(hierarchyCode);
    }

    ServerChildTreeNode node = object.getChildGeoObjects(sht, childrenTypes, recursive, date);

    return node.toNode(true);
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode getParentGeoObjects(String sessionId, String childCode, String childGeoObjectTypeCode, String hierarchyCode, String[] parentTypes, boolean recursive, boolean includeInherited, Date date)
  {
    ServerGeoObjectIF object = this.service.getGeoObjectByCode(childCode, childGeoObjectTypeCode, true);

    if (date != null)
    {
      object.setDate(date);
    }

    ServerHierarchyType sht = null;
    if (!StringUtils.isEmpty(hierarchyCode))
    {
      sht = ServerHierarchyType.get(hierarchyCode);
    }

    return object.getParentGeoObjects(sht, parentTypes, recursive, includeInherited, date).toNode(true);
  }

  public ServerGeoObjectQuery createQuery(String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    return this.service.createQuery(type, null);
  }

  ///////////////////// Hierarchy Management /////////////////////

  /**
   * Returns the {@link OrganizationDTO}s with the given codes or all
   * {@link OrganizationDTO}s if no codes are provided.
   * 
   * @param sessionId
   * @param codes
   *          codes of the {@link OrganizationDTO}s.
   * @return the {@link OrganizationDTO}s with the given codes or all
   *         {@link OrganizationDTO}s if no codes are provided.
   */
  @Request(RequestType.SESSION)
  public OrganizationDTO[] getOrganizations(String sessionId, String[] codes)
  {
    List<OrganizationDTO> orgs = new LinkedList<OrganizationDTO>();

    if (codes == null || codes.length == 0)
    {
      List<OrganizationDTO> cachedOrgs = ServiceFactory.getAdapter().getMetadataCache().getAllOrganizations();

      for (OrganizationDTO cachedOrg : cachedOrgs)
      {
        orgs.add(cachedOrg);
      }
    }
    else
    {
      for (int i = 0; i < codes.length; ++i)
      {
        Optional<OrganizationDTO> optional = ServiceFactory.getAdapter().getMetadataCache().getOrganization(codes[i]);

        if (optional.isPresent())
        {
          orgs.add(optional.get());
        }
        else
        {
          net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
          ex.setTypeLabel(OrganizationMetadata.get().getClassDisplayLabel());
          ex.setDataIdentifier(codes[i]);
          ex.setAttributeLabel(OrganizationMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }
      }
    }

    // Filter out orgs based on permissions
    Iterator<OrganizationDTO> it = orgs.iterator();
    while (it.hasNext())
    {
      OrganizationDTO orgDTO = it.next();

      if (!ServiceFactory.getOrganizationPermissionService().canActorRead(orgDTO.getCode()))
      {
        it.remove();
      }
    }

    return orgs.toArray(new OrganizationDTO[orgs.size()]);
  }

  /**
   * Creates a {@link OrganizationDTO} from the given JSON.
   * 
   * @param sessionId
   * @param json
   *          JSON of the {@link OrganizationDTO} to be created.
   * @return newly created {@link OrganizationDTO}
   */
  @Request(RequestType.SESSION)
  public OrganizationDTO createOrganization(String sessionId, String json)
  {
    OrganizationDTO organizationDTO = OrganizationDTO.fromJSON(json);

    final ServerOrganization org = new OrganizationConverter().create(organizationDTO);

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addOrganization(org);

    return ServiceFactory.getAdapter().getMetadataCache().getOrganization(org.getCode()).get();
  }

  /**
   * Updates the given {@link OrganizationDTO} represented as JSON.
   * 
   * @pre given {@link OrganizationDTO} must already exist.
   * 
   * @param sessionId
   * @param json
   *          JSON of the {@link OrganizationDTO} to be updated.
   * @return updated {@link OrganizationDTO}
   */
  @Request(RequestType.SESSION)
  public OrganizationDTO updateOrganization(String sessionId, String json)
  {
    OrganizationDTO organizationDTO = OrganizationDTO.fromJSON(json);

    final ServerOrganization org = new OrganizationConverter().update(organizationDTO);

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addOrganization(org);

    SerializedListTypeCache.getInstance().clear();

    return ServiceFactory.getAdapter().getMetadataCache().getOrganization(org.getCode()).get();
  }

  /**
   * Deletes the {@link OrganizationDTO} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link OrganizationDTO} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteOrganization(String sessionId, String code)
  {
    Organization organization = Organization.getByKey(code);

    ServiceFactory.getOrganizationPermissionService().enforceActorCanDelete();

    organization.delete();

    SerializedListTypeCache.getInstance().clear();

    // If this did not error out then remove from the cache
    ServiceFactory.getMetadataCache().removeOrganization(code);
  }

  /**
   * Returns the {@link GeoObjectType}s with the given codes or all
   * {@link GeoObjectType}s if no codes are provided.
   * 
   * @param sessionId
   * @param codes
   *          codes of the {@link GeoObjectType}s.
   * @param context
   *          TODO
   * @return the {@link GeoObjectType}s with the given codes or all
   *         {@link GeoObjectType}s if no codes are provided.
   */
  @Request(RequestType.SESSION)
  public GeoObjectType[] getGeoObjectTypes(String sessionId, String[] codes, PermissionContext context)
  {
    List<GeoObjectType> lTypes = new GeoObjectTypeService(ServiceFactory.getAdapter()).getGeoObjectTypes(codes, context);

    return lTypes.toArray(new GeoObjectType[lTypes.size()]);
  }

  @Request(RequestType.SESSION)
  public JsonObject serialize(String sessionId, GeoObjectType got)
  {
    return got.toJSON(this.serializer(sessionId));
  }

  /**
   * Creates a {@link GeoObjectType} from the given JSON.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be created.
   * @return newly created {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    ServerGeoObjectType type = null;

    type = new ServerGeoObjectTypeConverter().create(gtJSON);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addGeoObjectType(type);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));

    return type.getType();
  }

  /**
   * Creates a {@link GeoObjectType} from the given JSON.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be created.
   * @return newly created {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public void importTypes(String sessionId, String orgCode, InputStream istream)
  {
    ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(orgCode, true);

    GeoRegistryUtil.importTypes(orgCode, istream);

    this.refreshMetadataCache();
    SerializedListTypeCache.getInstance().clear();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));
  }

  /**
   * Updates the given {@link GeoObjectType} represented as JSON.
   * 
   * @pre given {@link GeoObjectType} must already exist.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link GeoObjectType} to be updated.
   * @return updated {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public GeoObjectType updateGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType geoObjectType = GeoObjectType.fromJSON(gtJSON, ServiceFactory.getAdapter());
    ServerGeoObjectType serverGeoObjectType = ServerGeoObjectType.get(geoObjectType.getCode());

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(geoObjectType.getOrganizationCode(), serverGeoObjectType, geoObjectType.getIsPrivate());

    serverGeoObjectType.update(geoObjectType);

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.TYPE_CACHE_CHANGE, null));

    return serverGeoObjectType.getType();
  }

  /**
   * Adds an attribute to the given {@link GeoObjectType}.
   * 
   * @pre given {@link GeoObjectType} must already exist.
   * 
   * @param sessionId
   *
   * @param geoObjectTypeCode
   *          string of the {@link GeoObjectType} to be updated.
   * @param attributeTypeJSON
   *          AttributeType to be added to the GeoObjectType
   * @return updated {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public AttributeType createAttributeType(String sessionId, String geoObjectTypeCode, String attributeTypeJSON)
  {
    ServerGeoObjectType got = ServerGeoObjectType.get(geoObjectTypeCode);

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(got.getOrganization().getCode(), got, got.getIsPrivate());

    AttributeType attrType = got.createAttributeType(attributeTypeJSON);

    return attrType;
  }

  /**
   * Updates an attribute in the given {@link GeoObjectType}.
   * 
   * @pre given {@link GeoObjectType} must already exist.
   * 
   * @param sessionId
   * @param geoObjectTypeCode
   *          string of the {@link GeoObjectType} to be updated.
   * @param attributeTypeJSON
   *          AttributeType to be added to the GeoObjectType
   * @return updated {@link AttributeType}
   */
  @Request(RequestType.SESSION)
  public AttributeType updateAttributeType(String sessionId, String geoObjectTypeCode, String attributeTypeJSON)
  {
    ServerGeoObjectType got = ServerGeoObjectType.get(geoObjectTypeCode);

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(got.getOrganization().getCode(), got, got.getIsPrivate());

    AttributeType attrType = got.updateAttributeType(attributeTypeJSON);

    return attrType;
  }

  /**
   * Deletes an attribute from the given {@link GeoObjectType}.
   * 
   * @pre given {@link GeoObjectType} must already exist.
   * @pre given {@link GeoObjectType} must already exist.
   * 
   * @param sessionId
   * @param gtId
   *          string of the {@link GeoObjectType} to be updated.
   * @param attributeName
   *          Name of the attribute to be removed from the GeoObjectType
   * @return updated {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public void deleteAttributeType(String sessionId, String gtId, String attributeName)
  {
    ServerGeoObjectType got = ServerGeoObjectType.get(gtId);

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(got.getOrganization().getCode(), got, got.getIsPrivate());

    got.removeAttribute(attributeName);
  }

  /**
   * Creates a new {@link Term} object and makes it a child of the term with the
   * given code.
   * 
   * @param sessionId
   * @param parentTemCode
   *          The code of the parent [@link Term}.
   * @param termJSON
   *          JSON of the term object.
   * 
   * @return Newly created {@link Term} object.
   */
  @Request(RequestType.SESSION)
  public Term createTerm(String sessionId, String parentTermCode, String termJSON)
  {
    JsonObject termJSONobj = JsonParser.parseString(termJSON).getAsJsonObject();

    LocalizedValue label = LocalizedValue.fromJSON(termJSONobj.get(Term.JSON_LOCALIZED_LABEL).getAsJsonObject());

    Term term = new Term(termJSONobj.get(Term.JSON_CODE).getAsString(), label, new LocalizedValue(""));

    Classifier classifier = TermConverter.createClassifierFromTerm(parentTermCode, term);

    TermConverter termBuilder = new TermConverter(classifier.getKeyName());

    Term returnTerm = termBuilder.build();

    List<MdAttributeConcrete> mdAttrList = this.findRootClassifier(classifier);
    this.refreshAttributeTermTypeInCache(mdAttrList);

    return returnTerm;
  }

  /**
   * Creates a new {@link Term} object and makes it a child of the term with the
   * given code.
   * 
   * @param sessionId
   * @param parentTermCode
   *          TODO
   * @param termJSON
   *          JSON of the term object.
   * @return Updated {@link Term} object.
   */
  @Request(RequestType.SESSION)
  public Term updateTerm(String sessionId, String parentTermCode, String termJSON)
  {
    JsonObject termJSONobj = JsonParser.parseString(termJSON).getAsJsonObject();

    String termCode = termJSONobj.get(Term.JSON_CODE).getAsString();

    LocalizedValue value = LocalizedValue.fromJSON(termJSONobj.get(Term.JSON_LOCALIZED_LABEL).getAsJsonObject());

    Classifier classifier = TermConverter.updateClassifier(parentTermCode, termCode, value);

    TermConverter termBuilder = new TermConverter(classifier.getKeyName());

    Term returnTerm = termBuilder.build();

    List<MdAttributeConcrete> mdAttrList = this.findRootClassifier(classifier);

    this.refreshAttributeTermTypeInCache(mdAttrList);

    return returnTerm;
  }

  /**
   * Deletes the {@link Term} with the given code. All children codoe will be
   * deleted.
   * 
   * @param sessionId
   * @param parentTermCode
   *          TODO
   * @param geoObjectTypeCode
   * @param attributeTypeJSON
   */
  @Request(RequestType.SESSION)
  public void deleteTerm(String sessionId, String parentTermCode, String termCode)
  {
    String parentClassifierKey = TermConverter.buildClassifierKeyFromTermCode(parentTermCode);

    Classifier parent = Classifier.getByKey(parentClassifierKey);

    TermConverter.enforceTermPermissions(parent, RepoPermissionAction.DELETE);

    String classifierKey = Classifier.buildKey(parent.getKey(), termCode);

    Classifier classifier = Classifier.getByKey(classifierKey);

    List<MdAttributeConcrete> mdAttrList = this.findRootClassifier(classifier);

    classifier.delete();

    this.refreshAttributeTermTypeInCache(mdAttrList);
  }

  /**
   * Returns the {@link AttributeTermType}s that use the given term.
   * 
   * @param term
   * @return
   */
  private void refreshAttributeTermTypeInCache(List<MdAttributeConcrete> mdAttrList)
  {
    for (MdAttributeConcrete mdAttribute : mdAttrList)
    {
      String geoObjectTypeCode = mdAttribute.getDefiningMdClass().getTypeName();

      Optional<ServerGeoObjectType> optional = ServiceFactory.getMetadataCache().getGeoObjectType(geoObjectTypeCode);

      if (optional.isPresent())
      {
        ServerGeoObjectType geoObjectType = optional.get();

        AttributeType attributeType = new RegistryAttributeTypeConverter().build((MdAttributeConcreteDAOIF) BusinessFacade.getEntityDAO(mdAttribute));

        geoObjectType.getType().addAttribute(attributeType);

        ServiceFactory.getMetadataCache().addGeoObjectType(geoObjectType);
      }
    }
  }

  private List<MdAttributeConcrete> findRootClassifier(Classifier classifier)
  {
    List<MdAttributeConcrete> mdAttributeList = new LinkedList<MdAttributeConcrete>();

    return this.findRootClassifier(classifier, mdAttributeList);
  }

  private List<MdAttributeConcrete> findRootClassifier(Classifier classifier, List<MdAttributeConcrete> mdAttributeList)
  {
    // Is this a root term for an {@link MdAttributeTerm}
    OIterator<? extends MdAttributeTerm> attrTerm = classifier.getAllClassifierTermAttributeRoots();
    for (MdAttributeTerm mdAttributeTerm : attrTerm)
    {
      mdAttributeList.add(mdAttributeTerm);
    }

    OIterator<? extends MdAttributeMultiTerm> attrMultiTerm = classifier.getAllClassifierMultiTermAttributeRoots();
    for (MdAttributeMultiTerm mdAttributeMultiTerm : attrMultiTerm)
    {
      mdAttributeList.add(mdAttributeMultiTerm);
    }

    // Traverse up the tree
    OIterator<? extends Classifier> parentTerms = classifier.getAllIsAParent();
    for (Classifier parent : parentTerms)
    {
      return this.findRootClassifier(parent, mdAttributeList);
    }

    return mdAttributeList;
  }

  /**
   * Deletes the {@link GeoObjectType} with the given code. Do nothing if the
   * type does not exist.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link GeoObjectType} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteGeoObjectType(String sessionId, String code)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(code);

    if (type != null)
    {
      ServiceFactory.getGeoObjectTypePermissionService().enforceCanDelete(type.getOrganization().getCode(), type, type.getIsPrivate());

      type.delete();
    }
  }

  /*
   * 
   * select $filteredLabel,* from province0 let $dateLabel =
   * first(displayLabel_cot[(date('2020-05-01', 'yyyy-MM-dd') BETWEEN startDate
   * AND endDate) AND (date('2020-05-01', 'yyyy-MM-dd') BETWEEN startDate AND
   * endDate)]), $filteredLabel = COALESCE($dateLabel.value.defaultLocale) where
   * ( displayLabel_cot CONTAINS ( (date('2020-05-01', 'yyyy-MM-dd') BETWEEN
   * startDate AND endDate) AND (date('2020-05-01', 'yyyy-MM-dd') BETWEEN
   * startDate AND endDate) AND COALESCE(value.defaultLocale).toLowerCase() LIKE
   * '%' + 'a' + '%' ) OR code.toLowerCase() LIKE '%' + 'a' + '%' ) AND
   * invalid=false AND ( exists_cot CONTAINS ( (date('2020-05-01', 'yyyy-MM-dd')
   * BETWEEN startDate AND endDate) AND (date('2020-05-01', 'yyyy-MM-dd')
   * BETWEEN startDate AND endDate) AND value=true ) ) ORDER BY $filteredLabel
   * ASC LIMIT 10
   */
  @Request(RequestType.SESSION)
  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String parentTypeCode, String hierarchyCode, Date startDate, Date endDate)
  {
    return VertexServerGeoObject.getGeoObjectSuggestions(text, typeCode, parentCode, parentTypeCode, hierarchyCode, startDate, endDate);
  }

  @Request(RequestType.SESSION)
  public GeoObject newGeoObjectInstance(String sessionId, String geoObjectTypeCode)
  {
    return ServiceFactory.getAdapter().newGeoObjectInstance(geoObjectTypeCode);
  }

  @Request(RequestType.SESSION)
  public String newGeoObjectInstance2(String sessionId, String geoObjectTypeCode)
  {
    CustomSerializer serializer = this.serializer(sessionId);
    JSONObject joResp = new JSONObject();

    /**
     * Create a new GeoObject
     */
    GeoObject go = ServiceFactory.getAdapter().newGeoObjectInstance(geoObjectTypeCode);

    /**
     * Add all locales so the front-end knows what are available.
     */
    LocalizedValue label = new LocalizedValue("");
    label.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "");

    Set<Locale> locales = LocalizationFacade.getInstalledLocales();

    for (Locale locale : locales)
    {
      label.setValue(locale, "");
    }

    go.setValue(DefaultAttribute.DISPLAY_LABEL.getName(), label);

    /**
     * Serialize the GeoObject and add it to the response
     */
    JsonObject jsonObject = go.toJSON(serializer);
    joResp.put("geoObject", new JSONObject(jsonObject.toString()));

    JsonArray hierarchies = ServiceFactory.getHierarchyService().getHierarchiesForType(sessionId, go.getType().getCode(), true);

    joResp.put("hierarchies", new JSONArray(hierarchies.toString()));

    return joResp.toString();
  }

  @Request(RequestType.SESSION)
  public String newGeoObjectInstanceOverTime(String sessionId, String typeCode)
  {
    final ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    ServerGeoObjectIF go = service.newInstance(type);

    go.setInvalid(false);

    final GeoObjectOverTime goot = go.toGeoObjectOverTime();
    ServerParentTreeNodeOverTime pot = go.getParentsOverTime(null, true, true);

    HierarchyService.filterHierarchiesFromPermissions(type, pot);

    /**
     * Serialize the GeoObject and add it to the response
     */
    JsonObject response = new JsonObject();

    response.add("geoObject", goot.toJSON());
    response.add("hierarchies", pot.toJSON());

    return response.toString();
  }

  // private ParentTreeNode ptnFromHierarchyNode(HierarchyNode hn, HierarchyType
  // ht)
  // {
  // List<HierarchyNode> lhnChildren = hn.getChildren();
  //
  // for (HierarchyNode hnChild : lhnChildren)
  // {
  // ParentTreeNode ptnChild = ptnFromHierarchyNode(hnChild, ht);
  //
  // ptnChild.addParent(parents);
  // ParentTreeNode ptnHn = new ParentTreeNode(null, ht);
  // }
  // }

  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode, Date date)
  {
    ServerGeoObjectIF geoObject = this.service.getGeoObjectByCode(code, typeCode);

    return geoObject.getHierarchiesForGeoObject(date);
  }

  @Request(RequestType.SESSION)
  public JsonArray getLocales(String sessionId)
  {
    return getLocales();
  }

  private JsonArray getLocales()
  {
    Set<SupportedLocaleIF> locales = LocalizationFacade.getSupportedLocales();

    JsonArray array = new JsonArray();

    array.add(new DefaultLocaleView().toJson());

    for (SupportedLocaleIF locale : locales)
    {
      array.add(LocaleView.fromSupportedLocale(locale).toJson());
    }

    return array;
  }

  @Request(RequestType.SESSION)
  public String getCurrentLocale(String sessionId)
  {
    Locale locale = Session.getCurrentLocale();

    return locale.toString();
  }

  @Request(RequestType.SESSION)
  public CustomSerializer serializer(String sessionId)
  {
    Locale locale = Session.getCurrentLocale();

    return new LocaleSerializer(locale);
  }

  @Request(RequestType.SESSION)
  public String getGeoObjectBounds(String sessionId, GeoObject geoObject)
  {
    return this.service.getGeoObject(geoObject).bbox(null);
  }

  @Request(RequestType.SESSION)
  public String getGeoObjectBoundsAtDate(String sessionId, GeoObject geoObject, Date date)
  {
    return this.service.getGeoObject(geoObject).bbox(date);
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime getGeoObjectOverTimeByCode(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF goServer = service.getGeoObjectByCode(code, typeCode, true);

    return goServer.toGeoObjectOverTime();
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime updateGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    GeoObjectOverTime goTime = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), jGeoObj);
    ServerGeoObjectType type = ServerGeoObjectType.get(goTime.getType().getCode());

    ServiceFactory.getGeoObjectPermissionService().enforceCanWrite(goTime.getType().getOrganizationCode(), type);

    ServerGeoObjectIF object = service.apply(goTime, false, false);

    return object.toGeoObjectOverTime();
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime createGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    GeoObjectOverTime goTime = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), jGeoObj);
    ServerGeoObjectType type = ServerGeoObjectType.get(goTime.getType().getCode());

    ServiceFactory.getGeoObjectPermissionService().enforceCanCreate(goTime.getType().getOrganizationCode(), type);

    ServerGeoObjectIF object = service.apply(goTime, true, false);

    return object.toGeoObjectOverTime();
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime getGeoObjectOverTime(String sessionId, String id, String typeCode)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(id, typeCode);

    // TODO: Figure out if this should be deleted. It is impossible to fall into
    // the following branch because this.service.getGeoObject will throw an
    // exception if the object is null

    // if (object == null)
    // {
    // net.geoprism.registry.DataNotFoundException ex = new
    // net.geoprism.registry.DataNotFoundException();
    // ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
    // ex.setDataIdentifier(id);
    // ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.UID.getName()));
    // throw ex;
    // }

    ServiceFactory.getGeoObjectPermissionService().enforceCanRead(object.getType().getOrganization().getCode(), object.getType());

    return object.toGeoObjectOverTime();
  }

  @Request(RequestType.SESSION)
  public List<GeoObject> search(String sessionId, String typeCode, String text, Date date)
  {
    List<GeoObject> objects = new LinkedList<GeoObject>();

    if (date == null)
    {
      date = ValueOverTime.INFINITY_END_DATE;
    }

    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    ServerGeoObjectQuery query = this.service.createQuery(type, date);
    query.setRestriction(new ServerSynonymRestriction(text, date));
    query.setLimit(20);

    List<ServerGeoObjectIF> results = query.getResults();

    for (ServerGeoObjectIF result : results)
    {
      objects.add(result.toGeoObject(date));
    }

    return objects;
  }

  @Request(RequestType.SESSION)
  public String getLocalizationMap(String sessionId)
  {
    return new net.geoprism.localization.LocalizationService().getAllView();
  }

  @Request(RequestType.SESSION)
  public JsonObject configuration(String sessionId, String contextPath)
  {
    Locale locale = Session.getCurrentLocale();

    JsonObject config = new JsonObject();
    config.addProperty("contextPath", contextPath);
    config.addProperty("locale", locale.toString());
    config.add("locales", this.getLocales());
    config.addProperty("searchEnabled", GeoregistryProperties.isSearchEnabled());
    config.addProperty("graphVisualizerEnabled", GeoregistryProperties.isGraphVisualizerEnabled());
    config.addProperty("enableBusinessData", GeoregistryProperties.isBusinessDataEnabled());
    config.addProperty("enableLabeledPropertyGraph", GeoregistryProperties.isLabeledPropertyGraphEnabled());
    config.addProperty("mapboxAccessToken", GeoserverProperties.getMapboxglAccessToken());
    config.add("defaultMapBounds", JsonParser.parseString(GeoregistryProperties.getDefaultMapBounds()));
    config.add("localization", JsonParser.parseString(getLocalizationMap(sessionId)));
    config.addProperty("googleanalyticstoken", GeoregistryProperties.getGoogleAnalyticsToken());
    config.addProperty("customFont", GeoregistryProperties.getCustomFont());

    return config;
  }

  @Request(RequestType.SESSION)
  public List<CGRApplication> getApplications(String sessionId)
  {
    return CGRApplication.getApplications();
  }

  @Request(RequestType.SESSION)
  public InputStream exportTypes(String sessionId, String code)
  {
    ServerOrganization organization = ServerOrganization.getByCode(code);

    XMLExporter exporter = new XMLExporter(organization);
    exporter.build();

    return exporter.write();
  }

  public static RegistryService getInstance()
  {
    return ServiceFactory.getRegistryService();
  }

}
