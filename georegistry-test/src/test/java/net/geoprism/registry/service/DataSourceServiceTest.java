/**
 *
 */
package net.geoprism.registry.service;

import java.util.List;

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
import net.geoprism.registry.exception.RequiredSourceAuthorityException;
import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.model.GovernanceLevel;
import net.geoprism.registry.model.MetadataProfile;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@RunWith(SpringInstanceTestClassRunner.class)
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

    Assert.assertEquals(source.getCode(), result.getCode());

    Assert.assertTrue(this.service.getByCode(source.getCode()).isPresent());

    this.service.delete(result);

    Assert.assertFalse(this.service.getByCode(source.getCode()).isPresent());
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

    Assert.assertNull(dto.getGovernanceLevel());
    Assert.assertNull(dto.getMetadataProfile());

    DataSource result = this.service.apply(dto);

    try
    {
      Assert.assertEquals(source.getCode(), result.getCode());
      Assert.assertNull(result.getMetadataProfile());
      Assert.assertNull(result.getGovernanceLevel());
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

      Assert.assertEquals(1, results.size());
      Assert.assertEquals(source.getCode(), results.get(0).getCode());
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
      Assert.assertEquals(Long.valueOf(1), this.authorityService.getUseCount(authority));
    }
    finally
    {
      this.service.delete(source);
    }
  }

  @Test(expected = RequiredSourceAuthorityException.class)
  @Request
  public void testRequiredSourceAuthorityException()
  {
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
