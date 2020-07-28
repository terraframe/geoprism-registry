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
