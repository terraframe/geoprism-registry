/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service.business;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.view.RDFExport;

@Service
public class RepoRDFExportBusinessService
{
  public static interface CachedType
  {
    public String getCode();

    public String getOrgCode();

    public String getDBClassName();
  }

  public static class CachedGeoObjectType implements CachedType
  {
    public ServerGeoObjectType source;

    public CachedGeoObjectType(ServerGeoObjectType source)
    {
      this.source = source;
    }

    public String getDBClassName()
    {
      return this.source.getDBClassName();
    }

    @Override
    public String getCode()
    {
      return this.source.getCode();
    }

    @Override
    public String getOrgCode()
    {
      return this.source.getOrganizationCode();
    }
  }

  public static class CachedBusinessType implements CachedType
  {
    public BusinessType source;

    private String      orgCode;

    private MdVertex    mdVertex;

    public CachedBusinessType(BusinessType source)
    {
      this.source = source;
      this.mdVertex = this.source.getMdVertex();
      this.orgCode = source.getOrganization().getCode();
    }

    @Override
    public String getCode()
    {
      return this.source.getCode();
    }

    @Override
    public String getOrgCode()
    {
      return this.orgCode;
    }

    @Override
    public String getDBClassName()
    {
      return mdVertex.getDbClassName();
    }
  }

  public static class State
  {
    protected RDFExport                 config;

    protected String                    graphNamespace         = GeoprismProperties.getRemoteServerUrl() + "registry#";

    protected String                    graphMetadataNamespace = GeoprismProperties.getRemoteServerUrl() + "metadata#";

    protected String                    quadGraphName          = graphNamespace;

    protected Map<String, String>       prefixes               = new HashMap<String, String>();

    protected Map<String, CachedType>   typeCache              = new HashMap<String, CachedType>();

    protected StreamRDF                 writer;

    protected List<GraphType>           graphTypes;

    protected List<ServerGeoObjectType> gots;

    protected List<BusinessEdgeType>    businessEdgeTypes;

    protected List<BusinessType>        businessTypes;

    protected long                      total;

    protected long                      count;

    protected ClassificationCache       classiCache            = new ClassificationCache();

    protected String                    progressId;

    protected ImportHistory             monitor;
  }

  public static final String                RDF        = "rdf";

  public static final String                RDFS       = "rdfs";

  public static final String                DCTERMS    = "dcterms";

  public static final String                GEO        = "geo";

  public static final String                SF         = "sf";

  public static final long                  BLOCK_SIZE = 1000;

  private static final Logger               logger     = LoggerFactory.getLogger(RepoRDFExportBusinessService.class);

  @Autowired
  private ClassificationBusinessServiceIF   classificationService;

  @Autowired
  private GeoObjectBusinessServiceIF        objectService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  public void export(ImportHistory history, RDFExport config, OutputStream os)
  {
    State state = new State();
    state.config = config;
    state.graphTypes = config.getGraphTypes().stream().map(ref -> GraphType.resolve(ref)).toList();
    state.gots = config.getTypeCodes().stream().map(code -> ServerGeoObjectType.get(code)).toList();
    state.businessTypes = config.getBusinessTypeCodes().stream().map(code -> this.bTypeService.getByCode(code)).toList();
    state.businessEdgeTypes = config.getBusinessEdgeCodes().stream().map(code -> this.bEdgeService.getByCode(code)).toList();
    state.progressId = history.getOid();
    state.monitor = history;

    state.gots.forEach(type -> {
      CachedType cachedType = new CachedGeoObjectType(type);

      state.typeCache.put(cachedType.getDBClassName(), cachedType);
    });

    state.businessTypes.forEach(type -> {
      CachedType cachedType = new CachedBusinessType(type);

      state.typeCache.put(cachedType.getDBClassName(), cachedType);
    });

    long startTime = System.currentTimeMillis();

    queryTotal(state);

    state.count = 0L;

    updateProgress(state);

    try
    {
      logger.info("Begin rdf exporting " + state.total + " objects");

      state.writer = StreamRDFWriter.getWriterStream(os, RDFFormat.TURTLE_BLOCKS);

      state.writer.start();

      definePrefixes(state);

      writeMetadata(state);

      for (ServerGeoObjectType got : state.gots)
      {
        this.exportGot(state, got);
      }

      for (BusinessType type : state.businessTypes)
      {
        this.exportBusinessType(state, type);
      }

      for (GraphType graphType : state.graphTypes)
      {
        this.exportGraphType(state, graphType);
      }

      for (BusinessEdgeType graphType : state.businessEdgeTypes)
      {
        this.exportBusinessEdgeType(state, graphType);
      }

      state.count = state.total;
      this.updateProgress(state);

      logger.info("Finished rdf exporting: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");
    }
    finally
    {
      if (state.writer != null)
      {
        state.writer.finish();
      }

      ProgressService.remove(state.progressId);
    }
  }

  protected void exportGot(State state, ServerGeoObjectType got)
  {
    long skip = 0;
    boolean hasMoreData = true;

    do
    {
      logger.info("Exporting " + got.getCode() + " block " + skip + " through " + ( skip + BLOCK_SIZE ));

      VertexGeoObjectQuery query = new VertexGeoObjectQuery(got, null);
      query.setLimit((int) BLOCK_SIZE);
      query.setSkip(skip);
      // query.setIncludeGeometries(state.geomExportType !=
      // GeometryExportType.NO_GEOMETRIES);

      List<ServerGeoObjectIF> results = query.getResults();

      for (var go : results)
      {
        exportGeoObject(state, got, go);

        state.count++;

        if (state.count % 50 == 0)
        {
          updateProgress(state);
        }
      }

      skip += BLOCK_SIZE;

      hasMoreData = results.size() > 0;
      results = null; // Garbage collect me plz
    } while (hasMoreData);
  }

  protected void exportGraphType(State state, GraphType graphType)
  {
    this.exportEdgeType(state, graphType.getMdEdgeDAO().getDBClassName(), graphType.getCode());
  }

  protected void exportGeoObject(State state, ServerGeoObjectType type, ServerGeoObjectIF serverGo)
  {
    final String code = serverGo.getCode();
    final String orgCode = type.getOrganizationCode();

    // Write type information
    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.quadGraphName), //
        buildObjectUri(state, code, type.getCode(), orgCode, false), //
        org.apache.jena.vocabulary.RDF.type.asNode(), //
        NodeFactory.createURI(state.graphMetadataNamespace + type.getCode())));

    var go = this.objectService.toGeoObject(serverGo, null, false, state.classiCache);

    Map<String, AttributeType> attributes = go.getType().getAttributeMap();

    attributes.forEach((attributeName, attribute) -> {
      String literal = null;

      if (attribute instanceof AttributeTermType)
      {
        // Iterator<String> it = (Iterator<String>)
        // geoObject.getValue(attributeName);
        //
        // if (it.hasNext())
        // {
        // String code = it.next();
        //
        // Term root = ( (AttributeTermType) attribute ).getRootTerm();
        // String parent =
        // TermConverter.buildClassifierKeyFromTermCode(root.getCode());
        //
        // String classifierKey = Classifier.buildKey(parent, code);
        // Classifier classifier = Classifier.getByKey(classifierKey);
        //
        // node.setValue(attributeName, classifier.getOid());
        // }
        // else
        // {
        // node.setValue(attributeName, (String) null);
        // }
      }
      else if (attribute instanceof AttributeClassificationType)
      {
        String value = (String) go.getValue(attributeName);

        if (value != null)
        {
          String classificationTypeCode = ( (AttributeClassificationType) attribute ).getClassificationType();

          Classification classification = null;
          if (state.classiCache != null)
          {
            classification = state.classiCache.getClassification(classificationTypeCode, value.toString().trim());
          }

          if (classification == null)
          {
            classification = this.classificationService.get((AttributeClassificationType) attribute, value);

            if (classification != null && state.classiCache != null)
            {
              state.classiCache.putClassification(classificationTypeCode, value.toString().trim(), classification);
            }
          }

          literal = classification.getCode();
        }
        else
        {
          literal = null;
        }
      }
      else
      {
        Object value = go.getValue(attributeName);

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
        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            buildObjectUri(state, code, type.getCode(), orgCode, false), //
            NodeFactory.createURI(buildAttributeUri(state, type, orgCode, attribute)), //
            NodeFactory.createLiteral(literal)));
      }
    });

    boolean includeGeoms = !GeometryExportType.NO_GEOMETRIES.equals(state.config.getGeomExportType());
    if (includeGeoms)
    {
      Geometry geom = go.getGeometry();

      if (geom != null)
      {
        final boolean simplify = GeometryExportType.WRITE_SIMPLIFIED_GEOMETRIES.equals(state.config.getGeomExportType());
        final double MAX_POINTS = 10000;
        final int numPoints = simplify ? geom.getNumPoints() : -1;

        if (simplify && numPoints > MAX_POINTS)
        {
          double tolerance = Math.min(4d, 0.0000000006d * numPoints);
          geom = TopologyPreservingSimplifier.simplify(geom, tolerance);
          logger.info("Simplified geometry from " + numPoints + " to " + geom.getNumPoints());
        }

        // This GeoObject has a Geometry
        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            buildObjectUri(state, code, type.getCode(), orgCode, false), //
            NodeFactory.createURI(state.prefixes.get(GEO) + "hasGeometry"), //
            NodeFactory.createURI(state.graphNamespace + type.getCode() + "-" + code + "Geometry")));

        String srs_uri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        if (geom.getSRID() > 0)
        {
          srs_uri = "http://www.opengis.net/def/crs/EPSG/0/" + geom.getSRID();
        }

        // Write a Geometry Node
        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            NodeFactory.createURI(state.graphNamespace + type.getCode() + "-" + code + "Geometry"), //
            org.apache.jena.vocabulary.RDF.type.asNode(), //
            NodeFactory.createURI(state.prefixes.get(GEO) + "Geometry")));

        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            NodeFactory.createURI(state.graphNamespace + type.getCode() + "-" + code + "Geometry"), //
            org.apache.jena.vocabulary.RDF.type.asNode(), //
            NodeFactory.createURI(state.prefixes.get(SF) + geom.getClass().getSimpleName())));

        state.writer.quad(Quad.create(//
            NodeFactory.createURI(state.quadGraphName), //
            NodeFactory.createURI(state.graphNamespace + type.getCode() + "-" + code + "Geometry"), //
            NodeFactory.createURI(state.prefixes.get(GEO) + "asWKT"), //
            NodeFactory.createLiteral("<" + srs_uri + "> " + geom.toText(), new org.apache.jena.datatypes.BaseDatatype(state.prefixes.get(GEO) + "wktLiteral"))

        // The Jena GeoSPARQL Java API was found to be incompatible with our
        // stack due to the fact that we are using a GeoTools below v30.
        // Geotools v26.7 includes within in it embedded GeoAPI source (see
        // https://docs.geotools.org/stable/userguide/library/api/faq.html)
        // This embedded source code conflicts with the GeoAPI library
        // dependency which Jena GeoSPARQL brings in and causes compile errors
        // For this reason, we are instead manually building the literal
        // reference.
        // GeometryWrapperFactory.createGeometry(geom,
        // SRSInfo.convertSRID(geom.getSRID()),
        // WKTDatatype.INSTANCE.getURI()).asNode()
        ));
      }
    }
  }

  protected void exportBusinessObject(State state, BusinessType type, BusinessObject object)
  {
    final String code = type.getCode();
    final String orgCode = type.getOrganization().getCode();

    Map<String, AttributeType> attributes = type.getAttributeMap();

    attributes.forEach((attributeName, attribute) -> {
      String literal = null;

      if (attribute instanceof AttributeTermType)
      {
        // Iterator<String> it = (Iterator<String>)
        // geoObject.getValue(attributeName);
        //
        // if (it.hasNext())
        // {
        // String code = it.next();
        //
        // Term root = ( (AttributeTermType) attribute ).getRootTerm();
        // String parent =
        // TermConverter.buildClassifierKeyFromTermCode(root.getCode());
        //
        // String classifierKey = Classifier.buildKey(parent, code);
        // Classifier classifier = Classifier.getByKey(classifierKey);
        //
        // node.setValue(attributeName, classifier.getOid());
        // }
        // else
        // {
        // node.setValue(attributeName, (String) null);
        // }
      }
      else if (attribute instanceof AttributeClassificationType)
      {
        String value = (String) object.getObjectValue(attributeName);

        if (value != null)
        {
          String classificationTypeCode = ( (AttributeClassificationType) attribute ).getClassificationType();

          Classification classification = null;
          if (state.classiCache != null)
          {
            classification = state.classiCache.getClassification(classificationTypeCode, value.toString().trim());
          }

          if (classification == null)
          {
            classification = this.classificationService.get((AttributeClassificationType) attribute, value);

            if (classification != null && state.classiCache != null)
            {
              state.classiCache.putClassification(classificationTypeCode, value.toString().trim(), classification);
            }
          }

          literal = classification.getCode();
        }
        else
        {
          literal = null;
        }
      }
      else
      {
        Object value = object.getObjectValue(attributeName);

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
        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            buildObjectUri(state, code, type.getCode(), orgCode, false), //
            NodeFactory.createURI(buildAttributeUri(state, type.getCode(), attribute)), //
            NodeFactory.createLiteral(literal)));
      }
    });
  }

  protected void exportBusinessEdgeType(State state, BusinessEdgeType type)
  {
    final String dbClass = type.getMdEdgeDAO().getDBClassName();
    String edgeTypeCode = type.getCode();

    exportEdgeType(state, dbClass, edgeTypeCode);

  }

  protected void exportEdgeType(State state, final String dbClass, String edgeTypeCode)
  {
    long skip = 0;
    boolean hasMoreData = true;

    while (hasMoreData)
    {
      logger.info("Exporting " + edgeTypeCode + " block " + skip + " through " + ( skip + BLOCK_SIZE ));

      StringBuilder sb = new StringBuilder("SELECT in.@class AS in_class, in.code AS in_code, out.@class AS out_class, out.code AS out_code");
      sb.append(" FROM " + dbClass);
      sb.append(" ORDER BY out SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      List<Map<String, Object>> records = new GraphQuery<Map<String, Object>>(sb.toString()).getResults();

      for (Map<String, Object> record : records)
      {
        CachedType inType = state.typeCache.get(record.get("in_class"));
        CachedType outType = state.typeCache.get(record.get("out_class"));

        // Its possible that the in type or out type is not included in the graph export
        // As such we need to not include those edges in the graph export
        if (inType != null && outType != null)
        {
          final String inCode = record.get("in_code").toString();
          final String inTypeCode = inType.getCode();
          final String inOrgCode = inType.getOrgCode();

          final String outCode = record.get("out_code").toString();
          final String outTypeCode = outType.getCode();
          final String outOrgCode = outType.getOrgCode();

          state.writer.quad(Quad.create( //
              NodeFactory.createURI(state.quadGraphName), //
              buildObjectUri(state, outCode, outTypeCode, outOrgCode, false), //
              NodeFactory.createURI(buildGraphTypeUri(state, outOrgCode, edgeTypeCode)), //
              buildObjectUri(state, inCode, inTypeCode, inOrgCode, false)));
        }

        state.count++;

        if (state.count % 50 == 0)
        {
          updateProgress(state);
        }
      }

      skip += BLOCK_SIZE;

      hasMoreData = records.size() > 0;
      records = null; // Explicitly drop all references to the old data so that
                      // it can be GC'd
    }
  }

  protected void exportBusinessType(State state, BusinessType type)
  {
    final String dbClass = type.getMdVertex().getDbClassName();

    long skip = 0;
    boolean hasMoreData = true;

    while (hasMoreData)
    {
      logger.info("Exporting " + type.getCode() + " block " + skip + " through " + ( skip + BLOCK_SIZE ));

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT FROM " + dbClass);
      statement.append(" SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

      List<VertexObject> results = query.getResults();

      for (VertexObject vertex : results)
      {
        BusinessObject object = new BusinessObject(vertex, type);

        this.exportBusinessObject(state, type, object);

        state.count++;

        if (state.count % 50 == 0)
        {
          updateProgress(state);
        }

      }

      skip += BLOCK_SIZE;

      hasMoreData = results.size() > 0;
      results = null; // Explicitly drop all references to the old data so that
                      // it can be GC'd
    }
  }

  protected void definePrefixes(State state)
  {
    // IMPORTANT : As a little bit of a quirk of Jena, you are NOT allowed to
    // put a # or / in the URI outside the prefix (called the 'local' portion of
    // the URI).
    // If you want to use a # in the URI, it must be put in the prefix
    // This behaviour is a consequence of
    // org.apache.jena.riot.system.PrefixLib.isSafeLocalPart as invoked by
    // PrefixLib.abbrev
    // Due to this quirk, most prefixes here should probably end with a #
    // (unless they're only used in metadata and not instance data).
    // If for some reason a uri does have a # or a / outside the prefix, the
    // prefix will not be used.

    // prefixes.put(RDF, org.apache.jena.vocabulary.RDF.getURI()); // If we
    // include this Jena won't replace rdf:type with 'a', which is just a little
    // syntactic sugar
    state.prefixes.put(RDFS, org.apache.jena.vocabulary.RDFS.getURI());
    state.prefixes.put(DCTERMS, "http://purl.org/dc/terms/");
    state.prefixes.put(GEO, "http://www.opengis.net/ont/geosparql#");
    state.prefixes.put(SF, "http://www.opengis.net/ont/sf#");

    for (String key : state.prefixes.keySet())
    {
      state.writer.prefix(key, state.prefixes.get(key));
    }
  }

  protected void writeMetadata(State state)
  {
    // GeoObject definition
    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.graphMetadataNamespace), //
        NodeFactory.createURI(state.graphMetadataNamespace + "GeoObject"), //
        org.apache.jena.vocabulary.RDF.type.asNode(), //
        org.apache.jena.vocabulary.RDFS.Class.asNode()));

    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.graphMetadataNamespace), //
        NodeFactory.createURI(state.graphMetadataNamespace + "GeoObject"), //
        org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), //
        NodeFactory.createURI(state.prefixes.get(GEO) + "feature")));

    // Our graph has the following GeoObjects
    for (ServerGeoObjectType got : state.gots)
    {
      state.writer.quad(Quad.create(NodeFactory.createURI(state.graphMetadataNamespace), NodeFactory.createURI(state.graphMetadataNamespace + got.getCode()), NodeFactory.createURI(state.prefixes.get(RDFS) + "subClassOf"), NodeFactory.createURI(state.graphMetadataNamespace + "GeoObject")));
    }

    // Our graph has the following GraphTypes
    for (GraphType graphType : state.graphTypes)
    {
      state.writer.quad(Quad.create(NodeFactory.createURI(state.graphMetadataNamespace), NodeFactory.createURI(state.graphMetadataNamespace + graphType.getCode()), NodeFactory.createURI(state.prefixes.get(RDFS) + "subClassOf"), NodeFactory.createURI(state.graphMetadataNamespace + "GraphType")));
    }
  }

  protected String buildAttributeUri(State state, ServerGeoObjectType type, final String orgCode, AttributeType attribute)
  {
    return buildAttributeUri(state, type.getCode(), attribute);
  }

  protected String buildAttributeUri(State state, String typeCode, AttributeType attribute)
  {
    if (attribute.getIsDefault())
    {
      if (attribute.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        return state.prefixes.get(RDFS) + "label";
      }

      return state.graphMetadataNamespace + "GeoObject-" + attribute.getName();
    }

    return state.graphMetadataNamespace + typeCode + "-" + attribute.getName();
  }

  protected String buildGraphTypeUri(State state, final String orgCode, GraphType graphType)
  {
    return this.buildGraphTypeUri(state, orgCode, graphType.getCode());
  }

  protected String buildGraphTypeUri(State state, final String orgCode, String typeCode)
  {
    return state.graphMetadataNamespace + typeCode;
  }

  protected Node buildObjectUri(State state, String code, final String typeCode, final String orgCode, boolean includeType)
  {
    String uri = state.graphNamespace + typeCode + "-" + code;

    return NodeFactory.createURI(uri);
  }

  protected String buildQuadGraphName(State state)
  {
    return state.graphNamespace;
  }

  protected void queryTotal(State state)
  {
    state.total = 0;

    for (GraphType gt : state.graphTypes)
    {
      final String dbClassName = gt.getMdEdgeDAO().getDBClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }

    for (ServerGeoObjectType got : state.gots)
    {
      final String dbClassName = got.getMdVertex().getDBClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }

    for (BusinessType type : state.businessTypes)
    {
      final String dbClassName = type.getMdVertex().getDbClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }

    for (BusinessEdgeType type : state.businessEdgeTypes)
    {
      final String dbClassName = type.getMdEdge().getDbClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }
  }

  protected void updateProgress(State state)
  {
    if (state.monitor != null)
    {
      ImportStage stage = ImportStage.IMPORT;
      if (state.count == 0)
      {
        stage = ImportStage.NEW;
      }

      if (state.count == state.total)
      {
        stage = ImportStage.COMPLETE;
      }

      state.monitor.appLock();
      state.monitor.setWorkTotal(state.total);
      state.monitor.setWorkProgress(state.count);
      state.monitor.setImportedRecords(state.count);
      state.monitor.clearStage();
      state.monitor.addStage(stage);
      state.monitor.apply();

      ProgressService.put(state.progressId, new Progress(state.count, state.total, state.progressId));
    }
  }
}
