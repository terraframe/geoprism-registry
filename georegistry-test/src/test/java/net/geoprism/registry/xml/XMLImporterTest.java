/**
 *
 */
package net.geoprism.registry.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyNode;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.classification.ClassificationTypeTest;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.service.request.GraphRepoServiceIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class XMLImporterTest implements InstanceTestClassListener
{

  private ClassificationType                  type      = null;

  private String                              ROOT_CODE = "Test_Classification";

  private boolean                             isSetup   = false;

  @Autowired
  private GraphRepoServiceIF                  graphRepo;

  @Autowired
  private OrganizationBusinessServiceIF       orgService;

  @Autowired
  private HierarchyTypeBusinessServiceIF      hierarchyBizService;

  @Autowired
  private BusinessTypeBusinessServiceIF       bizService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF   bizEdgeService;

  @Autowired
  private ClassificationTypeBusinessServiceIF cTypeService;

  @Autowired
  private ClassificationBusinessServiceIF     cService;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    // This is a hack to allow for spring injection of classification tasks
    if (!isSetup)
    {
      setupClasses();
    }
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (type != null)
    {
      this.cTypeService.delete(type);

      type = null;
    }
  }

  public void setupClasses()
  {
    setUpClassInRequest();
  }

  @Request
  private void setUpClassInRequest()
  {
    type = this.cTypeService.apply(ClassificationTypeTest.createMock());

    Classification root = this.cService.newInstance(type);
    root.setCode(ROOT_CODE);
    root.setDisplayLabel(new LocalizedValue("Test Classification"));
    this.cService.apply(root, null);

    isSetup = true;
  }

  @Request
  @Test
  public void testImportAndExport() throws IOException
  {
    // ServerOrganization organization = new ServerOrganization();
    // organization.setCode("TEST_ORG");
    // organization.getDisplayLabel().setValue("Test Org");
    // organization.apply();

    OrganizationDTO org = new OrganizationDTO("TEST_ORG", new LocalizedValue("Test Org"), new LocalizedValue(""), true, null, new LocalizedValue(""));
    ServerOrganization serverOrg = orgService.create(org);

    try (InputStream istream = this.getClass().getResourceAsStream("/xml/test-domain.xml"))
    {
      XMLImporter xmlImporter = new XMLImporter();

      List<ServerElement> results = xmlImporter.importXMLDefinitions(serverOrg, new StreamResource(istream, "test-domain.xml"));

      try
      {
        graphRepo.refreshMetadataCache();

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

        oattribute = type.getAttribute("TEST_TERM");

        Assert.assertTrue(oattribute.isPresent());

        attributeType = oattribute.get();
        Assert.assertEquals("Test Term", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("Test Term Description", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));

        // TODO: HEADS UP
//        List<Term> terms = ( (AttributeTermType) attributeType ).getTerms();
//
//        Assert.assertEquals(3, terms.size());
//
//        oattribute = type.getAttribute("TEST_CLASSIFICATION");
//
//        Assert.assertTrue(oattribute.isPresent());
//
//        attributeType = oattribute.get();
//        Assert.assertEquals("Test Classification", attributeType.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
//        Assert.assertEquals("Test Text Classification", attributeType.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
//        Assert.assertEquals("TEST_PROG", ( (AttributeClassificationType) attributeType ).getClassificationType());
//        Assert.assertEquals(ROOT_CODE, ( (AttributeClassificationType) attributeType ).getRootTerm().getCode());

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

        BusinessType businessType = bizService.getByCode(results.get(4).getCode());

        Assert.assertEquals("BUSINESS_POP", businessType.getCode());
        Assert.assertEquals("Business Pop", businessType.getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
        Assert.assertEquals("TEST_TEXT", businessType.getLabelAttribute().getAttributeName());

        // TODO: HEADS UP
//        AttributeType businessAttribute = businessType.getAttribute("TEST_TEXT");
//
//        Assert.assertEquals("Test Text", businessAttribute.getLocalizedLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
//        Assert.assertEquals("Test Text Description", businessAttribute.getLocalizedDescription().getValue(LocalizedValue.DEFAULT_LOCALE));
//
//        BusinessEdgeType businessEdge = bizEdgeService.getByCode(results.get(6).getCode());
//        Assert.assertEquals("BUS_EDGE", businessEdge.getCode());

        XMLExporter exporter = new XMLExporter(serverOrg);
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
        Collections.reverse(results);

        for (ServerElement result : results)
        {
          graphRepo.deleteObject(result);
        }
      }
    }
    finally
    {
      serverOrg.delete();
    }

  }
}
