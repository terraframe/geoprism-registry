/**
 *
 */
package net.geoprism.registry.test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.Source;

public class TestSourceInfo
{

  private String code;

  private Source dataSource;

  public TestSourceInfo(String code)
  {
    this.code = code;
    this.dataSource = null;
  }

  public void delete()
  {
    Source.getByCode(code).ifPresent(source -> {
      source.delete();
    });
  }

  @Request
  public Source apply()
  {
    return Source.getByCode(code).orElseGet(() -> {
      this.dataSource = new Source();
      dataSource.setCode(this.code);
      dataSource.apply();

      return dataSource;
    });

  }

  public Source getDataSource()
  {
    if (this.dataSource == null)
    {
      Source.getByCode(code).ifPresent(s -> this.dataSource = s);
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
    if (! ( obj instanceof TestSourceInfo ))
      return false;
    if (obj == this)
      return true;

    TestSourceInfo rhs = (TestSourceInfo) obj;
    return new EqualsBuilder().
    // if deriving: appendSuper(super.equals(obj)).
        append(code, rhs.code).isEquals();
  }

}
