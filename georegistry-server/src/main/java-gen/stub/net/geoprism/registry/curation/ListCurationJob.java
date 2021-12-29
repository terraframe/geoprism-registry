package net.geoprism.registry.curation;

import java.util.Date;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.GeoprismUser;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.etl.ImportHistory;
import net.geoprism.registry.etl.export.ExportHistory;
import net.geoprism.registry.etl.export.ExportStage;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class ListCurationJob extends ListCurationJobBase
{
  private static final long serialVersionUID = -1676131772;
  
  public ListCurationJob()
  {
    super();
  }
  
  @Override
  public synchronized JobHistory start()
  {
    throw new UnsupportedOperationException();
  }

  public synchronized ListCurationHistory start(ListTypeVersion version)
  {
    return executableJobStart(version);
  }

  private ListCurationHistory executableJobStart(ListTypeVersion version)
  {
    JobHistoryRecord record = startInTrans(version);

    this.getQuartzJob().start(record);

    return (ListCurationHistory) record.getChild();
  }

  @Transaction
  private JobHistoryRecord startInTrans(ListTypeVersion version)
  {
    final ListType listType = version.getListType();
    final ServerGeoObjectType type = listType.getGeoObjectType();
    final Organization org = listType.getOrganization();
    
    RolePermissionService perms = ServiceFactory.getRolePermissionService();
    if (perms.isRA())
    {
      perms.enforceRA(org.getCode());
    }
    else if (perms.isRM())
    {
      perms.enforceRM(org.getCode(), type);
    }
    else
    {
      perms.enforceRM();
    }

    ListCurationHistory history = (ListCurationHistory) this.createNewHistory();

    history.appLock();
    history.setVersion(version);
    history.apply();

    JobHistoryRecord record = new JobHistoryRecord(this, history);
    record.apply();

    return record;
  }
  
  @Override
  public void execute(ExecutionContext executionContext) throws Throwable
  {
    ListCurationHistory history = (ListCurationHistory) executionContext.getHistory();
    
    ListTypeVersion version = history.getVersion();
    
    new ListCurator(history, version).run();
  }
  
  @Override
  protected JobHistory createNewHistory()
  {
    ListCurationHistory history = new ListCurationHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.apply();

//    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.DATA_EXPORT_JOB_CHANGE, null));

    return history;
  }
  
  @Override
  public boolean canResume(JobHistoryRecord jhr)
  {
    return false;
  }

  @Override
  protected QuartzRunwayJob createQuartzRunwayJob()
  {
    return new QueueingQuartzJob(this);
  }
  
  public static ListCurationHistory getMostRecent(String versionId)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());
    
    query.WHERE(query.getVersion().EQ(versionId));
    
    query.ORDER_BY_DESC(query.getCreateDate());
    
    List<? extends ListCurationHistory> histories = query.getIterator().getAll();
    
    if (histories.size() > 0)
    {
      return histories.get(0);
    }
    else
    {
      return null;
    }
  }
  
  public static ListCurationHistory getRunning(String versionId)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());
    
    query.WHERE(query.getVersion().EQ(versionId));
    
    query.AND(query.getStatus().containsExactly(AllJobStatus.RUNNING));
    
    List<? extends ListCurationHistory> histories = query.getIterator().getAll();
    
    if (histories.size() > 0)
    {
      return histories.get(0);
    }
    else
    {
      return null;
    }
  }
  
}
