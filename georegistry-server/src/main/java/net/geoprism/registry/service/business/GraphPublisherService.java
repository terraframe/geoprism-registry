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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.id.ORecordId;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdEntityDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.database.ServerIDGenerator;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.dataaccess.metadata.MdEntityDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.util.IDGenerator;
import com.runwaysdk.util.IdParser;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.model.EdgeConstant;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.service.business.ReadonlyEdgeAndInOutResultSetConverter.EdgeAndGOInOut;
import net.geoprism.registry.service.business.ReadonlyEdgeAndInOutResultSetConverter.ReadonlyEdgeAndVerticies;

@Service
public class GraphPublisherService extends AbstractGraphVersionPublisherService
{
  public static final long BLOCK_SIZE = 4000;
  
  public static final boolean PUBLISH_GEOMETRIES = false;
  
  
  private static final Logger logger = LoggerFactory.getLogger(GraphPublisherService.class);
  
  private Map<String, CachedGOTSnapshot> gotSnaps;
  
  private Set<String> allAttributeColumns;
  
  private ClassificationCache classiCache;

  private static class TraversalState extends State
  {
    protected Set<String> publishedGOs = new HashSet<String>();
    
    protected Map<String, String> cache;

    public TraversalState(LabeledPropertyGraphSynchronization synchronization, LabeledPropertyGraphTypeVersion version)
    {
      super(synchronization, version);

      // Estimated RAM use for a million mappings is 54MB
      // 36 * 1000000 + (36/2) * 1000000 = 54MB
      int cacheSize = 2000000;
      
      this.cache = new LinkedHashMap<String, String>(cacheSize + 1, .75F, true)
      {
        public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
        {
          return size() > cacheSize;
        }
      };
    }
  }

  @Autowired
  private GeoObjectBusinessServiceIF             objectService;
  
  @Autowired
  private GeoObjectTypeBusinessServiceIF         typeService;
  
  @Autowired
  protected ClassificationBusinessServiceIF cService;
  
  @Autowired
  protected ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF tSnapshotService;
  
  @Autowired
  private GraphTypeSnapshotBusinessServiceIF graphSnapshotService;
  
  public static class CachedGOTSnapshot
  {
    public GeoObjectTypeSnapshot got;
    
    public MdVertex graphMdVertex;
    
    public CachedGOTSnapshot(GeoObjectTypeSnapshot got)
    {
      this.got = got;
      this.graphMdVertex = got.getGraphMdVertex();
    }
  }

  public void publish(LabeledPropertyGraphTypeVersion version)
  {
    TraversalState state = new TraversalState(null, version);
    cacheMetadata(version);

    long startTime = System.currentTimeMillis();

    logger.info("Started publishing");

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
        
        for (GraphTypeReference gtr : type.getGraphTypeReferences())
        {
          GraphTypeSnapshot graphSnapshot = this.graphSnapshotService.get(version, gtr.typeCode, gtr.code);
          
          publish(state, GraphType.resolve(gtr), graphSnapshot, version);
          
          count++;
          
          ProgressService.put(type.getOid(), new Progress(count, (long) type.getGraphTypeReferences().size(), version.getOid()));
        }

        ProgressService.put(type.getOid(), new Progress((long) type.getGraphTypeReferences().size(), (long) type.getGraphTypeReferences().size(), version.getOid()));
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

    logger.info("Finished publishing: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");
  }

  /*
    SELECT in.@rid, in.@class, in.oid, in.code, inAttr.@rid, inAttr.oid, inAttr.@class, inAttr.value, edgeClass, edgeOid FROM (
      SELECT expand(d) FROM (
        SELECT unionAll( $b, $c ) as d
        LET $a = (SELECT in, in.out('has_value', 'has_geometry') as inAttr, out, out.out('has_value', 'has_geometry') as outAttr, @class as edgeClass, oid as edgeOid FROM (
          SELECT * FROM `fasta_dmin_code` ORDER BY oid SKIP 0 LIMIT 5000
        )),
        $b = (SELECT in, in.out('has_value', 'has_geometry') as inAttr, edgeClass, edgeOid FROM $a UNWIND inAttr),
        $c = (SELECT out, out.out('has_value', 'has_geometry') as outAttr, edgeClass, edgeOid FROM $a UNWIND outAttr)
      )
    )
    ORDER BY edgeOid, in.oid, out.oid
   */
  protected void publish(TraversalState state, GraphType graphType, GraphTypeSnapshot graphSnapshot, LabeledPropertyGraphTypeVersion version)
  {
    final MdEdge graphMdEdge = graphSnapshot.getGraphMdEdge();
    List<EdgeAndGOInOut> edges = null;
    
    long skip = 0;
    boolean hasMoreData = true;
    
    final String inAttrs = StringUtils.join(allAttributeColumns.stream().map(c -> EdgeAndVerticiesResultSetConverter.IN_ATTR_PREFIX + "." + c).collect(Collectors.toList()), ", ");
    final String outAttrs = StringUtils.join(allAttributeColumns.stream().map(c -> EdgeAndVerticiesResultSetConverter.OUT_ATTR_PREFIX + "." + c).collect(Collectors.toList()), ", ");
    
    List<String> types = gotSnaps.values().stream().filter(gs -> !gs.got.isRoot()).map(gs -> ServerGeoObjectType.get(gs.got.getCode()).getDBClassName()).collect(Collectors.toList());
    final String typeCriteria = "("
        + String.join(" OR ", types.stream().map(t -> "in.@class='" + t + "'").collect(Collectors.toList()))
        + ") AND ("
        + String.join(" OR ", types.stream().map(t -> "out.@class='" + t + "'").collect(Collectors.toList()))
        + ")";
    
    long total = queryTotal(graphType, typeCriteria);
    logger.info("Beginning publishing " + total + " records of graphType " + graphType.getMdEdgeDAO().getDBClassName());
    
    while (hasMoreData)
    {
      logger.info("Publishing block " + skip + " through " + Math.min(skip + BLOCK_SIZE, total) + " of total " + total);
      
      final String valueEdges;
      if (PUBLISH_GEOMETRIES)
      {
        valueEdges = "'" + EdgeConstant.HAS_VALUE.getDBClassName() + "', '" + EdgeConstant.HAS_GEOMETRY.getDBClassName() + "'";
      }
      else
      {
        valueEdges = "'" + EdgeConstant.HAS_VALUE.getDBClassName() + "'";
      }
      
      StringBuilder sb = new StringBuilder("SELECT edgeOid, edgeClass, ");
      sb.append(EdgeAndVerticiesResultSetConverter.vertexColumns(EdgeAndVerticiesResultSetConverter.VERTEX_IN_PREFIX));
      sb.append(", " + EdgeAndVerticiesResultSetConverter.vertexColumns(EdgeAndVerticiesResultSetConverter.VERTEX_OUT_PREFIX));
      sb.append(", " + inAttrs);
      sb.append(", " + outAttrs);
      sb.append(" FROM (");
      sb.append("  SELECT expand(d) FROM (");
      sb.append("    SELECT unionAll( $b, $c ) as d");
      sb.append("    LET $a = (SELECT in, in.out(" + valueEdges + ") as inAttr, out, out.out(" + valueEdges + ") as outAttr, @class as edgeClass, oid as edgeOid FROM (");
      sb.append("      SELECT * FROM " + graphType.getMdEdgeDAO().getDBClassName() + " WHERE " + typeCriteria + " ORDER BY oid SKIP " + skip + " LIMIT " + BLOCK_SIZE);
      sb.append("    )),");
      sb.append("    $b = (SELECT in, in.out(" + valueEdges + ") as inAttr, edgeClass, edgeOid FROM $a UNWIND inAttr),");
      sb.append("    $c = (SELECT out, out.out(" + valueEdges + ") as outAttr, edgeClass, edgeOid FROM $a UNWIND outAttr)");
      sb.append("  )");
      sb.append(")");
      sb.append("ORDER BY edgeOid, in.oid, out.oid");
      
      List<ReadonlyEdgeAndVerticies> edgeAndVerticies = new GraphQuery<ReadonlyEdgeAndVerticies>(sb.toString(), null, new ReadonlyEdgeAndInOutResultSetConverter()).getResults();
      
      edges = ReadonlyEdgeAndInOutResultSetConverter.convertResults(edgeAndVerticies, version.getForDate(), classiCache, cService, cTypeService);
      edgeAndVerticies = null; // We don't need this data anymore. Allow GC to remove it.
      
      for (EdgeAndGOInOut edge : edges)
      {
        if (this.gotSnaps.containsKey(edge.in.getType().getCode()) && this.gotSnaps.containsKey(edge.out.getType().getCode()))
        {
          publishEdge(state, version, graphMdEdge, edge);
        }
      }
      
      skip += BLOCK_SIZE;
      
      hasMoreData = edges.size() > 0;
      edges = null; // Explicitly drop all references to the old data so that it can be GC'd
    }
  }

//  @Transaction
  protected void publishEdge(TraversalState state, LabeledPropertyGraphTypeVersion version, final MdEdge graphMdEdge, EdgeAndGOInOut edge)
  {
    CachedGOTSnapshot inGotCached = this.gotSnaps.get(edge.in.getType().getCode());
    MdVertex publishInMdVertex = inGotCached.graphMdVertex;
    
    String publishedInRid;
    if (!state.publishedGOs.contains(edge.in.getUid()))
    {
      publishedInRid = super.publish(state, publishInMdVertex, edge.in, classiCache).getRID().toString();
      state.publishedGOs.add(edge.in.getUid());
      state.cache.put(edge.in.getUid(), publishedInRid);
    }
    else
    {
      publishedInRid = getRid(state, publishInMdVertex, edge.in.getUid());
    }
    
    CachedGOTSnapshot outGotCached = this.gotSnaps.get(edge.out.getType().getCode());
    MdVertex publishOutMdVertex = outGotCached.graphMdVertex;
    
    String publishedOutRid;
    if (!state.publishedGOs.contains(edge.out.getUid()))
    {
      publishedOutRid = super.publish(state, publishOutMdVertex, edge.out, classiCache).getRID().toString();
      state.publishedGOs.add(edge.out.getUid());
      state.cache.put(edge.out.getUid(), publishedOutRid);
    }
    else
    {
      publishedOutRid = getRid(state, publishOutMdVertex, edge.out.getUid());
    }
    
    createEdge(publishedOutRid, publishedInRid, graphMdEdge);
  }
  
  private void createEdge(final String parentRid, final String childRid, final MdEdge graphMdEdge)
  {
    final String sql = "CREATE EDGE " + graphMdEdge.getDbClassName() + " FROM " + parentRid + " TO " + childRid + " SET oid = :oid";
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();
    service.command(request, sql, parameters);
  }
  
  private long queryTotal(GraphType graphType, String typeCriteria)
  {
    final String sql = "SELECT COUNT(*) FROM " + graphType.getMdEdgeDAO().getDBClassName() + " WHERE " + typeCriteria;
    
    return new GraphQuery<Long>(sql).getSingleResult();
  }
  
  private void cacheMetadata(LabeledPropertyGraphTypeVersion version)
  {
    gotSnaps = new HashMap<String, CachedGOTSnapshot>();
    
    GeoObjectTypeSnapshotQuery query = new GeoObjectTypeSnapshotQuery(new QueryFactory());
    query.WHERE(query.getVersion().EQ(version));

    for (GeoObjectTypeSnapshot snapshot : query.getIterator().getAll())
    {
      gotSnaps.put(snapshot.getCode(), new CachedGOTSnapshot(snapshot));
    }
    
    allAttributeColumns = EdgeAndVerticiesResultSetConverter.allAttributeColumns();
    classiCache = new ClassificationCache();
  }
  
  private String getRid(TraversalState state, MdVertex mdVertex, String uid)
  {
    if (state.cache.containsKey(uid))
    {
      return state.cache.get(uid);
    }
    else
    {
      MdVertexDAOIF mdVertexDAO = (MdVertexDAOIF) BusinessFacade.getEntityDAO(mdVertex);
      MdAttributeDAOIF attribute = mdVertexDAO.definesAttribute(DefaultAttribute.UID.getName());
  
      StringBuffer statement = new StringBuffer();
      statement.append("SELECT @rid FROM " + mdVertex.getDbClassName());
      statement.append(" WHERE " + attribute.getColumnName() + " = :uid");
  
      GraphQuery<ORecordId> query = new GraphQuery<ORecordId>(statement.toString());
      query.setParameter("uid", uid);
  
      ORecordId rid = (ORecordId) query.getSingleResult();
      
      if (rid == null)
      {
        throw new RuntimeException("Query returned null which is not allowed. " + statement.toString());
      }
      
      state.cache.put(uid, rid.toString());
      
      return rid.toString();
    }
  }

}
