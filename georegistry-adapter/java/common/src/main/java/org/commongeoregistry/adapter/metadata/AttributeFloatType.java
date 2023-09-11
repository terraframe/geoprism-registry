/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;

public class AttributeFloatType extends AttributeNumericType
{

  public static final String JSON_PRECISION   = "precision";

  public static final String JSON_SCALE       = "scale";

  /**
   * 
   */
  private static final long  serialVersionUID = -2000724524967535694L;

  public static String       TYPE             = "float";

  private int                precision;

  private int                scale;

  public AttributeFloatType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);

    this.precision = 32;
    this.scale = 8;
  }

  public int getPrecision()
  {
    return precision;
  }

  public void setPrecision(int precision)
  {
    this.precision = precision;
  }

  public int getScale()
  {
    return scale;
  }

  public void setScale(int scale)
  {
    this.scale = scale;
  }

  @Override
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject object = super.toJSON(serializer);
    object.addProperty(JSON_PRECISION, this.precision);
    object.addProperty(JSON_SCALE, this.scale);

    return object;
  }

  @Override
  public void fromJSON(JsonObject attrObj)
  {
    super.fromJSON(attrObj);

    this.precision = attrObj.get(JSON_PRECISION).getAsInt();
    this.scale = attrObj.get(JSON_SCALE).getAsInt();
  }

}
