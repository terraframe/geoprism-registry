/**
 *
 */
package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BusinessEdgeTypeTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static BusinessType               parentType;

  private static BusinessType               childType;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();
    
    setUpClassInRequest();
  }

  @Request
  public void setUpClassInRequest()
  {
    String orgCode = FastTestDataset.ORG_CGOV.getCode();

    JsonObject parentObject = new JsonObject();
    parentObject.addProperty(BusinessType.CODE, "TEST_PARENT");
    parentObject.addProperty(BusinessType.ORGANIZATION, orgCode);
    parentObject.add(BusinessType.DISPLAYLABEL, new LocalizedValue("TEST_PARENT").toJSON());

    JsonObject childObject = new JsonObject();
    childObject.addProperty(BusinessType.CODE, "TEST_CHILD");
    childObject.addProperty(BusinessType.ORGANIZATION, orgCode);
    childObject.add(BusinessType.DISPLAYLABEL, new LocalizedValue("TEST_CHILD").toJSON());

    parentType = this.bTypeService.apply(parentObject);
    childType = this.bTypeService.apply(childObject);
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();

    super.afterClassSetup();
  }

  @Request
  public void cleanUpClassInRequest()
  {
    if (parentType != null)
    {
      this.bTypeService.delete(parentType);
    }

    if (childType != null)
    {
      this.bTypeService.delete(childType);
    }
  }

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST";
    LocalizedValue label = new LocalizedValue("Test Label");
    LocalizedValue description = new LocalizedValue("Test Description");

    BusinessEdgeType type = this.bEdgeService.create(FastTestDataset.ORG_CGOV.getCode(), code, label, description, parentType.getCode(), childType.getCode());

    try
    {
      Assert.assertNotNull(type);
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label.getValue(), type.getDisplayLabel().getValue());
      Assert.assertEquals(description.getValue(), type.getDescription().getValue());

      MdEdge mdEdge = type.getMdEdge();

      Assert.assertNotNull(mdEdge);
    }
    finally
    {
      this.bEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testUpdate()
  {
    BusinessEdgeType type = createTestRelationship();

    try
    {
      JsonObject object = new JsonObject();
      object.add(BusinessEdgeType.DISPLAYLABEL, new LocalizedValue("Updated Label").toJSON());
      object.add(BusinessEdgeType.DESCRIPTION, new LocalizedValue("Updated Description").toJSON());
      
      this.bEdgeService.update(type, object);

      Assert.assertEquals("Updated Label", type.getDisplayLabel().getValue());
      Assert.assertEquals("Updated Description", type.getDescription().getValue());
    }
    finally
    {
      this.bEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByCode()
  {
    BusinessEdgeType type = createTestRelationship();

    try
    {
      BusinessEdgeType result = this.bEdgeService.getByCode(type.getCode());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.bEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByMdEdge()
  {
    BusinessEdgeType type = createTestRelationship();

    try
    {
      BusinessEdgeType result = this.bEdgeService.getByMdEdge(type.getMdEdge());

      Assert.assertNotNull(result);
      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.bEdgeService.delete(type);
    }

  }

  @Test
  @Request
  public void testGetByAll()
  {
    BusinessEdgeType type = createTestRelationship();

    try
    {
      List<BusinessEdgeType> results = this.bEdgeService.getAll();

      Assert.assertEquals(1, results.size());

      BusinessEdgeType result = results.get(0);

      Assert.assertEquals(type.getCode(), result.getCode());
    }
    finally
    {
      this.bEdgeService.delete(type);
    }

  }

  public BusinessEdgeType createTestRelationship()
  {
    return this.bEdgeService.create(FastTestDataset.ORG_CGOV.getCode(), "TEST", new LocalizedValue("Test Label"), new LocalizedValue("Test Description"), parentType.getCode(), childType.getCode());
  }

}
