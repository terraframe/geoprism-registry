/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.graph.transition;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.Roles;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.task.Task;
import net.geoprism.registry.task.Task.TaskType;

public class Transition extends TransitionBase
{
  private static final long serialVersionUID = 1506268214;

  public static enum TransitionImpact {
    PARTIAL, FULL;
  }

  public static enum TransitionType {
    MERGE, SPLIT, REASSIGN, UPGRADE_MERGE, UPGRADE_SPLIT, UPGRADE, DOWNGRADE_MERGE, DOWNGRADE_SPLIT, DOWNGRADE;

    public boolean isReassign()
    {
      return this.equals(TransitionType.REASSIGN) || this.equals(TransitionType.UPGRADE) || this.equals(TransitionType.DOWNGRADE);
    }

    public boolean isMerge()
    {
      return this.equals(TransitionType.MERGE) || this.equals(TransitionType.UPGRADE_MERGE) || this.equals(TransitionType.DOWNGRADE_MERGE);
    }

    public boolean isSplit()
    {
      return this.equals(TransitionType.SPLIT) || this.equals(TransitionType.UPGRADE_SPLIT) || this.equals(TransitionType.DOWNGRADE_SPLIT);
    }
  }

  public Transition()
  {
    super();
  }

  @Override
  public void delete()
  {
    super.delete();

    Task.removeTasks(this.getOid());
  }

  public JsonObject toJSON()
  {
    VertexServerGeoObject source = this.getSourceVertex();
    VertexServerGeoObject target = this.getTargetVertex();

    JsonObject object = new JsonObject();
    object.addProperty(OID, this.getOid());
    object.addProperty(Transition.ORDER, this.getOrder());
    object.addProperty("isNew", this.isNew());
    object.addProperty("sourceCode", source.getCode());
    object.addProperty("sourceType", source.getType().getCode());
    object.addProperty("sourceText", source.getLabel() + " (" + source.getCode() + ")");
    object.addProperty("targetCode", target.getCode());
    object.addProperty("targetType", target.getType().getCode());
    object.addProperty("targetText", target.getLabel() + " (" + target.getCode() + ")");
    object.addProperty(Transition.TRANSITIONTYPE, this.getTransitionType());
    object.addProperty(Transition.IMPACT, this.getImpact());

    return object;
  }

  @Transaction
  public void apply(TransitionEvent event, int order, VertexServerGeoObject source, VertexServerGeoObject target)
  {
    this.validate(event, source, target);

    this.setOrder(order);
    this.setValue(Transition.SOURCE, source.getVertex());
    this.setValue(Transition.TARGET, target.getVertex());

    boolean isModified = this.isModified(Transition.TARGET);
    boolean isNew = this.isNew() && !this.isAppliedToDb();

    super.apply();

    if (isNew || isModified)
    {
      Task.removeTasks(this.getOid());

      this.createTask(source, target, event.getEventDate());
    }
  }

  public void createTask(VertexServerGeoObject source, VertexServerGeoObject target, Date eventDate)
  {
    LocalizedValue dateValue = LocalizedValueConverter.convert(eventDate);

    TransitionType transitionType = this.toTransitionType();
    ServerGeoObjectType sourceType = source.getType();
    ServerGeoObjectType targetType = target.getType();

    List<ServerGeoObjectType> types = Arrays.asList(new ServerGeoObjectType[] { sourceType, targetType }).stream().distinct().collect(Collectors.toList());

    for (ServerGeoObjectType type : types)
    {
      List<ServerHierarchyType> hierarchies = type.getHierarchies();

      for (ServerHierarchyType hierarchy : hierarchies)
      {
        List<ServerGeoObjectType> children = type.getChildren(hierarchy);

        for (ServerGeoObjectType child : children)
        {
          List<Roles> roles = Arrays.asList(new String[] { child.getMaintainerRoleName(), child.getAdminRoleName() }).stream().distinct().map(name -> Roles.findRoleByName(name)).collect(Collectors.toList());

          HashMap<String, LocalizedValue> values = new HashMap<String, LocalizedValue>();
          values.put("1", source.getDisplayLabel());
          values.put("2", sourceType.getLabel());
          values.put("3", target.getDisplayLabel());
          values.put("4", targetType.getLabel());
          values.put("5", child.getLabel());
          values.put("6", hierarchy.getLabel());
          values.put("7", dateValue);

          TaskType taskType = Task.TaskType.SPLIT_EVENT_TASK;

          if (transitionType.isMerge())
          {
            taskType = Task.TaskType.MERGE_EVENT_TASK;
          }
          else if (transitionType.isReassign())
          {
            taskType = Task.TaskType.REASSIGN_EVENT_TASK;
          }

          Task.createNewTask(roles, taskType, values, this.getOid());
        }
      }
    }
  }

  public void setTransitionType(TransitionType value)
  {
    this.setTransitionType(value.name());
  }

  public TransitionType toTransitionType()
  {
    return TransitionType.valueOf(this.getTransitionType());
  }

  public void setImpact(TransitionImpact value)
  {
    this.setImpact(value.name());
  }

  public VertexServerGeoObject getSourceVertex()
  {
    return getVertex(SOURCE);
  }

  public VertexServerGeoObject getTargetVertex()
  {
    return getVertex(TARGET);
  }

  private VertexServerGeoObject getVertex(String attributeName)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT expand(" + mdAttribute.getColumnName() + ")");
    statement.append(" FROM :parent");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("parent", this.getRID());

    VertexObject vertex = query.getSingleResult();
    MdVertexDAOIF geoVertex = (MdVertexDAOIF) vertex.getMdClass();

    ServerGeoObjectType type = ServerGeoObjectType.get(geoVertex);

    return new VertexServerGeoObject(type, vertex);
  }

  public void validate(TransitionEvent event, VertexServerGeoObject source, VertexServerGeoObject target)
  {
    ServerGeoObjectType beforeType = ServerGeoObjectType.get(event.getBeforeTypeCode());
    ServerGeoObjectType afterType = ServerGeoObjectType.get(event.getAfterTypeCode());

    List<ServerGeoObjectType> beforeSubtypes = beforeType.getSubtypes();
    List<ServerGeoObjectType> afterSubtypes = afterType.getSubtypes();

    if (! ( beforeSubtypes.contains(source.getType()) || beforeType.getCode().equals(source.getType().getCode()) ))
    {
      // This should be prevented by the front-end
      throw new ProgrammingErrorException("Source type must be a subtype of (" + beforeType.getCode() + ")");
    }

    if (! ( afterSubtypes.contains(target.getType()) || afterType.getCode().equals(target.getType().getCode()) ))
    {
      // This should be prevented by the front-end
      throw new ProgrammingErrorException("Target type must be a subtype of (" + afterType.getCode() + ")");
    }
  }

  public static Transition apply(TransitionEvent event, int order, JsonObject object)
  {
    Transition transition = ( object.has("isNew") && object.get("isNew").getAsBoolean() ) ? new Transition() : Transition.get(object.get(OID).getAsString());
    transition.setTransitionType(object.get(Transition.TRANSITIONTYPE).getAsString());
    transition.setImpact(object.get(Transition.IMPACT).getAsString());

    if (transition.isNew())
    {
      transition.setValue(Transition.EVENT, event);
    }

    String sourceCode = object.get("sourceCode").getAsString();
    String sourceType = object.get("sourceType").getAsString();

    String targetCode = object.get("targetCode").getAsString();
    String targetType = object.get("targetType").getAsString();

    VertexServerGeoObject source = new VertexGeoObjectStrategy(ServerGeoObjectType.get(sourceType)).getGeoObjectByCode(sourceCode);
    VertexServerGeoObject target = new VertexGeoObjectStrategy(ServerGeoObjectType.get(targetType)).getGeoObjectByCode(targetCode);

    transition.apply(event, order, source, target);

    return transition;
  }

  @Transaction
  public static void removeAll(ServerGeoObjectType type)
  {
    MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(Transition.CLASS);
    MdAttributeDAOIF sourceAttribute = mdVertex.definesAttribute(Transition.SOURCE);
    MdAttributeDAOIF targetAttribute = mdVertex.definesAttribute(Transition.TARGET);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" WHERE " + sourceAttribute.getColumnName() + ".@class = :vertexClass");
    statement.append(" OR " + targetAttribute.getColumnName() + ".@class = :vertexClass");

    GraphQuery<Transition> query = new GraphQuery<Transition>(statement.toString());
    query.setParameter("vertexClass", type.getMdVertex().getDBClassName());

    List<Transition> results = query.getResults();
    results.forEach(event -> event.delete());
  }
}
