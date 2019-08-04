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
package net.geoprism.registry.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessEnumeration;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.BusinessDAO;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.attributes.entity.AttributeLocal;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.AssociationType;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.RelationshipCache;
import com.runwaysdk.util.IDGenerator;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ConversionService
{
  public ConversionService()
  {
  }

  public static ConversionService getInstance()
  {
    return ServiceFactory.getConversionService();
  }

  public void populate(LocalStruct struct, LocalizedValue label)
  {
    struct.setValue(label.getValue());
    struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      if (label.contains(locale))
      {
        struct.setValue(locale, label.getValue(locale));
      }
    }
  }

  public void populate(LocalStruct struct, LocalizedValue label, String suffix)
  {
    struct.setValue(label.getValue());
    struct.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, label.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE) + suffix);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      if (label.contains(locale))
      {
        struct.setValue(locale, label.getValue(locale) + suffix);
      }
    }
  }

  /**
   * It creates an {@link MdTermRelationship} to model the relationship between
   * {@link GeoEntity}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdTermRelationship newHierarchyToMdTermRelForGeoEntities(HierarchyType hierarchyType)
  {
    MdBusiness mdBusGeoEntity = MdBusiness.getMdBusiness(GeoEntity.CLASS);

    MdTermRelationship mdTermRelationship = new MdTermRelationship();

    mdTermRelationship.setTypeName(hierarchyType.getCode());
    mdTermRelationship.setPackageName(GISConstants.GEO_PACKAGE);
    this.populate(mdTermRelationship.getDisplayLabel(), hierarchyType.getLabel());
    this.populate(mdTermRelationship.getDescription(), hierarchyType.getDescription());
    mdTermRelationship.setIsAbstract(false);
    mdTermRelationship.setGenerateSource(false);
    mdTermRelationship.addCacheAlgorithm(RelationshipCache.CACHE_NOTHING);
    mdTermRelationship.addAssociationType(AssociationType.Graph);
    mdTermRelationship.setRemove(true);
    // Create the relationship between different universals.
    mdTermRelationship.setParentMdBusiness(mdBusGeoEntity);
    mdTermRelationship.setParentCardinality("1");
    mdTermRelationship.setChildMdBusiness(mdBusGeoEntity);
    mdTermRelationship.setChildCardinality("*");
    mdTermRelationship.setParentMethod("Parent");
    mdTermRelationship.setChildMethod("Children");

    return mdTermRelationship;
  }

  public Term getTerm(String code)
  {
    return ServiceFactory.getAdapter().getMetadataCache().getTerm(code).get();
  }

  // public GeoObject getGeoObjectById(String uuid, String geoObjectTypeCode)
  // {
  // Universal universal = Universal.getByKey(geoObjectTypeCode);
  //
  // MdBusiness mdBusiness = universal.getMdBusiness();
  //
  //
  //
  // }

  @Transaction
  public static void createBusinessObjectForExistingGeoEntity(GeoEntity geoEntity)
  {
    Universal universal = geoEntity.getUniversal();
    MdBusiness mdBusiness = universal.getMdBusiness();

    String uuid = IDGenerator.nextID();

    BusinessDAO businessDAO = BusinessDAO.newInstance(mdBusiness.definesType());
    businessDAO.setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, geoEntity.getOid());
    businessDAO.setValue(RegistryConstants.UUID, uuid);
    businessDAO.setValue(DefaultAttribute.CODE.getName(), geoEntity.getGeoId());
    businessDAO.setValue(ComponentInfo.KEY, geoEntity.getGeoId());
    businessDAO.addItem(DefaultAttribute.STATUS.getName(), GeoObjectStatus.ACTIVE.getOid());
    businessDAO.apply();
  }

  public GeoObjectStatus termToGeoObjectStatus(Term term)
  {
    return this.termToGeoObjectStatus(term.getCode());
  }

  public GeoObjectStatus termToGeoObjectStatus(String termCode)
  {
    if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code))
    {
      return GeoObjectStatus.ACTIVE;
    }
    else if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code))
    {
      return GeoObjectStatus.INACTIVE;
    }
    else if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.NEW.code))
    {
      return GeoObjectStatus.NEW;
    }
    else if (termCode.equals(DefaultTerms.GeoObjectStatusTerm.PENDING.code))
    {
      return GeoObjectStatus.PENDING;
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status Term [" + termCode + "].");
    }
  }

  public Term geoObjectStatusToTerm(GeoObjectStatus gos)
  {
    return geoObjectStatusToTerm(gos.getEnumName());
  }

  public Term geoObjectStatusToTerm(String termCode)
  {
    if (termCode.equals(GeoObjectStatus.ACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.INACTIVE.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code);
    }
    else if (termCode.equals(GeoObjectStatus.NEW.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code);
    }
    else if (termCode.equals(GeoObjectStatus.PENDING.getEnumName()))
    {
      return getTerm(DefaultTerms.GeoObjectStatusTerm.PENDING.code);
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status [" + termCode + "].");
    }
  }
}
