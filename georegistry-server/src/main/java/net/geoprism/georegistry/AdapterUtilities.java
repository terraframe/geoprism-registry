package net.geoprism.georegistry;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
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
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeFloat;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeInteger;
import com.runwaysdk.system.metadata.MdAttributeLocalCharacter;
import com.runwaysdk.system.metadata.MdAttributeMultiTerm;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdAttributeUUID;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.georegistry.service.ConversionService;
import net.geoprism.georegistry.service.RegistryIdService;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierMultiTermAttributeRoot;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeometryTypeException;

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
    if (geoObject.getType().isLeaf())
    {
      return this.applyLeafObject(geoObject, isNew);
    }
    else
    {

      return this.applyTreeObject(geoObject, isNew);
    }
  }

  private GeoObject applyLeafObject(GeoObject geoObject, boolean isNew)
  {
    Business biz = this.constructLeafObject(geoObject, isNew);

    if (geoObject.getCode() != null)
    {
      biz.setValue(GeoObject.CODE, geoObject.getCode());
    }

    if (geoObject.getLocalizedDisplayLabel() != null)
    {
      biz.setValue(GeoObject.LOCALIZED_DISPLAY_LABEL, geoObject.getLocalizedDisplayLabel());
    }

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
        biz.setValue(GeoObject.LOCALIZED_DISPLAY_LABEL, geoObject.getLocalizedDisplayLabel());
      }
    }

    Term status = this.populateBusiness(geoObject, isNew, biz, null);

    /*
     * Update the returned GeoObject
     */
    geoObject.setStatus(status);

    return geoObject;
  }

  private GeoObject applyTreeObject(GeoObject geoObject, boolean isNew)
  {
    GeoEntity ge = this.constructGeoEntity(geoObject, isNew);

    if (geoObject.getCode() != null)
    {
      ge.setGeoId(geoObject.getCode());
    }

    if (geoObject.getLocalizedDisplayLabel() != null)
    {
      ge.getDisplayLabel().setValue(geoObject.getLocalizedDisplayLabel());
    }

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

    Term statusTerm = populateBusiness(geoObject, isNew, biz, ge);

    biz.apply();

    /*
     * Update the returned GeoObject
     */
    geoObject.setStatus(statusTerm);

    return geoObject;
  }

  @SuppressWarnings("unchecked")
  private Term populateBusiness(GeoObject geoObject, boolean isNew, Business business, GeoEntity entity)
  {
    GeoObjectStatus gos = isNew ? GeoObjectStatus.PENDING : ConversionService.getInstance().termToGeoObjectStatus(geoObject.getStatus());
    Term status = isNew ? ConversionService.getInstance().geoObjectStatusToTerm(GeoObjectStatus.PENDING) : geoObject.getStatus();

    business.setValue(RegistryConstants.UUID, geoObject.getUid());
    business.setValue(DefaultAttribute.CODE.getName(), geoObject.getCode());
    business.setValue(DefaultAttribute.STATUS.getName(), gos.getOid());

    if (entity != null)
    {
      business.setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, entity.getOid());
    }

    Map<String, AttributeType> attributes = geoObject.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (business.hasAttribute(attributeName) && !business.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          List<Term> value = (List<Term>) geoObject.getValue(attributeName);

          if (value != null && value.size() > 0)
          {
            Term term = value.get(0);
            Classifier classifier = Classifier.getByKey(term.getCode());

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
    
    return status;
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
    GeoObjectType got = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(geoObjectTypeCode).get();
    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(registryId, got);

    if (!got.isLeaf())
    {
      GeoEntity ge = GeoEntity.get(runwayId);

      GeoObject gobj = ServiceFactory.getConversionService().geoEntityToGeoObject(ge);

      return gobj;
    }
    else
    {
      Business business = Business.get(runwayId);

      return ServiceFactory.getConversionService().leafToGeoObject(got, business);
    }
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
    
    // Build the parent class term root if it does not exist.
    buildIfNotExistdMdBusinessClassifier(mdBusiness);

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
    objStatusNdAttrEnum.getDescription().setValue(DefaultAttribute.STATUS.getDefaultLocalizedDescription());
    objStatusNdAttrEnum.setRequired(true);
    objStatusNdAttrEnum.setMdEnumeration(geoObjectStatusEnum);
    objStatusNdAttrEnum.setSelectMultiple(false);
    objStatusNdAttrEnum.setDefiningMdClass(definingMdBusiness);
    objStatusNdAttrEnum.apply();

    if (universal.getIsLeafType())
    {
      // DefaultAttribute.DISPLAY_LABEL
      MdAttributeLocalCharacter labelMdAttr = new MdAttributeLocalCharacter();
      labelMdAttr.setAttributeName(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName());
      labelMdAttr.getDisplayLabel().setValue(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getDefaultLocalizedName());
      labelMdAttr.getDescription().setValue(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getDefaultLocalizedDescription());
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
   * Creates an {@link MdAttributeConcrete} for the given {@link MdBusiness} from the given {@link AttributeType}
   * 
   * @pre assumes no attribute has been defined on the type with the given name.
   * 
   * @param mdBusiness Type to receive attribute definition
   * @param attributeType newly defined attribute
   * 
   * @return {@link AttributeType}
   */
  public AttributeType createMdAttributeFromAttributeType(MdBusiness mdBusiness, AttributeType attributeType)
  {  
	MdAttributeConcrete mdAttribute = null;
	  
	if (attributeType.getType().equals(AttributeCharacterType.TYPE))
	{
//	  AttributeCharacterType attributeCharacterType = (AttributeCharacterType)attributeType;		
      mdAttribute = new MdAttributeCharacter();
      MdAttributeCharacter mdAttributeCharacter = (MdAttributeCharacter)mdAttribute;
      mdAttributeCharacter.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
	}
    else if (attributeType.getType().equals(AttributeDateType.TYPE))
    {
//      AttributeDateType attributeDateType = (AttributeDateType)attributeType;
      mdAttribute = new MdAttributeDateTime();
//      MdAttributeDateTime mdAttributeDateTime = (MdAttributeDateTime)mdAttribute;
    }
    else if (attributeType.getType().equals(AttributeIntegerType.TYPE))
    {
//      AttributeIntegerType attributeIntegerType = (AttributeIntegerType)attributeType;
      mdAttribute = new MdAttributeInteger();
//      MdAttributeInteger mdAttributeInteger = (MdAttributeInteger)mdAttribute;
    }
    else if (attributeType.getType().equals(AttributeFloatType.TYPE))
    {
//      AttributeFloatType attributeIntegerType = (AttributeFloatType)attributeType;
      mdAttribute = new MdAttributeFloat();
//      MdAttributeFloat mdAttributeFloat = (MdAttributeFloat)mdAttribute;
    }
    else if (attributeType.getType().equals(AttributeTermType.TYPE))
    {
      mdAttribute = new MdAttributeMultiTerm();
      MdAttributeMultiTerm mdAttributeMultiTerm = (MdAttributeMultiTerm)mdAttribute;
      
      MdBusiness classifierMdBusiness = MdBusiness.getMdBusiness(Classifier.CLASS);
      mdAttributeMultiTerm.setMdBusiness(classifierMdBusiness);
      
//      public static java.lang.String MDBUSINESS = "mdBusiness";
      
      
      // TODO - implement Terms
    }
    else if (attributeType.getType().equals(AttributeBooleanType.TYPE))
    {
//      AttributeBooleanType attributeBooleanType = (AttributeBooleanType)attributeType;
      mdAttribute = new MdAttributeBoolean();
//      MdAttributeBoolean mdAttributeBoolean = (MdAttributeBoolean)mdAttribute;
    }  

	mdAttribute.setAttributeName(attributeType.getName());
	mdAttribute.getDisplayLabel().setValue(attributeType.getLocalizedLabel());
	mdAttribute.getDescription().setValue(attributeType.getLocalizedDescription());
	mdAttribute.setDefiningMdClass(mdBusiness);
	mdAttribute.apply();
	
	
	if (attributeType.getType().equals(AttributeTermType.TYPE))
    {
	  MdAttributeMultiTerm mdAttributeMultiTerm = (MdAttributeMultiTerm)mdAttribute;

//	  org.commongeoregistry.adapter.Term rootTerm = attributeTermType.getRootTerm();
      
      // Build the parent class term root if it does not exist.
      Classifier classTerm = this.buildIfNotExistdMdBusinessClassifier(mdBusiness);
      
      // Create the root term node for this attribute
      Classifier attributeTermRoot = this.buildIfNotExistAttribute(mdBusiness, mdAttributeMultiTerm);
      classTerm.addIsAChild(attributeTermRoot).apply();
      
      // Make this the root term of the multi-attribute
      attributeTermRoot.addClassifierMultiTermAttributeRoots(mdAttributeMultiTerm).apply();
      
	  AttributeTermType attributeTermType = (AttributeTermType)attributeType;
	  
	  Term term = new Term(attributeTermRoot.getKey(), attributeTermRoot.getDisplayLabel().getValue(), "");
	  attributeTermType.setRootTerm(term);
    }
	
	return attributeType;
  }

  
  /**
   * Builds if not exists a {@link Classifier} object as a parent of terms that pertain to
   * the given {@link MdBusiness}.
   * 
   * @param mdBusiness {@link MdBusiness}
   * 
   * @return {@link Classifier} object as a parent of terms that pertain to
   * the given {@link MdBusiness}.
   */
  public Classifier buildIfNotExistdMdBusinessClassifier(MdBusiness mdBusiness)
  {
    String clasTermKey = buildRootForClassTermKey(mdBusiness);
    
    Classifier classTerm = null;
    
    try
    {
      classTerm = Classifier.getByKey(clasTermKey);
    }
    catch (DataNotFoundException e)
    {
      classTerm = new Classifier();
      classTerm.setKeyName(clasTermKey);
      classTerm.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
      // This will set the value of the display label to the locale of the user performing the action.
      classTerm.getDisplayLabel().setValue(mdBusiness.getDisplayLabel().getValue());
      classTerm.getDisplayLabel().setDefaultValue(mdBusiness.getDisplayLabel().getDefaultValue());
      classTerm.apply();
      
      Classifier rootClassTerm = Classifier.getByKey(RegistryConstants.TERM_CLASS);
      rootClassTerm.addIsAChild(classTerm).apply();
    }
      
    return classTerm;
  }
  
  /**
   * Builds if not exists a {@link Classifier} object as a parent of terms of the 
   * given {@link MdAttributeMultiTerm}.
   * 
   * @param mdAttributeMultiTerm {@link MdAttributeMultiTerm}
   * @param mdAttributeMultiTerm {@link MdAttributeMultiTerm}
   * 
   * @return {@link Classifier} object as a parent of terms that pertain to
   * the given {@link MdBusiness}.
   */
  public Classifier buildIfNotExistAttribute(MdBusiness mdBusiness, MdAttributeMultiTerm mdAttributeMultiTerm)
  {	    
    String attributeTermKey = buildtAtttributeTermKey(mdBusiness, mdAttributeMultiTerm);
    
    Classifier attributeTerm = null;
    
    try
    {
      attributeTerm = Classifier.getByKey(attributeTermKey);
    }
    catch (DataNotFoundException e)
    {
      attributeTerm = new Classifier();
      attributeTerm.setKeyName(attributeTermKey);
      attributeTerm.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
      // This will set the value of the display label to the locale of the user performing the action.
      attributeTerm.getDisplayLabel().setValue(mdBusiness.getDisplayLabel().getValue());
      attributeTerm.getDisplayLabel().setDefaultValue(mdBusiness.getDisplayLabel().getDefaultValue());
      attributeTerm.apply();
    }
      
    return attributeTerm;
  }
  
  /**
   * Returns the {@link Classifier} code/key of the root term specifying the values for the
   * given {@link MdBusiness}.
   * 
   * @param mdBusiness
   * 
   * @return {@link Classifier} code/key of the root term specifying the values for the
   * given {@link MdBusiness}.
   */
  public static String buildRootForClassTermKey(MdBusiness mdBusiness)
  {
    return RegistryConstants.TERM_CLASS+"_"+mdBusiness.definesType();
  }
  
  /**
   * Returns the {@link Classifier} code/key of the root term specifying the values for an
   * {@link AttributeTermType}.
   * 
   * @param mdBusiness
   * @param mdAttributeMultiTerm
   * 
   * @return {@link Classifier} code/key of the root term specifying the values for an
   * {@link AttributeTermType}.
   */
  public static String buildtAtttributeTermKey(MdBusiness mdBusiness, MdAttributeMultiTerm mdAttributeMultiTerm)
  {
	String rootClassTermCode = buildRootForClassTermKey(mdBusiness);
	  
    return rootClassTermCode+"_"+mdAttributeMultiTerm.getAttributeName();
  }
  
  /**
   * Creates an {@link MdAttributeConcrete} for the given {@link MdBusiness} from the given {@link AttributeType}
   * 
   * @pre assumes no attribute has been defined on the type with the given name.
   * 
   * @param mdBusiness Type to receive attribute definition
   * @param attributeType newly defined attribute
   * 
   * @return {@link AttributeType}
   */
  public AttributeType updateMdAttributeFromAttributeType(MdBusiness mdBusiness, AttributeType attributeType)
  { 
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = getMdAttribute(mdBusiness, attributeType.getName());
    
    if (mdAttributeConcreteDAOIF != null)
    {
      // Get the type safe version
      MdAttributeConcrete mdAttribute = (MdAttributeConcrete)BusinessFacade.get(mdAttributeConcreteDAOIF);
      mdAttribute.lock();
      
      mdAttribute.setAttributeName(attributeType.getName());
  	  mdAttribute.getDisplayLabel().setValue(attributeType.getLocalizedLabel());
  	  mdAttribute.getDescription().setValue(attributeType.getLocalizedDescription());
  	  mdAttribute.apply();   
  	  
  	  mdAttribute.unlock();
    }
    
    return attributeType;
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
      mdAttributeConcreteDAOIF.getBusinessDAO().delete();
    }
  }
  
  /**
   * Returns the {link MdAttributeConcreteDAOIF} for the given {@link AttributeType} defined on the
   * given {@link MdBusiness} or null no such attribute is defined.
   * 
   * @param mdBusiness
   * @param attributeName
   * @return
   */
  private MdAttributeConcreteDAOIF getMdAttribute(MdBusiness mdBusiness, String attributeName)
  {
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);
	    
	return mdBusinessDAOIF.definesAttribute(attributeName);
  }  
  
}
