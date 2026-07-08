package net.geoprism.registry.service.business;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.etl.upload.EdgeObjectImportConfiguration;
import net.geoprism.registry.etl.upload.EdgeObjectImporter.ReferenceStrategy;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.view.EdgeImportConfigurationView;
import net.geoprism.registry.view.EdgeObjectImportConfigurationDTO;
import net.geoprism.registry.view.ImportConfigurationDTO;
import net.geoprism.registry.view.ImportHistoryView;

@Service
public class EdgeImportTestService
{
  @Autowired
  private GraphBusinessService service;

  @Autowired
  private ETLBusinessService   etlService;

  public List<ImportHistoryView> getHistory(String classType, String typeCode)
  {
    return this.etlService.getHistory(classType, typeCode);
  }

  public ImportHistory importJsonFile(ImportConfigurationDTO dto) throws InterruptedException
  {
    ImportConfigurationDTO retConfig = this.etlService.doImport(dto);

    EdgeObjectImportConfiguration configuration = (EdgeObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }

  public EdgeObjectImportConfiguration getTestConfiguration(String graphTypeClass, String graphTypeCode, InputStream istream, ImportStrategy strategy)
  {
    EdgeObjectImportConfigurationDTO dto = service.getJsonImportConfiguration("test.json", istream, EdgeImportConfigurationView.of(graphTypeClass, graphTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null, strategy));

    dto.setFormatType(FormatImporterType.JSON);
    dto.setObjectType(JobHistoryType.EDGE_OBJECT);

    dto.setEdgeSource("source");
    dto.setEdgeSourceStrategy(ReferenceStrategy.CODE);
    dto.setEdgeSourceType("sourceType");
    dto.setEdgeSourceTypeStrategy(ReferenceStrategy.CODE);
    dto.setEdgeTarget("target");
    dto.setEdgeTargetStrategy(ReferenceStrategy.CODE);
    dto.setEdgeTargetType("targetType");
    dto.setEdgeTargetTypeStrategy(ReferenceStrategy.CODE);

    EdgeObjectImportConfiguration configuration = (EdgeObjectImportConfiguration) ImportConfiguration.build(dto, true);

    return configuration;
  }

  public InputStream generateEdgeJson(TestGeoObjectInfo source, TestGeoObjectInfo target)
  {
    return this.generateEdgeJson(source.getCode(), source.getGeoObjectType().getCode(), target.getCode(), target.getGeoObjectType().getCode());
  }

  public InputStream generateEdgeJson(String sourceCode, String sourceType, String targetCode, String targetType)
  {
    JSONArray all = new JSONArray();

    JSONObject jo = new JSONObject();
    jo.put("source", sourceCode);
    jo.put("sourceType", sourceType);
    jo.put("target", targetCode);
    jo.put("targetType", targetType);
    all.put(jo);

    return new ByteArrayInputStream(all.toString().getBytes());
  }
}
