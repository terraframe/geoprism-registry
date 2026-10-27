/**
 *
 */
package net.geoprism.registry.service;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.InstanceTestClassExtension;
import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.AuthorityType;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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

    Assertions.assertEquals(source.getCode(), result.getCode());

    Assertions.assertTrue(this.service.getByCode(source.getCode()).isPresent());

    this.service.delete(result);

    Assertions.assertFalse(this.service.getByCode(source.getCode()).isPresent());
  }

  @Test
  @Request
  public void testNonRequiredDTO()
  {
    SourceAuthority source = createMock();
    source.setAuthorityType(null);

    SourceAuthorityDTO dto = this.service.toDTO(source);
    dto.setOid(null);

    Assertions.assertNull(dto.getAuthorityType());

    SourceAuthority result = this.service.apply(dto);

    try
    {
      Assertions.assertNull(result.getAuthorityType());
    }
    finally
    {
      this.service.delete(result);
    }
  }

  public static SourceAuthority createMock()
  {
    SourceAuthority source = new SourceAuthority();
    source.setCode("ABCD");
    LocalizedValueConverter.populate(source, SourceAuthority.DISPLAYLABEL, new LocalizedValue("Test Label"));
    LocalizedValueConverter.populate(source, SourceAuthority.DESCRIPTION, new LocalizedValue("Test Description"));
    source.setAuthorityType(AuthorityType.COMMUNITY.name());

    return source;
  }
}
