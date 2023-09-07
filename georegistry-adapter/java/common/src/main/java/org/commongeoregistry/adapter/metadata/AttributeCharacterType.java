/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class AttributeCharacterType extends AttributePrimitiveType
{
  /**
   * 
   */
  private static final long serialVersionUID = -4241500416669749156L;

  public static String      TYPE             = "character";

  public AttributeCharacterType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);
  }
}
