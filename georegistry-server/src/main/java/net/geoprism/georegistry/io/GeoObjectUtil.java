package net.geoprism.georegistry.io;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.georegistry.service.ServiceFactory;

public class GeoObjectUtil
{
  @SuppressWarnings("unchecked")
  public static String convertToTermString(Object value)
  {
    Collection<Term> terms = (Collection<Term>) value;
    StringBuilder builder = new StringBuilder();

    boolean first = true;

    for (Term term : terms)
    {
      if (!first)
      {
        builder.append(",");
      }

      builder.append(term.getLocalizedLabel());
      first = false;
    }

    return builder.toString();
  }

  public static List<GeoObject> getObjects(GeoObjectType type)
  {
    List<GeoObject> objects = new LinkedList<>();

    Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(type);

    GeoEntityQuery query = new GeoEntityQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));
    query.ORDER_BY_ASC(query.getGeoId());

    OIterator<? extends GeoEntity> it = query.getIterator();

    try
    {
      List<? extends GeoEntity> entities = it.getAll();

      for (GeoEntity entity : entities)
      {
        objects.add(ServiceFactory.getUtilities().getGeoObjectByCode(entity.getGeoId(), type.getCode()));
      }
    }
    finally
    {
      it.close();
    }

    return objects;
  }
}
