package net.geoprism.registry.hierarchy;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.graph.Taxonomy;
import net.geoprism.registry.service.business.ConceptSetBusinessServiceIF;
import net.geoprism.registry.service.business.TaxonomyBusinessServiceIF;
import net.geoprism.registry.view.ConceptSetDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class TaxonomyTest extends ConceptSetTest<Taxonomy, ConceptSetDTO>
{
  @Autowired
  private TaxonomyBusinessServiceIF service;

  @Override
  protected ConceptSetBusinessServiceIF<Taxonomy, ConceptSetDTO> getService()
  {
    return this.service;
  }

}
