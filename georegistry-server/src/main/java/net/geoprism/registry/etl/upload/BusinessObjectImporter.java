/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.etl.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdAttributeClassificationDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.RequestState;
import com.runwaysdk.session.Session;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.etl.ParentReferenceProblem;
import net.geoprism.registry.etl.RowValidationProblem;
import net.geoprism.registry.etl.TermReferenceProblem;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.io.AmbiguousParentException;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.IgnoreRowException;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.ParentCodeException;
import net.geoprism.registry.io.ParentMatchStrategy;
import net.geoprism.registry.io.RequiredMappingException;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GeoObjectMetadata;
import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class BusinessObjectImporter implements ObjectImporterIF
{
  private static class RowData
  {
    private String                goJson;

    private boolean               isNew;

    private GeoObjectErrorBuilder builder;

    public void setGoJson(String goJson)
    {
      this.goJson = goJson;
    }

    public void setNew(boolean isNew)
    {
      this.isNew = isNew;
    }

    public void setParentBuilder(GeoObjectErrorBuilder parentBuilder)
    {
      this.builder = parentBuilder;
    }

  }

  private static final Logger                 logger                     = LoggerFactory.getLogger(BusinessObjectImporter.class);

  protected static final String               ERROR_OBJECT_TYPE          = GeoObjectOverTime.class.getName();

  protected BusinessObjectImportConfiguration configuration;

  protected Map<String, ServerGeoObjectIF>    cache;

  protected static final String               parentConcatToken          = "&";

  protected ImportProgressListenerIF          progressListener;

  protected FormatSpecificImporterIF          formatImporter;

  private long                                lastValidateSessionRefresh = 0;

  private long                                lastImportSessionRefresh   = 0;

  // Refresh the user's session every X amount of records
  private static final long                   refreshSessionRecordCount  = GeoregistryProperties.getRefreshSessionRecordCount();

  public BusinessObjectImporter(BusinessObjectImportConfiguration configuration, ImportProgressListenerIF progressListener)
  {
    this.configuration = configuration;
    this.progressListener = progressListener;
    this.cache = new HashMap<String, ServerGeoObjectIF>();

    final int MAX_ENTRIES = 10000; // The size of our parentCache
    this.cache = new LinkedHashMap<String, ServerGeoObjectIF>(MAX_ENTRIES + 1, .75F, true)
    {
      private static final long serialVersionUID = 1L;

      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
      {
        return size() > MAX_ENTRIES;
      }
    };
  }

  protected class GeoObjectErrorBuilder
  {
    protected ServerGeoObjectIF geoObject;

    protected BusinessObject    object;

    protected GeoObjectErrorBuilder()
    {

    }

    public ServerGeoObjectIF getGeoObject()
    {
      return geoObject;
    }

    public void setGeoObject(ServerGeoObjectIF geoObject)
    {
      this.geoObject = geoObject;
    }

    public BusinessObject getObject()
    {
      return object;
    }

    public void setObject(BusinessObject object)
    {
      this.object = object;
    }

    public JSONArray build()
    {
      JSONArray parents = new JSONArray();

      try
      {
        if (this.getGeoObject() != null)
        {
          final ServerGeoObjectIF geoObject = this.getGeoObject();
          final List<Location> locations = BusinessObjectImporter.this.getConfiguration().getLocations();
          final ServerHierarchyType hierarchy = BusinessObjectImporter.this.configuration.getHierarchy();

          String[] types = new String[locations.size() - 1];

          for (int i = 0; i < locations.size() - 1; ++i)
          {
            Location location = locations.get(i);
            types[i] = location.getType().getCode();
          }

          ServerParentTreeNode tnParent = new ServerParentTreeNode(geoObject, hierarchy, BusinessObjectImporter.this.getConfiguration().getDate(), BusinessObjectImporter.this.getConfiguration().getDate(), null);

          ServerParentTreeNodeOverTime grandParentsOverTime = geoObject.getParentsOverTime(null, true);

          if (grandParentsOverTime != null && grandParentsOverTime.hasEntries(hierarchy))
          {
            List<ServerParentTreeNode> entries = grandParentsOverTime.getEntries(hierarchy);

            if (entries != null && entries.size() > 0)
            {
              ServerParentTreeNode ptn = grandParentsOverTime.getEntries(hierarchy).get(0);

              tnParent.addParent(ptn);
            }
          }

          ServerParentTreeNodeOverTime parentsOverTime = new ServerParentTreeNodeOverTime(geoObject.getType());
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

  @Override
  public BusinessObjectImportConfiguration getConfiguration()
  {
    return this.configuration;
  }

  @Transaction
  public void validateRow(FeatureRow row) throws InterruptedException
  {
    try
    {
      // Refresh the session because it might expire on long imports
      final long curWorkProgress = this.progressListener.getWorkProgress();
      if ( ( this.lastValidateSessionRefresh + BusinessObjectImporter.refreshSessionRecordCount ) < curWorkProgress)
      {
        SessionFacade.renewSession(Session.getCurrentSession().getOid());
        this.lastValidateSessionRefresh = curWorkProgress;
      }

      try
      {
        if (this.configuration.getHierarchy() != null && this.configuration.getLocations().size() > 0)
        {
          this.getGeoObject(row);
        }

        /*
         * 2. Check for serialization and term problems
         */
        String code = this.getCode(row);

        if (code == null || code.length() <= 0)
        {
          RequiredMappingException ex = new RequiredMappingException();
          ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }

        BusinessObject entity;

        entity = BusinessObject.newInstance(this.configuration.getType());

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

                this.setValue(entity, attributeType, attributeName, value);
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
        problem.addAffectedRowNumber(curWorkProgress + 1);
        problem.setHistoryId(this.configuration.historyId);

        this.progressListener.addRowValidationProblem(problem);
      }

      this.progressListener.setWorkProgress(curWorkProgress + 1);

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

  /**
   * Imports a GeoObject based on the given SimpleFeature.
   * 
   * @param feature
   * @throws InterruptedException
   * @throws Exception
   */
  public void importRow(FeatureRow row) throws InterruptedException
  {
    RowData data = new RowData();

    try
    {
      try
      {
        this.importRowInTrans(row, data);
      }
      catch (DuplicateDataException e)
      {
        buildRecordException(data.goJson, data.isNew, data.builder, e);
      }
    }
    catch (BusinessObjectRecordedErrorException e)
    {
      this.recordError(e);
    }

    if (Thread.interrupted())
    {
      throw new InterruptedException();
    }

    Thread.yield();
  }

  @Transaction
  private void recordError(BusinessObjectRecordedErrorException e)
  {
    JSONObject obj = new JSONObject(e.getObjectJson());

    GeoObjectErrorBuilder parentBuilder = e.getBuilder();
    if (parentBuilder != null)
    {
      obj.put("parents", parentBuilder.build());
    }

    this.progressListener.recordError(e.getError(), obj.toString(), e.getObjectType(), this.progressListener.getRawWorkProgress() + 1);
    this.progressListener.setWorkProgress(this.progressListener.getRawWorkProgress() + 1);
    this.progressListener.setImportedRecords(this.progressListener.getRawImportedRecords());
    this.getConfiguration().addException(e);
  }

  @Transaction
  public void importRowInTrans(FeatureRow row, RowData data)
  {
    // Refresh the session because it might expire on long imports
    final long curWorkProgress = this.progressListener.getWorkProgress();
    if ( ( this.lastImportSessionRefresh + BusinessObjectImporter.refreshSessionRecordCount ) < curWorkProgress)
    {
      SessionFacade.renewSession(Session.getCurrentSession().getOid());
      this.lastImportSessionRefresh = curWorkProgress;
    }

    BusinessObject businessObject = null;

    ServerGeoObjectIF geoObject = null;

    boolean isNew = false;

    GeoObjectErrorBuilder builder = new GeoObjectErrorBuilder();

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
        businessObject = BusinessObject.getByCode(this.configuration.getType(), code);
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

        businessObject = BusinessObject.newInstance(this.configuration.getType());
        businessObject.setCode(code);
      }

      builder.setObject(businessObject);

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
            this.setValue(businessObject, attributeType, attributeName, value);
          }
          else if (this.configuration.getCopyBlank())
          {
            this.setValue(businessObject, attributeType, attributeName, null);
          }
        }

        /*
         * Try to get the parent and ensure that this row is not ignored. The
         * getParent method will throw a IgnoreRowException if the parent is
         * configured to be ignored.
         */
        if (this.configuration.getHierarchy() != null && this.configuration.getLocations().size() > 0)
        {
          geoObject = this.getGeoObject(row);
        }
        builder.setGeoObject(geoObject);

        if (this.progressListener.hasValidationProblems())
        {
          throw new RuntimeException("Did not expect to encounter validation problems during import.");
        }

        data.setGoJson(businessObject.toJSON().toString());
        data.setNew(isNew);
        data.setParentBuilder(builder);

        businessObject.setGeoObject(geoObject);
        businessObject.apply();
      }

      // We must ensure that any problems created during the transaction are
      // logged now instead of when the request returns. As such, if any
      // problems exist immediately throw a ProblemException so that normal
      // exception handling can occur.
      List<ProblemIF> existingProblems = RequestState.getProblemsInCurrentRequest();

      if (existingProblems.size() != 0)
      {
        throw new ProblemException(null, new LinkedList<ProblemIF>(existingProblems));
      }

      this.progressListener.setImportedRecords(this.progressListener.getImportedRecords() + 1);
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
    catch (Throwable t)
    {
      buildRecordException(businessObject.toJSON().toString(), isNew, builder, t);
    }

    this.progressListener.setWorkProgress(curWorkProgress + 1);
  }

  private void buildRecordException(String goJson, boolean isNew, GeoObjectErrorBuilder parentBuilder, Throwable t)
  {
    JSONObject obj = new JSONObject();

    if (goJson != null)
    {
      obj.put("geoObject", new JSONObject(goJson));
    }

    obj.put("isNew", isNew);

    BusinessObjectRecordedErrorException re = new BusinessObjectRecordedErrorException();
    re.setError(t);
    re.setObjectJson(obj.toString());
    re.setObjectType(ERROR_OBJECT_TYPE);
    re.setBuilder(parentBuilder);
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
      ex.setAttributeLabel(this.configuration.getType().getAttribute(BusinessObject.CODE).getLabel().getValue());
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
  private ServerGeoObjectIF getGeoObject(FeatureRow feature)
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
        if (this.cache.containsKey(parentChainKey))
        {
          parent = this.cache.get(parentChainKey);

          JSONObject element = new JSONObject();
          element.put("label", label.toString());
          element.put("type", location.getType().getLabel().getValue());

          context.put(element);

          continue;
        }

        final ParentMatchStrategy ms = location.getMatchStrategy();

        // Search
        ServerGeoObjectQuery query = new ServerGeoObjectService().createQuery(location.getType(), this.configuration.getDate());
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
          query.setRestriction(new ServerSynonymRestriction(label.toString(), this.configuration.getDate(), parent, location.getHierarchy()));
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

          this.cache.put(parentChainKey, parent);
        }
        else
        {
          // if (context.length() == 0)
          // {
          // GeoObject root = this.configuration.getRoot();
          //
          // if (root != null)
          // {
          // JSONObject element = new JSONObject();
          // element.put("label", root.getLocalizedDisplayLabel());
          // element.put("type", root.getType().getLabel().getValue());
          //
          // context.put(element);
          // }
          // }

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
            prp.addAffectedRowNumber(this.progressListener.getWorkProgress() + 1);
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

  protected void setTermValue(BusinessObject entity, AttributeType attributeType, String attributeName, Object value)
  {
    if (!this.configuration.isExclusion(attributeName, value.toString()))
    {
      try
      {
        BusinessType type = this.configuration.getType();
        MdVertexDAOIF mdBusiness = type.getMdVertexDAO();
        MdAttributeTermDAOIF mdAttribute = (MdAttributeTermDAOIF) mdBusiness.definesAttribute(attributeName);

        Classifier classifier = Classifier.findMatchingTerm(value.toString().trim(), mdAttribute);

        if (classifier == null)
        {
          Term rootTerm = ( (AttributeTermType) attributeType ).getRootTerm();

          TermReferenceProblem trp = new TermReferenceProblem(value.toString(), rootTerm.getCode(), mdAttribute.getOid(), attributeName, attributeType.getLabel().getValue());
          trp.addAffectedRowNumber(this.progressListener.getWorkProgress() + 1);
          trp.setHistoryId(this.configuration.getHistoryId());

          this.progressListener.addReferenceProblem(trp);
        }
        else
        {
          entity.setValue(attributeName, classifier.getOid());
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

  protected void setClassificationValue(BusinessObject entity, AttributeType attributeType, String attributeName, Object value)
  {
    if (!this.configuration.isExclusion(attributeName, value.toString()))
    {
      try
      {
        BusinessType type = this.configuration.getType();
        MdVertexDAOIF mdBusiness = type.getMdVertexDAO();
        MdAttributeClassificationDAOIF mdAttribute = (MdAttributeClassificationDAOIF) mdBusiness.definesAttribute(attributeName);

        VertexObject classifier = AbstractClassification.findMatchingClassification(value.toString().trim(), mdAttribute);

        if (classifier == null)
        {
          throw new UnknownTermException(value.toString().trim(), attributeType);
        }
        else
        {
          entity.setValue(attributeName, classifier.getOid());
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

  protected void setValue(BusinessObject entity, AttributeType attributeType, String attributeName, Object value)
  {
    if (attributeType instanceof AttributeClassificationType)
    {
      if (value != null)
      {
        this.setClassificationValue(entity, attributeType, attributeName, value);
      }
      else
      {
        entity.setValue(attributeName, null);
      }
    }
    else if (attributeType instanceof AttributeTermType)
    {
      if (value != null)
      {
        this.setTermValue(entity, attributeType, attributeName, value);
      }
      else
      {
        entity.setValue(attributeName, null);
      }
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      if (value == null)
      {
        entity.setValue(attributeName, null);
      }
      else if (value instanceof String)
      {
        entity.setValue(attributeName, new Long((String) value));
      }
      else if (value instanceof Number)
      {
        entity.setValue(attributeName, ( (Number) value ).longValue());
      }
      else
      {
        throw new UnsupportedOperationException();
      }
    }
    else if (attributeType instanceof AttributeFloatType)
    {
      if (value == null)
      {
        entity.setValue(attributeName, null);
      }
      else if (value instanceof String)
      {
        entity.setValue(attributeName, new Double((String) value));
      }
      else if (value instanceof Number)
      {
        entity.setValue(attributeName, ( (Number) value ).doubleValue());
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
        entity.setValue(attributeName, null);
      }
      else
      {
        entity.setValue(attributeName, value.toString());
      }
    }
    else if (attributeType instanceof AttributeBooleanType)
    {
      entity.setValue(attributeName, value);
    }
    else
    {
      entity.setValue(attributeName, value);
    }
  }
}
