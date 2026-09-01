/**
 *
 */
package net.geoprism.registry.xml;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GraphRepoServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.test.TestOrganizationInfo;
import net.geoprism.registry.test.USATestData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class XMLImporterTest extends ConceptDatasetTest implements InstanceTestClassListener
{

  @Autowired
  private GraphRepoServiceIF                graphRepo;

  @Autowired
  private HierarchyTypeBusinessServiceIF    hierarchyBizService;

  @Autowired
  private BusinessTypeBusinessServiceIF     bizService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bizEdgeService;

  @Override
  protected TestOrganizationInfo getOrganization()
  {
    return new TestOrganizationInfo("TEST_ORG");
  }

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    this.getOrganization().apply();

    super.beforeClassSetup();
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    super.afterClassSetup();

    USATestData.ORG_NPS.delete();
  }

  @Request
  @Test
  public void testImportAndExport() throws Exception
  {
    try (InputStream istream = this.getClass().getResourceAsStream("/xml/test-domain.xml"))
    {
      XMLImporter xmlImporter = new XMLImporter();

      List<ServerElement> results = xmlImporter.importXMLDefinitions(new StreamResource(istream, "test-domain.xml"), this.getOrganization().getServerObject());

      try
      {
        graphRepo.refreshMetadataCache();

        Assert.assertEquals(10, results.size());

        ServerGeoObjectType type = ServerGeoObjectType.get("TEST_VILLAGE");

        Assert.assertEquals("TEST_VILLAGE", type.getCode());
        Assert.assertEquals("Test Village", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assert.assertFalse(type.getIsPrivate());
        Assert.assertFalse(type.isGeometryEditable());
        Assert.assertTrue(type.getIsAbstract());

        Optional<AttributeType> oattribute = type.getAttribute("TEST_TEXT");

        Assert.assertTrue(oattribute.isPresent());

        AttributeType attributeType = oattribute.get();
        Assert.assertEquals("Test Text", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Text Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_BOOLEAN");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Boolean", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Boolean Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_INTEGER");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Integer", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Integer Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_DATE");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Date", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Date Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_DECIMAL");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Decimal", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Decimal Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        type = ServerGeoObjectType.get("TEST_GI");

        Assert.assertEquals("TEST_GI", type.getCode());
        Assert.assertEquals("Test GI", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assert.assertFalse(type.getIsPrivate());
        Assert.assertFalse(type.isGeometryEditable());
        Assert.assertFalse(type.getIsAbstract());
        Assert.assertEquals("TEST_VILLAGE", type.getSuperType().getCode());

        ServerHierarchyType hierarchy = ServerHierarchyType.get("TEST_HIERARCHY");

        Assert.assertEquals("TEST_HIERARCHY", hierarchy.getCode());
        Assert.assertEquals("Test Hierarchy", hierarchy.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Hierarchy Description", hierarchy.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Progress", hierarchy.getProgress());
        Assert.assertEquals("Test Disclaimer", hierarchy.getDisclaimer());
        Assert.assertEquals("Test Access Constraints", hierarchy.getAccessConstraints());
        Assert.assertEquals("Test Use Constraints", hierarchy.getUseConstraints());
        Assert.assertEquals("Test Acknowledgement", hierarchy.getAcknowledgement());

        List<HierarchyNode> nodes = hierarchyBizService.getRootGeoObjectTypes(hierarchy);

        Assert.assertEquals(1, nodes.size());

        HierarchyNode node = nodes.get(0);

        Assert.assertEquals("TEST_DISTRICT", node.getGeoObjectType().getCode());

        nodes = node.getChildren();

        Assert.assertEquals(1, nodes.size());

        node = nodes.get(0);

        Assert.assertEquals("TEST_VILLAGE", node.getGeoObjectType().getCode());

        BusinessType businessType = bizService.getByCodeOrThrow("BUSINESS_POP");

        Assert.assertEquals("BUSINESS_POP", businessType.getCode());
        Assert.assertEquals("Business Pop", businessType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("TEST_TEXT", businessType.getLabelAttribute().getAttributeName());
        Assert.assertEquals(7, businessType.getAttributes().size());

        AttributeType businessAttribute = businessType.getAttribute("TEST_TEXT").orElseThrow();

        Assert.assertEquals("Test Text", businessAttribute.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Text Description", businessAttribute.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        BusinessEdgeType businessEdge = bizEdgeService.getByCodeOrThrow("BUS_EDGE");
        Assert.assertEquals("BUS_EDGE", businessEdge.getCode());

        BusinessEdgeType businessGeoEdge = bizEdgeService.getByCodeOrThrow("BUS_GEO_EDGE");
        Assert.assertEquals("BUS_GEO_EDGE", businessGeoEdge.getCode());

        ConceptClass conceptClass = cClassService.getByCodeOrThrow("CONCEPT_POP");

        Assert.assertEquals("CONCEPT_POP", conceptClass.getCode());
        Assert.assertEquals("Concept Pop", conceptClass.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals(7, conceptClass.getAttributes().size());

        AttributeType conceptAttribute = conceptClass.getAttribute("TEST_TEXT").orElseThrow();

        Assert.assertEquals("Test Text", conceptAttribute.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Text Description", conceptAttribute.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        ConceptEdgeType conceptEdge = cEdgeTypeService.getByCodeOrThrow("CONCEPT_POP_EDGE");
        Assert.assertEquals("CONCEPT_POP_EDGE", conceptEdge.getCode());

        XMLExporter exporter = new XMLExporter(this.getOrganization().getServerObject());
        exporter.build();

        File file = File.createTempFile("test", ".xml");

        try
        {
          exporter.write(file);

          System.out.println(FileUtils.readFileToString(file, "UTF-8"));
        }
        finally
        {
          FileUtils.deleteQuietly(file);
        }

      }
      finally
      {
        deleteObjects(results);
      }
    }
  }

  protected void deleteObjects(List<ServerElement> results) throws InterruptedException
  {
    Collections.reverse(results);

    for (ServerElement result : results)
    {
      graphRepo.deleteObject(result);
    }
  }
}
