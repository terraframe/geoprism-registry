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

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

public class AllAttributesDataset extends TestDataSet
{
  public final String                TEST_DATA_KEY    = "CustomAttr";
  
  public final TestOrganizationInfo  ORG          = new TestOrganizationInfo(this.getTestDataKey() + "Org");
  
  public final TestHierarchyTypeInfo HIER       = new TestHierarchyTypeInfo(this.getTestDataKey() +  "Hier", ORG);
  
  public final TestGeoObjectTypeInfo GOT_ALL          = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "All", GeometryType.MULTIPOLYGON, ORG);

  public final TestGeoObjectTypeInfo GOT_CHAR            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "CHAR", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_INT            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "INT", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_FLOAT            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "FLOAT", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_BOOL            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "BOOL", GeometryType.MULTIPOLYGON, ORG);
  
  public final TestGeoObjectTypeInfo GOT_DATE             = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "DATE", GeometryType.MULTIPOLYGON, ORG);

  public final TestGeoObjectInfo     GO_ALL              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_ALL", GOT_ALL);
  
  public final TestGeoObjectInfo     GO_CHAR              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_CHAR", GOT_CHAR);
  
  public final TestGeoObjectInfo     GO_INT              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_INT", GOT_INT);
  
  public final TestGeoObjectInfo     GO_FLOAT              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_FLOAT", GOT_FLOAT);
  
  public final TestGeoObjectInfo     GO_BOOL              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_BOOL", GOT_BOOL);
  
  public final TestGeoObjectInfo     GO_DATE               = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_DATE", GOT_DATE);
  
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

  {
    managedOrganizationInfos.add(ORG);
    
    managedHierarchyTypeInfos.add(HIER);
    
    managedGeoObjectTypeInfos.add(GOT_ALL);
    managedGeoObjectTypeInfos.add(GOT_CHAR);
    managedGeoObjectTypeInfos.add(GOT_INT);
    managedGeoObjectTypeInfos.add(GOT_FLOAT);
    managedGeoObjectTypeInfos.add(GOT_BOOL);
    managedGeoObjectTypeInfos.add(GOT_DATE);

    managedGeoObjectInfos.add(GO_ALL);
    managedGeoObjectInfos.add(GO_CHAR);
    managedGeoObjectInfos.add(GO_INT);
    managedGeoObjectInfos.add(GO_FLOAT);
    managedGeoObjectInfos.add(GO_BOOL);
    managedGeoObjectInfos.add(GO_DATE);
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
    this.AT_GO_CHAR = createAttribute(GOT_CHAR, AttributeCharacterType.TYPE);
    this.GO_CHAR.setDefaultValue(this.AT_GO_CHAR.getAttributeName(), "Test Attribute Text Value 123");
    
    this.AT_ALL_INT = createAttribute(GOT_ALL, AttributeIntegerType.TYPE);
    this.AT_GO_INT = createAttribute(GOT_INT, AttributeIntegerType.TYPE);
    this.GO_INT.setDefaultValue(this.AT_GO_INT.getAttributeName(), 123L);
    
    this.AT_ALL_FLOAT = createAttribute(GOT_ALL, AttributeFloatType.TYPE);
    this.AT_GO_FLOAT = createAttribute(GOT_FLOAT, AttributeFloatType.TYPE);
    this.GO_FLOAT.setDefaultValue(this.AT_GO_FLOAT.getAttributeName(), 123.123D);
    
    this.AT_ALL_BOOL = createAttribute(GOT_ALL, AttributeBooleanType.TYPE);
    this.AT_GO_BOOL = createAttribute(GOT_BOOL, AttributeBooleanType.TYPE);
    this.GO_BOOL.setDefaultValue(this.AT_GO_BOOL.getAttributeName(), true);
    
    this.AT_ALL_DATE = createAttribute(GOT_ALL, AttributeDateType.TYPE);
    this.AT_GO_DATE = createAttribute(GOT_DATE, AttributeDateType.TYPE);
    this.GO_DATE.setDefaultValue(this.AT_GO_DATE.getAttributeName(), new Date());
  }
  
  public static TestAttributeTypeInfo createAttribute(TestGeoObjectTypeInfo got, String type)
  {
    AttributeType atTestChar = AttributeType.factory("test" + type, new LocalizedValue("Label for test" + type), new LocalizedValue("Description for test" + type), type, false, false, false);
    
    String attributeTypeJSON = atTestChar.toJSON().toString();
    
    atTestChar = got.getServerObject().createAttributeType(attributeTypeJSON);
    
    return  new TestAttributeTypeInfo(atTestChar, got);
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
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
