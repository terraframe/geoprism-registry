/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
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

import com.google.gson.JsonObject;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.MasterList;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.conversion.SupportedLocaleCache;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.MasterListTest;
import net.geoprism.registry.shapefile.MasterListShapefileExporter;
import net.geoprism.registry.test.FastTestDataset;

public class MasterListGeoObjectShapefileExporterTest
{
  private static FastTestDataset                          testData;

  private static MasterList                               masterlist;

  private static MasterListVersion                        version;

  private static MdBusinessDAOIF                          mdBusiness;

  private static List<? extends MdAttributeConcreteDAOIF> mdAttributes;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();

    classSetupInRequest();
  }

  @Request
  private static void classSetupInRequest()
  {
    JsonObject json = MasterListTest.getJson(FastTestDataset.ORG_CGOV.getServerObject(), FastTestDataset.HIER_ADMIN, FastTestDataset.PROVINCE, MasterList.PUBLIC, false, FastTestDataset.COUNTRY);

    masterlist = MasterList.create(json);
    version = masterlist.createVersion(FastTestDataset.DEFAULT_OVER_TIME_DATE, MasterListVersion.EXPLORATORY);
    mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());
    mdAttributes = mdBusiness.definesAttributesOrdered().stream().filter(mdAttribute -> version.isValid(mdAttribute)).collect(Collectors.toList());
  }

  @AfterClass
  public static void cleanUpClass()
  {
    if (testData != null)
    {
      classTearDownInRequest();

      testData.tearDownMetadata();
    }
  }

  @Request
  private static void classTearDownInRequest()
  {
    if (version != null)
    {
      version.delete();
    }

    if (masterlist != null)
    {
      masterlist.delete();
    }
  }

  @Before
  @Request
  public void setUp()
  {
    if (testData != null)
    {
      testData.setUpInstanceData();

      version.publish();
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
    MasterListShapefileExporter exporter = new MasterListShapefileExporter(version, mdBusiness, mdAttributes, null);

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
    MasterListShapefileExporter exporter = new MasterListShapefileExporter(version, mdBusiness, mdAttributes, null);
    SimpleFeatureType featureType = exporter.createFeatureType();

    Assert.assertNotNull(featureType);

    Assert.assertEquals(MasterListShapefileExporter.GEOM, featureType.getGeometryDescriptor().getLocalName());

    List<AttributeDescriptor> attributes = featureType.getAttributeDescriptors();

    Assert.assertEquals(6, attributes.size());
  }

  @Test
  @Request
  public void testCreateFeatures()
  {
    ServerGeoObjectIF object = FastTestDataset.PROV_CENTRAL.getServerObject();
    ServerGeoObjectType type = object.getType();

    MasterListShapefileExporter exporter = new MasterListShapefileExporter(version, mdBusiness, mdAttributes, null);
    SimpleFeatureType featureType = exporter.createFeatureType();

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = exporter.features(featureType);

    Assert.assertEquals(1, features.size());

    SimpleFeature feature = features.features().next();

    Assert.assertEquals("Attributes not equal [code]", object.getCode(), feature.getAttribute(GeoObject.CODE));

    Object geometry = feature.getDefaultGeometry();
    Assert.assertNotNull(geometry);

    ImportAttributeSerializer serializer = new ImportAttributeSerializer(Session.getCurrentLocale(), false, false, false, SupportedLocaleCache.getLocales());
    Collection<AttributeType> attributes = serializer.attributes(type.getType());

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

    Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), feature.getAttribute(exporter.getColumnName("fastadmincodefastcountry")));
    Assert.assertEquals(FastTestDataset.CAMBODIA.getDisplayLabel(), feature.getAttribute(exporter.getColumnName("fastadmincodefastcountryDefaultLocale")));
  }

  @Test
  @Request
  public void testWriteToFile() throws IOException
  {

    MasterListShapefileExporter exporter = new MasterListShapefileExporter(version, mdBusiness, mdAttributes, null);
    File directory = exporter.writeToFile();

    Assert.assertTrue(directory.exists());

    File[] files = directory.listFiles();

    Assert.assertEquals(5, files.length);
  }

  @Test
  @Request
  public void testExport() throws IOException
  {
    MasterListShapefileExporter exporter = new MasterListShapefileExporter(version, mdBusiness, mdAttributes, null);
    InputStream export = exporter.export();

    Assert.assertNotNull(export);

    IOUtils.copy(export, NullOutputStream.NULL_OUTPUT_STREAM);
  }

}
