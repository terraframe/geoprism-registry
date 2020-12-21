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
package net.geoprism.registry.service;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest.AuthenticationRequestBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
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
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.json.RunwayJsonAdapters;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.IsARelationship;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeMultiTerm;
import com.runwaysdk.system.metadata.MdAttributeTerm;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MdTermRelationshipQuery;

import net.geoprism.account.OauthServer;
import net.geoprism.account.OauthServerIF;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.OrganizationConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.conversion.SupportedLocaleCache;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.geoobjecttype.GeoObjectTypeService;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.OrganizationMetadata;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerLookupRestriction;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class RegistryService
{
  private RegistryAdapter        adapter;

  private ServerGeoObjectService service;

  protected RegistryService()
  {
    this.service = new ServerGeoObjectService();
  }

  public static RegistryService getInstance()
  {
    return ServiceFactory.getRegistryService();
  }

  @Request
  public synchronized void initialize(RegistryAdapter adapter)
  {
    this.adapter = adapter;

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

    MdBusiness univMdBusiness = MdBusiness.getMdBusiness(Universal.CLASS);

    MdTermRelationshipQuery trq = new MdTermRelationshipQuery(qf);
    trq.WHERE(trq.getParentMdBusiness().EQ(univMdBusiness).AND(trq.getChildMdBusiness().EQ(univMdBusiness)));

    OIterator<? extends MdTermRelationship> it2 = trq.getIterator();

    try
    {
      while (it2.hasNext())
      {
        MdTermRelationship mdTermRel = it2.next();

        // Ignore the IsARelationship class between universals. It should be
        // deprecated
        if (mdTermRel.definesType().equals(IsARelationship.CLASS) || mdTermRel.getKey().equals(AllowedIn.CLASS) || mdTermRel.getKey().equals(LocatedIn.CLASS))
        {
          continue;
        }

        ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(mdTermRel);

        ServiceFactory.getMetadataCache().addHierarchyType(ht);
      }
    }
    finally
    {
      it2.close();
    }

    try
    {
      // This is, unfortunately, a big hack. Some patch items need to occur
      // before the organizaiton class is defined
      MdClassDAO.getMdClassDAO(Organization.CLASS);

      OrganizationQuery oQ = new OrganizationQuery(qf);
      OIterator<? extends Organization> it3 = oQ.getIterator();

      try
      {
        while (it3.hasNext())
        {
          Organization organization = it3.next();

          ServiceFactory.getMetadataCache().addOrganization(organization);
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
      String redirect = GeoregistryProperties.getRemoteServerUrl() + "cgrsession/ologin";

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
      
      for (OauthServer server : servers)
      {
        JsonObject json = new JsonObject();
        
        json.add("label", LocalizedValueConverter.convert(server.getDisplayLabel()).toJSON());
        json.addProperty("url", buildOauthServerUrl(server));
        
        ja.add(json);
      }
    }
    else
    {
      OauthServer server = OauthServer.get(id);
      
      JsonObject json = new JsonObject();
      
      json.add("label", LocalizedValueConverter.convert(server.getDisplayLabel()).toJSON());
      json.addProperty("url", buildOauthServerUrl(server));
      
      ja.add(json);
    }
    
    return ja.toString();
  }
  
  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String uid, String geoObjectTypeCode)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(uid, geoObjectTypeCode);

    if (object == null)
    {
      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(uid);
      ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.UID.getName()));
      throw ex;
    }

    ServiceFactory.getGeoObjectPermissionService().enforceCanRead(object.getType().getOrganization().getCode(), object.getType());

    return object.toGeoObject();
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObjectByCode(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF object = service.getGeoObjectByCode(code, typeCode);

    if (object == null)
    {
      // DataNotFoundException ex = new DataNotFoundException();
      // ex.setDataIdentifier(code);
      // throw ex;

      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(code);
      ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
      throw ex;
    }

    ServiceFactory.getGeoObjectPermissionService().enforceCanRead(object.getType().getOrganization().getCode(), object.getType());

    return object.toGeoObject();
  }

  @Request(RequestType.SESSION)
  public GeoObject createGeoObject(String sessionId, String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(adapter, jGeoObj);

    ServerGeoObjectIF object = service.apply(geoObject, true, false);

    return object.toGeoObject();
  }

  @Request(RequestType.SESSION)
  public GeoObject updateGeoObject(String sessionId, String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(adapter, jGeoObj);

    ServerGeoObjectIF object = service.apply(geoObject, false, false);

    return object.toGeoObject();
  }

  @Request(RequestType.SESSION)
  public String[] getUIDS(String sessionId, Integer amount)
  {
    return RegistryIdService.getInstance().getUids(amount);
  }

  @Request(RequestType.SESSION)
  public List<GeoObjectType> getAncestors(String sessionId, String code, String hierarchyCode, Boolean includeInheritedTypes)
  {
    ServerGeoObjectType child = ServerGeoObjectType.get(code);
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyCode);

    return child.getTypeAncestors(hierarchyType, includeInheritedTypes);
  }

  @Request(RequestType.SESSION)
  public ChildTreeNode getChildGeoObjects(String sessionId, String parentUid, String parentGeoObjectTypeCode, String[] childrenTypes, Boolean recursive)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(parentUid, parentGeoObjectTypeCode);

    ServerChildTreeNode node = object.getChildGeoObjects(childrenTypes, recursive);
    
    return node.toNode(true);
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode getParentGeoObjects(String sessionId, String childId, String childGeoObjectTypeCode, String[] parentTypes, boolean recursive, Date forDate)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(childId, childGeoObjectTypeCode);

    if (forDate != null)
    {
      object.setDate(forDate);
    }

    return object.getParentGeoObjects(parentTypes, recursive).toNode(true);
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
      List<OrganizationDTO> cachedOrgs = adapter.getMetadataCache().getAllOrganizations();

      for (OrganizationDTO cachedOrg : cachedOrgs)
      {
        orgs.add(cachedOrg);
      }
    }
    else
    {
      for (int i = 0; i < codes.length; ++i)
      {
        Optional<OrganizationDTO> optional = adapter.getMetadataCache().getOrganization(codes[i]);

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

    final Organization org = new OrganizationConverter().create(organizationDTO);

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

    final Organization org = new OrganizationConverter().update(organizationDTO);

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addOrganization(org);

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
  public GeoObjectType[] getGeoObjectTypes(String sessionId, String[] codes, String[] hierarchies, PermissionContext context)
  {
    List<GeoObjectType> lTypes = new GeoObjectTypeService(adapter).getGeoObjectTypes(codes, hierarchies, context);

    return lTypes.toArray(new GeoObjectType[lTypes.size()]);
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

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addGeoObjectType(type);

    return type.getType();
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
    GeoObjectType geoObjectType = GeoObjectType.fromJSON(gtJSON, adapter);

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(geoObjectType.getOrganizationCode(), geoObjectType.getCode(), geoObjectType.getIsPrivate(), geoObjectType.getLabel().getValue());

    ServerGeoObjectType serverGeoObjectType = ServerGeoObjectType.get(geoObjectType.getCode());

    serverGeoObjectType.update(geoObjectType);

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

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(got.getOrganization().getCode(), got.getCode(), got.getIsPrivate(), got.getLabel().getValue());

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

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(got.getOrganization().getCode(), got.getCode(), got.getIsPrivate(), got.getLabel().getValue());

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

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanWrite(got.getOrganization().getCode(), got.getCode(), got.getIsPrivate(), got.getLabel().getValue());

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

    TermConverter.enforceTermPermissions(parent, Operation.DELETE);

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

        AttributeType attributeType = new AttributeTypeConverter().build((MdAttributeConcreteDAOIF) BusinessFacade.getEntityDAO(mdAttribute));

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

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanDelete(type.getOrganization().getCode(), type.getCode(), type.getIsPrivate(), type.getLabel().getValue());

    if (type != null)
    {
      type.delete();
    }
  }

  @Request(RequestType.SESSION)
  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String hierarchyCode, Date date)
  {
    if (date == null)
    {
      date = ValueOverTime.INFINITY_END_DATE;
    }

    // if (date != null)
    // {
    final ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    ServerHierarchyType ht = hierarchyCode != null ? ServerHierarchyType.get(hierarchyCode) : null;

    final VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, date);
    query.setRestriction(new ServerLookupRestriction(text, date, parentCode, ht));
    query.setLimit(10);

    final List<ServerGeoObjectIF> results = query.getResults();

    JsonArray array = new JsonArray();

    for (ServerGeoObjectIF object : results)
    {
      if (ServiceFactory.getGeoObjectPermissionService().canRead(object.getType().getOrganization().getCode(), object.getType()))
      {
        JsonObject result = new JsonObject();
        result.addProperty("id", object.getRunwayId());
        result.addProperty("name", object.getDisplayLabel().getValue());
        result.addProperty(GeoObject.CODE, object.getCode());
        result.addProperty(GeoObject.UID, object.getUid());

        array.add(result);
      }
    }

    return array;

    // }
    // else
    // {
    // GeoObjectQuery query =
    // ServiceFactory.getRegistryService().createQuery(typeCode);
    // query.setRestriction(new LookupRestriction(text, parentCode,
    // hierarchyCode));
    // query.setLimit(10);
    //
    // GeoObjectIterator it = query.getIterator();
    //
    // try
    // {
    // JsonArray results = new JsonArray();
    //
    // while (it.hasNext())
    // {
    // GeoObject object = it.next();
    //
    // JsonObject result = new JsonObject();
    // result.addProperty("id", it.currentOid());
    // result.addProperty("name", object.getLocalizedDisplayLabel());
    // result.addProperty(GeoObject.CODE, object.getCode());
    // result.addProperty(GeoObject.UID, object.getUid());
    //
    // results.add(result);
    // }
    //
    // return results;
    // }
    // finally
    // {
    // it.close();
    // }
    // }
  }

  @Request(RequestType.SESSION)
  public GeoObject newGeoObjectInstance(String sessionId, String geoObjectTypeCode)
  {
    return this.adapter.newGeoObjectInstance(geoObjectTypeCode);
  }

  @Request(RequestType.SESSION)
  public String newGeoObjectInstance2(String sessionId, String geoObjectTypeCode)
  {
    CustomSerializer serializer = ServiceFactory.getRegistryService().serializer(sessionId);
    JSONObject joResp = new JSONObject();

    /**
     * Create a new GeoObject
     */
    GeoObject go = this.adapter.newGeoObjectInstance(geoObjectTypeCode);

    /**
     * Add all locales so the front-end knows what are available.
     */
    LocalizedValue label = new LocalizedValue("");
    label.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "");

    List<Locale> locales = SupportedLocaleCache.getLocales();

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

    final GeoObjectOverTime goot = go.toGeoObjectOverTime();
    ServerParentTreeNodeOverTime pot = go.getParentsOverTime(null, true);

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
  public JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF geoObject = this.service.getGeoObjectByCode(code, typeCode);

    return geoObject.getHierarchiesForGeoObject();
  }

  @Request(RequestType.SESSION)
  public JsonArray getLocales(String sessionId)
  {
    List<Locale> locales = SupportedLocaleCache.getLocales();

    JsonArray array = new JsonArray();
    array.add(MdAttributeLocalInfo.DEFAULT_LOCALE);

    for (Locale locale : locales)
    {
      array.add(locale.toString());
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
    ServerGeoObjectIF goServer = service.getGeoObjectByCode(code, typeCode);

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

    if (object == null)
    {
      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(id);
      ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.UID.getName()));
      throw ex;
    }

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
      objects.add(result.toGeoObject());
    }

    return objects;
  }

}
