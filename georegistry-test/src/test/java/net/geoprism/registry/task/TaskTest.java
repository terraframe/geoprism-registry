package net.geoprism.registry.task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.ClientSession;
import com.runwaysdk.LocalizationFacade;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.localization.LocalizedValueStore;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.system.Roles;

import junit.framework.Assert;
import net.geoprism.DefaultConfiguration;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.task.Task.TaskStatus;
import net.geoprism.registry.task.Task.TaskType;
import net.geoprism.registry.test.TestDataSet;

public class TaskTest
{
  public ClientSession chineseSession = null;
  
  public ClientSession koreanSession = null;
  
  public ClientSession canadaSession = null;
  
  public ClientSession italianSession = null;
  
  public static void main(String[] args)
  {
    mainInReq();
  }
  @Request
  public static void mainInReq()
  {
    final List<Roles> roles = new ArrayList<Roles>();
    roles.add(Roles.findRoleByName(DefaultConfiguration.ADMIN));
    
    Map<String, LocalizedValue> values = new HashMap<String, LocalizedValue>();
    
    LocalizedValue lvTypeName = new LocalizedValue(null);
    lvTypeName.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "District");
    values.put("typeName", lvTypeName);
    
    LocalizedValue lvOldParentName = new LocalizedValue("");
    lvOldParentName.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "D1");
    values.put("oldParentName", lvOldParentName);
    
    LocalizedValue lvNewParentName1 = new LocalizedValue("");
    lvNewParentName1.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "D2");
    values.put("newParentName1", lvNewParentName1);
    
    LocalizedValue lvNewParentName2 = new LocalizedValue("");
    lvNewParentName2.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "D3");
    values.put("newParentName2", lvNewParentName2);
    
    LocalizedValue lvSplitDate = new LocalizedValue("");
    lvSplitDate.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "2012-11-15");
    values.put("splitDate", lvSplitDate);
    
    LocalizedValue lvChildTypeName = new LocalizedValue("");
    lvChildTypeName.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "Health Facility");
    values.put("childTypeName", lvChildTypeName);
    
    Task.createNewTask(roles, TaskType.GeoObjectSplitOrphanedChildren, values);
  }
  
  @BeforeClass
  @Request
  public static void setUpClass()
  {
    deleteAllTasks();
    
    List<Locale> installed = LocalizationFacade.getInstalledLocales();
    
    if (!installed.contains(Locale.CHINESE))
    {
      LocalizationFacade.install(Locale.CHINESE);
    }
    
    if (!installed.contains(Locale.KOREAN))
    {
      LocalizationFacade.install(Locale.KOREAN);
    }
    
    if (!installed.contains(Locale.CANADA))
    {
      LocalizationFacade.install(Locale.CANADA);
    }
  }
  
  @AfterClass
  @Request
  public static void tearDownClass()
  {
    deleteAllTasks();
    
    LocalizationFacade.uninstall(Locale.CHINESE);
    LocalizationFacade.uninstall(Locale.KOREAN);
    LocalizationFacade.uninstall(Locale.CANADA);
  }
  
  @Request
  private static void deleteAllTasks()
  {
    TaskHasRoleQuery tq = new TaskHasRoleQuery(new QueryFactory());
    
    OIterator<? extends TaskHasRole> it = tq.getIterator();
    
    while (it.hasNext())
    {
      TaskHasRole thr = it.next();
      
      String taskId = thr.getParent().getOid();
      
      thr.delete();
      
      Task.get(taskId).delete();
    }
  }
  
  @Before
  public void setUp()
  {
    chineseSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { Locale.CHINESE });
    koreanSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { Locale.KOREAN });
    canadaSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { Locale.CANADA });
    italianSession = ClientSession.createUserSession(TestDataSet.ADMIN_USER_NAME, TestDataSet.ADMIN_PASSWORD, new Locale[] { Locale.ITALIAN });
  }

  @After
  public void tearDown()
  {
    if (chineseSession != null)
    {
      chineseSession.logout();
    }
    
    if (koreanSession != null)
    {
      koreanSession.logout();
    }
    
    if (canadaSession != null)
    {
      canadaSession.logout();
    }
    
    if (italianSession != null)
    {
      italianSession.logout();
    }
  }
  
  private Date parseDate(String sDate)
  {
    SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    
    try
    {
      return format.parse(sDate);
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  @Test
  public void testCreateAndFetchTasks()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());

    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 1);
    Date dateMin = cal.getTime();
    
    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 2);
    Date dateMax = cal.getTime();
    
    createInstanceData(chineseSession.getSessionId());
    
    JSONObject chinaJO = TaskService.getTasksForCurrentUser(chineseSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    Assert.assertEquals(TaskType.GeoObjectSplitOrphanedChildren.getTemplateKey(), chinaJO.getString("templateKey"));
    Assert.assertNotNull(chinaJO.getString("id"));
    Assert.assertEquals(TaskStatus.UNRESOLVED.name(), chinaJO.getString("status"));
//    Assert.assertTrue(dateMin.before(parseDate(chinaJO.getString("createDate"))));
//    Assert.assertTrue(dateMax.after(parseDate(chinaJO.getString("createDate"))));
    Assert.assertTrue(chinaJO.isNull("completedDate"));
    Assert.assertEquals(
        "区 D1 Chinese已拆分。 您必须将孩子重新分配给新父母。",
        chinaJO.getString("msg")
    );
    Assert.assertEquals(LocalizedValueStore.getByKey(TaskType.GeoObjectSplitOrphanedChildren.getTitleKey()).getStoreValue().getValue(Locale.CHINESE), chinaJO.getString("title"));
    
    System.out.println(chinaJO);
    
    JSONObject koreaJO = TaskService.getTasksForCurrentUser(koreanSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    Assert.assertEquals(TaskType.GeoObjectSplitOrphanedChildren.getTemplateKey(), koreaJO.getString("templateKey"));
    Assert.assertNotNull(koreaJO.getString("id"));
    Assert.assertEquals(TaskStatus.UNRESOLVED.name(), koreaJO.getString("status"));
//    Assert.assertTrue(dateMin.before(parseDate(koreaJO.getString("createDate"))));
//    Assert.assertTrue(dateMax.after(parseDate(koreaJO.getString("createDate"))));
    Assert.assertTrue(koreaJO.isNull("completedDate"));
    Assert.assertEquals(
        "지구 D1 Korean이 (가) 분할되었습니다. 새 부모에게 자녀를 재 할당해야합니다.",
        koreaJO.getString("msg")
    );
    Assert.assertEquals(LocalizedValueStore.getByKey(TaskType.GeoObjectSplitOrphanedChildren.getTitleKey()).getStoreValue().getValue(Locale.KOREAN), koreaJO.getString("title"));
    
    JSONObject canadaJO = TaskService.getTasksForCurrentUser(canadaSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    Assert.assertEquals(TaskType.GeoObjectSplitOrphanedChildren.getTemplateKey(), canadaJO.getString("templateKey"));
    Assert.assertNotNull(canadaJO.getString("id"));
    Assert.assertEquals(TaskStatus.UNRESOLVED.name(), canadaJO.getString("status"));
//    Assert.assertTrue(dateMin.before(parseDate(canadaJO.getString("createDate"))));
//    Assert.assertTrue(dateMax.after(parseDate(canadaJO.getString("createDate"))));
    Assert.assertTrue(canadaJO.isNull("completedDate"));
    Assert.assertEquals(
        "Oh no! The district eh D1 Canada has split. You must reassign the children with new parents eh.",
        canadaJO.getString("msg")
    );
    Assert.assertEquals(LocalizedValueStore.getByKey(TaskType.GeoObjectSplitOrphanedChildren.getTitleKey()).getStoreValue().getValue(Locale.CANADA), canadaJO.getString("title"));
    
    JSONObject italianJO = TaskService.getTasksForCurrentUser(italianSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    Assert.assertEquals(TaskType.GeoObjectSplitOrphanedChildren.getTemplateKey(), italianJO.getString("templateKey"));
    Assert.assertNotNull(italianJO.getString("id"));
    Assert.assertEquals(TaskStatus.UNRESOLVED.name(), italianJO.getString("status"));
//    Assert.assertTrue(dateMin.before(parseDate(italianJO.getString("createDate"))));
//    Assert.assertTrue(dateMax.after(parseDate(italianJO.getString("createDate"))));
    Assert.assertTrue(italianJO.isNull("completedDate"));
    Assert.assertEquals(
        "The district D1 has split. You must reassign the children with new parents.",
        italianJO.getString("msg")
    );
    Assert.assertEquals(LocalizedValueStore.getByKey(TaskType.GeoObjectSplitOrphanedChildren.getTitleKey()).getStoreValue().getValue(MdAttributeLocalInfo.DEFAULT_LOCALE), italianJO.getString("title"));
  }
  
  @Test
  @Request
  public void testCompleteTask()
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());

    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 1);
    Date dateMin = cal.getTime();
    
    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 2);
    Date dateMax = cal.getTime();
    
    createInstanceData(chineseSession.getSessionId());
    
    JSONObject chinaJO = TaskService.getTasksForCurrentUser(chineseSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    Assert.assertTrue(chinaJO.isNull("completedDate"));
    Assert.assertEquals(TaskStatus.UNRESOLVED.name(), chinaJO.getString("status"));
    
    TaskService.completeTask(chineseSession.getSessionId(), chinaJO.getString("id"));
    JSONObject chinaJO2 = TaskService.getTasksForCurrentUser(chineseSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    
    Task t = Task.get(chinaJO2.getString("id"));
    Assert.assertEquals(TaskStatus.RESOLVED.name(), t.getStatus());
    
//    Assert.assertTrue(dateMin.before(parseDate(chinaJO2.getString("completedDate"))));
//    Assert.assertTrue(dateMax.after(parseDate(chinaJO2.getString("completedDate"))));
  }
  
  @Test
  @Request
  public void testDeleteTask()
  {
    createInstanceData(chineseSession.getSessionId());
    
    JSONObject chinaJO = TaskService.getTasksForCurrentUser(chineseSession.getSessionId()).getJSONArray("results").getJSONObject(0);
    
    TaskService.deleteTask(chineseSession.getSessionId(), chinaJO.getString("id"));
    
    try
    {
      Task.get(chinaJO.getString("id"));
      Assert.fail();
    }
    catch (DataNotFoundException e)
    {
      
    }
  }
  
  @Request(RequestType.SESSION)
  private static void createInstanceData(String sessionId)
  {
    final List<Roles> roles = new ArrayList<Roles>();
    roles.add(Roles.findRoleByName(DefaultConfiguration.ADMIN));
    
    
    LocalizedValueStore lv = LocalizedValueStore.getByKey(TaskType.GeoObjectSplitOrphanedChildren.getTitleKey());
    lv.lock();
    lv.setStructValue(LocalizedValueStore.STOREVALUE, MdAttributeLocalInfo.DEFAULT_LOCALE, "Split Has Orphaned Children");
    lv.setStructValue(LocalizedValueStore.STOREVALUE, Locale.CHINESE.toString(), "斯普利特有孤儿");
    lv.setStructValue(LocalizedValueStore.STOREVALUE, Locale.KOREAN.toString(), "스 플리트는 고아를 낳았다");
    lv.setStructValue(LocalizedValueStore.STOREVALUE, Locale.CANADA.toString(), "Oh no! Split Has Orphaned Children eh.");
    lv.apply();
    
    LocalizedValueStore lv2 = LocalizedValueStore.getByKey(TaskType.GeoObjectSplitOrphanedChildren.getTemplateKey());
    lv2.lock();
    lv2.setStructValue(LocalizedValueStore.STOREVALUE, MdAttributeLocalInfo.DEFAULT_LOCALE, "The {typeName} {oldParentName} has split. You must reassign the children with new parents.");
    lv2.setStructValue(LocalizedValueStore.STOREVALUE, Locale.CHINESE.toString(), "{typeName} {oldParentName}已拆分。 您必须将孩子重新分配给新父母。");
    lv2.setStructValue(LocalizedValueStore.STOREVALUE, Locale.KOREAN.toString(), "{typeName} {oldParentName}이 (가) 분할되었습니다. 새 부모에게 자녀를 재 할당해야합니다.");
    lv2.setStructValue(LocalizedValueStore.STOREVALUE, Locale.CANADA.toString(), "Oh no! The {typeName} {oldParentName} has split. You must reassign the children with new parents eh.");
    lv2.apply();
    
    
    Map<String, LocalizedValue> values = new HashMap<String, LocalizedValue>();
    
    LocalizedValue lvTypeName = new LocalizedValue(null);
    lvTypeName.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "district");
    lvTypeName.setValue(Locale.CHINESE, "区");
    lvTypeName.setValue(Locale.KOREAN, "지구");
    lvTypeName.setValue(Locale.CANADA, "district eh");
    values.put("typeName", lvTypeName);
    
    LocalizedValue lvOldParentName = new LocalizedValue(null);
    lvOldParentName.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, "D1");
    lvOldParentName.setValue(Locale.CHINESE, "D1 Chinese");
    lvOldParentName.setValue(Locale.KOREAN, "D1 Korean");
    lvOldParentName.setValue(Locale.CANADA, "D1 Canada");
    values.put("oldParentName", lvOldParentName);
    
    Task.createNewTask(roles, TaskType.GeoObjectSplitOrphanedChildren, values);
  }
}
