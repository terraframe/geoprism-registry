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
import net.geoprism.registry.io.PostalCodeFormatException;

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

    public GeoObjectType getType()
    {
      return ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(this.typeCode).get();
    }

    protected Location location(ShapefileFunction function)
    {
      GeoObjectType type = getType();
      Universal universal = ServiceFactory.getConversionService().geoObjectTypeToUniversal(type);

      return new Location(type, universal, function);

    }

    protected void formatException(String value)
    {
      PostalCodeFormatException e = new PostalCodeFormatException();
      e.setCode(value);
      e.setTypeLabel(this.getType().getLabel().getValue());

      throw e;
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
      DelegateShapefileFunction delegate = new DelegateShapefileFunction(function)
      {
        @Override
        public Object getValue(FeatureRow feature)
        {
          String code = (String) super.getValue(feature);
          String value = "855 ";

          if (code != null)
          {
            if (getTypeCode().equals("Cambodia_Province"))
            {
              if (code.length() < 2)
              {
                formatException(code);
              }

              value += code.substring(0, 2);
            }
            else if (getTypeCode().equals("Cambodia_District"))
            {
              if (code.length() < 4)
              {
                formatException(code);
              }

              value += code.substring(0, 4);
            }
            else if (getTypeCode().equals("Cambodia_Commune"))
            {
              if (code.length() < 6)
              {
                formatException(code);
              }

              value += code.substring(0, 6);
            }
            else if (getTypeCode().equals("Cambodia_Village"))
            {
              if (code.length() < 9)
              {
                formatException(code);
              }

              value += code.substring(0, 9);
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
