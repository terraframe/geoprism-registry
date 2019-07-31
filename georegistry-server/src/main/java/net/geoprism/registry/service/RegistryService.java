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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.RelationshipQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
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

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.NoChildForLeafGeoObjectType;
import net.geoprism.registry.adapter.ServerAdapterFactory;
import net.geoprism.registry.conversion.AttributeTypeBuilder;
import net.geoprism.registry.conversion.ServerGeoObjectTypeBuilder;
import net.geoprism.registry.conversion.TermBuilder;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.GeoObjectIterator;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.query.LookupRestriction;

public class RegistryService
{
  private RegistryAdapter adapter;

  protected RegistryService()
  {
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

        ServerGeoObjectType type = new ServerGeoObjectTypeBuilder().build(uni);

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
        // depricated
        if (mdTermRel.definesType().equals(IsARelationship.CLASS))
        {
          continue;
        }

        HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRel);

        adapter.getMetadataCache().addHierarchyType(ht);
      }
    }
    finally
    {
      it2.close();
    }
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String uid, String geoObjectTypeCode)
  {
    return ServiceFactory.getUtilities().getGeoObjectById(uid, geoObjectTypeCode);
  }

  @Request(RequestType.SESSION)
  public GeoObject getGeoObjectByCode(String sessionId, String code, String typeCode)
  {
    return ServiceFactory.getUtilities().getGeoObjectByCode(code, typeCode);
  }

  @Request(RequestType.SESSION)
  public GeoObject createGeoObject(String sessionId, String jGeoObj)
  {
    return createGeoObjectInTransaction(jGeoObj);
  }

  @Transaction
  public GeoObject createGeoObjectInTransaction(String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(adapter, jGeoObj);

    return ServiceFactory.getUtilities().applyGeoObject(geoObject, true, null, false);
  }

  @Request(RequestType.SESSION)
  public GeoObject updateGeoObject(String sessionId, String jGeoObj)
  {
    return updateGeoObjectInTransaction(jGeoObj);
  }

  @Transaction
  public GeoObject updateGeoObjectInTransaction(String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(adapter, jGeoObj);

    return ServiceFactory.getUtilities().applyGeoObject(geoObject, false, null, false);
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
    return ServiceFactory.getUtilities().getChildGeoObjects(parentUid, parentGeoObjectTypeCode, childrenTypes, recursive);
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode getParentGeoObjects(String sessionId, String childId, String childGeoObjectTypeCode, String[] parentTypes, boolean recursive)
  {
    return ServiceFactory.getUtilities().getParentGeoObjects(childId, childGeoObjectTypeCode, parentTypes, recursive);
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode addChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    return addChildInTransaction(parentId, parentGeoObjectTypeCode, childId, childGeoObjectTypeCode, hierarchyCode);
  }

  @Transaction
  public ParentTreeNode addChildInTransaction(String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    GeoObject goParent = ServiceFactory.getUtilities().getGeoObjectById(parentId, parentGeoObjectTypeCode);
    GeoObject goChild = ServiceFactory.getUtilities().getGeoObjectById(childId, childGeoObjectTypeCode);
    HierarchyType hierarchy = adapter.getMetadataCache().getHierachyType(hierarchyCode).get();

    if (goParent.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Virtual leaf nodes cannot have children.");
    }
    else if (goChild.getType().isLeaf())
    {
      String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
      String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());

      GeoEntity parent = GeoEntity.get(parentRunwayId);
      Business child = Business.get(childRunwayId);

      Universal parentUniversal = parent.getUniversal();
      Universal childUniversal = ServerGeoObjectType.get(goChild.getType()).getUniversal();

      String refAttrName = ConversionService.getParentReferenceAttributeName(hierarchyCode, parentUniversal);
      String universalRelationshipType = ConversionService.buildMdTermRelUniversalKey(hierarchyCode);

      GeoEntity.validateUniversalRelationship(childUniversal, parentUniversal, universalRelationshipType);

      child.appLock();
      child.setValue(refAttrName, parent.getOid());
      child.apply();

      ParentTreeNode node = new ParentTreeNode(goChild, hierarchy);
      node.addParent(new ParentTreeNode(goParent, hierarchy));

      return node;
    }
    else
    {
      String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
      GeoEntity geParent = GeoEntity.get(parentRunwayId);

      String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());
      GeoEntity geChild = GeoEntity.get(childRunwayId);

      String mdTermRelGeoEntity = ConversionService.buildMdTermRelGeoEntityKey(hierarchyCode);
      String universalRelationshipType = ConversionService.buildMdTermRelUniversalKey(hierarchyCode);

      if (!universalRelationshipType.equals(AllowedIn.CLASS))
      {
        GeoEntity.validateUniversalRelationship(geChild.getUniversal(), geParent.getUniversal(), universalRelationshipType);
      }

      geChild.addLink(geParent, mdTermRelGeoEntity);

      ParentTreeNode node = new ParentTreeNode(goChild, hierarchy);
      node.addParent(new ParentTreeNode(goParent, hierarchy));

      return node;
    }
  }

  public Boolean exists(String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    GeoObject goParent = ServiceFactory.getUtilities().getGeoObjectById(parentId, parentGeoObjectTypeCode);
    GeoObject goChild = ServiceFactory.getUtilities().getGeoObjectById(childId, childGeoObjectTypeCode);

    if (goParent.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Virtual leaf nodes cannot have children.");
    }
    else if (goChild.getType().isLeaf())
    {
      return false;
    }
    else
    {
      String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
      String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());

      String mdTermRelGeoEntity = ConversionService.buildMdTermRelGeoEntityKey(hierarchyCode);

      RelationshipQuery query = new QueryFactory().relationshipQuery(mdTermRelGeoEntity);
      query.WHERE(query.parentOid().EQ(parentRunwayId));
      query.AND(query.childOid().EQ(childRunwayId));

      return ( query.getCount() > 0 );
    }
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    removeChildInTransaction(parentId, parentGeoObjectTypeCode, childId, childGeoObjectTypeCode, hierarchyCode);
  }

  @Transaction
  public void removeChildInTransaction(String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    GeoObject goParent = ServiceFactory.getUtilities().getGeoObjectById(parentId, parentGeoObjectTypeCode);
    GeoObject goChild = ServiceFactory.getUtilities().getGeoObjectById(childId, childGeoObjectTypeCode);

    if (goParent.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Virtual leaf nodes cannot have children.");
    }
    else if (goChild.getType().isLeaf())
    {
      String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
      String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());

      GeoEntity parent = GeoEntity.get(parentRunwayId);
      Business child = Business.get(childRunwayId);

      Universal parentUniversal = parent.getUniversal();
      String refAttrName = ConversionService.getParentReferenceAttributeName(hierarchyCode, parentUniversal);

      child.appLock();
      child.setValue(refAttrName, null);
      child.apply();
    }
    else
    {
      String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
      GeoEntity geParent = GeoEntity.get(parentRunwayId);

      String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());
      GeoEntity geChild = GeoEntity.get(childRunwayId);

      String mdTermRelGeoEntity = ConversionService.buildMdTermRelGeoEntityKey(hierarchyCode);

      geChild.removeLink(geParent, mdTermRelGeoEntity);
    }
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

  ///////////////////// Hierarchy Management /////////////////////

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
        throw new DataNotFoundException("Unable to find Geo Object Type with code [" + codes[i] + "]", MdBusinessDAO.getMdBusinessDAO(Universal.CLASS));
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
    ServerGeoObjectType type = new ServerGeoObjectTypeBuilder().create(gtJSON);

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

    Classifier classifier = TermBuilder.createClassifierFromTerm(parentTermCode, term);

    TermBuilder termBuilder = new TermBuilder(classifier.getKeyName());

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

    Classifier classifier = TermBuilder.updateClassifier(termCode, value);

    TermBuilder termBuilder = new TermBuilder(classifier.getKeyName());

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
    String classifierKey = TermBuilder.buildClassifierKeyFromTermCode(termCode);

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

        AttributeType attributeType = new AttributeTypeBuilder().build((MdAttributeConcreteDAOIF) BusinessFacade.getEntityDAO(mdAttribute));

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
   * Deletes the {@link GeoObjectType} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link GeoObjectType} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteGeoObjectType(String sessionId, String code)
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(code);
    type.delete();
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

    hierarchyType = updateHierarchyTypeTransaction(hierarchyType);

    // The transaction did not error out, so it is safe to put into the cache.
    adapter.getMetadataCache().addHierarchyType(hierarchyType);

    return hierarchyType;
  }

  @Transaction
  private HierarchyType updateHierarchyTypeTransaction(HierarchyType hierarchyType)
  {
    MdTermRelationship mdTermRelationship = ServiceFactory.getConversionService().existingHierarchyToUniversalMdTermRelationiship(hierarchyType);

    if (mdTermRelationship.definesType().equals(AllowedIn.CLASS))
    {
      mdTermRelationship = (MdTermRelationship) MdTermRelationship.getMdRelationship(LocatedIn.CLASS);
    }

    mdTermRelationship.lock();

    ServiceFactory.getConversionService().populate(mdTermRelationship.getDisplayLabel(), hierarchyType.getLabel());
    ServiceFactory.getConversionService().populate(mdTermRelationship.getDescription(), hierarchyType.getDescription());

    mdTermRelationship.apply();

    mdTermRelationship.unlock();

    HierarchyType returnHierarchyType = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);

    return returnHierarchyType;
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
    deleteHierarchyType(code);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    // No error at this point so the transaction completed successfully.
    adapter.getMetadataCache().removeHierarchyType(code);
  }

  @Transaction
  private void deleteHierarchyType(String code)
  {
    String mdTermRelUniversalKey = ConversionService.buildMdTermRelUniversalKey(code);

    MdTermRelationship mdTermRelUniversal = MdTermRelationship.getByKey(mdTermRelUniversalKey);

    Universal.getStrategy().shutdown(mdTermRelUniversal.definesType());

    AttributeHierarchy.deleteByRelationship(mdTermRelUniversal);

    mdTermRelUniversal.delete();

    String mdTermRelGeoEntityKey = ConversionService.buildMdTermRelGeoEntityKey(code);

    MdTermRelationship mdTermRelGeoEntity = MdTermRelationship.getByKey(mdTermRelGeoEntityKey);

    GeoEntity.getStrategy().shutdown(mdTermRelGeoEntity.definesType());

    mdTermRelGeoEntity.delete();

    // /*
    // * Delete the Registry Maintainer role for the hierarchy
    // */
    // RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_PREFIX +
    // code).getBusinessDAO().delete();
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
    String mdTermRelKey = ConversionService.buildMdTermRelUniversalKey(hierarchyTypeCode);
    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey);

    Universal parentUniversal = Universal.getByKey(parentGeoObjectTypeCode);

    if (parentUniversal.getIsLeafType())
    {
      Universal childUniversal = Universal.getByKey(childGeoObjectTypeCode);

      NoChildForLeafGeoObjectType exception = new NoChildForLeafGeoObjectType();

      exception.setChildGeoObjectTypeLabel(childUniversal.getDisplayLabel().getValue());
      exception.setHierarchyTypeLabel(mdTermRelationship.getDisplayLabel().getValue());
      exception.setParentGeoObjectTypeLabel(parentUniversal.getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }

    this.addToHierarchy(hierarchyTypeCode, mdTermRelationship, parentGeoObjectTypeCode, childGeoObjectTypeCode);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);

    adapter.getMetadataCache().addHierarchyType(ht);

    return ht;
  }

  @Transaction
  private void addToHierarchy(String hierarchyTypeCode, MdTermRelationship mdTermRelationship, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parent = Universal.getByKey(parentGeoObjectTypeCode);
    Universal child = Universal.getByKey(childGeoObjectTypeCode);

    child.addLink(parent, mdTermRelationship.definesType());

    if (child.getIsLeafType())
    {
      ConversionService.addParentReferenceToLeafType(hierarchyTypeCode, parent, child);
    }
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
    return ServerGeoObjectType.removeChild(hierarchyTypeCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);
  }

  @Request(RequestType.SESSION)
  public JsonArray getGeoObjectSuggestions(String sessionId, String text, String typeCode, String parentCode, String hierarchyCode)
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

    /**
     * Include information about potential parents
     */
    // JSONArray jaHts = new JSONArray();

    // HierarchyType[] hts =
    // ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    // for (HierarchyType ht : hts)
    // {
    //// JSONObject joHt = new JSONObject();
    ////
    //// joHt.put("ht", new JSONObject(joHt.toString()));
    ////
    //// jaHts.put(joHt);
    //
    // jaHts.put(new JSONObject(ht.toJSON(serializer).toString()));
    // }

    // ParentTreeNode ptnChild = new ParentTreeNode(go, null);

    // HierarchyType[] hts =
    // ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    // for (HierarchyType ht : hts)
    // {
    // List<HierarchyNode> hnlRoots = ht.getRootGeoObjectTypes();
    //
    // for (HierarchyNode hnlRoot : hnlRoots)
    // {
    // ParentTreeNode ptnParent = ptnFromHierarchyNode(hnlRoot);
    // ptnChild.addParent(ptnParent);
    // }
    // }

    ServerGeoObjectType type = ServerGeoObjectType.get(go.getType());

    JsonArray hierarchies = AdapterUtilities.getInstance().getHierarchiesForType(type, true);

    joResp.put("hierarchies", new JSONArray(hierarchies.toString()));

    return joResp.toString();
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
    GeoObject go = this.getGeoObjectByCode(sessionId, code, typeCode);

    return ServiceFactory.getUtilities().getHierarchiesForGeoObject(go);
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
    return ServerAdapterFactory.geoObject(geoObject).bbox();
  }
}
