/**
 *
 */
package net.geoprism.registry.business;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runwaysdk.session.Request;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.OrganizationGroup;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@ExtendWith(InstanceTestClassExtension.class)
public class BusinessTypeTest extends FastDatasetTest implements InstanceTestClassListener
{
  @Autowired
  private BusinessTypeBusinessServiceIF typeService;

  public BusinessTypeDTO createDTO()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    BusinessTypeDTO object = new BusinessTypeDTO();
    object.setCode(code);
    object.setOrganization(orgCode);
    object.setDisplayLabel(new LocalizedValue(label));
    object.setDescription(new LocalizedValue("Test Description"));

    return object;
  }

  @Test
  @Request
  public void testCreate()
  {
    BusinessTypeDTO dto = createDTO();
    BusinessType type = this.typeService.apply(dto);

    try
    {
      Assertions.assertEquals(dto.getCode(), type.getCode());
      Assertions.assertEquals(dto.getOrganization(), type.getServerOrganization().getCode());
      Assertions.assertEquals(dto.getDisplayLabel().getValue(), type.getLabel().getValue());
      Assertions.assertEquals(dto.getDescription().getValue(), type.getDescriptionLV().getValue());
      Assertions.assertNotNull(type.getMdVertex());
      Assertions.assertEquals(type.getOrigin(), GeoprismProperties.getOrigin());
      Assertions.assertEquals(Long.valueOf(0), type.getSequence());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      Assertions.assertNotNull(this.typeService.getByCode(type.getCode()));
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testUpdate()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      BusinessTypeDTO dto = this.typeService.toDTO(type);
      dto.setDisplayLabel(new LocalizedValue("BDUB"));

      type = this.typeService.apply(dto);

      Assertions.assertEquals(dto.getCode(), type.getCode());
      Assertions.assertEquals(dto.getOrganization(), type.getServerOrganization().getCode());
      Assertions.assertEquals(dto.getDisplayLabel().getValue(), type.getLabel().getValue());
      Assertions.assertNotNull(type.getMdVertex());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testAddCharacterAttribute()
  {
    BusinessTypeDTO object = createDTO();

    BusinessType type = this.typeService.apply(object);

    try
    {
      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assertions.assertTrue(actual instanceof AttributeCharacterType);
      Assertions.assertEquals(expected.getCode(), actual.getCode());
      Assertions.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assertions.assertEquals(expected.isDefault(), actual.isDefault());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testAddDateAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeDateType expected = new AttributeDateType("testDate", new LocalizedValue("Test Date"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assertions.assertTrue(actual instanceof AttributeDateType);
      Assertions.assertEquals(expected.getCode(), actual.getCode());
      Assertions.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assertions.assertEquals(expected.isDefault(), actual.isDefault());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testAddIntegerAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeIntegerType expected = new AttributeIntegerType("testInteger", new LocalizedValue("Test Integer"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assertions.assertTrue(actual instanceof AttributeIntegerType);
      Assertions.assertEquals(expected.getCode(), actual.getCode());
      Assertions.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assertions.assertEquals(expected.isDefault(), actual.isDefault());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testAddFloatAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeFloatType expected = new AttributeFloatType("testFloat", new LocalizedValue("Test Float"), new LocalizedValue("Test True"), false, false, false, false);
      expected.setPrecision(10);
      expected.setScale(2);

      this.typeService.createAttributeType(type, expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assertions.assertTrue(actual instanceof AttributeFloatType);
      Assertions.assertEquals(expected.getCode(), actual.getCode());
      Assertions.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assertions.assertEquals(expected.isDefault(), actual.isDefault());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testUpdateCharacterAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeCharacterType original = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, original);

      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Characterzzzzzz"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.updateAttributeType(type, expected);

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assertions.assertTrue(actual instanceof AttributeCharacterType);
      Assertions.assertEquals(expected.getCode(), actual.getCode());
      Assertions.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assertions.assertEquals(expected.isDefault(), actual.isDefault());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  public void testListByOrg()
  {
    TestDataSet.executeRequestAsUser(FastTestDataset.USER_CGOV_RA, () -> {

      BusinessTypeDTO dto = createDTO();

      BusinessType type = this.typeService.apply(dto);

      try
      {
        List<OrganizationGroup<BusinessTypeDTO>> orgs = this.typeService.listByOrg();

        Assertions.assertTrue(orgs.size() > 0);

        OrganizationGroup<BusinessTypeDTO> org = orgs.stream().filter(o -> o.getCode().equals(dto.getOrganization())).findFirst().orElseThrow();

        List<BusinessTypeDTO> types = org.getTypes();

        Assertions.assertEquals(1, types.size());

        BusinessTypeDTO actual = types.get(0);

        Assertions.assertEquals(type.getCode(), actual.getCode());
      }
      finally
      {
        this.typeService.delete(type);
      }
    });
  }

  @Test
  @Request
  public void testToDTO() throws JsonProcessingException
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      BusinessTypeDTO dto = this.typeService.toDTO(type, true, false);

      Assertions.assertEquals(type.getCode(), dto.getCode());
      Assertions.assertNotNull(dto.getAttributes());

      // Test serializing
      String json = BusinessTypeDTO.toJson(dto);
      BusinessTypeDTO test = BusinessTypeDTO.parseJson(json);

      Assertions.assertNotNull(test);
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testToJsonWithAttributes()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      BusinessTypeDTO dto = this.typeService.toDTO(type, true, false);

      Assertions.assertEquals(type.getCode(), dto.getCode());
      Assertions.assertNotNull(dto.getAttributes());
      Assertions.assertTrue(dto.getAttributes().size() > 0);
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testRemoveAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, expected);

      Assertions.assertEquals(Long.valueOf(1), type.getSequence());

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertTrue(attributeMap.containsKey(expected.getCode()));

      this.typeService.removeAttributeType(type, expected.getCode());

      attributeMap = type.getAttributeMapAsDTO();

      Assertions.assertFalse(attributeMap.containsKey(expected.getCode()));
      Assertions.assertEquals(Long.valueOf(2), type.getSequence());

    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testRemove()
  {
    BusinessType type = this.typeService.apply(createDTO());

    this.typeService.delete(type);

    Assertions.assertTrue(this.typeService.getByCode(type.getCode()).isEmpty());
  }

  @Test
  @Request
  public void testGetAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, expected);

      AttributeType actual = type.getAttribute(expected.getCode()).map(t -> t.toDTO()).orElseThrow();

      Assertions.assertTrue(actual instanceof AttributeCharacterType);
      Assertions.assertEquals(expected.getCode(), actual.getCode());
      Assertions.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assertions.assertEquals(expected.isDefault(), actual.isDefault());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testSetLabelAttribute()
  {
    BusinessType type = this.typeService.apply(createDTO());

    try
    {
      AttributeCharacterType expected = new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false, false);

      this.typeService.createAttributeType(type, expected);
      this.typeService.setLabelAttribute(type, expected.getCode());

      Assertions.assertNotNull(type.getLabelAttribute());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

}
