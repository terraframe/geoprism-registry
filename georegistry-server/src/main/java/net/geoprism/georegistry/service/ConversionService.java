package net.geoprism.georegistry.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.geoprism.georegistry.AdapterUtilities;
import net.geoprism.georegistry.RegistryConstants;
import net.geoprism.registry.GeoObjectStatus;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
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
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.AssociationType;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.RelationshipCache;

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
  
  public Universal geoObjectTypeToUniversal(GeoObjectType got)
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

  public Term geoObjectStatusToTerm(GeoObjectStatus gos)
  {
    if (gos.getEnumName().equals(GeoObjectStatus.ACTIVE.getEnumName()))
    {
      return adapter.getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.ACTIVE.code).get();
    }
    else if (gos.getEnumName().equals(GeoObjectStatus.INACTIVE.getEnumName()))
    {
      return adapter.getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.INACTIVE.code).get();
    }
    else if (gos.getEnumName().equals(GeoObjectStatus.NEW.getEnumName()))
    {
      return adapter.getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get();
    }
    else if (gos.getEnumName().equals(GeoObjectStatus.PENDING.getEnumName()))
    {
      return adapter.getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.PENDING.code).get();
    }
    else
    {
      throw new ProgrammingErrorException("Unknown Status [" + gos.getEnumName() + "].");
    }
  }
}
