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

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

@Service
public class LabeledPropertyGraphRDFExportBusinessService
{
  public static final long BLOCK_SIZE = 1000;
  
  
  private Logger logger = LoggerFactory.getLogger(LabeledPropertyGraphRDFExportBusinessService.class);
  
  private StreamRDF writer;
  
  private List<CachedGraphTypeSnapshot> graphTypes;
  
  private Map<String, GeoObjectTypeSnapshot> gotSnaps;
  
  private long total;
  
  private String quadGraphName;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;
  
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
    long startTime = System.currentTimeMillis();
    
    cacheMetadata(version);
    
    total = queryTotal(version);
    ProgressService.put(version.getOid(), new Progress(0L, (long) total, version.getOid()));
    
    try
    {
      logger.info("Begin rdf exporting " + total + " objects");
      
      writer = StreamRDFWriter.getWriterStream(os , Lang.TURTLE);
      
      long skip = 0;
      long count = 0;
      
      writer.start();
  
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
  
  private long exportVersion(LabeledPropertyGraphTypeVersion version, Long skip)
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
  
  private void exportGeoObject(LabeledPropertyGraphTypeVersion version, Map<String,Object> valueMap)
  {
    final GeoObjectTypeSnapshot type = this.gotSnaps.get(valueMap.get("clazz"));
    final String orgCode = type.getOrgCode();
    
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
        else
        {
          Object value = valueMap.get(attribute.getName());

          literal = value == null ? null : value.toString();
        }
        
        if (literal != null)
        {
          writer.quad(Quad.create(
              NodeFactory.createLiteral(quadGraphName),
              NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
              NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "#" + attribute.getName()),
              NodeFactory.createLiteral(literal))
          );
        }
      }

    });
    
    for (CachedGraphTypeSnapshot graphType : graphTypes)
    {
      final String edge = graphType.graphMdEdge.getDbClassName();
      
      if (valueMap.containsKey("in_" + edge + "_clazz") && valueMap.get("in_" + edge + "_clazz") != null)
      {
        final GeoObjectTypeSnapshot inType = this.gotSnaps.get(valueMap.get("in_" + edge + "_clazz"));
        
        final String literal = "urn:" + inType.getOrgCode() + ":" + inType.getCode() + "-" + valueMap.get("in_" + edge + "_code");
        
        writer.quad(Quad.create(
            NodeFactory.createLiteral(quadGraphName),
            NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
            NodeFactory.createURI("urn:" + orgCode + ":" + graphType.graphType.getCode()),
            NodeFactory.createLiteral(literal))
        );
      }
      
      if (valueMap.containsKey("out_" + edge + "_clazz") && valueMap.get("out_" + edge + "_clazz") != null)
      {
        final GeoObjectTypeSnapshot outType = this.gotSnaps.get(valueMap.get("out_" + edge + "_clazz"));
        
        final String literal = "urn:" + outType.getOrgCode() + ":" + outType.getCode() + "-" + valueMap.get("out_" + edge + "_code");
        
        writer.quad(Quad.create(
            NodeFactory.createLiteral(quadGraphName),
            NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
            NodeFactory.createURI("urn:" + orgCode + ":" + graphType.graphType.getCode()),
            NodeFactory.createLiteral(literal))
        );
      }
    }
  }
  
  private String buildQuadGraphName(LabeledPropertyGraphTypeVersion version)
  {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    String dateLabel = dateFormatter.format(version.getForDate());
    
    return version.getGraphType().getCode() + ":" + dateLabel;
  }

  private void cacheMetadata(LabeledPropertyGraphTypeVersion version)
  {
    gotSnaps = new HashMap<String, GeoObjectTypeSnapshot>();
    
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    for (GeoObjectTypeSnapshot snapshot : query.getIterator().getAll())
    {
      gotSnaps.put(snapshot.getGraphMdVertex().getDbClassName(), snapshot);
    }
    
    graphTypes = versionService.getGraphSnapshots(version).stream().map(s -> new CachedGraphTypeSnapshot(s)).collect(Collectors.toList());
    
    quadGraphName = buildQuadGraphName(version);
  }

  private long queryTotal(LabeledPropertyGraphTypeVersion version)
  {
    final GeoObjectTypeSnapshot rootType = this.versionService.getRootType(version);
    final MdVertex mdVertex = rootType.getGraphMdVertex();
    
    final String sql = "SELECT COUNT(*) FROM " + mdVertex.getDbClassName();
    
    return new GraphQuery<Long>(sql).getSingleResult();
  }
}
