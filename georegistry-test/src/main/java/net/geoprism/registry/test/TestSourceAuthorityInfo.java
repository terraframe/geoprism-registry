/**
 *
 */
package net.geoprism.registry.test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.AuthorityType;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;

public class TestSourceAuthorityInfo
{

  private String          code;

  private SourceAuthority authority;

  public TestSourceAuthorityInfo(String code)
  {
    this.code = code;
    this.authority = null;
  }

  public void delete()
  {
    SourceAuthority.getByCode(code).ifPresent(source -> {
      SourceAuthorityBusinessServiceIF service = ServiceFactory.getBean(SourceAuthorityBusinessServiceIF.class);

      service.delete(source);
    });

    this.authority = null;
  }

  @Request
  public SourceAuthority apply()
  {
    return SourceAuthority.getByCode(code).orElseGet(() -> {

      SourceAuthorityDTO dto = new SourceAuthorityDTO();
      dto.setCode(code);
      dto.setDescription(new LocalizedValue(code));
      dto.setLabel(new LocalizedValue(code));
      dto.setAuthorityType(AuthorityType.GOVERNMENT);

      SourceAuthorityBusinessServiceIF service = ServiceFactory.getBean(SourceAuthorityBusinessServiceIF.class);
      this.authority = service.apply(dto);

      return this.authority;
    });

  }

  public SourceAuthority getSourceAuthority()
  {
    if (this.authority == null)
    {
      SourceAuthority.getByCode(code).ifPresent(s -> this.authority = s);
    }

    return authority;
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
    if (! ( obj instanceof TestSourceAuthorityInfo ))
      return false;
    if (obj == this)
      return true;

    TestSourceAuthorityInfo rhs = (TestSourceAuthorityInfo) obj;
    return new EqualsBuilder().
    // if deriving: appendSuper(super.equals(obj)).
        append(code, rhs.code).isEquals();
  }

}
