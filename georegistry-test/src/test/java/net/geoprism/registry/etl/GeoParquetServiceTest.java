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
package net.geoprism.registry.etl;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.SchedulerManager;

import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InvalidProjectionException;
import net.geoprism.registry.USADatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.excel.SheetDTO;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.io.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.GeoParquetBusinessService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.request.GeoParquetService;
import net.geoprism.registry.service.request.RegistryComponentService;
import net.geoprism.registry.test.SchedulerTestUtils;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.test.USATestData;
import net.geoprism.registry.view.ImportConfigurationView;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class GeoParquetServiceTest extends USADatasetTest implements InstanceTestClassListener
{
  private static final String        STATES_RESOURCE   = "/geoparquet/cb_2017_us_state_500k.parquet.test";

  private static final String        STATES_FILE_NAME  = "cb_2017_us_state_500k.parquet";

  private static final String        MERCATOR_RESOURCE = "/geoparquet/mercator.parquet.test";

  @Autowired
  private RegistryComponentService   service;

  @Autowired
  private GeoParquetService          geoParquetService;

  @Autowired
  private GeoParquetBusinessService  geoParquetBusinessService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    TestDataSet.deleteAllSchedulerData();

    super.beforeClassSetup();

    if (!SchedulerManager.initialized())
    {
      SchedulerManager.start();
    }

    ServiceFactory.getMetadataCache().getAllGeoObjectTypes().forEach(t -> t.markAsDirty());
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();
    FileUtils.deleteDirectory(new File(VaultProperties.getPath("vault.default"), "files"));
  }

  @BeforeEach
  public void setUp() throws Exception
  {
    super.setUp();

    clearData();
  }

  @AfterEach
  public void tearDown() throws Exception
  {
    PostalCodeFactory.clear();

    clearData();

    super.tearDown();
  }

  @Request
  private void clearData()
  {
    SchedulerTestUtils.clearImportData();

    TestGeoObjectInfo parent = new TestGeoObjectInfo("00", USATestData.COUNTRY, USATestData.SOURCE);
    parent.delete();

    // Clear out the event table
    Database.deleteWhere("domainevententry", "true");
  }

  @Test
  @Request
  public void testGetConfiguration() throws Exception
  {
    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Assertions.assertNotNull(istream);

      GeoObjectImportConfigurationDTO dto = this.geoParquetService.getConfiguration(testData.clientRequest.getSessionId(), STATES_FILE_NAME, istream, getView());

      Assertions.assertEquals(FormatImporterType.GEOPARQUET, dto.getFormatType());
      Assertions.assertFalse(dto.getPostalCode());

      SheetDTO sheet = dto.getSheet();

      Assertions.assertNotNull(sheet);
      Assertions.assertEquals("cb_2017_us_state_500k", sheet.getName());

      Map<String, List<String>> attributes = sheet.getAttributes();

      // 7 string columns plus the 2 numeric columns. The geometry column and
      // the bounding box struct column written by GDAL are excluded.
      List<String> fields = attributes.get(GeoObjectImportConfiguration.TEXT);

      Assertions.assertEquals(9, fields.size());
      Assertions.assertEquals("STATEFP", fields.get(0));
      Assertions.assertFalse(fields.contains("geometry"));
      Assertions.assertFalse(fields.contains("geometry_bbox"));

      Assertions.assertEquals(2, attributes.get(GeoObjectImportConfiguration.NUMERIC).size());
      Assertions.assertEquals(0, attributes.get(AttributeBooleanType.TYPE).size());
      Assertions.assertEquals(0, attributes.get(AttributeDateType.TYPE).size());

      // Every state has LSAD "00", while GEOID is unique
      Assertions.assertTrue(sheet.getNonUniqueAttributes().contains("LSAD"));
      Assertions.assertFalse(sheet.getNonUniqueAttributes().contains("GEOID"));
    }
  }

  @Test
  @Request
  public void testInvalidProjection() throws Exception
  {
    try (InputStream istream = this.getClass().getResourceAsStream(MERCATOR_RESOURCE))
    {
      Assertions.assertNotNull(istream);

      Assertions.assertThrows(InvalidProjectionException.class, () -> {
        this.geoParquetBusinessService.getConfiguration("mercator.parquet", istream, getView(), false);
      });
    }
  }

  @Test
  @Request
  public void testImport() throws Throwable
  {
    try (InputStream istream = this.getClass().getResourceAsStream(STATES_RESOURCE))
    {
      Assertions.assertNotNull(istream);

      GeoObjectImportConfiguration config = this.getTestConfiguration(istream);
      config.setHierarchy(ServerHierarchyType.get(USATestData.HIER_ADMIN.getCode()));

      ImportHistory hist = mockImport(config);
      Assertions.assertTrue(hist.getStatus().get(0).equals(AllJobStatus.SUCCESS));

      hist = ImportHistory.get(hist.getOid());
      Assertions.assertEquals(Long.valueOf(56), hist.getWorkTotal());
      Assertions.assertEquals(Long.valueOf(56), hist.getWorkProgress());
      Assertions.assertEquals(Long.valueOf(56), hist.getImportedRecords());
      Assertions.assertEquals(ImportStage.COMPLETE, hist.getStage().get(0));

      GeoObject object = this.service.getGeoObjectByCode(testData.clientRequest.getSessionId(), "01", USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE);

      Assertions.assertNotNull(object);
      Assertions.assertNotNull(object.getGeometry());
      Assertions.assertEquals("Alabama", object.getLocalizedDisplayLabel());
      Assertions.assertEquals(true, object.getExists());
    }
  }

  private ImportConfigurationView getView()
  {
    return ImportConfigurationView.of(JobHistoryType.GEO_OBJECT, USATestData.STATE.getCode(), TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, USATestData.SOURCE.getCode(), ImportStrategy.NEW_AND_UPDATE, false);
  }

  private GeoObjectImportConfiguration getTestConfiguration(InputStream istream)
  {
    GeoObjectImportConfigurationDTO dto = this.geoParquetService.getConfiguration(testData.clientRequest.getSessionId(), STATES_FILE_NAME, istream, getView());
    dto.setObjectType(JobHistoryType.GEO_OBJECT);
    dto.setPostalCode(false);

    dto.getType().getAttributes().forEach(attribute -> {
      String attributeName = attribute.getCode();

      if (attributeName.equals(GeoObject.DISPLAY_LABEL))
      {
        attribute.setTarget("NAME");
      }
      else if (attributeName.equals(GeoObject.CODE))
      {
        attribute.setTarget("GEOID");
      }
    });

    GeoObjectImportConfiguration config = (GeoObjectImportConfiguration) ImportConfiguration.build(dto, true);
    config.setStartDate(USATestData.DEFAULT_OVER_TIME_DATE);
    config.setEndDate(USATestData.DEFAULT_END_TIME_DATE);
    config.setImportStrategy(ImportStrategy.NEW_AND_UPDATE);

    return config;
  }

  private ImportHistory mockImport(GeoObjectImportConfiguration config) throws Throwable
  {
    if (config.getStartDate() == null)
    {
      config.setStartDate(new Date());
    }

    if (config.getEndDate() == null)
    {
      config.setEndDate(new Date());
    }

    DataImportJob job = new DataImportJob();
    job.apply();
    ImportHistory hist = (ImportHistory) job.createNewHistory();

    config.setHistoryId(hist.getOid());
    config.setJobId(job.getOid());

    ServerGeoObjectType type = config.getType();

    hist.appLock();
    hist.setImportFileId(config.getVaultFileId());
    hist.setConfiguration(config.toDTO());
    hist.setOrganization(type.getOrganization().getOrganization());
    hist.setGeoObjectTypeCode(type.getCode());
    hist.apply();

    ExecutionContext context = job.startSynchronously(hist);

    return (ImportHistory) context.getHistory();
  }
}
