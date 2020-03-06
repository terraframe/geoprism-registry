package net.geoprism.registry.etl;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;

import net.geoprism.DefaultConfiguration;
import net.geoprism.GeoprismUser;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.controller.GeoObjectEditorController;
import net.geoprism.registry.etl.ImportError.Resolution;

public class ETLService
{
  @Request(RequestType.SESSION)
  public void cancelImport(String sessionId, String json)
  {
    ImportConfiguration config = ImportConfiguration.build(json);

    String id = config.getVaultFileId();
    
    VaultFile.get(id).delete();
  }
  
  @Request(RequestType.SESSION)
  public JSONObject doImport(String sessionId, String json)
  {
    ImportConfiguration config = ImportConfiguration.build(json);

    ImportHistory hist;
    
    if (config.getHistoryId() != null && config.getHistoryId().length() > 0)
    {
      String historyId = config.getHistoryId();
      hist = ImportHistory.get(historyId);
      
      JobHistoryRecord record = hist.getAllJobRel().getAll().get(0);
      ExecutableJob execJob = record.getParent();

      execJob.resume(record);
    }
    else
    {
      DataImportJob job = new DataImportJob();
      job.setRunAsUserId(Session.getCurrentSession().getUser().getOid());
      job.apply();
     
      hist = job.start(config);
    }
    
    return new JSONObject(hist.getConfigJson());
  }
  
  @Request(RequestType.SESSION)
  public JSONArray getActiveImports(String sessionId, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    JSONArray ja = new JSONArray();
    
    QueryFactory qf = new QueryFactory();
    ImportHistoryQuery ihq = new ImportHistoryQuery(qf);
    ihq.WHERE(ihq.getStatus().containsExactly(AllJobStatus.RUNNING).OR(ihq.getStatus().containsExactly(AllJobStatus.NEW)).OR(ihq.getStatus().containsExactly(AllJobStatus.QUEUED)).OR(ihq.getStatus().containsExactly(AllJobStatus.FEEDBACK)));
    
    ihq.restrictRows(pageSize, pageNumber);
    ihq.ORDER_BY(ihq.get(sortAttr), isAscending ? SortOrder.ASC : SortOrder.DESC);
    
    OIterator<? extends ImportHistory> it = ihq.getIterator();
    
    while (it.hasNext())
    {
      ImportHistory hist = it.next();
      DataImportJob job = (DataImportJob) hist.getAllJob().getAll().get(0);
      
      GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());
      
      ja.put(serializeHistory(hist, user));
    }
    
    return ja;
  }
  
  @Request(RequestType.SESSION)
  public JSONArray getCompletedImports(String sessionId, int pageSize, int pageNumber, String sortAttr, boolean isAscending)
  {
    JSONArray ja = new JSONArray();
    
    QueryFactory qf = new QueryFactory();
    ImportHistoryQuery ihq = new ImportHistoryQuery(qf);
    ihq.WHERE(ihq.getStatus().containsExactly(AllJobStatus.SUCCESS).OR(ihq.getStatus().containsExactly(AllJobStatus.FAILURE)));
    
    ihq.restrictRows(pageSize, pageNumber);
    ihq.ORDER_BY(ihq.get(sortAttr), isAscending ? SortOrder.ASC : SortOrder.DESC);
    
    OIterator<? extends ImportHistory> it = ihq.getIterator();
    
    while (it.hasNext())
    {
      ImportHistory hist = it.next();
      DataImportJob job = (DataImportJob) hist.getAllJob().getAll().get(0);
      
      GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());
      
      ja.put(serializeHistory(hist, user));
    }
    
    return ja;
  }

  protected JSONObject serializeHistory(ImportHistory hist, GeoprismUser user)
  {
    JSONObject jo = new JSONObject();
    
    jo.put("fileName", hist.getImportFile().getFileName());
    jo.put("stage", hist.getStage().get(0).name());
    jo.put("status", hist.getStatus().get(0).name());
    jo.put("author", user.getUsername());
    jo.put("createDate", hist.getCreateDate());
    jo.put("lastUpdateDate", hist.getLastUpdateDate());
    jo.put("importedRecords", hist.getImportedRecords());
    jo.put("workProgress", hist.getWorkProgress());
    jo.put("workTotal", hist.getWorkTotal());
    jo.put("historyId", hist.getOid());
    
    if (hist.getStatus().get(0).equals(AllJobStatus.FAILURE) && hist.getErrorJson().length() > 0)
    {
      JSONObject exception = new JSONObject();
      
      exception.put("type", new JSONObject(hist.getErrorJson()).get("type"));
      exception.put("message", hist.getLocalizedError(Session.getCurrentLocale()));
      
      jo.put("exception", exception);
    }
    
    return jo;
  }
  
  @Request(RequestType.SESSION)
  public JSONArray getImportErrors(String sessionId, String historyId, int pageSize, int pageNumber)
  {
    JSONArray ja = new JSONArray();
    
    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());
    
    query.WHERE(query.getHistory().EQ(historyId));

    query.restrictRows(pageSize, pageNumber);
    
    OIterator<? extends ImportError> it = query.getIterator();
    while (it.hasNext())
    {
      ImportError err = it.next();
      
      ja.put(serializeImportError(err));
    }
    
    return ja;
  }
  
  @Request(RequestType.SESSION)
  public JSONArray getValidationProblems(String sessionId, String historyId, int pageSize, int pageNumber)
  {
    JSONArray paginatedValidationProblems = new JSONArray();
    
    ImportHistory hist = ImportHistory.get(historyId);
    
    JSONArray allValidationProblems = new JSONArray(hist.getValidationProblems());
    
    for (int recordNum = 1; recordNum <= pageSize; ++recordNum)
    {
      int index = recordNum-1 + ((pageNumber-1)*pageSize);
      
      if (index < allValidationProblems.length())
      {
        paginatedValidationProblems.put(allValidationProblems.get(index));
      }
      else
      {
        break;
      }
    }
    
    return paginatedValidationProblems;
  }
  
  @Request(RequestType.SESSION)
  public JSONObject getImportDetails(String sessionId, String historyId, int pageSize, int pageNumber)
  {
    ImportHistory hist = ImportHistory.get(historyId);
    DataImportJob job = (DataImportJob) hist.getAllJob().getAll().get(0);
    GeoprismUser user = GeoprismUser.get(job.getRunAsUser().getOid());
    
    JSONObject jo = this.serializeHistory(hist, user);
    
    JSONObject errors = new JSONObject();
    
    errors.put("pageSize", pageSize);
    errors.put("pageNumber", pageNumber);
    
    if (hist.getStage().get(0).equals(ImportStage.IMPORT_RESOLVE) && hist.hasImportErrors())
    {
      errors.put("type", "ImportErrors");
      
      errors.put("page", this.getImportErrors(sessionId, historyId, pageSize, pageNumber));
      
      jo.put("errors", errors);
    }
    else if (hist.getStage().get(0).equals(ImportStage.VALIDATION_RESOLVE) && hist.getValidationProblems().length() > 0)
    {
      errors.put("type", "ValidationErrors");
      
      errors.put("page", this.getValidationProblems(sessionId, historyId, pageSize, pageNumber));
      
      jo.put("errors", errors);
    }
    
    return jo;
  }
  
  protected JSONObject serializeImportError(ImportError err)
  {
    JSONObject jo = new JSONObject();
    
    JSONObject exception = new JSONObject();
    exception.put("type", new JSONObject(err.getErrorJson()).get("type"));
    exception.put("message", JobHistory.readLocalizedException(new JSONObject(err.getErrorJson()), Session.getCurrentLocale()));
    jo.put("exception", exception);
    
    if (err.getObjectJson() != null && err.getObjectJson().length() > 0)
    {
      jo.put("object", new JSONObject(err.getObjectJson()));
    }
    
    jo.put("objectType", err.getObjectType());
    
    jo.put("importErrorId", err.getOid());
    
    jo.put("resolution", err.getResolution());
    
    return jo;
  }
  
  private void checkPermissions()
  {
    Map<String, String> roles = Session.getCurrentSession().getUserRoles();
    if (! (roles.keySet().contains(DefaultConfiguration.ADMIN)
        || roles.keySet().contains(RegistryConstants.REGISTRY_MAINTAINER_ROLE)
        || roles.keySet().contains(RegistryConstants.REGISTRY_ADMIN_ROLE)
      ))
    {
      throw new ProgrammingErrorException("You don't have permissions to access this endpoint.");
    }
  }

  @Request(RequestType.SESSION)
  public void submitImportErrorResolution(String sessionId, String json)
  {
    submitImportErrorResolutionInTrans(sessionId, json);
  }

  @Transaction
  private void submitImportErrorResolutionInTrans(String sessionId, String json)
  {
    checkPermissions();
    
    JSONObject config = new JSONObject(json);
    
    ImportHistory hist = ImportHistory.get(config.getString("historyId"));
    
    ImportError err = ImportError.get(config.getString("importErrorId"));
    
    String resolution = config.getString("resolution");
    
    if (resolution.equals(Resolution.APPLY_GEO_OBJECT.name()))
    {
      String parentTreeNode = config.getString("parentTreeNode");
      String geoObject = config.getString("geoObject");
      Boolean isNew = config.getBoolean("isNew");
      
      new GeoObjectEditorController().applyInReq(sessionId, parentTreeNode, geoObject, isNew, null, null);
      
      err.appLock();
      err.setResolution(resolution);
      err.apply();
      
      hist.appLock();
      hist.setErrorResolvedCount(hist.getErrorResolvedCount() + 1);
      hist.apply();
    }
    else if (resolution.equals(Resolution.IGNORE.name()))
    {
      err.appLock();
      err.setResolution(resolution);
      err.apply();
    }
    else
    {
      throw new UnsupportedOperationException("Invalid import resolution [" + resolution + "].");
    }
  }
  
  @Request(RequestType.SESSION)
  public void resolveImport(String sessionId, String historyId)
  {
    resolveImportInTrans(historyId);
  }
  
  @Transaction
  private void resolveImportInTrans(String historyId)
  {
    checkPermissions();
    
    ImportHistory hist = ImportHistory.get(historyId);
    hist.appLock();
    
    ImportErrorQuery ieq = new ImportErrorQuery(new QueryFactory());
    ieq.WHERE(ieq.getHistory().EQ(historyId));
    OIterator<? extends ImportError> it = ieq.getIterator();
    try
    {
      ImportError err = it.next();
      
      err.delete();
    }
    finally
    {
      it.close();
    }
    
    hist.clearStatus();
    hist.addStatus(AllJobStatus.SUCCESS);
    
    hist.clearStage();
    hist.addStage(ImportStage.COMPLETE);
    
    hist.apply();
  }
}
