/**
 *
 */
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;

public class TestAttributeTypeInfo
{
  private String                   name;

  private String                   label;

  private String                   type;

  private TestGeoObjectTypeInfo    got;

  private AttributeType            dto;

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
    this.type = at.getType();
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

  public AttributeType fetchDTO()
  {
    Optional<AttributeType> optional = got.fetchDTO().getAttribute(this.name);

    if (optional.isPresent())
    {
      return optional.get();
    }
    else
    {
      return null;
    }
  }

  public MdAttributeConcreteDAOIF getServerObject()
  {
    if (serverObject == null)
    {
      GeoObjectTypeBusinessServiceIF typeService = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);

      serverObject = typeService.getMdAttribute(got.getServerObject().getMdBusiness(), this.name);
    }

    return serverObject;
  }

  public void apply()
  {
    if (this.fetchDTO() == null)
    {
      if (this.type.equals(AttributeTermType.TYPE))
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
