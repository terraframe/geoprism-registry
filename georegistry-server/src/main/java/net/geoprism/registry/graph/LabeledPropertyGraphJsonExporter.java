/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.graph;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectTypeSnapshot;
import net.geoprism.registry.HierarchyTypeSnapshot;
import net.geoprism.registry.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;

public class LabeledPropertyGraphJsonExporter
{
  final private Logger                          logger = LoggerFactory.getLogger(LabeledPropertyGraphJsonExporter.class);

  final private LabeledPropertyGraphTypeVersion version;

  public LabeledPropertyGraphJsonExporter(LabeledPropertyGraphTypeVersion version)
  {
    this.version = version;
  }

  public JsonObject export()
  {
    JsonArray geoObjects = new JsonArray();
    JsonArray edges = new JsonArray();

    List<GeoObjectTypeSnapshot> types = version.getTypes().stream().filter(type -> !type.isRoot()).collect(Collectors.toList());
    List<HierarchyTypeSnapshot> hierarchies = version.getHierarchies();

    for (GeoObjectTypeSnapshot type : types)
    {

      final int BLOCK_SIZE = 2000;

      VertexObject prev = null;

      MdVertex mdVertex = type.getGraphMdVertex();

      do
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT FROM " + mdVertex.getDbClassName());

        if (prev != null)
        {
          statement.append(" WHERE @rid > :rid");
        }

        statement.append(" LIMIT " + BLOCK_SIZE);

        GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

        if (prev != null)
        {
          query.setParameter("rid", prev.getRID());
        }

        List<VertexObject> objects = query.getResults();

        prev = null;

        for (VertexObject object : objects)
        {
          GeoObject geoObject = this.serialize(object, type);

          geoObjects.add(geoObject.toJSON());

          for (HierarchyTypeSnapshot hierarchy : hierarchies)
          {
            MdEdgeDAOIF mdEdge = MdEdgeDAO.get(hierarchy.getGraphMdEdgeOid());

            object.getChildren(mdEdge, VertexObject.class).forEach(child -> {

              MdVertexDAOIF mdClass = (MdVertexDAOIF) child.getMdClass();

              GeoObjectTypeSnapshot childType = types.stream().filter(t -> {
                return t.getGraphMdVertexOid().equals(mdClass.getOid());
              }).findAny().orElseThrow(() -> {
                throw new ProgrammingErrorException("Unable to find end type");
              });

              JsonObject jsonEdge = new JsonObject();
              jsonEdge.addProperty("startNode", geoObject.getUid());
              jsonEdge.addProperty("startType", geoObject.getType().getCode());
              jsonEdge.addProperty("endNode", (String) child.getObjectValue(RegistryConstants.UUID));
              jsonEdge.addProperty("endType", childType.getCode());

              jsonEdge.addProperty("type", hierarchy.getCode());

              edges.add(jsonEdge);
            });

          }

          Thread.yield();
        }
      } while (prev != null);
    }

    JsonObject graph = new JsonObject();
    graph.add("geoObjects", geoObjects);
    graph.add("edges", edges);

    return graph;
  }

  protected GeoObject serialize(VertexObject vertex, GeoObjectTypeSnapshot snapshot)
  {
    GeoObjectType type = snapshot.toGeoObjectType();

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type);

    GeoObject geoObj = new GeoObject(type, type.getGeometryType(), attributeMap);

    Map<String, AttributeType> attributes = type.getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.TYPE.getName()))
      {
        // Ignore
      }
      else if (vertex.hasAttribute(attributeName))
      {
        Object value = vertex.getObjectValue(attributeName);

        if (value != null)
        {
          if (attribute instanceof AttributeTermType)
          {
            Classifier classifier = Classifier.get((String) value);

            try
            {
              geoObj.setValue(attributeName, classifier.getClassifierId());
            }
            catch (UnknownTermException e)
            {
              TermValueException ex = new TermValueException();
              ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
              ex.setCode(e.getCode());

              throw e;
            }
          }
          else if (attribute instanceof AttributeClassificationType)
          {
            String classificationTypeCode = ( (AttributeClassificationType) attribute ).getClassificationType();
            ClassificationType classificationType = ClassificationType.getByCode(classificationTypeCode);
            Classification classification = Classification.getByOid(classificationType, (String) value);

            try
            {
              geoObj.setValue(attributeName, classification.toTerm());
            }
            catch (UnknownTermException e)
            {
              TermValueException ex = new TermValueException();
              ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
              ex.setCode(e.getCode());

              throw e;
            }
          }
          else
          {
            geoObj.setValue(attributeName, value);
          }
        }
      }
    });

    geoObj.setUid(vertex.getObjectValue(RegistryConstants.UUID));
    geoObj.setCode(vertex.getObjectValue(DefaultAttribute.CODE.getName()));
    geoObj.setGeometry(vertex.getObjectValue(DefaultAttribute.GEOMETRY.getName()));
    geoObj.setDisplayLabel(LocalizedValueConverter.convert(vertex.getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName())));
    geoObj.setExists(true);
    geoObj.setInvalid(false);

    return geoObj;
  }

}
