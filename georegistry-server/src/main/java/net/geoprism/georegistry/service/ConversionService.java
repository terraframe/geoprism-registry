package net.geoprism.georegistry.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.registry.GeoObjectStatus;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeReferenceInfo;
import com.runwaysdk.dataaccess.MdAttributeBlobDAOIF;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDecDAOIF;
import com.runwaysdk.dataaccess.MdAttributeEncryptionDAOIF;
import com.runwaysdk.dataaccess.MdAttributeEnumerationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeFileDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIndicatorDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIntegerDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeStructDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.AssociationType;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdAttributeUUID;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.RelationshipCache;

public class ConversionService
{
  private RegistryAdapter registry;
  
  protected ConversionService(RegistryAdapter registry)
  {
    this.registry = registry;
  }
  
  protected RegistryAdapter getRegistry()
  {
    return this.registry;
  }
  
  protected void setRegistry(RegistryAdapter registry)
  {
    this.registry = registry;
  }
  
  /**
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  protected MdTermRelationship newHierarchyToMdTermRelationiship(HierarchyType hierarchyType)
  {
    MdBusiness mdBusUniversal = MdBusiness.getMdBusiness(Universal.CLASS);
    
    MdTermRelationship mdTermRelationship = new MdTermRelationship();
    
    mdTermRelationship.setTypeName(hierarchyType.getCode());
    mdTermRelationship.setPackageName(RegistryConstants.HIERARCHY_MDTERMRELATIONSHIP_PACKAGE);
    mdTermRelationship.getDisplayLabel().setValue(hierarchyType.getLocalizedLabel());
    mdTermRelationship.getDescription().setValue(hierarchyType.getLocalizedDescription());
    mdTermRelationship.setIsAbstract(false);
    mdTermRelationship.setGenerateSource(false);
    mdTermRelationship.addCacheAlgorithm(RelationshipCache.CACHE_EVERYTHING);
    mdTermRelationship.addAssociationType(AssociationType.Graph);
    mdTermRelationship.setRemove(true);
    // Create the relationship between different universals.
    mdTermRelationship.setParentMdBusiness(mdBusUniversal);
    mdTermRelationship.setParentCardinality("1");   
    mdTermRelationship.setParentMethod("Parent");
    mdTermRelationship.setChildMdBusiness(mdBusUniversal);
    mdTermRelationship.setChildCardinality("*");
    mdTermRelationship.setChildMethod("Children");
    
    return mdTermRelationship;
  }
  
  /**
   * Turns the given {@link HierarchyType} code into the corresponding {@link MdTermRelationship} key.
   * 
   * @param hierarchyCode {@link HierarchyType} code
   * @return corresponding {@link MdTermRelationship} key.
   */
  public static String buildMdTermRelationshipKey(String hierarchyCode)
  {
    return RegistryConstants.HIERARCHY_MDTERMRELATIONSHIP_PACKAGE+"."+hierarchyCode;
  }
  
  /**
   * Convert the given {@link MdTermRelationShip} key into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey {@link MdTermRelationShip} key 
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKey(String mdTermRelKey)
  {   
    int startIndex = 0;

    if (mdTermRelKey.indexOf(RegistryConstants.HIERARCHY_MDTERMRELATIONSHIP_PACKAGE) >= 0)
    {
      startIndex = RegistryConstants.HIERARCHY_MDTERMRELATIONSHIP_PACKAGE.length()+1;
    }
    else if (mdTermRelKey.indexOf(GISConstants.GEO_PACKAGE) >= 0)
    {
      startIndex = GISConstants.GEO_PACKAGE.length()+1;
    }
    
    String hierarchyKey = mdTermRelKey.substring(startIndex, mdTermRelKey.length());
    
    return hierarchyKey;
  }
  
  /**
   * 
   * @param mdTermRel
   * @return
   */
  public HierarchyType mdTermRelationshipToHierarchyType(MdTermRelationship mdTermRel)
  {
    String hierarchyKey = buildHierarchyKey(mdTermRel.getKey());
    
    HierarchyType ht = new HierarchyType(hierarchyKey, mdTermRel.getDisplayLabel().getValue(), mdTermRel.getDescription().getValue());
    
    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    // Copy all of the children to a list so as not to have recursion with open database cursors.
    List<Universal> childUniversals = new LinkedList<Universal>();
    
    OIterator<? extends Business> i = rootUniversal.getChildren(mdTermRel.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal)u));
    }
    finally
    {
      i.close();
    }


    for (Universal childUniversal : childUniversals)
    {      
      GeoObjectType geoObjectType = this.universalToGeoObjectType(childUniversal);
      
      HierarchyType.HierarchyNode node = new HierarchyType.HierarchyNode(geoObjectType);
      
      node = buildHierarchy(node, childUniversal, mdTermRel);
      
      ht.addRootGeoObjects(node);
    }

    return ht;
  }
  
  private HierarchyType.HierarchyNode buildHierarchy(HierarchyType.HierarchyNode parentNode, Universal parentUniversal, MdTermRelationship mdTermRel)
  {
    List<Universal> childUniversals = new LinkedList<Universal>();
    
    OIterator<? extends Business> i = parentUniversal.getChildren(mdTermRel.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal)u));
    }
    finally
    {
      i.close();
    }
    
    for (Universal childUniversal : childUniversals)
    {      
      GeoObjectType geoObjectType = this.universalToGeoObjectType(childUniversal);
      
      HierarchyType.HierarchyNode node = new HierarchyType.HierarchyNode(geoObjectType);
      
      node = buildHierarchy(node, childUniversal, mdTermRel);
      
      parentNode.addChild(node);
    }
    
    return parentNode;

  }
  
  
  
  protected Universal geoObjectTypeToUniversal(GeoObjectType got)
  {
    Universal uni = Universal.getByKey(got.getCode());
    
    return uni;
  }
  
  
  /** 
   * Creates, but does not persist, a {@link Universal} from the given {@link GeoObjectType}.
   * 
   * @pre needs to occur within a transaction
   * 
   * @param got
   * @return a {@link Universal} from the given {@link GeoObjectType} that is not persisted.
   */
  protected Universal newGeoObjectTypeToUniversal(GeoObjectType got)
  {    
    Universal universal = new Universal();
    universal.setUniversalId(got.getCode());
    universal.setIsLeafType(got.isLeaf());
    universal.getDisplayLabel().setValue(got.getLocalizedLabel());
    universal.getDescription().setValue(got.getLocalizedDescription());
        
    return universal;
  }
  
  /** 
   * Returns a {@link Universal} from the code value on the given {@link GeoObjectType}.
   * 
   * @param got
   * @return a {@link Universal} from the code value on the given {@link GeoObjectType}.
   */
  protected Universal getUniversalFromGeoObjectType(GeoObjectType got)
  {    
    Universal universal = Universal.getByKey(got.getCode());
        
    return universal;
  }
  
  
  public GeoObjectType universalToGeoObjectType(Universal uni)
  {
    com.runwaysdk.system.gis.geo.GeometryType geoPrismgeometryType = uni.getGeometryType().get(0);
    
    org.commongeoregistry.adapter.constants.GeometryType cgrGeometryType = this.convertPolygonType(geoPrismgeometryType);   

    GeoObjectType geoObjType = new GeoObjectType(uni.getUniversalId(), cgrGeometryType, uni.getDisplayLabel().getValue(), uni.getDescription().getValue(), uni.getIsLeafType(), registry);

    geoObjType = convertAttributeTypes(uni, geoObjType);
    
    return geoObjType;
  }
  
  private GeoObjectType convertAttributeTypes(Universal uni, GeoObjectType gt)
  {
    MdBusiness mdBusiness = uni.getMdBusiness();
    
    if (mdBusiness != null)
    {
      MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF)BusinessFacade.getEntityDAO(mdBusiness);
      
      // Standard attributes are defined by default on the GeoObjectType
      
      List<? extends MdAttributeConcreteDAOIF> definedMdAttributeList = mdBusinessDAOIF.getAllDefinedMdAttributes();
      
      for (MdAttributeConcreteDAOIF mdAttribute : definedMdAttributeList)
      {
        if (this.convertMdAttributeToAttributeType(mdAttribute))
        {
          AttributeType attributeType = this.mdAttributeToAttributeType(mdAttribute);
          
          if (attributeType != null)
          {
            gt.addAttribute(attributeType);
          }
        }
      }
    }  
    
    return gt;
  }
  

  /**
   * True if the given {@link MdAttributeConcreteDAOIF} should be converted to an {@link AttributeType}, false otherwise.
   * Standard attributes such as {@link DefaultAttribute} are already defined on a {@link GeoObjectType} and do not 
   * need to be converted. This method also returns true if the attribute is not a system attribue.
   * 
   * @return True if the given {@link MdAttributeConcreteDAOIF} should be converted to an {@link AttributeType}, false otherwise.
   */
  private boolean convertMdAttributeToAttributeType(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() ||
        mdAttribute instanceof MdAttributeStructDAOIF ||
        mdAttribute instanceof MdAttributeEncryptionDAOIF ||
        mdAttribute instanceof MdAttributeIndicatorDAOIF ||
        mdAttribute instanceof MdAttributeBlobDAOIF ||
        mdAttribute instanceof MdAttributeFileDAOIF ||
        mdAttribute instanceof MdAttributeTimeDAOIF ||
        mdAttribute instanceof MdAttributeUUIDDAOIF ||
        mdAttribute.getType().equals(MdAttributeReferenceInfo.CLASS))
    {
      return false;
    }
    else
    {
      return true;
    }
      
  }
  
  // TODO: Complete
  private MdAttributeDAO attributeTypeToMdAttribute(AttributeType attributeType)
  {
    return null;
  }
  
  private AttributeType mdAttributeToAttributeType(MdAttributeConcreteDAOIF mdAttribute)
  {
    Locale locale = Session.getCurrentLocale();
    
    String attributeName = mdAttribute.definesAttribute();
    String displayLabel = mdAttribute.getDisplayLabel(locale);
    String description = mdAttribute.getDescription(locale);
    
    AttributeType testChar = null;
    
    if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      testChar = AttributeType.factory(attributeName, displayLabel, description, AttributeBooleanType.TYPE);
    }
    else if (mdAttribute instanceof MdAttributeCharacterDAOIF)
    {
      testChar = AttributeType.factory(attributeName, displayLabel, description, AttributeCharacterType.TYPE);      
    }
    else if (mdAttribute instanceof MdAttributeDateDAOIF || mdAttribute instanceof MdAttributeDateTimeDAOIF)
    {
      testChar = AttributeType.factory(attributeName, displayLabel, description, AttributeDateType.TYPE);           
    }
    else if (mdAttribute instanceof MdAttributeDecDAOIF)
    {
      testChar = AttributeType.factory(attributeName, displayLabel, description, AttributeFloatType.TYPE);                
    }
    else if (mdAttribute instanceof MdAttributeIntegerDAOIF || mdAttribute instanceof MdAttributeLongDAOIF)
    {
      testChar = AttributeType.factory(attributeName, displayLabel, description, AttributeIntegerType.TYPE);   
    }
    else if (mdAttribute instanceof MdAttributeEnumerationDAOIF || mdAttribute instanceof MdAttributeTermDAOIF)
    {
      // TODO: Set the terms on the AttributeType
      testChar = AttributeType.factory(attributeName, displayLabel, description, AttributeTermType.TYPE);  
    }
    
    return testChar; 
  }
  
  /**
   * Convert Geometry types between GeoPrism and the CGR standard.
   * 
   * @param geoPrismgeometryType
   * @return CGR GeometryType
   */
  private org.commongeoregistry.adapter.constants.GeometryType convertPolygonType(com.runwaysdk.system.gis.geo.GeometryType geoPrismgeometryType)
  {
    if (geoPrismgeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POINT))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.POINT;
    }
    else if (geoPrismgeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.LINE))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.LINE;
    }
    else if (geoPrismgeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POLYGON))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.POLYGON;
    }
    else if (geoPrismgeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTIPOINT;
    }
    else if (geoPrismgeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTILINE))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTILINE;
    }
    else if (geoPrismgeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTIPOLYGON;
    }
    else
    {
      return null;
    }
  }
  
  /**
   * Needs to occur within a transaction!
   * 
   * @param geoObject
   * @return
   */
  public GeoEntity geoObjectToGeoEntity(GeoObject geoObject)
  {
    GeoEntity geo = new GeoEntity();
    
    populateGeoEntity(geoObject, geo);
    
    return geo;
  }
  
  /**
   * Needs to occur within a transaction!
   * 
   * @param geoObject
   * @return
   */
  public void populateGeoEntity(GeoObject geoObject, GeoEntity geo)
  {
    Universal uni = geoObjectTypeToUniversal(geoObject.getType());
    
    // TODO : Set the id ?
    // TODO : Status term
    geo.setUniversal(uni);
    geo.setGeoId(geoObject.getCode());
    geo.setWkt(geoObject.getGeometry().toString());
    geo.getDisplayLabel().setValue(geoObject.getLocalizedDisplayLabel());
  }
  
  public GeoObject geoEntityToGeoObject(GeoEntity geoEntity)
  {
    GeoObjectType got = universalToGeoObjectType(geoEntity.getUniversal());
    
    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(got);
    
    // TODO : GeometryType is hardcoded
    GeoObject geoObj = new GeoObject(got, GeometryType.POLYGON, attributeMap);
    
    geoObj.setUid(geoEntity.getOid());
    geoObj.setCode(geoEntity.getGeoId());
    geoObj.setWKTGeometry(geoEntity.getWkt());
    geoObj.setLocalizedDisplayLabel(geoEntity.getDisplayLabel().getValue());
    
    // TODO : Status term
//    geoObj.setStatus(this.registry.getMetadataCache().getTerm());
    
    // TODO : Type attribute?
    
    return geoObj;
  }
  
  /**
   * Adds default attributes to the given {@link MdBusinessDAO} according to the 
   * Common Geo-Registry specification for {@link GeoObject}.
   * 
   * @param mdBusinessDAO {@link MdBusinessDAO} that will define the default attributes.
   */
  @Transaction
  public void createDefaultAttributes(MdBusiness definingMdBusiness)
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
    
    // DefaultAttribute.UID - Defined on the MdBusiness and the values are from the {@code GeoObject#OID};
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
    
  // DefaultAttribute.TYPE - This is the display label of the Universal. BusObject.mdBusiness.Universal.displayLabel
  
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
  }
}
