package net.geoprism.registry;

public class LabeledPropertyGraphEdge extends LabeledPropertyGraphEdgeBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -1391888631;
  
  public LabeledPropertyGraphEdge()
  {
    super();
  }
  
  @Override
  public void delete()
  {
    super.delete();
    
    this.getGraphMdEdge().delete();
  }
}
