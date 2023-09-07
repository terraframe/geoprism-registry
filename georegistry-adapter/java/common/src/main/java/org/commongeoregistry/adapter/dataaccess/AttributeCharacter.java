/**
 *
 */
package org.commongeoregistry.adapter.dataaccess;

import org.commongeoregistry.adapter.metadata.AttributeCharacterType;

public class AttributeCharacter extends Attribute
{

  /**
   * 
   */
  private static final long serialVersionUID = -506321096607959557L;

  private String            value;

  public AttributeCharacter(String name)
  {
    super(name, AttributeCharacterType.TYPE);

    this.value = null;
  }

  @Override
  public void setValue(Object value)
  {
    this.setText((String) value);
  }

  public void setText(String value)
  {
    this.value = value;
  }

  @Override
  public String getValue()
  {
    return this.value;
  }

}
