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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdGraphClassDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
import com.runwaysdk.dataaccess.metadata.MdAttributeBooleanDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeEnumerationDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.dataaccess.metadata.graph.MdGeoVertexDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;

import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.graph.GeoVertexType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class PatchExists
{
  public static final String STATUS_ATTRIBUTE_NAME = "status";
  
  private static final Logger logger = LoggerFactory.getLogger(PatchExists.class);
  
  private static final Date today = new Date();
  
  public static void main(String[] args)
  {
    new PatchExists().doItInReq();
  }
  
  @Request
  private void doItInReq()
  {
    doItInTrans();
  }

  @Transaction
  private void doItInTrans()
  {
    List<Universal> unis = InvalidPatch.getUniversals();

    for (Universal uni : unis)
    {
      MdBusinessDAO bizDAO = (MdBusinessDAO) BusinessFacade.getEntityDAO(uni.getMdBusiness());
      MdAttributeConcreteDAO postgresStatusAttr = (MdAttributeConcreteDAO) bizDAO.definesAttribute(STATUS_ATTRIBUTE_NAME);
      if (postgresStatusAttr != null)
      {
        postgresStatusAttr.delete();
      }
      
      MdGeoVertexDAO mdVertex = GeoVertexType.getMdGeoVertex(uni.getUniversalId());
      
      MdAttributeEnumerationDAO statusMdAttr = (MdAttributeEnumerationDAO) mdVertex.definesAttribute(STATUS_ATTRIBUTE_NAME);
      statusMdAttr.delete();
    }
    
    int applied = 0;
    
    for (Universal uni : unis)
    {
      ServerGeoObjectType type = new ServerGeoObjectTypeConverter().build(uni);
      
      MdGraphClassDAOIF mdClass = type.getMdVertex();
      
      MdAttributeDAOIF existing = mdClass.definesAttribute(DefaultAttribute.EXISTS.getName());
      
      if (existing == null)
      {
        logger.info("Adding exists attribute to [" + mdClass.getKey() + "].");
        
        MdAttributeBooleanDAO existsMdAttr = MdAttributeBooleanDAO.newInstance();
        existsMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.EXISTS.getName());
        existsMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.EXISTS.getDefaultLocalizedName());
        existsMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.EXISTS.getDefaultDescription());
        existsMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdClass.getOid());
        existsMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.FALSE);
        existsMdAttr.setValue(MdAttributeConcreteInfo.DEFAULT_VALUE, MdAttributeBooleanInfo.FALSE);
        existsMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.NON_UNIQUE_INDEX.getOid());
        existsMdAttr.apply();
        
        List<VertexServerGeoObject> data = getInstanceData(type, mdClass);
        
        int current = 0;
        final int size = data.size();
        
        logger.info("Starting to patch instance data for type [" + mdClass.getDBClassName() + "] with count [" + size + "] ");
        
        for (VertexServerGeoObject go : data)
        {
          ValueOverTime defaultExists = go.buildDefaultExists();
          
          if (defaultExists != null)
          {
            go.setValue(DefaultAttribute.EXISTS.getName(), Boolean.TRUE, defaultExists.getStartDate(), defaultExists.getEndDate());
            
            // This apply method is mega slow due to the SearchService so we're going to just bypass it
//            go.apply(false);
            
            go.getVertex().setValue(GeoVertex.LASTUPDATEDATE, new Date());
            go.getVertex().apply();
            
            applied++;
          }
          
          if (current % 10 == 0)
          {
            logger.info("Finished record " + current + " of " + size);
          }
          
          current++;
        }
        
        // TODO : Patch masterlist tables
      }
    }
    
    logger.info("Applied " + applied + " records across " + unis.size() + " types.");
  }
  
  private List<VertexServerGeoObject> getInstanceData(ServerGeoObjectType type, MdGraphClassDAOIF mdClass)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdClass.getDBClassName());

    GraphQuery<VertexObject> vertexQuery = new GraphQuery<VertexObject>(statement.toString(), new HashMap<String, Object>());
    
    List<VertexServerGeoObject> list = new LinkedList<VertexServerGeoObject>();

    List<VertexObject> results = vertexQuery.getResults();

    for (VertexObject result : results)
    {
      list.add(new VertexServerGeoObject(type, result, today));
    }

    return list;
  }
}
