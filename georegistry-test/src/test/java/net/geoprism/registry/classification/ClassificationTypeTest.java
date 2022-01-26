package net.geoprism.registry.classification;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.view.Page;

public class ClassificationTypeTest
{
  @Test
  @Request
  public void testCreate()
  {
    String code = "TEST_PROG";
    String label = "Test Prog";
    String description = "Test Description";

    ClassificationType type = ClassificationType.apply(createMock(code, label, description));

    try
    {
      Assert.assertEquals(code, type.getCode());
      Assert.assertEquals(label, type.getDisplayLabel().getValue());
      Assert.assertNotNull(type.getMdVertex());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    ClassificationType type = ClassificationType.apply(createMock());

    try
    {
      Assert.assertNotNull(ClassificationType.getByCode(type.getCode()));
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testUpdate()
  {
    ClassificationType type = ClassificationType.apply(createMock());

    try
    {
      String label = "Updated";

      JsonObject json = type.toJSON();
      json.add(MdClassificationInfo.DISPLAY_LABEL, new LocalizedValue(label).toJSON());

      type = ClassificationType.apply(json);

      Assert.assertEquals(label, type.getDisplayLabel().getValue());
    }
    finally
    {
      type.delete();
    }
  }

  @Test
  @Request
  public void testRemove()
  {
    ClassificationType type = ClassificationType.apply(createMock());
    type.delete();

    try
    {
      ClassificationType.getByCode(type.getCode());

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
    ClassificationType classificationType = ClassificationType.apply(createMock());

    try
    {
      JsonObject json = classificationType.toJSON();

      Assert.assertEquals(classificationType.getCode(), json.get(DefaultAttribute.CODE.getName()).getAsString());
      Assert.assertEquals(classificationType.getDisplayLabel().toJSON(), json.get(MdClassificationInfo.DISPLAY_LABEL).getAsJsonObject());
    }
    finally
    {
      classificationType.delete();
    }

  }

  @Test
  @Request
  public void testPage()
  {
    ClassificationType classificationType = ClassificationType.apply(createMock());

    try
    {
      Page<ClassificationType> page = ClassificationType.page(new JsonObject());

      Assert.assertEquals(new Long(1), page.getCount());

      ClassificationType result = page.getResults().get(0);

      Assert.assertEquals(classificationType.getOid(), result.getOid());
    }
    finally
    {
      classificationType.delete();
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
