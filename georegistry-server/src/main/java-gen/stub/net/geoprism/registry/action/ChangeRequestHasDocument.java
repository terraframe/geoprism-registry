package net.geoprism.registry.action;

public class ChangeRequestHasDocument extends ChangeRequestHasDocumentBase
{
  private static final long serialVersionUID = -654782107;
  
  public ChangeRequestHasDocument(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ChangeRequestHasDocument(net.geoprism.registry.action.ChangeRequest parent, com.runwaysdk.system.VaultFile child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
