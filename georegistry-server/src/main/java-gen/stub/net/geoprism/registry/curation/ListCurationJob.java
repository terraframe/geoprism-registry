/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.curation;

import java.util.Date;
import java.util.List;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.scheduler.AllJobStatus;
import com.runwaysdk.system.scheduler.ExecutionContext;
import com.runwaysdk.system.scheduler.JobHistory;
import com.runwaysdk.system.scheduler.JobHistoryRecord;
import com.runwaysdk.system.scheduler.QuartzRunwayJob;
import com.runwaysdk.system.scheduler.QueueingQuartzJob;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.service.business.ServiceFactory;
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
    final ServerGeoObjectType type = listType.getServerGeoObjectType();
    final Organization org = listType.getOrganization();

    RolePermissionService permissions = ServiceFactory.getBean(RolePermissionService.class);

    if (permissions.isRA())
    {
      permissions.enforceRA(org.getCode());
    }
    else if (permissions.isRM())
    {
      permissions.enforceRM(org.getCode(), type);
    }
    else
    {
      permissions.enforceRM();
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
    try
    {
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

      ListCurationHistory history = (ListCurationHistory) executionContext.getHistory();

      ListTypeVersion version = history.getVersion();

      deleteOtherHistories(history, version);

      new ListCurator(history, version).run();
    }
    finally
    {
      Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }
  }

  @Override
  protected JobHistory createNewHistory()
  {
    ListCurationHistory history = new ListCurationHistory();
    history.setStartTime(new Date());
    history.addStatus(AllJobStatus.NEW);
    history.apply();

    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.CURATION_JOB_CHANGE, null));

    return history;
  }

  @Request
  @Override
  public void afterJobExecute(JobHistory history)
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.CURATION_JOB_CHANGE, null));
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

  /**
   * Since only one history is allowed to exist for each ListTypeVersion, we can
   * delete the other ones which are no longer relevant.
   * 
   * @param version
   */
  private void deleteOtherHistories(ListCurationHistory history, ListTypeVersion version)
  {
    ListCurationHistoryQuery query = new ListCurationHistoryQuery(new QueryFactory());

    query.WHERE(query.getVersion().EQ(version));

    try (OIterator<? extends ListCurationHistory> it = query.getIterator())
    {
      for (ListCurationHistory loopHist : it)
      {
        if (!loopHist.getOid().equals(history.getOid()))
        {
          loopHist.delete();
        }
      }
    }
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
