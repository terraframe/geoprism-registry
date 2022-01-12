package net.geoprism.registry.query;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.Business;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.session.Session;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.JsonWrapper;

public class ListTypeVersionPageQuery extends AbstractBusinessPageQuery<JsonSerializable>
{
  private ListTypeVersion                          version;

  private SimpleDateFormat                         format;

  private NumberFormat                             numberFormat;

  private Boolean                                  includeGeometries;

  private List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  public ListTypeVersionPageQuery(ListTypeVersion version, JsonObject criteria, Boolean includeGeometries)
  {
    super(MdBusinessDAO.get(version.getMdBusinessOid()), criteria);

    this.version = version;
    this.includeGeometries = includeGeometries;
    this.format = new SimpleDateFormat("yyyy-MM-dd");
    this.format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    this.numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());
    this.mdAttributes = this.getMdBusiness().definesAttributes();
  }

  @Override
  protected List<JsonSerializable> getResults(List<? extends Business> results)
  {

    return results.stream().map(row -> {
      JsonObject object = new JsonObject();

      MdAttributeConcreteDAOIF mdGeometry = this.getMdBusiness().definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

      if (includeGeometries)
      {
        Geometry geom = (Geometry) row.getObjectValue(mdGeometry.definesAttribute());

        if (geom != null)
        {
          GeoJSONWriter gw = new GeoJSONWriter();
          org.wololo.geojson.Geometry gJSON = gw.write(geom);

          JsonObject geojson = JsonParser.parseString(gJSON.toString()).getAsJsonObject();

          object.add("geometry", geojson);
        }
      }
      object.addProperty(ListTypeVersion.ORIGINAL_OID, row.getValue(ListTypeVersion.ORIGINAL_OID));
      object.addProperty(DefaultAttribute.UID.getName(), row.getValue(DefaultAttribute.UID.getName()));

      for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
      {
        if (this.version.isValid(mdAttribute))
        {
          String attributeName = mdAttribute.definesAttribute();
          Object value = row.getObjectValue(attributeName);

          if (value != null)
          {

            if (value instanceof Double)
            {
              object.addProperty(mdAttribute.definesAttribute(), numberFormat.format((Double) value));
            }
            else if (value instanceof Number)
            {
              object.addProperty(mdAttribute.definesAttribute(), (Number) value);
            }
            else if (value instanceof Boolean)
            {
              object.addProperty(mdAttribute.definesAttribute(), (Boolean) value);
            }
            else if (value instanceof String)
            {
              object.addProperty(mdAttribute.definesAttribute(), (String) value);
            }
            else if (value instanceof Character)
            {
              object.addProperty(mdAttribute.definesAttribute(), (Character) value);
            }
            else if (value instanceof Date)
            {
              object.addProperty(mdAttribute.definesAttribute(), format.format((Date) value));
            }
          }
        }
      }

      return new JsonWrapper(object);

    }).collect(Collectors.toList());
  }

}
