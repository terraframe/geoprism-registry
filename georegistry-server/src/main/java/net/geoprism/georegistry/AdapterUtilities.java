package net.geoprism.georegistry;

import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
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
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdAttributeUUID;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryIdService;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.registry.GeoObjectStatus;

public class AdapterUtilities
{
  private Logger logger = LoggerFactory.getLogger(AdapterUtilities.class);

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
   * @return
   */
  public GeoObject applyGeoObject(GeoObject geoObject, boolean isNew)
  {
    // TODO : Virtual leaf nodes

    GeoEntity ge;
    if (!isNew)
    {
      String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(geoObject.getUid(), geoObject.getType());

      ge = GeoEntity.get(runwayId);
      ge.appLock();
    }
    else
    {
      if (!RegistryIdService.getInstance().isIssuedId(geoObject.getUid()))
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(geoObject.getUid());
        throw ex;
      }

      ge = new GeoEntity();
    }

    if (geoObject.getCode() != null)
    {
      ge.setGeoId(geoObject.getCode());
    }

    if (geoObject.getLocalizedDisplayLabel() != null)
    {
      ge.getDisplayLabel().setValue(geoObject.getLocalizedDisplayLabel());
    }

    if (geoObject.getType() != null)
    {
      GeoObjectType got = geoObject.getType();

      Universal inputUni = ServiceFactory.getConversionService().geoObjectTypeToUniversal(got);
      Universal oldUni = ge.getUniversal();

      if (oldUni == null && inputUni != null)
      {
        ge.setUniversal(inputUni);
      }
      else if (oldUni != null && inputUni == null)
      {
        // do nothing
      }
      else if (oldUni != null && inputUni != null && inputUni.getKey() != oldUni.getKey())
      {
        if (!isNew && oldUni != null)
        {
          Business biz = ServiceFactory.getUtilities().getGeoEntityBusiness(ge);

          if (biz == null)
          {
            logger.error("Expected to find a business object on MdBusiness table [" + oldUni.getMdBusiness().definesType() + "] with GeoEntity oid [" + ge.getOid() + "].");
          }

          biz.delete();
        }

        ge.setUniversal(inputUni);
      }
    }

    Geometry geom = geoObject.getGeometry();
    if (geom != null)
    {
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

    /**
     * Figure out what status we are
     */
    GeoObjectStatus gos;
    Term statusTerm;
    if (isNew)
    {
      gos = GeoObjectStatus.ACTIVE;
      statusTerm = ConversionService.getInstance().geoObjectStatusToTerm(gos);
    }
    else
    {
      statusTerm = geoObject.getStatus();
      gos = ConversionService.getInstance().termToGeoObjectStatus(statusTerm);
    }

    biz.setValue(RegistryConstants.UUID, geoObject.getUid());
    biz.setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, ge.getOid());
    biz.setValue(DefaultAttribute.CODE.getName(), geoObject.getCode());
    biz.setValue(DefaultAttribute.STATUS.getName(), gos.getOid());

    Map<String, AttributeType> attributes = geoObject.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (biz.hasAttribute(attributeName) && !biz.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          // String termId = biz.getValue(attributeName);
          //
          // if (termId != null && termId.length() > 0)
          // {
          // Classifier classifier = Classifier.get(termId);
          //
          // biz.setValue(attributeName,
          // this.getTerm(classifier.getClassifierId()));
          // }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value != null)
          {
            biz.setValue(attributeName, value);
          }
          else
          {
            biz.setValue(attributeName, (String) null);
          }
        }
      }
    });

    biz.apply();

    /*
     * Update the returned GeoObject
     */
    geoObject.setStatus(statusTerm);

    return geoObject;
  }

  /**
   * Fetches a new GeoObject from the database for the given registry id.
   * 
   * @return
   */
  public GeoObject getGeoObjectById(String registryId, String geoObjectTypeCode)
  {
    // TODO : virtual leaf nodes

    GeoObjectType got = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();

    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(registryId, got);

    GeoEntity ge = GeoEntity.get(runwayId);

    GeoObject gobj = ServiceFactory.getConversionService().geoEntityToGeoObject(ge);

    return gobj;
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
    // TODO : virtual leaf nodes

    GeoEntity geo = GeoEntity.getByKey(code);

    GeoObject geoObject = ServiceFactory.getConversionService().geoEntityToGeoObject(geo);

    return geoObject;
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

    // Create the permissions for the new MdBusiness
    assignDefaultRolePermissions(mdBusiness);

    // Add the default attributes.
    this.createDefaultAttributes(universal, mdBusiness);

    universal.setMdBusiness(mdBusiness);

    universal.apply();

    return universal;
  }

  public void assignDefaultRolePermissions(MdBusiness mdBusiness)
  {
    RoleDAO adminRole = RoleDAO.findRole("geoprism.admin.Administrator").getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, mdBusiness.getOid());
    adminRole.grantPermission(Operation.DELETE, mdBusiness.getOid());
    adminRole.grantPermission(Operation.WRITE, mdBusiness.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
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

    MdAttributeReference geoEntRefMdAttrRef = new MdAttributeReference();
    geoEntRefMdAttrRef.setAttributeName(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
    geoEntRefMdAttrRef.getDisplayLabel().setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_LABEL);
    geoEntRefMdAttrRef.getDescription().setValue("References a GeoEntity for non-leaf Universal Types");
    geoEntRefMdAttrRef.setMdBusiness(mdBusGeoEntity);
    geoEntRefMdAttrRef.setDefiningMdClass(definingMdBusiness);
    geoEntRefMdAttrRef.setRequired(false);
    geoEntRefMdAttrRef.apply();

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
    codeMdAttr.getDescription().setValue(DefaultAttribute.CODE.getDefaultLocalizedDescription());
    codeMdAttr.setDatabaseSize(255);
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
    objStatusNdAttrEnum.getDescription().setValue(DefaultAttribute.STATUS.getDefaultLocalizedDescription());
    objStatusNdAttrEnum.setRequired(true);
    objStatusNdAttrEnum.setMdEnumeration(geoObjectStatusEnum);
    objStatusNdAttrEnum.setSelectMultiple(false);
    objStatusNdAttrEnum.setDefiningMdClass(definingMdBusiness);
    objStatusNdAttrEnum.apply();

    if (universal.getIsLeafType())
    {
      com.runwaysdk.system.gis.geo.GeometryType geometryType = universal.getGeometryType().get(0);

      MdAttributeGeometry mdAttributeGeometry;

      if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POINT))
      {
        mdAttributeGeometry = new MdAttributePoint();
        mdAttributeGeometry.setAttributeName(RegistryConstants.GEO_POINT_ATTRIBUTE_NAME);
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_POINT_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.LINE))
      {
        mdAttributeGeometry = new MdAttributeLineString();
        mdAttributeGeometry.setAttributeName(RegistryConstants.GEO_LINE_ATTRIBUTE_NAME);
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_LINE_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POLYGON))
      {
        mdAttributeGeometry = new MdAttributePolygon();
        mdAttributeGeometry.setAttributeName(RegistryConstants.GEO_POLYGON_ATTRIBUTE_NAME);
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_POLYGON_ATTRIBUTE_LABEL);
      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT))
      {
        mdAttributeGeometry = new MdAttributeMultiPoint();
        mdAttributeGeometry.setAttributeName(RegistryConstants.GEO_MULTIPOINT_ATTRIBUTE_NAME);
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTIPOINT_ATTRIBUTE_LABEL);

      }
      else if (geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTILINE))
      {
        mdAttributeGeometry = new MdAttributeMultiLineString();
        mdAttributeGeometry.setAttributeName(RegistryConstants.GEO_MULTILINE_ATTRIBUTE_NAME);
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTILINE_ATTRIBUTE_LABEL);

      }
      else // geometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON
      {
        mdAttributeGeometry = new MdAttributeMultiPolygon();
        mdAttributeGeometry.setAttributeName(RegistryConstants.GEO_MULTIPOLYGON_ATTRIBUTE_NAME);
        mdAttributeGeometry.getDisplayLabel().setValue(RegistryConstants.GEO_MULTIPOLYGON_ATTRIBUTE_LABEL);
      }

      mdAttributeGeometry.setRequired(false);
      mdAttributeGeometry.setDefiningMdClass(definingMdBusiness);
      mdAttributeGeometry.setSrid(GeoserverFacade.SRS_CODE);
      mdAttributeGeometry.apply();
    }
  }
}
