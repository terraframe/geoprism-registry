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
package net.geoprism.registry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.ontology.TermHacker;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.RelationshipCardinalityException;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Actor;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.InvalidGeoEntityUniversalException;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MetadataDisplayLabel;
import com.runwaysdk.system.ontology.ImmutableRootException;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.registry.AbstractParentException;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.HierarchyMetadata;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.NoChildForLeafGeoObjectType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.graph.CantRemoveInheritedGOT;
import net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException;
import net.geoprism.registry.graph.MultipleHierarchyRootsException;
import net.geoprism.registry.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;

public class ServerHierarchyType
{

  private HierarchyType      type;

  private MdTermRelationship universalRelationship;

  private MdTermRelationship entityRelationship;

  private MdEdgeDAOIF        mdEdge;

  public ServerHierarchyType(HierarchyType type, MdTermRelationship universalRelationship, MdTermRelationship entityRelationship, MdEdgeDAOIF mdEdge)
  {
    this.type = type;
    this.universalRelationship = universalRelationship;
    this.entityRelationship = entityRelationship;
    this.mdEdge = mdEdge;
  }
  
  private HierarchyNode buildHierarchy(HierarchyNode parentNode, Universal parentUniversal, MdTermRelationship mdTermRel)
  {
    List<Universal> childUniversals = new LinkedList<Universal>();

    OIterator<? extends Business> i = parentUniversal.getChildren(mdTermRel.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal) u));
    }
    finally
    {
      i.close();
    }

    for (Universal childUniversal : childUniversals)
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(childUniversal);

      HierarchyNode node = new HierarchyNode(geoObjectType.getType());

      node = buildHierarchy(node, childUniversal, mdTermRel);

      parentNode.addChild(node);
    }

    return parentNode;

  }
  
  public void buildHierarchyNodes()
  {
    this.type.clearRootGeoObjectTypes();
    
    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    // Copy all of the children to a list so as not to have recursion with open
    // database cursors.
    List<Universal> childUniversals = new LinkedList<Universal>();

    OIterator<? extends Business> i = rootUniversal.getChildren(universalRelationship.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal) u));
    }
    finally
    {
      i.close();
    }
    
    for (Universal childUniversal : childUniversals)
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(childUniversal);
      ServerHierarchyType inheritedHierarchy = geoObjectType.getInheritedHierarchy(universalRelationship);

      if (inheritedHierarchy != null)
      {
        HierarchyNode child = new HierarchyNode(geoObjectType.getType(), null);
        HierarchyNode root = child;

        List<GeoObjectType> ancestors = geoObjectType.getTypeAncestors(inheritedHierarchy, true);
        Collections.reverse(ancestors);

        for (GeoObjectType ancestor : ancestors)
        {
          HierarchyNode cNode = new HierarchyNode(ancestor, inheritedHierarchy.getCode());
          cNode.addChild(root);

          root = cNode;
        }

        buildHierarchy(child, childUniversal, universalRelationship);
        this.type.addRootGeoObjects(root);
      }
      else
      {
        HierarchyNode node = new HierarchyNode(geoObjectType.getType());
        node = buildHierarchy(node, childUniversal, universalRelationship);
        this.type.addRootGeoObjects(node);
      }

    }
  }

  public MdTermRelationship getEntityRelationship()
  {
    return entityRelationship;
  }

  public MdRelationshipDAOIF getEntityRelationshipDAO()
  {
    return (MdRelationshipDAOIF) BusinessFacade.getEntityDAO(this.entityRelationship);
  }

  public void setEntityRelationship(MdTermRelationship entityRelationship)
  {
    this.entityRelationship = entityRelationship;
  }

  public MdTermRelationship getUniversalRelationship()
  {
    return universalRelationship;
  }

  public MdRelationshipDAOIF getUniversalRelationshipDAO()
  {
    return (MdRelationshipDAOIF) BusinessFacade.getEntityDAO(this.universalRelationship);
  }

  public void setUniversalRelationship(MdTermRelationship universalRelationship)
  {
    this.universalRelationship = universalRelationship;
  }

  public MdEdgeDAOIF getMdEdge()
  {
    return mdEdge;
  }

  public void setMdEdge(MdEdgeDAOIF mdEdge)
  {
    this.mdEdge = mdEdge;
  }

  public HierarchyType getType()
  {
    return type;
  }

  public void setType(HierarchyType type)
  {
    this.type = type;
  }

  public String getCode()
  {
    return this.type.getCode();
  }

  public MetadataDisplayLabel getDisplayLabel()
  {
    return this.entityRelationship.getDisplayLabel();
  }

  public String getUniversalType()
  {
    return this.universalRelationship.definesType();
  }

  public String getEntityType()
  {
    return this.entityRelationship.definesType();
  }

  public void refresh()
  {
    ServerHierarchyType updated = new ServerHierarchyTypeBuilder().get(this.universalRelationship);

    this.type = updated.getType();
    this.universalRelationship = updated.getUniversalRelationship();
    this.entityRelationship = updated.getEntityRelationship();

    ServiceFactory.getMetadataCache().addHierarchyType(this);
  }

  public void update(HierarchyType hierarchyType)
  {
    this.updateTransaction(hierarchyType);

    this.refresh();
  }

  @Transaction
  private void updateTransaction(HierarchyType hierarchyType)
  {
    this.entityRelationship.lock();

    LocalizedValueConverter.populate(this.entityRelationship.getDisplayLabel(), hierarchyType.getLabel());
    LocalizedValueConverter.populate(this.entityRelationship.getDescription(), hierarchyType.getDescription());

    HierarchyMetadata metadata = null;

    try
    {
      metadata = HierarchyMetadata.getByKey(universalRelationship.getOid());
      metadata.appLock();
    }
    catch (DataNotFoundException e)
    {
      metadata = new HierarchyMetadata();
      metadata.setMdTermRelationship(this.universalRelationship);
    }

    metadata.setAbstractDescription(hierarchyType.getAbstractDescription());
    metadata.setProgress(hierarchyType.getProgress());
    metadata.setAcknowledgement(hierarchyType.getAcknowledgement());
    metadata.setDisclaimer(hierarchyType.getDisclaimer());
    metadata.setContact(hierarchyType.getContact());
    metadata.setPhoneNumber(hierarchyType.getPhoneNumber());
    metadata.setEmail(hierarchyType.getEmail());
    metadata.setAccessConstraints(hierarchyType.getAccessConstraints());
    metadata.setUseConstraints(hierarchyType.getUseConstraints());
    metadata.apply();

    this.entityRelationship.apply();

    this.entityRelationship.unlock();
  }

  public void delete()
  {
    deleteInTrans();

    if (Session.getCurrentSession() != null)
    {
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }

    // No error at this point so the transaction completed successfully.
    ServiceFactory.getMetadataCache().removeHierarchyType(this.getCode());
  }

  @Transaction
  private void deleteInTrans()
  {
    // // They can't delete it if there's existing data
    // Universal root = Universal.getRoot();
    // OIterator<? extends Business> it =
    // root.getChildren(this.getUniversalRelationship().definesType());
    // if (it.hasNext())
    // {
    // throw new ObjectHasDataException();
    // }

    /*
     * Delete all inherited hierarchies
     */
    List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByRelationship(this.getUniversalRelationship());

    for (InheritedHierarchyAnnotation annotation : annotations)
    {
      annotation.delete();
    }

    Universal.getStrategy().shutdown(this.universalRelationship.definesType());

    HierarchyMetadata.deleteByRelationship(this.universalRelationship);
    AttributeHierarchy.deleteByRelationship(this.universalRelationship);

    this.universalRelationship.delete();

    GeoEntity.getStrategy().shutdown(this.entityRelationship.definesType());

    this.entityRelationship.delete();

    ( (MdEdgeDAO) this.getMdEdge() ).delete();

    MasterList.markAllAsInvalid(this, null);
  }

  public void addToHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType)
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

    this.addToHierarchyTransaction(parentType, childType);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    this.refresh();
  }

  /**
   * @return The organization associated with this HierarchyType. If this
   *         HierarchyType is AllowedIn (or constructed incorrectly) this method
   *         will return null.
   */
  public String getOrganizationCode()
  {
    if (this.getUniversalRelationship().getKey().equals(AllowedIn.CLASS) || this.getUniversalRelationship().getKey().equals(LocatedIn.CLASS))
    {
      return null; // AllowedIn is deprecated and should not be used by the
      // end-user.
    }

    Actor uniRelActor = this.getUniversalRelationship().getOwner();
    if (! ( uniRelActor instanceof Roles ))
    {
      return null; // If we get here, then the HierarchyType was not created
      // correctly.
    }
    else
    {
      Roles uniRelRole = (Roles) uniRelActor;

      return RegistryRole.Type.parseOrgCode(uniRelRole.getRoleName());
    }
  }

  /**
   * @return The organization associated with this HierarchyType. If this
   *         HierarchyType is AllowedIn (or constructed incorrectly) this method
   *         will return null.
   */
  public Organization getOrganization()
  {
    if (this.getUniversalRelationship().getKey().equals(AllowedIn.CLASS) || this.getUniversalRelationship().getKey().equals(LocatedIn.CLASS))
    {
      return null; // AllowedIn is deprecated and should not be used by the
                   // end-user.
    }

    Actor uniRelActor = this.getUniversalRelationship().getOwner();
    if (! ( uniRelActor instanceof Roles ))
    {
      return null; // If we get here, then the HierarchyType was not created
                   // correctly.
    }
    else
    {
      Roles uniRelRole = (Roles) uniRelActor;
      String myOrgCode = RegistryRole.Type.parseOrgCode(uniRelRole.getRoleName());

      return ServiceFactory.getMetadataCache().getOrganization(myOrgCode).get();
    }
  }

  @Transaction
  private void addToHierarchyTransaction(ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {

    try
    {
      childType.getUniversal().addLink(parentType.getUniversal(), this.universalRelationship.definesType());
    }
    catch (RelationshipCardinalityException e)
    {
      GeoObjectTypeAlreadyInHierarchyException ex = new GeoObjectTypeAlreadyInHierarchyException();
      ex.setGotCode(childType.getCode());
      throw ex;
    }

    if (childType.getUniversal().getIsLeafType())
    {
      this.addParentReferenceToLeafType(parentType.getUniversal(), childType.getUniversal());
    }
  }

  /**
   * Creates a reference attribute name for a child leaf type that references
   * the parent type
   * 
   * @param hierarchyTypeCode
   * @param parentUniversal
   * @return
   */
  public String getParentReferenceAttributeName(Universal parentUniversal)
  {
    return this.getParentReferenceAttributeName(parentUniversal.getMdBusiness());
  }

  private String getParentReferenceAttributeName(MdBusiness parentMdBusiness)
  {
    String parentTypeName = parentMdBusiness.getTypeName();

    // Lower case the first character of the hierarchy Code
    String lowerCaseHierarchyName = Character.toLowerCase(this.getCode().charAt(0)) + this.getCode().substring(1);
    if (lowerCaseHierarchyName.length() > 32)
    {
      lowerCaseHierarchyName = lowerCaseHierarchyName.substring(0, 31);
    }

    // Upper case the first character of the parent class
    String upperCaseParentClassName = Character.toUpperCase(parentTypeName.charAt(0)) + parentTypeName.substring(1);
    if (upperCaseParentClassName.length() > 32)
    {
      upperCaseParentClassName = upperCaseParentClassName.substring(0, 31);
    }

    return lowerCaseHierarchyName + upperCaseParentClassName;
  }

  /**
   * Creates a reference attribute to the parent node class.
   * 
   * 
   * @param hierarchyTypeCode
   * @param parentUniversal
   * @param childUniversal
   */
  @Transaction
  public void addParentReferenceToLeafType(Universal parentUniversal, Universal childUniversal)
  {
    // MdBusiness parentMdBusiness = parentUniversal.getMdBusiness();
    MdBusiness childMdBusiness = childUniversal.getMdBusiness();

    String refAttrName = this.getParentReferenceAttributeName(parentUniversal);

    String displayLabel = "Reference to " + parentUniversal.getDisplayLabel().getValue() + " in hierarchy " + this.getDisplayLabel().getValue();

    MdAttributeReference mdAttributeReference = new MdAttributeReference();
    mdAttributeReference.setAttributeName(refAttrName);
    mdAttributeReference.getDisplayLabel().setValue(displayLabel);
    mdAttributeReference.getDescription().setValue(this.getCode());
    mdAttributeReference.setRequired(false);
    mdAttributeReference.setDefiningMdClass(childMdBusiness);
    mdAttributeReference.setMdBusiness(MdBusiness.getMdBusiness(GeoEntity.CLASS));
    mdAttributeReference.addIndexType(MdAttributeIndices.NON_UNIQUE_INDEX);
    mdAttributeReference.apply();

    AttributeHierarchy map = new AttributeHierarchy();
    map.setMdAttribute(mdAttributeReference);
    map.setMdTermRelationship(this.entityRelationship);
    map.setKeyName(mdAttributeReference.getKey());
    map.apply();
  }

  /**
   * Creates a reference attribute to the parent node class.
   * 
   * 
   * @param hierarchyTypeCode
   * @param parentUniversal
   * @param childUniversal
   */
  @Transaction
  public void removeParentReferenceToLeafType(Universal parentUniversal, Universal childUniversal)
  {
    // MdBusiness parentMdBusiness = parentUniversal.getMdBusiness();
    MdBusinessDAOIF childMdBusiness = MdBusinessDAO.get(childUniversal.getMdBusinessOid());

    String refAttrName = this.getParentReferenceAttributeName(parentUniversal);

    MdAttributeConcreteDAOIF mdAttributeReference = childMdBusiness.definesAttribute(refAttrName);

    AttributeHierarchy map = AttributeHierarchy.getByKey(mdAttributeReference.getKey());
    map.delete();

    mdAttributeReference.getBusinessDAO().delete();
  }

  public void removeChild(ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    this.removeFromHierarchy(parentType, childType, migrateChildren);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    this.refresh();
  }

  @Transaction
  private void removeFromHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByInheritedHierarchy(childType.getUniversal(), this.universalRelationship);

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

    Universal parent = parentType.getUniversal();
    Universal cUniversal = childType.getUniversal();

    removeLink(parent, cUniversal, this.universalRelationship.definesType());

    if (migrateChildren)
    {
      TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(cUniversal.getOid(), new String[] { this.universalRelationship.definesType() });

      if (parent.getKey().equals(Universal.ROOT) && tnrChildren.length > 1)
      {
        MultipleHierarchyRootsException ex = new MultipleHierarchyRootsException();
        throw ex;
      }

      for (TermAndRel tnrChild : tnrChildren)
      {
        Universal child = (Universal) tnrChild.getTerm();

        removeLink(cUniversal, child, this.universalRelationship.definesType());

        child.addLink(parent, this.universalRelationship.definesType());
      }
    }

    service.removeAllEdges(this, childType);

    MasterList.markAllAsInvalid(this, childType);

    InheritedHierarchyAnnotation annotation = InheritedHierarchyAnnotation.get(childType.getUniversal(), this.universalRelationship);

    if (annotation != null)
    {
      annotation.delete();
    }
  }

  public List<ServerGeoObjectType> getDirectRootNodes()
  {
    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    LinkedList<ServerGeoObjectType> roots = new LinkedList<ServerGeoObjectType>();

    try (OIterator<? extends Business> i = rootUniversal.getChildren(this.universalRelationship.definesType()))
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

  private static void removeLink(Universal parent, Universal child, String relationshipType)
  {
    if (child.getKey().equals(Term.ROOT_KEY))
    {
      ImmutableRootException exception = new ImmutableRootException("Cannot modify the root Term.");
      exception.setRootName(child.getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }

    // Remove the relationship
    parent.removeAllChildren(child, relationshipType);

    // Update the strategy
    TermHacker.getStrategy(child).removeLink(parent, child, relationshipType);
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

    // String universalKey =
    // buildMdTermRelUniversalKey(hierarchyType.getCode());
    // String geoEntityKey =
    // buildMdTermRelGeoEntityKey(hierarchyType.getCode());
    // String mdEdgeKey = buildMdEdgeKey(hierarchyType.getCode());
    //
    // MdTermRelationship universalRelationship =
    // MdTermRelationship.getByKey(universalKey);
    // MdTermRelationship entityRelationship =
    // MdTermRelationship.getByKey(geoEntityKey);
    // MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(mdEdgeKey);
    //
    // return new ServerHierarchyType(hierarchyType, universalRelationship,
    // entityRelationship, mdEdge);
  }

  public static ServerHierarchyType get(MdTermRelationship universalRelationship)
  {
    String code = buildHierarchyKeyFromMdTermRelUniversal(universalRelationship.getKey());
    return ServiceFactory.getMetadataCache().getHierachyType(code).get();

    // String geoEntityKey =
    // buildMdTermRelGeoEntityKey(hierarchyType.getCode());
    // String mdEdgeKey = buildMdEdgeKey(hierarchyType.getCode());
    //
    // MdTermRelationship entityRelationship =
    // MdTermRelationship.getByKey(geoEntityKey);
    // MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(mdEdgeKey);
    //
    // return new ServerHierarchyType(hierarchyType, universalRelationship,
    // entityRelationship, mdEdge);
  }

  public static ServerHierarchyType get(MdEdgeDAOIF mdEdge)
  {
    String code = buildHierarchyKeyFromMdEdge(mdEdge);

    return ServiceFactory.getMetadataCache().getHierachyType(code).get();

    // String universalKey =
    // buildMdTermRelUniversalKey(hierarchyType.getCode());
    // String geoEntityKey =
    // buildMdTermRelGeoEntityKey(hierarchyType.getCode());
    //
    // MdTermRelationship entityRelationship =
    // MdTermRelationship.getByKey(geoEntityKey);
    // MdTermRelationship universalRelationship =
    // MdTermRelationship.getByKey(universalKey);
    //
    // return new ServerHierarchyType(hierarchyType, universalRelationship,
    // entityRelationship, mdEdge);
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
      if (service.canRead(organization.getCode(), PermissionContext.WRITE))
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
  public String toString()
  {
    return HierarchyMetadata.sGetClassDisplayLabel() + " : " + this.getCode();
  }
}
