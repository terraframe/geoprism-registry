package net.geoprism.graph;

import java.io.File;
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

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonObject;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;

import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.ImportStage;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.LabeledPropertyGraphRDFExportBusinessServiceIF.GeometryExportType;
import net.geoprism.registry.service.business.RepoRDFExportBusinessService;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class RepoRDFExportJob extends RepoRDFExportJobBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 755269485;
  
  public static final String ARRAY_STORAGE_CONCAT_TOKEN = "___SPLIT___";
  
  public RepoRDFExportJob()
  {
    super();
  }
  
  public static ImportHistory runNewJob(GraphTypeReference[] graphTypes, String[] gotCodes, GeometryExportType geomExportType) {
    
    var job = new RepoRDFExportJob();
    job.setGraphTypeRefs(graphTypes);
    job.setGotCodes(String.join(ARRAY_STORAGE_CONCAT_TOKEN, gotCodes));
    job.setGeometryExportType(geomExportType.name());
    job.apply();
    
    return (ImportHistory) job.start();
  }
  
  public void setGraphTypeRefs(GraphTypeReference[] graphTypes) {
    List<String> codePairs = new ArrayList<String>();
    
    for (var ref : graphTypes) {
      codePairs.add(ref.typeCode);
      codePairs.add(ref.code);
    }
    
    this.setGraphTypeCodes(String.join(ARRAY_STORAGE_CONCAT_TOKEN, codePairs));
  }
  
  public List<GraphTypeReference> getGraphTypeRefs()
  {
    List<String> codePairs = Arrays.asList(getGraphTypeCodes().split(ARRAY_STORAGE_CONCAT_TOKEN));
    
    List<GraphTypeReference> result = new ArrayList<GraphTypeReference>();
    for (int i = 0; i < codePairs.size(); i=i+2) {
      result.add(new GraphTypeReference(codePairs.get(i), codePairs.get(i+1)));
    }
    
    return result;
  }

  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    ImportHistory history = (ImportHistory) executionContext.getJobHistoryRecord().getChild();
    
    var codes = new ArrayList<String>(Arrays.asList(getGotCodes().split(ARRAY_STORAGE_CONCAT_TOKEN)));
    codes.addAll(getGraphTypeRefs().stream().map(ref -> ref.code).toList());
    var it = codes.iterator();
    String codeDigest = it.next();
    while (codeDigest.length() < 100 && it.hasNext()) {
      codeDigest += ", " + it.next();
    }
    
    history.appLock();
    JsonObject jo = new JsonObject();
    jo.addProperty(ImportConfiguration.FILE_NAME, "Export RDF from Repo (" + codeDigest + ")");
    jo.addProperty(ImportConfiguration.OBJECT_TYPE, "RDF");
    history.setConfigJson(jo.toString());
    history.apply();
    
    var progressId = history.getOid();
    var gots = Arrays.asList(getGotCodes().split(ARRAY_STORAGE_CONCAT_TOKEN)).stream().map(c -> ServerGeoObjectType.get(c)).toList();
    var graphTypes = getGraphTypeRefs().stream().map(ref -> GraphType.resolve(ref)).toList();
    var geomExportType = GeometryExportType.valueOf(getGeometryExportType());
    
    var exporter = ServiceFactory.getBean(RepoRDFExportBusinessService.class);
    
    File tmp = Files.createTempFile(progressId, ".ttl").toFile();
    
    try (OutputStream os = new FileOutputStream(tmp)) {
      try (ZipOutputStream zos = new ZipOutputStream(os)) {
        ZipEntry entry = new ZipEntry(progressId + ".ttl");
        zos.putNextEntry(entry);
        
        exporter.export(progressId, graphTypes, gots, geomExportType, zos);
        os.flush();
        
        try (FileInputStream is = new FileInputStream(tmp)) {
          VaultFile.createAndApply(progressId + ".zip", is);
        }
      }
    } finally {
      FileUtils.deleteQuietly(tmp);
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
  
}
