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
package com.runwaysdk.build.domain;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.dataaccess.AttributeDoesNotExistException;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.BusinessDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.IsARelationship;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.MdTermRelationshipQuery;

import net.geoprism.registry.HierarchicalRelationshipType;
import net.geoprism.registry.HierarchyMetadata;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.InheritedHierarchyAnnotationQuery;
import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerHierarchyType;

public class PatchHierarchicalRelationshipType
{
  private static final Logger logger = LoggerFactory.getLogger(PatchHierarchicalRelationshipType.class);

  public static void main(String[] args)
  {
    new PatchHierarchicalRelationshipType().doIt();
  }

  @Request
  private void doIt()
  {
    this.transaction();
  }

  @Transaction
  private void transaction()
  {
    this.createHierarchicalRelationshipTypes();

    this.migrateInheritedHierarchyAnnotation();
  }

  public void createHierarchicalRelationshipTypes()
  {
    MdBusiness univMdBusiness = MdBusiness.getMdBusiness(Universal.CLASS);

    MdTermRelationshipQuery trq = new MdTermRelationshipQuery(new QueryFactory());
    trq.WHERE(trq.getParentMdBusiness().EQ(univMdBusiness).AND(trq.getChildMdBusiness().EQ(univMdBusiness)));

    try (OIterator<? extends MdTermRelationship> it = trq.getIterator())
    {
      it.getAll().stream().filter(mdTermRel -> {
        if (! ( mdTermRel.definesType().equals(IsARelationship.CLASS) || mdTermRel.getKey().equals(AllowedIn.CLASS) || mdTermRel.getKey().equals(LocatedIn.CLASS) ))
        {
          return ( HierarchicalRelationshipType.getByMdTermRelationship(mdTermRel) == null );
        }

        return false;
      }).forEach(mdTermRel -> {

        System.out.println("Creating HierarchicalRelationshipType for the MdTermRelationship [" + mdTermRel.definesType() + "]");

        String code = ServerHierarchyType.buildHierarchyKeyFromMdTermRelUniversal(mdTermRel.getKey());
        String geoEntityKey = ServerHierarchyType.buildMdTermRelGeoEntityKey(code);
        String mdEdgeKey = ServerHierarchyType.buildMdEdgeKey(code);

        MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(mdEdgeKey);

        String ownerActerOid = mdTermRel.getOwnerId();
        String organizationCode = Organization.getRootOrganizationCode(ownerActerOid);

        Organization organization = Organization.getByCode(organizationCode);

        HierarchicalRelationshipType hierarchicalRelationship = new HierarchicalRelationshipType();
        hierarchicalRelationship.setCode(code);
        hierarchicalRelationship.setOrganization(organization);
        hierarchicalRelationship.setMdTermRelationshipId(mdTermRel.getOid());
        hierarchicalRelationship.setMdEdgeId(mdEdge.getOid());

        try
        {
          MdTermRelationship entityRelationship = MdTermRelationship.getByKey(geoEntityKey);
          LocalizedValue displayLabel = AttributeTypeConverter.convert(entityRelationship.getDisplayLabel());
          LocalizedValue description = AttributeTypeConverter.convert(entityRelationship.getDescription());

          LocalizedValueConverter.populate(hierarchicalRelationship.getDisplayLabel(), displayLabel);
          LocalizedValueConverter.populate(hierarchicalRelationship.getDescription(), description);

          entityRelationship.delete();
        }
        catch (DataNotFoundException | AttributeDoesNotExistException e)
        {
          logger.debug("The entity geo relationship was not found defaulting to the mdTermRel displayLabel and description");

          LocalizedValue displayLabel = AttributeTypeConverter.convert(mdTermRel.getDisplayLabel());
          LocalizedValue description = AttributeTypeConverter.convert(mdTermRel.getDescription());

          LocalizedValueConverter.populate(hierarchicalRelationship.getDisplayLabel(), displayLabel);
          LocalizedValueConverter.populate(hierarchicalRelationship.getDescription(), description);
        }

        try
        {
          BusinessDAOIF metadata = BusinessDAO.get("net.geoprism.registry.HierarchyMetadata", mdTermRel.getOid());

          hierarchicalRelationship.setAbstractDescription(metadata.getValue("abstractDescription"));
          hierarchicalRelationship.setAcknowledgement(metadata.getValue("acknowledgement"));
          hierarchicalRelationship.setDisclaimer(metadata.getValue("disclaimer"));
          hierarchicalRelationship.setContact(metadata.getValue("contact"));
          hierarchicalRelationship.setPhoneNumber(metadata.getValue("phoneNumber"));
          hierarchicalRelationship.setEmail(metadata.getValue("email"));
          hierarchicalRelationship.setProgress(metadata.getValue("progress"));
          hierarchicalRelationship.setAccessConstraints(metadata.getValue("accessConstraints"));
          hierarchicalRelationship.setUseConstraints(metadata.getValue("useConstraints"));
        }
        catch (DataNotFoundException | AttributeDoesNotExistException e)
        {
        }

        hierarchicalRelationship.apply();
      });
    }
  }

  public void migrateInheritedHierarchyAnnotation()
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());

    try (OIterator<? extends InheritedHierarchyAnnotation> it = query.getIterator())
    {
      it.getAll().forEach(annotation -> {
        HierarchicalRelationshipType forHierarchicalRelationshipType = HierarchicalRelationshipType.getByMdTermRelationship(annotation.getForHierarchy());
        HierarchicalRelationshipType inheritedHierarchicalRelationshipType = HierarchicalRelationshipType.getByMdTermRelationship(annotation.getInheritedHierarchy());

        annotation.appLock();
        annotation.setForHierarchicalRelationshipType(forHierarchicalRelationshipType);
        annotation.setInheritedHierarchicalRelationshipType(inheritedHierarchicalRelationshipType);
        annotation.apply();
      });
    }
  }
}
