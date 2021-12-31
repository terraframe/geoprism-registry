package net.geoprism.registry;

import java.util.List;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdAttribute;

public class ListTypeAttributeGroup extends ListTypeAttributeGroupBase
{
  private static final long serialVersionUID = 862878064;
  
  public ListTypeAttributeGroup()
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

  public static void create(ListTypeVersion version, MdAttribute source, MdAttribute target)
  {
    ListTypeAttributeGroup group = new ListTypeAttributeGroup();
    group.setVersion(version);
    group.setSourceAttribute(source);
    group.setTargetAttribute(target);
    group.apply();
  }

  public static void deleteAll(ListTypeVersion version)
  {
    ListTypeAttributeGroupQuery query = new ListTypeAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    OIterator<? extends ListTypeAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends ListTypeAttributeGroup> groups = it.getAll();

      for (ListTypeAttributeGroup group : groups)
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
    ListTypeAttributeGroupQuery query = new ListTypeAttributeGroupQuery(new QueryFactory());
    query.WHERE(query.getTargetAttribute().EQ(mdAttribute.getOid()));
    query.OR(query.getSourceAttribute().EQ(mdAttribute.getOid()));

    OIterator<? extends ListTypeAttributeGroup> it = query.getIterator();

    try
    {
      List<? extends ListTypeAttributeGroup> groups = it.getAll();

      for (ListTypeAttributeGroup group : groups)
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
