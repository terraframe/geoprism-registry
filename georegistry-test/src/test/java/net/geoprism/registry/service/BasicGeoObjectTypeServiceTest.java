/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.CodeReference;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class BasicGeoObjectTypeServiceTest extends ConceptDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectTypeBusinessServiceIF service;

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
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

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
      Assert.assertNotNull(type.getMdVertexDAO());
      Assert.assertNotNull(type.getGeometryTable());

      Map<String, AttributeType> attributes = type.getAttributeMap();

      Assert.assertTrue(attributes.size() > 0);

      Set<String> attributeNames = attributes.keySet();

      Assert.assertTrue(attributeNames.contains(DefaultAttribute.DATA_SOURCE.getName()));
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
      Assert.assertEquals(Long.valueOf(0), type.getSequence());

      AttributeCharacterType attributeDto = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);

      attributeDto = (AttributeCharacterType) service.createAttributeType(type, attributeDto);

      Assert.assertEquals(Long.valueOf(1), type.getSequence());

      Assert.assertNotNull(attributeDto);

      Assert.assertTrue(type.getAttribute(attributeDto.getCode()).isPresent());

      service.deleteAttributeType(type, attributeDto.getCode());

      Assert.assertFalse(type.getAttribute(attributeDto.getCode()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testDuplicateAttribute()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      service.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false));
      service.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false));

      Assert.fail("Able to create attributes with the same name");
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

      Assert.assertTrue(type.getAttribute(attributeDto.getCode()).isPresent());

      attributeDto.setPrecision(32);
      attributeDto.setScale(2);

      attributeDto = (AttributeFloatType) service.updateAttributeType(type, attributeDto);

      service.deleteAttributeType(type, attributeDto.getCode());

      Assert.assertFalse(type.getAttribute(attributeDto.getCode()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testDifferentDoubleAttribute()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      AttributeFloatType attribute1Dto = new AttributeFloatType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);
      attribute1Dto.setPrecision(10);
      attribute1Dto.setScale(2);

      attribute1Dto = (AttributeFloatType) service.createAttributeType(type, attribute1Dto);

      Assert.assertNotNull(attribute1Dto);

      Assert.assertTrue(type.getAttribute(attribute1Dto.getCode()).isPresent());

      AttributeFloatType attribute2Dto = new AttributeFloatType("testDouble", new LocalizedValue("Test Double"), new LocalizedValue("Test Double"), false, false, false);

      attribute2Dto = (AttributeFloatType) service.createAttributeType(type, attribute2Dto);

      Assert.assertNotNull(attribute2Dto);

      Assert.assertTrue(type.getAttribute(attribute2Dto.getCode()).isPresent());
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
      attributeDto.setConceptSet(cSet.getCode());
      attributeDto.setRootTerm(CodeReference.build(rootConcept.getCode(), rootConcept.getType().getCode()));
      attributeDto.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
      attributeDto.setEndDate(TestDataSet.DEFAULT_END_TIME_DATE);

      attributeDto = (AttributeClassificationType) service.createAttributeType(type, attributeDto);

      Assert.assertNotNull(attributeDto);

      Optional<AttributeType> optional = type.getAttribute(attributeDto.getCode());

      Assert.assertTrue(optional.isPresent());

      net.geoprism.registry.graph.AttributeClassificationType attributeType = (net.geoprism.registry.graph.AttributeClassificationType) optional.get();

      Assert.assertNotNull(attributeType.getStartDate());
      Assert.assertNotNull(attributeType.getEndDate());
      Assert.assertNotNull(attributeType.getRootTerm());
      Assert.assertNotNull(attributeType.getConceptSet());

      service.deleteAttributeType(type, attributeDto.getCode());

      Assert.assertFalse(type.getAttribute(attributeDto.getCode()).isPresent());
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
        Assert.assertNotNull(childType.getMdVertexDAO());
        Assert.assertNotNull(childType.getGeometryTable());

        Assert.assertEquals(parentType.getOid(), childType.getSuperType().getOid());
        Assert.assertTrue(childType.getAttribute(DefaultAttribute.INVALID.getName()).isPresent());

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

  @Test(expected = ProgrammingErrorException.class)
  @Request
  public void testDuplicateAttributeOnSubType()
  {
    GeoObjectType parentDto = USATestData.COUNTRY.toDTO();
    parentDto.setIsAbstract(true);

    ServerGeoObjectType parentType = this.service.create(parentDto);

    try
    {
      service.createAttributeType(parentType, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false));

      GeoObjectType childDto = USATestData.STATE.toDTO();
      childDto.setSuperTypeCode(parentType.getCode());

      ServerGeoObjectType childType = this.service.create(childDto);

      try
      {
        service.createAttributeType(childType, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false));

        Assert.fail("Able to create attributes with the same name");
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

  @Test
  @Request
  public void testCreateDeleteWithClassification()
  {
    GeoObjectType dto = USATestData.COUNTRY.toDTO();
    dto.setClassification(createAttributeClassificationType());

    ServerGeoObjectType type = this.service.create(dto);

    try
    {
      Assert.assertNotNull(type.getMdVertexDAO());
      Assert.assertNotNull(type.getGeometryTable());

      Map<String, AttributeType> attributes = type.getAttributeMap();

      Assert.assertTrue(attributes.size() > 0);

      Set<String> attributeNames = attributes.keySet();

      Assert.assertTrue(attributeNames.contains(DefaultAttribute.CLASSIFICATION.getName()));
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

}
