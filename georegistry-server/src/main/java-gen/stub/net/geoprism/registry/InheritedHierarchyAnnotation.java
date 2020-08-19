package net.geoprism.registry;

import java.util.List;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

public class InheritedHierarchyAnnotation extends InheritedHierarchyAnnotationBase
{
  private static final long serialVersionUID = -918188612;

  public InheritedHierarchyAnnotation()
  {
    super();
  }

  public static List<? extends InheritedHierarchyAnnotation> getByUniversal(Universal universal)
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      return iterator.getAll();
    }
  }

  public static List<? extends InheritedHierarchyAnnotation> getByRelationship(MdTermRelationship mdRelationship)
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.WHERE(query.getInheritedHierarchy().EQ(mdRelationship));
    query.OR(query.getForHierarchy().EQ(mdRelationship));

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      return iterator.getAll();
    }
  }

  public static InheritedHierarchyAnnotation get(Universal universal, MdTermRelationship forRelationship)
  {
    InheritedHierarchyAnnotationQuery query = new InheritedHierarchyAnnotationQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));
    query.AND(query.getForHierarchy().EQ(forRelationship));

    try (OIterator<? extends InheritedHierarchyAnnotation> iterator = query.getIterator())
    {
      List<? extends InheritedHierarchyAnnotation> list = iterator.getAll();

      if (list.size() > 0)
      {
        return list.get(0);
      }

      return null;
    }
  }
}
