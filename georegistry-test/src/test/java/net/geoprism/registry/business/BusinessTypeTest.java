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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runwaysdk.session.Request;

import net.geoprism.configuration.GeoprismProperties;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;
import net.geoprism.registry.test.TestDataSet;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.OrganizationGroup;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
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
      Assert.assertEquals(dto.getCode(), type.getCode());
      Assert.assertEquals(dto.getOrganization(), type.getServerOrganization().getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), type.getLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
      Assert.assertEquals(type.getOrigin(), GeoprismProperties.getOrigin());
      Assert.assertEquals(Long.valueOf(0), type.getSequence());
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
      Assert.assertNotNull(this.typeService.getByCode(type.getCode()));
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

      Assert.assertEquals(dto.getCode(), type.getCode());
      Assert.assertEquals(dto.getOrganization(), type.getServerOrganization().getCode());
      Assert.assertEquals(dto.getDisplayLabel().getValue(), type.getLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
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

      Assert.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assert.assertTrue(actual instanceof AttributeCharacterType);
      Assert.assertEquals(expected.getCode(), actual.getCode());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.isDefault(), actual.isDefault());
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

      Assert.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assert.assertTrue(actual instanceof AttributeDateType);
      Assert.assertEquals(expected.getCode(), actual.getCode());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.isDefault(), actual.isDefault());
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

      Assert.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assert.assertTrue(actual instanceof AttributeIntegerType);
      Assert.assertEquals(expected.getCode(), actual.getCode());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.isDefault(), actual.isDefault());
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

      Assert.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assert.assertTrue(actual instanceof AttributeFloatType);
      Assert.assertEquals(expected.getCode(), actual.getCode());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.isDefault(), actual.isDefault());
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

      Assert.assertTrue(attributeMap.containsKey(expected.getCode()));

      AttributeType actual = attributeMap.get(expected.getCode());

      Assert.assertTrue(actual instanceof AttributeCharacterType);
      Assert.assertEquals(expected.getCode(), actual.getCode());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.isDefault(), actual.isDefault());
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

        Assert.assertTrue(orgs.size() > 0);

        OrganizationGroup<BusinessTypeDTO> org = orgs.stream().filter(o -> o.getCode().equals(dto.getOrganization())).findFirst().orElseThrow();

        List<BusinessTypeDTO> types = org.getTypes();

        Assert.assertEquals(1, types.size());

        BusinessTypeDTO actual = types.get(0);

        Assert.assertEquals(type.getCode(), actual.getCode());
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

      Assert.assertEquals(type.getCode(), dto.getCode());
      Assert.assertNotNull(dto.getAttributes());

      // Test serializing
      String json = BusinessTypeDTO.toJson(dto);
      BusinessTypeDTO test = BusinessTypeDTO.parseJson(json);

      Assert.assertNotNull(test);
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

      Assert.assertEquals(type.getCode(), dto.getCode());
      Assert.assertNotNull(dto.getAttributes());
      Assert.assertTrue(dto.getAttributes().size() > 0);
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

      Assert.assertEquals(Long.valueOf(1), type.getSequence());

      Map<String, AttributeType> attributeMap = type.getAttributeMapAsDTO();

      Assert.assertTrue(attributeMap.containsKey(expected.getCode()));

      this.typeService.removeAttributeType(type, expected.getCode());

      attributeMap = type.getAttributeMapAsDTO();

      Assert.assertFalse(attributeMap.containsKey(expected.getCode()));
      Assert.assertEquals(Long.valueOf(2), type.getSequence());

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

    Assert.assertTrue(this.typeService.getByCode(type.getCode()).isEmpty());
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

      Assert.assertTrue(actual instanceof AttributeCharacterType);
      Assert.assertEquals(expected.getCode(), actual.getCode());
      Assert.assertEquals(expected.getLabel().getValue(), actual.getLabel().getValue());
      Assert.assertEquals(expected.isDefault(), actual.isDefault());
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

      Assert.assertNotNull(type.getLabelAttribute());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

}
