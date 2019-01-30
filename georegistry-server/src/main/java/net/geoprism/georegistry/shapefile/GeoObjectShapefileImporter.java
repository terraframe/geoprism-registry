/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Runway SDK(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.georegistry.shapefile;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestState;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.GISImportLoggerIF;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.data.importer.SimpleFeatureRow;
import net.geoprism.data.importer.TaskObservable;
import net.geoprism.georegistry.GeoObjectQuery;
import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.io.Location;
import net.geoprism.georegistry.io.SynonymRestriction;
import net.geoprism.georegistry.io.TermProblem;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.io.RequiredMappingException;

/**
 * Class responsible for importing GeoObject definitions from a shapefile.
 * 
 * @author Justin Smethie
 */
public class GeoObjectShapefileImporter extends TaskObservable
{
  /**
   * URL of the file being imported
   */
  private URL                    url;

  private GeoObjectConfiguration config;

  /**
   * Map between a feature oid and its geo entity
   */
  private Map<String, String>    entityIdMap;

  /**
   * @param url
   *          URL of the shapefile
   */
  public GeoObjectShapefileImporter(URL url, GeoObjectConfiguration config)
  {
    super();

    this.url = url;
    this.config = config;
    this.entityIdMap = new HashMap<String, String>();
  }

  public GeoObjectShapefileImporter(File file, GeoObjectConfiguration config) throws MalformedURLException
  {
    this(file.toURI().toURL(), config);
  }

  /**
   * @return Map between Shapefile Feature OID and the imported Entity oid.
   */
  public Map<String, String> getEntityIdMap()
  {
    return entityIdMap;
  }

  @Request
  public void run(GISImportLoggerIF logger) throws InvocationTargetException
  {
    try
    {

      try
      {
        this.fireStart();

        this.createEntities(logger);

        // Rebuild the all paths table
        // GeoObject.getStrategy().reinitialize(LocatedIn.CLASS);
      }
      finally
      {
        logger.close();
      }
    }
    catch (RuntimeException e)
    {
      this.fireTaskDone(false);

      throw e;
    }
    catch (Exception e)
    {
      this.fireTaskDone(false);

      throw new InvocationTargetException(e);
    }
  }

  /**
   * Imports the entities from the shapefile
   * 
   * @param writer
   *          Log file writer
   * @throws InvocationTargetException
   */
  @Transaction
  private void createEntities(GISImportLoggerIF logger) throws InvocationTargetException
  {
    try
    {
      ShapefileDataStore store = new ShapefileDataStore(url);

      try
      {
        String[] typeNames = store.getTypeNames();

        if (typeNames.length > 0)
        {
          String typeName = typeNames[0];

          FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(typeName);

          // Display the geo entity information about each row
          FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

          FeatureIterator<SimpleFeature> iterator = collection.features();

          this.fireStartTask(LocalizationFacade.getFromBundles("IMPORT_ENTITIES"), collection.size());

          try
          {
            while (iterator.hasNext())
            {
              SimpleFeature feature = iterator.next();

              importEntity(feature);

              this.fireTaskProgress(1);
            }
          }
          finally
          {
            iterator.close();
          }
        }
      }
      finally
      {
        store.dispose();
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new InvocationTargetException(e);
    }
  }

  /**
   * Imports a GeoObject based on the given SimpleFeature. If a matching
   * GeoObject already exists then it is simply updated.
   * 
   * @param feature
   * @throws Exception
   */
  private void importEntity(SimpleFeature feature) throws Exception
  {
    FeatureRow row = new SimpleFeatureRow(feature);
    String geoId = this.getCode(row);

    GeoObject entity;
    boolean isNew = false;

    if (geoId != null && geoId.length() > 0)
    {
      try
      {
        // try an update
        isNew = false;
        entity = ServiceFactory.getUtilities().getGeoObjectByCode(geoId, this.config.getType().getCode());
      }
      catch (DataNotFoundException e)
      {
        // create a new entity
        isNew = true;
        entity = ServiceFactory.getAdapter().newGeoObjectInstance(this.config.getType().getCode());
      }
    }
    else
    {
      // create a new entity
      isNew = true;
      entity = ServiceFactory.getAdapter().newGeoObjectInstance(this.config.getType().getCode());
    }

    Geometry geometry = (Geometry) feature.getDefaultGeometry();
    String entityName = this.getName(row);

    if (entityName != null)
    {
      entity.setWKTGeometry(geometry.toText());

      if (isNew)
      {
        entity.setUid(ServiceFactory.getIdService().getUids(1)[0]);
      }

      Map<String, org.commongeoregistry.adapter.metadata.AttributeType> attributes = this.config.getType().getAttributeMap();
      Set<Entry<String, org.commongeoregistry.adapter.metadata.AttributeType>> entries = attributes.entrySet();

      for (Entry<String, org.commongeoregistry.adapter.metadata.AttributeType> entry : entries)
      {
        String attributeName = entry.getKey();

        ShapefileFunction function = this.config.getFunction(attributeName);

        if (function != null)
        {
          Object value = function.getValue(row);

          if (value != null)
          {
            org.commongeoregistry.adapter.metadata.AttributeType attributeType = entry.getValue();

            this.setValue(entity, attributeType, attributeName, value);
          }
        }
      }

      try
      {
        ServiceFactory.getUtilities().applyGeoObject(entity, isNew);
      }
      catch (RuntimeException e)
      {
        throw e;
      }
      catch (Exception e)
      {
        throw new RuntimeException(e);
      }

      if (isNew && this.config.getHierarchy() != null)
      {
        GeoObject parent = this.getParent(row);

        ServiceFactory.getRegistryService().addChildInTransaction(parent.getUid(), parent.getType().getCode(), entity.getUid(), entity.getType().getCode(), this.config.getHierarchy());
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

      // The entity was succesfully applied without any problems or exceptions
      this.entityIdMap.put(feature.getID(), entity.getUid());
    }
  }

  private void setValue(GeoObject entity, org.commongeoregistry.adapter.metadata.AttributeType attributeType, String attributeName, Object value)
  {
    if (attributeType instanceof AttributeTermType)
    {
      if (this.config.isExclusion(attributeName, value.toString()))
      {
        MdBusinessDAOIF mdBusiness = this.config.getMdBusiness();
        MdAttributeTermDAOIF mdAttribute = (MdAttributeTermDAOIF) mdBusiness.definesAttribute(attributeName);

        Classifier classifier = Classifier.findMatchingTerm(value.toString().trim(), mdAttribute);

        if (classifier == null)
        {
          this.config.addProblem(new TermProblem(value.toString(), attributeName, attributeType.getLocalizedLabel()));
        }
        else
        {
          List<Term> terms = ( (AttributeTermType) attributeType ).getTerms();

          for (Term term : terms)
          {
            if (term.getCode().equals(classifier.getClassifierId()))
            {
              entity.setValue(attributeName, term);
            }
          }
        }
      }
    }
    else
    {
      entity.setValue(attributeName, value);
    }
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
  private GeoObject getParent(FeatureRow feature)
  {
    List<Location> locations = this.config.getLocations();

    GeoObject parent = null;

    for (Location location : locations)
    {
      BasicColumnFunction function = location.getFunction();
      String label = (String) function.getValue(feature);

      if (label != null)
      {
        // Search
        GeoObjectQuery query = new GeoObjectQuery(location.getType(), location.getUniversal());
        query.setRestriction(new SynonymRestriction(label));

        GeoObject result = query.getSingleResult();

        if (result != null)
        {
          parent = result;
        }
        else
        {
          // throw new LocationProblem(label, context, parent, universal);
        }
      }
    }

    return parent;
  }

  /**
   * @param feature
   *          Shapefile feature
   * 
   * @return The geoId as defined by the 'oid' attribute on the feature. If the
   *         geoId is null then a blank geoId is returned.
   */
  private String getCode(FeatureRow feature)
  {
    ShapefileFunction function = this.config.getFunction(GeoObject.CODE);

    if (function == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.config.getType().getAttribute(GeoObject.CODE).get().getLocalizedLabel());
      throw ex;
    }

    Object geoId = function.getValue(feature);

    if (geoId != null)
    {
      return geoId.toString();
    }

    return "";
  }

  /**
   * @param feature
   * @return The entityName as defined by the 'name' attribute of the feature
   */
  private String getName(FeatureRow feature)
  {
    ShapefileFunction function = this.config.getFunction(GeoObject.LOCALIZED_DISPLAY_LABEL);

    if (function == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.config.getType().getAttribute(GeoObject.LOCALIZED_DISPLAY_LABEL).get().getLocalizedLabel());
      throw ex;
    }

    Object attribute = function.getValue(feature);

    if (attribute != null)
    {
      return attribute.toString();
    }

    return null;
  }
}
