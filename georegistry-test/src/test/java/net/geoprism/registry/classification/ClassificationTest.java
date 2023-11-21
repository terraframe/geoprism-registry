/**
 *
 */
package net.geoprism.registry.classification;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.runwaysdk.session.Request;

import net.geoprism.registry.InstanceTestClassListener;
import net.geoprism.registry.SpringInstanceTestClassRunner;
import net.geoprism.registry.TestConfig;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationNode;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.service.business.ClassificationBusinessServiceIF;
import net.geoprism.registry.service.business.ClassificationTypeBusinessServiceIF;
import net.geoprism.registry.view.Page;

@ContextConfiguration(classes = { TestConfig.class })
@RunWith(SpringInstanceTestClassRunner.class)
public class ClassificationTest implements InstanceTestClassListener
{
  private static String                       GRANDPARENT_CODE = "GRANDPARENT_OBJ";

  private static String                       PARENT_CODE      = "PARENT_OBJ";

  private static String                       CHILD_CODE       = "CHILD_OBJ";

  private static ClassificationType           type;

  @Autowired
  private ClassificationTypeBusinessServiceIF typeService;

  @Autowired
  private ClassificationBusinessServiceIF     service;

  @Override
  public void beforeClassSetup() throws Exception
  {
    setUpClassInRequest();
  }

  @Request
  private void setUpClassInRequest()
  {
    type = this.typeService.apply(ClassificationTypeTest.createMock());
  }

  @Override
  public void afterClassSetup() throws Exception
  {
    cleanUpClassInRequest();
  }

  @Request
  private void cleanUpClassInRequest()
  {
    if (type != null)
    {
      this.typeService.delete(type);
    }
  }

  @Test
  @Request
  public void testBasicCreate()
  {
    Classification object = this.service.newInstance(type);
    object.setCode(PARENT_CODE);
    this.service.apply(object, null);

    try
    {
      Assert.assertNotNull(object.getVertex().getRID());
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
    Classification object = this.service.newInstance(type);
    object.setCode(PARENT_CODE);
    this.service.apply(object, null);

    try
    {
      Classification result = this.service.get(type, object.getCode());

      Assert.assertEquals(object.getOid(), result.getOid());
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
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    this.service.apply(parent, null);

    try
    {
      Classification child = this.service.newInstance(type);
      child.setCode(CHILD_CODE);
      this.service.apply(child, null);

      try
      {
        this.service.addChild(parent, child);

        Page<Classification> children = this.service.getChildren(parent, 20, 1);

        Assert.assertEquals(Long.valueOf(1), children.getCount());

        Classification result = children.getResults().get(0);

        Assert.assertEquals(child.getOid(), result.getOid());
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
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    this.service.apply(parent, null);

    try
    {
      Classification child = this.service.newInstance(type);
      child.setCode(CHILD_CODE);
      this.service.apply(child, parent);

      try
      {
        Page<Classification> children = this.service.getChildren(parent, 20, 1);

        Assert.assertEquals(Long.valueOf(1), children.getCount());

        Classification result = children.getResults().get(0);

        Assert.assertEquals(child.getOid(), result.getOid());
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
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    this.service.apply(parent, null);

    try
    {
      Classification child = this.service.newInstance(type);
      child.setCode(CHILD_CODE);
      this.service.apply(child, null);

      try
      {
        this.service.addParent(child, parent);

        Assert.assertEquals(1, this.service.getParents(child).size());

        this.service.removeParent(child, parent);

        Assert.assertEquals(0, this.service.getParents(child).size());
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
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    this.service.apply(parent, null);

    try
    {
      Classification child = this.service.newInstance(type);
      child.setCode(CHILD_CODE);
      this.service.apply(child, null);

      try
      {
        this.service.addChild(parent, child);

        Assert.assertEquals(Long.valueOf(1), this.service.getChildren(parent).getCount());

        this.service.removeChild(parent, child);

        Assert.assertEquals(Long.valueOf(0), this.service.getChildren(parent).getCount());
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
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    this.service.apply(parent, null);

    try
    {
      Classification child = this.service.newInstance(type);
      child.setCode(CHILD_CODE);
      this.service.apply(child, null);

      try
      {
        this.service.addParent(child, parent);

        List<Classification> parents = this.service.getParents(child);

        Assert.assertEquals(1, parents.size());

        Classification result = parents.get(0);

        Assert.assertEquals(parent.getOid(), result.getOid());
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
    Classification grandParent = this.service.newInstance(type);
    grandParent.setCode(GRANDPARENT_CODE);
    this.service.apply(grandParent, null);

    try
    {
      Classification parent = this.service.newInstance(type);
      parent.setCode(PARENT_CODE);
      this.service.apply(parent, grandParent);

      try
      {
        Classification child = this.service.newInstance(type);
        child.setCode(CHILD_CODE);
        this.service.apply(child, parent);

        try
        {
          List<Classification> ancestors = this.service.getAncestors(child, null);

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
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    this.service.apply(parent, null);

    try
    {
      Classification child = this.service.newInstance(type);
      child.setCode(CHILD_CODE);
      this.service.apply(child, parent);

      try
      {
        ClassificationNode tree = this.service.getAncestorTree(child, null, 200);

        Assert.assertEquals(parent.getOid(), tree.getClassification().getOid());
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
  public void testSearch()
  {
    Classification parent = this.service.newInstance(type);
    parent.setCode(PARENT_CODE);
    parent.setDisplayLabel(new LocalizedValue("Test Parent"));
    this.service.apply(parent, null);

    try
    {
      Assert.assertEquals(1, this.service.search(type, null, PARENT_CODE.toLowerCase()).size());
      Assert.assertEquals(1, this.service.search(type, null, parent.getDisplayLabel().getValue()).size());
      Assert.assertEquals(1, this.service.search(type, null, "test").size());
      Assert.assertEquals(1, this.service.search(type, null, null).size());
      Assert.assertEquals(0, this.service.search(type, null, "ARG-BARG").size());
    }
    finally
    {
      this.service.delete(parent);
    }
  }

}
