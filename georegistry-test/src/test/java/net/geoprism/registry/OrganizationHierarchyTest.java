/**
 *
 */
package net.geoprism.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.GraphNode;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.view.Page;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class OrganizationHierarchyTest
{
  private static String                 GRANDPARENT_CODE = "GRANDPARENT_OBJ";

  private static String                 PARENT_CODE      = "PARENT_OBJ";

  private static String                 CHILD_CODE       = "CHILD_OBJ";

  @Autowired
  private OrganizationBusinessServiceIF service;

  @Test
  @Request
  public void testBasicCreate()
  {
    ServerOrganization object = createOrganization(PARENT_CODE, null);

    try
    {
      Assert.assertNotNull(object.getGraphOrganization().getRID());
    }
    finally
    {
      this.service.delete(object);
    }
  }

  @Test
  @Request
  public void testGetByCode()
  {
    ServerOrganization object = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization result = ServerOrganization.getByCode(object.getCode());

      Assert.assertEquals(object.getCode(), result.getCode());
    }
    finally
    {
      this.service.delete(object);
    }
  }

  @Test
  @Request
  public void testAddGetChild()
  {
    ServerOrganization parent = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization child = createOrganization(CHILD_CODE, null);

      try
      {
        parent.addChild(child);

        Page<ServerOrganization> children = parent.getChildren(20, 1);

        Assert.assertEquals(Long.valueOf(1), children.getCount());

        ServerOrganization result = children.getResults().get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.service.delete(child);
      }
    }
    finally
    {
      this.service.delete(parent);
    }
  }

  @Test
  @Request
  public void testAddGetChildApplyWithParent()
  {
    ServerOrganization parent = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization child = createOrganization(CHILD_CODE, parent);

      try
      {
        Page<ServerOrganization> children = parent.getChildren(20, 1);

        Assert.assertEquals(Long.valueOf(1), children.getCount());

        ServerOrganization result = children.getResults().get(0);

        Assert.assertEquals(child.getCode(), result.getCode());
      }
      finally
      {
        this.service.delete(child);
      }
    }
    finally
    {
      this.service.delete(parent);
    }
  }

  @Test
  @Request
  public void testRemoveParent()
  {
    ServerOrganization parent = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization child = createOrganization(CHILD_CODE, null);

      try
      {
        child.addParent(parent);

        Assert.assertNotNull(child.getParent());

        child.removeParent(parent);

        Assert.assertNull(child.getParent());
      }
      finally
      {
        this.service.delete(child);
      }
    }
    finally
    {
      this.service.delete(parent);
    }
  }

  @Test
  @Request
  public void testRemoveChild()
  {
    ServerOrganization parent = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization child = createOrganization(CHILD_CODE, null);

      try
      {
        parent.addChild(child);

        Assert.assertEquals(Long.valueOf(1), parent.getChildren().getCount());

        parent.removeChild(child);

        Assert.assertEquals(Long.valueOf(0), parent.getChildren().getCount());
      }
      finally
      {
        this.service.delete(child);
      }
    }
    finally
    {
      this.service.delete(parent);
    }
  }

  @Test
  @Request
  public void testAddGetParent()
  {
    ServerOrganization parent = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization child = createOrganization(CHILD_CODE, null);

      try
      {
        child.addParent(parent);

        ServerOrganization result = child.getParent();

        Assert.assertNotNull(result);

        Assert.assertEquals(parent.getCode(), result.getCode());
      }
      finally
      {
        this.service.delete(child);
      }
    }
    finally
    {
      this.service.delete(parent);
    }
  }

  @Test
  @Request
  public void testGetAncestor()
  {
    ServerOrganization grandParent = createOrganization(GRANDPARENT_CODE, null);

    try
    {
      ServerOrganization parent = createOrganization(PARENT_CODE, grandParent);

      try
      {
        ServerOrganization child = createOrganization(CHILD_CODE, parent);

        try
        {
          List<ServerOrganization> ancestors = child.getAncestors(null);

          Assert.assertEquals(3, ancestors.size());
        }
        finally
        {
          this.service.delete(child);
        }
      }
      finally
      {
        this.service.delete(parent);
      }
    }
    finally
    {
      this.service.delete(grandParent);
    }
  }

  @Test
  @Request
  public void testGetAncestorTree()
  {
    ServerOrganization parent = createOrganization(PARENT_CODE, null);

    try
    {
      ServerOrganization child = createOrganization(CHILD_CODE, parent);

      try
      {
        GraphNode<ServerOrganization> tree = child.getAncestorTree(null, 200);

        Assert.assertEquals(parent.getCode(), tree.getObject().getCode());
      }
      finally
      {
        this.service.delete(child);
      }
    }
    finally
    {
      this.service.delete(parent);
    }
  }

  @Test
  @Request
  public void testExportJson()
  {
    ServerOrganization grandParent = createOrganization(GRANDPARENT_CODE, null);

    try
    {
      ServerOrganization parent = createOrganization(PARENT_CODE, grandParent);

      try
      {
        ServerOrganization child = createOrganization(CHILD_CODE, parent);

        try
        {
          JsonArray results = this.service.exportToJson();

          System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(results));

          JsonObject result = null;

          for (int i = 0; i < results.size(); i++)
          {
            JsonObject item = results.get(i).getAsJsonObject();

            if (grandParent.getCode().equals(item.get(Organization.CODE).getAsString()))
            {
              result = item;
            }
          }

          Assert.assertNotNull(result);

          results = result.get("children").getAsJsonArray();

          Assert.assertEquals(1, results.size());

          result = results.get(0).getAsJsonObject();

          Assert.assertEquals(parent.getCode(), result.get(Organization.CODE).getAsString());

          results = result.get("children").getAsJsonArray();

          Assert.assertEquals(1, results.size());

          result = results.get(0).getAsJsonObject();

          Assert.assertEquals(child.getCode(), result.get(Organization.CODE).getAsString());
        }
        finally
        {
          this.service.delete(child);
        }
      }
      finally
      {
        this.service.delete(parent);
      }
    }
    finally
    {
      this.service.delete(grandParent);
    }

  }

  @Test
  @Request
  public void testImportJson() throws IOException
  {
    try (InputStream resource = this.getClass().getResourceAsStream("/org-tree.json"))
    {
      JsonArray array = JsonParser.parseReader(new InputStreamReader(resource)).getAsJsonArray();

      this.service.importJsonTree(array);

      ServerOrganization child = ServerOrganization.getByCode(CHILD_CODE);

      Assert.assertNotNull(child);

      this.service.delete(child);

      ServerOrganization parent = ServerOrganization.getByCode(PARENT_CODE);

      Assert.assertNotNull(parent);

      this.service.delete(parent);

      ServerOrganization grandParent = ServerOrganization.getByCode(GRANDPARENT_CODE);

      Assert.assertNotNull(grandParent);

      this.service.delete(grandParent);
    }
  }

  private ServerOrganization createOrganization(String code, ServerOrganization parent)
  {
    ServerOrganization object = new ServerOrganization();
    object.setCode(code);
    object.setDisplayLabel(new LocalizedValue(code));
    object.setContactInfo(new LocalizedValue(code));
    this.service.apply(object, parent);

    return object;
  }

}
