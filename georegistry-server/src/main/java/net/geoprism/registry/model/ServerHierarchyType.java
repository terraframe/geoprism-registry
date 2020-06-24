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
package net.geoprism.registry.model;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.ontology.TermHacker;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Actor;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MetadataDisplayLabel;
import com.runwaysdk.system.ontology.ImmutableRootException;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoObjectTypeHasDataException;
import net.geoprism.registry.NoChildForLeafGeoObjectType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.geoobject.GeoObjectPermissionService;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.hierarchy.HierarchyTypePermissionServiceIF;
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

  private void refresh()
  {
    ServerHierarchyType updated = new ServerHierarchyTypeBuilder().get(this.universalRelationship);

    this.type = updated.getType();
    this.universalRelationship = updated.getUniversalRelationship();
    this.entityRelationship = updated.getEntityRelationship();

    ServiceFactory.getAdapter().getMetadataCache().addHierarchyType(this.type);
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

    this.entityRelationship.apply();

    this.entityRelationship.unlock();
  }

  @Transaction
  public void delete()
  {
    Universal.getStrategy().shutdown(this.universalRelationship.definesType());

    AttributeHierarchy.deleteByRelationship(this.universalRelationship);

    this.universalRelationship.delete();

    GeoEntity.getStrategy().shutdown(this.entityRelationship.definesType());

    this.entityRelationship.delete();

    ( (MdEdgeDAO) this.getMdEdge() ).delete();
  }

  public void addToHierarchy(String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parentUniversal = Universal.getByKey(parentGeoObjectTypeCode);

    if (parentUniversal.getIsLeafType())
    {
      Universal childUniversal = Universal.getByKey(childGeoObjectTypeCode);

      NoChildForLeafGeoObjectType exception = new NoChildForLeafGeoObjectType();

      exception.setChildGeoObjectTypeLabel(childUniversal.getDisplayLabel().getValue());
      exception.setHierarchyTypeLabel(this.getDisplayLabel().getValue());
      exception.setParentGeoObjectTypeLabel(parentUniversal.getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }

    this.addToHierarchyTransaction(parentGeoObjectTypeCode, childGeoObjectTypeCode);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    this.refresh();
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

      return Organization.getByCode(myOrgCode);
    }
  }

  @Transaction
  private void addToHierarchyTransaction(String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parent = Universal.getByKey(parentGeoObjectTypeCode);
    Universal child = Universal.getByKey(childGeoObjectTypeCode);

    child.addLink(parent, this.universalRelationship.definesType());

    if (child.getIsLeafType())
    {
      this.addParentReferenceToLeafType(parent, child);
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

  public void removeChild(String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    this.removeFromHierarchy(parentGeoObjectTypeCode, childGeoObjectTypeCode);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    this.refresh();
  }

  @Transaction
  private void removeFromHierarchy(String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    ServerGeoObjectType parentType = ServerGeoObjectType.get(parentGeoObjectTypeCode);
    ServerGeoObjectType childType = ServerGeoObjectType.get(childGeoObjectTypeCode);

    ServerGeoObjectService service = new ServerGeoObjectService(new GeoObjectPermissionService());

    boolean hasData = service.hasData(this, childType);

    if (hasData)
    {
      GeoObjectTypeHasDataException ex = new GeoObjectTypeHasDataException();
      ex.setName(childType.getLabel().getValue());
      throw ex;
    }

    // Universal child = childType.getUniversal();
    Universal parent = parentType.getUniversal();

    removeAllChildrenFromHierarchy(parent, this.universalRelationship);

    // if (hasData)
    // {
    // child.enforceValidRemoveLink(parent,
    // this.universalRelationship.definesType());
    // }
    //
    // if (child.getIsLeafType())
    // {
    // this.removeParentReferenceToLeafType(parent, child);
    // }
  }

  private static void removeAllChildrenFromHierarchy(Universal parent, MdTermRelationship mdTermRelationship)
  {
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parent.getOid(), new String[] { mdTermRelationship.definesType() });
    for (TermAndRel tnrChild : tnrChildren)
    {
      Universal child = (Universal) tnrChild.getTerm();

      removeAllChildrenFromHierarchy(child, mdTermRelationship);

      removeLink(parent, child, mdTermRelationship.definesType());
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
    Optional<HierarchyType> hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hierarchyTypeCode);

    if (!hierarchyType.isPresent())
    {
      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setTypeLabel(HierarchyTypeMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(hierarchyTypeCode);
      ex.setAttributeLabel(HierarchyTypeMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
      throw ex;
    }

    return ServerHierarchyType.get(hierarchyType.get());
  }

  public static ServerHierarchyType get(HierarchyType hierarchyType)
  {
    String universalKey = buildMdTermRelUniversalKey(hierarchyType.getCode());
    String geoEntityKey = buildMdTermRelGeoEntityKey(hierarchyType.getCode());
    String mdEdgeKey = buildMdEdgeKey(hierarchyType.getCode());

    MdTermRelationship universalRelationship = MdTermRelationship.getByKey(universalKey);
    MdTermRelationship entityRelationship = MdTermRelationship.getByKey(geoEntityKey);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(mdEdgeKey);

    return new ServerHierarchyType(hierarchyType, universalRelationship, entityRelationship, mdEdge);
  }

  public static ServerHierarchyType get(MdTermRelationship universalRelationship)
  {
    String code = buildHierarchyKeyFromMdTermRelUniversal(universalRelationship.getKey());
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();

    String geoEntityKey = buildMdTermRelGeoEntityKey(hierarchyType.getCode());
    String mdEdgeKey = buildMdEdgeKey(hierarchyType.getCode());

    MdTermRelationship entityRelationship = MdTermRelationship.getByKey(geoEntityKey);
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(mdEdgeKey);

    return new ServerHierarchyType(hierarchyType, universalRelationship, entityRelationship, mdEdge);
  }

  public static ServerHierarchyType get(MdEdgeDAOIF mdEdge)
  {
    String code = buildHierarchyKeyFromMdEdge(mdEdge);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();

    String universalKey = buildMdTermRelUniversalKey(hierarchyType.getCode());
    String geoEntityKey = buildMdTermRelGeoEntityKey(hierarchyType.getCode());

    MdTermRelationship entityRelationship = MdTermRelationship.getByKey(geoEntityKey);
    MdTermRelationship universalRelationship = MdTermRelationship.getByKey(universalKey);

    return new ServerHierarchyType(hierarchyType, universalRelationship, entityRelationship, mdEdge);
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

    List<HierarchyType> lHt = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    // Filter out what they're not allowed to see

    lHt.forEach(ht -> {
      if (service.canRead(Session.getCurrentSession().getUser(), organization.getCode()))
      {
        list.add(ServerHierarchyType.get(ht));
      }
    });

    return list;
  }

}
