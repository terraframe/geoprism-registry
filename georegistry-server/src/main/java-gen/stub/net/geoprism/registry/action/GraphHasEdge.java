package net.geoprism.registry.action;

public class GraphHasEdge extends GraphHasEdgeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 575350155;
  
  public GraphHasEdge(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public GraphHasEdge(net.geoprism.registry.LabeledPropertyGraphTypeVersion parent, com.runwaysdk.system.metadata.MdEdge child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
