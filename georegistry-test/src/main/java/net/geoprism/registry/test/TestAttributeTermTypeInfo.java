/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
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
