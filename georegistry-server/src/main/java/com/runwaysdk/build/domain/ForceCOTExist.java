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
package com.runwaysdk.build.domain;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBImpl;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.session.Request;

public class ForceCOTExist
{
  public static void main(String[] args)
  {
    new ForceCOTExist().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    GraphDBService service = GraphDBService.getInstance();
    GraphRequest req = service.getGraphDBRequest();
    ODatabaseSession db = ( (OrientDBRequest) req ).getODatabaseSession();
    
    OrientDBImpl.getOrCreateChangeOverTime(db);
    OrientDBImpl.getOrCreateEnumerationChangeOverTime(db);
    
    OSchema schema = db.getMetadata().getSchema();
    OClass linkClass = schema.getClass("embedded_local_value");
    OrientDBImpl.getOrCreateChangeOverTime(db, linkClass, OType.EMBEDDED);
  }
}
