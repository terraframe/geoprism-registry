package net.geoprism.georegistry.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractAction;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.IsARelationship;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MdTermRelationshipQuery;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.DefaultConfiguration;
import net.geoprism.georegistry.action.RegistryAction;
import net.geoprism.registry.NoChildForLeafGeoObjectType;

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
        
        GeoObjectType got = ServiceFactory.getConversionService().universalToGeoObjectType(uni);
        
        adapter.getMetadataCache().addGeoObjectType(got);
      }
    }
    finally
    {
      it.close();
    }
    
    MdBusiness univMdBusiness = MdBusiness.getMdBusiness(Universal.CLASS);
    
    MdTermRelationshipQuery trq = new MdTermRelationshipQuery(qf);
    trq.WHERE(trq.getParentMdBusiness().EQ(univMdBusiness).
        AND(trq.getChildMdBusiness().EQ(univMdBusiness)));
    
    OIterator<? extends MdTermRelationship> it2 = trq.getIterator();
    
    
    try
    {
      while (it2.hasNext())
      {
        MdTermRelationship mdTermRel  = it2.next();
        
        // Ignore the IsARelationship class between universals. It should be depricated
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
    return createGeoObjectInTransaction(sessionId, jGeoObj);
  }
  
  @Transaction
  private GeoObject createGeoObjectInTransaction(String sessionId, String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(adapter, jGeoObj);
    
    return ServiceFactory.getUtilities().applyGeoObject(geoObject, true);
  }
  
  @Request(RequestType.SESSION)
  public GeoObject updateGeoObject(String sessionId, String jGeoObj)
  {
    return updateGeoObjectInTransaction(sessionId, jGeoObj);
  }
  
  @Transaction
  private GeoObject updateGeoObjectInTransaction(String sessionId, String jGeoObj)
  {
    GeoObject geoObject = GeoObject.fromJSON(adapter, jGeoObj);
    
    return ServiceFactory.getUtilities().applyGeoObject(geoObject, false);
  }

  @Request(RequestType.SESSION)
  public String[] getUIDS(String sessionId, Integer amount)
  {
    return RegistryIdService.getInstance().getUids(amount);
  }
  
  @Request(RequestType.SESSION)
  public ChildTreeNode getChildGeoObjects(String sessionId, String parentUid, String parentGeoObjectTypeCode, String[] childrenTypes, Boolean recursive)
  {
    GeoObject goParent = ServiceFactory.getUtilities().getGeoObjectById(parentUid, parentGeoObjectTypeCode);
    
    if (goParent.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Leaf nodes cannot have children.");
    }
    
    String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
    
    String[] relationshipTypes = TermUtil.getAllParentRelationships(parentRunwayId);
    Map<String, HierarchyType> htMap = getHierarchyTypeMap(relationshipTypes);
    GeoEntity parent = GeoEntity.get(parentRunwayId);
    
    GeoObject goRoot = ServiceFactory.getConversionService().geoEntityToGeoObject(parent);
    ChildTreeNode tnRoot = new ChildTreeNode(goRoot, null);
    
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parentRunwayId, relationshipTypes);
    for (TermAndRel tnrChild : tnrChildren)
    {
      GeoEntity geChild = (GeoEntity) tnrChild.getTerm();
      Universal uni = geChild.getUniversal();
      
      if (ArrayUtils.contains(childrenTypes, uni.getKey()))
      {
        GeoObject goChild = ServiceFactory.getConversionService().geoEntityToGeoObject(geChild);
        HierarchyType ht = htMap.get(tnrChild.getRelationshipType());
        
        ChildTreeNode tnChild;
        if (recursive)
        {
          tnChild = this.getChildGeoObjects(sessionId, goChild.getUid(), goChild.getType().getCode(), childrenTypes, recursive);
        }
        else
        {
          tnChild = new ChildTreeNode(goChild, ht);
        }
        
        tnRoot.addChild(tnChild);
      }
    }
    
    return tnRoot;
  }

  private Map<String, HierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    Map<String, HierarchyType> map = new HashMap<String, HierarchyType>();
    
    for (String relationshipType : relationshipTypes)
    {
      MdTermRelationship mdRel = (MdTermRelationship)MdTermRelationship.getMdRelationship(relationshipType);
      
      HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdRel);
      
      map.put(relationshipType, ht);
    }
    
    return map;
  }
  

  @Request(RequestType.SESSION)
  public ParentTreeNode getParentGeoObjects(String sessionId, String childId, String childGeoObjectTypeCode, String[] parentTypes, boolean recursive)
  {
    GeoObject goChild = ServiceFactory.getUtilities().getGeoObjectById(childId, childGeoObjectTypeCode);
    
    if (goChild.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Leaf nodes cannot have children.");
    }
    
    String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());
    
    String[] relationshipTypes = TermUtil.getAllChildRelationships(parentRunwayId);
    Map<String, HierarchyType> htMap = getHierarchyTypeMap(relationshipTypes);
    GeoEntity child = GeoEntity.get(parentRunwayId);
    
    GeoObject goRoot = ServiceFactory.getConversionService().geoEntityToGeoObject(child);
    ParentTreeNode tnRoot = new ParentTreeNode(goRoot, null);
    
    TermAndRel[] tnrParents = TermUtil.getDirectAncestors(parentRunwayId, relationshipTypes);
    for (TermAndRel tnrParent : tnrParents)
    {
      GeoEntity geParent = (GeoEntity) tnrParent.getTerm();
      Universal uni = geParent.getUniversal();
      
      if (ArrayUtils.contains(parentTypes, uni.getKey()))
      {
        GeoObject goParent = ServiceFactory.getConversionService().geoEntityToGeoObject(geParent);
        HierarchyType ht = htMap.get(tnrParent.getRelationshipType());
        
        ParentTreeNode tnParent;
        if (recursive)
        {
          tnParent = this.getParentGeoObjects(sessionId, goParent.getUid(), goParent.getType().getCode(), parentTypes, recursive);
        }
        else
        {
          tnParent = new ParentTreeNode(goParent, ht);
        }
        
        tnRoot.addParent(tnParent);
      }
    }
    
    return tnRoot;
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode addChild(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
  {
    return addChildInTransaction(sessionId, parentId, parentGeoObjectTypeCode, childId, childGeoObjectTypeCode, hierarchyCode);
  }
  
  @Transaction
  public ParentTreeNode addChildInTransaction(String sessionId, String parentId, String parentGeoObjectTypeCode, String childId, String childGeoObjectTypeCode, String hierarchyCode)
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
      throw new UnsupportedOperationException("Virtual leaf nodes are not yet supported."); // TODO
    }
    else
    {
      String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());
      GeoEntity geParent = GeoEntity.get(parentRunwayId);
      
      String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());
      GeoEntity geChild = GeoEntity.get(childRunwayId);
      
      
      String mdTermRelGeoEntity = ServiceFactory.getConversionService().buildMdTermRelGeoEntityKey(hierarchyCode);
      
      geChild.addLink(geParent, mdTermRelGeoEntity);
      
      ParentTreeNode node = new ParentTreeNode(goChild, hierarchy);
      node.addParent(new ParentTreeNode(goParent, hierarchy));
      
      return node;
    }
  }
  
  @Request(RequestType.SESSION)
  public void executeActions(String sessionId, String sJson)
  {
    executeActionsInTransaction(sessionId, sJson);
  }
  
  @Transaction
  private void executeActionsInTransaction(String sessionId, String sJson)
  {
    AbstractAction[] actions = AbstractAction.parseActions(sJson);
    
    for (AbstractAction action : actions)
    {
      RegistryAction ra = RegistryAction.convert(action, this, sessionId);
      
      ra.execute();
    }
  }

  @Request(RequestType.SESSION)
  public void deleteGeoObject(String sessionId, String id, String typeCode)
  {
    deleteGeoObjectInTransaction(sessionId, id, typeCode);
  }
  
  @Transaction
  private void deleteGeoObjectInTransaction(String sessionId, String id, String typeCode)
  {
    GeoObject geoObject = ServiceFactory.getUtilities().getGeoObjectById(id, typeCode);
    
    if (geoObject.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Not implemented yet.");
    }
    else
    {
      GeoEntity.get(geoObject.getUid()).delete();
    }
  }
  
  
  ///////////////////// Hierarchy Management /////////////////////
  

  /**
   * Returns the {@link GeoObjectType}s with the given codes or all {@link GeoObjectType}s if no codes are provided.
   * 
   * @param sessionId 
   * @param codes codes of the {@link GeoObjectType}s.
   * @return the {@link GeoObjectType}s with the given codes or all {@link GeoObjectType}s if no codes are provided.
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
      gots[i] = adapter.getMetadataCache().getGeoObjectType(codes[i]).get();
    }
    
    return gots;
  }
  
  /**
   * Creates a {@link GeoObjectType} from the given JSON.
   * 
   * @param sessionId
   * @param gtJSON JSON of the {@link GeoObjectType} to be created.
   * @return newly created {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public GeoObjectType createGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType geoObjectType = GeoObjectType.fromJSON(gtJSON, adapter);
    
    Universal universal = createGeoObjectType(geoObjectType);
    
    // If this did not error out then add to the cache
    adapter.getMetadataCache().addGeoObjectType(geoObjectType);
    
    ((Session)Session.getCurrentSession()).reloadPermissions();
    
    return ServiceFactory.getConversionService().universalToGeoObjectType(universal);
  }
  
  @Transaction
  private Universal createGeoObjectType(GeoObjectType geoObjectType)
  {
    Universal universal = ServiceFactory.getUtilities().createGeoObjectType(geoObjectType);
    
    return universal;
  }
  
  /**
   * Updates the given {@link GeoObjectType} represented as JSON.
   * 
   * @pre given {@link GeoObjectType} must already exist.
   * 
   * @param sessionId
   * @param gtJSON JSON of the {@link GeoObjectType} to be updated.
   * @return updated {@link GeoObjectType}
   */
  @Request(RequestType.SESSION)
  public GeoObjectType updateGeoObjectType(String sessionId, String gtJSON)
  {
    GeoObjectType geoObjectTypeNew = GeoObjectType.fromJSON(gtJSON, adapter);
    
    GeoObjectType geoObjectTypeOld = adapter.getMetadataCache().getGeoObjectType(geoObjectTypeNew.getCode()).get();
    
    GeoObjectType geoObjectTypeModified = geoObjectTypeOld.copy(geoObjectTypeNew);
        
    Universal universal = updateGeoObjectType(geoObjectTypeModified);
    
    GeoObjectType geoObjectTypeModifiedApplied = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

    // If this did not error out then add to the cache
    adapter.getMetadataCache().addGeoObjectType(geoObjectTypeModifiedApplied);
    
    return geoObjectTypeModifiedApplied;
  }
  
  @Transaction
  private Universal updateGeoObjectType(GeoObjectType geoObjectType)
  {
    Universal universal = ServiceFactory.getConversionService().getUniversalFromGeoObjectType(geoObjectType);
    
    universal.lock();
    universal.getDisplayLabel().setValue(geoObjectType.getLocalizedLabel());
    universal.getDescription().setValue(geoObjectType.getLocalizedDescription());
    universal.apply();
    
    MdBusiness mdBusiness = universal.getMdBusiness();
    
    mdBusiness.lock();
    mdBusiness.getDisplayLabel().setValue(universal.getDisplayLabel().getValue());
    mdBusiness.getDescription().setValue(universal.getDescription().getValue());
    mdBusiness.apply();
    
    mdBusiness.unlock();
    
    universal.unlock();
    
    return universal;
  }
  
  /**
   * Deletes the {@link GeoObjectType} with the given code.
   * 
   * @param sessionId
   * @param code code of the {@link GeoObjectType} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteGeoObjectType(String sessionId, String code)
  {
    deleteGeoObjectTypeInTransaction(sessionId, code);
    
    // If we get here then it was successfully deleted
    adapter.getMetadataCache().removeGeoObjectType(code);
  }
  
  @Transaction
  private void deleteGeoObjectTypeInTransaction(String sessionId, String code)
  {
    Universal uni = Universal.getByKey(code);
    uni.delete();
  }
  
  
  /**
   * Returns the {@link HierarchyType}s with the given codes or all {@link HierarchyType}s if no codes are provided.
   * 
   * @param sessionId 
   * @param codes codes of the {@link HierarchyType}s.
   * @return the {@link HierarchyType}s with the given codes or all {@link HierarchyType}s if no codes are provided.
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
   * @param htJSON JSON of the {@link HierarchyType} to be created.
   */
  @Request(RequestType.SESSION)
  public HierarchyType createHierarchyType(String sessionId, String htJSON)
  {
    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, adapter);

    hierarchyType = createHierarchyTypeTransaction(hierarchyType);
    
    // The transaction did not error out, so it is safe to put into the cache.
    adapter.getMetadataCache().addHierarchyType(hierarchyType);
    
    ((Session)Session.getCurrentSession()).reloadPermissions();
    
    return hierarchyType;
  }
  @Transaction
  private HierarchyType createHierarchyTypeTransaction(HierarchyType hierarchyType)
  {
    MdTermRelationship mdTermRelUniversal = ServiceFactory.getConversionService().newHierarchyToMdTermRelForUniversals(hierarchyType);
    mdTermRelUniversal.apply();
    this.grantAdminPermissionsOnMdTermRel(mdTermRelUniversal);
    
    MdTermRelationship mdTermRelGeoEntity = ServiceFactory.getConversionService().newHierarchyToMdTermRelForGeoEntities(hierarchyType);
    mdTermRelGeoEntity.apply();
    this.grantAdminPermissionsOnMdTermRel(mdTermRelGeoEntity); 

    return ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelUniversal);
  }
  private void grantAdminPermissionsOnMdTermRel(MdTermRelationship mdTermRelationship) 
  {
    RoleDAO registryAdminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();
    
    registryAdminRole.grantPermission(Operation.ADD_PARENT, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.ADD_CHILD, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.DELETE_PARENT, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.DELETE_CHILD, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.READ_PARENT, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.READ_CHILD, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.READ_ALL, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.WRITE_ALL, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.CREATE, mdTermRelationship.getOid());
    registryAdminRole.grantPermission(Operation.DELETE, mdTermRelationship.getOid());
  }
  
  
  /**
   * Updates the given {@link HierarchyType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON JSON of the {@link HierarchyType} to be updated.
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
    MdTermRelationship mdTermRelationship = ServiceFactory.getConversionService().existingHierarchyToMdTermRelationiship(hierarchyType);
    
    mdTermRelationship.lock();
    
    mdTermRelationship.getDisplayLabel().setValue(hierarchyType.getLocalizedLabel());
    mdTermRelationship.getDescription().setValue(hierarchyType.getLocalizedDescription());
    mdTermRelationship.apply();
    
    mdTermRelationship.unlock();
    
    HierarchyType returnHierarchyType = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);
    
    return returnHierarchyType;
  }
  
  
  /**
   * Deletes the {@link HierarchyType} with the given code.
   * 
   * @param sessionId
   * @param code code of the {@link HierarchyType} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteHierarchyType(String sessionId, String code)
  {
    deleteHierarchyType(code);
    
    // No error at this point so the transaction completed successfully.
    adapter.getMetadataCache().removeHierarchyType(code);
  }
  @Transaction
  private void deleteHierarchyType(String code)
  {
    String mdTermRelUniversalKey = ConversionService.buildMdTermRelUniversalKey(code);

    MdTermRelationship mdTermRelUniversal = MdTermRelationship.getByKey(mdTermRelUniversalKey);
    mdTermRelUniversal.delete();
    
    
    String mdTermRelGeoEntityKey = ConversionService.buildMdTermRelGeoEntityKey(code);
    
    MdTermRelationship mdTermRelGeoEntity = MdTermRelationship.getByKey(mdTermRelGeoEntityKey);
    mdTermRelGeoEntity.delete();
  }
  
  /**
   * Adds the {@link GeoObjectType} with the given child code to the
   * parent {@link GeoObjectType} with the given code for the 
   * given {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyTypeCode code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode child {@link GeoObjectType}.
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
    
    // No exceptions thrown. Refresh the HierarchyType object to include the new relationships.
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
   * Removes the {@link GeoObjectType} with the given child code from the
   * parent {@link GeoObjectType} with the given code for the 
   * given {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyCode code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode child {@link GeoObjectType}.
   */
  @Request(RequestType.SESSION)
  public HierarchyType removeFromHierarchy(String sessionId, String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    String mdTermRelKey = ConversionService.buildMdTermRelUniversalKey(hierarchyTypeCode);
    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey);
    
    this.removeFromHierarchy(mdTermRelationship, parentGeoObjectTypeCode, childGeoObjectTypeCode);
    
    // No exceptions thrown. Refresh the HierarchyType object to include the new relationships.
    HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);
    
    adapter.getMetadataCache().addHierarchyType(ht);
    
    return ht;
  }
  @Transaction
  private void removeFromHierarchy(MdTermRelationship mdTermRelationship, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parent = Universal.getByKey(parentGeoObjectTypeCode);
    Universal child = Universal.getByKey(childGeoObjectTypeCode);
    
    parent.removeAllChildren(child, mdTermRelationship.definesType()); 
  }
}
