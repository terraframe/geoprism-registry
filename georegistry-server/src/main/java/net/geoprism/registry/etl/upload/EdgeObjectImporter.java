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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
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
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.GeoObjectCache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.etl.EdgeJsonImporter;
import net.geoprism.registry.io.AmbiguousParentException;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.IgnoreRowException;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentCodeException;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.io.RequiredMappingException;
import net.geoprism.registry.jobs.ParentReferenceProblem;
import net.geoprism.registry.jobs.RowValidationProblem;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class EdgeObjectImporter implements ObjectImporterIF
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
        EdgeObjectImporter.this.performValidationTask(this.row);
      }
      else if (action.equals(Action.IMPORT))
      {
        EdgeObjectImporter.this.performImportTask(this.row);
      }
    }

  }

  private static class RowData
  {
    private String                goJson;

    private boolean               isNew;

    public void setGoJson(String goJson)
    {
      this.goJson = goJson;
    }

    public void setNew(boolean isNew)
    {
      this.isNew = isNew;
    }
  }

  private static final Logger                 logger                     = LoggerFactory.getLogger(BusinessObjectImporter.class);

  protected static final String               ERROR_OBJECT_TYPE          = GeoObjectOverTime.class.getName();

  protected static final String               parentConcatToken          = "&";

  // Refresh the user's session every X amount of records
  private static final long                   refreshSessionRecordCount  = GeoregistryProperties.getRefreshSessionRecordCount();

  protected EdgeObjectImportConfiguration configuration;

  protected GeoObjectCache    goCache;
  protected Cache<String, Object> goRidCache;

  protected ImportProgressListenerIF          progressListener;

  protected FormatSpecificImporterIF          formatImporter;

  private long                                lastValidateSessionRefresh = 0;

  private long                                lastImportSessionRefresh   = 0;

  private BlockingQueue<Runnable>             blockingQueue;

  private ThreadPoolExecutor                  executor;

  private GeoObjectBusinessServiceIF          objectService;
  
  public EdgeObjectImporter(EdgeObjectImportConfiguration configuration, ImportProgressListenerIF progressListener)
  {
    this.objectService = ServiceFactory.getBean(GeoObjectBusinessServiceIF.class);

    this.configuration = configuration;
    this.progressListener = progressListener;

    goCache    = new GeoObjectCache();
    goRidCache = new LRUCache<String, Object>(10000);

    this.blockingQueue = new LinkedBlockingDeque<Runnable>(50);

    this.executor = new ThreadPoolExecutor(10, 20, 5, TimeUnit.SECONDS, blockingQueue);
    this.executor.prestartAllCoreThreads();
  }

  public FormatSpecificImporterIF getFormatSpecificImporter()
  {
    return formatImporter;
  }

  public void setFormatSpecificImporter(FormatSpecificImporterIF formatImporter)
  {
    this.formatImporter = formatImporter;
  }

  @Override
  public EdgeObjectImportConfiguration getConfiguration()
  {
    return this.configuration;
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
      if ( ( this.lastValidateSessionRefresh + EdgeObjectImporter.refreshSessionRecordCount ) < row.getRowNumber())
      {
        SessionFacade.renewSession(Session.getCurrentSession().getOid());
        this.lastValidateSessionRefresh = row.getRowNumber();
      }

      try
      {
        String sourceCode = getValue("edgeSource", row);
        String sourceTypeCode = getValue("edgeSourceType", row);
        String targetCode = getValue("edgeTarget", row);
        String targetTypeCode = getValue("edgeTargetType", row);
        
        Date startDate = this.configuration.getStartDate();
        if (startDate == null) {
          RequiredMappingException ex = new RequiredMappingException();
          ex.setAttributeLabel("startDate");
          throw ex;
        }
        
        Date endDate = this.configuration.getEndDate();
        if (endDate == null) {
          RequiredMappingException ex = new RequiredMappingException();
          ex.setAttributeLabel("endDate");
          throw ex;
        }
        
        goCache.getOrFetchByCode(sourceCode, sourceTypeCode);
        goCache.getOrFetchByCode(targetCode, targetTypeCode);
      }
      catch (IgnoreRowException e)
      {
        // Do nothing
      }
      catch (Throwable t)
      {
        RowValidationProblem problem = new RowValidationProblem(t);
        problem.addAffectedRowNumber(row.getRowNumber());
        problem.setHistoryId(this.configuration.historyId);

        this.progressListener.addRowValidationProblem(problem);
      }

      this.progressListener.setCompletedRow(row.getRowNumber());

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
        for (int i = 0; i < 100; i++)
        {
          try
          {
            imported = this.importRowInTrans(row, data);

            break;
          }
          catch (ProgrammingErrorException e)
          {
            if (! ( e.getCause() instanceof OConcurrentModificationException ))
            {
              throw e;
            }
          }
        }

        if (imported)
        {
          this.progressListener.incrementImportedRecords();
        }
      }
      catch (DuplicateDataException e)
      {
        buildRecordException(data.goJson, data.isNew, e);
      }
    }
    catch (EdgeObjectRecordedErrorException e)
    {
      this.recordError(e, row);
    }

    this.progressListener.setCompletedRow(row.getRowNumber());

    if (Thread.interrupted())
    {
      throw new InterruptedException();
    }

    Thread.yield();
  }

  @Transaction
  private void recordError(EdgeObjectRecordedErrorException e, FeatureRow row)
  {
    JSONObject obj = new JSONObject(e.getObjectJson());

    this.progressListener.recordError(e.getError(), obj.toString(), e.getObjectType(), row.getRowNumber());
    this.getConfiguration().addException(e);
  }

  @Transaction
  public boolean importRowInTrans(FeatureRow row, RowData data)
  {
    boolean imported = false;

    // Refresh the session because it might expire on long imports
    if ( ( this.lastImportSessionRefresh + EdgeObjectImporter.refreshSessionRecordCount ) < row.getRowNumber())
    {
      SessionFacade.renewSession(Session.getCurrentSession().getOid());
      this.lastImportSessionRefresh = row.getRowNumber();
    }

    boolean isNew = false;

    try
    {
      String sourceCode = getValue("edgeSource", row);
      String sourceTypeCode = getValue("edgeSourceType", row);
      String targetCode = getValue("edgeTarget", row);
      String targetTypeCode = getValue("edgeTargetType", row);
      Date startDate = this.configuration.getStartDate();
      Date endDate = this.configuration.getEndDate();
      
      Object childRid = EdgeJsonImporter.getOrFetchRid(goRidCache, sourceCode, sourceTypeCode);
      Object parentRid = EdgeJsonImporter.getOrFetchRid(goRidCache, targetCode, targetTypeCode);
      EdgeJsonImporter.newEdge(childRid, parentRid, configuration.getGraphType(), startDate, endDate);

      imported = true;

      // We must ensure that any problems created during the transaction are
      // logged now instead of when the request returns. As such, if any
      // problems exist immediately throw a ProblemException so that normal
      // exception handling can occur.
      List<ProblemIF> existingProblems = RequestState.getProblemsInCurrentRequest();

      if (existingProblems.size() != 0)
      {
        throw new ProblemException(null, new LinkedList<ProblemIF>(existingProblems));
      }
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
    catch (Throwable t)
    {
      // TODO : Provide some json here maybe?
      buildRecordException(null, isNew, t);
    }

    return imported;
  }
  
  protected String getValue(String attribute, FeatureRow row) {
    String strategy = getStrategy(attribute);
    
    String target;
    if (strategy.toUpperCase().equals("FIXED-TYPE")) {
      target = this.getConfigValue(attribute);
    } else if (strategy.toUpperCase().equals("CODE")) {
      target = this.getConfigValue(attribute);
    } else {
      throw new UnsupportedOperationException(attribute);
    }
    
    if (StringUtils.isEmpty(target)) {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(attribute);
      throw ex;
    }
    
    if (strategy.toUpperCase().equals("FIXED-TYPE")) return target;
    
//    ShapefileFunction function = this.configuration.getFunction(attribute);
//
//    if (function == null)
//    {
//      RequiredMappingException ex = new RequiredMappingException();
//      ex.setAttributeLabel(attribute);
//      throw ex;
//    }
//    Object obj = function.getValue(row);
    
    String result = null;
    
    Object obj = row.getValue(target);

    if (obj != null)
    {
      result = obj.toString();
    }

    if (result == null || result.length() <= 0)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(target);
      throw ex;
    }
    
    return result;
  }
  
  private String getConfigValue(String attribute) {
    if (attribute.equals("edgeSource")) {
      return this.configuration.getEdgeSource();
    } else if (attribute.equals("edgeSourceType")) {
      return this.configuration.getEdgeSourceType();
    } else if (attribute.equals("edgeTarget")) {
      return this.configuration.getEdgeTarget();
    } else if (attribute.equals("edgeTargetType")) {
      return this.configuration.getEdgeTargetType();
    } else {
      throw new UnsupportedOperationException(attribute);
    }
  }
  
  private String getStrategy(String attribute) {
    if (attribute.equals("edgeSource")) {
      return this.configuration.getEdgeSourceStrategy();
    } else if (attribute.equals("edgeSourceType")) {
      return this.configuration.getEdgeSourceTypeStrategy();
    } else if (attribute.equals("edgeTarget")) {
      return this.configuration.getEdgeTargetStrategy();
    } else if (attribute.equals("edgeTargetType")) {
      return this.configuration.getEdgeTargetTypeStrategy();
    } else {
      throw new UnsupportedOperationException(attribute);
    }
  }

  private void buildRecordException(String goJson, boolean isNew, Throwable t)
  {
    JSONObject obj = new JSONObject();

    if (goJson != null)
    {
      obj.put("geoObject", new JSONObject(goJson));
    }

    obj.put("isNew", isNew);

    EdgeObjectRecordedErrorException re = new EdgeObjectRecordedErrorException();
    re.setError(t);
    re.setObjectJson(obj.toString());
    re.setObjectType(ERROR_OBJECT_TYPE);
    throw re;
  }

  protected Object getParentCode(FeatureRow feature, Location location)
  {
    ShapefileFunction function = location.getFunction();
    return function.getValue(feature);
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
