package net.geoprism.registry.task;

import com.runwaysdk.system.Roles;

public class TaskHasRole extends TaskHasRoleBase
{
  private static final long serialVersionUID = 1908834263;
  
  public TaskHasRole(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public TaskHasRole(Task parent, Roles child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
