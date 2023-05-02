package net.geoprism.registry.action;

public class GraphHasVertex extends GraphHasVertexBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1078305724;
  
  public GraphHasVertex(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public GraphHasVertex(net.geoprism.registry.LabeledPropertyGraphTypeVersion parent, net.geoprism.registry.LabeledPropertyGraphVertex child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
