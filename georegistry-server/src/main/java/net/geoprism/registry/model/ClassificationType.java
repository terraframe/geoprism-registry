package net.geoprism.registry.model;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdClassificationDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.AbstractClassification;

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

  public String getCode()
  {
    return this.mdClassification.getValue(MdClassificationInfo.TYPE_NAME);
  }

  public String getOid()
  {
    return this.mdClassification.getOid();
  }

  public LocalizedValue getDisplayLabel()
  {
    return LocalizedValueConverter.convert(this.mdClassification.getDisplayLabels());
  }

  @Transaction
  public void delete()
  {
    this.mdClassification.getBusinessDAO().delete();
  }

  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty(MdClassificationInfo.OID, getOid());
    object.addProperty(MdClassificationInfo.TYPE, this.mdClassification.definesType());
    object.addProperty(DefaultAttribute.CODE.getName(), this.getCode());
    object.add(MdClassificationInfo.DISPLAY_LABEL, this.getDisplayLabel().toJSON());

    String rootOid = this.mdClassification.getValue(MdClassificationInfo.ROOT);

    if (rootOid != null && rootOid.length() > 0)
    {
      VertexObject root = VertexObject.get(this.mdClassification.getReferenceMdVertexDAO(), rootOid);

      object.add(MdClassificationInfo.ROOT, this.toJSON(root));
    }

    return object;
  }

  private JsonObject toJSON(VertexObject root)
  {
    LocalizedValue displayLabel = LocalizedValueConverter.convert(root.getEmbeddedComponent(MdClassificationInfo.DISPLAY_LABEL));

    JsonObject object = new JsonObject();
    object.add(AbstractClassification.CODE, root.getObjectValue(AbstractClassification.CODE));
    object.add(MdClassificationInfo.DISPLAY_LABEL, displayLabel.toJSON());

    return object;
  }

  @Transaction
  public JsonObject createRootNode(JsonObject json)
  {
    String code = json.get(AbstractClassification.CODE).getAsString();
    LocalizedValue displayLabel = LocalizedValue.fromJSON(json.get("displayLabel").getAsJsonObject());

    MdVertexDAOIF referenceMdVertexDAO = mdClassification.getReferenceMdVertexDAO();

    VertexObject root = new VertexObject(referenceMdVertexDAO.definesType());
    root.setValue(AbstractClassification.CODE, code);
    LocalizedValueConverter.populate(root, MdClassificationInfo.DISPLAY_LABEL, displayLabel);
    root.apply();

    MdClassificationDAO mdClassification = (MdClassificationDAO) this.mdClassification.getBusinessDAO();
    mdClassification.setValue(MdClassificationInfo.ROOT, root.getOid());
    mdClassification.apply();

    return toJSON(root);
  }

  @Transaction
  public static ClassificationType apply(JsonObject json)
  {
    ClassificationType classificationType = new ClassificationType();

    if (json.has(MdClassificationInfo.OID) && !json.get(MdClassificationInfo.OID).isJsonNull())
    {
      String oid = json.get(MdClassificationInfo.OID).getAsString();
      classificationType.setMdClassification((MdClassificationDAOIF) MdClassificationDAO.get(oid));
    }
    else
    {
      String code = json.get(DefaultAttribute.CODE.getName()).getAsString();

      MdClassificationDAO mdClassification = MdClassificationDAO.newInstance();
      mdClassification.setValue(MdClassificationInfo.PACKAGE, RegistryConstants.CLASSIFICATION_PACKAGE);
      mdClassification.setValue(MdClassificationInfo.TYPE_NAME, code);
      mdClassification.setValue(MdClassificationInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);

      classificationType.setMdClassification(mdClassification);
    }

    MdClassificationDAO mdClassification = (MdClassificationDAO) classificationType.getMdClassification().getBusinessDAO();

    LocalizedValue displayLabel = LocalizedValue.fromJSON(json.get(MdClassificationInfo.DISPLAY_LABEL).getAsJsonObject());
    LocalizedValueConverter.populate(mdClassification, MdClassificationInfo.DISPLAY_LABEL, displayLabel);

    mdClassification.apply();

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

}
