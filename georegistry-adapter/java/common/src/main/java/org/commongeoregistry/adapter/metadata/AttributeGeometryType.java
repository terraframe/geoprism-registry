/**
 *
 */
package org.commongeoregistry.adapter.metadata;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

public class AttributeGeometryType extends AttributeType
{

  private static final long serialVersionUID = -3229658040745431777L;
  
  public static String       TYPE             = "geometry";
  
  public AttributeGeometryType(String _name, LocalizedValue _label, LocalizedValue _description, boolean _isDefault, boolean _required, boolean _unique)
  {
    super(_name, _label, _description, TYPE, _isDefault, _required, _unique);
  }
  
}
