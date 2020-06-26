/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.export;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectJsonAdapters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.IOUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.dhis2.dhis2adapter.DHIS2Facade;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectJsonExporter
{
  public static void main(String[] args) throws IOException
  {
    mainInReq();
  }

  @Request
  public static void mainInReq() throws IOException
  {
    GeoObjectJsonExporter exporter = new GeoObjectJsonExporter("test123leafgot", "test123hr", null, true, GeoObjectExportFormat.JSON_CGR, "test123sys", null, null);
    InputStream is = exporter.export();

    IOUtils.copy(is, System.out);
  }

  final private Logger              logger     = LoggerFactory.getLogger(GeoObjectJsonExporter.class);

  final private ServerGeoObjectType got;

  final private ServerHierarchyType hierarchyType;

  private Integer                   pageSize   = null;

  private Integer                   pageNumber = null;

  private Date                      since;

  private Long                      total;

  private Boolean                   includeLevel;

  private GeoObjectExportFormat     format;

  private ExternalSystem            externalSystem;

  private Thread                    exportThread;

  private DHIS2Facade               dhis2;

  private SyncLevel                 syncLevel;

  public GeoObjectJsonExporter(String gotCode, String hierarchyCode, Date since, Boolean includeLevel, GeoObjectExportFormat format, String externalSystemId, Integer pageSize, Integer pageNumber)
  {
    this.got = ServerGeoObjectType.get(gotCode);
    this.hierarchyType = ServerHierarchyType.get(hierarchyCode);
    this.since = since;
    this.includeLevel = includeLevel == null ? Boolean.FALSE : includeLevel;
    this.format = format == null ? GeoObjectExportFormat.JSON_CGR : format;
    this.externalSystem = ExternalSystem.getByExternalSystemId(externalSystemId);
    this.pageSize = pageSize;
    this.pageNumber = pageNumber;

    init();
  }

  public GeoObjectJsonExporter(ServerGeoObjectType got, ServerHierarchyType hierarchyType, Date since, Boolean includeLevel, GeoObjectExportFormat format, ExternalSystem externalSystem, Integer pageSize, Integer pageNumber)
  {
    this.got = got;
    this.hierarchyType = hierarchyType;
    this.externalSystem = externalSystem;
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

  public List<VertexServerGeoObject> query()
  {
    MdVertexDAOIF mdVertex = got.getMdVertex();
    MdAttributeDAOIF mdAttribute = MdAttributeDAO.getByKey(GeoVertex.CLASS + "." + GeoVertex.LASTUPDATEDATE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());

    if (this.since != null)
    {

      statement.append(" WHERE " + mdAttribute.getColumnName() + " <= :lastUpdateDate");
    }

    statement.append(" ORDER BY " + mdAttribute.getColumnName() + ", oid ASC");

    if (this.pageSize != null && this.pageNumber != null && this.pageSize != -1 && this.pageNumber != -1)
    {
      statement.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + this.pageSize);
    }

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    if (this.since != null)
    {
      query.setParameter("lastUpdateDate", this.since);
    }

    List<VertexObject> vObjects = query.getResults();

    List<VertexServerGeoObject> response = new LinkedList<VertexServerGeoObject>();

    for (VertexObject vObject : vObjects)
    {
      VertexServerGeoObject vSGO = new VertexServerGeoObject(got, vObject);
      vSGO.setDate(ValueOverTime.INFINITY_END_DATE);

      response.add(vSGO);
    }

    return response;
  }

  public Long count()
  {
    MdVertexDAOIF mdVertex = got.getMdVertex();
    MdAttributeDAOIF mdAttribute = MdAttributeDAO.getByKey(GeoVertex.CLASS + "." + GeoVertex.LASTUPDATEDATE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

    if (this.since != null)
    {
      statement.append(" WHERE " + mdAttribute.getColumnName() + " <= :lastUpdateDate");
    }

    statement.append(" ORDER BY " + mdAttribute.getColumnName() + ", oid ASC");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    if (this.since != null)
    {
      query.setParameter("lastUpdateDate", this.since);
    }

    return query.getSingleResult();
  }

  public void writeObjects(JsonWriter jw) throws IOException
  {
    this.total = this.count();

    List<VertexServerGeoObject> objects = this.query();

    for (VertexServerGeoObject object : objects)
    {
      exportObject(jw, object);
    }
  }

  public void write(OutputStream os)
  {
    try (JsonWriter jw = new JsonWriter(new PrintWriter(os)))
    {
      jw.beginObject();
      {
        jw.name("results").beginArray();
        {
          writeObjects(jw);
        }
        jw.endArray();

        if (this.pageSize != null && this.pageNumber != null && this.pageSize != -1 && this.pageNumber != -1)
        {
          jw.name("page").beginObject();
          {
            jw.name("pageSize").value(this.pageSize);
            jw.name("pageNumber").value(this.pageNumber);
            jw.name("total").value(this.total);
          }
          jw.endObject();
        }

      }
      jw.endObject();
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  protected void exportObject(JsonWriter jw, VertexServerGeoObject go) throws IOException
  {
    GsonBuilder builder = new GsonBuilder();

    if (this.format.equals(GeoObjectExportFormat.JSON_REVEAL))
    {
      builder.registerTypeAdapter(VertexServerGeoObject.class, new RevealGeoObjectJsonAdapters.RevealSerializer(this.got, this.hierarchyType, this.includeLevel, this.externalSystem));
    }
    else if (this.format.equals(GeoObjectExportFormat.JSON_CGR))
    {
      builder.registerTypeAdapter(VertexServerGeoObject.class, new SeverGeoObjectJsonAdapters.ServerGeoObjectSerializer());
      builder.registerTypeAdapter(GeoObject.class, new GeoObjectJsonAdapters.GeoObjectSerializer());
    }
    else if (this.format.equals(GeoObjectExportFormat.JSON_DHIS2))
    {
      builder.registerTypeAdapter(VertexServerGeoObject.class, new DHIS2GeoObjectJsonAdapters.DHIS2Serializer(this.dhis2, this.syncLevel, this.got, this.hierarchyType, this.externalSystem));
    }

    builder.create().toJson(go, go.getClass(), jw);
  }

  public InputStream export() throws IOException
  {
    PipedOutputStream pos = new PipedOutputStream();
    PipedInputStream pis = new PipedInputStream(pos);

    exportThread = new Thread(new Runnable()
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
        GeoObjectJsonExporter.this.write(pos);
      }
    });
    exportThread.setDaemon(true);
    exportThread.start();

    return pis;
  }

  /**
   * TODO : Abstraction is leaking here. Maybe pass in a config object which
   * contains this extra stuff we need?
   */

  public void setDHIS2Facade(DHIS2Facade dhis2)
  {
    this.dhis2 = dhis2;
  }

  public void setSyncLevel(SyncLevel syncLevel)
  {
    this.syncLevel = syncLevel;
  }
}
