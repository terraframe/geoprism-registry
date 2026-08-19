package net.geoprism.registry.hierarchy;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.Ontology;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.service.business.OntologyBusinessServiceIF;
import net.geoprism.registry.view.ConceptSetDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class OntologyTest extends ConceptSetTest<Ontology, ConceptSetDTO>
{
  @Autowired
  private OntologyBusinessServiceIF service;

  @Override
  protected ConceptSetBusinessServiceIF<Ontology, ConceptSetDTO> getService()
  {
    return this.service;
  }

}
