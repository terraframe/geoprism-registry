package net.geoprism.registry.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.Assert;
import org.junit.Test;

import com.runwaysdk.session.Request;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;

public class XMLImporterTest
{
  @Request
  @Test
  public void testImport() throws IOException
  {
    Organization organization = new Organization();
    organization.setCode("TEST_ORG");
    organization.getDisplayLabel().setValue("Test Org");
    organization.apply();

    try (InputStream istream = this.getClass().getResourceAsStream("/xml/test-domain.xml"))
    {
      XMLImporter xmlImporter = new XMLImporter();
      List<ServerElement> results = xmlImporter.importXMLDefinitions(organization, istream);

      try
      {
        Assert.assertEquals(1, results.size());

        ServerGeoObjectType type = (ServerGeoObjectType) results.get(0);

        Assert.assertEquals("TEST_VILLAGE", type.getCode());
        Assert.assertEquals("Test Village", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assert.assertFalse(type.getIsPrivate());
        Assert.assertFalse(type.isGeometryEditable());

        Optional<AttributeType> oattribute = type.getAttribute("TEST_TEXT");

        Assert.assertTrue(oattribute.isPresent());

        AttributeType attributeType = oattribute.get();
        Assert.assertEquals("Test Text", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Text Description", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_BOOLEAN");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Boolean", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Boolean Description", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_INTEGER");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Integer", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Integer Description", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_DATE");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Date", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Date Description", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_DECIMAL");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Decimal", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Decimal Description", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
      }
      finally
      {
        for (ServerElement result : results)
        {
          result.delete();
        }
      }
    }
    finally
    {
      organization.delete();
    }

  }
}
