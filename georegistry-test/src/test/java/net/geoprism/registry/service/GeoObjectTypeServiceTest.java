/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
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
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.test.USATestData;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class GeoObjectTypeServiceTest implements InstanceTestClassListener
{
  @Autowired
  private GeoObjectTypeBusinessServiceIF service;

  @Override
  public void beforeClassSetup() throws Exception
  {
    USATestData.ORG_NPS.apply();
  }

  @Override
  public void afterClassSetup() throws Exception
  {
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
  public void testCreateDeleteAttribute()
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
