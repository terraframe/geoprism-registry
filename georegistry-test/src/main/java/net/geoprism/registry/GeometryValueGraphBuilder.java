package net.geoprism.registry;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.locationtech.jts.geom.Geometry;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.spatial.shape.OShapeFactory;

public class GeometryValueGraphBuilder extends AbstractGraphBuilder
{
  public GeometryValueGraphBuilder(String path)
  {
    super(path);
  }

  protected void addAttribute(OVertex vertex, String name, String type, Object value)
  {
    for (int i = 0; i < this.years.length; i++)
    {
      int start = this.years[i];
      int end = ( i + 1 ) < this.years.length ? this.years[i + 1] : 2024;

      Calendar startDateCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      startDateCal.clear();
      startDateCal.set(start, Calendar.JANUARY, 1);

      Calendar endDateCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      endDateCal.clear();
      endDateCal.set(end, Calendar.JANUARY, 1);
      endDateCal.add(Calendar.SECOND, -1);

      Date startDate = startDateCal.getTime();
      Date endDate = endDateCal.getTime();

      OVertex attributeValue = db.newVertex(type);
      attributeValue.setProperty("startDate", startDate);
      attributeValue.setProperty("endDate", endDate);
      attributeValue.setProperty("name", name);

      if (type.equals("AttributeLocaleValue"))
      {
        attributeValue.setProperty("defaultLocale", value + "_" + i);
        attributeValue.setProperty("en", value + "_EN" + "_" + i);
      }
      else if (type.equals("AttributeMultiPolygon"))
      {
        attributeValue.setProperty("value", OShapeFactory.INSTANCE.toDoc((Geometry) value));
      }
      else
      {
        attributeValue.setProperty("value", value);
      }

      attributeValue = attributeValue.save();

      String edgeName = type.equals("AttributeMultiPolygon") ?  "ObjectHasGeometry" : "ObjectHasValue";
      vertex.addEdge(attributeValue, edgeName).save();
    }

  }

  protected void createSchema()
  {
    String parentName = "G_OBJ_NODE";

    OClass attributeValue = this.db.createVertexClass("AttributeValue");
    attributeValue.createProperty("startDate", OType.DATETIME).setMandatory(true);
    attributeValue.createProperty("endDate", OType.DATETIME).setMandatory(true);
    attributeValue.createProperty("name", OType.STRING).setMandatory(true);

    OClass oClass = this.db.createVertexClass("AttributeLocaleValue");
    oClass.addSuperClass(attributeValue);
    oClass.createProperty("defaultLocale", OType.STRING);
    oClass.createProperty("end", OType.STRING);

    oClass = this.db.createVertexClass("AttributeString");
    oClass.addSuperClass(attributeValue);
    oClass.createProperty("value", OType.STRING);

    oClass = this.db.createVertexClass("AttributeLong");
    oClass.addSuperClass(attributeValue);
    oClass.createProperty("value", OType.LONG);

    oClass = this.db.createVertexClass("AttributeMultiPolygon");
    oClass.addSuperClass(attributeValue);
    oClass.createProperty("value", OType.EMBEDDED, db.getClass("OMultiPolygon"));

    oClass = this.db.createEdgeClass("ObjectHasValue");
    oClass = this.db.createEdgeClass("ObjectHasGeometry");

    oClass = this.db.createEdgeClass("ObjectHasParent");
    oClass.createProperty("startDate", OType.DATETIME);
    oClass.createProperty("endDate", OType.DATETIME);

    OClass parent = this.db.createVertexClass(parentName);

    for (Type type : types)
    {
      createVertexClass(parent, type);
    }
  }

  public static void main(String[] args)
  {
    new GeometryValueGraphBuilder(args[0]).run();
  }
}
