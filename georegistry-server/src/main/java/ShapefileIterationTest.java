import java.io.File;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.sort.SortBy;

public class ShapefileIterationTest
{
  public static void main(String[] args) throws Exception
  {
    internetQueryCode();
  }
  
  public static void internetQueryCode() throws Exception
  {
    URL url = new File("/Users/richard1/dev/projects/georegistry/data/tolkien/Administrative_Boundries/2000/Shire/shire.shp").toURI().toURL();
    
    FileDataStore myData = FileDataStoreFinder.getDataStore(url);
    
    try
    {
      SimpleFeatureSource source = myData.getFeatureSource();
  
      Query query = new Query();
      query.setStartIndex(1);
      query.setSortBy(new SortBy[] {SortBy.NATURAL_ORDER});
//      query.setSortBy(SortBy.UNSORTED);
      
      SimpleFeatureIterator iterator = source.getFeatures(query).features();
      
      try
      {
        int i = 1;
        
        while (iterator.hasNext())
        {
          SimpleFeature feature = iterator.next();
          
          System.out.println("Feature num " + i + "[" + feature.getIdentifier() + "]");
          
          i++;
        }
      }
      finally
      {
        iterator.close();
      }
    }
    finally
    {
      myData.dispose();
    }
  }
  
  public static void smethiesCode() throws Exception
  {
    URL url = new File("/home/rich/dev/projects/georegistry/data/tolkien/Administrative_Boundries/2000/Shire/shire.dbf").toURI().toURL();
    
    System.out.println("Processing shapefile from url [" + url.toString() + "].");
    
//    FileDataStore fds = FileDataStoreFinder.getDataStore(url);
//    FeatureReader<SimpleFeatureType, SimpleFeature> reader = fds.getFeatureReader();
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
        
        int i = 1;

        try
        {
          while (iterator.hasNext())
          {
            SimpleFeature feature = iterator.next();
            
            String names = "";
//            Iterator<Property> it = feature.getProperties().iterator();
//            while (it.hasNext())
//            {
//              Property prop = it.next();
//              names = names + ", " + prop.getName();
//            }
            
//            System.out.println("Feature num " + i + "[" + feature.getAttribute("Shr_Code") + "] " + "[" + names + "]");
            System.out.println("Feature num " + i + "[" + feature.getIdentifier() + "] " + "[" + names + "]");
            
            i++;
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
}
