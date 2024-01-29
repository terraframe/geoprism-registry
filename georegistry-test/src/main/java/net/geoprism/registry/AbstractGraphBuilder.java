package net.geoprism.registry;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OEdge;
import com.orientechnologies.orient.core.record.OVertex;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.session.Request;

public abstract class AbstractGraphBuilder implements Runnable
{
  protected static class Type
  {
    String name;

    public Type(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }
  }

  protected String           path;

  protected Geometry         geometry;

  GraphRequest               ddlRequest;

  OrientDBRequest            dmlRequest;

  protected List<Type>       types;

  protected int[]            years;

  protected Random           random;

  protected long             count;

  protected ODatabaseSession db;

  public AbstractGraphBuilder(String path)
  {
    this.path = path;
  }

  protected void init()
  {
    GraphDBService service = GraphDBService.getInstance();

    this.ddlRequest = service.getDDLGraphDBRequest();
    this.dmlRequest = (OrientDBRequest) service.getGraphDBRequest();
    this.db = this.dmlRequest.getODatabaseSession();
    this.types = Arrays.asList(new Type("Country"), new Type("Province"), new Type("District"), new Type("Village"));
    this.years = new int[] { 2000, 2010, 2020 };
    this.random = new Random();
    this.count = 0;
  }

  protected abstract void addAttribute(OVertex vertex, String name, String type, Object value);

  protected abstract void createSchema();

  @Override
  @Request
  public void run()
  {
    init();

    try
    {
      FileDataStore myData = FileDataStoreFinder.getDataStore(new File(this.path));

      SimpleFeatureSource source = myData.getFeatureSource();
      SimpleFeatureCollection featCol = source.getFeatures();
      
      try (SimpleFeatureIterator features = featCol.features())
      {
        SimpleFeature feature = features.next();
        this.geometry = (Geometry) feature.getDefaultGeometry();
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }

    this.db.activateOnCurrentThread();

    createSchema();

    createData();
  }

  protected void createData()
  {
    for (int i = 0; i < 10; i++)
    {
      createVertex(0);
    }
  }

  protected OVertex createVertex(int index)
  {
    Type type = types.get(index);

    String code = type.getName() + "_" + ( this.count++ );

    OVertex vertex = db.newVertex(type.getName());
    vertex.setProperty("code", code);
    vertex = vertex.save();

    addAttribute(vertex, "label", "AttributeLocaleValue", code);
    addAttribute(vertex, "population", "AttributeLong", count);
    addAttribute(vertex, "oid", "AttributeString", code);
    addAttribute(vertex, "geometry", "AttributeMultiPolygon", this.geometry);

    if (index + 1 < this.types.size())
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

        OVertex child = this.createVertex(index + 1);

        this.addParent(vertex, startDate, endDate, child);
      }
    }

    return vertex;
  }

  protected void addParent(OVertex parent, Date startDate, Date endDate, OVertex vertex)
  {
    OEdge edge = parent.addEdge(vertex, "ObjectHasParent");
    edge.setProperty("startDate", startDate);
    edge.setProperty("endDate", endDate);
    edge = edge.save();
  }

  protected void createVertexClass(OClass parent, Type type)
  {
    OClass oClass = this.db.createVertexClass(type.name);
    oClass.createProperty("code", OType.STRING);
  }
}
