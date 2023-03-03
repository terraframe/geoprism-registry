/**
 *
 */
package net.geoprism.registry.test;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.commongeoregistry.adapter.Term;

import com.runwaysdk.session.Request;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.conversion.TermConverter;

public class TestTermInfo
{
  
  private String code;
  
  private TestAttributeTermTypeInfo attributeType;
  
  public TestTermInfo(String code, TestAttributeTermTypeInfo attributeType)
  {
    this.code = code;
    this.attributeType = attributeType;
  }
  
  public void delete()
  {
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  @Request
  public Term apply()
  {
    return TestDataSet.createTerm(this.attributeType, this.getCode(), this.getCode());
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

  public TestAttributeTermTypeInfo getAttributeType()
  {
    return attributeType;
  }

  public void setAttributeType(TestAttributeTermTypeInfo attributeType)
  {
    this.attributeType = attributeType;
  }
  
  public Classifier fetchClassifier()
  {
    Classifier root = this.attributeType.fetchRootAsClassifier();
    
    return Classifier.findClassifier(root.getKey(), this.code);
  }

  public Term fetchTerm()
  {
    return new TermConverter(this.fetchClassifier().getKey()).build();
  }
  
  @Override
  public int hashCode() {
      return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
          append(code).
          append(attributeType).
          toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
     if (!(obj instanceof TestTermInfo))
          return false;
      if (obj == this)
          return true;

      TestTermInfo rhs = (TestTermInfo) obj;
      return new EqualsBuilder().
          // if deriving: appendSuper(super.equals(obj)).
          append(code, rhs.code).
          append(attributeType, rhs.attributeType).
          isEquals();
  }
  
}
