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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
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
import com.runwaysdk.query.OIterator;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Actor;
import com.runwaysdk.system.Roles;
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
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.CannotDeleteGeoObjectTypeWithChildren;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.DeleteGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.WMSService;

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

      Session session = (Session) Session.getCurrentSession();
      
      // If this is being called in a JUnit test scenario then there is no session object in the request.
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

    Actor ownerActor = this.universal.getOwner();
    
    if (ownerActor instanceof Roles)
    {
      Roles ownerRole = (Roles)ownerActor;
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

  public void update(GeoObjectType geoObjectTypeNew)
  {
    GeoObjectType geoObjectTypeModified = this.type.copy(geoObjectTypeNew);

    Universal universal = updateGeoObjectType(geoObjectTypeModified);

    ServerGeoObjectType geoObjectTypeModifiedApplied = new ServerGeoObjectTypeConverter().build(universal);

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
    JsonParser parser = new JsonParser();
    JsonObject attrObj = parser.parse(attributeTypeJSON).getAsJsonObject();

    AttributeType attrType = AttributeType.parse(attrObj);

    MdAttributeConcrete mdAttribute = this.createMdAttributeFromAttributeType(attrType);

    attrType = new AttributeTypeConverter().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));

    this.type.addAttribute(attrType);

    // If this did not error out then add to the cache
    ServiceFactory.getAdapter().getMetadataCache().addGeoObjectType(this.type);

    // Refresh the users session
    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return attrType;
  }
  
  /**
   * @return The organization associated with this GeoObjectType.
   */
  public Organization getOrganization()
  {
    Actor owner = this.universal.getOwner();
    
    if (!(owner instanceof Roles))
    {
      return null; // If we get here, then the GeoObjectType was not created correctly.
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

      LocalizedValue label = new ServerGeoObjectTypeConverter().convert(attributeTermRoot.getDisplayLabel());

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
    attrType = new AttributeTypeConverter().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));

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

  public List<ServerHierarchyType> getHierarchies()
  {
    List<ServerHierarchyType> hierarchies = new LinkedList<ServerHierarchyType>();

    HierarchyType[] hierarchyTypes = ServiceFactory.getAdapter().getMetadataCache().getAllHierarchyTypes();
    Universal root = Universal.getRoot();

    for (HierarchyType hierarchyType : hierarchyTypes)
    {
      ServerHierarchyType sType = ServerHierarchyType.get(hierarchyType);

      // Note: Ordered ancestors always includes self
      Collection<?> parents = GeoEntityUtil.getOrderedAncestors(root, this.getUniversal(), sType.getUniversalType());

      if (parents.size() > 1)
      {
        hierarchies.add(sType);
      }
    }

    if (hierarchies.size() == 0)
    {
      /*
       * This is a root type so include all hierarchies
       */

      for (HierarchyType hierarchyType : hierarchyTypes)
      {
        hierarchies.add(ServerHierarchyType.get(hierarchyType));
      }
    }

    return hierarchies;
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
    Optional<GeoObjectType> geoObjectType = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(code);

    if (geoObjectType.isPresent())
    {
      return get(geoObjectType.get());
    }
    else
    {
      return null;
    }
  }

  public static ServerGeoObjectType get(GeoObjectType geoObjectType)
  {
    Universal universal = ServerGeoObjectType.geoObjectTypeToUniversal(geoObjectType);

    MdBusiness mdBusiness = universal.getMdBusiness();

    return new ServerGeoObjectType(geoObjectType, universal, mdBusiness, GeoVertexType.getMdGeoVertex(universal.getUniversalId()));
  }

  public static ServerGeoObjectType get(Universal universal)
  {
    GeoObjectType geoObjectType = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(universal.getKey()).get();

    MdBusiness mdBusiness = universal.getMdBusiness();

    return new ServerGeoObjectType(geoObjectType, universal, mdBusiness, GeoVertexType.getMdGeoVertex(universal.getUniversalId()));
  }

  public static ServerGeoObjectType get(MdVertexDAOIF mdVertex)
  {
    GeoObjectType geoObjectType = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(mdVertex.getTypeName()).get();

    Universal universal = ServerGeoObjectType.geoObjectTypeToUniversal(geoObjectType);
    MdBusiness mdBusiness = universal.getMdBusiness();

    return new ServerGeoObjectType(geoObjectType, universal, mdBusiness, mdVertex);
  }

  /**
   * Operation must be one of:
   * - WRITE (Update)
   * - READ
   * - DELETE
   * - CREATE
   * 
   * @param actor
   * @param op
   */
  public void enforceActorHasPermission(SingleActorDAOIF actor, Operation op, boolean allowRC)
  {
    if (!this.doesActorHavePermission(actor, op, allowRC))
    {
      Organization org = this.getOrganization();
      
      if (op.equals(Operation.WRITE))
      {
        WriteGeoObjectPermissionException ex = new WriteGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(this.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.READ))
      {
        ReadGeoObjectPermissionException ex = new ReadGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(this.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.DELETE))
      {
        DeleteGeoObjectPermissionException ex = new DeleteGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(this.getLabel().getValue());
        throw ex;
      }
      else if (op.equals(Operation.CREATE))
      {
        CreateGeoObjectPermissionException ex = new CreateGeoObjectPermissionException();
        ex.setOrganization(org.getDisplayLabel().getValue());
        ex.setGeoObjectType(this.getLabel().getValue());
        throw ex;
      }
    }
  }
  
  public boolean doesActorHavePermission(SingleActorDAOIF actor, Operation op, boolean allowRC)
  {
    Organization thisOrg = this.getOrganization();
    
    if (thisOrg != null)
    {
      String thisOrgCode = thisOrg.getCode();
      
      Set<RoleDAOIF> roles = actor.authorizedRoles();
      
      for (RoleDAOIF role : roles)
      {
        String roleName = role.getRoleName();
        
        if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
        {
          String orgCode = RegistryRole.Type.parseOrgCode(roleName);
          
          if (RegistryRole.Type.isRA_Role(roleName) && orgCode.equals(thisOrgCode))
          {
            return true;
          }
          else if ( ( (allowRC && RegistryRole.Type.isRC_Role(roleName)) || RegistryRole.Type.isRM_Role(roleName)) && orgCode.equals(thisOrgCode) )
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(this.getCode()))
            {
              return true;
            }
          }
          else if ( (RegistryRole.Type.isAC_Role(roleName) || RegistryRole.Type.isRC_Role(roleName)) && op.equals(Operation.READ) && orgCode.equals(thisOrgCode))
          {
            String gotCode = RegistryRole.Type.parseGotCode(roleName);
            
            if (gotCode.equals(this.getCode()))
            {
              return true;
            }
          }
        }
        else if (RegistryRole.Type.isSRA_Role(roleName))
        {
          return true;
        }
      }
    }
    
    return false;
  }

//  public String buildRMRoleName()
//  {
//    String ownerActorOid = this.universal.getOwnerOid();
//    Organization.getRootOrganization(ownerActorOid)
//  }
  
}
