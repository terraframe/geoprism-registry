package net.geoprism.registry.task;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.RolesQuery;

public class TaskService
{
  @Request(RequestType.SESSION)
  public static JSONArray getTasksForCurrentUser(String sessionId)
  {
    QueryFactory qf = new QueryFactory();
    
    ValueQuery vq = new ValueQuery(qf);
    
    TaskHasRoleQuery thrq = new TaskHasRoleQuery(qf);
    
    TaskQuery tq = new TaskQuery(qf);
    vq.WHERE(thrq.getParent().EQ(tq));
    
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
    
    
    JSONArray ja = new JSONArray();
    
    OIterator<ValueObject> it = vq.getIterator();
    
    while (it.hasNext())
    {
      ValueObject vo = it.next();
      
      JSONObject jo = new JSONObject();
      jo.put("id", vo.getValue("oid"));
      jo.put("templateKey", vo.getValue("templateKey"));
      jo.put("msg", vo.getValue("msg"));
      
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
    Task t = Task.get(id);
    t.delete();
  }
}
