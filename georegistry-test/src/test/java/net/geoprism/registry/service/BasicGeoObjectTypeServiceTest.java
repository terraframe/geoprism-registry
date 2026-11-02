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
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.GenericException;
import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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
      Assertions.assertNotNull(type.getMdVertexDAO());
      Assertions.assertNotNull(type.getGeometryTable());

      Map<String, AttributeType> attributes = type.getAttributeMap();

      Assertions.assertTrue(attributes.size() > 0);

      Set<String> attributeNames = attributes.keySet();

      Assertions.assertTrue(attributeNames.contains(DefaultAttribute.DATA_SOURCE.getName()));
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
      Assertions.assertEquals(Long.valueOf(0), type.getSequence());

      AttributeCharacterType attributeDto = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false);

      attributeDto = (AttributeCharacterType) service.createAttributeType(type, attributeDto);

      Assertions.assertEquals(Long.valueOf(1), type.getSequence());

      Assertions.assertNotNull(attributeDto);

      Assertions.assertTrue(type.getAttribute(attributeDto.getCode()).isPresent());

      service.deleteAttributeType(type, attributeDto.getCode());

      Assertions.assertFalse(type.getAttribute(attributeDto.getCode()).isPresent());
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

  @Test
  @Request
  public void testDuplicateAttribute()
  {
    assertThrows(GenericException.class, () -> {
      GeoObjectType dto = USATestData.COUNTRY.toDTO();

      ServerGeoObjectType type = this.service.create(dto);

      try
      {
        service.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false));
        service.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test Character"), false, false, false));

        Assertions.fail("Able to create attributes with the same name");
      }
      finally
      {
        this.service.deleteGeoObjectType(type.getCode());
      }
    });
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

      Assertions.assertNotNull(attributeDto);

      Assertions.assertTrue(type.getAttribute(attributeDto.getCode()).isPresent());

      attributeDto.setPrecision(32);
      attributeDto.setScale(2);

      attributeDto = (AttributeFloatType) service.updateAttributeType(type, attributeDto);

      service.deleteAttributeType(type, attributeDto.getCode());

      Assertions.assertFalse(type.getAttribute(attributeDto.getCode()).isPresent());
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

      Assertions.assertNotNull(attribute1Dto);

      Assertions.assertTrue(type.getAttribute(attribute1Dto.getCode()).isPresent());

      AttributeFloatType attribute2Dto = new AttributeFloatType("testDouble", new LocalizedValue("Test Double"), new LocalizedValue("Test Double"), false, false, false);

      attribute2Dto = (AttributeFloatType) service.createAttributeType(type, attribute2Dto);

      Assertions.assertNotNull(attribute2Dto);

      Assertions.assertTrue(type.getAttribute(attribute2Dto.getCode()).isPresent());
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
      attributeDto.setRootTerm(rootConcept.getCode());
      attributeDto.setStartDate(TestDataSet.DEFAULT_OVER_TIME_DATE);
      attributeDto.setEndDate(TestDataSet.DEFAULT_END_TIME_DATE);

      attributeDto = (AttributeClassificationType) service.createAttributeType(type, attributeDto);

      Assertions.assertNotNull(attributeDto);

      Optional<AttributeType> optional = type.getAttribute(attributeDto.getCode());

      Assertions.assertTrue(optional.isPresent());

      net.geoprism.registry.graph.AttributeClassificationType attributeType = (net.geoprism.registry.graph.AttributeClassificationType) optional.get();

      Assertions.assertNotNull(attributeType.getStartDate());
      Assertions.assertNotNull(attributeType.getEndDate());
      Assertions.assertNotNull(attributeType.getRootTerm());
      Assertions.assertNotNull(attributeType.getConceptSet());

      service.deleteAttributeType(type, attributeDto.getCode());

      Assertions.assertFalse(type.getAttribute(attributeDto.getCode()).isPresent());
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
        Assertions.assertNotNull(childType.getMdVertexDAO());
        Assertions.assertNotNull(childType.getGeometryTable());

        Assertions.assertEquals(parentType.getOid(), childType.getSuperType().getOid());
        Assertions.assertTrue(childType.getAttribute(DefaultAttribute.INVALID.getName()).isPresent());

        List<ServerGeoObjectType> subTypes = this.service.getSubtypes(parentType);

        Assertions.assertEquals(1, subTypes.size());

        Assertions.assertEquals(childType.getOid(), subTypes.get(0).getOid());
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
  public void testDuplicateAttributeOnSubType()
  {
    assertThrows(GenericException.class, () -> {
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

          Assertions.fail("Able to create attributes with the same name");
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
    });
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
      Assertions.assertNotNull(type.getMdVertexDAO());
      Assertions.assertNotNull(type.getGeometryTable());

      Map<String, AttributeType> attributes = type.getAttributeMap();

      Assertions.assertTrue(attributes.size() > 0);

      Set<String> attributeNames = attributes.keySet();

      Assertions.assertTrue(attributeNames.contains(DefaultAttribute.CLASSIFICATION.getName()));
    }
    finally
    {
      this.service.deleteGeoObjectType(type.getCode());
    }
  }

}
