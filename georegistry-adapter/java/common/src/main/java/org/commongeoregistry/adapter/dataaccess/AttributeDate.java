/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import java.util.Date;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class AttributeDate extends Attribute
{

  /**
   * 
   */
  private static final long serialVersionUID = 5532076653984789765L;

  private Date              date;

  public AttributeDate(String name)
  {
    super(name, AttributeDateType.TYPE);

    this.date = null;
  }

  @Override
  public void setValue(Object date)
  {
    this.setDate((Date) date);
  }

  public void setDate(Date date)
  {
    this.date = date;
  }

  @Override
  public Date getValue()
  {
    return this.date;
  }

  @Override
  public JsonElement toJSON(CustomSerializer serializer)
  {
    if (this.date != null)
    {
      return new JsonPrimitive(this.date.getTime());
    }
    else
    {
      return JsonNull.INSTANCE;
    }
  }

  @Override
  public void fromJSON(JsonElement jValue, RegistryAdapter registry)
  {
    long epoch = jValue.getAsLong();
    this.setValue(new Date(epoch));
  }
}