/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;

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
import net.geoprism.registry.exception.RequiredSourceAuthorityException;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.model.GovernanceLevel;
import net.geoprism.registry.model.MetadataProfile;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
public class DataSourceServiceTest implements InstanceTestClassListener
{

  @Autowired
  private SourceAuthorityBusinessServiceIF authorityService;

  private static SourceAuthority           authority;

  @Autowired
  private DataSourceBusinessServiceIF      service;

  @Override
  @Request
  public void beforeClassSetup() throws Exception
  {
    authority = this.authorityService.apply(SourceAuthorityServiceTest.createMock());
  }

  @Override
  @Request
  public void afterClassSetup() throws Exception
  {
    if (authority != null)
    {
      this.authorityService.delete(authority);
    }
  }

  @Test
  @Request
  public void testCreateDeleteSource()
  {
    DataSource source = createMock(authority);

    DataSourceDTO json = this.service.toDTO(source);
    json.setOid(null);

    DataSource result = this.service.apply(json);

    Assertions.assertEquals(source.getCode(), result.getCode());

    Assertions.assertTrue(this.service.getByCode(source.getCode()).isPresent());

    this.service.delete(result);

    Assertions.assertFalse(this.service.getByCode(source.getCode()).isPresent());
  }

  @Test
  @Request
  public void testNonRequiredToDTO()
  {
    DataSource source = createMock(authority);
    source.setGovernanceLevel(null);
    source.setMetadataProfile(null);

    DataSourceDTO dto = this.service.toDTO(source);
    dto.setOid(null);

    Assertions.assertNull(dto.getGovernanceLevel());
    Assertions.assertNull(dto.getMetadataProfile());

    DataSource result = this.service.apply(dto);

    try
    {
      Assertions.assertEquals(source.getCode(), result.getCode());
      Assertions.assertNull(result.getMetadataProfile());
      Assertions.assertNull(result.getGovernanceLevel());
    }
    finally
    {
      this.service.delete(result);
    }
  }

  @Test
  @Request
  public void testSearch()
  {
    DataSource source = createMock(authority);

    DataSourceDTO json = this.service.toDTO(source);
    json.setOid(null);

    source = this.service.apply(json);

    try
    {
      List<DataSource> results = this.service.search(source.getCode());

      Assertions.assertEquals(1, results.size());
      Assertions.assertEquals(source.getCode(), results.get(0).getCode());
    }
    finally
    {
      this.service.delete(source);
    }
  }

  @Test
  @Request
  public void testCountInUse()
  {
    DataSource source = createMock(authority);

    DataSourceDTO json = this.service.toDTO(source);
    json.setOid(null);

    source = this.service.apply(json);

    try
    {
      Assertions.assertEquals(Long.valueOf(1), this.authorityService.getUseCount(authority));
    }
    finally
    {
      this.service.delete(source);
    }
  }

  @Test
  @Request
  public void testRequiredSourceAuthorityException()
  {
    assertThrows(RequiredSourceAuthorityException.class, () -> {
      DataSource source = createMock(authority);

      DataSourceDTO json = this.service.toDTO(source);
      json.setOid(null);

      source = this.service.apply(json);

      try
      {
        this.authorityService.delete(authority);
      }
      finally
      {
        this.service.delete(source);
      }
    });
  }

  public static DataSource createMock(SourceAuthority authority)
  {
    DataSource source = new DataSource();
    source.setCode("ABCD");
    source.setAuthority(authority);
    LocalizedValueConverter.populate(source, SourceAuthority.DISPLAYLABEL, new LocalizedValue("Test Label"));
    LocalizedValueConverter.populate(source, SourceAuthority.DESCRIPTION, new LocalizedValue("Test Description"));
    source.setGovernanceLevel(GovernanceLevel.AD_HOC.name());
    source.setMetadataProfile(MetadataProfile.AD_HOC.name());
    source.setUri("terraframe.com#test");

    return source;
  }
}
