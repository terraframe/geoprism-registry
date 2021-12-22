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
package net.geoprism.registry;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.sort.SortBy;

import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;

import junit.framework.Assert;
import net.geoprism.registry.etl.upload.ShapefileImporter;

/**
 * This test makes sure that geotools loops through shapefiles correctly and doesn't skip features.
 * Geotools was caught doing this on an earlier version when using Query criteria.
 * 
 * @author rrowlands
 *
 */
public class GeotoolsLoopTest
{
  @Test
  public void testQuerySorting() throws Exception
  {
    String[] files = new String[] {"shapefile/ntd_zam_operational_28082020.zip.test", "shapefile/schs_voronoi_externalId.zip.test", "cb_2017_us_state_500k.zip.test"};
    
    for (String file : files)
    {
      String filename = file;
      if (file.endsWith(".test"))
      {
        filename = file.replace(".test", "");
      }
      
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
      ApplicationResource res = new StreamResource(is, filename);
      Set<String> sorted = runInReq(res, true);
      
      InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
      ApplicationResource res2 = new StreamResource(is2, filename);
      Set<String> unsorted = runInReq(res2, false);
      
      diff(unsorted, sorted);
      
      Assert.assertEquals(sorted.size(), unsorted.size());
    }
  }
  
  @Request
  public static Set<String> runInReq(ApplicationResource res, boolean sort) throws Exception
  {
    Set<String> featureIdSet = new HashSet<String>();
    
    Set<String> nonUniqueFeatureIds = new HashSet<String>();
    
    try (CloseableFile shp = ShapefileImporter.getShapefileFromResource(res, "shp"))
    {
      FileDataStore myData = FileDataStoreFinder.getDataStore(shp);
      
      try
      {
        SimpleFeatureSource source = myData.getFeatureSource();
    
        Query query = new Query();
        
        if (sort)
        {
          query.setSortBy(new SortBy[] {SortBy.NATURAL_ORDER}); // Enforce predictable ordering based on alphabetical Feature Ids
        }
        
        long total = source.getFeatures(query).size();
        
        SimpleFeatureIterator iterator = source.getFeatures(query).features();
        
        long count = 0;
        
        try
        {
          while (iterator.hasNext())
          {
            SimpleFeature feature = iterator.next();
            
            String featureId = feature.getID();
            
            if (!featureIdSet.add(featureId))
            {
              nonUniqueFeatureIds.add(featureId);
            }
            
            count++;
          }
        }
        finally
        {
          iterator.close();
        }
        
        System.out.println("Imported " + count + " of total " + total);
      }
      finally
      {
        myData.dispose();
      }
    }
    
    if (nonUniqueFeatureIds.size() > 0)
    {
      throw new RuntimeException("Detected non-unique feature Ids [" + StringUtils.join(nonUniqueFeatureIds, ", ") + "]");
    }
    
    return featureIdSet;
  }
  
  private static void diff(Set<String> big, Set<String> small)
  {
    Set<String> notFound = new HashSet<String>();
    
    for (String b : big)
    {
      boolean found = false;
      
      for (String s : small)
      {
        if (s.equals(b))
        {
          found = true;
          break;
        }
      }
      
      if (found == false)
      {
        notFound.add(b);
      }
    }
    
    if (notFound.size() > 0)
    {
      throw new RuntimeException("Did not find [" + StringUtils.join(notFound, ", ") + "].");
    }
  }
}
