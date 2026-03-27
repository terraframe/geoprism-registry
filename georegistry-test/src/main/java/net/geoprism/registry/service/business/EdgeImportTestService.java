package net.geoprism.registry.service.business;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory.ObjectImportType;
import net.geoprism.registry.etl.upload.EdgeObjectImportConfiguration;
import net.geoprism.registry.etl.upload.EdgeObjectImporter.ReferenceStrategy;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestGeoObjectInfo;
import net.geoprism.registry.view.ImportHistoryView;

@Service
public class EdgeImportTestService
{
  @Autowired
  private GraphBusinessService service;

  @Autowired
  private ETLBusinessService   etlService;

  public List<ImportHistoryView> getHistory(String objectType, String typeCode, String graphTypeClass)
  {
    return this.etlService.getHistory(objectType, typeCode, graphTypeClass);
  }

  public ImportHistory importJsonFile(String config) throws InterruptedException
  {
    String retConfig = this.etlService.doImport(config).toString();

    EdgeObjectImportConfiguration configuration = (EdgeObjectImportConfiguration) ImportConfiguration.build(retConfig, true);

    String historyId = configuration.getHistoryId();

    Thread.sleep(100);

    return ImportHistory.get(historyId);
  }

  public EdgeObjectImportConfiguration getTestConfiguration(String graphTypeClass, String graphTypeCode, InputStream istream, ImportStrategy strategy)
  {
    ObjectNode result = service.getJsonImportConfiguration(graphTypeClass, graphTypeCode, TestDataSet.DEFAULT_OVER_TIME_DATE, TestDataSet.DEFAULT_END_TIME_DATE, null, "test.json", istream, strategy);

    result.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.JSON.name());
    result.put(ImportConfiguration.OBJECT_TYPE, ObjectImportType.EDGE_OBJECT.name());

    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE, "source");
    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE_STRATEGY, ReferenceStrategy.CODE.name());
    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE_TYPE, "sourceType");
    result.put(EdgeObjectImportConfiguration.EDGE_SOURCE_TYPE_STRATEGY, ReferenceStrategy.CODE.name());
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET, "target");
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET_STRATEGY, ReferenceStrategy.CODE.name());
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET_TYPE, "targetType");
    result.put(EdgeObjectImportConfiguration.EDGE_TARGET_TYPE_STRATEGY, ReferenceStrategy.CODE.name());

    EdgeObjectImportConfiguration configuration = (EdgeObjectImportConfiguration) ImportConfiguration.build(result.toString(), true);

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
