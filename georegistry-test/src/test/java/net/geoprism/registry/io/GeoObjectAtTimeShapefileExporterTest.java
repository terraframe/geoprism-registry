/**
 *
 */
package net.geoprism.registry.io;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.shapefile.GeoObjectAtTimeShapefileExporter;
import net.geoprism.registry.test.FastTestDataset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class GeoObjectAtTimeShapefileExporterTest extends FastDatasetTest implements InstanceTestClassListener
{
  @AfterEach
  public void tearDown() throws Exception
  {
    super.tearDown();

    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @Test
  @Request
  public void testGenerateName()
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(FastTestDataset.PROVINCE.getCode());

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);

    Assertions.assertEquals("testestest", exporter.generateColumnName("testestestest1"));
    Assertions.assertEquals("testestes1", exporter.generateColumnName("testestestest2"));
    Assertions.assertEquals("testestes2", exporter.generateColumnName("testestestest3"));
    Assertions.assertEquals("testestes3", exporter.generateColumnName("testestestest4"));
    Assertions.assertEquals("testestes4", exporter.generateColumnName("testestestest5"));
    Assertions.assertEquals("testestes5", exporter.generateColumnName("testestestest6"));
    Assertions.assertEquals("testestes6", exporter.generateColumnName("testestestest7"));
    Assertions.assertEquals("testestes7", exporter.generateColumnName("testestestest8"));
    Assertions.assertEquals("testestes8", exporter.generateColumnName("testestestest9"));
    Assertions.assertEquals("testestes9", exporter.generateColumnName("testestestest10"));
    Assertions.assertEquals("testeste10", exporter.generateColumnName("testestestest11"));
  }

  @Test
  @Request
  public void testCreateFeatureType()
  {
    ServerGeoObjectType type = ServerGeoObjectType.get(FastTestDataset.PROVINCE.getCode());

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    SimpleFeatureType featureType = exporter.createFeatureType();

    Assertions.assertNotNull(featureType);

    Assertions.assertEquals(GeoObjectAtTimeShapefileExporter.GEOM, featureType.getGeometryDescriptor().getLocalName());

    List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();

    Assertions.assertEquals(4, attributes.size());
  }

  @Test
  @Request
  public void testCreateFeatures()
  {
    ServerGeoObjectType type = FastTestDataset.PROVINCE.getServerObject();

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    SimpleFeatureType featureType = exporter.createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = exporter.features(featureType);

    Assertions.assertTrue(features.size() > 0);

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
        Assertions.assertNotNull(geometry);

        Collection<AttributeType> attributes = new ImportAttributeSerializer(Session.getCurrentLocale(), false, false, type.toDTO()).attributes(type.toDTO());

        for (AttributeType attribute : attributes)
        {
          String attributeName = attribute.getCode();

          Object oValue = object.getValue(attributeName);
          Object fValue = feature.getAttribute(exporter.getColumnName(attributeName));

          if (attribute instanceof AttributeLocalType)
          {
            Assertions.assertEquals(( (LocalizedValue) oValue ).getValue(), fValue, "Attributes not equal [" + attributeName + "]");
          }
          else
          {
            Assertions.assertEquals(oValue, fValue, "Attributes not equal [" + attributeName + "]");
          }
        }
      }
    }

    Assertions.assertTrue(hasCentralProvince, "Unable to find the central province feature");

  }

  @Test
  @Request
  public void testWriteToFile() throws Exception
  {
    ServerGeoObjectType type = FastTestDataset.PROVINCE.getServerObject();

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    File directory = exporter.writeToFile();

    Assertions.assertTrue(directory.exists());

    File[] files = directory.listFiles();

    Assertions.assertEquals(5, files.length);
  }

  @Test
  @Request
  public void testExport() throws Exception
  {
    ServerGeoObjectType type = FastTestDataset.PROVINCE.getServerObject();

    GeoObjectAtTimeShapefileExporter exporter = new GeoObjectAtTimeShapefileExporter(type, FastTestDataset.DEFAULT_OVER_TIME_DATE);
    InputStream export = exporter.export();

    Assertions.assertNotNull(export);

    IOUtils.copy(export, NullOutputStream.NULL_OUTPUT_STREAM);
  }

}
