/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class AttributeStructType extends AttributeType
{
  /**
   * 
   */
  private static final long serialVersionUID = -4241500416669749156L;

  public AttributeStructType(String _name, LocalizedValue _label, LocalizedValue _description, String _type, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, _type, _isDefault, _required, _unique);
  }
}
