/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.graph.GraphRequest;
import com.runwaysdk.dataaccess.graph.orientdb.OrientDBRequest;
import com.runwaysdk.dataaccess.metadata.MdAttributeBooleanDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.MdGeoVertexDAOIF;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;

import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.GeoVertexType;

public class InvalidPatch
{
  private static final Logger logger = LoggerFactory.getLogger(InvalidPatch.class);
  
  public static void main(String[] args)
  {
    new InvalidPatch().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    doItInTrans();
  }

  @Transaction
  private void doItInTrans()
  {
    List<Universal> unis = getUniversals();

    for (Universal uni : unis)
    {
      MdGeoVertexDAO mdVertex = GeoVertexType.getMdGeoVertex(uni.getUniversalId());
      
      MdAttributeDAOIF existing = mdVertex.definesAttribute(DefaultAttribute.INVALID.getName());
      
      if (existing == null)
      {
        logger.info("Adding invalid attribute to [" + mdVertex.getKey() + "].");
        
        MdAttributeBooleanDAO invalidMdAttr = MdAttributeBooleanDAO.newInstance();
        invalidMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.INVALID.getName());
        invalidMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.INVALID.getDefaultLocalizedName());
        invalidMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.INVALID.getDefaultDescription());
        invalidMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdVertex.getOid());
        invalidMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
        invalidMdAttr.setValue(MdAttributeConcreteInfo.DEFAULT_VALUE, MdAttributeBooleanInfo.FALSE);
        invalidMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
        invalidMdAttr.apply();
        
        StringBuilder statement = new StringBuilder();
        statement.append("UPDATE " + mdVertex.getDBClassName() + " SET invalid=false");
        
        logger.info("Executing [" + statement.toString() + "].");

        GraphDBService service = GraphDBService.getInstance();
        GraphRequest request = service.getGraphDBRequest();

        ODatabaseSession db = ((OrientDBRequest) request).getODatabaseSession();
        
        try (OResultSet command = db.command(statement.toString(), new HashMap<String,Object>()))
        {
          logger.info(command.toString());
        }
        
        // TODO : Patch masterlist tables
      }
    }
  }
  
  public static List<Universal> getUniversals()
  {
    QueryFactory qf = new QueryFactory();
    UniversalQuery uq = new UniversalQuery(qf);
    
    @SuppressWarnings("unchecked")
    List<Universal> unis = (List<Universal>) uq.getIterator().getAll();
    
    Iterator<Universal> it = unis.iterator();
    
    while (it.hasNext())
    {
      Universal uni = it.next();
      
      if (uni.getKey().equals(Universal.ROOT_KEY))
      {
        it.remove();
        continue;
      }
      
      MdGeoVertexDAOIF superType = GeoVertexType.getMdGeoVertex(uni.getUniversalId()).getSuperClass();
      
      if (superType != null && !superType.definesType().equals(GeoVertex.CLASS))
      {
        it.remove();
        continue;
      }
    }
    
    return unis;
  }
}
