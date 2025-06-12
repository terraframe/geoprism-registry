/**
 *
 */
package net.geoprism.registry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.test.CambodiaTestDataset;

@ContextConfiguration(classes = { TestConfig.class }) @WebAppConfiguration
@RunWith(SpringInstanceTestClassRunner.class)
public class BuildCambodiaDataset
{
  @Test
  @Request
  public void testBuildDataset() throws Exception
  {
    CambodiaTestDataset data = CambodiaTestDataset.newTestData();
    data.setUpMetadata();
    data.setUpInstanceData();
  }
}
