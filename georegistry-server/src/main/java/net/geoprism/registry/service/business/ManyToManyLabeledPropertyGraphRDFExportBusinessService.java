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
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeSourceType;
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
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.graph.Source;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessService.CachedGraphTypeSnapshot;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessService.GeometryExportType;

@Service
@Primary
public class ManyToManyLabeledPropertyGraphRDFExportBusinessService implements LabeledPropertyGraphRDFExportBusinessServiceIF
{
  public static final String                               LPG         = "lpg";

  public static final String                               LPGS        = "lpgs";

  public static final String                               LPGV        = "lpgv";

  public static final String                               LPGVS       = "lpgvs";

  public static final String                               RDF         = "rdf";

  public static final String                               RDFS        = "rdfs";

  public static final String                               DCTERMS     = "dcterms";

  public static final String                               GEO         = "geo";

  public static final String                               SF          = "sf";

  public static final long                                 BLOCK_SIZE  = 1000;

  private Logger                                           logger      = LoggerFactory.getLogger(ManyToManyLabeledPropertyGraphRDFExportBusinessService.class);

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;

  @Autowired
  private ClassificationBusinessServiceIF                  classificationService;

  @Autowired
  private SourceBusinessServiceIF                          sourceService;

  protected Map<String, String>                            prefixes    = new HashMap<String, String>();

  protected LabeledPropertyGraphTypeVersion                version;

  protected LabeledPropertyGraphTypeEntry                  entry;

  protected LabeledPropertyGraphType                       lpg;

  protected StreamRDF                                      writer;

  protected List<Source>                                   sources;

  protected List<CachedGraphTypeSnapshot>                  graphTypes;

  protected Map<String, GeoObjectTypeSnapshot>             gotSnaps;

  protected long                                           total;

  protected String                                         quadGraphName;

  protected ClassificationCache                            classiCache = new ClassificationCache();

  protected GeometryExportType                             geomExportType;

  public void export(LabeledPropertyGraphTypeVersion version, GeometryExportType geomExportType, OutputStream os)
  {
    this.geomExportType = geomExportType;
    this.version = version;
    this.entry = version.getEntry();
    this.lpg = entry.getGraphType();

    long startTime = System.currentTimeMillis();

    cacheMetadata(version);

    total = queryTotal(version);
    ProgressService.put(version.getOid(), new Progress(0L, (long) total, version.getOid()));

    try
    {
      logger.info("Begin rdf exporting " + total + " objects");

      writer = StreamRDFWriter.getWriterStream(os, RDFFormat.TURTLE_BLOCKS);

      long skip = 0;
      long count = 0;

      writer.start();

      definePrefixes();

      writeLPGMetadata();

      do
      {
        count = this.exportVersion(version, skip);

        skip += BLOCK_SIZE;
      } while (count > 0);

      for (CachedGraphTypeSnapshot graphType : graphTypes)
      {
        this.exportEdgeType(version, graphType);
      }

      logger.info("Finished rdf exporting: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");
    }
    finally
    {
      if (writer != null)
      {
        writer.finish();
      }

      ProgressService.remove(version.getOid());
    }
  }


  private void exportEdgeType(LabeledPropertyGraphTypeVersion version, CachedGraphTypeSnapshot graphType)
  {
    long skip = 0;
    long count = 0;

    do
    {
      count = 0;

      StringBuilder sb = new StringBuilder("SELECT in.@class AS in_class, in.code AS in_code, out.@class AS out_class, out.code AS out_code");
      sb.append(" FROM " + graphType.graphMdEdge.getDbClassName());
      sb.append(" ORDER BY out SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      List<Map<String, Object>> records = new GraphQuery<Map<String, Object>>(sb.toString()).getResults();

      for (Map<String, Object> record : records)
      {
        final GeoObjectTypeSnapshot inType = this.gotSnaps.get(record.get("in_class"));
        final String inCode = record.get("in_code").toString();
        final String inTypeCode = inType.getCode();
        final String inOrgCode = inType.getOrgCode();

        final GeoObjectTypeSnapshot outType = this.gotSnaps.get(record.get("out_class"));
        final String outCode = record.get("out_code").toString();
        final String outTypeCode = outType.getCode();
        final String outOrgCode = outType.getOrgCode();

        writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(outCode, outTypeCode, outOrgCode, false), NodeFactory.createURI(buildGraphTypeUri(outOrgCode, graphType)), buildGeoObjectUri(inCode, inTypeCode, inOrgCode, false)));

        count++;
      }

      skip += BLOCK_SIZE;
    } while (count > 0);

  }

  protected long exportVersion(LabeledPropertyGraphTypeVersion version, Long skip)
  {
    GeoObjectTypeSnapshot rootType = this.versionService.getRootType(version);
    MdVertex mdVertex = rootType.getGraphMdVertex();

    version.lock();

    long count = 0;

    LabeledPropertyGraphType type = version.getGraphType();

    try
    {
      if (!type.isValid())
      {
        throw new InvalidMasterListException();
      }

      logger.info("Exporting block " + skip + " through " + ( skip + BLOCK_SIZE ));

      StringBuilder sb = new StringBuilder("SELECT *, @class as clazz");

      for (GeoObjectTypeSnapshot got : gotSnaps.values())
      {
        if (!got.isRoot())
        {
          got.getAttributeTypes().stream().filter(t -> t instanceof AttributeClassificationType).forEach(attribute -> {            
            sb.append(", " + attribute.getName() + ".displayLabel.defaultLocale as " + attribute.getName() + "_l");
          });
          
          got.getAttributeTypes().stream().filter(t -> t instanceof AttributeSourceType).forEach(attribute -> {            
            sb.append(", " + attribute.getName() + ".code as " + attribute.getName() + "_c");
          });
        }
      }

      sb.append(" FROM " + mdVertex.getDbClassName());
      sb.append(" ORDER BY oid SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      List<Map<String, Object>> vertexes = new GraphQuery<Map<String, Object>>(sb.toString()).getResults();

      for (Map<String, Object> valueMap : vertexes)
      {
        exportGeoObject(version, valueMap);

        count++;
        ProgressService.put(version.getOid(), new Progress(count, total, version.getOid()));
      }
    }
    finally
    {
      version.unlock();
    }

    return count;
  }

  protected void exportGeoObject(LabeledPropertyGraphTypeVersion version, Map<String, Object> valueMap)
  {
    final String code = valueMap.get("code").toString();
    final GeoObjectTypeSnapshot type = this.gotSnaps.get(valueMap.get("clazz"));
    final String orgCode = type.getOrgCode();

    // Write type information
    writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), org.apache.jena.vocabulary.RDF.type.asNode(), NodeFactory.createURI(prefixes.get(LPGVS) + type.getCode())));

    type.getAttributeTypes().forEach(attribute -> {
      if (valueMap.containsKey(attribute.getName()))
      {
        if (attribute instanceof AttributeIntegerType)
        {
          Object value = valueMap.get(attribute.getName());

          if (value != null)
          {
            writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)), NodeFactory.createLiteralByValue(value, XSDDatatype.XSDlong)));
          }
        }
        else if (attribute instanceof AttributeFloatType)
        {
          Object value = valueMap.get(attribute.getName());

          if (value != null)
          {
            writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)), NodeFactory.createLiteralByValue(value, XSDDatatype.XSDdouble)));
          }
        }
        else if (attribute instanceof AttributeDateType)
        {
          Object value = valueMap.get(attribute.getName());

          if (value != null)
          {
            writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)), NodeFactory.createLiteralByValue(value, XSDDatatype.XSDdateTime)));
          }
        }
        else if (attribute instanceof AttributeBooleanType)
        {
          Object value = valueMap.get(attribute.getName());

          if (value != null)
          {
            writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)), NodeFactory.createLiteralByValue(value, XSDDatatype.XSDboolean)));
          }
        }
        else if (attribute instanceof AttributeSourceType)
        {
          Object value = valueMap.get(attribute.getName() + "_c");
          
          if (value != null)
          {
            writer.quad(
                Quad.create(
                    NodeFactory.createURI(quadGraphName), 
                    buildGeoObjectUri(code, type.getCode(), orgCode, false), 
                    NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)), 
                    NodeFactory.createURI(buildSourceUri((String) value))
                )
            );
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
              String classificationTypeCode = ( (AttributeClassificationType) attribute ).getClassificationType();
              Classification classification = classiCache.getClassification(classificationTypeCode, value.toString().trim());

              if (classification == null)
              {
                classification = this.classificationService.get((AttributeClassificationType) attribute, value);

                if (classification != null)
                {
                  literal = classification.getDisplayLabel().getValue();
                  classiCache.putClassification(classificationTypeCode, value.toString().trim(), classification);
                }
              }
              else
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
            writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)), NodeFactory.createLiteral(literal)));
          }
        }
      }

    });

    boolean includeGeoms = !GeometryExportType.NO_GEOMETRIES.equals(geomExportType);
    if (includeGeoms && valueMap.containsKey(DefaultAttribute.GEOMETRY.getName()))
    {
      Geometry geom = (Geometry) valueMap.get(DefaultAttribute.GEOMETRY.getName());

      if (geom != null)
      {
        final boolean simplify = GeometryExportType.WRITE_SIMPLIFIED_GEOMETRIES.equals(geomExportType);
        final double MAX_POINTS = 10000;
        final int numPoints = simplify ? geom.getNumPoints() : -1;

        if (simplify && numPoints > MAX_POINTS)
        {
          double tolerance = Math.min(4d, 0.0000000006d * numPoints);
          geom = TopologyPreservingSimplifier.simplify(geom, tolerance);
          logger.info("Simplified geometry from " + numPoints + " to " + geom.getNumPoints());
        }

        // This GeoObject has a Geometry
        writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), buildGeoObjectUri(code, type.getCode(), orgCode, false), NodeFactory.createURI(prefixes.get(GEO) + "hasGeometry"), NodeFactory.createURI(prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry")));

        String srs_uri = "http://www.opengis.net/def/crs/OGC/1.3/CRS84";
        if (geom.getSRID() > 0)
        {
          srs_uri = "http://www.opengis.net/def/crs/EPSG/0/" + geom.getSRID();
        }

        // Write a Geometry Node
        writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), NodeFactory.createURI(prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry"), org.apache.jena.vocabulary.RDF.type.asNode(), NodeFactory.createURI(prefixes.get(GEO) + "Geometry")));
        writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), NodeFactory.createURI(prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry"), org.apache.jena.vocabulary.RDF.type.asNode(), NodeFactory.createURI(prefixes.get(SF) + geom.getClass().getSimpleName())));
        writer.quad(Quad.create(NodeFactory.createURI(quadGraphName), NodeFactory.createURI(prefixes.get(LPGV) + type.getCode() + "-" + code + "Geometry"), NodeFactory.createURI(prefixes.get(GEO) + "asWKT"), NodeFactory.createLiteral("<" + srs_uri + "> " + geom.toText(), new org.apache.jena.datatypes.BaseDatatype(prefixes.get(GEO) + "wktLiteral"))

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

  protected void definePrefixes()
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
    prefixes.put(RDFS, org.apache.jena.vocabulary.RDFS.getURI());
    prefixes.put(DCTERMS, "http://purl.org/dc/terms/");
    prefixes.put(GEO, "http://www.opengis.net/ont/geosparql#");
    prefixes.put(SF, "http://www.opengis.net/ont/sf#");

    prefixes.put(LPG, GeoprismProperties.getRemoteServerUrl() + "lpg#");
    prefixes.put(LPGS, GeoprismProperties.getRemoteServerUrl() + "lpg/rdfs#");

    final String lpgv = GeoprismProperties.getRemoteServerUrl() + "lpg/" + version.getGraphType().getCode() + "/" + version.getVersionNumber();
    prefixes.put(LPGV, lpgv + "#");
    prefixes.put(LPGVS, lpgv + "/rdfs#");

    for (String key : prefixes.keySet())
    {
      writer.prefix(key, prefixes.get(key));
    }

    quadGraphName = buildQuadGraphName(version);
  }

  protected void writeLPGMetadata()
  {
    // Our exported LPG is of type 'LabeledPropertyGraph'
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), org.apache.jena.vocabulary.RDF.type.asNode(), NodeFactory.createURI(prefixes.get(LPGS) + "LabeledPropertyGraph")));

    // Our LPG has a code
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), NodeFactory.createURI(prefixes.get(LPGS) + "code"), NodeFactory.createLiteral(lpg.getCode())));

    // Our LPG has a version number
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), NodeFactory.createURI(prefixes.get(LPGS) + "versionNumber"), NodeFactory.createLiteral(version.getVersionNumber().toString())));

    // Our LPG has a label with value..
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), org.apache.jena.vocabulary.RDFS.label.asNode(), NodeFactory.createLiteral(lpg.getDisplayLabel().getValue())));

    // Our LPG has a description with value..
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), NodeFactory.createURI(prefixes.get(DCTERMS) + "description"), NodeFactory.createLiteral(lpg.getDescription().getValue())));

    // Our LPG has a for date with value..
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateLabel = dateFormatter.format(version.getForDate());
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), NodeFactory.createURI(prefixes.get(LPGS) + "forDate"), NodeFactory.createLiteral(dateLabel)));

    // Our LPG has an associated Organization with code..
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGV)), NodeFactory.createURI(prefixes.get(LPGS) + "orgCode"), NodeFactory.createLiteral(lpg.getOrganization().getCode())));

    // GeoObject definition
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGS) + "GeoObject"), org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.RDFS.Class.asNode()));
    writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGS) + "GeoObject"), org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), NodeFactory.createURI(prefixes.get(GEO) + "feature")));

    // Our LPG has the following GeoObjects
    for (GeoObjectTypeSnapshot got : gotSnaps.values())
    {
      if (!got.isRoot())
      {
        writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGVS) + got.getCode()), NodeFactory.createURI(prefixes.get(RDFS) + "subClassOf"), NodeFactory.createURI(prefixes.get(LPGS) + "GeoObject")));
      }
    }

    // Our LPG has the following GraphTypes
    for (CachedGraphTypeSnapshot graphType : graphTypes)
    {
      writer.quad(Quad.create(NodeFactory.createURI(prefixes.get(LPG)), NodeFactory.createURI(prefixes.get(LPGVS) + graphType.graphType.getCode()), NodeFactory.createURI(prefixes.get(RDFS) + "subClassOf"), NodeFactory.createURI(prefixes.get(LPGS) + "GraphType")));
    }
  }

  protected String buildSourceUri(String code)
  {
    return prefixes.get(LPGVS) + "Source-" + code;
  }
  
  protected String buildAttributeUri(final GeoObjectTypeSnapshot type, final String orgCode, AttributeType attribute)
  {
    if (attribute.getIsDefault())
    {
      if (attribute.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName()))
      {
        return prefixes.get(RDFS) + "label";
      }

      return prefixes.get(LPGS) + "GeoObject-" + attribute.getName();
    }

    return prefixes.get(LPGVS) + type.getCode() + "-" + attribute.getName();
  }

  protected String buildGraphTypeUri(final String orgCode, CachedGraphTypeSnapshot graphType)
  {
    return prefixes.get(LPGVS) + graphType.graphType.getCode();
  }

  protected Node buildGeoObjectUri(String code, final String typeCode, final String orgCode, boolean includeType)
  {
    try
    {
      String uri = prefixes.get(LPGV) + typeCode + "-" + URLEncoder.encode(code, "UTF-8");

      return NodeFactory.createURI(uri);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  protected String buildQuadGraphName(LabeledPropertyGraphTypeVersion version)
  {
    return prefixes.get(LPGV);
  }

  protected void cacheMetadata(LabeledPropertyGraphTypeVersion version)
  {
    gotSnaps = new HashMap<String, GeoObjectTypeSnapshot>();

    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    for (GeoObjectTypeSnapshot snapshot : query.getIterator().getAll())
    {
      gotSnaps.put(snapshot.getGraphMdVertex().getDbClassName(), snapshot);
    }

    graphTypes = versionService.getGraphSnapshots(version).stream().map(s -> new CachedGraphTypeSnapshot(s)).collect(Collectors.toList());
  }

  protected long queryTotal(LabeledPropertyGraphTypeVersion version)
  {
    final GeoObjectTypeSnapshot rootType = this.versionService.getRootType(version);
    final MdVertex mdVertex = rootType.getGraphMdVertex();

    final String sql = "SELECT COUNT(*) FROM " + mdVertex.getDbClassName();

    return new GraphQuery<Long>(sql).getSingleResult();
  }
}
