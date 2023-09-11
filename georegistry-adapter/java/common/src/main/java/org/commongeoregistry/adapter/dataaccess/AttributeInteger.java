/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class AttributeInteger extends Attribute
{

  /**
   * 
   */
  private static final long serialVersionUID = -2116815892488790274L;
  
  private Long integer;
  
  public AttributeInteger(String name)
  {
    super(name, AttributeIntegerType.TYPE);
    
    this.integer = null;
  }
  
  @Override
  public void setValue(Object integer)
  {
    this.setInteger((Long)integer);
  }
  
  public void setInteger(Long integer)
  {
    this.integer = integer;
  }
  
  @Override
  public Long getValue()
  {
    return this.integer;
  }
  
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    if (!(jValue instanceof JsonNull))
    {
      this.setValue(jValue.getAsLong());
    }
  }

}
