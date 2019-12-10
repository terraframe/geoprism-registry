package net.geoprism.registry.test;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.GISConstants;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdClass;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class TestHierarchyTypeInfo
{
  private final TestDataSet testDataSet;
  
  private String                  code;
  
  private String                  displayLabel;
  
  private String                  oid;
  
  protected TestHierarchyTypeInfo(TestDataSet testDataSet, String genKey)
  {
    this.testDataSet = testDataSet;
    initialize(genKey);
  }
  
  protected TestHierarchyTypeInfo(TestDataSet testDataSet, String code, String displayLabel)
  {
    this.testDataSet = testDataSet;
    this.code = code;
    this.displayLabel = displayLabel;
  }
  
  private void initialize(String genKey)
  {
    this.code = this.testDataSet.getTestDataKey() + genKey + "Code";
    this.displayLabel = this.testDataSet.getTestDataKey() + " " + genKey + " Display Label";
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(String code)
  {
    this.code = code;
  }

  public String getDisplayLabel()
  {
    return displayLabel;
  }

  public void setDisplayLabel(String displayLabel)
  {
    this.displayLabel = displayLabel;
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }
  
  public ServerHierarchyType getServerObject()
  {
    Optional<HierarchyType> hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code);
    
    if (hierarchyType.isPresent())
    {
      return ServerHierarchyType.get(getCode());
    }
    
    return null;
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
      System.out.println("Deleting TestHierarchyTypeInfo [" + this.getCode() + "].");
    }

    ServerHierarchyType serverHOT = getServerObject();
    
    if (serverHOT != null)
    {
      serverHOT.delete();
    }
    
//    MdClass mdTermRelationship = this.testDataSet.getMdClassIfExist(GISConstants.GEO_PACKAGE, this.getCode());
//    
//    if (mdTermRelationship != null)
//    {
//      mdTermRelationship.delete();
//    }
  }
}
