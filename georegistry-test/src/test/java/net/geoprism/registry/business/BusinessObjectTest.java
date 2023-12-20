/**
 *
 */
package net.geoprism.registry.business;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.FastDatasetTest;
import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.BusinessEdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessObjectBusinessServiceIF;
import net.geoprism.registry.service.business.BusinessTypeBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectBusinessServiceIF;
import net.geoprism.registry.test.FastTestDataset;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class BusinessObjectTest extends FastDatasetTest implements InstanceTestClassListener
{
  private static String                     TEST_CODE = "TEST_OBJ";

  private static BusinessType               type;

  private static AttributeType              attribute;

  private static BusinessEdgeType           relationshipType;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  private BusinessObjectBusinessServiceIF   bObjectService;

  @Autowired
  private GeoObjectBusinessServiceIF        objectService;

  @Override
  public void beforeClassSetup() throws Exception
  {
    super.beforeClassSetup();

    testData.setUpInstanceData();

    setUpClassInRequest();
  }

  @Request
  private void setUpClassInRequest()
  {
    String code = "TEST_PROG";
    String orgCode = FastTestDataset.ORG_CGOV.getCode();
    String label = "Test Prog";

    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, code);
    object.addProperty(BusinessType.ORGANIZATION, orgCode);
    object.add(BusinessType.DISPLAYLABEL, new LocalizedValue(label).toJSON());

    type = this.bTypeService.apply(object);

    attribute = this.bTypeService.createAttributeType(type, new AttributeCharacterType("testCharacter", new LocalizedValue("Test Character"), new LocalizedValue("Test True"), false, false, false));

    relationshipType = this.bEdgeService.create(FastTestDataset.ORG_CGOV.getCode(), "TEST_REL", new LocalizedValue("Test Rel"), new LocalizedValue("Test Rel"), type.getCode(), type.getCode());
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();

    if (testData != null)
    {
      testData.tearDownInstanceData();
    }

    super.afterClassSetup();
  }

  @Request
  private void cleanUpClassInRequest()
  {
    if (relationshipType != null)
    {
      this.bEdgeService.delete(relationshipType);
    }

    if (type != null)
    {
      this.bTypeService.delete(type);
    }
  }

  @Test
  @Request
  public void testBasicCreate()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      Assert.assertNotNull(object.getVertex().getRID());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testSetGetValue()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      Assert.assertEquals("Test Text", object.getObjectValue(attribute.getName()));
    }
    finally
    {
      this.bObjectService.delete(object);
    }

  }

  @Test
  @Request
  public void testGet()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      BusinessObject result = this.bObjectService.get(type, attribute.getName(), object.getObjectValue(attribute.getName()));

      Assert.assertEquals((String) object.getObjectValue("oid"), (String) result.getObjectValue("oid"));
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      BusinessObject result = this.bObjectService.getByCode(type, object.getCode());

      Assert.assertEquals((String) object.getObjectValue("oid"), (String) result.getObjectValue("oid"));
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testAddGetGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addGeoObject(object, serverObject);

      List<VertexServerGeoObject> results = this.bObjectService.getGeoObjects(object);

      Assert.assertEquals(1, results.size());

      VertexServerGeoObject result = results.get(0);

      Assert.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testRemoveGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addGeoObject(object, serverObject);
      this.bObjectService.removeGeoObject(object, serverObject);

      Assert.assertEquals(0, this.bObjectService.getGeoObjects(object).size());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testDuplicateGeoObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addGeoObject(object, serverObject);
      this.bObjectService.addGeoObject(object, serverObject);
      this.bObjectService.addGeoObject(object, serverObject);
      this.bObjectService.addGeoObject(object, serverObject);

      List<VertexServerGeoObject> results = this.bObjectService.getGeoObjects(object);

      Assert.assertEquals(1, results.size());

      VertexServerGeoObject result = results.get(0);

      Assert.assertEquals(serverObject.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetBusinessObjects()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addGeoObject(object, serverObject);

      List<BusinessObject> results = this.objectService.getBusinessObjects((VertexServerGeoObject) serverObject, type);

      Assert.assertEquals(1, results.size());

      BusinessObject result = results.get(0);

      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testGetBusinessObjects_NoType()
  {
    ServerGeoObjectIF serverObject = FastTestDataset.CENTRAL_HOSPITAL.getServerObject();

    BusinessObject object = this.bObjectService.newInstance(type);
    object.setValue(attribute.getName(), "Test Text");
    object.setCode(TEST_CODE);
    this.bObjectService.apply(object);

    try
    {
      this.bObjectService.addGeoObject(object, serverObject);

      List<BusinessObject> results = this.objectService.getBusinessObjects((VertexServerGeoObject) serverObject);

      Assert.assertEquals(1, results.size());

      BusinessObject result = results.get(0);

      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      this.bObjectService.delete(object);
    }
  }

  @Test
  @Request
  public void testAddParent()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getName(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getName(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addParent(child, relationshipType, parent);

        List<BusinessObject> results = this.bObjectService.getParents(child, relationshipType);

        Assert.assertEquals(1, results.size());

        BusinessObject result = results.get(0);

        Assert.assertEquals(parent.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);;
    }
  }

  @Test
  @Request
  public void testRemoveParent()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getName(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getName(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addParent(child, relationshipType, parent);
        this.bObjectService.removeParent(child, relationshipType, parent);

        Assert.assertEquals(0, this.bObjectService.getParents(child, relationshipType).size());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);;
    }
  }

  @Test
  @Request
  public void testDuplicateParent()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getName(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getName(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addParent(child, relationshipType, parent);
        this.bObjectService.addParent(child, relationshipType, parent);
        this.bObjectService.addParent(child, relationshipType, parent);
        this.bObjectService.addParent(child, relationshipType, parent);
        this.bObjectService.addParent(child, relationshipType, parent);

        List<BusinessObject> results = this.bObjectService.getParents(child, relationshipType);

        Assert.assertEquals(1, results.size());

        BusinessObject result = results.get(0);

        Assert.assertEquals(parent.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);;
    }
  }

  @Test
  @Request
  public void testAddChildren()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getName(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getName(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addChild(parent, relationshipType, child);

        List<BusinessObject> results = this.bObjectService.getChildren(parent, relationshipType);

        Assert.assertEquals(1, results.size());

        BusinessObject result = results.get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);;
    }
  }

  @Test
  @Request
  public void testRemoveChildren()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getName(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getName(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {
        this.bObjectService.addChild(parent, relationshipType, child);
        this.bObjectService.removeChild(parent, relationshipType, child);

        Assert.assertEquals(0, this.bObjectService.getChildren(parent, relationshipType).size());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);;
    }
  }

  @Test
  @Request
  public void testDuplicateChildren()
  {
    BusinessObject parent = this.bObjectService.newInstance(type);
    parent.setValue(attribute.getName(), "Test Parnet");
    parent.setCode("TEST_PARENT");
    this.bObjectService.apply(parent);

    try
    {
      BusinessObject child = this.bObjectService.newInstance(type);
      child.setValue(attribute.getName(), "Test Child");
      child.setCode("TEST_CHILD");
      this.bObjectService.apply(child);

      try
      {        
        this.bObjectService.addChild(parent, relationshipType, child);
        this.bObjectService.addChild(parent, relationshipType, child);
        this.bObjectService.addChild(parent, relationshipType, child);
        this.bObjectService.addChild(parent, relationshipType, child);
        this.bObjectService.addChild(parent, relationshipType, child);

        List<BusinessObject> results = this.bObjectService.getChildren(parent, relationshipType);

        Assert.assertEquals(1, results.size());

        BusinessObject result = results.get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.bObjectService.delete(child);
      }
    }
    finally
    {
      this.bObjectService.delete(parent);;
    }
  }

}
