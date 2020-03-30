package net.geoprism.registry.task;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.constants.MdAttributeDateTimeUtil;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.RolesQuery;

import net.geoprism.registry.task.Task.TaskStatus;

public class TaskService
{
  public static JSONArray getTasksForCurrentUser(String sessionId)
  {
    return TaskService.getTasksForCurrentUser(sessionId, "createDate", 1, Integer.MAX_VALUE, null);
  }
  
  @Request(RequestType.SESSION)
  public static JSONArray getTasksForCurrentUser(String sessionId, String orderBy, int pageNum, int pageSize, String whereStatus)
  {
    QueryFactory qf = new QueryFactory();
    
    ValueQuery vq = new ValueQuery(qf);
    
    TaskHasRoleQuery thrq = new TaskHasRoleQuery(qf);
    
    TaskQuery tq = new TaskQuery(qf);
    vq.WHERE(thrq.getParent().EQ(tq));
    
    if (whereStatus != null)
    {
      vq.WHERE(tq.getStatus().EQ(TaskStatus.RESOLVED.name()));
    }
    
    RolesQuery rq = new RolesQuery(qf);
    vq.WHERE(thrq.getChild().EQ(rq));
    
    
    Condition cond = null;
    Map<String, String> roles = Session.getCurrentSession().getUserRoles();
    
    for (String roleName : roles.keySet())
    {
      Roles role = Roles.findRoleByName(roleName);
      
      if (cond == null)
      {
        cond = rq.getRoleName().EQ(role.getRoleName());
      }
      else
      {
        cond = cond.OR(rq.getRoleName().EQ(role.getRoleName()));
      }
    }
    
    vq.WHERE(cond);
    
    
    vq.SELECT(tq.getOid("oid"));
    vq.SELECT(tq.getTemplate().getStoreKey("templateKey"));
    vq.SELECT(tq.getMessage().localize("msg"));
    vq.SELECT(tq.getStatus("status"));
    vq.SELECT(tq.getCreateDate("createDate"));
    vq.SELECT(tq.getLastUpdateDate("completedDate"));
    
    vq.ORDER_BY(tq.get(orderBy), SortOrder.DESC);
    vq.restrictRows(pageSize, pageNum);
    
    
    JSONArray ja = new JSONArray();
    
    OIterator<ValueObject> it = vq.getIterator();
    
    while (it.hasNext())
    {
      ValueObject vo = it.next();
      
      JSONObject jo = new JSONObject();
      jo.put("id", vo.getValue("oid"));
      jo.put("templateKey", vo.getValue("templateKey"));
      jo.put("msg", vo.getValue("msg"));
      jo.put("status", vo.getValue("status"));
      jo.put("createDate", MdAttributeDateTimeUtil.getTypeSafeValue(vo.getValue("createDate")).getTime());
      jo.put("completedDate", vo.getValue("status").equals(TaskStatus.RESOLVED.name()) ? MdAttributeDateTimeUtil.getTypeSafeValue(vo.getValue("completedDate")).getTime() : null);
      
      ja.put(jo);
    }
    
    return ja;
  }

  @Request(RequestType.SESSION)
  public static void deleteTask(String sessionId, String taskId)
  {
    Task t = Task.get(taskId);
    t.delete();
  }

  @Request(RequestType.SESSION)
  public static void completeTask(String sessionId, String id)
  {
    Task t = Task.lock(id);
    t.setStatus(TaskStatus.RESOLVED.name());
    t.apply();
  }
}
