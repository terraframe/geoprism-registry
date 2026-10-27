/**
 *
 */
package net.geoprism.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;

import net.geoprism.registry.config.TestApplication;
import net.geoprism.registry.model.GraphNode;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.OrganizationBusinessServiceIF;
import net.geoprism.registry.view.Page;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = TestApplication.class)
@AutoConfigureMockMvc
@ExtendWith(InstanceTestClassExtension.class)
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
      Assertions.assertNotNull(object.getGraphOrganization().getRID());
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

      Assertions.assertEquals(object.getCode(), result.getCode());
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

        Assertions.assertEquals(Long.valueOf(1), children.getCount());

        ServerOrganization result = children.getResultSet().get(0);

        Assertions.assertEquals(child.getCode(), result.getCode());
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

        Assertions.assertEquals(Long.valueOf(1), children.getCount());

        ServerOrganization result = children.getResultSet().get(0);

        Assertions.assertEquals(child.getCode(), result.getCode());
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

        Assertions.assertNotNull(child.getParent());

        child.removeParent(parent);

        Assertions.assertNull(child.getParent());
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

        Assertions.assertEquals(Long.valueOf(1), parent.getChildren().getCount());

        parent.removeChild(child);

        Assertions.assertEquals(Long.valueOf(0), parent.getChildren().getCount());
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

        Assertions.assertNotNull(result);

        Assertions.assertEquals(parent.getCode(), result.getCode());
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

          Assertions.assertEquals(3, ancestors.size());
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

        Assertions.assertEquals(parent.getCode(), tree.getObject().getCode());
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

          Assertions.assertNotNull(result);

          results = result.get("children").getAsJsonArray();

          Assertions.assertEquals(1, results.size());

          result = results.get(0).getAsJsonObject();

          Assertions.assertEquals(parent.getCode(), result.get(Organization.CODE).getAsString());

          results = result.get("children").getAsJsonArray();

          Assertions.assertEquals(1, results.size());

          result = results.get(0).getAsJsonObject();

          Assertions.assertEquals(child.getCode(), result.get(Organization.CODE).getAsString());
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
  public void testImportJson() throws Exception
  {
    try (InputStream resource = this.getClass().getResourceAsStream("/org-tree.json"))
    {
      JsonArray array = JsonParser.parseReader(new InputStreamReader(resource)).getAsJsonArray();

      this.service.importJsonTree(array);

      ServerOrganization child = ServerOrganization.getByCode(CHILD_CODE);

      Assertions.assertNotNull(child);

      this.service.delete(child);

      ServerOrganization parent = ServerOrganization.getByCode(PARENT_CODE);

      Assertions.assertNotNull(parent);

      this.service.delete(parent);

      ServerOrganization grandParent = ServerOrganization.getByCode(GRANDPARENT_CODE);

      Assertions.assertNotNull(grandParent);

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
