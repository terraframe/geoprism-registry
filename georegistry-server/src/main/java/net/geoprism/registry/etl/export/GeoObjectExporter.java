package net.geoprism.registry.etl.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectJsonAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.IOUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.query.postgres.LastUpdateRestriction;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectExporter
{
  public static void main(String[] args) throws IOException
  {
    mainInReq();
  }
  
  @Request
  public static void mainInReq() throws IOException
  {
    GeoObjectExporter exporter = new GeoObjectExporter("test123root", "test123hr", null, true, GeoObjectExportFormat.JSON_REVEAL, null, null);
    InputStream is = exporter.export();
    
    IOUtils.copy(is, System.out);
  }
  
  final private Logger logger = LoggerFactory.getLogger(GeoObjectExporter.class);
  
  final private ServerGeoObjectType got;
  
  final private ServerHierarchyType hierarchyType;
  
  private Integer pageSize = null;
  
  private Integer pageNumber = null;
  
  private Long total = null;

  private Date since;

  private Boolean includeLevel;

  private GeoObjectExportFormat format;
  
  public GeoObjectExporter(String gotCode, String hierarchyCode, Date since, Boolean includeLevel, GeoObjectExportFormat format, Integer pageSize, Integer pageNumber)
  {
    this.got = ServerGeoObjectType.get(gotCode);
    this.hierarchyType = ServerHierarchyType.get(hierarchyCode);
    this.since = since;
    this.includeLevel = includeLevel == null ? Boolean.FALSE : includeLevel;
    this.format = format == null ? GeoObjectExportFormat.JSON_CGR : format;
    this.pageSize = pageSize;
    this.pageNumber = pageNumber;
    
    init();
  }
  
  public void init()
  {
    if (this.pageSize == null || this.pageNumber == null || this.pageNumber == 0 || this.pageSize == 0)
    {
      this.pageSize = 1000;
      this.pageNumber = 1;
    }
    
    if (Session.getCurrentSession() != null)
    {
      ServiceFactory.getGeoObjectPermissionService().enforceCanRead(Session.getCurrentSession().getUser(), this.got.getOrganization().getCode(), this.got.getCode());
    }
  }
  
  private OIterator<GeoObject> postgresQuery()
  {
    GeoObjectQuery goq = new GeoObjectQuery(got);
    
    if (this.since != null)
    {
      goq.setRestriction(new LastUpdateRestriction(this.since));
    }
    
    if (this.pageSize != null && this.pageNumber != null)
    {
      goq.paginate(this.pageNumber, this.pageSize);
      this.total = goq.getCount();
    }
    
    goq.orderBy(DefaultAttribute.LAST_UPDATE_DATE.getName(), SortOrder.ASC);
    
    return goq.getIterator();
  }
  
  public void write(OutputStream os)
  {
    try (JsonWriter jw = new JsonWriter(new PrintWriter(os)))
    {
      jw.beginObject();
      {
        jw.name("results").beginArray();
        {
          try (OIterator<GeoObject> it = postgresQuery())
          {
            while (it.hasNext())
            {
              GeoObject go = it.next();
              
              exportObject(jw, go);
            }
          }
        } jw.endArray();
        
        if (this.pageSize != null && this.pageNumber != null && this.total != null)
        {
          jw.name("page").beginObject();
          {
            jw.name("pageSize").value(this.pageSize);
            jw.name("pageNumber").value(this.pageNumber);
            jw.name("total").value(this.total);
          } jw.endObject();
        }
        
      } jw.endObject();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
  protected void exportObject(JsonWriter jw, GeoObject go) throws IOException
  {
    GsonBuilder builder = new GsonBuilder();
    
    if (this.format.equals(GeoObjectExportFormat.JSON_REVEAL))
    {
      builder.registerTypeAdapter(GeoObject.class, new RevealGeoObjectJsonAdapters.RevealSerializer(this.got, this.hierarchyType, includeLevel));
    }
    else if (this.format.equals(GeoObjectExportFormat.JSON_CGR))
    {
      builder.registerTypeAdapter(GeoObject.class, new GeoObjectJsonAdapters.GeoObjectSerializer());
    }
    
    builder.create().toJson(go, go.getClass(), jw);
  }
  
  public InputStream export() throws IOException
  {
    PipedOutputStream pos = new PipedOutputStream();
    PipedInputStream pis = new PipedInputStream(pos);

    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          try
          {
            runInReq();
          }
          finally
          {
            pos.close();
          }
        }
        catch (IOException e)
        {
          logger.error("Error while writing", e);
        }
      }
      
      @Request
      public void runInReq()
      {
        GeoObjectExporter.this.write(pos);
      }
    });
    t.setDaemon(true);
    t.start();

    return pis;
  }
}
