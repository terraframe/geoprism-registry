/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import net.geoprism.GeoprismUser;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.OrganizationQuery;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.OrganizationConverter;
import net.geoprism.registry.conversion.RegistryRoleConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.ServerLookupRestriction;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.query.postgres.GeoObjectIterator;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.query.postgres.LookupRestriction;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

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
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.commongeoregistry.adapter.metadata.RegistryRole;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.metadata.MdClassDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.IsARelationship;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeMultiTerm;
import com.runwaysdk.system.metadata.MdAttributeTerm;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MdTermRelationshipQuery;

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
    adapter.getMetadataCache().rebuild();

    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    OIterator<? extends Universal> it = uq.getIterator();

    try
    {
      while (it.hasNext())
      {
        Universal uni = it.next();

        ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(uni);

        adapter.getMetadataCache().addGeoObjectType(type.getType());
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
        if (mdTermRel.definesType().equals(IsARelationship.CLASS))
        {
          continue;
        }

        ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(mdTermRel);

        adapter.getMetadataCache().addHierarchyType(ht.getType());
      }
    }
    finally
    {
      it2.close();
    }
    
   try
   {
     // This is, unfortunately, a big hack. Some patch items need to occur before the organizaiton class is defined 
     MdClassDAO.getMdClassDAO(Organization.CLASS);
     
     OrganizationQuery oQ = new OrganizationQuery(qf);
     OIterator<? extends Organization> it3 = oQ.getIterator();
     
     try
     {
       while (it3.hasNext())
       {
         Organization organization = it3.next();
         
         OrganizationDTO organizationDTO =  new OrganizationConverter().build(organization);
         
         adapter.getMetadataCache().addOrganization(organizationDTO);
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

  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String uid, String geoObjectTypeCode)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(uid, geoObjectTypeCode);
    
    if (object == null)
    {
	  DataNotFoundException ex = new DataNotFoundException();
	  ex.setDataIdentifier(uid);
      throw ex;
    }

    return object.toGeoObject();
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObjectByCode(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF object = service.getGeoObjectByCode(code, typeCode);
    
    if (object == null)
    {
	  DataNotFoundException ex = new DataNotFoundException();
	  ex.setDataIdentifier(code);
      throw ex;
    }

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
  public List<GeoObjectType> getAncestors(String sessionId, String code, String hierarchyCode)
  {
    ServerGeoObjectType child = ServerGeoObjectType.get(code);

    return ServiceFactory.getUtilities().getAncestors(child, hierarchyCode);
  }

  @Request(RequestType.SESSION)
  public ChildTreeNode getChildGeoObjects(String sessionId, String parentUid, String parentGeoObjectTypeCode, String[] childrenTypes, Boolean recursive)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(parentUid, parentGeoObjectTypeCode);
    return object.getChildGeoObjects(childrenTypes, recursive).toNode();
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode getParentGeoObjects(String sessionId, String childId, String childGeoObjectTypeCode, String[] parentTypes, boolean recursive, Date forDate)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(childId, childGeoObjectTypeCode);

    if (forDate != null)
    {
      object.setDate(forDate);
    }

    return object.getParentGeoObjects(parentTypes, recursive).toNode();
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode addChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    ServerGeoObjectIF parent = this.service.getGeoObject(parentId, parentGeoObjectTypeCode);
    ServerGeoObjectIF child = this.service.getGeoObject(childId, childGeoObjectTypeCode);
    ServerHierarchyType ht = ServerHierarchyType.get(hierarchyCode);

    return parent.addChild(child, ht).toNode();
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    ServerGeoObjectIF parent = this.service.getGeoObject(parentId, parentGeoObjectTypeCode);
    ServerGeoObjectIF child = this.service.getGeoObject(childId, childGeoObjectTypeCode);

    parent.removeChild(child, hierarchyCode);
  }

  /**
   * 
   * @param sessionId
   * @param sJson
   *          - serialized array of AbstractActions
   */
  @Request(RequestType.SESSION)
  public void submitChangeRequest(String sessionId, String sJson)
  {
    GeoRegistryUtil.submitChangeRequest(sJson);
  }

  public GeoObjectQuery createQuery(String typeCode)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

    return new GeoObjectQuery(type);
  }

  ///////////////////// User/Role Management /////////////////////
  
  @Request(RequestType.SESSION)
  public RegistryRole[] getRolesForUser(String sessionId, String userOID)
  {
    GeoprismUser geoPrismUser = GeoprismUser.get(userOID);
    
    OIterator<? extends Roles> i = geoPrismUser.getAllAssignedRole();
    
    ArrayList<RegistryRole> registryRoles = new ArrayList<RegistryRole>();
    
    for (Roles role : i)
    {
      String roleName = role.getRoleName();
      
      if (RegistryRole.Type.isOrgRole(roleName))
      {
        RegistryRole registryRole = new RegistryRoleConverter().build(role);
        registryRoles.add(registryRole);
      }
    }
    
    return (RegistryRole[])registryRoles.toArray();
    
//    return new RegistryRole[]{};
  }
  
  @Request(RequestType.SESSION)
  public GeoprismUser[] getUsersForOrganization(String sessionId, String organizationCode)
  {
//    Organization organization = Organization.getByCode(organizationCode);
//    
//    Roles role = null;
//    
//    role.getAllSingleActor();
    
    return new GeoprismUser[]{};
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
    if (codes == null || codes.length == 0)
    {
      return adapter.getMetadataCache().getAllOrganizations();
    }

    OrganizationDTO[] orgs = new OrganizationDTO[codes.length];

    for (int i = 0; i < codes.length; ++i)
    {
      Optional<OrganizationDTO> optional = adapter.getMetadataCache().getOrganization(codes[i]);

      if (optional.isPresent())
      {
        orgs[i] = optional.get();
      }
      else
      {
        DataNotFoundException ex = new DataNotFoundException();
        ex.setDataIdentifier(codes[i]);
        throw ex;
      }
    }

    return orgs;
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
    OrganizationDTO dto = org.toDTO(); 

    // If this did not error out then add to the cache
    adapter.getMetadataCache().addOrganization(dto);

    return dto;
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
    OrganizationDTO dto = org.toDTO(); 
    
    // If this did not error out then add to the cache
    adapter.getMetadataCache().addOrganization(dto);

    return dto;
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
    organization.delete();
    
    // If this did not error out then remove from the cache
    adapter.getMetadataCache().removeOrganization(code);
  }
  
  
  
  
  
  /**
   * Returns the {@link GeoObjectType}s with the given codes or all
   * {@link GeoObjectType}s if no codes are provided.
   * 
   * @param sessionId
   * @param codes
   *          codes of the {@link GeoObjectType}s.
   * @return the {@link GeoObjectType}s with the given codes or all
   *         {@link GeoObjectType}s if no codes are provided.
   */
  @Request(RequestType.SESSION)
  public GeoObjectType[] getGeoObjectTypes(String sessionId, String[] codes)
  {
    if (codes == null || codes.length == 0)
    {
      return adapter.getMetadataCache().getAllGeoObjectTypes();
    }

    GeoObjectType[] gots = new GeoObjectType[codes.length];

    for (int i = 0; i < codes.length; ++i)
    {
      Optional<GeoObjectType> optional = adapter.getMetadataCache().getGeoObjectType(codes[i]);

      if (optional.isPresent())
      {
        gots[i] = optional.get();
      }
      else
      {
    	DataNotFoundException ex = new DataNotFoundException();
    	ex.setDataIdentifier(codes[i]);
        throw ex;
      }
    }

    return gots;
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
    adapter.getMetadataCache().addGeoObjectType(type.getType());

    /*
     * Create the GeoServer WMS layers
     */
    new WMSService().createGeoServerLayer(type, true);

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
    JsonParser parser = new JsonParser();

    JsonObject termJSONobj = parser.parse(termJSON).getAsJsonObject();

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
   * @param termJSON
   *          JSON of the term object.
   * 
   * @return Updated {@link Term} object.
   */
  @Request(RequestType.SESSION)
  public Term updateTerm(String sessionId, String termJSON)
  {
    JsonObject termJSONobj = new JsonParser().parse(termJSON).getAsJsonObject();

    String termCode = termJSONobj.get(Term.JSON_CODE).getAsString();

    LocalizedValue value = LocalizedValue.fromJSON(termJSONobj.get(Term.JSON_LOCALIZED_LABEL).getAsJsonObject());

    Classifier classifier = TermConverter.updateClassifier(termCode, value);

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
   * @param geoObjectTypeCode
   * @param attributeTypeJSON
   */
  @Request(RequestType.SESSION)
  public void deleteTerm(String sessionId, String termCode)
  {
    String classifierKey = TermConverter.buildClassifierKeyFromTermCode(termCode);

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

      Optional<GeoObjectType> optional = adapter.getMetadataCache().getGeoObjectType(geoObjectTypeCode);

      if (optional.isPresent())
      {
        GeoObjectType geoObjectType = optional.get();

        AttributeType attributeType = new AttributeTypeConverter().build((MdAttributeConcreteDAOIF) BusinessFacade.getEntityDAO(mdAttribute));

        geoObjectType.addAttribute(attributeType);

        adapter.getMetadataCache().addGeoObjectType(geoObjectType);
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
   * Deletes the {@link GeoObjectType} with the given code. Do nothing
   * if the type does not exist.
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
      type.delete();
    }
  }

  /**
   * Returns the {@link HierarchyType}s with the given codes or all
   * {@link HierarchyType}s if no codes are provided.
   * 
   * @param sessionId
   * @param codes
   *          codes of the {@link HierarchyType}s.
   * @return the {@link HierarchyType}s with the given codes or all
   *         {@link HierarchyType}s if no codes are provided.
   */
  @Request(RequestType.SESSION)
  public HierarchyType[] getHierarchyTypes(String sessionId, String[] codes)
  {
    if (codes == null || codes.length == 0)
    {
      return adapter.getMetadataCache().getAllHierarchyTypes();
    }

    List<HierarchyType> hierarchyTypeList = new LinkedList<HierarchyType>();
    for (String code : codes)
    {
      Optional<HierarchyType> oht = adapter.getMetadataCache().getHierachyType(code);

      if (oht.isPresent())
      {
        hierarchyTypeList.add(oht.get());
      }
    }

    HierarchyType[] hierarchies = hierarchyTypeList.toArray(new HierarchyType[hierarchyTypeList.size()]);

    return hierarchies;
  }

  /**
   * Create the {@link HierarchyType} from the given JSON.
   * 
   * @param sessionId
   * @param htJSON
   *          JSON of the {@link HierarchyType} to be created.
   */
  @Request(RequestType.SESSION)
  public HierarchyType createHierarchyType(String sessionId, String htJSON)
  {
    String code = GeoRegistryUtil.createHierarchyType(htJSON);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return adapter.getMetadataCache().getHierachyType(code).get();
  }

  /**
   * Updates the given {@link HierarchyType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON
   *          JSON of the {@link HierarchyType} to be updated.
   */
  @Request(RequestType.SESSION)
  public HierarchyType updateHierarchyType(String sessionId, String htJSON)
  {
    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, adapter);

    ServerHierarchyType type = ServerHierarchyType.get(hierarchyType);
    type.update(hierarchyType);

    return type.getType();
  }

  /**
   * Deletes the {@link HierarchyType} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link HierarchyType} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteHierarchyType(String sessionId, String code)
  {
    ServerHierarchyType type = ServerHierarchyType.get(code);
    type.delete();

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // No error at this point so the transaction completed successfully.
    adapter.getMetadataCache().removeHierarchyType(code);
  }

  /**
   * Adds the {@link GeoObjectType} with the given child code to the parent
   * {@link GeoObjectType} with the given code for the given
   * {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode
   *          parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode
   *          child {@link GeoObjectType}.
   */
  @Request(RequestType.SESSION)
  public HierarchyType addToHierarchy(String sessionId, String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    ServerHierarchyType type = ServerHierarchyType.get(hierarchyTypeCode);
    type.addToHierarchy(parentGeoObjectTypeCode, childGeoObjectTypeCode);

    return type.getType();
  }

  /**
   * Removes the {@link GeoObjectType} with the given child code from the parent
   * {@link GeoObjectType} with the given code for the given
   * {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyCode
   *          code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode
   *          parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode
   *          child {@link GeoObjectType}.
   */
  @Request(RequestType.SESSION)
  public HierarchyType removeFromHierarchy(String sessionId, String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    ServerHierarchyType type = ServerHierarchyType.get(hierarchyTypeCode);
    type.removeChild(parentGeoObjectTypeCode, childGeoObjectTypeCode);

    return type.getType();
  }

  @Request(RequestType.SESSION)
  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String hierarchyCode, Date date)
  {
    if (date != null)
    {
      final ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);

      ServerHierarchyType ht = hierarchyCode != null ? ServerHierarchyType.get(hierarchyCode) : null;

      final VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, date);
      query.setRestriction(new ServerLookupRestriction(text, date, parentCode, ht));
      query.setLimit(10);

      final List<ServerGeoObjectIF> results = query.getResults();

      JsonArray array = new JsonArray();

      for (ServerGeoObjectIF object : results)
      {
        JsonObject result = new JsonObject();
        result.addProperty("id", object.getRunwayId());
        result.addProperty("name", object.getDisplayLabel().getValue());
        result.addProperty(GeoObject.CODE, object.getCode());
        result.addProperty(GeoObject.UID, object.getUid());

        array.add(result);
      }

      return array;

    }
    else
    {
      GeoObjectQuery query = ServiceFactory.getRegistryService().createQuery(typeCode);
      query.setRestriction(new LookupRestriction(text, parentCode, hierarchyCode));
      query.setLimit(10);

      GeoObjectIterator it = query.getIterator();

      try
      {
        JsonArray results = new JsonArray();

        while (it.hasNext())
        {
          GeoObject object = it.next();

          JsonObject result = new JsonObject();
          result.addProperty("id", it.currentOid());
          result.addProperty("name", object.getLocalizedDisplayLabel());
          result.addProperty(GeoObject.CODE, object.getCode());
          result.addProperty(GeoObject.UID, object.getUid());

          results.add(result);
        }

        return results;
      }
      finally
      {
        it.close();
      }
    }
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

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

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

    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    JsonArray hierarchies = AdapterUtilities.getInstance().getHierarchiesForType(type, true);

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
  public JsonArray getHierarchiesForType(String sessionId, String code, Boolean includeTypes)
  {
    ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(code);

    return ServiceFactory.getUtilities().getHierarchiesForType(geoObjectType, includeTypes);
  }

  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForGeoObject(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF geoObject = this.service.getGeoObjectByCode(code, typeCode);

    return geoObject.getHierarchiesForGeoObject();
  }

  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForGeoObjectOverTime(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF geoObject = this.service.getGeoObjectByCode(code, typeCode);
    ServerParentTreeNodeOverTime pot = geoObject.getParentsOverTime(null, true);

    return pot.toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonArray getLocales(String sessionId)
  {
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    JsonArray array = new JsonArray();
    array.add(MdAttributeLocalInfo.DEFAULT_LOCALE);

    for (Locale locale : locales)
    {
      array.add(locale.toString());
    }

    return array;
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

    ServerGeoObjectIF object = service.apply(goTime, false, false);

    return object.toGeoObjectOverTime();
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime createGeoObjectOverTime(String sessionId, String jGeoObj)
  {
    GeoObjectOverTime goTime = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), jGeoObj);

    ServerGeoObjectIF object = service.apply(goTime, true, false);

    return object.toGeoObjectOverTime();
  }

  @Request(RequestType.SESSION)
  public GeoObjectOverTime getGeoObjectOverTime(String sessionId, String id, String typeCode)
  {
    ServerGeoObjectIF object = this.service.getGeoObject(id, typeCode);
    
    if (object == null)
    {
      DataNotFoundException ex = new DataNotFoundException();
      ex.setDataIdentifier(id);
      throw ex;
    }

    return object.toGeoObjectOverTime();
  }
  
}
