/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
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
