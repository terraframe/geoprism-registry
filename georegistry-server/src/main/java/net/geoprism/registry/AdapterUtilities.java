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
import org.commongeoregistry.adapter.constants.DefaultTerms.GeoObjectStatusTerm;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLocalDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMultiTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdLocalStructDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.WKTParsingProblem;
import com.runwaysdk.system.gis.mapping.GeoserverFacade;
import com.runwaysdk.system.gis.metadata.MdAttributeGeometry;
import com.runwaysdk.system.gis.metadata.MdAttributeLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPoint;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPolygon;
import com.runwaysdk.system.gis.metadata.MdAttributePoint;
import com.runwaysdk.system.gis.metadata.MdAttributePolygon;
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeDouble;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeLocalCharacter;
import com.runwaysdk.system.metadata.MdAttributeLong;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdAttributeTerm;
import com.runwaysdk.system.metadata.MdAttributeUUID;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.TermUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.DefaultConfiguration;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.conversion.TermBuilder;
import net.geoprism.registry.query.CodeRestriction;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.query.UidRestriction;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.WMSService;

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
   * @return
   */
  public GeoObject applyGeoObject(GeoObject geoObject, boolean isNew, String statusCode)
  {
    if (geoObject.getType().isLeaf())
    {
      this.applyLeafObject(geoObject, isNew, statusCode);
    }
    else
    {

      this.applyTreeObject(geoObject, isNew, statusCode);
    }

    return this.getGeoObjectByCode(geoObject.getCode(), geoObject.getType().getCode());
  }

  private void applyLeafObject(GeoObject geoObject, boolean isNew, String statusCode)
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

    Term status = this.populateBusiness(geoObject, isNew, biz, null, statusCode);

    biz.apply();

    /*
     * Update the returned GeoObject
     */
    geoObject.setStatus(status);

  }

  private void applyTreeObject(GeoObject geoObject, boolean isNew, String statusCode)
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

      GeoObjectType type = geoObject.getType();
      Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(type);
      MdBusiness mdBiz = universal.getMdBusiness();

      return new Business(mdBiz.definesType());
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

      Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(geoObject.getType());

      GeoEntity entity = new GeoEntity();
      entity.setUniversal(universal);
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
      throw new DataNotFoundException("Unable to find GeoObject with code [" + code + "]", MdBusinessDAO.get(query.getUniversal().getMdBusinessOid()));
    }

    return gObject;
  }

  @Request
  public List<GeoObjectType> getAncestors(GeoObjectType child, String code)
  {
    List<GeoObjectType> ancestors = new LinkedList<GeoObjectType>();

    Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(child);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();
    MdTermRelationship mdTermRelationship = ServiceFactory.getConversionService().existingHierarchyToUniversalMdTermRelationiship(hierarchyType);

    Collection<com.runwaysdk.business.ontology.Term> list = GeoEntityUtil.getOrderedAncestors(Universal.getRoot(), universal, mdTermRelationship.definesType());

    list.forEach(term -> {
      Universal parent = (Universal) term;

      if (!parent.getKeyName().equals(Universal.ROOT) && !parent.getOid().equals(universal.getOid()))
      {
        ancestors.add(ServiceFactory.getConversionService().universalToGeoObjectType(parent));
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

  @Transaction
  public Universal createGeoObjectType(GeoObjectType geoObjectType)
  {
    Universal universal = ServiceFactory.getConversionService().newGeoObjectTypeToUniversal(geoObjectType);

    MdBusiness mdBusiness = new MdBusiness();
    mdBusiness.setPackageName(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE);
    // The CODE name becomes the class name
    mdBusiness.setTypeName(universal.getUniversalId());
    mdBusiness.setGenerateSource(false);
    mdBusiness.setPublish(false);
    mdBusiness.setIsAbstract(false);
    mdBusiness.getDisplayLabel().setValue(universal.getDisplayLabel().getValue());
    mdBusiness.getDescription().setValue(universal.getDescription().getValue());
    mdBusiness.apply();

    // Add the default attributes.
    this.createDefaultAttributes(universal, mdBusiness);

    universal.setMdBusiness(mdBusiness);

    universal.apply();

    // Create the permissions for the new MdBusiness
    assignDefaultRolePermissions(mdBusiness);

    if (geoObjectType.isLeaf())
    {
      MdBusinessDAOIF mdBusinessDAO = MdBusinessDAO.get(mdBusiness.getOid());
      MdAttributeLocalDAOIF displayLabel = (MdAttributeLocalDAOIF) mdBusinessDAO.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName());
      MdLocalStructDAOIF mdStruct = displayLabel.getMdStructDAOIF();

      assignDefaultRolePermissions(mdStruct);
    }

    // Build the parent class term root if it does not exist.
    TermBuilder.buildIfNotExistdMdBusinessClassifier(mdBusiness);

    new WMSService().createDatabaseView(geoObjectType, true);

    return universal;
  }

  public void assignDefaultRolePermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    maintainer.grantPermission(Operation.CREATE, component.getOid());
    maintainer.grantPermission(Operation.DELETE, component.getOid());
    maintainer.grantPermission(Operation.WRITE, component.getOid());
    maintainer.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    consumer.grantPermission(Operation.READ, component.getOid());
    consumer.grantPermission(Operation.READ_ALL, component.getOid());

    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    contributor.grantPermission(Operation.READ, component.getOid());
    contributor.grantPermission(Operation.READ_ALL, component.getOid());

    // // TODO: Actual hierarchy role
    // RoleDAO hierarchyRole =
    // RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_PREFIX +
    // "LocatedIn").getBusinessDAO();
    // hierarchyRole.grantPermission(Operation.CREATE, mdBusiness.getOid());
    // hierarchyRole.grantPermission(Operation.DELETE, mdBusiness.getOid());
    // hierarchyRole.grantPermission(Operation.WRITE, mdBusiness.getOid());
    // hierarchyRole.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
  }

  /**
   * Adds default attributes to the given {@link MdBusinessDAO} according to the
   * Common Geo-Registry specification for {@link GeoObject}.
   * 
   * @param mdBusinessDAO
   *          {@link MdBusinessDAO} that will define the default attributes.
   */
  @Transaction
  public void createDefaultAttributes(Universal universal, MdBusiness definingMdBusiness)
  {
    MdBusiness mdBusGeoEntity = MdBusiness.getMdBusiness(GeoEntity.CLASS);

    if (!universal.getIsLeafType())
    {
      MdAttributeReference geoEntRefMdAttrRef = new MdAttributeReference();
      geoEntRefMdAttrRef.setAttributeName(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      geoEntRefMdAttrRef.getDisplayLabel().setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_LABEL);
      geoEntRefMdAttrRef.getDescription().setValue("References a GeoEntity for non-leaf Universal Types");
      geoEntRefMdAttrRef.setMdBusiness(mdBusGeoEntity);
      geoEntRefMdAttrRef.setDefiningMdClass(definingMdBusiness);
      geoEntRefMdAttrRef.setRequired(false);
      geoEntRefMdAttrRef.apply();
    }

    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from
    // the {@code GeoObject#OID};
    MdAttributeUUID uuidMdAttr = new MdAttributeUUID();
    uuidMdAttr.setAttributeName(RegistryConstants.UUID);
    uuidMdAttr.getDisplayLabel().setValue(RegistryConstants.UUID_LABEL);
    uuidMdAttr.getDescription().setValue("The universal unique identifier of the feature.");
    uuidMdAttr.setDefiningMdClass(definingMdBusiness);
    uuidMdAttr.setRequired(true);
    uuidMdAttr.addIndexType(MdAttributeIndices.UNIQUE_INDEX);
    uuidMdAttr.apply();

    // DefaultAttribute.CODE - defined by GeoEntity geoId
    MdAttributeCharacter codeMdAttr = new MdAttributeCharacter();
    codeMdAttr.setAttributeName(DefaultAttribute.CODE.getName());
    codeMdAttr.getDisplayLabel().setValue(DefaultAttribute.CODE.getDefaultLocalizedName());
    codeMdAttr.getDescription().setValue(DefaultAttribute.CODE.getDefaultDescription());
    codeMdAttr.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
    codeMdAttr.setDefiningMdClass(definingMdBusiness);
    codeMdAttr.setRequired(true);
    codeMdAttr.addIndexType(MdAttributeIndices.UNIQUE_INDEX);
    codeMdAttr.apply();

    // DefaultAttribute.TYPE - This is the display label of the Universal.
    // BusObject.mdBusiness.Universal.displayLabel

    // DefaultAttribute.CREATED_DATE - The create data on the Business Object?

    // DefaultAttribute.UPDATED_DATE - The update data on the Business Object?

    // DefaultAttribute.STATUS

    MdEnumeration geoObjectStatusEnum = MdEnumeration.getMdEnumeration(GeoObjectStatus.CLASS);

    MdAttributeEnumeration objStatusNdAttrEnum = new MdAttributeEnumeration();
    objStatusNdAttrEnum.setAttributeName(DefaultAttribute.STATUS.getName());
    objStatusNdAttrEnum.getDisplayLabel().setValue(DefaultAttribute.STATUS.getDefaultLocalizedName());
    objStatusNdAttrEnum.getDescription().setValue(DefaultAttribute.STATUS.getDefaultDescription());
    objStatusNdAttrEnum.setRequired(true);
    objStatusNdAttrEnum.setMdEnumeration(geoObjectStatusEnum);
    objStatusNdAttrEnum.setSelectMultiple(false);
    objStatusNdAttrEnum.setDefiningMdClass(definingMdBusiness);
    objStatusNdAttrEnum.apply();

    if (universal.getIsLeafType())
    {
      // DefaultAttribute.DISPLAY_LABEL
      MdAttributeLocalCharacter labelMdAttr = new MdAttributeLocalCharacter();
      labelMdAttr.setAttributeName(DefaultAttribute.DISPLAY_LABEL.getName());
      labelMdAttr.getDisplayLabel().setValue(DefaultAttribute.DISPLAY_LABEL.getDefaultLocalizedName());
      labelMdAttr.getDescription().setValue(DefaultAttribute.DISPLAY_LABEL.getDefaultDescription());
      labelMdAttr.setDefiningMdClass(definingMdBusiness);
      labelMdAttr.setRequired(true);
      labelMdAttr.apply();

      com.runwaysdk.system.gis.geo.GeometryType geometryType = universal.getGeometryType().get(0);

      MdAttributeGeometry mdAttributeGeometry;

      if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POINT))
      {
        mdAttributeGeometry = new MdAttributePoint();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_POINT_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.LINE))
      {
        mdAttributeGeometry = new MdAttributeLineString();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_LINE_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POLYGON))
      {
        mdAttributeGeometry = new MdAttributePolygon();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_POLYGON_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT))
      {
        mdAttributeGeometry = new MdAttributeMultiPoint();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTIPOINT_ATTRIBUTE_LABEL);

      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTILINE))
      {
        mdAttributeGeometry = new MdAttributeMultiLineString();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTILINE_ATTRIBUTE_LABEL);

      }
      else // geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON
      {
        mdAttributeGeometry = new MdAttributeMultiPolygon();
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTIPOLYGON_ATTRIBUTE_LABEL);
      }

      mdAttributeGeometry.setAttributeName(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
      mdAttributeGeometry.setRequired(false);
      mdAttributeGeometry.setDefiningMdClass(definingMdBusiness);
      mdAttributeGeometry.setSrid(GeoserverFacade.SRS_CODE);
      mdAttributeGeometry.apply();
    }
  }

  /**
   * Creates an {@link MdAttributeConcrete} for the given {@link MdBusiness}
   * from the given {@link AttributeType}
   * 
   * @pre assumes no attribute has been defined on the type with the given name.
   * 
   * @param mdBusiness
   *          Type to receive attribute definition
   * @param attributeType
   *          newly defined attribute
   * 
   * @return {@link AttributeType}
   */
  @Transaction
  public MdAttributeConcrete createMdAttributeFromAttributeType(MdBusiness mdBusiness, AttributeType attributeType)
  {
    MdAttributeConcrete mdAttribute = null;

    if (attributeType.getType().equals(AttributeCharacterType.TYPE))
    {
      mdAttribute = new MdAttributeCharacter();
      MdAttributeCharacter mdAttributeCharacter = (MdAttributeCharacter) mdAttribute;
      mdAttributeCharacter.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
    }
    else if (attributeType.getType().equals(AttributeDateType.TYPE))
    {
      mdAttribute = new MdAttributeDateTime();
    }
    else if (attributeType.getType().equals(AttributeIntegerType.TYPE))
    {
      mdAttribute = new MdAttributeLong();
    }
    else if (attributeType.getType().equals(AttributeFloatType.TYPE))
    {
      AttributeFloatType attributeFloatType = (AttributeFloatType) attributeType;

      mdAttribute = new MdAttributeDouble();
      mdAttribute.setValue(MdAttributeDoubleInfo.LENGTH, Integer.toString(attributeFloatType.getPrecision()));
      mdAttribute.setValue(MdAttributeDoubleInfo.DECIMAL, Integer.toString(attributeFloatType.getScale()));
    }
    else if (attributeType.getType().equals(AttributeTermType.TYPE))
    {
      mdAttribute = new MdAttributeTerm();
      MdAttributeTerm mdAttributeTerm = (MdAttributeTerm) mdAttribute;

      MdBusiness classifierMdBusiness = MdBusiness.getMdBusiness(Classifier.CLASS);
      mdAttributeTerm.setMdBusiness(classifierMdBusiness);
      // TODO implement support for multi-term
      // mdAttribute = new MdAttributeMultiTerm();
      // MdAttributeMultiTerm mdAttributeMultiTerm =
      // (MdAttributeMultiTerm)mdAttribute;
      //
      // MdBusiness classifierMdBusiness =
      // MdBusiness.getMdBusiness(Classifier.CLASS);
      // mdAttributeMultiTerm.setMdBusiness(classifierMdBusiness);
    }
    else if (attributeType.getType().equals(AttributeBooleanType.TYPE))
    {
      mdAttribute = new MdAttributeBoolean();
    }

    mdAttribute.setAttributeName(attributeType.getName());
    mdAttribute.setValue(MdAttributeConcreteInfo.REQUIRED, Boolean.toString(attributeType.isRequired()));

    if (attributeType.isUnique())
    {
      mdAttribute.addIndexType(MdAttributeIndices.UNIQUE_INDEX);
    }

    ServiceFactory.getConversionService().populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
    ServiceFactory.getConversionService().populate(mdAttribute.getDescription(), attributeType.getDescription());

    mdAttribute.setDefiningMdClass(mdBusiness);
    mdAttribute.apply();

    if (attributeType.getType().equals(AttributeTermType.TYPE))
    {
      MdAttributeTerm mdAttributeTerm = (MdAttributeTerm) mdAttribute;

      // Build the parent class term root if it does not exist.
      Classifier classTerm = TermBuilder.buildIfNotExistdMdBusinessClassifier(mdBusiness);

      // Create the root term node for this attribute
      Classifier attributeTermRoot = TermBuilder.buildIfNotExistAttribute(mdBusiness, mdAttributeTerm.getAttributeName(), classTerm);

      // Make this the root term of the multi-attribute
      attributeTermRoot.addClassifierTermAttributeRoots(mdAttributeTerm).apply();

      AttributeTermType attributeTermType = (AttributeTermType) attributeType;

      LocalizedValue label = ServiceFactory.getConversionService().convert(attributeTermRoot.getDisplayLabel());

      Term term = new Term(attributeTermRoot.getClassifierId(), label, new LocalizedValue(""));
      attributeTermType.setRootTerm(term);

      // MdAttributeMultiTerm mdAttributeMultiTerm =
      // (MdAttributeMultiTerm)mdAttribute;
      //
      // // Build the parent class term root if it does not exist.
      // Classifier classTerm =
      // this.buildIfNotExistdMdBusinessClassifier(mdBusiness);
      //
      // // Create the root term node for this attribute
      // Classifier attributeTermRoot =
      // this.buildIfNotExistAttribute(mdBusiness, mdAttributeMultiTerm);
      // classTerm.addIsAChild(attributeTermRoot).apply();
      //
      // // Make this the root term of the multi-attribute
      // attributeTermRoot.addClassifierMultiTermAttributeRoots(mdAttributeMultiTerm).apply();
      //
      // AttributeTermType attributeTermType = (AttributeTermType)attributeType;
      //
      // Term term = new Term(attributeTermRoot.getKey(),
      // attributeTermRoot.getDisplayLabel().getValue(), "");
      // attributeTermType.setRootTerm(term);
    }

    return mdAttribute;
  }

  /**
   * Creates an {@link MdAttributeConcrete} for the given {@link MdBusiness}
   * from the given {@link AttributeType}
   * 
   * @pre assumes no attribute has been defined on the type with the given name.
   * 
   * @param mdBusiness
   *          Type to receive attribute definition
   * @param attributeType
   *          newly defined attribute
   * 
   * @return {@link AttributeType}
   */
  @Transaction
  public MdAttributeConcrete updateMdAttributeFromAttributeType(MdBusiness mdBusiness, AttributeType attributeType)
  {
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = getMdAttribute(mdBusiness, attributeType.getName());

    if (mdAttributeConcreteDAOIF != null)
    {
      // Get the type safe version
      MdAttributeConcrete mdAttribute = (MdAttributeConcrete) BusinessFacade.get(mdAttributeConcreteDAOIF);
      mdAttribute.lock();

      try
      {
        // The name cannot be updated
        // mdAttribute.setAttributeName(attributeType.getName());
        ServiceFactory.getConversionService().populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
        ServiceFactory.getConversionService().populate(mdAttribute.getDescription(), attributeType.getDescription());

        if (attributeType instanceof AttributeFloatType)
        {
          // Refresh the terms
          AttributeFloatType attributeFloatType = (AttributeFloatType) attributeType;

          mdAttribute.setValue(MdAttributeDoubleInfo.LENGTH, Integer.toString(attributeFloatType.getPrecision()));
          mdAttribute.setValue(MdAttributeDoubleInfo.DECIMAL, Integer.toString(attributeFloatType.getScale()));
        }

        mdAttribute.apply();
      }
      finally
      {
        mdAttribute.unlock();
      }

      if (attributeType instanceof AttributeTermType)
      {
        // Refresh the terms
        AttributeTermType attributeTermType = (AttributeTermType) attributeType;

        Term getRootTerm = attributeTermType.getRootTerm();
        String classifierKey = TermBuilder.buildClassifierKeyFromTermCode(getRootTerm.getCode());

        TermBuilder termBuilder = new TermBuilder(classifierKey);
        attributeTermType.setRootTerm(termBuilder.build());
      }

      return mdAttribute;
    }

    return null;
  }

  /**
   * Delete the {@link MdAttributeConcreteDAOIF} from the given {
   * 
   * 
   * @param mdBusiness
   * @param attributeName
   */
  public void deleteMdAttributeFromAttributeType(MdBusiness mdBusiness, String attributeName)
  {
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = getMdAttribute(mdBusiness, attributeName);

    if (mdAttributeConcreteDAOIF != null)
    {
      if (mdAttributeConcreteDAOIF instanceof MdAttributeTermDAOIF || mdAttributeConcreteDAOIF instanceof MdAttributeMultiTermDAOIF)
      {
        String attributeTermKey = TermBuilder.buildtAtttributeKey(mdBusiness.getTypeName(), mdAttributeConcreteDAOIF.definesAttribute());

        try
        {
          Classifier attributeTerm = Classifier.getByKey(attributeTermKey);
          attributeTerm.delete();
        }
        catch (DataNotFoundException e)
        {
        }
      }

      mdAttributeConcreteDAOIF.getBusinessDAO().delete();
    }
  }

  /**
   * Returns the {link MdAttributeConcreteDAOIF} for the given
   * {@link AttributeType} defined on the given {@link MdBusiness} or null no
   * such attribute is defined.
   * 
   * @param mdBusiness
   * @param attributeName
   * @return
   */
  private MdAttributeConcreteDAOIF getMdAttribute(MdBusiness mdBusiness, String attributeName)
  {
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

    return mdBusinessDAOIF.definesAttribute(attributeName);
  }

  public JsonArray getHierarchiesForType(GeoObjectType geoObjectType, Boolean includeTypes)
  {
    ConversionService service = ServiceFactory.getConversionService();

    Universal universal = service.geoObjectTypeToUniversal(geoObjectType);
    HierarchyType[] hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    JsonArray hierarchies = new JsonArray();
    Universal root = Universal.getRoot();

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      MdTermRelationship mdTerm = service.existingHierarchyToUniversalMdTermRelationiship(hierarchyType);

      // Note: Ordered ancestors always includes self
      Collection<?> parents = GeoEntityUtil.getOrderedAncestors(root, universal, mdTerm.definesType());

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
            GeoObjectType pType = service.universalToGeoObjectType((Universal) parent);

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
        GeoObjectType childType = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(childrenTypes[i]).get();

        if (childType.isLeaf())
        {
          Universal universal = ServiceFactory.getConversionService().getUniversalFromGeoObjectType(childType);

          if (ArrayUtils.contains(childrenTypes, universal.getKey()))
          {
            MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(universal.getMdBusinessOid());

            List<MdAttributeDAOIF> mdAttributes = mdBusiness.definesAttributes().stream().filter(mdAttribute -> {
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

              BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
              query.WHERE(query.get(mdAttribute.definesAttribute()).EQ(parentRunwayId));

              OIterator<Business> it = query.getIterator();

              try
              {
                List<Business> children = it.getAll();

                for (Business child : children)
                {
                  // Do something
                  GeoObject goChild = ServiceFactory.getConversionService().leafToGeoObject(childType, child);

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
