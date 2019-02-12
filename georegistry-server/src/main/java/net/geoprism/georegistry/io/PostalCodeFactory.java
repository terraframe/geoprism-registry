package net.geoprism.georegistry.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.georegistry.service.ServiceFactory;

public class PostalCodeFactory
{
  public static abstract class AbstractLocationBuilder implements LocationBuilder
  {
    private String typeCode;

    public AbstractLocationBuilder(String typeCode)
    {
      super();
      this.typeCode = typeCode;
    }

    public String getTypeCode()
    {
      return typeCode;
    }

    protected Location location(ShapefileFunction function)
    {
      GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(this.typeCode).get();
      Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(type);

      return new Location(type, universal, function);

    }
  }

  /**
   * Class for parsing out a Cambodia postal code and converting it to the code
   * the system expects for a given type. The Cambodia postal code is defined as
   * the first 2 characters represent the province, the next 2 characters
   * represent the district, the next 2 characters represent the commune, and
   * the final 3 characters represent the village.
   * 
   * @author admin
   */
  public static class CambodiaBuilder extends AbstractLocationBuilder implements LocationBuilder
  {
    public CambodiaBuilder(String typeCode)
    {
      super(typeCode);
    }

    @Override
    @Request
    public Location build(final ShapefileFunction function)
    {
      AbstractShapefileFunction delegate = new AbstractShapefileFunction()
      {
        @Override
        public Object getValue(FeatureRow feature)
        {
          String code = (String) function.getValue(feature);
          String value = "855 ";

          if (code != null)
          {
            if (getTypeCode().equals("Cambodia_Province"))
            {
              value += code.subSequence(0, 2);
            }
            else if (getTypeCode().equals("Cambodia_District"))
            {
              value += code.subSequence(0, 4);
            }
            else if (getTypeCode().equals("Cambodia_Commune"))
            {
              value += code.subSequence(0, 6);
            }
            else if (getTypeCode().equals("Cambodia_Village"))
            {
              value += code.subSequence(0, 9);
            }
          }

          return value.trim();
        }
      };

      return this.location(delegate);
    }
  }

  private static final Map<String, LocationBuilder> locations = Collections.synchronizedMap(new HashMap<String, LocationBuilder>());

  static
  {
    locations.put("Cambodia_Province", new CambodiaBuilder("Cambodia"));
    locations.put("Cambodia_District", new CambodiaBuilder("Cambodia_Province"));
    locations.put("Cambodia_Commune", new CambodiaBuilder("Cambodia_District"));
    locations.put("Cambodia_Village", new CambodiaBuilder("Cambodia_Commune"));
  }

  public static boolean isAvailable(GeoObjectType type)
  {
    return locations.containsKey(type.getCode());
  }

  public static LocationBuilder get(GeoObjectType type)
  {
    return locations.get(type.getCode());
  }

  public static void remove(GeoObjectType type)
  {
    locations.remove(type.getCode());
  }

  public static void addPostalCode(GeoObjectType type, LocationBuilder builder)
  {
    locations.put(type.getCode(), builder);
  }
}
