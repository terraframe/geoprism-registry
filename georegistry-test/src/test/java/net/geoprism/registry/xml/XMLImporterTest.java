/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.Organization;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.RegistryService;

public class XMLImporterTest
{

  private static ClassificationType type      = null;

  private static String             ROOT_CODE = "Test_Classification";

  @Request
  @BeforeClass
  public static void classSetUp()
  {
    type = ClassificationType.apply(ClassificationTypeTest.createMock());

    Classification root = Classification.newInstance(type);
    root.setCode(ROOT_CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    root.apply(null);
  }

  @AfterClass
  @Request
  public static void classTearDown()
  {
    if (type != null)
    {
      type.delete();

      type = null;
    }
  }

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

      List<ServerElement> results = xmlImporter.importXMLDefinitions(organization, new StreamResource(istream, "test-domain.xml"));

      try
      {
        RegistryService.getInstance().refreshMetadataCache();

        Assert.assertEquals(7, results.size());

        ServerGeoObjectType type = ServerGeoObjectType.get(results.get(0).getCode());

        Assert.assertEquals("TEST_VILLAGE", type.getCode());
        Assert.assertEquals("Test Village", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assert.assertFalse(type.getIsPrivate());
        Assert.assertFalse(type.isGeometryEditable());
        Assert.assertTrue(type.getIsAbstract());

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

        oattribute = type.getAttribute("TEST_TERM");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Term", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Term Description", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        List<Term> terms = ( (AttributeTermType) attributeType ).getTerms();

        Assert.assertEquals(3, terms.size());

        oattribute = type.getAttribute("TEST_CLASSIFICATION");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Classification", attributeType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Text Classification", attributeType.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("TEST_PROG", ( (AttributeClassificationType) attributeType ).getClassificationType());
        Assert.assertEquals(ROOT_CODE, ( (AttributeClassificationType) attributeType ).getRootTerm().getCode());

        type = ServerGeoObjectType.get(results.get(1).getCode());

        Assert.assertEquals("TEST_GI", type.getCode());
        Assert.assertEquals("Test GI", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assert.assertFalse(type.getIsPrivate());
        Assert.assertFalse(type.isGeometryEditable());
        Assert.assertFalse(type.getIsAbstract());
        Assert.assertEquals("TEST_VILLAGE", type.getSuperType().getCode());

        ServerHierarchyType hierarchy = ServerHierarchyType.get(results.get(3).getCode());

        Assert.assertEquals("TEST_HIERARCHY", hierarchy.getCode());
        Assert.assertEquals("Test Hierarchy", hierarchy.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Hierarchy Description", hierarchy.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Progress", hierarchy.getProgress());
        Assert.assertEquals("Test Disclaimer", hierarchy.getDisclaimer());
        Assert.assertEquals("Test Access Constraints", hierarchy.getAccessConstraints());
        Assert.assertEquals("Test Use Constraints", hierarchy.getUseConstraints());
        Assert.assertEquals("Test Acknowledgement", hierarchy.getAcknowledgement());

        List<HierarchyNode> nodes = hierarchy.getRootGeoObjectTypes();

        Assert.assertEquals(1, nodes.size());

        HierarchyNode node = nodes.get(0);

        Assert.assertEquals("TEST_DISTRICT", node.getGeoObjectType().getCode());

        nodes = node.getChildren();

        Assert.assertEquals(1, nodes.size());

        node = nodes.get(0);

        Assert.assertEquals("TEST_VILLAGE", node.getGeoObjectType().getCode());

        BusinessType businessType = BusinessType.getByCode(results.get(4).getCode());

        Assert.assertEquals("BUSINESS_POP", businessType.getCode());
        Assert.assertEquals("Business Pop", businessType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));

        AttributeType businessAttribute = businessType.getAttribute("TEST_TEXT");

        Assert.assertEquals("Test Text", businessAttribute.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Text Description", businessAttribute.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        BusinessEdgeType businessEdge = BusinessEdgeType.getByCode(results.get(6).getCode());
        Assert.assertEquals("BUS_EDGE", businessEdge.getCode());
      }
      finally
      {
        Collections.reverse(results);

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
