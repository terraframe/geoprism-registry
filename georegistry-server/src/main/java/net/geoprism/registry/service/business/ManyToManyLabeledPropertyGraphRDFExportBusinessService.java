/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDataSourceType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.service.business.GraphPublisherService.CachedBusinessSnapshot;
import net.geoprism.registry.service.business.GraphPublisherService.CachedGOTSnapshot;
import net.geoprism.registry.service.business.GraphPublisherService.CachedSnapshot;

@Service
@Primary
public class ManyToManyLabeledPropertyGraphRDFExportBusinessService implements LabeledPropertyGraphRDFExportBusinessServiceIF
{
  private static class State
  {
    protected Map<String, String>             prefixes = new HashMap<String, String>();

    protected LabeledPropertyGraphTypeVersion version;

    protected LabeledPropertyGraphTypeEntry   entry;

    protected LabeledPropertyGraphType        lpg;

    protected StreamRDF                       writer;

    protected List<CachedGraphTypeSnapshot>   graphTypes;

    protected List<BusinessEdgeTypeSnapshot>  businessEdges;

    protected Map<String, CachedSnapshot>     snapshotCache;

    protected long                            total;

    protected long                            count;

    protected String                          quadGraphName;

    protected GeometryExportType              geomExportType;

    public ImportHistory                      history;

    protected List<DataSource>                sources;
  }

  public static final String                               LPG        = "lpg";

  public static final String                               LPGS       = "lpgs";

  public static final String                               LPGV       = "lpgv";

  public static final String                               LPGVS      = "lpgvs";

  public static final String                               RDF        = "rdf";

  public static final String                               RDFS       = "rdfs";

  public static final String                               DCTERMS    = "dcterms";

  public static final String                               GEO        = "geo";

  public static final String                               SF         = "sf";

  public static final long                                 BLOCK_SIZE = 1000;

  private Logger                                           logger     = LoggerFactory.getLogger(ManyToManyLabeledPropertyGraphRDFExportBusinessService.class);

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;

  @Autowired
  private ClassificationBusinessServiceIF                  classificationService;

  @Autowired
  private DataSourceBusinessServiceIF                      sourceService;

  @Override
  public void export(ImportHistory history, LabeledPropertyGraphTypeVersion version, GeometryExportType geomExportType, OutputStream os)
  {
    State state = new State();
    state.history = history;
    state.geomExportType = geomExportType;
    state.version = version;
    state.entry = version.getEntry();
    state.lpg = state.entry.getGraphType();

    long startTime = System.currentTimeMillis();

    cacheMetadata(state, version);

    queryTotal(state);

    updateProgress(state);

    try
    {
      logger.info("Begin rdf exporting " + state.total + " objects");

      state.writer = StreamRDFWriter.getWriterStream(os, RDFFormat.TURTLE_BLOCKS);

      long skip = 0;

      state.writer.start();

      definePrefixes(state);

      writeLPGMetadata(state);

      boolean hasMoreData = true;

      while (hasMoreData)
      {
        int count = this.exportGeoObjectTypes(state, version, skip);

        hasMoreData = count > 0;

        skip += BLOCK_SIZE;
      }

      // Export all business types
      state.snapshotCache.values().stream().map(c -> c.toBusiness()).filter(c -> c != null).forEach(cached -> {
        this.exportBusinessType(state, version, cached.type);
      });

      state.graphTypes.stream().forEach(graphType -> {
        String dbClassName = graphType.graphMdEdge.getDbClassName();

        this.exportEdgeType(state, version, graphType.graphType.getCode(), dbClassName);
      });

      state.businessEdges.stream().forEach(snapshot -> {
        String dbClassName = snapshot.getGraphMdEdge().getDbClassName();

        this.exportEdgeType(state, version, snapshot.getCode(), dbClassName);
      });

      logger.info("Finished rdf exporting: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");

      state.count = state.total;
      this.updateProgress(state);
    }
    finally
    {
      if (state.writer != null)
      {
        state.writer.finish();
      }

      if (state.history != null)
    	  ProgressService.remove(state.history.getOid());
    }
  }

  protected void exportEdgeType(State state, LabeledPropertyGraphTypeVersion version, String typeCode, String dbClassName)
  {
    long skip = 0;

    boolean hasMoreData = true;

    while (hasMoreData)
    {
      StringBuilder sb = new StringBuilder("SELECT in.@class AS in_class, in.code AS in_code, out.@class AS out_class, out.code AS out_code");
      sb.append(" FROM " + dbClassName);
      sb.append(" ORDER BY out SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      List<Map<String, Object>> records = new GraphQuery<Map<String, Object>>(sb.toString()).getResults();

      for (Map<String, Object> record : records)
      {
        CachedSnapshot inType = state.snapshotCache.get(record.get("in_class"));
        final String inCode = record.get("in_code").toString();
        final String inTypeCode = inType.getCode();
        final String inOrgCode = inType.getOrgCode();

        CachedSnapshot outType = state.snapshotCache.get(record.get("out_class"));
        final String outCode = record.get("out_code").toString();
        final String outTypeCode = outType.getCode();
        final String outOrgCode = outType.getOrgCode();

        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            buildObjectUri(state, outCode, outTypeCode, outOrgCode, false), //
            NodeFactory.createURI(buildGraphTypeUri(state, outOrgCode, typeCode)), //
            buildObjectUri(state, inCode, inTypeCode, inOrgCode, false)));

        state.count++;

        if (state.count % 50 == 0)
        {
          updateProgress(state);
        }
      }

      skip += BLOCK_SIZE;

      hasMoreData = records.size() > 0;
      records = null;
    }

  }

  protected void exportBusinessType(State state, LabeledPropertyGraphTypeVersion version, BusinessTypeSnapshot snapshot)
  {
    long skip = 0;

    boolean hasMoreData = true;

    while (hasMoreData)
    {
      StringBuilder statement = new StringBuilder("SELECT *, @class as clazz");

      state.snapshotCache.values().stream().map(c -> c.toType()).filter(c -> c != null).map(c -> c.type).forEach(got -> {
        if (!got.isRoot())
        {
          got.getAttributeTypes().stream().filter(t -> t instanceof AttributeClassificationType).forEach(attribute -> {
            statement.append(", " + attribute.getName() + ".displayLabel.defaultLocale as " + attribute.getName() + "_l");
          });
        }
      });

      statement.append(" FROM " + snapshot.getGraphMdVertex().getDbClassName());
      statement.append(" ORDER BY out SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      List<Map<String, Object>> records = new GraphQuery<Map<String, Object>>(statement.toString()).getResults();

      for (Map<String, Object> record : records)
      {
        exportBusinessObject(state, version, record);

        state.count++;

        if (state.count % 50 == 0)
        {
          updateProgress(state);
        }
      }

      skip += BLOCK_SIZE;
      hasMoreData = records.size() > 0;
      records = null;
    }

  }

  protected int exportGeoObjectTypes(State state, LabeledPropertyGraphTypeVersion version, Long skip)
  {
    GeoObjectTypeSnapshot rootType = this.versionService.getRootType(version);
    MdVertex mdVertex = rootType.getGraphMdVertex();

    version.lock();

    LabeledPropertyGraphType type = version.getGraphType();

    try
    {
      if (!type.isValid())
      {
        throw new InvalidMasterListException();
      }

      logger.info("Exporting block " + skip + " through " + ( skip + BLOCK_SIZE ));

      StringBuilder sb = new StringBuilder("SELECT *, @class as clazz");

      state.snapshotCache.values().stream().map(c -> c.toType()).filter(c -> c != null).map(c -> c.type).forEach(got -> {
        if (!got.isRoot())
        {
          got.getAttributeTypes().stream().filter(t -> t instanceof AttributeLocalType).forEach(attribute -> {
            sb.append(", " + attribute.getName() + ".displayLabel.defaultLocale as " + attribute.getName() + "_l");
          });

          got.getAttributeTypes().stream().filter(t -> t instanceof AttributeDataSourceType).forEach(attribute -> {
            sb.append(", " + attribute.getName() + ".code as " + attribute.getName() + "_c");
          });
        }
      });

      sb.append(" FROM " + mdVertex.getDbClassName());
      sb.append(" ORDER BY oid SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      List<Map<String, Object>> vertexes = new GraphQuery<Map<String, Object>>(sb.toString()).getResults();

      for (Map<String, Object> valueMap : vertexes)
      {
        exportGeoObject(state, version, valueMap);

        state.count++;

        if (state.count % 50 == 0)
        {
          updateProgress(state);
        }
      }

      return vertexes.size();
    }
    finally
    {
      version.unlock();
    }
  }

  protected void exportGeoObject(State state, LabeledPropertyGraphTypeVersion version, Map<String, Object> valueMap)
  {
    final String code = valueMap.get("code").toString();
    final GeoObjectTypeSnapshot type = state.snapshotCache.get(valueMap.get("clazz")).toType().type;
    final String orgCode = type.getOrgCode();

    // Write type information
    state.writer.quad(Quad.create(NodeFactory.createURI(state.quadGraphName), //
        buildObjectUri(state, code, type.getCode(), orgCode, false), //
        org.apache.jena.vocabulary.RDF.type.asNode(), //
        NodeFactory.createURI(state.prefixes.get(LPGVS) + type.getCode())));

    type.getAttributeTypes().forEach(attribute -> {
      exportObjectValue(state, valueMap, code, type.getCode(), orgCode, attribute);
    });

    boolean includeGeoms = !GeometryExportType.NO_GEOMETRIES.equals(state.geomExportType);
    if (includeGeoms && valueMap.containsKey(DefaultAttribute.GEOMETRY.getName()))
    {
      Geometry geom = (Geometry) valueMap.get(DefaultAttribute.GEOMETRY.getName());

      if (geom != null)
      {
        final boolean simplify = GeometryExportType.WRITE_SIMPLIFIED_GEOMETRIES.equals(state.geomExportType);
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
            NodeFactory.createURI(state.prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry")));

        String srs_uri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        if (geom.getSRID() > 0)
        {
          srs_uri = "http://www.opengis.net/def/crs/EPSG/0/" + geom.getSRID();
        }

        // Write a Geometry Node
        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            NodeFactory.createURI(state.prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry"), //
            org.apache.jena.vocabulary.RDF.type.asNode(), //
            NodeFactory.createURI(state.prefixes.get(GEO) + "Geometry")));

        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.quadGraphName), //
            NodeFactory.createURI(state.prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry"), //
            org.apache.jena.vocabulary.RDF.type.asNode(), //
            NodeFactory.createURI(state.prefixes.get(SF) + geom.getClass().getSimpleName())));

        state.writer.quad(Quad.create(//
            NodeFactory.createURI(state.quadGraphName), //
            NodeFactory.createURI(state.prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry"), //
            NodeFactory.createURI(state.prefixes.get(GEO) + "asWKT"), //
            NodeFactory.createLiteral("<" + srs_uri + "> " + geom.toText(), //
                new org.apache.jena.datatypes.BaseDatatype(state.prefixes.get(GEO) + "wktLiteral"))

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

  protected void exportBusinessObject(State state, LabeledPropertyGraphTypeVersion version, Map<String, Object> valueMap)
  {
    final String code = valueMap.get("code").toString();
    final BusinessTypeSnapshot type = state.snapshotCache.get(valueMap.get("clazz")).toBusiness().type;
    final String orgCode = type.getOrgCode();

    // Write type information
    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.quadGraphName), //
        buildObjectUri(state, code, type.getCode(), orgCode, false), //
        org.apache.jena.vocabulary.RDF.type.asNode(), //
        NodeFactory.createURI(state.prefixes.get(LPGVS) + type.getCode())));

    type.getAttributeTypes().forEach(attribute -> {
      exportObjectValue(state, valueMap, code, type.getCode(), orgCode, attribute);
    });
  }

  protected void exportObjectValue(State state, Map<String, Object> valueMap, final String code, final String typeCode, final String orgCode, AttributeType attribute)
  {
    if (valueMap.containsKey(attribute.getName()))
    {
      if (attribute instanceof AttributeIntegerType)
      {
        Object value = valueMap.get(attribute.getName());

        if (value != null)
        {
          state.writer.quad(Quad.create( //
              NodeFactory.createURI(state.quadGraphName), //
              buildObjectUri(state, code, typeCode, orgCode, false), //
              NodeFactory.createURI(buildAttributeUri(state, typeCode, orgCode, attribute)), //
              NodeFactory.createLiteralByValue(value, XSDDatatype.XSDlong)));
        }
      }
      else if (attribute instanceof AttributeFloatType)
      {
        Object value = valueMap.get(attribute.getName());

        if (value != null)
        {
          state.writer.quad(Quad.create( //
              NodeFactory.createURI(state.quadGraphName), //
              buildObjectUri(state, code, typeCode, orgCode, false), //
              NodeFactory.createURI(buildAttributeUri(state, typeCode, orgCode, attribute)), //
              NodeFactory.createLiteralByValue(value, XSDDatatype.XSDdouble)));
        }
      }
      else if (attribute instanceof AttributeDateType)
      {
        Object value = valueMap.get(attribute.getName());

        if (value != null)
        {
          state.writer.quad(Quad.create( //
              NodeFactory.createURI(state.quadGraphName), //
              buildObjectUri(state, code, typeCode, orgCode, false), //
              NodeFactory.createURI(buildAttributeUri(state, typeCode, orgCode, attribute)), //
              NodeFactory.createLiteralByValue(value, XSDDatatype.XSDdateTime)));
        }
      }
      else if (attribute instanceof AttributeBooleanType)
      {
        Object value = valueMap.get(attribute.getName());

        if (value != null)
        {
          state.writer.quad(Quad.create(//
              NodeFactory.createURI(state.quadGraphName), //
              buildObjectUri(state, code, typeCode, orgCode, false), //
              NodeFactory.createURI(buildAttributeUri(state, typeCode, orgCode, attribute)), //
              NodeFactory.createLiteralByValue(value, XSDDatatype.XSDboolean)));
        }
      }
      else if (attribute instanceof AttributeDataSourceType)
      {
        Object value = valueMap.get(attribute.getName() + "_c");

        if (value != null)
        {
          state.writer.quad( //
              Quad.create( //
                  NodeFactory.createURI(state.quadGraphName), //
                  buildObjectUri(state, code, typeCode, orgCode, false), //
                  NodeFactory.createURI(buildAttributeUri(state, typeCode, orgCode, attribute)), //
                  NodeFactory.createURI(buildSourceUri(state, (String) value))));
        }
      }
      else
      {

        String literal = null;

        if (attribute instanceof AttributeLocalType)
        {
          @SuppressWarnings("unchecked") Map<String, String> value = (Map<String, String>) valueMap.get(attribute.getName());

          literal = value.get(MdAttributeLocalInfo.DEFAULT_LOCALE);
        }
        else if (attribute instanceof AttributeClassificationType)
        {
          String value = (String) valueMap.get(attribute.getName() + "_l");

          if (value != null)
          {
            Classification classification = this.classificationService.get((AttributeClassificationType) attribute, value).get();

            if (classification != null)
            {
              literal = classification.getDisplayLabel().getValue();
            }
          }
        }
        else
        {
          Object value = valueMap.get(attribute.getName());

          literal = value == null ? null : value.toString();
        }

        if (literal != null)
        {
          state.writer.quad(Quad.create( //
              NodeFactory.createURI(state.quadGraphName), //
              buildObjectUri(state, code, typeCode, orgCode, false), //
              NodeFactory.createURI(buildAttributeUri(state, typeCode, orgCode, attribute)), //
              NodeFactory.createLiteral(literal)));
        }
      }
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

    state.prefixes.put(LPG, GeoprismProperties.getRemoteServerUrl() + "lpg#");
    state.prefixes.put(LPGS, GeoprismProperties.getRemoteServerUrl() + "lpg/rdfs#");

    final String lpgv = GeoprismProperties.getRemoteServerUrl() + "lpg/" + state.version.getGraphType().getCode() + "/" + state.version.getVersionNumber();
    state.prefixes.put(LPGV, lpgv + "#");
    state.prefixes.put(LPGVS, lpgv + "/rdfs#");

    for (String key : state.prefixes.keySet())
    {
      state.writer.prefix(key, state.prefixes.get(key));
    }

    state.quadGraphName = buildQuadGraphName(state, state.version);
  }

  protected void writeLPGMetadata(State state)
  {
    // Our exported LPG is of type 'LabeledPropertyGraph'
    state.writer.quad(Quad.create(//
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        org.apache.jena.vocabulary.RDF.type.asNode(), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "LabeledPropertyGraph")));

    // Our LPG has a code
    state.writer.quad(Quad.create(//
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "code"), //
        NodeFactory.createLiteral(state.lpg.getCode())));

    // Our LPG has a version number
    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "versionNumber"), //
        NodeFactory.createLiteral(state.version.getVersionNumber().toString())));

    // Our LPG has a label with value..
    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        org.apache.jena.vocabulary.RDFS.label.asNode(), //
        NodeFactory.createLiteral(state.lpg.getDisplayLabel().getValue())));

    // Our LPG has a description with value..
    state.writer.quad(Quad.create(//
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        NodeFactory.createURI(state.prefixes.get(DCTERMS) + "description"), //
        NodeFactory.createLiteral(state.lpg.getDescription().getValue())));

    // Our LPG has a for date with value..
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateLabel = dateFormatter.format(state.version.getForDate());

    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "forDate"), //
        NodeFactory.createLiteral(dateLabel)));

    // Our LPG has an associated Organization with code..
    state.writer.quad(Quad.create(//
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGV)), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "orgCode"), //
        NodeFactory.createLiteral(state.lpg.getOrganization().getCode())));

    // GeoObject definition
    state.writer.quad(Quad.create(//
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "GeoObject"), //
        org.apache.jena.vocabulary.RDF.type.asNode(), //
        org.apache.jena.vocabulary.RDFS.Class.asNode()));

    state.writer.quad(Quad.create( //
        NodeFactory.createURI(state.prefixes.get(LPG)), //
        NodeFactory.createURI(state.prefixes.get(LPGS) + "GeoObject"), //
        org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), //
        NodeFactory.createURI(state.prefixes.get(GEO) + "feature")));

    // Our LPG has the following GeoObjects
    state.snapshotCache.values().stream().map(c -> c.toType()).filter(c -> c != null).map(c -> c.type).forEach(got -> {
      if (!got.isRoot())
      {
        state.writer.quad(Quad.create( //
            NodeFactory.createURI(state.prefixes.get(LPG)), //
            NodeFactory.createURI(state.prefixes.get(LPGVS) + got.getCode()), //
            NodeFactory.createURI(state.prefixes.get(RDFS) + "subClassOf"), //
            NodeFactory.createURI(state.prefixes.get(LPGS) + "GeoObject")));
      }
    });

    // Our LPG has the following Business Types
    state.snapshotCache.values().stream().map(c -> c.toBusiness()).filter(c -> c != null).map(c -> c.type).forEach(snapshot -> {
      state.writer.quad(Quad.create( //
          NodeFactory.createURI(state.prefixes.get(LPG)), //
          NodeFactory.createURI(state.prefixes.get(LPGVS) + snapshot.getCode()), //
          NodeFactory.createURI(state.prefixes.get(RDFS) + "subClassOf"), //
          NodeFactory.createURI(state.prefixes.get(LPGS) + "BusinessObject")));
    });

    // Our LPG has the following GraphTypes
    for (CachedGraphTypeSnapshot graphType : state.graphTypes)
    {
      state.writer.quad(Quad.create(//
          NodeFactory.createURI(state.prefixes.get(LPG)), //
          NodeFactory.createURI(state.prefixes.get(LPGVS) + graphType.graphType.getCode()), //
          NodeFactory.createURI(state.prefixes.get(RDFS) + "subClassOf"), //
          NodeFactory.createURI(state.prefixes.get(LPGS) + "GraphType")));
    }
  }

  protected String buildSourceUri(State state, String code)
  {
    return state.prefixes.get(LPGVS) + "Source-" + code;
  }

  protected String buildAttributeUri(State state, final String typeCode, final String orgCode, AttributeType attribute)
  {
    if (attribute.getIsDefault())
    {
      if (attribute.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        return state.prefixes.get(RDFS) + "label";
      }

      return state.prefixes.get(LPGS) + "GeoObject-" + attribute.getName();
    }

    return state.prefixes.get(LPGVS) + typeCode + "-" + attribute.getName();
  }

  protected String buildAttributeUri(State state, final GeoObjectTypeSnapshot type, final String orgCode, AttributeType attribute)
  {
    return this.buildAttributeUri(state, type.getCode(), orgCode, attribute);
  }

  protected String buildGraphTypeUri(State state, final String orgCode, String typeCode)
  {
    return state.prefixes.get(LPGVS) + typeCode;
  }

  protected Node buildObjectUri(State state, String code, final String typeCode, final String orgCode, boolean includeType)
  {
    try
    {
      String uri = state.prefixes.get(LPGV) + typeCode + "-" + URLEncoder.encode(code, "UTF-8");

      return NodeFactory.createURI(uri);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  protected String buildQuadGraphName(State state, LabeledPropertyGraphTypeVersion version)
  {
    return state.prefixes.get(LPGV);
  }

  protected void cacheMetadata(State state, LabeledPropertyGraphTypeVersion version)
  {
    state.snapshotCache = new HashMap<String, CachedSnapshot>();

    this.versionService.getTypes(version).forEach(snapshot -> {
      state.snapshotCache.put(snapshot.getGraphMdVertex().getDbClassName(), new CachedGOTSnapshot(null, snapshot));
    });

    this.versionService.getBusinessTypes(version).forEach(snapshot -> {
      state.snapshotCache.put(snapshot.getGraphMdVertex().getDbClassName(), new CachedBusinessSnapshot(null, snapshot));
    });

    state.graphTypes = versionService.getGraphSnapshots(version).stream().map(s -> new CachedGraphTypeSnapshot(s)).collect(Collectors.toList());

    state.businessEdges = versionService.getBusinessEdgeTypes(version);
  }

  protected void queryTotal(State state)
  {
    state.total = 0;

    {
      final GeoObjectTypeSnapshot rootType = this.versionService.getRootType(state.version);
      final MdVertex mdVertex = rootType.getGraphMdVertex();

      final String sql = "SELECT COUNT(*) FROM " + mdVertex.getDbClassName();

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }

    for (CachedGraphTypeSnapshot gt : state.graphTypes)
    {
      final String dbClassName = gt.graphMdEdge.getDbClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }

    // Export all business types
    state.snapshotCache.values().stream().map(c -> c.toBusiness()).filter(c -> c != null).forEach(cached -> {
      final String dbClassName = cached.getGraphMdVertex().getDbClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    });

    for (BusinessEdgeTypeSnapshot type : state.businessEdges)
    {
      final String dbClassName = type.getGraphMdEdge().getDbClassName();

      final String sql = "SELECT COUNT(*) FROM " + dbClassName;

      state.total += new GraphQuery<Long>(sql).getSingleResult();
    }
  }

  protected void updateProgress(State state)
  {
    if (state.history != null)
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

      state.history.appLock();
      state.history.setWorkTotal(state.total);
      state.history.setWorkProgress(state.count);
      state.history.setImportedRecords(state.count);
      state.history.clearStage();
      state.history.addStage(stage);
      state.history.apply();

      ProgressService.put(state.history.getOid(), new Progress(state.count, state.total, state.version.getOid()));
    }
  }

}
