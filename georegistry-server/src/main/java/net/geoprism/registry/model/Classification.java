package net.geoprism.registry.model;

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.registry.CannotDeleteClassificationWithChildrenException;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.query.graph.AbstractVertexRestriction;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class Classification implements JsonSerializable
{
  private ClassificationType type;

  private VertexObject       vertex;

  public Classification(ClassificationType type, VertexObject vertex)
  {
    this.type = type;
    this.vertex = vertex;
  }

  public VertexObject getVertex()
  {
    return vertex;
  }

  public String getOid()
  {
    return this.getVertex().getOid();
  }

  public void setCode(String code)
  {
    this.getVertex().setValue(AbstractClassification.CODE, code);
  }

  public String getCode()
  {
    return this.getVertex().getObjectValue(AbstractClassification.CODE);
  }

  public LocalizedValue getDisplayLabel()
  {
    return LocalizedValueConverter.convert(this.vertex.getEmbeddedComponent(AbstractClassification.DISPLAYLABEL));
  }

  public void setDisplayLabel(LocalizedValue displayLabel)
  {
    LocalizedValueConverter.populate(this.vertex, AbstractClassification.DISPLAYLABEL, displayLabel);
  }

  public LocalizedValue getDescription()
  {
    return LocalizedValueConverter.convert(this.vertex.getEmbeddedComponent(AbstractClassification.DESCRIPTION));
  }

  public void setDescription(LocalizedValue description)
  {
    LocalizedValueConverter.populate(this.vertex, AbstractClassification.DESCRIPTION, description);
  }

  public void populate(JsonObject object)
  {
    this.setCode(object.get(AbstractClassification.CODE).getAsString());

    LocalizedValue displayLabel = LocalizedValue.fromJSON(object.get(AbstractClassification.DISPLAYLABEL).getAsJsonObject());
    this.setDisplayLabel(displayLabel);

    LocalizedValue description = LocalizedValue.fromJSON(object.get(AbstractClassification.DESCRIPTION).getAsJsonObject());
    this.setDescription(description);
  }

  public void populate(Term term)
  {
    this.setCode(term.getCode());
    this.setDisplayLabel(term.getLabel());
    this.setDescription(term.getDescription());
  }

  @Transaction
  public void apply(Classification parent)
  {
    boolean isNew = this.getVertex().isNew() && !this.getVertex().isAppliedToDb();

    this.getVertex().apply();

    if (isNew)
    {
      if (parent != null)
      {
        this.addParent(parent);
      }
      else
      {
        this.type.setRoot(this);
      }
    }
  }

  @Transaction
  public void delete()
  {
    if (this.getChildren(null, null).getCount() > 0)
    {
      throw new CannotDeleteClassificationWithChildrenException();
    }

    this.getVertex().delete();
  }

  @Transaction
  public void addParent(Classification parent)
  {
    if (this.getVertex().isNew() || !this.exists(parent))
    {
      EdgeObject edge = this.getVertex().addParent(parent.getVertex(), this.type.getMdEdge());
      edge.apply();
    }
  }

  @Transaction
  public void addChild(Classification child)
  {
    child.addParent(this);
  }

  @Transaction
  public void removeParent(Classification parent)
  {
    this.getVertex().removeParent(parent.getVertex(), this.type.getMdEdge());
  }

  @Transaction
  public void removeChild(Classification child)
  {
    child.removeParent(this);
  }

  @Transaction
  public void move(Classification newParent)
  {
    this.getParents().forEach(parent -> {
      this.removeParent(parent);
    });

    this.addParent(newParent);
  }

  public Page<Classification> getChildren()
  {
    return this.getChildren(20, 1);
  }

  public Page<Classification> getChildren(Integer pageSize, Integer pageNumber)
  {
    StringBuilder cStatement = new StringBuilder();
    cStatement.append("SELECT out('" + this.type.getMdEdge().getDBClassName() + "').size()");
    cStatement.append(" FROM :rid");

    GraphQuery<Integer> cQuery = new GraphQuery<Integer>(cStatement.toString());
    cQuery.setParameter("rid", this.getVertex().getRID());

    Integer count = cQuery.getSingleResult();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(out('" + this.type.getMdEdge().getDBClassName() + "')");
    statement.append(") FROM :rid");
    statement.append(" ORDER BY code");

    if (pageSize != null && pageNumber != null)
    {
      int first = pageSize * ( pageNumber - 1 );
      int rows = pageSize;

      statement.append(" SKIP " + first + " LIMIT " + rows);
    }

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("rid", this.getVertex().getRID());

    List<Classification> results = query.getResults().stream().map(vertex -> {
      return new Classification(this.type, vertex);
    }).collect(Collectors.toList());

    return new Page<Classification>(count, pageNumber, pageSize, results);
  }

  public List<Classification> getParents()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(in('" + this.type.getMdEdge().getDBClassName() + "')");
    statement.append(") FROM :rid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("rid", this.getVertex().getRID());

    List<Classification> results = query.getResults().stream().map(vertex -> {
      return new Classification(this.type, vertex);
    }).collect(Collectors.toList());

    return results;
  }

  public List<Classification> getAncestors(String rootCode)
  {
    GraphQuery<VertexObject> query = null;

    if (rootCode != null && rootCode.length() > 0)
    {
      StringBuilder statement = new StringBuilder();
      statement.append("SELECT expand($res)");
      statement.append(" LET $a = (TRAVERSE in(\"" + this.type.getMdEdge().getDBClassName() + "\") FROM :rid WHILE (code != :code))");
      statement.append(", $b = (SELECT FROM " + this.type.getMdVertex().getDBClassName() + " WHERE code = :code)");
      statement.append(", $res = (UNIONALL($a,$b))");

      query = new GraphQuery<VertexObject>(statement.toString());
      query.setParameter("rid", this.vertex.getRID());
      query.setParameter("code", rootCode);
    }
    else
    {
      StringBuilder statement = new StringBuilder();
      statement.append("TRAVERSE in(\"" + this.type.getMdEdge().getDBClassName() + "\") FROM :rid");

      query = new GraphQuery<VertexObject>(statement.toString());
      query.setParameter("rid", this.vertex.getRID());
    }

    List<Classification> results = query.getResults().stream().map(vertex -> {
      return new Classification(this.type, vertex);
    }).collect(Collectors.toList());

    return results;
  }

  private boolean exists(Classification parent)
  {
    EdgeObject edge = this.getEdge(parent);

    return ( edge != null );
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty(AbstractClassification.CODE, (String) this.vertex.getObjectValue(AbstractClassification.CODE));
    object.add(AbstractClassification.DISPLAYLABEL, this.getDisplayLabel().toJSON());
    object.add(AbstractClassification.DESCRIPTION, this.getDescription().toJSON());

    return object;
  }

  @Override
  public String toString()
  {
    return this.getCode();
  }

  public EdgeObject getEdge(Classification parent)
  {
    String statement = "SELECT FROM " + this.type.getMdEdge().getDBClassName();
    statement += " WHERE out = :parent";
    statement += " AND in = :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", parent.getVertex().getRID());
    query.setParameter("child", this.getVertex().getRID());

    return query.getSingleResult();
  }

  public Term toTerm()
  {
    return new Term(this.getCode(), this.getDisplayLabel(), this.getDescription());
  }

  public ClassificationNode getAncestorTree(String rootCode, Integer pageSize)
  {
    List<Classification> ancestors = this.getAncestors(rootCode);

    ClassificationNode prev = null;

    for (Classification ancestor : ancestors)
    {
      Page<Classification> page = ancestor.getChildren(pageSize, 1);

      List<ClassificationNode> transform = page.getResults().stream().map(r -> {
        return new ClassificationNode(r);
      }).collect(Collectors.toList());

      if (prev != null)
      {
        int index = transform.indexOf(prev);

        if (index != -1)
        {
          transform.set(index, prev);
        }
        else
        {
          transform.add(prev);
        }
      }

      ClassificationNode node = new ClassificationNode();
      node.setClassification(ancestor);
      node.setChildren(new Page<ClassificationNode>(page.getCount(), page.getPageNumber(), page.getPageSize(), transform));

      prev = node;
    }

    return prev;
  }

  public static Classification get(ClassificationType type, String code)
  {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + type.getMdVertex().getDBClassName());
    builder.append(" WHERE code = :code");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());
    query.setParameter("code", code);

    VertexObject result = query.getSingleResult();

    if (result != null)
    {
      return new Classification(type, result);
    }

    return null;
  }

  public static Classification getByOid(ClassificationType type, String oid)
  {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + type.getMdVertex().getDBClassName());
    builder.append(" WHERE oid = :oid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());
    query.setParameter("oid", oid);

    VertexObject result = query.getSingleResult();

    if (result != null)
    {
      return new Classification(type, result);
    }

    return null;
  }

  public static Classification newInstance(ClassificationType type)
  {
    VertexObjectDAO dao = VertexObjectDAO.newInstance(type.getMdVertex());

    return new Classification(type, VertexObject.instantiate(dao));
  }

  public static Classification construct(ClassificationType type, JsonObject object, boolean isNew)
  {
    if (isNew)
    {
      return Classification.newInstance(type);
    }

    String code = object.get(AbstractClassification.CODE).getAsString();

    return Classification.get(type, code);
  }

  public static List<Classification> search(ClassificationType type, String rootCode, String text)
  {
    StringBuilder builder = new StringBuilder();

    if (rootCode != null && rootCode.length() > 0)
    {
      builder.append("SELECT FROM (TRAVERSE out(\"" + type.getMdEdge().getDBClassName() + "\") FROM :rid) ");
    }
    else
    {
      builder.append("SELECT FROM " + type.getMdVertex().getDBClassName());
    }

    if (text != null)
    {
      builder.append(" WHERE (code.toUpperCase() LIKE :text");
      builder.append(" OR " + AbstractVertexRestriction.localize("displayLabel") + ".toUpperCase() LIKE :text)");
    }

    builder.append(" ORDER BY code");
    builder.append(" LIMIT 10");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(builder.toString());

    if (text != null)
    {
      query.setParameter("text", "%" + text.toUpperCase() + "%");
    }

    if (rootCode != null && rootCode.length() > 0)
    {
      Classification root = Classification.get(type, rootCode);
      query.setParameter("rid", root.getVertex().getRID());
    }

    List<Classification> results = query.getResults().stream().map(vertex -> {
      return new Classification(type, vertex);
    }).collect(Collectors.toList());

    return results;
  }

  public static Classification get(AttributeClassificationType attribute, String code)
  {
    String classificationTypeCode = attribute.getClassificationType();
    ClassificationType type = ClassificationType.getByCode(classificationTypeCode);

    return Classification.get(type, code);
  }

}
