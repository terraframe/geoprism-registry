/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class AttributeBooleanType extends AttributeType
{
  /**
   * 
   */
  private static final long serialVersionUID = -6889939609956215822L;

  public static String      TYPE             = "boolean";

  public AttributeBooleanType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);
  }
}
