/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.TermBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicGeoObjectServiceTest implements InstanceTestClassListener
{
  private static ServerGeoObjectType          type;

  private static AttributeFloatType           attributeFloat;

  private static AttributeClassificationType  attributeClassification;

  private static AttributeTermType            attributeTerm;

  private static ClassificationType           classificationType;

  private static Classification               root;

  private static Term                         term;

  private static Classifier                   classifier;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF      typeService;

  @Autowired
  private TermBusinessServiceIF               termService;

  @Autowired
  private GeoObjectBusinessServiceIF          service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();

    classificationType = this.cTypeService.apply(ClassificationTypeTest.createMock());

    root = this.cService.newInstance(classificationType);
    root.setCode("ROOT_OBJ");

    this.cService.apply(root, null);

    type = this.typeService.create(USATestData.COUNTRY.toDTO());

    attributeFloat = this.typeService.createAttributeType(type, new AttributeFloatType("testFloat", new LocalizedValue("Test Float"), new LocalizedValue("Test Float"), false, false, false));
    attributeTerm = this.typeService.createAttributeType(type, new AttributeTermType("testTerm", new LocalizedValue("Test Term"), new LocalizedValue("Test Term"), false, false, false));

    attributeClassification = new AttributeClassificationType("testClassification", new LocalizedValue("Test Classification"), new LocalizedValue("Test Classification"), false, false, false);
    attributeClassification.setClassificationType(classificationType.getCode());
    attributeClassification.setRootTerm(root.toTerm());

    attributeClassification = this.typeService.createAttributeType(type, attributeClassification);

    term = this.termService.createTerm(attributeTerm.getRootTerm().getCode(), new Term("TERM_C_1", new LocalizedValue("TT1"), new LocalizedValue("TT1")));

    Term root = attributeTerm.getRootTerm();
    String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

    classifier = Classifier.getByKey(Classifier.buildKey(parent, term.getCode()));
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (type != null)
    {
      this.typeService.deleteGeoObjectType(type.getCode());
    }

    if (root != null)
    {
      this.cService.delete(root);
    }

    if (classificationType != null)
    {
      this.cTypeService.delete(classificationType);
    }

    USATestData.ORG_NPS.delete();
  }

  @Test
  @Request
  public void testCreateDeleteGeoObject()
  {
    double testDouble = 10.4D;
    ServerGeoObjectIF object = this.service.newInstance(type);

    try
    {
      object.setInvalid(false);
      object.setCode(USATestData.USA.getCode());
      object.setDisplayLabel(new LocalizedValue(USATestData.USA.getDisplayLabel()), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setExists(true, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setGeometry(USATestData.USA.getGeometry(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setValue(attributeFloat.getName(), testDouble, USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setValue(attributeClassification.getName(), root.getVertex(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);
      object.setValue(attributeTerm.getName(), classifier.getOid(), USATestData.DEFAULT_OVER_TIME_DATE, USATestData.DEFAULT_END_TIME_DATE);

      this.service.apply(object, false, false);

      ServerGeoObjectIF test = this.service.getGeoObject(object.getUid(), type.getCode());

      Assert.assertNotNull(test);
      Assert.assertEquals(object.getInvalid(), test.getInvalid());
      Assert.assertEquals(object.getCode(), test.getCode());
      Assert.assertEquals(object.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getValue(), test.getDisplayLabel(USATestData.DEFAULT_OVER_TIME_DATE).getValue());
      Assert.assertEquals(object.getExists(USATestData.DEFAULT_OVER_TIME_DATE), test.getExists(USATestData.DEFAULT_OVER_TIME_DATE));
      Assert.assertEquals(testDouble, test.getValue(attributeFloat.getName(), USATestData.DEFAULT_OVER_TIME_DATE), 0.000001);
      Assert.assertEquals(root.getOid(), test.getValue(attributeClassification.getName(), USATestData.DEFAULT_OVER_TIME_DATE));
      Classifier value = test.getValue(attributeTerm.getName(), USATestData.DEFAULT_OVER_TIME_DATE);
      Assert.assertEquals(term.getCode(), value.getClassifierId());

      Geometry geometry = test.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE);

      Assert.assertNotNull(geometry);
      Assert.assertEquals(object.getGeometry(USATestData.DEFAULT_OVER_TIME_DATE), geometry);
    }
    finally
    {
      object.delete();
    }
  }
}
