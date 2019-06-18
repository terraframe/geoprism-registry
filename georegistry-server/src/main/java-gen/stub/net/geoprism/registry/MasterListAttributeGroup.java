package net.geoprism.registry;

import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdAttribute;

public class MasterListAttributeGroup extends MasterListAttributeGroupBase
{
  private static final long serialVersionUID = 314069411;

  public MasterListAttributeGroup()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    if (this.getTargetAttributeOid() != null && this.getTargetAttributeOid().length() > 0)
    {
      return this.getTargetAttributeOid();
    }

    return super.buildKey();
  }

  public static void create(MasterList masterList, MdAttribute source, MdAttribute target)
  {
    MasterListAttributeGroup group = new MasterListAttributeGroup();
    group.setMasterList(masterList);
    group.setSourceAttribute(source);
    group.setTargetAttribute(target);
    group.apply();
  }

  public static void deleteAll(MasterList masterList)
  {
    MasterListAttributeGroupQuery query = new MasterListAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getMasterList().EQ(masterList));

    OIterator<? extends MasterListAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends MasterListAttributeGroup> groups = it.getAll();

      for (MasterListAttributeGroup group : groups)
      {
        group.delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public static void remove(MdAttributeConcreteDAOIF mdAttribute)
  {
    MasterListAttributeGroupQuery query = new MasterListAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getTargetAttribute().EQ(mdAttribute.getOid()));
    query.OR(query.getSourceAttribute().EQ(mdAttribute.getOid()));

    OIterator<? extends MasterListAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends MasterListAttributeGroup> groups = it.getAll();

      for (MasterListAttributeGroup group : groups)
      {
        group.delete();
      }
    }
    finally
    {
      it.close();
    }
  }

}
