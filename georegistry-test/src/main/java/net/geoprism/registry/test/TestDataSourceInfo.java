/**
 *
 */
package net.geoprism.registry.test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.DataSource;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.model.GovernanceLevel;
import net.geoprism.registry.model.MetadataProfile;
import net.geoprism.registry.service.business.DataSourceBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class TestDataSourceInfo
{

  private String                  code;

  private TestSourceAuthorityInfo authority;

  private DataSource              dataSource;

  public TestDataSourceInfo(String code, TestSourceAuthorityInfo authority)
  {
    this.code = code;
    this.authority = authority;

    this.dataSource = null;
  }

  public void delete()
  {
    DataSource.getByCode(code).ifPresent(source -> {
      DataSourceBusinessServiceIF service = ServiceFactory.getBean(DataSourceBusinessServiceIF.class);

      service.delete(source);
    });

    this.dataSource = null;
  }

  @Request
  public DataSource apply()
  {
    return DataSource.getByCode(code).orElseGet(() -> {

      DataSourceDTO dto = new DataSourceDTO();
      dto.setCode(code);
      dto.setDescription(new LocalizedValue(code));
      dto.setLabel(new LocalizedValue(code));
      dto.setAuthority(this.authority.getCode());
      dto.setGovernanceLevel(GovernanceLevel.AD_HOC);
      dto.setMetadataProfile(MetadataProfile.AD_HOC);

      DataSourceBusinessServiceIF service = ServiceFactory.getBean(DataSourceBusinessServiceIF.class);
      this.dataSource = service.apply(dto);

      return this.dataSource;
    });

  }

  public DataSource getDataSource()
  {
    if (this.dataSource == null)
    {
      DataSource.getByCode(code).ifPresent(s -> this.dataSource = s);
    }

    return dataSource;
  }

  public String getLabel()
  {
    return code;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
        append(code).toHashCode();
  }

  @Override
  public boolean equals(Object obj)
  {
    if (! ( obj instanceof TestDataSourceInfo ))
      return false;
    if (obj == this)
      return true;

    TestDataSourceInfo rhs = (TestDataSourceInfo) obj;
    return new EqualsBuilder().
    // if deriving: appendSuper(super.equals(obj)).
        append(code, rhs.code).isEquals();
  }

}
