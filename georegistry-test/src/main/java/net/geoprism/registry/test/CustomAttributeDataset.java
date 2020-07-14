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

import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

public class CustomAttributeDataset extends TestDataSet
{
  public final String                TEST_DATA_KEY    = "CustomAttr";
  
  public final TestOrganizationInfo  ORG          = new TestOrganizationInfo(this.getTestDataKey() + "Org");
  
  public final TestHierarchyTypeInfo HIER_ADMIN       = new TestHierarchyTypeInfo(this.getTestDataKey() +  "Hier", ORG);
  
  public final TestGeoObjectTypeInfo GOT_ALL          = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "All", GeometryType.MULTIPOLYGON, ORG);

  public final TestGeoObjectTypeInfo GOT_CHAR            = new TestGeoObjectTypeInfo(this.getTestDataKey() +  "Char", GeometryType.MULTIPOLYGON, ORG);

  public final TestGeoObjectInfo     GO_ALL              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_ALL", GOT_ALL);
  
  public final TestGeoObjectInfo     GO_CHAR              = new TestGeoObjectInfo(this.getTestDataKey() +  "GO_CHAR", GOT_CHAR);
  
  public TestAttributeTypeInfo GOT_ALL_CHAR;

  {
    managedOrganizationInfos.add(ORG);
    
    managedHierarchyTypeInfos.add(HIER_ADMIN);
    
    managedGeoObjectTypeInfos.add(GOT_ALL);
    managedGeoObjectTypeInfos.add(GOT_CHAR);

    managedGeoObjectInfos.add(GO_ALL);
    managedGeoObjectInfos.add(GO_CHAR);
  }

  public static CustomAttributeDataset newTestData()
  {
    return new CustomAttributeDataset();
  }
  
  @Transaction
  @Override
  protected void setUpMetadataInTrans()
  {
    super.setUpMetadataInTrans();
    
    this.testChar = createAttribute(GOT_ALL, AttributeCharacterType.TYPE);
    this.testChar = createAttribute(GOT_ALL, AttributeCharacterType.TYPE);
    
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
    COUNTRY.getUniversal().addLink(Universal.getRoot(), HIER_ADMIN.getServerObject().getUniversalType());
    COUNTRY.addChild(STATE, HIER_ADMIN);
  }

  @Transaction
  @Override
  public void setUpRelationships()
  {
    USA.getGeoEntity().addLink(GeoEntity.getRoot(), HIER_ADMIN.getServerObject().getEntityType());
    USA.addChild(COLORADO, HIER_ADMIN);
  }

  @Override
  public String getTestDataKey()
  {
    return TEST_DATA_KEY;
  }
}
