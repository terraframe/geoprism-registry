package net.geoprism.registry.masterlist;

import java.util.Comparator;
import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

public class MasterListAttributeComparator implements Comparator<MdAttributeConcreteDAOIF>
{
  private List<String> order;

  public MasterListAttributeComparator(List<String> order, List<? extends MdAttributeConcreteDAOIF> attributes)
  {
    this.order = order;

    for (MdAttributeConcreteDAOIF attribute : attributes)
    {
      if (!this.order.contains(attribute.definesAttribute()))
      {
        this.order.add(attribute.definesAttribute());
      }
    }
  }

  {
    // TODO Auto-generated constructor stub
  }

  @Override
  public int compare(MdAttributeConcreteDAOIF o1, MdAttributeConcreteDAOIF o2)
  {
    Integer i1 = this.order.indexOf(o1.definesAttribute());
    Integer i2 = this.order.indexOf(o2.definesAttribute());

    return i1.compareTo(i2);
  }

}
