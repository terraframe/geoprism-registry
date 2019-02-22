package net.geoprism.georegistry.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.geotools.feature.FeatureCollection;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.SessionFacade;
import com.runwaysdk.system.gis.geo.LocatedIn;

import net.geoprism.georegistry.query.GeoObjectIterator;
import net.geoprism.georegistry.query.GeoObjectQuery;
import net.geoprism.georegistry.service.ServiceFactory;
import net.geoprism.georegistry.shapefile.GeoObjectShapefileExporter;
import net.geoprism.registry.test.ListIterator;
import net.geoprism.registry.test.USATestData;

public class GeoObjectShapefileExporterTest
{
  private static USATestData     testData;

  private static ClientRequestIF adminCR;

  @BeforeClass
  public static void setUp()
  {
    testData = USATestData.newTestData(GeometryType.POLYGON, true);

    adminCR = testData.adminClientRequest;

    reload();
  }

  @Request
  public static void reload()
  {
    /*
     * Reload permissions for the new attributes
     */
    SessionFacade.getSessionForRequest(adminCR.getSessionId()).reloadPermissions();
  }

  @AfterClass
  public static void tearDown() throws IOException
  {
    testData.cleanUp();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Test
  public void testGenerateName()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(testData.STATE.getCode()).get();
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

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
  public void testCreateFeatureType()
  {
    GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(testData.STATE.getCode()).get();
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

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
    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    List<GeoObject> objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator().getAll();

    GeoObjectShapefileExporter exporter = new GeoObjectShapefileExporter(type, hierarchyType, new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator());
    SimpleFeatureType featureType = exporter.createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = exporter.features(featureType);

    Assert.assertEquals(objects.size(), features.size());

    SimpleFeature feature = features.features().next();
    GeoObject object = objects.get(0);

    Assert.assertEquals("Attributes not equal [code]", object.getValue(GeoObject.CODE), feature.getAttribute(GeoObject.CODE));

    Object geometry = feature.getDefaultGeometry();
    Assert.assertNotNull(geometry);

    Collection<AttributeType> attributes = new ImportAttributeSerializer(false, true).attributes(type);

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
    List<GeoObjectType> ancestors = ServiceFactory.getUtilities().getAncestors(type, hierarchyType.getCode());

    GeoObjectType ancestor = ancestors.get(0);

    String code = ancestor.getCode() + " " + ancestor.getAttribute(GeoObject.CODE).get().getName();
    String label = ancestor.getCode() + " " + ancestor.getAttribute(GeoObject.DISPLAY_LABEL).get().getName();

    Assert.assertEquals(testData.USA.getCode(), feature.getAttribute(exporter.getColumnName(code)));
    Assert.assertEquals(testData.USA.getDisplayLabel(), feature.getAttribute(exporter.getColumnName(label)));
  }

  @Test
  @Request
  public void testWriteToFile() throws IOException
  {
    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObjectIterator objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator();

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
    GeoObjectType type = testData.STATE.getGeoObjectType(GeometryType.POLYGON);
    HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(LocatedIn.class.getSimpleName()).get();

    GeoObjectIterator objects = new GeoObjectQuery(type, testData.STATE.getUniversal()).getIterator();

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
