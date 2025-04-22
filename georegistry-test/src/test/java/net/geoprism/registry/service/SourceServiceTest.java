/**
 *
 */
package net.geoprism.registry.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.graph.Source;
import net.geoprism.registry.service.business.SourceBusinessServiceIF;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class SourceServiceTest implements InstanceTestClassListener
{

  @Autowired
  private SourceBusinessServiceIF service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
  }

  @Test
  @Request
  public void testCreateDeleteSource()
  {
    Source source = createMock();

    JsonObject json = this.service.toJSON(source);
    json.remove("oid");

    Source result = this.service.apply(json);

    Assert.assertEquals(source.getCode(), result.getCode());

    Assert.assertTrue(this.service.getByCode(source.getCode()).isPresent());

    this.service.delete(result);

    Assert.assertFalse(this.service.getByCode(source.getCode()).isPresent());
  }

  public static Source createMock()
  {
    Source source = new Source();
    source.setCode("ABCD");

    return source;
  }
}
