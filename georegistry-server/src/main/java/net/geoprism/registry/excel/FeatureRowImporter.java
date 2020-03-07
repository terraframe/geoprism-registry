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
package net.geoprism.registry.excel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.io.AmbiguousParentException;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.IgnoreRowException;
import net.geoprism.registry.io.InvalidGeometryException;
import net.geoprism.registry.io.Location;
import net.geoprism.registry.io.LocationBuilder;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.PostalCodeLocationException;
import net.geoprism.registry.io.RequiredMappingException;
import net.geoprism.registry.io.TermProblem;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.query.ServerGeoObjectQuery;
import net.geoprism.registry.query.ServerSynonymRestriction;
import net.geoprism.registry.query.postgres.CodeRestriction;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.query.postgres.NonUniqueResultException;
import net.geoprism.registry.service.ServerGeoObjectService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.GeoObjectLocationProblem;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.session.RequestState;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Geometry;

public abstract class FeatureRowImporter
{
  protected GeoObjectConfiguration configuration;

  protected ServerGeoObjectService service;
  
  protected Map<String, ServerGeoObjectIF> parentCache;
  
  protected static final String parentConcatToken = "&";

  public FeatureRowImporter(GeoObjectConfiguration configuration)
  {
    this.configuration = configuration;
    this.service = new ServerGeoObjectService();
    this.parentCache = new HashMap<String, ServerGeoObjectIF>();
    
    final int MAX_ENTRIES = 10000; // The size of our parentCache
    this.parentCache = new LinkedHashMap<String, ServerGeoObjectIF>(MAX_ENTRIES+1, .75F, true) {
      private static final long serialVersionUID = 1L;
        public boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest) {
            return size() > MAX_ENTRIES;
        }
    };
  }

  protected abstract Geometry getGeometry(FeatureRow row);

  protected abstract void setValue(ServerGeoObjectIF entity, AttributeType attributeType, String attributeName, Object value);

  public GeoObjectConfiguration getConfiguration()
  {
    return configuration;
  }

  /**
   * Imports a GeoObject based on the given SimpleFeature. If a matching
   * GeoObject already exists then it is simply updated.
   * 
   * @param feature
   * @throws Exception
   */
  public void create(FeatureRow row)
  {
    try
    {
      ServerGeoObjectIF parent = null;

      /*
       * First, try to get the parent and ensure that this row is not ignored.
       * The getParent method will throw a IgnoreRowException if the parent is
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

      String geoId = this.getCode(row);

      ServerGeoObjectIF entity;

      boolean isNew = false;

      if (geoId != null && geoId.length() > 0)
      {
        entity = service.getGeoObjectByCode(geoId, this.configuration.getType());

        if (entity == null)
        {
          isNew = true;

          entity = service.newInstance(this.configuration.getType());
          entity.setCode(geoId);
        }
        else
        {
          entity.lock();
        }

        entity.setStatus(GeoObjectStatus.ACTIVE, this.configuration.getStartDate(), this.configuration.getEndDate());

        Geometry geometry = (Geometry) this.getGeometry(row);
        LocalizedValue entityName = this.getName(row);

        if (entityName != null && this.hasValue(entityName))
        {
          entity.setDisplayLabel(entityName, this.configuration.getStartDate(), this.configuration.getEndDate());

          if (geometry != null)
          {
            // TODO : We should be able to check the CRS here and throw a specific invalid CRS error if it's not what we expect.
            // For some reason JTS always returns 0 when we call geometry.getSRID().
            if (geometry.isValid())
            {
              entity.setGeometry(geometry, this.configuration.getStartDate(), this.configuration.getEndDate());
            }
            else
            {
//              throw new SridException();
              throw new InvalidGeometryException();
            }
          }

          if (isNew)
          {
            entity.setUid(ServiceFactory.getIdService().getUids(1)[0]);
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

          entity.apply(true);

          if (parent != null)
          {
            parent.addChild(entity, this.configuration.getHierarchy(), this.configuration.getStartDate(), this.configuration.getEndDate());
          }
          else if (isNew && !this.configuration.hasProblems())
// Heads up: Clean up
//          else if (isNew && !this.configuration.hasProblems() && !this.configuration.getType().isLeaf())
          {
            GeoEntity child = GeoEntity.getByKey(entity.getCode());
            GeoEntity root = GeoEntity.getByKey(GeoEntity.ROOT);

            child.addLink(root, this.configuration.getHierarchy().getEntityType());
          }

          // We must ensure that any problems created during the transaction are
          // logged now instead of when the request returns. As such, if any
          // problems exist immediately throw a ProblemException so that normal
          // exception handling can occur.
          List<ProblemIF> problems = RequestState.getProblemsInCurrentRequest();

          if (problems.size() != 0)
          {
            throw new ProblemException(null, problems);
          }
        }
      }
    }
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
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

    JsonArray context = new JsonArray();
    
    ArrayList<String> parentKeyBuilder = new ArrayList<String>();

    for (Location location : locations)
    {
      Object label = getParentCode(feature, location);

      if (label != null)
      {
        String key = parent != null ? parent.getCode() + "-" + label : label.toString();
        
        parentKeyBuilder.add(label.toString());

        if (this.configuration.isExclusion(GeoObjectConfiguration.PARENT_EXCLUSION, key))
        {
          throw new IgnoreRowException();
        }
        
        // Check the parent cache
        String parentChainKey = StringUtils.join(parentKeyBuilder, parentConcatToken);
        if (this.parentCache.containsKey(parentChainKey))
        {
          parent = this.parentCache.get(parentChainKey);
          
          JsonObject element = new JsonObject();
          element.addProperty("label", label.toString());
          element.addProperty("type", location.getType().getLabel().getValue());

          context.add(element);
          
          continue;
        }

        // Search
        ServerGeoObjectQuery query = this.service.createQuery(location.getType(), this.configuration.getStartDate());
        query.setRestriction(new ServerSynonymRestriction(label.toString(), this.configuration.getStartDate(), parent, this.configuration.getHierarchy()));

        try
        {

          ServerGeoObjectIF result = query.getSingleResult();

          if (result != null)
          {
            parent = result;

            JsonObject element = new JsonObject();
            element.addProperty("label", label.toString());
            element.addProperty("type", location.getType().getLabel().getValue());

            context.add(element);
            
            this.parentCache.put(parentChainKey, parent);
          }
          else
          {
            if (context.size() == 0)
            {
              GeoObject root = this.configuration.getRoot();

              if (root != null)
              {
                JsonObject element = new JsonObject();
                element.addProperty("label", root.getLocalizedDisplayLabel());
                element.addProperty("type", root.getType().getLabel().getValue());

                context.add(element);
              }
            }

            this.configuration.addProblem(new GeoObjectLocationProblem(location.getType(), label.toString(), parent, context));

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

          this.configuration.addProblem(new TermProblem(value.toString(), rootTerm.getCode(), mdAttribute.getOid(), attributeName, attributeType.getLabel().getValue()));
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
}
