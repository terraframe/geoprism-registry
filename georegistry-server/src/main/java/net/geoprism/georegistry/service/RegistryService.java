package net.geoprism.georegistry.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.geoprism.georegistry.AdapterUtilities;
import net.geoprism.georegistry.action.RegistryAction;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.action.AbstractAction;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.Relationship;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.gis.geo.WKTParsingProblem;
import com.runwaysdk.system.metadata.MdRelationship;
import com.runwaysdk.system.metadata.MdRelationshipQuery;
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
    
    MdRelationshipQuery mrq = new MdRelationshipQuery(new QueryFactory());
    OIterator<? extends MdRelationship> relit = mrq.getIterator();
    
    try
    {
      while (relit.hasNext())
      {
        MdRelationship mdRel = relit.next();
        
        HierarchyType ht = conversionService.mdRelationshipToHierarchyType(mdRel);
        
        adapter.getMetadataCache().addHierarchyType(ht);
      }
    }
    finally
    {
      relit.close();
    }
    
    // TODO : Terms
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
  
  @Request(RequestType.SESSION)
  public HierarchyType[] getHierarchyTypes(String sessionId, String[] relationshipTypes)
  {
    if (relationshipTypes == null || relationshipTypes.length == 0)
    {
//      MdRelationshipQuery mrq = new MdRelationshipQuery(new QueryFactory());
//      List<? extends MdRelationship> mdRels = mrq.getIterator().getAll();
//      relationshipTypes = new String[mdRels.size()];
//      for (int i = 0; i < mdRels.size(); ++i)
//      {
//        // TODO : Maybe we want to filter out system types
//        MdRelationship mdRel = mdRels.get(i);
//        relationshipTypes[i] = mdRel.definesType();
//      }
      
      return adapter.getMetadataCache().getAllHierarchyTypes();
    }
    
    Map<String, HierarchyType> htMap = getHierarchyTypeMap(relationshipTypes);
    
    // Sort them based on the array we were given
    Collection<HierarchyType> htVals = htMap.values();
    HierarchyType[] out = new HierarchyType[htVals.size()];
    
    for (int i = 0; i < relationshipTypes.length; ++i)
    {
      String relType = relationshipTypes[i];
      
      for (HierarchyType ht : htVals)
      {
        if (ht.getCode().equals(relType))
        {
          out[i] = ht;
        }
      }
    }
    
    return out;
  }
  
  private Map<String, HierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    Map<String, HierarchyType> map = new HashMap<String, HierarchyType>();
    
    for (String relationshipType : relationshipTypes)
    {
      MdRelationship mdRel = MdRelationship.getMdRelationship(relationshipType);
      
      HierarchyType ht = conversionService.mdRelationshipToHierarchyType(mdRel);
      
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
      
      Relationship rel = geChild.addLink(geParent, hierarchy.getCode());
      
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
  
  @Request(RequestType.SESSION)
  public void deleteGeoObjectType(String sessionId, String code)
  {
    deleteGeoObjectTypeInTransaction(sessionId, code);
  }
  
  @Transaction
  private void deleteGeoObjectTypeInTransaction(String sessionId, String code)
  {
    Universal uni = Universal.getByKey(code);
    
    uni.delete();
  }
  
  
  ///////////////////// Hierarchy Management /////////////////////
  
  /**
   * Returns the {@link GeoObjectType} with the given code.
   * 
   * @param sessionId 
   * @param code code of the {@link GeoObjectType}
   * @return the {@link GeoObjectType} with the given code.
   */
  @Request(RequestType.SESSION)
  public GeoObjectType getGeoObjectType(String sessionId, String code)
  {
    return null;
  }
  
  /**
   * Creates a {@link GeoObjectType} from the given JSON.
   * 
   * @param sessionId
   * @param gtJSON JSON of the {@link GeoObjectType} to be created.
   */
  @Request(RequestType.SESSION)
  public void createGeoObjectType(String sessionId, String gtJSON)
  {
    
  }
  
  /**
   * Updates the given {@link GeoObjectType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON JSON of the {@link GeoObjectType} to be updated.
   */
  @Request(RequestType.SESSION)
  public void updateGeoObjectType(String sessionId, String gtJSON)
  {
    
  }
  
  /**
   * Returns the {@link HierarchyType} with the given code.
   * 
   * @param sessionId
   * @param code code value of the {@link HierarchyType}.
   * @return the {@link HierarchyType} with the given code.
   */
  @Request(RequestType.SESSION)
  public HierarchyType getHierarchyType(String sessionId, String code)
  {
    return null;
  }
  
  /**
   * Create the {@link HierarchyType} from the given JSON.
   * 
   * @param sessionId
   * @param htJSON JSON of the {@link HierarchyType} to be created.
   */
  @Request(RequestType.SESSION)
  public void createHierarcyType(String sessionId, String htJSON)
  {
    
  }
  
  /**
   * Updates the given {@link HierarchyType} represented as JSON.
   * 
   * @param sessionId
   * @param gtJSON JSON of the {@link HierarchyType} to be updated.
   */
  @Request(RequestType.SESSION)
  public void updateHierarcyType(String sessionId, String htJSON)
  {
    
  }
  
  /**
   * Adds the {@link GeoObjectType} with the given child code to the
   * parent {@link GeoObjectType} with the given code for the 
   * given {@link HierarchyType} code.
   * 
   * @param sessionId
   * @param hierarchyCode code of the {@link HierarchyType} the child is being added to.
   * @param parentGeoObjectTypeCode parent {@link GeoObjectType}.
   * @param childGeoObjectTypeCode child {@link GeoObjectType}.
   */
  @Request(RequestType.SESSION)
  public void addToHierarchy(String sessionId, String hierarchyCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
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
  public void removeFromHierarchy(String sessionId, String hierarchyCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
  }
  
//  @Request(RequestType.SESSION)
//  public void m(String sessionId)
//  {
//  }
  
}
