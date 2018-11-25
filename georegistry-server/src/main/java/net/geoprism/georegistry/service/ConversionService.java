package net.geoprism.georegistry.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.geoprism.georegistry.AdapterUtilities;
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
import com.runwaysdk.dataaccess.BusinessDAO;
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
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.mapping.GeoserverFacade;
import com.runwaysdk.system.gis.metadata.MdAttributeGeometry;
import com.runwaysdk.system.gis.metadata.MdAttributeLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPoint;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPolygon;
import com.runwaysdk.system.gis.metadata.MdAttributePoint;
import com.runwaysdk.system.gis.metadata.MdAttributePolygon;
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
import com.runwaysdk.system.ontology.ImmutableRootException;
import com.runwaysdk.util.IDGenerator;

public class ConversionService
{
  private RegistryAdapter adapter = null;
  
  private static ConversionService instance = null;
  
  private AdapterUtilities util = null;
  
  public ConversionService()
  {
  }
  
  public synchronized static ConversionService getInstance()
  {
    if (instance == null)
    {
      instance = new ConversionService();
    }
    
    return instance;
  }
  
  public RegistryAdapter getAdapter()
  {
    return this.adapter;
  }
  
  public void setAdapter(RegistryAdapter adapter)
  {
    this.adapter = adapter;
  }
  
  public AdapterUtilities getUtil()
  {
    return util;
  }

  public void setUtil(AdapterUtilities util)
  {
    this.util = util;
  }

  /**
   * Turns the given {@link HierarchyType} code into the corresponding {@link MdTermRelationship} key for
   * the {@link Universal} relationship.
   * 
   * @param hierarchyCode {@link HierarchyType} code
   * @return corresponding {@link MdTermRelationship} key.
   */
  public static String buildMdTermRelUniversalKey(String hierarchyCode)
  {    
    // If the code is for the LocatedIn hierarchy, then the relationship that defines the 
    // Universals for that relationship is AllowedIn.
    if (hierarchyCode.trim().equals(LocatedIn.class.getSimpleName()))
    {
      return AllowedIn.CLASS;
    }    
    else
    {
      return GISConstants.GEO_PACKAGE+"."+hierarchyCode+RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    }
  }
  
  /**
   * Convert the given {@link MdTermRelationShip} key for {@link Universal}s into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey {@link MdTermRelationShip} key 
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKeyFromMdTermRelUniversal(String mdTermRelKey)
  {   
    // the hierarchyType code for the allowed in relationship is the located in relationship
    if (mdTermRelKey.trim().equals(AllowedIn.CLASS))
    {
      return LocatedIn.class.getSimpleName();
    }
    else
    {   
      int startIndex = GISConstants.GEO_PACKAGE.length()+1;

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
  
  /**
   * Turns the given {@link MdTermRelationShip} key for a {@link Universal} into the corresponding 
   * {@link MdTermRelationship} key for the {@link GeoEntity} relationship.
   * 
   * @param hierarchyCode {@link HierarchyType} code
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
      return GISConstants.GEO_PACKAGE+"."+hierarchyCode;
    }
  }
  
  /**
   * Convert the given {@link MdTermRelationShip} key for a {@link GeoEntities} into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey {@link MdTermRelationShip} key 
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKeyFromMdTermRelGeoEntity(String mdTermRelKey)
  {   
    int startIndex = GISConstants.GEO_PACKAGE.length()+1;

    return mdTermRelKey.substring(startIndex, mdTermRelKey.length());
  }
  
  /**
   * It creates an {@link MdTermRelationship} to model the relationship between {@link Universal}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  protected MdTermRelationship newHierarchyToMdTermRelForUniversals(HierarchyType hierarchyType)
  {
    MdBusiness mdBusUniversal = MdBusiness.getMdBusiness(Universal.CLASS);
    
    MdTermRelationship mdTermRelationship = new MdTermRelationship();
    
    mdTermRelationship.setTypeName(hierarchyType.getCode()+RegistryConstants.UNIVERSAL_RELATIONSHIP_POST);
    mdTermRelationship.setPackageName(GISConstants.GEO_PACKAGE);
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
    mdTermRelationship.setChildMdBusiness(mdBusUniversal);
    mdTermRelationship.setChildCardinality("*");
    mdTermRelationship.setParentMethod("Parent");
    mdTermRelationship.setChildMethod("Children");
    
    return mdTermRelationship;
  }
  
  /**
   * It creates an {@link MdTermRelationship} to model the relationship between {@link GeoEntity}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  protected MdTermRelationship newHierarchyToMdTermRelForGeoEntities(HierarchyType hierarchyType)
  {
    MdBusiness mdBusGeoEntity = MdBusiness.getMdBusiness(GeoEntity.CLASS);
    
    MdTermRelationship mdTermRelationship = new MdTermRelationship();
    
    mdTermRelationship.setTypeName(hierarchyType.getCode());
    mdTermRelationship.setPackageName(GISConstants.GEO_PACKAGE);
    mdTermRelationship.getDisplayLabel().setValue(hierarchyType.getLocalizedLabel());
    mdTermRelationship.getDescription().setValue(hierarchyType.getLocalizedDescription());
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
  
  /**
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  protected MdTermRelationship existingHierarchyToMdTermRelationiship(HierarchyType hierarchyType)
  {
    String mdTermRelKey = buildMdTermRelUniversalKey(hierarchyType.getCode());
  
    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey);
    
    return mdTermRelationship;
  }
  
  
  /**
   * 
   * @param mdTermRel
   * @return
   */
  public HierarchyType mdTermRelationshipToHierarchyType(MdTermRelationship mdTermRel)
  {
    String hierarchyKey = buildHierarchyKeyFromMdTermRelUniversal(mdTermRel.getKey());
    
    String displayLabel;
    String description;
    
    if (mdTermRel.definesType().equals(AllowedIn.CLASS))
    {
      MdTermRelationship locatedInMdTermRel = (MdTermRelationship)MdTermRelationship.getMdRelationship(LocatedIn.CLASS);
      displayLabel = locatedInMdTermRel.getDisplayLabel().getValue();
      description = locatedInMdTermRel.getDescription().getValue();
    }
    else
    {
      displayLabel = mdTermRel.getDisplayLabel().getValue();
      description = mdTermRel.getDescription().getValue();
    }
    
    
    HierarchyType ht = new HierarchyType(hierarchyKey, displayLabel, description);
    
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
  public Universal newGeoObjectTypeToUniversal(GeoObjectType got)
  {    
    Universal universal = new Universal();
    universal.setUniversalId(got.getCode());
    universal.setIsLeafType(got.isLeaf());
    universal.getDisplayLabel().setValue(got.getLocalizedLabel());
    universal.getDescription().setValue(got.getLocalizedDescription());
    
    com.runwaysdk.system.gis.geo.GeometryType geometryType = convertAdapterToRegistryPolygonType(got.getGeometryType());

    // Clear the default value
    universal.clearGeometryType();
    universal.addGeometryType(geometryType);

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
    
    org.commongeoregistry.adapter.constants.GeometryType cgrGeometryType = this.convertRegistryToAdapterPolygonType(geoPrismgeometryType);   

    GeoObjectType geoObjType = new GeoObjectType(uni.getUniversalId(), cgrGeometryType, uni.getDisplayLabel().getValue(), uni.getDescription().getValue(), uni.getIsLeafType(), adapter);

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
   * need to be converted. This method also returns true if the attribute is not a system attribute.
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
  private org.commongeoregistry.adapter.constants.GeometryType convertRegistryToAdapterPolygonType(com.runwaysdk.system.gis.geo.GeometryType geoPrismGeometryType)
  {
    if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POINT))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.POINT;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.LINE))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.LINE;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.POLYGON))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.POLYGON;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTIPOINT;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTILINE))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTILINE;
    }
    else if (geoPrismGeometryType.equals(com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON))
    {
      return org.commongeoregistry.adapter.constants.GeometryType.MULTIPOLYGON;
    }
    else
    {
      return null;
    }
  }
  
  /**
   * Convert Geometry types between the CGR standard by GeoPrism.
   * 
   * @param geoPrismgeometryType
   * @return CGR GeometryType
   */
  private com.runwaysdk.system.gis.geo.GeometryType convertAdapterToRegistryPolygonType(org.commongeoregistry.adapter.constants.GeometryType adapterGeometryType)
  {
    if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.POINT))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.POINT;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.LINE))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.LINE;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.POLYGON))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.POLYGON;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.MULTIPOINT))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.MULTIPOINT;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.MULTILINE))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.MULTILINE;
    }
    else if (adapterGeometryType.equals(org.commongeoregistry.adapter.constants.GeometryType.MULTIPOLYGON))
    {
      return com.runwaysdk.system.gis.geo.GeometryType.MULTIPOLYGON;
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
    
    geoObj.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(geoEntity.getOid(), geoEntity.getUniversal()));
    geoObj.setCode(geoEntity.getGeoId());
    geoObj.setWKTGeometry(geoEntity.getWkt());
    geoObj.setLocalizedDisplayLabel(geoEntity.getDisplayLabel().getValue());
    
    // TODO : Status term
//    geoObj.setStatus(this.registry.getMetadataCache().getTerm());
    
    // TODO : Type attribute?
    
    return geoObj;
  }
  
  /** 
   * Creates an {@link MdBusiness} for the given universal.
   * 
   * @pre universal.gertMdBusiness() == null
   * 
   * @param universal
   * 
   * @return modified {@link Universal} that references an applied {@link MdBusiness} .
   */
  @Transaction
  public static Universal createMdBusinessForUniversal(Universal universal)
  { 
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
    createDefaultAttributes(universal, mdBusiness);
    
    universal.setMdBusiness(mdBusiness);
    
    try
    {
      universal.apply();
    }
    // remove once ROOT attribute is defined.
    catch (ImmutableRootException e) 
    {
      System.out.println(universal.getUniversalId()+" - "+universal.getOid()+"    "+mdBusiness.getOid());
    }
      
    return universal;
  }
  
  /**
   * Adds default attributes to the given {@link MdBusinessDAO} according to the 
   * Common Geo-Registry specification for {@link GeoObject}.
   * 
   * @param mdBusinessDAO {@link MdBusinessDAO} that will define the default attributes.
   */
  @Transaction
  public static void createDefaultAttributes(Universal universal, MdBusiness definingMdBusiness)
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
  
  /**
   * Creates a reference attribute to the parent node class.
   * 
   * 
   * @param hierarchyTypeCode
   * @param parentUniversal
   * @param childUniversal
   */
  @Transaction
  public static void addParentReferenceToLeafType(String hierarchyTypeCode, Universal parentUniversal, Universal childUniversal)
  {
    String mdTermRelKey = buildMdTermRelGeoEntityKey(hierarchyTypeCode);
    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey); 

    MdBusiness parentMdBusiness = parentUniversal.getMdBusiness();
    MdBusiness childMdBusiness = childUniversal.getMdBusiness();
    
    String refAttrName = getParentReferenceAttributeName(hierarchyTypeCode, parentUniversal);
    
    String displayLabel = "Reference to "+parentUniversal.getDisplayLabel().getValue()+" in hierarchy "+mdTermRelationship.getDisplayLabel().getValue();

    MdAttributeReference mdAttributeReference = new MdAttributeReference();
    mdAttributeReference.setAttributeName(refAttrName);
    mdAttributeReference.getDisplayLabel().setValue(displayLabel);
    mdAttributeReference.getDescription().setValue(displayLabel);
    mdAttributeReference.setRequired(false);
    mdAttributeReference.setDefiningMdClass(childMdBusiness);
    mdAttributeReference.setMdBusiness(parentMdBusiness);
    mdAttributeReference.addIndexType(MdAttributeIndices.NON_UNIQUE_INDEX);
    mdAttributeReference.apply();

  }
  
  /**
   * Creates a reference attribute name for a child leaf type that references the parent type
   * 
   * @param hierarchyTypeCode
   * @param parentUniversal
   * @return
   */
  public static String getParentReferenceAttributeName(String hierarchyTypeCode, Universal parentUniversal)
  {
    MdBusiness parentMdBusiness = parentUniversal.getMdBusiness();
    String parentTypeName = parentMdBusiness.getTypeName();
    
    // Lower case the first character of the hierarchy Code
    String lowerCaseHierarchyName = Character.toLowerCase(hierarchyTypeCode.charAt(0)) + hierarchyTypeCode.substring(1);
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
    
    String refAttrName = lowerCaseHierarchyName+upperCaseParentClassName;
    
    return refAttrName;
  }
  
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
    businessDAO.addItem(DefaultAttribute.STATUS.getName(), GeoObjectStatus.ACTIVE.getOid());
    businessDAO.apply();
  }

}
