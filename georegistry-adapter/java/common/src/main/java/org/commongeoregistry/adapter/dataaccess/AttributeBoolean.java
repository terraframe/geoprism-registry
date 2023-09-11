/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class AttributeBoolean extends Attribute
{
  /**
   * 
   */
  private static final long serialVersionUID = -3802068636170892383L;
  
  private Boolean            value;

  public AttributeBoolean(String name)
  {
    super(name, AttributeBooleanType.TYPE);

    this.value = null;
  }

  @Override
  public void setValue(Object value)
  {
    this.setBoolean((Boolean) value);
  }

  public void setBoolean(Boolean value)
  {
    this.value = value;
  }

  @Override
  public Boolean getValue()
  {
    return this.value;
  }

  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    this.setValue(Boolean.valueOf(jValue.getAsString()));
  }
  
  @Override
  public JsonElement toJSON(CustomSerializer serializer)
  {
    Boolean value = this.getValue();
    
    if (value == null)
    {
      return JsonNull.INSTANCE;
    }

    return new JsonPrimitive(value);
  }
  
}
