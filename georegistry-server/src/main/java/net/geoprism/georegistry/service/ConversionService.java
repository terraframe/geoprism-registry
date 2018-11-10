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
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdAttributeEnumeration;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdEnumeration;
import com.runwaysdk.system.metadata.MdTermRelationship;

public class ConversionService
{
  private RegistryAdapter registry;
  
  public ConversionService(RegistryAdapter registry)
  {
    this.registry = registry;
  }
  
  public RegistryAdapter getRegistry()
  {
    return this.registry;
  }
  
  public void setRegistry(RegistryAdapter registry)
  {
    this.registry = registry;
  }
  
  public HierarchyType mdTermRelationshipToHierarchyType(MdTermRelationship mdTermRel)
  {
    HierarchyType ht = new HierarchyType(mdTermRel.getKey(), mdTermRel.getDisplayLabel().getValue(), mdTermRel.getDescription().getValue());
    
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
        
    return universal;
  }
  
  /** 
   * Updates, but does not persist, a {@link Universal} from the given {@link GeoObjectType}.
   * 
   * @param got
   * @return a {@link Universal} from the given {@link GeoObjectType} that is updated but not persisted.
   */
  public Universal existingGeoObjectTypeToUniversal(GeoObjectType got)
  {    
    Universal universal = Universal.getByKey(got.getCode());
    universal.getDisplayLabel().setValue(got.getLocalizedLabel());
    universal.getDescription().setValue(got.getLocalizedDescription());
        
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
  
  public GeoEntity geoObjectToGeoEntity(GeoObject geoObject)
  {
    GeoEntity geo = new GeoEntity();
    
    populateGeoEntity(geoObject, geo);
    
    return geo;
  }
  
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
    
    MdAttributeReference mdAttrRef = new MdAttributeReference();
    mdAttrRef.setAttributeName(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
    mdAttrRef.setMdBusiness(mdBusGeoEntity);
    mdAttrRef.setDefiningMdClass(definingMdBusiness);
    mdAttrRef.apply();
    
  // DefaultAttribute.CODE - defined by GeoEntity geoId
  
  // DefaultAttribute.TYPE - This is the type field on the business object associated with the GeoObject
  
  // DefaultAttribute.CREATED_DATE - The create data on the GeoObject?
  
  // DefaultAttribute.UPDATED_DATE - The update data on the GeoObject?
  
  // DefaultAttribute.STATUS 
    
    MdEnumeration geoObjectStatusEnum = MdEnumeration.getMdEnumeration(GeoObjectStatus.CLASS);
    
    MdAttributeEnumeration mdAttrEnum = new MdAttributeEnumeration();
    mdAttrEnum.setAttributeName(RegistryConstants.OBJECT_STATUS);
    mdAttrEnum.setMdEnumeration(geoObjectStatusEnum);
    mdAttrEnum.setSelectMultiple(false);
    mdAttrEnum.setDefiningMdClass(definingMdBusiness);
    mdAttrEnum.apply();
  }
}
