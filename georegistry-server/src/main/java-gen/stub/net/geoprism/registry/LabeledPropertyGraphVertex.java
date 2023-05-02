package net.geoprism.registry;

import com.runwaysdk.system.metadata.MdVertex;

public class LabeledPropertyGraphVertex extends LabeledPropertyGraphVertexBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1712281520;

  public LabeledPropertyGraphVertex()
  {
    super();
  }

  @Override
  public void delete()
  {
    MdVertex vertex = this.getGraphMdVertex();

    super.delete();

    if (vertex != null)
    {
      vertex.delete();
    }
  }
}
