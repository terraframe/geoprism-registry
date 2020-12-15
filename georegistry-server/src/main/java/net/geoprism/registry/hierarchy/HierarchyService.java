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
package net.geoprism.registry.hierarchy;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.geoobjecttype.AssignPublicChildOfPrivateType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.GeoObjectRelationshipPermissionServiceIF;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class HierarchyService
{

  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForType(String sessionId, String code, Boolean includeTypes)
  {
    ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(code);

    List<ServerHierarchyType> hierarchyTypes = ServerHierarchyType.getAll();

    JsonArray hierarchies = new JsonArray();

    HierarchyTypePermissionServiceIF pService = ServiceFactory.getHierarchyPermissionService();

    for (ServerHierarchyType sHT : hierarchyTypes)
    {
      HierarchyType hierarchyType = sHT.getType();

      if (pService.canRead(hierarchyType.getOrganizationCode(), PermissionContext.WRITE))
      {
        List<GeoObjectType> parents = geoObjectType.getTypeAncestors(sHT, true);

        if (parents.size() > 0)
        {
          JsonObject object = new JsonObject();
          object.addProperty("code", sHT.getCode());
          object.addProperty("label", sHT.getDisplayLabel().getValue());

          if (includeTypes)
          {
            JsonArray pArray = new JsonArray();

            for (GeoObjectType parent : parents)
            {
              ServerGeoObjectType pType = ServerGeoObjectType.get(parent);

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
      for (ServerHierarchyType sHT : hierarchyTypes)
      {
        HierarchyType hierarchyType = sHT.getType();

        if (pService.canRead(hierarchyType.getOrganizationCode(), PermissionContext.WRITE))
        {
          if (geoObjectType.isRoot(sHT))
          {
            JsonObject object = new JsonObject();
            object.addProperty("code", hierarchyType.getCode());
            object.addProperty("label", hierarchyType.getLabel().getValue());
            object.add("parents", new JsonArray());

            hierarchies.add(object);
          }
        }
      }
    }

    return hierarchies;
  }

  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForSubtypes(String sessionId, String code)
  {
    ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(code);
    Set<ServerHierarchyType> hierarchyTypes = geoObjectType.getHierarchiesOfSubTypes();

    JsonArray hierarchies = new JsonArray();

    HierarchyTypePermissionServiceIF pService = ServiceFactory.getHierarchyPermissionService();

    for (ServerHierarchyType sHT : hierarchyTypes)
    {
      HierarchyType hierarchyType = sHT.getType();

      if (pService.canRead(hierarchyType.getOrganizationCode(), PermissionContext.WRITE))
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", sHT.getCode());
        object.addProperty("label", sHT.getDisplayLabel().getValue());

        hierarchies.add(object);
      }
    }

    return hierarchies;
  }

  @Request(RequestType.SESSION)
  public JsonArray getHierarchiesForGeoObjectOverTime(String sessionId, String code, String typeCode)
  {
    GeoObjectRelationshipPermissionServiceIF service = ServiceFactory.getGeoObjectRelationshipPermissionService();
    ServerGeoObjectIF geoObject = ServiceFactory.getGeoObjectService().getGeoObjectByCode(code, typeCode);
    ServerParentTreeNodeOverTime pot = geoObject.getParentsOverTime(null, true);

    // Filter out hierarchies that they're not allowed to see
    Collection<ServerHierarchyType> hierarchies = pot.getHierarchies();

    for (ServerHierarchyType hierarchy : hierarchies)
    {
      Organization organization = hierarchy.getOrganization();

      if (!service.canViewChild(organization.getCode(), null, geoObject.getType()))
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
   * @param context
   * @return the {@link HierarchyType}s with the given codes or all
   *         {@link HierarchyType}s if no codes are provided.
   */
  @Request(RequestType.SESSION)
  public HierarchyType[] getHierarchyTypes(String sessionId, String[] codes, PermissionContext context)
  {
    final GeoObjectTypePermissionServiceIF typePermServ = ServiceFactory.getGeoObjectTypePermissionService();
    List<HierarchyType> hierarchyTypeList = new LinkedList<HierarchyType>();

    if (codes == null || codes.length == 0)
    {
      List<HierarchyType> lHt = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();

      hierarchyTypeList = new LinkedList<HierarchyType>(lHt);
    }
    else
    {
      for (String code : codes)
      {
        Optional<HierarchyType> oht = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code);

        if (oht.isPresent())
        {
          hierarchyTypeList.add(oht.get());
        }
      }
    }

    // Filter out what they're not allowed to see
    Iterator<HierarchyType> it = hierarchyTypeList.iterator();
    while (it.hasNext())
    {
      HierarchyType ht = it.next();

      Organization org = Organization.getByCode(ht.getOrganizationCode());

      if (!ServiceFactory.getHierarchyPermissionService().canRead(org.getCode(), context))
      {
        it.remove();
        continue;
      }
      
      List<HierarchyNode> rootTypes = ht.getRootGeoObjectTypes();
      
      Iterator<HierarchyNode> rootIt = rootTypes.iterator();
      
      boolean removed = false;
      while (rootIt.hasNext())
      {
        HierarchyNode hn = rootIt.next();
        GeoObjectType rootGot = hn.getGeoObjectType();
        
        if (!typePermServ.canRead(rootGot.getOrganizationCode(), rootGot.getCode(), rootGot.getIsPrivate()))
        {
          removed = true;
          rootIt.remove();
        }
        else
        {
          this.filterOutPrivateNodes(hn);
        }
      }
      
      if (removed && rootTypes.size() == 0)
      {
        it.remove();
      }
    }

    HierarchyType[] hierarchies = hierarchyTypeList.toArray(new HierarchyType[hierarchyTypeList.size()]);

    return hierarchies;
  }
  
  private void filterOutPrivateNodes(HierarchyNode parent)
  {
    final GeoObjectTypePermissionServiceIF typePermServ = ServiceFactory.getGeoObjectTypePermissionService();
    List<HierarchyNode> list = parent.getChildren();
    
    for (HierarchyNode child : list)
    {
      GeoObjectType got = child.getGeoObjectType();
      
      if (!typePermServ.canRead(got.getOrganizationCode(), got.getCode(), got.getIsPrivate()))
      {
        parent.removeChild(child);
      }
      else
      {
        this.filterOutPrivateNodes(child);
      }
    }
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

    ServiceFactory.getHierarchyPermissionService().enforceCanWrite(type.getOrganization().getCode());

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

    ServiceFactory.getHierarchyPermissionService().enforceCanDelete(type.getOrganization().getCode());

    type.delete();
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
    ServerGeoObjectType parentType = ServerGeoObjectType.get(parentGeoObjectTypeCode);
    ServerGeoObjectType childType = ServerGeoObjectType.get(childGeoObjectTypeCode);

    ServiceFactory.getGeoObjectTypeRelationshipPermissionService().enforceCanAddChild(type, parentType, childType);
    
    if (parentType.getIsPrivate() && !childType.getIsPrivate())
    {
      AssignPublicChildOfPrivateType ex = new AssignPublicChildOfPrivateType();
      throw ex;
    }

    type.addToHierarchy(parentType, childType);

    return type.getType();
  }

  /**
   * Inserts the {@link GeoObjectType} 'middleGeoObjectTypeCode' into the
   * hierarchy as the child of 'parentGeoObjectTypeCode' and the new parent for
   * 'youngestGeoObjectTypeCode'. If an existing parent/child relationship
   * already exists between 'youngestGeoObjectTypeCode' and
   * 'parentgeoObjectTypeCode', it will first be removed.
   * youngestGeoObjectTypeCode can also be an array (comma separated list).
   * 
   * @param sessionId
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType}
   * @param parentGeoObjectTypeCode
   *          parent {@link GeoObjectType}.
   * @param middleGeoObjectTypeCode
   *          middle child {@link GeoObjectType} after this method returns
   * @param youngestGeoObjectTypeCode
   *          youngest child {@link GeoObjectType} after this method returns
   */
  @Request(RequestType.SESSION)
  public HierarchyType insertBetweenTypes(String sessionId, String hierarchyTypeCode, String parentGeoObjectTypeCode, String middleGeoObjectTypeCode, String youngestGeoObjectTypeCode)
  {
    return this.insertBetweenTypesInTrans(sessionId, hierarchyTypeCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode);
  }

  @Transaction
  public HierarchyType insertBetweenTypesInTrans(String sessionId, String hierarchyTypeCode, String parentGeoObjectTypeCode, String middleGeoObjectTypeCode, String youngestGeoObjectTypeCode)
  {
    this.addToHierarchy(sessionId, hierarchyTypeCode, parentGeoObjectTypeCode, middleGeoObjectTypeCode);

    if (youngestGeoObjectTypeCode.contains(","))
    {
      String[] array = youngestGeoObjectTypeCode.split(",");

      for (String youngest : array)
      {
        this.removeFromHierarchy(sessionId, hierarchyTypeCode, parentGeoObjectTypeCode, youngest, false);
        this.addToHierarchy(sessionId, hierarchyTypeCode, middleGeoObjectTypeCode, youngest);
      }
    }
    else
    {
      this.removeFromHierarchy(sessionId, hierarchyTypeCode, parentGeoObjectTypeCode, youngestGeoObjectTypeCode, false);
      this.addToHierarchy(sessionId, hierarchyTypeCode, middleGeoObjectTypeCode, youngestGeoObjectTypeCode);
    }

    return ServerHierarchyType.get(hierarchyTypeCode).getType();
  }

  /**
   * Modifies a hierarchy to inherit from another hierarchy at the given
   * GeoObjectType
   * 
   * @param sessionId
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType} being modified.
   * @param inheritedHierarchyTypeCode
   *          code of the {@link HierarchyType} being inherited.
   * @param geoObjectTypeCode
   *          code of the root {@link GeoObjectType}.
   */
  @Request(RequestType.SESSION)
  public HierarchyType setInheritedHierarchy(String sessionId, String hierarchyTypeCode, String inheritedHierarchyTypeCode, String geoObjectTypeCode)
  {
    ServerHierarchyType forHierarchy = ServerHierarchyType.get(hierarchyTypeCode);
    ServerHierarchyType inheritedHierarchy = ServerHierarchyType.get(inheritedHierarchyTypeCode);
    ServerGeoObjectType childType = ServerGeoObjectType.get(geoObjectTypeCode);

    ServiceFactory.getGeoObjectTypeRelationshipPermissionService().enforceCanAddChild(forHierarchy, null, childType);

    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);

    type.setInheritedHierarchy(forHierarchy, inheritedHierarchy);
    forHierarchy.refresh();

    return forHierarchy.getType();
  }

  /**
   * Modifies a hierarchy to remove inheritance from another hierarchy for the
   * given root
   * 
   * @param sessionId
   * @param hierarchyTypeCode
   *          code of the {@link HierarchyType} being modified.
   * @param geoObjectTypeCode
   *          code of the root {@link GeoObjectType}.
   */
  @Request(RequestType.SESSION)
  public HierarchyType removeInheritedHierarchy(String sessionId, String hierarchyTypeCode, String geoObjectTypeCode)
  {
    ServerHierarchyType forHierarchy = ServerHierarchyType.get(hierarchyTypeCode);
    ServerGeoObjectType childType = ServerGeoObjectType.get(geoObjectTypeCode);

    ServiceFactory.getGeoObjectTypeRelationshipPermissionService().enforceCanAddChild(forHierarchy, null, childType);

    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectTypeCode);

    type.removeInheritedHierarchy(forHierarchy);
    forHierarchy.refresh();

    return forHierarchy.getType();
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
  public HierarchyType removeFromHierarchy(String sessionId, String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode, boolean migrateChildren)
  {
    ServerHierarchyType type = ServerHierarchyType.get(hierarchyTypeCode);
    ServerGeoObjectType parentType = ServerGeoObjectType.get(parentGeoObjectTypeCode);
    ServerGeoObjectType childType = ServerGeoObjectType.get(childGeoObjectTypeCode);

    ServiceFactory.getGeoObjectTypeRelationshipPermissionService().enforceCanRemoveChild(type, parentType, childType);

    type.removeChild(parentType, childType, migrateChildren);

    return type.getType();
  }

}
