package net.geoprism.registry.service.business;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.ServerObjectVertex;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.permission.GeoObjectPermissionServiceIF;
import net.geoprism.registry.service.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.service.request.RelationshipVisualizationService;
import net.geoprism.registry.visualization.VertexView;

@Service
public class StabilityPeriodService
{
  @Autowired
  private GeoObjectBusinessServiceIF geoObjectService;
  
  @Autowired
  private BusinessObjectBusinessServiceIF bObjectService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;
  
  @Autowired
  private BusinessTypeBusinessServiceIF bTypeService;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF dagService;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF undirectedService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF businessEdgeService;

  @Autowired
  private GeoObjectPermissionServiceIF objectPermissions;

  @Autowired
  private HierarchyTypePermissionServiceIF hierarchyPermissions;

  /**
   * Resolves the requested GeoObject and calculates its relationship stability
   * periods across every applicable relationship edge type.
   *
   * The returned list is directly serializable by Jackson.
   */
  @Request(RequestType.SESSION)
  public List<StabilityPeriod> getStabilityPeriods(
      String sessionId,
      VertexView.ObjectType objectType,
      String typeCode,
      String code
  )
  {
    ServerObjectVertex vertex;
    
    if (objectType.equals(VertexView.ObjectType.GEOOBJECT))
    {
      ServerGeoObjectType type = ServerGeoObjectType.get(typeCode);
  
      if (!this.objectPermissions.canRead(
          type.getOrganization().getCode(),
          type
      ))
      {
        throw new IllegalArgumentException("The user cannot read the requested GeoObject type.");
      }
  
      vertex = (VertexServerGeoObject) this.geoObjectService.getGeoObjectByCode(code, type);
    }
    else
    {
      final BusinessType type = this.bTypeService.getByCodeOrThrow(typeCode);

      RelationshipVisualizationService.enforceCanReadBusinessData(type);
      
      vertex = this.bObjectService.getByCode(type, code).orElseThrow();
    }

    return this.getStabilityPeriods(vertex);
  }

  /**
   * Calculates all relationship stability periods for the given GeoObject.
   */
  public List<StabilityPeriod> getStabilityPeriods(ServerObjectVertex vertex)
  {
    if (vertex == null)
    {
      throw new IllegalArgumentException(
          "A vertex is required."
      );
    }

    Collection<String> relationshipAttributes =
        this.getRelationshipAttributeNames(vertex.getType());

    return this.getStabilityPeriods(
        vertex,
        relationshipAttributes
    );
  }

  /**
   * Calculates stability periods using an explicit collection of edge names.
   */
  public List<StabilityPeriod> getStabilityPeriods(ServerObjectVertex vertex, Collection<String> edgeNames)
  {
    if (vertex == null)
    {
      throw new IllegalArgumentException("A vertex is required.");
    }

    if (edgeNames == null || edgeNames.isEmpty())
    {
      return new ArrayList<StabilityPeriod>();
    }

    List<ValueOverTimeCollection> collections =
        new ArrayList<ValueOverTimeCollection>();

    for (String edgeName : edgeNames)
    {
      if (edgeName == null || edgeName.isEmpty())
      {
        continue;
      }
      
      ValueOverTimeCollection values = getEdges(vertex, edgeName);

      if (values != null && !values.isEmpty())
      {
        collections.add(values);
      }
    }

    return this.calculateStabilityPeriods(collections);
  }
  
  public ValueOverTimeCollection getEdges(ServerObjectVertex vertex, String edgeName)
  {
    ValueOverTimeCollection votc = new ValueOverTimeCollection();

    String statement =
        "SELECT expand(bothE('" + edgeName + "')) FROM :vtx";

    GraphQuery<EdgeObject> query =
        new GraphQuery<EdgeObject>(statement);

    query.setParameter("vtx",vertex.getVertex().getRID());

    String currentOid = vertex.getOid();

    for (EdgeObject edge : query.getResults())
    {
      String parentOid = edge.getParent().getOid();
      String childOid = edge.getChild().getOid();

      /*
       * The ValueOverTime value is the GeoObject on the opposite side
       * of the edge.
       */
      String relatedOid;

      if (currentOid.equals(parentOid))
      {
        relatedOid = childOid;
      }
      else if (currentOid.equals(childOid))
      {
        relatedOid = parentOid;
      }
      else
      {
        /*
         * This should never happen because bothE() should only return edges
         * connected to the current vertex.
         */
        throw new IllegalStateException(
            "Edge ["
                + edge.getOid()
                + "] is not connected to GeoObject ["
                + currentOid
                + "]."
        );
      }

      ValueOverTime vot = new ValueOverTime(
          edge.getOid(),
          edge.getObjectValue(GeoVertex.START_DATE),
          edge.getObjectValue(GeoVertex.END_DATE),
          relatedOid
      );

      votc.add(vot);
    }

    return votc;
  }

  /**
   * Returns the VOT attribute names for all relationships applicable to the
   * GeoObject type:
   *
   * - Hierarchies
   * - Undirected graph relationships
   * - Directed acyclic graph relationships
   * - Business edges involving a GeoObject
   */
  protected Collection<String> getRelationshipAttributeNames(
      ServerGeoObjectType type
  )
  {
    Set<String> attributeNames =
        new LinkedHashSet<String>();

    /*
     * Hierarchies applicable to this GeoObject type.
     */
    this.typeService
        .getHierarchies(type)
        .stream()
        .filter(graphType -> this.hierarchyPermissions.canRead(
            graphType.getOrganizationCode()
        ))
        .forEach(graphType -> {
          attributeNames.add(
              graphType.getObjectEdge().getDBClassName()
          );
        });

    /*
     * Non-hierarchical, undirected relationships.
     */
    this.undirectedService
        .getAll()
        .forEach(graphType -> {
          attributeNames.add(
              graphType.getMdEdgeDAO().getDBClassName()
          );
        });

    /*
     * Directed acyclic graph relationships.
     */
    this.dagService
        .getAll()
        .forEach(graphType -> {
          attributeNames.add(
              graphType.getMdEdgeDAO().getDBClassName()
          );
        });

    /*
     * Business relationships where either side is a GeoObject.
     */
    this.businessEdgeService
        .getAll()
        .stream()
        .filter(this::containsGeoObject)
        .forEach(edgeType -> {
          attributeNames.add(
              edgeType.getMdEdgeDAO().getDBClassName()
          );
        });

    attributeNames.remove(null);
    attributeNames.remove("");

    return attributeNames;
  }

  private boolean containsGeoObject(
      BusinessEdgeType edgeType
  )
  {
    return edgeType.getIsParentGeoObject()
        || edgeType.getIsChildGeoObject();
  }

  /**
   */
  protected List<StabilityPeriod> calculateStabilityPeriods(
      Collection<ValueOverTimeCollection> collections
  )
  {
    /*
     * TreeMap both deduplicates the boundaries and sorts them
     * chronologically.
     */
    Map<LocalDate, DateBoundary> boundaries =
        new TreeMap<LocalDate, DateBoundary>();

    for (ValueOverTimeCollection collection : collections)
    {
      if (collection == null)
      {
        continue;
      }

      for (ValueOverTime value : collection)
      {
        if (value.getStartDate() == null
            || value.getEndDate() == null)
        {
          continue;
        }

        LocalDate startDate =
            this.toLocalDate(value.getStartDate());

        LocalDate endDate =
            this.toLocalDate(value.getEndDate());

        DateBoundary startBoundary =
            boundaries.computeIfAbsent(
                startDate,
                DateBoundary::new
            );

        startBoundary.setStart(true);

        DateBoundary endBoundary =
            boundaries.computeIfAbsent(
                endDate,
                DateBoundary::new
            );

        endBoundary.setEnd(true);
      }
    }

    List<DateBoundary> orderedBoundaries =
        new ArrayList<DateBoundary>(
            boundaries.values()
        );

    List<StabilityPeriod> periods =
        new ArrayList<StabilityPeriod>();

    for (int i = 0; i < orderedBoundaries.size(); ++i)
    {
      DateBoundary current =
          orderedBoundaries.get(i);

      DateBoundary next =
          i + 1 < orderedBoundaries.size()
              ? orderedBoundaries.get(i + 1)
              : null;

      /*
       * A boundary that starts and ends data represents a one-day stability
       * period.
       */
      if (current.isStart() && current.isEnd())
      {
        periods.add(new StabilityPeriod(current.getDate(), current.getDate()));
      }

      /*
       * Avoid creating a period between two directly adjacent VOT ranges.
       *
       * For example:
       *
       * Existing range ends: 2020-01-31
       * Next range starts:    2020-02-01
       */
      if (current.isEnd()
          && next != null
          && next.isStart()
          && current.getDate()
              .plusDays(1)
              .equals(next.getDate()))
      {
        continue;
      }

      LocalDate startDate =
          current.isEnd()
              ? current.getDate().plusDays(1)
              : current.getDate();

      /*
       * As in the TypeScript implementation, no period is generated after the
       * final boundary.
       */
      if (next == null)
      {
        continue;
      }

      /*
       * Do not create periods representing a gap in which no relationship VOT
       * exists.
       */
      if (!this.existsAtDate(collections, startDate))
      {
        continue;
      }

      LocalDate endDate = next.isStart() ? next.getDate().minusDays(1) : next.getDate();

      if (!endDate.isBefore(startDate))
      {
        periods.add(new StabilityPeriod(startDate, endDate));
      }
    }

    periods.sort(
        Comparator.comparing(
            StabilityPeriod::getStartDate
        )
    );

    return periods;
  }

  /**
   * Returns true when at least one relationship ValueOverTime exists on the
   * supplied date.
   *
   * This intentionally does not use ValueOverTimeCollection#getValueOnDate.
   * A temporal record can exist while containing a null value. Stability is
   * determined by the temporal record's range, not the returned value.
   */
  protected boolean existsAtDate(
      Collection<ValueOverTimeCollection> collections,
      LocalDate localDate
  )
  {
    Date date = this.toDate(localDate);

    for (ValueOverTimeCollection collection : collections)
    {
      if (collection == null)
      {
        continue;
      }

      for (ValueOverTime value : collection)
      {
        if (value.getStartDate() != null
            && value.getEndDate() != null
            && value.between(date))
        {
          return true;
        }
      }
    }

    return false;
  }

  protected LocalDate toLocalDate(Date date)
  {
    return date.toInstant()
        .atZone(ZoneOffset.UTC)
        .toLocalDate();
  }

  protected Date toDate(LocalDate date)
  {
    Instant instant =
        date.atStartOfDay()
            .toInstant(ZoneOffset.UTC);

    return Date.from(instant);
  }

  /**
   * A unique date on which one or more temporal values start or end.
   */
  private static final class DateBoundary
  {
    private final LocalDate date;

    private boolean start;

    private boolean end;

    private DateBoundary(LocalDate date)
    {
      this.date = date;
    }

    public LocalDate getDate()
    {
      return date;
    }

    public boolean isStart()
    {
      return start;
    }

    public void setStart(boolean start)
    {
      this.start = start;
    }

    public boolean isEnd()
    {
      return end;
    }

    public void setEnd(boolean end)
    {
      this.end = end;
    }
  }

  /**
   * Jackson-serializable stability-period response object.
   */
  public static class StabilityPeriod
  {
    private LocalDate startDate;
    private LocalDate endDate;
    
    public StabilityPeriod()
    {
    }

    public StabilityPeriod(
        LocalDate startDate,
        LocalDate endDate
    )
    {
      this.startDate = startDate;
      this.endDate = endDate;
    }

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd",
        timezone = "UTC"
    )
    public LocalDate getStartDate()
    {
      return startDate;
    }

    public void setStartDate(LocalDate startDate)
    {
      this.startDate = startDate;
    }

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd",
        timezone = "UTC"
    )
    public LocalDate getEndDate()
    {
      return endDate;
    }

    public void setEndDate(LocalDate endDate)
    {
      this.endDate = endDate;
    }
  }
}
