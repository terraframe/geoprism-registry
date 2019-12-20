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

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.FrequencyType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.junit.Assert;

import com.runwaysdk.business.Relationship;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdClass;

import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.service.WMSService;

public class TestGeoObjectTypeInfo
  {
    private TestDataSet testDataSet;

    private Universal                   universal;

    private String                      code;

    private LocalizedValue              displayLabel;

    private LocalizedValue              description;

    private String                      uid;

    private GeometryType                geomType;

    private boolean                     isLeaf;

    private List<TestGeoObjectTypeInfo> children;
    
    private ServerGeoObjectType         serverObject;
    
    private FrequencyType             frequency;

    protected TestGeoObjectTypeInfo(TestDataSet testDataSet, String genKey)
    {
      initialize(testDataSet, genKey, GeometryType.MULTIPOLYGON);
    }
    
    protected TestGeoObjectTypeInfo(TestDataSet testDataSet, String genKey, GeometryType geomType)
    {
      initialize(testDataSet, genKey, geomType);
    }
    
    private void initialize(TestDataSet testDataSet, String genKey, GeometryType geomType)
    {
      this.testDataSet = testDataSet;
      this.code = this.testDataSet.getTestDataKey() + genKey;
      this.displayLabel = new LocalizedValue(this.testDataSet.getTestDataKey() + " " + genKey);
      this.description = new LocalizedValue(this.testDataSet.getTestDataKey() + " " + genKey);
      this.children = new LinkedList<TestGeoObjectTypeInfo>();
      this.geomType = geomType;
      this.isLeaf = false; // Leaf types are not supported anymore
      this.frequency = FrequencyType.DAILY;
    }

    public String getCode()
    {
      return code;
    }

    public LocalizedValue getDisplayLabel()
    {
      return displayLabel;
    }

    public LocalizedValue getDescription()
    {
      return description;
    }

    public GeometryType getGeometryType()
    {
      return geomType;
    }

    public boolean getIsLeaf()
    {
      return isLeaf;
    }

    public String getUid()
    {
      return uid;
    }

    public void setUid(String uid)
    {
      this.uid = uid;
    }

    public Universal getUniversal()
    {
      return this.universal;
    }
    
    public ServerGeoObjectType getServerObject()
    {
      if (this.serverObject != null)
      {
        return this.serverObject;
      }
      else
      {
        Optional<GeoObjectType> got = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(code);
        
        if (got.isPresent())
        {
          return ServerGeoObjectType.get(getCode());
        }
        
        return null;
      }
    }
    
//    public ServerGeoObjectType getGeoObjectType()
//    {
      // if (this.getUniversal() != null)
      // {
      // return
      // registryService.getConversionService().universalToGeoObjectType(this.getUniversal());
      // }
      // else
      // {
//      return ServerGeoObjectType.get(this.getCode());
      // }
//    }

    public List<TestGeoObjectTypeInfo> getChildren()
    {
      return this.children;
    }

    public void addChild(TestGeoObjectTypeInfo child, TestHierarchyTypeInfo hierarchy)
    {
      if (!this.children.contains(child))
      {
        this.children.add(child);
      }

      hierarchy.getServerObject().addToHierarchy(this.getCode(), child.getCode());
//      this.getServerObject().add(child.getServerObject(), hierarchy.getServerObject());
//      return child.getUniversal().addLink(universal, hierarchy.getServerObject());
    }

    public void assertEquals(GeoObjectType got)
    {
      Assert.assertEquals(code, got.getCode());
      Assert.assertEquals(displayLabel.getValue(), got.getLabel().getValue());
      Assert.assertEquals(description.getValue(), got.getDescription().getValue());
    }

    public void assertEquals(Universal uni)
    {
      Assert.assertEquals(code, uni.getKey());
      Assert.assertEquals(displayLabel.getValue(), uni.getDisplayLabel().getValue());
      Assert.assertEquals(description.getValue(), uni.getDescription().getValue());
    }
    
    @Request
    public void apply()
    {
      applyInTrans();

      // If this did not error out then add to the cache
      this.testDataSet.adapter.getMetadataCache().addGeoObjectType(this.serverObject.getType());
    }

    @Transaction
    private void applyInTrans()
    {
      if (this.testDataSet.debugMode >= 1)
      {
        System.out.println("Applying TestGeoObjectTypeInfo [" + this.getCode() + "].");
      }

      GeoObjectType got = new GeoObjectType(this.getCode(), this.geomType, this.getDisplayLabel(), this.getDescription(), this.getIsLeaf(), true, frequency, this.testDataSet.adapter);
//      ServerGeoObjectType type = new ServerGeoObjectTypeBuilder().create(got);
      this.serverObject = new ServerGeoObjectTypeConverter().create(got);

      universal = this.serverObject.getUniversal();

      this.setUid(universal.getOid());
    }

    @Request
    public void delete()
    {
      deleteInTrans();
    }

    @Transaction
    private void deleteInTrans()
    {
      if (this.testDataSet.debugMode >= 1)
      {
        System.out.println("Deleting TestGeoObjectTypeInfo [" + this.getCode() + "].");
      }

      Universal uni = this.testDataSet.getUniversalIfExist(this.getCode());
      MdClass mdBiz = this.testDataSet.getMdClassIfExist(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);
      if (mdBiz != null)
      {
        AttributeHierarchy.deleteByMdBusiness(MdBusinessDAO.get(mdBiz.getOid()));
      }
      new WMSService().deleteDatabaseViewIfExists(this.getCode());
      if (uni != null)
      {
        this.testDataSet.deleteUniversal(this.getCode());
      }
      if (mdBiz != null)
      {
        this.testDataSet.deleteMdClass(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);
      }
      MdClass geoVertexType = this.testDataSet.getMdClassIfExist(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE, this.code);
      if (geoVertexType != null)
      {
        this.testDataSet.deleteMdClass(RegistryConstants.UNIVERSAL_GRAPH_PACKAGE, this.code);
      }

      this.children.clear();
      this.universal = null;
    }

    public boolean isPersisted()
    {
      Boolean exists = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(code).isPresent();
      
      if (exists)
      {
        MdClass universal = this.testDataSet.getMdClassIfExist(RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE, this.code);
        
        exists = universal != null;
      }
      
      return exists;
    }
  }