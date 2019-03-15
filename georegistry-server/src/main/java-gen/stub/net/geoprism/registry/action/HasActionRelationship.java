package net.geoprism.registry.action;

import net.geoprism.registry.action.HasActionRelationshipBase;

public class HasActionRelationship extends HasActionRelationshipBase
{
  private static final long serialVersionUID = 1581219958;
  
  public HasActionRelationship(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public HasActionRelationship(net.geoprism.registry.action.ChangeRequest parent, net.geoprism.registry.action.AbstractAction child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
