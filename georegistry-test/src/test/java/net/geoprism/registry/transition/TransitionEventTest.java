/**
 *
 */
package net.geoprism.registry.transition;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.transition.Transition;
import net.geoprism.registry.graph.transition.Transition.TransitionImpact;
import net.geoprism.registry.graph.transition.Transition.TransitionType;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GPRTransitionEventBusinessService;
import net.geoprism.registry.service.business.TransitionBusinessServiceIF;
import net.geoprism.registry.task.Task;
import net.geoprism.registry.task.TaskQuery;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestUserInfo;
import net.geoprism.registry.view.HistoricalRow;
import net.geoprism.registry.view.Page;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class TransitionEventTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GPRTransitionEventBusinessService traneService;

  @Autowired
  private TransitionBusinessServiceIF       tranService;

  @Before
  public void setUp()
  {
    testData.setUpInstanceData();

    testData.logIn(FastTestDataset.USER_CGOV_RA);
  }

  @After
  public void tearDown()
  {
    testData.logOut();

    testData.tearDownInstanceData();
  }

  @Test
  @Request
  public void testCreateBasicEvent()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      Assert.assertTrue(event.isAppliedToDb());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testEventToJson()
  {
    DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
    format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);

    TransitionEvent event = new TransitionEvent();

    try
    {
      Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

      LocalizedValue expectedDescription = new LocalizedValue("Test");
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, expectedDescription);
      event.setEventDate(date);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      Assert.assertTrue(event.isAppliedToDb());

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      JsonObject json = traneService.toJSON(event, true);

      Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), json.get(TransitionEvent.BEFORETYPECODE).getAsString());
      Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), json.get(TransitionEvent.AFTERTYPECODE).getAsString());
      Assert.assertEquals(format.format(date), json.get(TransitionEvent.EVENTDATE).getAsString());

      LocalizedValue actualDescription = LocalizedValue.fromJSON(json.get(TransitionEvent.DESCRIPTION).getAsJsonObject());

      Assert.assertEquals(expectedDescription.getValue(), actualDescription.getValue());

      JsonArray transitions = json.get("transitions").getAsJsonArray();

      Assert.assertEquals(1, transitions.size());

      JsonObject object = transitions.get(0).getAsJsonObject();

      Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), object.get("sourceType").getAsString());
      Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), object.get("sourceCode").getAsString());
      Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), object.get("targetType").getAsString());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), object.get("targetCode").getAsString());
      Assert.assertEquals(TransitionType.REASSIGN.name(), object.get(Transition.TRANSITIONTYPE).getAsString());
      Assert.assertEquals(TransitionImpact.FULL.name(), object.get(Transition.IMPACT).getAsString());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testAddTransition()
  {
    long beforeCount = new TaskQuery(new QueryFactory()).getCount();

    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      Transition transition = traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.DOWNGRADE, TransitionImpact.FULL);

      Assert.assertEquals(8, Task.getTasks(transition.getOid()).size());

      List<Transition> transitions = traneService.getTransitions(event);

      Assert.assertEquals(1, transitions.size());

      transition = transitions.get(0);
      VertexServerGeoObject source = transition.getSourceVertex();
      VertexServerGeoObject target = transition.getTargetVertex();

      Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), source.getCode());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), target.getCode());
    }
    finally
    {
      traneService.delete(event);
    }

    // Ensure that the unresolved tasks are deleted on event delete
    Assert.assertEquals(beforeCount, new TaskQuery(new QueryFactory()).getCount());
  }

  @Test
  @Request
  public void testAddTransitionSameType()
  {
    long beforeCount = new TaskQuery(new QueryFactory()).getCount();

    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      Transition transition = traneService.addTransition(event, FastTestDataset.PROV_WESTERN.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Assert.assertEquals(4, Task.getTasks(transition.getOid()).size());

      List<Transition> transitions = traneService.getTransitions(event);

      Assert.assertEquals(1, transitions.size());

      transition = transitions.get(0);
      VertexServerGeoObject source = transition.getSourceVertex();
      VertexServerGeoObject target = transition.getTargetVertex();

      Assert.assertEquals(FastTestDataset.PROV_WESTERN.getCode(), source.getCode());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), target.getCode());
    }
    finally
    {
      traneService.delete(event);
    }

    // Ensure that the unresolved tasks are deleted on event delete
    Assert.assertEquals(beforeCount, new TaskQuery(new QueryFactory()).getCount());
  }

  @Test
  @Request
  public void testUpdateTransition()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      Transition transition = traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);
      tranService.apply(transition, event, transition.getOrder(), (VertexServerGeoObject) FastTestDataset.CAMBODIA.getServerObject(), (VertexServerGeoObject) FastTestDataset.PROV_WESTERN.getServerObject());

      List<Task> tasks = Task.getTasks(transition.getOid());

      Assert.assertEquals(8, tasks.size());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testRemoveTransitionsByTarget()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Assert.assertEquals(1, traneService.getTransitions(event).size());

      tranService.removeAll(FastTestDataset.PROVINCE.getServerObject());

      Assert.assertEquals(0, traneService.getTransitions(event).size());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testRemoveTransitionsBySource()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Assert.assertEquals(1, traneService.getTransitions(event).size());

      tranService.removeAll(FastTestDataset.COUNTRY.getServerObject());

      Assert.assertEquals(0, traneService.getTransitions(event).size());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testGetAll()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      List<TransitionEvent> results = traneService.getAll(FastTestDataset.PROVINCE.getServerObject());

      Assert.assertEquals(1, results.size());

      TransitionEvent result = results.get(0);

      Assert.assertEquals(event.getOid(), result.getOid());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testGetHistoricalReport()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Page<HistoricalRow> page = traneService.getHistoricalReport(FastTestDataset.PROVINCE.getServerObject(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, null, null);
      List<HistoricalRow> results = page.getResults();

      Assert.assertEquals(1, results.size());

      HistoricalRow result = results.get(0);

      Assert.assertEquals(event.getEventId(), result.getEventId());
      Assert.assertEquals(TransitionType.REASSIGN.name(), result.getEventType());
      Assert.assertEquals(FastTestDataset.DEFAULT_OVER_TIME_DATE, result.getEventDate());
      Assert.assertEquals("Test", result.getDescription().getValue());
      Assert.assertEquals(event.getBeforeTypeCode(), result.getBeforeType());
      Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), result.getBeforeCode());
      Assert.assertEquals(FastTestDataset.CAMBODIA.getDisplayLabel(), result.getBeforeLabel());
      Assert.assertEquals(event.getAfterTypeCode(), result.getAfterType());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), result.getAfterCode());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), result.getAfterLabel());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testExportHistoricalReportToExcel() throws IOException
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      InputStream istream = traneService.exportToExcel(FastTestDataset.PROVINCE.getServerObject(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE);

      Workbook workbook = WorkbookFactory.create(istream);

      Assert.assertEquals(1, workbook.getNumberOfSheets());

      Sheet sheet = workbook.getSheetAt(0);

      Assert.assertEquals(1, sheet.getLastRowNum());

      Row header = sheet.getRow(0);
      Assert.assertEquals("Event Id", header.getCell(0).getStringCellValue());
      Assert.assertEquals("Event Date", header.getCell(1).getStringCellValue());
      Assert.assertEquals("Event Type", header.getCell(2).getStringCellValue());
      Assert.assertEquals("Description", header.getCell(3).getStringCellValue());
      Assert.assertEquals("Before Type Code", header.getCell(4).getStringCellValue());
      Assert.assertEquals("Before Code", header.getCell(5).getStringCellValue());
      Assert.assertEquals("Before Label", header.getCell(6).getStringCellValue());
      Assert.assertEquals("After Type Code", header.getCell(7).getStringCellValue());
      Assert.assertEquals("After Code", header.getCell(8).getStringCellValue());
      Assert.assertEquals("After Label", header.getCell(9).getStringCellValue());

      Row row = sheet.getRow(1);
      Assert.assertEquals(event.getEventId(), Long.valueOf((long) row.getCell(0).getNumericCellValue()));
      Assert.assertEquals("2020-04-04", row.getCell(1).getStringCellValue());
      Assert.assertEquals("Reassign", row.getCell(2).getStringCellValue());
      Assert.assertEquals("Test", row.getCell(3).getStringCellValue());
      Assert.assertEquals(FastTestDataset.COUNTRY.getCode(), row.getCell(4).getStringCellValue());
      Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), row.getCell(5).getStringCellValue());
      Assert.assertEquals(FastTestDataset.CAMBODIA.getDisplayLabel(), row.getCell(6).getStringCellValue());
      Assert.assertEquals(FastTestDataset.PROVINCE.getCode(), row.getCell(7).getStringCellValue());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), row.getCell(8).getStringCellValue());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getDisplayLabel(), row.getCell(9).getStringCellValue());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testGetHistoricalReportToJson()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Page<HistoricalRow> page = traneService.getHistoricalReport(FastTestDataset.PROVINCE.getServerObject(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, null, null);
      JsonObject json = page.toJSON().getAsJsonObject();

      Assert.assertEquals(1, json.get("count").getAsInt());
      Assert.assertTrue(json.get("pageNumber").isJsonNull());
      Assert.assertTrue(json.get("pageSize").isJsonNull());

      JsonArray results = json.get("resultSet").getAsJsonArray();

      Assert.assertEquals(1, results.size());

      JsonObject result = results.get(0).getAsJsonObject();
      Assert.assertEquals(event.getEventId(), Long.valueOf(result.get(HistoricalRow.EVENT_ID).getAsLong()));
      Assert.assertEquals("Reassign", result.get(HistoricalRow.EVENT_TYPE).getAsString());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testGetHistoricalReportWithPage()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Page<HistoricalRow> page = traneService.getHistoricalReport(FastTestDataset.PROVINCE.getServerObject(), FastTestDataset.DEFAULT_OVER_TIME_DATE, FastTestDataset.DEFAULT_OVER_TIME_DATE, 2, 10);

      Assert.assertEquals(Long.valueOf(1L), page.getCount());
      Assert.assertEquals(0, page.getResults().size());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testRemoveEventsByTarget()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Assert.assertEquals(1, traneService.getAll(FastTestDataset.PROVINCE.getServerObject()).size());

      traneService.removeAll(FastTestDataset.PROVINCE.getServerObject());

      Assert.assertEquals(0, traneService.getAll(FastTestDataset.PROVINCE.getServerObject()).size());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testRemoveEventsBySource()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      Assert.assertEquals(1, traneService.getAll(FastTestDataset.COUNTRY.getServerObject()).size());

      traneService.removeAll(FastTestDataset.COUNTRY.getServerObject());

      Assert.assertEquals(0, traneService.getAll(FastTestDataset.COUNTRY.getServerObject()).size());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testAddTransitionBadType()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(FastTestDataset.DEFAULT_OVER_TIME_DATE);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      traneService.addTransition(event, FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.DIST_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  @Request
  public void testPage()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

      LocalizedValue expectedDescription = new LocalizedValue("Test");
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, expectedDescription);
      event.setEventDate(date);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
      traneService.apply(event);

      Page<TransitionEvent> page = traneService.page(10, 1, null);

      Assert.assertEquals(Long.valueOf(1), page.getCount());
      Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
      Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
      Assert.assertEquals(event.getOid(), page.getResults().get(0).getOid());
    }
    finally
    {
      traneService.delete(event);
    }
  }

  @Test
  public void testPagePermissions()
  {
    TransitionEvent event = testPagePermissionsSetUp();

    try
    {
      // Test allowed users on a private GeoObjectType
      TestUserInfo[] allowedUsers = new TestUserInfo[] { FastTestDataset.USER_CGOV_RA, FastTestDataset.USER_CGOV_RM, FastTestDataset.USER_CGOV_RC, FastTestDataset.USER_CGOV_AC };
      for (TestUserInfo user : allowedUsers)
      {
        FastTestDataset.runAsUser(user, (request) -> {
          testPagePermissions(request.getSessionId(), event);
        });
      }
    }
    finally
    {
      testPagePermissionsTearDown(event);
    }
  }

  @Request(RequestType.SESSION)
  private void testPagePermissions(String sessionId, TransitionEvent event)
  {
    Page<TransitionEvent> page = traneService.page(10, 1, null);

    Assert.assertEquals(Long.valueOf(1), page.getCount());
    Assert.assertEquals(Integer.valueOf(1), page.getPageNumber());
    Assert.assertEquals(Integer.valueOf(10), page.getPageSize());
    Assert.assertEquals(event.getOid(), page.getResults().get(0).getOid());
  }

  @Request()
  private TransitionEvent testPagePermissionsSetUp()
  {
    TransitionEvent event = new TransitionEvent();

    Date date = FastTestDataset.DEFAULT_OVER_TIME_DATE;

    LocalizedValue expectedDescription = new LocalizedValue("Test");
    LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, expectedDescription);
    event.setEventDate(date);
    event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
    event.setBeforeTypeOrgCode(FastTestDataset.COUNTRY.getOrganization().getCode());
    event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
    event.setAfterTypeOrgCode(FastTestDataset.PROVINCE.getOrganization().getCode());
    traneService.apply(event);

    return event;
  }

  @Request()
  private void testPagePermissionsTearDown(TransitionEvent event)
  {
    traneService.delete(event);
  }

}
