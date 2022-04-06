/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.ontology.TermHacker;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.RelationshipCardinalityException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.ImmutableRootException;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException;
import net.geoprism.registry.graph.MultipleHierarchyRootsException;
import net.geoprism.registry.model.ServerGeoObjectType;

public class HierarchicalRelationshipType extends HierarchicalRelationshipTypeBase
{
  private static final long serialVersionUID = 100293172;

  public HierarchicalRelationshipType()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    return this.getCode();
  }

  @Transaction
  public void update(HierarchyType hierarchyType)
  {
    this.lock();

    LocalizedValueConverter.populate(this.getDisplayLabel(), hierarchyType.getLabel());
    LocalizedValueConverter.populate(this.getDescription(), hierarchyType.getDescription());

    this.setAbstractDescription(hierarchyType.getAbstractDescription());
    this.setProgress(hierarchyType.getProgress());
    this.setAcknowledgement(hierarchyType.getAcknowledgement());
    this.setDisclaimer(hierarchyType.getDisclaimer());
    this.setContact(hierarchyType.getContact());
    this.setPhoneNumber(hierarchyType.getPhoneNumber());
    this.setEmail(hierarchyType.getEmail());
    this.setAccessConstraints(hierarchyType.getAccessConstraints());
    this.setUseConstraints(hierarchyType.getUseConstraints());
    this.apply();

    this.unlock();
  }

  @Transaction
  public void addToHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType)
  {

    try
    {
      childType.getUniversal().addLink(parentType.getUniversal(), this.getMdTermRelationship().definesType());
    }
    catch (RelationshipCardinalityException e)
    {
      GeoObjectTypeAlreadyInHierarchyException ex = new GeoObjectTypeAlreadyInHierarchyException();
      ex.setGotCode(childType.getCode());
      throw ex;
    }
  }

  @Transaction
  public void removeFromHierarchy(ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    Universal parent = parentType.getUniversal();
    Universal cUniversal = childType.getUniversal();

    removeLink(parent, cUniversal, this.getMdTermRelationship().definesType());

    if (migrateChildren)
    {
      TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(cUniversal.getOid(), new String[] { this.getMdTermRelationship().definesType() });

      if (parent.getKey().equals(Universal.ROOT) && tnrChildren.length > 1)
      {
        MultipleHierarchyRootsException ex = new MultipleHierarchyRootsException();
        throw ex;
      }

      for (TermAndRel tnrChild : tnrChildren)
      {
        Universal child = (Universal) tnrChild.getTerm();

        removeLink(cUniversal, child, this.getMdTermRelationship().definesType());

        child.addLink(parent, this.getMdTermRelationship().definesType());
      }
    }
  }

  @Override
  @Transaction
  public void delete()
  {
    MdTermRelationship mdTermRelationship = this.getMdTermRelationship();
    MdEdge mdEdge = this.getMdEdge();

    super.delete();

    /*
     * Delete all inherited hierarchies
     */
    List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByRelationship(this);

    for (InheritedHierarchyAnnotation annotation : annotations)
    {
      annotation.delete();
    }

    Universal.getStrategy().shutdown(mdTermRelationship.definesType());

    // AttributeHierarchy.deleteByRelationship(mdTermRelationship);

    mdTermRelationship.delete();

    mdEdge.delete();
  }

  public List<ServerGeoObjectType> getChildren(ServerGeoObjectType parent)
  {
    Universal universal = parent.getUniversal();
    List<ServerGeoObjectType> children = new LinkedList<>();
    String mdRelationshipType = this.getMdTermRelationship().definesType();

    try (OIterator<? extends Business> iterator = universal.getDirectDescendants(mdRelationshipType))
    {
      while (iterator.hasNext())
      {
        Universal cUniversal = (Universal) iterator.next();

        children.add(ServerGeoObjectType.get(cUniversal));
      }

    }

    return children;
  }

  public static List<HierarchicalRelationshipType> getAll()
  {
    HierarchicalRelationshipTypeQuery query = new HierarchicalRelationshipTypeQuery(new QueryFactory());

    try (OIterator<? extends HierarchicalRelationshipType> it = query.getIterator())
    {
      return new LinkedList<HierarchicalRelationshipType>(it.getAll());
    }
  }

  public static HierarchicalRelationshipType getByCode(String code)
  {
    return HierarchicalRelationshipType.getByKey(code);
  }

  public static HierarchicalRelationshipType getByMdTermRelationship(MdTermRelationship mdTermRelationship)
  {
    HierarchicalRelationshipTypeQuery query = new HierarchicalRelationshipTypeQuery(new QueryFactory());
    query.WHERE(query.getMdTermRelationship().EQ(mdTermRelationship));

    try (OIterator<? extends HierarchicalRelationshipType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static HierarchicalRelationshipType getByMdEdge(MdEdgeDAOIF mdEdge)
  {
    HierarchicalRelationshipTypeQuery query = new HierarchicalRelationshipTypeQuery(new QueryFactory());
    query.WHERE(query.getMdEdge().EQ(mdEdge.getOid()));

    try (OIterator<? extends HierarchicalRelationshipType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
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

  public static boolean isEdgeAHierarchyType(MdEdgeDAOIF mdEdge)
  {
    return ( HierarchicalRelationshipType.getByMdEdge(mdEdge) != null );
  }

}
