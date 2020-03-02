package net.geoprism.registry.etl;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutableJob;
import com.runwaysdk.system.scheduler.JobHistoryRecord;

import net.geoprism.GeoprismUser;

public class ETLService
{
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
    ihq.WHERE(ihq.getStatus().containsExactly(AllJobStatus.RUNNING).OR(ihq.getStatus().containsExactly(AllJobStatus.NEW)).OR(ihq.getStatus().containsExactly(AllJobStatus.QUEUED)));
    
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
    
    jo.put("stage", hist.getStage().get(0));
    jo.put("status", hist.getStatus().get(0));
    jo.put("author", user.getUsername());
    jo.put("createDate", hist.getCreateDate());
    jo.put("lastUpdateDate", hist.getLastUpdateDate());
    jo.put("importedRecords", hist.getImportedRecords());
    jo.put("workProgress", hist.getWorkProgress());
    jo.put("workTotal", hist.getWorkTotal());
    
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
  
  protected JSONObject serializeImportError(ImportError err)
  {
    JSONObject jo = new JSONObject();
    
    jo.put("error", new JSONObject(err.getErrorJson()));
    
    if (err.getObjectJson() != null)
    {
      jo.put("object", new JSONObject(err.getObjectJson()));
    }
    
    jo.put("objectType", err.getObjectType());
    
    return jo;
  }
  
}
