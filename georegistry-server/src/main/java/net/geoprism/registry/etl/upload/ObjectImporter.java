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
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestState;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.AttributeCharacterType;
import net.geoprism.registry.graph.AttributeClassificationType;
import net.geoprism.registry.graph.AttributeDoubleType;
import net.geoprism.registry.graph.AttributeLongType;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.io.IgnoreRowException;
import net.geoprism.registry.io.RequiredMappingException;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.jobs.RowValidationProblem;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.graph.ServerObjectVertex;
import net.geoprism.registry.service.business.ObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.ObjectClassDTO;
import net.geoprism.registry.view.ObjectOverTimeDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public abstract class ObjectImporter<V extends ServerObjectVertex, T extends ObjectClass, D extends ObjectClassDTO> implements ObjectImporterIF
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
        ObjectImporter.this.performValidationTask(this.row);
      }
      else if (action.equals(Action.IMPORT))
      {
        ObjectImporter.this.performImportTask(this.row);
      }
    }

  }

  private static class RowData
  {
    private ObjectOverTimeDTO goJson;

    private boolean           isNew;

    public void setGoJson(ObjectOverTimeDTO goJson)
    {
      this.goJson = goJson;
    }

    public void setNew(boolean isNew)
    {
      this.isNew = isNew;
    }

  }

  // Refresh the user's session every X amount of records
  private static final long                   refreshSessionRecordCount  = GeoregistryProperties.getRefreshSessionRecordCount();

  protected TypedObjectImportConfiguration<T> configuration;

  protected ImportProgressListenerIF          progressListener;

  protected FormatSpecificImporterIF          formatImporter;

  private long                                lastValidateSessionRefresh = 0;

  private long                                lastImportSessionRefresh   = 0;

  private BlockingQueue<Runnable>             blockingQueue;

  private ThreadPoolExecutor                  executor;

  private EventGateway                        gateway;

  public ObjectImporter(TypedObjectImportConfiguration<T> configuration, ImportProgressListenerIF progressListener)
  {
    this.gateway = ServiceFactory.getBean(EventGateway.class);

    this.configuration = configuration;
    this.progressListener = progressListener;

    this.blockingQueue = new LinkedBlockingDeque<Runnable>(50);

    this.executor = new ThreadPoolExecutor(10, 20, 5, TimeUnit.SECONDS, blockingQueue);
    this.executor.prestartAllCoreThreads();
  }

  protected abstract ObjectBusinessServiceIF<V, T, D> getService();

  protected abstract List<RepositoryEvent> buildEvents(V businessObject, boolean isNew);

  public FormatSpecificImporterIF getFormatSpecificImporter()
  {
    return formatImporter;
  }

  public void setFormatSpecificImporter(FormatSpecificImporterIF formatImporter)
  {
    this.formatImporter = formatImporter;
  }

  @Override
  public TypedObjectImportConfiguration<T> getConfiguration()
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
      if ( ( this.lastValidateSessionRefresh + ObjectImporter.refreshSessionRecordCount ) < row.getRowNumber())
      {
        SessionFacade.renewSession(Session.getCurrentSession().getOid());
        this.lastValidateSessionRefresh = row.getRowNumber();
      }

      try
      {
        /*
         * 2. Check for serialization and term problems
         */
        String code = this.getCode(row);

        if (StringUtils.isBlank(code))
        {
          RequiredMappingException ex = new RequiredMappingException();
          ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }

        V entity;

        entity = this.getService().newInstance(this.configuration.getType());

        Map<String, AttributeType> attributes = this.getConfiguration().getType().getAttributeMap();
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
    catch (ObjectRecordedErrorException e)
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
  private void recordError(ObjectRecordedErrorException e, FeatureRow row)
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
    if ( ( this.lastImportSessionRefresh + ObjectImporter.refreshSessionRecordCount ) < row.getRowNumber())
    {
      SessionFacade.renewSession(Session.getCurrentSession().getOid());
      this.lastImportSessionRefresh = row.getRowNumber();
    }

    V businessObject = null;

    boolean isNew = false;

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
        businessObject = this.getService().getByCode(this.configuration.getType(), code);
      }

      if (businessObject == null)
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

        businessObject = this.getService().newInstance(this.configuration.getType());
        businessObject.setCode(code);
      }

      Map<String, AttributeType> attributes = this.configuration.getType().getAttributeMap();
      Set<Entry<String, AttributeType>> entries = attributes.entrySet();

      for (Entry<String, AttributeType> entry : entries)
      {
        String attributeName = entry.getKey();

        ShapefileFunction function = this.configuration.getFunction(attributeName);

        if (function != null)
        {
          Object value = function.getValue(row);

          AttributeType attributeType = entry.getValue();

          if (value != null && !this.isEmptyString(value))
          {
            this.setValue(businessObject, attributeType, attributeName, value, row);
          }
          else if (this.configuration.getCopyBlank())
          {
            this.setValue(businessObject, attributeType, attributeName, null, row);
          }
        }
      }

      businessObject.setValue(DefaultAttribute.DATA_SOURCE.getName(), configuration.getDataSource());

      if (this.progressListener.hasValidationProblems())
      {
        throw new RuntimeException("Did not expect to encounter validation problems during import.");
      }

      data.setGoJson(this.getService().toDTO(businessObject));
      data.setNew(isNew);

      List<RepositoryEvent> events = buildEvents(businessObject, isNew);

      this.gateway.publish(events.stream().map(GenericEventMessage::asEventMessage).toList());

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
      else
      {
        this.progressListener.add(new TypeInfo(TypeClass.BUSINESS_TYPE, this.configuration.getType().getCode()));
      }
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
    catch (Throwable t)
    {
      buildRecordException(this.getService().toDTO(businessObject), isNew, t);
    }

    return imported;
  }

  private void buildRecordException(ObjectOverTimeDTO dto, boolean isNew, Throwable t)
  {
    JsonObject obj = new JsonObject();

    if (dto != null)
    {
      obj.add("geoObject", JsonParser.parseString(ObjectOverTimeDTO.toJson(dto)));
    }

    obj.addProperty("isNew", isNew);

    ObjectRecordedErrorException re = new ObjectRecordedErrorException();
    re.setError(t);
    re.setObjectJson(obj.toString());
    re.setObjectType(this.configuration.getObjectType());

    throw re;
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
    ShapefileFunction function = this.configuration.getFunction(BusinessObject.CODE);

    if (function == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.configuration.getType().getAttribute(BusinessObject.CODE).get().getLocalizedLabel().getValue());
      throw ex;
    }

    Object code = function.getValue(row);

    if (code != null)
    {
      return code.toString();
    }

    return null;
  }

  protected void setClassificationValue(V entity, AttributeType attributeType, String attributeName, Object value)
  {
    if (!this.configuration.isExclusion(attributeName, value.toString()))
    {
      try
      {
        T type = this.configuration.getType();
        MdVertexDAOIF mdBusiness = type.getMdVertexDAO();
        MdAttributeClassificationDAOIF mdAttribute = (MdAttributeClassificationDAOIF) mdBusiness.definesAttribute(attributeName);

        VertexObject classifier = AbstractClassification.findMatchingClassification(value.toString().trim(), mdAttribute);

        if (classifier == null)
        {
          throw new UnknownTermException(value.toString().trim(), attributeType.toDTO());
        }
        else
        {
          if (attributeType.getIsChangeOverTime())
          {
            entity.setValue(attributeName, classifier.getOid(), configuration.getStartDate(), configuration.getEndDate());
          }
          else
          {
            entity.setValue(attributeName, classifier.getOid());
          }
        }
      }
      catch (UnknownTermException e)
      {
        TermValueException ex = new TermValueException();
        ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
        ex.setCode(e.getCode());

        throw e;
      }
    }
  }

  protected void setValue(V entity, AttributeType attributeType, String attributeName, Object value, FeatureRow row)
  {
    if (value != null && attributeType instanceof AttributeClassificationType)
    {
      this.setClassificationValue(entity, attributeType, attributeName, value);
    }
    else
    {
      if (value != null && attributeType instanceof AttributeLongType)
      {
        if (value instanceof String)
        {
          value = Long.valueOf((String) value);
        }
        else if (value instanceof Number)
        {
          value = ( (Number) value ).longValue();
        }
        else
        {
          throw new UnsupportedOperationException();
        }
      }
      else if (value != null && attributeType instanceof AttributeDoubleType)
      {
        if (value instanceof String)
        {
          value = Double.valueOf((String) value);
        }
        else if (value instanceof Number)
        {
          value = ( (Number) value ).doubleValue();
        }
        else
        {
          throw new UnsupportedOperationException();
        }
      }
      else if (value != null && attributeType instanceof AttributeCharacterType)
      {
        value = value.toString();
      }

      if (attributeType.getIsChangeOverTime())
      {
        entity.setValue(attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
      }
      else
      {
        entity.setValue(attributeName, value);
      }
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
