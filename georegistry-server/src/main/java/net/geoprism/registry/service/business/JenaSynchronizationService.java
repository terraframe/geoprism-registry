package net.geoprism.registry.service.business;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDataSourceType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geoprism.registry.Commit;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Publish;
import net.geoprism.registry.SynchronizationConfig;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectCreateEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.remote.RemoteObjectApplyEdgeEvent;
import net.geoprism.registry.axon.event.remote.RemoteObjectApplyEvent;
import net.geoprism.registry.axon.event.remote.RemoteObjectRemoveEdgeEvent;
import net.geoprism.registry.etl.JenaExportConfig;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

@Service
public class JenaSynchronizationService
{
  private static class ExportData
  {
    final AtomicLong       progress    = new AtomicLong(0);

    final JenaExportConfig config;

    private Set<String>    sources     = new TreeSet<String>();

    private Set<String>    authorities = new TreeSet<String>();

    final ExportHistory    history;

    public ExportData(ExportHistory history, JenaExportConfig config)
    {
      this.history = history;
      this.config = config;
    }

    public boolean isAuthorityExported(String source)
    {
      return this.authorities.contains(source);
    }

    public void addAuthority(String source)
    {
      this.authorities.add(source);
    }

    public boolean isSourceExported(String source)
    {
      return this.sources.contains(source);
    }

    public void addSource(String source)
    {
      this.sources.add(source);
    }
  }

  private static Logger                                      logger             = LoggerFactory.getLogger(JenaSynchronizationService.class);

  public static final String                                 GEO                = "http://www.opengis.net/ont/geosparql#";

  public static final String                                 SF                 = "http://www.opengis.net/ont/sf#";

  public static final Boolean                                INCLUDE_GEOMETRIES = true;

  @Autowired
  private SourceAuthorityBusinessServiceIF                   authorityService;

  @Autowired
  private DataSourceBusinessServiceIF                        sourceService;

  @Autowired
  private BusinessTypeBusinessServiceIF                      bTypeService;

  @Autowired
  private ConceptClassBusinessServiceIF                      cClassService;

  @Autowired
  private RemoteJenaServiceIF                                service;

  @Autowired
  private CommitBusinessServiceIF                            commitService;

  @Autowired
  private PublishBusinessServiceIF                           publishService;

  @Autowired
  private ConceptObjectBusinessServiceIF                     cObjectService;

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

      ExportData data = new ExportData(history, configuration);

      execute(synchronization, commit, data);

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

  protected void execute(SynchronizationConfig synchronization, Commit commit, ExportData data)
  {
    if (!this.exportService.hasBeenPublished(synchronization, commit))
    {
      // TODO: If commit is the first version then export out the metadata

      this.commitService.getDependencies(commit) //
          .stream() //
          .forEach(dependency -> this.execute(synchronization, dependency, data));

      if (data.history != null)
      {
        long count = this.commitService.getEventCount(commit) + 1;

        data.history.appLock();
        data.history.setWorkTotal(data.history.getWorkTotal() + count);
        data.history.apply();
      }

      AtomicReference<Model> model = new AtomicReference<Model>(ModelFactory.createDefaultModel());

      this.commitService.getRemoteEvents(commit).forEach(event -> {
        if (event instanceof RemoteGeoObjectEvent)
        {
          this.handleRemoteGeoObjectApply(commit, (RemoteGeoObjectEvent) event, data, model.get());
        }
        else if (event instanceof RemoteObjectApplyEvent)
        {
          this.handleRemoteObjectApply(commit, (RemoteObjectApplyEvent) event, data, model.get());
        }
        else if (event instanceof RemoteObjectApplyEdgeEvent)
        {
          this.handleRemoteCreateEdge(commit, (RemoteObjectApplyEdgeEvent) event, data, model.get());
        }
        else if (event instanceof RemoteObjectRemoveEdgeEvent)
        {
          this.handleRemoteRemoveEdge(commit, (RemoteObjectRemoveEdgeEvent) event, data, model.get());
        }
        else if (event instanceof RemoteGeoObjectCreateEdgeEvent)
        {
          this.handleRemoteCreateEdge(commit, (RemoteGeoObjectCreateEdgeEvent) event, data, model.get());
        }
        else if (event instanceof RemoteGeoObjectSetParentEvent)
        {
          this.handleRemoteParent(commit, (RemoteGeoObjectSetParentEvent) event, data, model.get());
        }

        long cWorkProgress = data.progress.incrementAndGet();

        if ( ( cWorkProgress % 1000 == 0 ))
        {
          // Push the model chunk to Jena
          this.service.load(model.get(), data.config);

          // Reset to an empty model
          model.set(ModelFactory.createDefaultModel());

          if (data.history != null)
          {
            data.history.appLock();
            data.history.setWorkProgress(cWorkProgress);
            data.history.setExportedRecords(cWorkProgress);
            data.history.apply();
          }
        }

      });

      this.commitService.getSources(commit).forEach(source -> {
        if (!data.isSourceExported(source.getCode()))
        {
          this.handleDataSource(this.sourceService.toDTO(source), data, model.get());

          data.addSource(source.getCode());
        }
      });

      // Push the model chunk to Jena
      this.service.load(model.get(), data.config);

      // Mark the commit as exported
      this.exportService.create(synchronization, commit);

      if (data.history != null)
      {
        data.history.appLock();
        data.history.setWorkProgress(data.progress.get());
        data.history.setExportedRecords(data.progress.get());
        data.history.apply();
      }
    }
    else

    {
      System.out.println("Skipping export of commit [" + commit.getUid() + "] because its already been exported");
    }
  }

  public void handleDataSource(DataSourceDTO source, ExportData data, Model model)
  {
    String subjectUri = buildObjectUri(data.config, source.getCode(), "DataSource");

    // Add type information
    this.addResourceToModel(model, //
        subjectUri, //
        org.apache.jena.vocabulary.RDF.type.getURI(), //
        buildTypeUri(data.config, "DataSource"));

    this.addLiteralToModel(model, //
        subjectUri, //
        buildAttributeUri(data.config, "DataSource", "code"), //
        source.getCode());

    if (StringUtils.isNotBlank(source.getLabel().getValue()))
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          org.apache.jena.vocabulary.RDFS.label.getURI(), //
          source.getLabel().getValue());
    }

    if (StringUtils.isNotBlank(source.getDescription().getValue()))
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "DataSource", "description"), //
          source.getDescription().getValue());
    }

    if (StringUtils.isNotBlank(source.getUri()))
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "DataSource", "uri"), //
          source.getUri());
    }

    if (source.getGovernanceLevel() != null)
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "DataSource", "governanceLevel"), //
          source.getGovernanceLevel().getName());
    }

    if (source.getMetadataProfile() != null)
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "DataSource", "metadataProfile"), //
          source.getMetadataProfile().getName());
    }

    if (StringUtils.isNotBlank(source.getAuthority()))
    {
      this.addResourceToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "DataSource", "authority"), //
          buildObjectUri(data.config, source.getAuthority(), "SourceAuthority"));

      if (!data.isAuthorityExported(source.getAuthority()))
      {
        this.authorityService.getByCode(source.getAuthority()).ifPresent(authority -> {
          this.handleSourceAuthority(this.authorityService.toDTO(authority), data, model);
        });

        data.addAuthority(source.getAuthority());
      }
    }

  }

  public void handleSourceAuthority(SourceAuthorityDTO source, ExportData data, Model model)
  {
    String subjectUri = buildObjectUri(data.config, source.getCode(), "SourceAuthority");

    // Add type information
    this.addResourceToModel(model, //
        subjectUri, //
        org.apache.jena.vocabulary.RDF.type.getURI(), //
        buildTypeUri(data.config, "SourceAuthority"));

    this.addLiteralToModel(model, //
        subjectUri, //
        buildAttributeUri(data.config, "SourceAuthority", "code"), //
        source.getCode());

    if (StringUtils.isNotBlank(source.getLabel().getValue()))
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          org.apache.jena.vocabulary.RDFS.label.getURI(), //
          source.getLabel().getValue());
    }

    if (StringUtils.isNotBlank(source.getDescription().getValue()))
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "SourceAuthority", "description"), //
          source.getDescription().getValue());
    }

    if (source.getAuthorityType() != null)
    {
      this.addLiteralToModel(model, //
          subjectUri, //
          buildAttributeUri(data.config, "SourceAuthority", "authorityType"), //
          source.getAuthorityType().getName());
    }

  }

  public void handleRemoteGeoObjectApply(Commit commit, RemoteGeoObjectEvent event, ExportData data, Model model)
  {
    logger.trace("Jena Projection - Handling remote geo object");

    List<String> statements = new LinkedList<>();

    final String code = event.getCode();
    final String typeCode = event.getType().getTypeCode();

    GeoObject dto = GeoObject.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    Map<String, AttributeType> attributes = dto.getType().getAttributeMap();

    // Add type information
    this.addResourceToModel(model, //
        buildObjectUri(data.config, code, typeCode), //
        org.apache.jena.vocabulary.RDF.type.getURI(), //
        buildTypeUri(data.config, typeCode));

    attributes.forEach((attributeName, attribute) -> {
      String subjectUri = buildObjectUri(data.config, code, typeCode);
      String attributeUri = buildAttributeUri(data.config, typeCode, attribute);

      statements.add("DELETE WHERE { GRAPH <" + data.config.getGraph() + "> { <" + subjectUri + "> <" + attributeUri + "> ?obj}}");

      Object literal = null;

      if (attribute instanceof AttributeGeometryType)
      {
        // SKIP
      }
      else if (attribute instanceof AttributeClassificationType)
      {
        String value = (String) dto.getValue(attributeName);

        if (StringUtils.isNotBlank(value))
        {
          this.cObjectService.getByCode(value).ifPresent(concept -> {
            this.addResourceToModel(model, //
                buildObjectUri(data.config, code, typeCode), //
                attributeUri, //
                buildObjectUri(data.config, concept.getCode(), concept.getType().getCode()));
          });
        }
      }
      else if (attribute instanceof AttributeDataSourceType)
      {
        String value = (String) dto.getValue(attributeName);

        if (StringUtils.isNotBlank(value))
        {
          this.sourceService.getByCode(value).ifPresent(source -> {
            this.addResourceToModel(model, //
                buildObjectUri(data.config, code, typeCode), //
                attributeUri, //
                buildObjectUri(data.config, source.getCode(), "DataSource"));
          });
        }
      }
      else
      {
        Object value = dto.getValue(attributeName);

        if (value instanceof LocalizedValue)
        {
          literal = ( (LocalizedValue) value ).getValue();
        }
        else
        {
          literal = value;
        }

        if (literal != null)
        {
          this.addLiteralToModel(model, //
              subjectUri, //
              attributeUri, //
              literal);
        }

      }

    });

    if (INCLUDE_GEOMETRIES)
    {
      Geometry geom = dto.getGeometry();

      if (geom != null)
      {
        this.addResourceToModel(model, //
            buildObjectUri(data.config, code, typeCode), //
            buildHasGeometryPredicate(), //
            buildObjectUri(data.config, code + "Geometry", typeCode));

        this.addResourceToModel(model, //
            () -> model.createResource(buildObjectUri(data.config, code + "Geometry", typeCode)), //
            () -> org.apache.jena.vocabulary.RDF.type, //
            () -> model.createResource(GEO + "Geometry"));

        this.addResourceToModel(model, //
            () -> model.createResource(buildObjectUri(data.config, code + "Geometry", typeCode)), //
            () -> org.apache.jena.vocabulary.RDF.type, //
            () -> model.createResource(SF + geom.getClass().getSimpleName()));

        final String geomValue = buildObjectUri(data.config, code + "Geometry", typeCode);

        this.addLiteralToModel(model, //
            () -> model.createResource(geomValue), //
            () -> model.createProperty(GEO + "asWKT"), //
            () -> model.createTypedLiteral("<" + getSrs(geom) + "> " + geom.toText(), new org.apache.jena.datatypes.BaseDatatype(GEO + "wktLiteral")));

        statements.add("DELETE WHERE { GRAPH <" + data.config.getGraph() + "> { <" + geomValue + "> <" + GEO + "asWKT" + "> ?obj}}");

      }
    }

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, data.config);
    }

    // // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteParent(Commit commit, RemoteGeoObjectSetParentEvent event, ExportData data, Model model)
  {
    logger.trace("Jena Projection - Handling remote set parent");

    String subjectUri = buildObjectUri(data.config, event.getCode(), event.getType());
    String edgeTypeUri = data.config.getNamespace() + "#" + event.getEdgeType();

    List<String> statements = new LinkedList<>();
    statements.add("DELETE WHERE { GRAPH <" + data.config.getGraph() + "> { <" + subjectUri + "> <" + edgeTypeUri + "> ?obj}}");

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, data.config);
    }

    if (event.getParentType() != null && !StringUtils.isBlank(event.getParentCode()))
    {
      this.addResourceToModel(model, //
          subjectUri, //
          edgeTypeUri, //
          buildObjectUri(data.config, event.getParentCode(), event.getParentType()));

      // this.service.load(GRAPH_NAME, model, config);
    }
  }

  public void handleRemoteCreateEdge(Commit commit, RemoteGeoObjectCreateEdgeEvent event, ExportData data, Model model)
  {
    logger.trace("Jena Projection - Handling remote create edge");

    this.addResourceToModel(model, //
        buildObjectUri(data.config, event.getSourceCode(), event.getSourceType()), //
        data.config.getNamespace() + "#" + event.getEdgeType().getTypeCode(), //
        buildObjectUri(data.config, event.getTargetCode(), event.getTargetType()));

    // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteObjectApply(Commit commit, RemoteObjectApplyEvent event, ExportData data, Model model)
  {
    logger.trace("Jena Projection - Handling remote business object");

    ObjectClass type = event.getType().getTypeClass().equals(TypeClass.BUSINESS_TYPE) ? //
        this.bTypeService.getByCodeOrThrow(event.getType()) : //
        this.cClassService.getByCodeOrThrow(event.getType());

    handleRemoteObject(commit, event, data, model, type);
  }

  @SuppressWarnings("unchecked")
  public void handleRemoteObject(Commit commit, RemoteObjectApplyEvent event, ExportData data, Model model, ObjectClass type)
  {
    List<String> statements = new LinkedList<>();

    final String typeCode = event.getType().getTypeCode();
    final String code = event.getCode();
    ObjectAtTimeDTO dto = event.getObject();

    Map<String, AttributeType> attributes = type.getAttributeMapAsDTO();

    attributes.values().stream() //
        .filter(a -> dto.has(a.getCode())) //
        .filter(a -> ! ( a instanceof AttributeGeometryType )) //
        .forEach(attribute -> {
          Object literal = null;

          String subjectUri = buildObjectUri(data.config, code, typeCode);
          String attributeUri = buildAttributeUri(data.config, typeCode, attribute);

          statements.add("DELETE WHERE { GRAPH <" + data.config.getGraph() + "> { <" + subjectUri + "> <" + attributeUri + "> ?obj}}");

          if (attribute instanceof AttributeClassificationType)
          {
            String value = (String) dto.getValue(attribute.getCode());

            if (StringUtils.isNotBlank(value))
            {
              this.cObjectService.getByCode(value).ifPresent(concept -> {
                this.addResourceToModel(model, //
                    buildObjectUri(data.config, code, typeCode), //
                    attributeUri, //
                    buildObjectUri(data.config, concept.getCode(), concept.getType().getCode()));
              });
            }
          }
          else if (attribute instanceof AttributeDataSourceType)
          {
            String value = (String) dto.getValue(attribute.getCode());

            if (StringUtils.isNotBlank(value))
            {
              this.sourceService.getByCode(value).ifPresent(source -> {
                this.addResourceToModel(model, //
                    buildObjectUri(data.config, code, typeCode), //
                    attributeUri, //
                    buildObjectUri(data.config, source.getCode(), "DataSource"));
              });
            }
          }
          else
          {
            Object value = dto.getValue(attribute.getCode());

            if (attribute instanceof AttributeLocalType)
            {
              Map<String, String> values = (Map<String, String>) value;

              literal = values.get(LocalizedValue.LOCALIZED_VALUE);
            }

            else if (attribute instanceof AttributeIntegerType)
            {
              literal = (Long) value;
            }
            else if (attribute instanceof AttributeFloatType)
            {
              literal = (Double) value;
            }
            else if (attribute instanceof AttributeDateType)
            {
              literal = (Date) value;
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              literal = (Boolean) value;
            }
            else
            {
              literal = value.toString();
            }

            if (literal != null)
            {
              this.addLiteralToModel(model, //
                  subjectUri, //
                  attributeUri, //
                  literal);
            }
          }
        });

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, data.config);
    }

    // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteCreateEdge(Commit commit, RemoteObjectApplyEdgeEvent event, ExportData data, Model model)
  {
    logger.trace("Jena Projection - Handling remote create edge");

    this.addResourceToModel(model, //
        buildObjectUri(data.config, event.getSourceCode(), event.getSourceType().getTypeCode()), //
        data.config.getNamespace() + "#" + event.getEdgeType(), //
        buildObjectUri(data.config, event.getTargetCode(), event.getTargetType().getTypeCode()));

    // this.service.load(GRAPH_NAME, model, config);
  }

  public void handleRemoteRemoveEdge(Commit commit, RemoteObjectRemoveEdgeEvent event, ExportData data, Model model)
  {
    logger.trace("Jena Projection - Handling remote remove edge");

    String subjectUri = buildObjectUri(data.config, event.getSourceCode(), event.getSourceType().getTypeCode());
    String edgeUri = data.config.getNamespace() + "#" + event.getEdgeType();
    String objectUri = buildObjectUri(data.config, event.getTargetCode(), event.getTargetType().getTypeCode());

    List<String> statements = new LinkedList<>();
    statements.add("DELETE WHERE { GRAPH <" + data.config.getGraph() + "> { <" + subjectUri + "> <" + edgeUri + "> <" + objectUri + ">}}");

    if (!commit.getVersionNumber().equals(Integer.valueOf(1)))
    {
      this.service.update(statements, data.config);
    }
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

  protected String buildTypeUri(JenaExportConfig config, final String typeCode)
  {
    return config.getNamespace() + "#" + typeCode;
  }

  protected String buildObjectUri(JenaExportConfig config, String code, final TypeInfo type)
  {
    return this.buildObjectUri(config, code, type.getTypeCode());
  }

  protected String buildObjectUri(JenaExportConfig config, String code, final String typeCode)
  {
    return config.getNamespace() + "#" + typeCode + "-" + IRILib.encodeUriComponent(code);
  }

  protected String buildAttributeUri(JenaExportConfig config, String typeCode, AttributeType attribute)
  {
    if (attribute.isDefault())
    {
      if (attribute.getCode().equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        return org.apache.jena.vocabulary.RDFS.label.getURI();
      }

      return config.getNamespace() + "#" + "GeoObject-" + attribute.getCode();
    }

    return config.getNamespace() + "#" + typeCode + "-" + attribute.getCode();
  }

  protected String buildAttributeUri(JenaExportConfig config, String typeCode, String code)
  {
    return config.getNamespace() + "#" + typeCode + "-" + code;
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
