package net.geoprism.georegistry.excel;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.jaitools.jts.CoordinateSequence2D;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.ProblemException;
import com.runwaysdk.ProblemIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.session.RequestState;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.FeatureRow;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.georegistry.GeoObjectQuery;
import net.geoprism.georegistry.io.GeoObjectConfiguration;
import net.geoprism.georegistry.io.IgnoreRowException;
import net.geoprism.georegistry.io.Location;
import net.geoprism.georegistry.io.SynonymRestriction;
import net.geoprism.georegistry.io.TermProblem;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.georegistry.shapefile.GeoObjectLocationProblem;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.io.RequiredMappingException;

public class GeoObjectConverter
{
  private GeoObjectConfiguration configuration;

  private GeometryFactory        factory;

  public GeoObjectConverter(GeoObjectConfiguration configuration)
  {
    this.configuration = configuration;
    this.factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED), 4326);
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
      GeoObject parent = null;

      /*
       * First, try to get the parent and ensure that this row is not ignored.
       * The getParent method will throw a IgnoreRowException if the parent is
       * configured to be ignored.
       */
      if (this.configuration.getHierarchy() != null)
      {
        parent = this.getParent(row);
      }

      String geoId = this.getCode(row);

      GeoObject entity;
      boolean isNew = false;

      if (geoId != null && geoId.length() > 0)
      {
        try
        {
          // try an update
          isNew = false;
          entity = ServiceFactory.getUtilities().getGeoObjectByCode(geoId, this.configuration.getType().getCode());
        }
        catch (DataNotFoundException e)
        {
          // create a new entity
          isNew = true;
          entity = ServiceFactory.getAdapter().newGeoObjectInstance(this.configuration.getType().getCode());
        }
      }
      else
      {
        // create a new entity
        isNew = true;
        entity = ServiceFactory.getAdapter().newGeoObjectInstance(this.configuration.getType().getCode());
      }

      Geometry geometry = (Geometry) this.getGeometry(row);
      String entityName = this.getName(row);

      if (entityName != null)
      {
        entity.setWKTGeometry(geometry.toText());

        if (isNew)
        {
          entity.setUid(ServiceFactory.getIdService().getUids(1)[0]);
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

            if (value != null)
            {
              AttributeType attributeType = entry.getValue();

              this.setValue(entity, attributeType, attributeName, value);
            }
          }
        }

        ServiceFactory.getUtilities().applyGeoObject(entity, isNew);

        if (parent != null)
        {
          ServiceFactory.getRegistryService().addChildInTransaction(parent.getUid(), parent.getType().getCode(), entity.getUid(), entity.getType().getCode(), this.configuration.getHierarchy().getCode());
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
    catch (IgnoreRowException e)
    {
      // Do nothing
    }
  }

  private void setValue(GeoObject entity, org.commongeoregistry.adapter.metadata.AttributeType attributeType, String attributeName, Object value)
  {
    if (attributeType instanceof AttributeTermType)
    {
      if (!this.configuration.isExclusion(attributeName, value.toString()))
      {
        MdBusinessDAOIF mdBusiness = this.configuration.getMdBusiness();
        MdAttributeTermDAOIF mdAttribute = (MdAttributeTermDAOIF) mdBusiness.definesAttribute(attributeName);

        Classifier classifier = Classifier.findMatchingTerm(value.toString().trim(), mdAttribute);

        if (classifier == null)
        {
          Classifier root = Classifier.findClassifierRoot(mdAttribute);

          this.configuration.addProblem(new TermProblem(value.toString(), mdAttribute.getOid(), root.getOid(), attributeName, attributeType.getLocalizedLabel()));
        }
        else
        {
          entity.setValue(attributeName, classifier.getClassifierId());
        }
      }
    }
    else if (attributeType instanceof AttributeIntegerType)
    {
      entity.setValue(attributeName, new Integer((String) value));
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

  private Geometry getGeometry(FeatureRow row)
  {
    ShapefileFunction latitudeFunction = this.configuration.getFunction(GeoObjectConfiguration.LATITUDE);
    ShapefileFunction longitudeFunction = this.configuration.getFunction(GeoObjectConfiguration.LONGITUDE);

    if (latitudeFunction != null && longitudeFunction != null)
    {
      Object latitude = latitudeFunction.getValue(row);
      Object longitude = longitudeFunction.getValue(row);

      return new Point(new CoordinateSequence2D(new Double(latitude.toString()), new Double(longitude.toString())), factory);
    }
    else if (latitudeFunction == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(LocalizationFacade.getFromBundles(GeoObjectConfiguration.LATITUDE_KEY));
      throw ex;
    }
    else if (longitudeFunction == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(LocalizationFacade.getFromBundles(GeoObjectConfiguration.LONGITUDE_KEY));
      throw ex;
    }

    return null;
  }

  /**
   * @param feature
   *          Shapefile feature
   * 
   * @return The geoId as defined by the 'oid' attribute on the feature. If the
   *         geoId is null then a blank geoId is returned.
   */
  private String getCode(FeatureRow row)
  {
    ShapefileFunction function = this.configuration.getFunction(GeoObject.CODE);

    if (function == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.configuration.getType().getAttribute(GeoObject.CODE).get().getLocalizedLabel());
      throw ex;
    }

    Object geoId = function.getValue(row);

    if (geoId != null)
    {
      return geoId.toString();
    }

    return "";
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
    List<Location> locations = this.configuration.getLocations();

    GeoObject parent = null;

    JsonArray context = new JsonArray();

    for (Location location : locations)
    {
      BasicColumnFunction function = location.getFunction();
      String label = (String) function.getValue(feature);

      if (label != null)
      {
        String key = parent != null ? parent.getCode() + "-" + label : label;

        if (this.configuration.isExclusion(GeoObjectConfiguration.PARENT_EXCLUSION, key))
        {
          throw new IgnoreRowException();
        }

        // Search
        GeoObjectQuery query = new GeoObjectQuery(location.getType(), location.getUniversal());
        query.setRestriction(new SynonymRestriction(label, parent, this.configuration.getHierarchyRelationship()));

        GeoObject result = query.getSingleResult();

        if (result != null)
        {
          parent = result;

          JsonObject element = new JsonObject();
          element.addProperty("label", label);
          element.addProperty("type", location.getType().getLocalizedLabel());

          context.add(element);
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
              element.addProperty("type", root.getType().getLocalizedLabel());

              context.add(element);
            }
          }

          this.configuration.addProblem(new GeoObjectLocationProblem(location.getType(), label, parent, context));

          return null;
        }
      }
    }

    return parent;
  }

  /**
   * @param feature
   * @return The entityName as defined by the 'name' attribute of the feature
   */
  private String getName(FeatureRow row)
  {
    ShapefileFunction function = this.configuration.getFunction(GeoObject.LOCALIZED_DISPLAY_LABEL);

    if (function == null)
    {
      RequiredMappingException ex = new RequiredMappingException();
      ex.setAttributeLabel(this.configuration.getType().getAttribute(GeoObject.LOCALIZED_DISPLAY_LABEL).get().getLocalizedLabel());
      throw ex;
    }

    Object attribute = function.getValue(row);

    if (attribute != null)
    {
      return attribute.toString();
    }

    return null;
  }
}
