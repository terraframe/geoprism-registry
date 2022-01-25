package net.geoprism.registry.model;

import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.AbstractClassification;

import net.geoprism.registry.CannotDeleteClassificationWithChildrenException;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.view.JsonSerializable;

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

  public LocalizedValue getDescription()
  {
    return LocalizedValueConverter.convert(this.vertex.getEmbeddedComponent(AbstractClassification.DESCRIPTION));
  }

  public void populate(JsonObject object)
  {
    this.setCode(object.get(AbstractClassification.CODE).getAsString());

    LocalizedValue displayLabel = LocalizedValue.fromJSON(object.get(AbstractClassification.DISPLAYLABEL).getAsJsonObject());
    LocalizedValueConverter.populate(this.vertex, AbstractClassification.DISPLAYLABEL, displayLabel);

    LocalizedValue description = LocalizedValue.fromJSON(object.get(AbstractClassification.DESCRIPTION).getAsJsonObject());
    LocalizedValueConverter.populate(this.vertex, AbstractClassification.DESCRIPTION, description);
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
    if (this.getChildren().size() > 0)
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

  public List<Classification> getChildren()
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT EXPAND(out('" + this.type.getMdEdge().getDBClassName() + "')");
    statement.append(") FROM :rid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("rid", this.getVertex().getRID());

    List<Classification> results = query.getResults().stream().map(vertex -> {
      return new Classification(this.type, vertex);
    }).collect(Collectors.toList());

    return results;
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

}
