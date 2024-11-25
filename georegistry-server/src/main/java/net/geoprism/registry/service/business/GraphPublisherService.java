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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.orientechnologies.orient.core.id.ORecordId;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshotQuery;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;
import net.geoprism.registry.service.business.GraphPublisherServiceOld.CachedGOTSnapshot;

@Service
public class GraphPublisherService extends AbstractGraphVersionPublisherService
{
  public static final long BLOCK_SIZE_YES_GEOMS = 1000;
  
  public static final long BLOCK_SIZE_NO_GEOMS = 4000;
  
  
  private static final Logger logger = LoggerFactory.getLogger(GraphPublisherService.class);
  
  private Map<String, CachedGOTSnapshot> gotSnaps;
  
  private Set<String> allAttributeColumns;
  
  private ClassificationCache classiCache;
  
  private long BLOCK_SIZE;
  
  private boolean publishGeometries;

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
    publish(version, true);
  }

  public void publish(LabeledPropertyGraphTypeVersion version, boolean withGeoms)
  {
    publishGeometries = withGeoms;
    BLOCK_SIZE = withGeoms ? BLOCK_SIZE_YES_GEOMS : BLOCK_SIZE_NO_GEOMS;
    
    TraversalState state = new TraversalState(null, version);
    cacheMetadata(version);

    long startTime = System.currentTimeMillis();

    logger.info("Started publishing");

    version.lock();

    long count = 0;

    try
    {
      LabeledPropertyGraphType lpgt = version.getGraphType();

      try
      {
        if (!lpgt.isValid())
        {
          throw new InvalidMasterListException();
        }
        
        ProgressService.put(lpgt.getOid(), new Progress(0L, (long) lpgt.getGraphTypeReferences().size(), version.getOid()));
        
        // Publish all the GeoObjectTypes
        for (CachedGOTSnapshot gotSnap : gotSnaps.values().stream().filter(gs -> !gs.got.isRoot()).collect(Collectors.toList()))
        {
          publish(state, gotSnap, version);
          
          count++;
          
          ProgressService.put(lpgt.getOid(), new Progress(count, (long) lpgt.getGraphTypeReferences().size(), version.getOid()));
        }
        
        // Publish the edges
        for (GraphTypeReference gtr : lpgt.getGraphTypeReferences())
        {
          GraphTypeSnapshot graphSnapshot = this.graphSnapshotService.get(version, gtr.typeCode, gtr.code);
          
          publish(state, GraphType.resolve(gtr), graphSnapshot, version);
          
          count++;
          
          ProgressService.put(lpgt.getOid(), new Progress(count, (long) lpgt.getGraphTypeReferences().size(), version.getOid()));
        }

        ProgressService.put(lpgt.getOid(), new Progress((long) lpgt.getGraphTypeReferences().size(), (long) lpgt.getGraphTypeReferences().size(), version.getOid()));
      }
      finally
      {
        ProgressService.remove(lpgt.getOid());
      }
    }
    finally
    {
      version.unlock();
    }

    logger.info("Finished publishing: " + ( ( System.currentTimeMillis() - startTime ) / 1000 ) + " sec");
  }

  protected void publish(TraversalState state, CachedGOTSnapshot gotSnapshot, LabeledPropertyGraphTypeVersion version)
  {
    Date forDate = version.getForDate();
    final ServerGeoObjectType type = ServerGeoObjectType.get(gotSnapshot.got.getCode());
    final String dbClass = type.getDBClassName();
    
    long skip = 0;
    boolean hasMoreData = true;
    
    long total = new GraphQuery<Long>("SELECT COUNT(*) FROM " + dbClass).getSingleResult();
    logger.info("Beginning publishing " + total + " records of GeoObjectType " + dbClass);
    
    while (hasMoreData)
    {
      logger.info("Publishing block " + skip + " through " + Math.min(skip + BLOCK_SIZE, total) + " of total " + total);
      
      VertexGeoObjectQuery query = new VertexGeoObjectQuery(type, null);
      query.setLimit((int) BLOCK_SIZE);
      query.setSkip(skip);
      List<ServerGeoObjectIF> results = query.getResults();
      
      for (ServerGeoObjectIF result : results)
      {
        var go = this.objectService.toGeoObject(result, forDate, false, classiCache);
        super.publish(state, gotSnapshot.graphMdVertex, go, classiCache);
      }
      
      skip += BLOCK_SIZE;
      
      hasMoreData = results.size() > 0;
      results = null; // Explicitly drop all references to the old data so that it can be GC'd
    }
  }

  protected void publish(TraversalState state, GraphType graphType, GraphTypeSnapshot graphSnapshot, LabeledPropertyGraphTypeVersion version)
  {
    final String dbClass = graphType.getMdEdgeDAO().getDBClassName();
    final MdEdge mdEdge = MdEdge.get(graphType.getMdEdgeDAO().getOid());
    
    long skip = 0;
    boolean hasMoreData = true;
    
    long total = new GraphQuery<Long>("SELECT COUNT(*) FROM " + dbClass).getSingleResult();
    logger.info("Beginning publishing " + total + " edge records of GraphType " + dbClass);
    
    while (hasMoreData)
    {
      logger.info("Publishing block " + skip + " through " + Math.min(skip + BLOCK_SIZE, total) + " of total " + total);
      
      List<EdgeObject> results = new GraphQuery<EdgeObject>("SELECT * FROM " + dbClass + " LIMIT " + BLOCK_SIZE + " SKIP " + skip).getResults();
      
      for (EdgeObject result : results)
      {
        CachedGOTSnapshot inGotCached = this.gotSnaps.get(result.getParent().getObjectValue(DefaultAttribute.CODE.getName()));
        CachedGOTSnapshot outGotCached = this.gotSnaps.get(result.getChild().getObjectValue(DefaultAttribute.CODE.getName()));
        
        if (inGotCached != null && outGotCached != null)
        {
          final String inRid = getRid(state, inGotCached.graphMdVertex, result.getChild().getObjectValue(DefaultAttribute.UID.getName()));
          final String outRid = getRid(state, outGotCached.graphMdVertex, result.getChild().getObjectValue(DefaultAttribute.UID.getName()));
          
          createEdge(inRid, outRid, mdEdge);
        }
      }
      
      skip += BLOCK_SIZE;
      
      hasMoreData = results.size() > 0;
      results = null; // Explicitly drop all references to the old data so that it can be GC'd
    }
  }
  
  private void createEdge(final String inRid, final String outRid, final MdEdge graphMdEdge)
  {
    final String sql = "CREATE EDGE " + graphMdEdge.getDbClassName() + " FROM " + inRid + " TO " + outRid + " SET oid = :oid";
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("oid", IDGenerator.nextID());
    
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest request = service.getGraphDBRequest();
    service.command(request, sql, parameters);
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
