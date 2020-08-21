/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.permission;

import com.runwaysdk.business.rbac.Operation;

public enum PermissionContext {
  READ(Operation.READ), WRITE(Operation.WRITE);

  private Operation operation;

  private PermissionContext(Operation operation)
  {
    this.operation = operation;
  }

  public Operation getOperation()
  {
    return operation;
  }

  public static PermissionContext get(String context)
  {
    if (context != null && context.length() > 0)
    {
      return PermissionContext.valueOf(context);
    }

    return PermissionContext.READ;
  }
}
