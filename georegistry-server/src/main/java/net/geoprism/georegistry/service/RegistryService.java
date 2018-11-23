package net.geoprism.georegistry.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.geoprism.DefaultConfiguration;
import net.geoprism.georegistry.AdapterUtilities;
import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.georegistry.action.RegistryAction;
import net.geoprism.registry.NoChildForLeafGeoObjectType;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
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
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.IsARelationship;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.gis.geo.WKTParsingProblem;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MdTermRelationshipQuery;
import com.runwaysdk.system.ontology.TermUtil;
import com.vividsolutions.jts.geom.Geometry;

public class RegistryService
{
  private static ConversionService conversionService;
  
  private static RegistryAdapter adapter;
  
  private static AdapterUtilities util;
  
  public RegistryService()
  {
    initialize();
  }
  
  @Request
  private synchronized void initialize()
  {
    if (RegistryService.adapter == null)
    {
      RegistryService.adapter = new RegistryAdapterServer();
      
      conversionService = new ConversionService(adapter);
      
      util = new AdapterUtilities(adapter, conversionService);
      
      refreshMetadataCache();
    }
  }
  
  public static ConversionService getConversionService()
  {
    return conversionService;
  }
  
  public static RegistryAdapter getRegistryAdapter()
  {
    return adapter;
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
        
        GeoObjectType got = conversionService.universalToGeoObjectType(uni);
        
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
        
        HierarchyType ht = conversionService.mdTermRelationshipToHierarchyType(mdTermRel);

        adapter.getMetadataCache().addHierarchyType(ht);
      }
    }
    finally
    {
      it2.close();
    }
  }
  
  @Request(RequestType.SESSION)
  public GeoObject getGeoObject(String sessionId, String uid)
  {
    GeoEntity geo = GeoEntity.get(uid);
    
    GeoObject geoObject = conversionService.geoEntityToGeoObject(geo);
    
    return geoObject;
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
    
    GeoEntity ge;
    if (geoObject.getUid() != null && geoObject.getUid().length() > 0)
    {
      GeoEntityQuery geq = new GeoEntityQuery(new QueryFactory());
      geq.WHERE(geq.getOid().EQ(geoObject.getUid()));
  
      OIterator<? extends GeoEntity> it = geq.getIterator();
      try
      {
        if (it.hasNext())
        {
          ge = it.next();
          ge.appLock();
        }
        else
        {
          ge = new GeoEntity();
        }
      }
      finally
      {
        it.close();
      }
    }
    else
    {
      ge = new GeoEntity();
    }
    
    if (geoObject.getCode() != null)
    {
      ge.setGeoId(geoObject.getCode());
    }
    
    if (geoObject.getLocalizedDisplayLabel() != null)
    {
      ge.getDisplayLabel().setValue(geoObject.getLocalizedDisplayLabel());
    }
    
    if (geoObject.getType() != null)
    {
      GeoObjectType got = geoObject.getType();
      
      Universal inputUni = conversionService.geoObjectTypeToUniversal(got);
      
      if (inputUni != ge.getUniversal())
      {
        ge.setUniversal(inputUni);
      }
    }
    
    org.locationtech.jts.geom.Geometry geom = geoObject.getGeometry();
    if (geom != null)
    {
      try
      {
        String wkt = geom.toText();
        
        GeometryHelper geometryHelper = new GeometryHelper();
        
        Geometry geo = geometryHelper.parseGeometry(wkt);
        ge.setGeoPoint(geometryHelper.getGeoPoint(geo));
        ge.setGeoMultiPolygon(geometryHelper.getGeoMultiPolygon(geo));
        ge.setWkt(wkt);
      }
      catch (Exception e)
      {
        String msg = "Error parsing WKT";
        
        WKTParsingProblem p = new WKTParsingProblem(msg);
        p.setNotification(ge, GeoEntity.WKT);
        p.setReason(e.getLocalizedMessage());
        p.apply();
        p.throwIt();
      }
    }
    
    // TODO : STATUS
    
    ge.apply();
    
    return geoObject;
  }

  @Request(RequestType.SESSION)
  public String[] getUIDS(String sessionId, Integer amount)
  {
    return IdService.getInstance(sessionId).getUIDS(amount);
  }
  
  @Request(RequestType.SESSION)
  public ChildTreeNode getChildGeoObjects(String sessionId, String parentUid, String[] childrenTypes, Boolean recursive)
  {
    String[] relationshipTypes = TermUtil.getAllParentRelationships(parentUid);
    Map<String, HierarchyType> htMap = getHierarchyTypeMap(relationshipTypes);
    GeoEntity parent = GeoEntity.get(parentUid);
    
    GeoObject goRoot = conversionService.geoEntityToGeoObject(parent);
    ChildTreeNode tnRoot = new ChildTreeNode(goRoot, null);
    
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parentUid, relationshipTypes);
    for (TermAndRel tnrChild : tnrChildren)
    {
      GeoEntity geChild = (GeoEntity) tnrChild.getTerm();
      Universal uni = geChild.getUniversal();
      
      if (ArrayUtils.contains(childrenTypes, uni.getKey()))
      {
        GeoObject goChild = conversionService.geoEntityToGeoObject(geChild);
        HierarchyType ht = htMap.get(tnrChild.getRelationshipType());
        
        ChildTreeNode tnChild;
        if (recursive)
        {
          tnChild = this.getChildGeoObjects(sessionId, geChild.getOid(), childrenTypes, recursive);
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
      
      HierarchyType ht = conversionService.mdTermRelationshipToHierarchyType(mdRel);
      
      map.put(relationshipType, ht);
    }
    
    return map;
  }
  
  private void addGeoObjectRoots(HierarchyType ht)
  {
    // TODO : I'm not sure how this is supposed to work.
  }

  @Request(RequestType.SESSION)
  public ParentTreeNode getParentGeoObjects(String sessionId, String childId, String[] parentTypes, boolean recursive)
  {
    String[] relationshipTypes = TermUtil.getAllChildRelationships(childId);
    Map<String, HierarchyType> htMap = getHierarchyTypeMap(relationshipTypes);
    GeoEntity child = GeoEntity.get(childId);
    
    GeoObject goRoot = conversionService.geoEntityToGeoObject(child);
    ParentTreeNode tnRoot = new ParentTreeNode(goRoot, null);
    
    TermAndRel[] tnrParents = TermUtil.getDirectAncestors(childId, relationshipTypes);
    for (TermAndRel tnrParent : tnrParents)
    {
      GeoEntity geParent = (GeoEntity) tnrParent.getTerm();
      Universal uni = geParent.getUniversal();
      
      if (ArrayUtils.contains(parentTypes, uni.getKey()))
      {
        GeoObject goParent = conversionService.geoEntityToGeoObject(geParent);
        HierarchyType ht = htMap.get(tnrParent.getRelationshipType());
        
        ParentTreeNode tnParent;
        if (recursive)
        {
          tnParent = this.getParentGeoObjects(sessionId, geParent.getOid(), parentTypes, recursive);
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
  public ParentTreeNode addChild(String sessionId, String parentId, String childId, String hierarchyCode)
  {
    return addChildInTransaction(sessionId, parentId, childId, hierarchyCode);
  }
  
  @Transaction
  public ParentTreeNode addChildInTransaction(String sessionId, String parentId, String childId, String hierarchyCode)
  {
    GeoObject goParent = util.getGeoObjectById(parentId);
    GeoObject goChild = util.getGeoObjectById(childId);
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
      GeoEntity geParent = GeoEntity.get(goParent.getUid());
      GeoEntity geChild = GeoEntity.get(goChild.getUid());
      
      
      String mdTermRelGeoEntity = ConversionService.buildMdTermRelGeoEntityKey(hierarchyCode);
      
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
  public void deleteGeoObject(String sessionId, String uid)
  {
    deleteGeoObjectInTransaction(sessionId, uid);
  }
  
  @Transaction
  private void deleteGeoObjectInTransaction(String sessionId, String uid)
  {
    GeoObject geoObject = util.getGeoObjectById(uid);
    
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
    
    return conversionService.universalToGeoObjectType(universal);
  }
  
  @Transaction
  private Universal createGeoObjectType(GeoObjectType geoObjectType)
  {
    Universal universal = conversionService.newGeoObjectTypeToUniversal(geoObjectType);
    
    universal= ConversionService.createMdBusinessForUniversal(universal);
    
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
    
    GeoObjectType geoObjectTypeModifiedApplied = conversionService.universalToGeoObjectType(universal);

    // If this did not error out then add to the cache
    adapter.getMetadataCache().addGeoObjectType(geoObjectTypeModifiedApplied);
    
    return geoObjectTypeModifiedApplied;
  }
  
  @Transaction
  private Universal updateGeoObjectType(GeoObjectType geoObjectType)
  {
    Universal universal = conversionService.getUniversalFromGeoObjectType(geoObjectType);
    
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
    MdTermRelationship mdTermRelUniversal = conversionService.newHierarchyToMdTermRelForUniversals(hierarchyType);
    mdTermRelUniversal.apply();
    this.grantAdmiPermissionsOnMdTermRel(mdTermRelUniversal);
    
    MdTermRelationship mdTermRelGeoEntity = conversionService.newHierarchyToMdTermRelForGeoEntities(hierarchyType);
    mdTermRelGeoEntity.apply();
    this.grantAdmiPermissionsOnMdTermRel(mdTermRelGeoEntity); 

    return conversionService.mdTermRelationshipToHierarchyType(mdTermRelUniversal);
  }
  private void grantAdmiPermissionsOnMdTermRel(MdTermRelationship mdTermRelationship) 
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
    MdTermRelationship mdTermRelationship = conversionService.existingHierarchyToMdTermRelationiship(hierarchyType);
    
    mdTermRelationship.lock();
    
    mdTermRelationship.getDisplayLabel().setValue(hierarchyType.getLocalizedLabel());
    mdTermRelationship.getDescription().setValue(hierarchyType.getLocalizedDescription());
    mdTermRelationship.apply();
    
    mdTermRelationship.unlock();
    
    HierarchyType returnHierarchyType = conversionService.mdTermRelationshipToHierarchyType(mdTermRelationship);
    
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
    HierarchyType ht = conversionService.mdTermRelationshipToHierarchyType(mdTermRelationship);
    
    adapter.getMetadataCache().addHierarchyType(ht);
    
    return ht;
  }
  @Transaction
  private void addToHierarchy(String hierarchyTypeCode, MdTermRelationship mdTermRelationship, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parent = Universal.getByKey(parentGeoObjectTypeCode);
    Universal child = Universal.getByKey(childGeoObjectTypeCode);
    
    parent.addChild(child, mdTermRelationship.definesType()).apply();
    
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
    HierarchyType ht = conversionService.mdTermRelationshipToHierarchyType(mdTermRelationship);
    
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
