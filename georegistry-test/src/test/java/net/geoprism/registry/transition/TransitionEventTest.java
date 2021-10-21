package net.geoprism.registry.transition;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.Transition;
import net.geoprism.registry.graph.Transition.TransitionImpact;
import net.geoprism.registry.graph.Transition.TransitionType;
import net.geoprism.registry.graph.TransitionEvent;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.view.Page;

public class TransitionEventTest
{
  private static FastTestDataset testData;

  @BeforeClass
  public static void setUpClass()
  {
    testData = FastTestDataset.newTestData();
    testData.setUpMetadata();
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    if (testData != null)
    {
      testData.tearDownMetadata();
    }
  }

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
      event.setEventDate(new Date());
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.apply();

      Assert.assertTrue(event.isAppliedToDb());
    }
    finally
    {
      event.delete();
    }
  }

  @Test
  @Request
  public void testEventToJson()
  {
    DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);

    TransitionEvent event = new TransitionEvent();

    try
    {
      Date date = new Date();

      LocalizedValue expectedDescription = new LocalizedValue("Test");
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, expectedDescription);
      event.setEventDate(date);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.apply();

      Assert.assertTrue(event.isAppliedToDb());

      event.addTransition(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      JsonObject json = event.toJSON(true);

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
      event.delete();
    }
  }

  @Test
  @Request
  public void testAddTransition()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, new LocalizedValue("Test"));
      event.setEventDate(new Date());
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.apply();

      event.addTransition(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.PROV_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);

      List<Transition> transitions = event.getTransitions();

      Assert.assertEquals(1, transitions.size());

      Transition transition = transitions.get(0);
      VertexServerGeoObject source = transition.getSource();
      VertexServerGeoObject target = transition.getTarget();

      Assert.assertEquals(FastTestDataset.CAMBODIA.getCode(), source.getCode());
      Assert.assertEquals(FastTestDataset.PROV_CENTRAL.getCode(), target.getCode());
    }
    finally
    {
      event.delete();
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
      event.setEventDate(new Date());
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.apply();

      event.addTransition(FastTestDataset.CAMBODIA.getServerObject(), FastTestDataset.DIST_CENTRAL.getServerObject(), TransitionType.REASSIGN, TransitionImpact.FULL);
    }
    finally
    {
      event.delete();
    }
  }

  @Test
  @Request
  public void testPage()
  {
    TransitionEvent event = new TransitionEvent();

    try
    {
      Date date = new Date();

      LocalizedValue expectedDescription = new LocalizedValue("Test");
      LocalizedValueConverter.populate(event, TransitionEvent.DESCRIPTION, expectedDescription);
      event.setEventDate(date);
      event.setBeforeTypeCode(FastTestDataset.COUNTRY.getCode());
      event.setAfterTypeCode(FastTestDataset.PROVINCE.getCode());
      event.apply();

      Page<TransitionEvent> page = TransitionEvent.page(10, 1);

      Assert.assertEquals(new Long(1), page.getCount());
      Assert.assertEquals(new Integer(1), page.getPageNumber());
      Assert.assertEquals(new Integer(10), page.getPageSize());
      Assert.assertEquals(event.getOid(), page.getResults().get(0).getOid());
    }
    finally
    {
      event.delete();
    }
  }

}
