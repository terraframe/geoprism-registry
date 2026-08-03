/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
public class SourceAuthorityServiceTest implements InstanceTestClassListener
{

  @Autowired
  private SourceAuthorityBusinessServiceIF service;

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
    SourceAuthority source = createMock();

    SourceAuthorityDTO dto = this.service.toDTO(source);
    dto.setOid(null);

    SourceAuthority result = this.service.apply(dto);

    Assert.assertEquals(source.getCode(), result.getCode());

    Assert.assertTrue(this.service.getByCode(source.getCode()).isPresent());

    this.service.delete(result);

    Assert.assertFalse(this.service.getByCode(source.getCode()).isPresent());
  }

  public static SourceAuthority createMock()
  {
    SourceAuthority source = new SourceAuthority();
    source.setCode("ABCD");
    LocalizedValueConverter.populate(source, SourceAuthority.DISPLAYLABEL, new LocalizedValue("Test Label"));
    LocalizedValueConverter.populate(source, SourceAuthority.DESCRIPTION, new LocalizedValue("Test Description"));
    source.setAuthorityType("Test Authority Type");

    return source;
  }
}
