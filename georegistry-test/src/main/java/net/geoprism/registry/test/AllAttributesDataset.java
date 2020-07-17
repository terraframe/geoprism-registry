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

import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

import junit.framework.Assert;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationship;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.TermConverter;

public class AllAttributesDataset extends TestDataSet
{
  public static final String                TEST_DATA_KEY    = "AllAttr";
  
  public final TestOrganizationInfo  ORG          = new TestOrganizationInfo(this.getTestDataKey() + "Org");
  
  public final TestHierarchyTypeInfo HIER       = new TestHierarchyTypeInfo(this.getTestDataKey() +  "Hier", ORG);
  
  public final TestGeoObjectTypeInfo GOT_ALL          = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "All", GeometryType.MULTIPOLYGON, ORG);

  public final TestGeoObjectTypeInfo GOT_CHAR            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "CHAR", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_INT            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "INT", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_FLOAT            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "FLOAT", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_BOOL            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "BOOL", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_DATE             = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "DATE", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_TERM             = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "TERM", GeometryType.MULTIPOLYGON, ORG);

  public final TestGeoObjectInfo     GO_ALL              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_ALL", GOT_ALL);
  
  public final TestGeoObjectInfo     GO_CHAR              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_CHAR", GOT_CHAR);
  
  public final TestGeoObjectInfo     GO_INT              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_INT", GOT_INT);
  
  public final TestGeoObjectInfo     GO_FLOAT              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_FLOAT", GOT_FLOAT);
  
  public final TestGeoObjectInfo     GO_BOOL              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_BOOL", GOT_BOOL);
  
  public final TestGeoObjectInfo     GO_DATE               = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_DATE", GOT_DATE);
  
  public final TestGeoObjectInfo     GO_TERM               = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_TERM", GOT_TERM);
  
  public TestAttributeTypeInfo AT_ALL_CHAR;
  public TestAttributeTypeInfo AT_GO_CHAR;
  
  public TestAttributeTypeInfo AT_ALL_INT;
  public TestAttributeTypeInfo AT_GO_INT;
  
  public TestAttributeTypeInfo AT_ALL_FLOAT;
  public TestAttributeTypeInfo AT_GO_FLOAT;
  
  public TestAttributeTypeInfo AT_ALL_BOOL;
  public TestAttributeTypeInfo AT_GO_BOOL;
  
  public TestAttributeTypeInfo AT_ALL_DATE;
  public TestAttributeTypeInfo AT_GO_DATE;
  
  public TestAttributeTypeInfo AT_ALL_TERM;
  public TestAttributeTypeInfo AT_GO_TERM;
  
  // Terms for the ALL GOT
  public static Term TERM_ALL_ROOT;
  public static Term TERM_ALL_VAL1;
  public static Term TERM_ALL_VAL2;
  
  // Terms for the Term GOT
  public static Term TERM_TERM_ROOT;
  public static Term TERM_TERM_VAL1;
  public static Term TERM_TERM_VAL2;
  
  private final static String ROOT_TEST_TERM_CLASSIFIER_ID = TEST_DATA_KEY + "_ROOT";
  
  public static final Date GO_DATE_VALUE = new Date();

  {
    managedOrganizationInfos.add(ORG);
    
    managedHierarchyTypeInfos.add(HIER);
    
    managedGeoObjectTypeInfos.add(GOT_ALL);
    managedGeoObjectTypeInfos.add(GOT_CHAR);
    managedGeoObjectTypeInfos.add(GOT_INT);
    managedGeoObjectTypeInfos.add(GOT_FLOAT);
    managedGeoObjectTypeInfos.add(GOT_BOOL);
    managedGeoObjectTypeInfos.add(GOT_DATE);
    managedGeoObjectTypeInfos.add(GOT_TERM);

    managedGeoObjectInfos.add(GO_ALL);
    managedGeoObjectInfos.add(GO_CHAR);
    managedGeoObjectInfos.add(GO_INT);
    managedGeoObjectInfos.add(GO_FLOAT);
    managedGeoObjectInfos.add(GO_BOOL);
    managedGeoObjectInfos.add(GO_DATE);
    managedGeoObjectInfos.add(GO_TERM);
  }

  public static AllAttributesDataset newTestData()
  {
    return new AllAttributesDataset();
  }
  
  @Transaction
  @Override
  protected void setUpMetadataInTrans()
  {
    super.setUpMetadataInTrans();
    
    this.AT_ALL_CHAR = createAttribute(GOT_ALL, AttributeCharacterType.TYPE);
    this.GO_ALL.setDefaultValue(this.AT_ALL_CHAR.getAttributeName(), "Test Attribute Text Value 123");
    this.AT_GO_CHAR = createAttribute(GOT_CHAR, AttributeCharacterType.TYPE);
    this.GO_CHAR.setDefaultValue(this.AT_GO_CHAR.getAttributeName(), "Test Attribute Text Value 123");
    
    this.AT_ALL_INT = createAttribute(GOT_ALL, AttributeIntegerType.TYPE);
    this.GO_ALL.setDefaultValue(this.AT_ALL_INT.getAttributeName(), 123L);
    this.AT_GO_INT = createAttribute(GOT_INT, AttributeIntegerType.TYPE);
    this.GO_INT.setDefaultValue(this.AT_GO_INT.getAttributeName(), 123L);
    
    this.AT_ALL_FLOAT = createAttribute(GOT_ALL, AttributeFloatType.TYPE);
    this.GO_ALL.setDefaultValue(this.AT_ALL_FLOAT.getAttributeName(), 123.123D);
    this.AT_GO_FLOAT = createAttribute(GOT_FLOAT, AttributeFloatType.TYPE);
    this.GO_FLOAT.setDefaultValue(this.AT_GO_FLOAT.getAttributeName(), 123.123D);
    
    this.AT_ALL_BOOL = createAttribute(GOT_ALL, AttributeBooleanType.TYPE);
    this.GO_ALL.setDefaultValue(this.AT_ALL_BOOL.getAttributeName(), true);
    this.AT_GO_BOOL = createAttribute(GOT_BOOL, AttributeBooleanType.TYPE);
    this.GO_BOOL.setDefaultValue(this.AT_GO_BOOL.getAttributeName(), true);
    
    this.AT_ALL_DATE = createAttribute(GOT_ALL, AttributeDateType.TYPE);
    this.GO_ALL.setDefaultValue(this.AT_ALL_DATE.getAttributeName(), GO_DATE_VALUE);
    this.AT_GO_DATE = createAttribute(GOT_DATE, AttributeDateType.TYPE);
    this.GO_DATE.setDefaultValue(this.AT_GO_DATE.getAttributeName(), GO_DATE_VALUE);
    
    
    createTestTerms();
    
    this.AT_ALL_TERM = createTermAttribute(GOT_ALL, TERM_ALL_ROOT);
    this.AT_GO_TERM = createTermAttribute(GOT_TERM, TERM_TERM_ROOT);
    
    // TODO : Delete this test?
    AttributeTermType att = (AttributeTermType) this.AT_ALL_TERM.toDTO();
    TestDataSet.refreshTerms(att);
    Assert.assertEquals(2, att.getRootTerm().getChildren().size());
    
    // TODO : Delete this test?
    AttributeTermType att2 = (AttributeTermType) this.AT_GO_TERM.toDTO();
    TestDataSet.refreshTerms(att2);
    Assert.assertEquals(2, att2.getRootTerm().getChildren().size());
    
    TERM_TERM_ROOT = ((AttributeTermType)AT_GO_TERM.toDTO()).getRootTerm();
    
    this.GO_ALL.setDefaultValue(this.AT_ALL_TERM.getAttributeName(), TERM_ALL_VAL1);
    this.GO_TERM.setDefaultValue(this.AT_GO_TERM.getAttributeName(), TERM_TERM_VAL1);
  }
  
  public void createTestTerms()
  {
    /*
     * Build terms for All GOT
     */
    MdBusiness allMdBiz = this.GOT_ALL.getServerObject().getMdBusiness();
    
    Classifier allRootClass = TermConverter.buildIfNotExistdMdBusinessClassifier(allMdBiz);
    
    Classifier allRoot = TermConverter.buildIfNotExistAttribute(allMdBiz, "testterm", allRootClass);
    System.out.println("Created allRoot with key [" + allRoot.getKey() + "]");
    TERM_ALL_ROOT = new TermConverter(allRoot.getKeyName()).build();
    
    Classifier allVal1 = new Classifier();
    allVal1.setClassifierId(TEST_DATA_KEY + "ALL_VAL1");
    allVal1.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
    allVal1.getDisplayLabel().setDefaultValue("All Value 1");
    allVal1.apply();
    
    allVal1.addLink(allRoot, ClassifierIsARelationship.CLASS).apply();
    TERM_ALL_VAL1 = new TermConverter(allVal1.getKeyName()).build();
    
    
    Classifier allVal2 = new Classifier();
    allVal2.setClassifierId(TEST_DATA_KEY + "_ALLVAL2");
    allVal2.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
    allVal2.getDisplayLabel().setDefaultValue("All Value 2");
    allVal2.apply();
    
    allVal2.addLink(allRoot, ClassifierIsARelationship.CLASS).apply();
    TERM_ALL_VAL2 = new TermConverter(allVal2.getKeyName()).build();
    
    
    List<? extends Classifier> childClassifiers = allRoot.getAllIsAChild().getAll();
    Assert.assertEquals(2, childClassifiers.size());
    
    /*
     * Build terms for Term GOT
     */
    MdBusiness termMdBiz = this.GOT_TERM.getServerObject().getMdBusiness();
    
    
    Classifier termRootClass = TermConverter.buildIfNotExistdMdBusinessClassifier(termMdBiz);
    
    Classifier termRoot = TermConverter.buildIfNotExistAttribute(termMdBiz, "testterm", termRootClass);
    System.out.println("Created termRoot with key [" + termRoot.getKey() + "]");
    TERM_TERM_ROOT = new TermConverter(termRoot.getKeyName()).build();
    
    Classifier termVal1 = new Classifier();
    termVal1.setClassifierId(TEST_DATA_KEY + "_TERMVAL1");
    termVal1.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
    termVal1.getDisplayLabel().setDefaultValue("All Value 1");
    termVal1.apply();
    
    termVal1.addLink(termRoot, ClassifierIsARelationship.CLASS).apply();
    TERM_TERM_VAL1 = new TermConverter(termVal1.getKeyName()).build();
    
    Classifier termVal2 = new Classifier();
    termVal2.setClassifierId(TEST_DATA_KEY + "_TERMVAL2");
    termVal2.setClassifierPackage(RegistryConstants.REGISTRY_PACKAGE);
    termVal2.getDisplayLabel().setDefaultValue("All Value 2");
    termVal2.apply();
    
    allVal2.addLink(termRoot, ClassifierIsARelationship.CLASS).apply();
    TERM_TERM_VAL2 = new TermConverter(termVal2.getKeyName()).build();
    
    List<? extends Classifier> childClassifiers2 = termRoot.getAllIsAChild().getAll();
    Assert.assertEquals(2, childClassifiers2.size());
  }
  
  @Transaction
  @Override
  protected void cleanUpClassInTrans()
  {
    deleteTestTerms();
  }
  
  public static void deleteTestTerms()
  {
    TestDataSet.deleteClassifier(ROOT_TEST_TERM_CLASSIFIER_ID);
    TestDataSet.deleteClassifier(TEST_DATA_KEY + "_VAL1");
    TestDataSet.deleteClassifier(TEST_DATA_KEY + "_VAL2");
  }
  
  public static TestAttributeTypeInfo createAttribute(TestGeoObjectTypeInfo got, String type)
  {
    AttributeType at = AttributeType.factory("test" + type, new LocalizedValue("Label for test" + type), new LocalizedValue("Description for test" + type), type, false, false, false);
    
    String attributeTypeJSON = at.toJSON().toString();
    
    at = got.getServerObject().createAttributeType(attributeTypeJSON);
    
    return new TestAttributeTypeInfo(at, got);
  }
  
  public static TestAttributeTypeInfo createTermAttribute(TestGeoObjectTypeInfo got, Term attrRoot)
  {
    final String type = AttributeTermType.TYPE;
    
    AttributeTermType att = (AttributeTermType) AttributeType.factory("test" + type, new LocalizedValue("Label for test" + type), new LocalizedValue("Description for test" + type), type, false, false, false);
    att.setRootTerm(attrRoot);
    
    String attributeTypeJSON = att.toJSON().toString();
    
    att = (AttributeTermType) got.getServerObject().createAttributeType(attributeTypeJSON);
    
    return new TestAttributeTypeInfo(att, got);
  }
  
  @Transaction
  @Override
  public void setUpClassRelationships()
  {
    GOT_ALL.getUniversal().addLink(Universal.getRoot(), HIER.getServerObject().getUniversalType());
    
    GOT_ALL.addChild(GOT_CHAR, HIER);
    GOT_ALL.addChild(GOT_INT, HIER);
    GOT_ALL.addChild(GOT_FLOAT, HIER);
    GOT_ALL.addChild(GOT_BOOL, HIER);
    GOT_ALL.addChild(GOT_DATE, HIER);
    GOT_ALL.addChild(GOT_TERM, HIER);
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
    GO_ALL.getGeoEntity().addLink(GeoEntity.getRoot(), HIER.getServerObject().getEntityType());
    
    GO_ALL.addChild(GO_CHAR, HIER);
    GO_ALL.addChild(GO_INT, HIER);
    GO_ALL.addChild(GO_FLOAT, HIER);
    GO_ALL.addChild(GO_BOOL, HIER);
    GO_ALL.addChild(GO_DATE, HIER);
    GO_ALL.addChild(GO_TERM, HIER);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
