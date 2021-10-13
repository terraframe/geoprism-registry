package net.geoprism.registry.etl.fhir;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.Base64.Decoder;

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Location.LocationPositionComponent;
import org.hl7.fhir.r4.model.Organization;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;

import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public class BasicFhirResourceProcessor extends AbstractFhirResourceProcessor implements FhirResourceProcessor
{
  @Override
  public String getLabel()
  {
    return "Basic Resource processor";
  }

  @Override
  protected void populate(ServerGeoObjectIF geoObject, Location location, Date lastUpdated)
  {
    if (lastUpdated == null)
    {
      lastUpdated = new Date();
    }

    LocalizedValue value = LocalizedValue.createEmptyLocalizedValue();
    value.setValue(LocalizedValue.DEFAULT_LOCALE, location.getName());

    geoObject.setDisplayLabel(value, lastUpdated, ValueOverTime.INFINITY_END_DATE);
    geoObject.setExists(true, lastUpdated, ValueOverTime.INFINITY_END_DATE);
  }

  @Override
  protected String getType(Organization organization)
  {
    Coding coding = organization.getTypeFirstRep().getCodingFirstRep();

    if (coding != null)
    {
      String code = coding.getCode();

      if (code != null)
      {
        return code;
      }
    }

    // TODO Throw new exception type
    throw new ProgrammingErrorException("Unable to derive the CGR GeoObject-Type for the organization [" + organization.getId() + "]");
  }

  @Override
  protected String getType(Location location)
  {
    String system = getSystem().getSystem();
    Optional<CodeableConcept> type = location.getType().stream().filter(t -> t.getCodingFirstRep().getSystem().equals(system)).findFirst();

    if (type.isPresent())
    {
      String code = type.get().getCodingFirstRep().getCode();

      if (code != null)
      {
        return code;
      }
    }

    // TODO Throw new exception type
    throw new ProgrammingErrorException("Unable to derive the CGR GeoObject-Type for the location [" + location.getId() + "]");
  }

  @Override
  protected Identifier getIdentifier(Organization organization)
  {
    return organization.getIdentifier().stream().filter(i -> i.getSystem().equals(this.getSystem().getSystem())).findFirst().orElse(null);
  }

  @Override
  protected Identifier getIdentifier(Location location)
  {
    return location.getIdentifier().stream().filter(i -> i.getSystem().equals(this.getSystem().getSystem())).findFirst().orElse(null);
  }

  @Override
  protected Geometry getGeometry(Location location, ServerGeoObjectType type)
  {
    if (type.getGeometryType().equals(GeometryType.MULTIPOINT))
    {
      LocationPositionComponent position = location.getPosition();

      if (position != null)
      {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        BigDecimal latitude = position.getLatitude();
        BigDecimal longitude = position.getLongitude();

        if (latitude != null && longitude != null)
        {
          Point point = factory.createPoint(new Coordinate(longitude.doubleValue(), latitude.doubleValue()));

          return factory.createMultiPoint(new Point[] { point });
        }
      }
    }

    return super.getGeometry(location, type);
  }
}
