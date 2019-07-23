package net.geoprism.registry.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
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
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessEnumeration;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.LocalStruct;
import com.runwaysdk.business.ontology.InitializationStrategyIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdAttributeReferenceInfo;
import com.runwaysdk.constants.MdBusinessInfo;
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
import com.runwaysdk.dataaccess.MdAttributeLocalDAOIF;
import com.runwaysdk.dataaccess.MdAttributeLongDAOIF;
import com.runwaysdk.dataaccess.MdAttributeStructDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTimeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.RelationshipDAOIF;
import com.runwaysdk.dataaccess.attributes.entity.AttributeLocal;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.gis.dataaccess.MdAttributeGeometryDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.LocatedIn;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.AssociationType;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeReference;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.RelationshipCache;
import com.runwaysdk.util.IDGenerator;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.DefaultConfiguration;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.InvalidMasterListCodeException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.TermBuilder;
import net.geoprism.registry.io.TermValueException;

public class ConversionService
{
  public ConversionService()
  {
  }

  public static ConversionService getInstance()
  {
    return ServiceFactory.getConversionService();
  }

  /**
   * Turns the given {@link HierarchyType} code into the corresponding
   * {@link MdTermRelationship} key for the {@link Universal} relationship.
   * 
   * @param hierarchyCode
   *          {@link HierarchyType} code
   * @return corresponding {@link MdTermRelationship} key.
   */
  public static String buildMdTermRelUniversalKey(String hierarchyCode)
  {
    // If the code is for the LocatedIn hierarchy, then the relationship that
    // defines the
    // Universals for that relationship is AllowedIn.
    if (hierarchyCode.trim().equals(LocatedIn.class.getSimpleName()))
    {
      return AllowedIn.CLASS;
    }
    else
    {
      return GISConstants.GEO_PACKAGE + "." + hierarchyCode + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST;
    }
  }

  /**
   * Convert the given {@link MdTermRelationShip} key for {@link Universal}s
   * into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey
   *          {@link MdTermRelationShip} key
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKeyFromMdTermRelUniversal(String mdTermRelKey)
  {
    // the hierarchyType code for the allowed in relationship is the located in
    // relationship
    if (mdTermRelKey.trim().equals(AllowedIn.CLASS))
    {
      return LocatedIn.class.getSimpleName();
    }
    else
    {
      int startIndex = GISConstants.GEO_PACKAGE.length() + 1;

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
   * Turns the given {@link MdTermRelationShip} key for a {@link Universal} into
   * the corresponding {@link MdTermRelationship} key for the {@link GeoEntity}
   * relationship.
   * 
   * @param hierarchyCode
   *          {@link HierarchyType} code
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
      return GISConstants.GEO_PACKAGE + "." + hierarchyCode;
    }
  }

  /**
   * Convert the given {@link MdTermRelationShip} key for a {@link GeoEntities}
   * into a {@link HierarchyType} key.
   * 
   * @param mdTermRelKey
   *          {@link MdTermRelationShip} key
   * @return a {@link HierarchyType} key.
   */
  public static String buildHierarchyKeyFromMdTermRelGeoEntity(String mdTermRelKey)
  {
    int startIndex = GISConstants.GEO_PACKAGE.length() + 1;

    return mdTermRelKey.substring(startIndex, mdTermRelKey.length());
  }

  /**
   * It creates an {@link MdTermRelationship} to model the relationship between
   * {@link Universal}s.
   * 
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdTermRelationship newHierarchyToMdTermRelForUniversals(HierarchyType hierarchyType)
  {
    if (!MasterList.isValidName(hierarchyType.getCode()))
    {
      throw new InvalidMasterListCodeException("The hierarchy type code has an invalid character");
    }

    
    MdBusiness mdBusUniversal = MdBusiness.getMdBusiness(Universal.CLASS);

    MdTermRelationship mdTermRelationship = new MdTermRelationship();

    mdTermRelationship.setTypeName(hierarchyType.getCode() + RegistryConstants.UNIVERSAL_RELATIONSHIP_POST);
    mdTermRelationship.setPackageName(GISConstants.GEO_PACKAGE);
    this.populate(mdTermRelationship.getDisplayLabel(), hierarchyType.getLabel());
    this.populate(mdTermRelationship.getDescription(), hierarchyType.getDescription());
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

  @Transaction
  public HierarchyType createHierarchyType(HierarchyType hierarchyType)
  {
    // /*
    // * Create a Registry Maintainer role for the new hierarchy
    // */
    // RoleDAO role = RoleDAO.newInstance();
    // role.setValue(RoleDAOIF.ROLENAME,
    // RegistryConstants.REGISTRY_MAINTAINER_PREFIX + hierarchyType.getCode());
    // role.setStructValue(RoleDAOIF.DISPLAY_LABEL,
    // MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyType.getLabel() +
    // " Registry Maintainer");
    // role.apply();
    //
    // /*
    // * Assign the new role has a child role of the generic registry maintainer
    // * role
    // */
    // role.addAscendant(RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE));

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    InitializationStrategyIF strategy = new InitializationStrategyIF()
    {
      @Override
      public void preApply(MdBusinessDAO mdBusiness)
      {
        mdBusiness.setValue(MdBusinessInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
      }

      @Override
      public void postApply(MdBusinessDAO mdBusiness)
      {
        RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();

        adminRole.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
        adminRole.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
        adminRole.grantPermission(Operation.CREATE, mdBusiness.getOid());
        adminRole.grantPermission(Operation.DELETE, mdBusiness.getOid());

        maintainer.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
        maintainer.grantPermission(Operation.WRITE_ALL, mdBusiness.getOid());
        maintainer.grantPermission(Operation.CREATE, mdBusiness.getOid());
        maintainer.grantPermission(Operation.DELETE, mdBusiness.getOid());

        consumer.grantPermission(Operation.READ, mdBusiness.getOid());
        consumer.grantPermission(Operation.READ_ALL, mdBusiness.getOid());

        contributor.grantPermission(Operation.READ, mdBusiness.getOid());
        contributor.grantPermission(Operation.READ_ALL, mdBusiness.getOid());
      }
    };

    MdTermRelationship mdTermRelUniversal = ServiceFactory.getConversionService().newHierarchyToMdTermRelForUniversals(hierarchyType);
    mdTermRelUniversal.apply();

    this.grantWritePermissionsOnMdTermRel(mdTermRelUniversal);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelUniversal);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelUniversal);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelUniversal);

    Universal.getStrategy().initialize(mdTermRelUniversal.definesType(), strategy);

    MdTermRelationship mdTermRelGeoEntity = ServiceFactory.getConversionService().newHierarchyToMdTermRelForGeoEntities(hierarchyType);
    mdTermRelGeoEntity.apply();

    this.grantWritePermissionsOnMdTermRel(mdTermRelGeoEntity);
    this.grantWritePermissionsOnMdTermRel(maintainer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(consumer, mdTermRelGeoEntity);
    this.grantReadPermissionsOnMdTermRel(contributor, mdTermRelGeoEntity);

    GeoEntity.getStrategy().initialize(mdTermRelGeoEntity.definesType(), strategy);

    return ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelUniversal);
  }

  private void grantWritePermissionsOnMdTermRel(MdTermRelationship mdTermRelationship)
  {
    RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();

    grantWritePermissionsOnMdTermRel(adminRole, mdTermRelationship);
  }

  public void grantWritePermissionsOnMdTermRel(RoleDAO role, MdTermRelationship mdTermRelationship)
  {
    role.grantPermission(Operation.ADD_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.ADD_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_PARENT, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_CHILD, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_ALL, mdTermRelationship.getOid());
    role.grantPermission(Operation.WRITE_ALL, mdTermRelationship.getOid());
    role.grantPermission(Operation.CREATE, mdTermRelationship.getOid());
    role.grantPermission(Operation.DELETE, mdTermRelationship.getOid());
  }

  public void grantReadPermissionsOnMdTermRel(RoleDAO role, MdTermRelationship mdTermRelationship)
  {
    role.grantPermission(Operation.READ, mdTermRelationship.getOid());
    role.grantPermission(Operation.READ_ALL, mdTermRelationship.getOid());
  }

  /**
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdTermRelationship existingHierarchyToUniversalMdTermRelationiship(HierarchyType hierarchyType)
  {
    String mdTermRelKey = buildMdTermRelUniversalKey(hierarchyType.getCode());

    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey);

    return mdTermRelationship;
  }

  /**
   * Needs to occur in a transaction.
   * 
   * @param hierarchyType
   * @return
   */
  public MdTermRelationship existingHierarchyToGeoEntityMdTermRelationiship(HierarchyType hierarchyType)
  {
    String mdTermRelKey = buildMdTermRelGeoEntityKey(hierarchyType.getCode());

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

    LocalizedValue displayLabel;
    LocalizedValue description;

    if (mdTermRel.definesType().equals(AllowedIn.CLASS))
    {
      MdTermRelationship locatedInMdTermRel = (MdTermRelationship) MdTermRelationship.getMdRelationship(LocatedIn.CLASS);
      displayLabel = this.convert(locatedInMdTermRel.getDisplayLabel());
      description = this.convert(locatedInMdTermRel.getDescription());
    }
    else
    {
      displayLabel = this.convert(mdTermRel.getDisplayLabel());
      description = this.convert(mdTermRel.getDescription());
    }

    HierarchyType ht = new HierarchyType(hierarchyKey, displayLabel, description);

    Universal rootUniversal = Universal.getByKey(Universal.ROOT);

    // Copy all of the children to a list so as not to have recursion with open
    // database cursors.
    List<Universal> childUniversals = new LinkedList<Universal>();

    OIterator<? extends Business> i = rootUniversal.getChildren(mdTermRel.definesType());
    try
    {
      i.forEach(u -> childUniversals.add((Universal) u));
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
      i.forEach(u -> childUniversals.add((Universal) u));
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
   * Creates, but does not persist, a {@link Universal} from the given
   * {@link GeoObjectType}.
   * 
   * @pre needs to occur within a transaction
   * 
   * @param got
   * @return a {@link Universal} from the given {@link GeoObjectType} that is
   *         not persisted.
   */
  public Universal newGeoObjectTypeToUniversal(GeoObjectType got)
  {
    Universal universal = new Universal();
    universal.setUniversalId(got.getCode());
    universal.setIsLeafType(got.isLeaf());
    universal.setIsGeometryEditable(got.isGeometryEditable());
    this.populate(universal.getDisplayLabel(), got.getLabel());
    this.populate(universal.getDescription(), got.getDescription());

    com.runwaysdk.system.gis.geo.GeometryType geometryType = convertAdapterToRegistryPolygonType(got.getGeometryType());

    // Clear the default value
    universal.clearGeometryType();
    universal.addGeometryType(geometryType);

    return universal;
  }

  /**
   * Returns a {@link Universal} from the code value on the given
   * {@link GeoObjectType}.
   * 
   * @param got
   * @return a {@link Universal} from the code value on the given
   *         {@link GeoObjectType}.
   */
  public Universal getUniversalFromGeoObjectType(GeoObjectType got)
  {
    Universal universal = Universal.getByKey(got.getCode());

    return universal;
  }

  public GeoObjectType universalToGeoObjectType(Universal uni)
  {
    com.runwaysdk.system.gis.geo.GeometryType geoPrismgeometryType = uni.getGeometryType().get(0);

    org.commongeoregistry.adapter.constants.GeometryType cgrGeometryType = this.convertRegistryToAdapterPolygonType(geoPrismgeometryType);

    LocalizedValue label = this.convert(uni.getDisplayLabel());
    LocalizedValue description = this.convert(uni.getDescription());
    GeoObjectType geoObjType = new GeoObjectType(uni.getUniversalId(), cgrGeometryType, label, description, uni.getIsLeafType(), uni.getIsGeometryEditable(), ServiceFactory.getAdapter());

    geoObjType = convertAttributeTypes(uni, geoObjType);

    return geoObjType;
  }

  public LocalizedValue convert(LocalStruct localStruct)
  {
    LocalizedValue label = new LocalizedValue(localStruct.getValue());
    label.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, localStruct.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      label.setValue(locale, localStruct.getValue(locale));
    }

    return label;
  }

  public LocalizedValue convert(String value, Map<String, String> map)
  {
    LocalizedValue localizedValue = new LocalizedValue(value);
    localizedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, map.get(MdAttributeLocalInfo.DEFAULT_LOCALE));

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    for (Locale locale : locales)
    {
      localizedValue.setValue(locale, map.get(locale.toString()));
    }

    return localizedValue;
  }

  private GeoObjectType convertAttributeTypes(Universal uni, GeoObjectType gt)
  {
    MdBusiness mdBusiness = uni.getMdBusiness();

    if (mdBusiness != null)
    {
      MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) BusinessFacade.getEntityDAO(mdBusiness);

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
   * True if the given {@link MdAttributeConcreteDAOIF} should be converted to
   * an {@link AttributeType}, false otherwise. Standard attributes such as
   * {@link DefaultAttribute} are already defined on a {@link GeoObjectType} and
   * do not need to be converted. This method also returns true if the attribute
   * is not a system attribute.
   * 
   * @return True if the given {@link MdAttributeConcreteDAOIF} should be
   *         converted to an {@link AttributeType}, false otherwise.
   */
  private boolean convertMdAttributeToAttributeType(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() || mdAttribute instanceof MdAttributeStructDAOIF || mdAttribute instanceof MdAttributeEncryptionDAOIF || mdAttribute instanceof MdAttributeIndicatorDAOIF || mdAttribute instanceof MdAttributeBlobDAOIF || mdAttribute instanceof MdAttributeGeometryDAOIF || mdAttribute instanceof MdAttributeFileDAOIF || mdAttribute instanceof MdAttributeTimeDAOIF || mdAttribute instanceof MdAttributeUUIDDAOIF || mdAttribute.getType().equals(MdAttributeReferenceInfo.CLASS))
    {
      return false;
    }
    else if (mdAttribute.definesAttribute().equals(ComponentInfo.KEY) || mdAttribute.definesAttribute().equals(ComponentInfo.TYPE))
    {
      return false;
    }
    else
    {
      return true;
    }

  }

  public AttributeType mdAttributeToAttributeType(MdAttributeConcreteDAOIF mdAttribute)
  {
    Locale locale = Session.getCurrentLocale();

    String attributeName = mdAttribute.definesAttribute();
    LocalizedValue displayLabel = this.convert(mdAttribute.getDisplayLabel(locale), mdAttribute.getDisplayLabels());
    LocalizedValue description = this.convert(mdAttribute.getDescription(locale), mdAttribute.getDescriptions());
    boolean required = mdAttribute.isRequired();
    boolean unique = mdAttribute.isUnique();

    if (mdAttribute instanceof MdAttributeBooleanDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeBooleanType.TYPE, required, unique);
    }
    else if (mdAttribute instanceof MdAttributeLocalDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeLocalType.TYPE, required, unique);
    }
    else if (mdAttribute instanceof MdAttributeCharacterDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeCharacterType.TYPE, required, unique);
    }
    else if (mdAttribute instanceof MdAttributeDateDAOIF || mdAttribute instanceof MdAttributeDateTimeDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeDateType.TYPE, required, unique);
    }
    else if (mdAttribute instanceof MdAttributeDecDAOIF)
    {
      MdAttributeDecDAOIF mdAttributeDec = (MdAttributeDecDAOIF) mdAttribute;

      AttributeFloatType attributeType = (AttributeFloatType) AttributeType.factory(attributeName, displayLabel, description, AttributeFloatType.TYPE, required, unique);
      attributeType.setPrecision(Integer.parseInt(mdAttributeDec.getLength()));
      attributeType.setScale(Integer.parseInt(mdAttributeDec.getDecimal()));

      return attributeType;
    }
    else if (mdAttribute instanceof MdAttributeIntegerDAOIF || mdAttribute instanceof MdAttributeLongDAOIF)
    {
      return AttributeType.factory(attributeName, displayLabel, description, AttributeIntegerType.TYPE, required, unique);
    }
    else if (mdAttribute instanceof MdAttributeEnumerationDAOIF || mdAttribute instanceof MdAttributeTermDAOIF)
    {
      AttributeTermType attributeType = (AttributeTermType) AttributeType.factory(attributeName, displayLabel, description, AttributeTermType.TYPE, required, unique);

      if (mdAttribute instanceof MdAttributeEnumerationDAOIF && mdAttribute.definesAttribute().equals(DefaultAttribute.STATUS.getName()))
      {
        Term rootStatusTerm = ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.ROOT.code).get();

        attributeType.setRootTerm(rootStatusTerm);
      }
      else if (mdAttribute instanceof MdAttributeTermDAOIF)
      {
        List<RelationshipDAOIF> rels = ( (MdAttributeTermDAOIF) mdAttribute ).getAllAttributeRoots();

        if (rels.size() > 0)
        {
          RelationshipDAOIF rel = rels.get(0);

          BusinessDAO classy = (BusinessDAO) rel.getChild();

          TermBuilder termBuilder = new TermBuilder(classy.getKey());
          Term adapterTerm = termBuilder.build();

          attributeType.setRootTerm(adapterTerm);
        }
        else
        {
          throw new ProgrammingErrorException("Expected an attribute root on MdAttribute [" + mdAttribute.getKey() + "].");
        }
      }
      else
      {
        throw new ProgrammingErrorException("Enum attributes are not supported at this time.");
      }

      return attributeType;
    }

    throw new UnsupportedOperationException("Unsupported attribute type [" + mdAttribute.getClass().getSimpleName() + "]");
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

  public GeoObject geoEntityToGeoObject(GeoEntity geoEntity)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    GeoObjectType got = universalToGeoObjectType(geoEntity.getUniversal());

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(got);

    GeoObject geoObj = new GeoObject(got, got.getGeometryType(), attributeMap);

    if (geoEntity.isNew())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      geoObj.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(geoEntity.getOid(), geoEntity.getUniversal()));

      Business biz = ServiceFactory.getUtilities().getGeoEntityBusiness(geoEntity);

      Map<String, AttributeType> attributes = got.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          BusinessEnumeration busEnum = biz.getEnumValues(attributeName).get(0);
          GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          Term statusTerm = this.geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (biz.hasAttribute(attributeName))
        {
          String value = biz.getValue(attributeName);

          if (value != null && value.length() > 0)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get(value);

              try
              {
                geoObj.setValue(attributeName, classifier.getClassifierId());
              }
              catch (UnknownTermException e)
              {
                TermValueException ex = new TermValueException();
                ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
                ex.setCode(e.getCode());

                throw e;
              }
            }
            else if (attribute instanceof AttributeDateType)
            {
              try
              {
                geoObj.setValue(attributeName, format.parse(value));
              }
              catch (ParseException e)
              {
                throw new RuntimeException(e);
              }
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              geoObj.setValue(attributeName, new Boolean(value));
            }
            else if (attribute instanceof AttributeFloatType)
            {
              geoObj.setValue(attributeName, new Double(value));
            }
            else if (attribute instanceof AttributeIntegerType)
            {
              geoObj.setValue(attributeName, new Long(value));
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setCode(geoEntity.getGeoId());
    geoObj.getDisplayLabel().setValue(geoEntity.getDisplayLabel().getValue());
    geoObj.setGeometry(this.getGeometry(geoEntity, got.getGeometryType()));

    return geoObj;
  }

  public GeoObject leafToGeoObject(GeoObjectType got, Business business)
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Universal universal = this.getUniversalFromGeoObjectType(got);

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(got);

    GeoObject geoObj = new GeoObject(got, got.getGeometryType(), attributeMap);

    if (business.isNew())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      geoObj.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(business.getOid(), universal));

      Map<String, AttributeType> attributes = got.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          BusinessEnumeration busEnum = business.getEnumValues(attributeName).get(0);
          GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          Term statusTerm = this.geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (business.hasAttribute(attributeName))
        {
          String value = business.getValue(attributeName);

          if (value != null && value.length() > 0)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get(value);

              try
              {
                geoObj.setValue(attributeName, classifier.getClassifierId());
              }
              catch (UnknownTermException e)
              {
                TermValueException ex = new TermValueException();
                ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
                ex.setCode(e.getCode());

                throw e;
              }

            }
            else if (attribute instanceof AttributeDateType)
            {
              try
              {
                geoObj.setValue(attributeName, format.parse(value));
              }
              catch (ParseException e)
              {
                throw new RuntimeException(e);
              }
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              geoObj.setValue(attributeName, new Boolean(value));
            }
            else if (attribute instanceof AttributeFloatType)
            {
              geoObj.setValue(attributeName, new Double(value));
            }
            else if (attribute instanceof AttributeIntegerType)
            {
              geoObj.setValue(attributeName, new Long(value));
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setCode(business.getValue(DefaultAttribute.CODE.getName()));

    String localizedValue = ( (AttributeLocal) BusinessFacade.getEntityDAO(business).getAttributeIF(DefaultAttribute.DISPLAY_LABEL.getName()) ).getValue(Session.getCurrentLocale());
    geoObj.getDisplayLabel().setValue(localizedValue);

    geoObj.setGeometry((Geometry) business.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME));

    return geoObj;
  }

  public Term getTerm(String code)
  {
    return ServiceFactory.getAdapter().getMetadataCache().getTerm(code).get();
  }

  private Geometry getGeometry(GeoEntity geoEntity, GeometryType geometryType)
  {
    if (geometryType.equals(GeometryType.LINE))
    {
      return geoEntity.getGeoLine();
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return geoEntity.getGeoMultiLine();
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return geoEntity.getGeoPoint();
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return geoEntity.getGeoMultiPoint();
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return geoEntity.getGeoPolygon();
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return geoEntity.getGeoMultiPolygon();
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
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

    // MdBusiness parentMdBusiness = parentUniversal.getMdBusiness();
    MdBusiness childMdBusiness = childUniversal.getMdBusiness();

    String refAttrName = getParentReferenceAttributeName(hierarchyTypeCode, parentUniversal);

    String displayLabel = "Reference to " + parentUniversal.getDisplayLabel().getValue() + " in hierarchy " + mdTermRelationship.getDisplayLabel().getValue();

    MdAttributeReference mdAttributeReference = new MdAttributeReference();
    mdAttributeReference.setAttributeName(refAttrName);
    mdAttributeReference.getDisplayLabel().setValue(displayLabel);
    mdAttributeReference.getDescription().setValue(hierarchyTypeCode);
    mdAttributeReference.setRequired(false);
    mdAttributeReference.setDefiningMdClass(childMdBusiness);
    mdAttributeReference.setMdBusiness(MdBusiness.getMdBusiness(GeoEntity.CLASS));
    mdAttributeReference.addIndexType(MdAttributeIndices.NON_UNIQUE_INDEX);
    mdAttributeReference.apply();

    AttributeHierarchy map = new AttributeHierarchy();
    map.setMdAttribute(mdAttributeReference);
    map.setMdTermRelationship(mdTermRelationship);
    map.setKeyName(mdAttributeReference.getKey());
    map.apply();
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
  public static void removeParentReferenceToLeafType(String hierarchyTypeCode, Universal parentUniversal, Universal childUniversal)
  {
    // MdBusiness parentMdBusiness = parentUniversal.getMdBusiness();
    MdBusinessDAOIF childMdBusiness = MdBusinessDAO.get(childUniversal.getMdBusinessOid());

    String refAttrName = getParentReferenceAttributeName(hierarchyTypeCode, parentUniversal);

    MdAttributeConcreteDAOIF mdAttributeReference = childMdBusiness.definesAttribute(refAttrName);

    AttributeHierarchy map = AttributeHierarchy.getByKey(mdAttributeReference.getKey());
    map.delete();

    mdAttributeReference.getBusinessDAO().delete();
  }

  /**
   * Creates a reference attribute name for a child leaf type that references
   * the parent type
   * 
   * @param hierarchyTypeCode
   * @param parentUniversal
   * @return
   */
  public static String getParentReferenceAttributeName(String hierarchyTypeCode, Universal parentUniversal)
  {
    return getParentReferenceAttributeName(hierarchyTypeCode, parentUniversal.getMdBusiness());
  }

  public static String getParentReferenceAttributeName(String hierarchyTypeCode, MdBusiness parentMdBusiness)
  {
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

    String refAttrName = lowerCaseHierarchyName + upperCaseParentClassName;

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
