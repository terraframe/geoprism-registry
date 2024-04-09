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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

@Service
public class LabeledPropertyGraphRDFExporterService
{
  private Path file;
  
  private StreamRDF writer;
  
  private static class TypeSnapshotCacheObject
  {
    private GeoObjectType type;

    public TypeSnapshotCacheObject(GeoObjectTypeSnapshot snapshot)
    {
      this.type = snapshot.toGeoObjectType();
    }

  }
  
  private Map<String, GeoObjectTypeSnapshot> gotSnaps;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;
  
  @Autowired
  private GraphTypeSnapshotBusinessServiceIF graphSnapshotService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF goTypeSnapshotService;
  
  @Autowired
  private GeoObjectBusinessServiceIF             objectService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF         typeService;

  @Autowired
  private HierarchyTypeBusinessServiceIF         hierarchyService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF tSnapshotService;

  @Autowired
  private HierarchyTypeSnapshotBusinessServiceIF hSnapshotService;
  
  @Autowired
  private OrganizationBusinessServiceIF orgService;

  private GeoObjectType getType(Map<String, TypeSnapshotCacheObject> cache, LabeledPropertyGraphTypeVersion version, MdClassDAOIF mdClass)
  {

    if (!cache.containsKey(mdClass.getOid()))
    {
      GeoObjectTypeSnapshot snapshot = this.goTypeSnapshotService.get(version, (MdVertexDAOIF) mdClass);

      cache.put(mdClass.getOid(), new TypeSnapshotCacheObject(snapshot));
    }

    GeoObjectType type = cache.get(mdClass.getOid()).type;
    return type;
  }

  public long exportVersion(LabeledPropertyGraphTypeVersion version, Long skip, Integer blockSize)
  {
    return this.exportVersion(new HashMap<>(), version, skip, blockSize);
  }

  private long exportVersion(Map<String, TypeSnapshotCacheObject> cache, LabeledPropertyGraphTypeVersion version, Long skip, Integer blockSize)
  {
    GeoObjectTypeSnapshot rootType = this.versionService.getRootType(version);
    MdVertex mdVertex = rootType.getGraphMdVertex();
    
    List<GraphType> graphTypes = version.getGraphType().getGraphTypeReferences().stream().map(ref -> GraphType.resolve(ref)).collect(Collectors.toList());
    
    long startTime = System.currentTimeMillis();

    System.out.println("Started exporting");

    version.lock();

    long count = 0;

    try
    {
      LabeledPropertyGraphType type = version.getGraphType();

      try
      {
        if (!type.isValid())
        {
          throw new InvalidMasterListException();
        }
        
        ProgressService.put(type.getOid(), new Progress(0L, (long) type.getGraphTypeReferences().size(), version.getOid()));
        
        StringBuilder sb = new StringBuilder("SELECT *, @class as clazz");
        
        for (GraphType graphType : graphTypes)
        {
          for (String attr : new String[] { "code", "@class" })
          {
            final String edge = graphType.getMdEdgeDAO().getDBClassName();
            
            sb.append(", first(in('" + edge + "')." + attr + ") as in_" + edge + "_" + attr.replace("@class", "clazz"));
            sb.append(", first(out('" + edge + "')." + attr + ") as out_" + edge + "_" + attr.replace("@class", "clazz"));
          }
        }
        
        sb.append(" FROM " + mdVertex.getDbClassName());
        sb.append(" ORDER BY oid SKIP " + skip + " LIMIT " + blockSize);
        
        List<Map<String,Object>> vertexes = new GraphQuery<Map<String,Object>>(sb.toString()).getResults();
        
        for (Map<String,Object> valueMap : vertexes)
        {
          exportGeoObject(version, valueMap);
          
          count++;
          ProgressService.put(type.getOid(), new Progress(count, (long) type.getGraphTypeReferences().size(), version.getOid()));
        }
      }
      finally
      {
        ProgressService.remove(type.getOid());
      }
    }
    finally
    {
      version.unlock();
    }

    System.out.println("Finished publishing: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");
    
    return count;
  }
  
  private void exportGeoObject(LabeledPropertyGraphTypeVersion version, Map<String,Object> valueMap)
  {
    final GeoObjectTypeSnapshot type = this.gotSnaps.get(valueMap.get("clazz"));
    final String orgCode = type.getOrgCode();
    
    writer.quad(Quad.create(
        NodeFactory.createLiteral(version.getGraphType().getCode()),
        NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
        NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "#code"),
        NodeFactory.createLiteral((String) valueMap.get("code")))
    );
    
    type.getAttributeTypes().forEach(attribute -> {
      if (valueMap.containsKey(attribute.getName()))
      {
        String literal = null;
        
//        if (attribute instanceof AttributeTermType)
//        {
////           Iterator<String> it = (Iterator<String>)
////           geoObject.getValue(attributeName);
////          
////           if (it.hasNext())
////           {
////           String code = it.next();
////          
////           Term root = ( (AttributeTermType) attribute ).getRootTerm();
////           String parent =
////           TermConverter.buildClassifierKeyFromTermCode(root.getCode());
////          
////           String classifierKey = Classifier.buildKey(parent, code);
////           Classifier classifier = Classifier.getByKey(classifierKey);
////          
////           node.setValue(attributeName, classifier.getOid());
////           }
//          
//          Object value = valueMap.get(attribute.getName());
//          System.out.println(value.toString());
//        }
//        else if (attribute instanceof AttributeClassificationType)
//        {
////          String value = (String) valueMap.get(attributeName);
////
////          if (value != null)
////          {
////            Classification classification = this.classificationService.get((AttributeClassificationType) attribute, value);
////
////            node.setValue(attributeName, classification.getVertex());
////          }
////          else
////          {
////            node.setValue(attributeName, (String) null);
////          }
//          
//          Object value = valueMap.get(attribute.getName());
//          System.out.println(value.toString());
//        }
//        else
        if (attribute instanceof AttributeLocalType)
        {
          Map<String,String> value = (Map<String,String>) valueMap.get(attribute.getName());
          
          literal = value.get(MdAttributeLocalInfo.DEFAULT_LOCALE);
        }
        else
        {
          Object value = valueMap.get(attribute.getName());

          literal = value.toString();
        }
        
        if (literal != null)
        {
          writer.quad(Quad.create(
              NodeFactory.createLiteral(version.getGraphType().getCode()),
              NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
              NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "#" + attribute.getName()),
              NodeFactory.createLiteral(literal))
          );
        }
      }

    });
    
    List<GraphType> graphTypes = version.getGraphType().getGraphTypeReferences().stream().map(ref -> GraphType.resolve(ref)).collect(Collectors.toList());
    for (GraphType graphType : graphTypes)
    {
      final String edge = graphType.getMdEdgeDAO().getDBClassName();
      
      if (valueMap.containsKey("in_" + edge + "_clazz") && valueMap.get("in_" + edge + "_clazz") != null)
      {
        final GeoObjectTypeSnapshot inType = this.gotSnaps.get(valueMap.get("in_" + edge + "_clazz"));
        
        final String literal = "urn:" + inType.getOrgCode() + ":" + inType.getCode() + "-" + valueMap.get("in_" + edge + "_code");
        
        writer.quad(Quad.create(
            NodeFactory.createLiteral(version.getGraphType().getCode()),
            NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
            NodeFactory.createURI("urn:" + orgCode + ":" + graphType.getCode()),
            NodeFactory.createLiteral(literal))
        );
      }
      
      if (valueMap.containsKey("out_" + edge + "_clazz") && valueMap.get("out_" + edge + "_clazz") != null)
      {
        final GeoObjectTypeSnapshot outType = this.gotSnaps.get(valueMap.get("out_" + edge + "_clazz"));
        
        final String literal = "urn:" + outType.getOrgCode() + ":" + outType.getCode() + "-" + valueMap.get("out_" + edge + "_code");
        
        writer.quad(Quad.create(
            NodeFactory.createLiteral(version.getGraphType().getCode()),
            NodeFactory.createURI("urn:" + orgCode + ":" + type.getCode() + "-" + valueMap.get("code")),
            NodeFactory.createURI("urn:" + orgCode + ":" + graphType.getCode()),
            NodeFactory.createLiteral(literal))
        );
      }
    }
  }
  
//  private void createModel(LabeledPropertyGraphTypeVersion version, GeoObjectTypeSnapshot got)
//  {
//    model = ModelFactory.createDefaultModel();
//    
//    final String uri = "http://terraframe.com/2024/gpr-rdf/1.0#";
//    
//    for(AttributeType type : got.getAttributeTypes())
//    {
//      model.createProperty(uri, type.getName());
//    }
//    
//    for (GraphTypeSnapshot graphType : this.versionService.getGraphSnapshots(version))
//    {
//      model.createProperty(uri, graphType.getCode());
//    }
//  }

  public void export(LabeledPropertyGraphTypeVersion version, OutputStream os)
  {
    buildGotSnapshotMap(version);
    
    writer = StreamRDFWriter.getWriterStream(os , Lang.TURTLE);
    
    Map<String, TypeSnapshotCacheObject> cache = new HashMap<>();

    final int BLOCK_SIZE = 2000;
    long skip = 0;
    long count = 0;
    
    writer.start();

    do
    {
      count = this.exportVersion(cache, version, skip, BLOCK_SIZE);

      skip += BLOCK_SIZE;
    } while (count > 0);
    
    writer.finish();
  }
  
  private void buildGotSnapshotMap(LabeledPropertyGraphTypeVersion version)
  {
    gotSnaps = new HashMap<String, GeoObjectTypeSnapshot>();
    
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    for (GeoObjectTypeSnapshot snapshot : query.getIterator().getAll())
    {
      gotSnaps.put(snapshot.getGraphMdVertex().getDbClassName(), snapshot);
    }
  }

}
