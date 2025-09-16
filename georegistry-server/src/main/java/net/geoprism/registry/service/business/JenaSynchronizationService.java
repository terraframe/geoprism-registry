package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
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
import org.commongeoregistry.adapter.metadata.AttributeDateType;
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
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectAddGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteBusinessObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.model.EdgeDirection;

@Service
public class JenaSynchronizationService
{
  private static Logger                                      logger             = LoggerFactory.getLogger(JenaSynchronizationService.class);

  public static final String                                 GEO                = "http://www.opengis.net/ont/geosparql#";

  public static final String                                 SF                 = "http://www.opengis.net/ont/sf#";

  public static final Boolean                                INCLUDE_GEOMETRIES = true;

  @Autowired
  private BusinessTypeBusinessServiceIF                      bTypeService;

  @Autowired
  private RemoteJenaServiceIF                                service;

  @Autowired
  private CommitBusinessServiceIF                            commitService;

  @Autowired
  private PublishBusinessServiceIF                           publishService;

  @Autowired
  private SynchronizationHasProcessedCommitBusinessServiceIF exportService;

  public void execute(SynchronizationConfig synchronization, JenaExportConfig configuration, ExportHistory history)
  {
    Publish publish = this.publishService.getByUid(configuration.getPublishUid()) //
        .orElseThrow(() -> GeoRegistryUtil.createDataNotFoundException(Publish.CLASS, Publish.UID, configuration.getPublishUid()));

    this.commitService.getLatest(publish).ifPresent(commit -> {

      if (history != null)
      {
        history.appLock();
        history.setWorkTotal(0l);
        history.setWorkProgress(0l);
        history.setExportedRecords(0l);
        history.clearStage();
        history.addStage(ExportStage.EXPORT);
        history.apply();
      }

      AtomicLong workProgress = new AtomicLong(0);

      execute(synchronization, configuration, commit, history, workProgress);

      if (history != null)
      {
        history.appLock();
        history.setWorkProgress(history.getWorkTotal());
        history.setExportedRecords(history.getWorkTotal());
        history.clearStage();
        history.addStage(ExportStage.COMPLETE);
        history.apply();
      }
    });
  }

  protected void execute(SynchronizationConfig synchronization, JenaExportConfig config, Commit commit, ExportHistory history, AtomicLong progress)
  {
    if (!this.exportService.hasBeenPublished(synchronization, commit))
    {
      // TODO: If commit is the first version then export out the metadata

      this.commitService.getDependencies(commit) //
          .stream() //
          .forEach(dependency -> this.execute(synchronization, config, dependency, history, progress));

      if (history != null)
      {
        long count = this.commitService.getEventCount(commit) + 1;

        history.appLock();
        history.setWorkTotal(history.getWorkTotal() + count);
        history.apply();
      }

      AtomicReference<Model> model = new AtomicReference<Model>(ModelFactory.createDefaultModel());

      this.commitService.getRemoteEvents(commit).forEach(event -> {
        if (event instanceof RemoteGeoObjectEvent)
        {
          this.handleRemoteGeoObject(commit, (RemoteGeoObjectEvent) event, config, model.get());
        }
        else if (event instanceof RemoteBusinessObjectAddGeoObjectEvent)
        {
          this.handleRemoteAddGeoObject(commit, (RemoteBusinessObjectAddGeoObjectEvent) event, config, model.get());
        }
        else if (event instanceof RemoteBusinessObjectEvent)
        {
          this.handleRemoteBusinessObject(commit, (RemoteBusinessObjectEvent) event, config, model.get());
        }
        else if (event instanceof RemoteBusinessObjectCreateEdgeEvent)
        {
          this.handleRemoteCreateEdge(commit, (RemoteBusinessObjectCreateEdgeEvent) event, config, model.get());
        }
        else if (event instanceof RemoteGeoObjectCreateEdgeEvent)
        {
          this.handleRemoteCreateEdge(commit, (RemoteGeoObjectCreateEdgeEvent) event, config, model.get());
        }
        else if (event instanceof RemoteGeoObjectSetParentEvent)
        {
          this.handleRemoteParent(commit, (RemoteGeoObjectSetParentEvent) event, config, model.get());
        }

        long cWorkProgress = progress.incrementAndGet();

        if ( ( cWorkProgress % 1000 == 0 ))
        {
          // Push the model chunk to Jena
          this.service.load(model.get(), config);

          // Reset to an empty model
          model.set(ModelFactory.createDefaultModel());

          if (history != null)
          {

            history.appLock();
            history.setWorkProgress(cWorkProgress);
            history.setExportedRecords(cWorkProgress);
            history.apply();
          }
        }

      });

      // Push the model chunk to Jena
      this.service.load(model.get(), config);

      // Mark the commit as exported
      this.exportService.create(synchronization, commit);

      if (history != null)
      {
        history.appLock();
        history.setWorkProgress(progress.get());
        history.setExportedRecords(progress.get());
        history.apply();
      }
    }
    else

    {
      System.out.println("Skipping export of commit [" + commit.getUid() + "] because its already been exported");
    }
  }

  public void handleRemoteGeoObject(Commit commit, RemoteGeoObjectEvent event, JenaExportConfig config, Model model)
  {
    logger.trace("Jena Projection - Handling remote geo object");

    List<String> statements = new LinkedList<>();

    final String code = event.getCode();
    final String typeCode = event.getType();

    GeoObject dto = GeoObject.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    Map<String, AttributeType> attributes = dto.getType().getAttributeMap();

    attributes.forEach((attributeName, attribute) -> {
      String subjectUri = buildObjectUri(config, code, typeCode);
      String attributeUri = buildAttributeUri(config, typeCode, attribute);

      statements.add("DELETE WHERE { GRAPH <" + config.getGraph() + "> { <" + subjectUri + "> <" + attributeUri + "> ?obj}}");

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
            buildObjectUri(config, code, typeCode), //
            buildHasGeometryPredicate(), //
            buildObjectUri(config, code + "Geometry", typeCode));

        this.addResourceToModel(model, //
            () -> model.createResource(buildObjectUri(config, code + "Geometry", typeCode)), //
            () -> org.apache.jena.vocabulary.RDF.type, //
            () -> model.createResource(GEO + "Geometry"));

        this.addResourceToModel(model, //
            () -> model.createResource(buildObjectUri(config, code + "Geometry", typeCode)), //
            () -> org.apache.jena.vocabulary.RDF.type, //
            () -> model.createResource(SF + geom.getClass().getSimpleName()));

        final String geomValue = buildObjectUri(config, code + "Geometry", typeCode);

        this.addLiteralToModel(model, //
            () -> model.createResource(geomValue), //
            () -> model.createProperty(GEO + "asWKT"), //
            () -> model.createTypedLiteral("<" + getSrs(geom) + "> " + geom.toText(), new org.apache.jena.datatypes.BaseDatatype(GEO + "wktLiteral")));

        statements.add("DELETE WHERE { GRAPH <" + config.getGraph() + "> { <" + geomValue + "> <" + GEO + "asWKT" + "> ?obj}}");

      }
    }

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, config);
    }

    // // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteParent(Commit commit, RemoteGeoObjectSetParentEvent event, JenaExportConfig config, Model model)
  {
    logger.trace("Jena Projection - Handling remote set parent");

    String subjectUri = buildObjectUri(config, event.getCode(), event.getType());
    String edgeTypeUri = config.getNamespace() + "/" + event.getEdgeType();

    List<String> statements = new LinkedList<>();
    statements.add("DELETE WHERE { GRAPH <" + config.getGraph() + "> { <" + subjectUri + "> <" + edgeTypeUri + "> ?obj}}");

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, config);
    }

    if (!StringUtils.isBlank(event.getParentType()) && !StringUtils.isBlank(event.getParentCode()))
    {
      this.addResourceToModel(model, //
          subjectUri, //
          edgeTypeUri, //
          buildObjectUri(config, event.getParentCode(), event.getParentType()));

      // this.service.load(GRAPH_NAME, model, config);
    }
  }

  public void handleRemoteCreateEdge(Commit commit, RemoteGeoObjectCreateEdgeEvent event, JenaExportConfig config, Model model)
  {
    logger.trace("Jena Projection - Handling remote create edge");

    this.addResourceToModel(model, //
        buildObjectUri(config, event.getSourceCode(), event.getSourceType()), //
        config.getNamespace() + "/" + event.getEdgeTypeCode(), //
        buildObjectUri(config, event.getTargetCode(), event.getTargetType()));

    // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteBusinessObject(Commit commit, RemoteBusinessObjectEvent event, JenaExportConfig config, Model model)
  {
    logger.trace("Jena Projection - Handling remote business object");

    List<String> statements = new LinkedList<>();

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
          Object literal = null;

          String subjectUri = buildObjectUri(config, code, typeCode);
          String attributeUri = buildAttributeUri(config, typeCode, attribute);

          statements.add("DELETE WHERE { GRAPH <" + config.getGraph() + "> { <" + subjectUri + "> <" + attributeUri + "> ?obj}}");

          JsonElement element = data.get(attribute.getName());

          if (attribute instanceof AttributeLocalType)
          {
            LocalizedValue value = LocalizedValue.fromJSON(element.getAsJsonObject());

            literal = ( (LocalizedValue) value ).getValue();
          }
          else if (attribute instanceof AttributeIntegerType)
          {
            literal = element.getAsLong();
          }
          else if (attribute instanceof AttributeFloatType)
          {
            literal = element.getAsDouble();
          }
          else if (attribute instanceof AttributeDateType)
          {
            literal = GeoRegistryUtil.parseDate(element.getAsString());
          }
          else if (attribute instanceof AttributeBooleanType)
          {
            literal = element.getAsBoolean();
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

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, config);
    }

    // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteAddGeoObject(Commit commit, RemoteBusinessObjectAddGeoObjectEvent event, JenaExportConfig config, Model model)
  {
    logger.trace("Jena Projection - Handling remote add geo object");

    if (event.getDirection().equals(EdgeDirection.CHILD))
    {
      this.addResourceToModel(model, //
          buildObjectUri(config, event.getCode(), event.getType()), //
          config.getNamespace() + "/" + event.getEdgeType(), //
          buildObjectUri(config, event.getGeoObjectCode(), event.getGeoObjectType()));
    }
    else
    {
      this.addResourceToModel(model, //
          buildObjectUri(config, event.getGeoObjectCode(), event.getGeoObjectType()), //
          config.getNamespace() + "/" + event.getEdgeType(), //
          buildObjectUri(config, event.getCode(), event.getType()));

    }

    // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteCreateEdge(Commit commit, RemoteBusinessObjectCreateEdgeEvent event, JenaExportConfig config, Model model)
  {
    logger.trace("Jena Projection - Handling remote create edge");

    this.addResourceToModel(model, //
        buildObjectUri(config, event.getSourceCode(), event.getSourceType()), //
        config.getNamespace() + "/" + event.getEdgeType(), //
        buildObjectUri(config, event.getTargetCode(), event.getTargetType()));

    // this.service.load(GRAPH_NAME, model, config);
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

  protected String buildObjectUri(JenaExportConfig config, String code, final String typeCode)
  {
    return config.getNamespace() + "/" + typeCode + "-" + code;
  }

  protected String buildAttributeUri(JenaExportConfig config, String typeCode, AttributeType attribute)
  {
    if (attribute.getIsDefault())
    {
      if (attribute.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        return org.apache.jena.vocabulary.RDFS.label.getURI();
      }

      return config.getNamespace() + "/" + "GeoObject-" + attribute.getName();
    }

    return config.getNamespace() + "/" + typeCode + "-" + attribute.getName();
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

  protected void addLiteralToModel(Model model, String sub, String pred, Object obj)
  {
    Resource subject = model.createResource(sub);
    Property predicate = model.createProperty(pred);
    Literal object = model.createTypedLiteral(obj);

    // This GeoObject has a Geometry
    model.add(subject, predicate, object);
  }

  protected void addLiteralToModel(Model model, Supplier<Resource> subject, Supplier<Property> predicate, Supplier<Literal> literal)
  {
    // This GeoObject has a Geometry
    model.add(subject.get(), predicate.get(), literal.get());
  }

}
