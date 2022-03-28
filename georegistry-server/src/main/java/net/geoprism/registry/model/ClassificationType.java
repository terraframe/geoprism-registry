package net.geoprism.registry.model;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.graph.VertexObjectDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdClassificationDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.AbstractClassification;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.gis.metadata.graph.MdGeoVertex;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdClassification;

import net.geoprism.registry.InvalidMasterListCodeException;
import net.geoprism.registry.MasterList;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.query.ClassificationTypePageQuery;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class ClassificationType implements JsonSerializable
{
  private MdClassificationDAOIF mdClassification;

  public ClassificationType()
  {
  }

  public ClassificationType(MdClassificationDAOIF mdClassification)
  {
    this.mdClassification = mdClassification;
  }

  public MdClassificationDAOIF getMdClassification()
  {
    return mdClassification;
  }

  public void setMdClassification(MdClassificationDAOIF mdClassification)
  {
    this.mdClassification = mdClassification;
  }

  public MdClassification getMdClassificationObject()
  {
    return (MdClassification) BusinessFacade.get(this.mdClassification);
  }

  public String getCode()
  {
    return this.mdClassification.getValue(MdClassificationInfo.TYPE_NAME);
  }

  public String getType()
  {
    return this.mdClassification.definesType();
  }

  public String getOid()
  {
    return this.mdClassification.getOid();
  }

  public LocalizedValue getDisplayLabel()
  {
    return LocalizedValueConverter.convert(this.mdClassification.getDisplayLabels());
  }

  public LocalizedValue getDescription()
  {
    return LocalizedValueConverter.convert(this.mdClassification.getDescriptions());
  }

  @Transaction
  public void delete()
  {
    this.mdClassification.getBusinessDAO().delete();
  }

  public MdEdgeDAOIF getMdEdge()
  {
    return this.mdClassification.getReferenceMdEdgeDAO();
  }

  public MdVertexDAOIF getMdVertex()
  {
    return this.mdClassification.getReferenceMdVertexDAO();
  }

  public void setRoot(Classification classification)
  {
    MdClassificationDAO mdClassificationDAO = (MdClassificationDAO) this.mdClassification.getBusinessDAO();
    mdClassificationDAO.setValue(MdClassificationInfo.ROOT, classification.getOid());
    mdClassificationDAO.apply();

    this.mdClassification = mdClassificationDAO;
  }

  public Classification getRoot()
  {
    VertexObjectDAOIF root = this.mdClassification.getRoot();

    if (root != null)
    {
      return new Classification(this, VertexObject.instantiate((VertexObjectDAO) root));
    }

    return null;
  }

  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty(MdClassificationInfo.OID, getOid());
    object.addProperty(MdClassificationInfo.TYPE, getType());
    object.addProperty(DefaultAttribute.CODE.getName(), this.getCode());
    object.add(MdClassificationInfo.DISPLAY_LABEL, this.getDisplayLabel().toJSON());
    object.add(MdClassificationInfo.DESCRIPTION, this.getDescription().toJSON());

    String rootOid = this.mdClassification.getValue(MdClassificationInfo.ROOT);

    if (rootOid != null && rootOid.length() > 0)
    {
      Classification root = Classification.getByOid(this, rootOid);

      object.add(MdClassificationInfo.ROOT, root.toJSON());
    }

    return object;
  }

  /**
   * Assigns all permissions to the {@link ComponentIF} to the given role.
   * 
   * Precondition: component is either a {@link MdGeoVertex} or a
   * {@link MdBusiness}.
   * 
   * @param component
   * @param role
   * @param includeWrite
   *          TODO
   */
  private void assignPermissions(ComponentIF component, Roles role, boolean includeWrite)
  {
    RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
    roleDAO.grantPermission(Operation.READ, component.getOid());
    roleDAO.grantPermission(Operation.READ_ALL, component.getOid());

    if (includeWrite)
    {
      roleDAO.grantPermission(Operation.CREATE, component.getOid());
      roleDAO.grantPermission(Operation.DELETE, component.getOid());
      roleDAO.grantPermission(Operation.WRITE, component.getOid());

      roleDAO.grantPermission(Operation.WRITE_ALL, component.getOid());
    }
  }

  public void assignPermissions()
  {
    MdVertexDAOIF mdVertex = this.mdClassification.getReferenceMdVertexDAO();
    MdEdgeDAOIF mdEdge = this.mdClassification.getReferenceMdEdgeDAO();

    Roles sraRole = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);

    this.assignPermissions(mdVertex, sraRole, true);
    this.assignPermissions(mdEdge, sraRole, true);

    Roles raRole = Roles.findRoleByName(RegistryConstants.REGISTRY_ADMIN_ROLE);

    this.assignPermissions(mdVertex, raRole, false);
    this.assignPermissions(mdEdge, raRole, false);

    Roles rmRole = Roles.findRoleByName(RegistryConstants.REGISTRY_MAINTAINER_ROLE);

    this.assignPermissions(mdVertex, rmRole, false);
    this.assignPermissions(mdEdge, rmRole, false);

    Roles rcRole = Roles.findRoleByName(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE);

    this.assignPermissions(mdVertex, rcRole, false);
    this.assignPermissions(mdEdge, rcRole, false);

    Roles acRole = Roles.findRoleByName(RegistryConstants.API_CONSUMER_ROLE);

    this.assignPermissions(mdVertex, acRole, false);
    this.assignPermissions(mdEdge, acRole, false);
  }

  @Transaction
  public static ClassificationType apply(JsonObject json)
  {
    String oid = (json.has(MdClassificationInfo.OID) && !json.get(MdClassificationInfo.OID).isJsonNull()) ? json.get(MdClassificationInfo.OID).getAsString() : null;
    String code = json.get(DefaultAttribute.CODE.getName()).getAsString();
    LocalizedValue displayLabel = LocalizedValue.fromJSON(json.get(MdClassificationInfo.DISPLAY_LABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(json.get(MdClassificationInfo.DESCRIPTION).getAsJsonObject());

    return apply(oid, code, displayLabel, description);
  }

  public static ClassificationType apply(String oid, String code, LocalizedValue displayLabel, LocalizedValue description)
  {
    MdClassificationDAO mdClassification = null;

    if (oid != null)
    {
      mdClassification = (MdClassificationDAO) MdClassificationDAO.get(oid).getBusinessDAO();
    }
    else
    {

      if (!MasterList.isValidName(code))
      {
        throw new InvalidMasterListCodeException("The geo object type code has an invalid character");
      }

      mdClassification = MdClassificationDAO.newInstance();
      mdClassification.setValue(MdClassificationInfo.PACKAGE, RegistryConstants.CLASSIFICATION_PACKAGE);
      mdClassification.setValue(MdClassificationInfo.TYPE_NAME, code);
      mdClassification.setValue(MdClassificationInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    }

    LocalizedValueConverter.populate(mdClassification, MdClassificationInfo.DISPLAY_LABEL, displayLabel);

    LocalizedValueConverter.populate(mdClassification, MdClassificationInfo.DESCRIPTION, description);

    boolean isNew = mdClassification.isNew() && !mdClassification.isAppliedToDB();

    mdClassification.apply();

    ClassificationType classificationType = new ClassificationType(mdClassification);

    if (isNew)
    {
      // Assign permissions
      classificationType.assignPermissions();
    }

    return classificationType;
  }

  public static Page<ClassificationType> page(JsonObject criteria)
  {
    return new ClassificationTypePageQuery(criteria).getPage();
  }

  public static ClassificationType get(String oid)
  {
    return new ClassificationType((MdClassificationDAOIF) MdClassificationDAO.get(oid));
  }

  public static ClassificationType getByCode(String code)
  {
    String classificationType = RegistryConstants.CLASSIFICATION_PACKAGE + "." + code;

    MdClassificationDAOIF mdClassification = (MdClassificationDAOIF) MdClassificationDAO.get(MdClassificationInfo.CLASS, classificationType);

    return new ClassificationType(mdClassification);
  }

}
