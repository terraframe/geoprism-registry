package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.Commit;
import net.geoprism.registry.Publish;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.model.EdgeDirection;

@Service
public class JenaExportBusinessService
{
  private static Logger                 logger             = LoggerFactory.getLogger(JenaExportBusinessService.class);

  public static final String            GEO                = "http://www.opengis.net/ont/geosparql#";

  public static final String            SF                 = "http://www.opengis.net/ont/sf#";

  public static final String            GRAPH_NAMESPACE    = "http://terraframe.com";

  public static final String            GRAPH_NAME         = GRAPH_NAMESPACE + "/g1";

  public static final Boolean           INCLUDE_GEOMETRIES = true;

  @Autowired
  private BusinessTypeBusinessServiceIF bTypeService;

  @Autowired
  private RemoteJenaServiceIF           service;

  @Autowired
  private CommitBusinessServiceIF       commitService;

  public void export(Publish publish)
  {
    this.commitService.getLatest(publish).ifPresent(commit -> {
      export(commit);
    });
  }

  protected void export(Commit commit)
  {
    // TODO: Determine if the commit has already been exported

    this.commitService.getDependencies(commit) //
        .stream() //
        .forEach(dependency -> this.export(dependency));

    int chunk = 0;
    List<RemoteEvent> remoteEvents = null;

    while ( ( remoteEvents = this.commitService.getRemoteEvents(commit, chunk) ).size() > 0)
    {
      remoteEvents.stream() //
          .forEach(event -> {
            if (event instanceof RemoteGeoObjectEvent)
            {
              this.handleRemoteGeoObject(commit, (RemoteGeoObjectEvent) event);
            }
            else if (event instanceof RemoteBusinessObjectAddGeoObjectEvent)
            {
              this.handleRemoteAddGeoObject(commit, (RemoteBusinessObjectAddGeoObjectEvent) event);
            }
            else if (event instanceof RemoteBusinessObjectEvent)
            {
              this.handleRemoteBusinessObject(commit, (RemoteBusinessObjectEvent) event);
            }
            else if (event instanceof RemoteBusinessObjectCreateEdgeEvent)
            {
              this.handleRemoteCreateEdge(commit, (RemoteBusinessObjectCreateEdgeEvent) event);
            }
            else if (event instanceof RemoteGeoObjectCreateEdgeEvent)
            {
              this.handleRemoteCreateEdge(commit, (RemoteGeoObjectCreateEdgeEvent) event);
            }
            else if (event instanceof RemoteGeoObjectSetParentEvent)
            {
              this.handleRemoteParent(commit, (RemoteGeoObjectSetParentEvent) event);
            }
          });

      chunk++;
    }
  }

  public void handleRemoteGeoObject(Commit commit, RemoteGeoObjectEvent event)
  {
    logger.trace("Jena Projection - Handling remote geo object");

    List<String> statements = new LinkedList<>();

    Model model = ModelFactory.createDefaultModel();

    final String code = event.getCode();
    final String typeCode = event.getType();

    GeoObject dto = GeoObject.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    Map<String, AttributeType> attributes = dto.getType().getAttributeMap();

    attributes.forEach((attributeName, attribute) -> {
      String subjectUri = buildObjectUri(code, typeCode);
      String attributeUri = buildAttributeUri(typeCode, attribute);

      statements.add("DELETE WHERE { GRAPH <" + GRAPH_NAME + "> { <" + subjectUri + "> <" + attributeUri + "> ?obj}}");

      String literal = null;

      if (attribute instanceof AttributeTermType || attribute instanceof AttributeGeometryType)
      {
        // SKIP
      }
      else
      {
        Object value = dto.getValue(attributeName);

        if (value instanceof LocalizedValue)
        {
          literal = ( (LocalizedValue) value ).getValue();
        }
        else if (value == null || value instanceof String)
        {
          literal = (String) value;
        }
        else
        {
          literal = value.toString();
        }
      }

      if (literal != null)
      {
        this.addLiteralToModel(model, //
            subjectUri, //
            attributeUri, //
            literal);
      }
    });

    if (INCLUDE_GEOMETRIES)
    {
      Geometry geom = dto.getGeometry();

      if (geom != null)
      {
        this.addResourceToModel(model, //
            buildObjectUri(code, typeCode), //
            buildHasGeometryPredicate(), //
            buildObjectUri(code + "Geometry", typeCode));

        this.addResourceToModel(model, //
            () -> model.createResource(buildObjectUri(code + "Geometry", typeCode)), //
            () -> org.apache.jena.vocabulary.RDF.type, //
            () -> model.createResource(GEO + "Geometry"));

        this.addResourceToModel(model, //
            () -> model.createResource(buildObjectUri(code + "Geometry", typeCode)), //
            () -> org.apache.jena.vocabulary.RDF.type, //
            () -> model.createResource(SF + geom.getClass().getSimpleName()));

        final String geomValue = buildObjectUri(code + "Geometry", typeCode);

        this.addLiteralToModel(model, //
            () -> model.createResource(geomValue), //
            () -> model.createProperty(GEO + "asWKT"), //
            () -> model.createTypedLiteral("<" + getSrs(geom) + "> " + geom.toText(), new org.apache.jena.datatypes.BaseDatatype(GEO + "wktLiteral")));

        statements.add("DELETE WHERE { GRAPH <" + GRAPH_NAME + "> { <" + geomValue + "> <" + GEO + "asWKT" + "> ?obj}}");

      }
    }

    if (!commit.getVersionNumber().equals(Integer.valueOf(0)))
    {
      this.service.update(statements);
    }

    this.service.load(GRAPH_NAME, model);
  }

  public void handleRemoteParent(Commit commit, RemoteGeoObjectSetParentEvent event)
  {
    logger.trace("Jena Projection - Handling remote set parent");

    Model model = ModelFactory.createDefaultModel();

    String subjectUri = buildObjectUri(event.getCode(), event.getType());
    String edgeTypeUri = GRAPH_NAMESPACE + "/" + event.getEdgeType();

    List<String> statements = new LinkedList<>();
    statements.add("DELETE WHERE { GRAPH <" + GRAPH_NAME + "> { <" + subjectUri + "> <" + edgeTypeUri + "> ?obj}}");

    this.addResourceToModel(model, //
        subjectUri, //
        edgeTypeUri, //
        buildObjectUri(event.getParentCode(), event.getParentType()));

    if (!commit.getVersionNumber().equals(Integer.valueOf(0)))
    {
      this.service.update(statements);
    }

    this.service.load(GRAPH_NAME, model);
  }

  public void handleRemoteCreateEdge(Commit commit, RemoteGeoObjectCreateEdgeEvent event)
  {
    logger.trace("Jena Projection - Handling remote create edge");

    Model model = ModelFactory.createDefaultModel();

    this.addResourceToModel(model, //
        buildObjectUri(event.getSourceCode(), event.getSourceType()), //
        GRAPH_NAMESPACE + "/" + event.getEdgeType(), //
        buildObjectUri(event.getTargetCode(), event.getTargetType()));

    this.service.load(GRAPH_NAME, model);
  }

  public void handleRemoteBusinessObject(Commit commit, RemoteBusinessObjectEvent event)
  {
    logger.trace("Jena Projection - Handling remote business object");

    List<String> statements = new LinkedList<>();

    Model model = ModelFactory.createDefaultModel();

    final String code = event.getCode();
    final String typeCode = event.getType();

    BusinessType type = this.bTypeService.getByCode(typeCode);

    JsonObject object = JsonParser.parseString(event.getObject()).getAsJsonObject();
    JsonObject data = object.get("data").getAsJsonObject();

    Map<String, AttributeType> attributes = type.getAttributeMap();

    attributes.values().stream() //
        .filter(a -> data.has(a.getName()) && !data.get(a.getName()).isJsonNull()) //
        .filter(a -> ! ( a instanceof AttributeTermType || a instanceof AttributeGeometryType )) //
        .forEach(attribute -> {
          String literal = null;

          String subjectUri = buildObjectUri(code, typeCode);
          String attributeUri = buildAttributeUri(typeCode, attribute);

          statements.add("DELETE WHERE { GRAPH <" + GRAPH_NAME + "> { <" + subjectUri + "> <" + attributeUri + "> ?obj}}");

          JsonElement element = data.get(attribute.getName());

          if (attribute instanceof AttributeLocalType)
          {
            LocalizedValue value = LocalizedValue.fromJSON(element.getAsJsonObject());

            literal = ( (LocalizedValue) value ).getValue();
          }
          else if (attribute instanceof AttributeIntegerType)
          {
            literal = Long.toString(element.getAsLong());
          }
          else if (attribute instanceof AttributeFloatType)
          {
            literal = Double.toString(element.getAsDouble());
          }
          else if (attribute instanceof AttributeBooleanType)
          {
            literal = Boolean.toString(element.getAsBoolean());
          }
          else
          {
            literal = element.getAsString();
          }

          if (literal != null)
          {
            this.addLiteralToModel(model, //
                subjectUri, //
                attributeUri, //
                literal);
          }
        });

    if (!commit.getVersionNumber().equals(Integer.valueOf(0)))
    {
      this.service.update(statements);
    }

    this.service.load(GRAPH_NAME, model);
  }

  public void handleRemoteAddGeoObject(Commit commit, RemoteBusinessObjectAddGeoObjectEvent event)
  {
    logger.trace("Jena Projection - Handling remote add geo object");

    Model model = ModelFactory.createDefaultModel();

    if (event.getDirection().equals(EdgeDirection.CHILD))
    {
      this.addResourceToModel(model, //
          buildObjectUri(event.getCode(), event.getType()), //
          GRAPH_NAMESPACE + "/" + event.getEdgeType(), //
          buildObjectUri(event.getGeoObjectCode(), event.getGeoObjectType()));
    }
    else
    {
      this.addResourceToModel(model, //
          buildObjectUri(event.getGeoObjectCode(), event.getGeoObjectType()), //
          GRAPH_NAMESPACE + "/" + event.getEdgeType(), //
          buildObjectUri(event.getCode(), event.getType()));

    }

    this.service.load(GRAPH_NAME, model);
  }

  public void handleRemoteCreateEdge(Commit commit, RemoteBusinessObjectCreateEdgeEvent event)
  {
    logger.trace("Jena Projection - Handling remote create edge");

    Model model = ModelFactory.createDefaultModel();

    this.addResourceToModel(model, //
        buildObjectUri(event.getSourceCode(), event.getSourceType()), //
        GRAPH_NAMESPACE + "/" + event.getEdgeType(), //
        buildObjectUri(event.getTargetCode(), event.getTargetType()));

    this.service.load(GRAPH_NAME, model);
  }

  protected String getSrs(Geometry geom)
  {
    String srs_uri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";

    if (geom.getSRID() > 0)
    {
      srs_uri = "http://www.opengis.net/def/crs/EPSG/0/" + geom.getSRID();
    }
    return srs_uri;
  }

  protected String buildObjectUri(String code, final String typeCode)
  {
    return GRAPH_NAMESPACE + "/" + typeCode + "-" + code;
  }

  protected String buildAttributeUri(String typeCode, AttributeType attribute)
  {
    if (attribute.getIsDefault())
    {
      if (attribute.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        return org.apache.jena.vocabulary.RDFS.label.getURI();
      }

      return GRAPH_NAMESPACE + "/" + "GeoObject-" + attribute.getName();
    }

    return GRAPH_NAMESPACE + "/" + typeCode + "-" + attribute.getName();
  }

  protected String buildHasGeometryPredicate()
  {
    return GEO + "hasGeometry";
  }

  protected void addResourceToModel(Model model, String subject, String predicate, String object)
  {
    this.addResourceToModel(model, //
        () -> model.createResource(subject), //
        () -> model.createProperty(predicate), //
        () -> model.createResource(object));

  }

  protected void addResourceToModel(Model model, Supplier<Resource> subject, Supplier<Property> predicate, Supplier<Resource> resource)
  {
    model.add(subject.get(), predicate.get(), resource.get());
  }

  protected void addLiteralToModel(Model model, String sub, String pred, String obj)
  {
    Resource subject = model.createResource(sub);
    Property predicate = model.createProperty(pred);
    Literal object = model.createLiteral(obj);

    // This GeoObject has a Geometry
    model.add(subject, predicate, object);
  }

  protected void addLiteralToModel(Model model, Supplier<Resource> subject, Supplier<Property> predicate, Supplier<Literal> literal)
  {
    // This GeoObject has a Geometry
    model.add(subject.get(), predicate.get(), literal.get());
  }

}
