/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class AttributeIntegerType extends AttributeNumericType
{
  /**
   * 
   */
  private static final long serialVersionUID = -8395438752409839660L;

  public static String      TYPE             = "integer";

  public AttributeIntegerType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);
  }
}
