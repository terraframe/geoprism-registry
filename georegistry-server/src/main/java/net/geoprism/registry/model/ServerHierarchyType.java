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
package net.geoprism.registry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.InvalidGeoEntityUniversalException;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.AbstractParentException;
import net.geoprism.registry.HierarchicalRelationshipType;
import net.geoprism.registry.HierarchyMetadata;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.ListType;
import net.geoprism.registry.NoChildForLeafGeoObjectType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.RootNodeCannotBeInheritedException;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.geoobjecttype.AssignPublicChildOfPrivateType;
import net.geoprism.registry.graph.CantRemoveInheritedGOT;
import net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException;
import net.geoprism.registry.model.graph.GraphStrategy;
import net.geoprism.registry.model.graph.ServerHierarchyStrategy;
import net.geoprism.registry.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.service.SerializedListTypeCache;
import net.geoprism.registry.service.ServerGeoObjectService;
import net.geoprism.registry.service.ServiceFactory;

public class ServerHierarchyType implements ServerElement, GraphType
{
  private HierarchicalRelationshipType hierarchicalRelationship;

  public ServerHierarchyType(HierarchicalRelationshipType hierarchicalRelationship)
  {
    this.hierarchicalRelationship = hierarchicalRelationship;
  }

  public HierarchicalRelationshipType getHierarchicalRelationshipType()
  {
    return hierarchicalRelationship;
  }

  public MdTermRelationship getMdTermRelationship()
  {
    return this.hierarchicalRelationship.getMdTermRelationship();
  }

  public MdEdgeDAOIF getMdEdge()
  {
    return (MdEdgeDAOIF) BusinessFacade.getEntityDAO(this.hierarchicalRelationship.getMdEdge());
  }

  // public HierarchyType getType()
  // {
  // return type;
  // }
  //
  // public void setType(HierarchyType type)
  // {
  // this.type = type;
  // }

  public String getCode()
  {
    return this.hierarchicalRelationship.getCode();
  }

  public String getProgress()
  {
    return this.hierarchicalRelationship.getProgress();
  }

  public String getAccessConstraints()
  {
    return this.hierarchicalRelationship.getAccessConstraints();
  }

  public String getUseConstraints()
  {
    return this.hierarchicalRelationship.getUseConstraints();
  }

  public String getAcknowledgement()
  {
    return this.hierarchicalRelationship.getAcknowledgement();
  }

  public String getDisclaimer()
  {
    return this.hierarchicalRelationship.getDisclaimer();
  }

  public List<HierarchyNode> getRootGeoObjectTypes()
  {
    return this.getRootGeoObjectTypes(true);
  }

  public List<HierarchyNode> getRootGeoObjectTypes(boolean includePrivateTypes)
  {
    List<HierarchyNode> rootGeoObjectTypes = new LinkedList<HierarchyNode>();

    List<ServerGeoObjectType> types = this.getDirectRootNodes();

    for (ServerGeoObjectType geoObjectType : types)
    {
      ServerHierarchyType inheritedHierarchy = geoObjectType.getInheritedHierarchy(this.hierarchicalRelationship);

      if (inheritedHierarchy != null)
      {
        List<GeoObjectType> ancestors = geoObjectType.getTypeAncestors(inheritedHierarchy, true);
        Collections.reverse(ancestors);

        HierarchyNode child = new HierarchyNode(geoObjectType.getType(), null);
        HierarchyNode root = child;

        for (GeoObjectType ancestor : ancestors)
        {
          HierarchyNode cNode = new HierarchyNode(ancestor, inheritedHierarchy.getCode());
          cNode.addChild(root);

          root = cNode;
        }
        buildHierarchy(child, geoObjectType);
        rootGeoObjectTypes.add(root);
      }
      else
      {
        HierarchyNode node = new HierarchyNode(geoObjectType.getType());
        node = buildHierarchy(node, geoObjectType);
        rootGeoObjectTypes.add(node);
      }

    }

    if (!includePrivateTypes)
    {
      Iterator<HierarchyNode> rootIt = rootGeoObjectTypes.iterator();

      while (rootIt.hasNext())
      {
        HierarchyNode hn = rootIt.next();

        if (isRootPrivate(hn))
        {
          rootIt.remove();
        }
        else
        {
          this.filterOutPrivateNodes(hn);
        }
      }
    }

    return rootGeoObjectTypes;
  }

  public boolean hasVisibleRoot()
  {
    List<ServerGeoObjectType> roots = this.getDirectRootNodes();

    if (roots.size() > 0)
    {
      final GeoObjectTypePermissionServiceIF typePermServ = ServiceFactory.getGeoObjectTypePermissionService();

      for (ServerGeoObjectType root : roots)
      {
        if (typePermServ.canRead(root.getOrganizationCode(), root, root.getIsPrivate()))
        {
          return true;
        }
      }

      return false;
    }

    return true;
  }

  private boolean isRootPrivate(HierarchyNode parent)
  {
    final GeoObjectTypePermissionServiceIF typePermServ = ServiceFactory.getGeoObjectTypePermissionService();

    if (parent.getInheritedHierarchyCode() == null || parent.getInheritedHierarchyCode().equals(""))
    {
      GeoObjectType rootGot = parent.getGeoObjectType();

      if (!typePermServ.canRead(rootGot.getOrganizationCode(), ServerGeoObjectType.get(rootGot), rootGot.getIsPrivate()))
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      for (HierarchyNode child : parent.getChildren())
      {
        if (this.isRootPrivate(child))
        {
          return true;
        }
      }

      return false;
    }
  }

  private void filterOutPrivateNodes(HierarchyNode parent)
  {
    final GeoObjectTypePermissionServiceIF typePermServ = ServiceFactory.getGeoObjectTypePermissionService();
    List<HierarchyNode> list = parent.getChildren();

    Iterator<HierarchyNode> it = list.iterator();
    while (it.hasNext())
    {
      HierarchyNode child = it.next();

      GeoObjectType got = child.getGeoObjectType();

      if (!typePermServ.canRead(got.getOrganizationCode(), ServerGeoObjectType.get(got), got.getIsPrivate()))
      {
        it.remove();
      }
      else
      {
        this.filterOutPrivateNodes(child);
      }
    }
  }

  public LocalStruct getDescription()
  {
    return this.hierarchicalRelationship.getDescription();
  }

  public LocalStruct getDisplayLabel()
  {
    return this.hierarchicalRelationship.getDisplayLabel();
  }

  public LocalizedValue getLabel()
  {
    return LocalizedValueConverter.convert(this.getDisplayLabel());
  }

  public String getUniversalType()
  {
    return this.hierarchicalRelationship.getMdTermRelationship().definesType();
  }

  public void refresh()
  {
    this.hierarchicalRelationship = HierarchicalRelationshipType.getByCode(this.hierarchicalRelationship.getCode());

    ServiceFactory.getMetadataCache().addHierarchyType(this);
    SerializedListTypeCache.getInstance().clear();
  }

  public void update(HierarchyType hierarchyType)
  {
    this.hierarchicalRelationship.update(hierarchyType);

    this.refresh();
  }

  public void delete()
  {
    deleteInTrans();

    if (Session.getCurrentSession() != null)
    {
      // Refresh the users session
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }

    // No error at this point so the transaction completed successfully.
    ServiceFactory.getMetadataCache().removeHierarchyType(this.getCode());
  }

  @Transaction
  private void deleteInTrans()
  {
    /*
     * Delete all inherited hierarchies
     */
    this.hierarchicalRelationship.delete();

    // MasterList.markAllAsInvalid(this, null);
    ListType.markAllAsInvalid(this, null);
  }

  public void addToHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {
    this.addToHierarchy(parentType, childType, true);
  }

  public void addToHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean refresh)
  {
    if (parentType.getIsAbstract())
    {
      AbstractParentException exception = new AbstractParentException();
      exception.setChildGeoObjectTypeLabel(childType.getUniversal().getDisplayLabel().getValue());
      exception.setHierarchyTypeLabel(this.getDisplayLabel().getValue());
      exception.setParentGeoObjectTypeLabel(parentType.getUniversal().getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }

    if (parentType.getUniversal().getIsLeafType())
    {
      NoChildForLeafGeoObjectType exception = new NoChildForLeafGeoObjectType();

      exception.setChildGeoObjectTypeLabel(childType.getUniversal().getDisplayLabel().getValue());
      exception.setHierarchyTypeLabel(this.getDisplayLabel().getValue());
      exception.setParentGeoObjectTypeLabel(parentType.getUniversal().getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }

    if (parentType.getIsPrivate() && !childType.getIsPrivate())
    {
      AssignPublicChildOfPrivateType ex = new AssignPublicChildOfPrivateType();
      throw ex;
    }

    // Check to see if the child type is already in the hierarchy
    List<ServerHierarchyType> hierarchies = childType.getHierarchies(true);

    if (hierarchies.contains(this))
    {
      GeoObjectTypeAlreadyInHierarchyException ex = new GeoObjectTypeAlreadyInHierarchyException();
      ex.setGotCode(childType.getCode());
      throw ex;
    }

    // Ensure a subtype is not already in the hierarchy
    if (childType.getIsAbstract())
    {
      Set<ServerHierarchyType> hierarchiesOfSubTypes = childType.getHierarchiesOfSubTypes();

      if (hierarchiesOfSubTypes.contains(this))
      {
        GeoObjectTypeAlreadyInHierarchyException ex = new GeoObjectTypeAlreadyInHierarchyException();
        ex.setGotCode(childType.getCode());
        throw ex;
      }
    }

    this.hierarchicalRelationship.addToHierarchy(parentType, childType);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    if (refresh)
    {
      this.refresh();
    }
  }

  /**
   * @return The organization associated with this HierarchyType. If this
   *         HierarchyType is AllowedIn (or constructed incorrectly) this method
   *         will return null.
   */
  public String getOrganizationCode()
  {
    return this.getOrganization().getCode();
  }

  /**
   * @return The organization associated with this HierarchyType. If this
   *         HierarchyType is AllowedIn (or constructed incorrectly) this method
   *         will return null.
   */
  public Organization getOrganization()
  {
    return this.hierarchicalRelationship.getOrganization();
  }

  public void removeChild(ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    this.removeFromHierarchy(parentType, childType, migrateChildren);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    this.refresh();
  }

  public List<ServerGeoObjectType> getAllTypes()
  {
    List<ServerGeoObjectType> types = new LinkedList<ServerGeoObjectType>();

    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    try (OIterator<? extends Business> i = rootUniversal.getAllDescendants(this.hierarchicalRelationship.getMdTermRelationship().definesType()))
    {
      i.forEach(u -> types.add(ServerGeoObjectType.get((Universal) u)));
    }

    return types;
  }

  public HierarchyType toHierarchyType()
  {
    return this.toHierarchyType(true);
  }

  public HierarchyType toHierarchyType(boolean includePrivateTypes)
  {
    LocalizedValue description = LocalizedValueConverter.convert(this.getDescription());

    final HierarchyType hierarchyType = new HierarchyType(this.getCode(), getLabel(), description, this.getOrganizationCode());
    hierarchyType.setAbstractDescription(this.hierarchicalRelationship.getAbstractDescription());
    hierarchyType.setAcknowledgement(this.hierarchicalRelationship.getAcknowledgement());
    hierarchyType.setDisclaimer(this.hierarchicalRelationship.getDisclaimer());
    hierarchyType.setContact(this.hierarchicalRelationship.getContact());
    hierarchyType.setPhoneNumber(this.hierarchicalRelationship.getPhoneNumber());
    hierarchyType.setEmail(this.hierarchicalRelationship.getEmail());
    hierarchyType.setProgress(this.hierarchicalRelationship.getProgress());
    hierarchyType.setAccessConstraints(this.hierarchicalRelationship.getAccessConstraints());
    hierarchyType.setUseConstraints(this.hierarchicalRelationship.getUseConstraints());

    this.getRootGeoObjectTypes(includePrivateTypes).forEach(rootType -> hierarchyType.addRootGeoObjects(rootType));

    return hierarchyType;
  }

  private HierarchyNode buildHierarchy(HierarchyNode parentNode, ServerGeoObjectType parent)
  {
    List<ServerGeoObjectType> children = this.getChildren(parent);

    for (ServerGeoObjectType child : children)
    {
      HierarchyNode node = new HierarchyNode(child.getType());

      node = buildHierarchy(node, child);

      parentNode.addChild(node);
    }

    return parentNode;
  }

  @Transaction
  public void insertBetween(ServerGeoObjectType parentType, ServerGeoObjectType middleType, List<ServerGeoObjectType> youngestTypes)
  {
    this.addToHierarchy(parentType, middleType);

    for (ServerGeoObjectType youngest : youngestTypes)
    {
      this.removeFromHierarchy(parentType, youngest, false);
      this.addToHierarchy(middleType, youngest);
    }
  }

  @Transaction
  private void removeFromHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByInheritedHierarchy(childType.getUniversal(), this.hierarchicalRelationship);

    if (annotations.size() > 0)
    {
      List<String> codes = new ArrayList<String>();

      for (InheritedHierarchyAnnotation annot : annotations)
      {
        String code = buildHierarchyKeyFromMdTermRelUniversal(annot.getForHierarchy().getKey());
        codes.add(code);
      }

      CantRemoveInheritedGOT ex = new CantRemoveInheritedGOT();
      ex.setGotCode(childType.getCode());
      ex.setHierCode(this.getCode());
      ex.setInheritedHierarchyList(StringUtils.join(codes, ", "));
      throw ex;

    }

    // If the child type is the root of the hierarchy then determine if removing
    // it will push up a child node to the root which is used in an inherited
    // hierarchy. If so we must prevent this, because the inherited hierarchy
    // model assumes that the inherited node is not the root of the inherited
    // hierarchy.
    if (parentType instanceof RootGeoObjectType)
    {
      List<ServerGeoObjectType> children = childType.getChildren(this);

      if (children.size() == 1)
      {
        ServerGeoObjectType nextRoot = children.get(0);

        List<? extends InheritedHierarchyAnnotation> results = InheritedHierarchyAnnotation.getByInheritedHierarchy(nextRoot.getUniversal(), this.hierarchicalRelationship);

        if (results.size() > 0)
        {
          throw new RootNodeCannotBeInheritedException("Cannot remove the root Geo-Object Type of a hierarchy if the new root Geo-Object Type is inherited by another hierarchy");
        }
      }
    }

    this.hierarchicalRelationship.removeFromHierarchy(parentType, childType, migrateChildren);

    service.removeAllEdges(this, childType);

    // MasterList.markAllAsInvalid(this, childType);
    ListType.markAllAsInvalid(this, childType);
    SerializedListTypeCache.getInstance().clear();

    InheritedHierarchyAnnotation annotation = InheritedHierarchyAnnotation.get(childType.getUniversal(), this.hierarchicalRelationship);

    if (annotation != null)
    {
      annotation.delete();
    }
  }

  public List<ServerGeoObjectType> getDirectRootNodes()
  {
    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    LinkedList<ServerGeoObjectType> roots = new LinkedList<ServerGeoObjectType>();

    try (OIterator<? extends Business> i = rootUniversal.getChildren(this.hierarchicalRelationship.getMdTermRelationship().definesType()))
    {
      i.forEach(u -> roots.add(ServerGeoObjectType.get((Universal) u)));
    }

    return roots;
  }

  public void validateUniversalRelationship(ServerGeoObjectType childType, ServerGeoObjectType parentType)
  {
    // Total hack for super types
    Universal childUniversal = childType.getUniversal();
    Universal parentUniversal = parentType.getUniversal();

    List<Term> ancestors = childUniversal.getAllAncestors(this.getUniversalType()).getAll();

    if (!ancestors.contains(parentUniversal))
    {
      ServerGeoObjectType superType = childType.getSuperType();

      if (superType != null)
      {
        ancestors = superType.getUniversal().getAllAncestors(this.getUniversalType()).getAll();
      }
    }

    if (!ancestors.contains(parentUniversal))
    {
      InvalidGeoEntityUniversalException exception = new InvalidGeoEntityUniversalException();
      exception.setChildUniversal(childUniversal.getDisplayLabel().getValue());
      exception.setParentUniversal(parentUniversal.getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }
  }

  @Override
  public String toString()
  {
    return HierarchyMetadata.sGetClassDisplayLabel() + " : " + this.getCode();
  }

  @Override
  public GraphStrategy getStrategy()
  {
    return new ServerHierarchyStrategy(this);
  }

  public List<ServerGeoObjectType> getChildren(ServerGeoObjectType parent)
  {
    return this.hierarchicalRelationship.getChildren(parent);
  }

  public static ServerHierarchyType get(String hierarchyTypeCode)
  {
    Optional<ServerHierarchyType> hierarchyType = ServiceFactory.getMetadataCache().getHierachyType(hierarchyTypeCode);

    if (!hierarchyType.isPresent())
    {
      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setTypeLabel(HierarchyMetadata.sGetClassDisplayLabel());
      ex.setDataIdentifier(hierarchyTypeCode);
      ex.setAttributeLabel(HierarchyMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
      throw ex;
    }

    return hierarchyType.get();
  }

  public static ServerHierarchyType get(HierarchyType hierarchyType)
  {
    return get(hierarchyType.getCode());
  }

  public static ServerHierarchyType get(HierarchicalRelationshipType hierarchicalRelationship)
  {
    return ServiceFactory.getMetadataCache().getHierachyType(hierarchicalRelationship.getCode()).get();
  }

  public static ServerHierarchyType get(MdTermRelationship universalRelationship)
  {
    String code = buildHierarchyKeyFromMdTermRelUniversal(universalRelationship.getKey());
    return ServiceFactory.getMetadataCache().getHierachyType(code).get();
  }

  public static ServerHierarchyType get(MdEdgeDAOIF mdEdge)
  {
    String code = buildHierarchyKeyFromMdEdge(mdEdge);

    return ServiceFactory.getMetadataCache().getHierachyType(code).get();
  }

  /**
   * Turns the given {@link HierarchyType} code into the corresponding
   * {@link MdTermRelationship} key for the {@link Universal} relationship.
   * 
   * @param hierarchyCode
   *          {@link HierarchyType} code
   * @return corresponding {@link MdTermRelationship} key.
   */
  public static String buildMdTermRelUniversalKey(String hierarchyCode)
  {
    // If the code is for the LocatedIn hierarchy, then the relationship that
    // defines the
    // Universals for that relationship is AllowedIn.
    if (hierarchyCode.trim().equals(LocatedIn.class.getSimpleName()))
    {
      return AllowedIn.CLASS;
    }
    else
    {
      return GISConstants.GEO_PACKAGE + "." + hierarchyCode + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    }
  }

  /**
   * Convert the given {@link MdTermRelationShip} key for {@link Universal}s
   * into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey
   *          {@link MdTermRelationShip} key
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKeyFromMdTermRelUniversal(String mdTermRelKey)
  {
    // the hierarchyType code for the allowed in relationship is the located in
    // relationship
    if (mdTermRelKey.trim().equals(AllowedIn.CLASS))
    {
      return LocatedIn.class.getSimpleName();
    }
    else
    {
      int startIndex = GISConstants.GEO_PACKAGE.length() + 1;

      int endIndex = mdTermRelKey.indexOf(RegistryConstants.UNIVERSAL_RELATIONSHIP_POST);

      String hierarchyKey;
      if (endIndex > -1)
      {
        hierarchyKey = mdTermRelKey.substring(startIndex, endIndex);
      }
      else
      {
        hierarchyKey = mdTermRelKey.substring(startIndex, mdTermRelKey.length());
      }

      return hierarchyKey;
    }
  }

  public static String buildHierarchyKeyFromMdEdge(MdEdgeDAOIF mdEdge)
  {
    return mdEdge.getTypeName();
  }

  /**
   * Turns the given {@link MdTermRelationShip} key for a {@link Universal} into
   * the corresponding {@link MdTermRelationship} key for the {@link GeoEntity}
   * relationship.
   * 
   * @param hierarchyCode
   *          {@link HierarchyType} code
   * @return corresponding {@link MdTermRelationship} key.
   */
  public static String buildMdTermRelGeoEntityKey(String hierarchyCode)
  {
    // Check for existing GeoPrism hierarchyTypes
    if (hierarchyCode.trim().equals(LocatedIn.class.getSimpleName()))
    {
      return LocatedIn.CLASS;
    }
    else
    {
      return GISConstants.GEO_PACKAGE + "." + hierarchyCode;
    }
  }

  public static String buildMdEdgeKey(String hierarchyCode)
  {
    return RegistryConstants.UNIVERSAL_GRAPH_PACKAGE + "." + hierarchyCode;
  }

  /**
   * Convert the given {@link MdTermRelationShip} key for a {@link GeoEntities}
   * into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey
   *          {@link MdTermRelationShip} key
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKeyFromMdTermRelGeoEntity(String mdTermRelKey)
  {
    int startIndex = GISConstants.GEO_PACKAGE.length() + 1;

    return mdTermRelKey.substring(startIndex, mdTermRelKey.length());
  }

  public static List<ServerHierarchyType> getForOrganization(Organization organization)
  {
    final HierarchyTypePermissionServiceIF service = ServiceFactory.getHierarchyPermissionService();
    final List<ServerHierarchyType> list = new LinkedList<ServerHierarchyType>();

    List<ServerHierarchyType> lHt = ServiceFactory.getMetadataCache().getAllHierarchyTypes();
    // Filter out what they're not allowed to see

    lHt.forEach(ht -> {
      if (service.canWrite(organization.getCode()))
      {
        list.add(ht);
      }
    });

    return list;
  }

  public static List<ServerHierarchyType> getAll()
  {
    return ServiceFactory.getMetadataCache().getAllHierarchyTypes();
  }

  @Override
  public MdEdgeDAOIF getMdEdgeDAO()
  {
    return this.getMdEdge();
  }

}
