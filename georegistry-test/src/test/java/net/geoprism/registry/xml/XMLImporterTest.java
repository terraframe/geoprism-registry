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
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;

import net.geoprism.registry.ConceptDatasetTest;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.view.DiscreteType;
import net.geoprism.registry.graph.AttributeClassificationType;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.graph.ConceptEdgeType;
import net.geoprism.registry.graph.ConceptSet;
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

@ExtendWith(InstanceTestClassExtension.class)
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

        Assertions.assertEquals(10, results.size());

        ServerGeoObjectType type = ServerGeoObjectType.get("TEST_VILLAGE");

        Assertions.assertEquals("TEST_VILLAGE", type.getCode());
        Assertions.assertEquals("Test Village", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assertions.assertFalse(type.getIsPrivate());
        Assertions.assertFalse(type.isGeometryEditable());
        Assertions.assertTrue(type.getIsAbstract());

        Optional<AttributeType> oattribute = type.getAttribute("TEST_TEXT");

        Assertions.assertTrue(oattribute.isPresent());

        AttributeType attributeType = oattribute.get();
        Assertions.assertEquals("Test Text", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Text Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_BOOLEAN");

        Assertions.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assertions.assertEquals("Test Boolean", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Boolean Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_INTEGER");

        Assertions.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assertions.assertEquals("Test Integer", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Integer Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_DATE");

        Assertions.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assertions.assertEquals("Test Date", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Date Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        oattribute = type.getAttribute("TEST_DECIMAL");

        Assertions.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assertions.assertEquals("Test Decimal", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Decimal Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        type = ServerGeoObjectType.get("TEST_GI");

        Assertions.assertEquals("TEST_GI", type.getCode());
        Assertions.assertEquals("Test GI", type.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals(GeometryType.MULTIPOINT, type.getGeometryType());
        Assertions.assertFalse(type.getIsPrivate());
        Assertions.assertFalse(type.isGeometryEditable());
        Assertions.assertFalse(type.getIsAbstract());
        Assertions.assertEquals("TEST_VILLAGE", type.getSuperType().getCode());

        ServerHierarchyType hierarchy = ServerHierarchyType.get("TEST_HIERARCHY");

        Assertions.assertEquals("TEST_HIERARCHY", hierarchy.getCode());
        Assertions.assertEquals("Test Hierarchy", hierarchy.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Hierarchy Description", hierarchy.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Progress", hierarchy.getProgress());
        Assertions.assertEquals("Test Disclaimer", hierarchy.getDisclaimer());
        Assertions.assertEquals("Test Access Constraints", hierarchy.getAccessConstraints());
        Assertions.assertEquals("Test Use Constraints", hierarchy.getUseConstraints());
        Assertions.assertEquals("Test Acknowledgement", hierarchy.getAcknowledgement());

        List<HierarchyNode> nodes = hierarchyBizService.getRootGeoObjectTypes(hierarchy);

        Assertions.assertEquals(1, nodes.size());

        HierarchyNode node = nodes.get(0);

        Assertions.assertEquals("TEST_DISTRICT", node.getGeoObjectType().getCode());

        nodes = node.getChildren();

        Assertions.assertEquals(1, nodes.size());

        node = nodes.get(0);

        Assertions.assertEquals("TEST_VILLAGE", node.getGeoObjectType().getCode());

        BusinessType businessType = bizService.getByCodeOrThrow("BUSINESS_POP");

        Assertions.assertEquals("BUSINESS_POP", businessType.getCode());
        Assertions.assertEquals("Business Pop", businessType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("TEST_TEXT", businessType.getLabelAttribute().getAttributeName());
        Assertions.assertEquals(7, businessType.getAttributes().size());

        AttributeType businessAttribute = businessType.getAttribute("TEST_TEXT").orElseThrow();

        Assertions.assertEquals("Test Text", businessAttribute.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Text Description", businessAttribute.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        BusinessEdgeType businessEdge = bizEdgeService.getByCodeOrThrow("BUS_EDGE");
        Assertions.assertEquals("BUS_EDGE", businessEdge.getCode());

        BusinessEdgeType businessGeoEdge = bizEdgeService.getByCodeOrThrow("BUS_GEO_EDGE");
        Assertions.assertEquals("BUS_GEO_EDGE", businessGeoEdge.getCode());

        ConceptClass conceptClass = cClassService.getByCodeOrThrow("CONCEPT_POP");

        Assertions.assertEquals("CONCEPT_POP", conceptClass.getCode());
        Assertions.assertEquals("Concept Pop", conceptClass.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals(8, conceptClass.getAttributes().size());

        AttributeType conceptAttribute = conceptClass.getAttribute("TEST_TEXT").orElseThrow();

        Assertions.assertEquals("Test Text", conceptAttribute.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Text Description", conceptAttribute.getDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        ConceptEdgeType conceptEdge = cEdgeTypeService.getByCodeOrThrow("CONCEPT_POP_EDGE");
        Assertions.assertEquals("CONCEPT_POP_EDGE", conceptEdge.getCode());

        // Validate the concept set
        ConceptSet conceptSet = cSetService.getByCodeOrThrow("CONCEPT_POP_SET");

        Assertions.assertEquals("CONCEPT_POP_SET", conceptSet.getCode());
        Assertions.assertEquals("Concept Pop Set", conceptSet.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Concept Pop Set Description", conceptSet.getDescriptionLV().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals(DiscreteType.TAXONOMY.name(), conceptSet.getDiscreteType());

        List<ConceptClass> setClasses = cSetService.getConceptClasses(conceptSet);

        Assertions.assertEquals(1, setClasses.size());
        Assertions.assertEquals("CONCEPT_POP", setClasses.get(0).getCode());

        List<ConceptEdgeType> setEdges = cSetService.getConceptEdgeTypes(conceptSet);

        Assertions.assertEquals(1, setEdges.size());
        Assertions.assertEquals("CONCEPT_POP_EDGE", setEdges.get(0).getCode());

        // Validate the classification of the geo-object type
        type = ServerGeoObjectType.get("TEST_VILLAGE");

        AttributeClassificationType classification = (AttributeClassificationType) type.getAttribute(DefaultAttribute.CLASSIFICATION.getName()).orElseThrow();

        Assertions.assertEquals("CONCEPT_POP_SET", classification.getConceptSet());
        Assertions.assertEquals("2000-01-01", GeoRegistryUtil.formatDate(classification.getStartDate(), false));
        Assertions.assertEquals("2025-12-31", GeoRegistryUtil.formatDate(classification.getEndDate(), false));

        // Validate the classification attribute
        AttributeClassificationType classificationAttribute = (AttributeClassificationType) type.getAttribute("TEST_CLASSIFICATION").orElseThrow();

        Assertions.assertEquals("Test Classification", classificationAttribute.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("Test Classification Description", classificationAttribute.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assertions.assertEquals("CONCEPT_POP_SET", classificationAttribute.getConceptSet());
        Assertions.assertEquals("2000-01-01", GeoRegistryUtil.formatDate(classificationAttribute.getStartDate(), false));
        Assertions.assertEquals("2025-12-31", GeoRegistryUtil.formatDate(classificationAttribute.getEndDate(), false));

        XMLExporter exporter = new XMLExporter(this.getOrganization().getServerObject());
        exporter.build();

        // Validate the exported classification of TEST_VILLAGE
        Element villageElement = findElement(exporter.getRoot(), "type", "TEST_VILLAGE");

        Assertions.assertEquals("CONCEPT_POP_SET", villageElement.getAttribute("conceptSet"));
        Assertions.assertEquals("2000-01-01", villageElement.getAttribute("startDate"));
        Assertions.assertEquals("2025-12-31", villageElement.getAttribute("endDate"));

        Element classificationElement = findElement(villageElement, "classification", "TEST_CLASSIFICATION");

        Assertions.assertEquals("Test Classification", classificationElement.getAttribute("label"));
        Assertions.assertEquals("CONCEPT_POP_SET", classificationElement.getAttribute("conceptSet"));
        Assertions.assertEquals("2000-01-01", classificationElement.getAttribute("startDate"));
        Assertions.assertEquals("2025-12-31", classificationElement.getAttribute("endDate"));

        // Validate the exported concept set
        Element conceptSetElement = findElement(exporter.getRoot(), "concept-set", "CONCEPT_POP_SET");

        Assertions.assertEquals("Concept Pop Set", conceptSetElement.getAttribute("label"));
        Assertions.assertEquals("Concept Pop Set Description", conceptSetElement.getAttribute("description"));
        Assertions.assertEquals(DiscreteType.TAXONOMY.name(), conceptSetElement.getAttribute("discreteType"));
        Assertions.assertEquals("CONCEPT_POP", conceptSetElement.getAttribute("conceptClass"));
        Assertions.assertEquals("CONCEPT_POP_EDGE", conceptSetElement.getAttribute("conceptEdgeType"));

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

  protected Element findElement(Element parent, String tagName, String code)
  {
    NodeList nodes = parent.getElementsByTagName(tagName);

    for (int i = 0; i < nodes.getLength(); i++)
    {
      Element element = (Element) nodes.item(i);

      if (code.equals(element.getAttribute("code")))
      {
        return element;
      }
    }

    return Assertions.fail("Unable to find exported element [" + tagName + "] with code [" + code + "]");
  }

  protected void deleteObjects(List<ServerElement> results) throws InterruptedException
  {
    Collections.reverse(results);

    // The concept set is not a ServerElement, so it is not included in the
    // import results. It must be deleted after the geo-object types which
    // reference it, but before the concept class and concept edge type which
    // it references.
    for (ServerElement result : results)
    {
      if (! ( result instanceof ConceptClass || result instanceof ConceptEdgeType ))
      {
        graphRepo.deleteObject(result);
      }
    }

    cSetService.getByCode("CONCEPT_POP_SET").ifPresent(cSetService::delete);

    for (ServerElement result : results)
    {
      if (result instanceof ConceptClass || result instanceof ConceptEdgeType)
      {
        graphRepo.deleteObject(result);
      }
    }
  }
}
