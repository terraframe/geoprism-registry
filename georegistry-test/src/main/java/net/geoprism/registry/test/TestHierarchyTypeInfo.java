/**
 *
 */
package net.geoprism.registry.test;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.graph.GeoObjectTypeAlreadyInHierarchyException;
import net.geoprism.registry.graph.HierarchicalRelationshipType;
import net.geoprism.registry.model.RootGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class TestHierarchyTypeInfo extends TestCachedObject<ServerHierarchyType>
{
  private String               code;

  private String               displayLabel;

  private String               oid;

  private TestOrganizationInfo org;

  public TestHierarchyTypeInfo(String genKey, TestOrganizationInfo org)
  {
    initialize(genKey, org);
  }

  public TestHierarchyTypeInfo(String code, String displayLabel, TestOrganizationInfo org)
  {
    this.code = code;
    this.displayLabel = displayLabel;
    this.org = org;
  }

  private void initialize(String genKey, TestOrganizationInfo org)
  {
    this.code = genKey + "Code";
    this.displayLabel = genKey + " Display Label";
    this.org = org;
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
    return this.getServerObject(false);
  }

  public ServerHierarchyType getServerObject(boolean forceFetch)
  {
    if (this.getCachedObject() != null && !forceFetch)
    {
      return this.getCachedObject();
    }

    this.setCachedObject(ServerHierarchyType.get(getCode()));

    return this.getCachedObject();
  }

  public Boolean doesMdTermRelationshipExist()
  {
    return HierarchicalRelationshipType.getByCode(this.getCode()) != null;
  }

  public TestOrganizationInfo getOrganization()
  {
    return this.org;
  }

  public HierarchyType toDTO()
  {
    LocalizedValue displayLabel = new LocalizedValue(this.displayLabel);
    LocalizedValue description = new LocalizedValue(this.displayLabel);

    HierarchyType ht = new HierarchyType(this.code, displayLabel, description, this.getOrganization().getCode());

    return ht;
  }

  @Request
  public void apply()
  {
    if (this.getServerObject(true) != null)
    {
      return;
    }

    HierarchyType dto = this.toDTO();

    HierarchyTypeBusinessServiceIF service = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);

    this.setCachedObject(service.createHierarchyType(dto));
  }

  @Request
  public void setRoot(TestGeoObjectTypeInfo type)
  {
    HierarchyTypeBusinessServiceIF service = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);

    try
    {
      service.addToHierarchy(this.getServerObject(), RootGeoObjectType.INSTANCE, type.getServerObject());
    }
    catch (GeoObjectTypeAlreadyInHierarchyException e)
    {
      // IGNORE
    }
  }

  @Request
  public void removeRoot(TestGeoObjectTypeInfo type)
  {
    HierarchyTypeBusinessServiceIF service = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);
    service.removeChild(this.getCachedObject(), RootGeoObjectType.INSTANCE, type.getServerObject(), true);
  }

  @Request
  public void delete()
  {
    deleteInTrans();
  }

  @Transaction
  private void deleteInTrans()
  {
    ServerHierarchyType serverHOT = getServerObject(true);

    if (serverHOT != null)
    {
      HierarchyTypeBusinessServiceIF service = ServiceFactory.getBean(HierarchyTypeBusinessServiceIF.class);
      service.delete(serverHOT);
    }

    this.setCachedObject(null);
  }
}
