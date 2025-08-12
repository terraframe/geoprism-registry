package net.geoprism.graph;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.service.business.RepoRDFExportBusinessService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.RDFExport;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class RepoRDFExportJob extends RepoRDFExportJobBase
{
  @SuppressWarnings("unused")
  private static final long  serialVersionUID           = 755269485;

  public static final String ARRAY_STORAGE_CONCAT_TOKEN = "___SPLIT___";

  public RepoRDFExportJob()
  {
    super();
  }

  public List<GraphTypeReference> getGraphTypeRefs()
  {
    List<String> codePairs = Arrays.asList(getGraphTypeCodes().split(ARRAY_STORAGE_CONCAT_TOKEN));

    List<GraphTypeReference> result = new ArrayList<GraphTypeReference>();
    for (int i = 0; i < codePairs.size(); i = i + 2)
    {
      result.add(new GraphTypeReference(codePairs.get(i), codePairs.get(i + 1)));
    }

    return result;
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    ImportHistory history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();

    RDFExport exportConfig = this.getExportConfig();

    JsonObject config = new JsonObject();
    config.addProperty(ImportConfiguration.FILE_NAME, "Export RDF from Repo (" + exportConfig.toDigest() + ")");
    config.addProperty(ImportConfiguration.OBJECT_TYPE, "RDF-REPO");

    history.appLock();
    history.setConfigJson(config.toString());
    history.apply();

    String progressId = history.getOid();

    RepoRDFExportBusinessService service = ServiceFactory.getBean(RepoRDFExportBusinessService.class);

    try (var tmp = new CloseableFile(Files.createTempFile(progressId, ".ttl").toFile()))
    {
      try (OutputStream os = new FileOutputStream(tmp))
      {
        try (ZipOutputStream zos = new ZipOutputStream(os))
        {
          ZipEntry entry = new ZipEntry(progressId + ".ttl");
          zos.putNextEntry(entry);

          service.export(history, exportConfig, zos);
        }
      }

      try (FileInputStream is = new FileInputStream(tmp))
      {
        var vf = VaultFile.createAndApply(progressId + ".zip", is);

        history.appLock();
        history.clearStage();
        history.addStage(ImportStage.IMPORT_RESOLVE);
        history.setImportFile(vf);
        history.apply();
        executionContext.setStatus(AllJobStatus.FEEDBACK);
      }
    }
  }

  @Override
  public JobHistory createNewHistory()
  {
    ImportHistory history = new ImportHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.addStage(ImportStage.VALIDATE);
    history.setWorkProgress(0L);
    history.setCompletedRowsJson("");
    history.setImportedRecords(0L);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.IMPORT_JOB_CHANGE, null));

    return history;
  }

  public RDFExport getExportConfig() throws JsonMappingException, JsonProcessingException
  {
    ObjectMapper mapper = new ObjectMapper();

    RDFExport config = new RDFExport();
    config.setGeomExportType(GeometryExportType.valueOf(getGeometryExportType()));
    config.setTypeCodes(mapper.readerForListOf(String.class).readValue(getGotCodes()));
    config.setBusinessTypeCodes(mapper.readerForListOf(String.class).readValue(getBusinessTypeCodes()));
    config.setGraphTypes(mapper.readerForListOf(GraphTypeReference.class).readValue(getGraphTypeCodes()));
    config.setBusinessEdgeCodes(mapper.readerForListOf(String.class).readValue(getBusinessEdgeCodes()));
    config.setNamespace(this.getNamespace());
    config.setValidFor(this.getValidFor());

    return config;
  }

  public static ImportHistory runNewJob(RDFExport config)
  {
    ObjectMapper mapper = new ObjectMapper();

    try
    {
      RepoRDFExportJob job = new RepoRDFExportJob();
      job.setNamespace(config.getNamespace());
      job.setGeometryExportType(config.getGeomExportType().name());
      job.setValidFor(config.getValidFor());
      job.setGotCodes(mapper.writeValueAsString(config.getTypeCodes()));
      job.setGraphTypeCodes(mapper.writeValueAsString(config.getGraphTypes()));
      job.setBusinessTypeCodes(mapper.writeValueAsString(config.getBusinessTypeCodes()));
      job.setBusinessEdgeCodes(mapper.writeValueAsString(config.getBusinessEdgeCodes()));
      job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.apply();

      return (ImportHistory) job.start();
    }
    catch (JsonProcessingException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

}
