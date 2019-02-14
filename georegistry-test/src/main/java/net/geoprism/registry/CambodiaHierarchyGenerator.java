package net.geoprism.registry;
import java.util.LinkedList;
import java.util.List;

import net.geoprism.GeoprismPatcher;
import net.geoprism.context.ProjectDataConfiguration;
import net.geoprism.data.LocationImporter;
import net.geoprism.data.XMLEndpoint;
import net.geoprism.data.XMLLocationImporter;
import net.geoprism.data.aws.AmazonEndpoint;
import net.geoprism.georegistry.io.HierarchyExporter;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdBusiness;

public class CambodiaHierarchyGenerator
{
  public static void main(String[] args) {
//    GeoprismPatcher.main(args);

//    updateBusObjKeyValueToCode();

//    generateHierarchyInXML();
    
//    GeoprismPatcher.checkDuplicateClasspathResources();
   
    importHierarchies();
  }
  
  
  @Request
  private static void updateBusObjKeyValueToCode()
  {
    List<MdBusiness> mdBusList = new LinkedList<MdBusiness>();
    
    QueryFactory qf = new QueryFactory();
    
    UniversalQuery uQ = new UniversalQuery(qf);
    
    OIterator<? extends Universal> i = uQ.getIterator();
    
    try
    {
      while (i.hasNext())
      {
        Universal universal = i.next();
        MdBusiness mdBusiness = universal.getMdBusiness();
        
        mdBusiness.setGenerateSource(false);
        mdBusiness.apply();
        
        mdBusList.add(mdBusiness);

        System.out.println(universal.getUniversalId()+" "+universal.getMdBusiness());
      }
    }
    finally
    {
      i.close();
    }
    
    for (MdBusiness mdBusiness : mdBusList)
    {
      updateBusObjeCode(mdBusiness);
    }
    
  }
  
  @Transaction
  private static void updateBusObjeCode(MdBusiness mdBusiness)
  {
    System.out.println("\nUpdating "+mdBusiness.getTypeName());
    System.out.println("-------------------------------------");
    
    QueryFactory qf = new QueryFactory();
    
    BusinessQuery bQ = qf.businessQuery(mdBusiness.definesType());
    
    OIterator<Business> i = bQ.getIterator();
    
    int counter = 0;
    try
    {
      while(i.hasNext())
      {
        Business business = i.next();
        business.setKeyName(business.getValue(DefaultAttribute.CODE.getName()));
        business.apply();
        
        counter++;
        
        System.out.print(".");
        
        if (counter % 100 == 0)
        {
          System.out.print("\n");
        }
      }
    }
    finally
    {
      i.close();
    }
  }
  
  
  @Request
  private static void importHierarchies()
  {
//    XMLEndpoint endpoint = new LocalEndpoint(new File("/Users/nathan/git/geoprism-registry/georegistry-server/cache/deployable_countries"));

    ProjectDataConfiguration configuration = new ProjectDataConfiguration();
    
    XMLEndpoint endpoint = new AmazonEndpoint();
    
    LocationImporter importer = new XMLLocationImporter(endpoint);
    importer.loadProjectData(configuration);
  }
  
  @Request
  private static void generateHierarchyInXML()
  {
// /Users/nathan/git/geoprism/geoprism-server/src/main/resources/geoprism/xsd/datatype.xsd
// runwaysdk-server/src/main/resources/com/runwaysdk/resources/xsd/version.xsd
// classpath:com/runwaysdk/resources/xsd/version.xsd    
    String uniFileName = "/Users/nathan/git/geoprism-registry/georegistry-server/cache/deployable_countries/cambodia/xml/2.0/universals/Universal-Cambodia(0001542848192180).xml";
    String geoFileName = "/Users/nathan/git/geoprism-registry/georegistry-server/cache/deployable_countries/cambodia/xml/2.0/geoentities/Geo Entity-Cambodia(0001542848192181).xml";
    String schemaLocation = "classpath:com/runwaysdk/resources/xsd/version.xsd";
    
    
    System.out.println("Test");
    
    HierarchyExporter.exportHierarchyDefinition(uniFileName, schemaLocation, false);
    
//    HierarchyExporter.exportHierarchyInstances(geoFileName,  schemaLocation, false);
   
    
//    Universal universal = Universal.getByKey("Cambodia_Village");
//    
//    universal.clearGeometryType();
//    universal.addGeometryType(GeometryType.POINT);
//    universal.apply();
    
  }
}
