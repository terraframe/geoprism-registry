/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMultiTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Actor;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.metadata.graph.MdGeoVertex;
import com.runwaysdk.system.gis.metadata.graph.MdGeoVertexQuery;
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

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.HierarchyRootException;
import net.geoprism.registry.InheritedHierarchyAnnotation;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.Organization;
import net.geoprism.registry.TypeInUseException;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;

public class ServerGeoObjectType
{
  // private Logger logger = LoggerFactory.getLogger(ServerLeafGeoObject.class);

  private GeoObjectType type;

  private Universal     universal;

  private MdBusiness    mdBusiness;

  private MdVertexDAOIF mdVertex;

  public ServerGeoObjectType(GeoObjectType go, Universal universal, MdBusiness mdBusiness, MdVertexDAOIF mdVertex)
  {
    this.type = go;
    this.universal = universal;
    this.mdBusiness = mdBusiness;
    this.mdVertex = mdVertex;
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

  public MdVertexDAOIF getMdVertex()
  {
    return mdVertex;
  }

  public void setMdVertex(MdVertexDAOIF mdVertex)
  {
    this.mdVertex = mdVertex;
  }

  public String getCode()
  {
    return this.type.getCode();
  }

  public GeometryType getGeometryType()
  {
    return this.type.getGeometryType();
  }

  public boolean isGeometryEditable()
  {
    return this.type.isGeometryEditable();
  }

  public LocalizedValue getLabel()
  {
    return this.type.getLabel();
  }

  public boolean getIsAbstract()
  {
    return this.type.getIsAbstract();
  }

  public JsonObject toJSON(CustomSerializer serializer)
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
    try
    {
      this.deleteInTransaction();

      Session session = (Session) Session.getCurrentSession();

      // If this is being called in a JUnit test scenario then there is no
      // session object in the request.
      if (session != null)
      {
        session.reloadPermissions();
      }

      // If we get here then it was successfully deleted
      // We have to do a full metadata cache
      // refresh because the GeoObjectType is
      // embedded in the HierarchyType
      ServiceFactory.getRegistryService().refreshMetadataCache();
    }
    catch (RuntimeException e)
    {
      // An error occurred re-create the WMS layer
      throw e;
    }
  }

  @Transaction
  private void deleteInTransaction()
  {
    List<ServerHierarchyType> hierarchies = this.getHierarchies(false, true);

    if (hierarchies.size() > 0)
    {
      throw new TypeInUseException("Cannot delete a GeoObjectType with children");
    }

    // for (String hierarchy : hierarchies)
    // {
    // OIterator<com.runwaysdk.business.ontology.Term> it =
    // this.universal.getDirectDescendants(hierarchy);
    //
    // try
    // {
    // if (it.hasNext())
    // {
    // }
    // }
    // finally
    // {
    // it.close();
    // }
    // }

    /*
     * Delete all inherited hierarchies
     */
    List<? extends InheritedHierarchyAnnotation> annotations = InheritedHierarchyAnnotation.getByUniversal(getUniversal());

    for (InheritedHierarchyAnnotation annotation : annotations)
    {
      annotation.delete();
    }

    GeoVertexType.remove(this.universal.getUniversalId());

    /*
     * Delete all Attribute references
     */
    AttributeHierarchy.deleteByUniversal(this.universal);

    // This deletes the {@link MdBusiness} as well
    this.universal.delete(false);

    // Delete the term root
    Classifier classRootTerm = TermConverter.buildIfNotExistdMdBusinessClassifier(this.mdBusiness);
    classRootTerm.delete();

    // Delete the roles. Sub types don't have direct roles, they only have the
    // roles specified on the super type.
    if (this.getSuperType() == null)
    {
      Actor ownerActor = this.universal.getOwner();

      if (ownerActor instanceof Roles)
      {
        Roles ownerRole = (Roles) ownerActor;
        String roleName = ownerRole.getRoleName();

        if (RegistryRole.Type.isOrgRole(roleName))
        {
          String organizationCode = RegistryRole.Type.parseOrgCode(roleName);

          String geoObjectTypeCode = this.type.getCode();

          String rmRoleName = RegistryRole.Type.getRM_RoleName(organizationCode, geoObjectTypeCode);
          Roles role = Roles.findRoleByName(rmRoleName);
          role.delete();

          String rcRoleName = RegistryRole.Type.getRC_RoleName(organizationCode, geoObjectTypeCode);
          role = Roles.findRoleByName(rcRoleName);
          role.delete();

          String acRoleName = RegistryRole.Type.getAC_RoleName(organizationCode, geoObjectTypeCode);
          role = Roles.findRoleByName(acRoleName);
          role.delete();
        }
      }
    }

    MasterList.markAllAsInvalid(null, this);
  }

  public void update(GeoObjectType geoObjectTypeNew)
  {
    GeoObjectType geoObjectTypeModified = this.type.copy(geoObjectTypeNew);

    Universal universal = updateGeoObjectType(geoObjectTypeModified);

    ServerGeoObjectType geoObjectTypeModifiedApplied = new ServerGeoObjectTypeConverter().build(universal);

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addGeoObjectType(geoObjectTypeModifiedApplied);

    this.type = geoObjectTypeModifiedApplied.getType();
    this.universal = geoObjectTypeModifiedApplied.getUniversal();
    this.mdBusiness = geoObjectTypeModifiedApplied.getMdBusiness();
  }

  @Transaction
  private Universal updateGeoObjectType(GeoObjectType geoObjectType)
  {
    this.universal.lock();

    this.universal.setIsGeometryEditable(geoObjectType.isGeometryEditable());
    LocalizedValueConverter.populate(universal.getDisplayLabel(), geoObjectType.getLabel());
    LocalizedValueConverter.populate(universal.getDescription(), geoObjectType.getDescription());

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
    JsonObject attrObj = JsonParser.parseString(attributeTypeJSON).getAsJsonObject();

    AttributeType attrType = AttributeType.parse(attrObj);

    MdAttributeConcrete mdAttribute = this.createMdAttributeFromAttributeType(attrType);

    attrType = new AttributeTypeConverter().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));

    this.type.addAttribute(attrType);

    // If this did not error out then add to the cache
    this.refreshCache();

    // Refresh the users session
    if (Session.getCurrentSession() != null)
    {
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }

    return attrType;
  }

  private void refreshCache()
  {
    ServiceFactory.getMetadataCache().addGeoObjectType(this);

    // Refresh all of the subtypes
    List<ServerGeoObjectType> subtypes = this.getSubtypes();
    for (ServerGeoObjectType subtype : subtypes)
    {
      ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(subtype.getUniversal());

      ServiceFactory.getMetadataCache().addGeoObjectType(type);
    }
  }

  /**
   * @return The organization associated with this GeoObjectType.
   */
  public Organization getOrganization()
  {
    Actor owner = this.universal.getOwner();

    if (! ( owner instanceof Roles ))
    {
      return null; // If we get here, then the GeoObjectType was not created
                   // correctly.
    }
    else
    {
      Roles uniRole = (Roles) owner;
      String myOrgCode = RegistryRole.Type.parseOrgCode(uniRole.getRoleName());

      return Organization.getByCode(myOrgCode);
    }
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

    LocalizedValueConverter.populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
    LocalizedValueConverter.populate(mdAttribute.getDescription(), attributeType.getDescription());

    mdAttribute.setDefiningMdClass(this.mdBusiness);
    mdAttribute.apply();

    if (attributeType.getType().equals(AttributeTermType.TYPE))
    {
      MdAttributeTerm mdAttributeTerm = (MdAttributeTerm) mdAttribute;

      // Build the parent class term root if it does not exist.
      Classifier classTerm = TermConverter.buildIfNotExistdMdBusinessClassifier(this.mdBusiness);

      // Create the root term node for this attribute
      Classifier attributeTermRoot = TermConverter.buildIfNotExistAttribute(this.mdBusiness, mdAttributeTerm.getAttributeName(), classTerm);

      // Make this the root term of the multi-attribute
      attributeTermRoot.addClassifierTermAttributeRoots(mdAttributeTerm).apply();

      AttributeTermType attributeTermType = (AttributeTermType) attributeType;

      LocalizedValue label = LocalizedValueConverter.convert(attributeTermRoot.getDisplayLabel());

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

    ( (MdVertexDAO) this.mdVertex ).copyAttribute(MdAttributeDAO.get(mdAttribute.getOid()));

    return mdAttribute;
  }

  public void removeAttribute(String attributeName)
  {
    this.deleteMdAttributeFromAttributeType(attributeName);

    this.type.removeAttribute(attributeName);

    // If this did not error out then add to the cache
    this.refreshCache();

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
        String attributeTermKey = TermConverter.buildtAtttributeKey(this.mdBusiness.getTypeName(), mdAttributeConcreteDAOIF.definesAttribute());

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

    MdAttributeDAOIF mdAttributeDAO = this.mdVertex.definesAttribute(attributeName);

    if (mdAttributeDAO != null)
    {
      mdAttributeDAO.getBusinessDAO().delete();
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
  public MdAttributeConcreteDAOIF getMdAttribute(String attributeName)
  {
    MdBusinessDAOIF mdBusinessDAOIF = (MdBusinessDAOIF) getMdBusinessDAO();

    return mdBusinessDAOIF.definesAttribute(attributeName);
  }

  public AttributeType updateAttributeType(String attributeTypeJSON)
  {
    JsonObject attrObj = JsonParser.parseString(attributeTypeJSON).getAsJsonObject();
    AttributeType attrType = AttributeType.parse(attrObj);

    MdAttributeConcrete mdAttribute = this.updateMdAttributeFromAttributeType(attrType);
    attrType = new AttributeTypeConverter().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));

    this.type.addAttribute(attrType);

    // If this did not error out then add to the cache
    this.refreshCache();

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
        LocalizedValueConverter.populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
        LocalizedValueConverter.populate(mdAttribute.getDescription(), attributeType.getDescription());

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
        String classifierKey = TermConverter.buildClassifierKeyFromTermCode(getRootTerm.getCode());

        TermConverter termBuilder = new TermConverter(classifierKey);
        attributeTermType.setRootTerm(termBuilder.build());
      }

      return mdAttribute;
    }

    return null;
  }

  public List<ServerGeoObjectType> getChildren(ServerHierarchyType hierarchy)
  {
    List<ServerGeoObjectType> children = new LinkedList<>();
    String mdRelationshipType = hierarchy.getUniversalRelationship().definesType();

    try (OIterator<? extends Business> iterator = this.universal.getDirectDescendants(mdRelationshipType))
    {
      while (iterator.hasNext())
      {
        Universal cUniversal = (Universal) iterator.next();

        children.add(ServerGeoObjectType.get(cUniversal));
      }

    }

    return children;
  }

  public ServerGeoObjectType getSuperType()
  {
    if (this.type.getSuperTypeCode() != null && this.type.getSuperTypeCode().length() > 0)
    {
      return ServerGeoObjectType.get(this.type.getSuperTypeCode());
    }

    return null;
  }

  public List<ServerGeoObjectType> getSubtypes()
  {
    List<ServerGeoObjectType> children = new LinkedList<>();

    if (this.getIsAbstract())
    {
      MdGeoVertexQuery query = new MdGeoVertexQuery(new QueryFactory());
      query.WHERE(query.getSuperMdVertex().EQ(this.getMdVertex().getOid()));

      try (OIterator<? extends MdGeoVertex> iterator = query.getIterator())
      {
        while (iterator.hasNext())
        {
          MdGeoVertex cUniversal = (MdGeoVertex) iterator.next();

          children.add(ServerGeoObjectType.get(MdGeoVertexDAO.get(cUniversal.getOid())));
        }

      }
    }

    return children;
  }

  public Set<ServerHierarchyType> getHierarchiesOfSubTypes()
  {
    List<ServerGeoObjectType> subtypes = this.getSubtypes();
    Set<ServerHierarchyType> hierarchyTypes = new TreeSet<ServerHierarchyType>(new Comparator<ServerHierarchyType>()
    {
      @Override
      public int compare(ServerHierarchyType o1, ServerHierarchyType o2)
      {
        return o1.getCode().compareTo(o2.getCode());
      }
    });

    for (ServerGeoObjectType type : subtypes)
    {
      hierarchyTypes.addAll(type.getHierarchies(false, false));
    }

    return hierarchyTypes;
  }

  public List<ServerHierarchyType> getHierarchies()
  {
    return getHierarchies(true, true);
  }

  public List<ServerHierarchyType> getHierarchies(boolean includeAllHierarchiesIfNone, boolean includeFromSuperType)
  {
    List<ServerHierarchyType> hierarchies = new LinkedList<ServerHierarchyType>();

    List<ServerHierarchyType> hierarchyTypes = ServiceFactory.getMetadataCache().getAllHierarchyTypes();
    Universal root = Universal.getRoot();

    for (ServerHierarchyType hierarchyType : hierarchyTypes)
    {
      Organization org = hierarchyType.getOrganization();

      if (ServiceFactory.getHierarchyPermissionService().canRead(org.getCode(), PermissionContext.READ))
      {

        if (this.isRoot(hierarchyType))
        {
          hierarchies.add(hierarchyType);
        }
        else
        {
          // Note: Ordered ancestors always includes self
          Collection<?> parents = GeoEntityUtil.getOrderedAncestors(root, this.getUniversal(), hierarchyType.getUniversalType());

          if (parents.size() > 1)
          {
            hierarchies.add(hierarchyType);
          }
        }

      }
    }

    if (includeFromSuperType)
    {
      ServerGeoObjectType superType = this.getSuperType();

      if (superType != null)
      {
        hierarchies.addAll(superType.getHierarchies(includeAllHierarchiesIfNone, includeFromSuperType));
      }
    }

    if (includeAllHierarchiesIfNone && hierarchies.size() == 0)
    {
      /*
       * This is a root type so include all hierarchies
       */

      for (ServerHierarchyType hierarchyType : hierarchyTypes)
      {
        Organization org = hierarchyType.getOrganization();

        if (ServiceFactory.getHierarchyPermissionService().canRead(org.getCode(), PermissionContext.READ))
        {
          hierarchies.add(hierarchyType);
        }
      }
    }

    return hierarchies;
  }

  /**
   * @param sType
   *          Hierarchy Type
   * 
   * @return If this geo object type is the direct (non-inherited) root of the
   *         given hierarchy
   */
  public boolean isRoot(ServerHierarchyType sType)
  {
    List<ServerGeoObjectType> roots = sType.getDirectRootNodes();

    for (ServerGeoObjectType root : roots)
    {
      if (root.getCode().equals(this.type.getCode()))
      {
        return true;
      }
    }

    return false;
  }

  @Transaction
  public InheritedHierarchyAnnotation setInheritedHierarchy(ServerHierarchyType forHierarchy, ServerHierarchyType inheritedHierarchy)
  {
    // Ensure that this geo object type is the root geo object type for the "For
    // Hierarchy"
    if (!this.isRoot(forHierarchy) || this.getIsAbstract())
    {
      throw new HierarchyRootException();
    }

    InheritedHierarchyAnnotation annotation = new InheritedHierarchyAnnotation();
    annotation.setUniversal(this.universal);
    annotation.setInheritedHierarchy(inheritedHierarchy.getUniversalRelationship());
    annotation.setForHierarchy(forHierarchy.getUniversalRelationship());
    annotation.apply();

    return annotation;
  }

  @Transaction
  public void removeInheritedHierarchy(ServerHierarchyType forHierarchy)
  {
    InheritedHierarchyAnnotation annotation = InheritedHierarchyAnnotation.get(this.universal, forHierarchy.getUniversalRelationship());

    if (annotation != null)
    {
      annotation.delete();
    }
  }

  public ServerHierarchyType getInheritedHierarchy(ServerHierarchyType hierarchy)
  {
    return this.getInheritedHierarchy(hierarchy.getUniversalRelationship());
  }

  public ServerHierarchyType getInheritedHierarchy(MdTermRelationship universalRelationship)
  {
    InheritedHierarchyAnnotation annotation = InheritedHierarchyAnnotation.get(this.universal, universalRelationship);

    if (annotation != null)
    {
      return ServerHierarchyType.get(annotation.getInheritedHierarchy());
    }

    return null;
  }

  /**
   * Returns all ancestors of a GeoObjectType
   * 
   * @param hierarchyType
   *          The Hierarchy code
   * @param includeInheritedTypes
   *          TODO
   * @param GeoObjectType
   *          child
   * 
   * @return
   */
  public List<GeoObjectType> getTypeAncestors(ServerHierarchyType hierarchyType, Boolean includeInheritedTypes)
  {
    List<GeoObjectType> ancestors = new LinkedList<GeoObjectType>();

    Collection<com.runwaysdk.business.ontology.Term> list = GeoEntityUtil.getOrderedAncestors(Universal.getRoot(), this.getUniversal(), hierarchyType.getUniversalType());

    list.forEach(term -> {
      Universal parent = (Universal) term;

      if (!parent.getKeyName().equals(Universal.ROOT) && !parent.getOid().equals(this.getUniversal().getOid()))
      {
        ServerGeoObjectType sParent = ServerGeoObjectType.get(parent);

        ancestors.add(sParent.getType());

        if (includeInheritedTypes && sParent.isRoot(hierarchyType))
        {
          ServerHierarchyType inheritedHierarchy = sParent.getInheritedHierarchy(hierarchyType);

          if (inheritedHierarchy != null)
          {
            ancestors.addAll(0, sParent.getTypeAncestors(inheritedHierarchy, includeInheritedTypes));
          }
        }
      }
    });

    if (ancestors.size() == 0)
    {
      ServerGeoObjectType superType = this.getSuperType();

      if (superType != null)
      {
        return superType.getTypeAncestors(hierarchyType, includeInheritedTypes);
      }
    }

    return ancestors;
  }

  /**
   * Finds the actual hierarchy used for the parent type if the parent type is
   * inherited from a different hierarchy
   * 
   * @param hierarchyType
   * @param parent
   * @return
   */
  public ServerHierarchyType findHierarchy(ServerHierarchyType hierarchyType, ServerGeoObjectType parent)
  {
    Collection<com.runwaysdk.business.ontology.Term> list = GeoEntityUtil.getOrderedAncestors(Universal.getRoot(), this.getUniversal(), hierarchyType.getUniversalType());

    for (Object term : list)
    {
      Universal universal = (Universal) term;

      if (parent.getUniversal().getOid().equals(universal.getOid()))
      {
        return hierarchyType;
      }

      ServerGeoObjectType sParent = ServerGeoObjectType.get(universal);

      if (sParent.isRoot(hierarchyType))
      {
        ServerHierarchyType inheritedHierarchy = sParent.getInheritedHierarchy(hierarchyType);

        if (inheritedHierarchy != null)
        {
          return sParent.findHierarchy(inheritedHierarchy, parent);
        }
      }
    }

    return hierarchyType;
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
    if (code == null || code.equals(Universal.ROOT))
    {
      return RootGeoObjectType.INSTANCE;
    }

    Optional<ServerGeoObjectType> geoObjectType = ServiceFactory.getMetadataCache().getGeoObjectType(code);

    if (geoObjectType.isPresent())
    {
      return geoObjectType.get();
    }
    else
    {
      net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
      ex.setTypeLabel(GeoObjectTypeMetadata.get().getClassDisplayLabel());
      ex.setDataIdentifier(code);
      ex.setAttributeLabel(GeoObjectTypeMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
      throw ex;
    }
  }

  public static ServerGeoObjectType get(GeoObjectType geoObjectType)
  {
    String code = geoObjectType.getCode();

    return ServiceFactory.getMetadataCache().getGeoObjectType(code).get();
  }

  public static ServerGeoObjectType get(Universal universal)
  {
    String code = universal.getKey();

    return ServiceFactory.getMetadataCache().getGeoObjectType(code).get();
  }

  public static ServerGeoObjectType get(MdVertexDAOIF mdVertex)
  {
    String code = mdVertex.getTypeName();

    return ServiceFactory.getMetadataCache().getGeoObjectType(code).get();
  }

  // public String buildRMRoleName()
  // {
  // String ownerActorOid = this.universal.getOwnerOid();
  // Organization.getRootOrganization(ownerActorOid)
  // }

}
