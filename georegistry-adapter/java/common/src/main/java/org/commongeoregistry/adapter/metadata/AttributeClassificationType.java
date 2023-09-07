/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AttributeClassificationType extends AttributeType
{
  /**
   * 
   */
  private static final long  serialVersionUID         = 6431580798592645011L;

  public static final String JSON_ROOT_TERM           = "rootTerm";

  public static final String JSON_CLASSIFICATION_TYPE = "classificationType";

  public static String       TYPE                     = "classification";

  private Term               rootTerm                 = null;

  private String             classificationType       = null;

  public AttributeClassificationType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);
  }

  public Term getRootTerm()
  {
    return this.rootTerm;
  }

  public void setRootTerm(Term rootTerm)
  {
    this.rootTerm = rootTerm;
  }

  public String getClassificationType()
  {
    return classificationType;
  }

  public void setClassificationType(String classificationType)
  {
    this.classificationType = classificationType;
  }

  @Override
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject json = super.toJSON(serializer);
    json.addProperty(JSON_CLASSIFICATION_TYPE, this.getClassificationType());

    if (this.rootTerm != null)
    {
      json.add(JSON_ROOT_TERM, this.getRootTerm().toJSON());
    }

    return json;
  }

  /**
   * Populates any additional attributes from JSON that were not populated in
   * {@link GeoObjectType#fromJSON(String, org.commongeoregistry.adapter.RegistryAdapter)}
   * 
   * @param attrObj
   * @return {@link AttributeType}
   */
  @Override
  public void fromJSON(JsonObject attrObj)
  {
    super.fromJSON(attrObj);

    this.setClassificationType(attrObj.get(AttributeClassificationType.JSON_CLASSIFICATION_TYPE).getAsString());

    JsonElement termElement = attrObj.get(AttributeClassificationType.JSON_ROOT_TERM);

    if (termElement != null && !termElement.isJsonNull())
    {
      Term rootTerm = Term.fromJSON(termElement.getAsJsonObject());

      if (rootTerm != null)
      {
        this.setRootTerm(rootTerm);
      }
    }
  }

  @Override
  public void validate(Object _value)
  {
  }

}
