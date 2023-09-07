/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public abstract class AttributePrimitiveType extends AttributeType
{
  /**
   * 
   */
  private static final long serialVersionUID = 7553432124777528154L;

  public AttributePrimitiveType(String _name, LocalizedValue _label, LocalizedValue _description, String _type, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, _type, _isDefault, _required, _unique);
  }
}
