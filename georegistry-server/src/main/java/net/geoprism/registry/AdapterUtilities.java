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
package net.geoprism.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.WKTParsingProblem;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.TermUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.dashboard.GeometryUpdateException;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.CodeRestriction;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.query.UidRestriction;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;

public class AdapterUtilities
{
  public synchronized static AdapterUtilities getInstance()
  {
    return ServiceFactory.getUtilities();
  }

  public AdapterUtilities()
  {
  }

  /**
   * Applies the GeoObject to the database.
   * 
   * @param geoObject
   * @param isNew
   * @param statusCode
   *          TODO
   * @param isImport
   *          TODO
   * @return
   */
  public GeoObject applyGeoObject(GeoObject geoObject, boolean isNew, String statusCode, boolean isImport)
  {
    if (geoObject.getType().isLeaf())
    {
      this.applyLeafObject(geoObject, isNew, statusCode, isImport);
    }
    else
    {

      this.applyTreeObject(geoObject, isNew, statusCode, isImport);
    }

    return this.getGeoObjectByCode(geoObject.getCode(), geoObject.getType().getCode());
  }

  private void applyLeafObject(GeoObject geoObject, boolean isNew, String statusCode, boolean isImport)
  {
    Business biz = this.constructLeafObject(geoObject, isNew);

    if (geoObject.getCode() != null)
    {
      biz.setValue(GeoObject.CODE, geoObject.getCode());
    }

    ServiceFactory.getConversionService().populate((LocalStruct) biz.getStruct(GeoObject.DISPLAY_LABEL), geoObject.getDisplayLabel());

    Geometry geom = geoObject.getGeometry();
    if (geom != null)
    {
      if (!this.isValidGeometry(geoObject.getType(), geom))
      {
        GeometryTypeException ex = new GeometryTypeException();
        ex.setActualType(geom.getGeometryType());
        ex.setExpectedType(geoObject.getGeometryType().name());

        throw ex;
      }
      else
      {
        biz.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, geom);
      }
    }

    if (!isImport && !isNew && !geoObject.getType().isGeometryEditable() && biz.isModified(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME))
    {
      throw new GeometryUpdateException();
    }

    Term status = this.populateBusiness(geoObject, isNew, biz, null, statusCode);

    biz.apply();

    /*
     * Update the returned GeoObject
     */
    geoObject.setStatus(status);

  }

  private void applyTreeObject(GeoObject geoObject, boolean isNew, String statusCode, boolean isImport)
  {
    GeoEntity ge = this.constructGeoEntity(geoObject, isNew);

    if (geoObject.getCode() != null)
    {
      ge.setGeoId(geoObject.getCode());
    }

    ServiceFactory.getConversionService().populate(ge.getDisplayLabel(), geoObject.getDisplayLabel());

    Geometry geom = geoObject.getGeometry();
    if (geom != null)
    {
      if (!this.isValidGeometry(geoObject.getType(), geom))
      {
        GeometryTypeException ex = new GeometryTypeException();
        ex.setActualType(geom.getGeometryType());
        ex.setExpectedType(geoObject.getGeometryType().name());

        throw ex;
      }

      try
      {
        GeometryHelper geometryHelper = new GeometryHelper();
        ge.setGeoPoint(geometryHelper.getGeoPoint(geom));
        ge.setGeoMultiPolygon(geometryHelper.getGeoMultiPolygon(geom));
        ge.setWkt(geom.toText());
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

    if (!isImport && !isNew && !geoObject.getType().isGeometryEditable() && ge.isModified(GeoEntity.WKT))
    {
      throw new GeometryUpdateException();
    }

    ge.apply();

    Business biz;
    MdBusiness mdBiz = ge.getUniversal().getMdBusiness();

    if (isNew)
    {
      biz = new Business(mdBiz.definesType());
    }
    else
    {
      biz = this.getGeoEntityBusiness(ge);
      biz.appLock();
    }

    Term statusTerm = populateBusiness(geoObject, isNew, biz, ge, statusCode);

    biz.apply();

    /*
     * Update the returned GeoObject
     */
    geoObject.setStatus(statusTerm);
  }

  @SuppressWarnings("unchecked")
  private Term populateBusiness(GeoObject geoObject, boolean isNew, Business business, GeoEntity entity, String statusCode)
  {
    GeoObjectStatus gos = isNew ? GeoObjectStatus.PENDING : ConversionService.getInstance().termToGeoObjectStatus(geoObject.getStatus());

    if (statusCode != null)
    {
      gos = ConversionService.getInstance().termToGeoObjectStatus(statusCode);
    }

    business.setValue(RegistryConstants.UUID, geoObject.getUid());
    business.setValue(DefaultAttribute.CODE.getName(), geoObject.getCode());
    business.setValue(DefaultAttribute.STATUS.getName(), gos.getOid());

    if (entity != null)
    {
      business.setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, entity.getOid());
    }

    Map<String, AttributeType> attributes = geoObject.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (business.hasAttribute(attributeName) && !business.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          Iterator<String> it = (Iterator<String>) geoObject.getValue(attributeName);

          if (it.hasNext())
          {
            String code = it.next();

            String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, code);
            Classifier classifier = Classifier.getByKey(classifierKey);

            business.setValue(attributeName, classifier.getOid());
          }
          else
          {
            business.setValue(attributeName, (String) null);
          }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value != null)
          {
            business.setValue(attributeName, value);
          }
          else
          {
            business.setValue(attributeName, (String) null);
          }
        }
      }
    });

    return ConversionService.getInstance().geoObjectStatusToTerm(gos);
  }

  private Business constructLeafObject(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), geoObject.getType());

      Business business = Business.get(runwayId);
      business.appLock();

      return business;
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      ServerGeoObjectType type = ServerGeoObjectType.get(geoObject.getType());

      return new Business(type.definesType());
    }
  }

  private GeoEntity constructGeoEntity(GeoObject geoObject, boolean isNew)
  {
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), geoObject.getType());

      GeoEntity entity = GeoEntity.get(runwayId);
      entity.appLock();

      return entity;
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      ServerGeoObjectType type = ServerGeoObjectType.get(geoObject.getType());

      GeoEntity entity = new GeoEntity();
      entity.setUniversal(type.getUniversal());
      return entity;
    }
  }

  private boolean isValidGeometry(GeoObjectType got, Geometry geometry)
  {
    if (geometry != null)
    {
      GeometryType type = got.getGeometryType();

      if (type.equals(GeometryType.LINE) && ! ( geometry instanceof LineString ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTILINE) && ! ( geometry instanceof MultiLineString ))
      {
        return false;
      }
      else if (type.equals(GeometryType.POINT) && ! ( geometry instanceof Point ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTIPOINT) && ! ( geometry instanceof MultiPoint ))
      {
        return false;
      }
      else if (type.equals(GeometryType.POLYGON) && ! ( geometry instanceof Polygon ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTIPOLYGON) && ! ( geometry instanceof MultiPolygon ))
      {
        return false;
      }

      return true;
    }

    return true;
  }

  /**
   * Fetches a new GeoObject from the database for the given registry id.
   * 
   * @return
   */
  public GeoObject getGeoObjectById(String registryId, String geoObjectTypeCode)
  {
    GeoObjectQuery query = ServiceFactory.getRegistryService().createQuery(geoObjectTypeCode);
    query.setRestriction(new UidRestriction(registryId));

    GeoObject gObject = query.getSingleResult();

    if (gObject == null)
    {
      InvalidRegistryIdException ex = new InvalidRegistryIdException();
      ex.setRegistryId(registryId);
      throw ex;
    }

    return gObject;
  }

  public Business getGeoEntityBusiness(GeoEntity ge)
  {
    QueryFactory qf = new QueryFactory();
    BusinessQuery bq = qf.businessQuery(ge.getUniversal().getMdBusiness().definesType());
    bq.WHERE(bq.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(ge));
    OIterator<? extends Business> bit = bq.getIterator();
    try
    {
      if (bit.hasNext())
      {
        return bit.next();
      }
    }
    finally
    {
      bit.close();
    }

    return null;
  }

  public GeoObject getGeoObjectByCode(String code, String typeCode)
  {
    GeoObjectQuery query = ServiceFactory.getRegistryService().createQuery(typeCode);
    query.setRestriction(new CodeRestriction(code));

    GeoObject gObject = query.getSingleResult();

    if (gObject == null)
    {
      throw new DataNotFoundException("Unable to find GeoObject with code [" + code + "]", query.getType().getMdBusinessDAO());
    }

    return gObject;
  }

  /**
   * Returns all ancestors of a GeoObjectType
   * 
   * @param GeoObjectType
   *          child
   * @param code
   *          The Hierarchy code
   * @return
   */
  @Request
  public List<GeoObjectType> getAncestors(GeoObjectType child, String code)
  {
    List<GeoObjectType> ancestors = new LinkedList<GeoObjectType>();

    ServerGeoObjectType type = ServerGeoObjectType.get(child);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();
    MdTermRelationship mdTermRelationship = ServiceFactory.getConversionService().existingHierarchyToUniversalMdTermRelationiship(hierarchyType);

    Collection<com.runwaysdk.business.ontology.Term> list = GeoEntityUtil.getOrderedAncestors(Universal.getRoot(), type.getUniversal(), mdTermRelationship.definesType());

    list.forEach(term -> {
      Universal parent = (Universal) term;

      if (!parent.getKeyName().equals(Universal.ROOT) && !parent.getOid().equals(type.getUniversal().getOid()))
      {
        ancestors.add(ServerGeoObjectType.get(parent).getType());
      }
    });

    return ancestors;
  }

  // public HierarchyType getHierarchyTypeById(String oid)
  // {
  // MdTermRelationship mdTermRel = MdTermRelationship.get(oid);
  //
  // HierarchyType ht =
  // ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRel);
  //
  // return ht;
  // }

  // public GeoObjectType getGeoObjectTypeById(String id)
  // {
  // Universal uni = Universal.get(id);
  //
  // return
  // ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(uni.getKey()).get();
  // }

  public JsonArray getHierarchiesForType(GeoObjectType geoObjectType, Boolean includeTypes)
  {
    ConversionService service = ServiceFactory.getConversionService();

    ServerGeoObjectType type = ServerGeoObjectType.get(geoObjectType);
    HierarchyType[] hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    JsonArray hierarchies = new JsonArray();
    Universal root = Universal.getRoot();

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      MdTermRelationship mdTerm = service.existingHierarchyToUniversalMdTermRelationiship(hierarchyType);

      // Note: Ordered ancestors always includes self
      Collection<?> parents = GeoEntityUtil.getOrderedAncestors(root, type.getUniversal(), mdTerm.definesType());

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

  public JsonArray getHierarchiesForGeoObject(GeoObject geoObject)
  {
    ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(geoObject.getType());
    ConversionService service = ServiceFactory.getConversionService();

    HierarchyType[] hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    JsonArray hierarchies = new JsonArray();
    Universal root = Universal.getRoot();

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      MdTermRelationship mdTerm = service.existingHierarchyToUniversalMdTermRelationiship(hierarchyType);

      // Note: Ordered ancestors always includes self
      Collection<?> uniParents = GeoEntityUtil.getOrderedAncestors(root, geoObjectType.getUniversal(), mdTerm.definesType());

      ParentTreeNode ptnAncestors = this.getParentGeoObjects(geoObject.getUid(), geoObject.getType().getCode(), null, true);

      if (uniParents.size() > 1)
      {
        JsonObject object = new JsonObject();
        object.addProperty("code", hierarchyType.getCode());
        object.addProperty("label", hierarchyType.getLabel().getValue());

        JsonArray pArray = new JsonArray();

        for (Object parent : uniParents)
        {
          ServerGeoObjectType pType = ServerGeoObjectType.get((Universal) parent);

          if (!pType.getCode().equals(geoObjectType.getCode()))
          {
            JsonObject pObject = new JsonObject();
            pObject.addProperty("code", pType.getCode());
            pObject.addProperty("label", pType.getLabel().getValue());

            List<ParentTreeNode> ptns = ptnAncestors.findParentOfType(pType.getCode());
            for (ParentTreeNode ptn : ptns)
            {
              if (ptn.getHierachyType().getCode().equals(hierarchyType.getCode()))
              {
                pObject.add("ptn", ptn.toJSON());
                break; // TODO Sibling ancestors
              }
            }

            pArray.add(pObject);
          }
        }

        object.add("parents", pArray);

        hierarchies.add(object);
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

  public ParentTreeNode getParentGeoObjects(String childId, String childGeoObjectTypeCode, String[] parentTypes, boolean recursive)
  {
    return internalGetParentGeoObjects(childId, childGeoObjectTypeCode, parentTypes, recursive, null);
  }

  private ParentTreeNode internalGetParentGeoObjects(String childId, String childGeoObjectTypeCode, String[] parentTypes, boolean recursive, HierarchyType htIn)
  {
    GeoObject goChild = ServiceFactory.getUtilities().getGeoObjectById(childId, childGeoObjectTypeCode);
    String childRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goChild.getUid(), goChild.getType());

    ParentTreeNode tnRoot = new ParentTreeNode(goChild, htIn);

    if (goChild.getType().isLeaf())
    {
      Business business = Business.get(childRunwayId);

      List<MdAttributeDAOIF> mdAttributes = business.getMdAttributeDAOs().stream().filter(mdAttribute -> {
        if (mdAttribute instanceof MdAttributeReferenceDAOIF)
        {
          MdBusinessDAOIF referenceMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

          if (referenceMdBusiness.definesType().equals(GeoEntity.CLASS))
          {
            return true;
          }
        }

        return false;
      }).collect(Collectors.toList());

      mdAttributes.forEach(mdAttribute -> {

        String parentRunwayId = business.getValue(mdAttribute.definesAttribute());

        if (parentRunwayId != null && parentRunwayId.length() > 0)
        {
          GeoEntity geParent = GeoEntity.get(parentRunwayId);
          GeoObject goParent = ServiceFactory.getConversionService().geoEntityToGeoObject(geParent);
          Universal uni = geParent.getUniversal();

          if (parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()))
          {
            ParentTreeNode tnParent;

            HierarchyType ht = AttributeHierarchy.getHierarchyType(mdAttribute.getKey());

            if (recursive)
            {
              tnParent = this.internalGetParentGeoObjects(goParent.getUid(), goParent.getType().getCode(), parentTypes, recursive, ht);
            }
            else
            {
              tnParent = new ParentTreeNode(goParent, ht);
            }

            tnRoot.addParent(tnParent);
          }
        }
      });

    }
    else
    {

      String[] relationshipTypes = TermUtil.getAllChildRelationships(childRunwayId);

      Map<String, HierarchyType> htMap = this.getHierarchyTypeMap(relationshipTypes);

      TermAndRel[] tnrParents = TermUtil.getDirectAncestors(childRunwayId, relationshipTypes);
      for (TermAndRel tnrParent : tnrParents)
      {
        GeoEntity geParent = (GeoEntity) tnrParent.getTerm();
        Universal uni = geParent.getUniversal();

        if (!geParent.getOid().equals(GeoEntity.getRoot().getOid()) && ( parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()) ))
        {
          GeoObject goParent = ServiceFactory.getConversionService().geoEntityToGeoObject(geParent);
          HierarchyType ht = htMap.get(tnrParent.getRelationshipType());

          ParentTreeNode tnParent;
          if (recursive)
          {
            tnParent = this.internalGetParentGeoObjects(goParent.getUid(), goParent.getType().getCode(), parentTypes, recursive, ht);
          }
          else
          {
            tnParent = new ParentTreeNode(goParent, ht);
          }

          tnRoot.addParent(tnParent);
        }
      }
    }

    return tnRoot;
  }

  public ChildTreeNode getChildGeoObjects(String parentUid, String parentGeoObjectTypeCode, String[] childrenTypes, Boolean recursive)
  {
    return internalGetChildGeoObjects(parentUid, parentGeoObjectTypeCode, childrenTypes, recursive, null);
  }

  private ChildTreeNode internalGetChildGeoObjects(String parentUid, String parentGeoObjectTypeCode, String[] childrenTypes, Boolean recursive, HierarchyType htIn)
  {
    GeoObject goParent = this.getGeoObjectById(parentUid, parentGeoObjectTypeCode);

    if (goParent.getType().isLeaf())
    {
      throw new UnsupportedOperationException("Leaf nodes cannot have children.");
    }

    String parentRunwayId = RegistryIdService.getInstance().registryIdToRunwayId(goParent.getUid(), goParent.getType());

    String[] relationshipTypes = TermUtil.getAllParentRelationships(parentRunwayId);
    Map<String, HierarchyType> htMap = this.getHierarchyTypeMap(relationshipTypes);
    GeoEntity parent = GeoEntity.get(parentRunwayId);

    GeoObject goRoot = ServiceFactory.getConversionService().geoEntityToGeoObject(parent);
    ChildTreeNode tnRoot = new ChildTreeNode(goRoot, htIn);

    /*
     * Handle leaf node children
     */
    if (childrenTypes != null)
    {
      for (int i = 0; i < childrenTypes.length; ++i)
      {
        ServerGeoObjectType childType = ServerGeoObjectType.get(childrenTypes[i]);

        if (childType.isLeaf())
        {
          if (ArrayUtils.contains(childrenTypes, childType.getCode()))
          {
            List<MdAttributeDAOIF> mdAttributes = childType.definesAttributes().stream().filter(mdAttribute -> {
              if (mdAttribute instanceof MdAttributeReferenceDAOIF)
              {
                MdBusinessDAOIF referenceMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

                if (referenceMdBusiness.definesType().equals(GeoEntity.CLASS))
                {
                  return true;
                }
              }

              return false;
            }).collect(Collectors.toList());

            for (MdAttributeDAOIF mdAttribute : mdAttributes)
            {
              HierarchyType ht = AttributeHierarchy.getHierarchyType(mdAttribute.getKey());

              BusinessQuery query = new QueryFactory().businessQuery(childType.definesType());
              query.WHERE(query.get(mdAttribute.definesAttribute()).EQ(parentRunwayId));

              OIterator<Business> it = query.getIterator();

              try
              {
                List<Business> children = it.getAll();

                for (Business child : children)
                {
                  // Do something
                  GeoObject goChild = ServiceFactory.getConversionService().leafToGeoObject(childType.getType(), child);

                  tnRoot.addChild(new ChildTreeNode(goChild, ht));
                }
              }
              finally
              {
                it.close();
              }
            }
          }
        }
      }
    }

    /*
     * Handle tree node children
     */
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parentRunwayId, relationshipTypes);
    for (TermAndRel tnrChild : tnrChildren)
    {
      GeoEntity geChild = (GeoEntity) tnrChild.getTerm();
      Universal uni = geChild.getUniversal();

      if (childrenTypes == null || childrenTypes.length == 0 || ArrayUtils.contains(childrenTypes, uni.getKey()))
      {
        GeoObject goChild = ServiceFactory.getConversionService().geoEntityToGeoObject(geChild);
        HierarchyType ht = htMap.get(tnrChild.getRelationshipType());

        ChildTreeNode tnChild;
        if (recursive)
        {
          tnChild = this.internalGetChildGeoObjects(goChild.getUid(), goChild.getType().getCode(), childrenTypes, recursive, ht);
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
      MdTermRelationship mdRel = (MdTermRelationship) MdTermRelationship.getMdRelationship(relationshipType);

      HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdRel);

      map.put(relationshipType, ht);
    }

    return map;
  }

}
