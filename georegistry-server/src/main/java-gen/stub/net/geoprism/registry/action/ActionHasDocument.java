package net.geoprism.registry.action;

public class ActionHasDocument extends ActionHasDocumentBase
{
  private static final long serialVersionUID = 970609275;
  
  public ActionHasDocument(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public ActionHasDocument(net.geoprism.registry.action.AbstractAction parent, com.runwaysdk.system.VaultFile child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
