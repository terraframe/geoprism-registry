/**
 *
 */
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import net.geoprism.registry.graph.AttributeType;

public class TestAttributeTypeInfo
{
  private String                name;

  private String                label;

  private String                type;

  private TestGeoObjectTypeInfo got;

  private AttributeType         serverObject;

  public TestAttributeTypeInfo(String attributeName, TestGeoObjectTypeInfo got)
  {
    this.name = attributeName;
    this.got = got;
  }

  public TestAttributeTypeInfo(org.commongeoregistry.adapter.metadata.AttributeType dto, TestGeoObjectTypeInfo got)
  {
    this.name = dto.getName();
    this.got = got;
    this.type = dto.getType();
  }

  public TestAttributeTypeInfo(String name, String label, TestGeoObjectTypeInfo got, String type)
  {
    this.name = name;
    this.label = label;
    this.got = got;
    this.type = type;
  }

  public String getAttributeName()
  {
    return name;
  }

  public void setAttributeName(String attributeName)
  {
    this.name = attributeName;
  }

  public org.commongeoregistry.adapter.metadata.AttributeType fetchDTO()
  {
    return got.fetchDTO().getAttribute(this.name).orElse(null);
  }

  public AttributeType getServerObject()
  {
    if (serverObject == null)
    {
      serverObject = got.getServerObject().getAttribute(this.name).get();
    }

    return serverObject;
  }

  public void apply()
  {
    if (this.fetchDTO() == null)
    {
      if (this.type.equals(org.commongeoregistry.adapter.metadata.AttributeTermType.TYPE))
      {
        TestDataSet.createTermAttribute(name, label, got, null);
      }
      else
      {
        TestDataSet.createAttribute(this.name, this.label, this.got, this.type);
      }
    }
  }

  public void applyTerm(Term attrRoot)
  {
    if (this.fetchDTO() == null)
    {
      TestDataSet.createTermAttribute(this.name, this.label, this.got, attrRoot);
    }
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public TestGeoObjectTypeInfo getGeoObjectType()
  {
    return got;
  }

  public void setGeoObjectType(TestGeoObjectTypeInfo got)
  {
    this.got = got;
  }
}
