package net.geoprism.registry.action;

public class GraphHasEdge extends GraphHasEdgeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -134390838;
  
  public GraphHasEdge(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public GraphHasEdge(net.geoprism.registry.LabeledPropertyGraphTypeVersion parent, net.geoprism.registry.LabeledPropertyGraphEdge child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
