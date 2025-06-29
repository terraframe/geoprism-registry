/**
 *
 */
package net.geoprism.registry.service;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.service.business.LabeledPropertyGraphTypeVersionBusinessServiceIF;
import net.geoprism.registry.service.business.ManyToManyLabeledPropertyGraphRDFExportBusinessService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc

@RunWith(SpringInstanceTestClassRunner.class)
public class ManyToManyLabeledPropertyGraphRDFExportBusinessServiceTest
{
  @Autowired
  private ManyToManyLabeledPropertyGraphRDFExportBusinessService service;

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF       versionService;

//  @Test
//  @Request
//  public void testSingleLabeledPropertyGraphTypeSerialization() throws Exception
//  {
//    this.versionService.getAll().stream().findAny().ifPresent(version -> {
//
//      File directory = new File("rdf");
//      directory.mkdirs();
//
//      try (FileOutputStream ostream = new FileOutputStream(new File(directory, version.getOid() + ".trig")))
//      {
//        this.service.export(version, GeometryExportType.WRITE_GEOMETRIES, ostream);
//      }
//      catch (IOException e)
//      {
//        throw new RuntimeException(e);
//      }
//    });
//
//  }
//
}
