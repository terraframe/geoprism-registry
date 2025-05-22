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

import com.google.gson.JsonObject;
import com.orientechnologies.orient.core.id.ORecordId;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;
import com.runwaysdk.util.IDGenerator;

import net.geoprism.graph.BusinessEdgeTypeSnapshot;
import net.geoprism.graph.BusinessTypeSnapshot;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeReference;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphSynchronization;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.cache.ClassificationCache;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.EdgeVertexType;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.graph.VertexGeoObjectQuery;

@Service
public class GraphPublisherService extends AbstractGraphVersionPublisherService
{
  public static interface CachedSnapshot
  {
    public CachedGOTSnapshot toType();

    public CachedBusinessSnapshot toBusiness();

    public MdVertex getGraphMdVertex();

    public String getCode();

    public String getOrgCode();
  }

  public static class CachedGOTSnapshot implements CachedSnapshot
  {
    public GeoObjectTypeSnapshot type;

    public MdVertex              graphMdVertex;

    public CachedGOTSnapshot(GeoObjectTypeSnapshot got)
    {
      this.type = got;
      this.graphMdVertex = got.getGraphMdVertex();
    }

    public MdVertex getGraphMdVertex()
    {
      return graphMdVertex;
    }

    @Override
    public CachedGOTSnapshot toType()
    {
      return this;
    }

    @Override
    public CachedBusinessSnapshot toBusiness()
    {
      return null;
    }

    @Override
    public String getCode()
    {
      return this.type.getCode();
    }

    @Override
    public String getOrgCode()
    {
      return this.type.getOrgCode();
    }
  }

  public static class CachedBusinessSnapshot implements CachedSnapshot
  {
    public BusinessTypeSnapshot type;

    public MdVertex             graphMdVertex;

    public CachedBusinessSnapshot(BusinessTypeSnapshot type)
    {
      this.type = type;
      this.graphMdVertex = type.getGraphMdVertex();
    }

    @Override
    public CachedGOTSnapshot toType()
    {
      return null;
    }

    @Override
    public CachedBusinessSnapshot toBusiness()
    {
      return this;
    }

    public MdVertex getGraphMdVertex()
    {
      return graphMdVertex;
    }

    @Override
    public String getCode()
    {
      return this.type.getCode();
    }

    @Override
    public String getOrgCode()
    {
      return this.type.getOrgCode();
    }
  }

  private static class TraversalState extends State
  {
    protected Set<String>         publishedGOs = new HashSet<String>();

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

  public static final long                                 BLOCK_SIZE_YES_GEOMS = 1000;

  public static final long                                 BLOCK_SIZE_NO_GEOMS  = 4000;

  private static final Logger                              logger               = LoggerFactory.getLogger(GraphPublisherService.class);

  @Autowired
  private GeoObjectTypeBusinessServiceIF                   typeService;

  @Autowired
  private GeoObjectBusinessServiceIF                       objectService;

  @Autowired
  protected ClassificationBusinessServiceIF                cService;

  @Autowired
  protected ClassificationTypeBusinessServiceIF            cTypeService;

  @Autowired
  private BusinessObjectBusinessServiceIF                  bObjectService;

  @Autowired
  private BusinessTypeBusinessServiceIF                    bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF                bEdgeTypeService;

  @Autowired
  private GraphTypeSnapshotBusinessServiceIF               graphSnapshotService;

  /*
   * 
   * ALL OF THE FOLLOWING PROPERTIES NEED TO BE REFACTORED. LOCAL PROPERTIES DO
   * NOT WORK IN A IOC SERVICE ARCHITECTURE. THESE PROPERTIES WILL CAUSE
   * PROBLEMS IF MULTIPLE THREADS ARE PUBLSIHING A GRAPH AT THE SAME TIME. THIS
   * NEEDS TO BE REFACTOR INTO A STATE TYPE OBJECT WHICH IS PASSED INTO THE
   * SERVICE METHODS.
   */
  private Map<String, CachedSnapshot>                      snapshotCache;

  private ClassificationCache                              classiCache;

  private long                                             BLOCK_SIZE;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;

  public void publish(LabeledPropertyGraphTypeVersion version)
  {
    publish(version, true);
  }

  public void publish(LabeledPropertyGraphTypeVersion version, boolean withGeoms)
  {
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

        List<CachedGOTSnapshot> publishedTypes = snapshotCache.values().stream().map(gs -> gs.toType()).filter(gs -> gs != null && !gs.type.isRoot()).collect(Collectors.toList());

        List<BusinessTypeSnapshot> bSnapshots = this.versionService.getBusinessTypes(version);
        List<BusinessEdgeTypeSnapshot> bEdgeSnapshots = this.versionService.getBusinessEdgeTypes(version);

        long totalWork = lpgt.getGraphTypeReferences().size() + publishedTypes.size() + bSnapshots.size();

        ProgressService.put(lpgt.getOid(), new Progress(0L, totalWork, version.getOid()));

        // Publish all the GeoObjectTypes
        for (CachedGOTSnapshot gotSnap : publishedTypes)
        {
          publish(state, gotSnap, version);

          count++;

          ProgressService.put(lpgt.getOid(), new Progress(count, totalWork, version.getOid()));
        }

        // Publish the business objects
        for (BusinessTypeSnapshot snapshot : bSnapshots)
        {
          BusinessType type = this.bTypeService.getByCode(snapshot.getCode());

          publish(state, type, snapshot, version);

          count++;

          ProgressService.put(lpgt.getOid(), new Progress(count, totalWork, version.getOid()));
        }

        // Publish the edges
        for (GraphTypeReference gtr : lpgt.getGraphTypeReferences())
        {
          GraphTypeSnapshot graphSnapshot = this.graphSnapshotService.get(version, gtr.typeCode, gtr.code);

          publish(state, GraphType.resolve(gtr), graphSnapshot, version);

          count++;

          ProgressService.put(lpgt.getOid(), new Progress(count, totalWork, version.getOid()));
        }

        // Publish the business objects
        for (BusinessEdgeTypeSnapshot snapshot : bEdgeSnapshots)
        {
          BusinessEdgeType type = this.bEdgeTypeService.getByCode(snapshot.getCode());

          publish(state, type, snapshot, version);

          count++;

          ProgressService.put(lpgt.getOid(), new Progress(count, totalWork, version.getOid()));
        }

        ProgressService.put(lpgt.getOid(), new Progress(totalWork, totalWork, version.getOid()));
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

  private void publish(TraversalState state, BusinessEdgeType type, BusinessEdgeTypeSnapshot snapshot, LabeledPropertyGraphTypeVersion version)
  {
    final String dbClass = type.getMdEdgeDAO().getDBClassName();
    final MdEdge snapshotMdEdge = snapshot.getGraphMdEdge();

    EdgeVertexType parentType = this.bEdgeTypeService.getParent(type);
    EdgeVertexType childType = this.bEdgeTypeService.getChild(type);

    long skip = 0;
    boolean hasMoreData = true;

    long total = new GraphQuery<Long>("SELECT COUNT(*) FROM " + dbClass).getSingleResult();

    logger.info("Beginning publishing " + total + " edge records of GraphType " + dbClass);

    while (hasMoreData)
    {
      logger.info("Publishing block " + skip + " through " + Math.min(skip + BLOCK_SIZE, total) + " of total " + total);

      List<EdgeObject> results = new GraphQuery<EdgeObject>("SELECT * FROM " + dbClass + " ORDER BY out.@class, in.@class, out, in LIMIT " + BLOCK_SIZE + " SKIP " + skip).getResults();

      for (EdgeObject result : results)
      {
        VertexObject parent = result.getParent();
        VertexObject child = result.getChild();

        CachedSnapshot outGotCached = this.snapshotCache.get( ( (MdVertexDAOIF) parent.getMdClass() ).getDBClassName().toLowerCase());
        CachedSnapshot inGotCached = this.snapshotCache.get( ( (MdVertexDAOIF) child.getMdClass() ).getDBClassName().toLowerCase());

        if (inGotCached != null && outGotCached != null)
        {
          final String inRid = childType.isGeoObjectType() ? getRid(state, inGotCached.getGraphMdVertex(), child.getObjectValue(DefaultAttribute.UID.getName())) : getBusinessRid(state, inGotCached.getGraphMdVertex(), child.getObjectValue(DefaultAttribute.CODE.getName()));

          final String outRid = parentType.isGeoObjectType() ? getRid(state, outGotCached.getGraphMdVertex(), parent.getObjectValue(DefaultAttribute.UID.getName())) : getBusinessRid(state, outGotCached.getGraphMdVertex(), parent.getObjectValue(DefaultAttribute.CODE.getName()));

          createEdge(outRid, inRid, snapshotMdEdge);
        }
      }

      skip += BLOCK_SIZE;

      hasMoreData = results.size() > 0;
      results = null; // Explicitly drop all references to the old data so that
                      // it can be GC'd
    }

  }

  private void publish(TraversalState state, BusinessType type, BusinessTypeSnapshot snapshot, LabeledPropertyGraphTypeVersion version)
  {
    final String dbClass = type.getMdVertex().getDbClassName();

    long skip = 0;
    boolean hasMoreData = true;

    long total = new GraphQuery<Long>("SELECT COUNT(*) FROM " + dbClass).getSingleResult();
    logger.info("Beginning publishing " + total + " records of BusinessType " + dbClass);

    while (hasMoreData)
    {
      logger.info("Publishing block " + skip + " through " + Math.min(skip + BLOCK_SIZE, total) + " of total " + total);

      StringBuilder statement = new StringBuilder();
      statement.append("SELECT FROM " + dbClass);
      statement.append(" SKIP " + skip + " LIMIT " + BLOCK_SIZE);

      GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

      List<VertexObject> results = query.getResults();

      for (VertexObject vertex : results)
      {
        JsonObject dto = this.bObjectService.toJSON(new BusinessObject(vertex, type));

        super.publishBusiness(state, MdVertexDAO.get(snapshot.getGraphMdVertexOid()), dto, classiCache);
      }

      skip += BLOCK_SIZE;

      hasMoreData = results.size() > 0;
      results = null; // Explicitly drop all references to the old data so that
                      // it can be GC'd
    }
  }

  protected void publish(TraversalState state, CachedGOTSnapshot gotSnapshot, LabeledPropertyGraphTypeVersion version)
  {
    Date forDate = version.getForDate();
    final ServerGeoObjectType type = ServerGeoObjectType.get(gotSnapshot.type.getCode());
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
      results = null; // Explicitly drop all references to the old data so that
                      // it can be GC'd
    }
  }

  protected void publish(TraversalState state, GraphType graphType, GraphTypeSnapshot graphSnapshot, LabeledPropertyGraphTypeVersion version)
  {
    final String dbClass = graphType.getMdEdgeDAO().getDBClassName();
    final MdEdge snapshotMdEdge = graphSnapshot.getGraphMdEdge();

    long skip = 0;
    boolean hasMoreData = true;

    long total = new GraphQuery<Long>("SELECT COUNT(*) FROM " + dbClass).getSingleResult();
    logger.info("Beginning publishing " + total + " edge records of GraphType " + dbClass);

    while (hasMoreData)
    {
      logger.info("Publishing block " + skip + " through " + Math.min(skip + BLOCK_SIZE, total) + " of total " + total);

      List<EdgeObject> results = new GraphQuery<EdgeObject>("SELECT * FROM " + dbClass + " ORDER BY out.@class, in.@class, out, in LIMIT " + BLOCK_SIZE + " SKIP " + skip).getResults();

      for (EdgeObject result : results)
      {
        VertexObject parent = result.getParent();
        VertexObject child = result.getChild();

        CachedSnapshot inGotCached = this.snapshotCache.get( ( (MdVertexDAOIF) parent.getMdClass() ).getDBClassName().toLowerCase());
        CachedSnapshot outGotCached = this.snapshotCache.get( ( (MdVertexDAOIF) child.getMdClass() ).getDBClassName().toLowerCase());

        if (inGotCached != null && outGotCached != null)
        {
          final String inRid = getRid(state, inGotCached.getGraphMdVertex(), parent.getObjectValue(DefaultAttribute.UID.getName()));
          final String outRid = getRid(state, outGotCached.getGraphMdVertex(), child.getObjectValue(DefaultAttribute.UID.getName()));

          createEdge(inRid, outRid, snapshotMdEdge);
        }
      }

      skip += BLOCK_SIZE;

      hasMoreData = results.size() > 0;
      results = null; // Explicitly drop all references to the old data so that
                      // it can be GC'd
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
    snapshotCache = new HashMap<String, CachedSnapshot>();

    classiCache = new ClassificationCache();

    this.versionService.getTypes(version).forEach(snapshot -> {
      if (!snapshot.isRoot())
      {
        ServerGeoObjectType type = ServerGeoObjectType.get(snapshot.getCode());
        String key = type.getMdVertex().getDBClassName().toLowerCase();

        snapshotCache.put(key, new CachedGOTSnapshot(snapshot));
      }
    });

    this.versionService.getBusinessTypes(version).forEach(snapshot -> {
      BusinessType type = this.bTypeService.getByCode(snapshot.getCode());
      String key = type.getMdVertex().getDbClassName().toLowerCase();

      snapshotCache.put(key, new CachedBusinessSnapshot(snapshot));
    });
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

  private String getBusinessRid(TraversalState state, MdVertex mdVertex, String code)
  {
    if (state.cache.containsKey(code))
    {
      return state.cache.get(code);
    }
    else
    {
      MdVertexDAOIF mdVertexDAO = (MdVertexDAOIF) BusinessFacade.getEntityDAO(mdVertex);
      MdAttributeDAOIF attribute = mdVertexDAO.definesAttribute(DefaultAttribute.CODE.getName());

      StringBuffer statement = new StringBuffer();
      statement.append("SELECT @rid FROM " + mdVertex.getDbClassName());
      statement.append(" WHERE " + attribute.getColumnName() + " = :code");

      GraphQuery<ORecordId> query = new GraphQuery<ORecordId>(statement.toString());
      query.setParameter("code", code);

      ORecordId rid = (ORecordId) query.getSingleResult();

      if (rid == null)
      {
        throw new RuntimeException("Query returned null which is not allowed. " + statement.toString());
      }

      state.cache.put(code, rid.toString());

      return rid.toString();
    }
  }

}
