/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicGeoObjectTypeServiceTest implements InstanceTestClassListener
{
  private static ClassificationType           classificationType;

  private static Classification               root;

  @Autowired
  private GeoObjectTypeBusinessServiceIF      service;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();

    classificationType = this.cTypeService.apply(ClassificationTypeTest.createMock());

    root = this.cService.newInstance(classificationType);
    root.setCode("ROOT_OBJ");

    this.cService.apply(root, null);
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
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
  public void testCreateDeleteGeoObjectType()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      Map<String, AttributeType> attributes = type.getAttributeMap();

      Assert.assertTrue(attributes.size() > 0);
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testCharacterAttribute()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      AttributeCharacterType attributeDto = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);

      attributeDto = (AttributeCharacterType) service.createAttributeType(type, attributeDto);

      Assert.assertNotNull(attributeDto);

      Assert.assertTrue(type.getAttribute(attributeDto.getName()).isPresent());

      service.deleteAttributeType(type, attributeDto.getName());

      Assert.assertFalse(type.getAttribute(attributeDto.getName()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testDoubleAttribute()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      AttributeFloatType attributeDto = new AttributeFloatType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);
      attributeDto.setPrecision(10);
      attributeDto.setScale(2);

      attributeDto = (AttributeFloatType) service.createAttributeType(type, attributeDto);

      Assert.assertNotNull(attributeDto);

      Assert.assertTrue(type.getAttribute(attributeDto.getName()).isPresent());

      attributeDto.setPrecision(32);
      attributeDto.setScale(2);

      attributeDto = (AttributeFloatType) service.updateAttributeType(type, attributeDto);

      service.deleteAttributeType(type, attributeDto.getName());

      Assert.assertFalse(type.getAttribute(attributeDto.getName()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testClassificationAttribute()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      AttributeClassificationType attributeDto = new AttributeClassificationType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);
      attributeDto.setClassificationType(classificationType.getCode());
      attributeDto.setRootTerm(root.toTerm());

      attributeDto = (AttributeClassificationType) service.createAttributeType(type, attributeDto);

      Assert.assertNotNull(attributeDto);

      Assert.assertTrue(type.getAttribute(attributeDto.getName()).isPresent());

      service.deleteAttributeType(type, attributeDto.getName());

      Assert.assertFalse(type.getAttribute(attributeDto.getName()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testTermAttribute()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      AttributeTermType attributeDto = new AttributeTermType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);

      attributeDto = (AttributeTermType) service.createAttributeType(type, attributeDto);

      Assert.assertNotNull(attributeDto);

      Assert.assertTrue(type.getAttribute(attributeDto.getName()).isPresent());

      service.deleteAttributeType(type, attributeDto.getName());

      Assert.assertFalse(type.getAttribute(attributeDto.getName()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testCreateDeleteSubType()
  {
    GeoObjectType parentDto = USATestData.COUNTRY.toDTO();
    parentDto.setIsAbstract(true);

    ServerGeoObjectType parentType = this.service.create(parentDto);

    try
    {
      GeoObjectType childDto = USATestData.STATE.toDTO();
      childDto.setSuperTypeCode(parentType.getCode());

      ServerGeoObjectType childType = this.service.create(childDto);

      try
      {
        Assert.assertEquals(parentType.getOid(), childType.getSuperType().getOid());

        List<ServerGeoObjectType> subTypes = this.service.getSubtypes(parentType);

        Assert.assertEquals(1, subTypes.size());

        Assert.assertEquals(childType.getOid(), subTypes.get(0).getOid());
      }
      finally
      {
        this.service.deleteGeoObjectType(childType.getCode());
      }

    }
    finally
    {
      this.service.deleteGeoObjectType(parentType.getCode());
    }
  }

}
