package net.geoprism.registry.test;

import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

public class TestAttributeTypeInfo
{
  private String name;
  
  private TestGeoObjectTypeInfo got;
  
  private AttributeType dto;
  
  private MdAttributeConcreteDAOIF serverObject;
  
  public TestAttributeTypeInfo(String attributeName, TestGeoObjectTypeInfo got)
  {
    this.name = attributeName;
    this.got = got;
  }
  
  public TestAttributeTypeInfo(AttributeType at, TestGeoObjectTypeInfo got)
  {
    this.name = at.getName();
    this.dto = at;
    this.got = got;
  }

  public String getAttributeName()
  {
    return name;
  }

  public void setAttributeName(String attributeName)
  {
    this.name = attributeName;
  }
  
  public AttributeType toDTO()
  {
    if (dto == null)
    {
      dto = got.toDTO().getAttribute(this.name).get();
    }
    
    return dto;
  }
  
  public MdAttributeConcreteDAOIF getServerObject()
  {
    if (serverObject == null)
    {
      serverObject = got.getServerObject().getMdAttribute(this.name);
    }
    
    return serverObject;
  }
}
