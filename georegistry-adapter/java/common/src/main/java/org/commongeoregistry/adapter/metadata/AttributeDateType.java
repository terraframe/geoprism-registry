/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class AttributeDateType extends AttributePrimitiveType
{
  /**
   * 
   */
  private static final long serialVersionUID = 1543071656686171731L;
  public static String TYPE = "date";
  
  public AttributeDateType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);
  }
}
