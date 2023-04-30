package net.geoprism.registry.action;

public class GraphHasVertex extends GraphHasVertexBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 412125081;
  
  public GraphHasVertex(String parentOid, String childOid)
  {
    super(parentOid, childOid);
  }
  
  public GraphHasVertex(net.geoprism.registry.LabeledPropertyGraphTypeVersion parent, com.runwaysdk.system.metadata.MdVertex child)
  {
    this(parent.getOid(), child.getOid());
  }
  
}
