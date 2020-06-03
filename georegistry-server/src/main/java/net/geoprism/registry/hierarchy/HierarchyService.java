package net.geoprism.registry.hierarchy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class HierarchyService
{
  
  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForType(String sessionId, String code, Boolean includeTypes)
  {
    ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(code);

    List<HierarchyType> hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    JsonArray hierarchies = new JsonArray();
    Universal root = Universal.getRoot();

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      ServerHierarchyType sType = ServerHierarchyType.get(hierarchyType);

      if (ServiceFactory.getHierarchyPermissionService().canRead(Session.getCurrentSession().getUser(), sType.getOrganization()))
      {
        // Note: Ordered ancestors always includes self
        Collection<?> parents = GeoEntityUtil.getOrderedAncestors(root, geoObjectType.getUniversal(), sType.getUniversalType());
  
        if (parents.size() > 1)
        {
          JsonObject object = new JsonObject();
          object.addProperty("code", hierarchyType.getCode());
          object.addProperty("label", hierarchyType.getLabel().getValue());
  
          if (includeTypes)
          {
            JsonArray pArray = new JsonArray();
  
            for (Object parent : parents)
            {
              ServerGeoObjectType pType = ServerGeoObjectType.get((Universal) parent);
  
              if (!pType.getCode().equals(geoObjectType.getCode()))
              {
                JsonObject pObject = new JsonObject();
                pObject.addProperty("code", pType.getCode());
                pObject.addProperty("label", pType.getLabel().getValue());
  
                pArray.add(pObject);
              }
            }
  
            object.add("parents", pArray);
          }
  
          hierarchies.add(object);
        }
      }
    }

    if (hierarchies.size() == 0)
    {
      /*
       * This is a root type so include all hierarchies
       */

      for (HierarchyType hierarchyType : hierarchyTypes)
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", hierarchyType.getCode());
        object.addProperty("label", hierarchyType.getLabel().getValue());
        object.add("parents", new JsonArray());

        hierarchies.add(object);
      }
    }
    
    return hierarchies;
  }
  
  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForGeoObjectOverTime(String sessionId, String code, String typeCode)
  {
    ServerGeoObjectIF geoObject = ServiceFactory.getServerGeoObjectService().getGeoObjectByCode(code, typeCode);
    ServerParentTreeNodeOverTime pot = geoObject.getParentsOverTime(null, true);
    
    SingleActorDAOIF actor = Session.getCurrentSession().getUser();
    
    // Filter out hierarchies that they're not allowed to see
    Collection<ServerHierarchyType> hierarchies = pot.getHierarchies();
    for (ServerHierarchyType hierarchy : hierarchies)
    {
      // TODO : This only makes sense for the GeoObjectEditor usecase
      if (!ServiceFactory.getHierarchyPermissionService().canAddChild(actor, hierarchy, null, null))
      {
        pot.remove(hierarchy);
      }
    }

    return pot.toJSON();
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
      List<HierarchyType> lHt = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
      
      return lHt.toArray(new HierarchyType[lHt.size()]);
    }

    List<HierarchyType> hierarchyTypeList = new LinkedList<HierarchyType>();
    for (String code : codes)
    {
      Optional<HierarchyType> oht = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code);

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

    return ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();
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
    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, ServiceFactory.getAdapter());
    ServerHierarchyType type = ServerHierarchyType.get(hierarchyType);
    
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      ServiceFactory.getHierarchyPermissionService().enforceCanWrite(Session.getCurrentSession().getUser(), type.getOrganization());
    }
    
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
    ServiceFactory.getAdapter().getMetadataCache().removeHierarchyType(code);
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
    
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      ServiceFactory.getHierarchyPermissionService().enforceCanRemoveChild(Session.getCurrentSession().getUser(), type, parentGeoObjectTypeCode, childGeoObjectTypeCode);
    }
    
    type.removeChild(parentGeoObjectTypeCode, childGeoObjectTypeCode);

    return type.getType();
  }
  
}
