/**
 *
 */
package net.geoprism.registry.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.shapefile.GeoObjectAtTimeShapefileExporter;
import net.geoprism.registry.test.FastTestDataset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectAtTimeShapefileExporterTest extends FastDatasetTest implements InstanceTestClassListener
{
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
    ServerGeoObjectType type = ServerGeoObjectType.get(FastTestDataset.PROVINCE.getCode());

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);

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
    ServerGeoObjectType type = ServerGeoObjectType.get(FastTestDataset.PROVINCE.getCode());

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    SimpleFeatureType featureType = exporter.createFeatureType();

    Assert.assertNotNull(featureType);

    Assert.assertEquals(GeoObjectAtTimeShapefileExporter.GEOM, featureType.getGeometryDescriptor().getLocalName());

    List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();

    Assert.assertEquals(4, attributes.size());
  }

  @Test
  @Request
  public void testCreateFeatures()
  {
    ServerGeoObjectType type = FastTestDataset.PROVINCE.getServerObject();

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    SimpleFeatureType featureType = exporter.createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = exporter.features(featureType);

    Assert.assertTrue(features.size() > 0);

    final FeatureIterator<SimpleFeature> it = features.features();

    boolean hasCentralProvince = false;

    while (it.hasNext())
    {
      SimpleFeature feature = it.next();

      if (feature.getID().equals(FastTestDataset.PROV_CENTRAL.getCode()))
      {
        hasCentralProvince = true;

        final ServerGeoObjectIF object = FastTestDataset.PROV_CENTRAL.getServerObject();

        Object geometry = feature.getDefaultGeometry();
        Assert.assertNotNull(geometry);

        Collection<AttributeType> attributes = new ImportAttributeSerializer(Session.getCurrentLocale(), false, false, type.toDTO()).attributes(type.toDTO());

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
      }
    }

    Assert.assertTrue("Unable to find the central province feature", hasCentralProvince);

  }

  @Test
  @Request
  public void testWriteToFile() throws IOException
  {
    ServerGeoObjectType type = FastTestDataset.PROVINCE.getServerObject();

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    File directory = exporter.writeToFile();

    Assert.assertTrue(directory.exists());

    File[] files = directory.listFiles();

    Assert.assertEquals(5, files.length);
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    ServerGeoObjectType type = FastTestDataset.PROVINCE.getServerObject();

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    InputStream export = exporter.export();

    Assert.assertNotNull(export);

    IOUtils.copy(export, NullOutputStream.NULL_OUTPUT_STREAM);
  }

}
