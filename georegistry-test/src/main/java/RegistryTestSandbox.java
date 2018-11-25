import java.io.File;

import net.geoprism.context.ProjectDataConfiguration;
import net.geoprism.data.LocalEndpoint;
import net.geoprism.data.LocationImporter;
import net.geoprism.data.XMLEndpoint;
import net.geoprism.data.XMLLocationImporter;
import net.geoprism.georegistry.io.HierarchyExporter;

import com.runwaysdk.session.Request;

public class RegistryTestSandbox
{
  public static void main(String[] args) {
//    GeoprismPatcher.main(args);

    
//    generateHierarchyInXML();
    
//    importHierarchies();
  }
  
  @Request
  private static void importHierarchies()
  {
    ProjectDataConfiguration configuration = new ProjectDataConfiguration();

    XMLEndpoint endpoint = new LocalEndpoint(new File("/Users/nathan/git/geoprism-registry/georegistry-server/cache/deployable_countries"));

    LocationImporter importer = new XMLLocationImporter(endpoint);
    importer.loadProjectData(configuration);
  }
  
  @Request
  private static void generateHierarchyInXML()
  {
// /Users/nathan/git/geoprism/geoprism-server/src/main/resources/geoprism/xsd/datatype.xsd
// runwaysdk-server/src/main/resources/com/runwaysdk/resources/xsd/version.xsd
// classpath:com/runwaysdk/resources/xsd/version.xsd    
    String uniFileName = "/Users/nathan/git/geoprism-registry/georegistry-server/cache/deployable_countries/cambodia/xml/2.0/universals/Universal-Cambodia.xml";
    String geoFileName = "/Users/nathan/git/geoprism-registry/georegistry-server/cache/deployable_countries/cambodia/xml/2.0/geoentities/GeoObject-Cambodia.xml";
    String schemaLocation = "classpath:com/runwaysdk/resources/xsd/version.xsd";
    
    
    System.out.println("Test");
    
//    HierarchyExporter.exportHierarchyDefinition(uniFileName, schemaLocation, false);
    
    HierarchyExporter.exportHierarchyInstances(geoFileName,  schemaLocation, false);
   
    
//    Universal universal = Universal.getByKey("Cambodia_Village");
//    
//    universal.clearGeometryType();
//    universal.addGeometryType(GeometryType.POINT);
//    universal.apply();
    
  }
}
