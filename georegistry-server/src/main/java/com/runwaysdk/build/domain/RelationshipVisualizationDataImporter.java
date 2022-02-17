package com.runwaysdk.build.domain;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.conversion.OrganizationConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.permission.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.service.ServiceFactory;

public class RelationshipVisualizationDataImporter
{
  private static final Logger logger = LoggerFactory.getLogger(RelationshipVisualizationDataImporter.class);

  private Organization        organization;

  public static final Date    START_DATE;

  static
  {
    Calendar cal = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cal.clear();
    cal.set(2020, Calendar.JANUARY, 1);

    START_DATE = cal.getTime();
  }

  public static final Date    END_DATE                = ValueOverTime.INFINITY_END_DATE;

  public static final String  GOT_DOCK                = "Dock";

  private ServerGeoObjectType sgotDock;

  public static final String  GOT_CHANNEL             = "Channel";

  private ServerGeoObjectType sgotChannel;

  public static final String  GOT_PROJECT             = "Project";

  private ServerGeoObjectType sgotProject;

  public static final String  GOT_REGULATORY_BOUNDARY = "RegulatoryBoundry";

  private ServerGeoObjectType sgotRegulatoryBoundary;

  public static final String  GOT_SITE                = "Site";

  private ServerGeoObjectType sgotSite;

  private UndirectedGraphType mdeAdjacentTo;

  private UndirectedGraphType mdeConnectedTo;

  public static final String  MDEDGE_ADJACENT_TO      = "AdjacentTo";

  public static final String  MDEDGE_CONNECTED_TO     = "ConnectedTo";

  public static void main(String[] args) throws Exception
  {
    new RelationshipVisualizationDataImporter().doItInReq();
  }

  @Request
  public void doItInReq() throws Exception
  {
    this.doIt();
  }

  @Transaction
  public void doIt() throws Exception
  {
    try
    {
      this.defineMetadata();
      this.importInstanceData();
    }
    catch (Throwable t)
    {
      logger.error("Encountered error while importing data.", t);
      throw t;
    }
  }

  private void defineMetadata() throws Exception
  {
    this.createOrganization();

    this.createTypes();

    this.createMdEdges();
  }

  private void createOrganization()
  {
    logger.info("Creating organization.");

    LocalizedValue displayLabel = new LocalizedValue("United States Army Core of Engineers");
    LocalizedValue contactInfo = new LocalizedValue("USACE");

    OrganizationDTO dto = new OrganizationDTO("USACE", displayLabel, contactInfo);

    this.organization = new OrganizationConverter().create(dto);
  }

  private void createTypes()
  {
    logger.info("Creating types.");

    this.sgotDock = new ServerGeoObjectTypeConverter().create(new GeoObjectType(GOT_DOCK, GeometryType.MULTIPOINT, new LocalizedValue("Dock"), new LocalizedValue("Dock"), true, this.organization.getCode(), ServiceFactory.getAdapter()));
    this.sgotDock.createAttributeType(AttributeType.factory("locationDescription", new LocalizedValue("Location Description"), new LocalizedValue("Location Description"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("remarks", new LocalizedValue("Remarks"), new LocalizedValue("Remarks"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("commoditie", new LocalizedValue("Commoditie"), new LocalizedValue("Commoditie"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("purpose", new LocalizedValue("Purpose"), new LocalizedValue("Purpose"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("owners", new LocalizedValue("Owners"), new LocalizedValue("Owners"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("streetAddress", new LocalizedValue("Street Address"), new LocalizedValue("Street Address"), AttributeCharacterType.TYPE, false, false, false));

    this.sgotChannel = new ServerGeoObjectTypeConverter().create(new GeoObjectType(GOT_CHANNEL, GeometryType.MULTIPOLYGON, new LocalizedValue("Channel"), new LocalizedValue("Channel"), true, this.organization.getCode(), ServiceFactory.getAdapter()));
    this.sgotChannel.createAttributeType(AttributeType.factory("sourceData", new LocalizedValue("Source Data"), new LocalizedValue("Source Data"), AttributeCharacterType.TYPE, false, false, false));

    this.sgotProject = new ServerGeoObjectTypeConverter().create(new GeoObjectType(GOT_PROJECT, GeometryType.MULTIPOLYGON, new LocalizedValue("Project"), new LocalizedValue("Project"), true, this.organization.getCode(), ServiceFactory.getAdapter()));

    this.sgotRegulatoryBoundary = new ServerGeoObjectTypeConverter().create(new GeoObjectType(GOT_REGULATORY_BOUNDARY, GeometryType.MULTIPOLYGON, new LocalizedValue("Regulatory Boundary"), new LocalizedValue("Regulatory Boundary"), true, this.organization.getCode(), ServiceFactory.getAdapter()));

    this.sgotSite = new ServerGeoObjectTypeConverter().create(new GeoObjectType(GOT_SITE, GeometryType.MULTIPOLYGON, new LocalizedValue("Site"), new LocalizedValue("Site"), true, this.organization.getCode(), ServiceFactory.getAdapter()));

    ServiceFactory.getRegistryService().refreshMetadataCache();
  }

  private void createMdEdges()
  {
    // Docs Located In Site
    // Docs Located In Project
    // Docs Located In Canal
    // Docs MIGHT (?) be Adjacent to Project
    // Projects Overlap with Sites (not cleanly within)
    // Canal Adjacent to Site
    // Canal Adjacent to Project
    // Project adjacent to other Project (kinda obvious but seems important for
    // the graph)
    // Site adjacent to other Site (kinda obvious but seems important for the
    // graph)
    // All are Located In Regulatory Boundary (not shown in map)

    this.mdeAdjacentTo = this.createMdEdge(MDEDGE_ADJACENT_TO, "Adjacent To");
    this.mdeConnectedTo = this.createMdEdge(MDEDGE_CONNECTED_TO, "Connected To");
  }

  private UndirectedGraphType createMdEdge(String code, String defaultLabel)
  {
    LocalizedValue label = new LocalizedValue(defaultLabel);
    LocalizedValue description = new LocalizedValue("");

    return UndirectedGraphType.create(code, label, description);
  }

  private void importInstanceData() throws Exception
  {
    this.importDocks();
    this.importChannels();
    this.importProjects();
    this.importRegulatoryBoundaries();
    this.importSites();
    this.importConnectedToEdgeData();
    this.importAdjacentToEdgeData();
  }

  public static class GeoJsonImporter
  {
    private ApplicationResource resource;

    private ServerGeoObjectType type;

    private String              codeAttr;

    private String              displayLabelAttr;

    public GeoJsonImporter(ApplicationResource resource, ServerGeoObjectType type, String codeAttr, String displayLabelAttr)
    {
      this.resource = resource;
      this.type = type;
      this.codeAttr = codeAttr;
      this.displayLabelAttr = displayLabelAttr;
    }

    public void importData() throws Exception
    {
      final ServerGeoObjectService service = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService());

      try (InputStream stream = this.resource.openNewStream())
      {
        JsonObject featureCollection = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

        JsonArray features = featureCollection.get("features").getAsJsonArray();

        logger.info("About to import [" + features.size() + "] features into GeoObjectType [" + this.type.getCode() + "].");

        for (int i = 0; i < features.size(); ++i)
        {
          JsonObject feature = features.get(i).getAsJsonObject();
          JsonObject sourceProps = feature.get("properties").getAsJsonObject();

          JsonObject targetProps = new JsonObject();
          targetProps.addProperty("type", this.type.getCode());

          JsonObject geoObject = new JsonObject();
          geoObject.addProperty("type", "Feature");
          targetProps.addProperty("code", this.readFromSource(this.codeAttr, sourceProps));
          targetProps.addProperty("invalid", false);
          geoObject.add("properties", targetProps);

          GeoObject go = GeoObject.fromJSON(ServiceFactory.getAdapter(), geoObject.toString());

          this.importGeometry(feature, go);
          this.importDisplayLabel(feature, go);
          this.importCustomAttributes(sourceProps, go);

          service.apply(go, START_DATE, END_DATE, true, true);
        }
      }
    }

    protected void importDisplayLabel(JsonObject sourceFeature, GeoObject target)
    {
      target.setDisplayLabel(new LocalizedValue(readFromSource(this.displayLabelAttr, sourceFeature.get("properties").getAsJsonObject())));
    }

    protected void importGeometry(JsonObject sourceFeature, GeoObject target)
    {
      target.setGeometry(geoJsonToGeometry(sourceFeature.get("geometry").getAsJsonObject()));
    }

    protected void importCustomAttributes(JsonObject sourceProps, GeoObject target)
    {
    }

    protected Geometry geoJsonToGeometry(JsonElement geoJson)
    {
      if (geoJson != null)
      {
        GeoJSONReader reader = new GeoJSONReader();
        Geometry jtsGeom = reader.read(geoJson.toString());

        return jtsGeom;
      }

      return null;
    }

    protected String readFromSource(String name, JsonObject sourceProps)
    {
      if (!sourceProps.has(name))
      {
        return null;
      }

      JsonElement value = sourceProps.get(name);

      if (value.isJsonNull())
      {
        return null;
      }

      return value.getAsString();
    }

    protected JsonElement convertPointToMultiPoint(JsonObject geometry)
    {
      JsonObject newGeom = new JsonObject();

      newGeom.addProperty("type", "MultiPoint");

      JsonArray multiPoint = new JsonArray();
      multiPoint.add(geometry.get("coordinates").getAsJsonArray());
      newGeom.add("coordinates", multiPoint);

      return newGeom;
    }
  }

  private void importDocks() throws Exception
  {
    GeoJsonImporter dockImporter = new GeoJsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/docks.geojson"), "docks.geojson"), this.sgotDock, "ID", "DOCK")
    {
      @Override
      public void importGeometry(JsonObject sourceFeature, GeoObject target)
      {
        target.setGeometry(geoJsonToGeometry(convertPointToMultiPoint(sourceFeature.get("geometry").getAsJsonObject())));
      }

      @Override
      protected void importCustomAttributes(JsonObject sourceProps, GeoObject target)
      {
        target.setValue("locationDescription", this.readFromSource("LOCATION_D", sourceProps));
        target.setValue("remarks", this.readFromSource("REMARKS", sourceProps));
        target.setValue("commoditie", this.readFromSource("COMMODITIE", sourceProps));
        target.setValue("purpose", this.readFromSource("PURPOSE", sourceProps));
        target.setValue("owners", this.readFromSource("OWNERS", sourceProps));
        target.setValue("streetAddress", this.readFromSource("STREET_ADD", sourceProps));
      }
    };

    dockImporter.importData();
  }

  private void importChannels() throws Exception
  {
    GeoJsonImporter importer = new GeoJsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/channels.geojson"), "channels.geojson"), this.sgotChannel, "channelare", "sdsfeatu_1")
    {
      @Override
      protected void importCustomAttributes(JsonObject sourceProps, GeoObject target)
      {
        target.setValue("sourceData", this.readFromSource("sourcedata", sourceProps));
      }
    };

    importer.importData();
  }

  private void importProjects() throws Exception
  {
    GeoJsonImporter importer = new GeoJsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/projects.geojson"), "projects.geojson"), this.sgotProject, "OBJECTID", "NAME");

    importer.importData();
  }

  private void importRegulatoryBoundaries() throws Exception
  {
    GeoJsonImporter importer = new GeoJsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/regulatory-boundary.geojson"), "regulatory-boundary.geojson"), this.sgotRegulatoryBoundary, "DISTRICT_N", "DISTRICT");

    importer.importData();
  }

  private void importSites() throws Exception
  {
    GeoJsonImporter importer = new GeoJsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/sites.geojson"), "sites.geojson"), this.sgotSite, "SITEIDPK", "FEATURENAM");

    importer.importData();
  }

  public static class EdgeGeojsonImporter
  {
    private ApplicationResource resource;

    private GraphType           graphType;

    public EdgeGeojsonImporter(ApplicationResource resource, GraphType graphType)
    {
      this.resource = resource;
      this.graphType = graphType;
    }

    public void importData() throws Exception
    {
      try (InputStream stream = this.resource.openNewStream())
      {
        JsonObject data = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();

        JsonArray edges = data.get("edges").getAsJsonArray();

        logger.info("About to import [" + edges.size() + "] edges as MdEdge [" + this.graphType.getCode() + "].");

        for (int i = 0; i < edges.size(); ++i)
        {
          JsonObject joEdge = edges.get(i).getAsJsonObject();

          String sourceCode = joEdge.get("source").getAsString();
          String targetCode = joEdge.get("target").getAsString();

          VertexServerGeoObject source = this.getVertexByCode(sourceCode);
          VertexServerGeoObject target = this.getVertexByCode(targetCode);

          source.addGraphChild(target, this.graphType, START_DATE, END_DATE);
        }
      }
    }

    public VertexServerGeoObject getVertexByCode(String code)
    {
      String statement = "SELECT FROM geo_vertex";
      statement += " WHERE code = :code";

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement);
      query.setParameter("code", code);

      VertexObject vertex = query.getSingleResult();
      MdVertexDAOIF mdClass = (MdVertexDAOIF) vertex.getMdClass();
      ServerGeoObjectType type = ServerGeoObjectType.get(mdClass);

      return new VertexServerGeoObject(type, vertex);
    }
  }

  private void importConnectedToEdgeData() throws Exception
  {
    EdgeGeojsonImporter importer = new EdgeGeojsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/edges/connected-to.json"), "connected-to.json"), this.mdeConnectedTo);

    importer.importData();
  }

  private void importAdjacentToEdgeData() throws Exception
  {
    EdgeGeojsonImporter importer = new EdgeGeojsonImporter(new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/edges/adjacent-to.json"), "adjacent-to.json"), this.mdeAdjacentTo);

    importer.importData();
  }
}
