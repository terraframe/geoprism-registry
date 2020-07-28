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
package net.geoprism.registry.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.feature.FeatureCollection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.query.postgres.GeoObjectIterator;
import net.geoprism.registry.query.postgres.GeoObjectQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.shapefile.GeoObjectShapefileExporter;
import net.geoprism.registry.test.ListIterator;
import net.geoprism.registry.test.USATestData;

public class GeoObjectShapefileExporterTest
{
  private static USATestData     testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = USATestData.newTestData();
    testData.setUpMetadata();
  }
  
  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }
  
  @Before
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();
    }
  }

  @After
  public void tearDown() throws IOException
  {
    if (testData != null)
    {
      testData.tearDownInstanceData();
    }
    
    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Test
  @Request
  public void testGenerateName()
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(testData.STATE.getCode());
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, hierarchyType, new ListIterator<>(new LinkedList<>()));

    Assert.assertEquals("testestest", exporter.generateColumnName("testestestest1"));
    Assert.assertEquals("testestes1", exporter.generateColumnName("testestestest2"));
    Assert.assertEquals("testestes2", exporter.generateColumnName("testestestest3"));
    Assert.assertEquals("testestes3", exporter.generateColumnName("testestestest4"));
    Assert.assertEquals("testestes4", exporter.generateColumnName("testestestest5"));
    Assert.assertEquals("testestes5", exporter.generateColumnName("testestestest6"));
    Assert.assertEquals("testestes6", exporter.generateColumnName("testestestest7"));
    Assert.assertEquals("testestes7", exporter.generateColumnName("testestestest8"));
    Assert.assertEquals("testestes8", exporter.generateColumnName("testestestest9"));
    Assert.assertEquals("testestes9", exporter.generateColumnName("testestestest10"));
    Assert.assertEquals("testeste10", exporter.generateColumnName("testestestest11"));
  }

  @Test
  @Request
  public void testCreateFeatureType()
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(testData.STATE.getCode());
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, hierarchyType, new ListIterator<>(new LinkedList<>()));
    SimpleFeatureType featureType = exporter.createFeatureType();

    Assert.assertNotNull(featureType);

    Assert.assertEquals(GeoObjectShapefileExporter.GEOM, featureType.getGeometryDescriptor().getLocalName());

    List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();

    Assert.assertEquals(6, attributes.size());
  }

  @Test
  @Request
  public void testCreateFeatures()
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    List<GeoObject> objects = new GeoObjectQuery(type).getIterator().getAll();

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, hierarchyType, new GeoObjectQuery(type).getIterator());
    SimpleFeatureType featureType = exporter.createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = exporter.features(featureType);

    Assert.assertEquals(objects.size(), features.size());

    SimpleFeature feature = features.features().next();
    GeoObject object = objects.get(0);

    Assert.assertEquals("Attributes not equal [code]", object.getValue(GeoObject.CODE), feature.getAttribute(GeoObject.CODE));

    Object geometry = feature.getDefaultGeometry();
    Assert.assertNotNull(geometry);

    Collection<AttributeType> attributes = new ImportAttributeSerializer(Session.getCurrentLocale(), false, true, SupportedLocaleDAO.getSupportedLocales()).attributes(type.getType());

    for (AttributeType attribute : attributes)
    {
      String attributeName = attribute.getName();

      Object oValue = object.getValue(attributeName);
      Object fValue = feature.getAttribute(exporter.getColumnName(attributeName));

      if (attribute instanceof AttributeTermType)
      {
        Assert.assertEquals("Attributes not equal [" + attributeName + "]", GeoObjectUtil.convertToTermString((AttributeTermType) attribute, oValue), fValue);
      }
      else if (attribute instanceof AttributeLocalType)
      {
        Assert.assertEquals("Attributes not equal [" + attributeName + "]", ( (LocalizedValue) oValue ).getValue(), fValue);
      }
      else
      {
        Assert.assertEquals("Attributes not equal [" + attributeName + "]", oValue, fValue);
      }
    }

    // Assert the value of the parent columns
    // Add the type ancestor fields
    List<GeoObjectType> ancestors = ServiceFactory.getUtilities().getTypeAncestors(type, hierarchyType.getCode());

    GeoObjectType ancestor = ancestors.get(0);

    String code = ancestor.getCode() + " " + ancestor.getAttribute(GeoObject.CODE).get().getName();
    String label = ancestor.getCode() + " " + MdAttributeLocalInfo.DEFAULT_LOCALE;

    Assert.assertEquals(testData.USA.getCode(), feature.getAttribute(exporter.getColumnName(code)));
    Assert.assertEquals(testData.USA.getDisplayLabel(), feature.getAttribute(exporter.getColumnName(label)));
  }

  @Test
  @Request
  public void testWriteToFile() throws IOException
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectIterator objects = new GeoObjectQuery(type).getIterator();

    try
    {
      GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, hierarchyType, objects);
      File directory = exporter.writeToFile();

      Assert.assertTrue(directory.exists());

      File[] files = directory.listFiles();

      Assert.assertEquals(5, files.length);
    }
    finally
    {
      objects.close();
    }
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    ServerGeoObjectType type = testData.STATE.getServerObject();
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(testData.HIER_ADMIN.getCode());

    GeoObjectIterator objects = new GeoObjectQuery(type).getIterator();

    try
    {
      GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, hierarchyType, objects);
      InputStream export = exporter.export();

      Assert.assertNotNull(export);

      IOUtils.copy(export, new NullOutputStream());
    }
    finally
    {
      objects.close();
    }
  }

}
