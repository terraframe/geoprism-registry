/**
 *
 */
package net.geoprism.registry.service;

import java.util.Set;
import java.util.TreeSet;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.BusinessDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.model.GeometryStateValue;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicGeoObjectServiceTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  private static ServerGeoObjectType         type;

  private static AttributeFloatType          attributeFloat;

  private static AttributeClassificationType attributeClassification;

  private static SourceAuthority             authority;

  private static DataSource                  source;

  @Autowired
  private GeoObjectTypeBusinessServiceIF     typeService;

  @Autowired
  private GeoObjectBusinessServiceIF         service;

  @Autowired
  private DataSourceBusinessServiceIF        sourceService;

  @Autowired
  private SourceAuthorityBusinessServiceIF   authorityService;

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

    attributeFloat = this.typeService.createAttributeType(type, new AttributeFloatType("testFloat", new LocalizedValue("Test Float"), new LocalizedValue("Test Float"), false, false, false));

    attributeClassification = this.typeService.createAttributeType(type, this.createAttributeClassificationType());

    authority = this.authorityService.apply(SourceAuthorityServiceTest.createMock());

    source = this.sourceService.apply(DataSourceServiceTest.createMock(authority));
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (type != null)
    {
      this.typeService.deleteGeoObjectType(type.getCode());
    }

    if (source != null)
    {
      this.sourceService.delete(source);
    }

    if (authority != null)
    {
      this.authorityService.delete(authority);
    }

    super.afterClassSetup();

    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testCreateDeleteGeoObject()
  {
    double testDouble = 10.4D;
    ServerGeoObjectIF object = this.service.newInstance(type);

    Set<String> geometryIds = new TreeSet<>();

    object.setInvalid(false);
    object.setCode(USATestData.USA.getCode());
    object.setDisplayLabel(new LocalizedValue(USATestData.USA.getDisplayLabel()), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setExists(true, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setGeometry(USATestData.USA.getGeometry(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(attributeFloat.getCode(), testDouble, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(attributeClassification.getCode(), rootConcept.getVertex(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), source, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    this.service.apply(object, false, false);

    try
    {
      ServerGeoObjectIF test = this.service.getGeoObject(object.getUid(), type.getCode());

      Assert.assertNotNull(test);
      Assert.assertEquals(object.getInvalid(), test.getInvalid());
      Assert.assertEquals(object.getCode(), test.getCode());
      Assert.assertEquals(object.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getValue(), test.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getValue());
      Assert.assertEquals(object.getExists(USATestData.DEFAULT_OVER_TIME_DATE), test.getExists(USATestData.DEFAULT_OVER_TIME_DATE));
      Assert.assertEquals(testDouble, test.getValue(attributeFloat.getCode(), USATestData.DEFAULT_OVER_TIME_DATE), 0.000001);
      Assert.assertEquals(rootConcept.getOid(), test.getValue(attributeClassification.getCode(), USATestData.DEFAULT_OVER_TIME_DATE));
      Assert.assertEquals(source.getOid(), test.getValue(DefaultAttribute.DATA_SOURCE.getName(), USATestData.DEFAULT_OVER_TIME_DATE));

      Geometry geometry = test.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(geometry);
      Assert.assertEquals(object.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE), geometry);

      ValueOverTimeCollection vots = test.getValuesOverTime(DefaultAttribute.GEOMETRY.getName());

      Assert.assertEquals(1, vots.size());

      String geometryId = test.getValue(DefaultAttribute.GEOMETRY.getName(), USATestData.DEFAULT_OVER_TIME_DATE);

      // Assert the values of the geometry table entry
      Assert.assertEquals(object.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE), vots.get(0).getValue());
      Assert.assertNotNull(geometryId);

      BusinessDAOIF entry = GeometryStateValue.getGeometryInstance(geometryId);

      Assert.assertNotNull(entry);

      Assert.assertEquals(object.getCode(), entry.getValue(DefaultAttribute.CODE.getName()));
      Assert.assertEquals(object.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getValue(), entry.getValue(DefaultAttribute.DISPLAY_LABEL.getName()));
      Assert.assertEquals(object.getUid(), entry.getValue(DefaultAttribute.UID.getName()));
      Assert.assertTrue(entry.getValue(EdgeType.START_DATE).length() > 0);
      Assert.assertTrue(entry.getValue(EdgeType.END_DATE).length() > 0);

      geometryIds.add(geometryId);

      // Test updating the object
      object = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type);
      object.setGeometry(USATestData.COLORADO.getGeometry(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      this.service.apply(object, false, false);

      geometryIds.add(test.getValue(DefaultAttribute.GEOMETRY.getName(), USATestData.DEFAULT_OVER_TIME_DATE));

      object = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type);
      geometryIds.add(test.getValue(DefaultAttribute.GEOMETRY.getName(), USATestData.DEFAULT_OVER_TIME_DATE));
    }
    finally
    {
      // Delete the object
      ServerGeoObjectIF sgo = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type);

      this.service.delete(sgo);

      // Make sure the entry was deleted

      for (String geometryId : geometryIds)
      {

        try
        {
          GeometryStateValue.getGeometryInstance(geometryId);

          Assert.fail("Able to find object that should be deleted");
        }
        catch (Exception e)
        {
          // This is expected
        }
      }
    }
  }

  @Test
  @Request
  public void testUpdateGeoObject()
  {
    double testDouble = 10.4D;
    ServerGeoObjectIF object = this.service.newInstance(type);

    Set<String> geometryIds = new TreeSet<>();

    object.setInvalid(false);
    object.setCode(USATestData.USA.getCode());
    object.setDisplayLabel(new LocalizedValue(USATestData.USA.getDisplayLabel()), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setExists(true, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setGeometry(USATestData.USA.getGeometry(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(attributeFloat.getCode(), testDouble, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(attributeClassification.getCode(), rootConcept.getVertex(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
    object.setValue(DefaultAttribute.DATA_SOURCE.getName(), source, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

    this.service.apply(object, false, false);

    try
    {
      object = this.service.getGeoObject(object.getUid(), type.getCode());
      object.setDisplayLabel(new LocalizedValue("Test"), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      this.service.apply(object, false, false);

      ServerGeoObjectIF test = this.service.getGeoObject(object.getUid(), type.getCode());

      Geometry geometry = test.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(geometry);
      Assert.assertEquals(object.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE), geometry);

    }
    finally
    {
      // Delete the object
      ServerGeoObjectIF sgo = this.service.getGeoObjectByCode(USATestData.USA.getCode(), type);

      this.service.delete(sgo);
    }
  }
}
