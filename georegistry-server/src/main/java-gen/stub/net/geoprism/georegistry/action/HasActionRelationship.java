package net.geoprism.georegistry.action;

public class HasActionRelationship extends HasActionRelationshipBase
{
  private static final long serialVersionUID = 1581219958;
  
  public HasActionRelationship(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public HasActionRelationship(net.geoprism.georegistry.action.ChangeRequest parent, net.geoprism.georegistry.action.AbstractAction child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
