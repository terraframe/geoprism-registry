package org.commongeoregistry.adapter.android;

import java.util.List;
import java.util.Optional;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

public class ApiExamples
{

  public void tutorialExamples()
  {
    // Instantiate a mobile adapter reference,

    // URL to the
    String commonGeoRegistryURL = "";

    AndroidRegistryClient registryAdapter = null; // = new AndroidRegistryClient();


    // Populate the registry instance with meadata from the registry
    registryAdapter.getMetadataCache().getAllGeoObjectTypes();
//
//    // Create a new and empty instance of a {@link GeoObject} to populate with values by the mobile host.
//    // Pass in the code of the {@link GeoObjectType}/
//    GeoObject newGeoObject = registryAdapter.newGeoObjectInstance("HEALTH_FACILITY");
//
//    // Set a value on an attribute
//    newGeoObject.setValue("numberOfBeds", 100);
//
//    // Set the geometry (using JTS library)
//    Coordinate newCoord = new Coordinate(0,0);
//    Point point = new GeometryFactory().createPoint(newCoord);
//    newGeoObject.setGeometry(point);
//
//    registryAdapter.getLocalCache().cache(newGeoObject);
//
//    // We need to come up with the way that the mobile host application will get the identifier of the location that it wishes
//    // to cache offline. Should it be the cached code?
//    GeoObject someDistrict = registryAdapter.getGeoObject("UID-123456");
//
//
//    // This is how you get the GeoJSON of the GeoObject to pass to the Geospatial Widget.
//    String districtGeoJSON = someDistrict.toJSON().toString();
//
//    // Get a hierarchy of a {@link GeoObject} and their relationships with other {@link GeoObject}s. This will be used
//    // to cache a set of {@link GeoObject}s for offline use.
//    ChildTreeNode childTreeNode = registryAdapter.getChildGeoObjects(someDistrict.getUid(), new String[] {"VILLAGE", "HOUSEHOLD", "HEALTH_FACILITY"}, true);
//
//    // Persist the tree of objects into the local cache.
//    registryAdapter.getLocalCache().cache(childTreeNode);
//
//    // Iterate over the hierarchy
//    for (ChildTreeNode node : childTreeNode.getChildren())
//    {
//      GeoObject childGeoObject = node.getGeoObject();
//
//      // Recursively traverse the hierarchy...
//      node.getChildren();
//    }
//
//    // Fetch a tree of objects from the local cache, such as the households in a village. These will
//    // Need to have been fetched from the CGR and cached when online (see example above).
//    ChildTreeNode houseHoldsInVillages = registryAdapter.getLocalCache().getChildGeoObjects("A Village UID", new String[] {"HOUSEHOLD"}, true);
//
//    // To render a form in the Geospatial widget or the host application, you will need to get the metadata
//    // of the object type in order to know what attributes to render and what their localized values are.
//    // Since we have not defined how we are doing error handling
//    Optional<GeoObjectType> optionGeoObjectType = registryAdapter.getMetadataCache().getGeoObjectType("HEATH_FACILITY");
//
//    if (optionGeoObjectType.isPresent())
//    {
//      GeoObjectType geoObjectType = optionGeoObjectType.get();
//
//      // Get the localized label of the type
//      String typeLabel = geoObjectType.getLabel();
//
//      // Get the localized description of the type.
//      String typeDescription = geoObjectType.getDescription();
//
//      for (AttributeType attributeType : geoObjectType.getAttributeMap().values())
//      {
//        // Get the localized Label
//        String attributeLabel = attributeType.getLabel();
//
//        // Get the localized attribute description
//        String attributeDescription = attributeType.getDescription();
//
//        // For term (select list) attributes
//
//        if (attributeType instanceof AttributeTermType)
//        {
//          AttributeTermType attributeTermType = (AttributeTermType)attributeType;
//
//          // Assuming this is a single dimension list instead of a tree (which the abstraction supports but
//          // we will assume single dimension for now.
//          List<Term> terms = attributeTermType.getTerms();
//
//          for (Term term: terms)
//          {
//            // Label of the option, such as "Maternity Ward"
//            String termLabel = term.getLabel();
//
//            // Description of the option, such as "Maternity Wards focus on..."
//            String termDescription = term.getDescription();
//
//            // This is the computer readable value, such as "MATURNITY_WARD"
//            String value = term.getCode();
//          }
//        }
//
//      }
//    }
//
//    // Topics for discussion:
//
//    // Define exception methodology and exception types
//
//    // Do we want an "isRequired" property on {@link AttributeType}
//
  }
  
  
}
