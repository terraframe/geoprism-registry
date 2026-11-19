/**
 *
 */
package net.geoprism.registry.service;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.EdgeConstant;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;

/**
 * Tests that deleting a change over time attribute type also deletes all of
 * the AttributeValue nodes (and their HasValue edges) which hold the values of
 * that attribute, while leaving the value nodes of other attributes intact.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class DeleteAttributeTypeValueNodeTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  private static ServerGeoObjectType     type;

  private static AttributeFloatType      attributeFloat;

  @Autowired
  private GeoObjectTypeBusinessServiceIF typeService;

  @Autowired
  private GeoObjectBusinessServiceIF     service;

  @Override
  protected TestOrganizationInfo getOrganization()
  {
    return USATestData.ORG_NPS;
  }

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();

    super.beforeClassSetup();

    type = this.typeService.create(USATestData.COUNTRY.toDTO());

    // Control attribute which is not deleted by the test
    attributeFloat = this.typeService.createAttributeType(type, new AttributeFloatType("testFloat", new LocalizedValue("Test Float"), new LocalizedValue("Test Float"), false, false, false));
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (type != null)
    {
      this.typeService.deleteGeoObjectType(type.getCode());
    }

    super.afterClassSetup();

    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testDeleteChangeOverTimeAttributeDeletesValueNodes()
  {
    AttributeCharacterType attributeDto = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);
    attributeDto.setChangeOverTime(true);

    AttributeCharacterType attributeCharacter = this.typeService.createAttributeType(type, attributeDto);

    boolean attributeDeleted = false;

    try
    {
      Assertions.assertTrue(type.getAttribute(attributeCharacter.getCode()).get().getIsChangeOverTime());

      // Create a geo object with two distinct value nodes for the attribute
      // which will be deleted, and one value node for the control attribute
      Date startDate = USATestData.DEFAULT_OVER_TIME_DATE;
      Date midDate = addDays(startDate, 10);
      Date midDatePlusOne = addDays(midDate, 1);
      Date endDate = USATestData.DEFAULT_END_TIME_DATE;

      ServerGeoObjectIF object = this.service.newInstance(type);
      object.setInvalid(false);
      object.setCode(USATestData.USA.getCode());
      object.setDisplayLabel(new LocalizedValue(USATestData.USA.getDisplayLabel()), startDate, endDate);
      object.setExists(true, startDate, endDate);
      object.setValue(attributeCharacter.getCode(), "First Value", startDate, midDate);
      object.setValue(attributeCharacter.getCode(), "Second Value", midDatePlusOne, endDate);
      object.setValue(attributeFloat.getCode(), 10.4D, startDate, endDate);

      this.service.apply(object, false, false);

      ServerGeoObjectIF test = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type);

      Assertions.assertEquals("First Value", test.getValue(attributeCharacter.getCode(), startDate));
      Assertions.assertEquals("Second Value", test.getValue(attributeCharacter.getCode(), endDate));

      Assertions.assertEquals(2L, countValueNodes(test, attributeCharacter.getCode()));
      Assertions.assertEquals(1L, countValueNodes(test, attributeFloat.getCode()));
      Assertions.assertEquals(2L, countValueNodes(type, attributeCharacter.getCode()));

      // Delete the change over time attribute type
      this.typeService.deleteAttributeType(type, attributeCharacter.getCode());
      attributeDeleted = true;

      Assertions.assertFalse(type.getAttribute(attributeCharacter.getCode()).isPresent());

      // The value nodes of the deleted attribute must be gone
      test = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type);

      Assertions.assertEquals(0L, countValueNodes(test, attributeCharacter.getCode()));
      Assertions.assertEquals(0L, countValueNodes(type, attributeCharacter.getCode()));

      // The value nodes of the other attributes must be untouched
      Assertions.assertEquals(1L, countValueNodes(test, attributeFloat.getCode()));
      Assertions.assertEquals(10.4D, (Double) test.getValue(attributeFloat.getCode(), startDate), 0.000001);
      Assertions.assertEquals(USATestData.USA.getDisplayLabel(), test.getDisplayLabel(startDate).getValue());
    }
    finally
    {
      ServerGeoObjectIF sgo = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type, false);

      if (sgo != null)
      {
        this.service.delete(sgo);
      }

      if (!attributeDeleted && type.getAttribute(attributeCharacter.getCode()).isPresent())
      {
        this.typeService.deleteAttributeType(type, attributeCharacter.getCode());
      }
    }
  }

  /**
   * @return The number of value nodes of the given attribute which are attached
   *         to the given geo object
   */
  private static long countValueNodes(ServerGeoObjectIF object, String attributeName)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT count(*) FROM (");
    statement.append(" SELECT expand(out('" + EdgeConstant.HAS_VALUE.getDBClassName() + "')[attributeName = :attributeName])");
    statement.append(" FROM " + object.getType().getDBClassName());
    statement.append(" WHERE @rid = :rid");
    statement.append(")");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("rid", object.getVertex().getRID());
    query.setParameter("attributeName", attributeName);

    return query.getSingleResult();
  }

  /**
   * @return The number of value nodes of the given attribute which are attached
   *         to any geo object of the given type
   */
  private static long countValueNodes(ServerGeoObjectType type, String attributeName)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT count(*) FROM (");
    statement.append(" SELECT expand(out('" + EdgeConstant.HAS_VALUE.getDBClassName() + "')[attributeName = :attributeName])");
    statement.append(" FROM " + type.getDBClassName());
    statement.append(")");

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());
    query.setParameter("attributeName", attributeName);

    return query.getSingleResult();
  }

  private static Date addDays(Date date, int days)
  {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    calendar.setTime(date);
    calendar.add(Calendar.DAY_OF_MONTH, days);

    return calendar.getTime();
  }
}
