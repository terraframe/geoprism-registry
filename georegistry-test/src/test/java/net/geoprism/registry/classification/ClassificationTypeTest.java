/**
 *
 */
package net.geoprism.registry.classification;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.view.Page;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ClassificationTypeTest
{
  @Autowired
  private ClassificationTypeBusinessServiceIF typeService;

  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST_PROG";
    String label = "Test Prog";
    String description = "Test Description";

    ClassificationType type = this.typeService.apply(createMock(code, label, description));

    try
    {
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label, type.getDisplayLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    ClassificationType type = this.typeService.apply(createMock());

    try
    {
      Assert.assertNotNull(this.typeService.getByCode(type.getCode()));
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testUpdate()
  {
    ClassificationType type = this.typeService.apply(createMock());

    try
    {
      String label = "Updated";

      JsonObject json = type.toJSON();
      json.add(MdClassificationInfo.DISPLAY_LABEL, new LocalizedValue(label).toJSON());

      type = this.typeService.apply(json);

      Assert.assertEquals(label, type.getDisplayLabel().getValue());
    }
    finally
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testRemove()
  {
    ClassificationType type = this.typeService.apply(createMock());
    this.typeService.delete(type);

    try
    {
      this.typeService.getByCode(type.getCode());

      Assert.fail("Able to get type which should have been deleted");
    }
    catch (DataNotFoundException ex)
    {
      // Expected
    }
  }

  @Test
  @Request
  public void testToJson()
  {
    ClassificationType classificationType = this.typeService.apply(createMock());

    try
    {
      JsonObject json = classificationType.toJSON();

      Assert.assertEquals(classificationType.getCode(), json.get(DefaultAttribute.CODE.getName()).getAsString());
      Assert.assertEquals(classificationType.getDisplayLabel().toJSON(), json.get(MdClassificationInfo.DISPLAY_LABEL).getAsJsonObject());
    }
    finally
    {
      this.typeService.delete(classificationType);
    }

  }

  @Test
  @Request
  public void testPage()
  {
    ClassificationType classificationType = this.typeService.apply(createMock());

    try
    {
      Page<ClassificationType> page = this.typeService.page(new JsonObject());

      Assert.assertEquals(Long.valueOf(1), page.getCount());

      ClassificationType result = page.getResults().get(0);

      Assert.assertEquals(classificationType.getOid(), result.getOid());
    }
    finally
    {
      this.typeService.delete(classificationType);
    }

  }

  public static JsonObject createMock()
  {
    return createMock("TEST_PROG", "Test Prog", "Test Description");
  }

  public static JsonObject createMock(String code, String label, String description)
  {
    JsonObject object = new JsonObject();
    object.addProperty(DefaultAttribute.CODE.getName(), code);
    object.add(MdClassificationInfo.DISPLAY_LABEL, new LocalizedValue(label).toJSON());
    object.add(MdClassificationInfo.DESCRIPTION, new LocalizedValue(description).toJSON());
    return object;
  }

}
