/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public abstract class AttributeNumericType extends AttributePrimitiveType
{
  /**
   * 
   */
  private static final long serialVersionUID = 5572144593795191683L;

  public AttributeNumericType(String _name, LocalizedValue _label, LocalizedValue _description, String _type, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, _type, _isDefault, _required, _unique);
  }
}
