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
package net.geoprism.registry.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.constants.MdAttributeDateTimeUtil;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.localization.LocalizedValueStoreQuery;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.RolesQuery;

import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.task.Task;
import net.geoprism.registry.task.Task.TaskStatus;
import net.geoprism.registry.task.TaskHasRoleQuery;
import net.geoprism.registry.task.TaskQuery;
import net.geoprism.registry.view.JsonWrapper;
import net.geoprism.registry.view.Page;

@Repository
public class TaskService
{
  public JsonObject getTasksForCurrentUser(String sessionId)
  {
    return this.getTasksForCurrentUser(sessionId, "createDate", 1, Integer.MAX_VALUE, null);
  }

  @Request(RequestType.SESSION)
  public JsonObject getTasksForCurrentUser(String sessionId, String orderBy, int pageNum, int pageSize, String whereStatus)
  {
    QueryFactory qf = new QueryFactory();

    ValueQuery vq = new ValueQuery(qf);

    TaskHasRoleQuery thrq = new TaskHasRoleQuery(vq);

    TaskQuery tq = new TaskQuery(vq);
    vq.WHERE(thrq.getParent().EQ(tq));

    if (whereStatus != null)
    {
      vq.WHERE(tq.getStatus().EQ(whereStatus));
    }

    RolesQuery rq = new RolesQuery(vq);
    vq.WHERE(thrq.getChild().EQ(rq));

    Condition cond = null;
    // Map<String, String> roles = Session.getCurrentSession().getUserRoles();
    Set<RoleDAOIF> roles = Session.getCurrentSession().getUser().assignedRoles();

    // for (String roleName : roles.keySet())
    for (RoleDAOIF role : roles)
    {
      String roleName = role.getRoleName();

      if (roleName.equals(RoleConstants.ADMIN))
      {
        continue;
      }

      if (cond == null)
      {
        cond = rq.getRoleName().EQ(roleName);
      }
      else
      {
        cond = cond.OR(rq.getRoleName().EQ(roleName));
      }
    }

    vq.WHERE(cond);

    LocalizedValueStoreQuery lvsqTemplate = new LocalizedValueStoreQuery(vq);
    vq.WHERE(tq.getTemplate().EQ(lvsqTemplate));

    LocalizedValueStoreQuery lvsqTitle = new LocalizedValueStoreQuery(vq);
    vq.WHERE(tq.getTitle().EQ(lvsqTitle));

    vq.SELECT(tq.getOid("oid"));
    vq.SELECT(lvsqTemplate.getStoreKey("templateKey"));
    vq.SELECT(tq.getMessage().localize("msg"));
    vq.SELECT(lvsqTitle.getStoreValue().localize("title"));
    vq.SELECT(tq.getStatus("status"));
    vq.SELECT(tq.getCreateDate("createDate"));
    vq.SELECT(tq.getLastUpdateDate("completedDate"));

    vq.ORDER_BY(tq.get(orderBy), SortOrder.DESC);
    vq.restrictRows(pageSize, pageNum);

    try (OIterator<ValueObject> it = vq.getIterator())
    {
      List<JsonWrapper> results = it.getAll().stream().map(vo -> {
        JsonObject jo = new JsonObject();
        jo.addProperty("id", vo.getValue("oid"));
        jo.addProperty("templateKey", vo.getValue("templateKey"));
        jo.addProperty("msg", vo.getValue("msg"));
        jo.addProperty("title", vo.getValue("title"));
        jo.addProperty("status", vo.getValue("status"));
        jo.addProperty("createDate", ETLService.formatDate(MdAttributeDateTimeUtil.getTypeSafeValue(vo.getValue("createDate"))));
        jo.addProperty("completedDate", vo.getValue("status").equals(TaskStatus.RESOLVED.name()) ? ETLService.formatDate(MdAttributeDateTimeUtil.getTypeSafeValue(vo.getValue("completedDate"))) : null);

        return new JsonWrapper(jo);
      }).collect(Collectors.toList());

      return new Page<JsonWrapper>(vq.getCount(), pageNum, pageSize, results).toJSON();
    }
  }

  @Request(RequestType.SESSION)
  public void deleteTask(String sessionId, String taskId)
  {
    Task t = Task.get(taskId);
    t.delete();
  }

  @Request(RequestType.SESSION)
  public void completeTask(String sessionId, String id)
  {
    Task t = Task.get(id);
    t.appLock();
    t.setStatus(TaskStatus.RESOLVED.name());
    t.apply();
  }

  @Request(RequestType.SESSION)
  public void setTaskStatus(String sessionId, String id, String status)
  {
    if (! ( status.equals(TaskStatus.RESOLVED.name()) || status.equals(TaskStatus.UNRESOLVED.name()) ))
    {
      throw new ProgrammingErrorException("Invalid task status [" + status + "].");
    }

    Task t = Task.get(id);
    t.appLock();
    t.setStatus(status);
    t.apply();
  }
}
