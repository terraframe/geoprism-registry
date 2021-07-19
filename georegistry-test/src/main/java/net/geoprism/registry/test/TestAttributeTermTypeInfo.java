package net.geoprism.registry.test;

import java.util.HashSet;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.metadata.AttributeTermType;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.RegistryConstants;

public class TestAttributeTermTypeInfo extends TestAttributeTypeInfo
{

  protected Set<TestTermInfo> managedTerms = new HashSet<TestTermInfo>();
  
  public TestAttributeTermTypeInfo(String attributeName, TestGeoObjectTypeInfo got)
  {
    super(attributeName, got);
  }
  
  public TestAttributeTermTypeInfo(AttributeTermType at, TestGeoObjectTypeInfo got)
  {
    super(at, got);
  }

  public TestAttributeTermTypeInfo(String name, String label, TestGeoObjectTypeInfo got)
  {
    super(name, label, got, AttributeTermType.TYPE);
  }

  @Override
  public void apply()
  {
    super.apply();
    
    this.applyAllChildren();
  }
  
  @Override
  public AttributeTermType fetchDTO()
  {
    return (AttributeTermType) super.fetchDTO();
  }
  
  protected void applyAllChildren()
  {
    TestDataSet.createAttributeRootTerm(this.getGeoObjectType(), this);

    for (TestTermInfo term : managedTerms)
    {
      term.apply();
    }
  }
  
  public Term fetchRootAsTerm()
  {
    return this.fetchDTO().getRootTerm();
  }
  
  public Classifier fetchRootAsClassifier()
  {
    return Classifier.findClassifier(RegistryConstants.REGISTRY_PACKAGE, this.fetchRootAsTerm().getCode());
  }

  public void addManagedTerm(TestTermInfo term)
  {
    this.managedTerms.add(term);
  }
  
  public Set<TestTermInfo> getManagedTerms()
  {
    return this.managedTerms;
  }

}
