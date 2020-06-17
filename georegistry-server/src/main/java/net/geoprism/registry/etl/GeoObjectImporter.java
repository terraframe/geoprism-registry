/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.etl;

import java.util.ArrayList;
import java.util.Date;
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
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
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
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.RequestState;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.etl.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.ExternalParentReferenceException;
import net.geoprism.registry.geoobject.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.geoobject.GeoObjectPermissionService;
import net.geoprism.registry.geoobject.GeoObjectPermissionServiceIF;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
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
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.query.ServerCodeRestriction;
import net.geoprism.registry.query.ServerExternalIdRestriction;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.query.postgres.CodeRestriction;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.query.postgres.NonUniqueResultException;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.ServerParentTreeNodeOverTime;

public class GeoObjectImporter implements ObjectImporterIF
{
  private static final Logger              logger            = LoggerFactory.getLogger(GeoObjectImporter.class);

  protected static final String            ERROR_OBJECT_TYPE = GeoObjectOverTime.class.getName();

  protected GeoObjectImportConfiguration   configuration;

  protected ServerGeoObjectService         service;

  protected Map<String, ServerGeoObjectIF> parentCache;

  protected static final String            parentConcatToken = "&";

  protected ImportProgressListenerIF       progressListener;

  protected FormatSpecificImporterIF       formatImporter;
  
  private GeoObjectPermissionServiceIF geoObjectPermissionService = new GeoObjectPermissionService();

  public GeoObjectImporter(GeoObjectImportConfiguration configuration, ImportProgressListenerIF progressListener)
  {
    this.configuration = configuration;
    this.progressListener = progressListener;
    this.service = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService());
    this.parentCache = new HashMap<String, ServerGeoObjectIF>();

    final int MAX_ENTRIES = 10000; // The size of our parentCache
    this.parentCache = new LinkedHashMap<String, ServerGeoObjectIF>(MAX_ENTRIES + 1, .75F, true)
    {
      private static final long serialVersionUID = 1L;

      public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest)
      {
        return size() > MAX_ENTRIES;
      }
    };
  }

  protected class GeoObjectParentErrorBuilder
  {
    protected ServerGeoObjectIF parent;

    protected ServerGeoObjectIF serverGO;

    protected GeoObjectParentErrorBuilder()
    {

    }

    public ServerGeoObjectIF getParent()
    {
      return this.parent;
    }

    public ServerGeoObjectIF getServerGO()
    {
      return this.serverGO;
    }

    public void setServerGO(ServerGeoObjectIF serverGo)
    {
      this.serverGO = serverGo;
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
          ServerGeoObjectIF parent = this.getParent();
          // ServerGeoObjectIF serverGo = this.getServerGO();

          List<Location> locations = GeoObjectImporter.this.configuration.getLocations();

          String[] types = new String[locations.size() - 1];

          for (int i = 0; i < locations.size() - 1; ++i)
          {
            Location location = locations.get(i);
            types[i] = location.getType().getCode();
          }

          ServerParentTreeNodeOverTime grandParentsOverTime = parent.getParentsOverTime(null, true);

          ServerHierarchyType hierarchy = GeoObjectImporter.this.configuration.getHierarchy();

          ServerParentTreeNode ptn = grandParentsOverTime.getEntries(hierarchy).get(0);

          ServerParentTreeNode tnParent = new ServerParentTreeNode(parent, hierarchy, GeoObjectImporter.this.configuration.getStartDate());
          tnParent.setEndDate(GeoObjectImporter.this.configuration.getEndDate());

          tnParent.addParent(ptn);
          
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

  @Transaction
  public void validateRow(FeatureRow row)
  {
    try
    {
      // int beforeProbCount =
      // this.progressListener.getValidationProblems().size();

      /*
       * Check permissions
       */
      ServerGeoObjectType type = this.configuration.getType();
      if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
      {
        if (this.configuration.getImportStrategy() == ImportStrategy.NEW_ONLY)
        {
          this.geoObjectPermissionService.enforceCanCreate(Session.getCurrentSession().getUser(), type.getOrganization().getCode(), type.getCode());
        }
        else
        {
          this.geoObjectPermissionService.enforceCanWrite(Session.getCurrentSession().getUser(), type.getOrganization().getCode(), type.getCode());
        }
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
      String geoId = this.getCode(row);

      ServerGeoObjectIF entity;

      if (geoId != null && geoId.length() > 0)
      {
        entity = service.newInstance(this.configuration.getType());
        entity.setCode(geoId);

        try
        {
          entity.setStatus(GeoObjectStatus.ACTIVE, this.configuration.getStartDate(), this.configuration.getEndDate());

          Geometry geometry = (Geometry) this.getFormatSpecificImporter().getGeometry(row);
          LocalizedValue entityName = this.getName(row);

          if (entityName != null && this.hasValue(entityName))
          {
            entity.setDisplayLabel(entityName, this.configuration.getStartDate(), this.configuration.getEndDate());

            if (geometry != null)
            {
              // TODO : We should be able to check the CRS here and throw a
              // specific invalid CRS error if it's not what we expect.
              // For some reason JTS always returns 0 when we call
              // geometry.getSRID().
              if (geometry.isValid())
              {
                entity.setGeometry(geometry, this.configuration.getStartDate(), this.configuration.getEndDate());
              }
              else
              {
                // throw new SridException();
                throw new InvalidGeometryException();
              }
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

                  if (value != null)
                  {
                    AttributeType attributeType = entry.getValue();

                    this.setValue(entity, attributeType, attributeName, value);
                  }
                }
              }
            }

            GeoObjectOverTime go = entity.toGeoObjectOverTime();
            go.toJSON().toString();

            // We must ensure that any problems created during the transaction
            // are
            // logged now instead of when the request returns. As such, if any
            // problems exist immediately throw a ProblemException so that
            // normal
            // exception handling can occur.
            // List<ProblemIF> problems =
            // RequestState.getProblemsInCurrentRequest();
            //
            // List<ProblemIF> problems2 = new LinkedList<ProblemIF>();
            // for (ProblemIF problem : problems)
            // {
            // problems2.add(problem);
            // }
            //
            // if (problems.size() != 0)
            // {
            // throw new ProblemException(null, problems2);
            // }
          }
        }
        finally
        {
          entity.unlock();
        }
      }

      // if (beforeProbCount ==
      // this.progressListener.getValidationProblems().size())
      // {
      // this.progressListener.setImportedRecords(this.progressListener.getImportedRecords()
      // + 1);
      // }
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
    catch (Throwable t)
    {
      RowValidationProblem problem = new RowValidationProblem(t);
      problem.addAffectedRowNumber(this.progressListener.getWorkProgress() + 1);
      problem.setHistoryId(this.configuration.historyId);
      
      this.progressListener.addRowValidationProblem(problem);
    }

    this.progressListener.setWorkProgress(this.progressListener.getWorkProgress() + 1);
  }

  /**
   * Imports a GeoObject based on the given SimpleFeature.
   * 
   * @param feature
   * @throws Exception
   */
  public void importRow(FeatureRow row)
  {
    try
    {
      this.importRowInTrans(row);
    }
    catch (RecordedErrorException e)
    {
      this.recordError(e);
    }

    Thread.yield();
  }

  @Transaction
  private void recordError(RecordedErrorException e)
  {
    JSONObject obj = new JSONObject(e.getObjectJson());

    GeoObjectParentErrorBuilder parentBuilder = e.getParentBuilder();
    if (parentBuilder != null)
    {
      obj.put("parents", parentBuilder.build());
    }

    this.progressListener.recordError(e.getError(), obj.toString(), e.getObjectType(), this.progressListener.getWorkProgress() + 1);
    this.progressListener.setWorkProgress(this.progressListener.getWorkProgress() + 1);
    this.configuration.addException(e);
  }

  @Transaction
  public void importRowInTrans(FeatureRow row)
  {
    GeoObjectOverTime go = null;

    String goJson = null;

    ServerGeoObjectIF serverGo = null;

    ServerGeoObjectIF parent = null;
    
    boolean isNew = false;

    GeoObjectParentErrorBuilder parentBuilder = new GeoObjectParentErrorBuilder();

    try
    {
      String geoId = this.getCode(row);

      if (geoId != null && geoId.length() > 0)
      {
        if (this.configuration.getImportStrategy().equals(ImportStrategy.UPDATE_ONLY) || this.configuration.getImportStrategy().equals(ImportStrategy.NEW_AND_UPDATE))
        {
          serverGo = service.getGeoObjectByCode(geoId, this.configuration.getType());
        }

        if (serverGo == null)
        {
          if (this.configuration.getImportStrategy().equals(ImportStrategy.UPDATE_ONLY))
          {
            DataNotFoundException ex = new DataNotFoundException();
            ex.setDataIdentifier(geoId);
            throw ex;
          }

          isNew = true;

          serverGo = service.newInstance(this.configuration.getType());
          serverGo.setCode(geoId);
        }
        else
        {
          serverGo.lock();
        }

        parentBuilder.setServerGO(serverGo);

        serverGo.setStatus(GeoObjectStatus.ACTIVE, this.configuration.getStartDate(), this.configuration.getEndDate());

        Geometry geometry = (Geometry) this.getFormatSpecificImporter().getGeometry(row);
        LocalizedValue entityName = this.getName(row);

        if (entityName != null && this.hasValue(entityName))
        {
          serverGo.setDisplayLabel(entityName, this.configuration.getStartDate(), this.configuration.getEndDate());

          if (geometry != null)
          {
            // TODO : We should be able to check the CRS here and throw a
            // specific invalid CRS error if it's not what we expect.
            // For some reason JTS always returns 0 when we call
            // geometry.getSRID().
            if (geometry.isValid())
            {
              serverGo.setGeometry(geometry, this.configuration.getStartDate(), this.configuration.getEndDate());
            }
            else
            {
              // throw new SridException();
              throw new InvalidGeometryException();
            }
          }

          if (isNew)
          {
            serverGo.setUid(ServiceFactory.getIdService().getUids(1)[0]);
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

                if (value != null)
                {
                  AttributeType attributeType = entry.getValue();

                  this.setValue(serverGo, attributeType, attributeName, value);
                }
              }
            }
          }
          
          go = serverGo.toGeoObjectOverTime();
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
            throw new RuntimeException("Did not expect to encounter validation problems during import."); // TODO
                                                                                                          // :
                                                                                                          // SmartException?
          }

          serverGo.apply(true);
          
          
          if (this.configuration.isExternalImport())
          {
            ShapefileFunction function = this.configuration.getExternalIdFunction();
            
            Object value = function.getValue(row);
            
            serverGo.createExternalId(this.configuration.getExternalSystem(), (String) value);
          }
          

          if (parent != null)
          {
            parent.addChild(serverGo, this.configuration.getHierarchy(), this.configuration.getStartDate(), this.configuration.getEndDate());
          }
          else if (isNew)
          {
            GeoEntity child = GeoEntity.getByKey(serverGo.getCode());
            GeoEntity root = GeoEntity.getByKey(GeoEntity.ROOT);

            child.addLink(root, this.configuration.getHierarchy().getEntityType());
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

          this.progressListener.setImportedRecords(this.progressListener.getImportedRecords() + 1);
        }
      }
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
    catch (Throwable t)
    {
      JSONObject obj = new JSONObject();

      if (goJson != null)
      {
        obj.put("geoObject", new JSONObject(goJson));
      }
      
      obj.put("isNew", isNew);

      RecordedErrorException re = new RecordedErrorException();
      re.setError(t);
      re.setObjectJson(obj.toString());
      re.setObjectType(ERROR_OBJECT_TYPE);
      re.setParentBuilder(parentBuilder);
      throw re;
    }

    this.progressListener.setWorkProgress(this.progressListener.getWorkProgress() + 1);
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
      ex.setAttributeLabel(this.configuration.getType().getAttribute(GeoObject.CODE).get().getLabel().getValue());
      throw ex;
    }

    Object geoId = function.getValue(row);

    if (geoId != null)
    {
      return geoId.toString();
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

        if (ms.equals(ParentMatchStrategy.CODE))
        {
          query.setRestriction(new ServerCodeRestriction(label.toString()));
        }
        else if (ms.equals(ParentMatchStrategy.EXTERNAL))
        {
          query.setRestriction(new ServerExternalIdRestriction(this.getConfiguration().getExternalSystem(), label.toString()));
        }
        else
        {
          query.setRestriction(new ServerSynonymRestriction(label.toString(), this.configuration.getStartDate(), parent, this.configuration.getHierarchy()));
        }

        try
        {

          ServerGeoObjectIF result = query.getSingleResult();

          if (result != null)
          {
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

              ParentReferenceProblem prp = new ParentReferenceProblem(location.getType().getCode(), label.toString(), parentCode, context);
              prp.addAffectedRowNumber(this.progressListener.getWorkProgress() + 1);
              prp.setHistoryId(this.configuration.historyId);
              
              this.progressListener.addReferenceProblem(prp);
            }

            return null;
          }
        }
        catch (NonUniqueResultException e)
        {
          AmbiguousParentException ex = new AmbiguousParentException();
          ex.setParentLabel(label.toString());
          ex.setContext(context.toString());

          throw ex;
        }
      }
    }

    if (parent != null)
    {
      return this.service.getGeoObjectByCode(parent.getCode(), parent.getType());
    }

    return null;
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
      GeoObjectQuery query = new GeoObjectQuery(location.getType());
      query.setRestriction(new CodeRestriction(code));

      GeoObject result = query.getSingleResult();

      if (result != null)
      {
        return service.getGeoObject(result);
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
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.configuration.getType().getAttribute(GeoObject.DISPLAY_LABEL).get().getLabel().getValue());
      throw ex;
    }

    Object attribute = function.getValue(row);

    if (attribute != null)
    {
      return (LocalizedValue) attribute;
    }

    return null;
  }

  protected void setTermValue(ServerGeoObjectIF entity, AttributeType attributeType, String attributeName, Object value, Date startDate, Date endDate)
  {
    if (!this.configuration.isExclusion(attributeName, value.toString()))
    {
      try
      {
        MdBusinessDAOIF mdBusiness = this.configuration.getType().getMdBusinessDAO();
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
          entity.setValue(attributeName, classifier.getOid(), startDate, endDate);
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

  protected void setValue(ServerGeoObjectIF entity, AttributeType attributeType, String attributeName, Object value)
  {
    if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      entity.setDisplayLabel((LocalizedValue) value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeTermType)
    {
      this.setTermValue(entity, attributeType, attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      if (value instanceof String)
      {
        entity.setValue(attributeName, new Long((String) value), this.configuration.getStartDate(), this.configuration.getEndDate());
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
    else if (attributeType instanceof AttributeFloatType)
    {
      if (value instanceof String)
      {
        entity.setValue(attributeName, new Double((String) value), this.configuration.getStartDate(), this.configuration.getEndDate());
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
      entity.setValue(attributeName, value.toString(), this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else if (attributeType instanceof AttributeBooleanType)
    {
      entity.setValue(attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
    else
    {
      entity.setValue(attributeName, value, this.configuration.getStartDate(), this.configuration.getEndDate());
    }
  }
}
