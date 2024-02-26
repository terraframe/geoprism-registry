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
package net.geoprism.registry.etl.upload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.common.io.OIOException;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestState;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionFacade;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.etl.InvalidExternalIdException;
import net.geoprism.registry.etl.ParentReferenceProblem;
import net.geoprism.registry.etl.RowValidationProblem;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.AttributeCharacterType;
import net.geoprism.registry.graph.AttributeClassificationType;
import net.geoprism.registry.graph.AttributeDoubleType;
import net.geoprism.registry.graph.AttributeLongType;
import net.geoprism.registry.graph.AttributeTermType;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.io.AmbiguousParentException;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.IgnoreRowException;
import net.geoprism.registry.io.InvalidGeometryException;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.ParentCodeException;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.PostalCodeLocationException;
import net.geoprism.registry.io.RequiredMappingException;
import net.geoprism.registry.io.SridException;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class GeoObjectImporter implements ObjectImporterIF
{
  private static enum Action {
    VALIDATE, IMPORT
  }

  private class Task implements Runnable
  {
    private FeatureRow row;

    private Action     action;

    private String     sessionId;

    public Task(FeatureRow row, Action action, String sessionId)
    {
      super();
      this.row = row;
      this.action = action;
      this.sessionId = sessionId;
    }

    @Override
    public void run()
    {
      try
      {
        runInRequest(sessionId);
      }
      catch (InterruptedException e)
      {
        // Do nothing
        e.printStackTrace();
      }
    }

    @Request(RequestType.SESSION)
    public void runInRequest(String sessionId) throws InterruptedException
    {
      if (action.equals(Action.VALIDATE))
      {
        GeoObjectImporter.this.performValidationTask(this.row);
      }
      else if (action.equals(Action.IMPORT))
      {
        GeoObjectImporter.this.performImportTask(this.row);
      }
    }

  }

  private static class RowData
  {
    private String                      goJson;

    private boolean                     isNew;

    private GeoObjectParentErrorBuilder parentBuilder;

    public void setGoJson(String goJson)
    {
      this.goJson = goJson;
    }

    public void setNew(boolean isNew)
    {
      this.isNew = isNew;
    }

    public void setParentBuilder(GeoObjectParentErrorBuilder parentBuilder)
    {
      this.parentBuilder = parentBuilder;
    }

  }

  private static final Logger              logger                     = LoggerFactory.getLogger(GeoObjectImporter.class);

  protected static final String            ERROR_OBJECT_TYPE          = GeoObjectOverTime.class.getName();

  // Refresh the user's session every X amount of records
  private static final long                refreshSessionRecordCount  = GeoregistryProperties.getRefreshSessionRecordCount();

  protected GeoObjectImportConfiguration   configuration;

  protected Map<String, ServerGeoObjectIF> parentCache;

  protected ClassifierCache                classifierCache            = new ClassifierCache();

  protected static final String            parentConcatToken          = "&";

  protected ImportProgressListenerIF       progressListener;

  protected FormatSpecificImporterIF       formatImporter;

  private long                             lastValidateSessionRefresh = 0;

  private long                             lastImportSessionRefresh   = 0;

  private BlockingQueue<Runnable>          blockingQueue;

  private ThreadPoolExecutor               executor;

  protected GPRGeoObjectBusinessServiceIF  service;

  public GeoObjectImporter(GeoObjectImportConfiguration configuration, ImportProgressListenerIF progressListener)
  {
    this.configuration = configuration;
    this.progressListener = progressListener;
    this.service = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);

    final int MAX_ENTRIES = 10000; // The size of our parentCache
    this.parentCache = Collections.synchronizedMap(new LinkedHashMap<String, ServerGeoObjectIF>(MAX_ENTRIES + 1, .75F, true)
    {
      private static final long serialVersionUID = 1L;

      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
      {
        return size() > MAX_ENTRIES;
      }
    });

    this.blockingQueue = new LinkedBlockingDeque<Runnable>(10);

    this.executor = new ThreadPoolExecutor(1, 5, 5, TimeUnit.SECONDS, blockingQueue);
    this.executor.prestartAllCoreThreads();
  }

  protected class GeoObjectParentErrorBuilder
  {
    protected ServerGeoObjectIF parent;

    protected GeoObjectParentErrorBuilder()
    {

    }

    public ServerGeoObjectIF getParent()
    {
      return this.parent;
    }

    public void setParent(ServerGeoObjectIF parent)
    {
      this.parent = parent;
    }

    public JSONArray build()
    {
      JSONArray parents = new JSONArray();

      try
      {
        if (this.getParent() != null)
        {
          // ServerGeoObjectIF serverGo = this.getServerGO();
          final ServerGeoObjectIF parent = this.getParent();
          final List<Location> locations = GeoObjectImporter.this.configuration.getLocations();
          final ServerHierarchyType hierarchy = GeoObjectImporter.this.configuration.getHierarchy();

          String[] types = new String[locations.size() - 1];

          for (int i = 0; i < locations.size() - 1; ++i)
          {
            Location location = locations.get(i);
            types[i] = location.getType().getCode();
          }

          ServerParentTreeNode tnParent = new ServerParentTreeNode(parent, hierarchy, GeoObjectImporter.this.configuration.getStartDate(), GeoObjectImporter.this.configuration.getEndDate(), null);

          ServerParentTreeNodeOverTime grandParentsOverTime = service.getParentsOverTime(parent, null, true, true);

          if (grandParentsOverTime != null && grandParentsOverTime.hasEntries(hierarchy))
          {
            List<ServerParentTreeNode> entries = grandParentsOverTime.getEntries(hierarchy);

            if (entries != null && entries.size() > 0)
            {
              ServerParentTreeNode ptn = grandParentsOverTime.getEntries(hierarchy).get(0);

              tnParent.addParent(ptn);
            }
          }

          ServerParentTreeNodeOverTime parentsOverTime = new ServerParentTreeNodeOverTime(GeoObjectImporter.this.configuration.getType());
          parentsOverTime.add(hierarchy, tnParent);

          return new JSONArray(parentsOverTime.toJSON().toString());
        }
      }
      catch (Throwable t2)
      {
        logger.error("Error constructing parents", t2);
      }

      return parents;
    }
  };

  public FormatSpecificImporterIF getFormatSpecificImporter()
  {
    return formatImporter;
  }

  public void setFormatSpecificImporter(FormatSpecificImporterIF formatImporter)
  {
    this.formatImporter = formatImporter;
  }

  public GeoObjectImportConfiguration getConfiguration()
  {
    return configuration;
  }

  public void validateRow(FeatureRow row) throws InterruptedException
  {
    this.blockingQueue.put(new Task(row, Action.VALIDATE, Session.getCurrentSession().getOid()));
  }

  @Transaction
  private void performValidationTask(FeatureRow row) throws InterruptedException
  {
    try
    {
      // Refresh the session because it might expire on long imports
      final long curRowNumber = row.getRowNumber().longValue();
      if ( ( this.lastValidateSessionRefresh + GeoObjectImporter.refreshSessionRecordCount ) < curRowNumber)
      {
        if (Session.getCurrentSession() != null)
        {
          SessionFacade.renewSession(Session.getCurrentSession().getOid());
        }
        this.lastValidateSessionRefresh = curRowNumber;
      }

      /*
       * 1. Check for location problems
       */
      if (this.configuration.isPostalCode() && PostalCodeFactory.isAvailable(this.configuration.getType()))
      {
        // Skip location synonym check
      }
      else if (this.configuration.getHierarchy() != null && this.configuration.getLocations().size() > 0)
      {
        this.getParent(row);
      }

      /*
       * 2. Check for serialization and term problems
       */
      String code = this.getCode(row);

      ServerGeoObjectIF entity;

      if (code == null || code.length() <= 0)
      {
        RequiredMappingException ex = new RequiredMappingException();
        ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
        throw ex;
      }

      entity = service.newInstance(this.configuration.getType());
      entity.setCode(code);
      entity.setInvalid(false);

      try
      {
        LocalizedValue entityName = this.getName(row);
        if (entityName != null && this.hasValue(entityName))
        {
          entity.setDisplayLabel(entityName, this.configuration.getStartDate(), this.configuration.getEndDate());
        }

        Geometry geometry = (Geometry) this.getFormatSpecificImporter().getGeometry(row);

        geometry = convertGeometry(geometry);

        if (geometry != null)
        {
          entity.setGeometry(geometry, this.configuration.getStartDate(), this.configuration.getEndDate());
        }

        Map<String, AttributeType> attributes = this.configuration.getType().getAttributeMap();
        Set<Entry<String, AttributeType>> entries = attributes.entrySet();

        for (Entry<String, AttributeType> entry : entries)
        {
          String attributeName = entry.getKey();

          if (!attributeName.equals(GeoObject.CODE))
          {
            ShapefileFunction function = this.configuration.getFunction(attributeName);

            if (function != null)
            {
              Object value = function.getValue(row);

              if (value != null && !this.isEmptyString(value))
              {
                AttributeType attributeType = entry.getValue();

                this.setValue(entity, attributeType, attributeName, value, row);
              }
            }
          }
        }

        GeoObjectOverTime go = this.service.toGeoObjectOverTime(entity, false, this.classifierCache);
        go.toJSON().toString();

        if (this.configuration.isExternalImport())
        {
          ShapefileFunction function = this.configuration.getExternalIdFunction();

          Object value = function.getValue(row);

          if (value == null || ! ( value instanceof String || value instanceof Integer || value instanceof Long ) || ( value instanceof String && ( (String) value ).length() == 0 ))
          {
            throw new InvalidExternalIdException();
          }
        }
      }
      catch (IgnoreRowException e)
      {
        // Do nothing
      }
      catch (Throwable t)
      {
        RowValidationProblem problem = new RowValidationProblem(t);
        problem.addAffectedRowNumber(curRowNumber);
        problem.setHistoryId(this.configuration.historyId);

        this.progressListener.addRowValidationProblem(problem);
      }

      this.progressListener.setCompletedRow(curRowNumber);

      if (Thread.interrupted())
      {
        throw new InterruptedException();
      }

      Thread.yield();
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }

  private boolean isEmptyString(Object value)
  {
    if (value instanceof String)
    {
      return ( (String) value ).trim().length() == 0;
    }

    return false;
  }

  public void importRow(FeatureRow row) throws InterruptedException
  {
    if (!this.progressListener.isComplete(row.getRowNumber()))
    {
      this.blockingQueue.put(new Task(row, Action.IMPORT, Session.getCurrentSession().getOid()));
    }
  }

  /**
   * Imports a GeoObject based on the given SimpleFeature.
   * 
   * @param feature
   * @throws InterruptedException
   * @throws Exception
   */
  private void performImportTask(FeatureRow row) throws InterruptedException
  {
    RowData data = new RowData();

    try
    {
      try
      {
        boolean imported = false;

        /*
         * In a multi-threaded import it is likely to get an
         * OCurrentModificationException because adding a link to the parent geo
         * object adds a pointer to the parent vertex and causes an optimistic
         * lock check on the parent vertex. So if multiple geo-objects are
         * assigned to the same parent at the same time the system will throw a
         * OConcurrentModificationException. Retry the commit again.
         */

        ProgrammingErrorException ex = null;

        for (int i = 0; i < 10; i++)
        {
          try
          {
            imported = this.importRowInTrans(row, data);

            break;
          }
          catch (ProgrammingErrorException e)
          {
            ex = e;

            if (! ( ( e.getCause() instanceof OConcurrentModificationException ) || ( e.getCause() instanceof OIOException ) ))
            {
              throw e;
            }
          }
        }

        if (imported)
        {
          // System.out.println("Executed " + (
          // ++GeoObjectImporter.this.executeCount ));

          this.progressListener.incrementImportedRecords();
        }
        else if (ex != null)
        {
          throw ex;
        }
      }
      catch (DuplicateDataException e)
      {
        try
        {
          this.service.handleDuplicateDataException(this.configuration.getType(), e);
        }
        catch (Throwable t)
        {
          buildRecordException(data.goJson, data.isNew, data.parentBuilder, t);
        }
      }
    }
    catch (GeoObjectRecordedErrorException e)
    {
      this.recordError(e, row.getRowNumber());
    }

    this.progressListener.setCompletedRow(row.getRowNumber());

    if (Thread.interrupted())
    {
      throw new InterruptedException();
    }

    Thread.yield();
  }

  @Transaction
  private void recordError(GeoObjectRecordedErrorException e, Long rowNumber)
  {
    JSONObject obj = new JSONObject(e.getObjectJson());

    GeoObjectParentErrorBuilder parentBuilder = e.getParentBuilder();
    if (parentBuilder != null)
    {
      obj.put("parents", parentBuilder.build());
    }

    this.progressListener.recordError(e.getError(), obj.toString(), e.getObjectType(), rowNumber);
    this.getConfiguration().addException(e);
  }

  @Transaction
  public boolean importRowInTrans(FeatureRow row, RowData data)
  {
    boolean imported = false;

    // Refresh the session because it might expire on long imports
    final long curRowNum = row.getRowNumber().longValue();
    if ( ( this.lastImportSessionRefresh + GeoObjectImporter.refreshSessionRecordCount ) < curRowNum)
    {
      if (Session.getCurrentSession() != null)
      {
        SessionFacade.renewSession(Session.getCurrentSession().getOid());
      }
      this.lastImportSessionRefresh = curRowNum;
    }

    GeoObjectOverTime go = null;

    String goJson = null;

    ServerGeoObjectIF serverGo = null;

    ServerGeoObjectIF parent = null;

    boolean isNew = false;

    GeoObjectParentErrorBuilder parentBuilder = new GeoObjectParentErrorBuilder();

    try
    {
      String code = this.getCode(row);

      if (code == null || code.length() <= 0)
      {
        RequiredMappingException ex = new RequiredMappingException();
        ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
        throw ex;
      }

      if (this.configuration.getImportStrategy().equals(ImportStrategy.UPDATE_ONLY) || this.configuration.getImportStrategy().equals(ImportStrategy.NEW_AND_UPDATE))
      {
        serverGo = service.getGeoObjectByCode(code, this.configuration.getType(), false);
      }

      if (serverGo == null)
      {
        if (this.configuration.getImportStrategy().equals(ImportStrategy.UPDATE_ONLY))
        {
          net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
          ex.setTypeLabel(GeoObjectMetadata.get().getClassDisplayLabel());
          ex.setDataIdentifier(code);
          ex.setAttributeLabel(GeoObjectMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }

        isNew = true;

        serverGo = service.newInstance(this.configuration.getType());
        serverGo.setCode(code);
        serverGo.setInvalid(false);
      }

      LocalizedValue entityName = this.getName(row);
      if (entityName != null && this.hasValue(entityName))
      {
        serverGo.setDisplayLabel(entityName, this.configuration.getStartDate(), this.configuration.getEndDate());
      }

      Geometry geometry = (Geometry) this.getFormatSpecificImporter().getGeometry(row);

      geometry = convertGeometry(geometry);

      if (geometry != null)
      {
        serverGo.setGeometry(geometry, this.configuration.getStartDate(), this.configuration.getEndDate());
      }

      if (isNew)
      {
        serverGo.setUid(ServiceFactory.getIdService().getUids(1)[0]);
      }

      // Set exists first so we can validate attributes on it
      // ShapefileFunction existsFunction =
      // this.configuration.getFunction(DefaultAttribute.EXISTS.getName());
      //
      // if (existsFunction != null)
      // {
      // Object value = existsFunction.getValue(row);
      //
      // if (value != null && !this.isEmptyString(value))
      // {
      // this.setValue(serverGo,
      // this.configuration.getType().getAttribute(DefaultAttribute.EXISTS.getName()).get(),
      // DefaultAttribute.EXISTS.getName(), value);
      // }
      // }
      // else if (isNew)
      // {
      // ValueOverTime defaultExists = ((VertexServerGeoObject)
      // serverGo).buildDefaultExists();
      // if (defaultExists != null)
      // {
      // serverGo.setValue(DefaultAttribute.EXISTS.getName(), Boolean.TRUE,
      // defaultExists.getStartDate(), defaultExists.getEndDate());
      // }
      // }
      this.setValue(serverGo, this.configuration.getType().getAttribute(DefaultAttribute.EXISTS.getName()).get(), DefaultAttribute.EXISTS.getName(), true, row);

      Map<String, AttributeType> attributes = this.configuration.getType().getAttributeMap();
      Set<Entry<String, AttributeType>> entries = attributes.entrySet();

      for (Entry<String, AttributeType> entry : entries)
      {
        String attributeName = entry.getKey();

        if (!attributeName.equals(GeoObject.CODE) && !attributeName.equals(DefaultAttribute.EXISTS.getName()))
        {
          ShapefileFunction function = this.configuration.getFunction(attributeName);

          if (function != null)
          {
            Object value = function.getValue(row);

            AttributeType attributeType = entry.getValue();

            if (value != null && !this.isEmptyString(value))
            {
              // if (!(existsFunction == null && isNew))
              // {
              // try
              // {
              // ((VertexServerGeoObject)
              // serverGo).enforceAttributeSetWithinRange(serverGo.getDisplayLabel().getValue(),
              // attributeName, this.configuration.getStartDate(),
              // this.configuration.getEndDate());
              // }
              // catch (ValueOutOfRangeException e)
              // {
              // final SimpleDateFormat format =
              // ValueOverTimeDTO.getTimeFormatter();
              //
              // ImportOutOfRangeException ex = new
              // ImportOutOfRangeException();
              // ex.setStartDate(format.format(this.configuration.getStartDate()));
              //
              // if
              // (ValueOverTime.INFINITY_END_DATE.equals(this.configuration.getEndDate()))
              // {
              // ex.setEndDate(LocalizationFacade.localize("changeovertime.present"));
              // }
              // else
              // {
              // ex.setEndDate(format.format(this.configuration.getEndDate()));
              // }
              //
              // throw ex;
              // }
              // }

              this.setValue(serverGo, attributeType, attributeName, value, row);
            }
            else if (this.configuration.getCopyBlank())
            {
              this.setValue(serverGo, attributeType, attributeName, null, row);
            }
          }
        }
      }

      go = this.service.toGeoObjectOverTime(serverGo, false, this.classifierCache);
      goJson = go.toJSON().toString();

      /*
       * Try to get the parent and ensure that this row is not ignored. The
       * getParent method will throw a IgnoreRowException if the parent is
       * configured to be ignored.
       */
      if (this.configuration.isPostalCode() && PostalCodeFactory.isAvailable(this.configuration.getType()))
      {
        parent = this.parsePostalCode(row);
      }
      else if (this.configuration.getHierarchy() != null && this.configuration.getLocations().size() > 0)
      {
        parent = this.getParent(row);
      }
      parentBuilder.setParent(parent);

      if (this.progressListener.hasValidationProblems())
      {
        throw new RuntimeException("Did not expect to encounter validation problems during import.");
      }

      data.setGoJson(goJson);
      data.setNew(isNew);
      data.setParentBuilder(parentBuilder);

      this.service.apply(serverGo, true);

      imported = true;

      if (this.configuration.isExternalImport())
      {
        ShapefileFunction function = this.configuration.getExternalIdFunction();

        Object value = function.getValue(row);

        this.service.createExternalId(serverGo, this.configuration.getExternalSystem(), String.valueOf(value), this.configuration.getImportStrategy());
      }

      if (parent != null)
      {
        if (!isNew)
        {
          this.service.addChild(parent, serverGo, this.configuration.getHierarchy(), this.configuration.getStartDate(), this.configuration.getEndDate());
        }
        else
        {
          // If we're a new object, we can speed things up quite a bit here by
          // just directly applying the edge object since the addChild method
          // does a lot of unnecessary validation.
          this.service.addParentRaw(serverGo, ( (VertexServerGeoObject) parent ).getVertex(), this.configuration.getHierarchy().getObjectEdge(), this.configuration.getStartDate(), this.configuration.getEndDate());
        }
      }
      else if (isNew)
      {
        // GeoEntity child = GeoEntity.getByKey(serverGo.getCode());
        // GeoEntity root = GeoEntity.getByKey(GeoEntity.ROOT);
        //
        // child.addLink(root,
        // this.configuration.getHierarchy().getEntityType());
      }

      // We must ensure that any problems created during the transaction are
      // logged now instead of when the request returns. As such, if any
      // problems exist immediately throw a ProblemException so that normal
      // exception handling can occur.
      List<ProblemIF> problems = RequestState.getProblemsInCurrentRequest();

      List<ProblemIF> problems2 = new LinkedList<ProblemIF>();
      for (ProblemIF problem : problems)
      {
        problems2.add(problem);
      }

      if (problems.size() != 0)
      {
        throw new ProblemException(null, problems2);
      }
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
    catch (Throwable t)
    {
      buildRecordException(goJson, isNew, parentBuilder, t);
    }

    return imported;
  }

  /**
   * Our system represents Points as MultiPoints. The source geometries might
   * need to be wrapped to support import.
   */
  private Geometry convertGeometry(Geometry in)
  {
    if (in == null)
    {
      return null;
    }

    // 0 is JTS's way of saying "we don't know what the SRID is"
    if (in.getSRID() > 0 && in.getSRID() != 4326)
    {
      throw new SridException();
    }

    if (!in.isValid())
    {
      throw new InvalidGeometryException();
    }

    ImportConfiguration configuration = this.getConfiguration();

    if (configuration instanceof GeoObjectImportConfiguration)
    {
      ServerGeoObjectType type = ( (GeoObjectImportConfiguration) configuration ).getType();

      if (type.getGeometryType().equals(GeometryType.MIXED))
      {
        return in;
      }
      else if (type.getGeometryType().equals(GeometryType.POINT) || type.getGeometryType().equals(GeometryType.LINE) || type.getGeometryType().equals(GeometryType.POLYGON))
      {
        if ( ( type.getGeometryType().equals(GeometryType.POINT) && !in.getGeometryType().equals(Geometry.TYPENAME_POINT) ) || ( type.getGeometryType().equals(GeometryType.LINE) && ! ( in.getGeometryType().equals(Geometry.TYPENAME_LINEARRING) || in.getGeometryType().equals(Geometry.TYPENAME_LINESTRING) ) ) || ( type.getGeometryType().equals(GeometryType.POLYGON) && !in.getGeometryType().equals(Geometry.TYPENAME_POLYGON) ))
        {
          throw new InvalidGeometryException();
        }

        return in;
      }
      else if (type.getGeometryType().equals(GeometryType.MULTIPOINT))
      {
        if (in.getGeometryType().equals(Geometry.TYPENAME_POINT))
        {
          return in.getFactory().createMultiPoint(new Point[] { (Point) in });
        }
        else if (in.getGeometryType().equals(Geometry.TYPENAME_MULTIPOINT))
        {
          return in;
        }
      }
      else if (type.getGeometryType().equals(GeometryType.MULTIPOLYGON))
      {
        if (in.getGeometryType().equals(Geometry.TYPENAME_POLYGON))
        {
          return in.getFactory().createMultiPolygon(new Polygon[] { (Polygon) in });
        }
        else if (in.getGeometryType().equals(Geometry.TYPENAME_MULTIPOLYGON))
        {
          return in;
        }
      }
      else if (type.getGeometryType().equals(GeometryType.MULTILINE))
      {
        if (in.getGeometryType().equals(Geometry.TYPENAME_LINESTRING))
        {
          return in.getFactory().createMultiLineString(new LineString[] { (LineString) in });
        }
        else if (in.getGeometryType().equals(Geometry.TYPENAME_MULTILINESTRING))
        {
          return in;
        }
      }
    }

    throw new InvalidGeometryException();
  }

  private void buildRecordException(String goJson, boolean isNew, GeoObjectParentErrorBuilder parentBuilder, Throwable t)
  {
    JSONObject obj = new JSONObject();

    if (goJson != null)
    {
      obj.put("geoObject", new JSONObject(goJson));
    }

    obj.put("isNew", isNew);

    GeoObjectRecordedErrorException re = new GeoObjectRecordedErrorException();
    re.setError(t);
    re.setObjectJson(obj.toString());
    re.setObjectType(ERROR_OBJECT_TYPE);
    re.setParentBuilder(parentBuilder);
    throw re;
  }

  private boolean hasValue(LocalizedValue value)
  {
    String defaultLocale = value.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE);

    return defaultLocale != null && defaultLocale.length() > 0;
  }

  /**
   * @param feature
   *          Shapefile feature
   * 
   * @return The geoId as defined by the 'oid' attribute on the feature. If the
   *         geoId is null then a blank geoId is returned.
   */
  protected String getCode(FeatureRow row)
  {
    ShapefileFunction function = this.configuration.getFunction(GeoObject.CODE);

    if (function == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.configuration.getType().getAttribute(GeoObject.CODE).get().getLocalizedLabel().getValue());
      throw ex;
    }

    Object code = function.getValue(row);

    if (code != null)
    {
      return code.toString();
    }

    return null;
  }

  /**
   * Returns the entity as defined by the 'parent' and 'parentType' attributes
   * of the given feature. If an entity is not found then Earth is returned by
   * default. The 'parent' value of the feature must define an entity name or a
   * geo oid. The 'parentType' value of the feature must define the localized
   * display label of the universal.
   * 
   * This algorithm resolves parent contexts starting at the top of the
   * hierarchy and traversing downward, resolving hierarchy inheritance as
   * needed. If at any point a location cannot be found, a SmartException will
   * be thrown which varies depending on the ParentMatchStrategy.
   *
   * @param feature
   *          Shapefile feature used to determine the parent
   * @return Parent entity
   */
  private ServerGeoObjectIF getParent(FeatureRow feature)
  {
    List<Location> locations = this.configuration.getLocations();

    ServerGeoObjectIF parent = null;

    JSONArray context = new JSONArray();

    ArrayList<String> parentKeyBuilder = new ArrayList<String>();

    for (Location location : locations)
    {
      Object label = getParentCode(feature, location);

      if (label != null)
      {
        String key = parent != null ? parent.getCode() + "-" + label : label.toString();

        parentKeyBuilder.add(label.toString());

        if (this.configuration.isExclusion(GeoObjectImportConfiguration.PARENT_EXCLUSION, key))
        {
          throw new IgnoreRowException();
        }

        // Check the parent cache
        String parentChainKey = StringUtils.join(parentKeyBuilder, parentConcatToken);
        if (this.parentCache.containsKey(parentChainKey))
        {
          parent = this.parentCache.get(parentChainKey);

          JSONObject element = new JSONObject();
          element.put("label", label.toString());
          element.put("type", location.getType().getLabel().getValue());

          context.put(element);

          continue;
        }

        final ParentMatchStrategy ms = location.getMatchStrategy();

        // Search
        ServerGeoObjectQuery query = this.service.createQuery(location.getType(), this.configuration.getStartDate());
        query.setLimit(20);

        if (ms.equals(ParentMatchStrategy.CODE))
        {
          query.setRestriction(new ServerCodeRestriction(label.toString()));
        }
        else if (ms.equals(ParentMatchStrategy.EXTERNAL))
        {
          query.setRestriction(new ServerExternalIdRestriction(this.getConfiguration().getExternalSystem(), label.toString()));
        }
        else if (ms.equals(ParentMatchStrategy.DHIS2_PATH))
        {
          String path = label.toString();

          String dhis2Parent;
          try
          {
            if (path.startsWith("/"))
            {
              path = path.substring(1);
            }

            String pathArr[] = path.split("/");

            dhis2Parent = pathArr[pathArr.length - 2];
          }
          catch (Throwable t)
          {
            InvalidDhis2PathException ex = new InvalidDhis2PathException(t);
            ex.setDhis2Path(path);
            throw ex;
          }

          query.setRestriction(new ServerExternalIdRestriction(this.getConfiguration().getExternalSystem(), dhis2Parent));
        }
        else
        {
          query.setRestriction(new ServerSynonymRestriction(label.toString(), this.configuration.getStartDate(), parent, location.getHierarchy()));
        }

        List<ServerGeoObjectIF> results = query.getResults();

        if (results != null && results.size() > 0)
        {
          ServerGeoObjectIF result = null;

          // There may be multiple results because our query doesn't filter out
          // relationships that don't fit the date criteria
          // You can't really add a date filter on a match query. Look at
          // RegistryService.getGeoObjectSuggestions for an example
          // of how this date filter could maybe be rewritten to be included
          // into the query SQL.
          for (ServerGeoObjectIF loop : results)
          {
            if (result != null && !result.getCode().equals(loop.getCode()))
            {
              AmbiguousParentException ex = new AmbiguousParentException();
              ex.setParentLabel(label.toString());
              ex.setContext(context.toString());
              throw ex;
            }

            result = loop;
          }

          parent = result;

          JSONObject element = new JSONObject();
          element.put("label", label.toString());
          element.put("type", location.getType().getLabel().getValue());

          context.put(element);

          this.parentCache.put(parentChainKey, parent);
        }
        else
        {
          if (context.length() == 0)
          {
            GeoObject root = this.configuration.getRoot();

            if (root != null)
            {
              JSONObject element = new JSONObject();
              element.put("label", root.getLocalizedDisplayLabel());
              element.put("type", root.getType().getLabel().getValue());

              context.put(element);
            }
          }

          if (ms.equals(ParentMatchStrategy.CODE))
          {
            final ParentCodeException ex = new ParentCodeException();
            ex.setParentCode(label.toString());
            ex.setParentType(location.getType().getLabel().getValue());
            ex.setContext(context.toString());

            throw ex;
          }
          else if (ms.equals(ParentMatchStrategy.EXTERNAL))
          {
            final ExternalParentReferenceException ex = new ExternalParentReferenceException();
            ex.setExternalId(label.toString());
            ex.setParentType(location.getType().getLabel().getValue());
            ex.setContext(context.toString());

            throw ex;
          }
          else
          {
            String parentCode = ( parent == null ) ? null : parent.getCode();

            ParentReferenceProblem prp = new ParentReferenceProblem(location.getType().getCode(), label.toString(), parentCode, context.toString());
            prp.addAffectedRowNumber(feature.getRowNumber());
            prp.setHistoryId(this.configuration.historyId);

            this.progressListener.addReferenceProblem(prp);
          }

          return null;
        }
      }
    }

    return parent;
  }

  protected Object getParentCode(FeatureRow feature, Location location)
  {
    ShapefileFunction function = location.getFunction();
    return function.getValue(feature);
  }

  private ServerGeoObjectIF parsePostalCode(FeatureRow feature)
  {
    LocationBuilder builder = PostalCodeFactory.get(this.configuration.getType());
    Location location = builder.build(this.configuration.getFunction(GeoObject.CODE));

    ShapefileFunction function = location.getFunction();
    String code = (String) function.getValue(feature);

    if (code != null)
    {
      // Search
      ServerGeoObjectQuery query = this.service.createQuery(location.getType(), this.configuration.getStartDate());
      query.setRestriction(new ServerCodeRestriction(code));

      // Assert.assertNull(query.getSingleResult());

      ServerGeoObjectIF result = query.getSingleResult();

      if (result != null)
      {
        return result;
      }
      else
      {
        PostalCodeLocationException e = new PostalCodeLocationException();
        e.setCode(code);
        e.setTypeLabel(location.getType().getLabel().getValue());

        throw e;
      }
    }

    return null;
  }

  /**
   * @param feature
   * @return The entityName as defined by the 'name' attribute of the feature
   */
  private LocalizedValue getName(FeatureRow row)
  {
    ShapefileFunction function = this.configuration.getFunction(GeoObject.DISPLAY_LABEL);

    if (function == null)
    {
      // RequiredMappingException ex = new RequiredMappingException();
      // ex.setAttributeLabel(this.configuration.getType().getAttribute(GeoObject.DISPLAY_LABEL).get().getLabel().getValue());
      // throw ex;

      return null;
    }

    Object attribute = function.getValue(row);

    if (attribute != null)
    {
      return (LocalizedValue) attribute;
    }

    return null;
  }

  protected void setTermValue(ServerGeoObjectIF entity, AttributeType attributeType, String attributeName, Object value, Date startDate, Date endDate, FeatureRow feature)
  {
    if (!this.configuration.isExclusion(attributeName, value.toString()))
    {
      // TODO: HEADS UP
      // try
      // {
      // ServerGeoObjectType type = this.configuration.getType();
      // MdBusinessDAOIF mdBusiness = type.getMdBusinessDAO();
      // MdAttributeTermDAOIF mdAttribute = (MdAttributeTermDAOIF)
      // mdBusiness.definesAttribute(attributeName);
      //
      // if (mdAttribute == null && type.getSuperType() != null)
      // {
      // mdAttribute = (MdAttributeTermDAOIF)
      // type.getSuperType().getMdBusinessDAO().definesAttribute(attributeName);
      // }
      //
      // Classifier classifier =
      // Classifier.findMatchingTerm(value.toString().trim(), mdAttribute);
      //
      // if (classifier == null)
      // {
      // Term rootTerm = ( (AttributeTermType) attributeType ).getRootTerm();
      //
      // TermReferenceProblem trp = new TermReferenceProblem(value.toString(),
      // rootTerm.getCode(), mdAttribute.getOid(), attributeName,
      // attributeType.getLabel().getValue());
      // trp.addAffectedRowNumber(feature.getRowNumber());
      // trp.setHistoryId(this.configuration.getHistoryId());
      //
      // this.progressListener.addReferenceProblem(trp);
      // }
      // else
      // {
      // entity.setValue(attributeName, classifier.getOid(), startDate,
      // endDate);
      // }
      // }
      // catch (UnknownTermException e)
      // {
      // TermValueException ex = new TermValueException();
      // ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
      // ex.setCode(e.getCode());
      //
      // throw e;
      // }
    }
  }

  protected void setClassificationValue(ServerGeoObjectIF entity, AttributeType attributeType, String attributeName, Object value, Date startDate, Date endDate)
  {
    if (!this.configuration.isExclusion(attributeName, value.toString()))
    {
      // TODO: HEADS UP
      // try
      // {
      // ServerGeoObjectType type = this.configuration.getType();
      // MdBusinessDAOIF mdBusiness = type.getMdBusinessDAO();
      // MdAttributeClassificationDAOIF mdAttribute =
      // (MdAttributeClassificationDAOIF)
      // mdBusiness.definesAttribute(attributeName);
      //
      // if (mdAttribute == null && type.getSuperType() != null)
      // {
      // mdAttribute = (MdAttributeClassificationDAOIF)
      // type.getSuperType().getMdBusinessDAO().definesAttribute(attributeName);
      // }
      //
      // if (mdAttribute == null)
      // {
      // throw new ProgrammingErrorException("Unable to find mdAttribute: " +
      // attributeName);
      // }
      //
      // VertexObject classifier =
      // this.classifierCache.getClassifier(mdAttribute.getMdClassificationDAOIF().definesType(),
      // value.toString().trim());
      //
      // if (classifier == null)
      // {
      // classifier =
      // AbstractClassification.findMatchingClassification(value.toString().trim(),
      // mdAttribute);
      //
      // if (classifier != null)
      // {
      // this.classifierCache.putClassifier(mdAttribute.getMdClassificationDAOIF().definesType(),
      // classifier.getObjectValue("code"), classifier);
      // }
      // }
      //
      // if (classifier == null)
      // {
      // throw new UnknownTermException(value.toString().trim(), attributeType);
      // // Term rootClassification = ( (AttributeClassificationType)
      // // attributeType ).getRootTerm();
      // //
      // // TermReferenceProblem trp = new
      // // TermReferenceProblem(value.toString(),
      // // rootClassification.getCode(), mdAttribute.getOid(), attributeName,
      // // attributeType.getLabel().getValue());
      // // trp.addAffectedRowNumber(this.progressListener.getWorkProgress() +
      // // 1);
      // // trp.setHistoryId(this.configuration.getHistoryId());
      // //
      // // this.progressListener.addReferenceProblem(trp);
      // }
      // else
      // {
      // AttributeClassification attrClass = ( (AttributeClassification) (
      // (VertexServerGeoObject) entity
      // ).getVertex().getGraphObjectDAO().getAttribute(attributeName) );
      // Boolean validationResult =
      // this.classifierCache.getClassifierAttributeValidation(mdAttribute.getOid(),
      // classifier);
      //
      // if (validationResult == null)
      // {
      // validationResult = attrClass.validateRid(classifier.getRID(), false);
      //
      // this.classifierCache.putClassifierAttributeValidation(mdAttribute.getOid(),
      // classifier, validationResult);
      // }
      //
      // if (Boolean.TRUE.equals(validationResult))
      // {
      // attrClass.setSkipValidation(true);
      //
      // try
      // {
      // attrClass.setValue(classifier, startDate, endDate);
      // }
      // finally
      // {
      // attrClass.setSkipValidation(false);
      // }
      // }
      // else
      // {
      // throw new ClassificationValidationException("Value must be a child of
      // the attribute root");
      // }
      // }
      // }
      // catch (UnknownTermException e)
      // {
      // TermValueException ex = new TermValueException();
      // ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
      // ex.setCode(e.getCode());
      //
      // throw e;
      // }
    }
  }

  protected void setValue(ServerGeoObjectIF entity, AttributeType attributeType, String attributeName, Object value, FeatureRow row)
  {
    if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      entity.setDisplayLabel((LocalizedValue) value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeClassificationType)
    {
      if (value != null)
      {
        this.setClassificationValue(entity, attributeType, attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else
      {
        entity.setValue(attributeName, null, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
    }
    else if (attributeType instanceof AttributeTermType)
    {
      if (value != null)
      {
        this.setTermValue(entity, attributeType, attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate(), row);
      }
      else
      {
        entity.setValue(attributeName, null, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
    }
    else if (attributeType instanceof AttributeLongType)
    {
      if (value == null)
      {
        entity.setValue(attributeName, null, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else if (value instanceof String)
      {
        entity.setValue(attributeName, Long.valueOf((String) value), this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else if (value instanceof Number)
      {
        entity.setValue(attributeName, ( (Number) value ).longValue(), this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }
    else if (attributeType instanceof AttributeDoubleType)
    {
      if (value == null)
      {
        entity.setValue(attributeName, null, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else if (value instanceof String)
      {
        entity.setValue(attributeName, Double.valueOf((String) value), this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else if (value instanceof Number)
      {
        entity.setValue(attributeName, ( (Number) value ).doubleValue(), this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }
    else if (attributeType instanceof AttributeCharacterType)
    {
      if (value == null)
      {
        entity.setValue(attributeName, null, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else
      {
        entity.setValue(attributeName, value.toString(), this.configuration.getStartDate(), this.configuration.getEndDate());
      }
    }
    else
    {
      entity.setValue(attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
  }

  @Override
  public void close()
  {
    executor.shutdown();
    try
    {
      executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}