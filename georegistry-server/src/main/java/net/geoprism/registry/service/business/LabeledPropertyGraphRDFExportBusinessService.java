/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeEntry;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.etl.upload.ClassificationCache;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

@Service
public class LabeledPropertyGraphRDFExportBusinessService implements LabeledPropertyGraphRDFExportBusinessServiceIF
{
  public static final String LPG = "lpg";
  
  public static final String LPGS = "lpgs";
  
  public static final String LPGV = "lpgv";
  
  public static final String LPGVS = "lpgvs";
  
  public static final String RDF = "rdf";
  
  public static final String RDFS = "rdfs";
  
  public static final String DCTERMS = "dcterms";
  
  public static final long BLOCK_SIZE = 1000;
  
  private Logger logger = LoggerFactory.getLogger(LabeledPropertyGraphRDFExportBusinessService.class);
  
  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;
  
  @Autowired
  private ClassificationBusinessServiceIF classificationService;
  
  protected Map<String, String> prefixes = new HashMap<String,String>();
  
  protected LabeledPropertyGraphTypeVersion version;
  
  protected LabeledPropertyGraphTypeEntry entry;
  
  protected LabeledPropertyGraphType lpg;
  
  protected StreamRDF writer;
  
  protected List<CachedGraphTypeSnapshot> graphTypes;
  
  protected Map<String, GeoObjectTypeSnapshot> gotSnaps;
  
  protected long total;
  
  protected String quadGraphName;
  
  protected ClassificationCache classiCache = new ClassificationCache();
  
  public static class CachedGraphTypeSnapshot
  {
    public GraphTypeSnapshot graphType;
    
    public MdEdge graphMdEdge;
    
    public CachedGraphTypeSnapshot(GraphTypeSnapshot graphType)
    {
      this.graphType = graphType;
      this.graphMdEdge = this.graphType.getGraphMdEdge();
    }
  }

  public void export(LabeledPropertyGraphTypeVersion version, OutputStream os)
  {
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
      
      writer = StreamRDFWriter.getWriterStream(os , RDFFormat.TRIG_BLOCKS);
      
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
      
      logger.info("Exporting block " + skip + " through " + (skip + BLOCK_SIZE));
      
      StringBuilder sb = new StringBuilder("SELECT *, @class as clazz");
      
      for (CachedGraphTypeSnapshot graphType : graphTypes)
      {
        for (String attr : new String[] { "code", "@class" })
        {
          final String edge = graphType.graphMdEdge.getDbClassName();
          
          sb.append(", first(in('" + edge + "')." + attr + ") as in_" + edge + "_" + attr.replace("@class", "clazz"));
          sb.append(", first(out('" + edge + "')." + attr + ") as out_" + edge + "_" + attr.replace("@class", "clazz"));
        }
      }
      
      for (GeoObjectTypeSnapshot got : gotSnaps.values())
      {
        got.getAttributeTypes().stream().filter(t -> t instanceof AttributeClassificationType).forEach(attribute -> {
          sb.append(", " + attribute.getName() + ".displayLabel.defaultLocale as " + attribute.getName() + "_l");
        });
      }
      
      sb.append(" FROM " + mdVertex.getDbClassName());
      sb.append(" ORDER BY oid SKIP " + skip + " LIMIT " + BLOCK_SIZE);
      
      List<Map<String,Object>> vertexes = new GraphQuery<Map<String,Object>>(sb.toString()).getResults();
      
      for (Map<String,Object> valueMap : vertexes)
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
  
  protected void exportGeoObject(LabeledPropertyGraphTypeVersion version, Map<String,Object> valueMap)
  {
    final String code = valueMap.get("code").toString();
    final GeoObjectTypeSnapshot type = this.gotSnaps.get(valueMap.get("clazz"));
    final String orgCode = type.getOrgCode();
    
    // Write type information
    writer.quad(Quad.create(
        NodeFactory.createURI(quadGraphName),
        buildGeoObjectUri(code, type.getCode(), orgCode, false),
        org.apache.jena.vocabulary.RDF.type.asNode(),
        NodeFactory.createURI(prefixes.get(LPGVS) + type.getCode())
    ));
    
    type.getAttributeTypes().forEach(attribute -> {
      if (valueMap.containsKey(attribute.getName()))
      {
        String literal = null;
        
        if (attribute instanceof AttributeLocalType)
        {
          @SuppressWarnings("unchecked")
          Map<String,String> value = (Map<String,String>) valueMap.get(attribute.getName());
          
          literal = value.get(MdAttributeLocalInfo.DEFAULT_LOCALE);
        }
        else if (attribute instanceof AttributeClassificationType)
        {
          String value = (String) valueMap.get(attribute.getName() + "_l");

          if (value != null)
          {
            String classificationTypeCode = ((AttributeClassificationType) attribute).getClassificationType();
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
          writer.quad(Quad.create(
              NodeFactory.createURI(quadGraphName),
              buildGeoObjectUri(code, type.getCode(), orgCode, false),
              NodeFactory.createURI(buildAttributeUri(type, orgCode, attribute)),
              NodeFactory.createLiteral(literal)
          ));
        }
      }

    });
    
    for (CachedGraphTypeSnapshot graphType : graphTypes)
    {
      final String edge = graphType.graphMdEdge.getDbClassName();
      
      if (valueMap.containsKey("in_" + edge + "_clazz") && valueMap.get("in_" + edge + "_clazz") != null)
      {
        final GeoObjectTypeSnapshot refType = this.gotSnaps.get(valueMap.get("in_" + edge + "_clazz"));
        final String refCode = valueMap.get("in_" + edge + "_code").toString();
        final String refTypeCode = refType.getCode();
        final String refOrgCode = refType.getOrgCode();
        
        writer.quad(Quad.create(
            NodeFactory.createURI(quadGraphName),
            buildGeoObjectUri(code, type.getCode(), orgCode, false),
            NodeFactory.createURI(buildGraphTypeUri(orgCode, graphType)),
            buildGeoObjectUri(refCode, refTypeCode, refOrgCode, false)
        ));
      }
      
      if (valueMap.containsKey("out_" + edge + "_clazz") && valueMap.get("out_" + edge + "_clazz") != null)
      {
        final GeoObjectTypeSnapshot refType = this.gotSnaps.get(valueMap.get("out_" + edge + "_clazz"));
        final String refCode = valueMap.get("out_" + edge + "_code").toString();
        final String refTypeCode = refType.getCode();
        final String refOrgCode = refType.getOrgCode();
        
        writer.quad(Quad.create(
            NodeFactory.createURI(quadGraphName),
            buildGeoObjectUri(code, type.getCode(), orgCode, false),
            NodeFactory.createURI(buildGraphTypeUri(orgCode, graphType)),
            buildGeoObjectUri(refCode, refTypeCode, refOrgCode, false)
        ));
      }
    }
  }
  
  protected void definePrefixes()
  {
    // IMPORTANT : As a little bit of a quirk of Jena, you are NOT allowed to put a # or / in the URI outside the prefix (called the 'local' portion of the URI).
    // If you want to use a # in the URI, it must be put in the prefix
    // This behaviour is a consequence of org.apache.jena.riot.system.PrefixLib.isSafeLocalPart as invoked by PrefixLib.abbrev
    // Due to this quirk, most prefixes here should probably end with a # (unless they're only used in metadata and not instance data).
    // If for some reason a uri does have a # or a / outside the prefix, the prefix will not be used.
    
//    prefixes.put(RDF, org.apache.jena.vocabulary.RDF.getURI()); // If we include this Jena won't replace rdf:type with 'a', which is just a little syntactic sugar
    prefixes.put(RDFS, org.apache.jena.vocabulary.RDFS.getURI());
    prefixes.put(DCTERMS, "http://purl.org/dc/terms/");
    
    prefixes.put(LPG, GeoprismProperties.getRemoteServerUrl() + "lpg#");
    prefixes.put(LPGS, GeoprismProperties.getRemoteServerUrl() + "lpg/rdfs#");
    
    final String lpgv = GeoprismProperties.getRemoteServerUrl() + "lpg/" + version.getGraphType().getCode() + "/" + version.getVersionNumber();
    prefixes.put(LPGV, lpgv + "#");
    prefixes.put(LPGVS, lpgv + "/rdfs#");
    
    for(String key : prefixes.keySet())
    {
      writer.prefix(key, prefixes.get(key));
    }
    
    quadGraphName = buildQuadGraphName(version);
  }
  
  protected void writeLPGMetadata()
  {
    // Our exported LPG is of type 'LabeledPropertyGraph'
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        org.apache.jena.vocabulary.RDF.type.asNode(),
        NodeFactory.createURI(prefixes.get(LPGS) + "LabeledPropertyGraph")
    ));
    
    // Our LPG has a code
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        NodeFactory.createURI(prefixes.get(LPGS) + "code"),
        NodeFactory.createLiteral(lpg.getCode())
    ));
    
    // Our LPG has a version number
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        NodeFactory.createURI(prefixes.get(LPGS) + "versionNumber"),
        NodeFactory.createLiteral(version.getVersionNumber().toString())
    ));
    
    // Our LPG has a label with value..
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        org.apache.jena.vocabulary.RDFS.label.asNode(),
        NodeFactory.createLiteral(lpg.getDisplayLabel().getValue())
    ));
    
    // Our LPG has a description with value..
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        NodeFactory.createURI(prefixes.get(DCTERMS) + "description"),
        NodeFactory.createLiteral(lpg.getDescription().getValue())
    ));
    
    // Our LPG has a for date with value..
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateLabel = dateFormatter.format(version.getForDate());
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        NodeFactory.createURI(prefixes.get(LPGS) + "forDate"),
        NodeFactory.createLiteral(dateLabel)
    ));
    
    // Our LPG has an associated Organization with code..
    writer.quad(Quad.create(
        NodeFactory.createURI(prefixes.get(LPG)),
        NodeFactory.createURI(prefixes.get(LPGV)),
        NodeFactory.createURI(prefixes.get(LPGS) + "orgCode"),
        NodeFactory.createLiteral(lpg.getOrganization().getCode())
    ));
  }

  protected String buildAttributeUri(final GeoObjectTypeSnapshot type, final String orgCode, AttributeType attribute)
  {
//    return "urn:" + orgCode + ":" + type.getCode() + "#" + attribute.getName();
    
    return prefixes.get(LPGVS) + type.getCode() + "-" + attribute.getName();
    
//    return (Node) model.createProperty(LPG_SCHEMA, ":#" + type.getCode() + "/" + attribute.getName());
  }

  protected String buildGraphTypeUri(final String orgCode, CachedGraphTypeSnapshot graphType)
  {
//    return "urn:" + orgCode + ":" + graphType.graphType.getCode();
    
    return prefixes.get(LPGVS) + graphType.graphType.getCode();
  }

  protected Node buildGeoObjectUri(String code, final String typeCode, final String orgCode, boolean includeType)
  {
    String uri = prefixes.get(LPGV) + typeCode + "-" + code;
    
//    if (includeType)
//    {
////      uri += " a " + prefixes.get(LPG_SCHEMA) + "#" + typeCode;
//      
//      return model.createResource(uri, model.createResource(prefixes.get(LPGS) + typeCode)).asNode();
//      
////      NodeFactory.getType(prefixes.get(LPGS) + typeCode)
//    }
    
    return NodeFactory.createURI(uri);
//    return NodeFactory.createLiteral("lpg:#" + typeCode + "-" + code);
//    return NodeFactory.
//    return NodeFactory.createURI("lpg:#" + typeCode + "-" + code);
    
//    final String obj = prefixes.get(LPG) + ":#" + typeCode + "/" + code;
//    final String type = prefixes.get(LPG_SCHEMA) + ":#" + typeCode;
    
////    Property obj = (Property) model.createProperty(LPG, ":#" + typeCode + "/" + code);
//    Resource obj = model.createResource(prefixes.get(LPG) + ":#" + typeCode + "/" + code);
//    
//    if (includeType)
//    {
//      // model.createProperty(LPG_SCHEMA, ":#" + typeCode)
////      return (Property) model.createResource(obj.getURI(), model.type);
//      return (Node) obj.asNode();
//    }
//    else
//    {
//      return (Node) obj.asNode();
//    }
  }
  
  protected String buildQuadGraphName(LabeledPropertyGraphTypeVersion version)
  {
//    final String url = GeoprismProperties.getRemoteServerUrl() + "lpg/";
//    
//    return url + version.getGraphType().getCode() + "/" + version.getVersionNumber();
    
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
