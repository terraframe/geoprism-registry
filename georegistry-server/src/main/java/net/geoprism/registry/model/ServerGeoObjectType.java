package net.geoprism.registry.model;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.ontology.TermHacker;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMultiTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeDouble;
import com.runwaysdk.system.metadata.MdAttributeIndices;
import com.runwaysdk.system.metadata.MdAttributeLong;
import com.runwaysdk.system.metadata.MdAttributeTerm;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.ImmutableRootException;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.CannotDeleteGeoObjectTypeWithChildren;
import net.geoprism.registry.GeoObjectTypeHasDataException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.conversion.AttributeTypeBuilder;
import net.geoprism.registry.conversion.ServerGeoObjectTypeBuilder;
import net.geoprism.registry.conversion.TermBuilder;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.WMSService;

public class ServerGeoObjectType
{
  private Logger        logger = LoggerFactory.getLogger(ServerLeafGeoObject.class);

  private GeoObjectType type;

  private Universal     universal;

  private MdBusiness    mdBusiness;

  public ServerGeoObjectType(GeoObjectType go, Universal universal, MdBusiness mdBusiness)
  {
    this.type = go;
    this.universal = universal;
    this.mdBusiness = mdBusiness;
  }

  public GeoObjectType getType()
  {
    return type;
  }

  public void setType(GeoObjectType type)
  {
    this.type = type;
  }

  public Universal getUniversal()
  {
    return universal;
  }

  public void setUniversal(Universal universal)
  {
    this.universal = universal;
  }

  public MdBusiness getMdBusiness()
  {
    return mdBusiness;
  }

  public MdBusinessDAOIF getMdBusinessDAO()
  {
    return (MdBusinessDAOIF) BusinessFacade.getEntityDAO(this.mdBusiness);
  }

  public void setMdBusiness(MdBusiness mdBusiness)
  {
    this.mdBusiness = mdBusiness;
  }

  public String getCode()
  {
    return this.type.getCode();
  }

  public GeometryType getGeometryType()
  {
    return this.type.getGeometryType();
  }

  public boolean isLeaf()
  {
    return this.type.isLeaf();
  }

  public LocalizedValue getLabel()
  {
    return this.type.getLabel();
  }

  public JsonObject toJSON(ImportAttributeSerializer serializer)
  {
    return this.type.toJSON(serializer);
  }

  public Map<String, AttributeType> getAttributeMap()
  {
    return this.type.getAttributeMap();
  }

  public Optional<AttributeType> getAttribute(String name)
  {
    return this.type.getAttribute(name);
  }

  public String definesType()
  {
    return this.mdBusiness.definesType();
  }

  public List<? extends MdAttributeConcreteDAOIF> definesAttributes()
  {
    return this.getMdBusinessDAO().definesAttributes();
  }

  public void deleteAllRecords()
  {
    this.getMdBusinessDAO().getBusinessDAO().deleteAllRecords();
  }

  public void delete()
  {
    new WMSService().deleteWMSLayer(this);

    try
    {
      this.deleteInTransaction();

      ( (Session) Session.getCurrentSession() ).reloadPermissions();

      // If we get here then it was successfully deleted
      // We have to do a full metadata cache
      // refresh because the GeoObjectType is
      // embedded in the HierarchyType
      ServiceFactory.getRegistryService().refreshMetadataCache();
    }
    catch (RuntimeException e)
    {
      // An error occurred re-create the WMS layer
      new WMSService().createWMSLayer(this, false);

      throw e;
    }
  }

  @Transaction
  private void deleteInTransaction()
  {
    String[] hierarchies = TermUtil.getAllParentRelationships(this.universal.getOid());

    for (String hierarchy : hierarchies)
    {
      OIterator<com.runwaysdk.business.ontology.Term> it = this.universal.getDirectDescendants(hierarchy);

      try
      {
        if (it.hasNext())
        {
          throw new CannotDeleteGeoObjectTypeWithChildren("Cannot delete a GeoObjectType with children");
        }
      }
      finally
      {
        it.close();
      }
    }

    /*
     * Delete all Attribute references
     */
    AttributeHierarchy.deleteByUniversal(this.universal);

    // This deletes the {@link MdBusiness} as well
    this.universal.delete(false);

    // Delete the term root
    Classifier classRootTerm = TermBuilder.buildIfNotExistdMdBusinessClassifier(this.mdBusiness);
    classRootTerm.delete();
  }

  public void update(GeoObjectType geoObjectTypeNew)
  {
    GeoObjectType geoObjectTypeModified = this.type.copy(geoObjectTypeNew);

    Universal universal = updateGeoObjectType(geoObjectTypeModified);

    ServerGeoObjectType geoObjectTypeModifiedApplied = new ServerGeoObjectTypeBuilder().build(universal);

    // If this did not error out then add to the cache
    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(geoObjectTypeModifiedApplied.getType());

    this.type = geoObjectTypeModifiedApplied.getType();
    this.universal = geoObjectTypeModifiedApplied.getUniversal();
    this.mdBusiness = geoObjectTypeModifiedApplied.getMdBusiness();
  }

  @Transaction
  private Universal updateGeoObjectType(GeoObjectType geoObjectType)
  {
    this.universal.lock();

    this.universal.setIsGeometryEditable(geoObjectType.isGeometryEditable());
    ServiceFactory.getConversionService().populate(universal.getDisplayLabel(), geoObjectType.getLabel());
    ServiceFactory.getConversionService().populate(universal.getDescription(), geoObjectType.getDescription());

    this.universal.apply();

    MdBusiness mdBusiness = universal.getMdBusiness();

    mdBusiness.lock();
    mdBusiness.getDisplayLabel().setValue(universal.getDisplayLabel().getValue());
    mdBusiness.getDescription().setValue(universal.getDescription().getValue());
    mdBusiness.apply();

    mdBusiness.unlock();

    universal.unlock();

    return universal;
  }

  public AttributeType createAttributeType(String attributeTypeJSON)
  {
    JsonParser parser = new JsonParser();
    JsonObject attrObj = parser.parse(attributeTypeJSON).getAsJsonObject();

    AttributeType attrType = AttributeType.parse(attrObj);

    MdAttributeConcrete mdAttribute = this.createMdAttributeFromAttributeType(attrType);

    attrType = new AttributeTypeBuilder().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));

    this.type.addAttribute(attrType);

    // If this did not error out then add to the cache
    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(this.type);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return attrType;
  }

  /**
   * Creates an {@link MdAttributeConcrete} for the given {@link MdBusiness}
   * from the given {@link AttributeType}
   * 
   * @pre assumes no attribute has been defined on the type with the given name.
   * @param geoObjectType
   *          TODO
   * @param mdBusiness
   *          Type to receive attribute definition
   * @param attributeType
   *          newly defined attribute
   * 
   * @return {@link AttributeType}
   */
  @Transaction
  public MdAttributeConcrete createMdAttributeFromAttributeType(AttributeType attributeType)
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

    mdAttribute.setDefiningMdClass(this.mdBusiness);
    mdAttribute.apply();

    if (attributeType.getType().equals(AttributeTermType.TYPE))
    {
      MdAttributeTerm mdAttributeTerm = (MdAttributeTerm) mdAttribute;

      // Build the parent class term root if it does not exist.
      Classifier classTerm = TermBuilder.buildIfNotExistdMdBusinessClassifier(this.mdBusiness);

      // Create the root term node for this attribute
      Classifier attributeTermRoot = TermBuilder.buildIfNotExistAttribute(this.mdBusiness, mdAttributeTerm.getAttributeName(), classTerm);

      // Make this the root term of the multi-attribute
      attributeTermRoot.addClassifierTermAttributeRoots(mdAttributeTerm).apply();

      AttributeTermType attributeTermType = (AttributeTermType) attributeType;

      LocalizedValue label = new ServerGeoObjectTypeBuilder().convert(attributeTermRoot.getDisplayLabel());

      org.commongeoregistry.adapter.Term term = new org.commongeoregistry.adapter.Term(attributeTermRoot.getClassifierId(), label, new LocalizedValue(""));
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

    MasterList.createMdAttribute(this, attributeType);

    return mdAttribute;
  }

  public void removeAttribute(String attributeName)
  {
    this.deleteMdAttributeFromAttributeType(attributeName);

    this.type.removeAttribute(attributeName);

    // If this did not error out then add to the cache
    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(this.type);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();
  }

  /**
   * Delete the {@link MdAttributeConcreteDAOIF} from the given {
   * 
   * @param type
   *          TODO
   * @param mdBusiness
   * @param attributeName
   */
  @Transaction
  public void deleteMdAttributeFromAttributeType(String attributeName)
  {
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = getMdAttribute(attributeName);

    if (mdAttributeConcreteDAOIF != null)
    {
      if (mdAttributeConcreteDAOIF instanceof MdAttributeTermDAOIF || mdAttributeConcreteDAOIF instanceof MdAttributeMultiTermDAOIF)
      {
        String attributeTermKey = TermBuilder.buildtAtttributeKey(this.mdBusiness.getTypeName(), mdAttributeConcreteDAOIF.definesAttribute());

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

      Optional<AttributeType> optional = this.type.getAttribute(attributeName);

      if (optional.isPresent())
      {
        MasterList.deleteMdAttribute(this.universal, optional.get());
      }
    }
  }

  /**
   * Returns the {link MdAttributeConcreteDAOIF} for the given
   * {@link AttributeType} defined on the given {@link MdBusiness} or null no
   * such attribute is defined.
   * 
   * @param attributeName
   * 
   * @return
   */
  private MdAttributeConcreteDAOIF getMdAttribute(String attributeName)
  {
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) getMdBusinessDAO();

    return mdBusinessDAOIF.definesAttribute(attributeName);
  }

  public AttributeType updateAttributeType(String attributeTypeJSON)
  {
    JsonObject attrObj = new JsonParser().parse(attributeTypeJSON).getAsJsonObject();
    AttributeType attrType = AttributeType.parse(attrObj);

    MdAttributeConcrete mdAttribute = this.updateMdAttributeFromAttributeType(attrType);
    attrType = new AttributeTypeBuilder().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));

    this.type.addAttribute(attrType);

    // If this did not error out then add to the cache
    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(this.type);

    return attrType;
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
  public MdAttributeConcrete updateMdAttributeFromAttributeType(AttributeType attributeType)
  {
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = getMdAttribute(attributeType.getName());

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

        org.commongeoregistry.adapter.Term getRootTerm = attributeTermType.getRootTerm();
        String classifierKey = TermBuilder.buildClassifierKeyFromTermCode(getRootTerm.getCode());

        TermBuilder termBuilder = new TermBuilder(classifierKey);
        attributeTermType.setRootTerm(termBuilder.build());
      }

      return mdAttribute;
    }

    return null;
  }

  public static HierarchyType removeChild(String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    String mdTermRelKey = ConversionService.buildMdTermRelUniversalKey(hierarchyTypeCode);
    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey);

    removeFromHierarchy(mdTermRelationship, hierarchyTypeCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);

    ServiceFactory.getAdapter().getMetadataCache().addHierarchyType(ht);

    return ht;
  }

  @Transaction
  private static void removeFromHierarchy(MdTermRelationship mdTermRelationship, String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parent;
    if (parentGeoObjectTypeCode == null)
    {
      parent = Universal.getRoot();
    }
    else
    {
      parent = Universal.getByKey(parentGeoObjectTypeCode);
    }

    Universal child = Universal.getByKey(childGeoObjectTypeCode);

    boolean hasData = hasData(child);

    removeAllChildrenFromHierarchy(parent, mdTermRelationship);

    if (hasData)
    {
      child.enforceValidRemoveLink(parent, mdTermRelationship.definesType());
    }

    if (child.getIsLeafType())
    {
      ConversionService.removeParentReferenceToLeafType(hierarchyTypeCode, parent, child);
    }
  }

  private static boolean hasData(Universal uni)
  {
    if (uni.getIsLeafType())
    {
      MdBusiness mdBiz = uni.getMdBusiness();

      BusinessQuery bq = new QueryFactory().businessQuery(mdBiz.definesType());

      if (bq.getCount() > 0)
      {
        GeoObjectTypeHasDataException ex = new GeoObjectTypeHasDataException();
        ex.setName(uni.getDisplayLabel().getValue());
        throw ex;
      }
    }
    else
    {
      GeoEntityQuery query = new GeoEntityQuery(new QueryFactory());
      Condition condition = query.getUniversal().EQ(uni);

      Term[] thisAllDescends = TermUtil.getAllDescendants(uni.getOid(), TermUtil.getAllParentRelationships(uni.getOid()));
      for (int i = 0; i < thisAllDescends.length; ++i)
      {
        condition = condition.OR(query.getUniversal().EQ((Universal) thisAllDescends[i]));
      }
      query.WHERE(condition);

      // return query.getCount() > 0;

      if (query.getCount() > 0)
      {
        GeoObjectTypeHasDataException ex = new GeoObjectTypeHasDataException();
        ex.setName(uni.getDisplayLabel().getValue());
        throw ex;
      }
    }

    return false;
  }

  private static void removeAllChildrenFromHierarchy(Universal parent, MdTermRelationship mdTermRelationship)
  {
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parent.getOid(), new String[] { mdTermRelationship.definesType() });
    for (TermAndRel tnrChild : tnrChildren)
    {
      Universal child = (Universal) tnrChild.getTerm();

      removeAllChildrenFromHierarchy(child, mdTermRelationship);

      removeLink(parent, child, mdTermRelationship.definesType());
    }
  }

  private static void removeLink(Universal parent, Universal child, String relationshipType)
  {
    if (child.getKey().equals(Term.ROOT_KEY))
    {
      ImmutableRootException exception = new ImmutableRootException("Cannot modify the root Term.");
      exception.setRootName(child.getDisplayLabel().getValue());
      exception.apply();

      throw exception;
    }

    // Remove the relationship
    parent.removeAllChildren(child, relationshipType);

    // Update the strategy
    TermHacker.getStrategy(child).removeLink(parent, child, relationshipType);
  }

  /**
   * Returns a {@link Universal} from the code value on the given
   * {@link GeoObjectType}.
   * 
   * @param got
   * @return a {@link Universal} from the code value on the given
   *         {@link GeoObjectType}.
   */
  public static Universal geoObjectTypeToUniversal(GeoObjectType got)
  {
    return Universal.getByKey(got.getCode());
  }

  public static ServerGeoObjectType get(String code)
  {
    GeoObjectType geoObjectType = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(code).get();

    return get(geoObjectType);
  }

  public static ServerGeoObjectType get(GeoObjectType geoObjectType)
  {
    Universal universal = ServerGeoObjectType.geoObjectTypeToUniversal(geoObjectType);

    MdBusiness mdBusiness = universal.getMdBusiness();

    return new ServerGeoObjectType(geoObjectType, universal, mdBusiness);
  }

  public static ServerGeoObjectType get(Universal universal)
  {
    GeoObjectType geoObjectType = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(universal.getKey()).get();

    MdBusiness mdBusiness = universal.getMdBusiness();

    return new ServerGeoObjectType(geoObjectType, universal, mdBusiness);
  }
}
