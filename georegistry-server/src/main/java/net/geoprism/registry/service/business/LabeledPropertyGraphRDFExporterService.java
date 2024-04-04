/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.service.business;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.MdClassDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.GraphTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;

@Service
public class LabeledPropertyGraphRDFExporterService
{
  private Model model;
  
  private static class TypeSnapshotCacheObject
  {
    private GeoObjectType type;

    public TypeSnapshotCacheObject(GeoObjectTypeSnapshot snapshot)
    {
      this.type = snapshot.toGeoObjectType();
    }

  }

  @Autowired
  private LabeledPropertyGraphTypeVersionBusinessServiceIF versionService;

  @Autowired
  private GeoObjectTypeSnapshotBusinessServiceIF           typeService;

  private GeoObjectType getType(Map<String, TypeSnapshotCacheObject> cache, LabeledPropertyGraphTypeVersion version, MdClassDAOIF mdClass)
  {

    if (!cache.containsKey(mdClass.getOid()))
    {
      GeoObjectTypeSnapshot snapshot = this.typeService.get(version, (MdVertexDAOIF) mdClass);

      cache.put(mdClass.getOid(), new TypeSnapshotCacheObject(snapshot));
    }

    GeoObjectType type = cache.get(mdClass.getOid()).type;
    return type;
  }

  public long generateStatements(LabeledPropertyGraphTypeVersion version, Long skip, Integer blockSize)
  {
    return this.generateStatements(new HashMap<>(), version, skip, blockSize);
  }

  private long generateStatements(Map<String, TypeSnapshotCacheObject> cache, LabeledPropertyGraphTypeVersion version, Long skip, Integer blockSize)
  {
    GraphTypeSnapshot snapshot = this.versionService.getGraphSnapshots(version).get(0);
    MdEdge mdEdge = snapshot.getGraphMdEdge();
    
    GeoObjectTypeSnapshot rootType = this.versionService.getRootType(version);
    MdVertex mdVertex = rootType.getGraphMdVertex();
    
//    StringBuilder statement = new StringBuilder();
//    statement.append("SELECT out.uid AS parentUid, out.@class AS parentClass, in.uid AS childUid, in.@class AS childClass FROM " + mdEdge.getDbClassName());
//    statement.append(" ORDER BY oid");
//    statement.append(" SKIP " + skip);
//    statement.append(" LIMIT " + blockSize);
//
//    GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
//    
//    for (VertexObject object : objects)
//    {
//      // Get the type of the geo object
//      GeoObjectType type = getType(cache, version, object.getMdClass());
//      GeoObject geoObject = this.typeService.toGeoObject(object, type);
//
//      geoObjects.add(geoObject.toJSON());
//    }
    
    
    
    
    
//    StringBuilder statement = new StringBuilder();
//    statement.append("SELECT " + VertexAndEdgeResultSetConverter.geoVertexColumns((MdGraphClassDAOIF) mdVertex.businessDAO()) + ", " + VertexAndEdgeResultSetConverter.geoVertexAttributeColumns(rootType.getAttributeTypes()) + ", edgeClass, edgeOid FROM ( ");
//    statement.append("SELECT v, v.out('" + EdgeConstant.HAS_VALUE.getDBClassName() + "', '" + EdgeConstant.HAS_GEOMETRY.getDBClassName() + "') as attr, edgeClass, edgeOid FROM ( ");
//    statement.append("SELECT in as v, @class as edgeClass, oid as edgeOid FROM ( ");
//    statement.append("SELECT EXPAND( outE( ");
//    statement.append("'" + mdEdge.getDbClassName() + "')");
//    statement.append(" ) FROM :rid");
//    statement.append(" )) UNWIND attr )");
    
    
    return -1l;
  }

  public void export(LabeledPropertyGraphTypeVersion version, OutputStream os)
  {
    model = ModelFactory.createDefaultModel();
    
    Map<String, TypeSnapshotCacheObject> cache = new HashMap<>();

    final int BLOCK_SIZE = 2000;
    long skip = 0;
    long count = 0;

    do
    {
      count = this.generateStatements(cache, version, skip, BLOCK_SIZE);

      skip += BLOCK_SIZE;
    } while (count > 0);
    
    model.write(os);
  }

}
